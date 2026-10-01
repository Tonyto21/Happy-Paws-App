package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserRole
import com.example.ui.components.HappyPawsLogo
import com.example.ui.theme.*

enum class AuthMode {
    SIGN_IN,
    REGISTER_PET_PARENT,
    STAFF_INVITATION_ACTIVATION,
    FORGOT_PASSWORD,
    LINK_EXISTING_RECORD
}

@Composable
fun AuthScreen(
    onSignIn: (email: String, pass: String) -> Unit,
    onRegisterPetOwner: (email: String, pass: String, fullName: String, phone: String) -> Unit,
    onActivateStaffInvite: (email: String, pass: String, token: String, fullName: String) -> Unit,
    onForgotPassword: (email: String) -> Unit,
    onLinkClinicRecord: (phoneOrEmail: String, claimCode: String) -> Unit,
    onDevSwitchRole: ((UserRole) -> Unit)? = null,
    errorMessage: String? = null,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var currentMode by remember { mutableStateOf(AuthMode.SIGN_IN) }
    
    // Form fields
    var emailText by remember { mutableStateOf("") }
    var passwordText by remember { mutableStateOf("") }
    var fullNameText by remember { mutableStateOf("") }
    var phoneText by remember { mutableStateOf("") }
    var inviteTokenText by remember { mutableStateOf("") }
    var claimCodeText by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }
    var showDevOptions by remember { mutableStateOf(false) }

    val activeError = errorMessage ?: localError

    Surface(
        modifier = modifier.fillMaxSize(),
        color = WarmIvory
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            HappyPawsLogo(size = 88.dp, showTagline = true)

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
                    // Header Title & Subtitle based on mode
                    val (title, subtitle) = when (currentMode) {
                        AuthMode.SIGN_IN -> "Welcome Back" to "Sign in to access your pets, records, and appointments"
                        AuthMode.REGISTER_PET_PARENT -> "Create Pet Parent Account" to "Register your companion with Happy Paws Liberia"
                        AuthMode.STAFF_INVITATION_ACTIVATION -> "Activate Staff Account" to "Enter the invitation code issued by your Clinic Administrator"
                        AuthMode.FORGOT_PASSWORD -> "Reset Password" to "Enter your registered email to receive recovery instructions"
                        AuthMode.LINK_EXISTING_RECORD -> "Link Existing Clinic Record" to "Enter your phone/email and claim code from your paper card or invoice"
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = FontFamily.Serif,
                            fontWeight = FontWeight.Bold
                        ),
                        color = DeepCharcoal,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MediumCharcoal,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Mode: REGISTER_PET_PARENT
                    if (currentMode == AuthMode.REGISTER_PET_PARENT) {
                        OutlinedTextField(
                            value = fullNameText,
                            onValueChange = { fullNameText = it },
                            label = { Text("Full Name") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = phoneText,
                            onValueChange = { phoneText = it },
                            label = { Text("Phone Number (e.g. 0881479329)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_phone_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Mode: STAFF_INVITATION_ACTIVATION
                    if (currentMode == AuthMode.STAFF_INVITATION_ACTIVATION) {
                        OutlinedTextField(
                            value = inviteTokenText,
                            onValueChange = { inviteTokenText = it },
                            label = { Text("Staff Invitation Code") },
                            leadingIcon = { Icon(Icons.Default.Key, contentDescription = null) },
                            placeholder = { Text("e.g. VET-HP-8492") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_token_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = fullNameText,
                            onValueChange = { fullNameText = it },
                            label = { Text("Your Professional Name") },
                            leadingIcon = { Icon(Icons.Default.Badge, contentDescription = null) },
                            placeholder = { Text("e.g. Dr. Emmanuel Sackor") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_staff_name_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Mode: LINK_EXISTING_RECORD
                    if (currentMode == AuthMode.LINK_EXISTING_RECORD) {
                        OutlinedTextField(
                            value = phoneText,
                            onValueChange = { phoneText = it },
                            label = { Text("Registered Phone or Email") },
                            leadingIcon = { Icon(Icons.Default.ContactPhone, contentDescription = null) },
                            placeholder = { Text("e.g. 0881479329 or antojayster@gmail.com") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_claim_phone_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = claimCodeText,
                            onValueChange = { claimCodeText = it },
                            label = { Text("Clinic Claim Code / PIN") },
                            leadingIcon = { Icon(Icons.Default.ConfirmationNumber, contentDescription = null) },
                            placeholder = { Text("e.g. HP-BELLA1") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_claim_code_input")
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Note: If you visited our clinic in Congo Town, reception provided a claim code on your receipt or registration card.",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MediumCharcoal
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Email and Password for standard modes
                    if (currentMode != AuthMode.LINK_EXISTING_RECORD) {
                        OutlinedTextField(
                            value = emailText,
                            onValueChange = { emailText = it },
                            label = { Text("Email Address") },
                            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth().testTag("auth_email_input")
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (currentMode != AuthMode.FORGOT_PASSWORD) {
                            OutlinedTextField(
                                value = passwordText,
                                onValueChange = { passwordText = it },
                                label = { Text("Password") },
                                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                visualTransformation = PasswordVisualTransformation(),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("auth_password_input")
                            )
                        }
                    }

                    // Error presentation
                    if (!activeError.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = activeError,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Primary Action Button
                    Button(
                        onClick = {
                            localError = null
                            when (currentMode) {
                                AuthMode.SIGN_IN -> {
                                    if (emailText.isBlank() || passwordText.isBlank()) {
                                        localError = "Please enter both email and password."
                                    } else {
                                        onSignIn(emailText.trim(), passwordText)
                                    }
                                }
                                AuthMode.REGISTER_PET_PARENT -> {
                                    if (emailText.isBlank() || passwordText.isBlank() || fullNameText.isBlank()) {
                                        localError = "Please fill in your name, email, and a password (min 6 characters)."
                                    } else {
                                        onRegisterPetOwner(emailText.trim(), passwordText, fullNameText.trim(), phoneText.trim())
                                    }
                                }
                                AuthMode.STAFF_INVITATION_ACTIVATION -> {
                                    if (emailText.isBlank() || passwordText.isBlank() || inviteTokenText.isBlank()) {
                                        localError = "Please enter your invitation code, email, and password."
                                    } else {
                                        onActivateStaffInvite(emailText.trim(), passwordText, inviteTokenText.trim(), fullNameText.trim())
                                    }
                                }
                                AuthMode.FORGOT_PASSWORD -> {
                                    if (emailText.isBlank()) {
                                        localError = "Please enter your registered email address."
                                    } else {
                                        onForgotPassword(emailText.trim())
                                        Toast.makeText(context, "Password reset instructions sent to your email", Toast.LENGTH_LONG).show()
                                        currentMode = AuthMode.SIGN_IN
                                    }
                                }
                                AuthMode.LINK_EXISTING_RECORD -> {
                                    if (phoneText.isBlank() || claimCodeText.isBlank()) {
                                        localError = "Please enter both your registered phone/email and claim code."
                                    } else {
                                        onLinkClinicRecord(phoneText.trim(), claimCodeText.trim())
                                    }
                                }
                            }
                        },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("auth_submit_button")
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = WarmIvory, modifier = Modifier.size(22.dp))
                        } else {
                            val buttonText = when (currentMode) {
                                AuthMode.SIGN_IN -> "Sign In"
                                AuthMode.REGISTER_PET_PARENT -> "Create Account"
                                AuthMode.STAFF_INVITATION_ACTIVATION -> "Activate Staff Account"
                                AuthMode.FORGOT_PASSWORD -> "Send Reset Link"
                                AuthMode.LINK_EXISTING_RECORD -> "Verify & Link Record"
                            }
                            Text(
                                text = buttonText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Navigation options
                    when (currentMode) {
                        AuthMode.SIGN_IN -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                TextButton(
                                    onClick = { currentMode = AuthMode.FORGOT_PASSWORD; localError = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Forgot Password?", style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                                }
                                TextButton(
                                    onClick = { currentMode = AuthMode.LINK_EXISTING_RECORD; localError = null },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text("Link Clinic Record", style = MaterialTheme.typography.bodySmall, color = AmberTerracotta)
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(
                                onClick = { currentMode = AuthMode.REGISTER_PET_PARENT; localError = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("New Pet Parent? Create an Account", color = AmberTerracotta, fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(
                                onClick = { currentMode = AuthMode.STAFF_INVITATION_ACTIVATION; localError = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Clinic Staff? Activate with Invitation Code", color = SoftSlate, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        AuthMode.REGISTER_PET_PARENT -> {
                            TextButton(
                                onClick = { currentMode = AuthMode.LINK_EXISTING_RECORD; localError = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Already a clinic patient? Link Existing Record", color = ForestSage, fontWeight = FontWeight.SemiBold)
                            }
                            TextButton(
                                onClick = { currentMode = AuthMode.SIGN_IN; localError = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Already have an account? Sign In", color = AmberTerracotta)
                            }
                        }
                        AuthMode.STAFF_INVITATION_ACTIVATION, AuthMode.FORGOT_PASSWORD, AuthMode.LINK_EXISTING_RECORD -> {
                            TextButton(
                                onClick = { currentMode = AuthMode.SIGN_IN; localError = null },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("← Back to Sign In", color = AmberTerracotta)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Isolated Developer / Sandbox Switcher (Strictly for evaluation in dev builds)
            if (onDevSwitchRole != null) {
                if (!showDevOptions) {
                    TextButton(
                        onClick = { showDevOptions = true }
                    ) {
                        Text(
                            "Developer Evaluation Mode",
                            style = MaterialTheme.typography.labelSmall,
                            color = SoftSlate.copy(alpha = 0.7f)
                        )
                    }
                } else {
                    Surface(
                        color = SoftCream,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Evaluation Sandbox (Local Mock Roles)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = DeepCharcoal
                                )
                                IconButton(
                                    onClick = { showDevOptions = false },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(16.dp))
                                }
                            }
                            Text(
                                text = "Notice: Sandbox switches local UI only and does not grant production permissions on Firestore.",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MediumCharcoal
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    UserRole.PET_OWNER to "Owner",
                                    UserRole.VETERINARIAN to "Vet",
                                    UserRole.RECEPTIONIST to "Desk",
                                    UserRole.SUPER_ADMIN to "Admin"
                                ).forEach { (role, label) ->
                                    FilledTonalButton(
                                        onClick = { onDevSwitchRole(role) },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                        modifier = Modifier.weight(1f).defaultMinSize(minHeight = 32.dp)
                                    ) {
                                        Text(label, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
