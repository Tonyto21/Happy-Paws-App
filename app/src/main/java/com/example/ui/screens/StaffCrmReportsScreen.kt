package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import com.example.data.local.*
import com.example.ui.components.MetricStatCard
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffCrmReportsScreen(
    enquiries: List<EnquiryEntity>,
    appointments: List<AppointmentEntity>,
    pets: List<PetEntity>,
    invoices: List<InvoiceEntity>,
    payments: List<PaymentEntity>,
    onAddEnquiry: (EnquiryEntity) -> Unit,
    onUpdateEnquiryStatus: (EnquiryEntity, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Enquiries CRM, 1: Reports & Performance
    val sections = listOf("Enquiries & CRM", "Clinic Reports")

    var showNewEnquiryDialog by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        TabRow(
            selectedTabIndex = selectedSection,
            containerColor = WarmIvory,
            contentColor = AmberTerracotta
        ) {
            sections.forEachIndexed { idx, title ->
                Tab(
                    selected = selectedSection == idx,
                    onClick = { selectedSection = idx },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (selectedSection == idx) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                    },
                    modifier = Modifier.defaultMinSize(minHeight = 48.dp)
                )
            }
        }

        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            when (selectedSection) {
                0 -> {
                    // Enquiries CRM
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
                                Column {
                                    Text(
                                        text = "Client Inquiries & CRM",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "Consolidated WhatsApp, Walk-in & Phone requests",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftSlate
                                    )
                                }

                                Button(
                                    onClick = { showNewEnquiryDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("add_enquiry_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Inquiry")
                                }
                            }
                        }

                        items(enquiries) { enq ->
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(14.dp),
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
                                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                                Text(
                                                    text = enq.customerName,
                                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                    color = DeepCharcoal
                                                )
                                                Surface(color = SoftSage, shape = RoundedCornerShape(4.dp)) {
                                                    Text(
                                                        text = enq.source,
                                                        color = ForestSage,
                                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "Phone: ${enq.phone} • Pet: ${enq.petDetails}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = SoftSlate
                                            )
                                        }
                                        StatusBadge(status = enq.status)
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "\"${enq.enquiryText}\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MediumCharcoal
                                    )

                                    if (enq.followUpDate.isNotEmpty()) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "Follow-up: ${enq.followUpDate}",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                                            color = AmberTerracotta
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    // Action buttons: Call / WhatsApp, Change Status
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        IconButton(
                                            onClick = {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${enq.phone}"))
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.Phone, contentDescription = "Call", tint = ForestSage)
                                        }

                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            if (enq.status != "Contacted") {
                                                OutlinedButton(
                                                    onClick = { onUpdateEnquiryStatus(enq, "Contacted") },
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Text("Mark Contacted", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                            if (enq.status != "Converted to Appointment") {
                                                Button(
                                                    onClick = { onUpdateEnquiryStatus(enq, "Converted to Appointment") },
                                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                                    modifier = Modifier.height(36.dp)
                                                ) {
                                                    Text("Convert to Appt", style = MaterialTheme.typography.labelSmall)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Reports & Performance Section
                    val totalBilled = invoices.sumOf { it.totalAmount }
                    val totalCollected = payments.sumOf { it.amount }
                    val totalOutstanding = totalBilled - totalCollected
                    val rescuePetsCount = pets.count { it.isRescuePet }

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        item {
                            Text(
                                text = "Happy Paws Clinical Operations Report",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = DeepCharcoal
                            )
                            Text(
                                text = "Aggregated data from local Room database and offline cache",
                                style = MaterialTheme.typography.bodySmall,
                                color = SoftSlate
                            )
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricStatCard(
                                    title = "Revenue Collected",
                                    value = "$${"%.2f".format(totalCollected)}",
                                    subtitle = "Cash & Mobile Money",
                                    icon = Icons.Default.AttachMoney,
                                    iconTint = ForestSage,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricStatCard(
                                    title = "Outstanding",
                                    value = "$${"%.2f".format(totalOutstanding)}",
                                    subtitle = "Pending client balances",
                                    icon = Icons.Default.ReceiptLong,
                                    iconTint = StatusAmber,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MetricStatCard(
                                    title = "Total Patients",
                                    value = "${pets.size}",
                                    subtitle = "$rescuePetsCount Rescue Animals",
                                    icon = Icons.Default.Pets,
                                    iconTint = AmberTerracotta,
                                    modifier = Modifier.weight(1f)
                                )
                                MetricStatCard(
                                    title = "Consultations",
                                    value = "${appointments.size}",
                                    subtitle = "Appointments logged",
                                    icon = Icons.Default.MedicalServices,
                                    iconTint = DeepNavy,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        item {
                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "Payment Method Breakdown",
                                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DeepCharcoal
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))

                                    val momoPayments = payments.filter { it.paymentMethod.contains("Mobile Money", ignoreCase = true) }
                                    val cashPayments = payments.filter { it.paymentMethod.contains("Cash", ignoreCase = true) }

                                    ReportBreakdownRow("Mobile Money (MTN & Orange)", "$${"%.2f".format(momoPayments.sumOf { it.amount })}", "${momoPayments.size} txns")
                                    Spacer(modifier = Modifier.height(6.dp))
                                    ReportBreakdownRow("Cash at Reception Register", "$${"%.2f".format(cashPayments.sumOf { it.amount })}", "${cashPayments.size} txns")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Enquiry Dialog
    if (showNewEnquiryDialog) {
        var custName by remember { mutableStateOf("") }
        var phone by remember { mutableStateOf("") }
        var petInfo by remember { mutableStateOf("") }
        var enquiryMsg by remember { mutableStateOf("") }
        var source by remember { mutableStateOf("WhatsApp") }
        var followUp by remember { mutableStateOf("Today 4:00 PM") }

        AlertDialog(
            onDismissRequest = { showNewEnquiryDialog = false },
            title = { Text("Log New Client Inquiry") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = custName, onValueChange = { custName = it }, label = { Text("Customer Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = petInfo, onValueChange = { petInfo = it }, label = { Text("Pet Species / Name (e.g. Dog 'Max')") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = enquiryMsg, onValueChange = { enquiryMsg = it }, label = { Text("Inquiry / Questions") }, minLines = 2, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = source, onValueChange = { source = it }, label = { Text("Channel (WhatsApp, Phone, Walk-in, Facebook)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = followUp, onValueChange = { followUp = it }, label = { Text("Follow-up Time") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (custName.isNotBlank() && phone.isNotBlank()) {
                            onAddEnquiry(
                                EnquiryEntity(
                                    customerName = custName,
                                    phone = phone,
                                    petDetails = petInfo,
                                    enquiryText = enquiryMsg,
                                    source = source,
                                    status = "New",
                                    followUpDate = followUp
                                )
                            )
                            showNewEnquiryDialog = false
                            Toast.makeText(context, "Inquiry logged to CRM", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Save Inquiry")
                }
            },
            dismissButton = { TextButton(onClick = { showNewEnquiryDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun ReportBreakdownRow(label: String, amount: String, count: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = DeepCharcoal)
            Text(count, style = MaterialTheme.typography.bodySmall, color = SoftSlate)
        }
        Text(amount, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = ForestSage)
    }
}
