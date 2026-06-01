package com.rudra.savingbuddy.ui.screens.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController?,
    viewModel: CalendarViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }
    var showBillSheet by remember { mutableStateOf(false) }
    var selectedBillDate by remember { mutableStateOf<LocalDate?>(null) }

    LaunchedEffect(currentMonth) {
        viewModel.setMonth(currentMonth)
    }

    val daysInMonth = remember(currentMonth) {
        val firstDay = currentMonth.atDay(1)
        val startOffset = (firstDay.dayOfWeek.value % 7)
        val totalDays = currentMonth.lengthOfMonth()
        (1..totalDays).map { day -> day to (startOffset + day - 1) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Calendar", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Text("View transactions by date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { currentMonth = YearMonth.now() }) {
                        Icon(Icons.Default.Today, contentDescription = "Go to Today", tint = IncomeGreen)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        val transactions = selectedDate?.let { viewModel.getTransactionsForDate(it) } ?: emptyList()

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Upcoming Bills Card
            if (uiState.showBills && uiState.upcomingBills.isNotEmpty()) {
                item {
                    UpcomingBillsCard(
                        upcomingBills = uiState.upcomingBills,
                        totalBillsDue = uiState.totalBillsDue,
                        onViewAllBills = { navController?.navigate("bills") },
                        viewModel = viewModel
                    )
                }
            }

            // Month Summary Card
            item {
                MonthlySummaryCard(
                    month = currentMonth,
                    income = uiState.monthlyIncome,
                    expense = uiState.monthlyExpense,
                    net = uiState.monthlyNet,
                    billCount = if (uiState.showBills) uiState.billsByDate.values.flatten().size else 0,
                    totalBillsDue = if (uiState.showBills) uiState.totalBillsDue else 0.0
                )
            }

            // Filter Chips
            item {
                FilterChipsRow(
                    selectedFilter = uiState.filterType,
                    showBills = uiState.showBills,
                    onFilterSelected = { viewModel.setFilter(it) },
                    onToggleBills = { viewModel.toggleShowBills() }
                )
            }

            // Month Navigation
            item {
                MonthNavigator(
                    currentMonth = currentMonth,
                    onPreviousMonth = { currentMonth = currentMonth.minusMonths(1) },
                    onNextMonth = { currentMonth = currentMonth.plusMonths(1) }
                )
            }

            // Day of Week Headers
            item {
                DayOfWeekHeader()
            }

            // Calendar Grid
            item {
                CalendarGrid(
                    currentMonth = currentMonth,
                    daysInMonth = daysInMonth,
                    selectedDate = selectedDate,
                    viewModel = viewModel,
                    onDateSelected = { date ->
                        selectedDate = date
                        val bills = viewModel.getBillsOnDate(date)
                        if (bills.isNotEmpty()) {
                            selectedBillDate = date
                            showBillSheet = true
                        }
                    }
                )
            }

            // Selected Date Header
            item {
                SelectedDateHeader(
                    selectedDate = selectedDate,
                    transactionCount = transactions.size
                )
            }

            if (selectedDate != null) {
                if (transactions.isEmpty()) {
                    item {
                        EmptyDateState()
                    }
                } else {
                    items(transactions) { transaction ->
                        TransactionItem(
                            transaction = transaction,
                            onClick = {}
                        )
                    }
                }
            } else {
                item {
                    SelectDatePrompt()
                }
            }
        }

        // Bill Bottom Sheet
        if (showBillSheet && selectedBillDate != null) {
            BillBottomSheet(
                date = selectedBillDate!!,
                bills = viewModel.getBillsOnDate(selectedBillDate!!),
                viewModel = viewModel,
                onDismiss = { showBillSheet = false },
                onNavigateToBills = { navController?.navigate("bills") }
            )
        }
    }
}

