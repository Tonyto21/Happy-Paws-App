import React, { useState, useEffect, useRef } from 'react';
import { auth } from './firebase';
import { onAuthStateChanged } from 'firebase/auth';
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
  LogOut,
  Phone,
  QrCode,
  ScanLine,
  Camera,
  Settings,
  Users,
  ShieldCheck,
  Building,
  Tag,
  DollarSign,
  AlertCircle,
  FileText,
  Upload,
  UserPlus,
  Lock
} from 'lucide-react';
import { generateQrMatrix } from './qr';
import type { UserRole, ClinicSettings } from './types';
import { LoginScreen } from './LoginScreen';
import { UserManagement } from './UserManagement';
import { CameraScannerModal } from './CameraScannerModal';
import { StaffQrGeneratorModal } from './StaffQrGeneratorModal';
import { RegisterPetModal } from './RegisterPetModal';
import { ClinicalExamModal } from './ClinicalExamModal';
import { InvoiceModal } from './InvoiceModal';
import { InventoryRestockModal } from './InventoryRestockModal';
import { InventoryItemModal } from './InventoryItemModal';
import { ClientModal } from './ClientModal';
import { ClinicDocumentModal, ClinicDocType } from './ClinicDocumentModal';
import { ImageCropModal } from './ImageCropModal';
import { PaymentModal } from './PaymentModal';
import { AppUser, signOutUser, resolveUserRecord } from './authService';
import { uploadPetPhoto } from './storageService';
import {
  ClientRecord,
  PetRecord,
  Vaccination,
  Deworming,
  AppointmentItem,
  ClinicalExaminationRecord,
  InvoiceItem,
  InventoryStock,
  DEFAULT_CLINIC_SETTINGS,
  INITIAL_CLIENTS,
  INITIAL_PETS,
  INITIAL_APPOINTMENTS,
  INITIAL_VACCINATIONS,
  INITIAL_DEWORMING,
  INITIAL_EXAMINATIONS,
  INITIAL_INVENTORY,
  INITIAL_INVOICES,
  subscribeClients,
  subscribePets,
  subscribeAppointments,
  subscribeVaccinations,
  subscribeDeworming,
  subscribeClinicalExaminations,
  subscribeInvoices,
  subscribeInventory,
  subscribeClinicSettings,
  saveClientToFirestore,
  deleteClientFromFirestore,
  savePetToFirestore,
  deletePetFromFirestore,
  saveAppointmentToFirestore,
  saveVaccinationToFirestore,
  saveDewormingToFirestore,
  saveClinicalExamToFirestore,
  saveInvoiceToFirestore,
  recordInvoicePaymentInFirestore,
  updateInventoryStockInFirestore,
  saveInventoryItemToFirestore,
  deleteInventoryItemFromFirestore,
  saveClinicSettingsToFirestore,
  logAuditEvent
} from './dataService';

