package com.rudra.savingbuddy.ui.screens.dashboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFabMenu by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    val view = LocalView.current

    val pullToRefreshState = rememberPullToRefreshState()

    val fabRotation by animateFloatAsState(
        targetValue = if (showFabMenu) 45f else 0f,
        animationSpec = tween(300),
        label = "fab_rotation"
    )

    val fabScale by animateFloatAsState(
        targetValue = if (showFabMenu) 0.9f else 1f,
        animationSpec = tween(200),
        label = "fab_scale"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                viewModel.refreshData()
                isRefreshing = false
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Header
                item {
                    DashboardHeader()
                }

                // Net Balance Card - Hero Section
                item {
                    NetBalanceCard(
                        netBalance = uiState.mainBalance,
                        monthlyIncome = uiState.monthlyIncome,
                        selectedAccountName = uiState.selectedAccountName,
                        availableAccounts = uiState.availableAccounts,
                        onAccountSelect = { viewModel.selectAccount(it) }
                    )
                }

                // Account Health Card
                if (uiState.accountHealthList.isNotEmpty()) {
                    item {
                        AccountHealthCard(
                            accountHealthList = uiState.accountHealthList,
                            onAccountClick = { accountId ->
                                navController?.navigate("account_detail/$accountId")
                            }
                        )
                    }
                }

                // Net Worth Card (if different from net balance)
                if (uiState.netWorth > 0 && uiState.netWorth != (uiState.monthlyIncome - uiState.monthlyExpenses)) {
                    item {
                        NetWorthCard(
                            netWorth = uiState.netWorth,
                            totalAssets = uiState.totalAssets,
                            onClick = { navController?.navigate("fusion") }
                        )
                    }
                }

                // Quick Actions
                item {
                    QuickActionsSection(navController = navController)
                }

                // Today's Summary - Compact Card
                item {
                    TodaySummaryCard(state = uiState)
                }

                // Monthly Summary - Detailed Card
                item {
                    MonthlySummaryCard(state = uiState)
                }

                // Active Savings Goal
                if (uiState.activeGoal != null) {
                    item {
                        SavingsGoalCard(
                            goal = uiState.activeGoal!!,
                            onClick = { navController?.navigate("goals") }
                        )
                    }
                }

                // Monthly Trend Sparkline
                if (uiState.monthlyTrend.isNotEmpty()) {
                    item {
                        MonthlyTrendCard(
                            trend = uiState.monthlyTrend,
                            onClick = { navController?.navigate("reports") }
                        )
                    }
                }

                // Upcoming Bills Warning
                if (uiState.upcomingBills.isNotEmpty()) {
                    item {
                        UpcomingBillsCard(
                            bills = uiState.upcomingBills,
                            onBillClick = { navController?.navigate("bills") }
                        )
                    }
                }

                // Category Breakdown
                if (uiState.expensesByCategory.isNotEmpty()) {
                    item {
                        CategoryBreakdownCard(
                            categories = uiState.expensesByCategory,
                            onCategoryClick = { category ->
                                navController?.navigate("category/$category")
                            }
                        )
                    }
                }

                // Insights Card
                if (uiState.insights.isNotEmpty()) {
                    item {
                        InsightsCard(insights = uiState.insights)
                    }
                }

                // Recent Transactions
                if (uiState.recentTransactions.isNotEmpty()) {
                    item {
                        RecentTransactionsCard(
                            transactions = uiState.recentTransactions,
                            onTransactionClick = { transaction ->
                                if (transaction.type == "INCOME") {
                                    navController?.navigate("income_detail/${transaction.id}")
                                } else {
                                    navController?.navigate("expense_detail/${transaction.id}")
                                }
                            },
                            onSeeAll = { navController?.navigate("transactions") }
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(100.dp)) }
            }
        }

        // Floating Action Button
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedFabMenu(
                expanded = showFabMenu,
                fabRotation = fabRotation,
                fabScale = fabScale,
                onFabClick = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    showFabMenu = !showFabMenu
                },
                onAddIncome = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    navController?.navigate("add_income")
                    showFabMenu = false
                },
                onAddExpense = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    navController?.navigate("add_expense")
                    showFabMenu = false
                },
                onAddGoal = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    navController?.navigate("add_goal")
                    showFabMenu = false
                }
            )
        }
    }
}