@Composable
private fun MonthlySummaryCard(
    month: YearMonth,
    income: Double,
    expense: Double,
    net: Double,
    billCount: Int = 0,
    totalBillsDue: Double = 0.0
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(IncomeGreen.copy(alpha = 0.06f), Color.Transparent)))
                .padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.DateRange, null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                Text("${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                    style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Income",
                        style = MaterialTheme.typography.labelSmall,
                        color = IncomeGreen
                    )
                    Text(
                        text = CurrencyFormatter.format(income),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = IncomeGreen
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Expense",
                        style = MaterialTheme.typography.labelSmall,
                        color = ExpenseRed
                    )
                    Text(
                        text = CurrencyFormatter.format(expense),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Net",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (net >= 0) SavingsBlue else ExpenseRed
                    )
                    Text(
                        text = CurrencyFormatter.format(net),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (net >= 0) IncomeGreen else ExpenseRed
                    )
                }
            }

            if (billCount > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.Receipt, null, tint = WarningOrange, modifier = Modifier.size(16.dp))
                    Text(
                        text = "\uD83D\uDCB0 $billCount bills due this month totaling ${CurrencyFormatter.format(totalBillsDue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun FilterChipsRow(
    selectedFilter: FilterType,
    showBills: Boolean,
    onFilterSelected: (FilterType) -> Unit,
    onToggleBills: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        FilterChip(
            selected = selectedFilter == FilterType.ALL,
            onClick = { onFilterSelected(FilterType.ALL) },
            label = { Text("All") },
            leadingIcon = if (selectedFilter == FilterType.ALL) {
                { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(16.dp)) }
            } else null,
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedFilter == FilterType.INCOME,
            onClick = { onFilterSelected(FilterType.INCOME) },
            label = { Text("Income") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = selectedFilter == FilterType.EXPENSE,
            onClick = { onFilterSelected(FilterType.EXPENSE) },
            label = { Text("Expense") },
            modifier = Modifier.weight(1f)
        )
        FilterChip(
            selected = showBills,
            onClick = onToggleBills,
            label = { Text(if (showBills) "Hide Bills" else "Show Bills") },
            leadingIcon = {
                Icon(
                    if (showBills) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    Modifier.size(16.dp)
                )
            }
        )
    }
}

