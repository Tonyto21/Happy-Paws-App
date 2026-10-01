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
import androidx.compose.material.icons.filled.ReceiptLong
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
import com.example.data.local.InvoiceEntity
import com.example.data.local.PaymentEntity
import com.example.data.local.PetEntity
import com.example.ui.theme.*
import com.example.util.PdfPrintHelper

@Composable
fun PrintInvoiceDialog(
    invoice: InvoiceEntity,
    client: ClientEntity?,
    pet: PetEntity?,
    payments: List<PaymentEntity>,
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
                // Header Bar
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
                        Icon(Icons.Default.ReceiptLong, contentDescription = null, tint = AmberTerracotta)
                        Text(
                            text = "Clinic Invoice & Receipt",
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
                                PdfPrintHelper.printInvoice(
                                    context = context,
                                    invoice = invoice,
                                    client = client,
                                    pet = pet,
                                    payments = payments,
                                    clinicSettings = clinicSettings
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("print_invoice_button")
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

                // Printable Content
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            HappyPawsLogo(size = 64.dp, showTagline = false)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Happy Paws Liberia", style = MaterialTheme.typography.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold))
                            Text("Rescue Center & Vet Clinic", style = MaterialTheme.typography.labelSmall, color = AmberTerracotta)
                            Text("Monrovia, Liberia • VET: 0881479329", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("INVOICE", style = MaterialTheme.typography.headlineMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            Text(invoice.invoiceNumber, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = AmberTerracotta)
                            Spacer(modifier = Modifier.height(4.dp))
                            StatusBadge(status = invoice.status)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Date: ${invoice.date}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                            Text("Due: ${invoice.dueDate}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider(color = BorderSubtle)
                    Spacer(modifier = Modifier.height(12.dp))

                    // Bill To & Patient Info
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("BILLED TO:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SoftSlate)
                            Text(client?.fullName ?: "N/A", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            Text("Phone: ${client?.phone ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                            Text("Address: ${client?.address ?: "Monrovia"}", style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                        }
                        Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text("PATIENT:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = SoftSlate)
                            Text(pet?.name ?: "N/A", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            Text("${pet?.species} (${pet?.breed})", style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                            Text("Microchip: ${pet?.microchipId ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Line Items
                    Text("SERVICES & CHARGES", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif), color = DeepCharcoal)
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(SoftCream)
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Item Description", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                                Text("Amount (USD)", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            }
                            HorizontalDivider(color = BorderSubtle)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(invoice.itemsSummary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = DeepCharcoal)
                                    if (invoice.notes.isNotEmpty()) {
                                        Text(invoice.notes, style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                                    }
                                }
                                Text("$${"%.2f".format(invoice.totalAmount)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Summary & Totals
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Column(
                            modifier = Modifier.width(220.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Subtotal:", style = MaterialTheme.typography.bodyMedium, color = SoftSlate)
                                Text("$${"%.2f".format(invoice.totalAmount)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium))
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Total Paid:", style = MaterialTheme.typography.bodyMedium, color = ForestSage)
                                Text("$${"%.2f".format(invoice.amountPaid)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold), color = ForestSage)
                            }
                            HorizontalDivider(color = BorderSubtle)
                            val balanceDue = invoice.totalAmount - invoice.amountPaid
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Balance Due:", style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                                Text(
                                    "$${"%.2f".format(balanceDue)}",
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = if (balanceDue > 0) StatusRed else StatusGreen
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment History
                    if (payments.isNotEmpty()) {
                        Text("RECORDED PAYMENTS", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontFamily = FontFamily.Serif), color = DeepCharcoal)
                        Spacer(modifier = Modifier.height(6.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, BorderSubtle, RoundedCornerShape(8.dp))
                        ) {
                            payments.forEach { pay ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(pay.paymentMethod, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                                        Text("${pay.paymentDate} • Ref: ${pay.reference.ifEmpty { "Cash" }}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)
                                    }
                                    Text("$${"%.2f".format(pay.amount)}", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold), color = ForestSage)
                                }
                                HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Payment Info Note
                    Surface(
                        color = SoftCream,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Payment Methods Accepted:", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                            Text("• Cash in USD / LRD at Clinic Desk\n• Mobile Money: Orange Money / MTN MoMo to 0881479329\n• Thank you for supporting Happy Paws Liberia Rescue Center!", style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                        }
                    }
                }
            }
        }
    }
}
