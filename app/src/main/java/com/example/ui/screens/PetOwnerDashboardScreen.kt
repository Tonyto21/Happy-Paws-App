package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.*
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetOwnerDashboardScreen(
    currentOwner: ClientEntity,
    pets: List<PetEntity>,
    appointments: List<AppointmentEntity>,
    vaccines: List<VaccinationEntity>,
    dewormings: List<DewormingEntity>,
    prescriptions: List<PrescriptionEntity>,
    consultations: List<ConsultationEntity>,
    weights: List<WeightRecordEntity>,
    clinicSettings: ClinicSettingsEntity? = null,
    onAddPetClick: () -> Unit,
    onRequestAppointment: (petId: Long, reason: String, isEmergency: Boolean) -> Unit,
    onLogWeight: (petId: Long, weightKg: Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedPetId by remember(pets) { mutableLongStateOf(pets.firstOrNull()?.id ?: 0L) }
    val currentPet = pets.find { it.id == selectedPetId } ?: pets.firstOrNull()

    var showPassportDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showRequestAppointmentDialog by remember { mutableStateOf(false) }
    var showLogWeightDialog by remember { mutableStateOf(false) }

    val petVaccines = vaccines.filter { it.petId == currentPet?.id }
    val petDewormings = dewormings.filter { it.petId == currentPet?.id }
    val petPrescriptions = prescriptions.filter { it.petId == currentPet?.id }
    val petConsultations = consultations.filter { it.petId == currentPet?.id }
    val petWeights = weights.filter { it.petId == currentPet?.id }
    val petAppointments = appointments.filter { it.petId == currentPet?.id }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Warm Greeting & Happy Paws Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good day, ${currentOwner.preferredName.ifEmpty { currentOwner.fullName }}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = DeepCharcoal
                    )
                    Text(
                        text = "Happy Paws Liberia • Pet Care Hub",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SoftSlate
                    )
                }
                HappyPawsLogo(size = 46.dp, showTagline = false)
            }
        }

        // 2. Offline Status Banner
        item {
            OfflineSyncBanner(
                lastSyncTime = "Ready for offline visit",
                onManualSync = {
                    Toast.makeText(context, "All pet records cached securely offline", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 3. Pet Switcher Pills
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "My Companions (${pets.size})",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = DeepCharcoal
                    )
                    TextButton(
                        onClick = onAddPetClick,
                        modifier = Modifier.testTag("add_pet_button")
                    ) {
                        Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = AmberTerracotta, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Pet", color = AmberTerracotta, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold))
                    }
                }

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(pets) { pet ->
                        val isSelected = pet.id == selectedPetId
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) AmberTerracotta else CardWarmSurface,
                            border = BorderStroke(1.dp, if (isSelected) AmberTerracotta else BorderSubtle),
                            modifier = Modifier
                                .clickable { selectedPetId = pet.id }
                                .testTag("pet_chip_${pet.name.lowercase()}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else SoftCream),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Pets,
                                        contentDescription = null,
                                        tint = if (isSelected) Color.White else AmberTerracotta,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                                Text(
                                    text = pet.name,
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) Color.White else DeepCharcoal
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. Hero Pet Profile Card
        if (currentPet != null) {
            item {
                Surface(
                    color = CardWarmSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            PetAvatar(name = currentPet.name, species = currentPet.species, size = 64)
                            Spacer(modifier = Modifier.width(14.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = currentPet.name,
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepCharcoal
                                    )
                                    if (currentPet.isRescuePet) {
                                        Surface(
                                            color = SoftSage,
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                text = "Rescue",
                                                color = ForestSage,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${currentPet.species} • ${currentPet.breed}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MediumCharcoal
                                )
                                Text(
                                    text = "Microchip: ${currentPet.microchipId.ifEmpty { "Pending" }}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftSlate
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = BorderSubtle.copy(alpha = 0.6f))
                        Spacer(modifier = Modifier.height(12.dp))

                        // Pet Stats Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            PetStatItem("WEIGHT", "${currentPet.weightKg} kg")
                            PetStatItem("AGE / DOB", currentPet.ageDisplay.ifEmpty { "Adult" })
                            PetStatItem("SEX", currentPet.sex.split(" ").first())
                            PetStatItem("STATUS", "Healthy")
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Quick Action Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showQrDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_qr_button")
                            ) {
                                Icon(Icons.Default.QrCode2, contentDescription = null, modifier = Modifier.size(17.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Pet QR", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = { showPassportDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("open_passport_button")
                            ) {
                                Icon(Icons.Default.Verified, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Passport", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }

                            Button(
                                onClick = { showRequestAppointmentDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepNavy),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("book_visit_button")
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Book Visit", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }

            // 5. Preventive Care & Vaccine Status Banner
            item {
                Surface(
                    color = SoftCream,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.Healing, contentDescription = null, tint = AmberTerracotta)
                                Text(
                                    text = "Preventive Care Status",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = DeepCharcoal
                                )
                            }
                            StatusBadge(status = if (petVaccines.isNotEmpty()) "Up to Date" else "Needs Review")
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val nextVaccine = petVaccines.firstOrNull()
                        if (nextVaccine != null) {
                            Text(
                                text = "Next booster: ${nextVaccine.vaccineName} due on ${nextVaccine.nextDueDate}",
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                color = DeepCharcoal
                            )
                        } else {
                            Text(
                                text = "No recorded vaccinations. Tap 'Book Visit' to protect ${currentPet.name}.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumCharcoal
                            )
                        }

                        val nextDeworming = petDewormings.firstOrNull()
                        if (nextDeworming != null) {
                            Text(
                                text = "Deworming: ${nextDeworming.productName} (Next: ${nextDeworming.nextDueDate})",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftSlate
                            )
                        }
                    }
                }
            }

            // 6. Upcoming Appointments
            item {
                SectionHeader(
                    title = "Upcoming Appointments",
                    actionText = "Schedule",
                    onActionClick = { showRequestAppointmentDialog = true }
                )

                if (petAppointments.isEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = CardWarmSurface,
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.EventAvailable, contentDescription = null, tint = SoftSlate, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "No appointments scheduled",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MediumCharcoal
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { showRequestAppointmentDialog = true },
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Text("Book Checkup or Vaccination")
                            }
                        }
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        petAppointments.forEach { appt ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = CardWarmSurface,
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "${appt.appointmentType} • ${appt.scheduledTime}",
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = DeepCharcoal
                                        )
                                        Text(
                                            text = "${appt.scheduledDate} • ${appt.reason}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MediumCharcoal
                                        )
                                        if (appt.isHomeEmergency) {
                                            Text(
                                                text = "Home Visit / Mobile Care",
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                color = AmberTerracotta
                                            )
                                        }
                                    }
                                    StatusBadge(status = appt.status)
                                }
                            }
                        }
                    }
                }
            }

            // 7. Weight Tracker
            item {
                SectionHeader(
                    title = "Weight Tracking",
                    actionText = "Log Weight",
                    onActionClick = { showLogWeightDialog = true }
                )

                Surface(
                    color = CardWarmSurface,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Latest Recorded Weight", style = MaterialTheme.typography.labelSmall, color = SoftSlate)
                                Text("${currentPet.weightKg} kg", style = MaterialTheme.typography.headlineSmall.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            }
                            Button(
                                onClick = { showLogWeightDialog = true },
                                colors = ButtonDefaults.buttonColors(containerColor = SoftSage),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, tint = ForestSage, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Add Entry", color = ForestSage, style = MaterialTheme.typography.labelMedium)
                            }
                        }

                        if (petWeights.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Recent Weight History:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SoftSlate)
                            petWeights.takeLast(4).reversed().forEach { w ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(w.recordedDate, style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                                    Text("${w.weightKg} kg", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = DeepCharcoal)
                                }
                            }
                        }
                    }
                }
            }

            // 8. Emergency Hotline Banner
            item {
                Surface(
                    color = DeepCharcoal,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Happy Paws Vet Hotline",
                                style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Text(
                                text = "24/7 Veterinary & Rescue: 0881479329",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmHoney
                            )
                        }
                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:0881479329"))
                                context.startActivity(intent)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                            shape = CircleShape,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("call_vet_button"),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = "Call Vet", tint = Color.White)
                        }
                    }
                }
            }
        }
    }

    // Passport Dialog
    if (showPassportDialog && currentPet != null) {
        PrintPassportDialog(
            pet = currentPet,
            owner = currentOwner,
            vaccines = petVaccines,
            dewormings = petDewormings,
            clinicSettings = clinicSettings,
            onDismiss = { showPassportDialog = false }
        )
    }

    // Request Appointment Dialog
    if (showRequestAppointmentDialog && currentPet != null) {
        var reason by remember { mutableStateOf("Annual Health Checkup & Vaccination") }
        var isEmergency by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRequestAppointmentDialog = false },
            title = {
                Text(
                    text = "Request Vet Appointment for ${currentPet.name}",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Select reason or describe symptoms:", style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(
                        value = reason,
                        onValueChange = { reason = it },
                        label = { Text("Reason for visit") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { isEmergency = !isEmergency }
                    ) {
                        Checkbox(checked = isEmergency, onCheckedChange = { isEmergency = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("This is an Urgent Home Visit / Emergency", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRequestAppointment(currentPet.id, reason, isEmergency)
                        showRequestAppointmentDialog = false
                        Toast.makeText(context, "Appointment requested! Happy Paws staff will confirm shortly.", Toast.LENGTH_LONG).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                    modifier = Modifier.testTag("submit_appointment_request")
                ) {
                    Text("Send Request")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRequestAppointmentDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Log Weight Dialog
    if (showLogWeightDialog && currentPet != null) {
        var weightInput by remember { mutableStateOf(currentPet.weightKg.toString()) }

        AlertDialog(
            onDismissRequest = { showLogWeightDialog = false },
            title = { Text("Log Weight for ${currentPet.name}") },
            text = {
                Column {
                    OutlinedTextField(
                        value = weightInput,
                        onValueChange = { weightInput = it },
                        label = { Text("Weight (kg)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val w = weightInput.toDoubleOrNull() ?: currentPet.weightKg
                        onLogWeight(currentPet.id, w)
                        showLogWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Save Weight")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogWeightDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showQrDialog && currentPet != null) {
        PetQrCodeDialog(
            pet = currentPet,
            ownerName = currentOwner.fullName,
            onDismiss = { showQrDialog = false }
        )
    }
}

@Composable
private fun PetStatItem(title: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = SoftSlate
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
            color = DeepCharcoal
        )
    }
}
