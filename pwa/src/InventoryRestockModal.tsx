import React, { useState } from 'react';
import { X, Package, AlertCircle, Loader2 } from 'lucide-react';
import { InventoryStock, updateInventoryStockInFirestore } from './dataService';

interface InventoryRestockModalProps {
  isOpen: boolean;
  onClose: () => void;
  item: InventoryStock | null;
  onRestockSaved: (updatedItem: InventoryStock) => void;
}

export const InventoryRestockModal: React.FC<InventoryRestockModalProps> = ({
  isOpen,
  onClose,
  item,
  onRestockSaved
}) => {
  const [adjustmentAmount, setAdjustmentAmount] = useState<string>('10');
  const [adjustmentType, setAdjustmentType] = useState<'ADD' | 'DEDUCT'>('ADD');
  const [batchNumber, setBatchNumber] = useState(item?.batchNumber || '');
  const [expiryDate, setExpiryDate] = useState(item?.expiryDate || '2026-12-31');
  const [reason, setReason] = useState('New shipment received from certified distributor');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  if (!isOpen || !item) return null;

  const countChange = parseInt(adjustmentAmount, 10) || 0;
  const newStock = adjustmentType === 'ADD' 
    ? item.stockCount + countChange 
    : Math.max(0, item.stockCount - countChange);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (countChange <= 0) {
      setErrorMessage('Please enter a valid quantity.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      await updateInventoryStockInFirestore(item.id, newStock, `${adjustmentType}: ${countChange} units (${reason})`);
      const updated: InventoryStock = {
        ...item,
        stockCount: newStock,
        batchNumber: batchNumber.trim() || item.batchNumber,
        expiryDate: expiryDate.trim() || item.expiryDate
      };
      onRestockSaved(updated);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to adjust inventory.');
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
        maxWidth: '460px',
        width: '100%',
        padding: '24px',
        boxShadow: 'var(--shadow-lg)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #E5E7EB', paddingBottom: '12px' }}>
          <div>
            <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>
              Restock & Stock Adjustment
            </h3>
            <p style={{ fontSize: '12px', color: '#6B7280', fontWeight: 500 }}>
              {item.name} ({item.category})
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
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Action</label>
              <select
                value={adjustmentType}
                onChange={(e) => setAdjustmentType(e.target.value as 'ADD' | 'DEDUCT')}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
              >
                <option value="ADD">➕ Restock (Add)</option>
                <option value="DEDUCT">➖ Dispense / Deduct</option>
              </select>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Quantity ({item.unit})</label>
              <input
                type="number"
                min="1"
                value={adjustmentAmount}
                onChange={(e) => setAdjustmentAmount(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
                required
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Batch / Lot #</label>
              <input
                type="text"
                value={batchNumber}
                onChange={(e) => setBatchNumber(e.target.value)}
                placeholder="e.g. RB-99482"
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Expiry Date (FEFO)</label>
              <input
                type="date"
                value={expiryDate}
                onChange={(e) => setExpiryDate(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Reason / Notes</label>
            <input
              type="text"
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
            />
          </div>

          <div style={{ padding: '12px', backgroundColor: '#F9FAFB', borderRadius: '10px', fontSize: '12px', color: '#4B5563' }}>
            Current Stock: <strong>{item.stockCount} {item.unit}</strong> → New Stock: <strong style={{ color: 'var(--color-forest-sage)' }}>{newStock} {item.unit}</strong>
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
                'Save Stock Update'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
