package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.PetEntity
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetOnboardingWizardDialog(
    ownerId: Long,
    ownerName: String,
    onSavePet: (PetEntity, Boolean) -> Unit, // pet, addAnother
    onDismiss: () -> Unit
) {
    var step by remember { mutableIntStateOf(1) } // 1: Welcome/Call You, 2: Species, 3: Pet Name, 4: Breed, 5: Details
    var preferredName by remember { mutableStateOf(ownerName) }
    var selectedSpecies by remember { mutableStateOf("Dog") }
    var petName by remember { mutableStateOf("") }
    var breedSearchQuery by remember { mutableStateOf("") }
    var selectedBreed by remember { mutableStateOf("Mixed Breed") }
    var customBreedText by remember { mutableStateOf("") }
    var selectedSex by remember { mutableStateOf("Male") }
    var dobOrAge by remember { mutableStateOf("") }
    var color by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var microchipId by remember { mutableStateOf("") }
    var isRescuePet by remember { mutableStateOf(false) }

    val speciesList = listOf(
        "Dog", "Cat", "Rabbit", "Bird", "Hamster", "Guinea Pig", "Turtle", "Horse", "Other"
    )

    val dogBreeds = listOf("African Village Dog", "Boerboel", "German Shepherd", "Golden Retriever", "Labrador Retriever", "Mixed Breed", "Pitbull Terrier", "Rottweiler", "Unknown", "Other")
    val catBreeds = listOf("Bengal", "Calico", "Domestic Longhair", "Domestic Shorthair", "Mixed Breed", "Persian", "Siamese", "Tabby", "Unknown", "Other")
    val defaultBreeds = listOf("Standard", "Dwarf", "Mixed Breed", "Unknown", "Other")

    val currentBreeds = when (selectedSpecies) {
        "Dog" -> dogBreeds
        "Cat" -> catBreeds
        else -> defaultBreeds
    }.filter { it.contains(breedSearchQuery, ignoreCase = true) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .wrapContentHeight()
                .padding(vertical = 20.dp),
            shape = RoundedCornerShape(24.dp),
            color = WarmIvory,
            border = BorderStroke(1.dp, BorderSubtle)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header & Progress Indicator
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Pet Onboarding",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontFamily = FontFamily.Serif,
                                fontWeight = FontWeight.Bold
                            ),
                            color = DeepCharcoal
                        )
                        Text(
                            text = "Step $step of 5",
                            style = MaterialTheme.typography.labelSmall,
                            color = AmberTerracotta
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = SoftSlate)
                    }
                }

                LinearProgressIndicator(
                    progress = { step / 5f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = AmberTerracotta,
                    trackColor = WarmSand
                )

                // Step Contents
                when (step) {
                    1 -> {
                        // Step 1 & 2: Preferred Name
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HappyPawsLogo(size = 70.dp, showTagline = false)
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = "Welcome to Happy Paws!",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "What would you like us to call you?",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MediumCharcoal,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = preferredName,
                                onValueChange = { preferredName = it },
                                label = { Text("Your Preferred Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_preferred_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmberTerracotta,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(
                                onClick = { step = 2 },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("onboarding_step1_next"),
                                colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                            ) {
                                Text("Continue", style = MaterialTheme.typography.labelLarge)
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    2 -> {
                        // Step 3: Species Selection
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "What type of pet do you have?",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal
                            )
                            Text(
                                text = "Choose the species that best describes your companion.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftSlate
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            LazyVerticalGrid(
                                columns = GridCells.Fixed(3),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.height(200.dp)
                            ) {
                                items(speciesList) { species ->
                                    val isSelected = selectedSpecies == species
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (isSelected) AmberTerracotta else CardWarmSurface,
                                        border = BorderStroke(1.dp, if (isSelected) AmberTerracotta else BorderSubtle),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedSpecies = species }
                                            .testTag("species_$species")
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Pets,
                                                contentDescription = species,
                                                tint = if (isSelected) Color.White else AmberTerracotta,
                                                modifier = Modifier.size(24.dp)
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = species,
                                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                                color = if (isSelected) Color.White else DeepCharcoal,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { step = 1 },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("Back")
                                }
                                Button(
                                    onClick = { step = 3 },
                                    modifier = Modifier.weight(1.5f).height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                                ) {
                                    Text("Next: Pet Name")
                                }
                            }
                        }
                    }

                    3 -> {
                        // Step 4: Pet Name
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "What is your ${selectedSpecies.lowercase()}'s name?",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedTextField(
                                value = petName,
                                onValueChange = { petName = it },
                                label = { Text("Pet Name (e.g. Bella, Kofi, Simba)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("onboarding_pet_name_input"),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = AmberTerracotta,
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { step = 2 },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("Back")
                                }
                                Button(
                                    onClick = { if (petName.isNotBlank()) step = 4 },
                                    enabled = petName.isNotBlank(),
                                    modifier = Modifier.weight(1.5f).height(48.dp).testTag("onboarding_step3_next"),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                                ) {
                                    Text("Next: Select Breed")
                                }
                            }
                        }
                    }

                    4 -> {
                        // Step 5: Breed Selector
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Select ${petName.ifEmpty { "Pet" }}'s Breed",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal
                            )
                            Text(
                                text = "Searchable list of $selectedSpecies breeds.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftSlate
                            )
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = breedSearchQuery,
                                onValueChange = { breedSearchQuery = it },
                                placeholder = { Text("Search breeds...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    unfocusedContainerColor = Color.White,
                                    focusedContainerColor = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
                                    .verticalScroll(rememberScrollState())
                            ) {
                                currentBreeds.forEach { breed ->
                                    val isSelected = selectedBreed == breed
                                    Surface(
                                        color = if (isSelected) SoftSage else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedBreed = breed }
                                            .padding(vertical = 2.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = breed,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                                ),
                                                color = if (isSelected) ForestSage else DeepCharcoal
                                            )
                                            if (isSelected) {
                                                Icon(Icons.Default.Check, contentDescription = null, tint = ForestSage, modifier = Modifier.size(18.dp))
                                            }
                                        }
                                    }
                                }
                            }

                            if (selectedBreed == "Other") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customBreedText,
                                    onValueChange = { customBreedText = it },
                                    label = { Text("Enter Custom Breed") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = { step = 3 },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Text("Back")
                                }
                                Button(
                                    onClick = { step = 5 },
                                    modifier = Modifier.weight(1.5f).height(48.dp).testTag("onboarding_step4_next"),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                                ) {
                                    Text("Next: Details")
                                }
                            }
                        }
                    }

                    5 -> {
                        // Step 6: Optional Details & Finish
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            Text(
                                text = "Pet Details & Health Info",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal
                            )
                            Text(
                                text = "All fields below are optional and can be updated later.",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftSlate
                            )
                            Spacer(modifier = Modifier.height(14.dp))

                            // Sex Selector
                            Text("Sex:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Male", "Female", "Spayed/Neutered").forEach { sexOption ->
                                    val isSel = selectedSex == sexOption
                                    FilterChip(
                                        selected = isSel,
                                        onClick = { selectedSex = sexOption },
                                        label = { Text(sexOption) },
                                        modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedTextField(
                                value = dobOrAge,
                                onValueChange = { dobOrAge = it },
                                label = { Text("Approximate Age / DOB (e.g. 2 yrs)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = weightText,
                                onValueChange = { weightText = it },
                                label = { Text("Current Weight (kg)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = color,
                                onValueChange = { color = it },
                                label = { Text("Color / Markings") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = microchipId,
                                onValueChange = { microchipId = it },
                                label = { Text("Microchip ID (Optional)") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { isRescuePet = !isRescuePet }
                                    .padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = isRescuePet,
                                    onCheckedChange = { isRescuePet = it }
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "This pet is a rescue / adopted animal in Liberia",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = DeepCharcoal
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            // Action Buttons: Add Another Pet or Finish
                            val finalBreed = if (selectedBreed == "Other" && customBreedText.isNotBlank()) customBreedText else selectedBreed
                            val weightVal = weightText.toDoubleOrNull() ?: 0.0

                            val newPet = PetEntity(
                                ownerId = ownerId,
                                name = petName.ifEmpty { "My Pet" },
                                species = selectedSpecies,
                                breed = finalBreed,
                                sex = selectedSex,
                                ageDisplay = dobOrAge,
                                color = color,
                                weightKg = weightVal,
                                microchipId = microchipId,
                                isRescuePet = isRescuePet
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        onSavePet(newPet, true)
                                        // Reset fields for next pet
                                        petName = ""
                                        selectedBreed = "Mixed Breed"
                                        customBreedText = ""
                                        weightText = ""
                                        step = 2
                                    },
                                    modifier = Modifier.weight(1f).height(48.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Another", maxLines = 1)
                                }

                                Button(
                                    onClick = {
                                        onSavePet(newPet, false)
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1.2f).height(48.dp).testTag("onboarding_finish_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                                ) {
                                    Text("Complete & View")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
