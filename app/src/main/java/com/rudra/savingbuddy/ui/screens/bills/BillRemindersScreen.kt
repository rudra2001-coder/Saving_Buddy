package com.rudra.savingbuddy.ui.screens.bills

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.BillCycle
import com.rudra.savingbuddy.domain.model.BillNotificationSettings
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.ui.theme.IncomeGreen
import com.rudra.savingbuddy.util.CurrencyFormatter
import java.util.Calendar

val billCategories = listOf(
    "Electricity" to Icons.Default.ElectricBolt,
    "Water" to Icons.Default.WaterDrop,
    "Internet" to Icons.Default.Wifi,
    "Phone" to Icons.Default.Phone,
    "Gas" to Icons.Default.LocalFireDepartment,
    "Rent" to Icons.Default.Home,
    "Insurance" to Icons.Default.Security,
    "Netflix" to Icons.Default.Movie,
    "Spotify" to Icons.Default.MusicNote,
    "Gym" to Icons.Default.FitnessCenter,
    "Credit Card" to Icons.Default.CreditCard,
    "Loan" to Icons.Default.AccountBalance,
    "Other" to Icons.Default.MoreHoriz
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillRemindersScreen(
    navController: NavController,
    viewModel: BillRemindersViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var billToDelete by remember { mutableStateOf<BillItem?>(null) }
    var editingBill by remember { mutableStateOf<BillItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text("Bill Reminders", fontWeight = FontWeight.Bold) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showSettingsDialog() }) {
                        Icon(Icons.Default.Settings, contentDescription = "Notification Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary,
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Add Bill") }
            )
        }
    ) { padding ->
        if (uiState.bills.isEmpty() && !uiState.isLoading) {
            EmptyBillsState(modifier = Modifier.fillMaxSize().padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    BillsSummaryCard(
                        monthlyTotal = uiState.totalMonthlyAmount,
                        weeklyTotal = uiState.totalWeeklyAmount,
                        upcomingCount = uiState.upcomingBillsCount,
                        activeCount = uiState.bills.count { it.isActive }
                    )
                }

                item {
                    Text(
                        "Upcoming Bills",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(uiState.bills) { bill ->
                    BillCard(
                        bill = bill,
                        onEdit = { editingBill = bill },
                        onDelete = { billToDelete = bill },
                        onToggleActive = { viewModel.toggleBillActive(bill.id, !bill.isActive) },
                        onToggleNotification = { viewModel.toggleBillNotification(bill.id, !bill.isNotificationEnabled) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddBillDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { name, amount, billingDay, billingCycle, category, notifyDays, notifyEnabled, notes ->
                viewModel.saveBill(name, amount, billingDay, billingCycle, category, notifyDays, notifyEnabled, notes)
            }
        )
    }

    editingBill?.let { bill ->
        EditBillDialog(
            bill = bill,
            onDismiss = { editingBill = null },
            onSave = { updatedBill ->
                viewModel.updateBill(updatedBill)
                editingBill = null
            }
        )
    }

    billToDelete?.let { bill ->
        AlertDialog(
            onDismissRequest = { billToDelete = null },
            title = { Text("Delete Bill") },
            text = { Text("Are you sure you want to delete \"${bill.name}\"?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBill(bill.id)
                        billToDelete = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { billToDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showSettingsDialog) {
        NotificationSettingsDialog(
            settings = uiState.notificationSettings,
            onDismiss = { viewModel.hideSettingsDialog() },
            onSave = { viewModel.updateNotificationSettings(it) }
        )
    }
}

@Composable
fun EmptyBillsState(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No bills added yet",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Add your recurring bills to get reminders",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun BillsSummaryCard(
    monthlyTotal: Double,
    weeklyTotal: Double,
    upcomingCount: Int,
    activeCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Monthly",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        CurrencyFormatter.format(monthlyTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Weekly",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Text(
                        CurrencyFormatter.format(weeklyTotal),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryChip(
                    icon = Icons.Default.NotificationsActive,
                    label = "Upcoming",
                    value = "$upcomingCount",
                    color = ExpenseRed
                )
                SummaryChip(
                    icon = Icons.Default.CheckCircle,
                    label = "Active",
                    value = "$activeCount",
                    color = IncomeGreen
                )
            }
        }
    }
}

@Composable
fun SummaryChip(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = color
        )
        Spacer(modifier = Modifier.width(4.dp))
        Column {
            Text(
                value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun BillCard(
    bill: BillItem,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit,
    onToggleNotification: () -> Unit
) {
    val daysUntil = bill.getDaysUntilDue()
    val isUrgent = bill.isUrgent() && bill.isActive

    val backgroundColor by animateColorAsState(
        targetValue = when {
            !bill.isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isUrgent && bill.isDueToday() -> ExpenseRed.copy(alpha = 0.15f)
            isUrgent -> ExpenseRed.copy(alpha = 0.1f)
            else -> MaterialTheme.colorScheme.surface
        },
        label = "billCardBackground"
    )

    val categoryIcon = billCategories.find { it.first == bill.category }?.second ?: Icons.Default.Receipt

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        onClick = onEdit
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(
                        if (bill.isActive) ExpenseRed.copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    categoryIcon,
                    contentDescription = null,
                    tint = if (bill.isActive) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        bill.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (bill.isActive) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                    if (!bill.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Paused",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    "${bill.billingCycle.displayName} • ${bill.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    val daysText = when {
                        daysUntil < 0 -> "Overdue"
                        daysUntil == 0 -> "Due Today"
                        daysUntil == 1 -> "Due Tomorrow"
                        else -> "Due in $daysUntil days"
                    }
                    
                    val daysColor = when {
                        daysUntil < 0 -> ExpenseRed
                        daysUntil == 0 -> ExpenseRed
                        daysUntil <= 3 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    
                    Icon(
                        when {
                            daysUntil < 0 -> Icons.Default.Warning
                            daysUntil == 0 -> Icons.Default.Today
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = daysColor
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        daysText,
                        style = MaterialTheme.typography.labelMedium,
                        color = daysColor,
                        fontWeight = if (daysUntil <= 3) FontWeight.Bold else FontWeight.Normal
                    )
                    
                    if (bill.isNotificationEnabled && bill.isActive) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(
                            Icons.Default.Notifications,
                            contentDescription = "Notifications on",
                            modifier = Modifier.size(14.dp),
                            tint = IncomeGreen
                        )
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(bill.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (bill.isActive) ExpenseRed else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                
                Row {
                    IconButton(
                        onClick = onToggleNotification,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            if (bill.isNotificationEnabled) Icons.Default.NotificationsActive else Icons.Default.NotificationsOff,
                            contentDescription = "Toggle notification",
                            modifier = Modifier.size(18.dp),
                            tint = if (bill.isNotificationEnabled) IncomeGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = ExpenseRed.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBillDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Int, BillCycle, String, List<Int>, Boolean, String?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var billingDay by remember { mutableStateOf(1) }
    var billingCycle by remember { mutableStateOf(BillCycle.MONTHLY) }
    var category by remember { mutableStateOf("Electricity") }
    var notify3Days by remember { mutableStateOf(true) }
    var notify2Days by remember { mutableStateOf(true) }
    var notify1Day by remember { mutableStateOf(true) }
    var isNotificationEnabled by remember { mutableStateOf(true) }
    var notes by remember { mutableStateOf("") }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var cycleExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    val daysInMonth = (1..31).toList()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Bill") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    placeholder = { Text("e.g., Electric Bill") }
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    leadingIcon = { Text("$") }
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        billCategories.forEach { (cat, _) ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = cycleExpanded,
                    onExpandedChange = { cycleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = billingCycle.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cycleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cycleExpanded,
                        onDismissRequest = { cycleExpanded = false }
                    ) {
                        BillCycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.displayName) },
                                onClick = {
                                    billingCycle = cycle
                                    cycleExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "Day $billingDay",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        daysInMonth.forEach { day ->
                            DropdownMenuItem(
                                text = { Text("Day $day") },
                                onClick = {
                                    billingDay = day
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    "Remind Me",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = notify3Days,
                        onClick = { notify3Days = !notify3Days },
                        label = { Text("3 days") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = notify2Days,
                        onClick = { notify2Days = !notify2Days },
                        label = { Text("2 days") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = notify1Day,
                        onClick = { notify1Day = !notify1Day },
                        label = { Text("1 day") },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0 && name.isNotBlank()) {
                        val notifyDays = buildList {
                            if (notify3Days) add(3)
                            if (notify2Days) add(2)
                            if (notify1Day) add(1)
                        }
                        onSave(
                            name,
                            amountValue,
                            billingDay,
                            billingCycle,
                            category,
                            notifyDays,
                            isNotificationEnabled,
                            notes.ifBlank { null }
                        )
                    }
                },
                enabled = amount.toDoubleOrNull() != null && name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBillDialog(
    bill: BillItem,
    onDismiss: () -> Unit,
    onSave: (com.rudra.savingbuddy.domain.model.BillReminder) -> Unit
) {
    var name by remember { mutableStateOf(bill.name) }
    var amount by remember { mutableStateOf(bill.amount.toString()) }
    var billingDay by remember { mutableStateOf(bill.billingDay) }
    var billingCycle by remember { mutableStateOf(bill.billingCycle) }
    var category by remember { mutableStateOf(bill.category) }
    var isActive by remember { mutableStateOf(bill.isActive) }
    var isNotificationEnabled by remember { mutableStateOf(bill.isNotificationEnabled) }
    var notes by remember { mutableStateOf(bill.notes ?: "") }
    
    var categoryExpanded by remember { mutableStateOf(false) }
    var cycleExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Bill") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active")
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it }
                    )
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Notifications")
                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { isNotificationEnabled = it }
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Bill Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                    label = { Text("Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        billCategories.forEach { (cat, _) ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = cycleExpanded,
                    onExpandedChange = { cycleExpanded = it }
                ) {
                    OutlinedTextField(
                        value = billingCycle.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = cycleExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = cycleExpanded,
                        onDismissRequest = { cycleExpanded = false }
                    ) {
                        BillCycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.displayName) },
                                onClick = {
                                    billingCycle = cycle
                                    cycleExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = dayExpanded,
                    onExpandedChange = { dayExpanded = it }
                ) {
                    OutlinedTextField(
                        value = "Day $billingDay",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Day") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = dayExpanded,
                        onDismissRequest = { dayExpanded = false }
                    ) {
                        (1..31).forEach { day ->
                            DropdownMenuItem(
                                text = { Text("Day $day") },
                                onClick = {
                                    billingDay = day
                                    dayExpanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val updatedBill = com.rudra.savingbuddy.domain.model.BillReminder(
                        id = bill.id,
                        name = name,
                        amount = amount.toDoubleOrNull() ?: bill.amount,
                        billingDay = billingDay,
                        billingCycle = billingCycle,
                        category = category,
                        isActive = isActive,
                        notifyDaysBefore = bill.notifyDaysBefore,
                        isNotificationEnabled = isNotificationEnabled,
                        notes = notes.ifBlank { null },
                        createdAt = System.currentTimeMillis()
                    )
                    onSave(updatedBill)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NotificationSettingsDialog(
    settings: BillNotificationSettings,
    onDismiss: () -> Unit,
    onSave: (BillNotificationSettings) -> Unit
) {
    var notify3Days by remember { mutableStateOf(settings.notify3DaysBefore) }
    var notify2Days by remember { mutableStateOf(settings.notify2DaysBefore) }
    var notify1Day by remember { mutableStateOf(settings.notify1DayBefore) }
    var notifyOnDueDate by remember { mutableStateOf(settings.notifyOnDueDate) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Notification Settings") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "Choose when to receive bill reminders:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("3 days before")
                    Switch(
                        checked = notify3Days,
                        onCheckedChange = { notify3Days = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("2 days before")
                    Switch(
                        checked = notify2Days,
                        onCheckedChange = { notify2Days = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("1 day before")
                    Switch(
                        checked = notify1Day,
                        onCheckedChange = { notify1Day = it }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("On due date")
                    Switch(
                        checked = notifyOnDueDate,
                        onCheckedChange = { notifyOnDueDate = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        BillNotificationSettings(
                            notify3DaysBefore = notify3Days,
                            notify2DaysBefore = notify2Days,
                            notify1DayBefore = notify1Day,
                            notifyOnDueDate = notifyOnDueDate,
                            defaultNotifyDays = buildList {
                                if (notify3Days) add(3)
                                if (notify2Days) add(2)
                                if (notify1Day) add(1)
                            }
                        )
                    )
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}