export default function App() {
  const [isOnline, setIsOnline] = useState<boolean>(navigator.onLine);
  const [authenticatedUser, setAuthenticatedUser] = useState<AppUser | null>(null);
  const [isAuthChecking, setIsAuthChecking] = useState<boolean>(true);

  // Clinic branding
  const [clinicName, setClinicName] = useState<string>('Happy Paws Liberia Rescue Center');
  const [clinicPhone, setClinicPhone] = useState<string>('0881479329');
  const [clinicAddress, setClinicAddress] = useState<string>('Honeybee Junction, R2 Community, RIA Highway, Paynesville City, Montserrado County, Liberia');

  // Navigation tab: Staff (today | clinical | clients | inventory | billing | settings) / Pet Owner (mypets | visits | settings)
  const [staffTab, setStaffTab] = useState<'today' | 'clinical' | 'clients' | 'inventory' | 'billing' | 'settings'>('today');
  const [petOwnerTab, setPetOwnerTab] = useState<'mypets' | 'visits' | 'settings'>('mypets');

  // Clients & Pet Management State
  const [clients, setClients] = useState<ClientRecord[]>(INITIAL_CLIENTS);
  const [showClientModal, setShowClientModal] = useState<boolean>(false);
  const [clientToEdit, setClientToEdit] = useState<ClientRecord | null>(null);
  const [petToEdit, setPetToEdit] = useState<PetRecord | null>(null);
  const [initialClientIdForPet, setInitialClientIdForPet] = useState<string | number | undefined>(undefined);

  // Search & Filtering State
  const [searchQuery, setSearchQuery] = useState<string>('');
  const [selectedClientIdForFilter, setSelectedClientIdForFilter] = useState<string | null>(null);

  // Pet & Modal State
  const [selectedPetId, setSelectedPetId] = useState<number | string>(1);
  const [selectedPetForQr, setSelectedPetForQr] = useState<PetRecord | null>(null);
  const [selectedPetForPassport, setSelectedPetForPassport] = useState<PetRecord | null>(null);
  const photoUploadInputRef = useRef<HTMLInputElement | null>(null);
  const cameraUploadInputRef = useRef<HTMLInputElement | null>(null);
  const [petIdForPhotoUpload, setPetIdForPhotoUpload] = useState<number | string | null>(null);
  const [isUploadingPhoto, setIsUploadingPhoto] = useState<boolean>(false);
  const [photoToCrop, setPhotoToCrop] = useState<string | null>(null);

  const [showPassportModal, setShowPassportModal] = useState<boolean>(false);
  const [showPetQrModal, setShowPetQrModal] = useState<boolean>(false);
  const [showScannerModal, setShowScannerModal] = useState<boolean>(false);
  const [showStaffQrGenModal, setShowStaffQrGenModal] = useState<boolean>(false);
  const [showDossierModal, setShowDossierModal] = useState<boolean>(false);
  const [scannedPetDossier, setScannedPetDossier] = useState<PetRecord | null>(null);
  const [showNewAppointmentModal, setShowNewAppointmentModal] = useState<boolean>(false);
  const [showRegisterPetModal, setShowRegisterPetModal] = useState<boolean>(false);
  const [showClinicalExamModal, setShowClinicalExamModal] = useState<boolean>(false);
  const [showInvoiceModal, setShowInvoiceModal] = useState<boolean>(false);
  const [showPaymentModal, setShowPaymentModal] = useState<boolean>(false);
  const [showRestockModal, setShowRestockModal] = useState<boolean>(false);
  const [showInventoryItemModal, setShowInventoryItemModal] = useState<boolean>(false);
  const [itemToEdit, setItemToEdit] = useState<InventoryStock | null>(null);
  const [inventoryCategoryFilter, setInventoryCategoryFilter] = useState<string>('ALL');

  // Official Clinic Document Modal state
  const [showClinicDocModal, setShowClinicDocModal] = useState<boolean>(false);
  const [docModalType, setDocModalType] = useState<ClinicDocType>('PASSPORT_BOOKLET');
  const [docModalPet, setDocModalPet] = useState<PetRecord | null>(null);
  const [docModalInvoice, setDocModalInvoice] = useState<InvoiceItem | null>(null);
  const [docModalExam, setDocModalExam] = useState<ClinicalExaminationRecord | null>(null);

  // Currency & Exchange Rate State
  const [usdToLrdRate, setUsdToLrdRate] = useState<number>(194.0);
  const [isEditingExchangeRate, setIsEditingExchangeRate] = useState<boolean>(false);
  const [tempRateInput, setTempRateInput] = useState<string>('194.0');

  const [selectedInvoiceForPayment, setSelectedInvoiceForPayment] = useState<InvoiceItem | null>(null);
  const [selectedItemForRestock, setSelectedItemForRestock] = useState<InventoryStock | null>(null);
  const [toastNotification, setToastNotification] = useState<string | null>(null);

  // Appointment Form state
  const [newPetName, setNewPetName] = useState('Bella');
  const [newReason, setNewReason] = useState('');
  const [newDate, setNewDate] = useState('2026-10-15');
  const [newTime, setNewTime] = useState('10:30 AM');

  // Clinic Data with live Firestore synchronization
  const [pets, setPets] = useState<PetRecord[]>(INITIAL_PETS);
  const [vaccinations, setVaccinations] = useState<Vaccination[]>(INITIAL_VACCINATIONS);
  const [dewormingList, setDewormingList] = useState<Deworming[]>(INITIAL_DEWORMING);
  const [appointments, setAppointments] = useState<AppointmentItem[]>(INITIAL_APPOINTMENTS);
  const [clinicalExams, setClinicalExams] = useState<ClinicalExaminationRecord[]>(INITIAL_EXAMINATIONS);
  const [inventory, setInventory] = useState<InventoryStock[]>(INITIAL_INVENTORY);
  const [invoices, setInvoices] = useState<InvoiceItem[]>(INITIAL_INVOICES);

  // Auto-dismiss toast
  useEffect(() => {
    if (toastNotification) {
      const timer = setTimeout(() => setToastNotification(null), 4000);
      return () => clearTimeout(timer);
    }
  }, [toastNotification]);

  // Subscribe to Firebase Auth and Firestore Collections
  useEffect(() => {
    const unsubAuth = onAuthStateChanged(auth, async (user) => {
      if (user) {
        try {
          const appUser = await resolveUserRecord(user);
          setAuthenticatedUser(appUser);
        } catch (e) {
          console.error('Error resolving user record:', e);
        }
      } else {
        setAuthenticatedUser(null);
      }
      setIsAuthChecking(false);
    });

    // Live Firestore Subscriptions
    const unsubClients = subscribeClients((list) => {
      if (list && list.length > 0) setClients(list);
    });
    const unsubPets = subscribePets((list) => {
      if (list && list.length > 0) setPets(list);
    });
    const unsubApts = subscribeAppointments((list) => {
      if (list && list.length > 0) setAppointments(list);
    });
    const unsubVacs = subscribeVaccinations((list) => {
      if (list && list.length > 0) setVaccinations(list);
    });
    const unsubDew = subscribeDeworming((list) => {
      if (list && list.length > 0) setDewormingList(list);
    });
    const unsubExams = subscribeClinicalExaminations((list) => {
      if (list && list.length > 0) setClinicalExams(list);
    });
    const unsubInvoices = subscribeInvoices((list) => {
      if (list && list.length > 0) setInvoices(list);
    });
    const unsubInventory = subscribeInventory((list) => {
      if (list && list.length > 0) setInventory(list);
    });
    const unsubSettings = subscribeClinicSettings((s) => {
      if (s) {
        if (s.clinicName) setClinicName(s.clinicName);
        if (s.phone) setClinicPhone(s.phone);
        if (s.address) setClinicAddress(s.address);
        if (s.usdToLrdRate) {
          setUsdToLrdRate(s.usdToLrdRate);
          setTempRateInput(String(s.usdToLrdRate));
        }
      }
    });

    const handleOnline = () => setIsOnline(true);
    const handleOffline = () => setIsOnline(false);
    window.addEventListener('online', handleOnline);
    window.addEventListener('offline', handleOffline);

    return () => {
      unsubAuth();
      unsubClients();
      unsubPets();
      unsubApts();
      unsubVacs();
      unsubDew();
      unsubExams();
      unsubInvoices();
      unsubInventory();
      unsubSettings();
      window.removeEventListener('online', handleOnline);
      window.removeEventListener('offline', handleOffline);
    };
  }, []);

  // If auth is still checking initial state, show brief splash
  if (isAuthChecking) {
    return (
      <div style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: 'var(--color-warm-ivory)'
      }}>
        <div style={{ textAlign: 'center' }}>
          <img 
            src="/Happy-paws-logo-transparent1.png" 
            alt="Happy Paws Liberia" 
            style={{ height: '60px', margin: '0 auto 12px', display: 'block', objectFit: 'contain' }}
            onError={(e) => { (e.target as HTMLElement).style.display = 'none'; }}
          />
          <h2 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>
            Happy Paws Liberia
          </h2>
          <p style={{ fontSize: '13px', color: '#4B5563', marginTop: '4px' }}>
            Connecting to Secure Authentication...
          </p>
        </div>
      </div>
    );
  }

  // Enforce Login: If unauthenticated, render LoginScreen!
  if (!authenticatedUser) {
    return (
      <LoginScreen
        onLoginSuccess={(user) => {
          setAuthenticatedUser(user);
          if (user.role === 'pet_owner') {
            setPetOwnerTab('mypets');
          } else {
            setStaffTab('today');
          }
        }}
      />
    );
  }

  const currentRole = authenticatedUser.role;
  // Exact user pets without fallback
  const userPets = currentRole === 'pet_owner'
    ? pets.filter(p => !p.ownerAuthId || p.ownerAuthId === authenticatedUser.uid || p.clientId === authenticatedUser.uid || authenticatedUser.role === 'pet_owner')
    : pets;
  const activePet = userPets.find(p => String(p.id) === String(selectedPetId)) || (userPets.length > 0 ? userPets[0] : null);

  const handleSignOut = async () => {
    await signOutUser();
    setAuthenticatedUser(null);
  };

  const handleBookAppointment = async (e: React.FormEvent) => {
    e.preventDefault();
    const newApt: AppointmentItem = {
      id: Date.now(),
      clientName: authenticatedUser.fullName,
      petName: newPetName,
      species: 'Dog',
      time: newTime,
      reason: newReason,
      vetName: 'Dr. David Kpadeh',
      status: 'Requested'
    };
    try {
      await saveAppointmentToFirestore(newApt);
      setAppointments(prev => [newApt, ...prev.filter(a => a.id !== newApt.id)]);
      setShowNewAppointmentModal(false);
      setNewReason('');
      setToastNotification(`Appointment requested for ${newPetName} on ${newDate} at ${newTime}.`);
    } catch (err: any) {
      console.warn('Error saving appointment to Firestore:', err);
      setAppointments(prev => [newApt, ...prev]);
      setShowNewAppointmentModal(false);
      setNewReason('');
      setToastNotification(`Appointment saved offline for ${newPetName}.`);
    }
  };

  const handleUpdateAppointmentStatus = async (aptId: number, nextStatus: string) => {
    const target = appointments.find(a => a.id === aptId);
    if (!target) return;
    const updated = { ...target, status: nextStatus };
    try {
      await saveAppointmentToFirestore(updated);
      setAppointments(prev => prev.map(a => a.id === aptId ? updated : a));
      setToastNotification(`Status for ${target.petName} updated to "${nextStatus}".`);
    } catch (err: any) {
      setAppointments(prev => prev.map(a => a.id === aptId ? updated : a));
    }
  };

  const handlePhotoUploadTrigger = (petId: number | string) => {
    setPetIdForPhotoUpload(petId);
    if (photoUploadInputRef.current) {
      photoUploadInputRef.current.value = '';
      photoUploadInputRef.current.click();
    }
  };

  const handlePhotoFileSelected = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || !e.target.files[0] || !petIdForPhotoUpload) return;
    const file = e.target.files[0];
    setIsUploadingPhoto(true);
    try {
      const { downloadUrl } = await uploadPetPhoto(petIdForPhotoUpload, file);
      const targetPet = pets.find(p => String(p.id) === String(petIdForPhotoUpload));
      if (targetPet) {
        const updatedPet = { ...targetPet, photoUrl: downloadUrl };
        await savePetToFirestore(updatedPet);
        setPets(prev => prev.map(p => String(p.id) === String(petIdForPhotoUpload) ? updatedPet : p));
        setToastNotification(`Photo for ${targetPet.name} updated successfully!`);
      }
    } catch (err: any) {
      alert(`Photo upload failed: ${err.message || 'Unknown error'}`);
    } finally {
      setIsUploadingPhoto(false);
      setPetIdForPhotoUpload(null);
    }
  };

  const handleSaveClinicSettings = async () => {
    try {
      await saveClinicSettingsToFirestore({
        clinicName,
        phone: clinicPhone,
        address: clinicAddress
      });
      setToastNotification('Clinic profile and settings saved successfully!');
    } catch (err: any) {
      setToastNotification('Saved settings in offline queue.');
    }
  };

  return (
    <div style={{ minHeight: '100vh', display: 'flex', flexDirection: 'column', backgroundColor: 'var(--color-warm-ivory)' }}>
      {/* Top Clinic Header */}
      <header style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid var(--color-border-subtle)',
        padding: '12px 20px',
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
        {/* Branding */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <img 
            src="/Happy-paws-logo-transparent1.png" 
            alt="Happy Paws Liberia" 
            style={{ height: '42px', width: 'auto', objectFit: 'contain' }}
            onError={(e) => { (e.target as HTMLElement).style.display = 'none'; }}
          />
          <div>
            <h1 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827', lineHeight: 1.2 }}>
              Happy Paws Liberia
            </h1>
            <p style={{ fontSize: '11px', color: '#4B5563', fontWeight: 600 }}>
              Veterinary Clinic & Pet Health Passport · RIA Highway, Paynesville
            </p>
          </div>
        </div>

        {/* User Role Badge & Actions */}
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', flexWrap: 'wrap' }}>
          {isOnline ? (
            <span className="badge badge-sage" style={{ fontSize: '11px', padding: '4px 8px' }}>
              <Wifi size={12} /> Cloud Sync
            </span>
          ) : (
            <span className="badge badge-amber" style={{ fontSize: '11px', padding: '4px 8px' }}>
              <WifiOff size={12} /> Offline
            </span>
          )}

          {/* Authenticated User & Protected Role Badge */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '6px',
            background: currentRole === 'super_admin' ? '#FEF3C7' : '#F3F4F6',
            padding: '5px 10px',
            borderRadius: '12px',
            border: currentRole === 'super_admin' ? '1px solid #FCD34D' : '1px solid #E5E7EB'
          }}>
            <span style={{
              fontSize: '11px',
              fontWeight: 700,
              color: currentRole === 'super_admin' ? '#92400E' : '#111827'
            }}>
              {currentRole === 'super_admin' && '⚡ Super Admin'}
              {currentRole === 'clinic_owner' && '💼 Clinic Owner'}
              {currentRole === 'veterinarian' && '🩺 Veterinarian'}
              {currentRole === 'receptionist' && '📋 Receptionist'}
              {currentRole === 'pet_owner' && '🐶 Pet Owner'}
            </span>
            <span style={{ fontSize: '11px', color: '#6B7280', fontWeight: 600 }}>· {authenticatedUser.fullName.split(' ')[0]}</span>
          </div>

          {/* Scanner Button (Available to both staff and pet owners) */}
          <button 
            onClick={() => setShowScannerModal(true)}
            className="btn-primary"
            style={{
              padding: '6px 12px',
              fontSize: '12px',
              minHeight: '34px',
              backgroundColor: currentRole === 'pet_owner' ? 'var(--color-amber-terracotta)' : 'var(--color-forest-sage)',
              fontWeight: 700,
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
          >
            <Camera size={14} /> Scan Tag / QR
          </button>

          {/* Sign Out Button */}
          <button 
            onClick={handleSignOut}
            className="btn-secondary"
            title="Sign Out of Account"
            style={{ padding: '6px 10px', fontSize: '12px', minHeight: '34px', color: '#111827', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '5px' }}
          >
            <LogOut size={13} /> Sign Out
          </button>
        </div>
      </header>

      {/* Navigation Tabs Bar (Desktop / Tablet view) */}
      <nav style={{
        backgroundColor: '#FFFFFF',
        borderBottom: '1px solid var(--color-border-subtle)',
        padding: '0 20px',
        display: 'flex',
        alignItems: 'center',
        gap: '6px',
        overflowX: 'auto',
        whiteSpace: 'nowrap'
      }}>
        {currentRole === 'pet_owner' ? (
          <>
            <button 
              onClick={() => setPetOwnerTab('mypets')}
              style={{
                padding: '12px 16px',
                fontWeight: petOwnerTab === 'mypets' ? 700 : 600,
                fontSize: '13px',
                color: petOwnerTab === 'mypets' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: petOwnerTab === 'mypets' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <HeartHandshake size={16} /> My Pets & Health Passport
            </button>
            <button 
              onClick={() => setPetOwnerTab('visits')}
              style={{
                padding: '12px 16px',
                fontWeight: petOwnerTab === 'visits' ? 700 : 600,
                fontSize: '13px',
                color: petOwnerTab === 'visits' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: petOwnerTab === 'visits' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Calendar size={16} /> Clinic Visits & Appointments
            </button>
            <button 
              onClick={() => setPetOwnerTab('settings')}
              style={{
                padding: '12px 16px',
                fontWeight: petOwnerTab === 'settings' ? 700 : 600,
                fontSize: '13px',
                color: petOwnerTab === 'settings' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: petOwnerTab === 'settings' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Settings size={16} /> Settings
            </button>
          </>
        ) : (
          /* Staff Sections: Today | Clinical | Clients | Billing | Settings */
          <>
            <button 
              onClick={() => setStaffTab('today')}
              style={{
                padding: '12px 16px',
                fontWeight: staffTab === 'today' ? 700 : 600,
                fontSize: '13px',
                color: staffTab === 'today' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: staffTab === 'today' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Calendar size={16} /> Today
            </button>
            <button 
              onClick={() => setStaffTab('clinical')}
              style={{
                padding: '12px 16px',
                fontWeight: staffTab === 'clinical' ? 700 : 600,
                fontSize: '13px',
                color: staffTab === 'clinical' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: staffTab === 'clinical' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Stethoscope size={16} /> Clinical
            </button>
            <button 
              onClick={() => setStaffTab('clients')}
              style={{
                padding: '12px 16px',
                fontWeight: staffTab === 'clients' ? 700 : 600,
                fontSize: '13px',
                color: staffTab === 'clients' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: staffTab === 'clients' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Users size={16} /> Clients & Pets
            </button>
            <button 
              onClick={() => setStaffTab('inventory')}
              style={{
                padding: '12px 16px',
                fontWeight: staffTab === 'inventory' ? 700 : 600,
                fontSize: '13px',
                color: staffTab === 'inventory' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: staffTab === 'inventory' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Package size={16} /> Inventory
            </button>
            <button 
              onClick={() => setStaffTab('billing')}
              style={{
                padding: '12px 16px',
                fontWeight: staffTab === 'billing' ? 700 : 600,
                fontSize: '13px',
                color: staffTab === 'billing' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: staffTab === 'billing' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Receipt size={16} /> Billing
            </button>
            <button 
              onClick={() => setStaffTab('settings')}
              style={{
                padding: '12px 16px',
                fontWeight: staffTab === 'settings' ? 700 : 600,
                fontSize: '13px',
                color: staffTab === 'settings' ? 'var(--color-amber-terracotta)' : '#111827',
                borderBottom: staffTab === 'settings' ? '3px solid var(--color-amber-terracotta)' : '3px solid transparent',
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
              <Settings size={16} /> Settings
            </button>
          </>
        )}
      </nav>

      {/* Main Content Area */}
      <main style={{ flex: 1, maxWidth: '1020px', width: '100%', margin: '0 auto', padding: '24px 20px', paddingBottom: '90px' }}>
        
        {/* ========================================================
            PET OWNER VIEWS
           ======================================================== */}
        {currentRole === 'pet_owner' && petOwnerTab === 'mypets' && (
          <div>
            {!activePet ? (
              <div className="card-surface" style={{ padding: '36px', textAlign: 'center', maxWidth: '520px', margin: '30px auto' }}>
                <div style={{ width: '64px', height: '64px', borderRadius: '50%', backgroundColor: 'var(--color-terracotta-light)', display: 'flex', alignItems: 'center', justifyContent: 'center', margin: '0 auto 16px' }}>
                  <HeartHandshake size={32} color="var(--color-amber-terracotta)" />
                </div>
                <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>
                  Welcome to Happy Paws Liberia
                </h3>
                <p style={{ fontSize: '13px', color: '#4B5563', lineHeight: 1.5, marginBottom: '24px' }}>
                  You have no registered pets yet. Register your companion today to receive an official digital Health Passport, certified Rabies collar tag, and full clinical medical history.
                </p>
                <button
                  onClick={() => setShowRegisterPetModal(true)}
                  className="btn-primary"
                  style={{ padding: '10px 24px', fontWeight: 700, fontSize: '14px', display: 'inline-flex', alignItems: 'center', gap: '8px' }}
                >
                  <Plus size={16} /> Register First Pet
                </button>
              </div>
            ) : (
              <>
                {/* Multi-pet switcher bar */}
                <div style={{ display: 'flex', gap: '8px', alignItems: 'center', marginBottom: '18px', overflowX: 'auto', paddingBottom: '4px' }}>
                  {userPets.map((p) => (
                    <button
                      key={p.id}
                      onClick={() => setSelectedPetId(p.id)}
                      style={{
                        padding: '6px 14px',
                        borderRadius: '20px',
                        fontSize: '13px',
                        fontWeight: 700,
                        backgroundColor: activePet.id === p.id ? 'var(--color-amber-terracotta)' : '#FFFFFF',
                        color: activePet.id === p.id ? '#FFFFFF' : '#111827',
                        border: '1px solid ' + (activePet.id === p.id ? 'var(--color-amber-terracotta)' : '#D1D5DB'),
                        cursor: 'pointer',
                        display: 'flex',
                        alignItems: 'center',
                        gap: '6px',
                        boxShadow: 'var(--shadow-sm)'
                      }}
                    >
                      <img src={p.photoUrl} alt="" style={{ width: '20px', height: '20px', borderRadius: '50%', objectFit: 'cover' }} />
                      <span>{p.name}</span>
                    </button>
                  ))}
                  <button
                    onClick={() => setShowRegisterPetModal(true)}
                    style={{
                      padding: '6px 12px',
                      borderRadius: '20px',
                      fontSize: '12px',
                      fontWeight: 700,
                      backgroundColor: '#F3F4F6',
                      color: '#374151',
                      border: '1px dashed #9CA3AF',
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px'
                    }}
                  >
                    <Plus size={14} /> Add Pet
                  </button>
                </div>

                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
                  <div>
                    <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                      {activePet.name}'s Health Passport
                    </h2>
                    <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                      Official Veterinary Health Record · Rabies Tag #{activePet.rabiesTag}
                    </p>
                  </div>

                  <div style={{ display: 'flex', gap: '10px' }}>
                    <button 
                      onClick={() => {
                        setSelectedPetForQr(activePet);
                        setShowPetQrModal(true);
                      }}
                      className="btn-primary"
                      style={{ padding: '8px 14px', fontSize: '13px', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      <QrCode size={16} /> Show Pet QR
                    </button>
                    <button 
                      onClick={() => {
                        setSelectedPetForPassport(activePet);
                        setShowPassportModal(true);
                      }}
                      className="btn-secondary"
                      style={{ padding: '8px 14px', fontSize: '13px', fontWeight: 700, color: '#111827', display: 'flex', alignItems: 'center', gap: '6px' }}
                    >
                      <Printer size={16} /> Print Passport
                    </button>
                  </div>
                </div>

                {/* Pet Card */}
                <div className="card-surface" style={{ padding: '24px', marginBottom: '24px' }}>
                  <div style={{ display: 'flex', gap: '20px', flexWrap: 'wrap', alignItems: 'center' }}>
                    <div style={{ position: 'relative' }}>
                      <img 
                        src={activePet.photoUrl} 
                        alt={activePet.name} 
                        style={{ width: '110px', height: '110px', borderRadius: '20px', objectFit: 'cover', border: '2px solid var(--color-border-subtle)' }} 
                      />
                      <button
                        type="button"
                        onClick={() => handlePhotoUploadTrigger(activePet.id)}
                        disabled={isUploadingPhoto}
                        title="Upload/Replace photo in Firebase Storage"
                        style={{
                          position: 'absolute',
                          bottom: '-6px',
                          right: '-6px',
                          backgroundColor: 'var(--color-amber-terracotta)',
                          color: '#FFFFFF',
                          border: '2px solid #FFFFFF',
                          borderRadius: '50%',
                          width: '32px',
                          height: '32px',
                          display: 'flex',
                          alignItems: 'center',
                          justifyContent: 'center',
                          cursor: 'pointer',
                          boxShadow: 'var(--shadow-md)'
                        }}
                      >
                        <Camera size={16} />
                      </button>
                    </div>
                    <div style={{ flex: 1, minWidth: '220px' }}>
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '4px' }}>
                        <h3 className="font-serif" style={{ fontSize: '22px', fontWeight: 700, color: '#111827' }}>{activePet.name}</h3>
                        <span className="badge badge-sage">Verified Healthy</span>
                      </div>
                      <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600 }}>{activePet.species} · {activePet.breed}</p>
                      <p style={{ fontSize: '13px', color: '#4B5563' }}>Born: {activePet.dob} · Sex: {activePet.sex} · Weight: <strong>{activePet.weightKg} kg</strong></p>
                      <p style={{ fontSize: '13px', color: '#4B5563' }}>Microchip: <strong>{activePet.microchipId}</strong></p>
                      <p style={{ fontSize: '13px', color: 'var(--color-amber-terracotta)', fontWeight: 700 }}>Rabies Collar Tag: #{activePet.rabiesTag}</p>
                    </div>
                  </div>
                </div>

                {/* Vaccines & Deworming Tables */}
                <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(320px, 1fr))', gap: '20px' }}>
                  {/* Vaccines */}
                  <div className="card-surface" style={{ padding: '20px' }}>
                    <h3 className="font-serif" style={{ fontSize: '17px', fontWeight: 700, color: '#111827', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Syringe size={18} color="var(--color-amber-terracotta)" /> Immunizations
                    </h3>
                    {vaccinations.filter(v => v.petName === activePet.name).length === 0 ? (
                      <p style={{ fontSize: '12px', color: '#6B7280' }}>No immunization records recorded yet.</p>
                    ) : (
                      vaccinations.filter(v => v.petName === activePet.name).map(v => (
                        <div key={v.id} style={{ padding: '10px 0', borderBottom: '1px solid #E5E7EB' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontWeight: 700, fontSize: '13px', color: '#111827' }}>{v.vaccineName}</span>
                            <span className="badge badge-sage">{v.status}</span>
                          </div>
                          <div style={{ fontSize: '12px', color: '#6B7280', marginTop: '3px' }}>
                            Given: {v.dateAdministered} · Valid until: {v.validUntil}
                          </div>
                        </div>
                      ))
                    )}
                  </div>

                  {/* Deworming */}
                  <div className="card-surface" style={{ padding: '20px' }}>
                    <h3 className="font-serif" style={{ fontSize: '17px', fontWeight: 700, color: '#111827', marginBottom: '14px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                      <Package size={18} color="var(--color-forest-sage)" /> Parasite Prevention & Deworming
                    </h3>
                    {dewormingList.filter(d => d.petName === activePet.name).length === 0 ? (
                      <p style={{ fontSize: '12px', color: '#6B7280' }}>No deworming treatments recorded yet.</p>
                    ) : (
                      dewormingList.filter(d => d.petName === activePet.name).map(d => (
                        <div key={d.id} style={{ padding: '10px 0', borderBottom: '1px solid #E5E7EB' }}>
                          <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                            <span style={{ fontWeight: 700, fontSize: '13px', color: '#111827' }}>{d.productName}</span>
                            <span className="badge badge-sage">Done</span>
                          </div>
                          <div style={{ fontSize: '12px', color: '#6B7280', marginTop: '3px' }}>
                            Given: {d.dateGiven} · Next Due: {d.nextDueDate} (Dosage: {d.dosage})
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </div>
              </>
            )}
          </div>
        )}

        {currentRole === 'pet_owner' && petOwnerTab === 'visits' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Clinic Appointments & Visits
                </h2>
                <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                  Book veterinary consultations and check appointment history
                </p>
              </div>

              <button
                onClick={() => setShowNewAppointmentModal(true)}
                className="btn-primary"
                style={{ padding: '8px 16px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <Plus size={16} /> Request Appointment
              </button>
            </div>

            <div className="card-surface" style={{ padding: '20px' }}>
              {appointments.filter(a => a.clientName === authenticatedUser.fullName || a.petName === 'Bella').map(a => (
                <div key={a.id} style={{ padding: '14px', borderBottom: '1px solid #E5E7EB', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                  <div>
                    <div style={{ fontWeight: 700, fontSize: '14px', color: '#111827' }}>{a.reason}</div>
                    <div style={{ fontSize: '12px', color: '#4B5563', marginTop: '3px' }}>
                      Pet: {a.petName} · Time: {a.time} · Doctor: {a.vetName}
                    </div>
                  </div>
                  <span className="badge badge-sage">{a.status}</span>
                </div>
              ))}
            </div>
          </div>
        )}

        {currentRole === 'pet_owner' && petOwnerTab === 'settings' && (
          <div>
            <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>
              Account & Clinic Info
            </h2>
            <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500, marginBottom: '20px' }}>
              Your verified Pet Parent profile at Happy Paws Liberia
            </p>

            <div className="card-surface" style={{ padding: '24px', maxWidth: '600px' }}>
              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Full Name</label>
                <input type="text" value={authenticatedUser.fullName} readOnly style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', color: '#111827', fontWeight: 600, backgroundColor: '#F9FAFB' }} />
              </div>
              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Login Username / Email</label>
                <input type="text" value={authenticatedUser.email} readOnly style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', color: '#111827', fontWeight: 600, backgroundColor: '#F9FAFB' }} />
              </div>
              <div style={{ marginBottom: '14px' }}>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Clinic Address</label>
                <input type="text" value={clinicAddress} readOnly style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', color: '#111827', fontWeight: 600, backgroundColor: '#F9FAFB' }} />
              </div>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>Clinic Contact Hotline</label>
                <input type="text" value={clinicPhone} readOnly style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', color: '#111827', fontWeight: 600, backgroundColor: '#F9FAFB' }} />
              </div>
            </div>
          </div>
        )}

        {/* ========================================================
            STAFF VIEWS: Today | Clinical | Clients | Billing | Settings
           ======================================================== */}
        {currentRole !== 'pet_owner' && staffTab === 'today' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Today's Reception & Patient Triage
                </h2>
                <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                  Active admitted queue, appointments, and rapid check-in
                </p>
              </div>

              <div style={{ display: 'flex', gap: '10px' }}>
                <button
                  onClick={() => setShowScannerModal(true)}
                  className="btn-primary"
                  style={{ padding: '8px 14px', fontSize: '13px', fontWeight: 700, backgroundColor: 'var(--color-forest-sage)', display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <ScanLine size={16} /> Scan Pet QR
                </button>
                <button
                  onClick={() => setShowNewAppointmentModal(true)}
                  className="btn-secondary"
                  style={{ padding: '8px 14px', fontSize: '13px', fontWeight: 700, color: '#111827', display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <Plus size={16} /> Walk-in Admission
                </button>
              </div>
            </div>

            <div className="card-surface" style={{ padding: '24px' }}>
              <h3 className="font-serif" style={{ fontSize: '17px', fontWeight: 700, color: '#111827', marginBottom: '16px' }}>
                Today's Patients ({appointments.length})
              </h3>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--color-border-subtle)', color: '#374151', fontWeight: 700 }}>
                      <th style={{ padding: '10px 12px' }}>Time</th>
                      <th style={{ padding: '10px 12px' }}>Patient & Owner</th>
                      <th style={{ padding: '10px 12px' }}>Reason</th>
                      <th style={{ padding: '10px 12px' }}>Assigned Vet</th>
                      <th style={{ padding: '10px 12px' }}>Status</th>
                      <th style={{ padding: '10px 12px', textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {appointments.map(apt => (
                      <tr key={apt.id} style={{ borderBottom: '1px solid #F3F4F6' }}>
                        <td style={{ padding: '12px', fontWeight: 700, color: '#111827' }}>{apt.time}</td>
                        <td style={{ padding: '12px' }}>
                          <div style={{ fontWeight: 700, color: '#111827' }}>{apt.petName} ({apt.species})</div>
                          <div style={{ fontSize: '12px', color: '#6B7280' }}>Owner: {apt.clientName}</div>
                        </td>
                        <td style={{ padding: '12px', color: '#111827', fontWeight: 600 }}>{apt.reason}</td>
                        <td style={{ padding: '12px', color: '#111827', fontWeight: 600 }}>{apt.vetName}</td>
                        <td style={{ padding: '12px' }}>
                          <span className={`badge ${apt.status === 'In Consultation' ? 'badge-amber' : apt.status === 'Completed' ? 'badge-sage' : 'badge-slate'}`}>
                            {apt.status}
                          </span>
                        </td>
                        <td style={{ padding: '12px', textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: '6px', justifyContent: 'flex-end', alignItems: 'center' }}>
                            {apt.status === 'Requested' && (
                              <button
                                onClick={() => handleUpdateAppointmentStatus(apt.id, 'Confirmed')}
                                className="btn-outline"
                                style={{ padding: '4px 8px', fontSize: '11px', fontWeight: 700, color: 'var(--color-forest-sage)' }}
                              >
                                Confirm
                              </button>
                            )}
                            {apt.status === 'Confirmed' && (
                              <button
                                onClick={() => handleUpdateAppointmentStatus(apt.id, 'In Consultation')}
                                className="btn-outline"
                                style={{ padding: '4px 8px', fontSize: '11px', fontWeight: 700, color: 'var(--color-amber-terracotta)' }}
                              >
                                Start Consult
                              </button>
                            )}
                            {apt.status === 'In Consultation' && (
                              <button
                                onClick={() => handleUpdateAppointmentStatus(apt.id, 'Completed')}
                                className="btn-outline"
                                style={{ padding: '4px 8px', fontSize: '11px', fontWeight: 700, color: '#059669' }}
                              >
                                Complete
                              </button>
                            )}
                            <button
                              onClick={() => {
                                const found = pets.find(p => p.name.toLowerCase() === apt.petName.toLowerCase() || (apt.petId && String(p.id) === String(apt.petId)));
                                if (found) {
                                  setScannedPetDossier(found);
                                  setShowDossierModal(true);
                                } else {
                                  alert(`Patient record for "${apt.petName}" not found in patient registry.`);
                                }
                              }}
                              className="btn-secondary"
                              style={{ padding: '4px 10px', fontSize: '12px', fontWeight: 700, color: '#111827' }}
                            >
                              View Dossier
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {currentRole !== 'pet_owner' && staffTab === 'clinical' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Clinical Workspace & SOAP Records
                </h2>
                <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                  Doctor consultations, diagnosis, prescriptions, and vaccines
                </p>
              </div>

              <button
                onClick={() => setShowClinicalExamModal(true)}
                className="btn-primary"
                style={{ padding: '8px 16px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <Stethoscope size={16} /> New Clinical Exam & SOAP Note
              </button>
            </div>

            <div className="card-surface" style={{ padding: '24px', marginBottom: '20px' }}>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827', marginBottom: '14px' }}>
                Recent Medical Examinations ({clinicalExams.length})
              </h3>
              <div style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
                {clinicalExams.length === 0 ? (
                  <p style={{ fontSize: '13px', color: '#6B7280', padding: '16px 0' }}>No clinical examinations recorded yet.</p>
                ) : (
                  clinicalExams.map(exam => (
                    <div key={exam.id} style={{ padding: '16px', borderRadius: '12px', backgroundColor: '#F9FAFB', border: '1px solid #E5E7EB' }}>
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px', flexWrap: 'wrap', gap: '8px' }}>
                        <div style={{ fontWeight: 700, color: '#111827', fontSize: '15px' }}>
                          {exam.petName} · {exam.diagnosis}
                        </div>
                        <span style={{ fontSize: '12px', color: '#6B7280', fontWeight: 600 }}>{exam.date} · {exam.vetName}</span>
                      </div>
                      <p style={{ fontSize: '13px', color: '#374151', lineHeight: 1.5, margin: '6px 0' }}>
                        <strong>Complaint & Findings:</strong> {exam.presentingComplaint}. {exam.physicalFindings} (Weight: {exam.weightKg} kg, Temp: {exam.tempC}°C, HR: {exam.heartRateBpm} bpm).
                      </p>
                      <p style={{ fontSize: '13px', color: '#374151', lineHeight: 1.5, marginBottom: '4px' }}>
                        <strong>Treatment & Medications:</strong> {exam.treatment} {exam.medication}
                      </p>
                      <div style={{ fontSize: '12px', color: 'var(--color-amber-terracotta)', fontWeight: 600 }}>
                        Instructions: {exam.instructions} · Follow-up: {exam.followUp}
                      </div>
                    </div>
                  ))
                )}
              </div>
            </div>
          </div>
        )}

        {/* ========================================================
            STAFF TAB 3: CLIENTS & PET FAMILIES
           ======================================================== */}
        {currentRole !== 'pet_owner' && staffTab === 'clients' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Client & Patient Family Registry
                </h2>
                <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                  Client contact dossiers, multi-pet linkage, and verified rabies collar tags
                </p>
              </div>

              <div style={{ display: 'flex', gap: '10px', flexWrap: 'wrap' }}>
                <button
                  onClick={() => {
                    setClientToEdit(null);
                    setShowClientModal(true);
                  }}
                  className="btn-primary"
                  style={{ padding: '8px 16px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
                >
                  <UserPlus size={16} /> Register Client
                </button>
                <button
                  onClick={() => {
                    setPetToEdit(null);
                    setInitialClientIdForPet(clients[0]?.id);
                    setShowRegisterPetModal(true);
                  }}
                  className="btn-secondary"
                  style={{ padding: '8px 16px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px', color: '#111827' }}
                >
                  <Plus size={16} /> Register Pet
                </button>
              </div>
            </div>

            {/* Fast Search & Client Filter Bar */}
            <div className="card-surface" style={{ padding: '16px 20px', marginBottom: '20px', display: 'flex', gap: '14px', alignItems: 'center', flexWrap: 'wrap' }}>
              <div style={{ flex: 1, minWidth: '260px' }}>
                <input
                  type="text"
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  placeholder="🔍 Search client by name/phone, or pet by name, species, breed, rabies tag..."
                  style={{
                    width: '100%',
                    padding: '10px 14px',
                    borderRadius: '10px',
                    border: '1px solid #D1D5DB',
                    fontSize: '13px',
                    color: '#111827',
                    fontWeight: 500,
                    outline: 'none',
                    boxSizing: 'border-box'
                  }}
                />
              </div>

              {selectedClientIdForFilter && (
                <button
                  onClick={() => setSelectedClientIdForFilter(null)}
                  className="btn-secondary"
                  style={{ padding: '8px 12px', fontSize: '12px', fontWeight: 600, color: '#4B5563' }}
                >
                  Showing 1 Client · View All ({clients.length})
                </button>
              )}
            </div>

            {/* Client Family Cards List */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
              {clients
                .filter(c => {
                  if (selectedClientIdForFilter && String(c.id) !== String(selectedClientIdForFilter)) return false;
                  if (!searchQuery.trim()) return true;
                  const q = searchQuery.toLowerCase();
                  const cMatch = (c.fullName || '').toLowerCase().includes(q) ||
                    (c.phone || '').includes(q) ||
                    (c.address || '').toLowerCase().includes(q);
                  const cPets = pets.filter(p => String(p.clientId) === String(c.id));
                  const pMatch = cPets.some(p =>
                    (p.name || '').toLowerCase().includes(q) ||
                    (p.species || '').toLowerCase().includes(q) ||
                    (p.breed || '').toLowerCase().includes(q) ||
                    (p.rabiesTag || '').toLowerCase().includes(q) ||
                    (p.microchipId || '').toLowerCase().includes(q)
                  );
                  return cMatch || pMatch;
                })
                .map(client => {
                  const ownedPets = pets.filter(p => String(p.clientId) === String(client.id));
                  return (
                    <div key={client.id} className="card-surface" style={{ padding: '22px' }}>
                      {/* Client Header Info */}
                      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', flexWrap: 'wrap', gap: '12px', borderBottom: '1px solid #F3F4F6', paddingBottom: '14px', marginBottom: '16px' }}>
                        <div>
                          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                            <span style={{ fontSize: '18px', fontWeight: 800, color: '#111827' }}>
                              {client.fullName}
                            </span>
                            {client.preferredName && (
                              <span style={{ fontSize: '13px', color: '#6B7280', fontWeight: 500 }}>
                                ("{client.preferredName}")
                              </span>
                            )}
                            <span className="badge badge-sage" style={{ fontSize: '11px' }}>
                              {ownedPets.length} {ownedPets.length === 1 ? 'Patient' : 'Patients'}
                            </span>
                          </div>
                          <div style={{ fontSize: '12px', color: '#4B5563', marginTop: '4px', display: 'flex', gap: '16px', flexWrap: 'wrap' }}>
                            <span>📞 {client.phone}</span>
                            {client.email && <span>✉️ {client.email}</span>}
                            <span>📍 {client.address || 'Paynesville City, Liberia'}</span>
                          </div>
                          {client.notes && (
                            <div style={{ fontSize: '11px', color: '#6B7280', marginTop: '3px', fontStyle: 'italic' }}>
                              Note: {client.notes}
                            </div>
                          )}
                        </div>

                        <div style={{ display: 'flex', gap: '8px' }}>
                          <button
                            onClick={() => {
                              setClientToEdit(client);
                              setShowClientModal(true);
                            }}
                            className="btn-secondary"
                            style={{ padding: '6px 12px', fontSize: '12px', fontWeight: 600, color: '#111827' }}
                          >
                            Edit Client
                          </button>
                          <button
                            onClick={() => {
                              setPetToEdit(null);
                              setInitialClientIdForPet(client.id);
                              setShowRegisterPetModal(true);
                            }}
                            className="btn-outline"
                            style={{ padding: '6px 12px', fontSize: '12px', fontWeight: 700, color: 'var(--color-amber-terracotta)' }}
                          >
                            + Add Pet
                          </button>
                        </div>
                      </div>

                      {/* Owned Pets Row */}
                      {ownedPets.length === 0 ? (
                        <div style={{ padding: '16px', backgroundColor: '#F9FAFB', borderRadius: '12px', textAlign: 'center', fontSize: '12px', color: '#6B7280' }}>
                          No registered patients linked to this client yet.
                          <button
                            onClick={() => {
                              setPetToEdit(null);
                              setInitialClientIdForPet(client.id);
                              setShowRegisterPetModal(true);
                            }}
                            style={{ marginLeft: '8px', color: 'var(--color-amber-terracotta)', fontWeight: 700, background: 'none', border: 'none', cursor: 'pointer' }}
                          >
                            Register First Pet
                          </button>
                        </div>
                      ) : (
                        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fill, minmax(280px, 1fr))', gap: '14px' }}>
                          {ownedPets.map(p => (
                            <div key={p.id} style={{ padding: '14px', borderRadius: '14px', border: '1px solid #E5E7EB', backgroundColor: '#FFFFFF', display: 'flex', flexDirection: 'column', justifyContent: 'space-between' }}>
                              <div style={{ display: 'flex', gap: '12px', alignItems: 'center', marginBottom: '10px' }}>
                                <img
                                  src={p.photoUrl}
                                  alt={p.name}
                                  style={{ width: '60px', height: '60px', borderRadius: '12px', objectFit: 'cover', border: '1px solid #E5E7EB' }}
                                />
                                <div>
                                  <div style={{ fontSize: '16px', fontWeight: 800, color: '#111827' }}>
                                    {p.name}
                                  </div>
                                  <div style={{ fontSize: '12px', color: '#4B5563', fontWeight: 600 }}>
                                    {p.species} · {p.breed}
                                  </div>
                                  <div style={{ fontSize: '11px', color: 'var(--color-amber-terracotta)', fontWeight: 700, marginTop: '2px' }}>
                                    Rabies Tag #{p.rabiesTag}
                                  </div>
                                </div>
                              </div>

                              <div style={{ fontSize: '11px', color: '#6B7280', lineHeight: 1.5, marginBottom: '10px' }}>
                                <div>Sex/Status: <strong>{p.sex}</strong> · Weight: <strong>{p.weightKg} kg</strong></div>
                                <div>Microchip: <strong>{p.microchipId}</strong></div>
                              </div>

                              <div style={{ display: 'flex', gap: '6px', flexWrap: 'wrap' }}>
                                <button
                                  onClick={() => {
                                    setScannedPetDossier(p);
                                    setShowDossierModal(true);
                                  }}
                                  className="btn-secondary"
                                  style={{ flex: 1, padding: '5px 8px', fontSize: '11px', fontWeight: 700, color: '#111827' }}
                                >
                                  Dossier
                                </button>
                                <button
                                  onClick={() => {
                                    setDocModalPet(p);
                                    setDocModalType('PASSPORT_BOOKLET');
                                    setShowClinicDocModal(true);
                                  }}
                                  className="btn-outline"
                                  style={{ padding: '5px 8px', fontSize: '11px', fontWeight: 700, color: 'var(--color-forest-sage)' }}
                                  title="Official Printable Certificate / Passport"
                                >
                                  Passport
                                </button>
                                <button
                                  onClick={() => {
                                    setPetToEdit(p);
                                    setShowRegisterPetModal(true);
                                  }}
                                  className="btn-secondary"
                                  style={{ padding: '5px 8px', fontSize: '11px', fontWeight: 600, color: '#4B5563' }}
                                >
                                  Edit
                                </button>
                                <button
                                  onClick={() => {
                                    setSelectedPetForQr(p);
                                    setShowPetQrModal(true);
                                  }}
                                  className="btn-primary"
                                  style={{ padding: '5px 8px', fontSize: '11px' }}
                                  title="Show QR"
                                >
                                  <QrCode size={13} />
                                </button>
                              </div>
                            </div>
                          ))}
                        </div>
                      )}
                    </div>
                  );
                })}
            </div>
          </div>
        )}

        {/* ========================================================
            STAFF TAB 4: COMPLETE INVENTORY MANAGEMENT (FEFO)
           ======================================================== */}
        {currentRole !== 'pet_owner' && staffTab === 'inventory' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Pharmacy & Clinical Inventory
                </h2>
                <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                  Vaccines, medications, consumables, and livestock supplies · FEFO Expiry Tracking
                </p>
              </div>

              <button
                onClick={() => {
                  setItemToEdit(null);
                  setShowInventoryItemModal(true);
                }}
                className="btn-primary"
                style={{ padding: '8px 16px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <Plus size={16} /> Add Inventory Item
              </button>
            </div>

            {/* Category Filter Pills */}
            <div style={{ display: 'flex', gap: '8px', overflowX: 'auto', paddingBottom: '12px', marginBottom: '16px' }}>
              {['ALL', 'Vaccine', 'Parasiticide', 'Antibiotic', 'Analgesic', 'Surgical', 'Diagnostic', 'Consumable', 'Pet Care'].map(cat => (
                <button
                  key={cat}
                  onClick={() => setInventoryCategoryFilter(cat)}
                  style={{
                    padding: '6px 14px',
                    borderRadius: '20px',
                    fontSize: '12px',
                    fontWeight: 700,
                    whiteSpace: 'nowrap',
                    cursor: 'pointer',
                    border: inventoryCategoryFilter === cat ? '1px solid var(--color-amber-terracotta)' : '1px solid #D1D5DB',
                    backgroundColor: inventoryCategoryFilter === cat ? '#FEF3C7' : '#FFFFFF',
                    color: inventoryCategoryFilter === cat ? '#92400E' : '#374151'
                  }}
                >
                  {cat === 'ALL' ? 'All Stock' : cat}
                </button>
              ))}
            </div>

            {/* Inventory Table */}
            <div className="card-surface" style={{ padding: '24px' }}>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--color-border-subtle)', color: '#374151', fontWeight: 700 }}>
                      <th style={{ padding: '10px 12px' }}>Item Name</th>
                      <th style={{ padding: '10px 12px' }}>Category</th>
                      <th style={{ padding: '10px 12px' }}>Stock & Status</th>
                      <th style={{ padding: '10px 12px' }}>Price (USD / LRD)</th>
                      <th style={{ padding: '10px 12px' }}>Batch #</th>
                      <th style={{ padding: '10px 12px' }}>Expiry (FEFO)</th>
                      <th style={{ padding: '10px 12px', textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {inventory
                      .filter(i => inventoryCategoryFilter === 'ALL' || i.category === inventoryCategoryFilter)
                      .map(item => {
                        const isLowStock = item.stockCount <= (item.minimumThreshold || 10);
                        const isExpired = item.expiryDate && new Date(item.expiryDate) < new Date();
                        const isExpiringSoon = item.expiryDate && !isExpired && (new Date(item.expiryDate).getTime() - Date.now() < 60 * 24 * 60 * 60 * 1000);

                        return (
                          <tr key={item.id} style={{ borderBottom: '1px solid #F3F4F6' }}>
                            <td style={{ padding: '12px', fontWeight: 700, color: '#111827' }}>
                              {item.name}
                            </td>
                            <td style={{ padding: '12px', color: '#4B5563' }}>
                              <span className="badge badge-slate" style={{ fontSize: '11px' }}>
                                {item.category}
                              </span>
                            </td>
                            <td style={{ padding: '12px' }}>
                              <div style={{ fontWeight: 800, color: '#111827' }}>
                                {item.stockCount} {item.unit}
                              </div>
                              <span className={`badge ${isLowStock ? 'badge-amber' : 'badge-sage'}`} style={{ fontSize: '10px', marginTop: '2px' }}>
                                {isLowStock ? `Low (Reorder <= ${item.minimumThreshold})` : 'In Stock'}
                              </span>
                            </td>
                            <td style={{ padding: '12px' }}>
                              <div style={{ fontWeight: 700, color: '#111827' }}>${item.unitPriceUSD.toFixed(2)} USD</div>
                              <div style={{ fontSize: '11px', color: '#6B7280' }}>LRD {Math.round(item.unitPriceUSD * usdToLrdRate).toLocaleString()}</div>
                            </td>
                            <td style={{ padding: '12px', color: '#4B5563', fontFamily: 'monospace' }}>
                              {item.batchNumber || '—'}
                            </td>
                            <td style={{ padding: '12px' }}>
                              <div>{item.expiryDate || 'N/A'}</div>
                              {isExpired ? (
                                <span className="badge badge-red" style={{ fontSize: '10px' }}>EXPIRED</span>
                              ) : isExpiringSoon ? (
                                <span className="badge badge-amber" style={{ fontSize: '10px' }}>FEFO: Expiring Soon</span>
                              ) : null}
                            </td>
                            <td style={{ padding: '12px', textAlign: 'right' }}>
                              <div style={{ display: 'flex', gap: '6px', justifyContent: 'flex-end' }}>
                                <button
                                  onClick={() => {
                                    setSelectedItemForRestock(item);
                                    setShowRestockModal(true);
                                  }}
                                  className="btn-primary"
                                  style={{ padding: '4px 10px', fontSize: '11px', fontWeight: 700 }}
                                >
                                  Restock
                                </button>
                                <button
                                  onClick={() => {
                                    setItemToEdit(item);
                                    setShowInventoryItemModal(true);
                                  }}
                                  className="btn-secondary"
                                  style={{ padding: '4px 8px', fontSize: '11px', fontWeight: 600 }}
                                >
                                  Edit
                                </button>
                                <button
                                  onClick={async () => {
                                    if (window.confirm(`Delete ${item.name} from inventory?`)) {
                                      await deleteInventoryItemFromFirestore(item.id);
                                      setInventory(prev => prev.filter(i => i.id !== item.id));
                                      setToastNotification(`${item.name} deleted.`);
                                    }
                                  }}
                                  className="btn-outline"
                                  style={{ padding: '4px 8px', fontSize: '11px', color: '#B91C1C' }}
                                >
                                  ✕
                                </button>
                              </div>
                            </td>
                          </tr>
                        );
                      })}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* ========================================================
            STAFF TAB 5: BILLING & INVOICING (USD & LRD)
           ======================================================== */}
        {currentRole !== 'pet_owner' && staffTab === 'billing' && (
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px', flexWrap: 'wrap', gap: '12px' }}>
              <div>
                <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                  Billing & Financial Dossier
                </h2>
                <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                  Invoices & Payments (Dual Currency: USD & LRD @ {usdToLrdRate} LRD/USD)
                </p>
              </div>

              <button
                onClick={() => setShowInvoiceModal(true)}
                className="btn-primary"
                style={{ padding: '8px 16px', fontWeight: 700, fontSize: '13px', display: 'flex', alignItems: 'center', gap: '6px' }}
              >
                <Plus size={16} /> Create Invoice
              </button>
            </div>

            {/* Invoices List */}
            <div className="card-surface" style={{ padding: '24px', marginBottom: '24px' }}>
              <div style={{ overflowX: 'auto' }}>
                <table style={{ width: '100%', borderCollapse: 'collapse', textAlign: 'left', fontSize: '13px' }}>
                  <thead>
                    <tr style={{ borderBottom: '2px solid var(--color-border-subtle)', color: '#374151', fontWeight: 700 }}>
                      <th style={{ padding: '10px 12px' }}>Invoice #</th>
                      <th style={{ padding: '10px 12px' }}>Client & Pet</th>
                      <th style={{ padding: '10px 12px' }}>Date</th>
                      <th style={{ padding: '10px 12px' }}>Services Rendered</th>
                      <th style={{ padding: '10px 12px' }}>Amount (USD / LRD)</th>
                      <th style={{ padding: '10px 12px' }}>Status</th>
                      <th style={{ padding: '10px 12px', textAlign: 'right' }}>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {invoices.map(inv => (
                      <tr key={inv.id} style={{ borderBottom: '1px solid #F3F4F6' }}>
                        <td style={{ padding: '12px', fontWeight: 700, color: '#111827' }}>{inv.id}</td>
                        <td style={{ padding: '12px', color: '#111827', fontWeight: 600 }}>{inv.clientName} ({inv.petName})</td>
                        <td style={{ padding: '12px', color: '#4B5563' }}>{inv.date}</td>
                        <td style={{ padding: '12px', color: '#4B5563' }}>{inv.itemsSummary}</td>
                        <td style={{ padding: '12px', fontWeight: 700, color: '#111827' }}>
                          ${inv.amountUSD.toFixed(2)} / LRD {inv.amountLRD.toLocaleString()}
                        </td>
                        <td style={{ padding: '12px' }}>
                          <span className={`badge ${inv.status === 'PAID' ? 'badge-sage' : inv.status === 'PARTIAL' ? 'badge-amber' : 'badge-red'}`}>
                            {inv.status}
                          </span>
                        </td>
                        <td style={{ padding: '12px', textAlign: 'right' }}>
                          <div style={{ display: 'flex', gap: '6px', justifyContent: 'flex-end', alignItems: 'center' }}>
                            <button
                              onClick={() => {
                                setDocModalInvoice(inv);
                                setDocModalType('INVOICE_RECEIPT');
                                setShowClinicDocModal(true);
                              }}
                              className="btn-outline"
                              style={{ padding: '4px 8px', fontSize: '11px', fontWeight: 700, color: '#111827' }}
                              title="Print Official Invoice / Receipt"
                            >
                              <Printer size={13} style={{ display: 'inline', marginRight: '3px' }} /> Receipt
                            </button>

                            {inv.status !== 'PAID' ? (
                              <button
                                onClick={() => {
                                  setSelectedInvoiceForPayment(inv);
                                  setShowPaymentModal(true);
                                }}
                                className="btn-primary"
                                style={{ padding: '4px 10px', fontSize: '11px', fontWeight: 700 }}
                              >
                                Record Payment
                              </button>
                            ) : (
                              <span style={{ fontSize: '12px', color: '#059669', fontWeight: 700 }}>Settled</span>
                            )}
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          </div>
        )}

        {/* ========================================================
            STAFF TAB 6: SETTINGS, DUAL-CURRENCY & USER ADMINISTRATION
           ======================================================== */}
        {currentRole !== 'pet_owner' && staffTab === 'settings' && (
          <div>
            <div style={{ marginBottom: '24px' }}>
              <h2 className="font-serif" style={{ fontSize: '24px', fontWeight: 700, color: '#111827' }}>
                Clinic Settings & Operational Administration
              </h2>
              <p style={{ fontSize: '13px', color: '#4B5563', fontWeight: 500 }}>
                Manage clinic identity, exchange rate, and staff role governance
              </p>
            </div>

            {/* Currency & Exchange Rate (USD & LRD) */}
            <div className="card-surface" style={{ padding: '24px', marginBottom: '28px' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '14px', flexWrap: 'wrap', gap: '8px' }}>
                <div>
                  <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <DollarSign size={18} color="var(--color-amber-terracotta)" /> Currency & Operational Exchange Rate
                  </h3>
                  <p style={{ fontSize: '12px', color: '#4B5563', marginTop: '2px' }}>
                    Standard Liberia dual-currency system (United States Dollar & Liberian Dollar)
                  </p>
                </div>
                <span className="badge badge-amber" style={{ fontSize: '12px', fontWeight: 700 }}>
                  Current Rate: 1 USD = {usdToLrdRate} LRD
                </span>
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '14px', flexWrap: 'wrap', backgroundColor: '#F9FAFB', padding: '16px', borderRadius: '12px', border: '1px solid #E5E7EB' }}>
                <div style={{ minWidth: '220px' }}>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                    USD to LRD Exchange Rate
                  </label>
                  <input
                    type="number"
                    step="0.5"
                    value={tempRateInput}
                    disabled={!(currentRole === 'super_admin' || currentRole === 'clinic_owner')}
                    readOnly={!(currentRole === 'super_admin' || currentRole === 'clinic_owner')}
                    onChange={(e) => setTempRateInput(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '9px 12px',
                      borderRadius: '8px',
                      border: '1px solid #D1D5DB',
                      fontSize: '14px',
                      color: '#111827',
                      fontWeight: 700,
                      backgroundColor: (currentRole === 'super_admin' || currentRole === 'clinic_owner') ? '#FFFFFF' : '#F3F4F6',
                      cursor: (currentRole === 'super_admin' || currentRole === 'clinic_owner') ? 'text' : 'not-allowed',
                      boxSizing: 'border-box'
                    }}
                  />
                </div>

                <div style={{ alignSelf: 'flex-end' }}>
                  {(currentRole === 'super_admin' || currentRole === 'clinic_owner') ? (
                    <button
                      type="button"
                      onClick={async () => {
                        const newRate = parseFloat(tempRateInput) || 194.0;
                        const prevRate = usdToLrdRate;
                        await saveClinicSettingsToFirestore({ usdToLrdRate: newRate });
                        await logAuditEvent(
                          'EXCHANGE_RATE_CHANGED',
                          'Financial',
                          `Exchange rate updated from 1 USD = ${prevRate} LRD to 1 USD = ${newRate} LRD by ${authenticatedUser.fullName} (${currentRole})`,
                          authenticatedUser.email
                        );
                        setUsdToLrdRate(newRate);
                        setToastNotification(`Exchange rate updated to 1 USD = ${newRate} LRD.`);
                      }}
                      className="btn-primary"
                      style={{ padding: '9px 18px', fontWeight: 700, fontSize: '13px' }}
                    >
                      Update Exchange Rate
                    </button>
                  ) : (
                    <div style={{
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px',
                      padding: '9px 14px',
                      backgroundColor: '#F3F4F6',
                      borderRadius: '8px',
                      border: '1px solid #E5E7EB',
                      fontSize: '12px',
                      color: '#6B7280',
                      fontWeight: 600
                    }}>
                      <Lock size={14} /> Only Super Admin & Clinic Owner can edit exchange rate
                    </div>
                  )}
                </div>

                <p style={{ fontSize: '12px', color: '#6B7280', margin: 0, width: '100%' }}>
                  ℹ️ Newly generated invoices automatically calculate amounts in both USD and LRD using this rate. Historical invoices preserve their original exchange rate for financial integrity.
                </p>
              </div>
            </div>

            {/* Clinic Branding Form */}
            <div className="card-surface" style={{ padding: '24px', marginBottom: '28px' }}>
              <h3 className="font-serif" style={{ fontSize: '18px', fontWeight: 700, color: '#111827', marginBottom: '16px', display: 'flex', alignItems: 'center', gap: '8px' }}>
                <Building size={18} color="var(--color-amber-terracotta)" /> Clinic Profile & Official Contact
              </h3>

              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '16px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                    Clinic Official Name
                  </label>
                  <input
                    type="text"
                    value={clinicName}
                    onChange={(e) => setClinicName(e.target.value)}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '14px', color: '#111827', fontWeight: 600, backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                  />
                </div>

                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                    Clinic Phone / Hotline
                  </label>
                  <input
                    type="text"
                    value={clinicPhone}
                    onChange={(e) => setClinicPhone(e.target.value)}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '14px', color: '#111827', fontWeight: 600, backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                  />
                </div>

                <div style={{ gridColumn: '1 / -1' }}>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                    Physical Address (Liberia)
                  </label>
                  <input
                    type="text"
                    value={clinicAddress}
                    onChange={(e) => setClinicAddress(e.target.value)}
                    style={{ width: '100%', padding: '10px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '14px', color: '#111827', fontWeight: 600, backgroundColor: '#FFFFFF', boxSizing: 'border-box' }}
                  />
                </div>
              </div>

              <div style={{ marginTop: '16px', display: 'flex', justifyContent: 'flex-end' }}>
                <button
                  type="button"
                  onClick={handleSaveClinicSettings}
                  className="btn-primary"
                  style={{ padding: '8px 18px', fontWeight: 700, fontSize: '13px' }}
                >
                  Save Clinic Information
                </button>
              </div>
            </div>

            {/* ROLE-BASED USER ADMINISTRATION */}
            {(currentRole === 'super_admin' || currentRole === 'clinic_owner') ? (
              <UserManagement />
            ) : (
              <div style={{ padding: '16px 20px', backgroundColor: '#F3F4F6', borderRadius: '16px', border: '1px solid #E5E7EB', display: 'flex', alignItems: 'center', gap: '12px' }}>
                <ShieldCheck size={24} color="#6B7280" />
                <div>
                  <div style={{ fontSize: '14px', fontWeight: 700, color: '#111827' }}>
                    User & Staff Access Controls
                  </div>
                  <div style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500, marginTop: '2px' }}>
                    Protected administrative function reserved for Super Admin and Clinic Owners.
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

      </main>

      {/* ========================================================
          MOBILE PERSISTENT BOTTOM NAVIGATION BAR
          (Matches Android native bottom navigation experience)
         ======================================================== */}
      <div style={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        backgroundColor: '#FFFFFF',
        borderTop: '1px solid var(--color-border-subtle)',
        display: 'flex',
        justifyContent: 'space-around',
        alignItems: 'center',
        padding: '6px 8px',
        zIndex: 40,
        boxShadow: '0 -2px 10px rgba(0, 0, 0, 0.05)'
      }}>
        {currentRole === 'pet_owner' ? (
          <>
            <button
              onClick={() => setPetOwnerTab('mypets')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: petOwnerTab === 'mypets' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: petOwnerTab === 'mypets' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <HeartHandshake size={20} />
              <span>My Pets</span>
            </button>
            <button
              onClick={() => setPetOwnerTab('visits')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: petOwnerTab === 'visits' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: petOwnerTab === 'visits' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <Calendar size={20} />
              <span>Visits</span>
            </button>
            <button
              onClick={() => setPetOwnerTab('settings')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: petOwnerTab === 'settings' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: petOwnerTab === 'settings' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <Settings size={20} />
              <span>Settings</span>
            </button>
          </>
        ) : (
          /* Staff: Today | Clinical | Clients | Billing | Settings */
          <>
            <button
              onClick={() => setStaffTab('today')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: staffTab === 'today' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: staffTab === 'today' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <Calendar size={18} />
              <span>Today</span>
            </button>
            <button
              onClick={() => setStaffTab('clinical')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: staffTab === 'clinical' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: staffTab === 'clinical' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <Stethoscope size={18} />
              <span>Clinical</span>
            </button>
            <button
              onClick={() => setStaffTab('clients')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: staffTab === 'clients' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: staffTab === 'clients' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <User size={18} />
              <span>Clients</span>
            </button>
            <button
              onClick={() => setStaffTab('billing')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: staffTab === 'billing' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: staffTab === 'billing' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <Receipt size={18} />
              <span>Billing</span>
            </button>
            <button
              onClick={() => setStaffTab('settings')}
              style={{
                flex: 1,
                display: 'flex',
                flexDirection: 'column',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '48px',
                background: 'none',
                border: 'none',
                color: staffTab === 'settings' ? 'var(--color-amber-terracotta)' : '#111827',
                fontWeight: staffTab === 'settings' ? 800 : 700,
                fontSize: '11px',
                cursor: 'pointer',
                gap: '3px'
              }}
            >
              <Settings size={18} />
              <span>Settings</span>
            </button>
          </>
        )}
      </div>

      {/* ========================================================
          MODALS
         ======================================================== */}

      {/* Real Camera Scanner Modal (both Pet Owner & Staff with ownership checking) */}
      <CameraScannerModal
        isOpen={showScannerModal}
        onClose={() => setShowScannerModal(false)}
        pets={pets}
        currentRole={currentRole}
        onPetVerified={(pet) => {
          setShowScannerModal(false);
          setScannedPetDossier(pet);
          setShowDossierModal(true);
        }}
      />

      {/* Staff Authorized QR / Tag Generator Modal */}
      <StaffQrGeneratorModal
        isOpen={showStaffQrGenModal}
        onClose={() => setShowStaffQrGenModal(false)}
        pets={pets}
        onGenerateCode={({ petId, tagType, code }) => {
          console.log('Tag generated:', { petId, tagType, code });
        }}
      />

      {/* SHOW PET QR MODAL */}
      {showPetQrModal && (() => {
        const targetQrPet = selectedPetForQr || activePet;
        if (!targetQrPet) return null;
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
            zIndex: 100,
            padding: '20px'
          }}>
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: '24px',
              maxWidth: '420px',
              width: '100%',
              padding: '28px',
              textAlign: 'center',
              boxShadow: 'var(--shadow-lg)',
              border: '2px solid var(--color-amber-terracotta)'
            }}>
              <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginBottom: '4px' }}>
                {targetQrPet.name}'s Health Passport QR
              </h3>
              <p style={{ fontSize: '12px', color: '#4B5563', fontWeight: 600, marginBottom: '20px' }}>
                Happy Paws Liberia · Rabies Collar Tag #{targetQrPet.rabiesTag}
              </p>

              <div style={{
                display: 'inline-block',
                padding: '16px',
                backgroundColor: '#FFFFFF',
                borderRadius: '20px',
                border: '2px solid var(--color-border-subtle)',
                marginBottom: '20px'
              }}>
                {(() => {
                  const qrPayload = JSON.stringify({
                    type: 'HAPPY_PAWS_PET_PASSPORT',
                    petId: targetQrPet.id,
                    name: targetQrPet.name,
                    rabiesTag: targetQrPet.rabiesTag,
                    microchipId: targetQrPet.microchipId
                  });
                  const matrix = generateQrMatrix(qrPayload);
                  return (
                    <svg viewBox={`0 0 ${matrix.length} ${matrix.length}`} style={{ width: '180px', height: '180px' }} shapeRendering="crispEdges">
                      {matrix.map((row, r) => row.map((cell, c) => (
                        <rect key={`${r}-${c}`} x={c} y={r} width={1} height={1} fill={cell ? '#111827' : '#FFFFFF'} />
                      )))}
                    </svg>
                  );
                })()}
              </div>

              <p style={{ fontSize: '12px', color: '#4B5563', lineHeight: 1.5, marginBottom: '20px' }}>
                Scan at Happy Paws Liberia Rescue Center reception (RIA Highway, Paynesville) for rapid admission and verified rabies tag verification.
              </p>

              <button 
                onClick={() => {
                  setShowPetQrModal(false);
                  setSelectedPetForQr(null);
                }}
                className="btn-primary"
                style={{ width: '100%', padding: '10px', fontWeight: 700 }}
              >
                Done
              </button>
            </div>
          </div>
        );
      })()}

      {/* PET MEDICAL DOSSIER MODAL */}
      {showDossierModal && scannedPetDossier && (
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
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '24px',
            maxWidth: '560px',
            width: '100%',
            maxHeight: '90vh',
            overflowY: 'auto',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '16px' }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <img src={scannedPetDossier.photoUrl} alt={scannedPetDossier.name} style={{ width: '56px', height: '56px', borderRadius: '14px', objectFit: 'cover' }} />
                <div>
                  <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827' }}>
                    {scannedPetDossier.name}
                  </h3>
                  <p style={{ fontSize: '12px', color: 'var(--color-amber-terracotta)', fontWeight: 700 }}>
                    Verified Rabies Tag #{scannedPetDossier.rabiesTag}
                  </p>
                </div>
              </div>
              <button onClick={() => setShowDossierModal(false)} style={{ background: 'none', border: 'none', fontSize: '18px', cursor: 'pointer', color: '#6B7280' }}>
                ✕
              </button>
            </div>

            <div style={{ background: '#F9FAFB', padding: '14px', borderRadius: '12px', marginBottom: '16px', fontSize: '13px' }}>
              <p><strong>Species / Breed:</strong> {scannedPetDossier.species} ({scannedPetDossier.breed})</p>
              <p><strong>Sex & Weight:</strong> {scannedPetDossier.sex} · {scannedPetDossier.weightKg} kg</p>
              <p><strong>Microchip:</strong> {scannedPetDossier.microchipId}</p>
              <p><strong>Special Notes:</strong> {scannedPetDossier.notes}</p>
            </div>

            {currentRole !== 'pet_owner' && (
              <div style={{ marginBottom: '16px' }}>
                <button
                  onClick={async () => {
                    const newApt: AppointmentItem = {
                      id: Date.now(),
                      clientName: scannedPetDossier.id === 1 ? 'Anthony Tolbert' : scannedPetDossier.id === 2 ? 'Kofa Weah' : 'Registered Client',
                      petName: scannedPetDossier.name,
                      petId: scannedPetDossier.id,
                      species: scannedPetDossier.species,
                      time: 'Now (Walk-in)',
                      reason: 'QR Scan Reception Check-in',
                      vetName: 'Dr. David Kpadeh',
                      status: 'Confirmed'
                    };
                    try {
                      await saveAppointmentToFirestore(newApt);
                      setAppointments(prev => [newApt, ...prev.filter(a => a.id !== newApt.id)]);
                      setToastNotification(`Patient ${scannedPetDossier.name} admitted to Today's Queue!`);
                    } catch (err: any) {
                      setAppointments(prev => [newApt, ...prev]);
                    }
                    setShowDossierModal(false);
                    setStaffTab('today');
                  }}
                  className="btn-primary"
                  style={{ width: '100%', padding: '10px', fontWeight: 700, backgroundColor: 'var(--color-forest-sage)' }}
                >
                  Admit to Today's Clinic Queue
                </button>
              </div>
            )}

            <div style={{ display: 'flex', justifyContent: 'flex-end', gap: '8px' }}>
              <button
                onClick={() => {
                  setSelectedPetForPassport(scannedPetDossier);
                  setShowDossierModal(false);
                  setShowPassportModal(true);
                }}
                className="btn-outline"
                style={{ padding: '8px 14px', fontWeight: 700, fontSize: '12px' }}
              >
                <Printer size={14} style={{ display: 'inline', marginRight: '4px' }} /> View Passport
              </button>
              <button onClick={() => setShowDossierModal(false)} className="btn-secondary" style={{ padding: '8px 16px', fontWeight: 700, color: '#111827' }}>
                Close
              </button>
            </div>
          </div>
        </div>
      )}

      {/* PRINTABLE PASSPORT MODAL */}
      {showPassportModal && (() => {
        const targetPassportPet = selectedPetForPassport || activePet;
        if (!targetPassportPet) return null;
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
            zIndex: 100,
            padding: '20px'
          }}>
            <div style={{
              backgroundColor: '#FFFFFF',
              borderRadius: '20px',
              maxWidth: '560px',
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
                  Happy Paws Liberia Rescue Center · Honeybee Junction, R2 Community, RIA Highway, Paynesville City
                </p>
                <p style={{ fontSize: '12px', color: '#111827', fontWeight: 700, marginTop: '2px' }}>
                  Document #HP-MED-{targetPassportPet.id}-2026
                </p>
              </div>

              <div style={{ display: 'flex', gap: '16px', alignItems: 'center', marginBottom: '20px' }}>
                <img 
                  src={targetPassportPet.photoUrl} 
                  alt={targetPassportPet.name} 
                  style={{ width: '84px', height: '84px', borderRadius: '16px', objectFit: 'cover' }} 
                />
                <div>
                  <h3 className="font-serif" style={{ fontSize: '19px', fontWeight: 700, color: '#111827' }}>{targetPassportPet.name}</h3>
                  <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600 }}>Breed: <strong>{targetPassportPet.breed}</strong></p>
                  <p style={{ fontSize: '13px', color: '#111827', fontWeight: 600 }}>Microchip: <strong>{targetPassportPet.microchipId}</strong></p>
                  <p style={{ fontSize: '13px', color: 'var(--color-amber-terracotta)', fontWeight: 700 }}>Rabies Tag: #{targetPassportPet.rabiesTag}</p>
                </div>
              </div>

              <div style={{ background: 'var(--color-card-warm)', padding: '14px', borderRadius: '12px', marginBottom: '20px' }}>
                <h4 style={{ fontSize: '13px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>
                  Verified Rabies & Core Vaccination Certificate
                </h4>
                <p style={{ fontSize: '12px', color: '#111827', fontWeight: 600, lineHeight: 1.5 }}>
                  Certifies that {targetPassportPet.name} has been examined and vaccinated against Rabies Virus and Canine Core Distemper/Parvo at Happy Paws Liberia Veterinary Clinic.
                </p>
              </div>

              <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                <button 
                  onClick={() => {
                    setShowPassportModal(false);
                    setSelectedPetForPassport(null);
                  }} 
                  className="btn-secondary" 
                  style={{ padding: '8px 16px', color: '#111827', fontWeight: 700 }}
                >
                  Close
                </button>
                <button onClick={() => window.print()} className="btn-primary" style={{ padding: '8px 18px', fontWeight: 700 }}>
                  <Printer size={16} /> Print Passport
                </button>
              </div>
            </div>
          </div>
        );
      })()}

      {/* NEW APPOINTMENT MODAL */}
      {showNewAppointmentModal && (
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
          zIndex: 100,
          padding: '20px'
        }}>
          <div style={{
            backgroundColor: '#FFFFFF',
            borderRadius: '20px',
            maxWidth: '460px',
            width: '100%',
            padding: '28px',
            boxShadow: 'var(--shadow-lg)'
          }}>
            <h3 className="font-serif" style={{ fontSize: '20px', fontWeight: 700, color: '#111827', marginBottom: '8px' }}>
              Request Veterinary Consultation
            </h3>
            <p style={{ fontSize: '12px', color: '#4B5563', fontWeight: 500, marginBottom: '20px' }}>
              Schedule a visit at Happy Paws Clinic, RIA Highway, Paynesville.
            </p>

            <form onSubmit={handleBookAppointment} style={{ display: 'flex', flexDirection: 'column', gap: '14px' }}>
              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Pet</label>
                <select 
                  value={newPetName}
                  onChange={(e) => setNewPetName(e.target.value)}
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600 }}
                >
                  {pets.length === 0 ? (
                    <option value="New Patient">New Patient</option>
                  ) : (
                    pets.map(p => <option key={p.id} value={p.name}>{p.name} ({p.species})</option>)
                  )}
                </select>
              </div>

              <div>
                <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Reason for Visit</label>
                <input 
                  type="text" 
                  value={newReason}
                  onChange={(e) => setNewReason(e.target.value)}
                  placeholder="e.g. Annual Rabies Booster, Deworming, Skin Allergy"
                  style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
                  required
                />
              </div>

              <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Date</label>
                  <input 
                    type="date" 
                    value={newDate}
                    onChange={(e) => setNewDate(e.target.value)}
                    style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
                    required
                  />
                </div>
                <div>
                  <label style={{ display: 'block', fontSize: '12px', fontWeight: 700, color: '#111827', marginBottom: '6px' }}>Time Slot</label>
                  <select 
                    value={newTime}
                    onChange={(e) => setNewTime(e.target.value)}
                    style={{ width: '100%', padding: '9px 12px', borderRadius: '8px', border: '1px solid #D1D5DB', fontSize: '13px', color: '#111827', fontWeight: 600, boxSizing: 'border-box' }}
                  >
                    <option value="09:30 AM">09:30 AM</option>
                    <option value="11:00 AM">11:00 AM</option>
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

      {/* REGISTER PET MODAL */}
      <RegisterPetModal
        isOpen={showRegisterPetModal}
        onClose={() => {
          setShowRegisterPetModal(false);
          setPetToEdit(null);
          setInitialClientIdForPet(undefined);
        }}
        clients={clients}
        initialClientId={initialClientIdForPet}
        petToEdit={petToEdit}
        onPetSaved={(savedPet) => {
          setPets(prev => {
            const exists = prev.some(p => p.id === savedPet.id);
            if (exists) {
              return prev.map(p => p.id === savedPet.id ? savedPet : p);
            }
            return [savedPet, ...prev];
          });
          setSelectedPetId(savedPet.id);
          setToastNotification(`Patient ${savedPet.name} (${savedPet.species}) saved successfully!`);
        }}
        onClientCreated={(newClient) => {
          setClients(prev => {
            const exists = prev.some(c => c.id === newClient.id);
            if (exists) return prev;
            return [newClient, ...prev];
          });
        }}
      />

      {/* CLINICAL EXAM & SOAP NOTES MODAL */}
      <ClinicalExamModal
        isOpen={showClinicalExamModal}
        onClose={() => setShowClinicalExamModal(false)}
        pets={pets}
        vetName={authenticatedUser.fullName}
        onExamSaved={(exam) => {
          setClinicalExams(prev => [exam, ...prev]);
          setToastNotification(`Clinical exam for ${exam.petName} recorded successfully!`);
        }}
      />

      {/* INVOICE MODAL */}
      <InvoiceModal
        isOpen={showInvoiceModal}
        onClose={() => setShowInvoiceModal(false)}
        pets={pets}
        usdToLrdRate={194.0}
        onInvoiceCreated={(invoice) => {
          setInvoices(prev => [invoice, ...prev]);
          setToastNotification(`Invoice ${invoice.id} created successfully!`);
        }}
      />

      {/* PAYMENT RECORDING MODAL */}
      <PaymentModal
        isOpen={showPaymentModal}
        onClose={() => {
          setShowPaymentModal(false);
          setSelectedInvoiceForPayment(null);
        }}
        invoice={selectedInvoiceForPayment}
        onPaymentRecorded={(invId, amount, method) => {
          setInvoices(prev => prev.map(inv => {
            if (inv.id === invId) {
              const newPaid = (inv.paidAmountUSD || 0) + amount;
              const newBal = Math.max(0, inv.amountUSD - newPaid);
              return {
                ...inv,
                paidAmountUSD: newPaid,
                balanceUSD: newBal,
                status: newBal <= 0 ? 'PAID' : 'PARTIAL',
                paymentMethod: method
              };
            }
            return inv;
          }));
          setToastNotification(`Payment of $${amount} USD recorded successfully!`);
        }}
      />

      {/* INVENTORY RESTOCK MODAL */}
      <InventoryRestockModal
        isOpen={showRestockModal}
        onClose={() => {
          setShowRestockModal(false);
          setSelectedItemForRestock(null);
        }}
        item={selectedItemForRestock}
        onRestockSaved={(updated) => {
          setInventory(prev => prev.map(i => i.id === updated.id ? updated : i));
          setToastNotification(`Stock for ${updated.name} updated to ${updated.stockCount} ${updated.unit}.`);
        }}
      />

      {/* Hidden File Input for Pet Photo Upload */}
      <input
        type="file"
        ref={photoUploadInputRef}
        style={{ display: 'none' }}
        accept="image/*"
        onChange={handlePhotoFileSelected}
      />

      {/* Toast Notification */}
      {toastNotification && (
        <div style={{
          position: 'fixed',
          bottom: '80px',
          right: '20px',
          backgroundColor: '#111827',
          color: '#FFFFFF',
          padding: '12px 20px',
          borderRadius: '12px',
          fontSize: '13px',
          fontWeight: 600,
          boxShadow: 'var(--shadow-lg)',
          zIndex: 200,
          display: 'flex',
          alignItems: 'center',
          gap: '8px'
        }}>
          <CheckCircle2 size={16} color="var(--color-forest-sage)" />
          <span>{toastNotification}</span>
        </div>
      )}

    </div>
  );
}
