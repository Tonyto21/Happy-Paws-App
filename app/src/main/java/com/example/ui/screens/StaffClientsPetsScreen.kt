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
import com.example.data.local.PetEntity
import com.example.model.UserRole
import com.example.ui.components.OfficialHealthPassportDialog
import com.example.ui.components.PetDossierDialog
import com.example.ui.components.PetQrPassportDialog
import com.example.ui.components.RegisterPetDialog
import com.example.ui.components.StaffQrGeneratorDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel

@Composable
fun StaffClientsPetsScreen(
    viewModel: HappyPawsViewModel
) {
    val context = LocalContext.current
    val pets by viewModel.allPets.collectAsState()
    val vaccinations by viewModel.allVaccinations.collectAsState()
    val clinicSettings by viewModel.clinicSettings.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedQrPet by remember { mutableStateOf<PetEntity?>(null) }
    var selectedDossierPet by remember { mutableStateOf<PetEntity?>(null) }
    var selectedPassportPet by remember { mutableStateOf<PetEntity?>(null) }
    var showStaffGenerator by remember { mutableStateOf(false) }
    var showRegisterPetDialog by remember { mutableStateOf(false) }

    val filteredPets = remember(pets, searchQuery) {
        if (searchQuery.isBlank()) pets
        else {
            pets.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.rabiesTag.contains(searchQuery, ignoreCase = true) ||
                it.breed.contains(searchQuery, ignoreCase = true) ||
                it.microchipId.contains(searchQuery, ignoreCase = true)
            }
        }
    }

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
                    text = "Clients & Registered Patients",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                Text(
                    text = "Patient registry & scan badge generation",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumCharcoal
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Register Pet
                OutlinedButton(
                    onClick = { showRegisterPetDialog = true },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = DeepCharcoal)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Register Pet", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.Bold)
                }

                // Generate Tag / QR
                Button(
                    onClick = { showStaffGenerator = true },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tag / QR", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by pet name, rabies tag #, breed, or microchip...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MediumCharcoal) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = MediumCharcoal)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = DeepCharcoal,
                unfocusedTextColor = DeepCharcoal,
                focusedBorderColor = AmberTerracotta,
                unfocusedBorderColor = Color(0xFFD1D5DB)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        if (filteredPets.isEmpty()) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Box(
                    modifier = Modifier.padding(32.dp).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Pets, contentDescription = null, tint = ForestSage, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching patients found" else "No registered pets in registry",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = DeepCharcoal
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search query." else "Tap 'Register Pet' above to add a patient to the clinic registry.",
                            fontSize = 13.sp,
                            color = MediumCharcoal,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredPets) { pet ->
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
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Text(pet.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                                        StatusBadge("Tag #${pet.rabiesTag}", TerracottaLight, AmberTerracottaDark)
                                    }
                                    Text("${pet.species} · ${pet.breed} · ${pet.sex}", fontSize = 12.sp, color = MediumCharcoal, modifier = Modifier.padding(top = 2.dp))
                                    Text("Microchip: ${pet.microchipId}", fontSize = 12.sp, color = DeepCharcoal, fontWeight = FontWeight.SemiBold)
                                    Text("Weight: ${pet.weightKg} kg · Owner: ${if (pet.id == 1L) "Anthony Tolbert" else "Kofa Weah"}", fontSize = 12.sp, color = MediumCharcoal)
                                }

                                IconButton(onClick = { selectedQrPet = pet }) {
                                    Icon(Icons.Default.QrCode, contentDescription = "View QR", tint = AmberTerracotta)
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = { selectedDossierPet = pet },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("View Dossier", fontSize = 11.sp, color = DeepCharcoal, fontWeight = FontWeight.SemiBold)
                                }

                                Button(
                                    onClick = { selectedPassportPet = pet },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Passport", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // QR Passport Dialog
    selectedQrPet?.let { pet ->
        PetQrPassportDialog(pet = pet, onDismiss = { selectedQrPet = null })
    }

    // Medical Dossier Dialog
    selectedDossierPet?.let { pet ->
        PetDossierDialog(
            pet = pet,
            currentRole = UserRole.RECEPTIONIST,
            onAdmitToQueue = {
                viewModel.checkInPetToday(pet.id, "Owner of ${pet.name}", pet.name, pet.species)
                selectedDossierPet = null
                Toast.makeText(context, "${pet.name} admitted to Today's Clinic Queue.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { selectedDossierPet = null }
        )
    }

    // Official A4 Health Passport Dialog
    selectedPassportPet?.let { pet ->
        OfficialHealthPassportDialog(
            pet = pet,
            vaccinations = vaccinations,
            clinicSettings = clinicSettings,
            onDismiss = { selectedPassportPet = null }
        )
    }

    // Staff QR Generator Dialog
    if (showStaffGenerator) {
        StaffQrGeneratorDialog(pets = pets, onDismiss = { showStaffGenerator = false })
    }

    // Register Pet Dialog
    if (showRegisterPetDialog) {
        RegisterPetDialog(
            onRegister = { newPet ->
                viewModel.addPet(newPet)
                showRegisterPetDialog = false
                Toast.makeText(context, "${newPet.name} registered to client.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showRegisterPetDialog = false }
        )
    }
}
