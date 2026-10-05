package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InvoiceEntity
import com.example.model.InvoiceStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*
import com.example.viewmodel.HappyPawsViewModel

@Composable
fun StaffBillingInventoryScreen(
    viewModel: HappyPawsViewModel
) {
    val context = LocalContext.current
    val invoices by viewModel.allInvoices.collectAsState()
    val inventory by viewModel.allInventory.collectAsState()

    var showNewInvoiceDialog by remember { mutableStateOf(false) }

    // New Invoice Form
    var invClientName by remember { mutableStateOf("") }
    var invPetName by remember { mutableStateOf("") }
    var invAmountUSD by remember { mutableStateOf("25.0") }
    var invServices by remember { mutableStateOf("Consultation, Deworming Prophylaxis") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WarmIvory)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Billing & Pharmacy Inventory",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepCharcoal
                )
                Text(
                    text = "Invoices (USD / LRD) & essential medical stock",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MediumCharcoal
                )
            }

            Button(
                onClick = { showNewInvoiceDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Invoice", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Business Metrics Row (Key Performance Indicators)
        val totalRevenueUSD = invoices.filter { it.status == InvoiceStatus.PAID }.sumOf { it.amountUSD }
        val totalRevenueLRD = invoices.filter { it.status == InvoiceStatus.PAID }.sumOf { it.amountLRD }
        val lowStockCount = inventory.count { it.stockCount <= it.minimumThreshold }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.weight(1.2f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Total Revenue", fontSize = 11.sp, color = MediumCharcoal, fontWeight = FontWeight.Medium)
                    Text("$${totalRevenueUSD.toInt()} USD", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = ForestSage)
                    Text("LRD ${totalRevenueLRD.toInt().toString().reversed().chunked(3).joinToString(",").reversed()}", fontSize = 10.sp, color = MediumCharcoal)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Invoices", fontSize = 11.sp, color = MediumCharcoal, fontWeight = FontWeight.Medium)
                    Text("${invoices.size} Issued", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = DeepCharcoal)
                    Text("${invoices.count { it.status == InvoiceStatus.PAID }} Paid", fontSize = 10.sp, color = ForestSage, fontWeight = FontWeight.Bold)
                }
            }

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (lowStockCount > 0) TerracottaLight else Color.White,
                modifier = Modifier.weight(1f)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Stock Alerts", fontSize = 11.sp, color = if (lowStockCount > 0) AmberTerracottaDark else MediumCharcoal, fontWeight = FontWeight.Medium)
                    Text("$lowStockCount Low", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (lowStockCount > 0) StatusRed else ForestSage)
                    Text("Inventory Items", fontSize = 10.sp, color = MediumCharcoal)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Invoices Section
        Text(
            text = "Clinic Invoices",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepCharcoal,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (invoices.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No billing invoices issued yet.", fontSize = 12.sp, color = MediumCharcoal)
                }
            }
        } else {
            invoices.forEach { inv ->
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${inv.invoiceNumber} · ${inv.clientName} (${inv.petName})",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = DeepCharcoal
                            )
                            Text(
                                text = "Services: ${inv.itemsSummary}",
                                fontSize = 12.sp,
                                color = MediumCharcoal,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                            Text(
                                text = "$${inv.amountUSD} USD / LRD ${inv.amountLRD.toInt()} · ${inv.date}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = AmberTerracottaDark,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        StatusBadge(inv.status.name)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Pharmacy & Stock Inventory Section
        Text(
            text = "Pharmacy & Vaccine Stock Inventory",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = DeepCharcoal,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        if (inventory.isEmpty()) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            ) {
                Box(modifier = Modifier.padding(20.dp).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text("No pharmacy stock items found.", fontSize = 12.sp, color = MediumCharcoal)
                }
            }
        } else {
            inventory.forEach { item ->
                val isLow = item.stockCount <= item.minimumThreshold
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoal)
                            Text("Category: ${item.category} · Unit: $${item.unitPriceUSD} USD", fontSize = 12.sp, color = MediumCharcoal)
                            Text("Threshold Alert Level: ${item.minimumThreshold} ${item.unit}", fontSize = 11.sp, color = MediumCharcoal)
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text("${item.stockCount} ${item.unit}", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = DeepCharcoal)
                            StatusBadge(
                                text = if (isLow) "Low Stock" else "In Stock",
                                backgroundColor = if (isLow) TerracottaLight else SageLight,
                                textColor = if (isLow) AmberTerracottaDark else ForestSage
                            )

                            // Quick adjustment buttons
                            Row(
                                modifier = Modifier.padding(top = 4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        if (item.stockCount > 0) {
                                            viewModel.updateInventoryStock(item.id, item.stockCount - 1)
                                        }
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease", modifier = Modifier.size(14.dp), tint = DeepCharcoal)
                                }
                                IconButton(
                                    onClick = {
                                        viewModel.updateInventoryStock(item.id, item.stockCount + 1)
                                    },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase", modifier = Modifier.size(14.dp), tint = ForestSage)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // New Invoice Dialog
    if (showNewInvoiceDialog) {
        AlertDialog(
            onDismissRequest = { showNewInvoiceDialog = false },
            title = { Text("Generate Clinic Invoice", fontWeight = FontWeight.Bold, color = DeepCharcoal) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = invClientName,
                        onValueChange = { invClientName = it },
                        label = { Text("Client Name") },
                        placeholder = { Text("e.g. Anthony Tolbert") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = invPetName,
                        onValueChange = { invPetName = it },
                        label = { Text("Pet Name") },
                        placeholder = { Text("e.g. Bella") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = invAmountUSD,
                        onValueChange = { invAmountUSD = it },
                        label = { Text("Amount (USD)") },
                        placeholder = { Text("e.g. 25.0") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = invServices,
                        onValueChange = { invServices = it },
                        label = { Text("Services / Treatment Summary") },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = DeepCharcoal,
                            unfocusedTextColor = DeepCharcoal
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val usd = invAmountUSD.toDoubleOrNull() ?: 20.0
                        val lrd = usd * 194.0 // official Monrovia exchange rate
                        val randomNum = (100..999).random()
                        val newInvoice = InvoiceEntity(
                            invoiceNumber = "INV-2026-$randomNum",
                            clientName = invClientName.ifBlank { "Anthony Tolbert" },
                            petName = invPetName.ifBlank { "Bella" },
                            date = "Today (Oct 2026)",
                            amountUSD = usd,
                            amountLRD = lrd,
                            status = InvoiceStatus.PAID,
                            itemsSummary = invServices.ifBlank { "Clinical Consultation" }
                        )
                        viewModel.addInvoice(newInvoice)
                        showNewInvoiceDialog = false
                        Toast.makeText(context, "Invoice ${newInvoice.invoiceNumber} created and marked PAID.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Issue Invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewInvoiceDialog = false }) {
                    Text("Cancel", color = MediumCharcoal)
                }
            }
        )
    }
}
