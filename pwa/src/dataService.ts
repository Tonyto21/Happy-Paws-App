import {
  collection,
  doc,
  setDoc,
  addDoc,
  getDocs,
  onSnapshot,
  updateDoc,
  deleteDoc,
  query,
  orderBy,
  serverTimestamp,
  writeBatch
} from 'firebase/firestore';
import { db, auth } from './firebase';
import type { ClinicSettings } from './types';

// Default Clinic Settings with Official Clinic Identity
export const DEFAULT_CLINIC_SETTINGS: ClinicSettings = {
  clinicName: 'Happy Paws Liberia Rescue Center',
  tagline: 'Veterinary Clinic & Pet Health Passport Rescue Center',
  address: 'Honeybee Junction, R2 Community, RIA Highway, Paynesville City, Montserrado County, Liberia',
  city: 'Paynesville City',
  phone: '0881479329',
  emergencyPhone: '0777123456',
  email: 'contact@happypaws-liberia.org',
  website: 'https://happypaws-liberia.org',
  whatsapp: '0881479329',
  operatingHours: 'Monday - Saturday: 8:00 AM - 6:00 PM · Emergency: 24/7',
  currency: 'USD',
  usdToLrdRate: 194.0
};

export interface ClientRecord {
  id: string;
  fullName: string;
  preferredName?: string;
  phone: string;
  email?: string;
  address?: string;
  notes?: string;
  ownerAuthId?: string;
  createdAt?: string;
}

