package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ClientEntity::class,
        PetEntity::class,
        AppointmentEntity::class,
        ConsultationEntity::class,
        VaccinationEntity::class,
        DewormingEntity::class,
        ParasitePreventionEntity::class,
        PrescriptionEntity::class,
        WeightRecordEntity::class,
        InventoryEntity::class,
        InvoiceEntity::class,
        PaymentEntity::class,
        EnquiryEntity::class,
        NotificationEntity::class,
        AuditLogEntity::class,
        ClinicSettingsEntity::class,
        SyncOutboxEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun clientDao(): ClientDao
    abstract fun petDao(): PetDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun consultationDao(): ConsultationDao
    abstract fun preventiveDao(): PreventiveDao
    abstract fun inventoryBillingDao(): InventoryBillingDao
    abstract fun crmDao(): CrmDao
    abstract fun syncOutboxDao(): SyncOutboxDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "happy_paws_liberia.db"
                )
                .fallbackToDestructiveMigration(true)
                .addCallback(DatabaseCallback(scope))
                .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateSeedData(database)
                    }
                }
            }
        }

        suspend fun populateSeedData(db: AppDatabase) {
            val crmDao = db.crmDao()
            val clientDao = db.clientDao()
            val petDao = db.petDao()
            val appointmentDao = db.appointmentDao()
            val consultationDao = db.consultationDao()
            val preventiveDao = db.preventiveDao()
            val invDao = db.inventoryBillingDao()

            // 1. Clinic Settings
            crmDao.saveClinicSettings(
                ClinicSettingsEntity(
                    id = 1,
                    clinicName = "Happy Paws Liberia",
                    subTitle = "Rescue Center & Veterinary Clinic",
                    vetPhoneNumber = "0881479329",
                    emergencyPhone = "0881479329",
                    email = "care@happypawsliberia.org",
                    address = "Tubman Boulevard, Congo Town, Monrovia, Liberia",
                    currency = "USD ($)",
                    emergencyFeeUSD = 35.0,
                    homeVisitTransportFeeUSD = 20.0,
                    clinicHours = "Mon - Sat: 8:00 AM - 6:00 PM | Emergency: 24/7"
                )
            )

            // 2. Clients
            val c1 = clientDao.insertClient(
                ClientEntity(
                    id = 1,
                    fullName = "Anthony Tolbert",
                    preferredName = "Anthony",
                    phone = "0881479329",
                    altPhone = "0770123456",
                    email = "antojayster@gmail.com",
                    address = "Sinkor 12th Street, Monrovia",
                    notes = "Rescue advocate, adopter of Bella",
                    claimCode = "HP-BELLA1"
                )
            )
            val c2 = clientDao.insertClient(
                ClientEntity(
                    id = 2,
                    fullName = "Marie Dennis",
                    preferredName = "Marie",
                    phone = "0776543210",
                    altPhone = "",
                    email = "marie.dennis@yahoo.com",
                    address = "Mamba Point, Monrovia",
                    notes = "Owns Simba and Luna",
                    claimCode = "HP-SIMBA2"
                )
            )
            val c3 = clientDao.insertClient(
                ClientEntity(
                    id = 3,
                    fullName = "Varney Sherman",
                    preferredName = "Varney",
                    phone = "0886987654",
                    altPhone = "",
                    email = "varney.s@hotmail.com",
                    address = "Paynesville, ELWA Junction, Monrovia",
                    notes = "Owner of Kofi",
                    claimCode = "HP-KOFI3"
                )
            )

            // 3. Pets
            val p1 = petDao.insertPet(
                PetEntity(
                    id = 1,
                    ownerId = c1,
                    name = "Bella",
                    species = "Dog",
                    breed = "Golden Retriever / Local Mix",
                    sex = "Female (Spayed)",
                    dateOfBirth = "2023-04-10",
                    ageDisplay = "3 yrs 5 mos",
                    color = "Honey Golden",
                    weightKg = 24.8,
                    microchipId = "HP-LR-89214",
                    specialNotes = "Rescued puppy in 2023. Gentle temperament, loves ear scratches.",
                    isRescuePet = true
                )
            )
            val p2 = petDao.insertPet(
                PetEntity(
                    id = 2,
                    ownerId = c2,
                    name = "Simba",
                    species = "Cat",
                    breed = "Domestic Shorthair",
                    sex = "Male (Neutered)",
                    dateOfBirth = "2024-01-15",
                    ageDisplay = "2 yrs 8 mos",
                    color = "Ginger Tabby",
                    weightKg = 4.7,
                    microchipId = "HP-LR-33219",
                    specialNotes = "Mild seasonal flea dermatitis. Indoor cat.",
                    isRescuePet = false
                )
            )
            val p3 = petDao.insertPet(
                PetEntity(
                    id = 3,
                    ownerId = c3,
                    name = "Kofi",
                    species = "Dog",
                    breed = "African Village Dog (Liberian Hound)",
                    sex = "Male (Intact)",
                    dateOfBirth = "2022-08-20",
                    ageDisplay = "4 yrs 1 mo",
                    color = "Tan & Black Mask",
                    weightKg = 18.5,
                    microchipId = "HP-LR-77102",
                    specialNotes = "Alert guard dog. Up to date on core vaccines.",
                    isRescuePet = true
                )
            )
            val p4 = petDao.insertPet(
                PetEntity(
                    id = 4,
                    ownerId = c2,
                    name = "Luna",
                    species = "Cat",
                    breed = "Calico",
                    sex = "Female (Spayed)",
                    dateOfBirth = "2024-06-01",
                    ageDisplay = "2 yrs 3 mos",
                    color = "White, Orange & Black",
                    weightKg = 3.6,
                    microchipId = "HP-LR-55410",
                    specialNotes = "Adopted from Happy Paws Rescue shelter.",
                    isRescuePet = true
                )
            )

            // 4. Appointments
            appointmentDao.insertAppointment(
                AppointmentEntity(
                    id = 1,
                    petId = p1,
                    clientId = c1,
                    appointmentType = "Vaccination",
                    status = "Checked In",
                    scheduledDate = "Today",
                    scheduledTime = "10:00 AM",
                    reason = "Annual Rabies booster and general health checkup",
                    assignedStaff = "Dr. Emmanuel Sackor"
                )
            )
            appointmentDao.insertAppointment(
                AppointmentEntity(
                    id = 2,
                    petId = p2,
                    clientId = c2,
                    appointmentType = "Consultation",
                    status = "In Progress",
                    scheduledDate = "Today",
                    scheduledTime = "11:30 AM",
                    reason = "Skin scratching & ears check",
                    assignedStaff = "Dr. Emmanuel Sackor"
                )
            )
            appointmentDao.insertAppointment(
                AppointmentEntity(
                    id = 3,
                    petId = p3,
                    clientId = c3,
                    appointmentType = "Follow-up",
                    status = "Confirmed",
                    scheduledDate = "Today",
                    scheduledTime = "02:00 PM",
                    reason = "Post-deworming weight recheck & nutritional advice",
                    assignedStaff = "Dr. Emmanuel Sackor"
                )
            )
            appointmentDao.insertAppointment(
                AppointmentEntity(
                    id = 4,
                    petId = p4,
                    clientId = c2,
                    appointmentType = "Home Visit",
                    status = "Confirmed",
                    scheduledDate = "Tomorrow",
                    scheduledTime = "09:00 AM",
                    reason = "Routine home health evaluation for senior rescue cat",
                    assignedStaff = "Dr. Emmanuel Sackor",
                    isHomeEmergency = true,
                    homeServiceFee = 25.0
                )
            )

            // 5. Clinical: Vaccinations & Deworming
            preventiveDao.insertVaccination(
                VaccinationEntity(
                    id = 1,
                    petId = p1,
                    vaccineName = "Rabies Inactivated (Defensor 3)",
                    administeredDate = "2025-10-15",
                    nextDueDate = "2026-10-15",
                    batchLotNumber = "RB-2025-99A",
                    manufacturer = "Zoetis",
                    veterinarian = "Dr. Emmanuel Sackor",
                    notes = "Administered SQ right hind limb. No adverse reaction observed."
                )
            )
            preventiveDao.insertVaccination(
                VaccinationEntity(
                    id = 2,
                    petId = p1,
                    vaccineName = "DHPP 5-in-1 (Canine Core)",
                    administeredDate = "2025-10-15",
                    nextDueDate = "2026-10-15",
                    batchLotNumber = "DH-8840-X",
                    manufacturer = "Boehringer Ingelheim",
                    veterinarian = "Dr. Emmanuel Sackor",
                    notes = "Protects against Distemper, Hepatitis, Parvovirus, Parainfluenza."
                )
            )
            preventiveDao.insertVaccination(
                VaccinationEntity(
                    id = 3,
                    petId = p2,
                    vaccineName = "FVRCP (Feline Viral Rhinotracheitis)",
                    administeredDate = "2026-02-10",
                    nextDueDate = "2027-02-10",
                    batchLotNumber = "FC-1102-L",
                    manufacturer = "Merck Animal Health",
                    veterinarian = "Dr. Emmanuel Sackor",
                    notes = "Administered right shoulder."
                )
            )
            preventiveDao.insertVaccination(
                VaccinationEntity(
                    id = 4,
                    petId = p3,
                    vaccineName = "Rabies Monovalent Vaccine",
                    administeredDate = "2025-11-20",
                    nextDueDate = "2026-11-20",
                    batchLotNumber = "RB-9941-K",
                    manufacturer = "Zoetis",
                    veterinarian = "Dr. Emmanuel Sackor",
                    notes = "Annual booster."
                )
            )

            // Deworming
            preventiveDao.insertDeworming(
                DewormingEntity(
                    id = 1,
                    petId = p1,
                    productName = "Drontal Plus Flavored Tablets",
                    dosage = "2.5 Tablets (Praziquantel/Pyrantel/Febantel)",
                    weightKg = 24.8,
                    administeredDate = "2026-07-10",
                    nextDueDate = "2026-10-10",
                    veterinarian = "Dr. Emmanuel Sackor",
                    notes = "Quarterly broad-spectrum deworming protocol."
                )
            )
            preventiveDao.insertDeworming(
                DewormingEntity(
                    id = 2,
                    petId = p2,
                    productName = "Profender Spot-on for Cats",
                    dosage = "1 Pipette (0.7 ml)",
                    weightKg = 4.7,
                    administeredDate = "2026-08-05",
                    nextDueDate = "2026-11-05",
                    veterinarian = "Dr. Emmanuel Sackor",
                    notes = "Topical tapeworm and roundworm prevention."
                )
            )

            // Parasite prevention
            preventiveDao.insertParasiteRecord(
                ParasitePreventionEntity(
                    id = 1,
                    petId = p1,
                    productName = "NexGard Chewables",
                    targetType = "Fleas & Ticks",
                    administeredDate = "2026-09-01",
                    nextDueDate = "2026-10-01",
                    notes = "Monthly oral ectoparasite prevention."
                )
            )
            preventiveDao.insertParasiteRecord(
                ParasitePreventionEntity(
                    id = 2,
                    petId = p2,
                    productName = "Advocate Feline Spot-on",
                    targetType = "Fleas & Heartworm",
                    administeredDate = "2026-09-05",
                    nextDueDate = "2026-10-05",
                    notes = "Applied between shoulder blades."
                )
            )

            // Weight History
            preventiveDao.insertWeight(WeightRecordEntity(petId = p1, weightKg = 23.5, recordedDate = "2025-10-15", notes = "Healthy weight"))
            preventiveDao.insertWeight(WeightRecordEntity(petId = p1, weightKg = 24.1, recordedDate = "2026-03-20", notes = "Steady growth"))
            preventiveDao.insertWeight(WeightRecordEntity(petId = p1, weightKg = 24.8, recordedDate = "2026-07-10", notes = "Optimal body condition score (5/9)"))

            preventiveDao.insertWeight(WeightRecordEntity(petId = p2, weightKg = 4.2, recordedDate = "2025-12-01", notes = "Post-rescue stabilization"))
            preventiveDao.insertWeight(WeightRecordEntity(petId = p2, weightKg = 4.7, recordedDate = "2026-08-05", notes = "Healthy adult weight"))

            // Prescriptions
            preventiveDao.insertPrescription(
                PrescriptionEntity(
                    id = 1,
                    petId = p2,
                    consultationId = 1,
                    medicationName = "Surolan Otic Drops",
                    dosage = "5 drops in left ear",
                    frequency = "Twice daily (q12h)",
                    route = "Otic (Ear canal)",
                    duration = "7 days",
                    quantity = "1 bottle (15ml)",
                    instructions = "Clean external ear gently with cleansing solution before applying drops.",
                    startDate = "2026-09-28",
                    endDate = "2026-10-05",
                    veterinarian = "Dr. Emmanuel Sackor",
                    status = "Active"
                )
            )

            // Consultations
            consultationDao.insertConsultation(
                ConsultationEntity(
                    id = 1,
                    petId = p2,
                    appointmentId = 2,
                    veterinarian = "Dr. Emmanuel Sackor",
                    date = "2026-09-28",
                    reasonForVisit = "Pruritus & head shaking",
                    symptoms = "Client noticed Simba scratching left ear frequently for past 3 days.",
                    clinicalFindings = "Mild erythema of left pinna. Brown ceruminous exudate in canal. Tympanic membrane intact.",
                    diagnosis = "Unilateral mild Otitis Externa (bacterial/yeast suspected)",
                    treatmentSummary = "Ear flushed in clinic with saline cleanser. Prescribed Surolan Otic drops for 7 days.",
                    followUpDate = "2026-10-06",
                    isFinalized = true,
                    finalizedAt = System.currentTimeMillis() - 3600000,
                    isOwnerVisible = true,
                    internalNotes = "Client advised not to use cotton swabs inside canal."
                )
            )

            // 6. Inventory Items
            invDao.insertInventory(
                InventoryEntity(
                    id = 1,
                    name = "Rabies Vaccine (Defensor 3)",
                    category = "Vaccine",
                    sku = "VAC-RAB-001",
                    quantity = 35,
                    unit = "vials",
                    purchaseCost = 6.50,
                    sellingPrice = 15.00,
                    supplier = "Zoetis Global / Liberia Health Distribution",
                    minStockThreshold = 10,
                    expiryDate = "2027-06-30",
                    batchNumber = "DEF-9901"
                )
            )
            invDao.insertInventory(
                InventoryEntity(
                    id = 2,
                    name = "DHPP Canine 5-in-1 Vaccine",
                    category = "Vaccine",
                    sku = "VAC-DHP-002",
                    quantity = 4, // Trigger low stock!
                    unit = "vials",
                    purchaseCost = 12.00,
                    sellingPrice = 25.00,
                    supplier = "Boehringer Ingelheim",
                    minStockThreshold = 10,
                    expiryDate = "2026-12-31",
                    batchNumber = "DH-4412"
                )
            )
            invDao.insertInventory(
                InventoryEntity(
                    id = 3,
                    name = "Drontal Plus Deworming Tablets",
                    category = "Dewormer",
                    sku = "DEW-DRO-003",
                    quantity = 58,
                    unit = "tablets",
                    purchaseCost = 2.00,
                    sellingPrice = 5.00,
                    supplier = "Vetoquinol",
                    minStockThreshold = 15,
                    expiryDate = "2028-01-31",
                    batchNumber = "DP-7731"
                )
            )
            invDao.insertInventory(
                InventoryEntity(
                    id = 4,
                    name = "Amoxicillin / Clavulanic Acid 250mg",
                    category = "Antibiotic",
                    sku = "MED-AMX-004",
                    quantity = 7, // Low stock warning!
                    unit = "strips",
                    purchaseCost = 4.50,
                    sellingPrice = 12.00,
                    supplier = "Liberia Pharma Supplies",
                    minStockThreshold = 12,
                    expiryDate = "2027-03-15",
                    batchNumber = "AMX-1120"
                )
            )
            invDao.insertInventory(
                InventoryEntity(
                    id = 5,
                    name = "NexGard Chewable 10-25kg",
                    category = "Parasiticide",
                    sku = "PAR-NEX-005",
                    quantity = 22,
                    unit = "chews",
                    purchaseCost = 9.00,
                    sellingPrice = 18.00,
                    supplier = "Boehringer Ingelheim",
                    minStockThreshold = 8,
                    expiryDate = "2027-09-01",
                    batchNumber = "NX-6650"
                )
            )
            invDao.insertInventory(
                InventoryEntity(
                    id = 6,
                    name = "Microchip Transponder (ISO 11784)",
                    category = "Equipment",
                    sku = "EQP-MCR-006",
                    quantity = 40,
                    unit = "kits",
                    purchaseCost = 5.00,
                    sellingPrice = 15.00,
                    supplier = "Datamars Animal ID",
                    minStockThreshold = 10,
                    expiryDate = "2030-01-01",
                    batchNumber = "MC-8810"
                )
            )

            // 7. Invoices & Payments
            val inv1 = invDao.insertInvoice(
                InvoiceEntity(
                    id = 1,
                    invoiceNumber = "INV-2026-0182",
                    clientId = c1,
                    petId = p1,
                    date = "2026-09-28",
                    dueDate = "2026-09-28",
                    itemsSummary = "Comprehensive Consultation ($20) + Rabies Booster ($15)",
                    totalAmount = 35.00,
                    amountPaid = 35.00,
                    status = "Paid",
                    notes = "Settled via Orange Mobile Money"
                )
            )
            invDao.insertPayment(
                PaymentEntity(
                    id = 1,
                    invoiceId = inv1,
                    amount = 35.00,
                    paymentMethod = "Mobile Money (MTN/Orange)",
                    reference = "TXN-OMM-994821",
                    recordedBy = "Front Desk Manager",
                    paymentDate = "2026-09-28",
                    notes = "Received to Happy Paws Liberia account"
                )
            )

            val inv2 = invDao.insertInvoice(
                InvoiceEntity(
                    id = 2,
                    invoiceNumber = "INV-2026-0183",
                    clientId = c2,
                    petId = p2,
                    date = "2026-09-28",
                    dueDate = "2026-10-05",
                    itemsSummary = "Ear exam & cleaning ($25) + Surolan Otic Drops ($18)",
                    totalAmount = 43.00,
                    amountPaid = 20.00,
                    status = "Partially Paid",
                    notes = "Partial deposit paid in Cash"
                )
            )
            invDao.insertPayment(
                PaymentEntity(
                    id = 2,
                    invoiceId = inv2,
                    amount = 20.00,
                    paymentMethod = "Cash",
                    reference = "RCP-CSH-0044",
                    recordedBy = "Front Desk Manager",
                    paymentDate = "2026-09-28",
                    notes = "Cash deposited in clinic register"
                )
            )

            // 8. CRM Enquiries
            crmDao.insertEnquiry(
                EnquiryEntity(
                    id = 1,
                    customerName = "Comfort Johnson",
                    phone = "0778112233",
                    petDetails = "Puppy rescued off Congo Town Beach",
                    enquiryText = "Found a stray 2-month puppy shivering near the beach. Needs rescue intake and first vaccinations.",
                    source = "WhatsApp",
                    status = "New",
                    assignedStaff = "Dr. Emmanuel Sackor",
                    followUpDate = "Today 3:00 PM",
                    notes = "Prepared isolation intake pen in rescue ward."
                )
            )
            crmDao.insertEnquiry(
                EnquiryEntity(
                    id = 2,
                    customerName = "Alexander Cooper",
                    phone = "0886554433",
                    petDetails = "2 adult German Shepherds",
                    enquiryText = "Inquiring about home visit availability for annual vaccinations in Mamba Point compound.",
                    source = "Phone Call",
                    status = "Contacted",
                    assignedStaff = "Front Desk",
                    followUpDate = "Tomorrow 10:00 AM",
                    notes = "Scheduled appointment for Thursday."
                )
            )
            crmDao.insertEnquiry(
                EnquiryEntity(
                    id = 3,
                    customerName = "Fatu Kamara",
                    phone = "0770998877",
                    petDetails = "Cat 'Mimi'",
                    enquiryText = "Interested in spay surgery appointment and cost estimate.",
                    source = "Facebook",
                    status = "Converted to Appointment",
                    assignedStaff = "Front Desk",
                    followUpDate = "Completed",
                    notes = "Quoted spay package $45 with pain relief."
                )
            )

            // 9. Notifications
            crmDao.insertNotification(
                NotificationEntity(
                    id = 1,
                    title = "Checked-in Patient Waiting",
                    message = "Bella (Samuel Tolbert) is checked in for Annual Rabies Booster.",
                    priority = "HIGH",
                    category = "Appointment",
                    relatedEntityId = 1
                )
            )
            crmDao.insertNotification(
                NotificationEntity(
                    id = 2,
                    title = "Low Stock Alert: DHPP 5-in-1",
                    message = "Only 4 vials of DHPP 5-in-1 remaining (Threshold: 10). Reorder recommended.",
                    priority = "CRITICAL",
                    category = "Inventory",
                    relatedEntityId = 2
                )
            )
            crmDao.insertNotification(
                NotificationEntity(
                    id = 3,
                    title = "Vaccine Due Reminder",
                    message = "Bella's Rabies Booster is due next month (Oct 15). Owner notified via in-app reminder.",
                    priority = "NORMAL",
                    category = "Vaccine",
                    relatedEntityId = 1
                )
            )

            // 10. Audit Log
            crmDao.insertAuditLog(
                AuditLogEntity(
                    id = 1,
                    userName = "Super Admin",
                    userRole = "SUPER_ADMIN",
                    action = "INITIALIZE",
                    entityName = "SYSTEM",
                    entityId = "HP-SYS-001",
                    details = "Initialized Happy Paws Liberia clinical database and local offline cache."
                )
            )
            crmDao.insertAuditLog(
                AuditLogEntity(
                    id = 2,
                    userName = "Dr. Emmanuel Sackor",
                    userRole = "VETERINARIAN",
                    action = "FINALIZE",
                    entityName = "CONSULTATION",
                    entityId = "CONS-1",
                    details = "Finalized medical examination and otic prescription for Simba."
                )
            )
        }
    }
}
