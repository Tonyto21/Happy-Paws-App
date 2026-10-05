package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.UserRole
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClientEntity::class,
        PetEntity::class,
        AppointmentEntity::class,
        VaccinationEntity::class,
        InvoiceEntity::class,
        InventoryEntity::class,
        UserEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun petDao(): PetDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun vaccinationDao(): VaccinationDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun inventoryDao(): InventoryDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "happypaws_database"
                )
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            CoroutineScope(Dispatchers.IO).launch {
                                populateInitialData(getInstance(context))
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun populateInitialData(db: AppDatabase) {
            // Initial Client
            val clientId = db.clientDao().insertClient(
                ClientEntity(
                    id = 1,
                    fullName = "Anthony Tolbert",
                    phone = "0881479329",
                    email = "pet.owner@happypaws-liberia.org",
                    firebaseAuthUid = "uid_pet_owner"
                )
            )

            // Initial Pets
            db.petDao().insertPet(
                PetEntity(
                    id = 1,
                    ownerClientId = clientId,
                    name = "Bella",
                    species = "Canine (Dog)",
                    breed = "African Boerboel Mix",
                    sex = "Spayed Female",
                    dob = "12 Jan 2021 (3.5 yrs)",
                    weightKg = 24.5,
                    color = "Brindle / Golden Tan",
                    microchipId = "985141002931882",
                    rabiesTag = "HP-LR-2024-0884",
                    photoUrl = "https://images.unsplash.com/photo-1543466835-00a7907e9de1?auto=format&fit=crop&w=400&q=80",
                    notes = "Rescued in Congo Town. Friendly, loves belly rubs. All vaccinations up to date."
                )
            )

            db.petDao().insertPet(
                PetEntity(
                    id = 2,
                    ownerClientId = 2,
                    name = "Rex",
                    species = "Canine (Dog)",
                    breed = "German Shepherd Mix",
                    sex = "Neutered Male",
                    dob = "05 Aug 2022 (2 yrs)",
                    weightKg = 28.0,
                    color = "Black & Tan",
                    microchipId = "985141002931990",
                    rabiesTag = "HP-LR-2024-0912",
                    photoUrl = "https://images.unsplash.com/photo-1589941013453-ec89f33b5e95?auto=format&fit=crop&w=400&q=80",
                    notes = "Active guard dog. Regular deworming completed."
                )
            )

            // Initial Vaccinations
            db.vaccinationDao().insertVaccination(
                VaccinationEntity(
                    petId = 1,
                    petName = "Bella",
                    vaccineName = "Rabies (Defensor 3)",
                    dateAdministered = "15 Nov 2023",
                    validUntil = "15 Nov 2024",
                    batchNumber = "RB-99482-EXP25",
                    vetName = "Dr. David Kpadeh, DVM"
                )
            )
            db.vaccinationDao().insertVaccination(
                VaccinationEntity(
                    petId = 1,
                    petName = "Bella",
                    vaccineName = "DHPP Canine Core Combo",
                    dateAdministered = "10 Feb 2024",
                    validUntil = "10 Feb 2025",
                    batchNumber = "DH-4412-V",
                    vetName = "Dr. David Kpadeh, DVM"
                )
            )
            db.vaccinationDao().insertVaccination(
                VaccinationEntity(
                    petId = 2,
                    petName = "Rex",
                    vaccineName = "Rabies (Defensor 3)",
                    dateAdministered = "12 Jan 2024",
                    validUntil = "12 Jan 2025",
                    batchNumber = "RB-99510-EXP25",
                    vetName = "Dr. Sarah Wilson, DVM"
                )
            )

            // Initial Appointments for Today's Triage
            db.appointmentDao().insertAppointment(
                AppointmentEntity(
                    clientId = 1,
                    clientName = "Anthony Tolbert",
                    petId = 1,
                    petName = "Bella",
                    species = "Canine (Dog)",
                    time = "09:30 AM",
                    reason = "Routine Health Check & Rabies Booster",
                    vetName = "Dr. David Kpadeh",
                    status = com.example.model.AppointmentStatus.IN_CONSULTATION
                )
            )
            db.appointmentDao().insertAppointment(
                AppointmentEntity(
                    clientId = 2,
                    clientName = "Kofa Weah",
                    petId = 2,
                    petName = "Rex",
                    species = "Canine (Dog)",
                    time = "11:00 AM",
                    reason = "Skin Itching / Mange Follow-up",
                    vetName = "Dr. Sarah Wilson",
                    status = com.example.model.AppointmentStatus.CONFIRMED
                )
            )

            // Initial Invoices
            db.invoiceDao().insertInvoice(
                InvoiceEntity(
                    invoiceNumber = "INV-2024-001",
                    clientName = "Anthony Tolbert",
                    petName = "Bella",
                    date = "15 Aug 2024",
                    amountUSD = 25.0,
                    amountLRD = 4850.0,
                    itemsSummary = "Clinical Consultation, Drontal Deworming"
                )
            )

            // Initial Inventory
            db.inventoryDao().insertInventory(
                InventoryEntity(name = "Rabies Vaccine (Defensor 3)", category = "Vaccine", stockCount = 42, unit = "vials", minimumThreshold = 15, unitPriceUSD = 12.0)
            )
            db.inventoryDao().insertInventory(
                InventoryEntity(name = "DHPP Canine Core Vaccine", category = "Vaccine", stockCount = 28, unit = "doses", minimumThreshold = 10, unitPriceUSD = 15.0)
            )
            db.inventoryDao().insertInventory(
                InventoryEntity(name = "Drontal Plus Flavor Tablets", category = "Parasiticide", stockCount = 95, unit = "tablets", minimumThreshold = 25, unitPriceUSD = 3.5)
            )

            // Initial 5 test user accounts
            db.userDao().insertUser(UserEntity(uid = "uid_super_admin", email = "super.admin@happypaws-liberia.org", fullName = "Super Admin (Clinic Owner)", role = UserRole.SUPER_ADMIN))
            db.userDao().insertUser(UserEntity(uid = "uid_clinic_owner", email = "clinic.owner@happypaws-liberia.org", fullName = "Dr. David Kpadeh", role = UserRole.CLINIC_OWNER))
            db.userDao().insertUser(UserEntity(uid = "uid_vet", email = "veterinarian@happypaws-liberia.org", fullName = "Dr. Sarah Wilson", role = UserRole.VETERINARIAN))
            db.userDao().insertUser(UserEntity(uid = "uid_receptionist", email = "receptionist@happypaws-liberia.org", fullName = "Marie Dennis", role = UserRole.RECEPTIONIST))
            db.userDao().insertUser(UserEntity(uid = "uid_pet_owner", email = "pet.owner@happypaws-liberia.org", fullName = "Anthony Tolbert", role = UserRole.PET_OWNER))
        }
    }
}
