package com.rudra.savingbuddy.ui.screens.features

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
    val route: String? = null
)

val allFeatures = listOf(
    FeatureItem("dashboard", "Dashboard", "Overview & summaries", Icons.Default.Dashboard, Color(0xFF4CAF50), "Main", "dashboard"),
    FeatureItem("today_summary", "Today's Summary", "Daily income/expenses", Icons.Default.Today, Color(0xFF2196F3), "Main", "dashboard"),
    FeatureItem("monthly_summary", "Monthly Summary", "Monthly overview", Icons.Default.CalendarMonth, Color(0xFF9C27B0), "Main", "reports"),
    FeatureItem("charts", "Charts & Graphs", "Visual analytics", Icons.Default.BarChart, Color(0xFFFF9800), "Main", "reports"),
    
    FeatureItem("add_income", "Add Income", "Record income", Icons.Default.Add, Color(0xFF4CAF50), "Income", "add"),
    FeatureItem("edit_income", "Edit Income", "Modify income", Icons.Default.Edit, Color(0xFF8BC34A), "Income", "add"),
    FeatureItem("delete_income", "Delete Income", "Remove income", Icons.Default.Delete, Color(0xFFF44336), "Income", "add"),
    FeatureItem("recurring_income", "Recurring Income", "Monthly salary etc", Icons.Default.Repeat, Color(0xFF009688), "Income", "add"),
    FeatureItem("income_categories", "Income Categories", "Salary, Freelance etc", Icons.Default.Category, Color(0xFF673AB7), "Income", "add"),
    
    FeatureItem("quick_add", "Quick Add", "1-tap expense", Icons.Default.Bolt, Color(0xFFFFEB3B), "Expense", "add"),
    FeatureItem("expense_categories", "Expense Categories", "Food, Transport etc", Icons.Default.ShoppingCart, Color(0xFFFF5722), "Expense", "add"),
    FeatureItem("expense_notes", "Add Notes", "Add details", Icons.Default.Notes, Color(0xFF795548), "Expense", "add"),
    FeatureItem("expense_date", "Set Date", "Pick date", Icons.Default.DateRange, Color(0xFF607D8B), "Expense", "add"),
    
    FeatureItem("create_goal", "Create Goal", "New savings goal", Icons.Default.Flag, Color(0xFFE91E63), "Goals", "goals"),
    FeatureItem("goal_progress", "Track Progress", "Visual progress", Icons.Default.TrendingUp, Color(0xFF4CAF50), "Goals", "goals"),
    FeatureItem("auto_allocate", "Auto Allocate", "Auto-save", Icons.Default.AutoAwesome, Color(0xFF2196F3), "Goals", "goals"),
    FeatureItem("goal_categories", "Goal Categories", "Vacation, Car etc", Icons.Default.Stars, Color(0xFFFFD700), "Goals", "goals"),
    
    FeatureItem("set_budget", "Set Budget", "Monthly limit", Icons.Default.AccountBalance, Color(0xFF3F51B5), "Budget", "settings"),
    FeatureItem("budget_warning", "Budget Warning", "80% alert", Icons.Default.Warning, Color(0xFFFF9800), "Budget", "settings"),
    FeatureItem("rollover_budget", "Rollover Budget", "Carry unused", Icons.Default.Refresh, Color(0xFF9C27B0), "Budget", "settings"),
    FeatureItem("category_budget", "Category Budget", "Per-category", Icons.Default.PieChart, Color(0xFF00BCD4), "Budget", "settings"),
    
    FeatureItem("savings_rate", "Savings Rate", "Track savings", Icons.Default.Savings, Color(0xFF4CAF50), "Reports", "reports"),
    FeatureItem("monthly_trend", "Monthly Trend", "6-month view", Icons.Default.ShowChart, Color(0xFF2196F3), "Reports", "reports"),
    FeatureItem("category_breakdown", "Category Breakdown", "By category", Icons.Default.DonutLarge, Color(0xFFFF5722), "Reports", "reports"),
    FeatureItem("year_over_year", "Year-over-Year", "Compare years", Icons.Default.Compare, Color(0xFF9C27B0), "Reports", "reports"),
    FeatureItem("custom_date", "Custom Date Range", "Pick dates", Icons.Default.DateRange, Color(0xFF607D8B), "Reports", "reports"),
    FeatureItem("export_csv", "Export CSV", "Share data", Icons.Default.Download, Color(0xFF4CAF50), "Reports", "reports"),
    
    FeatureItem("dark_mode", "Dark Mode", "Dark theme", Icons.Default.DarkMode, Color(0xFF424242), "Settings", "settings"),
    FeatureItem("notifications", "Notifications", "Daily reminders", Icons.Default.Notifications, Color(0xFFF44336), "Settings", "settings"),
    FeatureItem("biometric", "Biometric Lock", "Fingerprint", Icons.Default.Fingerprint, Color(0xFF2196F3), "Settings", "settings"),
    FeatureItem("privacy_mode", "Privacy Mode", "Hide amounts", Icons.Default.VisibilityOff, Color(0xFF757575), "Settings", "settings"),
    
    FeatureItem("widget", "Home Widget", "Quick add widget", Icons.Default.Widgets, Color(0xFF4CAF50), "Advanced", "settings"),
    FeatureItem("sms_parser", "SMS Parser", "Auto-detect SMS", Icons.Default.Sms, Color(0xFF2196F3), "Advanced", "settings"),
    FeatureItem("voice_input", "Voice Input", "Add by voice", Icons.Default.Mic, Color(0xFFFF9800), "Advanced", "add"),
    FeatureItem("multi_currency", "Multi-Currency", "15+ currencies", Icons.Default.CurrencyExchange, Color(0xFF9C27B0), "Advanced", "settings"),
    FeatureItem("gamification", "Gamification", "Scores & badges", Icons.Default.EmojiEvents, Color(0xFFFFD700), "Advanced", "dashboard"),
    FeatureItem("achievements", "Achievements", "Earn badges", Icons.Default.MilitaryTech, Color(0xFFE91E63), "Advanced", "dashboard"),
    FeatureItem("streaks", "Savings Streaks", "Track streaks", Icons.Default.LocalFireDepartment, Color(0xFFFF5722), "Advanced", "dashboard"),
    FeatureItem("analytics", "Deep Analytics", "Advanced insights", Icons.Default.Analytics, Color(0xFF00BCD4), "Advanced", "reports"),
    
    FeatureItem("sms_permission", "SMS Permission", "Read bank SMS", Icons.Default.Sms, Color(0xFF2196F3), "Security", "settings"),
    FeatureItem("notification_perm", "Notifications", "Permission", Icons.Default.Notifications, Color(0xFFF44336), "Security", "settings"),
)