@Composable
private fun DashboardHeader() {
    Column {
        Text(
            text = "Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = DateUtils.formatDate(System.currentTimeMillis()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetBalanceCard(
    netBalance: Double, 
    monthlyIncome: Double,
    selectedAccountName: String,
    availableAccounts: List<AccountSelection>,
    onAccountSelect: (Long) -> Unit
) {
    val isPositive = netBalance >= 0
    var showAccountPicker by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (isPositive) {
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.tertiary.copy(alpha = 0.9f)
                            )
                        } else {
                            listOf(
                                MaterialTheme.colorScheme.error,
                                MaterialTheme.colorScheme.error.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                            )
                        }
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAccountPicker = true }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isPositive) Icons.Default.AccountBalanceWallet else Icons.Default.Warning,
                            contentDescription = null,
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = selectedAccountName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Change account",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = CurrencyFormatter.format(netBalance.toDouble()),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isPositive) {
                            val rate = if (monthlyIncome > 0) ((netBalance / monthlyIncome) * 100).toInt() else 0
                            "▲ Saving $rate% of income"
                        } else {
                            "▼ Over budget"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    if (showAccountPicker) {
        AlertDialog(
            onDismissRequest = { showAccountPicker = false },
            title = { Text("Select Account", fontWeight = FontWeight.Bold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableAccounts.size) { index ->
                        val account = availableAccounts[index]
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAccountSelect(account.id)
                                    showAccountPicker = false
                                },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (account.name == selectedAccountName) 
                                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = account.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Balance: ${CurrencyFormatter.format(account.balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (account.name == selectedAccountName) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showAccountPicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun QuickActionsSection(navController: NavController?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionItem(
                icon = Icons.Outlined.QrCodeScanner,
                label = "Scan",
                color = MaterialTheme.colorScheme.primary,
                onClick = { navController?.navigate("scan") }
            )
            QuickActionItem(
                icon = Icons.Outlined.PieChart,
                label = "Budget",
                color = MaterialTheme.colorScheme.tertiary,
                onClick = { navController?.navigate("budget") }
            )
            QuickActionItem(
                icon = Icons.Outlined.Flag,
                label = "Goals",
                color = SavingsBlue,
                onClick = { navController?.navigate("goals") }
            )
            QuickActionItem(
                icon = Icons.Outlined.CalendarMonth,
                label = "Calendar",
                color = MaterialTheme.colorScheme.secondary,
                onClick = { navController?.navigate("calendar") }
            )
            QuickActionItem(
                icon = Icons.Outlined.JoinFull,
                label = "Fusion",
                color = Color(0xFF7C4DFF),
                onClick = { navController?.navigate("fusion") }
            )
            QuickActionItem(
                icon = Icons.Outlined.Receipt,
                label = "Bills",
                color = ExpenseRed,
                onClick = { navController?.navigate("bills") }
            )
        }
    }
}

@Composable
private fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TodaySummaryCard(state: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = DateUtils.formatShortDate(System.currentTimeMillis()),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryChip(
                    label = "Income",
                    amount = state.todayIncome,
                    icon = Icons.Default.TrendingUp,
                    color = IncomeGreen
                )
                SummaryChip(
                    label = "Expenses",
                    amount = state.todayExpenses,
                    icon = Icons.Default.TrendingDown,
                    color = ExpenseRed
                )
                SummaryChip(
                    label = "Net",
                    amount = state.todaySavings,
                    icon = if (state.todaySavings >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    color = if (state.todaySavings >= 0) SavingsBlue else ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun SummaryChip(
    label: String,
    amount: Double,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = CurrencyFormatter.formatCompact(amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun MonthlySummaryCard(state: DashboardUiState) {
    val progress by animateFloatAsState(
        targetValue = if (state.budget > 0) (state.monthlyExpenses / state.budget).toFloat().coerceIn(0f, 1f) else 0f,
        animationSpec = tween(1000),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This Month",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                MonthlyStatItem(
                    label = "Income",
                    amount = state.monthlyIncome,
                    color = IncomeGreen
                )
                MonthlyStatItem(
                    label = "Expenses",
                    amount = state.monthlyExpenses,
                    color = ExpenseRed
                )
                MonthlyStatItem(
                    label = "Savings",
                    amount = state.monthlySavings,
                    color = SavingsBlue
                )
            }

            if (state.budget > 0) {
                Spacer(modifier = Modifier.height(20.dp))
                Divider(color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Budget Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${CurrencyFormatter.format(state.monthlyExpenses)} / ${CurrencyFormatter.format(state.budget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = if (state.budgetWarning) ExpenseRed else MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )

                if (state.budgetWarning) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "You've used ${(progress * 100).toInt()}% of your budget",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExpenseRed
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatItem(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = CurrencyFormatter.formatCompact(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun InsightsCard(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            insights.forEach { insight ->
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = "•",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionsCard(
    transactions: List<TransactionItem>,
    onTransactionClick: (TransactionItem) -> Unit,
    onSeeAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                TextButton(onClick = onSeeAll) {
                    Text("See All")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            transactions.take(5).forEachIndexed { index, transaction ->
                TransactionItemRow(
                    transaction = transaction,
                    onClick = { onTransactionClick(transaction) }
                )
                if (index < minOf(transactions.size - 1, 4)) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionItemRow(
    transaction: TransactionItem,
    onClick: () -> Unit
) {
    val isIncome = transaction.type == "INCOME"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (isIncome) IncomeGreen.copy(alpha = 0.15f)
                    else ExpenseRed.copy(alpha = 0.15f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = if (isIncome) IncomeGreen else ExpenseRed,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${transaction.category} • ${DateUtils.formatShortDate(transaction.date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${if (isIncome) "+" else "-"}${CurrencyFormatter.format(transaction.amount)}",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = if (isIncome) IncomeGreen else ExpenseRed
        )
    }
}

@Composable
private fun AnimatedFabMenu(
    expanded: Boolean,
    fabRotation: Float,
    fabScale: Float,
    onFabClick: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddGoal: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.scale(fabScale)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                // Add Goal
                FabMenuItem(
                    text = "Add Goal",
                    icon = Icons.Outlined.Savings,
                    color = SavingsBlue,
                    onClick = onAddGoal
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Add Expense
                FabMenuItem(
                    text = "Add Expense",
                    icon = Icons.Default.TrendingDown,
                    color = ExpenseRed,
                    onClick = onAddExpense
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Add Income
                FabMenuItem(
                    text = "Add Income",
                    icon = Icons.Default.TrendingUp,
                    color = IncomeGreen,
                    onClick = onAddIncome
                )
            }
        }

        FloatingActionButton(
            onClick = onFabClick,
            containerColor = MaterialTheme.colorScheme.primary,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 6.dp,
                pressedElevation = 12.dp
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(
                if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.rotate(fabRotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            )
        }

        FloatingActionButton(
            onClick = onClick,
            containerColor = color,
            modifier = Modifier.size(40.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
