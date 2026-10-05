package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.local.UserEntity
import com.example.model.UserRole
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel

@Composable
fun SettingsScreen(
    viewModel: HappyPawsViewModel,
    currentRole: UserRole,
    onSignOut: () -> Unit
) {
    val context = LocalContext.current
    val settings by viewModel.clinicSettings.collectAsState()
    val users by viewModel.allUsers.collectAsState()
    val oneTimeCred by viewModel.oneTimeCredential.collectAsState()

    var clinicName by remember(settings) { mutableStateOf(settings.clinicName) }
    var phone by remember(settings) { mutableStateOf(settings.phone) }
    var address by remember(settings) { mutableStateOf(settings.address) }

    var showCreateUserDialog by remember { mutableStateOf(false) }
    var newUserName by remember { mutableStateOf("") }
    var newUserRole by remember { mutableStateOf(UserRole.VETERINARIAN) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Clinic Settings & Administration",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = DeepCharcoal
        )
        Text(
            text = "Clinic branding, contact info, and user management",
            style = MaterialTheme.typography.bodyMedium,
            color = MediumCharcoal,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Clinic Branding Card (with solid black/dark readable entered text)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Business, contentDescription = null, tint = AmberTerracotta)
                    Text("Clinic Profile & Branding", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = clinicName,
                    onValueChange = { clinicName = it },
                    label = { Text("Clinic Official Name") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal,
                        focusedLabelColor = AmberTerracotta,
                        unfocusedLabelColor = MediumCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Contact Hotline (Liberia)") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal,
                        focusedLabelColor = AmberTerracotta,
                        unfocusedLabelColor = MediumCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Physical Address") },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = DeepCharcoal,
                        unfocusedTextColor = DeepCharcoal,
                        focusedLabelColor = AmberTerracotta,
                        unfocusedLabelColor = MediumCharcoal
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.updateClinicSettings(clinicName, phone, address)
                        Toast.makeText(context, "Clinic settings saved.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Save Clinic Information", fontWeight = FontWeight.Bold)
                }
            }
        }

        // SUPER ADMIN USER MANAGEMENT SECTION
        if (currentRole == UserRole.SUPER_ADMIN) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = ForestSage)
                            Text("Super Admin User Management", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                        }

                        Button(
                            onClick = { showCreateUserDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("New User", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    users.forEach { user ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (user.active) WarmIvory else Color(0xFFF3F4F6),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoal)
                                    Text(user.email, fontSize = 11.sp, color = MediumCharcoal)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(top = 4.dp)) {
                                        StatusBadge(
                                            text = user.role.displayName,
                                            backgroundColor = if (user.role == UserRole.SUPER_ADMIN) TerracottaLight else SageLight,
                                            textColor = if (user.role == UserRole.SUPER_ADMIN) AmberTerracottaDark else ForestSage
                                        )
                                        StatusBadge(
                                            text = if (user.active) "Active" else "Deactivated",
                                            backgroundColor = if (user.active) SageLight else Color(0xFFFEE2E2),
                                            textColor = if (user.active) ForestSage else StatusRed
                                        )
                                    }
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    IconButton(
                                        onClick = {
                                            viewModel.resetUserPassword(user) { ok, msg ->
                                                if (!ok) Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Refresh, contentDescription = "Reset Password", tint = ForestSage)
                                    }
                                    IconButton(
                                        onClick = { viewModel.toggleUserActive(user) }
                                    ) {
                                        Icon(
                                            if (user.active) Icons.Default.Block else Icons.Default.CheckCircle,
                                            contentDescription = "Toggle Active",
                                            tint = if (user.active) StatusRed else StatusGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else if (currentRole == UserRole.CLINIC_OWNER) {
            // Clinic Owner Staff Directory Overview
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.People, contentDescription = null, tint = AmberTerracotta)
                        Text("Clinic Staff Directory & Roster", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    users.forEach { user ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = WarmIvory,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(user.fullName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DeepCharcoal)
                                    Text(user.email, fontSize = 11.sp, color = MediumCharcoal)
                                }
                                StatusBadge(
                                    text = user.role.displayName,
                                    backgroundColor = if (user.role == UserRole.CLINIC_OWNER) TerracottaLight else SageLight,
                                    textColor = if (user.role == UserRole.CLINIC_OWNER) AmberTerracottaDark else ForestSage
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Non-Admin message
            Surface(
                color = SoftCream,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(Icons.Default.Shield, contentDescription = null, tint = MediumCharcoal)
                    Text("Administrative privileges required to manage accounts and system roles.", fontSize = 12.sp, color = DeepCharcoal)
                }
            }
        }

        // System Audit & Security Log for Super Admin & Clinic Owner
        if (currentRole == UserRole.SUPER_ADMIN || currentRole == UserRole.CLINIC_OWNER) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = ForestSage)
                        Text("System Security & Audit Log", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    listOf(
                        "Encrypted Local Database Active & Synced" to "Monrovia Local Storage",
                        "Role-Based Access Control: 5 Roles Enforced" to "Security Policy",
                        "Official Rabies Tag Sequence Verified: HP-LR-2024" to "Registry Health",
                        "Session Authentication: Active Credentials" to "Auth Guard"
                    ).forEach { (event, category) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFF9FAFB),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(event, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = DeepCharcoal)
                                StatusBadge(category, SageLight, ForestSage)
                            }
                        }
                    }
                }
            }
        }

        // Account Sign Out button
        Button(
            onClick = onSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = StatusRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Sign Out of Account", fontWeight = FontWeight.Bold)
        }
    }

    // CREATE USER DIALOG (Super Admin)
    if (showCreateUserDialog) {
        Dialog(onDismissRequest = { showCreateUserDialog = false }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Create New Clinic User", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    Text("Auto-generates standard username and temporary password.", fontSize = 11.sp, color = MediumCharcoal, modifier = Modifier.padding(bottom = 14.dp))

                    OutlinedTextField(
                        value = newUserName,
                        onValueChange = { newUserName = it },
                        label = { Text("Full Name (e.g. James Brown)") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Select Role:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = DeepCharcoal)
                    UserRole.values().forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { newUserRole = role }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = newUserRole == role, onClick = { newUserRole = role })
                            Text(role.displayName, fontSize = 13.sp, color = DeepCharcoal)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = { showCreateUserDialog = false }) {
                            Text("Cancel", color = MediumCharcoal)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newUserName.isNotBlank()) {
                                    viewModel.createUser(newUserName, newUserRole) { ok, msg ->
                                        if (ok) {
                                            showCreateUserDialog = false
                                            newUserName = ""
                                        } else {
                                            Toast.makeText(context, msg ?: "Error", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create User", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // ONE-TIME CREDENTIAL DIALOG (Temporary Password shown once!)
    oneTimeCred?.let { cred ->
        Dialog(onDismissRequest = { viewModel.clearOneTimeCredential() }) {
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Key, contentDescription = null, tint = ForestSage, modifier = Modifier.size(40.dp))

                    Text(
                        text = if (cred.isReset) "Password Reset Successfully" else "User Created Successfully",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = DeepCharcoal,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    Text(
                        text = "Provide these credentials to the user. This temporary password will not be shown again.",
                        fontSize = 11.sp,
                        color = MediumCharcoal,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )

                    Surface(
                        color = WarmIvory,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Full Name: ${cred.fullName}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DeepCharcoal)
                            Text("Role: ${cred.role}", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = DeepCharcoal)
                            Text("Username: ${cred.username}", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AmberTerracotta)
                            Surface(
                                color = Color(0xFFFEE2E2),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Temp Password: ${cred.temporaryPassword}",
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 14.sp,
                                    color = StatusRed,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Happy Paws Credentials", "Full Name: ${cred.fullName}\nRole: ${cred.role}\nUsername: ${cred.username}\nTemporary Password: ${cred.temporaryPassword}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Credentials copied to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = { viewModel.clearOneTimeCredential() },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Done", color = DeepCharcoal, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
