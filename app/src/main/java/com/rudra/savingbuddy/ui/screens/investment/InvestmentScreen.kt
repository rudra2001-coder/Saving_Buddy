package com.rudra.savingbuddy.ui.screens.investment

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

data class Investment(
    val id: Int,
    val name: String,
    val type: InvestmentType,
    val amount: Double,
    val returns: Double,
    val returnPercentage: Double,
    val icon: ImageVector,
    val color: Color
)

enum class InvestmentType(val displayName: String) {
    STOCK("Stocks"),
    MUTUAL_FUND("Mutual Funds"),
    FIXED_DEPOSIT("Fixed Deposit"),
    GOLD("Gold"),
    CRYPTO("Crypto"),
    REAL_ESTATE("Real Estate"),
    SAVINGS("Savings"),
    BONDS("Bonds")
}

data class NetWorthData(
    val totalAssets: Double,
    val totalLiabilities: Double,
    val netWorth: Double,
    val monthlyChange: Double
)

private val sampleInvestments = emptyList<Investment>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    onNavigateBack: () -> Unit = {}
) {
    var selectedType by remember { mutableStateOf<InvestmentType?>(null) }

    val filteredInvestments = remember(selectedType) {
        if (selectedType == null) sampleInvestments
        else sampleInvestments.filter { it.type == selectedType }
    }

    val totalInvested = sampleInvestments.sumOf { it.amount }
    val totalReturns = sampleInvestments.sumOf { it.returns }
    val totalValue = totalInvested + totalReturns
    val overallReturnRate = if (totalInvested > 0) (totalReturns / totalInvested) * 100 else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Investment Tracker", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                PortfolioSummaryCard(
                    totalInvested = totalInvested,
                    totalReturns = totalReturns,
                    totalValue = totalValue,
                    returnRate = overallReturnRate
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filter by Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = selectedType == null,
                                    onClick = { selectedType = null },
                                    label = { Text("All") }
                                )
                            }
                            items(InvestmentType.entries) { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    label = { Text(type.displayName) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccountBalance, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Net Worth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        val netWorth = NetWorthData(
                            totalAssets = totalValue,
                            totalLiabilities = 0.0,
                            netWorth = totalValue,
                            monthlyChange = 0.0
                        )
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            NetWorthItem("Total Assets", CurrencyFormatter.format(netWorth.totalAssets), PrimaryGreen)
                            NetWorthItem("Liabilities", CurrencyFormatter.format(netWorth.totalLiabilities), ExpenseRed)
                            NetWorthItem("Net Worth", CurrencyFormatter.format(netWorth.netWorth), SavingsBlue)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                            Icon(if (netWorth.monthlyChange >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown, null,
                                tint = if (netWorth.monthlyChange >= 0) PrimaryGreen else ExpenseRed, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${if (netWorth.monthlyChange >= 0) "+" else ""}${CurrencyFormatter.format(netWorth.monthlyChange)} this month",
                                style = MaterialTheme.typography.bodySmall, color = if (netWorth.monthlyChange >= 0) PrimaryGreen else ExpenseRed)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.PieChart, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Investment Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${sampleInvestments.size} investments", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        sampleInvestments.forEach { inv ->
                            InvestmentRow(investment = inv)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun PortfolioSummaryCard(totalInvested: Double, totalReturns: Double, totalValue: Double, returnRate: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryGreen.copy(alpha = 0.3f),
                            SavingsBlue.copy(alpha = 0.2f),
                            BackgroundCard
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Portfolio Value", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(CurrencyFormatter.format(totalValue), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = if (totalReturns >= 0) PrimaryGreen.copy(alpha = 0.2f) else ExpenseRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (totalReturns >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null,
                            tint = if (totalReturns >= 0) PrimaryGreen else ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${if (totalReturns >= 0) "+" else ""}${CurrencyFormatter.format(totalReturns)} (${String.format("%.1f", returnRate)}%)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalReturns >= 0) PrimaryGreen else ExpenseRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Invested", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(CurrencyFormatter.format(totalInvested), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Returns", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(CurrencyFormatter.format(totalReturns), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (totalReturns >= 0) PrimaryGreen else ExpenseRed)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ROI", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${String.format("%.1f", returnRate)}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (returnRate >= 0) PrimaryGreen else ExpenseRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentRow(investment: Investment) {
    val isPositive = investment.returns >= 0
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(BackgroundCard).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape).background(investment.color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(investment.icon, null, tint = investment.color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(investment.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
            Text(investment.type.displayName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(CurrencyFormatter.format(investment.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                    null,
                    tint = if (isPositive) PrimaryGreen else ExpenseRed,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "${if (isPositive) "+" else ""}${String.format("%.1f", investment.returnPercentage)}%",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isPositive) PrimaryGreen else ExpenseRed
                )
            }
        }
    }
}

@Composable
private fun NetWorthItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    }
}
