import React, { useState, useEffect } from 'react';
import { auth } from './firebase';
import { onAuthStateChanged, signInWithEmailAndPassword } from 'firebase/auth';
import { 
  Wifi, 
  WifiOff, 
  HeartHandshake,
  Stethoscope,
  Calendar,
  Syringe,
  Package,
  Receipt,
  Printer,
  Plus,
  Check,
  CheckCircle2,
  Clock,
  User,
  LogIn,
  Phone,
  QrCode,
  ScanLine
} from 'lucide-react';
import { generateQrMatrix } from './qr';
import type { UserRole, ClinicSettings } from './types';

// Types for local state
interface PetRecord {
  id: number;
  name: string;
  species: string;
  breed: string;
  sex: string;
  dob: string;
  weightKg: number;
  color: string;
  microchipId: string;
  rabiesTag: string;
  photoUrl: string;
  notes: string;
}

interface Vaccination {
  id: number;
  petName: string;
  vaccineName: string;
  dateAdministered: string;
  validUntil: string;
  batchNumber: string;
  vetName: string;
  status: 'Up to Date' | 'Due Soon' | 'Overdue';
}

interface Deworming {
  id: number;
  petName: string;
  productName: string;
  dateGiven: string;
  nextDueDate: string;
  weightKg: number;
  dosage: string;
}

interface AppointmentItem {
  id: number;
  clientName: string;
  petName: string;
  species: string;
  time: string;
  reason: string;
  vetName: string;
  status: 'Confirmed' | 'In Consultation' | 'Completed' | 'Requested';
}

interface InventoryStock {
  id: number;
  name: string;
  category: 'Vaccine' | 'Antibiotic' | 'Parasiticide' | 'Surgical' | 'Supplies';
  stockCount: number;
  unit: string;
  minimumThreshold: number;
  unitPriceUSD: number;
}

interface InvoiceItem {
  id: string;
  clientName: string;
  petName: string;
  date: string;
  amountUSD: number;
  amountLRD: number;
  status: 'PAID' | 'PENDING';
  itemsSummary: string;
}

