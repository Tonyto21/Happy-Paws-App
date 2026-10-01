package com.example.model

enum class UserRole(val displayName: String, val description: String) {
    SUPER_ADMIN("Super Admin", "Full system control, roles, clinic branding, audit logs"),
    CLINIC_OWNER("Clinic Owner", "Business management, billing, reports, staff overview"),
    VETERINARIAN("Veterinarian", "Clinical workspace, consultations, prescriptions, passport"),
    RECEPTIONIST("Receptionist", "Client check-in, appointments, CRM enquiries, billing"),
    PET_OWNER("Pet Owner", "Pet profiles, health passport, appointments, weight, vaccines")
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

fun UserRole.hasPermission(permission: Permission): Boolean {
    return when (this) {
        UserRole.SUPER_ADMIN -> true
        UserRole.CLINIC_OWNER -> permission != Permission.FINALIZE_CONSULTATION && permission != Permission.MANAGE_STAFF
        UserRole.VETERINARIAN -> permission in listOf(
            Permission.VIEW_RECORDS,
            Permission.CREATE_RECORDS,
            Permission.EDIT_RECORDS,
            Permission.FINALIZE_CONSULTATION,
            Permission.DISPENSE_INVENTORY
        )
        UserRole.RECEPTIONIST -> permission in listOf(
            Permission.VIEW_RECORDS,
            Permission.CREATE_RECORDS,
            Permission.EDIT_RECORDS,
            Permission.MANAGE_BILLING,
            Permission.DISPENSE_INVENTORY
        )
        UserRole.PET_OWNER -> permission in listOf(
            Permission.VIEW_RECORDS,
            Permission.CREATE_RECORDS
        )
    }
}

enum class AppointmentType(val displayName: String) {
    CONSULTATION("Consultation"),
    VACCINATION("Vaccination"),
    SURGERY("Surgery"),
    FOLLOW_UP("Follow-up"),
    GROOMING("Grooming"),
    DEWORMING("Deworming"),
    PREVENTIVE("Preventive Care"),
    HOME_VISIT("Home Visit"),
    EMERGENCY("Emergency"),
    OTHER("Other")
}

enum class AppointmentStatus(val displayName: String) {
    REQUESTED("Requested"),
    PENDING("Pending"),
    CONFIRMED("Confirmed"),
    CHECKED_IN("Checked In"),
    IN_PROGRESS("In Progress"),
    COMPLETED("Completed"),
    CANCELLED("Cancelled"),
    NO_SHOW("No Show")
}

enum class InvoiceStatus(val displayName: String) {
    DRAFT("Draft"),
    ISSUED("Issued"),
    PARTIALLY_PAID("Partially Paid"),
    PAID("Paid"),
    OVERDUE("Overdue"),
    CANCELLED("Cancelled")
}

enum class PaymentMethod(val displayName: String) {
    CASH("Cash"),
    MOBILE_MONEY("Mobile Money (MTN/Orange)"),
    BANK_TRANSFER("Bank Transfer"),
    CARD("Debit/Credit Card"),
    OTHER("Other")
}

enum class EnquirySource(val displayName: String) {
    WHATSAPP("WhatsApp"),
    PHONE("Phone Call"),
    WALK_IN("Walk-in"),
    FACEBOOK("Facebook"),
    INSTAGRAM("Instagram"),
    WEBSITE("Website"),
    REFERRAL("Referral"),
    OTHER("Other")
}

enum class EnquiryStatus(val displayName: String) {
    NEW("New"),
    CONTACTED("Contacted"),
    FOLLOW_UP("Follow-up Needed"),
    CONVERTED("Converted to Appointment"),
    CLOSED("Closed"),
    LOST("Lost")
}

enum class NotificationPriority {
    LOW, NORMAL, HIGH, CRITICAL
}
