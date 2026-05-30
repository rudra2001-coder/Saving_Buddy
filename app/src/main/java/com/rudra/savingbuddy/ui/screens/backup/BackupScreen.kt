package com.rudra.savingbuddy.ui.screens.backup

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.rudra.savingbuddy.data.BackupFileInfo
import com.rudra.savingbuddy.data.models.BackupDay
import com.rudra.savingbuddy.data.models.BackupFrequency
import com.rudra.savingbuddy.data.models.BackupLocation
import com.rudra.savingbuddy.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backupList by viewModel.backupList.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showDeleteDialog by remember { mutableStateOf<BackupFileInfo?>(null) }
    var showRestoreDialog by remember { mutableStateOf<BackupFileInfo?>(null) }
    var showFrequencyMenu by remember { mutableStateOf(false) }
    var showDayMenu by remember { mutableStateOf(false) }
    var showLocationMenu by remember { mutableStateOf(false) }
    var showChooseFolderDialog by remember { mutableStateOf(false) }
    var showManualExportDialog by remember { mutableStateOf(false) }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.importBackup(it) }
    }

    val autoFolderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.updateCustomBackupFolder(it) }
    }

    val manualExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { viewModel.exportToFolder(it) }
    }

    LaunchedEffect(uiState.error, uiState.success) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
        uiState.success?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Backup & Restore", fontWeight = FontWeight.Bold)
                        Text(
                            "Protect your financial data",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                AutoBackupCard(
                    isEnabled = uiState.isEnabled,
                    frequency = uiState.frequency,
                    backupDay = uiState.backupDay,
                    backupLocation = uiState.backupLocation,
                    lastBackupTime = uiState.lastBackupTime,
                    onToggle = { viewModel.toggleBackup(it) },
                    onFrequencyChange = { viewModel.updateFrequency(it) },
                    onDayChange = { viewModel.updateBackupDay(it) },
                    onLocationChange = { viewModel.updateLocation(it) },
                    showFrequencyMenu = showFrequencyMenu,
                    onShowFrequencyMenu = { showFrequencyMenu = it },
                    showDayMenu = showDayMenu,
                    onShowDayMenu = { showDayMenu = it },
                    showLocationMenu = showLocationMenu,
                    onShowLocationMenu = { showLocationMenu = it },
                    onChooseFolderClick = { showChooseFolderDialog = true }
                )
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = { viewModel.createBackup() },
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = !uiState.isBackingUp,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                    ) {
                        if (uiState.isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.Backup, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Quick Backup")
                        }
                    }

                    Button(
                        onClick = { showManualExportDialog = true },
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = !uiState.isBackingUp,
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
                    ) {
                        if (uiState.isBackingUp) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                        } else {
                            Icon(Icons.Default.FolderOpen, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Export to Folder")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) },
                        modifier = Modifier.weight(1f).height(52.dp),
                        enabled = !uiState.isRestoring,
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.5.dp, PrimaryGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryGreen)
                    ) {
                        if (uiState.isRestoring) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), color = PrimaryGreen)
                        } else {
                            Icon(Icons.Default.RestorePage, null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import Backup (JSON)")
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Folder, null, modifier = Modifier.size(20.dp), tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Recent Backups",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    if (backupList.isNotEmpty()) {
                        TextButton(onClick = { viewModel.loadBackupList() }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Refresh")
                        }
                    }
                }
            }

            if (backupList.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Outlined.FolderOpen, null, modifier = Modifier.size(48.dp), tint = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No backups found", color = TextSecondary)
                            Text(
                                "Tap 'Export Backup' to create your first backup",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            } else {
                items(backupList, key = { it.path }) { backup ->
                    BackupItemCard(
                        backup = backup,
                        onShare = { viewModel.shareBackup(backup.path) },
                        onRestore = { showRestoreDialog = backup },
                        onDelete = { showDeleteDialog = backup }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    showRestoreDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showRestoreDialog = null },
            icon = { Icon(Icons.Default.RestorePage, null, tint = WarningOrange) },
            title = { Text("Restore Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("This will replace ALL current data with the backup data.")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(backup.name, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    Text("${formatSize(backup.size)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.restoreBackup(backup.path)
                        showRestoreDialog = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = WarningOrange)
                ) {
                    Text("Restore")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreDialog = null }) { Text("Cancel") }
            }
        )
    }

    showDeleteDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            icon = { Icon(Icons.Default.Delete, null, tint = ExpenseRed) },
            title = { Text("Delete Backup") },
            text = { Text("Are you sure you want to delete ${backup.name}?") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.deleteBackup(backup.path); showDeleteDialog = null }
                ) { Text("Delete", color = ExpenseRed) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }

    if (showChooseFolderDialog) {
        AlertDialog(
            onDismissRequest = { showChooseFolderDialog = false },
            icon = { Icon(Icons.Default.CreateNewFolder, null, tint = PrimaryGreen) },
            title = { Text("Choose Auto Backup Folder") },
            text = { Text("Select a folder where automatic backups will be saved. This is where your backup files will be stored if the app is deleted or crashes.") },
            confirmButton = {
                Button(
                    onClick = {
                        showChooseFolderDialog = false
                        autoFolderPickerLauncher.launch(null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) { Text("Choose Folder") }
            },
            dismissButton = {
                TextButton(onClick = { showChooseFolderDialog = false }) { Text("Cancel") }
            }
        )
    }

    if (showManualExportDialog) {
        AlertDialog(
            onDismissRequest = { showManualExportDialog = false },
            icon = { Icon(Icons.Default.FolderOpen, null, tint = SavingsBlue) },
            title = { Text("Export Backup to Folder") },
            text = { Text("Select a folder to save your backup file. Choose any location on your device.") },
            confirmButton = {
                Button(
                    onClick = {
                        showManualExportDialog = false
                        manualExportLauncher.launch(null)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
                ) { Text("Choose Folder") }
            },
            dismissButton = {
                TextButton(onClick = { showManualExportDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AutoBackupCard(
    isEnabled: Boolean,
    frequency: BackupFrequency,
    backupDay: BackupDay?,
    backupLocation: BackupLocation,
    lastBackupTime: Long,
    onToggle: (Boolean) -> Unit,
    onFrequencyChange: (BackupFrequency) -> Unit,
    onDayChange: (BackupDay) -> Unit,
    onLocationChange: (BackupLocation) -> Unit,
    showFrequencyMenu: Boolean,
    onShowFrequencyMenu: (Boolean) -> Unit,
    showDayMenu: Boolean,
    onShowDayMenu: (Boolean) -> Unit,
    showLocationMenu: Boolean,
    onShowLocationMenu: (Boolean) -> Unit,
    onChooseFolderClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) PrimaryGreen else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Auto Backup",
                        color = if (isEnabled) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        if (isEnabled) "ON \u2022 ${frequency.name.lowercase().replaceFirstChar { it.uppercase() }}" else "OFF",
                        color = if (isEnabled) Color.White.copy(alpha = 0.8f) else TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Switch(
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = if (isEnabled) SwitchDefaults.colors(
                        checkedTrackColor = Color.White,
                        checkedThumbColor = PrimaryGreen
                    ) else SwitchDefaults.colors()
                )
            }

            if (lastBackupTime > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isEnabled) Icons.Default.CheckCircle else Icons.Outlined.AccessTime,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isEnabled) Color.White.copy(alpha = 0.8f) else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        "Last backup: ${formatDate(lastBackupTime)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isEnabled) Color.White.copy(alpha = 0.7f) else TextSecondary
                    )
                }
            }

            if (isEnabled) {
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                Spacer(modifier = Modifier.height(12.dp))

                SettingsRow(
                    label = "Frequency",
                    value = when (frequency) {
                        BackupFrequency.DAILY -> "Daily"
                        BackupFrequency.WEEKLY -> "Weekly"
                        BackupFrequency.MONTHLY -> "Monthly"
                    },
                    icon = Icons.Default.Schedule,
                    expanded = showFrequencyMenu,
                    onToggle = onShowFrequencyMenu,
                    textColor = Color.White
                ) {
                    BackupFrequency.entries.forEach { freq ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    when (freq) {
                                        BackupFrequency.DAILY -> "Daily"
                                        BackupFrequency.WEEKLY -> "Weekly"
                                        BackupFrequency.MONTHLY -> "Monthly"
                                    },
                                    color = if (freq == frequency) PrimaryGreen else Color.Unspecified
                                )
                            },
                            onClick = { onFrequencyChange(freq); onShowFrequencyMenu(false) },
                            leadingIcon = {
                                if (freq == frequency) Icon(Icons.Default.Check, null, tint = PrimaryGreen)
                            }
                        )
                    }
                }

                if (frequency == BackupFrequency.WEEKLY) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsRow(
                        label = "Backup Day",
                        value = backupDay?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Monday",
                        icon = Icons.Default.CalendarToday,
                        expanded = showDayMenu,
                        onToggle = onShowDayMenu,
                        textColor = Color.White
                    ) {
                        BackupDay.entries.forEach { day ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        day.name.lowercase().replaceFirstChar { it.uppercase() },
                                        color = if (day == backupDay) PrimaryGreen else Color.Unspecified
                                    )
                                },
                                onClick = { onDayChange(day); onShowDayMenu(false) },
                                leadingIcon = {
                                    if (day == backupDay) Icon(Icons.Default.Check, null, tint = PrimaryGreen)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                SettingsRow(
                    label = "Save to",
                    value = when (backupLocation) {
                        BackupLocation.DOWNLOADS -> "Downloads"
                        BackupLocation.INTERNAL -> "Internal Storage"
                        BackupLocation.CUSTOM -> "Custom Folder"
                    },
                    icon = Icons.Default.Folder,
                    expanded = showLocationMenu,
                    onToggle = onShowLocationMenu,
                    textColor = Color.White
                ) {
                    DropdownMenuItem(
                        text = { Text("Downloads", color = if (backupLocation == BackupLocation.DOWNLOADS) PrimaryGreen else Color.Unspecified) },
                        onClick = { onLocationChange(BackupLocation.DOWNLOADS); onShowLocationMenu(false) },
                        leadingIcon = { if (backupLocation == BackupLocation.DOWNLOADS) Icon(Icons.Default.Check, null, tint = PrimaryGreen) }
                    )
                    DropdownMenuItem(
                        text = { Text("Internal Storage", color = if (backupLocation == BackupLocation.INTERNAL) PrimaryGreen else Color.Unspecified) },
                        onClick = { onLocationChange(BackupLocation.INTERNAL); onShowLocationMenu(false) },
                        leadingIcon = { if (backupLocation == BackupLocation.INTERNAL) Icon(Icons.Default.Check, null, tint = PrimaryGreen) }
                    )
                    DropdownMenuItem(
                        text = { Text("Custom Folder", color = if (backupLocation == BackupLocation.CUSTOM) PrimaryGreen else Color.Unspecified) },
                        onClick = { onLocationChange(BackupLocation.CUSTOM); onShowLocationMenu(false) },
                        leadingIcon = { if (backupLocation == BackupLocation.CUSTOM) Icon(Icons.Default.Check, null, tint = PrimaryGreen) }
                    )
                }

                if (backupLocation == BackupLocation.CUSTOM) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedCard(
                        onClick = onChooseFolderClick,
                        colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.1f)),
                        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreateNewFolder, null, modifier = Modifier.size(18.dp), tint = Color.White.copy(alpha = 0.8f))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Choose Backup Folder", color = Color.White, style = MaterialTheme.typography.bodyMedium)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.6f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsRow(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    expanded: Boolean,
    onToggle: (Boolean) -> Unit,
    textColor: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Box {
        OutlinedCard(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onToggle(true) },
            colors = CardDefaults.outlinedCardColors(containerColor = Color.White.copy(alpha = 0.1f)),
            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, modifier = Modifier.size(18.dp), tint = textColor.copy(alpha = 0.8f))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(label, color = textColor.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                }
                Text(value, color = textColor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { onToggle(false) }) {
            content()
        }
    }
}

@Composable
private fun BackupItemCard(
    backup: BackupFileInfo,
    onShare: () -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Description,
                null,
                modifier = Modifier.size(36.dp),
                tint = PrimaryGreen
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    backup.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1
                )
                Text(
                    "${formatSize(backup.size)} \u2022 ${formatDate(backup.modifiedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Row {
                IconButton(onClick = onShare) {
                    Icon(Icons.Default.Share, null, tint = SavingsBlue, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Restore, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = ExpenseRed, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String = when {
    bytes < 1024 -> "$bytes B"
    bytes < 1024 * 1024 -> "${bytes / 1024} KB"
    else -> "${"%.1f".format(bytes.toDouble() / (1024 * 1024))} MB"
}

private fun formatDate(timestamp: Long): String {
    return try {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        sdf.format(Date(timestamp))
    } catch (_: Exception) { "Unknown" }
}
