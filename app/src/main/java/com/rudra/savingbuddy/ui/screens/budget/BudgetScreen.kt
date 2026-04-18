package com.rudra.savingbuddy.ui.screens.budget

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.savingbuddy.domain.model.BillingCycle
import com.rudra.savingbuddy.domain.model.Subscription
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.ui.theme.IncomeGreen
import com.rudra.savingbuddy.ui.theme.SavingsBlue
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        floatingActionButton = {
            when (selectedTab) {
                0 -> FloatingActionButton(
                    onClick = { viewModel.showBudgetDialog() }
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Budget")
                }
                1 -> FloatingActionButton(
                    onClick = { viewModel.showAddSubscriptionDialog() }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Subscription")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Text(
                text = "Budget & Subscriptions",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Budget") },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Subscriptions") },
                    icon = { Icon(Icons.Default.Subscriptions, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Reminders") },
                    icon = { Icon(Icons.Default.Notifications, contentDescription = null) }
                )
            }

            when (selectedTab) {
                0 -> BudgetOverview(viewModel, uiState)
                1 -> SubscriptionsList(viewModel, uiState)
                2 -> RemindersList(viewModel, uiState)
            }
        }
    }

    if (uiState.showEditBudgetDialog) {
        BudgetDialog(
            currentBudget = uiState.totalBudget,
            currentThreshold = uiState.alertThreshold,
            onDismiss = { viewModel.hideBudgetDialog() },
            onSave = { amount, threshold, rollover ->
                viewModel.setBudget(amount, threshold, rollover)
            }
        )
    }

    if (uiState.showAddSubscriptionDialog) {
        SubscriptionDialog(
            subscription = uiState.editingSubscription,
            onDismiss = { viewModel.hideSubscriptionDialog() },
            onSave = { name, amount, cycle, date, category, isActive ->
                viewModel.saveSubscription(name, amount, cycle, date, category, isActive)
            }
        )
    }
}

@Composable
fun BudgetOverview(viewModel: BudgetViewModel, uiState: BudgetUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Monthly Budget",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = CurrencyFormatter.format(uiState.totalBudget),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Budget Status",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    val progress = if (uiState.totalBudget > 0) {
                        (uiState.spent / uiState.totalBudget).toFloat()
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(CircleShape),
                        color = when {
                            progress >= 0.9f -> ExpenseRed
                            progress >= 0.7f -> Color(0xFFFF9800)
                            else -> IncomeGreen
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Spent",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(uiState.spent),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Remaining",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(uiState.remaining),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Alert Threshold",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${uiState.alertThreshold}%",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFF9800)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Get notified when budget reaches ${uiState.alertThreshold}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (uiState.upcomingRenewals.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFF9800).copy(alpha = 0.1f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Upcoming Renewals",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        uiState.upcomingRenewals.forEach { sub ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(sub.name)
                                Text(
                                    text = CurrencyFormatter.format(sub.amount),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionsList(viewModel: BudgetViewModel, uiState: BudgetUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val totalMonthly = uiState.subscriptions
            .filter { it.isActive }
            .sumOf { sub ->
                when (sub.billingCycle) {
                    BillingCycle.DAILY -> sub.amount * 30
                    BillingCycle.WEEKLY -> sub.amount * 4
                    BillingCycle.MONTHLY -> sub.amount
                    BillingCycle.QUARTERLY -> sub.amount / 3
                    BillingCycle.YEARLY -> sub.amount / 12
                }
            }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SavingsBlue.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Monthly Total",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = CurrencyFormatter.format(totalMonthly),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SavingsBlue
                    )
                }
            }
        }

        if (uiState.subscriptions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.Subscriptions,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No subscriptions added",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        items(uiState.subscriptions) { subscription ->
            SubscriptionCard(
                subscription = subscription,
                onEdit = { viewModel.showEditSubscriptionDialog(subscription) },
                onDelete = { viewModel.deleteSubscription(subscription) },
                onToggle = { viewModel.toggleSubscriptionActive(subscription) }
            )
        }
    }
}

