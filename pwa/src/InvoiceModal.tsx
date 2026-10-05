import React, { useState } from 'react';
import { X, Receipt, Plus, Trash2, AlertCircle, Loader2 } from 'lucide-react';
import { PetRecord, InvoiceItem, InvoiceLineItem, saveInvoiceToFirestore } from './dataService';

interface InvoiceModalProps {
  isOpen: boolean;
  onClose: () => void;
  pets: PetRecord[];
  usdToLrdRate?: number;
  onInvoiceCreated: (invoice: InvoiceItem) => void;
}

export const InvoiceModal: React.FC<InvoiceModalProps> = ({
  isOpen,
  onClose,
  pets,
  usdToLrdRate = 194.0,
  onInvoiceCreated
}) => {
  const [selectedPetId, setSelectedPetId] = useState<number | string>(pets.length > 0 ? pets[0].id : '');
  const [clientName, setClientName] = useState(pets.length > 0 && pets[0].clientName ? pets[0].clientName : '');
  const [paymentOption, setPaymentOption] = useState<'PAID' | 'UNPAID'>('PAID');
  const [paymentMethod, setPaymentMethod] = useState('Cash (USD)');
  const [lineItems, setLineItems] = useState<InvoiceLineItem[]>([
    { description: 'Clinical Examination & Consultation', qty: 1, unitPriceUSD: 15.0, totalUSD: 15.0 },
    { description: 'Defensor 3 Rabies Vaccine Administration', qty: 1, unitPriceUSD: 12.0, totalUSD: 12.0 }
  ]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  React.useEffect(() => {
    const p = pets.find(item => String(item.id) === String(selectedPetId));
    if (p && p.clientName) {
      setClientName(p.clientName);
    }
  }, [selectedPetId, pets]);

  if (!isOpen) return null;

  const currentPet = pets.find(p => String(p.id) === String(selectedPetId)) || null;

  const totalUSD = lineItems.reduce((acc, item) => acc + (item.qty * item.unitPriceUSD), 0);
  const totalLRD = Math.round(totalUSD * usdToLrdRate);

  const handleAddItem = () => {
    setLineItems([...lineItems, { description: 'Medication / Prophylaxis', qty: 1, unitPriceUSD: 10.0, totalUSD: 10.0 }]);
  };

  const handleRemoveItem = (index: number) => {
    if (lineItems.length > 1) {
      setLineItems(lineItems.filter((_, i) => i !== index));
    }
  };

  const handleItemChange = (index: number, field: keyof InvoiceLineItem, value: any) => {
    const updated = [...lineItems];
    (updated[index] as any)[field] = value;
    if (field === 'qty' || field === 'unitPriceUSD') {
      const q = field === 'qty' ? Number(value) : updated[index].qty;
      const p = field === 'unitPriceUSD' ? Number(value) : updated[index].unitPriceUSD;
      updated[index].totalUSD = q * p;
    }
    setLineItems(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!clientName.trim()) {
      setErrorMessage('Please enter the client / owner name.');
      return;
    }
    if (!currentPet) {
      setErrorMessage('Please select a valid registered patient.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const invId = `INV-2026-${Math.floor(100 + Math.random() * 900)}`;
      const paidUSD = paymentOption === 'PAID' ? totalUSD : 0.0;
      const balanceUSD = totalUSD - paidUSD;
      const itemsSummary = lineItems.map(i => i.description).join(', ');

      const newInvoice: InvoiceItem = {
        id: invId,
        clientName: clientName.trim(),
        petName: currentPet ? currentPet.name : 'Patient',
        date: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
        amountUSD: totalUSD,
        amountLRD: totalLRD,
        paidAmountUSD: paidUSD,
        balanceUSD: balanceUSD,
        status: paymentOption === 'PAID' ? 'PAID' : 'UNPAID',
        paymentMethod: paymentOption === 'PAID' ? paymentMethod : undefined,
        itemsSummary,
        lineItems
      };

      await saveInvoiceToFirestore(newInvoice);
      onInvoiceCreated(newInvoice);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to create invoice.');
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
      zIndex: 110,
      padding: '16px'
    }}>
      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '24px',
        maxWidth: '620px',
        width: '100%',
        maxHeight: '92vh',
        overflowY: 'auto',
        padding: '24px',
        boxShadow: 'var(--shadow-lg)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #E5E7EB', paddingBottom: '12px' }}>
          <div>
            <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>
              Create Patient Invoice
            </h3>
            <p style={{ fontSize: '12px', color: '#6B7280', fontWeight: 500 }}>
              Dual Currency (USD / LRD @ {usdToLrdRate} LRD/USD) & Payment Recording
            </p>
          </div>
          <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6B7280' }}>
            <X size={20} />
          </button>
        </div>

        {errorMessage && (
          <div style={{ padding: '10px 14px', borderRadius: '10px', backgroundColor: '#FEF2F2', border: '1px solid #FCA5A5', color: '#991B1B', fontSize: '12px', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <AlertCircle size={16} />
            <span>{errorMessage}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Client / Owner Name *</label>
              <input
                type="text"
                value={clientName}
                onChange={(e) => setClientName(e.target.value)}
                required
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Patient / Pet</label>
              <select
                value={selectedPetId}
                onChange={(e) => setSelectedPetId(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
              >
                {pets.length === 0 ? (
                  <option value="">No registered patients found</option>
                ) : (
                  pets.map(p => (
                    <option key={p.id} value={p.id}>{p.name} ({p.species})</option>
                  ))
                )}
              </select>
            </div>
          </div>

          {/* Line Items */}
          <div style={{ border: '1px solid var(--color-border-subtle)', borderRadius: '14px', padding: '14px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '10px' }}>
              <div style={{ fontSize: '13px', fontWeight: 700, color: '#111827' }}>Services & Medications (USD)</div>
              <button
                type="button"
                onClick={handleAddItem}
                className="btn-outline"
                style={{ padding: '4px 10px', fontSize: '11px', fontWeight: 700, minHeight: '30px' }}
              >
                <Plus size={13} /> Add Line Item
              </button>
            </div>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {lineItems.map((item, idx) => (
                <div key={idx} style={{ display: 'grid', gridTemplateColumns: '2fr 60px 80px 28px', gap: '8px', alignItems: 'center' }}>
                  <input
                    type="text"
                    value={item.description}
                    onChange={(e) => handleItemChange(idx, 'description', e.target.value)}
                    placeholder="Item / Service description"
                    style={{ padding: '7px 10px', borderRadius: '6px', border: '1px solid #D1D5DB', fontSize: '12px', color: '#111827' }}
                  />
                  <input
                    type="number"
                    min="1"
                    value={item.qty}
                    onChange={(e) => handleItemChange(idx, 'qty', parseInt(e.target.value) || 1)}
                    style={{ padding: '7px 8px', borderRadius: '6px', border: '1px solid #D1D5DB', fontSize: '12px', color: '#111827', textAlign: 'center' }}
                  />
                  <input
                    type="number"
                    step="0.5"
                    value={item.unitPriceUSD}
                    onChange={(e) => handleItemChange(idx, 'unitPriceUSD', parseFloat(e.target.value) || 0)}
                    style={{ padding: '7px 8px', borderRadius: '6px', border: '1px solid #D1D5DB', fontSize: '12px', color: '#111827', textAlign: 'right' }}
                  />
                  {lineItems.length > 1 ? (
                    <button
                      type="button"
                      onClick={() => handleRemoveItem(idx)}
                      style={{ background: 'none', border: 'none', color: '#DC2626', cursor: 'pointer', padding: 0 }}
                    >
                      <Trash2 size={16} />
                    </button>
                  ) : <div />}
                </div>
              ))}
            </div>

            {/* Totals */}
            <div style={{ marginTop: '14px', paddingTop: '12px', borderTop: '1px solid #E5E7EB', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <div style={{ fontSize: '12px', color: '#6B7280' }}>
                LRD Equivalent: <strong>LRD {totalLRD.toLocaleString()}</strong>
              </div>
              <div style={{ fontSize: '16px', fontWeight: 800, color: '#111827' }}>
                Total: <span style={{ color: 'var(--color-forest-sage)' }}>${totalUSD.toFixed(2)} USD</span>
              </div>
            </div>
          </div>

          {/* Payment Recording */}
          <div style={{ padding: '14px', backgroundColor: '#F9FAFB', borderRadius: '14px', border: '1px solid #E5E7EB' }}>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>
              Payment Settlement
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '11px', color: '#4B5563', marginBottom: '3px' }}>Status</label>
                <select
                  value={paymentOption}
                  onChange={(e) => setPaymentOption(e.target.value as 'PAID' | 'UNPAID')}
                  style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #D1D5DB', fontSize: '12px', color: '#111827', fontWeight: 600 }}
                >
                  <option value="PAID">✅ Paid In Full</option>
                  <option value="UNPAID">⏳ Pending / Unpaid</option>
                </select>
              </div>
              {paymentOption === 'PAID' && (
                <div>
                  <label style={{ display: 'block', fontSize: '11px', color: '#4B5563', marginBottom: '3px' }}>Payment Method</label>
                  <select
                    value={paymentMethod}
                    onChange={(e) => setPaymentMethod(e.target.value)}
                    style={{ width: '100%', padding: '8px 10px', borderRadius: '6px', border: '1px solid #D1D5DB', fontSize: '12px', color: '#111827', fontWeight: 600 }}
                  >
                    <option value="Cash (USD)">Cash (USD)</option>
                    <option value="Cash (LRD)">Cash (LRD)</option>
                    <option value="Lonestar MTN Mobile Money">Lonestar MTN Mobile Money</option>
                    <option value="Orange Money Liberia">Orange Money Liberia</option>
                    <option value="Visa / Mastercard">Visa / Mastercard</option>
                  </select>
                </div>
              )}
            </div>
          </div>

          <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '6px' }}>
            <button
              type="button"
              onClick={onClose}
              className="btn-secondary"
              style={{ padding: '8px 16px', color: '#111827', fontWeight: 700 }}
              disabled={isSubmitting}
            >
              Cancel
            </button>
            <button
              type="submit"
              className="btn-primary"
              style={{ padding: '8px 20px', fontWeight: 700 }}
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 size={16} className="animate-spin" /> Saving...
                </>
              ) : (
                'Issue Invoice'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
