package com.rudra.savingbuddy.ui.screens.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
    val route: String
)

val allFeatures = listOf(
    // Dashboard
    FeatureItem("dashboard", "Dashboard", "Overview & summary", Icons.Default.Dashboard, Color(0xFF4CAF50), "Main", "dashboard"),
    
    // Add Transactions
    FeatureItem("add_income", "Add Income", "Add new income", Icons.Default.TrendingUp, Color(0xFF4CAF50), "Add", "add_income"),
    FeatureItem("add_expense", "Add Expense", "Add new expense", Icons.Default.TrendingDown, Color(0xFFF44336), "Add", "add_expense"),

    // History
    FeatureItem("income", "Income History", "View all income", Icons.Default.Savings, Color(0xFF4CAF50), "History", "income"),
    FeatureItem("expense", "Expense History", "View all expenses", Icons.Default.ShoppingCart, Color(0xFFF44336), "History", "expense"),

    // Recurring
    FeatureItem("recurring", "Recurring", "Manage recurring", Icons.Default.Repeat, Color(0xFF9C27B6), "Transactions", "recurring"),
    
    // Bills
    FeatureItem("bills", "Bill Reminders", "Upcoming bills", Icons.Default.Receipt, Color(0xFFFF9800), "Transactions", "bills"),
    
    // Goals
    FeatureItem("goals", "Goals", "Savings goals", Icons.Default.Flag, Color(0xFFE91E63), "Goals", "goals"),
    
    // Budget
    FeatureItem("budget", "Budget", "Monthly budget & subscriptions", Icons.Default.AccountBalance, Color(0xFF3F51B5), "Budget", "budget"),
    
    // Reports
    FeatureItem("reports", "Reports", "Analytics & transaction logs", Icons.Default.Analytics, Color(0xFF2196F3), "Reports", "reports"),
    
    // Calendar
    FeatureItem("calendar", "Calendar", "View transactions by date", Icons.Default.CalendarMonth, Color(0xFF673AB7), "Transactions", "calendar"),
    
    // Scanner
    FeatureItem("scanner", "Receipt Scanner", "Scan receipts for transactions", Icons.Default.QrCodeScanner, Color(0xFF009688), "Add", "scanner"),
    
    // Notification Center
    FeatureItem("notifications", "Notifications", "All notifications", Icons.Default.Notifications, Color(0xFFF44336), "Settings", "notifications"),
    
    // Settings
    FeatureItem("settings", "Settings", "App settings", Icons.Default.Settings, Color(0xFF424242), "Settings", "settings"),
)

val featureCategories = listOf("All", "Main", "Add", "History", "Transactions", "Goals", "Budget", "Reports", "Settings", "Tools")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(navController: NavController?) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showSearch by remember { mutableStateOf(false) }

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
                            placeholder = { Text("Search features...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    } else {
                        Text("All Features", fontWeight = FontWeight.Bold)
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch }) {
                        Icon(if (showSearch) Icons.Default.Close else Icons.Default.Search, contentDescription = "Search")
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
            // Category Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                featureCategories.forEach { category ->
                    val isSelected = selectedCategory == category
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            // Results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${filteredFeatures.size} features",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (selectedCategory != "All" || searchQuery.isNotBlank()) {
                    TextButton(onClick = {
                        selectedCategory = "All"
                        searchQuery = ""
                    }) {
                        Text("Clear filters")
                    }
                }
            }

            // Features Grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredFeatures) { feature ->
                    FeatureCard(
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

@Composable
fun FeatureCard(feature: FeatureItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1.1f)
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = feature.color.copy(alpha = 0.12f)),
        onClick = onClick
    ) {
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
    }
}