export interface PetRecord {
  id: number;
  clientId: string | number;
  clientName?: string;
  ownerAuthId?: string;
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

export interface Vaccination {
  id: number;
  petName: string;
  petId?: number;
  vaccineName: string;
  dateAdministered: string;
  validUntil: string;
  batchNumber: string;
  vetName: string;
  status: string;
}

export interface Deworming {
  id: number;
  petName: string;
  petId?: number;
  productName: string;
  dateGiven: string;
  nextDueDate: string;
  weightKg: number;
  dosage: string;
}

export interface AppointmentItem {
  id: number;
  clientName: string;
  petName: string;
  petId?: number;
  species: string;
  time: string;
  reason: string;
  vetName: string;
  status: string;
}

export interface ClinicalExaminationRecord {
  id: string;
  petId: number;
  petName: string;
  clientName: string;
  vetName: string;
  date: string;
  presentingComplaint: string;
  relevantHistory: string;
  weightKg: number;
  tempC: string;
  heartRateBpm: string;
  physicalFindings: string;
  diagnosis: string;
  assessmentNotes: string;
  treatment: string;
  medication: string;
  instructions: string;
  followUp: string;
}

export interface PrescriptionItem {
  id: string;
  petId?: number;
  petName: string;
  clientName: string;
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays: number;
  instructions: string;
  vetName: string;
  date: string;
}

export interface InvoiceLineItem {
  description: string;
  qty: number;
  unitPriceUSD: number;
  totalUSD: number;
}

export interface InvoiceItem {
  id: string;
  clientName: string;
  petName: string;
  date: string;
  amountUSD: number;
  amountLRD: number;
  paidAmountUSD: number;
  balanceUSD: number;
  status: 'PAID' | 'PARTIAL' | 'UNPAID';
  paymentMethod?: string;
  itemsSummary: string;
  lineItems?: InvoiceLineItem[];
}

export interface InventoryStock {
  id: number;
  name: string;
  category: string;
  stockCount: number;
  unit: string;
  minimumThreshold: number;
  unitPriceUSD: number;
  batchNumber?: string;
  expiryDate?: string;
}

export interface AuditLogItem {
  id?: string;
  action: string;
  category: string;
  details: string;
  performedBy: string;
  timestamp: string;
}

// Initial Seed Data for Liberia Clinic
export const INITIAL_CLIENTS: ClientRecord[] = [
  {
    id: 'CL-001',
    fullName: 'Anthony Tolbert',
    phone: '0886123456',
    email: 'pet.owner@happypaws-liberia.org',
    address: 'Congo Town, Tubman Boulevard, Monrovia',
    ownerAuthId: 'usr_pet_owner',
    notes: 'Long-time animal welfare advocate and registered pet parent.'
  },
  {
    id: 'CL-002',
    fullName: 'Kofa Weah',
    phone: '0777987654',
    email: 'kofa.weah@gmail.com',
    address: 'Paynesville City, Montserrado County',
    notes: 'Regular visitor for canine checkups and rabies boosters.'
  },
  {
    id: 'CL-003',
    fullName: 'Fatu Kromah',
    phone: '0881239876',
    email: 'fatu.k@gmail.com',
    address: 'Sinkor, Monrovia',
    notes: 'Small-scale farmer and community animal caretaker.'
  }
];

export const INITIAL_PETS: PetRecord[] = [
  {
    id: 1,
    clientId: 'CL-001',
    clientName: 'Anthony Tolbert',
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
    notes: 'Rescued in Paynesville. Friendly, loves belly rubs. All vaccinations up to date.'
  },
  {
    id: 2,
    clientId: 'CL-002',
    clientName: 'Kofa Weah',
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
  },
  {
    id: 3,
    clientId: 'CL-003',
    clientName: 'Fatu Kromah',
    name: 'Milo',
    species: 'Caprine (Goat)',
    breed: 'West African Dwarf Goat',
    sex: 'Intact Male',
    dob: '10 Mar 2023 (1.5 yrs)',
    weightKg: 18.2,
    color: 'White & Brown Pied',
    microchipId: '985141002931995',
    rabiesTag: 'HP-LR-2024-0935',
    photoUrl: 'https://images.unsplash.com/photo-1524024973431-2ad916746881?auto=format&fit=crop&w=400&q=80',
    notes: 'Small ruminant companion. Treated for tick/flea prevention and PPR vaccination.'
  }
];

export const INITIAL_VACCINATIONS: Vaccination[] = [
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
];

export const INITIAL_DEWORMING: Deworming[] = [
  {
    id: 201,
    petName: 'Bella',
    productName: 'Drontal Plus Flavor Tabs (Broad Spectrum)',
    dateGiven: '15 Aug 2024',
    nextDueDate: '15 Nov 2024',
    weightKg: 24.5,
    dosage: '2.5 Tablets'
  },
  {
    id: 202,
    petName: 'Rex',
    productName: 'Drontal Plus Flavor Tabs (Broad Spectrum)',
    dateGiven: '10 Sep 2024',
    nextDueDate: '10 Dec 2024',
    weightKg: 28.0,
    dosage: '3 Tablets'
  }
];

export const INITIAL_APPOINTMENTS: AppointmentItem[] = [
  {
    id: 301,
    clientName: 'Anthony Tolbert',
    petName: 'Bella',
    species: 'Dog',
    time: '09:30 AM',
    reason: 'Routine Health Check & Rabies Booster',
    vetName: 'Dr. David Kpadeh',
    status: 'In Consultation'
  },
  {
    id: 302,
    clientName: 'Kofa Weah',
    petName: 'Rex',
    species: 'Dog',
    time: '11:00 AM',
    reason: 'Skin Itching / Mange Checkup',
    vetName: 'Dr. Sarah Wilson',
    status: 'Confirmed'
  }
];

export const INITIAL_EXAMINATIONS: ClinicalExaminationRecord[] = [
  {
    id: 'EXAM-2024-001',
    petId: 1,
    petName: 'Bella',
    clientName: 'Anthony Tolbert',
    vetName: 'Dr. David Kpadeh, DVM',
    date: '10 Feb 2024',
    presentingComplaint: 'Annual Comprehensive Wellness Examination & Rabies Booster',
    relevantHistory: 'Rescued locally in Paynesville in 2021. Fully vaccinated, no known drug allergies.',
    weightKg: 24.5,
    tempC: '38.6',
    heartRateBpm: '110',
    physicalFindings: 'Bright, alert, responsive. Heart and lungs clear. Mucous membranes pink, CRT < 2s. No external parasites.',
    diagnosis: 'Healthy adult spayed female canine. Routine preventive prophylaxis indicated.',
    assessmentNotes: 'Excellent body condition (BCS 5/9). Teeth clear of calculus.',
    treatment: 'Administered Defensor 3 Rabies booster subcutaneous right hind. Administered DHPP Core vaccine subcutaneous right shoulder.',
    medication: 'Drontal Plus Flavor Tablets (2.5 tabs oral once every 3 months).',
    instructions: 'Monitor for mild injection site tenderness. Next annual booster due February 2025.',
    followUp: '10 Feb 2025'
  }
];

export const INITIAL_INVENTORY: InventoryStock[] = [
  { id: 401, name: 'Rabies Vaccine (Defensor 3)', category: 'Vaccine', stockCount: 42, unit: 'vials', minimumThreshold: 15, unitPriceUSD: 12.0, batchNumber: 'RB-99482', expiryDate: '2025-11-15' },
  { id: 402, name: 'DHPP Canine Core Vaccine', category: 'Vaccine', stockCount: 28, unit: 'doses', minimumThreshold: 10, unitPriceUSD: 15.0, batchNumber: 'DH-4412-V', expiryDate: '2025-08-20' },
  { id: 403, name: 'Drontal Plus Flavor Tablets', category: 'Parasiticide', stockCount: 95, unit: 'tablets', minimumThreshold: 25, unitPriceUSD: 3.5, batchNumber: 'DP-2024-A', expiryDate: '2026-06-30' },
  { id: 404, name: 'Amoxicillin Trihydrate 250mg', category: 'Antibiotic', stockCount: 60, unit: 'capsules', minimumThreshold: 20, unitPriceUSD: 1.5, batchNumber: 'AMX-8812', expiryDate: '2026-02-15' },
  { id: 405, name: 'Sterile Surgical Suture (Vicryl 3-0)', category: 'Surgical', stockCount: 18, unit: 'packs', minimumThreshold: 10, unitPriceUSD: 8.0, batchNumber: 'VIC-30-77', expiryDate: '2027-01-01' }
];

export const INITIAL_INVOICES: InvoiceItem[] = [
  {
    id: 'INV-2024-001',
    clientName: 'Anthony Tolbert',
    petName: 'Bella',
    date: '15 Aug 2024',
    amountUSD: 25.0,
    amountLRD: 4850,
    paidAmountUSD: 25.0,
    balanceUSD: 0.0,
    status: 'PAID',
    paymentMethod: 'Cash (USD)',
    itemsSummary: 'Clinical Wellness Consultation, Drontal Plus Prophylaxis',
    lineItems: [
      { description: 'Clinical Examination & Wellness Check', qty: 1, unitPriceUSD: 15.0, totalUSD: 15.0 },
      { description: 'Drontal Plus Flavor Tabs (Deworming)', qty: 1, unitPriceUSD: 10.0, totalUSD: 10.0 }
    ]
  },
  {
    id: 'INV-2024-002',
    clientName: 'Kofa Weah',
    petName: 'Rex',
    date: '12 Jan 2024',
    amountUSD: 30.0,
    amountLRD: 5820,
    paidAmountUSD: 30.0,
    balanceUSD: 0.0,
    status: 'PAID',
    paymentMethod: 'Lonestar MTN Mobile Money',
    itemsSummary: 'Defensor 3 Rabies Vaccine, Physical Examination',
    lineItems: [
      { description: 'Physical Clinical Examination', qty: 1, unitPriceUSD: 15.0, totalUSD: 15.0 },
      { description: 'Defensor 3 Rabies Vaccine Administration', qty: 1, unitPriceUSD: 15.0, totalUSD: 15.0 }
    ]
  }
];

/**
 * Fast Firestore write with timeout safety.
 * Commits immediately to Firestore persistent local cache.
 * Resolves within 1200ms so the user's UI never hangs, even when offline or during slow network.
 */
export async function fastSetDoc(docRef: any, data: any, options: any = { merge: true }): Promise<void> {
  try {
    const writePromise = setDoc(docRef, data, options);
    await Promise.race([
      writePromise,
      new Promise(resolve => setTimeout(resolve, 1200))
    ]);
  } catch (err) {
    console.warn('fastSetDoc caught warning (queued in persistent cache):', err);
  }
}

export async function fastUpdateDoc(docRef: any, data: any): Promise<void> {
  try {
    const writePromise = updateDoc(docRef, data);
    await Promise.race([
      writePromise,
      new Promise(resolve => setTimeout(resolve, 1200))
    ]);
  } catch (err) {
    // If doc was not yet created on server, fallback to setDoc with merge
    try {
      await fastSetDoc(docRef, data, { merge: true });
    } catch (_) {}
  }
}

export async function fastDeleteDoc(docRef: any): Promise<void> {
  try {
    const deletePromise = deleteDoc(docRef);
    await Promise.race([
      deletePromise,
      new Promise(resolve => setTimeout(resolve, 1200))
    ]);
  } catch (err) {
    console.warn('fastDeleteDoc caught warning:', err);
  }
}

/**
 * Real-time Firestore Subscriptions with local cache & initial seed fallback
 */

export function subscribeClients(callback: (clients: ClientRecord[]) => void): () => void {
  const colRef = collection(db, 'clients');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_CLIENTS);
    } else {
      const list: ClientRecord[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: docSnap.id } as ClientRecord);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Clients subscription snapshot using local fallback:', err);
    callback(INITIAL_CLIENTS);
  });
}

