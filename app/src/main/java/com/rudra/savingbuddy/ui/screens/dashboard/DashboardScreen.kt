
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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
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
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// ─────────────────────────────────────────────────────────────────────────────
// COLOR TOKENS  (single source of truth — Cards file imports these via copy)
// ─────────────────────────────────────────────────────────────────────────────

internal val Blue600  = Color(0xFF185FA5)
internal val Blue50   = Color(0xFFE6F1FB)
internal val Green600 = Color(0xFF3B6D11)
internal val Green50  = Color(0xFFEAF3DE)
internal val Red600   = Color(0xFFA32D2D)
internal val Red50    = Color(0xFFFCEBEB)
internal val Amber600 = Color(0xFF854F0B)
internal val Amber50  = Color(0xFFFAEEDA)

// ─────────────────────────────────────────────────────────────────────────────
// ROOT SCREEN
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val uiState        by viewModel.uiState.collectAsState()
    var showFabMenu    by remember { mutableStateOf(false) }
    var isRefreshing   by remember { mutableStateOf(false) }
    val view           = LocalView.current
    val screenWidth    = LocalConfiguration.current.screenWidthDp

    val hPad     = when { screenWidth < 360 -> 10.dp; screenWidth < 400 -> 14.dp; else -> 16.dp }
    val cardGap  = if (screenWidth < 360) 10.dp else 14.dp

    val pullState      = rememberPullToRefreshState()
    val snackbarHost   = remember { SnackbarHostState() }

    val fabRotation by animateFloatAsState(
        targetValue    = if (showFabMenu) 45f else 0f,
        animationSpec  = tween(280),
        label          = "fab_rotation"
    )
    val fabScale by animateFloatAsState(
        targetValue    = if (showFabMenu) 0.92f else 1f,
        animationSpec  = tween(200),
        label          = "fab_scale"
    )

    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && isRefreshing) isRefreshing = false
    }
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHost.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        SnackbarHost(
            hostState = snackbarHost,
            modifier  = Modifier.align(Alignment.BottomCenter)
        )

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh    = {
                if (!isRefreshing) {
                    isRefreshing = true
                    view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                    viewModel.refreshData()
                }
            },
            state    = pullState,
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
                        .padding(horizontal = hPad),
                    verticalArrangement = Arrangement.spacedBy(cardGap)
                ) {
                    item { Spacer(Modifier.height(4.dp)) }

                    // 1. Header
                    item { DashboardHeader() }

                    // 2. Hero balance
                    item {
                        NetBalanceCard(
                            netBalance           = uiState.mainBalance,
                            monthlyIncome        = uiState.monthlyIncome,
                            monthlyExpenses      = uiState.monthlyExpenses,
                            selectedAccountName  = uiState.selectedAccountName,
                            availableAccounts    = uiState.availableAccounts,
                            heroCardColor        = uiState.heroCardColor,
                            onAccountSelect      = { viewModel.selectAccount(it) },
                            onColorSelect        = { viewModel.updateHeroColor(it) }
                        )
                    }

                    // 3. Quick actions
                    item { QuickActionsRow(navController = navController) }

                    // 4. Today + Budget
                    item { TodayAndBudgetRow(state = uiState) }

                    // 5. Active savings goal
                    uiState.activeGoal?.let { goal ->
                        item {
                            SavingsGoalCard(
                                goal    = goal,
                                onClick = { navController?.navigate("goals") }
                            )
                        }
                    }

                    // 6. This month summary
                    item { MonthlySummaryCard(state = uiState) }

                    // 7. Upcoming bills  (high urgency — shown early)
                    if (uiState.upcomingBills.isNotEmpty()) {
                        item {
                            UpcomingBillsCard(
                                bills              = uiState.upcomingBills,
                                onBillClick        = { navController?.navigate("bills") },
                                onViewCalendarClick = { navController?.navigate("calendar") }
                            )
                        }
                    }

                    // 8. Account health
                    if (uiState.accountHealthList.isNotEmpty()) {
                        item {
                            AccountHealthCard(
                                accountHealthList = uiState.accountHealthList,
                                onAccountClick    = { navController?.navigate("account_detail/$it") }
                            )
                        }
                    }

                    // 9. Net worth (compact strip) — only if meaningful
                    if (uiState.netWorth > 0 && uiState.netWorth != uiState.monthlySavings) {
                        item {
                            NetWorthCard(
                                netWorth    = uiState.netWorth,
                                totalAssets = uiState.totalAssets,
                                onClick     = { navController?.navigate("fusion") }
                            )
                        }
                    }

                    // 10. Net worth trend sparkline
                    item {
                        NetWorthTrendCard(trendData = uiState.netWorthTrend)
                    }

                    // 11. Round-up savings
                    item {
                        RoundUpCard(
                            totalSaved   = uiState.totalRoundUpSaved,
                            goalName     = uiState.roundUpGoalName,
                            goalProgress = uiState.roundUpGoalProgress,
                            isEnabled    = uiState.roundUpEnabled,
                            onClick      = { navController?.navigate("settings") }
                        )
                    }

                    // 12. Category breakdown
                    if (uiState.expensesByCategory.isNotEmpty()) {
                        item {
                            CategoryBreakdownCard(
                                categories      = uiState.expensesByCategory,
                                onCategoryClick = { navController?.navigate("transaction_history") }
                            )
                        }
                    }

                    // 13. Monthly savings trend
                    if (uiState.monthlyTrend.isNotEmpty()) {
                        item {
                            val labels = buildMonthLabels(uiState.monthlyTrend.size)
                            MonthlyTrendCard(
                                trend       = uiState.monthlyTrend,
                                monthLabels = labels,
                                onClick     = { navController?.navigate("reports") }
                            )
                        }
                    }

                    // 14. Insights
                    if (uiState.insights.isNotEmpty()) {
                        item { InsightsCard(insights = uiState.insights) }
                    }

                    // 15. Recent transactions
                    if (uiState.recentTransactions.isNotEmpty()) {
                        item {
                            RecentTransactionsCard(
                                transactions       = uiState.recentTransactions,
                                onTransactionClick = { navController?.navigate("transaction_history") },
                                onSeeAll           = { navController?.navigate("transaction_history") }
                            )
                        }
                    }

                    item { Spacer(Modifier.height(96.dp)) }
                }
            }
        }

        // FAB
        Box(
            modifier        = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            AnimatedFabMenu(
                expanded     = showFabMenu,
                fabRotation  = fabRotation,
                fabScale     = fabScale,
                onFabClick   = {
                    view.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                    showFabMenu = !showFabMenu
                },
                onAddIncome  = {
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

// ─────────────────────────────────────────────────────────────────────────────
// DASHBOARD HEADER
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun DashboardHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text      = "Dashboard",
                style     = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color     = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text  = DateUtils.formatDate(System.currentTimeMillis()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Box(
            modifier        = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Blue50),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector  = Icons.Default.Sync,
                contentDescription = "Sync",
                tint         = Blue600,
                modifier     = Modifier.size(18.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HERO BALANCE CARD
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NetBalanceCard(
    netBalance: Double,
    monthlyIncome: Double,
    monthlyExpenses: Double,
    selectedAccountName: String,
    availableAccounts: List<AccountSelection>,
    heroCardColor: Long?,
    onAccountSelect: (Long) -> Unit,
    onColorSelect: (Long) -> Unit
) {
    val isPositive   = netBalance >= 0
    var showPicker   by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }

    val defaultStart = if (isPositive) Color(0xFF185FA5) else Color(0xFFA32D2D)
    val defaultEnd   = if (isPositive) Color(0xFF0C447C) else Color(0xFF791F1F)

    val gradientStart = heroCardColor?.let { Color(it) } ?: defaultStart
    val gradientEnd   = heroCardColor?.let {
        // Derive a slightly darker color for the gradient end
        Color(it).copy(alpha = 0.85f) // Simple way to create a gradient feel
    } ?: defaultEnd

    val heroColors = listOf(
        0xFF185FA5, // Blue
        0xFF3B6D11, // Green
        0xFFA32D2D, // Red
        0xFF854F0B, // Amber
        0xFF6A1B9A, // Purple
        0xFF00695C, // Teal
        0xFF283593, // Indigo
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(20.dp),
        colors   = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(listOf(gradientStart, gradientEnd)))
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {

                // Top content
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, top = 20.dp, bottom = 0.dp)
                ) {
                    // Account picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { if (availableAccounts.isNotEmpty()) showPicker = true }
                                .padding(vertical = 4.dp),
                            verticalAlignment    = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector  = Icons.Outlined.AccountBalanceWallet,
                                contentDescription = null,
                                tint         = Color.White.copy(alpha = 0.8f),
                                modifier     = Modifier.size(15.dp)
                            )
                            Text(
                                text  = selectedAccountName,
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (availableAccounts.isNotEmpty()) {
                                Icon(
                                    imageVector  = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Change account",
                                    tint         = Color.White.copy(alpha = 0.6f),
                                    modifier     = Modifier.size(17.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = { showColorPicker = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Customize Color",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    Text(
                        text      = CurrencyFormatter.format(netBalance),
                        style     = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                        color     = Color.White
                    )
                    Text(
                        text  = "Current balance · Updated just now",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.55f)
                    )

                    Spacer(Modifier.height(16.dp))
                }

                // Bottom strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.10f))
                ) {
                    HeroStat(
                        label    = "Income",
                        value    = CurrencyFormatter.format(monthlyIncome),
                        modifier = Modifier.weight(1f).padding(16.dp)
                    )
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(44.dp)
                            .align(Alignment.CenterVertically)
                            .background(Color.White.copy(alpha = 0.2f))
                    )
                    HeroStat(
                        label    = "Expenses",
                        value    = CurrencyFormatter.format(monthlyExpenses),
                        modifier = Modifier.weight(1f).padding(16.dp)
                    )
                }
            }
        }
    }

    // Color picker dialog
    if (showColorPicker) {
        AlertDialog(
            onDismissRequest = { showColorPicker = false },
            title = { Text("Customize Hero Color", fontWeight = FontWeight.SemiBold) },
            text = {
                Column {
                    Text("Select a color:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(12.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 4.dp)
                    ) {
                        items(heroColors) { colorLong ->
                            val color = Color(colorLong)
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(color)
                                    .clickable {
                                        onColorSelect(colorLong)
                                        showColorPicker = false
                                    }
                                    .then(
                                        if (heroCardColor == colorLong) {
                                            Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                                        } else Modifier
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    Text("Or mix your own:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))

                    var red by remember { mutableStateOf(((heroCardColor ?: 0xFF185FA5) shr 16 and 0xFF).toFloat()) }
                    var green by remember { mutableStateOf(((heroCardColor ?: 0xFF185FA5) shr 8 and 0xFF).toFloat()) }
                    var blue by remember { mutableStateOf(((heroCardColor ?: 0xFF185FA5) and 0xFF).toFloat()) }

                    Column {
                        Slider(value = red, onValueChange = { red = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(thumbColor = Color.Red))
                        Slider(value = green, onValueChange = { green = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(thumbColor = Color.Green))
                        Slider(value = blue, onValueChange = { blue = it }, valueRange = 0f..255f, colors = SliderDefaults.colors(thumbColor = Color.Blue))

                        val mixedColor = (0xFFL shl 24) or (red.toLong() shl 16) or (green.toLong() shl 8) or blue.toLong()
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(mixedColor))
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            TextButton(onClick = {
                                onColorSelect(-1L)
                                showColorPicker = false
                            }) {
                                Text("Reset to Default")
                            }
                            Button(
                                onClick = {
                                    onColorSelect(mixedColor)
                                    showColorPicker = false
                                }
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showColorPicker = false }) { Text("Close") }
            }
        )
    }

    // Account picker dialog
    if (showPicker && availableAccounts.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showPicker = false },
            title = { Text("Select account", fontWeight = FontWeight.SemiBold) },
            text  = {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableAccounts) { acc ->
                        val isSelected = acc.name == selectedAccountName
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onAccountSelect(acc.id)
                                    showPicker = false
                                },
                            shape  = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) Blue50
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(acc.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                                    Text(
                                        "Balance: ${CurrencyFormatter.format(acc.balance)}",
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
private fun HeroStat(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, fontSize = 11.sp, color = Color.White.copy(alpha = 0.55f))
        Spacer(Modifier.height(2.dp))
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// QUICK ACTIONS ROW
// ─────────────────────────────────────────────────────────────────────────────

private data class QuickAction(
    val label: String,
    val icon: ImageVector,
    val bg: Color,
    val fg: Color,
    val route: String
)

@Composable
private fun QuickActionsRow(navController: NavController?) {
    val actions = listOf(
        QuickAction("Income",   Icons.Outlined.TrendingUp,    Green50,  Green600, "add_income"),
        QuickAction("Expense",  Icons.Outlined.TrendingDown,  Red50,    Red600,   "add_expense"),
        QuickAction("Goals",    Icons.Outlined.Flag,          Blue50,   Blue600,  "goals"),
        QuickAction("Budget",   Icons.Outlined.PieChart,      Amber50,  Amber600, "budget"),
        QuickAction("Bills",    Icons.Outlined.Receipt,       Red50,    Red600,   "bills"),
        QuickAction("Export",   Icons.Outlined.FileDownload,  Blue50,   Blue600,  "export"),
        QuickAction("Calendar", Icons.Outlined.CalendarMonth, Amber50,  Amber600, "calendar"),
        QuickAction("Fusion",   Icons.Outlined.JoinFull,      Blue50,   Blue600,  "fusion"),
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        contentPadding        = PaddingValues(horizontal = 2.dp)
    ) {
        items(actions) { a ->
            QuickActionChip(
                label   = a.label,
                icon    = a.icon,
                bg      = a.bg,
                fg      = a.fg,
                onClick = { navController?.navigate(a.route) }
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String, icon: ImageVector, bg: Color, fg: Color, onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Box(
            modifier        = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(bg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(5.dp))
        Text(
            text      = label,
            fontSize  = 11.sp,
            color     = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines  = 1,
            overflow  = TextOverflow.Ellipsis
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TODAY + BUDGET  2-COLUMN
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TodayAndBudgetRow(state: DashboardUiState) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Today card
        Card(
            modifier  = Modifier.weight(1f),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border    = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text       = "Today",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(10.dp))
                MiniStat("Income",   CurrencyFormatter.formatCompact(state.todayIncome),   Green600)
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(6.dp))
                MiniStat("Expenses", CurrencyFormatter.formatCompact(state.todayExpenses), Red600)
                Spacer(Modifier.height(6.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(6.dp))
                MiniStat("Net",      CurrencyFormatter.formatCompact(state.todaySavings),  Blue600)
            }
        }

        // Budget card
        val budgetProgress by animateFloatAsState(
            targetValue   = if (state.budget > 0)
                (state.monthlyExpenses / state.budget).toFloat().coerceIn(0f, 1f)
            else 0f,
            animationSpec = tween(1000),
            label         = "budget_progress"
        )
        val budgetPct      = (budgetProgress * 100).toInt()
        val progressColor  = when {
            state.budgetWarning -> Red600
            budgetPct > 60      -> Color(0xFFEF9F27)
            else                -> Blue600
        }
        val statusLabel    = when {
            state.budgetWarning -> "Over budget!"
            budgetPct > 60      -> "Watch it"
            else                -> "On track"
        }
        val statusBg       = when {
            state.budgetWarning -> Red50
            budgetPct > 60      -> Amber50
            else                -> Blue50
        }

        Card(
            modifier  = Modifier.weight(1f),
            shape     = RoundedCornerShape(16.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border    = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text       = "Budget used",
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text       = "$budgetPct%",
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = progressColor
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
                Spacer(Modifier.height(8.dp))
                if (state.budget > 0) {
                    Text(
                        text  = "${CurrencyFormatter.formatCompact(state.monthlyExpenses)} of ${CurrencyFormatter.formatCompact(state.budget)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(6.dp))
                Surface(color = statusBg, shape = RoundedCornerShape(99.dp)) {
                    Text(
                        text     = statusLabel,
                        style    = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color    = progressColor,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MiniStat(label: String, value: String, color: Color) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, color = color)
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MONTHLY SUMMARY CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun MonthlySummaryCard(state: DashboardUiState) {
    val monthLabel = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date())

    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = Blue600, modifier = Modifier.size(17.dp))
                    Text("This month", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                Text(monthLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Spacer(Modifier.height(14.dp))

            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MonthPill("Income",   state.monthlyIncome,   Green600, Green50)
                MonthPill("Expenses", state.monthlyExpenses, Red600,   Red50)
                MonthPill("Saved",    state.monthlySavings,  Blue600,  Blue50)
            }

            if (state.budgetWarning) {
                Spacer(Modifier.height(14.dp))
                HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Warning, null, tint = Red600, modifier = Modifier.size(14.dp))
                    Text(
                        text  = "You've used over 80% of your monthly budget",
                        style = MaterialTheme.typography.labelSmall,
                        color = Red600
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthPill(label: String, amount: Double, fg: Color, bg: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier        = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(bg)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text       = CurrencyFormatter.formatCompact(amount),
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = fg
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            text  = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// INSIGHTS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun InsightsCard(insights: List<String>) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier              = Modifier.padding(bottom = 10.dp)
            ) {
                Icon(Icons.Default.Lightbulb, null, tint = Amber600, modifier = Modifier.size(17.dp))
                Text("Smart insights", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            }

            insights.forEachIndexed { idx, insight ->
                val (icon, color) = when {
                    insight.startsWith("Warning") || insight.contains("90%") ->
                        Icons.Default.Warning to Red600
                    insight.startsWith("Great") || insight.contains("50%") ->
                        Icons.Default.CheckCircle to Green600
                    else -> Icons.Default.Info to Blue600
                }
                Row(
                    modifier              = Modifier.padding(vertical = 4.dp),
                    verticalAlignment     = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(14.dp).padding(top = 2.dp))
                    Text(
                        text  = insight,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (idx < insights.lastIndex) {
                    HorizontalDivider(
                        modifier  = Modifier.padding(vertical = 4.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// RECENT TRANSACTIONS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun RecentTransactionsCard(
    transactions: List<TransactionItem>,
    onTransactionClick: (TransactionItem) -> Unit,
    onSeeAll: () -> Unit
) {
    Card(
        modifier  = Modifier.fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border    = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, null, tint = Blue600, modifier = Modifier.size(17.dp))
                    Text("Recent transactions", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick         = onSeeAll,
                    contentPadding  = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("See all", style = MaterialTheme.typography.labelMedium, color = Blue600)
                }
            }

            Spacer(Modifier.height(8.dp))

            transactions.take(5).forEachIndexed { idx, tx ->
                TransactionRow(tx = tx, onClick = { onTransactionClick(tx) })
                if (idx < minOf(transactions.size - 1, 4)) {
                    HorizontalDivider(
                        modifier  = Modifier.padding(vertical = 6.dp),
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant
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
        modifier              = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 2.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier        = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector  = if (isIncome) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                contentDescription = null,
                tint         = iconFg,
                modifier     = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text      = tx.title,
                style     = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color     = MaterialTheme.colorScheme.onSurface,
                maxLines  = 1,
                overflow  = TextOverflow.Ellipsis
            )
            Text(
                text  = "${tx.category} · ${DateUtils.formatShortDate(tx.date)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text      = "${if (isIncome) "+" else "-"}${CurrencyFormatter.format(tx.amount)}",
            style     = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color     = iconFg
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ANIMATED FAB MENU
// ─────────────────────────────────────────────────────────────────────────────

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
        modifier            = Modifier.scale(fabScale)
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter   = fadeIn(tween(200)) + expandVertically(tween(200)),
            exit    = fadeOut(tween(200)) + shrinkVertically(tween(200))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                modifier            = Modifier.padding(bottom = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FabMenuItem("Add income",
                    Icons.AutoMirrored.Filled.TrendingUp,           Green600, onAddIncome)

                FabMenuItem("Add expense",
                    Icons.AutoMirrored.Filled.TrendingDown,         Red600,   onAddExpense)

                FabMenuItem("Add account",
                    Icons.Default.AccountBalanceWallet, Blue600,  onAddAccount)

            }
        }

        FloatingActionButton(
            onClick          = onFabClick,
            containerColor   = Blue600,
            shape            = RoundedCornerShape(16.dp),
            elevation        = FloatingActionButtonDefaults.elevation(6.dp, 12.dp)
        ) {
            Icon(
                imageVector  = if (expanded) Icons.Default.Close else Icons.Default.Add,
                contentDescription = if (expanded) "Close" else "Add transaction",
                tint         = Color.White,
                modifier     = Modifier.rotate(fabRotation)
            )
        }
    }
}

@Composable
private fun FabMenuItem(label: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color           = MaterialTheme.colorScheme.surface,
            shape           = RoundedCornerShape(8.dp),
            shadowElevation = 4.dp
        ) {
            Text(
                text     = label,
                style    = MaterialTheme.typography.labelMedium,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                color    = MaterialTheme.colorScheme.onSurface
            )
        }
        SmallFloatingActionButton(
            onClick          = onClick,
            containerColor   = color,
            shape            = RoundedCornerShape(12.dp),
            modifier         = Modifier.size(40.dp)
        ) {
            Icon(icon, label, tint = Color.White, modifier = Modifier.size(18.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// HELPERS
// ─────────────────────────────────────────────────────────────────────────────

private fun buildMonthLabels(count: Int): List<String> {
    val sdf = SimpleDateFormat("MMM", Locale.getDefault())
    return (count - 1 downTo 0).map { offset ->
        val ms = System.currentTimeMillis() - offset * 30L * 24 * 60 * 60 * 1000
        sdf.format(Date(ms))
    }
}
