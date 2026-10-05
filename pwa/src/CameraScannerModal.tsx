import React, { useState, useEffect, useRef } from 'react';
import { Camera, X, AlertTriangle, CheckCircle, ShieldAlert, ScanLine } from 'lucide-react';
import { PetRecord } from './dataService';
import { UserRole } from './types';

interface CameraScannerModalProps {
  isOpen: boolean;
  onClose: () => void;
  pets: PetRecord[];
  currentRole: UserRole;
  currentUserId?: string;
  authorizedPetIds?: number[];
  onPetVerified: (pet: PetRecord) => void;
}

export const CameraScannerModal: React.FC<CameraScannerModalProps> = ({
  isOpen,
  onClose,
  pets,
  currentRole,
  authorizedPetIds,
  onPetVerified
}) => {
  const [cameraActive, setCameraActive] = useState<boolean>(false);
  const [cameraError, setCameraError] = useState<string>('');
  const [manualInput, setManualInput] = useState<string>('');
  const [authError, setAuthError] = useState<string>('');
  const videoRef = useRef<HTMLVideoElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);

  // Start live camera stream
  useEffect(() => {
    if (!isOpen) {
      stopCamera();
      return;
    }

    setAuthError('');
    setCameraError('');
    startCamera();

    return () => {
      stopCamera();
    };
  }, [isOpen]);

  const startCamera = async () => {
    try {
      if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
        setCameraError('Camera API is not supported in this browser environment. Please use manual code entry.');
        return;
      }

      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 640 }, height: { ideal: 480 } }
      });

      streamRef.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play();
        setCameraActive(true);
      }
    } catch (err: any) {
      console.warn('Camera access error:', err);
      if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
        setCameraError('Camera permission denied. Please grant camera access in browser permissions or enter code below.');
      } else {
        setCameraError('Camera hardware unavailable or in use by another application. Please enter tag/code manually.');
      }
      setCameraActive(false);
    }
  };

  const stopCamera = () => {
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(t => t.stop());
      streamRef.current = null;
    }
    setCameraActive(false);
  };

  const processPayload = (payload: string) => {
    setAuthError('');
    const cleanPayload = payload.trim();
    if (!cleanPayload) return;

    // Find matched pet from payload with exact identifier checks
    const matched = pets.find(p =>
      (p.rabiesTag && cleanPayload.includes(p.rabiesTag)) ||
      (p.microchipId && cleanPayload.includes(p.microchipId)) ||
      cleanPayload.includes(`"petId":${p.id}`) ||
      cleanPayload.includes(`"petId":"${p.id}"`) ||
      cleanPayload.includes(`PET:${p.id}`) ||
      cleanPayload.toLowerCase() === p.name.toLowerCase()
    );

    if (!matched) {
      setAuthError('Pet not found. Please verify the QR code or search manually.');
      return;
    }

    // Role-based authorization check
    if (currentRole === 'pet_owner' && authorizedPetIds && authorizedPetIds.length > 0) {
      if (!authorizedPetIds.includes(matched.id)) {
        setAuthError(`Access Denied: Pet "${matched.name}" (Rabies Tag #${matched.rabiesTag}) belongs to another client. Pet Owners are only authorized to access their own pet records.`);
        return;
      }
    }

    // Verified!
    stopCamera();
    onPetVerified(matched);
  };

  if (!isOpen) return null;

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0,0,0,0.7)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 110,
      padding: '16px'
    }}>
      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '24px',
        maxWidth: '500px',
        width: '100%',
        padding: '24px',
        boxShadow: 'var(--shadow-lg)',
        maxHeight: '90vh',
        overflowY: 'auto'
      }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '12px',
              backgroundColor: currentRole === 'pet_owner' ? 'var(--color-terracotta-light)' : 'var(--color-sage-light)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: currentRole === 'pet_owner' ? 'var(--color-amber-terracotta)' : 'var(--color-forest-sage)'
            }}>
              <Camera size={22} />
            </div>
            <div>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>
                {currentRole === 'pet_owner' ? 'Scan Pet Passport / Tag' : 'Staff Pet QR Scanner'}
              </h3>
              <p style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500 }}>
                {currentRole === 'pet_owner' ? 'Verify your pet collar tag or official passport' : 'Admit patient & retrieve clinical dossier'}
              </p>
            </div>
          </div>
          <button
            onClick={() => {
              stopCamera();
              onClose();
            }}
            style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6B7280', padding: '4px' }}
          >
            <X size={20} />
          </button>
        </div>

        {/* Auth Error Banner */}
        {authError && (
          <div style={{
            display: 'flex',
            alignItems: 'flex-start',
            gap: '10px',
            backgroundColor: '#FEF2F2',
            border: '1px solid #FCA5A5',
            borderRadius: '12px',
            padding: '12px',
            marginBottom: '16px',
            color: '#991B1B',
            fontSize: '13px'
          }}>
            <ShieldAlert size={18} style={{ flexShrink: 0, marginTop: '2px' }} />
            <div>
              <strong>Security Authorization Alert:</strong> {authError}
            </div>
          </div>
        )}

        {/* Live Camera Viewfinder */}
        <div style={{
          position: 'relative',
          width: '100%',
          height: '240px',
          backgroundColor: '#111827',
          borderRadius: '16px',
          overflow: 'hidden',
          marginBottom: '16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center'
        }}>
          {cameraActive ? (
            <video
              ref={videoRef}
              playsInline
              muted
              style={{ width: '100%', height: '100%', objectFit: 'cover' }}
            />
          ) : (
            <div style={{ textAlign: 'center', padding: '20px', color: '#9CA3AF' }}>
              <Camera size={36} style={{ margin: '0 auto 8px', opacity: 0.6 }} />
              <p style={{ fontSize: '12px', color: '#D1D5DB' }}>
                {cameraError || 'Initializing camera stream...'}
              </p>
            </div>
          )}

          {/* Scanner Overlay target */}
          <div style={{
            position: 'absolute',
            width: '180px',
            height: '180px',
            border: '2px dashed #F59E0B',
            borderRadius: '16px',
            boxShadow: '0 0 0 9999px rgba(0, 0, 0, 0.35)',
            pointerEvents: 'none'
          }} />
        </div>

        {/* Quick Tag Simulators for Environment Testing */}
        <div style={{ marginBottom: '16px' }}>
          <span style={{ fontSize: '11px', fontWeight: 700, color: '#374151', display: 'block', marginBottom: '8px' }}>
            Simulate Scan (Test Camera Detection):
          </span>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '8px' }}>
            {pets.map(p => (
              <button
                key={p.id}
                type="button"
                onClick={() => processPayload(p.rabiesTag)}
                className="btn-secondary"
                style={{
                  padding: '8px',
                  fontSize: '11px',
                  fontWeight: 700,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  color: '#111827',
                  textAlign: 'left'
                }}
              >
                <ScanLine size={13} color="var(--color-amber-terracotta)" />
                <span>Scan {p.name} ({p.rabiesTag})</span>
              </button>
            ))}
          </div>
        </div>

        {/* Manual Barcode / Tag Input */}
        <div style={{ borderTop: '1px solid #E5E7EB', paddingTop: '14px' }}>
          <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#374151', marginBottom: '6px' }}>
            Or Enter Scanned Tag Payload:
          </label>
          <div style={{ display: 'flex', gap: '8px' }}>
            <input
              type="text"
              value={manualInput}
              onChange={(e) => setManualInput(e.target.value)}
              placeholder="e.g. HP-LR-2024-0884 or Bella"
              style={{
                flex: 1,
                padding: '9px 12px',
                borderRadius: '8px',
                border: '1px solid #D1D5DB',
                fontSize: '13px',
                color: '#111827',
                fontWeight: 600,
                outline: 'none'
              }}
            />
            <button
              type="button"
              onClick={() => processPayload(manualInput)}
              className="btn-primary"
              style={{
                padding: '9px 16px',
                fontWeight: 700,
                fontSize: '13px',
                backgroundColor: currentRole === 'pet_owner' ? 'var(--color-amber-terracotta)' : 'var(--color-forest-sage)'
              }}
            >
              Verify
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