val categoryIcons: Map<String, ImageVector> = mapOf(
    "Main" to Icons.Default.Dashboard,
    "Income" to Icons.Default.AttachMoney,
    "Expense" to Icons.Default.ShoppingCart,
    "Goals" to Icons.Default.Flag,
    "Budget" to Icons.Default.AccountBalance,
    "Reports" to Icons.Default.Analytics,
    "Settings" to Icons.Default.Settings,
    "Advanced" to Icons.Default.AutoAwesome,
    "Security" to Icons.Default.Security
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeaturesScreen(navController: NavController? = null) {
    var selectedCategory by remember { mutableStateOf("All") }
    
    val categories = listOf("All", "Main", "Income", "Expense", "Goals", "Budget", "Reports", "Settings", "Advanced", "Security")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Features",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Category tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    val backgroundColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.primaryContainer 
                        else MaterialTheme.colorScheme.surfaceVariant,
                        label = "category_bg"
                    )
                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        label = "category_content"
                    )
                    
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = category },
                        label = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = categoryIcons[category] ?: Icons.Default.Apps,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(category)
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = backgroundColor,
                            selectedLabelColor = contentColor
                        )
                    )
                }
            }

            // Feature items
            val filteredFeatures = if (selectedCategory == "All") {
                allFeatures
            } else {
                allFeatures.filter { it.category == selectedCategory }
            }

            if (selectedCategory == "All") {
                // Group by category view
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    categories.filter { it != "All" }.forEach { category ->
                        val categoryFeatures = allFeatures.filter { it.category == category }
                        if (categoryFeatures.isNotEmpty()) {
                            item {
                                CategorySection(
                                    category = category,
                                    features = categoryFeatures,
                                    onFeatureClick = { feature ->
                                        feature.route?.let { route ->
                                            navController?.navigate(route)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredFeatures) { feature ->
                        FeatureCardItem(
                            feature = feature,
                            onClick = {
                                feature.route?.let { route ->
                                    navController?.navigate(route)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategorySection(
    category: String,
    features: List<FeatureItem>,
    onFeatureClick: (FeatureItem) -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 8.dp)
        ) {
            Icon(
                imageVector = categoryIcons[category] ?: Icons.Default.Apps,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = category,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "${features.size} features",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        features.chunked(2).forEach { rowFeatures ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowFeatures.forEach { feature ->
                    FeatureCardItem(
                        feature = feature,
                        onClick = { onFeatureClick(feature) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowFeatures.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
fun FeatureCardItem(
    feature: FeatureItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isPressed by remember { mutableStateOf(false) }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isPressed) 
                feature.color.copy(alpha = 0.2f)
            else 
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
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
                    .background(feature.color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = feature.icon,
                    contentDescription = null,
                    tint = feature.color,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = feature.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}