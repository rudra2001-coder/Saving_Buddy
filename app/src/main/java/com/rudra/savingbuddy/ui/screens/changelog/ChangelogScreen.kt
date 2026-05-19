package com.rudra.savingbuddy.ui.screens.changelog

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.rudra.savingbuddy.ui.theme.*

data class ChangelogEntry(
    val version: String,
    val date: String,
    val title: String,
    val changes: List<ChangeItem>
)

data class ChangeItem(
    val text: String,
    val type: ChangeType
)

enum class ChangeType { NEW, IMPROVED, FIXED, FEATURE }

private val changelog = listOf(
    ChangelogEntry(
        "1.2.0", "May 2026", "Major Feature Update",
        listOf(
            ChangeItem("Currency Converter with 14 currencies", ChangeType.NEW),
            ChangeItem("OCR Receipt Scanner", ChangeType.NEW),
            ChangeItem("Investment & Net Worth Tracker", ChangeType.NEW),
            ChangeItem("Subscription Manager", ChangeType.NEW),
            ChangeItem("Multi-language support (14 languages)", ChangeType.NEW),
            ChangeItem("Onboarding flow for new users", ChangeType.NEW),
            ChangeItem("PDF Report Generation", ChangeType.NEW),
            ChangeItem("Enhanced Analytics with Smart Insights", ChangeType.IMPROVED),
            ChangeItem("Investment Add & Tracking System", ChangeType.FEATURE),
            ChangeItem("Dark mode scheduling", ChangeType.IMPROVED),
            ChangeItem("Performance optimizations", ChangeType.IMPROVED)
        )
    ),
    ChangelogEntry(
        "1.1.0", "April 2026", "Analytics & Export Update",
        listOf(
            ChangeItem("Advanced analytics dashboard", ChangeType.NEW),
            ChangeItem("CSV/JSON/Text data export", ChangeType.NEW),
            ChangeItem("Calendar view for transactions", ChangeType.NEW),
            ChangeItem("Fusion timeline view", ChangeType.NEW),
            ChangeItem("Account health monitoring", ChangeType.NEW),
            ChangeItem("Backup & restore functionality", ChangeType.NEW),
            ChangeItem("Improved budget tracking", ChangeType.IMPROVED),
            ChangeItem("Bill payment reminders", ChangeType.IMPROVED),
            ChangeItem("UI/UX improvements", ChangeType.IMPROVED),
            ChangeItem("Bug fixes and performance", ChangeType.FIXED)
        )
    ),
    ChangelogEntry(
        "1.0.0", "March 2026", "Initial Release",
        listOf(
            ChangeItem("Income & expense tracking", ChangeType.NEW),
            ChangeItem("Budget management", ChangeType.NEW),
            ChangeItem("Savings goals", ChangeType.NEW),
            ChangeItem("Account management", ChangeType.NEW),
            ChangeItem("Recurring transactions", ChangeType.NEW),
            ChangeItem("Bill reminders", ChangeType.NEW),
            ChangeItem("Financial calculator", ChangeType.NEW),
            ChangeItem("Premium dark theme", ChangeType.NEW),
            ChangeItem("Biometric security", ChangeType.NEW),
            ChangeItem("Gamification system", ChangeType.NEW)
        )
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangelogScreen(
    onNavigateBack: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("What's New", fontWeight = FontWeight.Bold)
                        Text("Version History", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
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

            items(changelog) { entry ->
                ChangelogCard(entry)
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ChangelogCard(entry: ChangelogEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.NewReleases, contentDescription = null, tint = PrimaryGreen.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier.size(40.dp).clip(CircleShape).background(PrimaryGreen.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("v${entry.version}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("${entry.title}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        Text(entry.date, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            entry.changes.forEach { change ->
                val (bgColor, icon, tint) = when (change.type) {
                    ChangeType.NEW -> Triple(PrimaryGreen.copy(alpha = 0.1f), Icons.Default.Add, PrimaryGreen)
                    ChangeType.IMPROVED -> Triple(SavingsBlue.copy(alpha = 0.1f), Icons.Default.TrendingUp, SavingsBlue)
                    ChangeType.FIXED -> Triple(WarningOrange.copy(alpha = 0.1f), Icons.Default.BugReport, WarningOrange)
                    ChangeType.FEATURE -> Triple(AccentPurple.copy(alpha = 0.1f), Icons.Default.AutoAwesome, AccentPurple)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clip(RoundedCornerShape(8.dp)).background(bgColor).padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(icon, null, tint = tint, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(change.text, style = MaterialTheme.typography.bodySmall, color = TextPrimary, modifier = Modifier.weight(1f))
                    Surface(color = tint.copy(alpha = 0.2f), shape = RoundedCornerShape(4.dp)) {
                        Text(
                            change.type.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = tint,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