export default function App() {
  const [isOnline, setIsOnline] = useState<boolean>(navigator.onLine);
  const [firebaseStatus, setFirebaseStatus] = useState<'checking' | 'connected' | 'offline'>('connected');
  const [swRegistered, setSwRegistered] = useState<boolean>(false);
  const [clinicInfo, setClinicInfo] = useState<ClinicSettings | null>(null);

  // User & Portal Mode State
  const [currentRole, setCurrentRole] = useState<UserRole>('pet_owner');
  const [activeTab, setActiveTab] = useState<'passport' | 'vaccines' | 'appointments' | 'consultations' | 'inventory' | 'billing'>('passport');
  const [selectedPetIndex, setSelectedPetIndex] = useState<number>(0);
  const [showPassportModal, setShowPassportModal] = useState<boolean>(false);
  const [showPetQrModal, setShowPetQrModal] = useState<boolean>(false);
  const [showReceptionScannerModal, setShowReceptionScannerModal] = useState<boolean>(false);
  const [showDossierModal, setShowDossierModal] = useState<boolean>(false);
  const [scannedPetDossier, setScannedPetDossier] = useState<PetRecord | null>(null);
  const [manualCodeInput, setManualCodeInput] = useState<string>('');
  const [admitSuccess, setAdmitSuccess] = useState<boolean>(false);
  const [showAuthModal, setShowAuthModal] = useState<boolean>(false);
  const [showNewAppointmentModal, setShowNewAppointmentModal] = useState<boolean>(false);
  const [authEmail, setAuthEmail] = useState('');
  const [authPassword, setAuthPassword] = useState('');
  const [authError, setAuthError] = useState('');

  // Sample Clinic Data (matching Android Room database)
  const [pets, setPets] = useState<PetRecord[]>([
    {
      id: 1,
      name: 'Bella',
      species: 'Canine (Dog)',
      breed: 'African Boerboel Mix',
      sex: 'Spayed Female',
      dob: '12 Jan 2021 (3.5 yrs)',
      weightKg: 24.5,
      color: 'Brindle / Golden Tan',
      microchipId: '985141002931882',
      rabiesTag: 'HP-LR-2024-0884',
      photoUrl: 'https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=400&q=80',
      notes: 'Healthy, rescued in Congo Town. Friendly, loves belly rubs. All vaccinations up to date.'
    },
    {
      id: 2,
      name: 'Rex',
      species: 'Canine (Dog)',
      breed: 'German Shepherd Mix',
      sex: 'Neutered Male',
      dob: '05 Aug 2022 (2 yrs)',
      weightKg: 28.0,
      color: 'Black & Tan',
      microchipId: '985141002931990',
      rabiesTag: 'HP-LR-2024-0912',
      photoUrl: 'https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?auto=format&fit=crop&w=400&q=80',
      notes: 'Active guard dog, allergic to chicken byproducts. Regular deworming completed.'
    }
  ]);

  const [vaccinations, setVaccinations] = useState<Vaccination[]>([
    {
      id: 101,
      petName: 'Bella',
      vaccineName: 'Rabies (Defensor 3)',
      dateAdministered: '15 Nov 2023',
      validUntil: '15 Nov 2024',
      batchNumber: 'RB-99482-EXP25',
      vetName: 'Dr. David Kpadeh, DVM',
      status: 'Up to Date'
    },
    {
      id: 102,
      petName: 'Bella',
      vaccineName: 'DHPP Core Combo (Distemper, Hepatitis, Parvo, Parainfluenza)',
      dateAdministered: '10 Feb 2024',
      validUntil: '10 Feb 2025',
      batchNumber: 'DH-4412-V',
      vetName: 'Dr. David Kpadeh, DVM',
      status: 'Up to Date'
    },
    {
      id: 103,
      petName: 'Rex',
      vaccineName: 'Rabies (Defensor 3)',
      dateAdministered: '12 Jan 2024',
      validUntil: '12 Jan 2025',
      batchNumber: 'RB-99510-EXP25',
      vetName: 'Dr. Sarah Wilson, DVM',
      status: 'Up to Date'
    }
  ]);

  const [dewormings, setDewormings] = useState<Deworming[]>([
    {
      id: 201,
      petName: 'Bella',
      productName: 'Drontal Plus (Praziquantel / Pyrantel / Febantel)',
      dateGiven: '15 Jul 2024',
      nextDueDate: '15 Oct 2024',
      weightKg: 24.5,
      dosage: '2.5 tablets oral'
    },
    {
      id: 202,
      petName: 'Rex',
      productName: 'Ivermectin / Pyrantel Chewable',
      dateGiven: '01 Aug 2024',
      nextDueDate: '01 Nov 2024',
      weightKg: 28.0,
      dosage: '1 large chew'
    }
  ]);

  const [appointments, setAppointments] = useState<AppointmentItem[]>([
    {
      id: 301,
      clientName: 'Anthony Tolbert',
      petName: 'Bella',
      species: 'Dog',
      time: 'Today 09:30 AM',
      reason: 'Annual Health Check & Rabies Booster Verification',
      vetName: 'Dr. David Kpadeh',
      status: 'In Consultation'
    },
    {
      id: 302,
      clientName: 'Sarah Freeman',
      petName: 'Simba',
      species: 'Cat',
      time: 'Today 11:00 AM',
      reason: 'Skin allergy & ear checkup',
      vetName: 'Dr. Sarah Wilson',
      status: 'Confirmed'
    },
    {
      id: 303,
      clientName: 'Emmanuel Kollie',
      petName: 'Rocky',
      species: 'Dog',
      time: 'Today 02:15 PM',
      reason: 'Follow-up Wound Dressing',
      vetName: 'Dr. David Kpadeh',
      status: 'Confirmed'
    }
  ]);

  const [inventory, setInventory] = useState<InventoryStock[]>([
    { id: 1, name: 'Rabies Defensor 3 (10-dose vial)', category: 'Vaccine', stockCount: 42, unit: 'vials', minimumThreshold: 15, unitPriceUSD: 18.00 },
    { id: 2, name: 'DHPP Combo Canine Vaccine', category: 'Vaccine', stockCount: 18, unit: 'doses', minimumThreshold: 20, unitPriceUSD: 22.50 },
    { id: 3, name: 'Amoxicillin / Clavulanate 250mg', category: 'Antibiotic', stockCount: 120, unit: 'tablets', minimumThreshold: 40, unitPriceUSD: 1.50 },
    { id: 4, name: 'Drontal Plus Flavored Dewormer', category: 'Parasiticide', stockCount: 65, unit: 'tablets', minimumThreshold: 30, unitPriceUSD: 4.00 },
    { id: 5, name: 'Sterile Surgical Glove Pairs (7.5)', category: 'Surgical', stockCount: 88, unit: 'pairs', minimumThreshold: 25, unitPriceUSD: 2.20 }
  ]);

  const [invoices, setInvoices] = useState<InvoiceItem[]>([
    { id: 'INV-2024-0089', clientName: 'Anthony Tolbert', petName: 'Bella', date: '2024-09-28', amountUSD: 45.00, amountLRD: 8775, status: 'PAID', itemsSummary: 'Routine Consult + Deworming' },
    { id: 'INV-2024-0088', clientName: 'Sarah Freeman', petName: 'Simba', date: '2024-09-27', amountUSD: 30.00, amountLRD: 5850, status: 'PAID', itemsSummary: 'Ear Cytology & Cleanse' }
  ]);

  // Appointment creation form state
  const [newPetName, setNewPetName] = useState('Bella');
  const [newReason, setNewReason] = useState('Routine Checkup');
  const [newDate, setNewDate] = useState('2024-10-05');
  const [newTime, setNewTime] = useState('10:00 AM');

  useEffect(() => {
    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);

    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    if ('serviceWorker' in navigator) {
      navigator.serviceWorker.getRegistration().then((reg) => {
        setSwRegistered(!!reg);
      });
    }

    try {
      const unsub = onAuthStateChanged(auth, (user) => {
        if (user) {
          setFirebaseStatus('connected');
        }
      });
      return () => {
        window.removeEventListener('online', handleOnline);
        window.removeEventListener('offline', handleOffline);
        unsub();
      };
    } catch (e) {
      console.warn('Firebase init status:', e);
    }
  }, []);

  const handleSignIn = async (e: React.FormEvent) => {
    e.preventDefault();
    setAuthError('');
    try {
      if (authEmail && authPassword) {
        await signInWithEmailAndPassword(auth, authEmail, authPassword);
        setShowAuthModal(false);
      }
    } catch (err: any) {
      setAuthError(err.message || 'Login failed. Please verify credentials.');
    }
  };

  const handleBookAppointment = (e: React.FormEvent) => {
    e.preventDefault();
    const newAppt: AppointmentItem = {
      id: Date.now(),
      clientName: 'Anthony Tolbert',
      petName: newPetName,
      species: 'Canine',
      time: `${newDate} at ${newTime}`,
      reason: newReason,
      vetName: 'Dr. David Kpadeh',
      status: 'Requested'
    };
    setAppointments([newAppt, ...appointments]);
    setShowNewAppointmentModal(false);
    setActiveTab('appointments');
  };

  const activePet = pets[selectedPetIndex] || pets[0];

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--color-warm-ivory)' }}>
      
      {/* Top Clinic Header */}
      <header style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid var(--color-border-subtle)',
        padding: '12px 24px',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '12px',
        position: 'sticky',
        top: 0,
        zIndex: 50,
        boxShadow: 'var(--shadow-sm)'
      }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <img 
            src="/Happy-paws-logo-transparent1.png" 
            alt="Happy Paws Liberia" 
            style={{ height: '44px', width: 'auto', objectFit: 'contain' }}
            onError={(e) => {
              (e.target as HTMLElement).style.display = 'none';
            }}
          />
          <div>
            <h1 className="font-serif" style={{ fontSize: '19px', fontWeight: 700, color: '#111827', lineHeight: 1.2 }}>
              Happy Paws Liberia
            </h1>
            <p style={{ fontSize: '12px', color: '#1F2937', fontWeight: 600 }}>
              Veterinary Clinic & Pet Health Passport · Congo Town, Monrovia
            </p>
          </div>
        </div>

        {/* Status Badges & Persona Switcher */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
          
          {/* Online/Offline Badge */}
          {isOnline ? (
            <span className="badge badge-sage">
              <Wifi size={14} /> Cloud Sync Active
            </span>
          ) : (
            <span className="badge badge-amber">
              <WifiOff size={14} /> Offline Mode (IndexedDB)
            </span>
          )}

          {/* Quick Persona Switcher for Evaluation */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px', background: '#F3F4F6', padding: '4px 8px', borderRadius: '12px' }}>
            <span style={{ fontSize: '11px', fontWeight: 700, color: '#111827' }}>Role:</span>
            <select 
              value={currentRole}
              onChange={(e) => {
                const r = e.target.value as UserRole;
                setCurrentRole(r);
                if (r === 'pet_owner') setActiveTab('passport');
                else if (r === 'veterinarian') setActiveTab('appointments');
                else if (r === 'receptionist') setActiveTab('appointments');
                else setActiveTab('inventory');
              }}
              style={{
                fontSize: '12px',
                fontWeight: 600,
                color: '#111827',
                background: 'transparent',
                border: 'none',
                outline: 'none',
                cursor: 'pointer'
              }}
            >
              <option value="pet_owner">🐶 Pet Owner (Anthony Tolbert)</option>
              <option value="veterinarian">🩺 Veterinarian (Dr. David Kpadeh)</option>
              <option value="receptionist">📋 Receptionist (Marie Dennis)</option>
              <option value="super_admin">⚡ Super Admin (Clinic Owner)</option>
            </select>
          </div>

          {/* Staff Quick QR Scanner Button */}
          {currentRole !== 'pet_owner' && (
            <button 
              onClick={() => {
                setManualCodeInput('');
                setAdmitSuccess(false);
                setShowReceptionScannerModal(true);
              }}
              className="btn-primary"
              style={{
                padding: '6px 14px',
                fontSize: '12px',
                minHeight: '34px',
                backgroundColor: 'var(--color-forest-sage)',
                fontWeight: 700
              }}
            >
              <ScanLine size={14} /> Scan Pet QR
            </button>
          )}

          {/* Sign In / Sign Out button */}
          <button 
            onClick={() => setShowAuthModal(true)}
            className="btn-secondary"
            style={{ padding: '6px 12px', fontSize: '12px', minHeight: '34px', color: '#111827', fontWeight: 700 }}
          >
            <User size={14} /> Account
          </button>
        </div>
      </header>

      {/* Navigation Tabs Bar */}
      <nav style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid var(--color-border-subtle)',
        padding: '0 24px',
        display: 'flex',
        alignItems: 'center',
        gap: '8px',
        overflowX: 'auto',
        whiteSpace: 'nowrap'
      }}>
        {currentRole === 'pet_owner' ? (
          <>
            <button 
              onClick={() => setActiveTab('passport')}
              style={{
                padding: '12px 16px',
                fontWeight: activeTab === 'passport' ? 700 : 600,
                fontSize: '13px',
                color: activeTab === 'passport' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: activeTab === 'passport' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
                background: 'transparent',
                borderTop: 'none',
                borderLeft: 'none',
                borderRight: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <HeartHandshake size={16} /> Official Health Passport
            </button>
            <button 
              onClick={() => setActiveTab('vaccines')}
              style={{
                padding: '12px 16px',
                fontWeight: activeTab === 'vaccines' ? 700 : 600,
                fontSize: '13px',
                color: activeTab === 'vaccines' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: activeTab === 'vaccines' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
                background: 'transparent',
                borderTop: 'none',
                borderLeft: 'none',
                borderRight: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Syringe size={16} /> Vaccines & Deworming
            </button>
            <button 
              onClick={() => setActiveTab('appointments')}
              style={{
                padding: '12px 16px',
                fontWeight: activeTab === 'appointments' ? 700 : 600,
                fontSize: '13px',
                color: activeTab === 'appointments' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: activeTab === 'appointments' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
                background: 'transparent',
                borderTop: 'none',
                borderLeft: 'none',
                borderRight: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Calendar size={16} /> Clinic Appointments
            </button>
          </>
        ) : (
          <>
            <button 
              onClick={() => setActiveTab('appointments')}
              style={{
                padding: '12px 16px',
                fontWeight: activeTab === 'appointments' ? 700 : 600,
                fontSize: '13px',
                color: activeTab === 'appointments' ? 'var(--color-forest-sage)' : '#111827',
                borderBottom: activeTab === 'appointments' ? '3px solid var(--color-forest-sage)' : '3px solid transparent',
                background: 'transparent',
                borderTop: 'none',
                borderLeft: 'none',
                borderRight: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Calendar size={16} /> Daily Triage & Patients
            </button>
            <button 
              onClick={() => setActiveTab('consultations')}
              style={{
                padding: '12px 16px',
                fontWeight: activeTab === 'consultations' ? 700 : 600,
                fontSize: '13px',
                color: activeTab === 'consultations' ? 'var(--color-forest-sage)' : '#111827',
                borderBottom: activeTab === 'consultations' ? '3px solid var(--color-forest-sage)' : '3px solid transparent',
                background: 'transparent',
                borderTop: 'none',
                borderLeft: 'none',
                borderRight: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Stethoscope size={16} /> Clinical Records
            </button>
            <button 
              onClick={() => setActiveTab('inventory')}
              style={{
                padding: '12px 16px',
                fontWeight: activeTab === 'inventory' ? 700 : 600,
                fontSize: '13px',
                color: activeTab === 'inventory' ? 'var(--color-forest-sage)' : '#111827',
                borderBottom: activeTab === 'inventory' ? '3px solid var(--color-forest-sage)' : '3px solid transparent',
                background: 'transparent',
                borderTop: 'none',
                borderLeft: 'none',
                borderRight: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Package size={16} /> Pharmacy & Stock
            </button>
            <button 
              onClick={() => setActiveTab('billing')}
              style={{
                padding: '12px 16px',
                fontWeight: activeTab === 'billing' ? 700 : 600,
                fontSize: '13px',
                color: activeTab === 'billing' ? 'var(--color-forest-sage)' : '#111827',
                borderBottom: activeTab === 'billing' ? '3px solid var(--color-forest-sage)' : '3px solid transparent',
                background: 'transparent',
                borderTop: 'none',
                borderLeft: 'none',
                borderRight: 'none',
                cursor: 'pointer',
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <Receipt size={16} /> Invoices & Billing
            </button>
          </>
        )}
      </nav>

      {/* Main Container */}
      <main style={{ flex: 1, maxWidth: '1020px', width: '100%', margin: '0 auto', padding: '24px 20px' }}>
        
        {/* PET PARENT PORTAL - PASSPORT VIEW */}
        {currentRole === 'pet_owner' && activeTab === 'passport' && (
          <div>
            {/* Pet Switcher Bar */}
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  {activePet.name}'s Health Passport
                </h2>
                <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                  Owner: Anthony Tolbert · Happy Paws Liberia Rescue Center ID #0884
                </p>
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                {pets.map((p, idx) => (
                  <button
                    key={p.id}
                    onClick={() => setSelectedPetIndex(idx)}
                    style={{
                      padding: '8px 16px',
                      borderRadius: '20px',
                      border: idx === selectedPetIndex ? '2px solid var(--color-amber-terracotta)' : '1px solid var(--color-border-medium)',
                      backgroundColor: idx === selectedPetIndex ? 'var(--color-terracotta-light)' : '#FFFFFF',
                      color: '#111827',
                      fontWeight: 700,
                      fontSize: '13px',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '8px'
                    }}
                  >
                    <img 
                      src={p.photoUrl} 
                      alt={p.name} 
                      style={{ width: '24px', height: '24px', borderRadius: '50%', objectFit: 'cover' }} 
                    />
                    {p.name}
                  </button>
                ))}

                <button 
                  onClick={() => setShowPetQrModal(true)}
                  className="btn-primary"
                  style={{ padding: '8px 16px', fontSize: '13px', minHeight: '38px', fontWeight: 700, backgroundColor: 'var(--color-amber-terracotta)' }}
                >
                  <QrCode size={15} /> Pet QR Code
                </button>

                <button 
                  onClick={() => setShowPassportModal(true)}
                  className="btn-secondary"
                  style={{ padding: '8px 16px', fontSize: '13px', minHeight: '38px', fontWeight: 700, color: '#111827' }}
                >
                  <Printer size={15} /> Print Passport
                </button>
              </div>
            </div>

            {/* Passport Identity Card */}
            <div className="card-surface" style={{ padding: '24px', marginBottom: '24px', borderLeft: '6px solid var(--color-amber-terracotta)' }}>
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '24px', alignItems: 'center' }}>
                <div style={{ display: 'flex', gap: '20px', alignItems: 'center' }}>
                  <img 
                    src={activePet.photoUrl} 
                    alt={activePet.name} 
                    style={{ width: '110px', height: '110px', borderRadius: '20px', objectFit: 'cover', border: '3px solid #FFFFFF', boxShadow: 'var(--shadow-md)' }} 
                  />
                  <div>
                    <span className="badge badge-sage" style={{ marginBottom: '6px' }}>
                      <CheckCircle2 size={13} /> Verified Rabies Vaccinated
                    </span>
                    <h3 className="font-serif" style={{ fontSize: '26px', fontWeight: 700, color: '#111827', margin: '4px 0' }}>
                      {activePet.name}
                    </h3>
                    <p style={{ fontSize: '14px', color: '#1F2937', fontWeight: 600 }}>
                      {activePet.species} · {activePet.breed}
                    </p>
                    <p style={{ fontSize: '13px', color: '#111827', fontWeight: 700, marginTop: '4px' }}>
                      Rabies Tag: <span style={{ color: 'var(--color-amber-terracotta)' }}>{activePet.rabiesTag}</span>
                    </p>
                  </div>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px', background: 'var(--color-card-warm)', padding: '16px', borderRadius: '14px' }}>
                  <div>
                    <span style={{ fontSize: '11px', textTransform: 'uppercase', color: '#1F2937', fontWeight: 700 }}>Sex</span>
                    <p style={{ fontSize: '14px', fontWeight: 700, color: '#111827' }}>{activePet.sex}</p>
                  </div>
                  <div>
                    <span style={{ fontSize: '11px', textTransform: 'uppercase', color: '#1F2937', fontWeight: 700 }}>Weight</span>
                    <p style={{ fontSize: '14px', fontWeight: 700, color: '#111827' }}>{activePet.weightKg} kg</p>
                  </div>
                  <div>
                    <span style={{ fontSize: '11px', textTransform: 'uppercase', color: '#1F2937', fontWeight: 700 }}>Microchip #</span>
                    <p style={{ fontSize: '13px', fontWeight: 700, color: '#111827', wordBreak: 'break-all' }}>{activePet.microchipId}</p>
                  </div>
                  <div>
                    <span style={{ fontSize: '11px', textTransform: 'uppercase', color: '#1F2937', fontWeight: 700 }}>Date of Birth</span>
                    <p style={{ fontSize: '13px', fontWeight: 700, color: '#111827' }}>{activePet.dob}</p>
                  </div>
                </div>
              </div>
            </div>

            {/* Quick Action Banner */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px', marginBottom: '24px' }}>
              <div className="card-surface" style={{ padding: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h4 style={{ fontSize: '16px', fontWeight: 700, color: '#111827' }}>Need a Vet Appointment?</h4>
                  <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600, marginTop: '2px' }}>
                    Consultations, wellness checkups & booster vaccines
                  </p>
                </div>
                <button 
                  onClick={() => setShowNewAppointmentModal(true)}
                  className="btn-primary"
                  style={{ padding: '8px 16px', fontSize: '13px', minHeight: '38px', fontWeight: 700 }}
                >
                  <Plus size={16} /> Book Visit
                </button>
              </div>

              <div className="card-surface" style={{ padding: '20px', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <div>
                  <h4 style={{ fontSize: '16px', fontWeight: 700, color: '#111827' }}>Monrovia Emergency Line</h4>
                  <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600, marginTop: '2px' }}>
                    Congo Town Tubman Blvd Clinic Dispatch
                  </p>
                </div>
                <a 
                  href="tel:+231881479329" 
                  className="btn-secondary"
                  style={{ padding: '8px 16px', fontSize: '13px', minHeight: '38px', textDecoration: 'none', color: '#111827', fontWeight: 700 }}
                >
                  <Phone size={15} /> 088 147 9329
                </a>
              </div>
            </div>

            {/* Recent Medical Timeline */}
            <div className="card-surface" style={{ padding: '24px' }}>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827', marginBottom: '16px' }}>
                Recent Immunization & Deworming Record
              </h3>
              
              <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
                {vaccinations.filter(v => v.petName === activePet.name).map((v) => (
                  <div 
                    key={v.id} 
                    style={{ 
                      padding: '14px', 
                      borderRadius: '12px', 
                      backgroundColor: 'var(--color-card-warm)', 
                      display: 'flex', 
                      alignItems: 'center', 
                      justifyContent: 'space-between',
                      flexWrap: 'wrap',
                      gap: '10px'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                      <div style={{ width: '38px', height: '38px', borderRadius: '10px', backgroundColor: 'var(--color-sage-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                        <Syringe size={20} color="var(--color-forest-sage)" />
                      </div>
                      <div>
                        <h4 style={{ fontSize: '14px', fontWeight: 700, color: '#111827' }}>{v.vaccineName}</h4>
                        <p style={{ fontSize: '12px', color: '#1F2937', fontWeight: 600 }}>
                          Administered: {v.dateAdministered} by {v.vetName}
                        </p>
                      </div>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <span className="badge badge-sage">Valid until {v.validUntil}</span>
                      <p style={{ fontSize: '11px', color: '#111827', fontWeight: 700, marginTop: '4px' }}>Batch: {v.batchNumber}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* PET PARENT PORTAL - VACCINES TAB */}
        {currentRole === 'pet_owner' && activeTab === 'vaccines' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Vaccination & Preventive Timeline
                </h2>
                <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                  Showing clinical records for {activePet.name}
                </p>
              </div>
            </div>

            {/* Vaccines Table Card */}
            <div className="card-surface" style={{ padding: '24px', marginBottom: '24px' }}>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827', marginBottom: '16px' }}>
                Vaccinations
              </h3>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--color-border-subtle)' }}>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Vaccine</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Date Given</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Valid Through</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Status</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Veterinarian</th>
                    </tr>
                  </thead>
                  <tbody>
                    {vaccinations.map(v => (
                      <tr key={v.id} style={{ borderBottom: '1px solid var(--color-border-subtle)' }}>
                        <td style={{ padding: '12px', fontWeight: 700, fontSize: '13px', color: '#111827' }}>{v.vaccineName}</td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>{v.dateAdministered}</td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 700 }}>{v.validUntil}</td>
                        <td style={{ padding: '12px' }}>
                          <span className="badge badge-sage">{v.status}</span>
                        </td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>{v.vetName}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Deworming Table Card */}
            <div className="card-surface" style={{ padding: '24px' }}>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827', marginBottom: '16px' }}>
                Deworming & Parasite Prevention
              </h3>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--color-border-subtle)' }}>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Product</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Administered Date</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Dosage & Weight</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Next Due</th>
                    </tr>
                  </thead>
                  <tbody>
                    {dewormings.map(d => (
                      <tr key={d.id} style={{ borderBottom: '1px solid var(--color-border-subtle)' }}>
                        <td style={{ padding: '12px', fontWeight: 700, fontSize: '13px', color: '#111827' }}>{d.productName}</td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>{d.dateGiven}</td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>{d.dosage} ({d.weightKg} kg)</td>
                        <td style={{ padding: '12px', fontSize: '13px', fontWeight: 700, color: 'var(--color-amber-terracotta)' }}>{d.nextDueDate}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* APPOINTMENTS TAB (Shared / Staff Triage) */}
        {activeTab === 'appointments' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  {currentRole === 'pet_owner' ? 'Your Appointments' : 'Daily Clinic Queue & Triage'}
                </h2>
                <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                  Happy Paws Veterinary Clinic · Congo Town Back Road, Monrovia
                </p>
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                {currentRole !== 'pet_owner' && (
                  <button 
                    onClick={() => {
                      setManualCodeInput('');
                      setAdmitSuccess(false);
                      setShowReceptionScannerModal(true);
                    }}
                    className="btn-primary"
                    style={{ padding: '8px 16px', fontSize: '13px', minHeight: '38px', fontWeight: 700, backgroundColor: 'var(--color-forest-sage)' }}
                  >
                    <ScanLine size={16} /> Scan Pet QR
                  </button>
                )}

                <button 
                  onClick={() => setShowNewAppointmentModal(true)}
                  className="btn-secondary"
                  style={{ padding: '8px 16px', fontSize: '13px', minHeight: '38px', fontWeight: 700, color: '#111827' }}
                >
                  <Plus size={16} /> Schedule Visit
                </button>
              </div>
            </div>

            <div className="card-surface" style={{ padding: '24px' }}>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                {appointments.map(appt => (
                  <div 
                    key={appt.id}
                    style={{
                      padding: '16px',
                      borderRadius: '14px',
                      backgroundColor: 'var(--color-card-warm)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      flexWrap: 'wrap',
                      gap: '12px',
                      borderLeft: appt.status === 'In Consultation' ? '4px solid var(--color-forest-sage)' : '4px solid var(--color-amber-terracotta)'
                    }}
                  >
                    <div>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '4px' }}>
                        <span style={{ fontSize: '15px', fontWeight: 700, color: '#111827' }}>
                          {appt.petName} ({appt.species})
                        </span>
                        <span style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                          · Parent: <strong>{appt.clientName}</strong>
                        </span>
                        <span className={`badge ${appt.status === 'In Consultation' ? 'badge-sage' : 'badge-amber'}`}>
                          {appt.status}
                        </span>
                      </div>
                      <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600 }}>
                        {appt.reason}
                      </p>
                      <p style={{ fontSize: '12px', color: '#1F2937', fontWeight: 600, marginTop: '4px' }}>
                        <Clock size={12} style={{ display: 'inline', marginRight: '4px' }} />
                        {appt.time} · Assigned: {appt.vetName}
                      </p>
                    </div>

                    {currentRole !== 'pet_owner' && (
                      <div style={{ display: 'flex', gap: '8px' }}>
                        {appt.status !== 'Completed' && (
                          <button 
                            onClick={() => {
                              setAppointments(appointments.map(a => a.id === appt.id ? { ...a, status: 'Completed' } : a));
                            }}
                            className="btn-secondary"
                            style={{ padding: '6px 12px', fontSize: '12px', minHeight: '34px', color: '#111827', fontWeight: 700 }}
                          >
                            <Check size={14} /> Mark Done
                          </button>
                        )}
                        <button 
                          onClick={() => setActiveTab('consultations')}
                          className="btn-primary"
                          style={{ padding: '6px 12px', fontSize: '12px', minHeight: '34px', fontWeight: 700 }}
                        >
                          <Stethoscope size={14} /> Open SOAP Note
                        </button>
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* CLINICAL CONSULTATIONS TAB (Staff Only) */}
        {currentRole !== 'pet_owner' && activeTab === 'consultations' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Clinical Consultations & SOAP Notes
                </h2>
                <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                  Records become legally immutable upon finalization as per veterinary regulatory guidelines.
                </p>
              </div>
            </div>

            {/* Active Consultation Record */}
            <div className="card-surface" style={{ padding: '24px', marginBottom: '24px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
                <div>
                  <span className="badge badge-sage">Finalized Clinical Note</span>
                  <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginTop: '6px' }}>
                    Bella (Anthony Tolbert) · Routine Examination & Rabies Titer Check
                  </h3>
                  <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                    Attending Vet: Dr. David Kpadeh, DVM · Date: 2024-09-28
                  </p>
                </div>
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px', background: 'var(--color-card-warm)', padding: '16px', borderRadius: '14px' }}>
                <div>
                  <h4 style={{ fontSize: '13px', textTransform: 'uppercase', color: '#111827', fontWeight: 700 }}>Subjective (History)</h4>
                  <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600, marginTop: '4px' }}>
                    Owner reports energetic behavior, appetite normal. Presented for routine follow-up after rescue rehabilitation.
                  </p>
                </div>
                <div>
                  <h4 style={{ fontSize: '13px', textTransform: 'uppercase', color: '#111827', fontWeight: 700 }}>Objective (Findings)</h4>
                  <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600, marginTop: '4px' }}>
                    Temp: 38.6°C, Heart Rate: 98 bpm, Weight: 24.5 kg. Mucous membranes pink, CRT &lt; 2s. Skin clear, coat glossy.
                  </p>
                </div>
                <div>
                  <h4 style={{ fontSize: '13px', textTransform: 'uppercase', color: '#111827', fontWeight: 700 }}>Assessment (Diagnosis)</h4>
                  <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600, marginTop: '4px' }}>
                    Healthy adult canine in prime body condition score (BCS 5/9). No active clinical pathology.
                  </p>
                </div>
                <div>
                  <h4 style={{ fontSize: '13px', textTransform: 'uppercase', color: '#111827', fontWeight: 700 }}>Plan & Prescriptions</h4>
                  <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600, marginTop: '4px' }}>
                    Administered Drontal Plus dewormer (2.5 tabs). Re-check in 6 months or if dietary changes occur.
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* PHARMACY & INVENTORY TAB (Staff Only) */}
        {currentRole !== 'pet_owner' && activeTab === 'inventory' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Pharmacy, Vaccines & Medical Stock
                </h2>
                <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                  Live inventory synchronized across Happy Paws Android app and web portal.
                </p>
              </div>
            </div>

            <div className="card-surface" style={{ padding: '24px' }}>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--color-border-subtle)' }}>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Item Name</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Category</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>In Stock</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Unit Price (USD)</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Status</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Action</th>
                    </tr>
                  </thead>
                  <tbody>
                    {inventory.map(item => (
                      <tr key={item.id} style={{ borderBottom: '1px solid var(--color-border-subtle)' }}>
                        <td style={{ padding: '12px', fontWeight: 700, fontSize: '13px', color: '#111827' }}>{item.name}</td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>{item.category}</td>
                        <td style={{ padding: '12px', fontSize: '14px', fontWeight: 700, color: '#111827' }}>
                          {item.stockCount} {item.unit}
                        </td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 700 }}>
                          ${item.unitPriceUSD.toFixed(2)}
                        </td>
                        <td style={{ padding: '12px' }}>
                          {item.stockCount <= item.minimumThreshold ? (
                            <span className="badge badge-amber">Low Stock</span>
                          ) : (
                            <span className="badge badge-sage">Sufficient</span>
                          )}
                        </td>
                        <td style={{ padding: '12px' }}>
                          <button
                            onClick={() => {
                              setInventory(inventory.map(i => i.id === item.id ? { ...i, stockCount: i.stockCount + 10 } : i));
                            }}
                            className="btn-secondary"
                            style={{ padding: '4px 10px', fontSize: '11px', minHeight: '28px', color: '#111827', fontWeight: 700 }}
                          >
                            +10 Stock
                          </button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* INVOICES & BILLING TAB (Staff Only) */}
        {currentRole !== 'pet_owner' && activeTab === 'billing' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Billing & Invoicing
                </h2>
                <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                  Supports dual-currency Liberia Dollars (LRD) and US Dollars (USD).
                </p>
              </div>
            </div>

            <div className="card-surface" style={{ padding: '24px' }}>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--color-border-subtle)' }}>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Invoice #</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Client & Pet</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Date</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Services</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Amount (USD / LRD)</th>
                      <th style={{ padding: '10px 12px', fontSize: '12px', fontWeight: 700, color: '#111827' }}>Status</th>
                    </tr>
                  </thead>
                  <tbody>
                    {invoices.map(inv => (
                      <tr key={inv.id} style={{ borderBottom: '1px solid var(--color-border-subtle)' }}>
                        <td style={{ padding: '12px', fontWeight: 700, fontSize: '13px', color: '#111827' }}>{inv.id}</td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>
                          <strong>{inv.clientName}</strong> ({inv.petName})
                        </td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>{inv.date}</td>
                        <td style={{ padding: '12px', fontSize: '13px', color: '#111827', fontWeight: 600 }}>{inv.itemsSummary}</td>
                        <td style={{ padding: '12px', fontSize: '13px', fontWeight: 700, color: '#111827' }}>
                          ${inv.amountUSD.toFixed(2)} / LRD {inv.amountLRD.toLocaleString()}
                        </td>
                        <td style={{ padding: '12px' }}>
                          <span className="badge badge-sage">{inv.status}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

      </main>

      {/* PRINTABLE / EXPORTABLE PASSPORT MODAL */}
      {showPassportModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.65)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '20px',
            maxWidth: '600px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)',
            border: '2px solid var(--color-amber-terracotta)'
          }}>
            <div style={{ textAlign: 'center', borderBottom: '2px solid var(--color-border-subtle)', paddingBottom: '16px', marginBottom: '20px' }}>
              <h2 className="font-serif" style={{ fontSize: '22px', fontWeight: 700, color: '#111827' }}>
                OFFICIAL PET HEALTH PASSPORT
              </h2>
              <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 700 }}>
                Happy Paws Liberia Rescue Center · Congo Town, Monrovia
              </p>
              <p style={{ fontSize: '12px', color: '#111827', fontWeight: 700, marginTop: '2px' }}>
                Document #HP-MED-{activePet.id}-2024
              </p>
            </div>

            <div style={{ display: 'flex', gap: '16px', alignItems: 'center', marginBottom: '20px' }}>
              <img 
                src={activePet.photoUrl} 
                alt={activePet.name} 
                style={{ width: '90px', height: '90px', borderRadius: '16px', objectFit: 'cover' }} 
              />
              <div>
                <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>{activePet.name}</h3>
                <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600 }}>Breed: <strong>{activePet.breed}</strong></p>
                <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600 }}>Sex: {activePet.sex} · Weight: {activePet.weightKg} kg</p>
                <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600 }}>Microchip: <strong>{activePet.microchipId}</strong></p>
                <p style={{ fontSize: '13px', color: 'var(--color-amber-terracotta)', fontWeight: 700 }}>Rabies Tag: {activePet.rabiesTag}</p>
              </div>
            </div>

            <div style={{ background: 'var(--color-card-warm)', padding: '14px', borderRadius: '12px', marginBottom: '20px' }}>
              <h4 style={{ fontSize: '13px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>
                Verified Rabies & Core Vaccination Certificate
              </h4>
              <p style={{ fontSize: '12px', color: '#111827', fontWeight: 600, lineHeight: 1.6 }}>
                This certifies that {activePet.name} has been examined and vaccinated against Rabies Virus and Canine Core Distemper/Parvo at Happy Paws Liberia Veterinary Clinic. Valid for cross-county movement and domestic pet registry.
              </p>
            </div>

            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button 
                onClick={() => setShowPassportModal(false)}
                className="btn-secondary"
                style={{ padding: '8px 16px', color: '#111827', fontWeight: 700 }}
              >
                Close
              </button>
              <button 
                onClick={() => window.print()}
                className="btn-primary"
                style={{ padding: '8px 18px', fontWeight: 700 }}
              >
                <Printer size={16} /> Print Official Passport
              </button>
            </div>
          </div>
        </div>
      )}

      {/* NEW APPOINTMENT MODAL */}
      {showNewAppointmentModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.65)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '20px',
            maxWidth: '480px',
            width: '100%',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>
              Book Veterinary Appointment
            </h3>
            <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600, marginBottom: '20px' }}>
              Schedule a consultation at Happy Paws Clinic, Congo Town, Monrovia.
            </p>

            <form onSubmit={handleBookAppointment} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Pet</label>
                <select 
                  value={newPetName}
                  onChange={(e) => setNewPetName(e.target.value)}
                  className="form-input"
                  style={{ color: '#111827', fontWeight: 600 }}
                >
                  {pets.map(p => <option key={p.id} value={p.name}>{p.name} ({p.species})</option>)}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Reason for Visit</label>
                <input 
                  type="text" 
                  value={newReason}
                  onChange={(e) => setNewReason(e.target.value)}
                  className="form-input"
                  placeholder="e.g. Annual Rabies Booster, Deworming, Skin Allergy"
                  style={{ color: '#111827', fontWeight: 600 }}
                  required
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Preferred Date</label>
                  <input 
                    type="date" 
                    value={newDate}
                    onChange={(e) => setNewDate(e.target.value)}
                    className="form-input"
                    style={{ color: '#111827', fontWeight: 600 }}
                    required
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Time Slot</label>
                  <select 
                    value={newTime}
                    onChange={(e) => setNewTime(e.target.value)}
                    className="form-input"
                    style={{ color: '#111827', fontWeight: 600 }}
                  >
                    <option value="09:00 AM">09:00 AM</option>
                    <option value="10:30 AM">10:30 AM</option>
                    <option value="02:00 PM">02:00 PM</option>
                    <option value="03:30 PM">03:30 PM</option>
                  </select>
                </div>
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '10px' }}>
                <button 
                  type="button" 
                  onClick={() => setShowNewAppointmentModal(false)}
                  className="btn-secondary"
                  style={{ padding: '8px 16px', color: '#111827', fontWeight: 700 }}
                >
                  Cancel
                </button>
                <button 
                  type="submit" 
                  className="btn-primary"
                  style={{ padding: '8px 18px', fontWeight: 700 }}
                >
                  Confirm Request
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* AUTHENTICATION MODAL */}
      {showAuthModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.65)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '20px',
            maxWidth: '440px',
            width: '100%',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
              Firebase Account Sign In
            </h3>
            <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600, marginBottom: '20px' }}>
              Sign in with your Happy Paws staff or pet owner credentials.
            </p>

            {authError && (
              <div style={{ padding: '10px', borderRadius: '10px', backgroundColor: 'var(--color-red-light)', color: 'var(--color-status-red)', fontSize: '12px', fontWeight: 700, marginBottom: '14px' }}>
                {authError}
              </div>
            )}

            <form onSubmit={handleSignIn} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Email</label>
                <input 
                  type="email" 
                  value={authEmail}
                  onChange={(e) => setAuthEmail(e.target.value)}
                  className="form-input"
                  placeholder="e.g. anthony@happypaws.lr or vet@happypaws.lr"
                  style={{ color: '#111827', fontWeight: 600 }}
                  required
                />
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Password</label>
                <input 
                  type="password" 
                  value={authPassword}
                  onChange={(e) => setAuthPassword(e.target.value)}
                  className="form-input"
                  placeholder="••••••••"
                  style={{ color: '#111827', fontWeight: 600 }}
                  required
                />
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end', marginTop: '10px' }}>
                <button 
                  type="button" 
                  onClick={() => setShowAuthModal(false)}
                  className="btn-secondary"
                  style={{ padding: '8px 16px', color: '#111827', fontWeight: 700 }}
                >
                  Close
                </button>
                <button 
                  type="submit" 
                  className="btn-primary"
                  style={{ padding: '8px 18px', fontWeight: 700 }}
                >
                  <LogIn size={15} /> Sign In
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* PET QR CODE MODAL (Pet Owner View) */}
      {showPetQrModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.65)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '24px',
            maxWidth: '460px',
            width: '100%',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)',
            border: '2px solid var(--color-amber-terracotta)',
            textAlign: 'center'
          }}>
            <h3 className="font-serif" style={{ fontSize: '22px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
              {activePet.name}'s Digital QR Passport
            </h3>
            <p style={{ fontSize: '12px', color: 'var(--color-amber-terracotta)', fontWeight: 700, marginBottom: '16px' }}>
              Happy Paws Liberia Rescue Center · Verified Pet ID
            </p>

            <div style={{ display: 'flex', alignItems: 'center', gap: '12px', background: 'var(--color-card-warm)', padding: '12px 16px', borderRadius: '14px', marginBottom: '16px', textAlign: 'left' }}>
              <img 
                src={activePet.photoUrl} 
                alt={activePet.name} 
                style={{ width: '48px', height: '48px', borderRadius: '12px', objectFit: 'cover' }} 
              />
              <div>
                <h4 style={{ fontSize: '15px', fontWeight: 700, color: '#111827' }}>{activePet.name}</h4>
                <p style={{ fontSize: '12px', color: '#1F2937', fontWeight: 600 }}>{activePet.species} · {activePet.breed}</p>
                <p style={{ fontSize: '11px', color: '#111827', fontWeight: 700 }}>Rabies Tag: {activePet.rabiesTag}</p>
              </div>
            </div>

            {/* QR Code SVG */}
            <div style={{
              display: 'inline-block',
              padding: '14px',
              backgroundColor: '#FFFFFF',
              borderRadius: '16px',
              border: '1px solid var(--color-border-subtle)',
              boxShadow: 'var(--shadow-sm)',
              marginBottom: '16px'
            }}>
              {(() => {
                const payload = `HAPPYPAWS:PET:${activePet.id}:${activePet.name}:${activePet.rabiesTag}:${activePet.microchipId}`;
                const matrix = generateQrMatrix(payload);
                const cellSize = 7;
                const matrixDim = matrix.length;
                return (
                  <svg 
                    width={matrixDim * cellSize} 
                    height={matrixDim * cellSize} 
                    viewBox={`0 0 ${matrixDim * cellSize} ${matrixDim * cellSize}`}
                    style={{ display: 'block' }}
                  >
                    <rect width="100%" height="100%" fill="#FFFFFF" />
                    {matrix.map((row, y) =>
                      row.map((cell, x) =>
                        cell ? (
                          <rect
                            key={`${x}-${y}`}
                            x={x * cellSize}
                            y={y * cellSize}
                            width={cellSize}
                            height={cellSize}
                            fill="#111827"
                          />
                        ) : null
                      )
                    )}
                  </svg>
                );
              })()}
            </div>

            <div style={{ display: 'flex', justifyContent: 'center', gap: '8px', marginBottom: '12px' }}>
              <span className="badge badge-sage">
                <CheckCircle2 size={12} /> Rabies Certified
              </span>
              <span className="badge badge-terracotta">
                Chip #{activePet.microchipId.slice(-6)}
              </span>
            </div>

            <p style={{ fontSize: '12px', color: '#1F2937', fontWeight: 600, lineHeight: 1.5, marginBottom: '20px' }}>
              Present this unique QR code at clinic reception in Congo Town, Monrovia for rapid triage admission and complete medical history lookup.
            </p>

            <button 
              onClick={() => setShowPetQrModal(false)}
              className="btn-primary"
              style={{ width: '100%', padding: '10px', fontWeight: 700 }}
            >
              Done
            </button>
          </div>
        </div>
      )}

      {/* RECEPTIONIST SCANNER MODAL */}
      {showReceptionScannerModal && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.65)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '24px',
            maxWidth: '520px',
            width: '100%',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)',
            border: '2px solid var(--color-forest-sage)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div style={{ width: '40px', height: '40px', borderRadius: '12px', backgroundColor: 'var(--color-sage-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <ScanLine size={22} color="var(--color-forest-sage)" />
                </div>
                <div>
                  <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>
                    Scan Pet QR Code
                  </h3>
                  <p style={{ fontSize: '12px', color: 'var(--color-forest-sage)', fontWeight: 700 }}>
                    Receptionist Check-In & Medical Record Retrieval
                  </p>
                </div>
              </div>
              <button 
                onClick={() => setShowReceptionScannerModal(false)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#111827', fontSize: '18px', fontWeight: 700 }}
              >
                ✕
              </button>
            </div>

            <p style={{ fontSize: '13px', color: '#111827', fontWeight: 700, marginBottom: '10px' }}>
              One-Tap Demo Scanner (Select Registered Patient):
            </p>

            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px', marginBottom: '20px' }}>
              {pets.map(p => (
                <div 
                  key={p.id}
                  onClick={() => {
                    setScannedPetDossier(p);
                    setShowReceptionScannerModal(false);
                    setShowDossierModal(true);
                  }}
                  style={{
                    padding: '12px 16px',
                    borderRadius: '14px',
                    backgroundColor: 'var(--color-card-warm)',
                    border: '1px solid var(--color-border-subtle)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease'
                  }}
                  onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = 'var(--color-sage-light)')}
                  onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = 'var(--color-card-warm)')}
                >
                  <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                    <img 
                      src={p.photoUrl} 
                      alt={p.name} 
                      style={{ width: '40px', height: '40px', borderRadius: '10px', objectFit: 'cover' }} 
                    />
                    <div>
                      <h4 style={{ fontSize: '15px', fontWeight: 700, color: '#111827' }}>{p.name}</h4>
                      <p style={{ fontSize: '12px', color: '#1F2937', fontWeight: 600 }}>{p.species} · Owner: Anthony Tolbert</p>
                    </div>
                  </div>
                  <span className="badge badge-sage">
                    <ScanLine size={12} /> Scan Now
                  </span>
                </div>
              ))}
            </div>

            <div style={{ borderTop: '1px solid var(--color-border-subtle)', paddingTop: '16px' }}>
              <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                Or Input Scanned Payload / Tag:
              </label>
              <div style={{ display: 'flex', gap: '8px' }}>
                <input 
                  type="text" 
                  value={manualCodeInput}
                  onChange={(e) => setManualCodeInput(e.target.value)}
                  placeholder="e.g. HAPPYPAWS:PET:1 or Bella"
                  className="form-input"
                  style={{ color: '#111827', fontWeight: 600, flex: 1 }}
                />
                <button 
                  onClick={() => {
                    const matched = pets.find(p => 
                      manualCodeInput.includes(p.name) || 
                      manualCodeInput.includes(p.id.toString()) || 
                      manualCodeInput.includes(p.rabiesTag)
                    ) || pets[0];
                    setScannedPetDossier(matched);
                    setShowReceptionScannerModal(false);
                    setShowDossierModal(true);
                  }}
                  className="btn-primary"
                  style={{ padding: '0 16px', fontWeight: 700, backgroundColor: 'var(--color-forest-sage)' }}
                >
                  Lookup
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* PET MEDICAL DOSSIER MODAL (Scanned Result) */}
      {showDossierModal && scannedPetDossier && (
        <div style={{
          position: 'fixed',
          top: 0,
          left: 0,
          right: 0,
          bottom: 0,
          backgroundColor: 'rgba(0, 0, 0, 0.65)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '24px',
            maxWidth: '650px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)',
            border: '2px solid var(--color-forest-sage)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{ width: '44px', height: '44px', borderRadius: '12px', backgroundColor: 'var(--color-sage-light)', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
                  <Stethoscope size={24} color="var(--color-forest-sage)" />
                </div>
                <div>
                  <h3 className="font-serif" style={{ fontSize: '22px', fontWeight: 700, color: '#111827' }}>
                    Medical Dossier: {scannedPetDossier.name}
                  </h3>
                  <p style={{ fontSize: '12px', color: 'var(--color-forest-sage)', fontWeight: 700 }}>
                    Scanned & Verified · Happy Paws Liberia Rescue Center
                  </p>
                </div>
              </div>
              <button 
                onClick={() => setShowDossierModal(false)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#111827', fontSize: '18px', fontWeight: 700 }}
              >
                ✕
              </button>
            </div>

            {admitSuccess && (
              <div style={{ padding: '12px', borderRadius: '12px', backgroundColor: 'var(--color-sage-light)', color: 'var(--color-forest-sage)', fontSize: '13px', fontWeight: 700, marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <CheckCircle2 size={16} />
                {scannedPetDossier.name} has been admitted directly to Today's Clinical Floor & Queue!
              </div>
            )}

            {/* Profile Overview Card */}
            <div style={{ background: 'var(--color-card-warm)', padding: '16px', borderRadius: '16px', display: 'flex', gap: '16px', alignItems: 'center', marginBottom: '20px' }}>
              <img 
                src={scannedPetDossier.photoUrl} 
                alt={scannedPetDossier.name} 
                style={{ width: '84px', height: '84px', borderRadius: '16px', objectFit: 'cover' }} 
              />
              <div style={{ flex: 1 }}>
                <h4 style={{ fontSize: '18px', fontWeight: 700, color: '#111827' }}>{scannedPetDossier.name}</h4>
                <p style={{ fontSize: '13px', color: '#1F2937', fontWeight: 600 }}>
                  {scannedPetDossier.species} · {scannedPetDossier.breed} · {scannedPetDossier.sex} ({scannedPetDossier.weightKg} kg)
                </p>
                <p style={{ fontSize: '12px', color: '#111827', fontWeight: 700, marginTop: '2px' }}>
                  Parent: <strong>Anthony Tolbert</strong> · Contact: <a href="tel:0881479329" style={{ color: 'var(--color-amber-terracotta)', fontWeight: 700 }}>088 147 9329</a>
                </p>
                <div style={{ display: 'flex', gap: '8px', marginTop: '6px' }}>
                  <span className="badge badge-sage">Tag: {scannedPetDossier.rabiesTag}</span>
                  <span className="badge badge-terracotta">Chip: {scannedPetDossier.microchipId}</span>
                </div>
              </div>
            </div>

            {/* Vaccination History */}
            <div style={{ marginBottom: '20px' }}>
              <h4 style={{ fontSize: '14px', fontWeight: 700, color: '#111827', marginBottom: '8px', textTransform: 'uppercase' }}>
                Vaccination Status
              </h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {vaccinations.filter(v => v.petName === scannedPetDossier.name).map(v => (
                  <div key={v.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 14px', background: '#FFFFFF', border: '1px solid var(--color-border-subtle)', borderRadius: '10px' }}>
                    <div>
                      <p style={{ fontSize: '13px', fontWeight: 700, color: '#111827' }}>{v.vaccineName}</p>
                      <p style={{ fontSize: '11px', color: '#1F2937', fontWeight: 600 }}>Administered: {v.dateAdministered}</p>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <span className="badge badge-sage">{v.status}</span>
                      <p style={{ fontSize: '11px', color: '#111827', fontWeight: 700, marginTop: '2px' }}>Valid: {v.validUntil}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Deworming History */}
            <div style={{ marginBottom: '24px' }}>
              <h4 style={{ fontSize: '14px', fontWeight: 700, color: '#111827', marginBottom: '8px', textTransform: 'uppercase' }}>
                Deworming History
              </h4>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
                {dewormings.filter(d => d.petName === scannedPetDossier.name).map(d => (
                  <div key={d.id} style={{ display: 'flex', justifyContent: 'space-between', padding: '10px 14px', background: '#FFFFFF', border: '1px solid var(--color-border-subtle)', borderRadius: '10px' }}>
                    <div>
                      <p style={{ fontSize: '13px', fontWeight: 700, color: '#111827' }}>{d.productName}</p>
                      <p style={{ fontSize: '11px', color: '#1F2937', fontWeight: 600 }}>Given: {d.dateGiven} ({d.dosage})</p>
                    </div>
                    <div style={{ textAlign: 'right' }}>
                      <p style={{ fontSize: '12px', color: 'var(--color-amber-terracotta)', fontWeight: 700 }}>Next Due: {d.nextDueDate}</p>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            {/* Modal Actions */}
            <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
              <button 
                onClick={() => setShowDossierModal(false)}
                className="btn-secondary"
                style={{ padding: '8px 16px', color: '#111827', fontWeight: 700 }}
              >
                Close
              </button>
              <button 
                onClick={() => {
                  const newAppt: AppointmentItem = {
                    id: Date.now(),
                    clientName: 'Anthony Tolbert',
                    petName: scannedPetDossier.name,
                    species: scannedPetDossier.species,
                    time: `Today at ${new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`,
                    reason: 'QR Scan Reception Triage & Check-in',
                    vetName: 'Dr. David Kpadeh',
                    status: 'In Consultation'
                  };
                  setAppointments([newAppt, ...appointments]);
                  setAdmitSuccess(true);
                }}
                className="btn-primary"
                style={{ padding: '8px 18px', fontWeight: 700, backgroundColor: 'var(--color-forest-sage)' }}
              >
                <Plus size={16} /> Admit to Today's Clinic Queue
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Clinic Footer */}
      <footer style={{
        borderTop: '1px solid var(--color-border-subtle)',
        backgroundColor: '#FFFFFF',
        padding: '16px 24px',
        textAlign: 'center',
        fontSize: '13px',
        color: '#111827',
        fontWeight: 600
      }}>
        © {new Date().getFullYear()} Happy Paws Liberia Rescue Center. Dual Client: Android Native & Progressive Web App (PWA). All text styled in high-visibility black.
      </footer>
    </div>
  );
}
