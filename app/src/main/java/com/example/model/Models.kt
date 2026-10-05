package com.example.model

enum class UserRole(val displayName: String, val description: String) {
    SUPER_ADMIN("Super Admin", "Full system control, roles, clinic branding, user administration"),
    CLINIC_OWNER("Clinic Owner", "Business management, billing, reports, staff overview"),
    VETERINARIAN("Veterinarian", "Clinical workspace, consultations, prescriptions, passport"),
    RECEPTIONIST("Receptionist", "Client check-in, appointments, CRM enquiries, billing"),
    PET_OWNER("Pet Owner", "Pet profiles, health passport, appointments, vaccines")
}

enum class Permission {
    VIEW_RECORDS,
    CREATE_RECORDS,
    EDIT_RECORDS,
    DELETE_RECORDS,
    FINALIZE_CONSULTATION,
    MANAGE_BILLING,
    DISPENSE_INVENTORY,
    MANAGE_STAFF,
    EDIT_CLINIC_SETTINGS
}

data class ClinicSettings(
    val clinicName: String = "Happy Paws Liberia Rescue Center",
    val tagline: String = "Compassionate Veterinary Care & Rabies Prevention in Liberia",
    val phone: String = "0881479329",
    val email: String = "contact@happypaws-liberia.org",
    val address: String = "Tubman Boulevard, Congo Town, Monrovia, Liberia",
    val emergencyPhone: String = "0777123456"
)

enum class AppointmentStatus {
    REQUESTED,
    CONFIRMED,
    IN_CONSULTATION,
    COMPLETED,
    CANCELLED
}

enum class InvoiceStatus {
    PENDING,
    PAID,
    CANCELLED
}

data class PetQrPayload(
    val type: String = "HAPPY_PAWS_PET_PASSPORT",
    val petId: Long,
    val name: String,
    val rabiesTag: String,
    val microchipId: String
)
