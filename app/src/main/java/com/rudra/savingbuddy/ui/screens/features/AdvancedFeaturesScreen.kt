package com.rudra.savingbuddy.ui.screens.features

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.rudra.savingbuddy.ui.theme.*

data class AdvancedFeature(
    val id: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val category: String,
    val route: String,
    val isNew: Boolean = false
)

val advancedFeatures = listOf(
    // Budget & Analytics
    AdvancedFeature("category_budget", "Category Budgets", "Set limits per category", Icons.Default.Category, Color(0xFF4CAF50), "Budget", "category_budgets", isNew = true),
    AdvancedFeature("analytics_insights", "Smart Insights", "AI-powered spending insights", Icons.Default.Insights, Color(0xFF2196F3), "Analytics", "smart_insights", isNew = true),
    AdvancedFeature("spending_patterns", "Spending Patterns", "Weekend & daily analysis", Icons.Default.TrendingUp, Color(0xFF9C27B0), "Analytics", "spending_patterns", isNew = true),
    
    // Export & Reports
    AdvancedFeature("pdf_export", "PDF Reports", "Export monthly reports", Icons.Default.PictureAsPdf, Color(0xFFF44336), "Export", "pdf_export", isNew = true),
    AdvancedFeature("excel_export", "Excel Export", "Download as Excel", Icons.Default.TableChart, Color(0xFF4CAF50), "Export", "excel_export", isNew = true),
    AdvancedFeature("monthly_report", "Monthly Report", "Summary with charts", Icons.Default.Assessment, Color(0xFF2196F3), "Export", "monthly_report", isNew = true),
    
    // Notifications
    AdvancedFeature("smart_notify", "Smart Alerts", "Unusual spending alerts", Icons.Default.NotificationsActive, Color(0xFFFF9800), "Notifications", "smart_notifications", isNew = true),
    AdvancedFeature("reminder_notify", "Daily Reminders", "Log expense reminder", Icons.Default.Alarm, Color(0xFF9C27B0), "Notifications", "daily_reminder", isNew = true),
    AdvancedFeature("goal_alert", "Goal Alerts", "Goal progress updates", Icons.Default.Flag, Color(0xFFE91E63), "Notifications", "goal_alerts", isNew = true),
    
    // Split & Loan
    AdvancedFeature("split_expense", "Split Expense", "Split with friends", Icons.Default.People, Color(0xFF3F51B5), "Tools", "split_expense", isNew = true),
    AdvancedFeature("loan_tracker", "Loan Tracker", "Track money owed", Icons.Default.AccountBalance, Color(0xFF009688), "Tools", "loan_tracker", isNew = true),
    
    // Gamification
    AdvancedFeature("achievements", "Achievements", "Earn badges", Icons.Default.EmojiEvents, Color(0xFFFFD700), "Gamification", "achievements", isNew = true),
    AdvancedFeature("streaks", "Streaks", "Daily logging streak", Icons.Default.LocalFireDepartment, Color(0xFFFF5722), "Gamification", "streaks", isNew = true),
    AdvancedFeature("levels", "Levels", "Saver levels", Icons.Default.Star, Color(0xFF673AB7), "Gamification", "saver_levels", isNew = true),
    
    // Data Safety
    AdvancedFeature("app_lock", "App Lock", "PIN or fingerprint", Icons.Default.Lock, Color(0xFF424242), "Security", "app_lock", isNew = true),
    AdvancedFeature("local_encrypt", "Local Encryption", "Encrypt local data", Icons.Default.Security, Color(0xFF607D8B), "Security", "local_encrypt", isNew = true),
    AdvancedFeature("gdrive_backup", "Google Drive", "Backup to cloud", Icons.Default.Cloud, Color(0xFF4285F4), "Security", "gdrive_backup", isNew = true),
    
    // Recurring
    AdvancedFeature("auto_recurring", "Auto-Add", "Auto-add recurring", Icons.Default.Loop, Color(0xFF00BCD4), "Automation", "auto_recurring", isNew = true),
    AdvancedFeature("salary_track", "Salary Tracker", "Track income", Icons.Default.Payments, Color(0xFF4CAF50), "Automation", "salary_track", isNew = true),
    
    // Category Intelligence
    AdvancedFeature("auto_category", "Auto-Category", "Detect category", Icons.Default.AutoAwesome, Color(0xFF7C4DFF), "Intelligence", "auto_category", isNew = true),
    AdvancedFeature("category_icon", "Custom Icons", "Category images", Icons.Default.Image, Color(0xFFE91E63), "Intelligence", "category_icons", isNew = true)
)

val advancedCategories = listOf(
    "All", "Budget", "Analytics", "Export", "Notifications", "Tools", "Gamification", "Security", "Automation", "Intelligence"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdvancedFeaturesScreen(
    navController: NavController?,
    onNavigateBack: () -> Unit = {}
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    
    val filteredFeatures = advancedFeatures.filter { feature ->
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
                    Column {
                        Text("Advanced Features", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "${advancedFeatures.size} powerful features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Stats Row
            item {
                AdvancedStatsCard()
            }
            
            // Category Chips
            item {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            advancedCategories.take(5).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            advancedCategories.drop(5).forEach { category ->
                                FilterChip(
                                    selected = selectedCategory == category,
                                    onClick = { selectedCategory = category },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }
                        }
                    }
                }
            }
            
            // Features Grid
            items(filteredFeatures) { feature ->
                AdvancedFeatureCard(
                    feature = feature,
                    onClick = {
                        when (feature.id) {
                            "category_budget" -> navController?.navigate("budget")
                            "analytics_insights" -> navController?.navigate("reports")
                            "smart_notify", "reminder_notify", "goal_alert" -> navController?.navigate("notifications")
                            "achievements", "streaks", "levels" -> navController?.navigate("gamification")
                            "app_lock", "local_encrypt" -> navController?.navigate("settings")
                            else -> { /* Show coming soon */ }
                        }
                    }
                )
            }
            
            if (filteredFeatures.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "No features found",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "Try a different search term",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun AdvancedStatsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBox(value = "18", label = "New Features", color = Color.White)
                StatBox(value = "10", label = "Categories", color = Color.White)
                StatBox(value = "5", label = "Coming Soon", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun StatBox(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            label,
            style = MaterialTheme.typography.labelSmall,
            color = color.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AdvancedFeatureCard(
    feature: AdvancedFeature,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = 1f, animationSpec = tween(200), label = "scale")
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = feature.color.copy(alpha = 0.08f)
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
                    .background(feature.color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    feature.icon,
                    contentDescription = null,
                    tint = feature.color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        feature.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (feature.isNew) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = IncomeGreen
                        ) {
                            Text(
                                "NEW",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Text(
                    feature.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}