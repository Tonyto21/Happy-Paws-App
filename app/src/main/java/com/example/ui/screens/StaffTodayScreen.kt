package com.example.ui.screens

import android.widget.Toast
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
import com.example.model.UserRole
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffTodayScreen(
    currentRole: UserRole,
    appointments: List<AppointmentEntity>,
    pets: List<PetEntity>,
    clients: List<ClientEntity>,
    lowStockItems: List<InventoryEntity>,
    notifications: List<NotificationEntity>,
    vaccinations: List<VaccinationEntity> = emptyList(),
    dewormings: List<DewormingEntity> = emptyList(),
    consultations: List<ConsultationEntity> = emptyList(),
    onUpdateAppointmentStatus: (id: Long, newStatus: String) -> Unit,
    onStartConsultation: (petId: Long, appointmentId: Long) -> Unit,
    onQuickCheckIn: () -> Unit,
    onRegisterNewClient: () -> Unit,
    onRecordPayment: () -> Unit,
    onCheckInPetToday: ((Long, Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Checked In", "In Progress", "Confirmed", "Emergencies")

    var showScanQrDialog by remember { mutableStateOf(false) }
    var showDossierDialog by remember { mutableStateOf(false) }
    var scannedPet by remember { mutableStateOf<PetEntity?>(null) }

    val filteredAppointments = appointments.filter { appt ->
        when (selectedFilter) {
            "Checked In" -> appt.status.equals("Checked In", ignoreCase = true)
            "In Progress" -> appt.status.equals("In Progress", ignoreCase = true)
            "Confirmed" -> appt.status.equals("Confirmed", ignoreCase = true)
            "Emergencies" -> appt.isHomeEmergency || appt.appointmentType.equals("Emergency", ignoreCase = true)
            else -> true
        }
    }

    val waitingCount = appointments.count { it.status.equals("Checked In", ignoreCase = true) }
    val inProgressCount = appointments.count { it.status.equals("In Progress", ignoreCase = true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 32.dp)
    ) {
        // 1. Staff Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "Today's Clinical Floor",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DeepCharcoal
                        )
                    }
                    Text(
                        text = "Role: ${currentRole.displayName} • Happy Paws Liberia Clinic",
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberTerracotta
                    )
                }
                HappyPawsLogo(size = 44.dp, showTagline = false)
            }
        }

        // 2. Offline sync indicator
        item {
            OfflineSyncBanner(
                lastSyncTime = "Continuous Offline Room Sync",
                onManualSync = {
                    Toast.makeText(context, "Local clinic database up to date", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // 3. Operational Stat Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricStatCard(
                    title = "Waiting Queue",
                    value = "$waitingCount",
                    subtitle = "Checked in patients",
                    icon = Icons.Default.AccessTime,
                    iconTint = StatusGreen,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "In Exam",
                    value = "$inProgressCount",
                    subtitle = "With Veterinarian",
                    icon = Icons.Default.MedicalServices,
                    iconTint = AmberTerracotta,
                    modifier = Modifier.weight(1f)
                )
                MetricStatCard(
                    title = "Low Stock",
                    value = "${lowStockItems.size}",
                    subtitle = "Items need reorder",
                    icon = Icons.Default.Inventory2,
                    iconTint = if (lowStockItems.isNotEmpty()) StatusRed else StatusGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // 4. Critical Alerts (Low Stock / Emergencies)
        if (lowStockItems.isNotEmpty()) {
            item {
                Surface(
                    color = StatusRedBg,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Critical Inventory Alert",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusRed
                            )
                            Text(
                                text = "${lowStockItems.first().name} has only ${lowStockItems.first().quantity} ${lowStockItems.first().unit} remaining!",
                                style = MaterialTheme.typography.bodySmall,
                                color = DeepCharcoal
                            )
                        }
                    }
                }
            }
        }

        // 5. Quick Actions Bar
        item {
            Text(
                text = "Rapid Actions",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold
                ),
                color = DeepCharcoal
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    QuickActionButton(
                        title = "Scan Pet QR",
                        icon = Icons.Default.QrCodeScanner,
                        containerColor = AmberTerracotta,
                        onClick = { showScanQrDialog = true }
                    )
                }
                item {
                    QuickActionButton(
                        title = "New Consultation",
                        icon = Icons.Default.Healing,
                        containerColor = ForestSage,
                        onClick = {
                            val firstWaiting = appointments.find { it.status.equals("Checked In", true) }
                            if (firstWaiting != null) {
                                onStartConsultation(firstWaiting.petId, firstWaiting.id)
                            } else {
                                onQuickCheckIn()
                            }
                        }
                    )
                }
                item {
                    QuickActionButton(
                        title = "New Client / Pet",
                        icon = Icons.Default.PersonAdd,
                        containerColor = AmberTerracotta,
                        onClick = onRegisterNewClient
                    )
                }
                item {
                    QuickActionButton(
                        title = "Record Payment",
                        icon = Icons.Default.ReceiptLong,
                        containerColor = DeepNavy,
                        onClick = onRecordPayment
                    )
                }
            }
        }

        // 6. Appointments Timeline & Patient Flow
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Patient Schedule (${filteredAppointments.size})",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepCharcoal
                )
            }

            // Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 6.dp)
            ) {
                items(filters) { f ->
                    val isSel = selectedFilter == f
                    FilterChip(
                        selected = isSel,
                        onClick = { selectedFilter = f },
                        label = { Text(f) },
                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                    )
                }
            }
        }

        // Appointment Cards with Instant Status Workflow
        items(filteredAppointments) { appt ->
            val pet = pets.find { it.id == appt.petId }
            val client = clients.find { it.id == appt.clientId }

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
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            PetAvatar(name = pet?.name ?: "Pet", species = pet?.species ?: "Dog", size = 44)
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Text(
                                        text = pet?.name ?: "Unknown Patient",
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = DeepCharcoal
                                    )
                                    if (pet?.isRescuePet == true) {
                                        Text(
                                            text = "• Rescue",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ForestSage
                                        )
                                    }
                                }
                                Text(
                                    text = "Owner: ${client?.fullName ?: "N/A"} (${client?.phone ?: ""})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SoftSlate
                                )
                            }
                        }
                        StatusBadge(status = appt.status)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Reason: ${appt.reason}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumCharcoal
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Time: ${appt.scheduledTime} • ${appt.appointmentType}",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = DeepCharcoal
                        )
                        if (appt.isHomeEmergency) {
                            Text(
                                text = "Emergency / Home Visit",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusRed
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(10.dp))

                    // Status Transition Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        when (appt.status) {
                            "Confirmed", "Requested" -> {
                                Button(
                                    onClick = { onUpdateAppointmentStatus(appt.id, "Checked In") },
                                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Check In", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            "Checked In" -> {
                                Button(
                                    onClick = {
                                        onUpdateAppointmentStatus(appt.id, "In Progress")
                                        onStartConsultation(appt.petId, appt.id)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Begin Exam", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            "In Progress" -> {
                                Button(
                                    onClick = { onUpdateAppointmentStatus(appt.id, "Completed") },
                                    colors = ButtonDefaults.buttonColors(containerColor = StatusGreen),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f).height(40.dp)
                                ) {
                                    Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Complete Visit", style = MaterialTheme.typography.labelMedium)
                                }
                            }
                            else -> {
                                Text(
                                    text = "Completed",
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                    color = StatusGreen
                                )
                            }
                        }

                        // Clinical Record Shortcut
                        OutlinedButton(
                            onClick = { onStartConsultation(appt.petId, appt.id) },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text("SOAP Record", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    if (showScanQrDialog) {
        ScanPetQrDialog(
            pets = pets,
            clients = clients,
            onPetSelected = { pet ->
                scannedPet = pet
                showScanQrDialog = false
                showDossierDialog = true
            },
            onDismiss = { showScanQrDialog = false }
        )
    }

    if (showDossierDialog && scannedPet != null) {
        val p = scannedPet!!
        val owner = clients.find { it.id == p.ownerId }
        val petVaccines = vaccinations.filter { it.petId == p.id }
        val petDewormings = dewormings.filter { it.petId == p.id }
        val petConsults = consultations.filter { it.petId == p.id }

        PetMedicalDossierDialog(
            pet = p,
            owner = owner,
            vaccinations = petVaccines,
            dewormings = petDewormings,
            consultations = petConsults,
            onCheckInToday = { petId, clientId ->
                onCheckInPetToday?.invoke(petId, clientId)
            },
            onDismiss = { showDossierDialog = false }
        )
    }
}
