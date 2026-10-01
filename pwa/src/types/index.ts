export type UserRole =
  | 'super_admin'
  | 'clinic_owner'
  | 'veterinarian'
  | 'receptionist'
  | 'pet_owner';

export interface StaffRoleDoc {
  role: UserRole;
  active: boolean;
  email: string;
  fullName?: string;
  assignedBy?: string;
  updatedAt?: any;
}

export interface StaffInvitation {
  id?: string;
  token: string;
  email: string;
  role: UserRole;
  status: 'PENDING' | 'ACCEPTED' | 'REVOKED';
  createdAt: any;
  createdByName?: string;
}

export interface Client {
  id: number | string;
  ownerAuthId?: string;
  firebaseAuthUid?: string;
  fullName: string;
  preferredName?: string;
  phone: string;
  email?: string;
  address?: string;
  claimCode?: string;
  isClaimed?: boolean;
  notes?: string;
  cloudId?: string;
  createdAt?: any;
}

export interface Pet {
  id: number | string;
  clientId: number | string;
  ownerId?: number;
  name: string;
  species: 'Dog' | 'Cat' | string;
  breed: string;
  sex: 'Male' | 'Female' | 'Neutered Male' | 'Spayed Female' | string;
  dateOfBirth?: string;
  color?: string;
  weightKg?: number;
  photoUri?: string;
  specialNotes?: string;
  microchipId?: string;
  cloudId?: string;
  createdAt?: any;
}

export type AppointmentStatus =
  | 'Requested'
  | 'Confirmed'
  | 'Checked In'
  | 'In Progress'
  | 'Completed'
  | 'Cancelled';

export interface Appointment {
  id?: string;
  clientId: string | number;
  clientName?: string;
  petId: string | number;
  petName?: string;
  scheduledDate: string;
  scheduledTime?: string;
  reason: string;
  status: AppointmentStatus;
  notes?: string;
  vetStaffId?: string;
  vetStaffName?: string;
  cloudId?: string;
  createdAt?: any;
  updatedAt?: any;
}

export interface Consultation {
  id?: string;
  petId: string | number;
  clientId: string | number;
  vetStaffId: string;
  vetStaffName: string;
  consultDate: string;
  reasonForVisit: string;
  ownerReportSymptoms: string;
  examFindings: string;
  diagnosis: string;
  treatmentPlan: string;
  followUpInstructions?: string;
  followUpDate?: string;
  weightKg?: number;
  isFinalized: boolean;
  isOwnerVisible: boolean;
  createdAt?: any;
  finalizedAt?: any;
}

export interface ConsultationAddendum {
  id?: string;
  consultId: string;
  vetStaffId: string;
  vetStaffName: string;
  note: string;
  isOwnerVisible: boolean;
  createdAt?: any;
}

export interface VaccinationRecord {
  id?: string;
  petId: string | number;
  vaccineName: string;
  dateAdministered: string;
  nextDueDate: string;
  batchLotNumber?: string;
  manufacturer?: string;
  administeredBy: string;
  notes?: string;
  certificateNumber?: string;
  createdAt?: any;
}

export interface DewormingRecord {
  id?: string;
  petId: string | number;
  productName: string;
  dateAdministered: string;
  nextDueDate: string;
  dosage?: string;
  administeredBy: string;
  notes?: string;
  createdAt?: any;
}

export interface ParasiteRecord {
  id?: string;
  petId: string | number;
  productName: string;
  treatmentType: 'Flea & Tick' | 'Heartworm' | 'All-in-one' | string;
  dateAdministered: string;
  nextDueDate: string;
  dosage?: string;
  administeredBy: string;
  notes?: string;
  createdAt?: any;
}

export interface Prescription {
  id?: string;
  petId: string | number;
  clientId?: string | number;
  medicationName: string;
  dosage: string;
  frequency: string;
  durationDays: number;
  instructions: string;
  prescribedBy: string;
  prescribedDate: string;
  status: 'ACTIVE' | 'COMPLETED' | 'CANCELLED';
  createdAt?: any;
}

export interface WeightRecord {
  id?: string;
  petId: string | number;
  weightKg: number;
  recordedDate: string;
  recordedBy: string;
  createdAt?: any;
}

export interface InventoryItem {
  id?: string;
  sku: string;
  name: string;
  category: 'MEDICATION' | 'VACCINE' | 'SUPPLY' | 'PET_CARE' | 'FOOD' | string;
  quantityOnHand: number;
  unit: string;
  purchaseCostUsd: number;
  sellingPriceUsd: number;
  minimumStockAlert: number;
  supplier?: string;
  batchNumber?: string;
  expiryDate?: string;
  updatedAt?: any;
}

export interface InventoryMovement {
  id?: string;
  delta: number;
  previousQty: number;
  newQty: number;
  reason?: string;
  referenceId?: string;
  performedBy?: string;
  timestamp: any;
}

export interface InvoiceItem {
  description: string;
  quantity: number;
  unitPriceUsd: number;
  totalUsd: number;
}

export interface Invoice {
  id?: string;
  clientId: string | number;
  clientName?: string;
  petId?: string | number;
  petName?: string;
  invoiceDate: string;
  dueDate?: string;
  items: InvoiceItem[];
  subtotalUsd: number;
  discountUsd?: number;
  totalUsd: number;
  amountPaid: number;
  balanceDue: number;
  status: 'UNPAID' | 'PARTIAL' | 'PAID' | 'VOID';
  createdAt?: any;
}

export interface Payment {
  id?: string;
  invoiceId: string;
  clientId: string | number;
  amount: number;
  paymentMethod: 'CASH_USD' | 'CASH_LRD' | 'MOMO_LIBERIA' | 'ORANGE_MONEY' | 'CARD' | string;
  paymentReference?: string;
  receivedBy: string;
  paymentDate: string;
  timestamp: any;
}

export interface Enquiry {
  id?: string;
  contactName: string;
  contactPhone: string;
  contactEmail?: string;
  subject: string;
  message: string;
  source: 'ADOPTION' | 'CLINIC_VISIT' | 'SURGERY' | 'GENERAL' | string;
  status: 'NEW' | 'CONTACTED' | 'APPOINTMENT_SET' | 'RESOLVED';
  assignedTo?: string;
  followUpDate?: string;
  notes?: string;
  createdAt?: any;
}

export interface ClinicSettings {
  clinicName: string;
  tagline: string;
  logoUrl?: string;
  address: string;
  city: string;
  phone: string;
  emergencyPhone: string;
  email: string;
  website: string;
  whatsapp: string;
  operatingHours: string;
  currency: 'USD' | 'LRD';
  usdToLrdRate: number;
  updatedAt?: any;
}
