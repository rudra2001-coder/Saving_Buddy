package com.rudra.savingbuddy.ui.screens.backup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import com.rudra.savingbuddy.data.models.*
import com.rudra.savingbuddy.data.BackupFileInfo
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(
    onNavigateBack: () -> Unit,
    viewModel: BackupViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val backupList by viewModel.backupList.collectAsStateWithLifecycle()
    
    var showDeleteDialog by remember { mutableStateOf<BackupFileInfo?>(null) }
    var expandedFrequency by remember { mutableStateOf(false) }
    var expandedDay by remember { mutableStateOf(false) }
    var expandedLocation by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.initialize(context)
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
                title = { Text("Backup & Restore", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (uiState.isEnabled) Color(0xFF4CAF50) else MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Auto Backup",
                                color = if (uiState.isEnabled) Color.White else Color.Unspecified,
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.titleLarge.fontSize
                            )
                            Text(
                                if (uiState.isEnabled) "ON • ${uiState.frequency.name}" else "OFF • Manual only",
                                color = if (uiState.isEnabled) Color.White.copy(alpha = 0.8f) else Color.Gray
                            )
                        }
                        Switch(
                            checked = uiState.isEnabled,
                            onCheckedChange = { viewModel.toggleBackup(it) }
                        )
                    }
                }
            }

            if (uiState.isEnabled) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("Schedule", fontWeight = FontWeight.Bold)
                            
                            Box {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { expandedFrequency = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Frequency")
                                        Text(uiState.frequency.name, color = Color.Gray)
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedFrequency,
                                    onDismissRequest = { expandedFrequency = false }
                                ) {
                                    BackupFrequency.entries.forEach { freq ->
                                        DropdownMenuItem(
                                            text = { Text(freq.name) },
                                            onClick = {
                                                viewModel.updateFrequency(freq)
                                                expandedFrequency = false
                                            }
                                        )
                                    }
                                }
                            }

                            if (uiState.frequency == BackupFrequency.WEEKLY) {
                                Box {
                                    OutlinedCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { expandedDay = true }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Backup Day")
                                            Text(uiState.backupDay?.name ?: "Monday", color = Color.Gray)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = expandedDay,
                                        onDismissRequest = { expandedDay = false }
                                    ) {
                                        BackupDay.entries.forEach { day ->
                                            DropdownMenuItem(
                                                text = { Text(day.name) },
                                                onClick = {
                                                    viewModel.updateBackupDay(day)
                                                    expandedDay = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Box {
                                OutlinedCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { expandedLocation = true }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Location")
                                        Text(
                                            when (uiState.backupLocation) {
                                                BackupLocation.DOWNLOADS -> "Downloads"
                                                BackupLocation.INTERNAL -> "Internal"
                                            },
                                            color = Color.Gray
                                        )
                                    }
                                }
                                DropdownMenu(
                                    expanded = expandedLocation,
                                    onDismissRequest = { expandedLocation = false }
                                ) {
                                    BackupLocation.entries.forEach { loc ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    when (loc) {
                                                        BackupLocation.DOWNLOADS -> "Downloads Folder"
                                                        BackupLocation.INTERNAL -> "Internal Storage"
                                                    }
                                                )
                                            },
                                            onClick = {
                                                viewModel.updateLocation(loc)
                                                expandedLocation = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Button(
                    onClick = { viewModel.createBackup() },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBackingUp
                ) {
                    if (uiState.isBackingUp) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color.White
                        )
                    } else {
                        Icon(Icons.Default.Backup, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Backup Now")
                    }
                }
            }

            item {
                Text(
                    "Recent Backups",
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.titleMedium.fontSize
                )
            }

            if (backupList.isEmpty()) {
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
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = Color.Gray
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No backups found", color = Color.Gray)
                            Text(
                                "Tap 'Backup Now' to create your first backup",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.Gray
                            )
                        }
                    }
                }
            } else {
                items(backupList) { backup ->
                    BackupItemCard(
                        backup = backup,
                        onRestore = { viewModel.restoreBackup(backup.path) },
                        onDelete = { showDeleteDialog = backup }
                    )
                }
            }

            item { Spacer(modifier = Modifier.height(32.dp)) }
        }
    }

    showDeleteDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Backup") },
            text = { Text("Are you sure you want to delete this backup?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteBackup(backup.path)
                        showDeleteDialog = null
                    }
                ) {
                    Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BackupItemCard(
    backup: BackupFileInfo,
    onRestore: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    backup.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Text(
                    "${formatSize(backup.size)} • ${formatDate(backup.modifiedDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Row {
                IconButton(onClick = onRestore) {
                    Icon(Icons.Default.Restore, contentDescription = "Restore")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${bytes / (1024 * 1024)} MB"
    }
}

private fun formatDate(timestamp: Long): String {
    val format = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    return format.format(Date(timestamp))
}