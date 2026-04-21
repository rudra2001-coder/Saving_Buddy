package com.rudra.savingbuddy.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.ui.components.ExpenseDialog
import com.rudra.savingbuddy.ui.components.QuickAddExpenseDialog
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    navController: androidx.navigation.NavController? = null,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf<Expense?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<ExpenseCategory?>(null) }
    val listState = rememberLazyListState()

    val categoryEmojis = mapOf(
        ExpenseCategory.FOOD to "🍔", ExpenseCategory.TRANSPORT to "🚌", ExpenseCategory.BILLS to "💡",
        ExpenseCategory.SHOPPING to "🛒", ExpenseCategory.ENTERTAINMENT to "🎬", ExpenseCategory.HEALTH to "💊",
        ExpenseCategory.EDUCATION to "📚", ExpenseCategory.GIFTS to "🎁", ExpenseCategory.TRAVEL to "✈️",
        ExpenseCategory.SUBSCRIPTIONS to "📱", ExpenseCategory.RENT to "🏠", ExpenseCategory.UTILITY to "⚡",
        ExpenseCategory.INSURANCE to "🛡️", ExpenseCategory.TAX to "📋", ExpenseCategory.EMI to "🏦",
        ExpenseCategory.OTHERS to "📦"
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= uiState.expenseList.size - 5) {
                    viewModel.loadMoreExpenses()
                }
            }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showQuickAddDialog = true },
                containerColor = ExpenseRed,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Expense")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(ExpenseRed.copy(alpha = 0.1f), ExpenseRed.copy(alpha = 0.05f))))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Expense History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                Text("${uiState.totalCount} records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier.size(40.dp).background(ExpenseRed.copy(alpha = 0.2f), CircleShape).padding(8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TrendingDown, null, tint = ExpenseRed)
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; viewModel.searchExpenses(it) },
                            placeholder = { Text("Search expenses...") },
                            leadingIcon = { Icon(Icons.Default.Search, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = ExpenseRed)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null; viewModel.filterByCategory(null) },
                                label = { Text("All") },
                                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ExpenseRed.copy(alpha = 0.2f), selectedLabelColor = ExpenseRed)
                            )
                            ExpenseCategory.entries.take(8).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category; viewModel.filterByCategory(category) },
                                    label = { Row(verticalAlignment = Alignment.CenterVertically) { Text(categoryEmojis[category] ?: "📦"); Spacer(Modifier.width(4.dp)); Text(category.displayName, maxLines = 1) } },
                                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = ExpenseRed.copy(alpha = 0.2f), selectedLabelColor = ExpenseRed)
                                )
                            }
                        }
                    }
                }
            }

            if (uiState.expenseList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(Modifier.height(16.dp))
                            Text("No expenses recorded yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(Modifier.height(8.dp))
                            Text("Tap + to add your first expense", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }
                }
            }

            items(uiState.expenseList, key = { it.id }) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.showEditDialog(expense) }.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(modifier = Modifier.size(48.dp).background(ExpenseRed.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                            Text(categoryEmojis[expense.category] ?: "📦", style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(expense.category.displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                            expense.notes?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis) }
                            Text(DateUtils.formatDate(expense.date), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("-${CurrencyFormatter.formatBDT(expense.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = ExpenseRed)
                            if (expense.isRecurring) Icon(Icons.Default.Repeat, null, tint = ExpenseRed, modifier = Modifier.size(16.dp))
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(onClick = { showDeleteDialog = expense }) { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showQuickAddDialog) {
        QuickAddExpenseDialog(
            onDismiss = { showQuickAddDialog = false },
            onSave = { amount, category, date, notes, accountId ->
                viewModel.quickAdd(category, amount, accountId)
                showQuickAddDialog = false
            }
        )
    }

    showDeleteDialog?.let { expense ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Expense") },
            text = { Text("Delete ${CurrencyFormatter.formatBDT(expense.amount)}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteExpense(expense); showDeleteDialog = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    if (uiState.showAddDialog) {
        ExpenseDialog(
            expense = uiState.editingExpense,
            onDismiss = { viewModel.hideDialog() },
            onSave = { amount, category, date, notes, accountId ->
                viewModel.saveExpense(amount, category, date, notes, accountId)
            }
        )
    }
}