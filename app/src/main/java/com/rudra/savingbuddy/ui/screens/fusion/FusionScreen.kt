package com.rudra.savingbuddy.ui.screens.fusion

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.*
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.ui.components.AnimatedNumber
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FusionScreen(
    viewModel: FusionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val view = LocalView.current
    val listState = rememberLazyListState()
    val pullToRefreshState = rememberPullToRefreshState()
    var isRefreshing by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing && isRefreshing) {
            isRefreshing = false
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { error ->
            snackbarHostState.showSnackbar(error)
            viewModel.onEvent(FusionEvent.ClearError)
        }
    }

    val isDarkMode = !MaterialTheme.colorScheme.background.luminance().let { it > 0.5f }

    Box(modifier = Modifier.fillMaxSize()) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { 
                        Text(
                            "Fusion Analytics", 
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        ) 
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                Icons.Default.ArrowBack, 
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent
                    ),
                    actions = {
                        IconButton(onClick = {
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.onEvent(FusionEvent.Refresh)
                        }) {
                            Icon(
                                Icons.Default.Refresh, 
                                contentDescription = "Refresh",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (uiState.isLoading && uiState.unifiedTransactions.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    ModernFusionTabRow(selectedTab = selectedTab, onTabSelected = { selectedTab = it }, isDarkMode = isDarkMode)

                    PullToRefreshBox(
                        isRefreshing = isRefreshing,
                        onRefresh = {
                            isRefreshing = true
                            view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                            viewModel.onEvent(FusionEvent.Refresh)
                        },
                        state = pullToRefreshState,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        AnimatedContent(
                            targetState = selectedTab,
                            transitionSpec = {
                                slideInVertically { it } + fadeIn() togetherWith slideOutVertically { -it } + fadeOut()
                            },
                            label = "tab_animation"
                        ) { tab ->
                            when (tab) {
                                0 -> UnifiedTimelineTab(
                                    uiState = uiState,
                                    viewModel = viewModel,
                                    isDarkMode = isDarkMode
                                )
                                1 -> NetWorthTab(netWorthSummary = uiState.netWorthSummary, isDarkMode = isDarkMode)
                                2 -> InsightsTab(
                                    insights = uiState.insights,
                                    goalSuggestions = uiState.goalSuggestions,
                                    transferPatterns = uiState.transferPatterns,
                                    onAllocateToGoal = { goalId, amount, accountId ->
                                        viewModel.onEvent(FusionEvent.AllocateToGoal(goalId, amount, accountId))
                                    },
                                    isDarkMode = isDarkMode
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ModernFusionTabRow(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    isDarkMode: Boolean
) {
    TabRow(
        selectedTabIndex = selectedTab,
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        divider = {}
    ) {
        ModernTab(
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) },
            text = "Timeline",
            icon = Icons.Default.Timeline
        )
        ModernTab(
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) },
            text = "Net Worth",
            icon = Icons.Default.AccountBalance
        )
        ModernTab(
            selected = selectedTab == 2,
            onClick = { onTabSelected(2) },
            text = "Insights",
            icon = Icons.Default.Lightbulb
        )
    }
}

@Composable
private fun ModernTab(
    selected: Boolean,
    onClick: () -> Unit,
    text: String,
    icon: ImageVector
) {
    Tab(
        selected = selected,
        onClick = onClick,
        selectedContentColor = MaterialTheme.colorScheme.primary,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun UnifiedTimelineTab(
    uiState: FusionUiState,
    viewModel: FusionViewModel,
    isDarkMode: Boolean
) {
    val transactions = viewModel.getFilteredTransactions()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModernAllAccountsCard(
                showAllAccounts = uiState.showAllAccounts,
                showAllSelected = uiState.selectedAccountId == null,
                onToggleAllAccounts = { viewModel.onEvent(FusionEvent.ToggleAllAccounts(it)) },
                isDarkMode = isDarkMode
            )
        }

        if (uiState.showAllAccounts && uiState.accountHealthList.isNotEmpty()) {
            item {
                ModernAccountHealthCards(
                    accountHealthList = uiState.accountHealthList,
                    selectedAccountId = uiState.selectedAccountId,
                    onSelectAccount = { viewModel.onEvent(FusionEvent.SelectAccount(it)) }
                )
            }
        }

        item {
            Text(
                text = if (uiState.showAllAccounts) "All Account Activity" else "Recent Transactions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (transactions.isEmpty()) {
            item {
                ModernEmptyHistoryCard(isDarkMode = isDarkMode)
            }
        } else {
            val groupedByDate = transactions.groupBy { 
                DateUtils.getStartOfDay(it.timestamp) 
            }

            groupedByDate.forEach { (date, dayTransactions) ->
                item(key = "header_$date") {
                    ModernDateSectionHeader(date = date, transactionCount = dayTransactions.size, isDarkMode = isDarkMode)
                }
                
                items(dayTransactions.size, key = { index -> 
                    dayTransactions[index].id 
                }) { index ->
                    ModernTransactionCard(
                        transaction = dayTransactions[index],
                        isFirst = index == 0,
                        isLast = index == dayTransactions.lastIndex,
                        isDarkMode = isDarkMode
                    )
                }
            }

            if (uiState.isLoadingMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(32.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ModernAllAccountsCard(
    showAllAccounts: Boolean,
    showAllSelected: Boolean,
    onToggleAllAccounts: (Boolean) -> Unit,
    isDarkMode: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (showAllAccounts) Icons.Default.AccountBalanceWallet else Icons.Default.Wallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = if (showAllAccounts) "All Accounts" else "Quick View",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (showAllAccounts) "View complete transaction history" else "Show recent activity only",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = showAllAccounts,
                onCheckedChange = onToggleAllAccounts,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
private fun ModernAccountHealthCards(
    accountHealthList: List<AccountHealth>,
    selectedAccountId: Long?,
    onSelectAccount: (Long?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ModernAccountHealthChip(
                accountName = "All",
                health = if (selectedAccountId == null) HealthStatus.GOOD else null,
                isSelected = selectedAccountId == null,
                onClick = { onSelectAccount(null) }
            )
        }
        
        items(accountHealthList) { health ->
            ModernAccountHealthChip(
                accountName = health.accountName,
                health = health.status,
                isSelected = selectedAccountId == health.accountId,
                onClick = { onSelectAccount(health.accountId) }
            )
        }
    }
}

@Composable
private fun ModernAccountHealthChip(
    accountName: String,
    health: HealthStatus?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val healthColor = when (health) {
        HealthStatus.GOOD -> IncomeGreen
        HealthStatus.MEDIUM -> Color(0xFFFFC107)
        HealthStatus.LOW -> Color(0xFFFF9800)
        HealthStatus.CRITICAL -> ExpenseRed
        null -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) healthColor.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (health != null) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(healthColor)
                )
            }
            Text(
                text = accountName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) healthColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun ModernDateSectionHeader(date: Long, transactionCount: Int, isDarkMode: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = DateUtils.formatDate(date),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "$transactionCount",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun ModernTransactionCard(
    transaction: UnifiedTransaction,
    isFirst: Boolean,
    isLast: Boolean,
    isDarkMode: Boolean
) {
    val isIncome = transaction.type in listOf(TransactionType.INCOME, TransactionType.TRANSFER_IN)
    val isTransfer = transaction.type in listOf(TransactionType.TRANSFER_OUT, TransactionType.TRANSFER_IN)
    
    val backgroundColor = when (transaction.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER_OUT -> Color(0xFFFF9800)
        TransactionType.TRANSFER_IN -> Color(0xFF2196F3)
    }

    val backgroundLight = backgroundColor.copy(alpha = 0.12f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(backgroundLight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.icon,
                    style = MaterialTheme.typography.headlineSmall
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = transaction.category,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Surface(
                        color = backgroundLight,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = transaction.type.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelSmall,
                            color = backgroundColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                if (isTransfer && transaction.relatedAccountName != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = transaction.accountName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = transaction.relatedAccountName,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = transaction.accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = DateUtils.formatShortTime(transaction.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (isIncome) "+" else "-"}${CurrencyFormatter.format(transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = backgroundColor
                )
            }
        }
    }
}

@Composable
private fun ModernEmptyHistoryCard(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.ReceiptLong,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Transactions Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Start adding income and expenses",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NetWorthTab(netWorthSummary: NetWorthSummary?, isDarkMode: Boolean) {
    if (netWorthSummary == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ModernNetWorthCard(netWorthSummary = netWorthSummary, isDarkMode = isDarkMode)
        }

        item {
            ModernNetWorthBreakdownCard(netWorthSummary = netWorthSummary, isDarkMode = isDarkMode)
        }

        if (netWorthSummary.assetsByType.isNotEmpty()) {
            item {
                Text(
                    text = "Assets Breakdown",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(netWorthSummary.assetsByType.toList()) { (type, amount) ->
                ModernAssetLiabilityItem(
                    type = type.displayName,
                    amount = amount,
                    isAsset = true,
                    totalAmount = netWorthSummary.totalAssets,
                    isDarkMode = isDarkMode
                )
            }
        }

        if (netWorthSummary.totalLiabilities > 0) {
            item {
                Text(
                    text = "Liabilities",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(netWorthSummary.liabilitiesByType.toList()) { (type, amount) ->
                ModernAssetLiabilityItem(
                    type = type.displayName,
                    amount = amount,
                    isAsset = false,
                    totalAmount = netWorthSummary.totalLiabilities,
                    isDarkMode = isDarkMode
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ModernNetWorthCard(netWorthSummary: NetWorthSummary, isDarkMode: Boolean) {
    val animatedNetWorth by animateFloatAsState(
        targetValue = netWorthSummary.netWorth.toFloat(),
        animationSpec = tween(1500),
        label = "net_worth_animation"
    )

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
                        colors = if (isDarkMode) {
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary,
                                MaterialTheme.colorScheme.primary
                            )
                        } else {
                            listOf(PrimaryGreen, AccentTeal, PrimaryGreen)
                        }
                    )
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Total Net Worth",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                AnimatedNumber(
                    targetNumber = animatedNetWorth.toDouble(),
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    textColor = Color.White
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ModernNetWorthStatBox(
                        icon = Icons.Default.TrendingUp,
                        label = "Assets",
                        value = CurrencyFormatter.formatCompact(netWorthSummary.totalAssets),
                        color = IncomeGreen
                    )
                    ModernNetWorthStatBox(
                        icon = Icons.Default.TrendingDown,
                        label = "Liabilities",
                        value = CurrencyFormatter.formatCompact(netWorthSummary.totalLiabilities),
                        color = ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernNetWorthStatBox(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun ModernNetWorthBreakdownCard(netWorthSummary: NetWorthSummary, isDarkMode: Boolean) {
    val assetsPercent = if (netWorthSummary.totalAssets > 0) {
        (netWorthSummary.totalAssets / (netWorthSummary.totalAssets + netWorthSummary.totalLiabilities) * 100).toInt()
    } else 100

    val liabilitiesPercent = 100 - assetsPercent

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "Financial Overview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(assetsPercent / 100f)
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(IncomeGreen, MaterialTheme.colorScheme.primary)
                                ),
                                shape = RoundedCornerShape(6.dp)
                            )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(IncomeGreen)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assets $assetsPercent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = IncomeGreen
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(ExpenseRed)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Liabilities $liabilitiesPercent%",
                        style = MaterialTheme.typography.bodySmall,
                        color = ExpenseRed
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernAssetLiabilityItem(
    type: String,
    amount: Double,
    isAsset: Boolean,
    totalAmount: Double,
    isDarkMode: Boolean
) {
    val percentage = if (totalAmount > 0) (amount / totalAmount).toFloat() else 0f
    
    val color = if (isAsset) IncomeGreen else ExpenseRed

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = if (isDarkMode) 0.08f else 0.05f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = CurrencyFormatter.format(amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LinearProgressIndicator(
                    progress = { percentage },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = color,
                    trackColor = color.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
private fun InsightsTab(
    insights: List<FusionInsight>,
    goalSuggestions: List<GoalFundingSuggestion>,
    transferPatterns: List<TransferPattern>,
    onAllocateToGoal: (Long, Double, Long) -> Unit,
    isDarkMode: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (goalSuggestions.isNotEmpty()) {
            item {
                ModernSectionHeader(
                    title = "Goal Funding",
                    subtitle = "Fund your goals faster",
                    icon = Icons.Default.Flag,
                    isDarkMode = isDarkMode
                )
            }

            items(goalSuggestions) { suggestion ->
                ModernGoalCard(
                    suggestion = suggestion,
                    onAllocate = { 
                        onAllocateToGoal(
                            suggestion.goalId, 
                            suggestion.suggestedAmount, 
                            suggestion.fromAccountId 
                        ) 
                    },
                    isDarkMode = isDarkMode
                )
            }
        }

        if (insights.isNotEmpty()) {
            item {
                ModernSectionHeader(
                    title = "Smart Insights",
                    subtitle = "AI-powered recommendations",
                    icon = Icons.Default.Psychology,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(insights) { insight ->
                ModernInsightCard(insight = insight, isDarkMode = isDarkMode)
            }
        }

        if (transferPatterns.isNotEmpty()) {
            item {
                ModernSectionHeader(
                    title = "Transfer Patterns",
                    subtitle = "Track your money movement",
                    icon = Icons.Default.SwapHoriz,
                    isDarkMode = isDarkMode,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(transferPatterns.take(5)) { pattern ->
                ModernTransferPatternCard(pattern = pattern, isDarkMode = isDarkMode)
            }
        }

        if (insights.isEmpty() && goalSuggestions.isEmpty() && transferPatterns.isEmpty()) {
            item {
                ModernEmptyInsightsCard(isDarkMode = isDarkMode)
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

@Composable
private fun ModernSectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isDarkMode: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernGoalCard(
    suggestion: GoalFundingSuggestion,
    onAllocate: () -> Unit,
    isDarkMode: Boolean
) {
    val progress by animateFloatAsState(
        targetValue = (suggestion.currentAmount / suggestion.targetAmount).toFloat(),
        animationSpec = tween(1000),
        label = "progress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = SavingsBlue.copy(alpha = if (isDarkMode) 0.1f else 0.08f)
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
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(SavingsBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = SavingsBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = suggestion.goalName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${CurrencyFormatter.formatCompact(suggestion.currentAmount)} / ${CurrencyFormatter.formatCompact(suggestion.targetAmount)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Surface(
                    color = SavingsBlue.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = SavingsBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = SavingsBlue,
                trackColor = SavingsBlue.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = suggestion.reason,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onAllocate,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SavingsBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "+${CurrencyFormatter.formatCompact(suggestion.suggestedAmount)}",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun ModernInsightCard(insight: FusionInsight, isDarkMode: Boolean) {
    val (icon, color, backgroundColor) = getInsightColors(insight.type)

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernTransferPatternCard(pattern: TransferPattern, isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDarkMode) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = pattern.fromAccountName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFFF9800),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = pattern.toAccountName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFFF9800).copy(alpha = 0.1f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "${pattern.transactionCount} transfers",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Avg: ${CurrencyFormatter.formatCompact(pattern.averageAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.formatCompact(pattern.totalTransferred),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun ModernEmptyInsightsCard(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(40.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Insights Yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Add more transactions to see AI insights",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun getInsightColors(type: InsightType): Triple<ImageVector, Color, Color> {
    return when (type) {
        InsightType.SPENDING_WARNING -> Triple(Icons.Default.Warning, ExpenseRed, ExpenseRed.copy(alpha = 0.1f))
        InsightType.SAVING_OPPORTUNITY -> Triple(Icons.Default.Savings, SavingsBlue, SavingsBlue.copy(alpha = 0.1f))
        InsightType.TRANSFER_HABIT -> Triple(Icons.Default.SwapHoriz, Color(0xFFFF9800), Color(0xFFFF9800).copy(alpha = 0.1f))
        InsightType.GOAL_SUGGESTION -> Triple(Icons.Default.Flag, Color(0xFF9C27B0), Color(0xFF9C27B0).copy(alpha = 0.1f))
        InsightType.BALANCE_ALERT -> Triple(Icons.Default.AccountBalance, Color(0xFFFFC107), Color(0xFFFFC107).copy(alpha = 0.1f))
        InsightType.TREND_INFO -> Triple(Icons.Default.TrendingUp, IncomeGreen, IncomeGreen.copy(alpha = 0.1f))
    }
}

private fun Color.luminance(): Float {
    val red = this.red
    val green = this.green
    val blue = this.blue
    return 0.299f * red + 0.587f * green + 0.114f * blue
}