package com.rudra.savingbuddy.ui.screens.dashboard

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

// ─── Color tokens (mirrors DashboardCards.kt) ────────────────────────────────

private val Blue600  = Color(0xFF185FA5)
private val Blue50   = Color(0xFFE6F1FB)
private val Green600 = Color(0xFF3B6D11)
private val Green50  = Color(0xFFEAF3DE)
private val Red600   = Color(0xFFA32D2D)
private val Red50    = Color(0xFFFCEBEB)
private val Amber600 = Color(0xFF854F0B)
private val Amber50  = Color(0xFFFAEEDA)

// ─── Root screen ──────────────────────────────────────────────────────────────

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
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val horizontalPadding = when {
        screenWidth < 360 -> 10.dp
        screenWidth < 400 -> 14.dp
        else              -> 16.dp
    }
    val cardSpacing = if (screenWidth < 360) 10.dp else 14.dp

    val pullToRefreshState = rememberPullToRefreshState()
    val snackbarHostState  = remember { SnackbarHostState() }

    val fabRotation by animateFloatAsState(
        targetValue = if (showFabMenu) 45f else 0f,
        animationSpec = tween(300), label = "fab_rotation"
    )
    val fabScale by animateFloatAsState(
        targetValue = if (showFabMenu) 0.9f else 1f,
        animationSpec = tween(200), label = "fab_scale"
    )

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isRefreshing) isRefreshing = false
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let { err ->
            snackbarHostState.showSnackbar(err)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                if (!isRefreshing) {
                    isRefreshing = true
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.refreshData()
                }
            },
            state = pullToRefreshState,
            modifier = Modifier.fillMaxSize()
        ) {
            if (uiState.isLoading && uiState.mainBalance == 0.0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Blue600)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = horizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(cardSpacing)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }

                    // Header
                    item { DashboardHeader() }

                    // Hero balance card
                    item {
                        NetBalanceCard(
                            netBalance = uiState.mainBalance,
                            monthlyIncome = uiState.monthlyIncome,
                            monthlyExpenses = uiState.monthlyExpenses,
                            selectedAccountName = uiState.selectedAccountName,
                            availableAccounts = uiState.availableAccounts,
                            onAccountSelect = { viewModel.selectAccount(it) }
                        )
                    }

                    // Quick actions
                    item { QuickActionsRow(navController = navController) }

                    // Today + Budget (2-column)
                    item {
                        TodayAndBudgetRow(state = uiState)
                    }

                    // Account Health
                    if (uiState.accountHealthList.isNotEmpty()) {
                        item {
                            AccountHealthCard(
                                accountHealthList = uiState.accountHealthList,
                                onAccountClick = { navController?.navigate("account_detail/$it") }
                            )
                        }
                    }

                    // Net Worth
                    if (uiState.netWorth > 0 && uiState.netWorth != uiState.monthlyIncome - uiState.monthlyExpenses) {
                        item {
                            NetWorthCard(
                                netWorth = uiState.netWorth,
                                totalAssets = uiState.totalAssets,
                                onClick = { navController?.navigate("fusion") }
                            )
                        }
                    }

                    // Monthly summary
                    item { MonthlySummaryCard(state = uiState) }

                    // Savings goal
                    uiState.activeGoal?.let { goal ->
                        item {
                            SavingsGoalCard(
                                goal = goal,
                                onClick = { navController?.navigate("goals") }
                            )
                        }
                    }

                    // Category breakdown
                    if (uiState.expensesByCategory.isNotEmpty()) {
                        item {
                            CategoryBreakdownCard(
                                categories = uiState.expensesByCategory,
                                onCategoryClick = { navController?.navigate("transaction_history") }
                            )
                        }
                    }

                    // Monthly trend
                    if (uiState.monthlyTrend.isNotEmpty()) {
                        item {
                            MonthlyTrendCard(
                                trend = uiState.monthlyTrend,
                                onClick = { navController?.navigate("reports") }
                            )
                        }
                    }

                    // Upcoming bills
                    if (uiState.upcomingBills.isNotEmpty()) {
                        item {
                            UpcomingBillsCard(
                                bills = uiState.upcomingBills,
                                onBillClick = { navController?.navigate("bills") }
                            )
                        }
                    }

                    // Insights
                    if (uiState.insights.isNotEmpty()) {
                        item { InsightsCard(insights = uiState.insights) }
                    }

                    // Recent transactions
                    if (uiState.recentTransactions.isNotEmpty()) {
                        item {
                            RecentTransactionsCard(
                                transactions = uiState.recentTransactions,
                                onTransactionClick = { navController?.navigate("transaction_history") },
                                onSeeAll = { navController?.navigate("transaction_history") }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }

        // FAB
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp),
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
                onAddAccount = {
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    navController?.navigate("add_account")
                    showFabMenu = false
                }
            )
        }
    }
}

