package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.auth.AuthRepository
import com.example.data.auth.AuthState
import com.example.data.local.*
import com.example.model.AppointmentStatus
import com.example.model.ClinicSettings
import com.example.model.UserRole
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class OneTimeCredentialInfo(
    val fullName: String,
    val role: String,
    val username: String,
    val temporaryPassword: String,
    val isReset: Boolean = false
)

class HappyPawsViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    val authRepo = AuthRepository(application, db)

    val authState: StateFlow<AuthState> = authRepo.authState

    val allPets: StateFlow<List<PetEntity>> = db.petDao().getAllPets()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allClients: StateFlow<List<ClientEntity>> = db.clientDao().getAllClients()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAppointments: StateFlow<List<AppointmentEntity>> = db.appointmentDao().getAllAppointments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allVaccinations: StateFlow<List<VaccinationEntity>> = db.vaccinationDao().getAllVaccinations()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvoices: StateFlow<List<InvoiceEntity>> = db.invoiceDao().getAllInvoices()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInventory: StateFlow<List<InventoryEntity>> = db.inventoryDao().getAllInventory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = db.userDao().getAllUsers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _clinicSettings = MutableStateFlow(ClinicSettings())
    val clinicSettings: StateFlow<ClinicSettings> = _clinicSettings.asStateFlow()

    private val _oneTimeCredential = MutableStateFlow<OneTimeCredentialInfo?>(null)
    val oneTimeCredential: StateFlow<OneTimeCredentialInfo?> = _oneTimeCredential.asStateFlow()

    fun updateClinicSettings(name: String, phone: String, address: String) {
        _clinicSettings.value = _clinicSettings.value.copy(
            clinicName = name,
            phone = phone,
            address = address
        )
    }

    fun clearOneTimeCredential() {
        _oneTimeCredential.value = null
    }

    fun createUser(fullName: String, role: UserRole, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = authRepo.createUserByAdmin(fullName, role)
            res.fold(
                onSuccess = { (user, tempPass) ->
                    _oneTimeCredential.value = OneTimeCredentialInfo(
                        fullName = user.fullName,
                        role = user.role.displayName,
                        username = user.email,
                        temporaryPassword = tempPass,
                        isReset = false
                    )
                    onResult(true, null)
                },
                onFailure = { err ->
                    onResult(false, err.message)
                }
            )
        }
    }

    fun resetUserPassword(user: UserEntity, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val res = authRepo.resetPasswordByAdmin(user)
            res.fold(
                onSuccess = { tempPass ->
                    _oneTimeCredential.value = OneTimeCredentialInfo(
                        fullName = user.fullName,
                        role = user.role.displayName,
                        username = user.email,
                        temporaryPassword = tempPass,
                        isReset = true
                    )
                    onResult(true, null)
                },
                onFailure = { err ->
                    onResult(false, err.message)
                }
            )
        }
    }

    fun toggleUserActive(user: UserEntity) {
        viewModelScope.launch {
            authRepo.toggleUserActive(user)
        }
    }

    fun checkInPetToday(petId: Long, clientName: String, petName: String, species: String) {
        viewModelScope.launch {
            val apt = AppointmentEntity(
                clientId = 1,
                clientName = clientName,
                petId = petId,
                petName = petName,
                species = species,
                time = "Now (Walk-in)",
                reason = "QR Scan Reception Check-in",
                vetName = "Dr. David Kpadeh",
                status = AppointmentStatus.CONFIRMED
            )
            db.appointmentDao().insertAppointment(apt)
        }
    }

    fun requestAppointment(petName: String, reason: String, time: String, clientName: String) {
        viewModelScope.launch {
            val apt = AppointmentEntity(
                clientId = 1,
                clientName = clientName,
                petId = 1,
                petName = petName,
                species = "Dog",
                time = time,
                reason = reason,
                vetName = "Dr. David Kpadeh",
                status = AppointmentStatus.REQUESTED
            )
            db.appointmentDao().insertAppointment(apt)
        }
    }

    fun addPet(pet: PetEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = db.petDao().insertPet(pet)
            onComplete?.invoke(id)
        }
    }

    fun updateAppointmentStatus(appointmentId: Long, newStatus: AppointmentStatus) {
        viewModelScope.launch {
            db.appointmentDao().updateAppointmentStatus(appointmentId, newStatus)
        }
    }

    fun recordVaccination(vac: VaccinationEntity) {
        viewModelScope.launch {
            db.vaccinationDao().insertVaccination(vac)
        }
    }

    fun addInvoice(invoice: InvoiceEntity) {
        viewModelScope.launch {
            db.invoiceDao().insertInvoice(invoice)
        }
    }

    fun updateInventoryStock(itemId: Long, newCount: Int) {
        viewModelScope.launch {
            db.inventoryDao().updateStock(itemId, newCount)
        }
    }

    fun addClient(client: ClientEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            val id = db.clientDao().insertClient(client)
            onComplete?.invoke(id)
        }
    }

    fun signOut() {
        authRepo.signOut()
    }
}
