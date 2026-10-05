package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.example.data.local.AppointmentEntity
import com.example.data.local.PetEntity
import com.example.model.AppointmentStatus
import com.example.model.UserRole
import com.example.ui.components.CameraScannerDialog
import com.example.ui.components.PetDossierDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel

@Composable
fun StaffTodayScreen(
    viewModel: HappyPawsViewModel,
    currentRole: UserRole
) {
    val context = LocalContext.current
    val appointments by viewModel.allAppointments.collectAsState()
    val pets by viewModel.allPets.collectAsState()

    var showScanner by remember { mutableStateOf(false) }
    var showWalkInDialog by remember { mutableStateOf(false) }
    var selectedDossierPet by remember { mutableStateOf<PetEntity?>(null) }

    // Walk-in form state
    var walkInPetName by remember { mutableStateOf("") }
    var walkInOwnerName by remember { mutableStateOf("") }
    var walkInReason by remember { mutableStateOf("") }
    var walkInVet by remember { mutableStateOf("Dr. David Kpadeh") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Today's Patient Triage",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                Text(
                    text = "Reception check-in & clinical appointments",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumCharcoal
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Walk-in Check-in
                OutlinedButton(
                    onClick = { showWalkInDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepCharcoal)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Walk-in", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.Bold)
                }

                // Scan Pet QR
                Button(
                    onClick = { showScanner = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCodeScanner, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Summary Statistics Row
        val inConsultationCount = appointments.count { it.status == AppointmentStatus.IN_CONSULTATION }
        val confirmedCount = appointments.count { it.status == AppointmentStatus.CONFIRMED || it.status == AppointmentStatus.REQUESTED }
        val completedCount = appointments.count { it.status == AppointmentStatus.COMPLETED }
        val totalCount = appointments.size

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$totalCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                    Text("Total", fontSize = 11.sp, color = MediumCharcoal, fontWeight = FontWeight.Medium)
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = TerracottaLight,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$inConsultationCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AmberTerracottaDark)
                    Text("In Consult", fontSize = 11.sp, color = AmberTerracottaDark, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = SageLight,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$confirmedCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ForestSage)
                    Text("Queue", fontSize = 11.sp, color = ForestSage, fontWeight = FontWeight.Bold)
                }
            }
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF3F4F6),
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("$completedCount", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                    Text("Done", fontSize = 11.sp, color = DeepCharcoal, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (appointments.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = ForestSage,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No appointments today",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Reception is ready for patient triage or walk-in QR check-in.",
                        fontSize = 13.sp,
                        color = MediumCharcoal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(appointments) { apt ->
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${apt.petName} (${apt.species})",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Client: ${apt.clientName} · Time: ${apt.time}",
                                        fontSize = 12.sp,
                                        color = MediumCharcoal
                                    )
                                    Text(
                                        text = "Reason: ${apt.reason}",
                                        fontSize = 13.sp,
                                        color = DeepCharcoal,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    Text(
                                        text = "Doctor: ${apt.vetName}",
                                        fontSize = 12.sp,
                                        color = ForestSage,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                StatusBadge(
                                    text = apt.status.name.replace("_", " "),
                                    backgroundColor = when (apt.status) {
                                        AppointmentStatus.IN_CONSULTATION -> TerracottaLight
                                        AppointmentStatus.COMPLETED -> Color(0xFFE0E7FF)
                                        else -> SageLight
                                    },
                                    textColor = when (apt.status) {
                                        AppointmentStatus.IN_CONSULTATION -> AmberTerracottaDark
                                        AppointmentStatus.COMPLETED -> Color(0xFF3730A3)
                                        else -> ForestSage
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Action buttons: View Dossier & Status Transitions
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val match = pets.find { it.name.equals(apt.petName, ignoreCase = true) || it.id == apt.petId }
                                            ?: pets.firstOrNull()
                                        selectedDossierPet = match
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("View Dossier", fontSize = 11.sp, color = DeepCharcoal, fontWeight = FontWeight.SemiBold)
                                }

                                when (apt.status) {
                                    AppointmentStatus.CONFIRMED, AppointmentStatus.REQUESTED -> {
                                        Button(
                                            onClick = {
                                                viewModel.updateAppointmentStatus(apt.id, AppointmentStatus.IN_CONSULTATION)
                                                Toast.makeText(context, "${apt.petName} admitted to consultation room.", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Start Consult", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    AppointmentStatus.IN_CONSULTATION -> {
                                        Button(
                                            onClick = {
                                                viewModel.updateAppointmentStatus(apt.id, AppointmentStatus.COMPLETED)
                                                Toast.makeText(context, "Consultation completed for ${apt.petName}.", Toast.LENGTH_SHORT).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text("Complete", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    AppointmentStatus.COMPLETED -> {
                                        Text(
                                            text = "Consultation Finalized",
                                            fontSize = 11.sp,
                                            color = ForestSage,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.align(Alignment.CenterVertically).padding(start = 4.dp)
                                        )
                                    }
                                    else -> {}
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Camera Scanner Dialog
    if (showScanner) {
        CameraScannerDialog(
            pets = pets,
            currentRole = currentRole,
            onDismiss = { showScanner = false },
            onPetVerified = { pet ->
                showScanner = false
                viewModel.checkInPetToday(pet.id, "Owner of ${pet.name}", pet.name, pet.species)
                Toast.makeText(context, "Checked in ${pet.name} to Today's Queue.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Dossier Dialog
    selectedDossierPet?.let { pet ->
        PetDossierDialog(
            pet = pet,
            currentRole = currentRole,
            onAdmitToQueue = {
                viewModel.checkInPetToday(pet.id, "Owner of ${pet.name}", pet.name, pet.species)
                selectedDossierPet = null
                Toast.makeText(context, "${pet.name} admitted to Today's queue.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { selectedDossierPet = null }
        )
    }

    // Walk-in Admission Dialog
    if (showWalkInDialog) {
        AlertDialog(
            onDismissRequest = { showWalkInDialog = false },
            title = { Text("Admit Walk-in Patient", fontWeight = FontWeight.Bold, color = DeepCharcoal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = walkInPetName,
                        onValueChange = { walkInPetName = it },
                        label = { Text("Pet Name *") },
                        placeholder = { Text("e.g. Bella, Simba") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = walkInOwnerName,
                        onValueChange = { walkInOwnerName = it },
                        label = { Text("Owner / Client Name") },
                        placeholder = { Text("e.g. Anthony Tolbert") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = walkInReason,
                        onValueChange = { walkInReason = it },
                        label = { Text("Reason for visit") },
                        placeholder = { Text("e.g. Rabies Booster, Wound Triage") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = walkInVet,
                        onValueChange = { walkInVet = it },
                        label = { Text("Attending Vet") },
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
                        if (walkInPetName.isNotBlank()) {
                            viewModel.checkInPetToday(
                                petId = 1,
                                clientName = walkInOwnerName.ifBlank { "Walk-in Client" },
                                petName = walkInPetName.trim(),
                                species = "Canine (Dog)"
                            )
                            showWalkInDialog = false
                            walkInPetName = ""
                            walkInOwnerName = ""
                            walkInReason = ""
                            Toast.makeText(context, "Walk-in admitted to Today's Queue.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage)
                ) {
                    Text("Admit Patient")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWalkInDialog = false }) {
                    Text("Cancel", color = MediumCharcoal)
                }
            }
        )
    }
}
