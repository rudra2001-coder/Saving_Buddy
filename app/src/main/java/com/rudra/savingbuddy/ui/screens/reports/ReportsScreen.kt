package com.rudra.savingbuddy.ui.screens.reports

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
    var showDatePicker by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadTransactionLogs()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("Overview") }, icon = { Icon(Icons.Outlined.PieChart, null, modifier = Modifier.size(18.dp)) })
            Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Logs (${uiState.logCount})") }, icon = { Icon(Icons.Outlined.Receipt, null, modifier = Modifier.size(18.dp)) })
        }

        when (selectedTab) {
            0 -> ReportsOverview(viewModel = viewModel, uiState = uiState, onFilterClick = { showFilterSheet = true })
            1 -> TransactionLogsView(viewModel = viewModel, uiState = uiState, showFilterSheet = showFilterSheet, onFilterClick = { showFilterSheet = true }, onClearFilter = { viewModel.clearFilters() })
        }
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            onDismiss = { showFilterSheet = false },
            onApply = { startDate, endDate, type ->
                viewModel.loadTransactionLogs(startDate, endDate, type)
                viewModel.applyDateRangeToOverview(startDate, endDate)
                showFilterSheet = false
            },
            onClear = {
                viewModel.clearFilters()
                showFilterSheet = false
            }
        )
    }

    if (showDatePicker) {
        DateRangePickerDialog(
            onDismiss = { showDatePicker = false },
            onApply = { startDate, endDate ->
                viewModel.applyDateRangeToOverview(startDate, endDate)
                showDatePicker = false
            }
        )
    }
}

@Composable
fun ReportsOverview(viewModel: ReportsViewModel, uiState: ReportsUiState, onFilterClick: () -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item { Spacer(modifier = Modifier.height(8.dp)) }

        item {
            Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DatePresetChip(label = "Today", isSelected = uiState.isToday, onClick = { viewModel.selectDatePreset("today") })
                DatePresetChip(label = "This Week", isSelected = uiState.isThisWeek, onClick = { viewModel.selectDatePreset("week") })
                DatePresetChip(label = "This Month", isSelected = uiState.isThisMonth, onClick = { viewModel.selectDatePreset("month") })
                DatePresetChip(label = "This Year", isSelected = uiState.isThisYear, onClick = { viewModel.selectDatePreset("year") })
                AssistChip(onClick = onFilterClick, label = { Text("Custom") }, leadingIcon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp)) })
            }
        }

        if (uiState.overviewStartDate != null && uiState.overviewEndDate != null) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("${DateUtils.formatShortDate(uiState.overviewStartDate!!)} - ${DateUtils.formatShortDate(uiState.overviewEndDate!!)}", style = MaterialTheme.typography.bodySmall)
                        }
                        IconButton(onClick = { viewModel.clearOverviewDateRange() }, modifier = Modifier.size(24.dp)) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)))).padding(24.dp)) {
                    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Total Savings", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(CurrencyFormatter.format(uiState.totalSavings), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatItemColumn(label = "Income", amount = uiState.totalIncome, color = IncomeGreen)
                            StatItemColumn(label = "Expenses", amount = uiState.totalExpenses, color = ExpenseRed)
                            StatItemColumn(label = "Savings", amount = uiState.totalSavings, color = SavingsBlue)
                        }
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.TrendingUp, contentDescription = null, tint = SavingsBlue, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Savings Rate", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Surface(color = if (uiState.savingsRate >= 20) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                            Text("${uiState.savingsRate.toInt()}%", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (uiState.savingsRate >= 20) IncomeGreen else ExpenseRed, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    val animatedRate by animateFloatAsState(targetValue = (uiState.savingsRate.toFloat() / 100f).coerceIn(0f, 1f), animationSpec = tween(1000), label = "rate")
                    LinearProgressIndicator(progress = { animatedRate }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp)), color = if (uiState.savingsRate >= 20) IncomeGreen else ExpenseRed, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(when { uiState.savingsRate >= 50 -> "Excellent! Keep it up!"; uiState.savingsRate >= 20 -> "Good progress, keep saving!"; uiState.savingsRate >= 0 -> "Room for improvement"; else -> "Overspending detected" }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        if (uiState.monthlyData.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.BarChart, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Monthly Trend", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val chartData = uiState.monthlyData.map { Pair(it.month.take(3), it.income) }
                        BarChart(data = chartData, modifier = Modifier.fillMaxWidth(), barColor = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }

        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.Category, contentDescription = null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Expense Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        uiState.categoryBreakdown.forEach { item ->
                            CategoryBreakdownRow(category = item.category, amount = item.amount, percentage = item.percentage)
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun DatePresetChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    FilterChip(selected = isSelected, onClick = onClick, label = { Text(label) })
}

@Composable
private fun StatItemColumn(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.8f))
        Text(CurrencyFormatter.formatCompact(amount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color.copy(alpha = 0.9f))
    }
}