export function subscribePets(callback: (pets: PetRecord[]) => void): () => void {
  const colRef = collection(db, 'pets');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_PETS);
    } else {
      const list: PetRecord[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: Number(docSnap.id) || docSnap.data().id } as PetRecord);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Pets subscription snapshot using local fallback:', err);
    callback(INITIAL_PETS);
  });
}

export function subscribeAppointments(callback: (apts: AppointmentItem[]) => void): () => void {
  const colRef = collection(db, 'appointments');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_APPOINTMENTS);
    } else {
      const list: AppointmentItem[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: Number(docSnap.id) || docSnap.data().id } as AppointmentItem);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Appointments snapshot using local fallback:', err);
    callback(INITIAL_APPOINTMENTS);
  });
}

export function subscribeVaccinations(callback: (vacs: Vaccination[]) => void): () => void {
  const colRef = collection(db, 'vaccinations');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_VACCINATIONS);
    } else {
      const list: Vaccination[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: Number(docSnap.id) || docSnap.data().id } as Vaccination);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Vaccinations snapshot using local fallback:', err);
    callback(INITIAL_VACCINATIONS);
  });
}

export function subscribeDeworming(callback: (dew: Deworming[]) => void): () => void {
  const colRef = collection(db, 'deworming');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_DEWORMING);
    } else {
      const list: Deworming[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: Number(docSnap.id) || docSnap.data().id } as Deworming);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Deworming snapshot using local fallback:', err);
    callback(INITIAL_DEWORMING);
  });
}

