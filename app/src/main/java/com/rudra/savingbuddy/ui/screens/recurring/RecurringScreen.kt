package com.rudra.savingbuddy.ui.screens.recurring

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.ui.theme.IncomeGreen
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecurringScreen(
    navController: NavController,
    viewModel: RecurringViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var itemToDelete by remember { mutableStateOf<RecurringItem?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recurring Transactions", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Recurring")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.recurringItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Repeat, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No recurring transactions", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Tap + to add recurring income or expense", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                // Summary
                Card(modifier = Modifier.fillMaxWidth().padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${uiState.recurringItems.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                            Text("Active", style = MaterialTheme.typography.bodySmall)
                        }
                        val incomeMonthly = uiState.recurringItems.filter { it.type == "Income" && it.interval == RecurringInterval.MONTHLY }.sumOf { it.amount }
                        val expenseMonthly = uiState.recurringItems.filter { it.type == "Expense" && it.interval == RecurringInterval.MONTHLY }.sumOf { it.amount }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(CurrencyFormatter.format(incomeMonthly - expenseMonthly), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = if (incomeMonthly >= expenseMonthly) IncomeGreen else ExpenseRed)
                            Text("Net Monthly", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(uiState.recurringItems) { item ->
                        RecurringItemCard(item = item, onDelete = { itemToDelete = item })
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddRecurringDialog(
            onDismiss = { viewModel.hideAddDialog() },
            onSaveIncome = { name, amount, category, interval, notes ->
                viewModel.saveRecurringIncome(name, amount, category, interval, notes)
            },
            onSaveExpense = { name, amount, category, interval, notes ->
                viewModel.saveRecurringExpense(name, amount, category, interval, notes)
            }
        )
    }

    itemToDelete?.let { item ->
        AlertDialog(
            onDismissRequest = { itemToDelete = null },
            title = { Text("Delete Recurring") },
            text = { Text("Delete \"${item.name}\"?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteRecurring(item); itemToDelete = null }, colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)) {
                    Text("Delete")
                }
            },
            dismissButton = { TextButton(onClick = { itemToDelete = null }) { Text("Cancel") } }
        )
    }
}

@Composable
fun RecurringItemCard(item: RecurringItem, onDelete: () -> Unit) {
    val daysUntil = ((item.nextDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000)).toInt()
    val isUrgent = daysUntil <= 3
    val isIncome = item.type == "Income"

    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(
        containerColor = when {
            isUrgent -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            isIncome -> IncomeGreen.copy(alpha = 0.1f)
            else -> ExpenseRed.copy(alpha = 0.1f)
        }
    )) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(if (isIncome) IncomeGreen.copy(alpha = 0.2f) else ExpenseRed.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null, tint = if (isIncome) IncomeGreen else ExpenseRed, modifier = Modifier.size(24.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Medium)
                    Spacer(modifier = Modifier.width(8.dp))
                    AssistChip(onClick = {}, label = { Text(item.type) }, colors = AssistChipDefaults.assistChipColors(containerColor = if (isIncome) IncomeGreen.copy(alpha = 0.2f) else ExpenseRed.copy(alpha = 0.2f)))
                }
                Text("${item.interval.displayName} • ${item.category}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(when {
                    daysUntil < 0 -> "Due ${-daysUntil} days ago"
                    daysUntil == 0 -> "Due today"
                    daysUntil == 1 -> "Due tomorrow"
                    daysUntil <= 7 -> "Due in $daysUntil days"
                    else -> "Next: ${DateUtils.formatDate(item.nextDate)}"
                }, style = MaterialTheme.typography.labelSmall, color = when {
                    daysUntil <= 1 -> ExpenseRed
                    daysUntil <= 3 -> Color(0xFFFF9800)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                })
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyFormatter.format(item.amount), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isIncome) IncomeGreen else ExpenseRed)
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = ExpenseRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRecurringDialog(
    onDismiss: () -> Unit,
    onSaveIncome: (String, Double, IncomeCategory, RecurringInterval, String?) -> Unit,
    onSaveExpense: (String, Double, ExpenseCategory, RecurringInterval, String?) -> Unit
) {
    var selectedType by remember { mutableStateOf("Income") }
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedIncomeCategory by remember { mutableStateOf(IncomeCategory.SALARY) }
    var selectedExpenseCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var selectedInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
    var notes by remember { mutableStateOf("") }
    var incomeCategoryExpanded by remember { mutableStateOf(false) }
    var expenseCategoryExpanded by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Recurring") },
        text = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Type Toggle
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(selected = selectedType == "Income", onClick = { selectedType = "Income" }, label = { Text("Income") }, modifier = Modifier.weight(1f))
                    FilterChip(selected = selectedType == "Expense", onClick = { selectedType = "Expense" }, label = { Text("Expense") }, modifier = Modifier.weight(1f))
                }

                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth(), singleLine = true)

                OutlinedTextField(
                    value = amount,
                    onValueChange = { if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                if (selectedType == "Income") {
                    ExposedDropdownMenuBox(expanded = incomeCategoryExpanded, onExpandedChange = { incomeCategoryExpanded = it }) {
                        OutlinedTextField(value = selectedIncomeCategory.displayName, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = incomeCategoryExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = incomeCategoryExpanded, onDismissRequest = { incomeCategoryExpanded = false }) {
                            IncomeCategory.entries.forEach { cat -> DropdownMenuItem(text = { Text(cat.displayName) }, onClick = { selectedIncomeCategory = cat; incomeCategoryExpanded = false }) }
                        }
                    }
                } else {
                    ExposedDropdownMenuBox(expanded = expenseCategoryExpanded, onExpandedChange = { expenseCategoryExpanded = it }) {
                        OutlinedTextField(value = selectedExpenseCategory.displayName, onValueChange = {}, readOnly = true, label = { Text("Category") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expenseCategoryExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                        ExposedDropdownMenu(expanded = expenseCategoryExpanded, onDismissRequest = { expenseCategoryExpanded = false }) {
                            ExpenseCategory.entries.forEach { cat -> DropdownMenuItem(text = { Text(cat.displayName) }, onClick = { selectedExpenseCategory = cat; expenseCategoryExpanded = false }) }
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = intervalExpanded, onExpandedChange = { intervalExpanded = it }) {
                    OutlinedTextField(value = selectedInterval.displayName, onValueChange = {}, readOnly = true, label = { Text("Repeat") }, trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) }, modifier = Modifier.fillMaxWidth().menuAnchor())
                    ExposedDropdownMenu(expanded = intervalExpanded, onDismissRequest = { intervalExpanded = false }) {
                        RecurringInterval.entries.forEach { interval -> DropdownMenuItem(text = { Text(interval.displayName) }, onClick = { selectedInterval = interval; intervalExpanded = false }) }
                    }
                }

                OutlinedTextField(value = notes, onValueChange = { notes = it }, label = { Text("Notes (optional)") }, modifier = Modifier.fillMaxWidth(), maxLines = 2)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amountValue = amount.toDoubleOrNull() ?: 0.0
                if (amountValue > 0) {
                    if (selectedType == "Income") {
                        onSaveIncome(name.ifBlank { selectedIncomeCategory.displayName }, amountValue, selectedIncomeCategory, selectedInterval, notes.ifBlank { null })
                    } else {
                        onSaveExpense(name.ifBlank { selectedExpenseCategory.displayName }, amountValue, selectedExpenseCategory, selectedInterval, notes.ifBlank { null })
                    }
                }
            }, enabled = amount.isNotBlank()) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}