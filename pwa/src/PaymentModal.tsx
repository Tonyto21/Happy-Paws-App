import React, { useState } from 'react';
import { X, DollarSign, AlertCircle, Loader2 } from 'lucide-react';
import { InvoiceItem, recordInvoicePaymentInFirestore } from './dataService';

interface PaymentModalProps {
  isOpen: boolean;
  onClose: () => void;
  invoice: InvoiceItem | null;
  onPaymentRecorded: (invoiceId: string, amount: number, method: string) => void;
}

export const PaymentModal: React.FC<PaymentModalProps> = ({
  isOpen,
  onClose,
  invoice,
  onPaymentRecorded
}) => {
  const [amountUSD, setAmountUSD] = useState<string>(invoice ? String(invoice.balanceUSD || invoice.amountUSD) : '25');
  const [paymentMethod, setPaymentMethod] = useState('Cash (USD)');
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  if (!isOpen || !invoice) return null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    const payment = parseFloat(amountUSD) || 0;
    if (payment <= 0) {
      setErrorMessage('Please enter a valid payment amount.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      await recordInvoicePaymentInFirestore(invoice.id, payment, paymentMethod);
      onPaymentRecorded(invoice.id, payment, paymentMethod);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to record payment.');
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
        maxWidth: '440px',
        width: '100%',
        padding: '24px',
        boxShadow: 'var(--shadow-lg)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #E5E7EB', paddingBottom: '12px' }}>
          <div>
            <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>
              Record Payment
            </h3>
            <p style={{ fontSize: '12px', color: '#6B7280', fontWeight: 500 }}>
              Invoice {invoice.id} · {invoice.clientName}
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
          <div style={{ padding: '12px', backgroundColor: '#F9FAFB', borderRadius: '10px', fontSize: '13px' }}>
            <div>Total Amount: <strong>${invoice.amountUSD.toFixed(2)} USD</strong></div>
            <div>Already Paid: <strong>${(invoice.paidAmountUSD || 0).toFixed(2)} USD</strong></div>
            <div style={{ color: '#DC2626', fontWeight: 700, marginTop: '4px' }}>
              Outstanding Balance: ${(invoice.balanceUSD != null ? invoice.balanceUSD : invoice.amountUSD).toFixed(2)} USD
            </div>
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              Payment Amount (USD) *
            </label>
            <input
              type="number"
              step="0.5"
              value={amountUSD}
              onChange={(e) => setAmountUSD(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 700, boxSizing: 'border-box' }}
              required
            />
          </div>

          <div>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              Payment Method *
            </label>
            <select
              value={paymentMethod}
              onChange={(e) => setPaymentMethod(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
            >
              <option value="Cash (USD)">Cash (USD)</option>
              <option value="Cash (LRD)">Cash (LRD)</option>
              <option value="Lonestar MTN Mobile Money">Lonestar MTN Mobile Money</option>
              <option value="Orange Money Liberia">Orange Money Liberia</option>
              <option value="Visa / Mastercard">Visa / Mastercard</option>
            </select>
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
                  <Loader2 size={16} className="animate-spin" /> Recording...
                </>
              ) : (
                'Confirm Payment'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
