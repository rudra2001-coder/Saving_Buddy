package com.rudra.savingbuddy.ui.screens.dashboard

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rudra.savingbuddy.data.local.dao.CategoryTotal
import com.rudra.savingbuddy.domain.model.BillReminder
import com.rudra.savingbuddy.domain.model.Goal
import com.rudra.savingbuddy.domain.model.HealthStatus
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import java.util.Calendar

// ─── Color tokens ────────────────────────────────────────────────────────────

private val Blue600   = Color(0xFF185FA5)
private val Blue100   = Color(0xFFB5D4F4)
private val Blue50    = Color(0xFFE6F1FB)

private val Green600  = Color(0xFF3B6D11)
private val Green100  = Color(0xFFC0DD97)
private val Green50   = Color(0xFFEAF3DE)

private val Red600    = Color(0xFFA32D2D)
private val Red100    = Color(0xFFF7C1C1)
private val Red50     = Color(0xFFFCEBEB)

private val Amber600  = Color(0xFF854F0B)
private val Amber100  = Color(0xFFFAC775)
private val Amber50   = Color(0xFFFAEEDA)

private val Purple400 = Color(0xFF7F77DD)
private val Teal400   = Color(0xFF1D9E75)
private val Coral400  = Color(0xFFD85A30)

// ─── Reusable primitives ─────────────────────────────────────────────────────

@Composable
private fun UrgencyBadge(daysLeft: Int) {
    val (bg, fg, text) = when {
        daysLeft == 0 -> Triple(Red50, Red600, "Today!")
        daysLeft <= 3 -> Triple(Amber50, Amber600, "$daysLeft days")
        else          -> Triple(Green50, Green600, "$daysLeft days")
    }
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = text,
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = fg,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun ColorBadge(text: String, bg: Color, fg: Color) {
    Surface(color = bg, shape = RoundedCornerShape(20.dp)) {
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = fg,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun SectionCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: @Composable () -> Unit,
    badge: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            icon()
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        badge?.invoke()
    }
}

private fun getCategoryColor(category: String): Color = when (category) {
    "FOOD"          -> Coral400
    "TRANSPORT"     -> Blue600
    "BILLS"         -> Color(0xFFEF9F27)
    "SHOPPING"      -> Purple400
    "ENTERTAINMENT" -> Color(0xFFD4537E)
    "HEALTH"        -> Teal400
    "EDUCATION"     -> Color(0xFF534AB7)
    else            -> Color(0xFF888780)
}

// ─── Savings Goal Card ────────────────────────────────────────────────────────

@Composable
fun SavingsGoalCard(
    goal: Goal,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = goal.progress.toFloat(),
        animationSpec = tween(durationMillis = 1000),
        label = "progress"
    )
    val pct = (animatedProgress * 100).toInt()

    SectionCard(modifier = modifier.clickable(onClick = onClick)) {
        SectionHeader(
            title = "Savings goal",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Flag,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(18.dp)
                )
            },
            badge = { ColorBadge("$pct%", Blue50, Blue600) }
        )

        Spacer(Modifier.height(12.dp))

        Text(
            text = goal.name,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        Text(
            text = "${CurrencyFormatter.format(goal.currentAmount)} of ${CurrencyFormatter.format(goal.targetAmount)} saved",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(10.dp))

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)),
            color = Green600,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Remaining: ${CurrencyFormatter.format(goal.targetAmount - goal.currentAmount)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            ColorBadge("${goal.daysRemaining} days left", Green50, Green600)
        }
    }
}

// ─── Upcoming Bills Card ──────────────────────────────────────────────────────

