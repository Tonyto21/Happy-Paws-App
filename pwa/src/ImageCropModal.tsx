import React, { useState, useRef, useEffect } from 'react';
import { Check, X, RotateCw, ZoomIn, ZoomOut, Crop } from 'lucide-react';

interface ImageCropModalProps {
  isOpen: boolean;
  imageSrc: string;
  onConfirmCrop: (croppedBlob: Blob, previewUrl: string) => void;
  onCancel: () => void;
}

export const ImageCropModal: React.FC<ImageCropModalProps> = ({
  isOpen,
  imageSrc,
  onConfirmCrop,
  onCancel
}) => {
  const [zoom, setZoom] = useState<number>(1);
  const [rotation, setRotation] = useState<number>(0);
  const [pan, setPan] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState<boolean>(false);
  const [dragStart, setDragStart] = useState<{ x: number; y: number }>({ x: 0, y: 0 });
  const imageRef = useRef<HTMLImageElement | null>(null);

  useEffect(() => {
    setZoom(1);
    setRotation(0);
    setPan({ x: 0, y: 0 });
  }, [imageSrc]);

  if (!isOpen || !imageSrc) return null;

  const handleMouseDown = (e: React.MouseEvent) => {
    setIsDragging(true);
    setDragStart({ x: e.clientX - pan.x, y: e.clientY - pan.y });
  };

  const handleMouseMove = (e: React.MouseEvent) => {
    if (!isDragging) return;
    setPan({
      x: e.clientX - dragStart.x,
      y: e.clientY - dragStart.y
    });
  };

  const handleMouseUp = () => setIsDragging(false);

  // Touch handlers for mobile
  const handleTouchStart = (e: React.TouchEvent) => {
    if (e.touches.length === 1) {
      setIsDragging(true);
      setDragStart({
        x: e.touches[0].clientX - pan.x,
        y: e.touches[0].clientY - pan.y
      });
    }
  };

  const handleTouchMove = (e: React.TouchEvent) => {
    if (!isDragging || e.touches.length !== 1) return;
    setPan({
      x: e.touches[0].clientX - dragStart.x,
      y: e.touches[0].clientY - dragStart.y
    });
  };

  const handleTouchEnd = () => setIsDragging(false);

  const handleCropAndConfirm = () => {
    if (!imageRef.current) return;
    const img = imageRef.current;

    const canvas = document.createElement('canvas');
    const CROP_SIZE = 600;
    canvas.width = CROP_SIZE;
    canvas.height = CROP_SIZE;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.fillStyle = '#FFFFFF';
    ctx.fillRect(0, 0, CROP_SIZE, CROP_SIZE);

    ctx.save();
    ctx.translate(CROP_SIZE / 2, CROP_SIZE / 2);
    ctx.rotate((rotation * Math.PI) / 180);
    ctx.scale(zoom, zoom);

    // Scaling ratio based on visual viewport to canvas size
    const visualBoxSize = 260; // size of the crop circle/box in CSS px
    const factor = CROP_SIZE / visualBoxSize;
    ctx.translate(pan.x * factor, pan.y * factor);

    const aspect = img.naturalWidth / img.naturalHeight;
    let drawW = CROP_SIZE;
    let drawH = CROP_SIZE;
    if (aspect > 1) {
      drawW = CROP_SIZE * aspect;
    } else {
      drawH = CROP_SIZE / aspect;
    }

    ctx.drawImage(img, -drawW / 2, -drawH / 2, drawW, drawH);
    ctx.restore();

    canvas.toBlob(
      (blob) => {
        if (blob) {
          const previewUrl = URL.createObjectURL(blob);
          onConfirmCrop(blob, previewUrl);
        }
      },
      'image/jpeg',
      0.88
    );
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.85)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 150,
      padding: '16px'
    }}>
      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '24px',
        maxWidth: '440px',
        width: '100%',
        padding: '24px',
        boxShadow: 'var(--shadow-lg)',
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center'
      }}>
        {/* Header */}
        <div style={{ width: '100%', display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <Crop size={20} color="var(--color-amber-terracotta)" />
            <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>
              Crop Patient Photo
            </h3>
          </div>
          <button onClick={onCancel} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6B7280' }}>
            <X size={20} />
          </button>
        </div>

        {/* Viewport Cropper Box */}
        <div
          onMouseDown={handleMouseDown}
          onMouseMove={handleMouseMove}
          onMouseUp={handleMouseUp}
          onTouchStart={handleTouchStart}
          onTouchMove={handleTouchMove}
          onTouchEnd={handleTouchEnd}
          style={{
            width: '280px',
            height: '280px',
            borderRadius: '20px',
            backgroundColor: '#1F2937',
            position: 'relative',
            overflow: 'hidden',
            cursor: isDragging ? 'grabbing' : 'grab',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            userSelect: 'none',
            touchAction: 'none'
          }}
        >
          {/* Circular mask guide */}
          <div style={{
            position: 'absolute',
            width: '240px',
            height: '240px',
            borderRadius: '50%',
            border: '2px dashed rgba(255, 255, 255, 0.85)',
            boxShadow: '0 0 0 9999px rgba(0, 0, 0, 0.45)',
            pointerEvents: 'none',
            zIndex: 10
          }} />

          <img
            ref={imageRef}
            src={imageSrc}
            alt="To crop"
            draggable={false}
            style={{
              maxWidth: 'none',
              transform: `translate(${pan.x}px, ${pan.y}px) scale(${zoom}) rotate(${rotation}deg)`,
              transition: isDragging ? 'none' : 'transform 0.1s ease',
              width: '240px',
              height: 'auto',
              pointerEvents: 'none'
            }}
          />
        </div>

        <p style={{ fontSize: '12px', color: '#6B7280', marginTop: '10px', textAlign: 'center' }}>
          Drag to center patient face. Use controls below to zoom or rotate.
        </p>

        {/* Controls */}
        <div style={{ display: 'flex', gap: '10px', marginTop: '14px', alignItems: 'center' }}>
          <button
            type="button"
            onClick={() => setZoom(prev => Math.max(0.6, prev - 0.2))}
            className="btn-secondary"
            title="Zoom Out"
            style={{ padding: '8px 12px', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px' }}
          >
            <ZoomOut size={16} /> -
          </button>
          <span style={{ fontSize: '12px', fontWeight: 600, color: '#374151', minWidth: '45px', textAlign: 'center' }}>
            {Math.round(zoom * 100)}%
          </span>
          <button
            type="button"
            onClick={() => setZoom(prev => Math.min(3.0, prev + 0.2))}
            className="btn-secondary"
            title="Zoom In"
            style={{ padding: '8px 12px', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px' }}
          >
            <ZoomIn size={16} /> +
          </button>
          <button
            type="button"
            onClick={() => setRotation(prev => (prev + 90) % 360)}
            className="btn-secondary"
            title="Rotate 90°"
            style={{ padding: '8px 12px', display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px' }}
          >
            <RotateCw size={16} /> Rotate
          </button>
        </div>

        {/* Actions */}
        <div style={{ display: 'flex', gap: '12px', width: '100%', marginTop: '22px' }}>
          <button
            type="button"
            onClick={onCancel}
            className="btn-secondary"
            style={{ flex: 1, padding: '10px', fontSize: '14px', fontWeight: 600 }}
          >
            Cancel / Retake
          </button>
          <button
            type="button"
            onClick={handleCropAndConfirm}
            className="btn-primary"
            style={{ flex: 1, padding: '10px', fontSize: '14px', fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
          >
            <Check size={18} /> Confirm & Use
          </button>
        </div>
      </div>
    </div>
  );
};
