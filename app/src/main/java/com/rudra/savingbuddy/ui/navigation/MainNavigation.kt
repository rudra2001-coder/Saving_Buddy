package com.rudra.savingbuddy.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.rudra.savingbuddy.ui.screens.add.AddExpenseScreen
import com.rudra.savingbuddy.ui.screens.add.AddIncomeScreen
import com.rudra.savingbuddy.ui.screens.bills.BillRemindersScreen
import com.rudra.savingbuddy.ui.screens.recurring.RecurringScreen
import com.rudra.savingbuddy.ui.screens.budget.BudgetScreen
import com.rudra.savingbuddy.ui.screens.dashboard.DashboardScreen
import com.rudra.savingbuddy.ui.screens.expense.ExpenseScreen
import com.rudra.savingbuddy.ui.screens.features.FeaturesScreen
import com.rudra.savingbuddy.ui.screens.income.IncomeScreen
import com.rudra.savingbuddy.ui.screens.goals.GoalsScreen
import com.rudra.savingbuddy.ui.screens.notifications.NotificationsScreen
import com.rudra.savingbuddy.ui.screens.reports.ReportsScreen
import com.rudra.savingbuddy.ui.screens.settings.SettingsScreen
import com.rudra.savingbuddy.ui.screens.calendar.CalendarScreen
import com.rudra.savingbuddy.ui.screens.scanner.ScannerScreen
import com.rudra.savingbuddy.ui.screens.accounts.AccountsScreen
import com.rudra.savingbuddy.ui.screens.accounts.TransferScreen
import com.rudra.savingbuddy.ui.screens.accounts.AddAccountScreen
import com.rudra.savingbuddy.ui.screens.accounts.AccountDetailScreen
import com.rudra.savingbuddy.ui.screens.fusion.FusionScreen
import com.rudra.savingbuddy.ui.screens.backup.BackupScreen

@Composable
fun MainNavigation() {
    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

                bottomNavItems.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController = navController)
            }
            composable(Screen.AddIncome.route) {
                AddIncomeScreen(navController = navController)
            }
            composable(Screen.AddExpense.route) {
                AddExpenseScreen(navController = navController)
            }
            composable(Screen.Goals.route) {
                GoalsScreen()
            }
            composable(Screen.Budget.route) {
                BudgetScreen()
            }
            composable(Screen.Recurring.route) {
                RecurringScreen(navController = navController)
            }
            composable(Screen.Reports.route) {
                ReportsScreen()
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController = navController)
            }
            composable(Screen.Features.route) {
                FeaturesScreen(navController = navController)
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen()
            }
            composable(Screen.Bills.route) {
                BillRemindersScreen(navController = navController)
            }
            composable(Screen.Income.route) {
                IncomeScreen(navController = navController)
            }
            composable(Screen.Expense.route) {
                ExpenseScreen(navController = navController)
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(navController = navController)
            }
            composable(Screen.Scanner.route) {
                ScannerScreen(navController = navController)
            }
            composable(Screen.Accounts.route) {
                AccountsScreen(navController = navController)
            }
            composable(Screen.Transfer.route) {
                TransferScreen(navController = navController)
            }
            composable(Screen.AddAccount.route) {
                AddAccountScreen(navController = navController)
            }
            composable(Screen.AccountDetail.route + "/{accountId}") { backStackEntry ->
                val accountId = backStackEntry.arguments?.getString("accountId")?.toLongOrNull() ?: 0L
                AccountDetailScreen(navController = navController, accountId = accountId)
            }
            composable(Screen.Fusion.route) {
                FusionScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable(Screen.Backup.route) {
                BackupScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }
}