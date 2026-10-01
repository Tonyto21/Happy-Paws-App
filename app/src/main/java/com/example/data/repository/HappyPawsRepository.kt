package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

class HappyPawsRepository(
    private val clientDao: ClientDao,
    private val petDao: PetDao,
    private val appointmentDao: AppointmentDao,
    private val consultationDao: ConsultationDao,
    private val preventiveDao: PreventiveDao,
    private val inventoryBillingDao: InventoryBillingDao,
    private val crmDao: CrmDao,
    private val syncOutboxDao: SyncOutboxDao? = null
) {
    // Clients
    val allClients: Flow<List<ClientEntity>> = clientDao.getAllClients()
    suspend fun getClientById(id: Long) = clientDao.getClientById(id)
    suspend fun saveClient(client: ClientEntity): Long {
        val id = clientDao.insertClient(client)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "CLIENT",
                cloudId = "client_$id",
                action = "CREATE",
                payloadJson = """{"fullName":"${client.fullName}","phone":"${client.phone}","email":"${client.email}","address":"${client.address}"}""",
                isSeedData = false
            )
        )
        return id
    }
    suspend fun updateClient(client: ClientEntity) = clientDao.updateClient(client)

    // Pets
    val allPets: Flow<List<PetEntity>> = petDao.getAllPets()
    fun getPetsByOwner(ownerId: Long): Flow<List<PetEntity>> = petDao.getPetsByOwner(ownerId)
    suspend fun getPetById(id: Long) = petDao.getPetById(id)
    fun searchPets(query: String): Flow<List<PetEntity>> = petDao.searchPets(query)
    suspend fun savePet(pet: PetEntity): Long {
        val id = petDao.insertPet(pet)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "PET",
                cloudId = "pet_$id",
                action = "CREATE",
                payloadJson = """{"name":"${pet.name}","species":"${pet.species}","breed":"${pet.breed}","sex":"${pet.sex}","weightKg":${pet.weightKg},"ownerId":${pet.ownerId}}""",
                isSeedData = false
            )
        )
        return id
    }
    suspend fun updatePet(pet: PetEntity) = petDao.updatePet(pet)
    suspend fun deletePet(id: Long) = petDao.deletePet(id)

    // Appointments
    val allAppointments: Flow<List<AppointmentEntity>> = appointmentDao.getAllAppointments()
    fun getAppointmentsForPet(petId: Long): Flow<List<AppointmentEntity>> = appointmentDao.getAppointmentsForPet(petId)
    fun getAppointmentsForDate(date: String): Flow<List<AppointmentEntity>> = appointmentDao.getAppointmentsForDate(date)
    suspend fun saveAppointment(appointment: AppointmentEntity): Long {
        val id = appointmentDao.insertAppointment(appointment)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "APPOINTMENT",
                cloudId = "appt_$id",
                action = "CREATE",
                payloadJson = """{"petId":${appointment.petId},"clientId":${appointment.clientId},"scheduledDate":"${appointment.scheduledDate}","status":"${appointment.status}","reason":"${appointment.reason}"}""",
                isSeedData = false
            )
        )
        return id
    }
    suspend fun updateAppointmentStatus(id: Long, status: String) {
        appointmentDao.updateStatus(id, status)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "APPOINTMENT",
                cloudId = "appt_$id",
                action = "UPDATE",
                payloadJson = """{"status":"$status"}""",
                isSeedData = false
            )
        )
    }
    suspend fun updateAppointment(appointment: AppointmentEntity) = appointmentDao.updateAppointment(appointment)

    // Consultations & Clinical
    val allConsultations: Flow<List<ConsultationEntity>> = consultationDao.getAllConsultations()
    fun getConsultationsForPet(petId: Long): Flow<List<ConsultationEntity>> = consultationDao.getConsultationsForPet(petId)
    suspend fun getConsultationById(id: Long) = consultationDao.getConsultationById(id)
    suspend fun saveConsultation(consultation: ConsultationEntity): Long {
        val id = consultationDao.insertConsultation(consultation)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "CONSULTATION",
                cloudId = "consult_$id",
                action = "CREATE",
                payloadJson = """{"petId":${consultation.petId},"date":"${consultation.date}","reasonForVisit":"${consultation.reasonForVisit.replace("\"", "\\\"")}","diagnosis":"${consultation.diagnosis.replace("\"", "\\\"")}","treatmentSummary":"${consultation.treatmentSummary.replace("\"", "\\\"")}","veterinarian":"${consultation.veterinarian}","isFinalized":${consultation.isFinalized},"isOwnerVisible":${consultation.isOwnerVisible}}""",
                isSeedData = false
            )
        )
        return id
    }
    suspend fun finalizeConsultation(id: Long) {
        consultationDao.finalizeConsultation(id, System.currentTimeMillis())
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "CONSULTATION",
                cloudId = "consult_$id",
                action = "UPDATE",
                payloadJson = """{"isFinalized":true,"finalizedAt":${System.currentTimeMillis()}}""",
                isSeedData = false
            )
        )
    }

    // Preventive Care
    val allVaccinations: Flow<List<VaccinationEntity>> = preventiveDao.getAllVaccinations()
    fun getVaccinationsForPet(petId: Long): Flow<List<VaccinationEntity>> = preventiveDao.getVaccinationsForPet(petId)
    suspend fun recordVaccination(vaccine: VaccinationEntity): Long {
        val id = preventiveDao.insertVaccination(vaccine)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "VACCINATION",
                cloudId = "vaccine_$id",
                action = "CREATE",
                payloadJson = """{"petId":${vaccine.petId},"vaccineName":"${vaccine.vaccineName}","administeredDate":"${vaccine.administeredDate}","nextDueDate":"${vaccine.nextDueDate}","veterinarian":"${vaccine.veterinarian}"}""",
                isSeedData = false
            )
        )
        return id
    }

    fun getDewormingForPet(petId: Long): Flow<List<DewormingEntity>> = preventiveDao.getDewormingForPet(petId)
    suspend fun recordDeworming(deworming: DewormingEntity): Long {
        val id = preventiveDao.insertDeworming(deworming)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "DEWORMING",
                cloudId = "deworm_$id",
                action = "CREATE",
                payloadJson = """{"petId":${deworming.petId},"productName":"${deworming.productName}","administeredDate":"${deworming.administeredDate}","nextDueDate":"${deworming.nextDueDate}"}""",
                isSeedData = false
            )
        )
        return id
    }

    fun getParasiteRecordsForPet(petId: Long): Flow<List<ParasitePreventionEntity>> = preventiveDao.getParasiteRecordsForPet(petId)
    suspend fun recordParasitePrevention(record: ParasitePreventionEntity): Long {
        val id = preventiveDao.insertParasiteRecord(record)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "PARASITE",
                cloudId = "parasite_$id",
                action = "CREATE",
                payloadJson = """{"petId":${record.petId},"productName":"${record.productName}","targetType":"${record.targetType}","administeredDate":"${record.administeredDate}"}""",
                isSeedData = false
            )
        )
        return id
    }

    val allPrescriptions: Flow<List<PrescriptionEntity>> = preventiveDao.getAllPrescriptions()
    fun getPrescriptionsForPet(petId: Long): Flow<List<PrescriptionEntity>> = preventiveDao.getPrescriptionsForPet(petId)
    suspend fun recordPrescription(prescription: PrescriptionEntity): Long {
        val id = preventiveDao.insertPrescription(prescription)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "PRESCRIPTION",
                cloudId = "rx_$id",
                action = "CREATE",
                payloadJson = """{"petId":${prescription.petId},"medicationName":"${prescription.medicationName}","dosage":"${prescription.dosage}","instructions":"${prescription.instructions.replace("\"", "\\\"")}","startDate":"${prescription.startDate}"}""",
                isSeedData = false
            )
        )
        return id
    }

    fun getWeightsForPet(petId: Long): Flow<List<WeightRecordEntity>> = preventiveDao.getWeightsForPet(petId)
    suspend fun recordWeight(weight: WeightRecordEntity): Long {
        val id = preventiveDao.insertWeight(weight)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "WEIGHT",
                cloudId = "weight_$id",
                action = "CREATE",
                payloadJson = """{"petId":${weight.petId},"weightKg":${weight.weightKg},"recordedDate":"${weight.recordedDate}"}""",
                isSeedData = false
            )
        )
        return id
    }

    // Inventory & Billing
    val allInventory: Flow<List<InventoryEntity>> = inventoryBillingDao.getAllInventory()
    val lowStockInventory: Flow<List<InventoryEntity>> = inventoryBillingDao.getLowStockInventory()
    suspend fun saveInventoryItem(item: InventoryEntity): Long {
        val id = inventoryBillingDao.insertInventory(item)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "INVENTORY",
                cloudId = "sku_${item.sku.ifEmpty { id.toString() }}",
                action = "CREATE",
                payloadJson = """{"sku":"${item.sku}","name":"${item.name}","category":"${item.category}","quantity":${item.quantity},"sellingPrice":${item.sellingPrice}}""",
                isSeedData = false
            )
        )
        return id
    }
    suspend fun adjustInventoryStock(id: Long, delta: Int) {
        inventoryBillingDao.adjustInventoryQuantity(id, delta)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "INVENTORY",
                cloudId = "inv_$id",
                action = "STOCK_DELTA",
                payloadJson = """{"itemId":$id,"delta":$delta,"timestamp":${System.currentTimeMillis()}}""",
                isSeedData = false
            )
        )
    }

    val allInvoices: Flow<List<InvoiceEntity>> = inventoryBillingDao.getAllInvoices()
    fun getInvoicesForClient(clientId: Long): Flow<List<InvoiceEntity>> = inventoryBillingDao.getInvoicesForClient(clientId)
    suspend fun saveInvoice(invoice: InvoiceEntity): Long {
        val id = inventoryBillingDao.insertInvoice(invoice)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "INVOICE",
                cloudId = "inv_${invoice.invoiceNumber.ifEmpty { id.toString() }}",
                action = "CREATE",
                payloadJson = """{"clientId":${invoice.clientId},"invoiceNumber":"${invoice.invoiceNumber}","totalAmount":${invoice.totalAmount},"amountPaid":${invoice.amountPaid},"status":"${invoice.status}"}""",
                isSeedData = false
            )
        )
        return id
    }
    suspend fun updateInvoice(invoice: InvoiceEntity) {
        inventoryBillingDao.updateInvoice(invoice)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "INVOICE",
                cloudId = "inv_${invoice.invoiceNumber.ifEmpty { invoice.id.toString() }}",
                action = "UPDATE",
                payloadJson = """{"totalAmount":${invoice.totalAmount},"amountPaid":${invoice.amountPaid},"status":"${invoice.status}"}""",
                isSeedData = false
            )
        )
    }

    val allPayments: Flow<List<PaymentEntity>> = inventoryBillingDao.getAllPayments()
    fun getPaymentsForInvoice(invoiceId: Long): Flow<List<PaymentEntity>> = inventoryBillingDao.getPaymentsForInvoice(invoiceId)
    suspend fun recordPayment(payment: PaymentEntity): Long {
        val id = inventoryBillingDao.insertPayment(payment)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "PAYMENT",
                cloudId = "pay_$id",
                action = "CREATE",
                payloadJson = """{"invoiceId":${payment.invoiceId},"amount":${payment.amount},"paymentMethod":"${payment.paymentMethod}","paymentDate":"${payment.paymentDate}","reference":"${payment.reference}","recordedBy":"${payment.recordedBy}"}""",
                isSeedData = false
            )
        )
        return id
    }

    // CRM, Notifications, Audit, Settings
    val allEnquiries: Flow<List<EnquiryEntity>> = crmDao.getAllEnquiries()
    suspend fun saveEnquiry(enquiry: EnquiryEntity): Long {
        val id = crmDao.insertEnquiry(enquiry)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "ENQUIRY",
                cloudId = "enq_$id",
                action = "CREATE",
                payloadJson = """{"customerName":"${enquiry.customerName}","phone":"${enquiry.phone}","petDetails":"${enquiry.petDetails}","enquiryText":"${enquiry.enquiryText.replace("\"", "\\\"")}","status":"${enquiry.status}"}""",
                isSeedData = false
            )
        )
        return id
    }
    suspend fun updateEnquiry(enquiry: EnquiryEntity) {
        crmDao.updateEnquiry(enquiry)
        syncOutboxDao?.insertMutation(
            SyncOutboxEntity(
                entityType = "ENQUIRY",
                cloudId = "enq_${enquiry.id}",
                action = "UPDATE",
                payloadJson = """{"status":"${enquiry.status}","assignedStaff":"${enquiry.assignedStaff}"}""",
                isSeedData = false
            )
        )
    }

    val allNotifications: Flow<List<NotificationEntity>> = crmDao.getAllNotifications()
    val unreadNotificationCount: Flow<Int> = crmDao.getUnreadNotificationCount()
    suspend fun sendNotification(notification: NotificationEntity): Long = crmDao.insertNotification(notification)
    suspend fun markNotificationAsRead(id: Long) = crmDao.markNotificationAsRead(id)
    suspend fun markAllNotificationsAsRead() = crmDao.markAllNotificationsAsRead()

    val recentAuditLogs: Flow<List<AuditLogEntity>> = crmDao.getRecentAuditLogs()
    suspend fun logAuditAction(userName: String, role: String, action: String, entity: String, id: String, details: String) {
        crmDao.insertAuditLog(
            AuditLogEntity(
                userName = userName,
                userRole = role,
                action = action,
                entityName = entity,
                entityId = id,
                details = details
            )
        )
    }

    val clinicSettings: Flow<ClinicSettingsEntity?> = crmDao.getClinicSettings()
    suspend fun updateClinicSettings(settings: ClinicSettingsEntity) = crmDao.saveClinicSettings(settings)
}
