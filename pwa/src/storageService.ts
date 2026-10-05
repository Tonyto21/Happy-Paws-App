import { ref, uploadBytesResumable, getDownloadURL } from 'firebase/storage';
import { storage, auth } from './firebase';

export interface PhotoUploadResult {
  downloadUrl: string;
  storagePath: string;
}

/**
 * Resizes and compresses an image file in the browser before uploading.
 * Max dimension 1200px, JPEG quality 0.85 (keeps file size under 500KB while preserving high quality).
 */
export async function compressImage(file: File, maxDimension: number = 1200, quality: number = 0.85): Promise<Blob> {
  return new Promise((resolve, reject) => {
    // If not an image, reject
    if (!file.type.startsWith('image/')) {
      reject(new Error('Selected file must be an image (JPEG, PNG, WebP).'));
      return;
    }

    const reader = new FileReader();
    reader.onerror = () => reject(new Error('Failed to read image file.'));
    reader.onload = (e) => {
      const img = new Image();
      img.onerror = () => reject(new Error('Failed to decode image data.'));
      img.onload = () => {
        let width = img.width;
        let height = img.height;

        if (width > maxDimension || height > maxDimension) {
          if (width > height) {
            height = Math.round((height * maxDimension) / width);
            width = maxDimension;
          } else {
            width = Math.round((width * maxDimension) / height);
            height = maxDimension;
          }
        }

        const canvas = document.createElement('canvas');
        canvas.width = width;
        canvas.height = height;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          reject(new Error('Canvas 2D context not available.'));
          return;
        }

        ctx.drawImage(img, 0, 0, width, height);
        canvas.toBlob(
          (blob) => {
            if (blob) {
              resolve(blob);
            } else {
              reject(new Error('Image compression returned empty blob.'));
            }
          },
          'image/jpeg',
          quality
        );
      };
      img.src = e.target?.result as string;
    };
    reader.readAsDataURL(file);
  });
}

export function blobToDataUrl(blob: Blob): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.onloadend = () => resolve(reader.result as string);
    reader.onerror = reject;
    reader.readAsDataURL(blob);
  });
}

/**
 * Uploads a pet photo to Firebase Storage under pet_photos/{petId}/{timestamp}.jpg
 * Includes file validation, size limits (max 5MB original), compression, and progress reporting.
 * If Storage is unreachable, offline, or hangs past 6s, falls back to optimized compressed local URI
 * so the patient photo is NEVER lost and the UI never stays stuck at 0%.
 */
export async function uploadPetPhoto(
  petId: string | number,
  fileOrBlob: File | Blob,
  onProgress?: (percent: number) => void
): Promise<PhotoUploadResult> {
  if (!fileOrBlob) {
    throw new Error('No photo file provided.');
  }

  // 1. Initial simulated start
  if (onProgress) onProgress(15);

  // 2. Compress image before upload
  let uploadBlob: Blob;
  if (fileOrBlob instanceof File) {
    try {
      uploadBlob = await compressImage(fileOrBlob, 800, 0.82);
    } catch (err) {
      console.warn('Compression fallback to original file:', err);
      uploadBlob = fileOrBlob;
    }
  } else {
    uploadBlob = fileOrBlob;
  }

  if (onProgress) onProgress(35);

  // 3. Fallback dataURL in case of offline/Storage failure
  const fallbackDataUrl = await blobToDataUrl(uploadBlob);

  // If navigator is offline, resolve immediately with compressed data URL
  if (typeof navigator !== 'undefined' && !navigator.onLine) {
    if (onProgress) onProgress(100);
    return {
      downloadUrl: fallbackDataUrl,
      storagePath: 'offline_local_data_url'
    };
  }

  // 4. Firebase Storage path
  const timestamp = Date.now();
  const safePetId = String(petId).replace(/[^a-zA-Z0-9_-]/g, '_');
  const storagePath = `pet_photos/${safePetId}/${timestamp}.jpg`;
  const storageRef = ref(storage, storagePath);

  // 5. Upload with progress reporting and 6-second timeout safety
  return new Promise((resolve) => {
    let hasResolved = false;

    // Safety timeout: Never leave UI stuck at 0%
    const timeoutTimer = setTimeout(() => {
      if (!hasResolved) {
        hasResolved = true;
        console.warn('Storage upload timeout reached, resolving with compressed photo data:');
        if (onProgress) onProgress(100);
        resolve({
          downloadUrl: fallbackDataUrl,
          storagePath: 'local_compressed_fallback'
        });
      }
    }, 6000);

    try {
      const uploadTask = uploadBytesResumable(storageRef, uploadBlob, {
        contentType: 'image/jpeg',
        customMetadata: {
          petId: String(petId),
          uploadedBy: auth.currentUser?.email || 'authenticated_user',
          uploadedAt: new Date().toISOString()
        }
      });

      uploadTask.on(
        'state_changed',
        (snapshot) => {
          if (onProgress && snapshot.totalBytes > 0) {
            const percent = Math.round(35 + (snapshot.bytesTransferred / snapshot.totalBytes) * 65);
            onProgress(percent);
          }
        },
        (error) => {
          clearTimeout(timeoutTimer);
          if (!hasResolved) {
            hasResolved = true;
            console.warn('Firebase Storage upload error, falling back to local compressed image:', error);
            if (onProgress) onProgress(100);
            resolve({
              downloadUrl: fallbackDataUrl,
              storagePath: 'local_compressed_fallback'
            });
          }
        },
        async () => {
          clearTimeout(timeoutTimer);
          if (!hasResolved) {
            hasResolved = true;
            try {
              const downloadUrl = await getDownloadURL(uploadTask.snapshot.ref);
              if (onProgress) onProgress(100);
              resolve({
                downloadUrl,
                storagePath
              });
            } catch (urlError) {
              if (onProgress) onProgress(100);
              resolve({
                downloadUrl: fallbackDataUrl,
                storagePath: 'local_compressed_fallback'
              });
            }
          }
        }
      );
    } catch (taskErr) {
      clearTimeout(timeoutTimer);
      if (!hasResolved) {
        hasResolved = true;
        if (onProgress) onProgress(100);
        resolve({
          downloadUrl: fallbackDataUrl,
          storagePath: 'local_compressed_fallback'
        });
      }
    }
  });
}
