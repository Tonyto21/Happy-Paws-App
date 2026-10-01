package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PetEntity
import com.example.ui.components.HappyPawsLogo
import com.example.ui.components.PetOnboardingWizardDialog
import com.example.ui.theme.*

@Composable
fun CompleteProfileScreen(
    userEmail: String,
    userName: String,
    onLinkClinicRecord: (phoneOrEmail: String, claimCode: String) -> Unit,
    onSaveNewPet: (pet: PetEntity, phone: String) -> Unit,
    onSignOut: () -> Unit,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showLinkDialog by remember { mutableStateOf(false) }
    var showOnboardingWizard by remember { mutableStateOf(false) }
    
    var phoneInput by remember { mutableStateOf("") }
    var claimCodeInput by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HappyPawsLogo(size = 80.dp, showTagline = false)
            Spacer(modifier = Modifier.height(20.dp))

            Surface(
                color = CardWarmSurface,
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, BorderSubtle),
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Welcome to Happy Paws!",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = DeepCharcoal,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Hello $userName, how would you like to set up your account?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MediumCharcoal,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Option A: Link Existing Clinic Record (Anthony & Bella)
                    Surface(
                        color = SoftCream,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = ForestSage.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Link, contentDescription = null, tint = ForestSage)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Already a Clinic Patient?",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Link your existing file to avoid duplicate records",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MediumCharcoal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            OutlinedButton(
                                onClick = { showLinkDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = ForestSage),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("link_existing_client_button")
                            ) {
                                Text("Link Existing Clinic Record", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Option B: Register Brand New Companion (5-Step Flow)
                    Surface(
                        color = WarmIvory,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    shape = CircleShape,
                                    color = AmberTerracotta.copy(alpha = 0.15f),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(Icons.Default.Pets, contentDescription = null, tint = AmberTerracotta)
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "New to Happy Paws?",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Complete the 5-step guided companion onboarding",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MediumCharcoal
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = { showOnboardingWizard = true },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("start_pet_onboarding_button")
                            ) {
                                Text("Add Your First Pet", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (!errorMessage.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    TextButton(onClick = onSignOut) {
                        Text("Sign Out", color = MediumCharcoal)
                    }
                }
            }
        }
    }

    // Modal Dialog: Link Existing Clinic Record
    if (showLinkDialog) {
        AlertDialog(
            onDismissRequest = { showLinkDialog = false },
            title = {
                Text(
                    text = "Link Clinic Record",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter your phone number or email and the claim code printed on your clinic registration receipt.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumCharcoal
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = phoneInput,
                        onValueChange = { phoneInput = it },
                        label = { Text("Phone or Email") },
                        placeholder = { Text("e.g. 0881479329 or antojayster@gmail.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("modal_claim_phone_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = claimCodeInput,
                        onValueChange = { claimCodeInput = it },
                        label = { Text("Clinic Claim Code") },
                        placeholder = { Text("e.g. HP-BELLA1") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("modal_claim_code_input")
                    )
                    if (localError != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = localError ?: "", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (phoneInput.isBlank() || claimCodeInput.isBlank()) {
                            localError = "Please enter both fields"
                        } else {
                            localError = null
                            showLinkDialog = false
                            onLinkClinicRecord(phoneInput.trim(), claimCodeInput.trim())
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                    modifier = Modifier.testTag("modal_claim_confirm_button")
                ) {
                    Text("Verify & Link")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLinkDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // 5-Step Guided Pet Onboarding Wizard
    if (showOnboardingWizard) {
        PetOnboardingWizardDialog(
            ownerId = 0L,
            ownerName = userName,
            onSavePet = { newPet, _ ->
                showOnboardingWizard = false
                onSaveNewPet(newPet, phoneInput.ifEmpty { "0881479329" })
            },
            onDismiss = { showOnboardingWizard = false }
        )
    }
}
