import React, { useState } from 'react';
import { QrCode, X, Printer, Check, Copy, Tag, ShieldCheck } from 'lucide-react';
import { PetRecord } from './dataService';
import { generateQrMatrix } from './qr';

interface StaffQrGeneratorModalProps {
  isOpen: boolean;
  onClose: () => void;
  pets: PetRecord[];
  onGenerateCode: (tagInfo: { petId: number; tagType: string; code: string }) => void;
}

export const StaffQrGeneratorModal: React.FC<StaffQrGeneratorModalProps> = ({
  isOpen,
  onClose,
  pets,
  onGenerateCode
}) => {
  const [selectedPetId, setSelectedPetId] = useState<number | string>(pets.length > 0 ? pets[0].id : '');
  const [codeType, setCodeType] = useState<'RABIES_TAG' | 'INTAKE_BADGE' | 'MICROCHIP_TAG'>('RABIES_TAG');
  const [customSuffix, setCustomSuffix] = useState<string>('');
  const [generatedPayload, setGeneratedPayload] = useState<string>('');
  const [copied, setCopied] = useState<boolean>(false);

  if (!isOpen) return null;

  const activePet = pets.find(p => String(p.id) === String(selectedPetId)) || null;

  if (!activePet) {
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
        <div style={{ backgroundColor: '#fff', padding: '24px', borderRadius: '16px', maxWidth: '400px', width: '100%', textAlign: 'center' }}>
          <h3 style={{ fontSize: '18px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>No Patient Available</h3>
          <p style={{ fontSize: '13px', color: '#6B7280', marginBottom: '16px' }}>No registered patient was found in the database. Please register a pet first.</p>
          <button onClick={onClose} className="btn-primary" style={{ padding: '8px 16px', fontWeight: 700 }}>Close</button>
        </div>
      </div>
    );
  }

  const handleGenerate = (e: React.FormEvent) => {
    e.preventDefault();
    const tagCode = customSuffix.trim() 
      ? `HP-LR-2026-${customSuffix.trim().toUpperCase()}`
      : activePet.rabiesTag;

    const payload = JSON.stringify({
      schema: 'HAPPY_PAWS_OFFICIAL_TAG',
      clinic: 'Happy Paws Liberia Rescue Center',
      petId: activePet.id,
      name: activePet.name,
      species: activePet.species,
      tagType: codeType,
      tagCode: tagCode,
      microchipId: activePet.microchipId,
      issuedAt: new Date().toISOString()
    });

    setGeneratedPayload(payload);
    onGenerateCode({
      petId: activePet.id,
      tagType: codeType,
      code: tagCode
    });
  };

  const qrMatrix = generatedPayload ? generateQrMatrix(generatedPayload) : generateQrMatrix(activePet.rabiesTag);

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
      padding: '20px'
    }}>
      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '24px',
        maxWidth: '520px',
        width: '100%',
        padding: '28px',
        boxShadow: 'var(--shadow-lg)',
        maxHeight: '90vh',
        overflowY: 'auto'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '40px',
              height: '40px',
              borderRadius: '12px',
              backgroundColor: 'var(--color-sage-light)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--color-forest-sage)'
            }}>
              <Tag size={22} />
            </div>
            <div>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>
                Authorized Staff QR / Tag Generator
              </h3>
              <p style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500 }}>
                Generate official intake QR badges and rabies collar tags
              </p>
            </div>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6B7280' }}>
            <X size={20} />
          </button>
        </div>

        <form onSubmit={handleGenerate} style={{ display: 'flex', flexDirection: 'column', gap: '14px', marginBottom: '20px' }}>
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
              Select Patient Pet
            </label>
            <select
              value={selectedPetId}
              onChange={(e) => setSelectedPetId(Number(e.target.value))}
              style={{
                width: '100%',
                padding: '9px 12px',
                borderRadius: '8px',
                border: '1px solid #D1D5DB',
                fontSize: '13px',
                color: '#111827',
                fontWeight: 600,
                boxSizing: 'border-box'
              }}
            >
              {pets.map(p => (
                <option key={p.id} value={p.id}>
                  {p.name} ({p.species} · Tag: {p.rabiesTag})
                </option>
              ))}
            </select>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                Code / Tag Type
              </label>
              <select
                value={codeType}
                onChange={(e) => setCodeType(e.target.value as any)}
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '8px',
                  border: '1px solid #D1D5DB',
                  fontSize: '13px',
                  color: '#111827',
                  fontWeight: 600,
                  boxSizing: 'border-box'
                }}
              >
                <option value="RABIES_TAG">Rabies Collar Tag</option>
                <option value="INTAKE_BADGE">Intake Patient Badge</option>
                <option value="MICROCHIP_TAG">Microchip Scan Tag</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                Custom Tag Suffix (Optional)
              </label>
              <input
                type="text"
                value={customSuffix}
                onChange={(e) => setCustomSuffix(e.target.value)}
                placeholder="e.g. 0992"
                style={{
                  width: '100%',
                  padding: '9px 12px',
                  borderRadius: '8px',
                  border: '1px solid #D1D5DB',
                  fontSize: '13px',
                  color: '#111827',
                  fontWeight: 600,
                  boxSizing: 'border-box'
                }}
              />
            </div>
          </div>

          <button
            type="submit"
            className="btn-primary"
            style={{ padding: '10px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '6px' }}
          >
            <ShieldCheck size={16} /> Generate Verified QR Badge
          </button>
        </form>

        {/* QR Code Preview Card */}
        <div style={{
          backgroundColor: '#F9FAFB',
          border: '1px solid #E5E7EB',
          borderRadius: '16px',
          padding: '20px',
          textAlign: 'center'
        }}>
          <div style={{
            display: 'inline-block',
            padding: '12px',
            backgroundColor: '#FFFFFF',
            borderRadius: '14px',
            border: '1px solid #D1D5DB',
            boxShadow: '0 4px 6px -1px rgba(0,0,0,0.1)'
          }}>
            <svg
              viewBox={`0 0 ${qrMatrix.length} ${qrMatrix.length}`}
              style={{ width: '160px', height: '160px', display: 'block' }}
              shapeRendering="crispEdges"
            >
              {qrMatrix.map((row, r) =>
                row.map((cell, c) => (
                  <rect
                    key={`${r}-${c}`}
                    x={c}
                    y={r}
                    width={1}
                    height={1}
                    fill={cell ? '#111827' : '#FFFFFF'}
                  />
                ))
              )}
            </svg>
          </div>

          <div style={{ marginTop: '14px' }}>
            <span style={{ fontSize: '14px', fontWeight: 700, color: '#111827', display: 'block' }}>
              {activePet.name} · {codeType.replace('_', ' ')}
            </span>
            <span style={{ fontSize: '12px', fontWeight: 600, color: 'var(--color-amber-terracotta)', display: 'block', marginTop: '2px' }}>
              Tag #{activePet.rabiesTag}
            </span>
          </div>

          <div style={{ display: 'flex', gap: '10px', justifyContent: 'center', marginTop: '16px' }}>
            <button
              type="button"
              onClick={() => window.print()}
              className="btn-secondary"
              style={{ padding: '8px 14px', fontSize: '12px', fontWeight: 700, color: '#111827', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <Printer size={14} /> Print Tag Sticker
            </button>
            <button
              type="button"
              onClick={() => {
                navigator.clipboard.writeText(generatedPayload || activePet.rabiesTag);
                setCopied(true);
                setTimeout(() => setCopied(false), 2500);
              }}
              className="btn-secondary"
              style={{ padding: '8px 14px', fontSize: '12px', fontWeight: 700, color: '#111827', display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              {copied ? <Check size={14} /> : <Copy size={14} />}
              {copied ? 'Copied' : 'Copy Payload'}
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
