package com.rudra.savingbuddy.ui.screens.features

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
    AdvancedFeature("category_budget", "Category Budgets", "Set limits per category", Icons.Default.Category, Color(0xFF4CAF50), "Budget", "category_budgets", isNew = true),
    AdvancedFeature("analytics_insights", "Smart Insights", "AI-powered spending insights", Icons.Default.Insights, Color(0xFF2196F3), "Analytics", "smart_insights", isNew = true),
    AdvancedFeature("spending_patterns", "Spending Patterns", "Weekend & daily analysis", Icons.Default.TrendingUp, Color(0xFF9C27B0), "Analytics", "spending_patterns", isNew = true),
    AdvancedFeature("pdf_export", "PDF Reports", "Export monthly reports", Icons.Default.PictureAsPdf, Color(0xFFF44336), "Export", "pdf_export", isNew = true),
    AdvancedFeature("excel_export", "Excel Export", "Download as Excel", Icons.Default.TableChart, Color(0xFF4CAF50), "Export", "excel_export", isNew = true),
    AdvancedFeature("monthly_report", "Monthly Report", "Summary with charts", Icons.Default.Assessment, Color(0xFF2196F3), "Export", "monthly_report", isNew = true),
    AdvancedFeature("smart_notify", "Smart Alerts", "Unusual spending alerts", Icons.Default.NotificationsActive, Color(0xFFFF9800), "Notifications", "smart_notifications", isNew = true),
    AdvancedFeature("reminder_notify", "Daily Reminders", "Log expense reminder", Icons.Default.Alarm, Color(0xFF9C27B0), "Notifications", "daily_reminder", isNew = true),
    AdvancedFeature("goal_alert", "Goal Alerts", "Goal progress updates", Icons.Default.Flag, Color(0xFFE91E63), "Notifications", "goal_alerts", isNew = true),
    AdvancedFeature("split_expense", "Split Expense", "Split with friends", Icons.Default.People, Color(0xFF3F51B5), "Tools", "split_expense", isNew = true),
    AdvancedFeature("loan_tracker", "Loan Tracker", "Track money owed", Icons.Default.AccountBalance, Color(0xFF009688), "Tools", "loan_tracker", isNew = true),
    AdvancedFeature("achievements", "Achievements", "Earn badges", Icons.Default.EmojiEvents, Color(0xFFFFD700), "Gamification", "achievements", isNew = true),
    AdvancedFeature("streaks", "Streaks", "Daily logging streak", Icons.Default.LocalFireDepartment, Color(0xFFFF5722), "Gamification", "streaks", isNew = true),
    AdvancedFeature("levels", "Levels", "Saver levels", Icons.Default.Star, Color(0xFF673AB7), "Gamification", "saver_levels", isNew = true),
    AdvancedFeature("app_lock", "App Lock", "PIN or fingerprint", Icons.Default.Lock, Color(0xFF424242), "Security", "app_lock", isNew = true),
    AdvancedFeature("local_encrypt", "Local Encryption", "Encrypt local data", Icons.Default.Security, Color(0xFF607D8B), "Security", "local_encrypt", isNew = true),
    AdvancedFeature("gdrive_backup", "Google Drive", "Backup to cloud", Icons.Default.Cloud, Color(0xFF4285F4), "Security", "gdrive_backup", isNew = true),
    AdvancedFeature("auto_recurring", "Auto-Add", "Auto-add recurring", Icons.Default.Loop, Color(0xFF00BCD4), "Automation", "auto_recurring", isNew = true),
    AdvancedFeature("salary_track", "Salary Tracker", "Track income", Icons.Default.Payments, Color(0xFF4CAF50), "Automation", "salary_track", isNew = true),
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
    var selectedCategory by remember { mutableStateOf("All") }
    
    val filteredFeatures = remember(selectedCategory) {
        advancedFeatures.filter { feature ->
            selectedCategory == "All" || feature.category == selectedCategory
        }
    }
    
    val isDarkMode = !MaterialTheme.colorScheme.background.luminance().let { it > 0.5f }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Advanced Features", 
                            fontWeight = FontWeight.Bold, 
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            "${advancedFeatures.size} powerful features",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        try { onNavigateBack() } catch (e: Exception) { navController?.popBackStack() }
                    }) {
                        Icon(
                            Icons.Default.ArrowBack, 
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
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
            item {
                ModernStatsCard(isDarkMode = isDarkMode)
            }
            
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(advancedCategories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
            
            items(filteredFeatures.chunked(2)) { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { feature ->
                        ModernFeatureCard(
                            feature = feature,
                            onClick = {
                                try {
                                    when (feature.id) {
                                        "category_budget" -> navController?.navigate("budget")
                                        "analytics_insights" -> navController?.navigate("reports")
                                        "smart_notify", "reminder_notify", "goal_alert" -> navController?.navigate("notifications")
                                        "achievements", "streaks", "levels" -> navController?.navigate("gamification")
                                        "app_lock", "local_encrypt" -> navController?.navigate("settings")
                                        "split_expense", "loan_tracker" -> navController?.navigate("features")
                                        "pdf_export", "excel_export" -> navController?.navigate("export")
                                        "monthly_report" -> navController?.navigate("reports")
                                        "auto_recurring", "salary_track" -> navController?.navigate("recurring")
                                        "auto_category" -> navController?.navigate("features")
                                        else -> { }
                                    }
                                } catch (e: Exception) { }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            
            if (filteredFeatures.isEmpty()) {
                item {
                    ModernEmptyState(isDarkMode = isDarkMode)
                }
            }
            
            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ModernStatsCard(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = if (isDarkMode) {
                            listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            listOf(
                                PrimaryGreen,
                                AccentTeal
                            )
                        }
                    )
                )
                .padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ModernStatItem(value = "18", label = "Features", color = Color.White)
                ModernStatItem(value = "10", label = "Categories", color = Color.White.copy(alpha = 0.9f))
                ModernStatItem(value = "5", label = "Coming Soon", color = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}

@Composable
private fun ModernStatItem(value: String, label: String, color: Color) {
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
private fun ModernFeatureCard(
    feature: AdvancedFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        label = "bg"
    )
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
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
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    feature.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (feature.isNew) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = PrimaryGreen
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
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                feature.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ModernEmptyState(isDarkMode: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.SearchOff,
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "No features found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Try a different search term",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun Color.luminance(): Float {
    val red = this.red
    val green = this.green
    val blue = this.blue
    return 0.299f * red + 0.587f * green + 0.114f * blue
}