@Composable
fun SubscriptionCard(
    subscription: Subscription,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val daysUntil = ((subscription.nextBillingDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
    val isExpiringSoon = daysUntil in 0..3 && subscription.isActive

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !subscription.isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                isExpiringSoon -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surface
            }
        )
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
                    .background(
                        when {
                            !subscription.isActive -> Color.Gray.copy(alpha = 0.2f)
                            isExpiringSoon -> Color(0xFFFF9800).copy(alpha = 0.2f)
                            else -> SavingsBlue.copy(alpha = 0.2f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subscription.name.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !subscription.isActive -> Color.Gray
                        isExpiringSoon -> Color(0xFFFF9800)
                        else -> SavingsBlue
                    }
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${subscription.billingCycle.displayName} • ${subscription.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subscription.isActive) {
                    Text(
                        text = when {
                            daysUntil < 0 -> "Expired ${-daysUntil} days ago"
                            daysUntil == 0 -> "Expires today!"
                            daysUntil == 1 -> "Expires tomorrow"
                            daysUntil <= 7 -> "Expires in $daysUntil days"
                            else -> "Next: ${DateUtils.formatDate(subscription.nextBillingDate)}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = when {
                            daysUntil <= 3 -> ExpenseRed
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(subscription.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row {
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun RemindersList(viewModel: BudgetViewModel, uiState: BudgetUiState) {
    val reminders = viewModel.getSubscriptionReminders()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Upcoming Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (reminders.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.NotificationsOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = IncomeGreen
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No upcoming reminders",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "You're all caught up!",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        items(reminders) { (subscription, daysLeft) ->
            ReminderCard(subscription, daysLeft)
        }
    }
}

@Composable
fun ReminderCard(subscription: Subscription, daysLeft: Int) {
    val reminderType = when (daysLeft) {
        3 -> "Renews in 3 days"
        2 -> "Renews in 2 days"
        1 -> "Renews tomorrow!"
        0 -> "Expires today!"
        else -> "Expired ${-daysLeft} days ago"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = when {
                daysLeft <= 1 -> ExpenseRed.copy(alpha = 0.1f)
                daysLeft <= 3 -> Color(0xFFFF9800).copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.primaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when {
                    daysLeft <= 1 -> Icons.Default.Warning
                    daysLeft <= 3 -> Icons.Default.Schedule
                    else -> Icons.Default.Notifications
                },
                contentDescription = null,
                tint = when {
                    daysLeft <= 1 -> ExpenseRed
                    daysLeft <= 3 -> Color(0xFFFF9800)
                    else -> SavingsBlue
                },
                modifier = Modifier.size(32.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = reminderType,
                    style = MaterialTheme.typography.bodyMedium,
                    color = when {
                        daysLeft <= 1 -> ExpenseRed
                        daysLeft <= 3 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Text(
                text = CurrencyFormatter.format(subscription.amount),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BudgetDialog(
    currentBudget: Double,
    currentThreshold: Int,
    onDismiss: () -> Unit,
    onSave: (Double, Int, Boolean) -> Unit
) {
    var amount by remember { mutableStateOf(currentBudget.toString()) }
    var threshold by remember { mutableIntStateOf(currentThreshold) }
    var enableRollover by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Monthly Budget") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Budget Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Alert Threshold: $threshold%",
                    style = MaterialTheme.typography.labelMedium
                )
                Slider(
                    value = threshold.toFloat(),
                    onValueChange = { threshold = it.toInt() },
                    valueRange = 50f..100f,
                    steps = 9
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Rollover")
                    Switch(
                        checked = enableRollover,
                        onCheckedChange = { enableRollover = it }
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { onSave(it, threshold, enableRollover) }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDialog(
    subscription: Subscription?,
    onDismiss: () -> Unit,
    onSave: (String, Double, BillingCycle, Long, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(subscription?.name ?: "") }
    var amount by remember { mutableStateOf(subscription?.amount?.toString() ?: "") }
    var billingCycle by remember { mutableStateOf(subscription?.billingCycle ?: BillingCycle.MONTHLY) }
    var nextBillingDate by remember { mutableStateOf(subscription?.nextBillingDate ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) }
    var category by remember { mutableStateOf(subscription?.category ?: "Entertainment") }
    var isActive by remember { mutableStateOf(subscription?.isActive ?: true) }
    var showCyclePicker by remember { mutableStateOf(false) }

    val categories = listOf("Entertainment", "Music", "Shopping", "Productivity", "Utilities", "Fitness", "News", "Education", "Other")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subscription == null) "Add Subscription" else "Edit Subscription") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subscription Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = showCyclePicker,
                    onExpandedChange = { showCyclePicker = !showCyclePicker }
                ) {
                    OutlinedTextField(
                        value = billingCycle.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCyclePicker) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = showCyclePicker,
                        onDismissRequest = { showCyclePicker = false }
                    ) {
                        BillingCycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.displayName) },
                                onClick = {
                                    billingCycle = cycle
                                    showCyclePicker = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Next billing: ${DateUtils.formatDate(nextBillingDate)}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Category: $category",
                    style = MaterialTheme.typography.bodySmall
                )

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
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let {
                        onSave(name, it, billingCycle, nextBillingDate, category, isActive)
                    }
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