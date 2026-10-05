package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
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
import com.example.data.local.PetEntity
import com.example.model.UserRole
import com.example.ui.components.CameraScannerDialog
import com.example.ui.components.OfficialHealthPassportDialog
import com.example.ui.components.PetQrPassportDialog
import com.example.ui.components.RegisterPetDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel

@Composable
fun PetOwnerDashboardScreen(
    viewModel: HappyPawsViewModel,
    ownerPet: PetEntity,
    currentRole: UserRole
) {
    val context = LocalContext.current
    val pets by viewModel.allPets.collectAsState()
    val vaccinations by viewModel.allVaccinations.collectAsState()
    val appointments by viewModel.allAppointments.collectAsState()
    val clinicSettings by viewModel.clinicSettings.collectAsState()

    // Multi-pet selector
    var selectedPetId by remember { mutableStateOf<Long?>(null) }
    val activePet = pets.find { it.id == selectedPetId } ?: pets.firstOrNull() ?: ownerPet

    var showQrPassport by remember { mutableStateOf(false) }
    var showPrintPassport by remember { mutableStateOf(false) }
    var showCameraScanner by remember { mutableStateOf(false) }
    var showRegisterPetDialog by remember { mutableStateOf(false) }
    var showAppointmentRequest by remember { mutableStateOf(false) }
    var requestReason by remember { mutableStateOf("") }
    var requestTime by remember { mutableStateOf("10:30 AM") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Multi-Pet Switcher Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            pets.forEach { p ->
                val isSelected = p.id == activePet.id
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isSelected) AmberTerracotta else Color.White,
                    shadowElevation = if (isSelected) 3.dp else 1.dp,
                    modifier = Modifier.clickable { selectedPetId = p.id }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (p.species.contains("Cat", ignoreCase = true)) "🐱" else "🐶",
                            fontSize = 14.sp
                        )
                        Text(
                            text = p.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (isSelected) Color.White else DeepCharcoal
                        )
                    }
                }
            }

            // Add Another Pet Button
            OutlinedButton(
                onClick = { showRegisterPetDialog = true },
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepCharcoal)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Pet", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Check if there are no registered pets (New User Onboarding flow)
        if (pets.isEmpty()) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(28.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        Icons.Default.Pets,
                        contentDescription = null,
                        tint = AmberTerracotta,
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Welcome to Happy Paws Liberia!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Text(
                        text = "Register your first companion to generate their verified digital Health Passport, official rabies collar tag, and vaccination record.",
                        fontSize = 13.sp,
                        color = MediumCharcoal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
                    )
                    Button(
                        onClick = { showRegisterPetDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Register First Pet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            // Top Pet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${activePet.name}'s Health Passport",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Official Veterinary Health Record · Rabies Tag #${activePet.rabiesTag}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = AmberTerracotta,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Real Camera Scan Button for Pet Owner
                Button(
                    onClick = { showCameraScanner = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Tag", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pet Profile Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(activePet.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                                StatusBadge("Verified Healthy", SageLight, ForestSage)
                            }
                            Text("${activePet.species} · ${activePet.breed}", fontSize = 13.sp, color = MediumCharcoal, modifier = Modifier.padding(top = 2.dp))
                            Text("Sex: ${activePet.sex} · Weight: ${activePet.weightKg} kg · Born: ${activePet.dob}", fontSize = 13.sp, color = DeepCharcoal)
                            Text("Microchip ID: ${activePet.microchipId}", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.SemiBold)
                            Text("Rabies Collar Tag: #${activePet.rabiesTag}", fontSize = 12.sp, color = AmberTerracotta, fontWeight = FontWeight.Bold)
                            if (activePet.notes.isNotBlank()) {
                                Text("Notes: ${activePet.notes}", fontSize = 12.sp, color = MediumCharcoal, modifier = Modifier.padding(top = 4.dp))
                            }
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { showQrPassport = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { showPrintPassport = true },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepCharcoal)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Passport", fontSize = 11.sp, color = DeepCharcoal, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Vaccinations & Immunizations Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Vaccines, contentDescription = null, tint = AmberTerracotta)
                        Text("Official Immunizations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val petVaccines = vaccinations.filter { it.petName == activePet.name || it.petId == activePet.id }
                    if (petVaccines.isEmpty()) {
                        Surface(
                            color = WarmIvory,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No immunizations registered yet for ${activePet.name}.",
                                fontSize = 12.sp,
                                color = MediumCharcoal,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        petVaccines.forEach { vac ->
                            Surface(
                                color = WarmIvory,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(vac.vaccineName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoal)
                                        Text("Administered: ${vac.dateAdministered} · Valid: ${vac.validUntil}", fontSize = 11.sp, color = MediumCharcoal)
                                        Text("Batch: ${vac.batchNumber} · Vet: ${vac.vetName}", fontSize = 11.sp, color = ForestSage)
                                    }
                                    StatusBadge(vac.status)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Parasite Prevention & Deworming Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.MedicalServices, contentDescription = null, tint = ForestSage)
                        Text("Parasite Prevention & Deworming", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        color = WarmIvory,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Drontal Plus Flavor Tabs (Broad Spectrum)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoal)
                                Text("Given: 15 Aug 2024 · Next Due: 15 Nov 2024", fontSize = 11.sp, color = MediumCharcoal)
                                Text("Dosage: 2.5 Tablets · Prophylaxis Complete", fontSize = 11.sp, color = ForestSage)
                            }
                            StatusBadge("Protected", SageLight, ForestSage)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Appointment booking and history
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.CalendarToday, contentDescription = null, tint = ForestSage)
                            Text("Clinic Appointments", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                        }

                        OutlinedButton(
                            onClick = { showAppointmentRequest = true },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepCharcoal)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Book Visit", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val petAppointments = appointments.filter { it.petName == activePet.name || it.clientId == 1L }
                    if (petAppointments.isEmpty()) {
                        Text(
                            text = "No appointment history yet. Tap 'Book Visit' above to schedule a consultation.",
                            fontSize = 12.sp,
                            color = MediumCharcoal,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        petAppointments.forEach { apt ->
                            Surface(
                                color = WarmIvory,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(apt.reason, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoal)
                                        Text("Time: ${apt.time} · Doctor: ${apt.vetName}", fontSize = 12.sp, color = MediumCharcoal)
                                        Text("Patient: ${apt.petName}", fontSize = 11.sp, color = AmberTerracotta)
                                    }
                                    StatusBadge(apt.status.name.replace("_", " "))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Camera Scanner Dialog with strict ownership verification
    if (showCameraScanner) {
        CameraScannerDialog(
            pets = pets,
            currentRole = currentRole,
            onDismiss = { showCameraScanner = false },
            onPetVerified = { pet ->
                showCameraScanner = false
                selectedPetId = pet.id
                showQrPassport = true
                Toast.makeText(context, "Pet Verified: ${pet.name}. Official Health Passport loaded.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // QR Code Passport Dialog
    if (showQrPassport) {
        PetQrPassportDialog(pet = activePet, onDismiss = { showQrPassport = false })
    }

    // Official A4 Printable / Shareable Passport Dialog
    if (showPrintPassport) {
        OfficialHealthPassportDialog(
            pet = activePet,
            vaccinations = vaccinations,
            clinicSettings = clinicSettings,
            onDismiss = { showPrintPassport = false }
        )
    }

    // Register / Add Pet Dialog
    if (showRegisterPetDialog) {
        RegisterPetDialog(
            onRegister = { newPet ->
                viewModel.addPet(newPet) { generatedId ->
                    selectedPetId = generatedId
                }
                showRegisterPetDialog = false
                Toast.makeText(context, "${newPet.name} registered successfully.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showRegisterPetDialog = false }
        )
    }

    // Request Consultation Dialog
    if (showAppointmentRequest) {
        AlertDialog(
            onDismissRequest = { showAppointmentRequest = false },
            title = { Text("Request Clinic Consultation", fontWeight = FontWeight.Bold, color = DeepCharcoal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Booking consultation for ${activePet.name} (Rabies Tag #${activePet.rabiesTag})",
                        fontSize = 12.sp,
                        color = AmberTerracotta,
                        fontWeight = FontWeight.SemiBold
                    )
                    OutlinedTextField(
                        value = requestReason,
                        onValueChange = { requestReason = it },
                        label = { Text("Reason for visit") },
                        placeholder = { Text("e.g. Annual Rabies Booster, Deworming, Wellness Exam") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = requestTime,
                        onValueChange = { requestTime = it },
                        label = { Text("Preferred Time") },
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
                        if (requestReason.isNotBlank()) {
                            viewModel.requestAppointment(activePet.name, requestReason, requestTime, "Anthony Tolbert")
                            showAppointmentRequest = false
                            requestReason = ""
                            Toast.makeText(context, "Consultation requested for ${activePet.name}.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Confirm Booking")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppointmentRequest = false }) {
                    Text("Cancel", color = MediumCharcoal)
                }
            }
        )
    }
}
