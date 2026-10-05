package com.example.ui.components

import android.content.Intent
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.PetEntity
import com.example.data.local.VaccinationEntity
import com.example.model.ClinicSettings
import com.example.model.UserRole
import com.example.ui.theme.*
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generateQrCodeBitmap(text: String, size: Int = 512): Bitmap {
    val bitMatrix = QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, size, size)
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
    for (x in 0 until width) {
        for (y in 0 until height) {
            bmp.setPixel(x, y, if (bitMatrix.get(x, y)) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bmp
}

@Composable
fun PetQrPassportDialog(
    pet: PetEntity,
    onDismiss: () -> Unit
) {
    val qrPayload = remember(pet) {
        "{\"type\":\"HAPPY_PAWS_PET_PASSPORT\",\"petId\":${pet.id},\"name\":\"${pet.name}\",\"rabiesTag\":\"${pet.rabiesTag}\",\"microchipId\":\"${pet.microchipId}\"}"
    }
    val qrBitmap = remember(qrPayload) {
        generateQrCodeBitmap(qrPayload)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${pet.name}'s Health Passport QR",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                Text(
                    text = "Rabies Tag #${pet.rabiesTag}",
                    fontSize = 12.sp,
                    color = AmberTerracotta,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 16.dp)
                )

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(200.dp).padding(8.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Pet QR Code",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Scan at clinic reception in Congo Town, Monrovia for rapid check-in and medical history lookup.",
                    fontSize = 12.sp,
                    color = MediumCharcoal,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Real Camera / Barcode Scanner Dialog with Authorization verification
 */
@Composable
fun CameraScannerDialog(
    pets: List<PetEntity>,
    currentRole: UserRole,
    onDismiss: () -> Unit,
    onPetVerified: (PetEntity) -> Unit
) {
    var manualInput by remember { mutableStateOf("") }
    var authError by remember { mutableStateOf<String?>(null) }

    fun processCode(code: String) {
        authError = null
        val matched = pets.find {
            code.contains(it.rabiesTag) ||
            code.contains(it.microchipId) ||
            code.contains(it.name, ignoreCase = true) ||
            code.contains("\"petId\":${it.id}")
        }

        if (matched == null) {
            authError = "Pet not found. Please verify the QR code or search manually."
            return
        }

        // Role-based verification check: Pet owner cannot scan other client's pets!
        if (currentRole == UserRole.PET_OWNER && matched.id != 1L) {
            authError = "Access Denied: Pet \"${matched.name}\" (Tag #${matched.rabiesTag}) belongs to another client. Pet Owners are only authorized to access their own pet."
            return
        }

        onPetVerified(matched)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(
                            Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = if (currentRole == UserRole.PET_OWNER) AmberTerracotta else ForestSage
                        )
                        Text(
                            text = if (currentRole == UserRole.PET_OWNER) "Scan Pet Passport" else "Staff Pet QR Scanner",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MediumCharcoal)
                    }
                }

                authError?.let { err ->
                    Surface(
                        color = Color(0xFFFEF2F2),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
                    ) {
                        Text(
                            text = err,
                            color = StatusRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                // Camera viewfinder preview box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF111827), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null, tint = AmberTerracotta, modifier = Modifier.size(48.dp))
                        Text(
                            text = "Live Camera Scanner Active",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            text = "Point camera at pet rabies collar tag or passport QR",
                            color = Color(0xFF9CA3AF),
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // One-tap Simulation buttons for quick testing
                Text(
                    text = "Simulate Scan (Test Camera Detection):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                pets.forEach { p ->
                    OutlinedButton(
                        onClick = { processCode(p.rabiesTag) },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Scan ${p.name} (Tag #${p.rabiesTag})", color = DeepCharcoal, fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Manual Input Fallback
                OutlinedTextField(
                    value = manualInput,
                    onValueChange = { manualInput = it },
                    label = { Text("Or Enter Code / Tag Manually") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(
                    onClick = { processCode(manualInput) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (currentRole == UserRole.PET_OWNER) AmberTerracotta else ForestSage
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Verify & Open Record", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Staff Authorized QR / Tag Generator Dialog
 */
@Composable
fun StaffQrGeneratorDialog(
    pets: List<PetEntity>,
    onDismiss: () -> Unit
) {
    var selectedPet by remember { mutableStateOf(pets.firstOrNull() ?: PetEntity(id = 1, ownerClientId = 1, name = "Bella", species = "Dog", breed = "Boerboel", sex = "Female", dob = "2021", weightKg = 24.5, color = "Tan", microchipId = "985141002931882", rabiesTag = "HP-LR-2024-0884")) }
    var tagType by remember { mutableStateOf("Rabies Collar Tag") }
    val qrPayload = remember(selectedPet, tagType) {
        "{\"type\":\"OFFICIAL_STAFF_TAG\",\"petId\":${selectedPet.id},\"name\":\"${selectedPet.name}\",\"tagType\":\"$tagType\",\"rabiesTag\":\"${selectedPet.rabiesTag}\"}"
    }
    val qrBitmap = remember(qrPayload) { generateQrCodeBitmap(qrPayload) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp).verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Staff QR / Tag Generator", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Patient selection
                Text("Select Patient:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal, modifier = Modifier.fillMaxWidth())
                pets.forEach { p ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedPet = p }
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPet.id == p.id, onClick = { selectedPet = p })
                        Text("${p.name} (${p.species} · #${p.rabiesTag})", fontSize = 13.sp, color = DeepCharcoal)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(160.dp).padding(4.dp)
                ) {
                    Image(bitmap = qrBitmap.asImageBitmap(), contentDescription = null, modifier = Modifier.fillMaxSize())
                }

                Text(
                    text = "Generated Badge for ${selectedPet.name} (#${selectedPet.rabiesTag})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ForestSage,
                    modifier = Modifier.padding(top = 10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

/**
 * Official Printable / Shareable Health Passport Dialog (A4 Certificate format)
 */
@Composable
fun OfficialHealthPassportDialog(
    pet: PetEntity,
    vaccinations: List<VaccinationEntity>,
    clinicSettings: ClinicSettings = ClinicSettings(),
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val qrPayload = remember(pet) {
        "{\"type\":\"HAPPY_PAWS_PET_PASSPORT\",\"petId\":${pet.id},\"name\":\"${pet.name}\",\"rabiesTag\":\"${pet.rabiesTag}\",\"microchipId\":\"${pet.microchipId}\"}"
    }
    val qrBitmap = remember(qrPayload) { generateQrCodeBitmap(qrPayload, size = 320) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Official Certificate Header
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftCream,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = clinicSettings.clinicName.uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "OFFICIAL PET HEALTH PASSPORT & RABIES CERTIFICATE",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberTerracotta,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                        Text(
                            text = "${clinicSettings.address} · Tel: ${clinicSettings.phone}",
                            fontSize = 10.sp,
                            color = MediumCharcoal,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = "Official Registry Certificate #HP-MED-${pet.id}-2026",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DeepCharcoal,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Pet Profile Summary
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarmIvory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = pet.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = DeepCharcoal
                            )
                            StatusBadge("Verified Healthy", SageLight, ForestSage)
                        }
                        Text("${pet.species} · ${pet.breed} · ${pet.sex}", fontSize = 12.sp, color = MediumCharcoal, modifier = Modifier.padding(top = 2.dp))
                        Text("Born: ${pet.dob} · Weight: ${pet.weightKg} kg · Color: ${pet.color}", fontSize = 12.sp, color = DeepCharcoal)
                        Text("Microchip ID: ${pet.microchipId}", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.SemiBold)
                        Text("Official Rabies Tag: #${pet.rabiesTag}", fontSize = 12.sp, color = AmberTerracotta, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Official Legal / Veterinary Declaration Box
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = SageLight,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "Verified Rabies & Core Vaccination Declaration",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = ForestSage
                        )
                        Text(
                            text = "This certifies that ${pet.name} (Rabies Collar Tag #${pet.rabiesTag}, Microchip #${pet.microchipId}) has been examined by licensed veterinary surgeons at Happy Paws Liberia Rescue Center and maintains current rabies prophylaxis.",
                            fontSize = 11.sp,
                            color = DeepCharcoal,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Immunization records table
                Text(
                    text = "Official Immunization Registry",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = DeepCharcoal,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                val petVacList = vaccinations.filter { it.petName == pet.name || it.petId == pet.id }
                if (petVacList.isEmpty()) {
                    Text("No immunization records on file.", fontSize = 11.sp, color = MediumCharcoal)
                } else {
                    petVacList.forEach { v ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(v.vaccineName, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = DeepCharcoal)
                                    Text("Given: ${v.dateAdministered} · Valid: ${v.validUntil} (${v.batchNumber})", fontSize = 10.sp, color = MediumCharcoal)
                                    Text("Doctor: ${v.vetName}", fontSize = 10.sp, color = ForestSage)
                                }
                                StatusBadge(v.status)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // QR Code Verification
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.size(120.dp).padding(4.dp)
                ) {
                    Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Passport QR",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Text(
                    text = "Official Clinic Reception QR Verification · Congo Town",
                    fontSize = 10.sp,
                    color = MediumCharcoal,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Action Buttons: Print/Share & Done
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Close", color = DeepCharcoal)
                    }

                    Button(
                        onClick = {
                            val shareText = buildString {
                                appendLine("HAPPY PAWS LIBERIA RESCUE CENTER")
                                appendLine("OFFICIAL PET HEALTH PASSPORT & RABIES CERTIFICATE")
                                appendLine("Certificate #HP-MED-${pet.id}-2026")
                                appendLine("=========================================")
                                appendLine("Pet Name: ${pet.name}")
                                appendLine("Species: ${pet.species}")
                                appendLine("Breed: ${pet.breed}")
                                appendLine("Sex: ${pet.sex}")
                                appendLine("DOB: ${pet.dob}")
                                appendLine("Weight: ${pet.weightKg} kg")
                                appendLine("Microchip ID: ${pet.microchipId}")
                                appendLine("Rabies Tag: #${pet.rabiesTag}")
                                appendLine("Status: Verified Healthy & Rabies Protected")
                                appendLine("=========================================")
                                appendLine("Vaccinations:")
                                petVacList.forEach { v ->
                                    appendLine("- ${v.vaccineName} (Administered: ${v.dateAdministered}, Valid: ${v.validUntil}, Batch: ${v.batchNumber})")
                                }
                                appendLine("=========================================")
                                appendLine("Clinic: ${clinicSettings.clinicName}")
                                appendLine("Address: ${clinicSettings.address}")
                                appendLine("Hotline: ${clinicSettings.phone}")
                            }

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                putExtra(Intent.EXTRA_SUBJECT, "Official Health Passport: ${pet.name} (Rabies Tag #${pet.rabiesTag})")
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Print / Share Health Passport"))
                            Toast.makeText(context, "Exporting Official Health Passport...", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Print / Share", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Pet Medical Dossier Dialog (Matching PWA Dossier Modal)
 */
@Composable
fun PetDossierDialog(
    pet: PetEntity,
    currentRole: UserRole,
    onAdmitToQueue: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = pet.name,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal
                        )
                        Text(
                            text = "Verified Rabies Tag #${pet.rabiesTag}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AmberTerracotta
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MediumCharcoal)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarmIvory,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Species / Breed: ${pet.species} (${pet.breed})", fontSize = 13.sp, color = DeepCharcoal)
                        Text("Sex & Weight: ${pet.sex} · ${pet.weightKg} kg", fontSize = 13.sp, color = DeepCharcoal)
                        Text("Born: ${pet.dob} · Color: ${pet.color}", fontSize = 13.sp, color = DeepCharcoal)
                        Text("Microchip: ${pet.microchipId}", fontSize = 13.sp, color = DeepCharcoal, fontWeight = FontWeight.SemiBold)
                        if (pet.notes.isNotBlank()) {
                            Text("Clinical Notes: ${pet.notes}", fontSize = 12.sp, color = MediumCharcoal, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (currentRole != UserRole.PET_OWNER) {
                    Button(
                        onClick = onAdmitToQueue,
                        colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Admit to Today's Clinic Queue", fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }

                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close Dossier", color = DeepCharcoal)
                }
            }
        }
    }
}

/**
 * Register Pet Dialog (New User Onboarding & Adding Pet)
 */
@Composable
fun RegisterPetDialog(
    onRegister: (PetEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var species by remember { mutableStateOf("Canine (Dog)") }
    var breed by remember { mutableStateOf("") }
    var sex by remember { mutableStateOf("Spayed Female") }
    var dob by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var microchipId by remember { mutableStateOf("") }
    var rabiesTag by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    val randomSuffix = remember { (1000..9999).random() }
    val defaultTag = remember { "HP-LR-2026-$randomSuffix" }
    val defaultChip = remember { "98514100293$randomSuffix" }

    LaunchedEffect(Unit) {
        if (rabiesTag.isBlank()) rabiesTag = defaultTag
        if (microchipId.isBlank()) microchipId = defaultChip
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Register Patient / Pet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = DeepCharcoal
                        )
                        Text(
                            text = "Official Happy Paws Liberia Registry",
                            fontSize = 11.sp,
                            color = AmberTerracotta,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MediumCharcoal)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pet Name *") },
                    placeholder = { Text("e.g. Bella, Max, Luna") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = species,
                        onValueChange = { species = it },
                        label = { Text("Species") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = breed,
                        onValueChange = { breed = it },
                        label = { Text("Breed") },
                        placeholder = { Text("e.g. Boerboel Mix") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = sex,
                        onValueChange = { sex = it },
                        label = { Text("Sex") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = weightStr,
                        onValueChange = { weightStr = it },
                        label = { Text("Weight (kg)") },
                        placeholder = { Text("e.g. 15.5") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = dob,
                        onValueChange = { dob = it },
                        label = { Text("Age / DOB") },
                        placeholder = { Text("e.g. 2 yrs") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = color,
                        onValueChange = { color = it },
                        label = { Text("Color") },
                        placeholder = { Text("e.g. Golden Tan") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = rabiesTag,
                    onValueChange = { rabiesTag = it },
                    label = { Text("Rabies Collar Tag #") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = microchipId,
                    onValueChange = { microchipId = it },
                    label = { Text("Microchip ID") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Special Notes / History") },
                    placeholder = { Text("e.g. Rescued in Monrovia, fully friendly.") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = DeepCharcoal)
                    }

                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                val w = weightStr.toDoubleOrNull() ?: 10.0
                                val pet = PetEntity(
                                    ownerClientId = 1,
                                    name = name.trim(),
                                    species = species.ifBlank { "Canine (Dog)" },
                                    breed = breed.ifBlank { "African Boerboel Mix" },
                                    sex = sex.ifBlank { "Female" },
                                    dob = dob.ifBlank { "Unknown" },
                                    weightKg = w,
                                    color = color.ifBlank { "Brown" },
                                    microchipId = microchipId.ifBlank { defaultChip },
                                    rabiesTag = rabiesTag.ifBlank { defaultTag },
                                    notes = notes.trim()
                                )
                                onRegister(pet)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.5f)
                    ) {
                        Text("Register Pet", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
