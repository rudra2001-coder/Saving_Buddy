package com.rudra.savingbuddy.ui.screens.income

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.ui.components.IncomeDialog
import com.rudra.savingbuddy.ui.theme.IncomeGreen
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    navController: androidx.navigation.NavController? = null,
    viewModel: IncomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf<Income?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<IncomeCategory?>(null) }
    val listState = rememberLazyListState()

    val categoryEmojis = mapOf(
        IncomeCategory.SALARY to "💰",
        IncomeCategory.FREELANCE to "💻",
        IncomeCategory.INVESTMENTS to "📈",
        IncomeCategory.BUSINESS to "🏢",
        IncomeCategory.GIFTS to "🎁",
        IncomeCategory.OTHERS to "💵"
    )

    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastIndex ->
                if (lastIndex != null && lastIndex >= uiState.incomeList.size - 5) {
                    viewModel.loadMoreIncome()
                }
            }
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = IncomeGreen,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Income")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HeaderCard(
                    searchQuery = searchQuery,
                    onSearchChange = { searchQuery = it; viewModel.searchIncome(it) },
                    selectedCategory = selectedCategory,
                    onCategorySelect = { selectedCategory = it; viewModel.filterByCategory(it) },
                    totalCount = uiState.totalCount,
                    categoryEmojis = categoryEmojis
                )
            }

            if (uiState.incomeList.isEmpty()) {
                item {
                    EmptyStateCard()
                }
            }

            items(uiState.incomeList, key = { it.id }) { income ->
                IncomeListCard(
                    income = income,
                    categoryEmoji = categoryEmojis[income.category] ?: "💵",
                    onEdit = { viewModel.showEditDialog(income) },
                    onDelete = { showDeleteDialog = income }
                )
            }

            if (uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = IncomeGreen, modifier = Modifier.size(32.dp))
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    showDeleteDialog?.let { income ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) },
            title = { Text("Delete Income") },
            text = { Text("Delete ${CurrencyFormatter.formatBDT(income.amount)}?") },
            confirmButton = {
                TextButton(onClick = { viewModel.deleteIncome(income); showDeleteDialog = null }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }

    if (uiState.showAddDialog) {
        IncomeDialog(
            income = uiState.editingIncome,
            onDismiss = { viewModel.hideDialog() },
            onSave = { s, a, c, d, r, i, n, acc -> viewModel.saveIncome(s, a, c, d, r, i, n, acc) }
        )
    }
}

@Composable
private fun HeaderCard(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedCategory: IncomeCategory?,
    onCategorySelect: (IncomeCategory?) -> Unit,
    totalCount: Int,
    categoryEmojis: Map<IncomeCategory, String>
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(IncomeGreen.copy(alpha = 0.1f), IncomeGreen.copy(alpha = 0.05f))))
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Income History", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("$totalCount records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier.size(40.dp).background(IncomeGreen.copy(alpha = 0.2f), CircleShape).padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.TrendingUp, null, tint = IncomeGreen)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search income...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = { if (searchQuery.isNotEmpty()) IconButton(onClick = { onSearchChange("") }) { Icon(Icons.Default.Clear, null) } },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IncomeGreen)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedCategory == null,
                    onClick = { onCategorySelect(null) },
                    label = { Text("All") },
                    colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IncomeGreen.copy(alpha = 0.2f), selectedLabelColor = IncomeGreen)
                )
                IncomeCategory.entries.forEach { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { onCategorySelect(category) },
                        label = { Row(verticalAlignment = Alignment.CenterVertically) { Text(categoryEmojis[category] ?: "💵"); Spacer(Modifier.width(4.dp)); Text(category.displayName) } },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = IncomeGreen.copy(alpha = 0.2f), selectedLabelColor = IncomeGreen)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Spacer(Modifier.height(16.dp))
            Text("No income recorded yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.height(8.dp))
            Text("Tap + to add your first income", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun IncomeListCard(income: Income, categoryEmoji: String, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().clickable { onEdit() }.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(48.dp).background(IncomeGreen.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Text(categoryEmoji, style = MaterialTheme.typography.titleLarge)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(income.source, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${income.category.displayName} • ${DateUtils.formatDate(income.date)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("+${CurrencyFormatter.formatBDT(income.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                if (income.isRecurring) Icon(Icons.Default.Repeat, null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(Icons.Default.Edit, null) })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) })
                }
            }
        }
    }
}