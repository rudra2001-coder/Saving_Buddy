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
    data object Accounts : Screen("accounts", "Accounts", Icons.Default.AccountBalanceWallet)
    data object Transfer : Screen("transfer", "Transfer", Icons.Default.SwapHoriz)
    data object AddAccount : Screen("add_account", "Add Account", Icons.Default.Add)
    data object AccountDetail : Screen("account_detail", "Account Detail", Icons.Default.Info)
    data object Fusion : Screen("fusion", "Fusion", Icons.Default.JoinFull)
    data object Backup : Screen("backup", "Backup", Icons.Default.Backup)
    data object Calculator : Screen("calculator", "Calculator", Icons.Default.Calculate)
    data object Export : Screen("export", "Export", Icons.Default.FileDownload)
    data object Analytics : Screen("analytics", "Analytics", Icons.Default.Insights)
    data object TransactionHistory : Screen("transaction_history", "Transaction History", Icons.Default.History)
    data object PrivacyPolicy : Screen("privacy_policy", "Privacy Policy", Icons.Default.Policy)
    data object TermsOfService : Screen("terms_of_service", "Terms of Service", Icons.Default.Description)
    data object AdvancedFeatures : Screen("advanced_features", "Advanced", Icons.Default.AutoAwesome)
    data object Gamification : Screen("gamification", "Achievements", Icons.Default.EmojiEvents)
    data object SplitExpense : Screen("split_expense", "Split", Icons.Default.People)
    data object LoanTracker : Screen("loan_tracker", "Loans", Icons.Default.AccountBalance)
    data object CategoryBudgets : Screen("category_budgets", "Category Budgets", Icons.Default.Category)
    data object SmartInsights : Screen("smart_insights", "Insights", Icons.Default.Insights)
    data object SpendingPatterns : Screen("spending_patterns", "Patterns", Icons.Default.TrendingUp)
    data object PDFExport : Screen("pdf_export", "PDF Report", Icons.Default.PictureAsPdf)
    data object MonthlyReport : Screen("monthly_report", "Report", Icons.Default.Assessment)
    data object SmartNotifications : Screen("smart_notifications", "Smart Alerts", Icons.Default.NotificationsActive)
    data object AppLock : Screen("app_lock", "App Lock", Icons.Default.Lock)
    data object AutoRecurring : Screen("auto_recurring", "Auto Add", Icons.Default.Loop)
    data object AutoCategory : Screen("auto_category", "Auto Category", Icons.Default.AutoAwesome)
    data object ExpenseDetail : Screen("expense_detail", "Expense Detail", Icons.Default.Receipt)
    data object Transactions : Screen("transactions", "Transactions", Icons.Default.SwapHoriz)
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