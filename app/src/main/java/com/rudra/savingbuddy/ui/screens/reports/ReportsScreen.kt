package com.rudra.savingbuddy.ui.screens.reports

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.ui.components.BarChart
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTransactionLogs()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Overview") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Logs (${uiState.logCount})") }
            )
        }

        when (selectedTab) {
            0 -> ReportsOverview(viewModel, uiState)
            1 -> TransactionLogsView(viewModel, uiState, showFilterSheet, 
                onFilterClick = { showFilterSheet = true },
                onClearFilter = { viewModel.clearFilters() })
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApply = { startDate, endDate, type ->
                viewModel.loadTransactionLogs(startDate, endDate, type)
                showFilterSheet = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilterSheet = false
            }
        )
    }
}

@Composable
fun ReportsOverview(viewModel: ReportsViewModel, uiState: ReportsUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Reports & Savings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Income",
                            amount = uiState.totalIncome,
                            color = IncomeGreen
                        )
                        StatItem(
                            label = "Expenses",
                            amount = uiState.totalExpenses,
                            color = ExpenseRed
                        )
                        StatItem(
                            label = "Savings",
                            amount = uiState.totalSavings,
                            color = SavingsBlue
                        )
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Savings Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${uiState.savingsRate.toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.savingsRate >= 20) IncomeGreen else ExpenseRed
                        )
                        Text(
                            text = when {
                                uiState.savingsRate >= 50 -> "Excellent! 🎉"
                                uiState.savingsRate >= 20 -> "Good"
                                uiState.savingsRate >= 0 -> "Needs improvement"
                                else -> "Overspending!"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (uiState.savingsRate >= 20) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }

        if (uiState.monthlyData.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monthly Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val chartData = uiState.monthlyData.map { 
                            Pair(it.month.take(3), it.income)
                        }
                        BarChart(
                            data = chartData,
                            modifier = Modifier.fillMaxWidth(),
                            barColor = IncomeGreen
                        )
                    }
                }
            }
        }

        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        uiState.categoryBreakdown.forEach { item ->
                            CategoryBreakdownRow(
                                category = item.category,
                                amount = item.amount,
                                percentage = item.percentage
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyFormatter.format(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CategoryBreakdownRow(
    category: String,
    amount: Double,
    percentage: Double
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = getCategoryColor(category)
        )
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    return when (category) {
        "FOOD" -> FoodColor
        "TRANSPORT" -> TransportColor
        "BILLS" -> BillsColor
        "SHOPPING" -> ShoppingColor
        else -> OthersColor
    }
}

@Composable
fun TransactionLogsView(
    viewModel: ReportsViewModel,
    uiState: ReportsUiState,
    showFilterSheet: Boolean,
    onFilterClick: () -> Unit,
    onClearFilter: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Transaction History",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row {
                Text(
                    text = "${uiState.transactionLogs.size} records",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onFilterClick) {
                    Icon(Icons.Default.FilterList, contentDescription = "Filter")
                }
            }
        }

        if (uiState.filterStartDate != null || uiState.filterEndDate != null || uiState.filterType != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Active Filters",
                            style = MaterialTheme.typography.labelMedium
                        )
                        if (uiState.filterStartDate != null && uiState.filterEndDate != null) {
                            Text(
                                text = "${DateUtils.formatDate(uiState.filterStartDate!!)} - ${DateUtils.formatDate(uiState.filterEndDate!!)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        uiState.filterType?.let {
                            Text(
                                text = "Type: $it",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    TextButton(onClick = onClearFilter) {
                        Text("Clear")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.transactionLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No transactions found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.transactionLogs) { log ->
                    TransactionLogItem(log)
                }
            }
        }
    }
}

@Composable
fun TransactionLogItem(log: TransactionLog) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        if (log.type == "Income") IncomeGreen.copy(alpha = 0.2f)
                        else ExpenseRed.copy(alpha = 0.2f),
                        androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (log.type == "Income") Icons.Default.ArrowDownward
                    else Icons.Default.ArrowUpward,
                    contentDescription = null,
                    tint = if (log.type == "Income") IncomeGreen else ExpenseRed,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${log.category} • ${DateUtils.formatDate(log.date)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                log.notes?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Text(
                text = if (log.type == "Income") "+${CurrencyFormatter.format(log.amount)}"
                else "-${CurrencyFormatter.format(log.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (log.type == "Income") IncomeGreen else ExpenseRed
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    onDismiss: () -> Unit,
    onApply: (Long?, Long?, String?) -> Unit,
    onClear: () -> Unit
) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Filter Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Transaction Type", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = selectedType == null,
                    onClick = { selectedType = null },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = selectedType == "Income",
                    onClick = { selectedType = "Income" },
                    label = { Text("Income") }
                )
                FilterChip(
                    selected = selectedType == "Expense",
                    onClick = { selectedType = "Expense" },
                    label = { Text("Expense") }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text("Date Range Presets", style = MaterialTheme.typography.labelMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val now = System.currentTimeMillis()
                val todayStart = now - (24 * 60 * 60 * 1000)
                val weekStart = now - (7 * 24 * 60 * 60 * 1000)
                val monthStart = now - (30L * 24 * 60 * 60 * 1000)
                
                FilterChip(
                    selected = startDate == todayStart,
                    onClick = { 
                        startDate = todayStart
                        endDate = now
                    },
                    label = { Text("Today") }
                )
                FilterChip(
                    selected = startDate == weekStart,
                    onClick = { 
                        startDate = weekStart
                        endDate = now
                    },
                    label = { Text("Week") }
                )
                FilterChip(
                    selected = startDate == monthStart,
                    onClick = { 
                        startDate = monthStart
                        endDate = now
                    },
                    label = { Text("Month") }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear")
                }
                Button(
                    onClick = { onApply(startDate, endDate, selectedType) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}