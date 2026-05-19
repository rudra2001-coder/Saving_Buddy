package com.rudra.savingbuddy.ui.screens.recurring

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
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
import com.rudra.savingbuddy.domain.model.EndCondition
import com.rudra.savingbuddy.domain.model.RecurringStatus
import com.rudra.savingbuddy.ui.theme.*
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
                title = {
                    Column {
                        Text("Recurring", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Text("Manage recurring transactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.isBulkMode) {
                        TextButton(onClick = { viewModel.toggleBulkMode() }) {
                            Text("Cancel", fontWeight = FontWeight.SemiBold, color = ExpenseRed)
                        }
                    } else {
                        IconButton(onClick = { viewModel.toggleBulkMode() }) {
                            Icon(Icons.Default.Checklist, contentDescription = "Bulk Select", tint = IncomeGreen)
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
                    containerColor = IncomeGreen,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Recurring", tint = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Insight Cards
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

            // Net Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(IncomeGreen.copy(alpha = 0.06f), Color.Transparent)))
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Net Monthly", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                                    .background(if (uiState.totalMonthlyIncome >= uiState.totalMonthlyExpense) IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center) {
                                    Icon(if (uiState.totalMonthlyIncome >= uiState.totalMonthlyExpense) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                                        null, tint = if (uiState.totalMonthlyIncome >= uiState.totalMonthlyExpense) IncomeGreen else ExpenseRed,
                                        modifier = Modifier.size(16.dp))
                                }
                                Text(
                                    CurrencyFormatter.format(uiState.totalMonthlyIncome - uiState.totalMonthlyExpense),
                                    style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
                                    color = if (uiState.totalMonthlyIncome >= uiState.totalMonthlyExpense) IncomeGreen else ExpenseRed
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Commitments", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(CurrencyFormatter.format(uiState.commitmentsTotal), style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold, color = ExpenseRed)
                        }
                    }
                }
            }

            // Warnings
            if (uiState.warnings.isNotEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = WarningOrange.copy(alpha = 0.1f)),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, WarningOrange.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(WarningOrange.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Warning, null, tint = WarningOrange, modifier = Modifier.size(18.dp))
                            }
                            Column {
                                uiState.warnings.forEach { warning ->
                                    Text(warning, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = WarningOrange)
                                }
                            }
                        }
                    }
                }
            }

            // Smart Suggestions
            if (uiState.suggestions.isNotEmpty()) {
                item {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AccentPurple))
                        Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(18.dp), tint = AccentPurple)
                        Text("Smart Suggestions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                }
                item {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(uiState.suggestions) { suggestion ->
                            SuggestionCard(suggestion = suggestion)
                        }
                    }
                }
            }

            // Active Recurring Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(IncomeGreen))
                        Icon(Icons.Default.Repeat, null, modifier = Modifier.size(18.dp), tint = IncomeGreen)
                        Text("Active Recurring", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    }
                    Surface(shape = RoundedCornerShape(8.dp), color = IncomeGreen.copy(alpha = 0.1f)) {
                        Text("${uiState.recurringItems.size}", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = IncomeGreen)
                    }
                }
            }

            // Empty State
            if (uiState.recurringItems.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(IncomeGreen.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Repeat, null, modifier = Modifier.size(36.dp), tint = IncomeGreen.copy(alpha = 0.5f))
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No recurring transactions", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Text("Add recurring income/expenses or create from transaction patterns",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center, modifier = Modifier.padding(horizontal = 24.dp))
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
                        onLongClick = { viewModel.toggleItemSelection(item.id) },
                    onDelete = { itemToDelete = item },
                    onPause = { months -> viewModel.pauseItem(item, months) },
                    onSkip = { viewModel.skipNextOccurrence(item) },
                    onApprove = { viewModel.approveItem(item) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
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
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Default.Delete, null, tint = ExpenseRed, modifier = Modifier.size(40.dp)) },
            title = { Text("Delete Recurring", fontWeight = FontWeight.Bold) },
            text = { Text("Delete \"${item.name}\"? Future occurrences will stop.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteRecurring(item); itemToDelete = null },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) { Text("Delete", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { itemToDelete = null }) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }

    // Item Details Sheet
    selectedItem?.let { item ->
        ItemDetailsSheet(
            item = item,
            onDismiss = { selectedItem = null },
            onPause = { months -> viewModel.pauseItem(item, months); selectedItem = null },
            onSkip = { viewModel.skipNextOccurrence(item); selectedItem = null },
            onDelete = { itemToDelete = item; selectedItem = null },
            onApprove = { viewModel.approveItem(item); selectedItem = null }
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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(color.copy(alpha = 0.06f), Color.Transparent)))
            .padding(14.dp))
        {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(14.dp))
                }
                Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(CurrencyFormatter.format(amount), style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun SuggestionCard(suggestion: RecurringSuggestion) {
    Card(
        modifier = Modifier.width(200.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, AccentPurple.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(AccentPurple.copy(alpha = 0.06f), Color.Transparent)))
            .padding(14.dp))
        {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(modifier = Modifier.size(28.dp).clip(RoundedCornerShape(8.dp)).background(AccentPurple.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lightbulb, null, tint = AccentPurple, modifier = Modifier.size(14.dp))
                }
                Text("Pattern Detected", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = AccentPurple)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(suggestion.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
            Text("${suggestion.occurrences}x detected • ~${suggestion.avgDaysBetween} days",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(4.dp))
            Text(CurrencyFormatter.format(suggestion.amount), style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold, color = AccentPurple)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = AccentPurple.copy(alpha = 0.1f)
            ) {
                Text("${(suggestion.confidence * 100).toInt()}% match", modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = AccentPurple)
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
    onSkip: () -> Unit,
    onApprove: () -> Unit = {}
) {
    val isPastDue = item.nextDate < System.currentTimeMillis()
    val isToday = DateUtils.formatDate(item.nextDate) == DateUtils.formatDate(System.currentTimeMillis())
    val accentColor = when {
        item.status == RecurringStatus.PAUSED -> Color.Gray
        isPastDue -> ExpenseRed
        isToday -> WarningOrange
        item.type == "Income" -> IncomeGreen
        else -> ExpenseRed
    }

    val borderColor = when {
        isSelected -> IncomeGreen
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(if (isSelected) 1.5.dp else 0.5.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onClick, onLongClick = onLongClick)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (isBulkMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onClick() },
                    colors = CheckboxDefaults.colors(checkedColor = IncomeGreen),
                    modifier = Modifier.padding(end = 8.dp)
                )
            }

            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (item.type == "Income") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null, tint = accentColor, modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                    if (item.status == RecurringStatus.PAUSED) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Paused", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
                Text("${item.interval.displayName} • ${item.category}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                val dateText = when {
                    isPastDue -> "Overdue"
                    isToday -> "Today"
                    else -> DateUtils.formatShortDate(item.nextDate)
                }
                Text("Next: $dateText", style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Medium)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyFormatter.format(item.amount), style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold, color = accentColor)
                if (!isBulkMode) {
                    Row {
                        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                            Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
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

    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IncomeGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("$selectedCount selected", style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold, color = Color.White)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showPauseDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.6f))
                ) { Text("Pause", fontWeight = FontWeight.Medium) }
                Button(
                    onClick = onDelete,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) { Text("Delete", fontWeight = FontWeight.Medium) }
            }
        }
    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Pause Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 3, 6, 12).forEach { months ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onPause(months); showPauseDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Text("$months month${if (months > 1) "s" else ""}",
                                modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) { Text("Cancel") }
            }
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
    onDelete: () -> Unit,
    onApprove: () -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState()
    var showPauseDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(modifier = Modifier.size(48.dp).clip(RoundedCornerShape(16.dp))
                    .background(if (item.type == "Income") IncomeGreen.copy(alpha = 0.12f) else ExpenseRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center) {
                    Icon(if (item.type == "Income") Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null, tint = if (item.type == "Income") IncomeGreen else ExpenseRed, modifier = Modifier.size(24.dp))
                }
                Column {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("${item.type} • ${item.category}", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Amount", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(CurrencyFormatter.format(item.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = if (item.type == "Income") IncomeGreen else ExpenseRed) }
                Column(horizontalAlignment = Alignment.End) { Text("Interval", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.interval.displayName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }
            }

            Column { Text("Next Date", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(DateUtils.formatDate(item.nextDate), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium) }

            if (!item.notes.isNullOrBlank()) {
                Column { Text("Notes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item.notes, style = MaterialTheme.typography.bodyMedium) }
            }

            if (item.status == RecurringStatus.NEEDS_APPROVAL) {
                Button(
                    onClick = { onApprove(); onDismiss() },
                    modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) { Text("Approve & Add", fontWeight = FontWeight.SemiBold) }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = { showPauseDialog = true },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)
                ) { Text("Pause", fontWeight = FontWeight.Medium) }
                OutlinedButton(
                    onClick = { onSkip(); onDismiss() },
                    modifier = Modifier.weight(1f), shape = RoundedCornerShape(14.dp)
                ) { Text("Skip Next", fontWeight = FontWeight.Medium) }
            }

            Button(
                onClick = { onDelete(); onDismiss() },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
            ) { Text("Delete Recurring", fontWeight = FontWeight.SemiBold) }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            shape = RoundedCornerShape(24.dp),
            title = { Text("Pause Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(1, 3, 6, 12).forEach { months ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { onPause(months); showPauseDialog = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ) {
                            Text("$months month${if (months > 1) "s" else ""}",
                                modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPauseDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onSaveIncome: (String, Double, IncomeCategory, RecurringInterval, String?, EndCondition, Long?, Int?) -> Unit,
    onSaveExpense: (String, Double, ExpenseCategory, RecurringInterval, String?, EndCondition, Long?, Int?) -> Unit
) {
    var isIncome by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
    var notes by remember { mutableStateOf("") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.Repeat, null, tint = IncomeGreen, modifier = Modifier.size(22.dp))
                Text("Add Recurring", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Income/Expense Toggle
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(4.dp)) {
                        listOf(Triple(true, "Income", IncomeGreen), Triple(false, "Expense", ExpenseRed)).forEach { (isInc, label, color) ->
                            Surface(
                                modifier = Modifier.weight(1f).clip(RoundedCornerShape(12.dp)).clickable { isIncome = isInc },
                                color = if (isIncome == isInc) color else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(label, modifier = Modifier.padding(vertical = 10.dp).fillMaxWidth(),
                                    textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold,
                                    color = if (isIncome == isInc) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(if (isIncome) "Income Source" else "Expense Name") },
                    leadingIcon = { Icon(if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = if (isIncome) IncomeGreen else ExpenseRed) },
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isIncome) IncomeGreen else ExpenseRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), cursorColor = if (isIncome) IncomeGreen else ExpenseRed)
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    leadingIcon = { Text("৳", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold,
                        color = if (isIncome) IncomeGreen else ExpenseRed) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(), singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isIncome) IncomeGreen else ExpenseRed,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )

                // Interval Picker
                ExposedDropdownMenuBox(expanded = intervalExpanded, onExpandedChange = { intervalExpanded = it }) {
                    OutlinedTextField(
                        value = selectedInterval.displayName, onValueChange = {}, readOnly = true,
                        label = { Text("Interval") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isIncome) IncomeGreen else ExpenseRed,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    ExposedDropdownMenu(expanded = intervalExpanded, onDismissRequest = { intervalExpanded = false }) {
                        RecurringInterval.entries.forEach { interval ->
                            DropdownMenuItem(text = { Text(interval.displayName) },
                                onClick = { selectedInterval = interval; intervalExpanded = false })
                        }
                    }
                }

                // Category Picker
                ExposedDropdownMenuBox(expanded = categoryExpanded, onExpandedChange = { categoryExpanded = it }) {
                    OutlinedTextField(
                        value = selectedCategory, onValueChange = {}, readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = if (isIncome) IncomeGreen else ExpenseRed,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    )
                    ExposedDropdownMenu(expanded = categoryExpanded, onDismissRequest = { categoryExpanded = false }) {
                        val cats = if (isIncome) IncomeCategory.entries.map { it.displayName }
                        else ExpenseCategory.entries.map { it.displayName }
                        cats.forEach { cat ->
                            DropdownMenuItem(text = { Text(cat) },
                                onClick = { selectedCategory = cat; categoryExpanded = false })
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(), maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountVal = amount.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amountVal > 0 && selectedCategory.isNotBlank()) {
                        if (isIncome) {
                            val category = IncomeCategory.entries.find { it.displayName == selectedCategory } ?: IncomeCategory.OTHERS
                            onSaveIncome(name, amountVal, category, selectedInterval, notes.ifBlank { null }, EndCondition.NEVER, null, null)
                        } else {
                            val category = ExpenseCategory.entries.find { it.displayName == selectedCategory } ?: ExpenseCategory.OTHERS
                            onSaveExpense(name, amountVal, category, selectedInterval, notes.ifBlank { null }, EndCondition.NEVER, null, null)
                        }
                    }
                },
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null && selectedCategory.isNotBlank(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = if (isIncome) IncomeGreen else ExpenseRed)
            ) { Text("Add", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
    )
}
