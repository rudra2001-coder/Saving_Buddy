package com.rudra.savingbuddy.ui.screens.fusion

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.*
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FusionScreen(
    viewModel: FusionViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fusion Timeline") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Timeline") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Net Worth") }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        text = { Text("Insights") }
                    )
                }

                when (selectedTab) {
                    0 -> UnifiedTimelineTab(
                        transactions = viewModel.getFilteredTransactions(),
                        showAllAccounts = uiState.showAllAccounts,
                        accountHealthList = uiState.accountHealthList,
                        selectedAccountId = uiState.selectedAccountId,
                        onToggleAllAccounts = { viewModel.onEvent(FusionEvent.ToggleAllAccounts(it)) },
                        onSelectAccount = { viewModel.onEvent(FusionEvent.SelectAccount(it)) }
                    )
                    1 -> NetWorthTab(netWorthSummary = uiState.netWorthSummary)
                    2 -> InsightsTab(
                        insights = uiState.insights,
                        goalSuggestions = uiState.goalSuggestions,
                        transferPatterns = uiState.transferPatterns,
                        onAllocateToGoal = { goalId, amount, accountId ->
                            viewModel.onEvent(FusionEvent.AllocateToGoal(goalId, amount, accountId))
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UnifiedTimelineTab(
    transactions: List<UnifiedTransaction>,
    showAllAccounts: Boolean,
    accountHealthList: List<AccountHealth>,
    selectedAccountId: Long?,
    onToggleAllAccounts: (Boolean) -> Unit,
    onSelectAccount: (Long?) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AllAccountsToggleCard(
                showAllAccounts = showAllAccounts,
                selectedAccountId = selectedAccountId,
                onToggleAllAccounts = onToggleAllAccounts
            )
        }

        if (showAllAccounts && accountHealthList.isNotEmpty()) {
            item {
                AccountHealthRow(
                    accountHealthList = accountHealthList,
                    selectedAccountId = selectedAccountId,
                    onSelectAccount = onSelectAccount
                )
            }
        }

        item {
            Text(
                text = if (showAllAccounts) "All Transactions" else "Recent Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (transactions.isEmpty()) {
            item {
                EmptyTransactionsCard()
            }
        } else {
            val groupedByDate = transactions.groupBy { 
                DateUtils.getStartOfDay(it.timestamp) 
            }

            groupedByDate.forEach { (date, dayTransactions) ->
                item {
                    DateHeader(date = date)
                }
                
                items(dayTransactions) { transaction ->
                    UnifiedTransactionCard(transaction = transaction)
                }
            }
        }
    }
}

@Composable
private fun AllAccountsToggleCard(
    showAllAccounts: Boolean,
    selectedAccountId: Long?,
    onToggleAllAccounts: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "All Accounts View",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (showAllAccounts) "Showing all transactions" else "Showing recent transactions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = showAllAccounts,
                onCheckedChange = onToggleAllAccounts
            )
        }
    }
}

@Composable
private fun AccountHealthRow(
    accountHealthList: List<AccountHealth>,
    selectedAccountId: Long?,
    onSelectAccount: (Long?) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            FilterChip(
                selected = selectedAccountId == null,
                onClick = { onSelectAccount(null) },
                label = { Text("All") }
            )
        }
        
        items(accountHealthList) { health ->
            FilterChip(
                selected = selectedAccountId == health.accountId,
                onClick = { onSelectAccount(health.accountId) },
                label = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(health.accountName.take(10))
                        Spacer(modifier = Modifier.width(4.dp))
                        HealthIndicator(status = health.status)
                    }
                }
            )
        }
    }
}