export function subscribeClinicalExaminations(callback: (exams: ClinicalExaminationRecord[]) => void): () => void {
  const colRef = collection(db, 'consultations');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_EXAMINATIONS);
    } else {
      const list: ClinicalExaminationRecord[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: docSnap.id } as ClinicalExaminationRecord);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Examinations snapshot using local fallback:', err);
    callback(INITIAL_EXAMINATIONS);
  });
}

export function subscribeInvoices(callback: (inv: InvoiceItem[]) => void): () => void {
  const colRef = collection(db, 'invoices');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_INVOICES);
    } else {
      const list: InvoiceItem[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: docSnap.id } as InvoiceItem);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Invoices snapshot using local fallback:', err);
    callback(INITIAL_INVOICES);
  });
}

export function subscribeInventory(callback: (items: InventoryStock[]) => void): () => void {
  const colRef = collection(db, 'inventory');
  return onSnapshot(colRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.empty) {
      callback(INITIAL_INVENTORY);
    } else {
      const list: InventoryStock[] = [];
      snapshot.forEach((docSnap) => {
        list.push({ ...docSnap.data(), id: Number(docSnap.id) || docSnap.data().id } as InventoryStock);
      });
      callback(list);
    }
  }, (err) => {
    console.warn('Inventory snapshot using local fallback:', err);
    callback(INITIAL_INVENTORY);
  });
}

export function subscribeClinicSettings(callback: (settings: ClinicSettings) => void): () => void {
  const docRef = doc(db, 'settings', 'clinic');
  return onSnapshot(docRef, { includeMetadataChanges: true }, (snapshot) => {
    if (snapshot.exists()) {
      callback({ ...DEFAULT_CLINIC_SETTINGS, ...snapshot.data() } as ClinicSettings);
    } else {
      callback(DEFAULT_CLINIC_SETTINGS);
    }
  }, (err) => {
    console.warn('Settings snapshot using local fallback:', err);
    callback(DEFAULT_CLINIC_SETTINGS);
  });
}