@Composable
private fun MonthNavigator(
    currentMonth: YearMonth,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onPreviousMonth) {
            Icon(
                Icons.Default.ChevronLeft,
                contentDescription = "Previous Month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        IconButton(onClick = onNextMonth) {
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Next Month",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun DayOfWeekHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
            Text(
                text = day,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: YearMonth,
    daysInMonth: List<Pair<Int, Int>>,
    selectedDate: LocalDate?,
    viewModel: CalendarViewModel,
    onDateSelected: (LocalDate) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
            .padding(horizontal = 16.dp),
        userScrollEnabled = false
    ) {
        items(42) { index ->
            val dayInfo = daysInMonth.find { it.second == index }
            if (dayInfo != null && dayInfo.first <= currentMonth.lengthOfMonth()) {
                val date = currentMonth.atDay(dayInfo.first)
                val daySummary = viewModel.getDaySummary(date)
                val isSelected = selectedDate == date
                val isToday = date == LocalDate.now()

                CalendarDayCell(
                    day = dayInfo.first,
                    date = date,
                    daySummary = daySummary,
                    isSelected = isSelected,
                    isToday = isToday,
                    onClick = { onDateSelected(date) }
                )
            } else {
                Box(modifier = Modifier.aspectRatio(1f))
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    date: LocalDate,
    daySummary: DayTransactionSummary,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val hasTransactions = daySummary.transactionCount > 0
    val hasIncome = daySummary.hasIncome
    val hasExpense = daySummary.hasExpense
    val hasBills = daySummary.billCount > 0

    val backgroundColor by animateColorAsState(
        targetValue = when {
            isSelected -> MaterialTheme.colorScheme.primary
            isToday -> MaterialTheme.colorScheme.primaryContainer
            else -> Color.Transparent
        },
        label = "day_bg"
    )

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .then(
                if (hasTransactions && !isSelected && !isToday) {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                } else Modifier
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = day.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )

            BillCalendarDot(
                hasIncome = hasIncome,
                hasExpense = hasExpense,
                hasBills = hasBills,
                billCount = daySummary.billCount,
                isSelected = isSelected
            )
        }
    }
}

@Composable
private fun SelectedDateHeader(
    selectedDate: LocalDate?,
    transactionCount: Int
) {
    if (selectedDate != null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = DateUtils.formatDate(
                        selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (transactionCount > 0) {
                    Text(
                        text = "$transactionCount txns",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyDateState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No transactions on this date",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectDatePrompt() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.TouchApp,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Select a date to view transactions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TransactionItem(
    transaction: Any,
    onClick: () -> Unit
) {
    val isExpense = transaction is Expense
    val isIncome = transaction is Income

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isExpense -> ExpenseRed.copy(alpha = 0.1f)
                isIncome -> IncomeGreen.copy(alpha = 0.1f)
                else -> MaterialTheme.colorScheme.surfaceVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            when {
                                isExpense -> ExpenseRed.copy(alpha = 0.2f)
                                isIncome -> IncomeGreen.copy(alpha = 0.2f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when {
                            isExpense -> Icons.Default.TrendingDown
                            isIncome -> Icons.Default.TrendingUp
                            else -> Icons.Default.SwapHoriz
                        },
                        contentDescription = null,
                        tint = when {
                            isExpense -> ExpenseRed
                            isIncome -> IncomeGreen
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column {
                    when (transaction) {
                        is Expense -> {
                            Text(
                                text = transaction.category.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = DateUtils.formatShortDate(transaction.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        is Income -> {
                            Text(
                                text = transaction.source,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = DateUtils.formatShortDate(transaction.date),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Text(
                text = when (transaction) {
                    is Expense -> "-${CurrencyFormatter.format(transaction.amount)}"
                    is Income -> "+${CurrencyFormatter.format(transaction.amount)}"
                    else -> ""
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = when {
                    isExpense -> ExpenseRed
                    isIncome -> IncomeGreen
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun BillCalendarDot(
    hasIncome: Boolean,
    hasExpense: Boolean,
    hasBills: Boolean,
    billCount: Int,
    isSelected: Boolean
) {
    val dotColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    if (hasBills) {
        if (hasIncome || hasExpense) {
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                if (hasIncome) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else IncomeGreen)
                    )
                }
                if (hasExpense) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else ExpenseRed)
                    )
                }
                if (billCount <= 3) {
                    repeat(billCount) {
                        Box(
                            modifier = Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(WarningOrange)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(WarningOrange.copy(alpha = 0.8f))
                            .padding(horizontal = 4.dp, vertical = 1.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$billCount",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 8.sp,
                            color = Color.White
                        )
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(WarningOrange.copy(alpha = 0.8f))
                    .padding(horizontal = 4.dp, vertical = 1.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$billCount",
                    style = MaterialTheme.typography.labelSmall,
                    fontSize = 8.sp,
                    color = Color.White
                )
            }
        }
    } else if (hasIncome || hasExpense) {
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            if (hasIncome) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else IncomeGreen)
                )
            }
            if (hasExpense) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else ExpenseRed)
                )
            }
        }
    }
}

@Composable
private fun UpcomingBillsCard(
    upcomingBills: List<BillWithDate>,
    totalBillsDue: Double,
    onViewAllBills: () -> Unit,
    viewModel: CalendarViewModel
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(WarningOrange.copy(alpha = 0.06f), Color.Transparent)))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Receipt, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                    Text("Upcoming Bills", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = WarningOrange.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = CurrencyFormatter.format(totalBillsDue),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = WarningOrange
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            upcomingBills.forEachIndexed { index, billWithDate ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
                BillUpcomingItem(
                    bill = billWithDate.bill,
                    dueDate = billWithDate.dueDate,
                    viewModel = viewModel
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onViewAllBills,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("View All Bills")
            }
        }
    }
}

@Composable
private fun BillUpcomingItem(
    bill: BillReminder,
    dueDate: Long,
    viewModel: CalendarViewModel
) {
    val now = System.currentTimeMillis()
    val daysUntil = ((dueDate - now) / 86400000L).toInt()
    val status = viewModel.getBillStatus(bill, dueDate)

    val pillColor = when {
        daysUntil < 0 -> ExpenseRed
        daysUntil <= 3 -> ExpenseRed.copy(alpha = 0.7f)
        daysUntil <= 7 -> WarningOrange
        else -> PrimaryGreen
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(text = "\uD83D\uDCC5", fontSize = 20.sp)
        Column(modifier = Modifier.weight(1f)) {
            Text(bill.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text(DateUtils.formatDate(dueDate), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(
            text = CurrencyFormatter.format(bill.amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.width(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = pillColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = when {
                    daysUntil < 0 -> "${-daysUntil}d overdue"
                    daysUntil == 0 -> "Due today"
                    else -> "${daysUntil}d left"
                },
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = pillColor
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BillBottomSheet(
    date: LocalDate,
    bills: List<BillReminder>,
    viewModel: CalendarViewModel,
    onDismiss: () -> Unit,
    onNavigateToBills: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Bills Due", fontWeight = FontWeight.Bold)
                    Text(
                        DateUtils.formatDate(date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            val dateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                bills.forEach { bill ->
                    val status = viewModel.getBillStatus(bill, dateMillis)
                    val statusText = when (status) {
                        BillStatus.OVERDUE -> "Overdue"
                        BillStatus.DUE_TODAY -> "Due Today"
                        BillStatus.UPCOMING -> "Upcoming"
                    }
                    val statusColor = when (status) {
                        BillStatus.OVERDUE -> ExpenseRed
                        BillStatus.DUE_TODAY -> WarningOrange
                        BillStatus.UPCOMING -> PrimaryGreen
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(text = "\uD83D\uDCC5", fontSize = 24.sp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text(bill.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                Text(
                                    "${bill.category} - ${bill.billingCycle.displayName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    CurrencyFormatter.format(bill.amount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = statusText,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Medium,
                                        color = statusColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onNavigateToBills) {
                Icon(Icons.Default.OpenInNew, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Manage Bills")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}