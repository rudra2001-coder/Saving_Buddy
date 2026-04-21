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
    data object Goals : Screen("goals", "Goals", Icons.Default.Flag)
    data object Reports : Screen("reports", "Reports", Icons.Default.Receipt)
    data object Settings : Screen("settings", "Settings", Icons.Default.Settings)
    data object Features : Screen("features", "Features", Icons.Default.Apps)
    data object More : Screen("more", "More", Icons.Default.MoreVert)
    data object Notifications : Screen("notifications", "Notifications", Icons.Default.Notifications)
    data object Budget : Screen("budget", "Budget", Icons.Default.AccountBalance)
    data object AddIncome : Screen("add_income", "Income", Icons.Default.TrendingUp)
    data object AddExpense : Screen("add_expense", "Expense", Icons.Default.TrendingDown)
    data object Recurring : Screen("recurring", "Recurring", Icons.Default.Repeat)
    data object Bills : Screen("bills", "Bills", Icons.Default.Receipt)
    data object Income : Screen("income", "Income History", Icons.Default.TrendingUp)
    data object Expense : Screen("expense", "Expense History", Icons.Default.TrendingDown)
    data object Calendar : Screen("calendar", "Calendar", Icons.Default.CalendarMonth)
    data object Scanner : Screen("scanner", "Scanner", Icons.Default.QrCodeScanner)
    data object Accounts : Screen("accounts", "Accounts", Icons.Default.AccountBalanceWallet)
    data object Transfer : Screen("transfer", "Transfer", Icons.Default.SwapHoriz)
    data object AddAccount : Screen("add_account", "Add Account", Icons.Default.Add)
    data object AccountDetail : Screen("account_detail", "Account Detail", Icons.Default.Info)
    data object Fusion : Screen("fusion", "Fusion", Icons.Default.JoinFull)
    data object Backup : Screen("backup", "Backup", Icons.Default.Backup)
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Features,
    Screen.Reports,
    Screen.Settings,

)

val moreMenuItems = listOf(
    Screen.Notifications,
    Screen.Dashboard
)