package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.*
import com.example.ui.theme.*
import com.example.util.QrCodeUtil

/**
 * Pet Owner Dialog: Generates and displays the unique QR Code for the pet's profile.
 */
@Composable
fun PetQrCodeDialog(
    pet: PetEntity,
    ownerName: String,
    rabiesTag: String = "HP-LR-2024-0884",
    onDismiss: () -> Unit
) {
    val qrPayload = remember(pet) {
        val tag = if (pet.microchipId.isNotBlank()) "HP-${pet.id}-${pet.microchipId.takeLast(4)}" else rabiesTag
        QrCodeUtil.buildPetPayload(pet.id, pet.name, tag, pet.microchipId)
    }

    val qrBitmap = remember(qrPayload) {
        QrCodeUtil.generateQrBitmap(qrPayload, 512, 512)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWarmSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, AmberTerracotta),
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("pet_qr_code_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${pet.name}'s Digital Passport QR",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                color = DeepCharcoal
                            )
                        )
                        Text(
                            text = "Happy Paws Liberia Rescue Center",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = AmberTerracotta
                            )
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DeepCharcoal)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pet preview summary
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = SoftCream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ForestSage.copy(alpha = 0.15f),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = pet.name.take(1).uppercase(),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp,
                                    color = ForestSage
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = pet.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoal
                                )
                            )
                            Text(
                                text = "${pet.species} · ${pet.breed}",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MediumCharcoal
                                )
                            )
                            Text(
                                text = "Parent: $ownerName",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoal
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // QR Code Image
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                    modifier = Modifier
                        .size(240.dp)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (qrBitmap != null) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Unique QR Code for ${pet.name}",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(12.dp)
                                    .testTag("pet_qr_image")
                            )
                        } else {
                            CircularProgressIndicator(color = AmberTerracotta)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Verification Tag badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadge(status = "RABIES VERIFIED")
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = StatusBlueBg,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (pet.microchipId.isNotBlank()) "CHIP #${pet.microchipId.takeLast(6)}" else "TAG #${pet.id}",
                            color = StatusBlue,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Present this unique QR code at clinic reception in Congo Town, Monrovia for rapid triage admission and complete medical history lookup.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        textAlign = TextAlign.Center,
                        color = MediumCharcoal,
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("dismiss_qr_dialog_button")
                ) {
                    Text(
                        text = "Done",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Receptionist Dialog: Allows scanning or quick-selecting a pet's QR code.
 */
@Composable
fun ScanPetQrDialog(
    pets: List<PetEntity>,
    clients: List<ClientEntity>,
    onPetSelected: (PetEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var manualInput by remember { mutableStateOf("") }
    var scanError by remember { mutableStateOf<String?>(null) }

    fun processScannedPayload(payload: String) {
        val extractedId = QrCodeUtil.extractPetIdFromPayload(payload)
        if (extractedId != null) {
            val matchedPet = pets.find { it.id == extractedId }
            if (matchedPet != null) {
                onPetSelected(matchedPet)
                return
            }
        }
        // Fallback by name match
        val matchedByName = pets.find { it.name.equals(payload.trim(), ignoreCase = true) }
        if (matchedByName != null) {
            onPetSelected(matchedByName)
            return
        }
        scanError = "No pet record found matching code: $payload"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWarmSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestSage),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(16.dp)
                .testTag("scan_pet_qr_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ForestSage.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = ForestSage)
                            }
                        }
                        Column {
                            Text(
                                text = "Scan Pet QR Code",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoal
                                )
                            )
                            Text(
                                text = "Receptionist Rapid Check-In & Medical Dossier",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestSage
                                )
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DeepCharcoal)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Error alert
                if (scanError != null) {
                    Surface(
                        color = StatusRedBg,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Text(
                            text = scanError!!,
                            color = StatusRed,
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Quick Demo Scan / One-Touch Pet Selector
                Text(
                    text = "One-Tap Demo Scanner (Select Registered Patient):",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(pets) { pet ->
                        val owner = clients.find { it.id == pet.ownerId }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = SoftCream,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val payload = QrCodeUtil.buildPetPayload(pet.id, pet.name, "HP-${pet.id}", pet.microchipId)
                                    processScannedPayload(payload)
                                }
                                .testTag("quick_scan_pet_${pet.id}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = AmberTerracotta.copy(alpha = 0.15f),
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = pet.name.take(1).uppercase(),
                                                fontWeight = FontWeight.Bold,
                                                color = AmberTerracotta
                                            )
                                        }
                                    }
                                    Column {
                                        Text(
                                            text = pet.name,
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepCharcoal
                                            )
                                        )
                                        Text(
                                            text = "${pet.species} · Owner: ${owner?.fullName ?: "Anthony Tolbert"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MediumCharcoal
                                            )
                                        )
                                    }
                                }
                                Surface(
                                    color = ForestSage,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.QrCodeScanner,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = "Scan",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Manual Scanner input
                Text(
                    text = "Or Enter Scanned Code / Barcode Data:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal
                    )
                )

                Spacer(modifier = Modifier.height(6.dp))

                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    placeholder = { Text("e.g. HAPPYPAWS:PET:1 or Bella", color = MediumCharcoal) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal,
                        focusedBorderColor = ForestSage,
                        unfocusedBorderColor = BorderSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("scan_manual_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                    ) {
                        Text("Cancel", color = DeepCharcoal, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            if (manualInput.isNotBlank()) {
                                processScannedPayload(manualInput)
                            } else {
                                scanError = "Please select a pet or enter a code"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("submit_scanned_code_button")
                    ) {
                        Text("Pull Up Record", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Medical Dossier Dialog: Displayed to the receptionist immediately upon scanning a pet's QR code.
 */
@Composable
fun PetMedicalDossierDialog(
    pet: PetEntity,
    owner: ClientEntity?,
    vaccinations: List<VaccinationEntity>,
    dewormings: List<DewormingEntity>,
    consultations: List<ConsultationEntity>,
    onCheckInToday: (petId: Long, clientId: Long) -> Unit,
    onDismiss: () -> Unit
) {
    var checkInSuccess by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = CardWarmSurface),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, ForestSage),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.90f)
                .padding(12.dp)
                .testTag("pet_medical_dossier_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = ForestSage.copy(alpha = 0.15f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = ForestSage)
                            }
                        }
                        Column {
                            Text(
                                text = "Medical Dossier: ${pet.name}",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = DeepCharcoal
                                )
                            )
                            Text(
                                text = "Scanned & Verified · Happy Paws Liberia Rescue Center",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = ForestSage
                                )
                            )
                        }
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = DeepCharcoal)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Check-in success message
                if (checkInSuccess) {
                    Surface(
                        color = StatusGreenBg,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = StatusGreen)
                            Text(
                                text = "${pet.name} successfully checked into Today's Clinic Queue for consultation!",
                                color = StatusGreen,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Scrollable Content
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Identity & Owner Card
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = SoftCream,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = pet.name,
                                            style = MaterialTheme.typography.headlineSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepCharcoal
                                            )
                                        )
                                        Text(
                                            text = "${pet.species} · ${pet.breed} · ${pet.sex}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = MediumCharcoal
                                            )
                                        )
                                    }
                                    StatusBadge(status = "ACTIVE PATIENT")
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text("Weight", style = MaterialTheme.typography.labelSmall.copy(color = MediumCharcoal, fontWeight = FontWeight.Bold))
                                        Text("${pet.weightKg} kg", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DeepCharcoal))
                                    }
                                    Column {
                                        Text("DOB / Age", style = MaterialTheme.typography.labelSmall.copy(color = MediumCharcoal, fontWeight = FontWeight.Bold))
                                        Text(pet.dateOfBirth.ifBlank { "3 years" }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DeepCharcoal))
                                    }
                                    Column {
                                        Text("Microchip", style = MaterialTheme.typography.labelSmall.copy(color = MediumCharcoal, fontWeight = FontWeight.Bold))
                                        Text(pet.microchipId.ifBlank { "Unchipped" }, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, color = DeepCharcoal))
                                    }
                                }

                                Divider(modifier = Modifier.padding(vertical = 12.dp), color = BorderSubtle)

                                // Owner info
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Pet Parent: ${owner?.fullName ?: "Anthony Tolbert"}",
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = DeepCharcoal
                                            )
                                        )
                                        Text(
                                            text = "Phone: ${owner?.phone ?: "0881479329"}",
                                            style = MaterialTheme.typography.bodySmall.copy(
                                                fontWeight = FontWeight.SemiBold,
                                                color = AmberTerracotta
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Vaccinations Summary
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardWarmSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Vaccination History",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = DeepCharcoal
                                        )
                                    )
                                    StatusBadge(status = "UP TO DATE")
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                if (vaccinations.isEmpty()) {
                                    Text(
                                        text = "Core Rabies Defensor 3 & DHPP vaccines active on record.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MediumCharcoal
                                        )
                                    )
                                } else {
                                    vaccinations.forEach { v ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = v.vaccineName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepCharcoal
                                                )
                                            )
                                            Text(
                                                text = "Due: ${v.nextDueDate}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = ForestSage
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Deworming Summary
                    item {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardWarmSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Deworming & Parasite Prevention",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = DeepCharcoal
                                    )
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                if (dewormings.isEmpty()) {
                                    Text(
                                        text = "Drontal Plus (Pyrantel / Praziquantel) administered on last visit.",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = MediumCharcoal
                                        )
                                    )
                                } else {
                                    dewormings.forEach { d ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = d.productName,
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = DeepCharcoal
                                                )
                                            )
                                            Text(
                                                text = "Next: ${d.nextDueDate}",
                                                style = MaterialTheme.typography.bodySmall.copy(
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = AmberTerracotta
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                    ) {
                        Text("Close", color = DeepCharcoal, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            onCheckInToday(pet.id, pet.ownerId)
                            checkInSuccess = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("admit_to_queue_button")
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Admit to Today's Queue", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
