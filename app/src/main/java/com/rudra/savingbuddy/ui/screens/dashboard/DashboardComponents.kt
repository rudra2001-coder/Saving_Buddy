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
import androidx.compose.material.icons.outlined.ShowChart
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
import androidx.compose.ui.graphics.StrokeCap
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

// ─────────────────────────────────────────────────────────────────────────────
// COLOR PALETTE  (Using tokens from DashboardScreen.kt)
// ─────────────────────────────────────────────────────────────────────────────

private val Warning  = Color(0xFFEF9F27)

// ─────────────────────────────────────────────────────────────────────────────
// SAVINGS GOAL CARD
// ─────────────────────────────────────────────────────────────────────────────

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

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(Blue50),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Flag,
                            contentDescription = null,
                            tint = Blue600,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    color = Green50,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = "${(animatedProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Green600,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ── Progress bar ──
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(99.dp)),
                color = Green600,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // ── Stats row ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                GoalStat(label = "Saved",     value = CurrencyFormatter.formatBDT(goal.currentAmount), color = Green600)
                GoalStat(label = "Target",    value = CurrencyFormatter.formatBDT(goal.targetAmount),  color = MaterialTheme.colorScheme.onSurface)
                GoalStat(label = "Days left", value = "${goal.daysRemaining}",                          color = Blue600)
            }
        }
    }
}

