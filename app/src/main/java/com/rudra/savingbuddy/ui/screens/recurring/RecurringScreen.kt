package com.rudra.savingbuddy.ui.screens.recurring

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval
import com.rudra.savingbuddy.domain.model.RecurringStatus
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.ui.theme.IncomeGreen
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    navController: NavController,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var itemToDelete by remember { mutableStateOf<RecurringItem?>(null) }
    var selectedItem by remember { mutableStateOf<RecurringItem?>(null) }
    var showBulkActions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isBulkMode) {
                        TextButton(onClick = { viewModel.toggleBulkMode() }) {
                            Text("Cancel")
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleBulkMode() }) {
                            Icon(Icons.Default.Checklist, contentDescription = "Bulk Select")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            if (!uiState.isBulkMode) {
                FloatingActionButton(
                    onClick = { viewModel.showAddDialog() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Recurring")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Insights Cards
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    InsightCard(
                        title = "Monthly Income",
                        amount = uiState.totalMonthlyIncome,
                        color = IncomeGreen,
                        icon = Icons.Default.TrendingUp,
                        modifier = Modifier.weight(1f)
                    )
                    InsightCard(
                        title = "Monthly Expenses",
                        amount = uiState.totalMonthlyExpense,
                        color = ExpenseRed,
                        icon = Icons.Default.TrendingDown,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Net Balance
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Net Monthly", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.format(uiState.totalMonthlyIncome - uiState.totalMonthlyExpense),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (uiState.totalMonthlyIncome >= uiState.totalMonthlyExpense) IncomeGreen else ExpenseRed
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Commitments", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                CurrencyFormatter.format(uiState.commitmentsTotal),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                        }
                    }
                }
            }

            // Warnings
            if (uiState.warnings.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, null, tint = Color(0xFFFF9800))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                uiState.warnings.forEach { warning ->
                                    Text(warning, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }

            // Smart Suggestions
            if (uiState.suggestions.isNotEmpty()) {
                item {
                    Text(
                        "Smart Suggestions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(uiState.suggestions) { suggestion ->
                            SuggestionCard(suggestion = suggestion)
                        }
                    }
                }
            }

            // Section Header with Toggle
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Active Recurring (${uiState.recurringItems.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Recurring Items
            if (uiState.recurringItems.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.Repeat,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No recurring transactions",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Add recurring income/expenses or create from transaction patterns",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            } else {
                items(uiState.recurringItems) { item ->
                    RecurringItemCard(
                        item = item,
                        isSelected = uiState.selectedItems.contains(item.id),
                        isBulkMode = uiState.isBulkMode,
                        onClick = {
                            if (uiState.isBulkMode) {
                                viewModel.toggleItemSelection(item.id)
                            } else {
                                selectedItem = item
                            }
                        },
                        onLongClick = {
                            viewModel.toggleItemSelection(item.id)
                        },
                        onDelete = { itemToDelete = item },
                        onPause = { months -> viewModel.pauseItem(item, months) },
                        onSkip = { viewModel.skipNextOccurrence(item) }
                    )
                }
            }

            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    // Bulk Actions Bar
    if (uiState.isBulkMode && uiState.selectedItems.isNotEmpty()) {
        BulkActionsBar(
            selectedCount = uiState.selectedItems.size,
            onDelete = { viewModel.deleteSelected() },
            onPause = { months -> viewModel.pauseSelected(months) }
        )
    }

    // Add Dialog
    if (uiState.showAddDialog) {
        AddRecurringDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSaveIncome = { name, amount, category, interval, notes, endCondition, endDate, occurrences ->
                viewModel.saveRecurringIncome(name, amount, category, interval, notes, endCondition, endDate, occurrences)
            },
            onSaveExpense = { name, amount, category, interval, notes, endCondition, endDate, occurrences ->
                viewModel.saveRecurringExpense(name, amount, category, interval, notes, endCondition, endDate, occurrences)
            }
        )
    }

    // Delete Confirmation
    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Recurring") },
            text = { Text("Delete \"${item.name}\"? Future occurrences will stop.") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecurring(item); itemToDelete = null }) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancel") } }
        )
    }

    // Item Details Sheet
    selectedItem?.let { item ->
        ItemDetailsSheet(
            item = item,
            onDismiss = { selectedItem = null },
            onPause = { months -> viewModel.pauseItem(item, months); selectedItem = null },
            onSkip = { viewModel.skipNextOccurrence(item); selectedItem = null },
            onDelete = { itemToDelete = item; selectedItem = null }
        )
    }
}

