package com.rudra.savingbuddy.ui.screens.settings

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter

data class SettingsSection(
    val title: String,
    val icon: ImageVector,
    val items: List<SettingsItem>
)

data class SettingsItem(
    val title: String,
    val subtitle: String? = null,
    val icon: ImageVector,
    val iconColor: Color = Color(0xFF6200EE),
    val action: SettingsAction = SettingsAction.None,
    val trailing: @Composable (() -> Unit)? = null
)

enum class SettingsAction {
    None, Navigation, Toggle, Dialog
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedSection by remember { mutableStateOf("Appearance") }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }

    val settingsSections = listOf(
        SettingsSection("Appearance", Icons.Outlined.Palette, listOf(
            SettingsItem("Theme", if (uiState.darkMode) "Dark" else "Light", Icons.Outlined.DarkMode, Color(0xFF7C4DFF), SettingsAction.Toggle, { Switch(checked = uiState.darkMode, onCheckedChange = { viewModel.setDarkMode(it) }) }),
            SettingsItem("Currency", "BDT (৳)", Icons.Outlined.AttachMoney, Color(0xFF4CAF50), SettingsAction.Dialog),
            SettingsItem("Start of Week", "Saturday", Icons.Outlined.CalendarViewWeek, Color(0xFFFF9800), SettingsAction.Dialog)
        )),
        SettingsSection("Budget & Goals", Icons.Outlined.AccountBalance, listOf(
            SettingsItem("Monthly Budget", "৳${CurrencyFormatter.formatBDT(uiState.budget?.monthlyLimit ?: 0.0)}", Icons.Outlined.Wallet, Color(0xFF4CAF50), SettingsAction.Dialog),
            SettingsItem("Budget Alerts", "Alert at 80%", Icons.Outlined.NotificationsActive, Color(0xFFFF9800), SettingsAction.Toggle, { Switch(checked = true, onCheckedChange = {}) }),
            SettingsItem("Goal Reminders", "Weekly progress", Icons.Outlined.Flag, Color(0xFFE91E63), SettingsAction.Toggle, { Switch(checked = true, onCheckedChange = {}) })
        )),
        SettingsSection("Accounts", Icons.Outlined.AccountBalanceWallet, listOf(
            SettingsItem("Default Account", "All Accounts", Icons.Outlined.AccountBalanceWallet, Color(0xFF2196F3), SettingsAction.Navigation),
            SettingsItem("Account Categories", "Wallet, Bank, MFS", Icons.Outlined.Category, Color(0xFF2196F3), SettingsAction.Navigation),
            SettingsItem("Daily Transfer Limits", "Configure limits", Icons.Outlined.Timer, Color(0xFFFF9800), SettingsAction.Navigation)
        )),
        SettingsSection("Notifications", Icons.Outlined.Notifications, listOf(
            SettingsItem("Push Notifications", "Receive alerts", Icons.Outlined.Notifications, Color(0xFFF44336), SettingsAction.Toggle, { Switch(checked = true, onCheckedChange = {}) }),
            SettingsItem("Bill Reminders", "3 days before", Icons.Outlined.Receipt, Color(0xFFFF9800), SettingsAction.Toggle, { Switch(checked = true, onCheckedChange = {}) }),
            SettingsItem("Weekly Summary", "Every Monday", Icons.Outlined.Summarize, Color(0xFF2196F3), SettingsAction.Toggle, { Switch(checked = true, onCheckedChange = {}) }),
            SettingsItem("Goal Progress", "On completion", Icons.Outlined.Flag, Color(0xFFE91E63), SettingsAction.Toggle, { Switch(checked = false, onCheckedChange = {}) })
        )),
        SettingsSection("Data & Privacy", Icons.Outlined.Security, listOf(
            SettingsItem("Export Data", "CSV, PDF, JSON", Icons.Outlined.FileDownload, Color(0xFF607D8B), SettingsAction.Dialog),
            SettingsItem("Backup", "Local storage", Icons.Outlined.Backup, Color(0xFF607D8B), SettingsAction.Navigation),
            SettingsItem("Privacy Mode", "Hide amounts", Icons.Outlined.VisibilityOff, Color(0xFF424242), SettingsAction.Toggle, { Switch(checked = false, onCheckedChange = {}) }),
            SettingsItem("Biometric Lock", "Fingerprint unlock", Icons.Outlined.Fingerprint, Color(0xFF424242), SettingsAction.Toggle, { Switch(checked = false, onCheckedChange = {}) })
        )),
        SettingsSection("About & Support", Icons.Outlined.Info, listOf(
            SettingsItem("App Version", "1.0.0 (Build 1)", Icons.Outlined.Info, Color(0xFF607D8B), SettingsAction.None),
            SettingsItem("Privacy Policy", "", Icons.Outlined.Policy, Color(0xFF607D8B), SettingsAction.Navigation),
            SettingsItem("Terms of Service", "", Icons.Outlined.Description, Color(0xFF607D8B), SettingsAction.Navigation),
            SettingsItem("Rate App", "", Icons.Outlined.Star, Color(0xFFFFC107), SettingsAction.Navigation),
            SettingsItem("Share App", "", Icons.Outlined.Share, Color(0xFF4CAF50), SettingsAction.Navigation),
            SettingsItem("Contact Support", "", Icons.Outlined.Support, Color(0xFF2196F3), SettingsAction.Navigation)
        ))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.Transparent)) {
                    Box(modifier = Modifier.fillMaxWidth().background(Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primary.copy(alpha = 0.7f), MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)))).padding(24.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            QuickStatItem(icon = Icons.Default.AccountBalanceWallet, value = "${uiState.accountCount}", label = "Accounts", color = Color.White)
                            QuickStatItem(icon = Icons.Default.Flag, value = "${uiState.goalCount}", label = "Goals", color = Color.White)
                            QuickStatItem(icon = Icons.Default.Receipt, value = "${uiState.billCount}", label = "Bills", color = Color.White)
                        }
                    }
                }
            }

            item {
                Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    settingsSections.forEach { section ->
                        FilterChip(selected = expandedSection == section.title, onClick = { expandedSection = section.title }, label = { Text(section.title) }, leadingIcon = { Icon(section.icon, null, modifier = Modifier.size(16.dp)) })
                    }
                }
            }

            items(settingsSections.filter { it.title == expandedSection }) { section ->
                SettingsSectionCard(title = section.title, icon = section.icon, items = section.items, isExpanded = true, onToggle = {}, onItemClick = { item -> when (item.title) { "Monthly Budget" -> showBudgetDialog = true; "Currency" -> showCurrencyDialog = true; "Export Data" -> showExportDialog = true; "Theme" -> showThemeDialog = true } })
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                    Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.Savings, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Saving Buddy", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Your Personal Finance Tracker", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            AssistChip(onClick = {}, label = { Text("Privacy") }, leadingIcon = { Icon(Icons.Outlined.Security, null, modifier = Modifier.size(16.dp)) })
                            AssistChip(onClick = {}, label = { Text("Terms") }, leadingIcon = { Icon(Icons.Outlined.Description, null, modifier = Modifier.size(16.dp)) })
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (showBudgetDialog) {
        BudgetDialog(currentBudget = uiState.budget?.monthlyLimit, onDismiss = { showBudgetDialog = false }, onSave = { viewModel.setBudget(it); showBudgetDialog = false })
    }
}

