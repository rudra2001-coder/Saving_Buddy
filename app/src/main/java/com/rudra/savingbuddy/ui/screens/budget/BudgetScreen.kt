package com.rudra.savingbuddy.ui.screens.budget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
        animationSpec = tween(800),
        label = "budgetProgress"
    )

    val tabs = listOf("Budget", "Subscriptions", "Reminders")
    val tabIcons = listOf(Icons.Outlined.PieChart, Icons.Outlined.Subscriptions, Icons.Outlined.Notifications)

    Scaffold(
        topBar = {
            SimpleTopBar()
        },
        floatingActionButton = {
            SimpleFab(
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
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            item {
                SimpleTabRow(
                    selectedTab = selectedTab,
                    tabs = tabs,
                    tabIcons = tabIcons,
                    onTabSelected = { selectedTab = it }
                )
            }

            when (selectedTab) {
                0 -> renderBudgetOverview(uiState, animatedProgress)
                1 -> renderSubscriptionsList(viewModel, uiState)
                2 -> renderRemindersList(viewModel, uiState)
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (uiState.showEditBudgetDialog) {
        SimpleBudgetDialog(
            currentBudget = uiState.totalBudget,
            currentThreshold = uiState.alertThreshold,
            onDismiss = { viewModel.hideBudgetDialog() },
            onSave = { amount, threshold, rollover ->
                viewModel.setBudget(amount, threshold, rollover)
            }
        )
    }

    if (uiState.showAddSubscriptionDialog) {
        SimpleSubscriptionDialog(
            subscription = uiState.editingSubscription,
            onDismiss = { viewModel.hideSubscriptionDialog() },
            onSave = { name, amount, cycle, date, category, isActive ->
                viewModel.saveSubscription(name, amount, cycle, date, category, isActive)
            }
        )
    }

    uiState.subscriptionToDelete?.let { subscription ->
        DeleteConfirmationDialog(
            title = "Delete Subscription",
            message = "Are you sure you want to delete \"${subscription.name}\"? This action cannot be undone.",
            onConfirm = { viewModel.confirmDelete() },
            onDismiss = { viewModel.hideDeleteConfirmation() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    "Budget",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "& Subscriptions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Composable
private fun SimpleTabRow(
    selectedTab: Int,
    tabs: List<String>,
    tabIcons: List<ImageVector>,
    onTabSelected: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
                TabButton(
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
private fun TabButton(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                title,
                style = MaterialTheme.typography.labelMedium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun SimpleFab(
    selectedTab: Int,
    onBudgetClick: () -> Unit,
    onSubscriptionClick: () -> Unit
) {
    FloatingActionButton(
        onClick = if (selectedTab == 0) onBudgetClick else onSubscriptionClick,
        containerColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(20.dp)
    ) {
        Icon(
            if (selectedTab == 0) Icons.Default.Edit else Icons.Default.Add,
            contentDescription = if (selectedTab == 0) "Edit Budget" else "Add Subscription"
        )
    }
}

private fun LazyListScope.renderBudgetOverview(uiState: BudgetUiState, animatedProgress: Float) {
    val progressPercentage = (animatedProgress * 100).toInt()
    val isOverBudget = uiState.spent > uiState.totalBudget
    val isNearThreshold = progressPercentage >= uiState.alertThreshold && uiState.totalBudget > 0

    item {
        SimpleBudgetCard(
            totalBudget = uiState.totalBudget,
            spent = uiState.spent,
            remaining = uiState.remaining,
            progressPercentage = progressPercentage,
            isOverBudget = isOverBudget,
            isNearThreshold = isNearThreshold,
            animatedProgress = animatedProgress
        )
    }

    if (uiState.upcomingRenewals.isNotEmpty()) {
        item {
            UpcomingRenewalsCard(renewals = uiState.upcomingRenewals)
        }
    }
}

@Composable
private fun SimpleBudgetCard(
    totalBudget: Double,
    spent: Double,
    remaining: Double,
    progressPercentage: Int,
    isOverBudget: Boolean,
    isNearThreshold: Boolean,
    animatedProgress: Float
) {
    val accentColor = when {
        isOverBudget -> ExpenseRed
        isNearThreshold -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.primary
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Monthly Budget",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        if (isOverBudget) "Over Budget" else "$progressPercentage% used",
                        style = MaterialTheme.typography.bodySmall,
                        color = accentColor
                    )
                }
                if (isOverBudget) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ExpenseRed.copy(alpha = 0.1f)
                    ) {
                        Text(
                            "OVER",
                            style = MaterialTheme.typography.labelSmall,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                CurrencyFormatter.format(totalBudget),
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold,
                color = accentColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { animatedProgress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = accentColor,
                trackColor = accentColor.copy(alpha = 0.2f),
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatColumn(label = "Spent", value = CurrencyFormatter.formatCompact(spent), color = ExpenseRed)
                StatColumn(label = "Remaining", value = CurrencyFormatter.formatCompact(remaining), color = IncomeGreen)
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: String, color: Color) {
    Column {
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun UpcomingRenewalsCard(renewals: List<Subscription>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFF9800).copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, Color(0xFFFF9800).copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Upcoming Renewals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFFF9800)
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    "${renewals.size}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            renewals.take(3).forEach { subscription ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        subscription.name,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        CurrencyFormatter.format(subscription.amount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = ExpenseRed
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

private fun LazyListScope.renderSubscriptionsList(
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
        SubscriptionSummaryCard(
            activeCount = activeSubscriptions.size,
            totalMonthly = totalMonthly
        )
    }

    if (uiState.subscriptions.isEmpty()) {
        item {
            EmptySubscriptionsCard(onAddClick = { viewModel.showAddSubscriptionDialog() })
        }
    } else {
        if (activeSubscriptions.isNotEmpty()) {
            item {
                SectionHeader(title = "Active", count = activeSubscriptions.size)
            }
            items(activeSubscriptions, key = { it.id }) { subscription ->
                SubscriptionCard(
                    subscription = subscription,
                    onEdit = { viewModel.showEditSubscriptionDialog(subscription) },
                    onDelete = { viewModel.showDeleteConfirmation(subscription) },
                    onToggle = { viewModel.toggleSubscriptionActive(subscription) }
                )
            }
        }

        if (inactiveSubscriptions.isNotEmpty()) {
            item {
                SectionHeader(title = "Paused", count = inactiveSubscriptions.size)
            }
            items(inactiveSubscriptions, key = { it.id }) { subscription ->
                SubscriptionCard(
                    subscription = subscription,
                    onEdit = { viewModel.showEditSubscriptionDialog(subscription) },
                    onDelete = { viewModel.showDeleteConfirmation(subscription) },
                    onToggle = { viewModel.toggleSubscriptionActive(subscription) }
                )
            }
        }
    }
}

@Composable
private fun SubscriptionSummaryCard(activeCount: Int, totalMonthly: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = SavingsBlue
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Monthly Total",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    "$activeCount active",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
            Text(
                CurrencyFormatter.format(totalMonthly),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Text(
                "$count",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun EmptySubscriptionsCard(onAddClick: () -> Unit) {
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
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Outlined.Subscriptions,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "No Subscriptions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Add your first subscription",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onAddClick,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Subscription")
            }
        }
    }
}

@Composable
private fun SubscriptionCard(
    subscription: Subscription,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggle: () -> Unit
) {
    val daysUntil = ((subscription.nextBillingDate - System.currentTimeMillis()) / (24 * 60 * 60 * 1000L)).toInt()
    val isExpiringSoon = daysUntil in 0..3 && subscription.isActive
    val isExpired = daysUntil < 0 && subscription.isActive

    val accentColor = when {
        !subscription.isActive -> Color.Gray
        isExpired -> ExpenseRed
        isExpiringSoon -> Color(0xFFFF9800)
        else -> SavingsBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!subscription.isActive) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (!subscription.isActive) 0.dp else 4.dp
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = subscription.name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        subscription.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (!subscription.isActive) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Paused",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray
                        )
                    }
                }
                Text(
                    "${subscription.billingCycle.displayName} • ${subscription.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (subscription.isActive) {
                    val daysText = when {
                        daysUntil < 0 -> "Overdue"
                        daysUntil == 0 -> "Today"
                        daysUntil == 1 -> "Tomorrow"
                        else -> "in $daysUntil days"
                    }
                    Text(
                        "Next: $daysText",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(subscription.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Row {
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
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
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

private fun LazyListScope.renderRemindersList(
    viewModel: BudgetViewModel,
    uiState: BudgetUiState
) {
    val reminders = viewModel.getSubscriptionReminders()

    item {
        RemindersHeader(count = reminders.size)
    }

    if (reminders.isEmpty()) {
        item {
            EmptyRemindersCard()
        }
    } else {
        itemsIndexed(reminders) { _, pair ->
            val (subscription, daysLeft) = pair
            ReminderCard(subscription = subscription, daysLeft = daysLeft)
        }
    }
}

@Composable
private fun RemindersHeader(count: Int) {
    Column {
        Text(
            "Upcoming Reminders",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        if (count > 0) {
            Text(
                "$count subscription${if (count > 1) "s" else ""} need attention",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EmptyRemindersCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = IncomeGreen.copy(alpha = 0.1f)
        ),
        border = BorderStroke(1.dp, IncomeGreen.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = IncomeGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "All Clear!",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = IncomeGreen
            )
            Text(
                "No upcoming reminders",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReminderCard(subscription: Subscription, daysLeft: Int) {
    val reminderColor = when {
        daysLeft <= 0 -> ExpenseRed
        daysLeft <= 3 -> Color(0xFFFF9800)
        else -> SavingsBlue
    }

    val statusText = when (daysLeft) {
        0 -> "Due today!"
        1 -> "Due tomorrow"
        else -> "Due in $daysLeft days"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
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
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(reminderColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (daysLeft <= 0) Icons.Default.Warning else Icons.Default.Notifications,
                    contentDescription = null,
                    tint = reminderColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    subscription.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    statusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = reminderColor,
                    fontWeight = FontWeight.Medium
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.format(subscription.amount),
                    style = MaterialTheme.typography.titleMedium,
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

@Composable
private fun DeleteConfirmationDialog(
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                Icons.Default.Delete,
                contentDescription = null,
                tint = ExpenseRed
            )
        },
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = { Text(message) },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = ExpenseRed)
            ) {
                Text("Delete")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleBudgetDialog(
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Text("Set Budget", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Monthly Budget") },
                    leadingIcon = { Icon(Icons.Outlined.AttachMoney, null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Text("Alert at $threshold%", style = MaterialTheme.typography.labelMedium)
                Slider(
                    value = threshold.toFloat(),
                    onValueChange = { threshold = it.toInt() },
                    valueRange = 50f..100f,
                    steps = 9,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable Rollover", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = enableRollover, onCheckedChange = { enableRollover = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { amount.toDoubleOrNull()?.let { onSave(it, threshold, enableRollover) } },
                enabled = budgetAmount > 0,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleSubscriptionDialog(
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

    val categories = listOf("Entertainment", "Productivity", "Health & Fitness", "Education", "Music", "Cloud Storage", "Shopping", "Other")
    var showCategoryPicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                if (subscription == null) "Add Subscription" else "Edit Subscription",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = showCyclePicker,
                    onExpandedChange = { showCyclePicker = !showCyclePicker }
                ) {
                    OutlinedTextField(
                        value = billingCycle.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Billing Cycle") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCyclePicker) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
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

                ExposedDropdownMenuBox(
                    expanded = showCategoryPicker,
                    onExpandedChange = { showCategoryPicker = !showCategoryPicker }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryPicker) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(16.dp)
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Active", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = isActive, onCheckedChange = { isActive = it })
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
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (subscription == null) "Add" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}