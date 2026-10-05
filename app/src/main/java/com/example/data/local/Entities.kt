package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.model.AppointmentStatus
import com.example.model.InvoiceStatus
import com.example.model.UserRole

@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val preferredName: String = "",
    val phone: String,
    val email: String,
    val firebaseAuthUid: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerClientId: Long,
    val name: String,
    val species: String,
    val breed: String,
    val sex: String,
    val dob: String,
    val weightKg: Double,
    val color: String,
    val microchipId: String,
    val rabiesTag: String,
    val photoUrl: String = "",
    val notes: String = ""
)

@Entity(tableName = "appointments")
data class AppointmentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val clientId: Long,
    val clientName: String,
    val petId: Long,
    val petName: String,
    val species: String,
    val time: String,
    val reason: String,
    val vetName: String,
    val status: AppointmentStatus = AppointmentStatus.CONFIRMED,
    val date: String = "2026-10-02"
)

@Entity(tableName = "vaccinations")
data class VaccinationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val petId: Long,
    val petName: String,
    val vaccineName: String,
    val dateAdministered: String,
    val validUntil: String,
    val batchNumber: String,
    val vetName: String,
    val status: String = "Up to Date"
)

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceNumber: String,
    val clientName: String,
    val petName: String,
    val date: String,
    val amountUSD: Double,
    val amountLRD: Double,
    val status: InvoiceStatus = InvoiceStatus.PAID,
    val itemsSummary: String
)

@Entity(tableName = "inventory")
data class InventoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val stockCount: Int,
    val unit: String,
    val minimumThreshold: Int,
    val unitPriceUSD: Double
)

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val uid: String,
    val email: String,
    val fullName: String,
    val role: UserRole,
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
