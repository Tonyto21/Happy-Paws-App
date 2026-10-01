package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val preferredName: String = "",
    val phone: String,
    val altPhone: String = "",
    val email: String = "",
    val address: String = "",
    val notes: String = "",
    val status: String = "Active",
    val firebaseAuthUid: String = "",
    val claimCode: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerId: Long,
    val name: String,
    val species: String, // Dog, Cat, Rabbit, Bird, etc.
    val breed: String,
    val sex: String, // Male, Female, Neutered Male, Spayed Female
    val dateOfBirth: String = "", // e.g. "2024-03-12"
    val ageDisplay: String = "",
    val color: String = "",
    val weightKg: Double = 0.0,
    val microchipId: String = "",
    val photoUri: String = "",
    val specialNotes: String = "",
    val isRescuePet: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val clientId: Long,
    val appointmentType: String, // Consultation, Vaccination, etc.
    val status: String, // Requested, Confirmed, Checked In, In Progress, Completed, Cancelled
    val scheduledDate: String, // "2026-09-28"
    val scheduledTime: String, // "10:30 AM"
    val reason: String,
    val assignedStaff: String = "Dr. Emmanuel Sackor",
    val isHomeEmergency: Boolean = false,
    val locationNotes: String = "",
    val homeServiceFee: Double = 0.0,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "consultations")
data class ConsultationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val appointmentId: Long = 0,
    val veterinarian: String,
    val date: String,
    val reasonForVisit: String,
    val symptoms: String,
    val clinicalFindings: String,
    val diagnosis: String,
    val treatmentSummary: String,
    val followUpDate: String = "",
    val isFinalized: Boolean = false,
    val finalizedAt: Long = 0L,
    val isOwnerVisible: Boolean = true,
    val internalNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val vaccineName: String,
    val administeredDate: String,
    val nextDueDate: String,
    val batchLotNumber: String = "",
    val manufacturer: String = "",
    val veterinarian: String = "Dr. Emmanuel Sackor",
    val notes: String = "",
    val isCertified: Boolean = true
)

@Entity(tableName = "deworming_records")
data class DewormingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val productName: String,
    val dosage: String,
    val weightKg: Double,
    val administeredDate: String,
    val nextDueDate: String,
    val veterinarian: String = "Dr. Emmanuel Sackor",
    val notes: String = ""
)

@Entity(tableName = "parasite_records")
data class ParasitePreventionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val productName: String,
    val targetType: String, // Flea, Tick, Heartworm, Broad
    val administeredDate: String,
    val nextDueDate: String,
    val notes: String = ""
)

@Entity(tableName = "prescriptions")
data class PrescriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val consultationId: Long = 0,
    val medicationName: String,
    val dosage: String,
    val frequency: String,
    val route: String, // Oral, Topical, Subcutaneous, Ophthalmic
    val duration: String,
    val quantity: String,
    val instructions: String,
    val startDate: String,
    val endDate: String = "",
    val veterinarian: String = "Dr. Emmanuel Sackor",
    val status: String = "Active" // Active, Completed, Discontinued
)

@Entity(tableName = "weight_records")
data class WeightRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val weightKg: Double,
    val recordedDate: String,
    val notes: String = ""
)

@Entity(tableName = "inventory_items")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String, // Vaccine, Antibiotic, Dewormer, Parasiticide, Surgical, Consumable, Food
    val sku: String,
    val quantity: Int,
    val unit: String, // vials, tablets, bottles, packs
    val purchaseCost: Double,
    val sellingPrice: Double,
    val supplier: String = "Liberia Pharma Supplies",
    val minStockThreshold: Int = 10,
    val expiryDate: String = "",
    val batchNumber: String = ""
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String, // e.g. "INV-2026-0042"
    val clientId: Long,
    val petId: Long,
    val date: String,
    val dueDate: String,
    val itemsSummary: String, // Description of line items
    val totalAmount: Double,
    val amountPaid: Double,
    val status: String, // Draft, Issued, Partially Paid, Paid, Overdue, Cancelled
    val notes: String = ""
)

@Entity(tableName = "payments")
data class PaymentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val amount: Double,
    val paymentMethod: String, // Cash, Mobile Money (MTN/Orange), Bank Transfer, Card
    val reference: String = "",
    val recordedBy: String = "Front Desk",
    val paymentDate: String,
    val notes: String = ""
)

@Entity(tableName = "enquiries")
data class EnquiryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerName: String,
    val phone: String,
    val petDetails: String,
    val enquiryText: String,
    val source: String, // WhatsApp, Phone Call, Walk-in, Facebook, Instagram
    val status: String, // New, Contacted, Follow-up Needed, Converted to Appointment, Closed
    val assignedStaff: String = "Front Desk",
    val followUpDate: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val priority: String = "NORMAL", // LOW, NORMAL, HIGH, CRITICAL
    val category: String, // Appointment, Vaccine, Inventory, Payment, CRM
    val relatedEntityId: Long = 0,
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userName: String,
    val userRole: String,
    val action: String, // CREATE, UPDATE, FINALIZE, DELETE, LOGIN
    val entityName: String,
    val entityId: String,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "clinic_settings")
data class ClinicSettingsEntity(
    @PrimaryKey val id: Int = 1,
    val clinicName: String = "Happy Paws Liberia",
    val subTitle: String = "Rescue Center & Veterinary Clinic",
    val vetPhoneNumber: String = "0881479329",
    val emergencyPhone: String = "0881479329",
    val email: String = "care@happypawsliberia.org",
    val address: String = "Congo Town, Monrovia, Liberia",
    val currency: String = "USD ($)",
    val emergencyFeeUSD: Double = 35.0,
    val homeVisitTransportFeeUSD: Double = 20.0,
    val clinicHours: String = "Mon - Sat: 8:00 AM - 6:00 PM | Sun: Emergency Only"
)
