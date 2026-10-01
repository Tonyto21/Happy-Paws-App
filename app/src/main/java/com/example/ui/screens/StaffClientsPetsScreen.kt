package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ClientEntity
import com.example.data.local.PetEntity
import com.example.ui.components.PetAvatar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffClientsPetsScreen(
    clients: List<ClientEntity>,
    pets: List<PetEntity>,
    onAddClientAndPet: (ClientEntity, PetEntity) -> Unit,
    onSelectPetForClinical: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var showRegisterDialog by remember { mutableStateOf(false) }

    val filteredClients = clients.filter { client ->
        val clientPets = pets.filter { it.ownerId == client.id }
        client.fullName.contains(searchQuery, ignoreCase = true) ||
        client.phone.contains(searchQuery, ignoreCase = true) ||
        clientPets.any {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.microchipId.contains(searchQuery, ignoreCase = true) ||
            it.breed.contains(searchQuery, ignoreCase = true)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header & Search
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Clients & Patients",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepCharcoal
                )
                Text(
                    text = "${clients.size} Registered Guardians • ${pets.size} Patients",
                    style = MaterialTheme.typography.bodySmall,
                    color = SoftSlate
                )
            }

            Button(
                onClick = { showRegisterDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("register_client_btn")
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("New Client")
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Multi-Field Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by owner, pet name, phone, or microchip...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = SoftSlate) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("client_search_input"),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedContainerColor = CardWarmSurface,
                focusedContainerColor = CardWarmSurface,
                focusedBorderColor = AmberTerracotta
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            if (filteredClients.isEmpty()) {
                item {
                    Surface(
                        color = CardWarmSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.PersonSearch, contentDescription = null, tint = SoftSlate, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matching clients or pets found", color = MediumCharcoal)
                        }
                    }
                }
            } else {
                items(filteredClients) { client ->
                    val clientPets = pets.filter { it.ownerId == client.id }

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
                                    Text(
                                        text = client.fullName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Phone: ${client.phone} • ${client.address.ifEmpty { "Monrovia" }}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftSlate
                                    )
                                }
                                Surface(
                                    color = SoftSage,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${clientPets.size} ${if (clientPets.size == 1) "pet" else "pets"}",
                                        color = ForestSage,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Registered Pets:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = SoftSlate
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            clientPets.forEach { pet ->
                                Surface(
                                    color = SoftCream,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 3.dp)
                                        .clickable { onSelectPetForClinical(pet.id) }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            PetAvatar(name = pet.name, species = pet.species, size = 32)
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    Text(
                                                        text = pet.name,
                                                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                                        color = DeepCharcoal
                                                    )
                                                    if (pet.isRescuePet) {
                                                        Text("• Rescue", color = ForestSage, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                                                    }
                                                }
                                                Text(
                                                    text = "${pet.species} • ${pet.breed} • ${pet.weightKg} kg",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MediumCharcoal
                                                )
                                            }
                                        }
                                        Icon(Icons.Default.ChevronRight, contentDescription = "View", tint = SoftSlate)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Register New Client & Pet Dialog
    if (showRegisterDialog) {
        var ownerName by remember { mutableStateOf("") }
        var ownerPhone by remember { mutableStateOf("") }
        var ownerEmail by remember { mutableStateOf("") }
        var ownerAddress by remember { mutableStateOf("") }

        var petName by remember { mutableStateOf("") }
        var petSpecies by remember { mutableStateOf("Dog") }
        var petBreed by remember { mutableStateOf("African Village Dog") }
        var petSex by remember { mutableStateOf("Male") }
        var petWeight by remember { mutableStateOf("15.0") }
        var isRescue by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showRegisterDialog = false },
            title = {
                Text(
                    text = "Register New Client & Patient",
                    style = MaterialTheme.typography.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold)
                )
            },
            text = {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text("1. Client Details", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = AmberTerracotta)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = ownerName, onValueChange = { ownerName = it }, label = { Text("Client Full Name *") }, modifier = Modifier.fillMaxWidth().testTag("reg_client_name"))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = ownerPhone, onValueChange = { ownerPhone = it }, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth().testTag("reg_client_phone"))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = ownerEmail, onValueChange = { ownerEmail = it }, label = { Text("Email Address") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = ownerAddress, onValueChange = { ownerAddress = it }, label = { Text("Address (e.g. Sinkor, Monrovia)") }, modifier = Modifier.fillMaxWidth())
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("2. First Pet Details", style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold), color = AmberTerracotta)
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedTextField(value = petName, onValueChange = { petName = it }, label = { Text("Pet Name *") }, modifier = Modifier.fillMaxWidth().testTag("reg_pet_name"))
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = petSpecies, onValueChange = { petSpecies = it }, label = { Text("Species (Dog, Cat, etc.)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = petBreed, onValueChange = { petBreed = it }, label = { Text("Breed") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        OutlinedTextField(value = petWeight, onValueChange = { petWeight = it }, label = { Text("Weight (kg)") }, modifier = Modifier.fillMaxWidth())
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { isRescue = !isRescue }
                        ) {
                            Checkbox(checked = isRescue, onCheckedChange = { isRescue = it })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Adopted / Rescue Animal in Liberia", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (ownerName.isNotBlank() && ownerPhone.isNotBlank() && petName.isNotBlank()) {
                            val client = ClientEntity(
                                fullName = ownerName,
                                preferredName = ownerName.split(" ").first(),
                                phone = ownerPhone,
                                email = ownerEmail,
                                address = ownerAddress
                            )
                            val pet = PetEntity(
                                ownerId = 0, // Assigned by repository
                                name = petName,
                                species = petSpecies,
                                breed = petBreed,
                                sex = petSex,
                                weightKg = petWeight.toDoubleOrNull() ?: 10.0,
                                isRescuePet = isRescue
                            )
                            onAddClientAndPet(client, pet)
                            showRegisterDialog = false
                            Toast.makeText(context, "Client and Pet registered successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Please enter client name, phone and pet name", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                    modifier = Modifier.testTag("save_client_pet_button")
                ) {
                    Text("Save & Register")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRegisterDialog = false }) { Text("Cancel") }
            }
        )
    }
}
