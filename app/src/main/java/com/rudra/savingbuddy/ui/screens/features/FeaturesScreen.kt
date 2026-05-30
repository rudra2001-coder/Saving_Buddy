package com.rudra.savingbuddy.ui.screens.features

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.savingbuddy.ui.theme.*

data class FeatureItem(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val category: String,
    val route: String,
    val isNew: Boolean = false,
    val isBeta: Boolean = false
)

val allFeatures = listOf(
    FeatureItem("dashboard", "Dashboard", "Overview & summary", Icons.Default.Dashboard, PrimaryGreen, "Main", "dashboard"),
    FeatureItem("fusion", "Fusion Timeline", "Unified transactions", Icons.Default.JoinInner, AccentPurple, "Main", "fusion", isNew = true),

    FeatureItem("add_income", "Add Income", "Add new income", Icons.Default.TrendingUp, IncomeGreen, "Add", "add_income"),
    FeatureItem("add_expense", "Add Expense", "Add new expense", Icons.Default.TrendingDown, ExpenseRed, "Add", "add_expense"),
    FeatureItem("transfer", "Transfer Money", "Move between accounts", Icons.Default.SwapHoriz, WarningOrange, "Add", "transfer"),

    FeatureItem("accounts", "All Accounts", "Manage accounts", Icons.Default.AccountBalanceWallet, SavingsBlue, "Accounts", "accounts"),
    FeatureItem("add_account", "Add Account", "Add new account", Icons.Default.AddCircle, SavingsBlue, "Accounts", "add_account"),

    FeatureItem("income", "Income History", "View all income", Icons.Default.Savings, IncomeGreen, "History", "income"),
    FeatureItem("expense", "Expense History", "View all expenses", Icons.Default.ShoppingCart, ExpenseRed, "History", "expense"),
    FeatureItem("transaction_history", "Transaction History", "All transactions in one place", Icons.Default.History, AccentTeal, "History", "transaction_history"),

    FeatureItem("goals", "Savings Goals", "Track your goals", Icons.Default.Flag, EntertainmentColor, "Goals", "goals"),
    FeatureItem("gamification", "Achievements", "Gamified milestones", Icons.Default.EmojiEvents, WarningOrange, "Goals", "gamification"),

    FeatureItem("budget", "Budget", "Monthly budget", Icons.Default.AccountBalance, SavingsBlue, "Budget", "budget"),

    FeatureItem("recurring", "Recurring", "Recurring transactions", Icons.Default.Repeat, AccentPurple, "Transactions", "recurring"),
    FeatureItem("bills", "Bill Reminders", "Upcoming bills", Icons.Default.Receipt, WarningOrange, "Transactions", "bills"),

    FeatureItem("calendar", "Calendar", "View by date", Icons.Default.CalendarMonth, AccentPurple, "Tools", "calendar"),
    FeatureItem("export", "Export Data", "Export to CSV/PDF", Icons.Default.FileDownload, SurfaceDark, "Tools", "export"),
    FeatureItem("calculator", "Calculator", "Financial calculator", Icons.Default.Calculate, AccentCyan, "Tools", "calculator"),

    FeatureItem("reports", "Reports", "Analytics & insights", Icons.Default.Analytics, SavingsBlue, "Reports", "reports"),
    FeatureItem("analytics", "Analytics", "Detailed analytics", Icons.Default.Insights, SavingsBlue, "Reports", "analytics"),

    FeatureItem("settings", "Settings", "App settings", Icons.Default.Settings, SurfaceDark, "Settings", "settings"),
    FeatureItem("notifications", "Notifications", "Notification center", Icons.Default.Notifications, ExpenseRed, "Settings", "notifications"),
    FeatureItem("backup", "Backup & Restore", "Data backup", Icons.Default.Backup, PrimaryGreen, "Settings", "backup"),
    FeatureItem("changelog", "What's New", "Version history", Icons.Default.NewReleases, AccentPurple, "Settings", "changelog"),
    FeatureItem("language", "Language", "Multi-language support", Icons.Default.Language, SurfaceDark, "Settings", "language_settings"),
    FeatureItem("advanced", "Advanced Features", "AI & smart features", Icons.Default.AutoAwesome, AccentPurple, "Settings", "advanced_features"),

    FeatureItem("investment_tracker", "Investments", "Track investments & portfolio", Icons.Default.TrendingUp, PrimaryGreen, "Finance", "investment_tracker", isNew = true),
    FeatureItem("subscription_manager", "Subscriptions", "Manage subscriptions", Icons.Default.Subscriptions, WarningOrange, "Finance", "subscription_manager", isNew = true),
    FeatureItem("currency_converter", "Currency Converter", "14 currencies live", Icons.Default.CurrencyExchange, AccentCyan, "Finance", "currency_converter", isNew = true),
    FeatureItem("receipt_scanner", "Receipt Scanner", "Scan with camera", Icons.Default.CameraAlt, SavingsBlue, "Finance", "receipt_scanner", isNew = true),
)

val featureCategories = listOf(
    "All", "Main", "Add", "Accounts", "History", "Goals", "Budget", "Transactions", "Tools", "Reports", "Finance", "Settings"
)

private val StatBlue600 = Color(0xFF185FA5)
private val StatGreen600 = Color(0xFF22C55E)
private val StatAmber600 = Color(0xFF854F0B)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(navController: NavController?) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showSearch by remember { mutableStateOf(false) }
    var showGrid by remember { mutableStateOf(true) }

    val filteredFeatures = allFeatures.filter { feature ->
        val matchesSearch = searchQuery.isBlank() ||
            feature.title.contains(searchQuery, ignoreCase = true) ||
            feature.description.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategory == "All" || feature.category == selectedCategory
        matchesSearch && matchesCategory
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search features...", color = MaterialTheme.colorScheme.onSurfaceVariant) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Column {
                            Text("Features", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text(
                                "${allFeatures.size} available",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search")
                    }
                    IconButton(onClick = { showGrid = !showGrid }) {
                        Icon(if (showGrid) Icons.Default.ViewList else Icons.Default.GridView, contentDescription = "Toggle view")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(featureCategories) { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FeatureStatColumn(
                        value = allFeatures.size.toString(),
                        label = "Features",
                        color = StatBlue600
                    )
                    FeatureStatColumn(
                        value = (featureCategories.size - 1).toString(),
                        label = "Categories",
                        color = StatGreen600
                    )
                    FeatureStatColumn(
                        value = allFeatures.count { it.isNew }.toString(),
                        label = "New",
                        color = StatAmber600
                    )
                }
            }

            if (showGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(filteredFeatures) { feature ->
                        FeatureCard(
                            feature = feature,
                            onClick = { navController?.navigate(feature.route) }
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredFeatures) { feature ->
                        FeatureListItem(
                            feature = feature,
                            onClick = { navController?.navigate(feature.route) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureStatColumn(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun FeatureCard(feature: FeatureItem, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = tween(200), label = "scale")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.95f)
            .scale(scale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    feature.color.copy(alpha = 0.25f),
                                    feature.color.copy(alpha = 0.1f)
                                )
                            )
                        )
                        .border(
                            width = 1.dp,
                            color = feature.color.copy(alpha = 0.3f),
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = feature.color,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (feature.isNew || feature.isBeta) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (feature.isNew) {
                        Surface(
                            color = PrimaryGreen,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    if (feature.isBeta) {
                        Surface(
                            color = WarningOrange,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "BETA",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FeatureListItem(feature: FeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                feature.color.copy(alpha = 0.25f),
                                feature.color.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = feature.color.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.color,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (feature.isNew) {
                Surface(
                    color = PrimaryGreen,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        "NEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