@Composable
private fun CategoryBreakdownRow(category: String, amount: Double, percentage: Double) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(category, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(CurrencyFormatter.format(amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(progress = { (percentage / 100).toFloat() }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = getCategoryColor(category))
        Text("${percentage.toInt()}% of total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun getCategoryColor(category: String): Color = when (category) { "FOOD" -> FoodColor; "TRANSPORT" -> TransportColor; "BILLS" -> BillsColor; "SHOPPING" -> ShoppingColor; else -> OthersColor }

@Composable
fun TransactionLogsView(viewModel: ReportsViewModel, uiState: ReportsUiState, showFilterSheet: Boolean, onFilterClick: () -> Unit, onClearFilter: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Transaction History", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row {
                Text("${uiState.transactionLogs.size} records", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = onFilterClick) { Icon(Icons.Default.FilterList, contentDescription = "Filter") }
            }
        }

        if (uiState.filterStartDate != null || uiState.filterEndDate != null || uiState.filterType != null) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer), shape = RoundedCornerShape(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Active Filters", style = MaterialTheme.typography.labelMedium)
                        if (uiState.filterStartDate != null && uiState.filterEndDate != null) Text("${DateUtils.formatDate(uiState.filterStartDate!!)} - ${DateUtils.formatDate(uiState.filterEndDate!!)}", style = MaterialTheme.typography.bodySmall)
                        uiState.filterType?.let { Text("Type: $it", style = MaterialTheme.typography.bodySmall) }
                    }
                    TextButton(onClick = onClearFilter) { Text("Clear") }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.transactionLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.SearchOff, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No transactions found", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.transactionLogs) { log -> TransactionLogItem(log) }
            }
        }
    }
}

@Composable
fun TransactionLogItem(log: TransactionLog) {
    val isIncome = log.type == "Income"
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(44.dp).background(if (isIncome) IncomeGreen.copy(alpha = 0.15f) else ExpenseRed.copy(alpha = 0.15f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(if (isIncome) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward, contentDescription = null, tint = if (isIncome) IncomeGreen else ExpenseRed, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(log.description, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text("${log.category} • ${DateUtils.formatShortDate(log.date)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                log.notes?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
            Text(if (isIncome) "+${CurrencyFormatter.format(log.amount)}" else "-${CurrencyFormatter.format(log.amount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = if (isIncome) IncomeGreen else ExpenseRed)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(onDismiss: () -> Unit, onApply: (Long?, Long?, String?) -> Unit, onClear: () -> Unit) {
    var selectedType by remember { mutableStateOf<String?>(null) }
    var startDate by remember { mutableStateOf<Long?>(null) }
    var endDate by remember { mutableStateOf<Long?>(null) }
    val now = System.currentTimeMillis()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text("Filter Transactions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(20.dp))
            
            Text("Transaction Type", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = selectedType == null, onClick = { selectedType = null }, label = { Text("All") })
                FilterChip(selected = selectedType == "Income", onClick = { selectedType = "Income" }, label = { Text("Income") }, leadingIcon = if (selectedType == "Income") {{ Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }} else null)
                FilterChip(selected = selectedType == "Expense", onClick = { selectedType = "Expense" }, label = { Text("Expense") }, leadingIcon = if (selectedType == "Expense") {{ Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }} else null)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Text("Quick Date Range", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                val todayStart = now - (24 * 60 * 60 * 1000)
                val weekStart = now - (7 * 24 * 60 * 60 * 1000)
                val monthStart = now - (30L * 24 * 60 * 60 * 1000)
                val yearStart = now - (365L * 24 * 60 * 60 * 1000)
                
                FilterChip(selected = startDate == todayStart, onClick = { startDate = todayStart; endDate = now }, label = { Text("Today") })
                FilterChip(selected = startDate == weekStart, onClick = { startDate = weekStart; endDate = now }, label = { Text("Week") })
                FilterChip(selected = startDate == monthStart, onClick = { startDate = monthStart; endDate = now }, label = { Text("Month") })
                FilterChip(selected = startDate == yearStart, onClick = { startDate = yearStart; endDate = now }, label = { Text("Year") })
            }

            if (startDate != null && endDate != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)), shape = RoundedCornerShape(12.dp)) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("${DateUtils.formatShortDate(startDate!!)} - ${DateUtils.formatShortDate(endDate!!)}", style = MaterialTheme.typography.bodyMedium)
                        IconButton(onClick = { startDate = null; endDate = null }, modifier = Modifier.size(24.dp)) { Icon(Icons.Default.Close, null, modifier = Modifier.size(16.dp)) }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onClear, modifier = Modifier.weight(1f)) { Text("Clear") }
                Button(onClick = { onApply(startDate, endDate, selectedType) }, modifier = Modifier.weight(1f)) { Text("Apply") }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerDialog(onDismiss: () -> Unit, onApply: (Long, Long) -> Unit) {
    var startDateMillis by remember { mutableStateOf(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000) }
    var endDateMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Select Date Range", fontWeight = FontWeight.Bold) }, text = {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant), shape = RoundedCornerShape(12.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Start: ${DateUtils.formatDate(startDateMillis)}", style = MaterialTheme.typography.bodyMedium)
                    Slider(value = 0f, onValueChange = {}, enabled = false)
                    Text("End: ${DateUtils.formatDate(endDateMillis)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
            Text("Use the filter sheet for custom date selection", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }, confirmButton = { Button(onClick = { onApply(startDateMillis, endDateMillis) }) { Text("Apply") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}