@Composable
fun InsightCard(
    title: String,
    amount: Double,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun SuggestionCard(suggestion: RecurringSuggestion) {
    Card(
        modifier = Modifier.width(180.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Lightbulb, null, tint = Color(0xFFFFC107), modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Pattern Detected", style = MaterialTheme.typography.labelSmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(suggestion.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text(
                "${suggestion.occurrences}x detected • ~${suggestion.avgDaysBetween} days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                CurrencyFormatter.format(suggestion.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = { /* Add as recurring */ },
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text("Add", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RecurringItemCard(
    item: RecurringItem,
    isSelected: Boolean,
    isBulkMode: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDelete: () -> Unit,
    onPause: (Int) -> Unit,
    onSkip: () -> Unit
) {
    val daysUntil = ((item.nextDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
    val isUrgent = daysUntil <= 3 && item.status == RecurringStatus.ACTIVE
    val isIncome = item.type == "Income"
    val isPaused = item.status == RecurringStatus.PAUSED

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> MaterialTheme.colorScheme.primaryContainer
                isPaused -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                isUrgent -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                isIncome -> IncomeGreen.copy(alpha = 0.1f)
                else -> ExpenseRed.copy(alpha = 0.1f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Checkbox in bulk mode
            if (isBulkMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() }
                )
            }

            // Icon
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isIncome) IncomeGreen.copy(alpha = 0.2f) 
                        else ExpenseRed.copy(alpha = 0.2f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null,
                    tint = if (isIncome) IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Content
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isPaused) {
                        Spacer(modifier = Modifier.width(6.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text("Paused", style = MaterialTheme.typography.labelSmall) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = Color(0xFFFFE0B2)
                            ),
                            modifier = Modifier.height(20.dp)
                        )
                    }
                    if (item.isVariableAmount) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            Icons.Default.TrendingFlat,
                            null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        item.interval.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        " • ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        item.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    when {
                        isPaused -> "Resumes: ${DateUtils.formatDate(item.pausedUntil ?: 0)}"
                        daysUntil < 0 -> "Due ${-daysUntil} days ago"
                        daysUntil == 0 -> "Due today"
                        daysUntil == 1 -> "Due tomorrow"
                        daysUntil <= 7 -> "Due in $daysUntil days"
                        else -> "Next: ${DateUtils.formatDate(item.nextDate)}"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = when {
                        daysUntil <= 1 -> ExpenseRed
                        daysUntil <= 3 -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(item.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isIncome) IncomeGreen else ExpenseRed
                )

                // Quick actions (only when not in bulk mode)
                if (!isBulkMode && !isPaused) {
                    Row {
                        IconButton(
                            onClick = onSkip,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.SkipNext,
                                "Skip",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(
                            onClick = { onPause(1) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.Default.Pause,
                                "Pause",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BulkActionsBar(
    selectedCount: Int,
    onDelete: () -> Unit,
    onPause: (Int) -> Unit
) {
    var showPauseDialog by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("$selectedCount selected", style = MaterialTheme.typography.labelMedium)
            }
            Row {
                FilledTonalButton(onClick = { showPauseDialog = true }) {
                    Icon(Icons.Default.Pause, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pause")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete")
                }
            }
        }
    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = { Text("Pause for...") },
            text = {
                Column {
                    listOf(1, 2, 3).forEach { months ->
                        TextButton(
                            onClick = { onPause(months); showPauseDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("$months month${if (months > 1) "s" else ""}")
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showPauseDialog = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemDetailsSheet(
    item: RecurringItem,
    onDismiss: () -> Unit,
    onPause: (Int) -> Unit,
    onSkip: () -> Unit,
    onDelete: () -> Unit
) {
    var showEndConditionDialog by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (item.type == "Income") IncomeGreen.copy(alpha = 0.2f)
                            else ExpenseRed.copy(alpha = 0.2f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        if (item.type == "Income") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null,
                        tint = if (item.type == "Income") IncomeGreen else ExpenseRed
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text(item.category, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    CurrencyFormatter.format(item.amount),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (item.type == "Income") IncomeGreen else ExpenseRed
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Details
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    DetailRow(label = "Interval", value = item.interval.displayName)
                    DetailRow(label = "Next Due", value = DateUtils.formatDate(item.nextDate))
                    if (item.isVariableAmount) {
                        DetailRow(label = "Variable Amount", value = "Yes (avg ₹${item.averageAmount})")
                    }
                    item.notes?.let { DetailRow(label = "Notes", value = it) }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Actions
            Text("Actions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onSkip, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.SkipNext, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Skip Next")
                }
                OutlinedButton(onClick = { showPauseDialog = true }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Pause, null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pause")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = { showEndConditionDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Set End Condition")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onDelete,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
            ) {
                Icon(Icons.Default.Delete, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete Recurring")
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Pause Dialog
    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = { Text("Pause Recurring") },
            text = { Text("Pause this recurring transaction temporarily?") },
            confirmButton = {
                TextButton(onClick = { onPause(1); showPauseDialog = false }) {
                    Text("1 Month")
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = { showPauseDialog = false }) { Text("Cancel") }
                }
            }
        )
    }

    // End Condition Dialog
    if (showEndConditionDialog) {
        AlertDialog(
            onDismissRequest = { showEndConditionDialog = false },
            title = { Text("End Condition") },
            text = {
                Column {
                    listOf("Never", "After 6 months", "After 12 months", "On specific date").forEach { option ->
                        TextButton(
                            onClick = { showEndConditionDialog = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(option)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showEndConditionDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onSaveIncome: (String, Double, IncomeCategory, RecurringInterval, String?, com.rudra.savingbuddy.domain.model.EndCondition, Long?, Int?) -> Unit,
    onSaveExpense: (String, Double, ExpenseCategory, RecurringInterval, String?, com.rudra.savingbuddy.domain.model.EndCondition, Long?, Int?) -> Unit
) {
    var selectedType by remember { mutableStateOf("Expense") }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedIncomeCategory by remember { mutableStateOf(IncomeCategory.SALARY) }
    var selectedExpenseCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var selectedInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
    var notes by remember { mutableStateOf("") }
    var isVariableAmount by remember { mutableStateOf(false) }
    var needsApproval by remember { mutableStateOf(false) }
    var incomeCategoryExpanded by remember { mutableStateOf(false) }
    var expenseCategoryExpanded by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == "Income",
                        onClick = { selectedType = "Income" },
                        label = { Text("Income") },
                        leadingIcon = { Icon(Icons.Default.TrendingUp, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = selectedType == "Expense",
                        onClick = { selectedType = "Expense" },
                        label = { Text("Expense") },
                        leadingIcon = { Icon(Icons.Default.TrendingDown, null, modifier = Modifier.size(16.dp)) },
                        modifier = Modifier.weight(1f)
                    )
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    placeholder = { Text("e.g., Netflix, Rent, Salary") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    leadingIcon = { Text("₹", style = MaterialTheme.typography.bodyLarge) }
                )

                // Variable Amount Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Variable Amount", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.weight(1f))
                    Switch(checked = isVariableAmount, onCheckedChange = { isVariableAmount = it })
                }

                // Category
                if (selectedType == "Income") {
                    ExposedDropdownMenuBox(expanded = incomeCategoryExpanded, onExpandedChange = { incomeCategoryExpanded = it }) {
                        OutlinedTextField(
                            value = selectedIncomeCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = incomeCategoryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = incomeCategoryExpanded, onDismissRequest = { incomeCategoryExpanded = false }) {
                            IncomeCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = { selectedIncomeCategory = cat; incomeCategoryExpanded = false }
                                )
                            }
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(expanded = expenseCategoryExpanded, onExpandedChange = { expenseCategoryExpanded = it }) {
                        OutlinedTextField(
                            value = selectedExpenseCategory.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expenseCategoryExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor()
                        )
                        ExposedDropdownMenu(expanded = expenseCategoryExpanded, onDismissRequest = { expenseCategoryExpanded = false }) {
                            ExpenseCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = { selectedExpenseCategory = cat; expenseCategoryExpanded = false }
                                )
                            }
                        }
                    }
                }

                // Interval
                ExposedDropdownMenuBox(expanded = intervalExpanded, onExpandedChange = { intervalExpanded = it }) {
                    OutlinedTextField(
                        value = selectedInterval.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Repeat") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = intervalExpanded, onDismissRequest = { intervalExpanded = false }) {
                        RecurringInterval.entries.forEach { interval ->
                            DropdownMenuItem(
                                text = { Text(interval.displayName) },
                                onClick = { selectedInterval = interval; intervalExpanded = false }
                            )
                        }
                    }
                }

                // Needs Approval Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Needs Approval", style = MaterialTheme.typography.bodyMedium)
                        Text("Review before adding", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(checked = needsApproval, onCheckedChange = { needsApproval = it })
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
                    if (amountValue > 0) {
                        if (selectedType == "Income") {
                            onSaveIncome(
                                name.ifBlank { selectedIncomeCategory.displayName },
                                amountValue,
                                selectedIncomeCategory,
                                selectedInterval,
                                notes.ifBlank { null },
                                com.rudra.savingbuddy.domain.model.EndCondition.NEVER,
                                null,
                                null
                            )
                        } else {
                            onSaveExpense(
                                name.ifBlank { selectedExpenseCategory.displayName },
                                amountValue,
                                selectedExpenseCategory,
                                selectedInterval,
                                notes.ifBlank { null },
                                com.rudra.savingbuddy.domain.model.EndCondition.NEVER,
                                null,
                                null
                            )
                        }
                    }
                },
                enabled = amount.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}