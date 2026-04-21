package com.rudra.savingbuddy.ui.screens.features

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

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
    // Main
    FeatureItem("dashboard", "Dashboard", "Overview & summary", Icons.Default.Dashboard, Color(0xFF4CAF50), "Main", "dashboard"),
    FeatureItem("fusion", "Fusion Timeline", "Unified transactions", Icons.Default.JoinInner, Color(0xFF7C4DFF), "Main", "fusion", isNew = true),
    
    // Add Transactions
    FeatureItem("add_income", "Add Income", "Add new income", Icons.Default.TrendingUp, Color(0xFF4CAF50), "Add", "add_income"),
    FeatureItem("add_expense", "Add Expense", "Add new expense", Icons.Default.TrendingDown, Color(0xFFF44336), "Add", "add_expense"),
    FeatureItem("transfer", "Transfer Money", "Move between accounts", Icons.Default.SwapHoriz, Color(0xFFFF9800), "Add", "transfer"),
    FeatureItem("scanner", "Scan QR", "Scan receipts/QR codes", Icons.Default.QrCodeScanner, Color(0xFF009688), "Add", "scanner"),

    // Accounts
    FeatureItem("accounts", "All Accounts", "Manage accounts", Icons.Default.AccountBalanceWallet, Color(0xFF2196F3), "Accounts", "accounts"),
    FeatureItem("add_account", "Add Account", "Add new account", Icons.Default.AddCircle, Color(0xFF2196F3), "Accounts", "add_account"),

    // History
    FeatureItem("income", "Income History", "View all income", Icons.Default.Savings, Color(0xFF4CAF50), "History", "income"),
    FeatureItem("expense", "Expense History", "View all expenses", Icons.Default.ShoppingCart, Color(0xFFF44336), "History", "expense"),
    FeatureItem("transfers", "Transfer History", "View transfers", Icons.Default.SwapHoriz, Color(0xFFFF9800), "History", "transfers"),

    // Goals
    FeatureItem("goals", "Savings Goals", "Track your goals", Icons.Default.Flag, Color(0xFFE91E63), "Goals", "goals"),
    FeatureItem("add_goal", "Add New Goal", "Create a goal", Icons.Default.AddLocationAlt, Color(0xFFE91E63), "Goals", "add_goal"),

    // Budget
    FeatureItem("budget", "Budget", "Monthly budget", Icons.Default.AccountBalance, Color(0xFF3F51B5), "Budget", "budget"),
    FeatureItem("subscriptions", "Subscriptions", "Manage subscriptions", Icons.Default.Subscriptions, Color(0xFF9C27B0), "Budget", "subscriptions"),

    // Recurring
    FeatureItem("recurring", "Recurring", "Recurring transactions", Icons.Default.Repeat, Color(0xFF9C27B6), "Transactions", "recurring"),
    FeatureItem("bills", "Bill Reminders", "Upcoming bills", Icons.Default.Receipt, Color(0xFFFF9800), "Transactions", "bills"),

    // Tools
    FeatureItem("calendar", "Calendar", "View by date", Icons.Default.CalendarMonth, Color(0xFF673AB7), "Tools", "calendar"),
    FeatureItem("export", "Export Data", "Export to CSV/PDF", Icons.Default.FileDownload, Color(0xFF607D8B), "Tools", "export"),
    FeatureItem("calculator", "Calculator", "Financial calculator", Icons.Default.Calculate, Color(0xFF607D8B), "Tools", "calculator"),

    // Reports
    FeatureItem("reports", "Reports", "Analytics & insights", Icons.Default.Analytics, Color(0xFF2196F3), "Reports", "reports"),
    FeatureItem("analytics", "Analytics", "Detailed analytics", Icons.Default.Insights, Color(0xFF2196F3), "Reports", "analytics"),

    // Settings
    FeatureItem("settings", "Settings", "App settings", Icons.Default.Settings, Color(0xFF424242), "Settings", "settings"),
    FeatureItem("notifications", "Notifications", "Notification center", Icons.Default.Notifications, Color(0xFFF44336), "Settings", "notifications"),
    FeatureItem("backup", "Backup & Restore", "Data backup", Icons.Default.Backup, Color(0xFF4CAF50), "Settings", "backup", isNew = true),
    FeatureItem("about", "About", "App info & help", Icons.Default.Info, Color(0xFF424242), "Settings", "about"),
)

val featureCategories = listOf(
    "All", "Main", "Add", "Accounts", "History", "Goals", "Budget", "Transactions", "Tools", "Reports", "Settings"
)

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
            // Category Chips - Scrollable Row
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
                            selectedContainerColor = Color(0xFF6200EE),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            // Quick Stats Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${allFeatures.size}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Features", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${featureCategories.size - 1}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Categories", style = MaterialTheme.typography.bodySmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${allFeatures.count { it.isNew }}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text("New", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Features Grid or List
            if (showGrid) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFeatures) { feature ->
                        ModernFeatureCard(
                            feature = feature,
                            onClick = {
                                navController?.navigate(feature.route)
                            }
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
                        ModernFeatureListItem(
                            feature = feature,
                            onClick = {
                                navController?.navigate(feature.route)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ModernFeatureCard(feature: FeatureItem, onClick: () -> Unit) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = tween(200), label = "scale")
    val backgroundColor by animateColorAsState(
        targetValue = feature.color.copy(alpha = 0.1f),
        animationSpec = tween(300),
        label = "bg_color"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.05f)
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(feature.color.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = feature.icon,
                        contentDescription = null,
                        tint = feature.color,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )

                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }

            // Badges
            if (feature.isNew || feature.isBeta) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (feature.isNew) {
                        Badge(containerColor = Color(0xFF4CAF50)) {
                            Text("NEW", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                    if (feature.isBeta) {
                        Badge(containerColor = Color(0xFFFF9800)) {
                            Text("BETA", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ModernFeatureListItem(feature: FeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(feature.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

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
                Badge(containerColor = Color(0xFF4CAF50)) {
                    Text("NEW", style = MaterialTheme.typography.labelSmall)
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}