// ─── Dashboard header ─────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = DateUtils.formatDate(System.currentTimeMillis()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Blue50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Sync,
                contentDescription = "Sync",
                tint = Blue600,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ─── Hero balance card ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetBalanceCard(
    netBalance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    selectedAccountName: String,
    availableAccounts: List<AccountSelection>,
    onAccountSelect: (Long) -> Unit
) {
    val isPositive = netBalance >= 0
    var showPicker by remember { mutableStateOf(false) }

    val gradientColors = if (isPositive)
        listOf(Color(0xFF185FA5), Color(0xFF0C447C))
    else
        listOf(Color(0xFFA32D2D), Color(0xFF791F1F))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 0.dp)
                ) {
                    // Account picker row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { if (availableAccounts.isNotEmpty()) showPicker = true }
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = selectedAccountName,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                        if (availableAccounts.isNotEmpty()) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Change account",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Balance
                    Text(
                        text = CurrencyFormatter.format(netBalance),
                        style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text = "Current balance",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    Spacer(Modifier.height(16.dp))
                }

                // Bottom strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.1f))
                ) {
                    BalanceStrip(
                        label = "Income",
                        value = CurrencyFormatter.format(monthlyIncome),
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(48.dp)
                            .align(Alignment.CenterVertically)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    BalanceStrip(
                        label = "Expenses",
                        value = CurrencyFormatter.format(monthlyExpenses),
                        modifier = Modifier
                            .weight(1f)
                            .padding(16.dp)
                    )
                }
            }
        }
    }

    // Account picker dialog
    if (showPicker && availableAccounts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Select account", fontWeight = FontWeight.SemiBold) },
            text = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableAccounts.size) { i ->
                        val acc = availableAccounts[i]
                        val isSelected = acc.name == selectedAccountName
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable {
                                onAccountSelect(acc.id)
                                showPicker = false
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Blue50
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = acc.name,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "Balance: ${CurrencyFormatter.format(acc.balance)}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isSelected) {
                                    Icon(Icons.Default.Check, "Selected", tint = Blue600)
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showPicker = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun BalanceStrip(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.6f)
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}

// ─── Quick actions row ────────────────────────────────────────────────────────

private data class QuickAction(val label: String, val icon: ImageVector, val bg: Color, val fg: Color, val route: String)

@Composable
private fun QuickActionsRow(navController: NavController?) {
    val actions = listOf(
        QuickAction("Income",   Icons.Outlined.TrendingUp,     Green50,  Green600, "add_income"),
        QuickAction("Expense",  Icons.Outlined.TrendingDown,   Red50,    Red600,   "add_expense"),
        QuickAction("Goals",    Icons.Outlined.Flag,           Blue50,   Blue600,  "goals"),
        QuickAction("Budget",   Icons.Outlined.PieChart,       Amber50,  Amber600, "budget"),
        QuickAction("Bills",    Icons.Outlined.Receipt,        Red50,    Red600,   "bills"),
        QuickAction("Export",   Icons.Outlined.FileDownload,   Blue50,   Blue600,  "export"),
        QuickAction("Calendar", Icons.Outlined.CalendarMonth,  Amber50,  Amber600, "calendar"),
        QuickAction("Fusion",   Icons.Outlined.JoinFull,       Blue50,   Blue600,  "fusion"),
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp)
    ) {
        items(actions) { action ->
            QuickActionChip(
                label = action.label,
                icon = action.icon,
                bg = action.bg,
                fg = action.fg,
                onClick = { navController?.navigate(action.route) }
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    bg: Color,
    fg: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = fg,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ─── Today + Budget 2-column ──────────────────────────────────────────────────

@Composable
private fun TodayAndBudgetRow(state: DashboardUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today card
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Today",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                MiniStat("Income", CurrencyFormatter.formatCompact(state.todayIncome), Green600)
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(6.dp))
                MiniStat("Expenses", CurrencyFormatter.formatCompact(state.todayExpenses), Red600)
            }
        }

        // Budget card
        val budgetProgress by animateFloatAsState(
            targetValue = if (state.budget > 0)
                (state.monthlyExpenses / state.budget).toFloat().coerceIn(0f, 1f)
            else 0f,
            animationSpec = tween(1000), label = "budget_progress"
        )
        val budgetPct = (budgetProgress * 100).toInt()
        val progressColor = when {
            state.budgetWarning -> Red600
            budgetPct > 60      -> Amber600
            else                -> Blue600
        }

        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "Budget used",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "$budgetPct%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = progressColor
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(99.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(budgetProgress)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(99.dp))
                            .background(progressColor)
                    )
                }
                Spacer(Modifier.height(6.dp))
                if (state.budget > 0) {
                    Text(
                        text = "${CurrencyFormatter.formatCompact(state.monthlyExpenses)} of ${CurrencyFormatter.formatCompact(state.budget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

// ─── Monthly summary card ─────────────────────────────────────────────────────

@Composable
private fun MonthlySummaryCard(state: DashboardUiState) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "This month",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonthlyStatPill("Income", state.monthlyIncome, Green600, Green50)
                MonthlyStatPill("Expenses", state.monthlyExpenses, Red600, Red50)
                MonthlyStatPill("Savings", state.monthlySavings, Blue600, Blue50)
            }

            if (state.budgetWarning) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = Red600, modifier = Modifier.size(14.dp))
                    Text(
                        text = "You've used over 80% of your monthly budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = Red600
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyStatPill(label: String, amount: Double, fg: Color, bg: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = CurrencyFormatter.formatCompact(amount),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─── Insights card ────────────────────────────────────────────────────────────

@Composable
private fun InsightsCard(insights: List<String>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = Amber600,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Insights",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            insights.forEach { insight ->
                val (icon, color) = when {
                    insight.startsWith("Warning") || insight.startsWith("▼") ->
                        Icons.Default.Warning to Red600
                    insight.startsWith("Great") || insight.startsWith("▲") ->
                        Icons.Default.CheckCircle to Green600
                    else -> Icons.Default.Info to Blue600
                }
                Row(
                    modifier = Modifier.padding(vertical = 4.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                    Text(
                        text = insight,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ─── Recent transactions card ─────────────────────────────────────────────────

@Composable
private fun RecentTransactionsCard(
    transactions: List<TransactionItem>,
    onTransactionClick: (TransactionItem) -> Unit,
    onSeeAll: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(
                        imageVector = Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "Recent transactions",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                TextButton(
                    onClick = onSeeAll,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "See all",
                        style = MaterialTheme.typography.labelMedium,
                        color = Blue600
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            transactions.take(5).forEachIndexed { index, tx ->
                TransactionRow(tx = tx, onClick = { onTransactionClick(tx) })
                if (index < minOf(transactions.size - 1, 4)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 6.dp),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(tx: TransactionItem, onClick: () -> Unit) {
    val isIncome = tx.type == "INCOME"
    val iconBg   = if (isIncome) Green50 else Red50
    val iconFg   = if (isIncome) Green600 else Red600

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint = iconFg,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tx.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${tx.category} · ${DateUtils.formatShortDate(tx.date)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "${if (isIncome) "+" else "-"}${CurrencyFormatter.format(tx.amount)}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = iconFg
        )
    }
}

// ─── Animated FAB menu ────────────────────────────────────────────────────────

@Composable
private fun AnimatedFabMenu(
    expanded: Boolean,
    fabRotation: Float,
    fabScale: Float,
    onFabClick: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onAddAccount: () -> Unit
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
                modifier = Modifier.padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FabMenuItem("Add account", Icons.Default.AccountBalanceWallet, Blue600, onAddAccount)
                FabMenuItem("Add expense", Icons.Default.TrendingDown, Red600, onAddExpense)
                FabMenuItem("Add income", Icons.Default.TrendingUp, Green600, onAddIncome)
            }
        }

        FloatingActionButton(
            onClick = onFabClick,
            containerColor = Blue600,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(6.dp, 12.dp)
        ) {
            Icon(
                imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Add",
                tint = Color.White,
                modifier = Modifier.rotate(fabRotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        SmallFloatingActionButton(
            onClick = onClick,
            containerColor = color,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(40.dp)
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}
