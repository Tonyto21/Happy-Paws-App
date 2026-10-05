import React, { useState, useEffect } from 'react';
import { X, Package, AlertCircle, Check, Loader2 } from 'lucide-react';
import { InventoryStock, saveInventoryItemToFirestore } from './dataService';

interface InventoryItemModalProps {
  isOpen: boolean;
  onClose: () => void;
  itemToEdit?: InventoryStock | null;
  onItemSaved: (item: InventoryStock) => void;
}

export const InventoryItemModal: React.FC<InventoryItemModalProps> = ({
  isOpen,
  onClose,
  itemToEdit,
  onItemSaved
}) => {
  const [name, setName] = useState('');
  const [category, setCategory] = useState('Vaccine');
  const [stockCount, setStockCount] = useState('20');
  const [unit, setUnit] = useState('vials');
  const [minimumThreshold, setMinimumThreshold] = useState('10');
  const [unitPriceUSD, setUnitPriceUSD] = useState('12.0');
  const [batchNumber, setBatchNumber] = useState('');
  const [expiryDate, setExpiryDate] = useState('2026-12-31');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  useEffect(() => {
    if (itemToEdit) {
      setName(itemToEdit.name || '');
      setCategory(itemToEdit.category || 'Vaccine');
      setStockCount(String(itemToEdit.stockCount ?? 20));
      setUnit(itemToEdit.unit || 'vials');
      setMinimumThreshold(String(itemToEdit.minimumThreshold ?? 10));
      setUnitPriceUSD(String(itemToEdit.unitPriceUSD ?? 12.0));
      setBatchNumber(itemToEdit.batchNumber || '');
      setExpiryDate(itemToEdit.expiryDate || '2026-12-31');
    } else {
      setName('');
      setCategory('Vaccine');
      setStockCount('25');
      setUnit('vials');
      setMinimumThreshold('10');
      setUnitPriceUSD('15.0');
      setBatchNumber(`LOT-${Date.now().toString().slice(-5)}`);
      setExpiryDate('2026-12-31');
    }
    setErrorMessage('');
  }, [itemToEdit, isOpen]);

  if (!isOpen) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!name.trim()) {
      setErrorMessage('Please enter the inventory item name.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const targetId = itemToEdit ? itemToEdit.id : Date.now();
      const savedItem: InventoryStock = {
        id: targetId,
        name: name.trim(),
        category,
        stockCount: parseInt(stockCount, 10) || 0,
        unit: unit.trim() || 'units',
        minimumThreshold: parseInt(minimumThreshold, 10) || 5,
        unitPriceUSD: parseFloat(unitPriceUSD) || 0.0,
        batchNumber: batchNumber.trim() || undefined,
        expiryDate: expiryDate.trim() || undefined
      };

      await saveInventoryItemToFirestore(savedItem);
      onItemSaved(savedItem);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to save inventory item.');
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
      zIndex: 115,
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
              <Package size={20} color="var(--color-amber-terracotta)" />
            </div>
            <div>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>
                {itemToEdit ? 'Edit Inventory Item' : 'Add Clinic Inventory Item'}
              </h3>
              <p style={{ fontSize: '12px', color: '#6B7280', fontWeight: 500 }}>
                Pharmaceutical, Vaccine, and Medical Stock
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
              Product / Medication Name *
            </label>
            <input
              type="text"
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="e.g. Rabies Vaccine (Defensor 3) / Amoxicillin"
              required
              style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
              >
                <option value="Vaccine">Vaccine</option>
                <option value="Parasiticide">Parasiticide / Dewormer</option>
                <option value="Antibiotic">Antibiotic</option>
                <option value="Analgesic">Analgesic / Anti-inflammatory</option>
                <option value="Surgical">Surgical / Wound Care</option>
                <option value="Diagnostic">Diagnostic / Test Kits</option>
                <option value="Consumable">Consumable / Supply</option>
                <option value="Pet Care">Pet Care / Nutritional</option>
              </select>
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Unit of Measure
              </label>
              <input
                type="text"
                value={unit}
                onChange={(e) => setUnit(e.target.value)}
                placeholder="vials / tablets / packs"
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: '10px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Initial Stock
              </label>
              <input
                type="number"
                value={stockCount}
                onChange={(e) => setStockCount(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Reorder Threshold
              </label>
              <input
                type="number"
                value={minimumThreshold}
                onChange={(e) => setMinimumThreshold(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Selling Price ($)
              </label>
              <input
                type="number"
                step="0.5"
                value={unitPriceUSD}
                onChange={(e) => setUnitPriceUSD(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Batch / Lot Number
              </label>
              <input
                type="text"
                value={batchNumber}
                onChange={(e) => setBatchNumber(e.target.value)}
                placeholder="e.g. RB-99482"
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>

            <div>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                Expiry Date (FEFO)
              </label>
              <input
                type="date"
                value={expiryDate}
                onChange={(e) => setExpiryDate(e.target.value)}
                style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
              />
            </div>
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
                  <Loader2 size={16} className="animate-spin" /> Saving Item...
                </>
              ) : (
                <>
                  <Check size={16} /> Save Inventory Item
                </>
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
