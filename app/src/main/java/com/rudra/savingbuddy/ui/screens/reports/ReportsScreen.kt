package com.rudra.savingbuddy.ui.screens.reports

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.ui.components.BarChart
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Reports & Savings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "This Month",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            label = "Income",
                            amount = uiState.totalIncome,
                            color = IncomeGreen
                        )
                        StatItem(
                            label = "Expenses",
                            amount = uiState.totalExpenses,
                            color = ExpenseRed
                        )
                        StatItem(
                            label = "Savings",
                            amount = uiState.totalSavings,
                            color = SavingsBlue
                        )
                    }
                }
            }
        }

        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Savings Rate",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${uiState.savingsRate.toInt()}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (uiState.savingsRate >= 20) IncomeGreen else ExpenseRed
                        )
                        Text(
                            text = when {
                                uiState.savingsRate >= 50 -> "Excellent! 🎉"
                                uiState.savingsRate >= 20 -> "Good"
                                uiState.savingsRate >= 0 -> "Needs improvement"
                                else -> "Overspending!"
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (uiState.savingsRate >= 20) IncomeGreen else ExpenseRed
                        )
                    }
                }
            }
        }

        if (uiState.monthlyData.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Monthly Trend",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        val chartData = uiState.monthlyData.map { 
                            Pair(it.month.take(3), it.income)
                        }
                        BarChart(
                            data = chartData,
                            modifier = Modifier.fillMaxWidth(),
                            barColor = IncomeGreen
                        )
                    }
                }
            }
        }

        if (uiState.categoryBreakdown.isNotEmpty()) {
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Category Breakdown",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        uiState.categoryBreakdown.forEach { item ->
                            CategoryBreakdownRow(
                                category = item.category,
                                amount = item.amount,
                                percentage = item.percentage
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    amount: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = CurrencyFormatter.format(amount),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun CategoryBreakdownRow(
    category: String,
    amount: Double,
    percentage: Double
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = category,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = CurrencyFormatter.format(amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100).toFloat() },
            modifier = Modifier.fillMaxWidth(),
            color = getCategoryColor(category)
        )
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun getCategoryColor(category: String): androidx.compose.ui.graphics.Color {
    return when (category) {
        "FOOD" -> FoodColor
        "TRANSPORT" -> TransportColor
        "BILLS" -> BillsColor
        "SHOPPING" -> ShoppingColor
        else -> OthersColor
    }
}