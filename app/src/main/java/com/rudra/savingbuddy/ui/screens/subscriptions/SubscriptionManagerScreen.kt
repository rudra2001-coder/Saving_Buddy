package com.rudra.savingbuddy.ui.screens.subscriptions

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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import java.text.SimpleDateFormat
import java.util.*

data class SubscriptionItem(
    val id: Int,
    val name: String,
    val amount: Double,
    val billingCycle: String,
    val nextBilling: Long,
    val category: String,
    val isActive: Boolean,
    val icon: ImageVector,
    val color: Color,
    val daysUntilNextBilling: Int
)

private val sampleSubscriptions = emptyList<SubscriptionItem>()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionManagerScreen(
    onNavigateBack: () -> Unit = {}
) {
    var showInactive by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<String?>(null) }

    val filteredSubscriptions = remember(showInactive, selectedCategory) {
        sampleSubscriptions.filter { sub ->
            (showInactive || sub.isActive) && (selectedCategory == null || sub.category == selectedCategory)
        }
    }

    val categories = sampleSubscriptions.map { it.category }.distinct()
    val monthlyTotal = sampleSubscriptions.filter { it.isActive }.let { subs ->
        subs.filter { it.billingCycle == "Monthly" }.sumOf { it.amount } +
        subs.filter { it.billingCycle == "Yearly" }.sumOf { it.amount / 12 }
    }
    val yearlyTotal = sampleSubscriptions.filter { it.isActive }.sumOf { it.amount }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Subscription Manager", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                actions = {
                    IconButton(onClick = { showInactive = !showInactive }) {
                        Icon(
                            if (showInactive) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            "Toggle inactive",
                            tint = if (showInactive) PrimaryGreen else TextSecondary
                        )
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SubscriptionSummaryCard(monthlyTotal = monthlyTotal, yearlyTotal = yearlyTotal, activeCount = sampleSubscriptions.count { it.isActive })
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filter", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(selected = selectedCategory == null, onClick = { selectedCategory = null }, label = { Text("All") })
                            }
                            items(categories) { cat ->
                                FilterChip(selected = selectedCategory == cat, onClick = { selectedCategory = cat }, label = { Text(cat) })
                            }
                        }
                    }
                }
            }

            if (filteredSubscriptions.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Outlined.Subscriptions, null, modifier = Modifier.size(64.dp), tint = TextSecondary.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No subscriptions found", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Add subscriptions to track your recurring payments", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                    }
                }
            } else {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Outlined.List, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Your Subscriptions", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Spacer(modifier = Modifier.weight(1f))
                                Text("${filteredSubscriptions.size} services", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            filteredSubscriptions.forEach { sub ->
                                SubscriptionRow(subscription = sub)
                                Spacer(modifier = Modifier.height(6.dp))
                            }
                        }
                    }
                }
            }

            if (!showInactive) {
                val dueSoon = sampleSubscriptions.filter { it.isActive && it.daysUntilNextBilling <= 7 }
                if (dueSoon.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f)),
                            border = BorderStroke(1.dp, ExpenseRed.copy(alpha = 0.3f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.NotificationsActive, null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Due Soon", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = ExpenseRed)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                dueSoon.forEach { sub ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(sub.name, style = MaterialTheme.typography.bodyMedium, color = TextPrimary)
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(color = ExpenseRed.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                                Text("${sub.daysUntilNextBilling}d", style = MaterialTheme.typography.labelSmall, color = ExpenseRed, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp))
                                            }
                                        }
                                        Text(CurrencyFormatter.format(sub.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = ExpenseRed)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SubscriptionSummaryCard(monthlyTotal: Double, yearlyTotal: Double, activeCount: Int) {
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
                            AccentPurple.copy(alpha = 0.3f),
                            PrimaryGreen.copy(alpha = 0.2f),
                            BackgroundCard
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Subscriptions, null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Subscription Overview", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f))
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Monthly", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text(CurrencyFormatter.format(monthlyTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Yearly", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text(CurrencyFormatter.format(yearlyTotal), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Active", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
                        Text("$activeCount", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionRow(subscription: SubscriptionItem) {
    val dateFormat = remember { SimpleDateFormat("MMM dd", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(BackgroundCard)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    if (subscription.isActive) subscription.color.copy(alpha = 0.2f)
                    else SurfaceMedium.copy(alpha = 0.2f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                subscription.icon,
                null,
                tint = if (subscription.isActive) subscription.color else SurfaceMedium,
                modifier = Modifier.size(22.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                subscription.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = if (subscription.isActive) TextPrimary else TextSecondary
            )
            Text(
                "${subscription.billingCycle} • ${subscription.category}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                CurrencyFormatter.format(subscription.amount),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (subscription.isActive) TextPrimary else TextSecondary
            )
            if (subscription.isActive) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dateFormat.format(Date(subscription.nextBilling)),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (subscription.daysUntilNextBilling <= 7) ExpenseRed else TextSecondary
                    )
                }
            } else {
                Text("Inactive", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
            }
        }
    }
}
