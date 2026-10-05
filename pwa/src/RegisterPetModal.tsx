import React, { useState, useRef, useEffect } from 'react';
import { X, Camera, Upload, AlertCircle, Check, Loader2, UserPlus, Scissors } from 'lucide-react';
import { PetRecord, ClientRecord, savePetToFirestore, saveClientToFirestore } from './dataService';
import { uploadPetPhoto } from './storageService';
import { ImageCropModal } from './ImageCropModal';

interface RegisterPetModalProps {
  isOpen: boolean;
  onClose: () => void;
  clients: ClientRecord[];
  initialClientId?: string | number;
  petToEdit?: PetRecord | null;
  onPetSaved: (pet: PetRecord) => void;
  onClientCreated?: (client: ClientRecord) => void;
}

export const RegisterPetModal: React.FC<RegisterPetModalProps> = ({
  isOpen,
  onClose,
  clients,
  initialClientId,
  petToEdit,
  onPetSaved,
  onClientCreated
}) => {
  const [name, setName] = useState('');
  const [species, setSpecies] = useState('Canine (Dog)');
  const [breed, setBreed] = useState('');
  const [sex, setSex] = useState('Spayed Female');
  const [dob, setDob] = useState('');
  const [weightKg, setWeightKg] = useState('15.0');
  const [color, setColor] = useState('');
  const [microchipId, setMicrochipId] = useState('');
  const [rabiesTag, setRabiesTag] = useState('');
  const [notes, setNotes] = useState('');

  // Client link state
  const [selectedClientId, setSelectedClientId] = useState<string>('');
  const [showInlineNewClient, setShowInlineNewClient] = useState<boolean>(false);
  const [newClientName, setNewClientName] = useState('');
  const [newClientPhone, setNewClientPhone] = useState('');
  const [newClientAddress, setNewClientAddress] = useState('');

  // Photo & Crop state
  const [rawPhotoToCrop, setRawPhotoToCrop] = useState<string | null>(null);
  const [croppedBlob, setCroppedBlob] = useState<Blob | null>(null);
  const [photoPreview, setPhotoPreview] = useState<string>('');
  const [uploadProgress, setUploadProgress] = useState<number | null>(null);
  const [isSubmitting, setIsSubmitting] = useState<boolean>(false);
  const [errorMessage, setErrorMessage] = useState<string>('');

  const cameraInputRef = useRef<HTMLInputElement | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    if (petToEdit) {
      setName(petToEdit.name || '');
      setSpecies(petToEdit.species || 'Canine (Dog)');
      setBreed(petToEdit.breed || '');
      setSex(petToEdit.sex || 'Female');
      setDob(petToEdit.dob || '');
      setWeightKg(String(petToEdit.weightKg || '10.0'));
      setColor(petToEdit.color || '');
      setMicrochipId(petToEdit.microchipId || '');
      setRabiesTag(petToEdit.rabiesTag || '');
      setNotes(petToEdit.notes || '');
      setSelectedClientId(String(petToEdit.clientId || ''));
      setPhotoPreview(petToEdit.photoUrl || '');
    } else {
      setName('');
      setSpecies('Canine (Dog)');
      setBreed('');
      setSex('Spayed Female');
      setDob('');
      setWeightKg('12.0');
      setColor('');
      setMicrochipId('');
      setRabiesTag('');
      setNotes('');
      setSelectedClientId(initialClientId ? String(initialClientId) : clients[0]?.id ? String(clients[0].id) : '');
      setPhotoPreview('');
    }
    setShowInlineNewClient(false);
    setRawPhotoToCrop(null);
    setCroppedBlob(null);
    setErrorMessage('');
    setUploadProgress(null);
  }, [petToEdit, isOpen, initialClientId, clients]);

  if (!isOpen) return null;

  const handleFilePicked = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const file = e.target.files[0];
      const reader = new FileReader();
      reader.onload = (ev) => {
        setRawPhotoToCrop(ev.target?.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleConfirmCrop = (blob: Blob, previewUrl: string) => {
    setCroppedBlob(blob);
    setPhotoPreview(previewUrl);
    setRawPhotoToCrop(null);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMessage('Please enter the patient name.');
      return;
    }

    let finalClientId = selectedClientId;
    let finalClientName = '';

    // If creating new client on the fly
    if (showInlineNewClient) {
      if (!newClientName.trim() || !newClientPhone.trim()) {
        setErrorMessage('Please enter both name and phone number for the new client.');
        return;
      }
      const createdId = `CL-${Date.now().toString().slice(-4)}`;
      const newClientObj: ClientRecord = {
        id: createdId,
        fullName: newClientName.trim(),
        phone: newClientPhone.trim(),
        address: newClientAddress.trim() || 'Paynesville City, Liberia',
        createdAt: new Date().toISOString()
      };
      try {
        await saveClientToFirestore(newClientObj);
        if (onClientCreated) onClientCreated(newClientObj);
        finalClientId = createdId;
        finalClientName = newClientObj.fullName;
      } catch (clientErr: any) {
        console.warn('Client save warning:', clientErr);
        finalClientId = createdId;
        finalClientName = newClientObj.fullName;
      }
    } else {
      const matchedClient = clients.find(c => String(c.id) === String(selectedClientId));
      finalClientName = matchedClient ? matchedClient.fullName : 'Registered Client';
      if (!finalClientId && clients.length > 0) {
        finalClientId = String(clients[0].id);
        finalClientName = clients[0].fullName;
      }
    }

    setIsSubmitting(true);
    setErrorMessage('');
    setUploadProgress(10);

    try {
      const targetId = petToEdit ? petToEdit.id : Date.now();
      let photoUrl = petToEdit?.photoUrl || 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=400&q=80';

      // Upload cropped photo if user selected a new photo
      if (croppedBlob) {
        try {
          const uploadRes = await uploadPetPhoto(targetId, croppedBlob, (progress) => {
            setUploadProgress(progress);
          });
          photoUrl = uploadRes.downloadUrl;
        } catch (storageErr: any) {
          console.warn('Storage upload error, using local cropped preview:', storageErr);
          if (photoPreview) {
            photoUrl = photoPreview;
          }
        }
      }

      const generatedRabiesTag = rabiesTag.trim() || petToEdit?.rabiesTag || `HP-LR-2026-${Math.floor(1000 + Math.random() * 9000)}`;
      const generatedMicrochip = microchipId.trim() || petToEdit?.microchipId || `98514100${Math.floor(1000000 + Math.random() * 9000000)}`;

      const savedPet: PetRecord = {
        id: targetId,
        clientId: finalClientId || 'CL-001',
        clientName: finalClientName,
        name: name.trim(),
        species,
        breed: breed.trim() || (species.includes('Goat') ? 'West African Dwarf' : species.includes('Sheep') ? 'Djallonke' : 'Mixed Breed'),
        sex,
        dob: dob.trim() || 'Unknown',
        weightKg: parseFloat(weightKg) || 12.0,
        color: color.trim() || 'Standard',
        microchipId: generatedMicrochip,
        rabiesTag: generatedRabiesTag,
        photoUrl,
        notes: notes.trim(),
        ownerAuthId: petToEdit?.ownerAuthId
      };

      // Persist to Firestore
      await savePetToFirestore(savedPet);
      onPetSaved(savedPet);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to save pet record.');
    } finally {
      setIsSubmitting(false);
      setUploadProgress(null);
    }
  };

  return (
    <>
      {/* Optional Cropping Dialog */}
      {rawPhotoToCrop && (
        <ImageCropModal
          isOpen={true}
          imageSrc={rawPhotoToCrop}
          onConfirmCrop={handleConfirmCrop}
          onCancel={() => setRawPhotoToCrop(null)}
        />
      )}

      <div style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0, 0, 0, 0.75)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 110,
        padding: '16px'
      }}>
        <div style={{
          backgroundColor: '#FFFFFF',
          borderRadius: '24px',
          maxWidth: '560px',
          width: '100%',
          maxHeight: '90vh',
          overflowY: 'auto',
          padding: '28px',
          boxShadow: 'var(--shadow-lg)'
        }}>
          {/* Header */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #E5E7EB', paddingBottom: '14px' }}>
            <div>
              <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>
                {petToEdit ? `Edit Patient: ${petToEdit.name}` : 'Register New Patient'}
              </h3>
              <p style={{ fontSize: '12px', color: '#6B7280', fontWeight: 500 }}>
                Happy Paws Liberia Rescue Center · Clinical & Vaccination Dossier
              </p>
            </div>
            <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6B7280' }}>
              <X size={20} />
            </button>
          </div>

          {errorMessage && (
            <div style={{ padding: '10px 14px', borderRadius: '10px', backgroundColor: '#FEF2F2', border: '1px solid #FCA5A5', color: '#991B1B', fontSize: '12px', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
              <AlertCircle size={16} />
              <span>{errorMessage}</span>
            </div>
          )}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
            {/* 1. Client / Owner Link */}
            <div style={{ padding: '14px', borderRadius: '14px', backgroundColor: '#F8FAFC', border: '1px solid #E2E8F0' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
                <label style={{ fontSize: '12px', fontWeight: 700, color: '#1E293B', display: 'flex', alignItems: 'center', gap: '6px' }}>
                  Pet Owner / Client *
                </label>
                <button
                  type="button"
                  onClick={() => setShowInlineNewClient(!showInlineNewClient)}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'var(--color-amber-terracotta)',
                    fontSize: '12px',
                    fontWeight: 700,
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}
                >
                  <UserPlus size={14} />
                  {showInlineNewClient ? 'Choose Existing Client' : '+ New Client'}
                </button>
              </div>

              {showInlineNewClient ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '8px', marginTop: '6px' }}>
                  <input
                    type="text"
                    value={newClientName}
                    onChange={(e) => setNewClientName(e.target.value)}
                    placeholder="Owner Full Name (e.g. Marie Dennis)"
                    style={{ width: '100%', padding: '8px 10px', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '13px', boxSizing: 'border-box' }}
                  />
                  <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
                    <input
                      type="tel"
                      value={newClientPhone}
                      onChange={(e) => setNewClientPhone(e.target.value)}
                      placeholder="Phone (0881479329)"
                      style={{ width: '100%', padding: '8px 10px', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '13px', boxSizing: 'border-box' }}
                    />
                    <input
                      type="text"
                      value={newClientAddress}
                      onChange={(e) => setNewClientAddress(e.target.value)}
                      placeholder="Community / Location"
                      style={{ width: '100%', padding: '8px 10px', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '13px', boxSizing: 'border-box' }}
                    />
                  </div>
                </div>
              ) : (
                <select
                  value={selectedClientId}
                  onChange={(e) => setSelectedClientId(e.target.value)}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #CBD5E1', fontSize: '13px', color: '#1E293B', backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                >
                  {clients.length === 0 && <option value="">No clients registered yet</option>}
                  {clients.map(c => (
                    <option key={c.id} value={c.id}>
                      {c.fullName} ({c.phone}) — {c.address || 'Liberia'}
                    </option>
                  ))}
                </select>
              )}
            </div>

            {/* 2. Patient Profile Basics */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Patient Name *
                </label>
                <input
                  type="text"
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="e.g. Bella"
                  required
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Species *
                </label>
                <select
                  value={species}
                  onChange={(e) => setSpecies(e.target.value)}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                >
                  <option value="Canine (Dog)">Canine (Dog)</option>
                  <option value="Feline (Cat)">Feline (Cat)</option>
                  <option value="Caprine (Goat)">Caprine (Goat)</option>
                  <option value="Ovine (Sheep)">Ovine (Sheep)</option>
                  <option value="Porcine (Pig)">Porcine (Pig)</option>
                  <option value="Avian (Bird / Poultry)">Avian (Bird / Poultry)</option>
                  <option value="Other Small Animal / Livestock">Other Small Animal / Livestock</option>
                </select>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Breed
                </label>
                <input
                  type="text"
                  value={breed}
                  onChange={(e) => setBreed(e.target.value)}
                  placeholder="e.g. Boerboel Mix / West African Dwarf"
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Sex / Reproductive Status
                </label>
                <select
                  value={sex}
                  onChange={(e) => setSex(e.target.value)}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                >
                  <option value="Spayed Female">Spayed Female</option>
                  <option value="Neutered Male">Neutered Male</option>
                  <option value="Intact Female">Intact Female</option>
                  <option value="Intact Male">Intact Male</option>
                </select>
              </div>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Weight (kg)
                </label>
                <input
                  type="number"
                  step="0.1"
                  value={weightKg}
                  onChange={(e) => setWeightKg(e.target.value)}
                  placeholder="15.0"
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Color / Markings
                </label>
                <input
                  type="text"
                  value={color}
                  onChange={(e) => setColor(e.target.value)}
                  placeholder="Brindle / Pied"
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  DOB / Est. Age
                </label>
                <input
                  type="text"
                  value={dob}
                  onChange={(e) => setDob(e.target.value)}
                  placeholder="e.g. 2 years"
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
            </div>

            {/* Identifiers */}
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Rabies Collar Tag ID
                </label>
                <input
                  type="text"
                  value={rabiesTag}
                  onChange={(e) => setRabiesTag(e.target.value)}
                  placeholder="HP-LR-2026-XXXX"
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                  Microchip ID (ISO 11784/5)
                </label>
                <input
                  type="text"
                  value={microchipId}
                  onChange={(e) => setMicrochipId(e.target.value)}
                  placeholder="98514100XXXXXXX"
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
            </div>

            {/* 3. Photo & Camera with Cropping */}
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                Patient Photo (Camera / Upload with Cropper)
              </label>

              {/* Hidden Inputs */}
              <input
                ref={cameraInputRef}
                type="file"
                accept="image/*"
                capture="environment"
                onChange={handleFilePicked}
                style={{ display: 'none' }}
              />
              <input
                ref={fileInputRef}
                type="file"
                accept="image/*"
                onChange={handleFilePicked}
                style={{ display: 'none' }}
              />

              <div style={{ display: 'flex', gap: '12px', alignItems: 'center', flexWrap: 'wrap' }}>
                {photoPreview ? (
                  <div style={{ position: 'relative' }}>
                    <img
                      src={photoPreview}
                      alt="Patient preview"
                      style={{ width: '70px', height: '70px', borderRadius: '16px', objectFit: 'cover', border: '2px solid var(--color-amber-terracotta)' }}
                    />
                    <div style={{ position: 'absolute', bottom: -4, right: -4, backgroundColor: 'var(--color-forest-sage)', color: '#FFFFFF', borderRadius: '50%', padding: '2px' }}>
                      <Check size={12} />
                    </div>
                  </div>
                ) : (
                  <div style={{ width: '70px', height: '70px', borderRadius: '16px', backgroundColor: '#F3F4F6', border: '2px dashed #D1D5DB', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#9CA3AF' }}>
                    <Camera size={24} />
                  </div>
                )}

                <div style={{ display: 'flex', gap: '8px', flexWrap: 'wrap' }}>
                  <button
                    type="button"
                    onClick={() => cameraInputRef.current?.click()}
                    className="btn-secondary"
                    style={{ padding: '8px 12px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px', color: '#111827' }}
                  >
                    <Camera size={14} /> Take Photo (Camera)
                  </button>
                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    className="btn-secondary"
                    style={{ padding: '8px 12px', fontSize: '12px', display: 'flex', alignItems: 'center', gap: '6px', color: '#111827' }}
                  >
                    <Upload size={14} /> Upload File
                  </button>
                </div>
              </div>
            </div>

            {/* Notes */}
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Special Clinical / Behavioral Notes
              </label>
              <textarea
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                rows={2}
                placeholder="Temperament, prior history, rescue background..."
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box', fontFamily: 'inherit' }}
              />
            </div>

            {/* Progress bar if uploading */}
            {uploadProgress !== null && (
              <div style={{ marginTop: '4px' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '11px', color: '#4B5563', marginBottom: '4px' }}>
                  <span>Saving & uploading photo to Storage...</span>
                  <span>{uploadProgress}%</span>
                </div>
                <div style={{ width: '100%', height: '6px', backgroundColor: '#E5E7EB', borderRadius: '3px', overflow: 'hidden' }}>
                  <div style={{ width: `${uploadProgress}%`, height: '100%', backgroundColor: 'var(--color-amber-terracotta)', transition: 'width 0.2s ease' }} />
                </div>
              </div>
            )}

            {/* Actions */}
            <div style={{ display: 'flex', gap: '10px', marginTop: '10px' }}>
              <button
                type="button"
                onClick={onClose}
                className="btn-secondary"
                style={{ flex: 1, padding: '10px', fontSize: '13px', fontWeight: 600 }}
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={isSubmitting}
                className="btn-primary"
                style={{ flex: 1, padding: '10px', fontSize: '13px', fontWeight: 700, display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
              >
                {isSubmitting ? (
                  <>
                    <Loader2 size={16} className="animate-spin" /> Saving Patient...
                  </>
                ) : (
                  <>
                    <Check size={16} /> {petToEdit ? 'Save Changes' : 'Register Patient'}
                  </>
                )}
              </button>
            </div>
          </form>
        </div>
      </div>
    </>
  );
};
