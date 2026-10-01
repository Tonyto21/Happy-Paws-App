package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.HappyPawsRepository
import com.example.model.UserRole
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class HappyPawsUiState(
    val currentRole: UserRole = UserRole.PET_OWNER,
    val selectedPetForClinicalId: Long? = null,
    val clients: List<ClientEntity> = emptyList(),
    val pets: List<PetEntity> = emptyList(),
    val appointments: List<AppointmentEntity> = emptyList(),
    val consultations: List<ConsultationEntity> = emptyList(),
    val vaccinations: List<VaccinationEntity> = emptyList(),
    val dewormings: List<DewormingEntity> = emptyList(),
    val parasiteRecords: List<ParasitePreventionEntity> = emptyList(),
    val prescriptions: List<PrescriptionEntity> = emptyList(),
    val weights: List<WeightRecordEntity> = emptyList(),
    val inventory: List<InventoryEntity> = emptyList(),
    val lowStockInventory: List<InventoryEntity> = emptyList(),
    val invoices: List<InvoiceEntity> = emptyList(),
    val payments: List<PaymentEntity> = emptyList(),
    val enquiries: List<EnquiryEntity> = emptyList(),
    val notifications: List<NotificationEntity> = emptyList(),
    val unreadNotifCount: Int = 0,
    val auditLogs: List<AuditLogEntity> = emptyList(),
    val settings: ClinicSettingsEntity? = null
)

