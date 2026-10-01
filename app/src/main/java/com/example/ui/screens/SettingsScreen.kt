package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AuditLogEntity
import com.example.data.local.ClinicSettingsEntity
import com.example.data.local.NotificationEntity
import com.example.model.UserRole
import com.example.ui.components.HappyPawsLogo
import com.example.ui.components.SectionHeader
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentRole: UserRole,
    settings: ClinicSettingsEntity?,
    auditLogs: List<AuditLogEntity>,
    notifications: List<NotificationEntity>,
    onRoleSelected: (UserRole) -> Unit,
    onSaveSettings: (ClinicSettingsEntity) -> Unit,
    onMarkAllNotificationsRead: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var clinicName by remember(settings) { mutableStateOf(settings?.clinicName ?: "Happy Paws Liberia") }
    var subTitle by remember(settings) { mutableStateOf(settings?.subTitle ?: "Rescue Center & Veterinary Clinic") }
    var vetPhone by remember(settings) { mutableStateOf(settings?.vetPhoneNumber ?: "0881479329") }
    var address by remember(settings) { mutableStateOf(settings?.address ?: "Tubman Blvd, Congo Town, Monrovia") }
    var hours by remember(settings) { mutableStateOf(settings?.clinicHours ?: "Mon - Sat: 8:00 AM - 6:00 PM") }

    var selectedSettingsTab by remember { mutableIntStateOf(0) } // 0: Roles & Branding, 1: Notifications, 2: Audit Logs
    val tabs = listOf("Branding & Roles", "Notifications (${notifications.count { !it.isRead }})", "Audit Logs")

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        TabRow(
            selectedTabIndex = selectedSettingsTab,
            containerColor = WarmIvory,
            contentColor = AmberTerracotta
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedSettingsTab == idx,
                    onClick = { selectedSettingsTab = idx },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedSettingsTab == idx) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedSettingsTab) {
                0 -> {
                    // Branding & Role Switcher
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        // Role Switcher Card
                        item {
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Active Experience & Role",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Switch roles seamlessly to experience both Pet Owner and all Staff views:",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftSlate
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    UserRole.values().forEach { role ->
                                        val isCurrent = role == currentRole
                                        Surface(
                                            color = if (isCurrent) SoftSage else SoftCream,
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, if (isCurrent) ForestSage else Color.Transparent),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 3.dp)
                                                .clickable {
                                                    onRoleSelected(role)
                                                    Toast.makeText(context, "Switched to ${role.displayName}", Toast.LENGTH_SHORT).show()
                                                }
                                                .testTag("role_switch_${role.name.lowercase()}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = role.displayName,
                                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                        color = if (isCurrent) ForestSage else DeepCharcoal
                                                    )
                                                    Text(
                                                        text = role.description,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MediumCharcoal
                                                    )
                                                }
                                                if (isCurrent) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = "Active", tint = ForestSage)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Clinic Branding Section
                        item {
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
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
                                                text = "Clinic Branding & Information",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = FontFamily.Serif,
                                                    fontWeight = FontWeight.Bold
                                                ),
                                                color = DeepCharcoal
                                            )
                                            Text(
                                                text = "Applied across receipts, passports & dashboards",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftSlate
                                            )
                                        }
                                        HappyPawsLogo(size = 54.dp, showTagline = false)
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))
                                    OutlinedTextField(
                                        value = clinicName,
                                        onValueChange = { clinicName = it },
                                        label = { Text("Clinic Name") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = subTitle,
                                        onValueChange = { subTitle = it },
                                        label = { Text("Tagline / Subtitle") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = vetPhone,
                                        onValueChange = { vetPhone = it },
                                        label = { Text("Veterinary Hotline (From Official Seal)") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = address,
                                        onValueChange = { address = it },
                                        label = { Text("Clinic Physical Address") },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = hours,
                                        onValueChange = { hours = it },
                                        label = { Text("Operating Hours") },
                                        modifier = Modifier.fillMaxWidth()
                                    )

                                    Spacer(modifier = Modifier.height(14.dp))
                                    Button(
                                        onClick = {
                                            val updated = (settings ?: ClinicSettingsEntity()).copy(
                                                clinicName = clinicName,
                                                subTitle = subTitle,
                                                vetPhoneNumber = vetPhone,
                                                address = address,
                                                clinicHours = hours
                                            )
                                            onSaveSettings(updated)
                                            Toast.makeText(context, "Clinic branding updated across platform", Toast.LENGTH_SHORT).show()
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                        modifier = Modifier.fillMaxWidth().height(48.dp)
                                    ) {
                                        Text("Save Branding Settings")
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Notifications
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "In-App Alerts & Reminders",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = FontFamily.Serif,
                                        fontWeight = FontWeight.Bold
                                    ),
                                    color = DeepCharcoal
                                )
                                TextButton(onClick = onMarkAllNotificationsRead) {
                                    Text("Mark all read", color = AmberTerracotta)
                                }
                            }
                        }

                        items(notifications) { notif ->
                            Surface(
                                color = if (notif.isRead) CardWarmSurface else SoftCream,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = when (notif.priority) {
                                            "CRITICAL" -> Icons.Default.Warning
                                            "HIGH" -> Icons.Default.PriorityHigh
                                            else -> Icons.Default.Notifications
                                        },
                                        contentDescription = null,
                                        tint = if (notif.priority == "CRITICAL") StatusRed else AmberTerracotta
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notif.title,
                                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                            color = DeepCharcoal
                                        )
                                        Text(
                                            text = notif.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MediumCharcoal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Audit Logs (Section 25 of prompt)
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                text = "System Audit Trail",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal
                            )
                            Text(
                                text = "Immutable record of clinical consultations, billing, and status updates",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftSlate
                            )
                        }

                        items(auditLogs) { log ->
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "${log.userName} (${log.userRole})",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = ForestSage
                                        )
                                        Text(
                                            text = log.action,
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = AmberTerracotta
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = log.details,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Entity: ${log.entityName} • ID: ${log.entityId}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = SoftSlate
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
