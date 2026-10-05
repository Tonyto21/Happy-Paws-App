package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.VaccinationEntity
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel

data class SoapNote(
    val id: Long = System.currentTimeMillis(),
    val petName: String,
    val date: String,
    val vetName: String,
    val subjective: String,
    val objective: String,
    val assessment: String,
    val plan: String
)

@Composable
fun StaffClinicalScreen(
    viewModel: HappyPawsViewModel
) {
    val context = LocalContext.current
    val vaccinations by viewModel.allVaccinations.collectAsState()
    val pets by viewModel.allPets.collectAsState()

    var showSoapDialog by remember { mutableStateOf(false) }
    var showVaccineDialog by remember { mutableStateOf(false) }

    // In-memory SOAP consultation notes list seeded with Bella's official examination
    var soapNotes by remember {
        mutableStateOf(
            listOf(
                SoapNote(
                    id = 1,
                    petName = "Bella (3.5 yrs · African Boerboel Mix)",
                    date = "10 Feb 2024",
                    vetName = "Dr. David Kpadeh, DVM",
                    subjective = "Pet presented for routine annual wellness examination and Defensor 3 rabies booster.",
                    objective = "Temp 38.4°C, HR 110 bpm, Weight 24.5 kg. Cardiopulmonary auscultation clear. Mucous membranes pink, CRT < 2s.",
                    assessment = "Healthy adult spayed female canine. No ectoparasites or dermatological lesions detected.",
                    plan = "Administered Defensor 3 Rabies vaccine (Batch RB-99482-EXP25). Dispensed 3-month Drontal Plus chewable tablets for parasite prophylaxis."
                )
            )
        )
    }

    // SOAP form state
    var selectedPetName by remember { mutableStateOf("Bella") }
    var vetNameInput by remember { mutableStateOf("Dr. David Kpadeh, DVM") }
    var subjectiveInput by remember { mutableStateOf("") }
    var objectiveInput by remember { mutableStateOf("") }
    var assessmentInput by remember { mutableStateOf("") }
    var planInput by remember { mutableStateOf("") }

    // Vaccine form state
    var vacPetName by remember { mutableStateOf("Bella") }
    var vacNameInput by remember { mutableStateOf("Rabies (Defensor 3)") }
    var vacBatchInput by remember { mutableStateOf("RB-2026-EXP27") }
    var vacValidUntil by remember { mutableStateOf("15 Nov 2027") }
    var vacVetInput by remember { mutableStateOf("Dr. David Kpadeh, DVM") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Clinical Workspace & SOAP Records",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                Text(
                    text = "Doctor consultations, diagnosis, prescriptions, and vaccines",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumCharcoal
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { showSoapDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PostAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add SOAP", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { showVaccineDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Vaccines, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Record Vaccine", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Clinical Consultations / SOAP Notes Section
        Text(
            text = "Medical Examinations & Clinical Consultations",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepCharcoal,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        soapNotes.forEach { note ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = note.petName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = DeepCharcoal
                        )
                        StatusBadge("Finalized", SageLight, ForestSage)
                    }
                    Text(
                        text = "${note.date} · ${note.vetName}",
                        fontSize = 12.sp,
                        color = MediumCharcoal,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = WarmIvory,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Subjective (History): ${note.subjective}", fontSize = 12.sp, color = DeepCharcoal)
                            Text("Objective (Exam/Vitals): ${note.objective}", fontSize = 12.sp, color = DeepCharcoal)
                            Text("Assessment (Diagnosis): ${note.assessment}", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.SemiBold)
                            Text("Plan (Treatment & Rx): ${note.plan}", fontSize = 12.sp, color = ForestSage, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Official Immunization & Deworming Registry
        Text(
            text = "Immunization & Vaccine Administration Registry",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepCharcoal,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (vaccinations.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No immunization records found.", fontSize = 13.sp, color = MediumCharcoal)
                }
            }
        } else {
            vaccinations.forEach { vac ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${vac.petName} · ${vac.vaccineName}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DeepCharcoal
                            )
                            Text(
                                text = "Administered: ${vac.dateAdministered} · Valid: ${vac.validUntil}",
                                fontSize = 12.sp,
                                color = MediumCharcoal
                            )
                            Text(
                                text = "Batch: ${vac.batchNumber} · Attending: ${vac.vetName}",
                                fontSize = 11.sp,
                                color = ForestSage,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        StatusBadge(vac.status)
                    }
                }
            }
        }
    }

    // Add SOAP Note Dialog
    if (showSoapDialog) {
        AlertDialog(
            onDismissRequest = { showSoapDialog = false },
            title = { Text("Record Clinical Examination (SOAP)", fontWeight = FontWeight.Bold, color = DeepCharcoal) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = selectedPetName,
                        onValueChange = { selectedPetName = it },
                        label = { Text("Patient Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = subjectiveInput,
                        onValueChange = { subjectiveInput = it },
                        label = { Text("Subjective (Chief Complaint & History)") },
                        placeholder = { Text("e.g. Coughing, lethargy, decreased appetite") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = objectiveInput,
                        onValueChange = { objectiveInput = it },
                        label = { Text("Objective (Vitals, Temp, HR, Weight)") },
                        placeholder = { Text("e.g. Temp 38.6°C, HR 105, Weight 24.5 kg") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = assessmentInput,
                        onValueChange = { assessmentInput = it },
                        label = { Text("Assessment (Clinical Diagnosis)") },
                        placeholder = { Text("e.g. Mild upper respiratory infection") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = planInput,
                        onValueChange = { planInput = it },
                        label = { Text("Plan (Rx, Injections, Follow-up)") },
                        placeholder = { Text("e.g. Amoxicillin 250mg BID x 7d, rest") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (selectedPetName.isNotBlank() && assessmentInput.isNotBlank()) {
                            val newNote = SoapNote(
                                petName = selectedPetName.trim(),
                                date = "Today (Oct 2026)",
                                vetName = vetNameInput,
                                subjective = subjectiveInput.ifBlank { "Routine checkup presented by client." },
                                objective = objectiveInput.ifBlank { "Physical vitals within normal parameters." },
                                assessment = assessmentInput.trim(),
                                plan = planInput.ifBlank { "Continue regular diet and hydration." }
                            )
                            soapNotes = listOf(newNote) + soapNotes
                            showSoapDialog = false
                            subjectiveInput = ""
                            objectiveInput = ""
                            assessmentInput = ""
                            planInput = ""
                            Toast.makeText(context, "Clinical SOAP note recorded.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Save SOAP Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSoapDialog = false }) {
                    Text("Cancel", color = MediumCharcoal)
                }
            }
        )
    }

    // Record Vaccine Dialog
    if (showVaccineDialog) {
        AlertDialog(
            onDismissRequest = { showVaccineDialog = false },
            title = { Text("Administer / Record Vaccination", fontWeight = FontWeight.Bold, color = DeepCharcoal) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = vacPetName,
                        onValueChange = { vacPetName = it },
                        label = { Text("Patient Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vacNameInput,
                        onValueChange = { vacNameInput = it },
                        label = { Text("Vaccine Name") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vacBatchInput,
                        onValueChange = { vacBatchInput = it },
                        label = { Text("Batch / Lot #") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vacValidUntil,
                        onValueChange = { vacValidUntil = it },
                        label = { Text("Valid Until Date") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = vacVetInput,
                        onValueChange = { vacVetInput = it },
                        label = { Text("Administering Veterinarian") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (vacPetName.isNotBlank() && vacNameInput.isNotBlank()) {
                            val newVac = VaccinationEntity(
                                petId = 1,
                                petName = vacPetName.trim(),
                                vaccineName = vacNameInput.trim(),
                                dateAdministered = "Today (Oct 2026)",
                                validUntil = vacValidUntil.trim(),
                                batchNumber = vacBatchInput.trim(),
                                vetName = vacVetInput.trim(),
                                status = "Up to Date"
                            )
                            viewModel.recordVaccination(newVac)
                            showVaccineDialog = false
                            Toast.makeText(context, "Vaccination recorded into official registry.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage)
                ) {
                    Text("Save Vaccine Record")
                }
            },
            dismissButton = {
                TextButton(onClick = { showVaccineDialog = false }) {
                    Text("Cancel", color = MediumCharcoal)
                }
            }
        )
    }
}
