package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.ClientEntity
import com.example.data.local.ClinicSettingsEntity
import com.example.data.local.DewormingEntity
import com.example.data.local.PetEntity
import com.example.data.local.VaccinationEntity
import com.example.ui.theme.*
import com.example.util.PdfPrintHelper

@Composable
fun PrintPassportDialog(
    pet: PetEntity,
    owner: ClientEntity?,
    vaccines: List<VaccinationEntity>,
    dewormings: List<DewormingEntity>,
    clinicSettings: ClinicSettingsEntity? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f)
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(16.dp),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(SoftCream)
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = null,
                            tint = ForestSage
                        )
                        Text(
                            text = "Official Pet Health Passport",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif
                            ),
                            color = DeepCharcoal
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                PdfPrintHelper.printPassport(
                                    context = context,
                                    pet = pet,
                                    owner = owner,
                                    vaccines = vaccines,
                                    dewormings = dewormings,
                                    clinicSettings = clinicSettings
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("print_button")
                        ) {
                            Icon(Icons.Default.Print, contentDescription = "Print", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Print / PDF", style = MaterialTheme.typography.labelMedium)
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = DeepCharcoal)
                        }
                    }
                }

                // Printable Passport Body
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header with Happy Paws Emblem
                    HappyPawsLogo(size = 72.dp, showTagline = false)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "HAPPY PAWS LIBERIA",
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            letterSpacing = 1.sp
                        ),
                        color = DeepCharcoal
                    )
                    Text(
                        text = "RESCUE CENTER & VETERINARY CLINIC",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp
                        ),
                        color = AmberTerracotta
                    )
                    Text(
                        text = "Tubman Blvd, Congo Town, Monrovia • VET: 0881479329",
                        style = MaterialTheme.typography.bodySmall,
                        color = SoftSlate
                    )

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = DeepCharcoal, thickness = 1.5.dp)
                    Spacer(modifier = Modifier.height(14.dp))

                    // Passport Certificate Banner
                    Surface(
                        color = LightIvoryTint,
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("PASSPORT NO.", style = MaterialTheme.typography.labelSmall, color = SoftSlate)
                                Text("HPL-${pet.id.toString().padStart(5, '0')}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif), color = DeepCharcoal)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("MICROCHIP ID", style = MaterialTheme.typography.labelSmall, color = SoftSlate)
                                Text(pet.microchipId.ifEmpty { "Pending Implant" }, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ForestSage)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Patient Details Section
                    Text(
                        text = "1. PATIENT IDENTIFICATION",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = DeepCharcoal,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            PassportRow("Pet Name:", pet.name, "Species:", pet.species)
                            PassportRow("Breed:", pet.breed, "Sex:", pet.sex)
                            PassportRow("Date of Birth:", pet.dateOfBirth.ifEmpty { pet.ageDisplay }, "Weight:", "${pet.weightKg} kg")
                            PassportRow("Coat / Color:", pet.color.ifEmpty { "Standard" }, "Rescue Pet:", if (pet.isRescuePet) "Yes (Liberia Rescue)" else "Client Owned")
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Owner Details
                    Text(
                        text = "2. REGISTERED GUARDIAN / OWNER",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = DeepCharcoal,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            PassportRow("Full Name:", owner?.fullName ?: "N/A", "Phone:", owner?.phone ?: "N/A")
                            PassportRow("Address:", owner?.address ?: "Monrovia, Liberia", "Email:", owner?.email ?: "N/A")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Vaccination Record Table
                    Text(
                        text = "3. OFFICIAL VACCINATION CERTIFICATIONS",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = DeepCharcoal,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (vaccines.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SoftCream,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "No vaccinations recorded yet. Schedule appointment at Happy Paws Clinic.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MediumCharcoal,
                                modifier = Modifier.padding(12.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        ) {
                            // Table Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftCream)
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Vaccine / Batch", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1.5f))
                                Text("Given Date", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                Text("Next Due", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                                Text("Vet Signature", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.weight(1f))
                            }
                            HorizontalDivider(color = BorderSubtle)
                            vaccines.forEach { v ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(v.vaccineName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold), color = DeepCharcoal)
                                        if (v.batchLotNumber.isNotEmpty()) {
                                            Text("Lot: ${v.batchLotNumber}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                                        }
                                    }
                                    Text(v.administeredDate, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text(v.nextDueDate, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = ForestSage, modifier = Modifier.weight(1f))
                                    Text("Dr. Sackor", style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.Serif), color = AmberTerracotta, modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Deworming & Parasite Table
                    Text(
                        text = "4. DEWORMING & PARASITE PREVENTION",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif),
                        color = DeepCharcoal,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    if (dewormings.isNotEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        ) {
                            dewormings.forEach { d ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1.5f)) {
                                        Text(d.productName, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                                        Text("Dose: ${d.dosage} (${d.weightKg} kg)", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                                    }
                                    Text("Given: ${d.administeredDate}", style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                                    Text("Due: ${d.nextDueDate}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium), color = AmberTerracotta, modifier = Modifier.weight(1f))
                                }
                                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Official Stamp Box
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, DeepCharcoal, RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("OFFICIAL CLINIC VALIDATION", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            Text("Happy Paws Liberia Rescue Center", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                            Text("Verified Veterinarian: Dr. Emmanuel Sackor, DVM", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                        }
                        Box(
                            modifier = Modifier
                                .border(2.dp, AmberTerracotta, RoundedCornerShape(6.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "HAPPY PAWS\nSEAL OF HEALTH",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 9.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = AmberTerracotta,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PassportRow(label1: String, val1: String, label2: String, val2: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.weight(1f)) {
            Text(label1, style = MaterialTheme.typography.labelSmall, color = SoftSlate, modifier = Modifier.width(85.dp))
            Text(val1, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = DeepCharcoal)
        }
        Row(modifier = Modifier.weight(1f)) {
            Text(label2, style = MaterialTheme.typography.labelSmall, color = SoftSlate, modifier = Modifier.width(80.dp))
            Text(val2, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium), color = DeepCharcoal)
        }
    }
}
