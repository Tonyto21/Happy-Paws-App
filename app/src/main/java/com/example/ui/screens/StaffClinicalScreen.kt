package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffClinicalScreen(
    pets: List<PetEntity>,
    clients: List<ClientEntity>,
    consultations: List<ConsultationEntity>,
    vaccines: List<VaccinationEntity>,
    dewormings: List<DewormingEntity>,
    parasiteRecords: List<ParasitePreventionEntity>,
    prescriptions: List<PrescriptionEntity>,
    weights: List<WeightRecordEntity>,
    preSelectedPetId: Long? = null,
    onSaveConsultation: (ConsultationEntity) -> Unit,
    onFinalizeConsultation: (Long) -> Unit,
    onAddVaccination: (VaccinationEntity) -> Unit,
    onAddDeworming: (DewormingEntity) -> Unit,
    onAddParasiteRecord: (ParasitePreventionEntity) -> Unit,
    onAddPrescription: (PrescriptionEntity) -> Unit,
    onAddWeight: (WeightRecordEntity) -> Unit,
    clinicSettings: ClinicSettingsEntity? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPetId by remember(pets, preSelectedPetId) {
        mutableLongStateOf(preSelectedPetId ?: pets.firstOrNull()?.id ?: 0L)
    }
    val currentPet = pets.find { it.id == selectedPetId } ?: pets.firstOrNull()
    val currentOwner = clients.find { it.id == currentPet?.ownerId }

    var selectedTab by remember { mutableIntStateOf(0) } // 0: Veterinary Consultation, 1: Vaccines, 2: Parasite/Deworm, 3: Prescriptions, 4: Weight, 5: Passport
    val clinicalTabs = listOf("Veterinary Consultation", "Vaccines", "Deworming", "Rx Prescriptions", "Weight", "Passport")

    var showPassportModal by remember { mutableStateOf(false) }

    // Forms State
    var reasonText by remember { mutableStateOf("") }
    var symptomsText by remember { mutableStateOf("") }
    var findingsText by remember { mutableStateOf("") }
    var diagnosisText by remember { mutableStateOf("") }
    var treatmentPlanText by remember { mutableStateOf("") }
    var followUpDateText by remember { mutableStateOf("") }

    // Vaccine Dialog
    var showVaccineDialog by remember { mutableStateOf(false) }
    var showDewormingDialog by remember { mutableStateOf(false) }
    var showRxDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

    val petConsultations = consultations.filter { it.petId == currentPet?.id }
    val petVaccines = vaccines.filter { it.petId == currentPet?.id }
    val petDewormings = dewormings.filter { it.petId == currentPet?.id }
    val petParasites = parasiteRecords.filter { it.petId == currentPet?.id }
    val petPrescriptions = prescriptions.filter { it.petId == currentPet?.id }
    val petWeights = weights.filter { it.petId == currentPet?.id }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        // Patient Selector Bar
        Surface(
            color = SoftCream,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = "Active Clinical Patient",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = SoftSlate
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(pets) { pet ->
                        val isSel = pet.id == selectedPetId
                        FilterChip(
                            selected = isSel,
                            onClick = { selectedPetId = pet.id },
                            leadingIcon = {
                                Icon(Icons.Default.Pets, contentDescription = null, modifier = Modifier.size(16.dp))
                            },
                            label = { Text(pet.name) },
                            modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                        )
                    }
                }
            }
        }

        // Active Patient Mini Summary
        if (currentPet != null) {
            Surface(
                color = CardWarmSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, BorderSubtle)
            ) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 10.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = "${currentPet.name} (${currentPet.species})",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = DeepCharcoal
                            )
                            if (currentPet.isRescuePet) {
                                Text("• Rescue", color = ForestSage, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                        Text(
                            text = "Breed: ${currentPet.breed} • ${currentPet.weightKg} kg • ${currentPet.sex}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MediumCharcoal
                        )
                        Text(
                            text = "Guardian: ${currentOwner?.fullName ?: "N/A"} • ${currentOwner?.phone ?: ""}",
                            style = MaterialTheme.typography.bodySmall,
                            color = SoftSlate
                        )
                    }

                    Button(
                        onClick = { showPassportModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.defaultMinSize(minHeight = 44.dp).testTag("clinical_view_passport")
                    ) {
                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Passport", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        // Scrollable Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = WarmIvory,
            contentColor = AmberTerracotta,
            edgePadding = 12.dp
        ) {
            clinicalTabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTab == index,
                    onClick = {
                        if (index == 5) {
                            showPassportModal = true
                        } else {
                            selectedTab = index
                        }
                    },
                    text = {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedTab) {
                0 -> {
                    // SOAP Clinical Consultation Form & History
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Veterinary Consultation & Clinical Notes",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Veterinarian: Dr. Emmanuel Sackor, DVM",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AmberTerracotta
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedTextField(
                                        value = reasonText,
                                        onValueChange = { reasonText = it },
                                        label = { Text("Reason for Visit") },
                                        modifier = Modifier.fillMaxWidth().testTag("clinical_reason_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = symptomsText,
                                        onValueChange = { symptomsText = it },
                                        label = { Text("Pet Owner's Report / Symptoms") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().testTag("clinical_symptoms_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = findingsText,
                                        onValueChange = { findingsText = it },
                                        label = { Text("Examination Findings") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().testTag("clinical_findings_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = diagnosisText,
                                        onValueChange = { diagnosisText = it },
                                        label = { Text("Diagnosis / Clinical Assessment") },
                                        modifier = Modifier.fillMaxWidth().testTag("clinical_diagnosis_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = treatmentPlanText,
                                        onValueChange = { treatmentPlanText = it },
                                        label = { Text("Treatment & Medication Plan") },
                                        minLines = 2,
                                        modifier = Modifier.fillMaxWidth().testTag("clinical_plan_input")
                                    )

                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = followUpDateText,
                                        onValueChange = { followUpDateText = it },
                                        label = { Text("Follow-up Instructions & Date (e.g. 2026-10-10)") },
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(16.dp))
                                    Button(
                                        onClick = {
                                            if (currentPet != null && diagnosisText.isNotBlank()) {
                                                val newConsult = ConsultationEntity(
                                                    petId = currentPet.id,
                                                    veterinarian = "Dr. Emmanuel Sackor",
                                                    date = "2026-09-28",
                                                    reasonForVisit = reasonText.ifEmpty { "General Examination" },
                                                    symptoms = symptomsText,
                                                    clinicalFindings = findingsText,
                                                    diagnosis = diagnosisText,
                                                    treatmentSummary = treatmentPlanText,
                                                    followUpDate = followUpDateText,
                                                    isFinalized = true,
                                                    finalizedAt = System.currentTimeMillis()
                                                )
                                                onSaveConsultation(newConsult)
                                                reasonText = ""
                                                symptomsText = ""
                                                findingsText = ""
                                                diagnosisText = ""
                                                treatmentPlanText = ""
                                                followUpDateText = ""
                                                Toast.makeText(context, "Consultation record saved & finalized into medical timeline", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Please enter a diagnosis / assessment", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                                        modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_consultation_button")
                                    ) {
                                        Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("Save & Finalize Record", style = MaterialTheme.typography.labelLarge)
                                    }
                                }
                            }
                        }

                        // Medical History Timeline for this Pet
                        item {
                            SectionHeader(title = "Clinical History Timeline")
                        }

                        if (petConsultations.isEmpty()) {
                            item {
                                Surface(
                                    color = SoftCream,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "No prior consultation records on file for ${currentPet?.name}.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MediumCharcoal,
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        } else {
                            items(petConsultations) { consult ->
                                Surface(
                                    color = CardWarmSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = consult.date,
                                                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                                color = AmberTerracotta
                                            )
                                            if (consult.isFinalized) {
                                                Surface(color = SoftSage, shape = RoundedCornerShape(6.dp)) {
                                                    Text(
                                                        text = "Finalized",
                                                        color = ForestSage,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Diagnosis: ${consult.diagnosis}",
                                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                            color = DeepCharcoal
                                        )
                                        Text(
                                            text = "Findings: ${consult.clinicalFindings}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MediumCharcoal
                                        )
                                        Text(
                                            text = "Plan: ${consult.treatmentSummary}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ForestSage
                                        )
                                        if (consult.followUpDate.isNotEmpty()) {
                                            Text(
                                                text = "Follow-up: ${consult.followUpDate}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = SoftSlate
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Vaccines Tab
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Vaccine Administrations",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                                    color = DeepCharcoal
                                )
                                Button(
                                    onClick = { showVaccineDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("add_vaccine_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Administer Vaccine")
                                }
                            }
                        }

                        if (petVaccines.isEmpty()) {
                            item {
                                Surface(shape = RoundedCornerShape(12.dp), color = CardWarmSurface, modifier = Modifier.fillMaxWidth()) {
                                    Text("No vaccines recorded yet.", modifier = Modifier.padding(16.dp), color = MediumCharcoal)
                                }
                            }
                        } else {
                            items(petVaccines) { vac ->
                                Surface(
                                    color = CardWarmSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(vac.vaccineName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                                            StatusBadge(status = "Certified")
                                        }
                                        Text("Administered: ${vac.administeredDate} • Lot: ${vac.batchLotNumber.ifEmpty { "N/A" }}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                                        Text("Next Booster Due: ${vac.nextDueDate}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ForestSage)
                                        Text("Veterinarian: ${vac.veterinarian}", style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Deworming & Parasite Prevention
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Deworming Protocols",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                                    color = DeepCharcoal
                                )
                                Button(
                                    onClick = { showDewormingDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Log Deworming")
                                }
                            }
                        }

                        items(petDewormings) { dew ->
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(14.dp)) {
                                    Text(dew.productName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold))
                                    Text("Dose: ${dew.dosage} • Pet Weight: ${dew.weightKg} kg", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                                    Text("Given: ${dew.administeredDate} • Next Due: ${dew.nextDueDate}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = ForestSage)
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // Prescriptions Tab
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Active Prescriptions",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                                    color = DeepCharcoal
                                )
                                Button(
                                    onClick = { showRxDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("write_prescription_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Write Rx")
                                }
                            }
                        }

                        if (petPrescriptions.isEmpty()) {
                            item {
                                Surface(shape = RoundedCornerShape(12.dp), color = CardWarmSurface, modifier = Modifier.fillMaxWidth()) {
                                    Text("No prescriptions on file for this patient.", modifier = Modifier.padding(16.dp), color = MediumCharcoal)
                                }
                            }
                        } else {
                            items(petPrescriptions) { rx ->
                                Surface(
                                    color = CardWarmSurface,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(1.dp, BorderSubtle),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(rx.medicationName, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                                            StatusBadge(status = rx.status)
                                        }
                                        Text("Dosage: ${rx.dosage} • ${rx.frequency} (${rx.route})", style = MaterialTheme.typography.bodyMedium, color = MediumCharcoal)
                                        Text("Duration: ${rx.duration} • Qty: ${rx.quantity}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                                        Text("Instructions: ${rx.instructions}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = ForestSage)
                                    }
                                }
                            }
                        }
                    }
                }

                4 -> {
                    // Weight Tab
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Weight History & Growth",
                                    style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                                    color = DeepCharcoal
                                )
                                Button(
                                    onClick = { showWeightDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Record Weight")
                                }
                            }
                        }

                        items(petWeights.reversed()) { w ->
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(w.recordedDate, style = MaterialTheme.typography.labelMedium, color = SoftSlate)
                                        if (w.notes.isNotEmpty()) {
                                            Text(w.notes, style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                                        }
                                    }
                                    Text("${w.weightKg} kg", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Passport Viewer & Printable Certificate
    if (showPassportModal && currentPet != null) {
        PrintPassportDialog(
            pet = currentPet,
            owner = currentOwner,
            vaccines = petVaccines,
            dewormings = petDewormings,
            clinicSettings = clinicSettings,
            onDismiss = { showPassportModal = false }
        )
    }

    // Administer Vaccine Dialog
    if (showVaccineDialog && currentPet != null) {
        var vacName by remember { mutableStateOf("Rabies Inactivated Booster") }
        var lotNo by remember { mutableStateOf("RB-2026-09A") }
        var nextDue by remember { mutableStateOf("2027-09-28") }

        AlertDialog(
            onDismissRequest = { showVaccineDialog = false },
            title = { Text("Administer Vaccine to ${currentPet.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = vacName, onValueChange = { vacName = it }, label = { Text("Vaccine Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = lotNo, onValueChange = { lotNo = it }, label = { Text("Batch / Lot #") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nextDue, onValueChange = { nextDue = it }, label = { Text("Next Due Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val v = VaccinationEntity(
                            petId = currentPet.id,
                            vaccineName = vacName,
                            administeredDate = "2026-09-28",
                            nextDueDate = nextDue,
                            batchLotNumber = lotNo,
                            veterinarian = "Dr. Emmanuel Sackor"
                        )
                        onAddVaccination(v)
                        showVaccineDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Save Vaccine")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaccineDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Deworming Dialog
    if (showDewormingDialog && currentPet != null) {
        var prodName by remember { mutableStateOf("Drontal Plus Tablets") }
        var dosage by remember { mutableStateOf("2 Tablets") }
        var nextDue by remember { mutableStateOf("2026-12-28") }

        AlertDialog(
            onDismissRequest = { showDewormingDialog = false },
            title = { Text("Log Deworming for ${currentPet.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = prodName, onValueChange = { prodName = it }, label = { Text("Product Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = nextDue, onValueChange = { nextDue = it }, label = { Text("Next Due Date") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val d = DewormingEntity(
                            petId = currentPet.id,
                            productName = prodName,
                            dosage = dosage,
                            weightKg = currentPet.weightKg,
                            administeredDate = "2026-09-28",
                            nextDueDate = nextDue,
                            veterinarian = "Dr. Emmanuel Sackor"
                        )
                        onAddDeworming(d)
                        showDewormingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Save Deworming")
                }
            },
            dismissButton = { TextButton(onClick = { showDewormingDialog = false }) { Text("Cancel") } }
        )
    }

    // Write Prescription Dialog
    if (showRxDialog && currentPet != null) {
        var medName by remember { mutableStateOf("Amoxicillin/Clavulanate 250mg") }
        var dosage by remember { mutableStateOf("1 tablet twice daily") }
        var route by remember { mutableStateOf("Oral") }
        var duration by remember { mutableStateOf("7 days") }
        var qty by remember { mutableStateOf("14 tablets") }
        var instructions by remember { mutableStateOf("Administer with food. Complete entire course.") }

        AlertDialog(
            onDismissRequest = { showRxDialog = false },
            title = { Text("Write Prescription for ${currentPet.name}") },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(value = medName, onValueChange = { medName = it }, label = { Text("Medication") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = dosage, onValueChange = { dosage = it }, label = { Text("Dosage & Frequency") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = route, onValueChange = { route = it }, label = { Text("Route (Oral, Topical, etc.)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = instructions, onValueChange = { instructions = it }, label = { Text("Instructions") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val rx = PrescriptionEntity(
                            petId = currentPet.id,
                            medicationName = medName,
                            dosage = dosage,
                            frequency = "Twice daily",
                            route = route,
                            duration = duration,
                            quantity = qty,
                            instructions = instructions,
                            startDate = "2026-09-28",
                            veterinarian = "Dr. Emmanuel Sackor"
                        )
                        onAddPrescription(rx)
                        showRxDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Issue Prescription")
                }
            },
            dismissButton = { TextButton(onClick = { showRxDialog = false }) { Text("Cancel") } }
        )
    }

    // Weight Dialog
    if (showWeightDialog && currentPet != null) {
        var wVal by remember { mutableStateOf(currentPet.weightKg.toString()) }
        var wNotes by remember { mutableStateOf("Routine clinical exam weight") }

        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = { Text("Record Weight for ${currentPet.name}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = wVal, onValueChange = { wVal = it }, label = { Text("Weight (kg)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = wNotes, onValueChange = { wNotes = it }, label = { Text("Clinical Notes") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val num = wVal.toDoubleOrNull() ?: currentPet.weightKg
                        onAddWeight(
                            WeightRecordEntity(
                                petId = currentPet.id,
                                weightKg = num,
                                recordedDate = "2026-09-28",
                                notes = wNotes
                            )
                        )
                        showWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Save Weight")
                }
            },
            dismissButton = { TextButton(onClick = { showWeightDialog = false }) { Text("Cancel") } }
        )
    }
}
