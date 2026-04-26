package com.rudra.savingbuddy.ui.screens.reports

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(
    viewModel: AnalyticsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                PeriodSelector(
                    selectedRange = uiState.selectedRange,
                    onSelect = { viewModel.selectDateRange(it) }
                )
            }

            item {
                NetSavingsCard(
                    savings = uiState.savings,
                    totalIncome = uiState.totalIncome,
                    totalExpenses = uiState.totalExpenses,
                    savingsRate = uiState.savingsRate
                )
            }

            item {
                IncomeExpenseComparison(
                    totalIncome = uiState.totalIncome,
                    totalExpenses = uiState.totalExpenses
                )
            }

            if (uiState.expensesByCategory.isNotEmpty()) {
                item {
                    CategoryBreakdownCard(
                        categories = uiState.expensesByCategory
                    )
                }

                item {
                    CategoryPieChartCard(
                        categories = uiState.expensesByCategory,
                        totalExpenses = uiState.totalExpenses
                    )
                }
            }

            if (uiState.dailySpending.isNotEmpty()) {
                item {
                    DailySpendingTrendCard(
                        dailySpending = uiState.dailySpending,
                        title = "Daily Spending Trend"
                    )
                }
            }

            item {
                MonthlyComparisonCard(
                    dailySpending = uiState.dailySpending,
                    totalExpenses = uiState.totalExpenses
                )
            }

            item {
                InsightsCard(
                    totalIncome = uiState.totalIncome,
                    totalExpenses = uiState.totalExpenses,
                    savingsRate = uiState.savingsRate,
                    categoryCount = uiState.expensesByCategory.size
                )
            }

            item {
                StatisticsSummaryCard(
                    totalIncome = uiState.totalIncome,
                    totalExpenses = uiState.totalExpenses,
                    savings = uiState.savings,
                    categoryCount = uiState.expensesByCategory.size,
                    transactionCount = uiState.dailySpending.size
                )
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun PeriodSelector(
    selectedRange: DateRange,
    onSelect: (DateRange) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DateRange.entries.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { onSelect(range) },
                        label = { Text(range.displayName) },
                        leadingIcon = if (selectedRange == range) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
        }
    }
}

@Composable
private fun NetSavingsCard(
    savings: Double,
    totalIncome: Double,
    totalExpenses: Double,
    savingsRate: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (savings >= 0) {
                            listOf(IncomeGreen, IncomeGreen.copy(alpha = 0.7f), SavingsBlue.copy(alpha = 0.8f))
                        } else {
                            listOf(ExpenseRed, ExpenseRed.copy(alpha = 0.7f), Color(0xFFB71C1C))
                        }
                    )
                )
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Net Savings",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = CurrencyFormatter.format(savings),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                val rateColor = when {
                    savingsRate >= 50 -> Color(0xFF4CAF50)
                    savingsRate >= 20 -> Color(0xFFFFEB3B)
                    else -> Color(0xFFFF5722)
                }
                Surface(
                    color = rateColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = "Savings Rate: ${savingsRate.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatColumn(label = "Income", value = totalIncome, color = IncomeGreen)
                    StatColumn(label = "Expenses", value = totalExpenses, color = ExpenseRed)
                    StatColumn(label = "Rate", value = savingsRate, color = SavingsBlue)
                }
            }
        }
    }
}

