package com.rudra.savingbuddy.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    data object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Add : Screen("add", "Add", Icons.Default.Add)
    data object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    data object Reports : Screen("reports", "Reports", Icons.Default.Receipt)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Features : Screen("features", "Features", Icons.Default.Apps)
    data object More : Screen("more", "More", Icons.Default.MoreVert)
    data object Notifications : Screen("notifications", "Notifications", Icons.Default.Notifications)
    data object Budget : Screen("budget", "Budget", Icons.Default.AccountBalance)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Add,
    Screen.Goals,
    Screen.Budget,
    Screen.Reports,
    Screen.Settings
)

val moreMenuItems = listOf(
    Screen.Features,
    Screen.Notifications,
    Screen.Dashboard
)