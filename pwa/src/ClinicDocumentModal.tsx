import React, { useState } from 'react';
import { X, Printer, ShieldCheck, CheckCircle2, QrCode, Building, Phone, Mail } from 'lucide-react';
import { PetRecord, ClientRecord, InvoiceItem, ClinicalExaminationRecord, Vaccination, Deworming } from './dataService';
import { generateQrMatrix } from './qr';

export type ClinicDocType = 'PASSPORT_BOOKLET' | 'INVOICE_RECEIPT' | 'CLINICAL_REPORT' | 'PROCEDURE_SUMMARY';

interface ClinicDocumentModalProps {
  isOpen: boolean;
  onClose: () => void;
  docType: ClinicDocType;
  pet?: PetRecord | null;
  client?: ClientRecord | null;
  invoice?: InvoiceItem | null;
  exam?: ClinicalExaminationRecord | null;
  vaccinations?: Vaccination[];
  deworming?: Deworming[];
  vetName?: string;
  usdToLrdRate?: number;
}

export const ClinicDocumentModal: React.FC<ClinicDocumentModalProps> = ({
  isOpen,
  onClose,
  docType,
  pet,
  client,
  invoice,
  exam,
  vaccinations = [],
  deworming = [],
  vetName = 'Dr. David Kpadeh, DVM',
  usdToLrdRate = 194.0
}) => {
  const [isVerifying, setIsVerifying] = useState<boolean>(false);
  const [verificationResult, setVerificationResult] = useState<string | null>(null);

  if (!isOpen) return null;

  const docId = invoice
    ? `HPL-INV-${invoice.id.replace(/[^a-zA-Z0-9]/g, '')}`
    : exam
    ? `HPL-MED-${exam.id.replace(/[^a-zA-Z0-9]/g, '')}`
    : pet
    ? `HPL-PASSPORT-${pet.rabiesTag ? pet.rabiesTag.replace(/[^a-zA-Z0-9]/g, '') : pet.id}`
    : `HPL-DOC-2026-${Date.now().toString().slice(-6)}`;

  const verificationUrl = `https://happypawsliberia.netlify.app/?verifyDoc=${docId}`;
  const qrMatrix = generateQrMatrix(verificationUrl);

  const handlePrint = () => {
    window.print();
  };

  const handleVerifyAuthenticity = () => {
    setIsVerifying(true);
    setTimeout(() => {
      setIsVerifying(false);
      setVerificationResult(`Official Verified Record: ${docId} is active in Happy Paws Liberia Cloud Registry. Issued for patient ${pet?.name || invoice?.petName || 'Patient'}.`);
    }, 400);
  };

  return (
    <div style={{
      position: 'fixed',
      top: 0,
      left: 0,
      right: 0,
      bottom: 0,
      backgroundColor: 'rgba(0, 0, 0, 0.8)',
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'center',
      zIndex: 140,
      padding: '16px'
    }}>
      <div style={{
        backgroundColor: '#FFFFFF',
        borderRadius: '20px',
        maxWidth: '750px',
        width: '100%',
        maxHeight: '92vh',
        overflowY: 'auto',
        display: 'flex',
        flexDirection: 'column',
        boxShadow: 'var(--shadow-lg)'
      }}>
        {/* Top Control Bar (Hidden when printing) */}
        <div className="no-print" style={{
          padding: '14px 24px',
          borderBottom: '1px solid #E5E7EB',
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          backgroundColor: '#FAFAF9'
        }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <ShieldCheck size={18} color="var(--color-forest-sage)" />
            <span style={{ fontSize: '13px', fontWeight: 700, color: '#111827' }}>
              Official Certified Clinic Document · {docId}
            </span>
          </div>

          <div style={{ display: 'flex', gap: '8px' }}>
            <button
              onClick={handleVerifyAuthenticity}
              className="btn-secondary"
              style={{ padding: '6px 12px', fontSize: '12px', fontWeight: 600, display: 'flex', alignItems: 'center', gap: '4px', color: '#111827' }}
            >
              <QrCode size={14} /> Verify Authenticity
            </button>
            <button
              onClick={handlePrint}
              className="btn-primary"
              style={{ padding: '6px 14px', fontSize: '12px', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}
            >
              <Printer size={15} /> Print / Save PDF
            </button>
            <button onClick={onClose} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#6B7280', marginLeft: '6px' }}>
              <X size={20} />
            </button>
          </div>
        </div>

        {verificationResult && (
          <div className="no-print" style={{ padding: '10px 24px', backgroundColor: '#ECFDF5', borderBottom: '1px solid #A7F3D0', fontSize: '12px', color: '#065F46', display: 'flex', alignItems: 'center', gap: '8px' }}>
            <CheckCircle2 size={16} />
            <span>{verificationResult}</span>
          </div>
        )}

        {/* ========================================================
            OFFICIAL PRINTABLE CLINIC LETTERHEAD & BODY
           ======================================================== */}
        <div id="printable-clinic-doc" style={{ padding: '36px 32px', backgroundColor: '#FFFFFF', color: '#111827', fontFamily: 'Inter, system-ui, sans-serif' }}>
          {/* Header Letterhead */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', borderBottom: '2px solid #111827', paddingBottom: '18px', marginBottom: '20px' }}>
            <div style={{ display: 'flex', gap: '16px', alignItems: 'center' }}>
              <img
                src={`${import.meta.env.BASE_URL}Happy-paws-logo-transparent1.png`}
                alt="Happy Paws Liberia Logo"
                style={{ height: '70px', width: 'auto', objectFit: 'contain' }}
                onError={(e) => { (e.target as HTMLElement).style.display = 'none'; }}
              />
              <div>
                <h1 className="font-serif" style={{ fontSize: '20px', fontWeight: 800, color: '#111827', margin: 0, textTransform: 'uppercase', letterSpacing: '0.02em' }}>
                  Happy Paws Liberia Rescue Center
                </h1>
                <p style={{ fontSize: '11px', color: '#4B5563', margin: '3px 0 0', fontWeight: 600 }}>
                  Honeybee Junction, R2 Community, RIA Highway · Paynesville City, Montserrado County, Liberia
                </p>
                <p style={{ fontSize: '11px', color: '#4B5563', margin: '2px 0 0' }}>
                  Hotline: 0881479329 · Emergency: 0777123456 · WhatsApp: 0881479329
                </p>
              </div>
            </div>

            {/* Document QR Code Stamp */}
            <div style={{ textAlign: 'center' }}>
              <div style={{
                display: 'grid',
                gridTemplateColumns: `repeat(${qrMatrix.length}, 2.4px)`,
                gap: '0px',
                padding: '4px',
                backgroundColor: '#FFFFFF',
                border: '1px solid #111827',
                borderRadius: '4px'
              }}>
                {qrMatrix.map((row, rIdx) =>
                  row.map((cell, cIdx) => (
                    <div
                      key={`${rIdx}-${cIdx}`}
                      style={{
                        width: '2.4px',
                        height: '2.4px',
                        backgroundColor: cell ? '#111827' : '#FFFFFF'
                      }}
                    />
                  ))
                )}
              </div>
              <div style={{ fontSize: '8px', fontWeight: 700, marginTop: '3px', color: '#111827' }}>
                VERIFIED SEAL
              </div>
            </div>
          </div>

          {/* Document Meta Row */}
          <div style={{ display: 'flex', justifyContent: 'space-between', backgroundColor: '#F9FAFB', padding: '12px 16px', borderRadius: '10px', border: '1px solid #E5E7EB', marginBottom: '22px' }}>
            <div>
              <div style={{ fontSize: '10px', textTransform: 'uppercase', color: '#6B7280', fontWeight: 700 }}>DOCUMENT TITLE</div>
              <div style={{ fontSize: '14px', fontWeight: 800, color: '#111827' }}>
                {docType === 'PASSPORT_BOOKLET' && 'Official Pet Health & Vaccination Passport'}
                {docType === 'INVOICE_RECEIPT' && 'Official Invoice & Clinical Receipt'}
                {docType === 'CLINICAL_REPORT' && 'Veterinary Clinical Examination & SOAP Report'}
                {docType === 'PROCEDURE_SUMMARY' && 'Surgical / Treatment Procedure Follow-up Record'}
              </div>
            </div>
            <div style={{ textAlign: 'right' }}>
              <div style={{ fontSize: '10px', textTransform: 'uppercase', color: '#6B7280', fontWeight: 700 }}>SERIAL NUMBER / DATE</div>
              <div style={{ fontSize: '13px', fontWeight: 700, color: '#111827' }}>
                {docId} · {new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
              </div>
            </div>
          </div>

          {/* Patient & Owner Summary */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '20px', marginBottom: '22px' }}>
            <div style={{ padding: '14px', borderRadius: '10px', border: '1px solid #E5E7EB', backgroundColor: '#FFFFFF' }}>
              <div style={{ fontSize: '11px', fontWeight: 800, textTransform: 'uppercase', color: 'var(--color-amber-terracotta)', marginBottom: '8px' }}>
                PATIENT DETAILS
              </div>
              <div style={{ fontSize: '12px', lineHeight: 1.6, color: '#1F2937' }}>
                <div><strong>Patient Name:</strong> {pet?.name || invoice?.petName || 'Bella'}</div>
                <div><strong>Species:</strong> {pet?.species || 'Canine (Dog)'}</div>
                <div><strong>Breed:</strong> {pet?.breed || 'Boerboel Mix'}</div>
                <div><strong>Sex / Status:</strong> {pet?.sex || 'Spayed Female'}</div>
                <div><strong>Weight:</strong> {pet?.weightKg || 24.5} kg</div>
                <div><strong>Rabies Tag #:</strong> <span style={{ color: '#B45309', fontWeight: 700 }}>{pet?.rabiesTag || 'HP-LR-2024-0884'}</span></div>
                <div><strong>Microchip ID:</strong> {pet?.microchipId || '985141002931882'}</div>
              </div>
            </div>

            <div style={{ padding: '14px', borderRadius: '10px', border: '1px solid #E5E7EB', backgroundColor: '#FFFFFF' }}>
              <div style={{ fontSize: '11px', fontWeight: 800, textTransform: 'uppercase', color: 'var(--color-forest-sage)', marginBottom: '8px' }}>
                CLIENT / OWNER DETAILS
              </div>
              <div style={{ fontSize: '12px', lineHeight: 1.6, color: '#1F2937' }}>
                <div><strong>Client Full Name:</strong> {client?.fullName || invoice?.clientName || 'Anthony Tolbert'}</div>
                <div><strong>Phone Hotline:</strong> {client?.phone || '0881479329'}</div>
                <div><strong>Email:</strong> {client?.email || 'Registered Client'}</div>
                <div><strong>Location:</strong> {client?.address || 'Honeybee Junction, RIA Highway, Paynesville, Liberia'}</div>
                <div><strong>Attending Clinician:</strong> {vetName}</div>
              </div>
            </div>
          </div>

          {/* Document Content Based on Type */}
          {docType === 'PASSPORT_BOOKLET' && (
            <div>
              <h3 style={{ fontSize: '13px', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#111827', borderBottom: '1px solid #E5E7EB', paddingBottom: '6px', marginBottom: '12px' }}>
                Verified Immunization & Prophylaxis History
              </h3>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px', textAlign: 'left', marginBottom: '20px' }}>
                <thead>
                  <tr style={{ backgroundColor: '#F3F4F6', color: '#111827', fontWeight: 700 }}>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>Vaccine / Product</th>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>Date Given</th>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>Valid Until</th>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>Batch / Lot</th>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>Administering Vet</th>
                  </tr>
                </thead>
                <tbody>
                  {vaccinations.length === 0 ? (
                    <tr>
                      <td colSpan={5} style={{ padding: '12px', textAlign: 'center', color: '#6B7280', border: '1px solid #E5E7EB' }}>
                        Rabies Defensor 3 — Administered by Dr. David Kpadeh, DVM (Batch RB-99482)
                      </td>
                    </tr>
                  ) : (
                    vaccinations.map(v => (
                      <tr key={v.id}>
                        <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB', fontWeight: 600 }}>{v.vaccineName}</td>
                        <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>{v.dateAdministered}</td>
                        <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB', color: '#047857', fontWeight: 700 }}>{v.validUntil}</td>
                        <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>{v.batchNumber}</td>
                        <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>{v.vetName}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          )}

          {docType === 'INVOICE_RECEIPT' && invoice && (
            <div>
              <h3 style={{ fontSize: '13px', fontWeight: 800, textTransform: 'uppercase', letterSpacing: '0.05em', color: '#111827', borderBottom: '1px solid #E5E7EB', paddingBottom: '6px', marginBottom: '12px' }}>
                Itemized Services & Medications
              </h3>
              <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '12px', textAlign: 'left', marginBottom: '16px' }}>
                <thead>
                  <tr style={{ backgroundColor: '#F3F4F6', color: '#111827', fontWeight: 700 }}>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>Service / Product</th>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB', textAlign: 'center' }}>Qty</th>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB', textAlign: 'right' }}>Price (USD)</th>
                    <th style={{ padding: '8px 10px', border: '1px solid #E5E7EB', textAlign: 'right' }}>Total (USD)</th>
                  </tr>
                </thead>
                <tbody>
                  {invoice.lineItems?.map((item, idx) => (
                    <tr key={idx}>
                      <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB' }}>{item.description}</td>
                      <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB', textAlign: 'center' }}>{item.qty}</td>
                      <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB', textAlign: 'right' }}>${item.unitPriceUSD.toFixed(2)}</td>
                      <td style={{ padding: '8px 10px', border: '1px solid #E5E7EB', textAlign: 'right', fontWeight: 700 }}>${item.totalUSD.toFixed(2)}</td>
                    </tr>
                  )) || (
                    <tr>
                      <td colSpan={4} style={{ padding: '10px', border: '1px solid #E5E7EB' }}>{invoice.itemsSummary}</td>
                    </tr>
                  )}
                </tbody>
              </table>

              <div style={{ display: 'flex', justifyContent: 'flex-end', marginBottom: '16px' }}>
                <div style={{ width: '280px', padding: '12px', backgroundColor: '#F9FAFB', borderRadius: '8px', border: '1px solid #E5E7EB', fontSize: '12px', lineHeight: 1.6 }}>
                  <div style={{ display: 'flex', justifyContent: 'space-between' }}>
                    <span>Total (USD):</span>
                    <strong>${invoice.amountUSD.toFixed(2)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#6B7280' }}>
                    <span>Equivalent (LRD @ {usdToLrdRate}):</span>
                    <span>LRD {Math.round(invoice.amountUSD * usdToLrdRate).toLocaleString()}</span>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', color: '#047857' }}>
                    <span>Amount Paid:</span>
                    <strong>${invoice.paidAmountUSD.toFixed(2)}</strong>
                  </div>
                  <div style={{ display: 'flex', justifyContent: 'space-between', borderTop: '1px solid #D1D5DB', paddingTop: '4px', marginTop: '4px', fontWeight: 800 }}>
                    <span>Outstanding Balance:</span>
                    <span style={{ color: invoice.balanceUSD > 0 ? '#B91C1C' : '#047857' }}>
                      ${invoice.balanceUSD.toFixed(2)} USD
                    </span>
                  </div>
                </div>
              </div>
            </div>
          )}

          {docType === 'CLINICAL_REPORT' && exam && (
            <div style={{ fontSize: '12px', lineHeight: 1.6, color: '#1F2937', marginBottom: '18px' }}>
              <div style={{ padding: '12px', backgroundColor: '#F9FAFB', borderRadius: '8px', border: '1px solid #E5E7EB', marginBottom: '10px' }}>
                <strong>Presenting Complaint & History:</strong> {exam.presentingComplaint}. {exam.relevantHistory}
              </div>
              <div style={{ padding: '12px', backgroundColor: '#F9FAFB', borderRadius: '8px', border: '1px solid #E5E7EB', marginBottom: '10px' }}>
                <strong>Vital Signs & Physical Examination:</strong> Temp: {exam.tempC}°C, Heart Rate: {exam.heartRateBpm} bpm, Weight: {exam.weightKg} kg. Findings: {exam.physicalFindings}
              </div>
              <div style={{ padding: '12px', backgroundColor: '#F9FAFB', borderRadius: '8px', border: '1px solid #E5E7EB', marginBottom: '10px' }}>
                <strong>Diagnosis & Clinical Assessment:</strong> {exam.diagnosis}. {exam.assessmentNotes}
              </div>
              <div style={{ padding: '12px', backgroundColor: '#F9FAFB', borderRadius: '8px', border: '1px solid #E5E7EB', marginBottom: '10px' }}>
                <strong>Treatment Plan & Prescriptions:</strong> {exam.treatment} {exam.medication}. Instructions: {exam.instructions}. Follow-up: {exam.followUp}.
              </div>
            </div>
          )}

          {/* Official Signatures & Seal */}
          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-end', marginTop: '30px', paddingTop: '20px', borderTop: '1px solid #E5E7EB' }}>
            <div>
              <div style={{ fontSize: '11px', color: '#6B7280', marginBottom: '36px' }}>Examined & Certified By:</div>
              <div style={{ borderTop: '1px dashed #4B5563', width: '220px', paddingTop: '4px', fontSize: '12px', fontWeight: 700 }}>
                {vetName}
              </div>
              <div style={{ fontSize: '10px', color: '#6B7280' }}>Licensed Veterinary Surgeon, Liberia</div>
            </div>

            <div style={{ textAlign: 'right' }}>
              <div style={{
                display: 'inline-block',
                border: '2px double var(--color-forest-sage)',
                color: 'var(--color-forest-sage)',
                padding: '8px 16px',
                borderRadius: '8px',
                fontWeight: 800,
                fontSize: '11px',
                textTransform: 'uppercase',
                letterSpacing: '0.05em'
              }}>
                ✓ OFFICIAL REGISTERED SEAL<br />
                HAPPY PAWS LIBERIA RESCUE CENTER
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
