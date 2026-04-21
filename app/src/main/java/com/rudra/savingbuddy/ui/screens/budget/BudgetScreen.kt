package com.rudra.savingbuddy.ui.screens.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.BillingCycle
import com.rudra.savingbuddy.domain.model.Subscription
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import kotlin.math.abs

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BudgetScreen(
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }

    val animatedProgress by animateFloatAsState(
        targetValue = if (uiState.totalBudget > 0)
            (uiState.spent / uiState.totalBudget).toFloat().coerceIn(0f, 1f)
        else 0f,
        animationSpec = tween(1200),
        label = "budgetProgress"
    )

    val tabs = listOf("Budget", "Subscriptions", "Reminders")
    val tabIcons = listOf(Icons.Outlined.PieChart, Icons.Outlined.Subscriptions, Icons.Outlined.Notifications)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            AnimatedFloatingActionButton(
                selectedTab = selectedTab,
                onBudgetClick = { viewModel.showBudgetDialog() },
                onSubscriptionClick = { viewModel.showAddSubscriptionDialog() }
            )
        },
        floatingActionButtonPosition = FabPosition.Center
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                HeaderSection()
            }

            item {
                EnhancedTabRow(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    tabIcons = tabIcons,
                    onTabSelected = { selectedTab = it }
                )
            }

            when (selectedTab) {
                0 -> renderEnhancedBudgetOverview(uiState, animatedProgress)
                1 -> renderEnhancedSubscriptionsList(viewModel, uiState)
                2 -> renderEnhancedRemindersList(viewModel, uiState)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    // Dialogs
    if (uiState.showEditBudgetDialog) {
        EnhancedBudgetDialog(
            currentBudget = uiState.totalBudget,
            currentThreshold = uiState.alertThreshold,
            onDismiss = { viewModel.hideBudgetDialog() },
            onSave = { amount, threshold, rollover ->
                viewModel.setBudget(amount, threshold, rollover)
            }
        )
    }

    if (uiState.showAddSubscriptionDialog) {
        EnhancedSubscriptionDialog(
            subscription = uiState.editingSubscription,
            onDismiss = { viewModel.hideSubscriptionDialog() },
            onSave = { name, amount, cycle, date, category, isActive ->
                viewModel.saveSubscription(name, amount, cycle, date, category, isActive)
            }
        )
    }
}

@Composable
private fun HeaderSection() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                "Budget &",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                "Subscriptions",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Card(
            modifier = Modifier
                .size(48.dp)
                .shadow(8.dp, CircleShape),
            shape = CircleShape,
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EnhancedTabRow(
    selectedTab: Int,
    tabs: List<String>,
    tabIcons: List<ImageVector>,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            tabs.forEachIndexed { index, title ->
                TabItem(
                    title = title,
                    icon = tabIcons[index],
                    isSelected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TabItem(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            Color.Transparent,
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun AnimatedFloatingActionButton(
    selectedTab: Int,
    onBudgetClick: () -> Unit,
    onSubscriptionClick: () -> Unit
) {
    val onClick = when (selectedTab) {
        0 -> onBudgetClick
        else -> onSubscriptionClick
    }

    val icon = when (selectedTab) {
        0 -> Icons.Default.Edit
        else -> Icons.Default.Add
    }

    FloatingActionButton(
        onClick = onClick,
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .size(64.dp)
            .shadow(12.dp, RoundedCornerShape(20.dp)),
        elevation = FloatingActionButtonDefaults.elevation(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = when (selectedTab) {
                0 -> "Edit Budget"
                else -> "Add Subscription"
            },
            modifier = Modifier.size(28.dp)
        )
    }
}

private fun LazyListScope.renderEnhancedBudgetOverview(
    uiState: BudgetUiState,
    animatedProgress: Float
) {
    val progressPercentage = (animatedProgress * 100).toInt()
    val isOverBudget = uiState.spent > uiState.totalBudget
    val isNearThreshold = progressPercentage >= uiState.alertThreshold && uiState.totalBudget > 0

    item {
        EnhancedBudgetCard(
            totalBudget = uiState.totalBudget,
            spent = uiState.spent,
            remaining = uiState.remaining,
            progress = animatedProgress,
            progressPercentage = progressPercentage,
            isOverBudget = isOverBudget,
            isNearThreshold = isNearThreshold
        )
    }

    item {
        AlertThresholdCard(
            threshold = uiState.alertThreshold,
            currentPercentage = progressPercentage,
            isNearThreshold = isNearThreshold
        )
    }

    item {
        QuickStatsCard(
            spent = uiState.spent,
            remaining = uiState.remaining,
            totalBudget = uiState.totalBudget
        )
    }

    if (uiState.upcomingRenewals.isNotEmpty()) {
        item {
            UpcomingRenewalsCard(renewals = uiState.upcomingRenewals)
        }
    }
}

@Composable
private fun EnhancedBudgetCard(
    totalBudget: Double,
    spent: Double,
    remaining: Double,
    progress: Float,
    progressPercentage: Int,
    isOverBudget: Boolean,
    isNearThreshold: Boolean
) {
    val gradientColors = when {
        isOverBudget -> listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.7f))
        isNearThreshold -> listOf(Color(0xFFFF9800), Color(0xFFFFB74D))
        else -> listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(0f, 0f),
                        end = Offset(1000f, 1000f)
                    )
                )
                .padding(28.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Monthly Budget",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.95f)
                    )

                    if (isOverBudget) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "OVER BUDGET",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    CurrencyFormatter.format(totalBudget),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 48.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                EnhancedProgressIndicator(
                    progress = progress,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(16.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Spent",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            CurrencyFormatter.format(spent),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Remaining",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            CurrencyFormatter.format(remaining),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "$progressPercentage% of budget used",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f),
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EnhancedProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.White, Color.White.copy(alpha = 0.9f))
                    )
                )
                .clip(RoundedCornerShape(8.dp))
        )
    }
}