@Composable
private fun StatColumn(label: String, value: Any, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
        Text(
            text = when (value) {
                is String -> value
                is Double -> CurrencyFormatter.formatCompact(value)
                else -> value.toString()
            },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun IncomeExpenseComparison(
    totalIncome: Double,
    totalExpenses: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Income vs Expenses",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            val total = (totalIncome + totalExpenses).coerceAtLeast(1.0)
            val incomePercent = (totalIncome / total * 100).toInt()
            val expensePercent = (totalExpenses / total * 100).toInt()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Income", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "$incomePercent%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val animatedProgress by animateFloatAsState(
                        targetValue = (incomePercent / 100f).coerceIn(0f, 1f),
                        animationSpec = tween(1000),
                        label = "income"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = IncomeGreen,
                        trackColor = IncomeGreen.copy(alpha = 0.2f)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Expenses", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "$expensePercent%",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    val animatedProgress by animateFloatAsState(
                        targetValue = (expensePercent / 100f).coerceIn(0f, 1f),
                        animationSpec = tween(1000),
                        label = "expense"
                    )
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = ExpenseRed,
                        trackColor = ExpenseRed.copy(alpha = 0.2f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ComparisonItem(
                    icon = Icons.Default.TrendingUp,
                    label = "Total Income",
                    value = totalIncome,
                    color = IncomeGreen
                )
                ComparisonItem(
                    icon = Icons.Default.TrendingDown,
                    label = "Total Expenses",
                    value = totalExpenses,
                    color = ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun ComparisonItem(
    icon: ImageVector,
    label: String,
    value: Double,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyFormatter.format(value),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CategoryBreakdownCard(
    categories: List<AnalyticsCategoryBreakdown>
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending by Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${categories.size} categories",
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            categories.take(6).forEachIndexed { index, category ->
                CategoryRow(
                    category = category.category,
                    amount = category.amount,
                    percentage = category.percentage,
                    index = index,
                    isLast = index >= categories.take(6).lastIndex
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    category: String,
    amount: Double,
    percentage: Double,
    index: Int,
    isLast: Boolean
) {
    val colors = listOf(
        Color(0xFFFF5722),
        Color(0xFF2196F3),
        Color(0xFFFFEB3B),
        Color(0xFF4CAF50),
        Color(0xFF9C27B0),
        Color(0xFF00BCD4),
        Color(0xFFFF9800),
        Color(0xFF607D8B)
    )
    val color = colors.getOrElse(index) { Color.Gray }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(color)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = category.replace("_", " "),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${percentage.toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = color
                )
            }
        }
        if (!isLast) {
            val animatedProgress by animateFloatAsState(
                targetValue = (percentage.toFloat() / 100f).coerceIn(0f, 1f),
                animationSpec = tween(800),
                label = "progress"
            )
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.15f)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun CategoryPieChartCard(
    categories: List<AnalyticsCategoryBreakdown>,
    totalExpenses: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Expense Distribution",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isNotEmpty() && totalExpenses > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        val colors = listOf(
                            Color(0xFFFF5722),
                            Color(0xFF2196F3),
                            Color(0xFFFFEB3B),
                            Color(0xFF4CAF50),
                            Color(0xFF9C27B0),
                            Color(0xFF00BCD4),
                            Color(0xFFFF9800),
                            Color(0xFF607D8B)
                        )

                        categories.take(8).forEachIndexed { index, category ->
                            val sweepAngle = (category.percentage / 100f * 360f).toFloat()
                            drawArc(
                                color = colors.getOrElse(index) { Color.Gray },
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                size = Size(size.width, size.height)
                            )
                            startAngle += sweepAngle
                        }

                        drawCircle(
                            color = Color.White,
                            radius = size.width / 4
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                categories.take(5).forEachIndexed { index, category ->
                    val colors = listOf(
                        Color(0xFFFF5722),
                        Color(0xFF2196F3),
                        Color(0xFFFFEB3B),
                        Color(0xFF4CAF50),
                        Color(0xFF9C27B0),
                        Color(0xFF00BCD4),
                        Color(0xFFFF9800),
                        Color(0xFF607D8B)
                    )
                    val color = colors.getOrElse(index) { Color.Gray }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category.category.replace("_", " "),
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${category.percentage.toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = color
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySpendingTrendCard(
    dailySpending: List<DailySpending>,
    title: String
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${dailySpending.size} days",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (dailySpending.isNotEmpty()) {
                val maxAmount = dailySpending.maxOfOrNull { it.amount } ?: 1.0

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                ) {
                    val barWidth = size.width / dailySpending.size.coerceAtLeast(1) * 0.7f
                    val spacing = size.width / dailySpending.size.coerceAtLeast(1)

                    dailySpending.takeLast(14).forEachIndexed { index, spending ->
                        val barHeight = (spending.amount / maxAmount * size.height).toFloat()
                        val x = index * spacing + spacing / 2 - barWidth / 2

                        drawRoundRect(
                            color = ExpenseRed.copy(alpha = 0.7f),
                            topLeft = Offset(x, size.height - barHeight),
                            size = Size(barWidth, barHeight)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (dailySpending.isNotEmpty()) DateUtils.formatShortDate(dailySpending.first().date) else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (dailySpending.isNotEmpty()) DateUtils.formatShortDate(dailySpending.last().date) else "",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun MonthlyComparisonCard(
    dailySpending: List<DailySpending>,
    totalExpenses: Double
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Spending Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryStat(
                    icon = Icons.Default.CalendarToday,
                    label = "Daily Avg",
                    value = if (dailySpending.isNotEmpty()) CurrencyFormatter.formatCompact(totalExpenses / dailySpending.size) else "-"
                )
                SummaryStat(
                    icon = Icons.Default.DateRange,
                    label = "Transactions",
                    value = "${dailySpending.size}"
                )
                SummaryStat(
                    icon = Icons.Default.TrendingDown,
                    label = "Highest",
                    value = if (dailySpending.isNotEmpty()) CurrencyFormatter.formatCompact(dailySpending.maxOfOrNull { it.amount } ?: 0.0) else "-"
                )
            }
        }
    }
}

@Composable
private fun SummaryStat(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun InsightsCard(
    totalIncome: Double,
    totalExpenses: Double,
    savingsRate: Double,
    categoryCount: Int
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Insights",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val insightsList = mutableListOf<InsightItem>()
            
            when {
                savingsRate >= 50 -> insightsList.add(InsightItem(Icons.Default.Stars, "Excellent savings!", "You're saving over 50% of your income", IncomeGreen))
                savingsRate >= 20 -> insightsList.add(InsightItem(Icons.Default.ThumbUp, "Good progress", "You're saving ${savingsRate.toInt()}% of income", SavingsBlue))
                savingsRate > 0 -> insightsList.add(InsightItem(Icons.Default.TrendingUp, "Room to improve", "Try to increase savings rate to 20%+", Color(0xFFFF9800)))
                else -> insightsList.add(InsightItem(Icons.Default.Warning, "Overspending", "Expenses exceed income", ExpenseRed))
            }
            if (categoryCount > 5) insightsList.add(InsightItem(Icons.Default.Category, "Diverse spending", "You have $categoryCount expense categories", MaterialTheme.colorScheme.primary))
            if (totalExpenses > totalIncome * 0.8) insightsList.add(InsightItem(Icons.Default.Lightbulb, "Budget tip", "Consider tracking daily expenses", Color(0xFF2196F3)))

            insightsList.forEach { insight ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(insight.color.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            insight.icon,
                            contentDescription = null,
                            tint = insight.color,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = insight.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = insight.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private data class InsightItem(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val color: Color
)

@Composable
private fun StatisticsSummaryCard(
    totalIncome: Double,
    totalExpenses: Double,
    savings: Double,
    categoryCount: Int,
    transactionCount: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Full Data Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Total Income", CurrencyFormatter.format(totalIncome), IncomeGreen)
                SummaryItem("Total Expenses", CurrencyFormatter.format(totalExpenses), ExpenseRed)
                SummaryItem("Net Savings", CurrencyFormatter.format(savings), SavingsBlue)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryItem("Categories", "$categoryCount", MaterialTheme.colorScheme.primary)
                SummaryItem("Transactions", "$transactionCount", MaterialTheme.colorScheme.secondary)
                SummaryItem("Savings Rate", "${(if (totalIncome > 0) (savings / totalIncome * 100).toInt() else 0)}%", 
                    if (savings >= 0) IncomeGreen else ExpenseRed)
            }
        }
    }
}

@Composable
private fun SummaryItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}