@Composable
private fun GoalStat(label: String, value: String, color: Color) {
    Column {
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

// ─────────────────────────────────────────────────────────────────────────────
// UPCOMING BILLS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun UpcomingBillsCard(
    bills: List<BillReminder>,
    onBillClick: (BillReminder) -> Unit,
    modifier: Modifier = Modifier,
    onViewCalendarClick: (() -> Unit)? = null
) {
    if (bills.isEmpty()) return

    val today              = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val daysUntilNextBill  = bills.firstOrNull()?.let {
        val d = it.billingDay - today
        if (d < 0) d + 30 else d
    } ?: 0
    val totalDue = bills.sumOf { it.amount }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Red50.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // ── Header ──
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Red600.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Red600,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Upcoming Bills",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${bills.size} bill${if (bills.size > 1) "s" else ""} due",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (daysUntilNextBill <= 3) {
                    Surface(
                        color = Red600,
                        shape = RoundedCornerShape(99.dp)
                    ) {
                        Text(
                            text = if (daysUntilNextBill == 0) "Today!" else "Due in $daysUntilNextBill days",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            bills.take(3).forEachIndexed { idx, bill ->
                BillRow(bill = bill, today = today, onClick = { onBillClick(bill) })
                if (idx < minOf(bills.size - 1, 2)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 10.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${CurrencyFormatter.formatBDT(totalDue)} total due",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (onViewCalendarClick != null) {
                    TextButton(
                        onClick = onViewCalendarClick,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.Default.CalendarMonth, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View calendar", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun BillRow(bill: BillReminder, today: Int, onClick: () -> Unit) {
    val dueIn        = bill.billingDay - today
    val displayDue   = if (dueIn < 0) dueIn + 30 else dueIn
    val statusColor by animateColorAsState(
        targetValue = when {
            displayDue <= 0 -> Red600
            displayDue <= 3 -> Warning
            else            -> Blue600
        },
        label = "bill_status"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
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
        }
        Text(
            text = CurrencyFormatter.formatBDT(bill.amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// MONTHLY TREND CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MonthlyTrendCard(
    trend: List<Double>,
    monthLabels: List<String> = emptyList(),
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (trend.isEmpty()) return

    val lastValue   = trend.lastOrNull() ?: 0.0
    val isPositive  = lastValue >= 0
    val trendColor  = if (isPositive) Green600 else Red600
    val trendBadge  = if (isPositive) Green50 else Red50

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = trendColor,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Monthly savings trend",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = trendBadge,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = "${trend.size} months",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = trendColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            SparklineChart(
                data = trend,
                lineColor = trendColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            )

            if (monthLabels.size == trend.size) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    monthLabels.forEach { label ->
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

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
                    text = "Current: ${CurrencyFormatter.formatCompact(lastValue)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = trendColor
                )
            }
        }
    }
}

@Composable
private fun SparklineChart(
    data: List<Double>,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOrNull() ?: 1.0
    val minValue = data.minOrNull() ?: 0.0
    val range    = (maxValue - minValue).coerceAtLeast(1.0)

    Canvas(modifier = modifier) {
        val stepX = if (data.size > 1) size.width / (data.size - 1) else size.width

        val points = data.mapIndexed { index, value ->
            val x = index * stepX
            val y = if (range > 0)
                size.height - ((value - minValue) / range * (size.height - 8f)).toFloat() - 4f
            else
                size.height / 2f
            Offset(x, y)
        }

        // Fill area
        val fillPath = Path().apply {
            moveTo(0f, size.height)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(size.width, size.height)
            close()
        }
        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.25f), lineColor.copy(alpha = 0.03f))
            )
        )

        // Line
        val linePath = Path().apply {
            moveTo(points.first().x, points.first().y)
            points.drop(1).forEach { lineTo(it.x, it.y) }
        }
        drawPath(
            path = linePath,
            color = lineColor,
            style = Stroke(width = 2.2f, cap = StrokeCap.Round)
        )

        // Dots
        points.forEachIndexed { idx, point ->
            val isLast = idx == points.lastIndex
            drawCircle(color = lineColor, radius = if (isLast) 5f else 3.5f, center = point)
            drawCircle(color = Color.White, radius = if (isLast) 2.5f else 1.5f, center = point)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CATEGORY BREAKDOWN CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun CategoryBreakdownCard(
    categories: List<CategoryTotal>,
    onCategoryClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (categories.isEmpty()) return

    val sorted  = categories.sortedByDescending { it.total }.take(5)
    val maxTotal = sorted.maxOfOrNull { it.total }?.coerceAtLeast(1.0) ?: 1.0

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = null,
                        tint = Blue600,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Spending by category",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = Blue50,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "Top 5",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Blue600,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            sorted.forEachIndexed { idx, category ->
                CategoryBar(
                    category = category,
                    fraction = (category.total / maxTotal).toFloat(),
                    onClick = { onCategoryClick(category.category) }
                )
                if (idx < sorted.lastIndex) Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun CategoryBar(
    category: CategoryTotal,
    fraction: Float,
    onClick: () -> Unit
) {
    val color = getCategoryColor(category.category)
    val animFraction by animateFloatAsState(
        targetValue = fraction,
        animationSpec = tween(900),
        label = "cat_bar"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = category.category.replace("_", " ").lowercase().replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = CurrencyFormatter.formatCompact(category.total),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(99.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animFraction)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(99.dp))
                    .background(color)
            )
        }
    }
}

private fun getCategoryColor(category: String): Color = when (category.uppercase()) {
    "FOOD"          -> Color(0xFFFF7043)
    "TRANSPORT"     -> Color(0xFF42A5F5)
    "BILLS"         -> Color(0xFFEF9F27)
    "SHOPPING"      -> Color(0xFFAB47BC)
    "ENTERTAINMENT" -> Color(0xFFE91E63)
    "HEALTH"        -> Color(0xFF26A69A)
    "EDUCATION"     -> Color(0xFF5C6BC0)
    else            -> Color(0xFF78909C)
}

// ─────────────────────────────────────────────────────────────────────────────
// NET WORTH TREND CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NetWorthTrendCard(
    trendData: List<Pair<Long, Double>>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        if (trendData.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Outlined.ShowChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Net worth trend",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Not enough data yet",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            Column(modifier = Modifier.padding(18.dp)) {

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Net worth trend",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Summary row
                val lastValue      = trendData.lastOrNull()?.second ?: 0.0
                val firstValue     = trendData.firstOrNull()?.second ?: 0.0
                val changePercent  = if (firstValue != 0.0) ((lastValue - firstValue) / firstValue) * 100 else 0.0
                val isUp           = changePercent >= 0
                val trendColor     = if (isUp) Green600 else Red600
                val badgeBg        = if (isUp) Green50 else Red50

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Total net worth",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = CurrencyFormatter.formatBDT(lastValue),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Surface(color = badgeBg, shape = RoundedCornerShape(99.dp)) {
                                Text(
                                    text = "${if (isUp) "+" else ""}${"%.1f".format(changePercent)}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = trendColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = "Last 30 days",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                val values = trendData.map { it.second }
                SparklineChart(
                    data = values,
                    lineColor = trendColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "30 days ago · ${CurrencyFormatter.formatBDT(firstValue)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp
                    )
                    Text(
                        text = "Now · ${CurrencyFormatter.formatBDT(lastValue)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = trendColor,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ACCOUNT HEALTH CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun AccountHealthCard(
    accountHealthList: List<com.rudra.savingbuddy.domain.model.AccountHealth>,
    onAccountClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    if (accountHealthList.isEmpty()) return

    val goodCount = accountHealthList.count { it.status == HealthStatus.GOOD }
    val hasIssues = goodCount < accountHealthList.size

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = if (hasIssues) Warning else Green600,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Account health",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Surface(
                    color = if (hasIssues) Amber50 else Green50,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "$goodCount/${accountHealthList.size} Good",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (hasIssues) Amber600 else Green600,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            accountHealthList.take(4).forEachIndexed { idx, health ->
                AccountHealthRow(health = health, onClick = { onAccountClick(health.accountId) })
                if (idx < minOf(accountHealthList.size - 1, 3)) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AccountHealthRow(
    health: com.rudra.savingbuddy.domain.model.AccountHealth,
    onClick: () -> Unit
) {
    val statusColor = when (health.status) {
        HealthStatus.GOOD     -> Green600
        HealthStatus.MEDIUM   -> Warning
        HealthStatus.LOW      -> Color(0xFFFF9800)
        HealthStatus.CRITICAL -> Red600
    }
    val usagePct = if (health.dailyLimit != null && health.dailyLimit > 0)
        (health.usedToday / health.dailyLimit * 100).toInt() else 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(statusColor)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
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
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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
                Text(
                    text = "$usagePct% of limit",
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// NET WORTH CARD (compact header strip)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun NetWorthCard(
    netWorth: Double,
    totalAssets: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Net worth",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatBDT(netWorth),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (netWorth >= 0) Green600 else Red600
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Total assets",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = CurrencyFormatter.formatBDT(totalAssets),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Green600
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// ROUND-UP SAVINGS CARD
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RoundUpCard(
    totalSaved: Double,
    goalName: String?,
    goalProgress: Float,
    isEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = goalProgress,
        animationSpec = tween(900),
        label = "roundup_progress"
    )

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Green600.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Green50),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Savings,
                        contentDescription = null,
                        tint = Green600,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Round-Up Savings",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.weight(1f))
                Surface(
                    color = if (isEnabled) Green50 else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(99.dp)
                ) {
                    Text(
                        text = if (isEnabled) "Active" else "Disabled",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isEnabled) Green600 else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (isEnabled && goalName != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Total saved", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.formatBDT(totalSaved), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Green600)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(goalName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("${(animProgress * 100).toInt()}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Green600)
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { animProgress },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(99.dp)),
                    color = Green600,
                    trackColor = Green50
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Every purchase rounds up to the nearest ৳10",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else if (isEnabled) {
                Text(
                    text = "Link a savings goal in Settings to start rounding up",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total saved so far", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(CurrencyFormatter.formatBDT(totalSaved), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Text(
                        text = "Tap to enable →",
                        style = MaterialTheme.typography.bodySmall,
                        color = Green600,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
