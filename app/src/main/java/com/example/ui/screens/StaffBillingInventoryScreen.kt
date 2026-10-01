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
import com.example.data.local.*
import com.example.ui.components.PrintInvoiceDialog
import com.example.ui.components.StatusBadge
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffBillingInventoryScreen(
    inventory: List<InventoryEntity>,
    invoices: List<InvoiceEntity>,
    payments: List<PaymentEntity>,
    clients: List<ClientEntity>,
    pets: List<PetEntity>,
    onAdjustStock: (id: Long, delta: Int) -> Unit,
    onAddInventoryItem: (InventoryEntity) -> Unit,
    onCreateInvoice: (InvoiceEntity) -> Unit,
    onRecordPayment: (PaymentEntity) -> Unit,
    clinicSettings: ClinicSettingsEntity? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedSection by remember { mutableIntStateOf(0) } // 0: Inventory, 1: Billing & Invoices
    val sections = listOf("Inventory & Stock", "Billing & Invoices")

    var selectedInvoiceForPrint by remember { mutableStateOf<InvoiceEntity?>(null) }
    var showNewItemDialog by remember { mutableStateOf(false) }
    var showNewInvoiceDialog by remember { mutableStateOf(false) }
    var showRecordPaymentDialog by remember { mutableStateOf(false) }
    var paymentTargetInvoice by remember { mutableStateOf<InvoiceEntity?>(null) }

    val lowStockCount = inventory.count { it.quantity <= it.minStockThreshold }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(WarmIvory)
    ) {
        // Tab Selector
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
                            text = if (idx == 0 && lowStockCount > 0) "$title ($lowStockCount Low)" else title,
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
                    // Inventory Section
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
                                        text = "Clinic Pharmacy & Supplies",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepCharcoal
                                    )
                                    Text(
                                        text = "${inventory.size} SKU items tracked",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SoftSlate
                                    )
                                }
                                Button(
                                    onClick = { showNewItemDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("add_inventory_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Item")
                                }
                            }
                        }

                        // Low stock warning banner
                        if (lowStockCount > 0) {
                            item {
                                Surface(
                                    color = StatusRedBg,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, StatusRed.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(Icons.Default.Warning, contentDescription = null, tint = StatusRed)
                                        Text(
                                            text = "$lowStockCount items at or below reorder threshold in Monrovia warehouse!",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = StatusRed
                                        )
                                    }
                                }
                            }
                        }

                        // Inventory Items
                        items(inventory) { item ->
                            val isLowStock = item.quantity <= item.minStockThreshold

                            Surface(
                                color = CardWarmSurface,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (isLowStock) StatusRed.copy(alpha = 0.5f) else BorderSubtle),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text(
                                                text = item.name,
                                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                                color = DeepCharcoal
                                            )
                                            if (isLowStock) {
                                                Surface(color = StatusRedBg, shape = RoundedCornerShape(4.dp)) {
                                                    Text("LOW", color = StatusRed, style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                                }
                                            }
                                        }
                                        Text(
                                            text = "Category: ${item.category} • SKU: ${item.sku}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = SoftSlate
                                        )
                                        Text(
                                            text = "Selling Price: $${"%.2f".format(item.sellingPrice)} • Supplier: ${item.supplier}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MediumCharcoal
                                        )
                                    }

                                    // Quantity & Restock controls
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        IconButton(
                                            onClick = { onAdjustStock(item.id, -1) },
                                            enabled = item.quantity > 0,
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.RemoveCircleOutline, contentDescription = "Use 1", tint = SoftSlate)
                                        }

                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = "${item.quantity}",
                                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                                color = if (isLowStock) StatusRed else ForestSage
                                            )
                                            Text(item.unit, style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp), color = SoftSlate)
                                        }

                                        IconButton(
                                            onClick = { onAdjustStock(item.id, 5) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(Icons.Default.AddCircleOutline, contentDescription = "Restock +5", tint = ForestSage)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Billing & Invoices Section
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
                                        text = "Client Invoices & Billing",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontFamily = FontFamily.Serif,
                                            fontWeight = FontWeight.Bold
                                        ),
                                        color = DeepCharcoal
                                    )
                                    val totalDue = invoices.sumOf { it.totalAmount - it.amountPaid }
                                    Text(
                                        text = "Outstanding balances: $${"%.2f".format(totalDue)}",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = if (totalDue > 0) StatusAmber else ForestSage
                                    )
                                }

                                Button(
                                    onClick = { showNewInvoiceDialog = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta),
                                    modifier = Modifier.defaultMinSize(minHeight = 48.dp).testTag("create_invoice_btn")
                                ) {
                                    Icon(Icons.Default.Receipt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("New Invoice")
                                }
                            }
                        }

                        items(invoices) { inv ->
                            val client = clients.find { it.id == inv.clientId }
                            val pet = pets.find { it.id == inv.petId }
                            val balance = inv.totalAmount - inv.amountPaid

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
                                            Text(inv.invoiceNumber, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepCharcoal)
                                            Text("Client: ${client?.fullName ?: "N/A"} • Pet: ${pet?.name ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = MediumCharcoal)
                                        }
                                        StatusBadge(status = inv.status)
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text("Services: ${inv.itemsSummary}", style = MaterialTheme.typography.bodySmall, color = SoftSlate)

                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("Total: $${"%.2f".format(inv.totalAmount)} | Paid: $${"%.2f".format(inv.amountPaid)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium), color = DeepCharcoal)
                                        if (balance > 0) {
                                            Text("Balance: $${"%.2f".format(balance)}", style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = StatusRed)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(10.dp))
                                    HorizontalDivider(color = BorderSubtle.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { selectedInvoiceForPrint = inv },
                                            modifier = Modifier.weight(1f).height(40.dp)
                                        ) {
                                            Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Receipt / PDF")
                                        }

                                        if (balance > 0) {
                                            Button(
                                                onClick = {
                                                    paymentTargetInvoice = inv
                                                    showRecordPaymentDialog = true
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = ForestSage),
                                                modifier = Modifier.weight(1.2f).height(40.dp)
                                            ) {
                                                Icon(Icons.Default.Payment, contentDescription = null, modifier = Modifier.size(16.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Record Payment")
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Print Invoice / Receipt Dialog
    if (selectedInvoiceForPrint != null) {
        val targetInv = selectedInvoiceForPrint!!
        val cl = clients.find { it.id == targetInv.clientId }
        val pt = pets.find { it.id == targetInv.petId }
        val targetPayments = payments.filter { it.invoiceId == targetInv.id }

        PrintInvoiceDialog(
            invoice = targetInv,
            client = cl,
            pet = pt,
            payments = targetPayments,
            clinicSettings = clinicSettings,
            onDismiss = { selectedInvoiceForPrint = null }
        )
    }

    // New Item Dialog
    if (showNewItemDialog) {
        var name by remember { mutableStateOf("") }
        var category by remember { mutableStateOf("Vaccine") }
        var sku by remember { mutableStateOf("HP-MED-${(100..999).random()}") }
        var qtyText by remember { mutableStateOf("20") }
        var unit by remember { mutableStateOf("vials") }
        var costText by remember { mutableStateOf("8.00") }
        var priceText by remember { mutableStateOf("20.00") }

        AlertDialog(
            onDismissRequest = { showNewItemDialog = false },
            title = { Text("Add Clinic Inventory Item") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Item Name *") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Category (Vaccine, Antibiotic, Dewormer, etc.)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = sku, onValueChange = { sku = it }, label = { Text("SKU / Item Code") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = qtyText, onValueChange = { qtyText = it }, label = { Text("Initial Stock") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = unit, onValueChange = { unit = it }, label = { Text("Unit (vials, tabs)") }, modifier = Modifier.weight(1f))
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = costText, onValueChange = { costText = it }, label = { Text("Cost ($)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Selling Price ($)") }, modifier = Modifier.weight(1f))
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank()) {
                            onAddInventoryItem(
                                InventoryEntity(
                                    name = name,
                                    category = category,
                                    sku = sku,
                                    quantity = qtyText.toIntOrNull() ?: 10,
                                    unit = unit,
                                    purchaseCost = costText.toDoubleOrNull() ?: 5.0,
                                    sellingPrice = priceText.toDoubleOrNull() ?: 15.0
                                )
                            )
                            showNewItemDialog = false
                            Toast.makeText(context, "Item added to inventory", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Save Item")
                }
            },
            dismissButton = { TextButton(onClick = { showNewItemDialog = false }) { Text("Cancel") } }
        )
    }

    // New Invoice Dialog
    if (showNewInvoiceDialog) {
        var selectedClientId by remember { mutableLongStateOf(clients.firstOrNull()?.id ?: 0L) }
        var servicesSummary by remember { mutableStateOf("Consultation ($20) + Deworming Protocol ($10)") }
        var amountText by remember { mutableStateOf("30.00") }

        AlertDialog(
            onDismissRequest = { showNewInvoiceDialog = false },
            title = { Text("Create Clinic Invoice") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Select Client:", style = MaterialTheme.typography.labelSmall)
                    clients.forEach { c ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedClientId = c.id }
                        ) {
                            RadioButton(selected = selectedClientId == c.id, onClick = { selectedClientId = c.id })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(c.fullName, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                    OutlinedTextField(value = servicesSummary, onValueChange = { servicesSummary = it }, label = { Text("Services & Medications") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = amountText, onValueChange = { amountText = it }, label = { Text("Total Amount (USD)") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clientPets = pets.filter { it.ownerId == selectedClientId }
                        val firstPetId = clientPets.firstOrNull()?.id ?: 1L
                        val total = amountText.toDoubleOrNull() ?: 25.0

                        onCreateInvoice(
                            InvoiceEntity(
                                invoiceNumber = "INV-2026-0${(200..999).random()}",
                                clientId = selectedClientId,
                                petId = firstPetId,
                                date = "2026-09-28",
                                dueDate = "2026-10-05",
                                itemsSummary = servicesSummary,
                                totalAmount = total,
                                amountPaid = 0.0,
                                status = "Issued"
                            )
                        )
                        showNewInvoiceDialog = false
                        Toast.makeText(context, "Invoice issued successfully", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberTerracotta)
                ) {
                    Text("Issue Invoice")
                }
            },
            dismissButton = { TextButton(onClick = { showNewInvoiceDialog = false }) { Text("Cancel") } }
        )
    }

    // Record Payment Dialog
    if (showRecordPaymentDialog && paymentTargetInvoice != null) {
        val inv = paymentTargetInvoice!!
        val balance = inv.totalAmount - inv.amountPaid
        var paymentAmountText by remember { mutableStateOf(balance.toString()) }
        var paymentMethod by remember { mutableStateOf("Mobile Money (MTN/Orange)") }
        var referenceText by remember { mutableStateOf("TXN-${(100000..999999).random()}") }

        AlertDialog(
            onDismissRequest = { showRecordPaymentDialog = false },
            title = { Text("Record Payment for ${inv.invoiceNumber}") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Balance due: $${"%.2f".format(balance)}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = AmberTerracotta)
                    OutlinedTextField(value = paymentAmountText, onValueChange = { paymentAmountText = it }, label = { Text("Payment Amount ($)") }, modifier = Modifier.fillMaxWidth())

                    Text("Payment Method:", style = MaterialTheme.typography.labelSmall)
                    listOf("Mobile Money (MTN/Orange)", "Cash", "Bank Transfer").forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { paymentMethod = method }
                        ) {
                            RadioButton(selected = paymentMethod == method, onClick = { paymentMethod = method })
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(method, style = MaterialTheme.typography.bodyMedium)
                        }
                    }

                    OutlinedTextField(value = referenceText, onValueChange = { referenceText = it }, label = { Text("Payment Reference / Receipt #") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = paymentAmountText.toDoubleOrNull() ?: balance
                        onRecordPayment(
                            PaymentEntity(
                                invoiceId = inv.id,
                                amount = amount,
                                paymentMethod = paymentMethod,
                                reference = referenceText,
                                paymentDate = "2026-09-28",
                                recordedBy = "Front Desk"
                            )
                        )
                        showRecordPaymentDialog = false
                        Toast.makeText(context, "Payment recorded! Receipt updated.", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestSage)
                ) {
                    Text("Confirm Payment")
                }
            },
            dismissButton = { TextButton(onClick = { showRecordPaymentDialog = false }) { Text("Cancel") } }
        )
    }
}