class HappyPawsViewModel(
    private val repository: HappyPawsRepository,
    private val authRepository: com.example.data.auth.AuthRepository
) : ViewModel() {

    val authState = authRepository.authState
    private val _currentRole = MutableStateFlow(UserRole.PET_OWNER)
    private val _selectedPetForClinicalId = MutableStateFlow<Long?>(null)

    init {
        // Automatically sync UI role with AuthState
        viewModelScope.launch {
            authRepository.authState.collect { state ->
                when (state) {
                    is com.example.data.auth.AuthState.AuthenticatedStaff -> {
                        _currentRole.value = state.role
                    }
                    is com.example.data.auth.AuthState.AuthenticatedPetOwner -> {
                        _currentRole.value = UserRole.PET_OWNER
                    }
                    else -> {}
                }
            }
        }
    }

    fun signIn(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.signIn(email, pass)
            result.onSuccess { state ->
                if (state is com.example.data.auth.AuthState.AuthenticatedStaff) {
                    _currentRole.value = state.role
                } else if (state is com.example.data.auth.AuthState.AuthenticatedPetOwner) {
                    _currentRole.value = UserRole.PET_OWNER
                }
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.message)
            }
        }
    }

    fun registerPetOwner(email: String, pass: String, fullName: String, phone: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.registerPetOwner(email, pass, fullName, phone)
            result.onSuccess {
                _currentRole.value = UserRole.PET_OWNER
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.message)
            }
        }
    }

    fun activateStaffInvitation(email: String, pass: String, token: String, fullName: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.activateStaffInvitation(email, pass, token, fullName)
            result.onSuccess { state ->
                if (state is com.example.data.auth.AuthState.AuthenticatedStaff) {
                    _currentRole.value = state.role
                }
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.message)
            }
        }
    }

    fun forgotPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.sendPasswordReset(email)
            result.onSuccess { onResult(true, null) }
                .onFailure { onResult(false, it.message) }
        }
    }

    fun linkClinicRecord(phoneOrEmail: String, claimCode: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.linkExistingClinicRecord(phoneOrEmail, claimCode)
            result.onSuccess {
                _currentRole.value = UserRole.PET_OWNER
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.message)
            }
        }
    }

    fun completeNewPetOwnerOnboarding(pet: PetEntity, phone: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val state = authRepository.authState.value
            val userName = when (state) {
                is com.example.data.auth.AuthState.AuthenticatedProfileIncomplete -> state.displayName
                is com.example.data.auth.AuthState.AuthenticatedPetOwner -> state.displayName
                else -> "Pet Guardian"
            }
            val result = authRepository.completeNewPetOwnerOnboarding(
                fullName = userName,
                phone = phone,
                petName = pet.name,
                species = pet.species,
                breed = pet.breed,
                sex = pet.sex,
                dob = pet.dateOfBirth,
                color = pet.color,
                weightKg = pet.weightKg,
                photoUri = pet.photoUri,
                specialNotes = pet.specialNotes,
                microchipId = pet.microchipId
            )
            result.onSuccess {
                _currentRole.value = UserRole.PET_OWNER
                onResult(true, null)
            }.onFailure { err ->
                onResult(false, err.message)
            }
        }
    }

    fun sendVerificationEmail() {
        viewModelScope.launch {
            authRepository.sendVerificationEmail()
        }
    }

    fun checkEmailVerification(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = authRepository.checkEmailVerification()
            onResult(result.getOrDefault(false))
        }
    }

    fun signOut() {
        authRepository.signOut()
        _currentRole.value = UserRole.PET_OWNER
    }

    fun devSwitchRole(role: UserRole) {
        authRepository.devSwitchRole(role)
        switchRole(role)
    }

    val uiState: StateFlow<HappyPawsUiState> = combine(
        _currentRole,
        _selectedPetForClinicalId,
        repository.allClients,
        repository.allPets,
        repository.allAppointments,
        repository.allConsultations,
        repository.allVaccinations,
        repository.allInventory,
        repository.allInvoices,
        repository.allPayments,
        repository.allEnquiries,
        repository.allNotifications,
        repository.unreadNotificationCount,
        repository.recentAuditLogs,
        repository.clinicSettings
    ) { args: Array<Any?> ->
        val role = args[0] as UserRole
        val petId = args[1] as Long?
        @Suppress("UNCHECKED_CAST") val clients = args[2] as List<ClientEntity>
        @Suppress("UNCHECKED_CAST") val pets = args[3] as List<PetEntity>
        @Suppress("UNCHECKED_CAST") val appts = args[4] as List<AppointmentEntity>
        @Suppress("UNCHECKED_CAST") val consults = args[5] as List<ConsultationEntity>
        @Suppress("UNCHECKED_CAST") val vaccines = args[6] as List<VaccinationEntity>
        @Suppress("UNCHECKED_CAST") val inventory = args[7] as List<InventoryEntity>
        @Suppress("UNCHECKED_CAST") val invoices = args[8] as List<InvoiceEntity>
        @Suppress("UNCHECKED_CAST") val payments = args[9] as List<PaymentEntity>
        @Suppress("UNCHECKED_CAST") val enquiries = args[10] as List<EnquiryEntity>
        @Suppress("UNCHECKED_CAST") val notifications = args[11] as List<NotificationEntity>
        val unread = args[12] as Int
        @Suppress("UNCHECKED_CAST") val auditLogs = args[13] as List<AuditLogEntity>
        val settings = args[14] as ClinicSettingsEntity?

        HappyPawsUiState(
            currentRole = role,
            selectedPetForClinicalId = petId,
            clients = clients,
            pets = pets,
            appointments = appts,
            consultations = consults,
            vaccinations = vaccines,
            dewormings = emptyList(), // Populated in reactive queries
            parasiteRecords = emptyList(),
            prescriptions = emptyList(),
            weights = emptyList(),
            inventory = inventory,
            lowStockInventory = inventory.filter { it.quantity <= it.minStockThreshold },
            invoices = invoices,
            payments = payments,
            enquiries = enquiries,
            notifications = notifications,
            unreadNotifCount = unread,
            auditLogs = auditLogs,
            settings = settings
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HappyPawsUiState()
    )

    // Also observe all dewormings, prescriptions and weights reactively
    val allDewormings = repository.allVaccinations // preventiveDao
    val allPrescriptions = repository.allPrescriptions

    fun switchRole(role: UserRole) {
        _currentRole.value = role
        viewModelScope.launch {
            repository.logAuditAction(
                userName = "Active User",
                role = role.name,
                action = "ROLE_SWITCH",
                entity = "USER_SESSION",
                id = role.name,
                details = "Switched interface experience to ${role.displayName}"
            )
        }
    }

    fun selectPetForClinical(petId: Long) {
        _selectedPetForClinicalId.value = petId
    }

    fun addPetFromOnboarding(pet: PetEntity) {
        viewModelScope.launch {
            val newId = repository.savePet(pet)
            repository.logAuditAction(
                userName = "Pet Owner",
                role = "PET_OWNER",
                action = "CREATE",
                entity = "PET",
                id = newId.toString(),
                details = "Registered companion ${pet.name} (${pet.species}, ${pet.breed})"
            )
            repository.sendNotification(
                NotificationEntity(
                    title = "New Companion Registered",
                    message = "${pet.name} has been enrolled in Happy Paws Liberia registry.",
                    category = "Pet",
                    relatedEntityId = newId
                )
            )
        }
    }

    fun checkInPetToday(petId: Long, clientId: Long, reason: String = "QR Scan Reception Check-in") {
        viewModelScope.launch {
            val todayDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US).format(java.util.Date())
            val timeNow = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.US).format(java.util.Date())
            val appointment = AppointmentEntity(
                petId = petId,
                clientId = clientId,
                appointmentType = "Consultation",
                status = "Checked In",
                scheduledDate = todayDate,
                scheduledTime = timeNow,
                reason = reason,
                assignedStaff = "Dr. Emmanuel Sackor"
            )
            repository.saveAppointment(appointment)
            repository.logAuditAction(
                userName = "Receptionist",
                role = "RECEPTIONIST",
                action = "QR_CHECK_IN",
                entity = "PET",
                id = petId.toString(),
                details = "Scanned pet QR and admitted pet $petId to today's queue"
            )
        }
    }

    fun requestAppointment(petId: Long, reason: String, isEmergency: Boolean) {
        viewModelScope.launch {
            val pet = repository.getPetById(petId)
            val appt = AppointmentEntity(
                petId = petId,
                clientId = pet?.ownerId ?: 1L,
                appointmentType = if (isEmergency) "Emergency" else "Consultation",
                status = "Requested",
                scheduledDate = "Pending Confirmation",
                scheduledTime = "Flexible",
                reason = reason,
                isHomeEmergency = isEmergency
            )
            val apptId = repository.saveAppointment(appt)
            repository.sendNotification(
                NotificationEntity(
                    title = if (isEmergency) "Emergency Request: ${pet?.name}" else "Appointment Requested",
                    message = "Request for ${pet?.name}: $reason",
                    priority = if (isEmergency) "CRITICAL" else "HIGH",
                    category = "Appointment",
                    relatedEntityId = apptId
                )
            )
        }
    }

    fun updateAppointmentStatus(appointmentId: Long, newStatus: String) {
        viewModelScope.launch {
            repository.updateAppointmentStatus(appointmentId, newStatus)
            repository.logAuditAction(
                userName = "Staff Member",
                role = _currentRole.value.name,
                action = "STATUS_UPDATE",
                entity = "APPOINTMENT",
                id = appointmentId.toString(),
                details = "Updated appointment status to $newStatus"
            )
        }
    }

    fun saveConsultation(consultation: ConsultationEntity) {
        viewModelScope.launch {
            val id = repository.saveConsultation(consultation)
            repository.logAuditAction(
                userName = consultation.veterinarian,
                role = "VETERINARIAN",
                action = "CONSULTATION_SAVE",
                entity = "CONSULTATION",
                id = id.toString(),
                details = "Recorded clinical examination: ${consultation.diagnosis}"
            )
        }
    }

    fun recordVaccination(vaccine: VaccinationEntity) {
        viewModelScope.launch {
            repository.recordVaccination(vaccine)
            repository.logAuditAction(
                userName = vaccine.veterinarian,
                role = "VETERINARIAN",
                action = "VACCINE_ADMINISTER",
                entity = "VACCINATION",
                id = vaccine.petId.toString(),
                details = "Administered ${vaccine.vaccineName} (Batch: ${vaccine.batchLotNumber})"
            )
        }
    }

    fun recordDeworming(deworming: DewormingEntity) {
        viewModelScope.launch {
            repository.recordDeworming(deworming)
        }
    }

    fun recordParasite(record: ParasitePreventionEntity) {
        viewModelScope.launch {
            repository.recordParasitePrevention(record)
        }
    }

    fun recordPrescription(prescription: PrescriptionEntity) {
        viewModelScope.launch {
            repository.recordPrescription(prescription)
            repository.logAuditAction(
                userName = prescription.veterinarian,
                role = "VETERINARIAN",
                action = "PRESCRIPTION_ISSUE",
                entity = "PRESCRIPTION",
                id = prescription.petId.toString(),
                details = "Prescribed ${prescription.medicationName} (${prescription.dosage})"
            )
        }
    }

    fun recordWeight(weight: WeightRecordEntity) {
        viewModelScope.launch {
            repository.recordWeight(weight)
            val pet = repository.getPetById(weight.petId)
            if (pet != null) {
                repository.updatePet(pet.copy(weightKg = weight.weightKg))
            }
        }
    }

    fun adjustStock(itemId: Long, delta: Int) {
        viewModelScope.launch {
            repository.adjustInventoryStock(itemId, delta)
            repository.logAuditAction(
                userName = "Staff Member",
                role = _currentRole.value.name,
                action = "STOCK_ADJUST",
                entity = "INVENTORY",
                id = itemId.toString(),
                details = "Adjusted inventory quantity by $delta"
            )
        }
    }

    fun addInventoryItem(item: InventoryEntity) {
        viewModelScope.launch {
            repository.saveInventoryItem(item)
        }
    }

    fun createInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            val id = repository.saveInvoice(invoice)
            repository.logAuditAction(
                userName = "Front Desk",
                role = _currentRole.value.name,
                action = "INVOICE_CREATE",
                entity = "INVOICE",
                id = id.toString(),
                details = "Created invoice ${invoice.invoiceNumber} for $${invoice.totalAmount}"
            )
        }
    }

    fun recordPayment(payment: PaymentEntity) {
        viewModelScope.launch {
            repository.recordPayment(payment)
            val inv = repository.allInvoices.first().find { it.id == payment.invoiceId }
            if (inv != null) {
                val updatedPaid = inv.amountPaid + payment.amount
                val newStatus = if (updatedPaid >= inv.totalAmount) "Paid" else "Partially Paid"
                repository.updateInvoice(inv.copy(amountPaid = updatedPaid, status = newStatus))
            }
            repository.logAuditAction(
                userName = payment.recordedBy,
                role = _currentRole.value.name,
                action = "PAYMENT_RECORD",
                entity = "PAYMENT",
                id = payment.invoiceId.toString(),
                details = "Recorded payment of $${payment.amount} via ${payment.paymentMethod}"
            )
        }
    }

    fun addClientAndPet(client: ClientEntity, pet: PetEntity) {
        viewModelScope.launch {
            val clientId = repository.saveClient(client)
            val newPet = pet.copy(ownerId = clientId)
            repository.savePet(newPet)
            repository.logAuditAction(
                userName = "Front Desk",
                role = _currentRole.value.name,
                action = "CLIENT_PET_REGISTRATION",
                entity = "CLIENT",
                id = clientId.toString(),
                details = "Registered client ${client.fullName} and pet ${pet.name}"
            )
        }
    }

    fun addEnquiry(enquiry: EnquiryEntity) {
        viewModelScope.launch {
            repository.saveEnquiry(enquiry)
        }
    }

    fun updateEnquiryStatus(enquiry: EnquiryEntity, newStatus: String) {
        viewModelScope.launch {
            repository.updateEnquiry(enquiry.copy(status = newStatus))
        }
    }

    fun saveClinicSettings(settings: ClinicSettingsEntity) {
        viewModelScope.launch {
            repository.updateClinicSettings(settings)
            repository.logAuditAction(
                userName = "Super Admin",
                role = _currentRole.value.name,
                action = "SETTINGS_UPDATE",
                entity = "CLINIC_SETTINGS",
                id = "1",
                details = "Updated clinic contact and branding parameters"
            )
        }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch {
            repository.markAllNotificationsAsRead()
        }
    }
}
