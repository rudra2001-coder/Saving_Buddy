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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.ui.components.ExpenseDialog
import com.rudra.savingbuddy.ui.components.QuickAddExpenseDialog
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

private val Red600 = Color(0xFFA32D2D)
private val Red50 = Color(0xFFFCEBEB)
private val Blue600 = Color(0xFF185FA5)

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
                containerColor = Red600,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Expense", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(ExpenseRed.copy(alpha = 0.08f), ExpenseRed.copy(alpha = 0.02f))))
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Expense History", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                                Text("${uiState.totalCount} records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(ExpenseRed.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = ExpenseRed, modifier = Modifier.size(22.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it; viewModel.searchExpenses(it) },
                            placeholder = { Text("Search expenses...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp)).background(Blue600.copy(alpha = 0.1f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.Search, null, tint = Blue600, modifier = Modifier.size(16.dp))
                                }
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = ""; viewModel.searchExpenses("") }) {
                                        Icon(Icons.Default.Clear, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ExpenseRed,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                cursorColor = ExpenseRed
                            )
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                onClick = { selectedCategory = null; viewModel.filterByCategory(null) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (selectedCategory == null) ExpenseRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selectedCategory == null) ExpenseRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(
                                    "All",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedCategory == null) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                            ExpenseCategory.entries.take(8).forEach { category ->
                                val isSelected = selectedCategory == category
                                Surface(
                                    onClick = { selectedCategory = category; viewModel.filterByCategory(category) },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) ExpenseRed.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) ExpenseRed.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    ) {
                                        Text(categoryEmojis[category] ?: "📦", style = MaterialTheme.typography.bodyMedium)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            category.displayName,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            color = if (isSelected) ExpenseRed else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1
                                        )
                                    }
                                }
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
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier.size(72.dp).clip(CircleShape).background(ExpenseRed.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(36.dp), tint = ExpenseRed.copy(alpha = 0.5f))
                            }
                            Spacer(Modifier.height(20.dp))
                            Text("No expenses recorded yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(Modifier.height(6.dp))
                            Text("Tap + to add your first expense", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            items(uiState.expenseList, key = { it.id }) { expense ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.showEditDialog(expense) }.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(ExpenseRed.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(categoryEmojis[expense.category] ?: "📦", style = MaterialTheme.typography.titleMedium)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                expense.category.displayName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                expense.notes?.let {
                                    Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                                if (expense.notes != null) {
                                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(DateUtils.formatShortDate(expense.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                "-${CurrencyFormatter.formatCompact(expense.amount)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            if (expense.isRecurring) {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Icon(Icons.Default.Repeat, null, tint = ExpenseRed, modifier = Modifier.size(12.dp))
                                    Text("Recurring", style = MaterialTheme.typography.labelSmall, color = ExpenseRed)
                                }
                            }
                        }
                        Spacer(Modifier.width(4.dp))
                        IconButton(onClick = { showDeleteDialog = expense }) {
                            Icon(Icons.Default.Delete, null, tint = ExpenseRed.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                        }
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
            icon = {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape).background(ExpenseRed.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Delete, null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                }
            },
            title = { Text("Delete Expense", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete ${CurrencyFormatter.formatBDT(expense.amount)}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteExpense(expense); showDeleteDialog = null }) {
                    Text("Delete", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
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