/**
 * Audit log recorder in Firestore
 */
export async function logAuditEvent(action: string, category: string, details: string, performedBy?: string) {
  try {
    const auditCol = collection(db, 'audit_logs');
    await addDoc(auditCol, {
      action,
      category,
      details,
      performedBy: performedBy || auth.currentUser?.email || 'System Staff',
      timestamp: new Date().toISOString()
    });
  } catch (err) {
    console.warn('Audit log write error (saved in offline queue):', err);
  }
}

/**
 * Saves or updates a client record in Firestore
 */
export async function saveClientToFirestore(client: ClientRecord): Promise<void> {
  const clientDocRef = doc(db, 'clients', String(client.id));
  await fastSetDoc(clientDocRef, {
    ...client,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('SAVE_CLIENT', 'Client Registry', `Saved client ${client.fullName} (${client.phone})`);
}

export async function deleteClientFromFirestore(clientId: string | number): Promise<void> {
  const clientDocRef = doc(db, 'clients', String(clientId));
  await fastDeleteDoc(clientDocRef);
  await logAuditEvent('DELETE_CLIENT', 'Client Registry', `Deleted client #${clientId}`);
}

/**
 * Saves or updates a pet record in Firestore
 */
export async function savePetToFirestore(pet: PetRecord): Promise<void> {
  const petDocRef = doc(db, 'pets', String(pet.id));
  await fastSetDoc(petDocRef, {
    ...pet,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('SAVE_PET', 'Pet Registry', `Saved pet ${pet.name} (Rabies Tag #${pet.rabiesTag})`);
}

export async function deletePetFromFirestore(petId: string | number): Promise<void> {
  const petDocRef = doc(db, 'pets', String(petId));
  await fastDeleteDoc(petDocRef);
  await logAuditEvent('DELETE_PET', 'Pet Registry', `Archived/Deleted pet #${petId}`);
}

/**
 * Saves a new appointment to Firestore
 */
export async function saveAppointmentToFirestore(apt: AppointmentItem): Promise<void> {
  const aptDocRef = doc(db, 'appointments', String(apt.id));
  await fastSetDoc(aptDocRef, {
    ...apt,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('APPOINTMENT_UPDATED', 'Appointments', `Appointment for ${apt.petName} set to ${apt.status}`);
}

export async function deleteAppointmentFromFirestore(aptId: number | string): Promise<void> {
  const aptDocRef = doc(db, 'appointments', String(aptId));
  await fastDeleteDoc(aptDocRef);
  await logAuditEvent('DELETE_APPOINTMENT', 'Appointments', `Removed appointment #${aptId}`);
}

/**
 * Saves an immunization record to Firestore
 */
export async function saveVaccinationToFirestore(vac: Vaccination): Promise<void> {
  const vacDocRef = doc(db, 'vaccinations', String(vac.id));
  await fastSetDoc(vacDocRef, {
    ...vac,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('VACCINE_ADMINISTERED', 'Clinical', `Administered ${vac.vaccineName} to ${vac.petName} (Batch ${vac.batchNumber})`);
}

/**
 * Saves a deworming prophylaxis record to Firestore
 */
export async function saveDewormingToFirestore(dew: Deworming): Promise<void> {
  const dewDocRef = doc(db, 'deworming', String(dew.id));
  await fastSetDoc(dewDocRef, {
    ...dew,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('DEWORMING_ADMINISTERED', 'Clinical', `Administered ${dew.productName} to ${dew.petName} (Dosage ${dew.dosage})`);
}

/**
 * Saves a structured Clinical Examination (History -> Exam -> Assessment -> Treatment Plan)
 */
export async function saveClinicalExamToFirestore(exam: ClinicalExaminationRecord): Promise<void> {
  const examDocRef = doc(db, 'consultations', exam.id);
  await fastSetDoc(examDocRef, {
    ...exam,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('CLINICAL_EXAM', 'Clinical', `Recorded examination for ${exam.petName} (${exam.diagnosis})`);
}

export async function deleteClinicalExamFromFirestore(examId: string): Promise<void> {
  const examDocRef = doc(db, 'consultations', examId);
  await fastDeleteDoc(examDocRef);
  await logAuditEvent('DELETE_CLINICAL_EXAM', 'Clinical', `Deleted examination #${examId}`);
}

/**
 * Saves a Prescription to Firestore
 */
export async function savePrescriptionToFirestore(rx: PrescriptionItem): Promise<void> {
  const rxDocRef = doc(db, 'prescriptions', rx.id);
  await fastSetDoc(rxDocRef, {
    ...rx,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('PRESCRIPTION_DISPENSED', 'Pharmacy', `Prescribed ${rx.medicationName} (${rx.dosage}) for ${rx.petName}`);
}

/**
 * Saves an invoice to Firestore
 */
export async function saveInvoiceToFirestore(inv: InvoiceItem): Promise<void> {
  const invDocRef = doc(db, 'invoices', inv.id);
  await fastSetDoc(invDocRef, {
    ...inv,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('INVOICE_ISSUED', 'Billing', `Invoice ${inv.id} issued for ${inv.clientName} ($${inv.amountUSD} USD / LRD ${inv.amountLRD})`);
}

/**
 * Records a payment against an invoice in Firestore without blocking getDocs
 */
export async function recordInvoicePaymentInFirestore(invoiceId: string, paymentAmountUSD: number, paymentMethod: string, existingInvoice?: InvoiceItem): Promise<void> {
  const invDocRef = doc(db, 'invoices', invoiceId);
  const existingPaid = existingInvoice?.paidAmountUSD || 0;
  const totalAmount = existingInvoice?.amountUSD || paymentAmountUSD;
  const newPaid = existingPaid + paymentAmountUSD;
  const newBalance = Math.max(0, totalAmount - newPaid);
  const newStatus: 'PAID' | 'PARTIAL' = newBalance <= 0 ? 'PAID' : 'PARTIAL';

  await fastUpdateDoc(invDocRef, {
    paidAmountUSD: newPaid,
    balanceUSD: newBalance,
    status: newStatus,
    paymentMethod: paymentMethod,
    updatedAt: serverTimestamp()
  });

  await logAuditEvent('PAYMENT_RECORDED', 'Billing', `Recorded $${paymentAmountUSD} USD payment for invoice ${invoiceId} via ${paymentMethod}`);
}

/**
 * Adjusts inventory stock in Firestore
 */
export async function updateInventoryStockInFirestore(itemId: number, newStock: number, reason: string): Promise<void> {
  const invDocRef = doc(db, 'inventory', String(itemId));
  await fastSetDoc(invDocRef, {
    stockCount: newStock,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('STOCK_ADJUSTMENT', 'Inventory', `Adjusted stock for item #${itemId} to ${newStock} (${reason})`);
}

/**
 * Adds or edits an inventory item in Firestore
 */
export async function saveInventoryItemToFirestore(item: InventoryStock): Promise<void> {
  const invDocRef = doc(db, 'inventory', String(item.id));
  await fastSetDoc(invDocRef, {
    ...item,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('INVENTORY_ITEM_SAVED', 'Inventory', `Saved stock item ${item.name} (${item.stockCount} ${item.unit})`);
}

export async function deleteInventoryItemFromFirestore(itemId: number | string): Promise<void> {
  const invDocRef = doc(db, 'inventory', String(itemId));
  await fastDeleteDoc(invDocRef);
  await logAuditEvent('DELETE_INVENTORY_ITEM', 'Inventory', `Deleted inventory item #${itemId}`);
}

/**
 * Updates clinic settings in Firestore
 */
export async function saveClinicSettingsToFirestore(settings: Partial<ClinicSettings>): Promise<void> {
  const settingsDocRef = doc(db, 'settings', 'clinic');
  await fastSetDoc(settingsDocRef, {
    ...settings,
    updatedAt: serverTimestamp()
  }, { merge: true });

  await logAuditEvent('CLINIC_SETTINGS_UPDATED', 'Administration', `Updated clinic settings: ${settings.clinicName || 'Clinic'}`);
}