@Composable
private fun HealthIndicator(status: HealthStatus) {
    val color = when (status) {
        HealthStatus.GOOD -> IncomeGreen
        HealthStatus.MEDIUM -> Color(0xFFFFC107)
        HealthStatus.LOW -> Color(0xFFFF9800)
        HealthStatus.CRITICAL -> ExpenseRed
    }
    
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Composable
private fun DateHeader(date: Long) {
    Text(
        text = DateUtils.formatDate(date),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun UnifiedTransactionCard(transaction: UnifiedTransaction) {
    val isIncome = transaction.type in listOf(
        TransactionType.INCOME, 
        TransactionType.TRANSFER_IN
    )
    val isTransfer = transaction.type in listOf(
        TransactionType.TRANSFER_OUT, 
        TransactionType.TRANSFER_IN
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        when (transaction.type) {
                            TransactionType.INCOME -> IncomeGreen.copy(alpha = 0.15f)
                            TransactionType.EXPENSE -> ExpenseRed.copy(alpha = 0.15f)
                            TransactionType.TRANSFER_OUT -> Color(0xFFFF9800).copy(alpha = 0.15f)
                            TransactionType.TRANSFER_IN -> Color(0xFF2196F3).copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = transaction.icon,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                if (isTransfer && transaction.relatedAccountName != null) {
                    Text(
                        text = "${transaction.accountName} → ${transaction.relatedAccountName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = transaction.accountName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = DateUtils.formatShortTime(transaction.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = "${if (isIncome) "+" else "-"}${CurrencyFormatter.format(transaction.amount)}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = when (transaction.type) {
                    TransactionType.INCOME -> IncomeGreen
                    TransactionType.EXPENSE -> ExpenseRed
                    TransactionType.TRANSFER_OUT -> Color(0xFFFF9800)
                    TransactionType.TRANSFER_IN -> Color(0xFF2196F3)
                }
            )
        }
    }
}

@Composable
private fun EmptyTransactionsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Receipt,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No transactions yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NetWorthTab(netWorthSummary: NetWorthSummary?) {
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
            NetWorthCard(netWorthSummary = netWorthSummary)
        }

        item {
            Text(
                text = "Assets Breakdown",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (netWorthSummary.assetsByType.isNotEmpty()) {
            items(netWorthSummary.assetsByType.toList()) { (type, amount) ->
                AssetLiabilityRow(
                    type = type.displayName,
                    amount = amount,
                    isAsset = true
                )
            }
        }

        item {
            Text(
                text = "Total Assets",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = IncomeGreen
            )
        }
        
        item {
            Text(
                text = CurrencyFormatter.format(netWorthSummary.totalAssets),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = IncomeGreen
            )
        }

        if (netWorthSummary.totalLiabilities > 0) {
            item {
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = "Total Liabilities",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = ExpenseRed
                )
                
                Text(
                    text = CurrencyFormatter.format(netWorthSummary.totalLiabilities),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun NetWorthCard(netWorthSummary: NetWorthSummary) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Net Worth",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = CurrencyFormatter.format(netWorthSummary.netWorth),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Assets",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.formatCompact(netWorthSummary.totalAssets),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Liabilities",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                    Text(
                        text = CurrencyFormatter.formatCompact(netWorthSummary.totalLiabilities),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}

@Composable
private fun AssetLiabilityRow(
    type: String,
    amount: Double,
    isAsset: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                text = type,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isAsset) IncomeGreen else ExpenseRed
            )
        }
    }
}

@Composable
private fun InsightsTab(
    insights: List<FusionInsight>,
    goalSuggestions: List<GoalFundingSuggestion>,
    transferPatterns: List<TransferPattern>,
    onAllocateToGoal: (Long, Double, Long) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (goalSuggestions.isNotEmpty()) {
            item {
                Text(
                    text = "Goal Funding Suggestions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            items(goalSuggestions) { suggestion ->
                GoalSuggestionCard(
                    suggestion = suggestion,
                    onAllocate = { 
                        onAllocateToGoal(
                            suggestion.goalId, 
                            suggestion.suggestedAmount, 
                            suggestion.fromAccountId 
                        ) 
                    }
                )
            }
        }

        if (insights.isNotEmpty()) {
            item {
                Text(
                    text = "Smart Insights",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(insights) { insight ->
                InsightCard(insight = insight)
            }
        }

        if (transferPatterns.isNotEmpty()) {
            item {
                Text(
                    text = "Transfer Patterns",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(transferPatterns.take(5)) { pattern ->
                TransferPatternCard(pattern = pattern)
            }
        }

        if (insights.isEmpty() && goalSuggestions.isEmpty() && transferPatterns.isEmpty()) {
            item {
                EmptyInsightsCard()
            }
        }
    }
}

@Composable
private fun GoalSuggestionCard(
    suggestion: GoalFundingSuggestion,
    onAllocate: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = SavingsBlue.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = suggestion.goalName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${((suggestion.currentAmount / suggestion.targetAmount) * 100).toInt()}%",
                    style = MaterialTheme.typography.labelMedium,
                    color = SavingsBlue
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { (suggestion.currentAmount / suggestion.targetAmount).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = SavingsBlue,
                trackColor = SavingsBlue.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = suggestion.reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Suggested: ${CurrencyFormatter.format(suggestion.suggestedAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = onAllocate,
                    colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
                ) {
                    Text("Add Funds")
                }
            }
        }
    }
}

@Composable
private fun InsightCard(insight: FusionInsight) {
    val (icon, color) = when (insight.type) {
        InsightType.SPENDING_WARNING -> Icons.Default.Warning to ExpenseRed
        InsightType.SAVING_OPPORTUNITY -> Icons.Default.Savings to SavingsBlue
        InsightType.TRANSFER_HABIT -> Icons.Default.SwapHoriz to Color(0xFFFF9800)
        InsightType.GOAL_SUGGESTION -> Icons.Default.Flag to Color(0xFF9C27B0)
        InsightType.BALANCE_ALERT -> Icons.Default.AccountBalance to Color(0xFFFFC107)
        InsightType.TREND_INFO -> Icons.Default.TrendingUp to IncomeGreen
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = insight.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = insight.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TransferPatternCard(pattern: TransferPattern) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${pattern.fromAccountName} → ${pattern.toAccountName}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${pattern.transactionCount} transfers • Avg: ${CurrencyFormatter.formatCompact(pattern.averageAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.formatCompact(pattern.totalTransferred),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun EmptyInsightsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "No insights yet",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Add more transactions to see insights",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
