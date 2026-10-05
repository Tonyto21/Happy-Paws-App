import React, { useState } from 'react';
import { X, Stethoscope, AlertCircle, Syringe, FileText, CheckCircle2, Loader2 } from 'lucide-react';
import { PetRecord, ClinicalExaminationRecord, saveClinicalExamToFirestore, saveVaccinationToFirestore, saveDewormingToFirestore } from './dataService';

interface ClinicalExamModalProps {
  isOpen: boolean;
  onClose: () => void;
  pets: PetRecord[];
  vetName: string;
  onExamSaved: (exam: ClinicalExaminationRecord) => void;
}

export const ClinicalExamModal: React.FC<ClinicalExamModalProps> = ({
  isOpen,
  onClose,
  pets,
  vetName,
  onExamSaved
}) => {
  const [selectedPetId, setSelectedPetId] = useState<number | string>(pets.length > 0 ? pets[0].id : '');
  const [complaint, setComplaint] = useState('Routine Annual Wellness Checkup');
  const [history, setHistory] = useState('Vaccinated previously. No adverse reactions, normal appetite and activity.');
  const [weightKg, setWeightKg] = useState('24.5');
  const [tempC, setTempC] = useState('38.5');
  const [heartRate, setHeartRate] = useState('110');
  const [physicalFindings, setPhysicalFindings] = useState('Bright, alert, responsive. Heart and lungs auscultate clearly. Mucous membranes pink, CRT < 2s.');
  const [diagnosis, setDiagnosis] = useState('Clinically healthy canine. Preventive immunization indicated.');
  const [notes, setNotes] = useState('Good body condition score (BCS 5/9). Teeth clear of calculus.');
  const [treatment, setTreatment] = useState('Administered annual core immunizations and broad-spectrum deworming.');
  const [medication, setMedication] = useState('Drontal Plus Flavor Tablets (2.5 tablets oral once every 3 months)');
  const [instructions, setInstructions] = useState('Feed normal diet. Monitor for mild injection site tenderness. Return in 12 months for booster.');
  const [followUp, setFollowUp] = useState('12 Months (October 2027)');

  // Concurrently record vaccine
  const [recordVaccine, setRecordVaccine] = useState(true);
  const [vaccineName, setVaccineName] = useState('Rabies (Defensor 3)');
  const [batchNumber, setBatchNumber] = useState('RB-2026-LR');

  const [isSubmitting, setIsSubmitting] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  if (!isOpen) return null;

  const currentPet = pets.find(p => String(p.id) === String(selectedPetId)) || null;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!currentPet) {
      setErrorMessage('Please select a valid registered patient before saving.');
      return;
    }

    setIsSubmitting(true);
    setErrorMessage('');

    try {
      const examId = `EXAM-${Date.now().toString().slice(-6)}`;
      const examRecord: ClinicalExaminationRecord = {
        id: examId,
        petId: currentPet.id,
        petName: currentPet.name,
        clientName: currentPet.clientName || 'Registered Client',
        vetName: vetName || 'Dr. David Kpadeh, DVM',
        date: new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }),
        presentingComplaint: complaint.trim(),
        relevantHistory: history.trim(),
        weightKg: parseFloat(weightKg) || currentPet.weightKg,
        tempC: tempC.trim(),
        heartRateBpm: heartRate.trim(),
        physicalFindings: physicalFindings.trim(),
        diagnosis: diagnosis.trim(),
        assessmentNotes: notes.trim(),
        treatment: treatment.trim(),
        medication: medication.trim(),
        instructions: instructions.trim(),
        followUp: followUp.trim()
      };

      // 1. Save consultation to Firestore
      await saveClinicalExamToFirestore(examRecord);

      // 2. Concurrently record vaccine if toggled
      if (recordVaccine && vaccineName) {
        await saveVaccinationToFirestore({
          id: Date.now(),
          petName: currentPet.name,
          petId: currentPet.id,
          vaccineName: vaccineName,
          dateAdministered: examRecord.date,
          validUntil: '1 Year from today',
          batchNumber: batchNumber.trim() || 'HP-BATCH-2026',
          vetName: examRecord.vetName,
          status: 'Up to Date'
        });
      }

      onExamSaved(examRecord);
      onClose();
    } catch (err: any) {
      setErrorMessage(err.message || 'Failed to save examination record.');
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
        maxWidth: '680px',
        width: '100%',
        maxHeight: '92vh',
        overflowY: 'auto',
        padding: '24px',
        boxShadow: 'var(--shadow-lg)'
      }}>
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px', borderBottom: '1px solid #E5E7EB', paddingBottom: '12px' }}>
          <div>
            <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>
              Clinical Examination & Consultation
            </h3>
            <p style={{ fontSize: '12px', color: '#6B7280', fontWeight: 500 }}>
              Structured Workflow: History → Examination → Assessment → Treatment Plan
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

        <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
          {/* Patient Selection */}
          <div style={{ padding: '12px 14px', backgroundColor: '#F9FAFB', borderRadius: '12px', border: '1px solid #E5E7EB' }}>
            <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              Select Patient *
            </label>
            <select
              value={selectedPetId}
              onChange={(e) => setSelectedPetId(e.target.value)}
              style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
            >
              {pets.length === 0 ? (
                <option value="">No registered patients found</option>
              ) : (
                pets.map(p => (
                  <option key={p.id} value={p.id}>
                    {p.name} ({p.species} · Tag #{p.rabiesTag})
                  </option>
                ))
              )}
            </select>
          </div>

          {/* 1. History */}
          <div style={{ border: '1px solid var(--color-border-subtle)', borderRadius: '14px', padding: '14px' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-amber-terracotta)', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <FileText size={15} /> 1. History & Presenting Complaint
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Presenting Complaint</label>
                <input
                  type="text"
                  value={complaint}
                  onChange={(e) => setComplaint(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Relevant Medical & Dietary History</label>
                <textarea
                  value={history}
                  onChange={(e) => setHistory(e.target.value)}
                  rows={2}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box', resize: 'none' }}
                />
              </div>
            </div>
          </div>

          {/* 2. Examination (Vitals & Physical Findings) */}
          <div style={{ border: '1px solid var(--color-border-subtle)', borderRadius: '14px', padding: '14px' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-forest-sage)', marginBottom: '8px', display: 'flex', alignItems: 'center', gap: '6px' }}>
              <Stethoscope size={15} /> 2. Physical Examination & Vitals
            </div>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px', marginBottom: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Weight (kg)</label>
                <input
                  type="number"
                  step="0.1"
                  value={weightKg}
                  onChange={(e) => setWeightKg(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Temp (°C)</label>
                <input
                  type="text"
                  value={tempC}
                  onChange={(e) => setTempC(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Heart Rate (bpm)</label>
                <input
                  type="text"
                  value={heartRate}
                  onChange={(e) => setHeartRate(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
            </div>
            <div>
              <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Physical Findings</label>
              <textarea
                value={physicalFindings}
                onChange={(e) => setPhysicalFindings(e.target.value)}
                rows={2}
                style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box', resize: 'none' }}
              />
            </div>
          </div>

          {/* 3. Clinical Assessment */}
          <div style={{ border: '1px solid var(--color-border-subtle)', borderRadius: '14px', padding: '14px' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>
              3. Clinical Assessment & Diagnosis
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Primary Diagnosis *</label>
                <input
                  type="text"
                  value={diagnosis}
                  onChange={(e) => setDiagnosis(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
                  required
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Assessment Notes</label>
                <input
                  type="text"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
            </div>
          </div>

          {/* 4. Treatment Plan */}
          <div style={{ border: '1px solid var(--color-border-subtle)', borderRadius: '14px', padding: '14px' }}>
            <div style={{ fontSize: '13px', fontWeight: 700, color: 'var(--color-amber-terracotta)', marginBottom: '8px' }}>
              4. Treatment Plan & Prescriptions
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Treatment Administered</label>
                <input
                  type="text"
                  value={treatment}
                  onChange={(e) => setTreatment(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Medications / Prescriptions Dispensed</label>
                <input
                  type="text"
                  value={medication}
                  onChange={(e) => setMedication(e.target.value)}
                  style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                />
              </div>
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Owner Instructions</label>
                  <input
                    type="text"
                    value={instructions}
                    onChange={(e) => setInstructions(e.target.value)}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#4B5563', marginBottom: '3px' }}>Follow-up Date / Timeline</label>
                  <input
                    type="text"
                    value={followUp}
                    onChange={(e) => setFollowUp(e.target.value)}
                    style={{ width: '100%', padding: '8px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', boxSizing: 'border-box' }}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Record Vaccine Toggle */}
          <div style={{ padding: '12px 14px', backgroundColor: '#F0FDF4', borderRadius: '12px', border: '1px solid #BBF7D0' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', fontSize: '12px', fontWeight: 700, color: '#166534', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={recordVaccine}
                onChange={(e) => setRecordVaccine(e.target.checked)}
              />
              <Syringe size={16} /> Record Administered Vaccine to Official Pet Immunization Record
            </label>
            {recordVaccine && (
              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginTop: '10px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#166534', marginBottom: '2px' }}>Vaccine Name</label>
                  <input
                    type="text"
                    value={vaccineName}
                    onChange={(e) => setVaccineName(e.target.value)}
                    style={{ width: '100%', padding: '7px 10px', borderRadius: '6px', border: '1px solid #86EFAC', fontSize: '12px', color: '#111827', boxSizing: 'border-box' }}
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '11px', fontWeight: 700, color: '#166534', marginBottom: '2px' }}>Batch / Lot #</label>
                  <input
                    type="text"
                    value={batchNumber}
                    onChange={(e) => setBatchNumber(e.target.value)}
                    style={{ width: '100%', padding: '7px 10px', borderRadius: '6px', border: '1px solid #86EFAC', fontSize: '12px', color: '#111827', boxSizing: 'border-box' }}
                  />
                </div>
              </div>
            )}
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
              style={{ padding: '8px 20px', fontWeight: 700, backgroundColor: 'var(--color-forest-sage)' }}
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <Loader2 size={16} className="animate-spin" /> Saving...
                </>
              ) : (
                'Save Consultation & Treatment Plan'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