@Composable
private fun AlertThresholdCard(
    threshold: Int,
    currentPercentage: Int,
    isNearThreshold: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isNearThreshold)
                Color(0xFFFF9800).copy(alpha = 0.1f)
            else
                MaterialTheme.colorScheme.surface
        ),
        border = if (isNearThreshold)
            BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f))
        else
            null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF9800).copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Alert Threshold",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        "Get notified at ${threshold}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "$threshold%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isNearThreshold) Color(0xFFFF9800) else MaterialTheme.colorScheme.primary
                )

                if (isNearThreshold) {
                    Text(
                        "Approaching!",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFFFF9800)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickStatsCard(
    spent: Double,
    remaining: Double,
    totalBudget: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                "Quick Stats",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatItem(
                    icon = Icons.Outlined.TrendingDown,
                    label = "Daily Average",
                    value = if (totalBudget > 0)
                        CurrencyFormatter.format(spent / 30)
                    else
                        "₹0",
                    color = ExpenseRed
                )

                StatItem(
                    icon = Icons.Outlined.TrendingUp,
                    label = "Monthly Goal",
                    value = CurrencyFormatter.format(totalBudget),
                    color = IncomeGreen
                )

                StatItem(
                    icon = Icons.Outlined.Savings,
                    label = "Saved",
                    value = CurrencyFormatter.format(abs(remaining).coerceAtMost(totalBudget)),
                    color = SavingsBlue
                )
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(36.dp),
            shape = CircleShape,
            color = color.copy(alpha = 0.1f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UpcomingRenewalsCard(renewals: List<Subscription>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = ExpenseRed.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Surface(
                    modifier = Modifier.size(36.dp),
                    shape = CircleShape,
                    color = ExpenseRed.copy(alpha = 0.15f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        "Upcoming Renewals",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                    Text(
                        "${renewals.size} subscription${if (renewals.size > 1) "s" else ""} renewing soon",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            renewals.forEach { subscription ->
                RenewalItem(subscription = subscription)
                if (subscription != renewals.last()) {
                    Divider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = ExpenseRed.copy(alpha = 0.1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun RenewalItem(subscription: Subscription) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                    .background(
                        ExpenseRed.copy(alpha = 0.1f),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    subscription.name.take(1).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column {
                Text(
                    subscription.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    subscription.billingCycle.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text(
            CurrencyFormatter.format(subscription.amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = ExpenseRed
        )
    }
}

private fun LazyListScope.renderEnhancedSubscriptionsList(
    viewModel: BudgetViewModel,
    uiState: BudgetUiState
) {
    val activeSubscriptions = uiState.subscriptions.filter { it.isActive }
    val inactiveSubscriptions = uiState.subscriptions.filter { !it.isActive }
    val totalMonthly = activeSubscriptions.sumOf { sub ->
        when (sub.billingCycle) {
            BillingCycle.DAILY -> sub.amount * 30
            BillingCycle.WEEKLY -> sub.amount * 4
            BillingCycle.MONTHLY -> sub.amount
            BillingCycle.QUARTERLY -> sub.amount / 3
            BillingCycle.YEARLY -> sub.amount / 12
        }
    }

    item {
        SubscriptionsSummaryCard(
            activeCount = activeSubscriptions.size,
            totalMonthly = totalMonthly,
            inactiveCount = inactiveSubscriptions.size
        )
    }

    if (uiState.subscriptions.isEmpty()) {
        item {
            EmptyStateCard(
                icon = Icons.Outlined.Subscriptions,
                title = "No Subscriptions",
                message = "Add your first subscription to track recurring payments",
                actionText = "Add Subscription",
                onAction = { viewModel.showAddSubscriptionDialog() }
            )
        }
    } else {
        if (activeSubscriptions.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Active Subscriptions",
                    count = activeSubscriptions.size
                )
            }

            items(activeSubscriptions, key = { it.id }) { subscription ->
                EnhancedSubscriptionCard(
                    subscription = subscription,
                    onEdit = { viewModel.showEditSubscriptionDialog(subscription) },
                    onDelete = { viewModel.deleteSubscription(subscription) },
                    onToggle = { viewModel.toggleSubscriptionActive(subscription) }
                )
            }
        }

        if (inactiveSubscriptions.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Paused Subscriptions",
                    count = inactiveSubscriptions.size
                )
            }

            items(inactiveSubscriptions, key = { it.id }) { subscription ->
                EnhancedSubscriptionCard(
                    subscription = subscription,
                    onEdit = { viewModel.showEditSubscriptionDialog(subscription) },
                    onDelete = { viewModel.deleteSubscription(subscription) },
                    onToggle = { viewModel.toggleSubscriptionActive(subscription) }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionsSummaryCard(
    activeCount: Int,
    totalMonthly: Double,
    inactiveCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SavingsBlue
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Monthly Total",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    Text(
                        "$activeCount active • $inactiveCount paused",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }

                Text(
                    CurrencyFormatter.format(totalMonthly),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 36.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptyStateCard(
    icon: ImageVector,
    title: String,
    message: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
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
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                modifier = Modifier.size(80.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = null,
                        modifier = Modifier.size(40.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            if (actionText != null && onAction != null) {
                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onAction,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(actionText)
                }
            }
        }
    }
}

@Composable
private fun EnhancedSubscriptionCard(
    subscription: Subscription,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val daysUntil = ((subscription.nextBillingDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
    val isExpiringSoon = daysUntil in 0..3 && subscription.isActive
    val isExpired = daysUntil < 0 && subscription.isActive

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                !subscription.isActive -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                isExpired -> ExpenseRed.copy(alpha = 0.05f)
                isExpiringSoon -> Color(0xFFFF9800).copy(alpha = 0.08f)
                else -> MaterialTheme.colorScheme.surface
            }
        ),
        border = when {
            isExpired -> BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f))
            isExpiringSoon -> BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.3f))
            else -> null
        },
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!subscription.isActive) 0.dp else 2.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon/Avatar
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(
                        when {
                            !subscription.isActive -> Color.Gray.copy(alpha = 0.15f)
                            isExpired -> ExpenseRed.copy(alpha = 0.15f)
                            isExpiringSoon -> Color(0xFFFF9800).copy(alpha = 0.15f)
                            else -> SavingsBlue.copy(alpha = 0.15f)
                        },
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subscription.name.take(2).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !subscription.isActive -> Color.Gray
                        isExpired -> ExpenseRed
                        isExpiringSoon -> Color(0xFFFF9800)
                        else -> SavingsBlue
                    },
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        subscription.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (!subscription.isActive) {
                        Surface(
                            color = Color.Gray.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Paused",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.Gray,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (isExpired && subscription.isActive) {
                        Surface(
                            color = ExpenseRed.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Expired",
                                style = MaterialTheme.typography.labelSmall,
                                color = ExpenseRed,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        subscription.billingCycle.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(
                                MaterialTheme.colorScheme.onSurfaceVariant,
                                CircleShape
                            )
                    )

                    Text(
                        subscription.category,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (subscription.isActive) {
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Outlined.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = when {
                                isExpired -> ExpenseRed
                                daysUntil <= 3 -> ExpenseRed
                                daysUntil <= 7 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )

                        Text(
                            text = when {
                                daysUntil < 0 -> "Expired ${-daysUntil} days ago"
                                daysUntil == 0 -> "Expires today!"
                                daysUntil == 1 -> "Expires tomorrow"
                                daysUntil <= 7 -> "Expires in $daysUntil days"
                                else -> "Next: ${DateUtils.formatDate(subscription.nextBillingDate)}"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = when {
                                isExpired -> ExpenseRed
                                daysUntil <= 3 -> ExpenseRed
                                daysUntil <= 7 -> Color(0xFFFF9800)
                                else -> MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Amount and Actions
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = CurrencyFormatter.format(subscription.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        !subscription.isActive -> Color.Gray
                        isExpired -> ExpenseRed
                        isExpiringSoon -> Color(0xFFFF9800)
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "Delete",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Switch(
                        checked = subscription.isActive,
                        onCheckedChange = { onToggle() },
                        modifier = Modifier.height(24.dp),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color.Gray.copy(alpha = 0.3f)
                        )
                    )
                }
            }
        }
    }
}

private fun LazyListScope.renderEnhancedRemindersList(
    viewModel: BudgetViewModel,
    uiState: BudgetUiState
) {
    val reminders = viewModel.getSubscriptionReminders()

    item {
        RemindersHeader(reminderCount = reminders.size)
    }

    if (reminders.isEmpty()) {
        item {
            EmptyStateCard(
                icon = Icons.Outlined.NotificationsOff,
                title = "All Clear!",
                message = "No upcoming reminders. You're all caught up!"
            )
        }
    } else {
        itemsIndexed(reminders) { index, pair ->
            val (subscription, daysLeft) = pair
            EnhancedReminderCard(
                subscription = subscription,
                daysLeft = daysLeft,
                index = index
            )
        }
    }
}

@Composable
private fun RemindersHeader(reminderCount: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {
        Text(
            "Upcoming Reminders",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        if (reminderCount > 0) {
            Text(
                "$reminderCount subscription${if (reminderCount > 1) "s" else ""} need attention",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EnhancedReminderCard(
    subscription: Subscription,
    daysLeft: Int,
    index: Int
) {
    val reminderColor = when {
        daysLeft <= 0 -> ExpenseRed
        daysLeft <= 3 -> Color(0xFFFF9800)
        else -> SavingsBlue
    }

    val statusText = when (daysLeft) {
        0 -> "Expires today!"
        1 -> "Renews tomorrow"
        2 -> "Renews in 2 days"
        3 -> "Renews in 3 days"
        in 4..7 -> "Renews in $daysLeft days"
        else -> "Renews in $daysLeft days"
    }

    val icon = when {
        daysLeft <= 0 -> Icons.Default.Warning
        daysLeft <= 3 -> Icons.Default.Schedule
        else -> Icons.Default.Notifications
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = reminderColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, reminderColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(50.dp)
                    .background(
                        reminderColor,
                        RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Icon
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = reminderColor.copy(alpha = 0.15f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = reminderColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        statusText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = reminderColor,
                        fontWeight = FontWeight.Medium
                    )

                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .background(reminderColor, CircleShape)
                    )

                    Text(
                        subscription.billingCycle.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Amount
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(subscription.amount),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = reminderColor
                )

                if (daysLeft <= 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = ExpenseRed.copy(alpha = 0.15f)
                    ) {
                        Text(
                            "OVERDUE",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExpenseRed,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedBudgetDialog(
    currentBudget: Double,
    currentThreshold: Int,
    onDismiss: () -> Unit,
    onSave: (Double, Int, Boolean) -> Unit
) {
    var amount by remember { mutableStateOf(if (currentBudget > 0) currentBudget.toString() else "") }
    var threshold by remember { mutableIntStateOf(currentThreshold) }
    var enableRollover by remember { mutableStateOf(false) }

    val budgetAmount = amount.toDoubleOrNull() ?: 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "Set Budget",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // Budget Amount Input
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly Budget") },
                    placeholder = { Text("Enter amount") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AttachMoney,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Quick Amount Suggestions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5000.0, 10000.0, 20000.0, 50000.0).forEach { suggestion ->
                        SuggestionChip(
                            amount = suggestion,
                            isSelected = budgetAmount == suggestion,
                            onClick = { amount = suggestion.toString() }
                        )
                    }
                }

                // Alert Threshold
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Outlined.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = Color(0xFFFF9800)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Alert Threshold",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFFF9800).copy(alpha = 0.15f)
                        ) {
                            Text(
                                "$threshold%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFF9800),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = threshold.toFloat(),
                        onValueChange = { threshold = it.toInt() },
                        valueRange = 50f..100f,
                        steps = 9,
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF9800),
                            activeTrackColor = Color(0xFFFF9800),
                            inactiveTrackColor = Color(0xFFFF9800).copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("50%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("75%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("100%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                // Rollover Option
                Card(
                    shape = RoundedCornerShape(16.dp),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Outlined.Refresh,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    "Enable Rollover",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    "Carry over remaining budget to next month",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Switch(
                            checked = enableRollover,
                            onCheckedChange = { enableRollover = it }
                        )
                    }
                }

                // Preview
                if (budgetAmount > 0) {
                    BudgetPreviewCard(
                        amount = budgetAmount,
                        threshold = threshold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let {
                        onSave(it, threshold, enableRollover)
                    }
                },
                enabled = budgetAmount > 0,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(52.dp)
            ) {
                Text(
                    "Save Budget",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    )
}

@Composable
private fun SuggestionChip(
    amount: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(52.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected)
            MaterialTheme.colorScheme.primary
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = if (isSelected) null else BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Text(
            CurrencyFormatter.format(amount),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected)
                MaterialTheme.colorScheme.onPrimary
            else
                MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

@Composable
private fun BudgetPreviewCard(
    amount: Double,
    threshold: Int
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                "Preview",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Alert at",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        CurrencyFormatter.format(amount * threshold / 100),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF9800)
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "Monthly Budget",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        CurrencyFormatter.format(amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedSubscriptionDialog(
    subscription: Subscription?,
    onDismiss: () -> Unit,
    onSave: (String, Double, BillingCycle, Long, String, Boolean) -> Unit
) {
    var name by remember { mutableStateOf(subscription?.name ?: "") }
    var amount by remember { mutableStateOf(subscription?.amount?.toString() ?: "") }
    var billingCycle by remember { mutableStateOf(subscription?.billingCycle ?: BillingCycle.MONTHLY) }
    var nextBillingDate by remember { mutableStateOf(subscription?.nextBillingDate ?: (System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000)) }
    var category by remember { mutableStateOf(subscription?.category ?: "Entertainment") }
    var isActive by remember { mutableStateOf(subscription?.isActive ?: true) }
    var showCyclePicker by remember { mutableStateOf(false) }

    val categories = listOf(
        "Entertainment",
        "Productivity",
        "Health & Fitness",
        "Education",
        "Music",
        "Cloud Storage",
        "Shopping",
        "Other"
    )
    var showCategoryPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (subscription == null) Icons.Outlined.Add else Icons.Outlined.Edit,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    if (subscription == null) "Add Subscription" else "Edit Subscription",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subscription Name") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.Subscriptions,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Amount
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    leadingIcon = {
                        Icon(
                            Icons.Outlined.AttachMoney,
                            contentDescription = null
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary
                    )
                )

                // Billing Cycle
                ExposedDropdownMenuBox(
                    expanded = showCyclePicker,
                    onExpandedChange = { showCyclePicker = !showCyclePicker }
                ) {
                    OutlinedTextField(
                        value = billingCycle.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Repeat,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCyclePicker)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = showCyclePicker,
                        onDismissRequest = { showCyclePicker = false }
                    ) {
                        BillingCycle.entries.forEach { cycle ->
                            DropdownMenuItem(
                                text = { Text(cycle.displayName) },
                                onClick = {
                                    billingCycle = cycle
                                    showCyclePicker = false
                                }
                            )
                        }
                    }
                }

                // Category
                ExposedDropdownMenuBox(
                    expanded = showCategoryPicker,
                    onExpandedChange = { showCategoryPicker = !showCategoryPicker }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        leadingIcon = {
                            Icon(
                                Icons.Outlined.Category,
                                contentDescription = null
                            )
                        },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryPicker)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    ExposedDropdownMenu(
                        expanded = showCategoryPicker,
                        onDismissRequest = { showCategoryPicker = false }
                    ) {
                        categories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    showCategoryPicker = false
                                }
                            )
                        }
                    }
                }

                // Active Toggle
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                if (isActive) Icons.Outlined.CheckCircle else Icons.Outlined.PauseCircle,
                                contentDescription = null,
                                tint = if (isActive) IncomeGreen else Color.Gray
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Active",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Switch(
                            checked = isActive,
                            onCheckedChange = { isActive = it }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    amount.toDoubleOrNull()?.let {
                        onSave(name, it, billingCycle, nextBillingDate, category, isActive)
                    }
                },
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .height(52.dp)
            ) {
                Text(
                    if (subscription == null) "Add Subscription" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Cancel",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    )
}