import React, { useState, useEffect } from 'react';
import { X, User, Phone, Mail, MapPin, FileText, Check, AlertCircle, Loader2 } from 'lucide-react';
import { ClientRecord, saveClientToFirestore } from './dataService';

interface ClientModalProps {
  isOpen: boolean;
  onClose: () => void;
  client?: ClientRecord | null;
  onClientSaved: (savedClient: ClientRecord) => void;
}

export const ClientModal: React.FC<ClientModalProps> = ({
  isOpen,
  onClose,
  client,
  onClientSaved
}) => {
  const [fullName, setFullName] = useState('');
  const [preferredName, setPreferredName] = useState('');
  const [phone, setPhone] = useState('');
  const [email, setEmail] = useState('');
  const [address, setAddress] = useState('');
  const [notes, setNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (client) {
      setFullName(client.fullName || '');
      setPreferredName(client.preferredName || '');
      setPhone(client.phone || '');
      setEmail(client.email || '');
      setAddress(client.address || '');
      setNotes(client.notes || '');
    } else {
      setFullName('');
      setPreferredName('');
      setPhone('');
      setEmail('');
      setAddress('Paynesville City, Montserrado County');
      setNotes('');
    }
    setErrorMessage('');
  }, [client, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!fullName.trim()) {
      setErrorMessage('Client full name is required.');
      return;
    }
    if (!phone.trim()) {
      setErrorMessage('Contact phone number is required.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const clientId = client?.id || `CL-${Date.now().toString().slice(-4)}`;
      const savedClient: ClientRecord = {
        id: clientId,
        fullName: fullName.trim(),
        preferredName: preferredName.trim() || undefined,
        phone: phone.trim(),
        email: email.trim() || undefined,
        address: address.trim() || undefined,
        notes: notes.trim() || undefined,
        ownerAuthId: client?.ownerAuthId,
        createdAt: client?.createdAt || new Date().toISOString()
      };

      await saveClientToFirestore(savedClient);
      onClientSaved(savedClient);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to save client.');
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.7)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 120,
      padding: '16px'
    }}>
      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '24px',
        maxWidth: '480px',
        width: '100%',
        padding: '28px',
        boxShadow: 'var(--shadow-lg)'
      }}>
        {/* Header */}
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', borderBottom: '1px solid #E5E7EB', paddingBottom: '14px' }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{ width: '38px', height: '38px', borderRadius: '10px', backgroundColor: 'var(--color-warm-ivory)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <User size={20} color="var(--color-amber-terracotta)" />
            </div>
            <div>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>
                {client ? 'Edit Client Record' : 'Register New Pet Owner / Client'}
              </h3>
              <p style={{ fontSize: '12px', color: '#6B7280', fontWeight: 500 }}>
                Happy Paws Liberia Client Registry
              </p>
            </div>
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
          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              Full Legal Name *
            </label>
            <input
              type="text"
              value={fullName}
              onChange={(e) => setFullName(e.target.value)}
              placeholder="e.g. Anthony Tolbert"
              required
              style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Phone Number *
              </label>
              <div style={{ position: 'relative' }}>
                <Phone size={14} style={{ position: 'absolute', left: '10px', top: '11px', color: '#9CA3AF' }} />
                <input
                  type="tel"
                  value={phone}
                  onChange={(e) => setPhone(e.target.value)}
                  placeholder="0881479329"
                  required
                  style={{ width: '100%', padding: '9px 12px 9px 32px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Preferred / Nickname
              </label>
              <input
                type="text"
                value={preferredName}
                onChange={(e) => setPreferredName(e.target.value)}
                placeholder="e.g. Tony"
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              Email Address (Optional)
            </label>
            <div style={{ position: 'relative' }}>
              <Mail size={14} style={{ position: 'absolute', left: '10px', top: '11px', color: '#9CA3AF' }} />
              <input
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="client@gmail.com"
                style={{ width: '100%', padding: '9px 12px 9px 32px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              Residential Community / Address
            </label>
            <div style={{ position: 'relative' }}>
              <MapPin size={14} style={{ position: 'absolute', left: '10px', top: '11px', color: '#9CA3AF' }} />
              <input
                type="text"
                value={address}
                onChange={(e) => setAddress(e.target.value)}
                placeholder="e.g. Honeybee Junction, RIA Highway, Paynesville"
                style={{ width: '100%', padding: '9px 12px 9px 32px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              Special Client Notes / Communication Preferences
            </label>
            <textarea
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              rows={2}
              placeholder="Prefers WhatsApp messages. Emergency contact available."
              style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box', fontFamily: 'inherit' }}
            />
          </div>

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
                  <Loader2 size={16} className="animate-spin" /> Saving...
                </>
              ) : (
                <>
                  <Check size={16} /> Save Client
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
