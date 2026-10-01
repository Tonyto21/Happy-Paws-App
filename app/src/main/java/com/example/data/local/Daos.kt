package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY fullName ASC")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: Long): ClientEntity?

    @Query("SELECT * FROM clients WHERE firebaseAuthUid = :uid LIMIT 1")
    suspend fun getClientByAuthUid(uid: String): ClientEntity?

    @Query("SELECT * FROM clients WHERE (phone = :phoneOrEmail OR email = :phoneOrEmail) AND claimCode = :claimCode LIMIT 1")
    suspend fun findClientByClaimCredentials(phoneOrEmail: String, claimCode: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertClient(client: ClientEntity): Long

    @Update
    suspend fun updateClient(client: ClientEntity)

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun getClientCount(): Int
}

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY name ASC")
    fun getAllPets(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId ORDER BY name ASC")
    fun getPetsByOwner(ownerId: Long): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id LIMIT 1")
    suspend fun getPetById(id: Long): PetEntity?

    @Query("SELECT * FROM pets WHERE name LIKE '%' || :query || '%' OR breed LIKE '%' || :query || '%' OR microchipId LIKE '%' || :query || '%'")
    fun searchPets(query: String): Flow<List<PetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity): Long

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Query("DELETE FROM pets WHERE id = :id")
    suspend fun deletePet(id: Long)

    @Query("SELECT COUNT(*) FROM pets")
    suspend fun getPetCount(): Int
}

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments ORDER BY scheduledDate DESC, scheduledTime DESC")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE scheduledDate = :date ORDER BY scheduledTime ASC")
    fun getAppointmentsForDate(date: String): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE petId = :petId ORDER BY scheduledDate DESC")
    fun getAppointmentsForPet(petId: Long): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments WHERE status = :status ORDER BY scheduledDate ASC")
    fun getAppointmentsByStatus(status: String): Flow<List<AppointmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAppointment(appointment: AppointmentEntity): Long

    @Update
    suspend fun updateAppointment(appointment: AppointmentEntity)

    @Query("UPDATE appointments SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun getAppointmentCount(): Int
}

@Dao
interface ConsultationDao {
    @Query("SELECT * FROM consultations WHERE petId = :petId ORDER BY date DESC, id DESC")
    fun getConsultationsForPet(petId: Long): Flow<List<ConsultationEntity>>

    @Query("SELECT * FROM consultations ORDER BY date DESC, id DESC")
    fun getAllConsultations(): Flow<List<ConsultationEntity>>

    @Query("SELECT * FROM consultations WHERE id = :id LIMIT 1")
    suspend fun getConsultationById(id: Long): ConsultationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConsultation(consultation: ConsultationEntity): Long

    @Update
    suspend fun updateConsultation(consultation: ConsultationEntity)

    @Query("UPDATE consultations SET isFinalized = 1, finalizedAt = :timestamp WHERE id = :id")
    suspend fun finalizeConsultation(id: Long, timestamp: Long)
}

@Dao
interface PreventiveDao {
    // Vaccinations
    @Query("SELECT * FROM vaccinations WHERE petId = :petId ORDER BY administeredDate DESC")
    fun getVaccinationsForPet(petId: Long): Flow<List<VaccinationEntity>>

    @Query("SELECT * FROM vaccinations ORDER BY administeredDate DESC")
    fun getAllVaccinations(): Flow<List<VaccinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVaccination(vaccination: VaccinationEntity): Long

    // Deworming
    @Query("SELECT * FROM deworming_records WHERE petId = :petId ORDER BY administeredDate DESC")
    fun getDewormingForPet(petId: Long): Flow<List<DewormingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDeworming(deworming: DewormingEntity): Long

    // Parasite
    @Query("SELECT * FROM parasite_records WHERE petId = :petId ORDER BY administeredDate DESC")
    fun getParasiteRecordsForPet(petId: Long): Flow<List<ParasitePreventionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParasiteRecord(record: ParasitePreventionEntity): Long

    // Prescriptions
    @Query("SELECT * FROM prescriptions WHERE petId = :petId ORDER BY startDate DESC")
    fun getPrescriptionsForPet(petId: Long): Flow<List<PrescriptionEntity>>

    @Query("SELECT * FROM prescriptions ORDER BY startDate DESC")
    fun getAllPrescriptions(): Flow<List<PrescriptionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrescription(prescription: PrescriptionEntity): Long

    @Update
    suspend fun updatePrescription(prescription: PrescriptionEntity)

    // Weights
    @Query("SELECT * FROM weight_records WHERE petId = :petId ORDER BY recordedDate ASC")
    fun getWeightsForPet(petId: Long): Flow<List<WeightRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeight(weight: WeightRecordEntity): Long
}

@Dao
interface InventoryBillingDao {
    // Inventory
    @Query("SELECT * FROM inventory_items ORDER BY name ASC")
    fun getAllInventory(): Flow<List<InventoryEntity>>

    @Query("SELECT * FROM inventory_items WHERE quantity <= minStockThreshold ORDER BY quantity ASC")
    fun getLowStockInventory(): Flow<List<InventoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInventory(item: InventoryEntity): Long

    @Update
    suspend fun updateInventory(item: InventoryEntity)

    @Query("UPDATE inventory_items SET quantity = quantity + :delta WHERE id = :id")
    suspend fun adjustInventoryQuantity(id: Long, delta: Int)

    // Invoices
    @Query("SELECT * FROM invoices ORDER BY id DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE clientId = :clientId ORDER BY id DESC")
    fun getInvoicesForClient(clientId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)

    // Payments
    @Query("SELECT * FROM payments WHERE invoiceId = :invoiceId ORDER BY id DESC")
    fun getPaymentsForInvoice(invoiceId: Long): Flow<List<PaymentEntity>>

    @Query("SELECT * FROM payments ORDER BY id DESC")
    fun getAllPayments(): Flow<List<PaymentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: PaymentEntity): Long
}

@Dao
interface CrmDao {
    // Enquiries
    @Query("SELECT * FROM enquiries ORDER BY createdAt DESC")
    fun getAllEnquiries(): Flow<List<EnquiryEntity>>

    @Query("SELECT * FROM enquiries WHERE status = :status ORDER BY createdAt DESC")
    fun getEnquiriesByStatus(status: String): Flow<List<EnquiryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnquiry(enquiry: EnquiryEntity): Long

    @Update
    suspend fun updateEnquiry(enquiry: EnquiryEntity)

    // Notifications
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadNotificationCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: Long)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // Audit Logs
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
    fun getRecentAuditLogs(): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLogEntity): Long

    // Clinic Settings
    @Query("SELECT * FROM clinic_settings WHERE id = 1 LIMIT 1")
    fun getClinicSettings(): Flow<ClinicSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveClinicSettings(settings: ClinicSettingsEntity)
}