@Composable
private fun QuickStatItem(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = color.copy(alpha = 0.9f), modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
    }
}

@Composable
private fun SettingsSectionCard(title: String, icon: ImageVector, items: List<SettingsItem>, isExpanded: Boolean, onToggle: () -> Unit, onItemClick: (SettingsItem) -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().animateContentSize(), shape = RoundedCornerShape(20.dp)) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(44.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            items.forEach { item ->
                SettingsItemRow(item = item, onClick = { onItemClick(item) })
            }
        }
    }
}

@Composable
private fun SettingsItemRow(item: SettingsItem, onClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().clickable { onClick() }.padding(horizontal = 20.dp, vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(item.iconColor.copy(alpha = 0.12f)), contentAlignment = Alignment.Center) {
                Icon(item.icon, contentDescription = null, tint = item.iconColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                item.subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
        item.trailing?.invoke() ?: run {
            if (item.action == SettingsAction.Navigation || item.action == SettingsAction.Dialog) {
                Icon(Icons.Outlined.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
private fun BudgetDialog(currentBudget: Double?, onDismiss: () -> Unit, onSave: (Double) -> Unit) {
    var amount by remember { mutableStateOf(currentBudget?.toString() ?: "") }

    AlertDialog(onDismissRequest = onDismiss, title = { Text("Monthly Budget", fontWeight = FontWeight.Bold) }, text = {
        Column {
            Text("Set your monthly spending limit", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = amount, onValueChange = { amount = it }, label = { Text("Budget Amount") }, leadingIcon = { Text("৳", style = MaterialTheme.typography.titleMedium) }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
        }
    }, confirmButton = { Button(onClick = { amount.toDoubleOrNull()?.let { onSave(it) } }, shape = RoundedCornerShape(12.dp)) { Text("Save") } }, dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } })
}