@Composable
fun UpcomingBillsCard(
    bills: List<BillReminder>,
    onBillClick: (BillReminder) -> Unit,
    modifier: Modifier = Modifier
) {
    if (bills.isEmpty()) return
    val today = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    SectionCard(modifier = modifier) {
        SectionHeader(
            title = "Upcoming bills",
            icon = {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = Red600,
                    modifier = Modifier.size(18.dp)
                )
            },
            badge = {
                ColorBadge(
                    "${bills.size} due",
                    Red50,
                    Red600
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        bills.take(3).forEachIndexed { index, bill ->
            BillItem(bill = bill, today = today, onClick = { onBillClick(bill) })
            if (index < minOf(bills.size - 1, 2)) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 10.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun BillItem(
    bill: BillReminder,
    today: Int,
    onClick: () -> Unit
) {
    val dueIn = bill.billingDay - today
    val displayDueDays = if (dueIn < 0) dueIn + 30 else dueIn

    val iconBg by animateColorAsState(
        targetValue = when {
            displayDueDays <= 1 -> Red50
            displayDueDays <= 3 -> Amber50
            else -> Blue50
        },
        label = "bill_icon_bg"
    )
    val iconFg by animateColorAsState(
        targetValue = when {
            displayDueDays <= 1 -> Red600
            displayDueDays <= 3 -> Amber600
            else -> Blue600
        },
        label = "bill_icon_fg"
    )

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Receipt,
                contentDescription = null,
                tint = iconFg,
                modifier = Modifier.size(18.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = bill.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "${bill.billingCycle.displayName} · Day ${bill.billingDay}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = CurrencyFormatter.format(bill.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            UrgencyBadge(displayDueDays)
        }
    }
}

// ─── Monthly Trend Card ───────────────────────────────────────────────────────

@Composable
fun MonthlyTrendCard(
    trend: List<Double>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (trend.isEmpty()) return

    val isPositive = (trend.lastOrNull() ?: 0.0) >= 0
    val lineColor  = if (isPositive) Green600 else Red600
    val badgeBg    = if (isPositive) Green50  else Red50
    val badgeFg    = if (isPositive) Green600 else Red600

    SectionCard(modifier = modifier.clickable(onClick = onClick)) {
        SectionHeader(
            title = if (isPositive) "Savings trend" else "Expense trend",
            icon = {
                Icon(
                    imageVector = if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    contentDescription = null,
                    tint = lineColor,
                    modifier = Modifier.size(18.dp)
                )
            },
            badge = {
                ColorBadge(
                    if (isPositive) "Positive" else "Negative",
                    badgeBg, badgeFg
                )
            }
        )

        Spacer(Modifier.height(12.dp))

        SparklineChart(
            data = trend,
            lineColor = lineColor,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        )

        Spacer(Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Avg: ${CurrencyFormatter.formatCompact(trend.average())}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Current: ${CurrencyFormatter.formatCompact(trend.lastOrNull() ?: 0.0)}",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = lineColor
            )
        }
    }
}

@Composable
private fun SparklineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.size < 2) return

    val maxValue = data.maxOrNull() ?: 1.0
    val minValue = data.minOrNull() ?: 0.0
    val range    = (maxValue - minValue).coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)

        val points = data.mapIndexed { i, v ->
            Offset(
                x = i * stepX,
                y = size.height - ((v - minValue) / range * size.height).toFloat()
            )
        }

        // Fill
        val fill = Path().apply {
            moveTo(0f, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(fill, Brush.verticalGradient(listOf(lineColor.copy(.25f), lineColor.copy(.03f))))

        // Line
        val line = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(line, lineColor, style = Stroke(2.5f))

        // Dots
        points.forEach { p ->
            drawCircle(lineColor, 4f, p)
            drawCircle(Color.White, 2f, p)
        }
    }
}

// ─── Category Breakdown Card ──────────────────────────────────────────────────

@Composable
fun CategoryBreakdownCard(
    categories: List<CategoryTotal>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    val sorted = categories.sortedByDescending { it.total }.take(5)
    val maxTotal = sorted.firstOrNull()?.total ?: 1.0

    SectionCard(modifier = modifier) {
        SectionHeader(
            title = "Spending by category",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.BarChart,
                    contentDescription = null,
                    tint = Blue600,
                    modifier = Modifier.size(18.dp)
                )
            },
            badge = { ColorBadge("Top 5", Blue50, Blue600) }
        )

        Spacer(Modifier.height(14.dp))

        sorted.forEachIndexed { index, cat ->
            CategoryBarItem(
                category = cat,
                maxTotal = maxTotal,
                onClick = { onCategoryClick(cat.category) }
            )
            if (index < sorted.size - 1) Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CategoryBarItem(
    category: CategoryTotal,
    maxTotal: Double,
    onClick: () -> Unit
) {
    val color     = getCategoryColor(category.category)
    val fraction  = (category.total / maxTotal).toFloat().coerceIn(0f, 1f)
    val animated  by animateFloatAsState(fraction, tween(900), label = "cat_bar")
    val pct       = (fraction * 100).toInt()

    Column(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = category.category.replace("_", " ").lowercase()
                        .replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = CurrencyFormatter.formatCompact(category.total),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
                Text(
                    text = "$pct%",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(Modifier.height(5.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animated)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(color)
            )
        }
    }
}

// ─── Account Health Card ──────────────────────────────────────────────────────

@Composable
fun AccountHealthCard(
    accountHealthList: List<com.rudra.savingbuddy.domain.model.AccountHealth>,
    onAccountClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accountHealthList.isEmpty()) return

    val goodCount = accountHealthList.count { it.status == HealthStatus.GOOD }
    val hasIssues = goodCount < accountHealthList.size
    val headerColor = if (hasIssues) Amber600 else Green600
    val headerBg    = if (hasIssues) Amber50   else Green50

    SectionCard(
        modifier = modifier,
        containerColor = if (hasIssues)
            Amber50.copy(alpha = 0.4f)
        else
            MaterialTheme.colorScheme.surface
    ) {
        SectionHeader(
            title = "Account health",
            icon = {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = headerColor,
                    modifier = Modifier.size(18.dp)
                )
            },
            badge = {
                ColorBadge("$goodCount/${accountHealthList.size} Good", headerBg, headerColor)
            }
        )

        Spacer(Modifier.height(12.dp))

        accountHealthList.take(4).forEachIndexed { index, health ->
            AccountHealthItem(health = health, onClick = { onAccountClick(health.accountId) })
            if (index < minOf(accountHealthList.size - 1, 3)) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun AccountHealthItem(
    health: com.rudra.savingbuddy.domain.model.AccountHealth,
    onClick: () -> Unit
) {
    val (dotColor, statusLabel) = when (health.status) {
        HealthStatus.GOOD     -> Green600 to "Good"
        HealthStatus.MEDIUM   -> Color(0xFFFFC107) to "Medium"
        HealthStatus.LOW      -> Amber600 to "Low"
        HealthStatus.CRITICAL -> Red600 to "Critical"
    }

    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = health.accountName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            health.recommendation?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = CurrencyFormatter.formatBDT(health.balance),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (health.dailyLimit != null && health.dailyLimit > 0) {
                val usagePct = (health.usedToday / health.dailyLimit * 100).toInt()
                Text(
                    text = "$usagePct% of limit · $statusLabel",
                    style = MaterialTheme.typography.labelSmall,
                    color = dotColor
                )
            } else {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = dotColor
                )
            }
        }
    }
}

// ─── Net Worth Card ───────────────────────────────────────────────────────────

@Composable
fun NetWorthCard(
    netWorth: Double,
    totalAssets: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPositive = netWorth >= 0

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPositive) Green50 else Red50
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (isPositive) Green100 else Red100
        )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Net worth",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isPositive) Green600 else Red600
                )
                Text(
                    text = CurrencyFormatter.formatBDT(netWorth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) Green600 else Red600
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total assets",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isPositive) Green600 else Red600
                )
                Text(
                    text = CurrencyFormatter.formatBDT(totalAssets),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isPositive) Green600 else Red600
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = if (isPositive) Green600 else Red600
            )
        }
    }
}
