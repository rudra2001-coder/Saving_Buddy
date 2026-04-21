package com.rudra.savingbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
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
    None,
    Navigation,
    Toggle,
    Dialog
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    navController: NavController? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    var expandedSection by remember { mutableStateOf<String?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }
    var showCurrencyDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val settingsSections = listOf(
        SettingsSection(
            title = "Appearance",
            icon = Icons.Default.Palette,
            items = listOf(
                SettingsItem(
                    title = "Theme",
                    subtitle = if (uiState.darkMode) "Dark" else "Light",
                    icon = Icons.Default.DarkMode,
                    iconColor = Color(0xFF7C4DFF),
                    action = SettingsAction.Dialog,
                    trailing = { 
                        Switch(
                            checked = uiState.darkMode,
                            onCheckedChange = { viewModel.setDarkMode(it) }
                        )
                    }
                ),
                SettingsItem(
                    title = "Currency",
                    subtitle = "BDT (৳)",
                    icon = Icons.Default.AttachMoney,
                    iconColor = Color(0xFF4CAF50),
                    action = SettingsAction.Dialog
                ),
                SettingsItem(
                    title = "Start of Week",
                    subtitle = "Saturday",
                    icon = Icons.Default.CalendarViewWeek,
                    iconColor = Color(0xFFFF9800),
                    action = SettingsAction.Dialog
                )
            )
        ),
        SettingsSection(
            title = "Budget & Goals",
            icon = Icons.Default.AccountBalance,
            items = listOf(
                SettingsItem(
                    title = "Monthly Budget",
                    subtitle = "৳${CurrencyFormatter.formatBDT(uiState.budget?.monthlyLimit ?: 0.0)}",
                    icon = Icons.Default.Wallet,
                    iconColor = Color(0xFF4CAF50),
                    action = SettingsAction.Dialog
                ),
                SettingsItem(
                    title = "Budget Alerts",
                    subtitle = "Alert at 80%",
                    icon = Icons.Default.NotificationsActive,
                    iconColor = Color(0xFFFF9800),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = true, onCheckedChange = {}) }
                ),
                SettingsItem(
                    title = "Goal Reminders",
                    subtitle = "Weekly progress",
                    icon = Icons.Default.Flag,
                    iconColor = Color(0xFFE91E63),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = true, onCheckedChange = {}) }
                )
            )
        ),
        SettingsSection(
            title = "Accounts",
            icon = Icons.Default.AccountBalanceWallet,
            items = listOf(
                SettingsItem(
                    title = "Default Account",
                    subtitle = "All Accounts",
                    icon = Icons.Default.AccountBalanceWallet,
                    iconColor = Color(0xFF2196F3),
                    action = SettingsAction.Navigation
                ),
                SettingsItem(
                    title = "Account Categories",
                    subtitle = "Wallet, Bank, MFS",
                    icon = Icons.Default.Category,
                    iconColor = Color(0xFF2196F3),
                    action = SettingsAction.Navigation
                ),
                SettingsItem(
                    title = "Daily Transfer Limits",
                    subtitle = "Configure limits per account",
                    icon = Icons.Default.Timer,
                    iconColor = Color(0xFFFF9800),
                    action = SettingsAction.Navigation
                )
            )
        ),
        SettingsSection(
            title = "Notifications",
            icon = Icons.Default.Notifications,
            items = listOf(
                SettingsItem(
                    title = "Push Notifications",
                    subtitle = "Receive alerts",
                    icon = Icons.Default.Notifications,
                    iconColor = Color(0xFFF44336),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = true, onCheckedChange = {}) }
                ),
                SettingsItem(
                    title = "Bill Reminders",
                    subtitle = "3 days before",
                    icon = Icons.Default.Receipt,
                    iconColor = Color(0xFFFF9800),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = true, onCheckedChange = {}) }
                ),
                SettingsItem(
                    title = "Weekly Summary",
                    subtitle = "Every Monday",
                    icon = Icons.Default.Summarize,
                    iconColor = Color(0xFF2196F3),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = true, onCheckedChange = {}) }
                ),
                SettingsItem(
                    title = "Goal Progress",
                    subtitle = "On completion",
                    icon = Icons.Default.Flag,
                    iconColor = Color(0xFFE91E63),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = false, onCheckedChange = {}) }
                )
            )
        ),
        SettingsSection(
            title = "Data & Privacy",
            icon = Icons.Default.Security,
            items = listOf(
                SettingsItem(
                    title = "Export Data",
                    subtitle = "CSV, PDF, JSON",
                    icon = Icons.Default.FileDownload,
                    iconColor = Color(0xFF607D8B),
                    action = SettingsAction.Dialog
                ),
                SettingsItem(
                    title = "Backup",
                    subtitle = "Local storage",
                    icon = Icons.Default.Backup,
                    iconColor = Color(0xFF607D8B),
                    action = SettingsAction.Navigation
                ),
                SettingsItem(
                    title = "Privacy Mode",
                    subtitle = "Hide amounts",
                    icon = Icons.Default.VisibilityOff,
                    iconColor = Color(0xFF424242),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = false, onCheckedChange = {}) }
                ),
                SettingsItem(
                    title = "Biometric Lock",
                    subtitle = "Fingerprint unlock",
                    icon = Icons.Default.Fingerprint,
                    iconColor = Color(0xFF424242),
                    action = SettingsAction.Toggle,
                    trailing = { Switch(checked = false, onCheckedChange = {}) }
                )
            )
        ),
        SettingsSection(
            title = "About & Support",
            icon = Icons.Default.Info,
            items = listOf(
                SettingsItem(
                    title = "App Version",
                    subtitle = "1.0.0 (Build 1)",
                    icon = Icons.Default.Info,
                    iconColor = Color(0xFF607D8B),
                    action = SettingsAction.None
                ),
                SettingsItem(
                    title = "Privacy Policy",
                    icon = Icons.Default.Policy,
                    iconColor = Color(0xFF607D8B),
                    action = SettingsAction.Navigation
                ),
                SettingsItem(
                    title = "Terms of Service",
                    icon = Icons.Default.Description,
                    iconColor = Color(0xFF607D8B),
                    action = SettingsAction.Navigation
                ),
                SettingsItem(
                    title = "Rate App",
                    icon = Icons.Default.Star,
                    iconColor = Color(0xFFFFC107),
                    action = SettingsAction.Navigation
                ),
                SettingsItem(
                    title = "Share App",
                    icon = Icons.Default.Share,
                    iconColor = Color(0xFF4CAF50),
                    action = SettingsAction.Navigation
                ),
                SettingsItem(
                    title = "Contact Support",
                    icon = Icons.Default.Support,
                    iconColor = Color(0xFF2196F3),
                    action = SettingsAction.Navigation
                )
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Settings", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                        Text(
                            "Saving Buddy v1.0.0",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController?.navigate("features") }) {
                        Icon(Icons.Default.Apps, contentDescription = "Features")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Quick Stats Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
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
                        QuickStatItem(
                            icon = Icons.Default.AccountBalanceWallet,
                            value = "${uiState.accountCount}",
                            label = "Accounts"
                        )
                        QuickStatItem(
                            icon = Icons.Default.Flag,
                            value = "${uiState.goalCount}",
                            label = "Goals"
                        )
                        QuickStatItem(
                            icon = Icons.Default.Receipt,
                            value = "${uiState.billCount}",
                            label = "Bills"
                        )
                    }
                }
            }

            // Settings Sections
            items(settingsSections) { section ->
                SettingsSectionCard(
                    title = section.title,
                    icon = section.icon,
                    items = section.items,
                    isExpanded = expandedSection == section.title,
                    onToggle = {
                        expandedSection = if (expandedSection == section.title) null else section.title
                    },
                    onItemClick = { item ->
                        when (item.title) {
                            "Monthly Budget" -> showBudgetDialog = true
                            "Currency" -> showCurrencyDialog = true
                            "Export Data" -> showExportDialog = true
                            "Theme" -> showThemeDialog = true
                            "Start of Week" -> showLanguageDialog = true
                            else -> {}
                        }
                    }
                )
            }

            // App Info Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Saving Buddy",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Your Personal Finance Tracker",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AssistChip(
                                onClick = {},
                                label = { Text("Privacy") },
                                leadingIcon = { Icon(Icons.Default.Security, null, modifier = Modifier.size(16.dp)) }
                            )
                            AssistChip(
                                onClick = {},
                                label = { Text("Terms") },
                                leadingIcon = { Icon(Icons.Default.Description, null, modifier = Modifier.size(16.dp)) }
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showBudgetDialog) {
        BudgetDialog(
            currentBudget = uiState.budget?.monthlyLimit,
            onDismiss = { showBudgetDialog = false },
            onSave = { 
                viewModel.setBudget(it)
                showBudgetDialog = false
            }
        )
    }
}

@Composable
private fun QuickStatItem(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun SettingsSectionCard(
    title: String,
    icon: ImageVector,
    items: List<SettingsItem>,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onItemClick: (SettingsItem) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggle() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                }
                Icon(
                    if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null
                )
            }

            // Items
            if (isExpanded) {
                HorizontalDivider()
                items.forEach { item ->
                    SettingsItemRow(
                        item = item,
                        onClick = { onItemClick(item) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsItemRow(
    item: SettingsItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(item.iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    item.icon,
                    contentDescription = null,
                    tint = item.iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(item.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                item.subtitle?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        item.trailing?.invoke() ?: run {
            if (item.action == SettingsAction.Navigation || item.action == SettingsAction.Dialog) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun BudgetDialog(
    currentBudget: Double?,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var amount by remember { mutableStateOf(currentBudget?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Monthly Budget") },
        text = {
            Column {
                Text(
                    "Set your monthly spending limit",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Budget Amount") },
                    leadingIcon = { Text("৳", style = MaterialTheme.typography.titleMedium) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    amount.toDoubleOrNull()?.let { onSave(it) }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
