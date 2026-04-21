package com.rudra.savingbuddy.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.Transfer
import com.rudra.savingbuddy.domain.model.TransferStatus
import com.rudra.savingbuddy.ui.navigation.Screen
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailScreen(
    navController: NavController,
    accountId: Long,
    viewModel: AccountDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(accountId) {
        viewModel.loadAccount(accountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.account?.name ?: "Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Transfer.route) }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer")
                    }
                }
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
            // Balance Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Current Balance",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            CurrencyFormatter.formatBDT(uiState.account?.balance ?: 0.0),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Last updated: ${uiState.account?.let { DateUtils.formatDate(it.lastUpdated) } ?: "N/A"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        title = "Add Money",
                        icon = Icons.Default.Add,
                        onClick = { viewModel.showAddMoneyDialog() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Cash Out",
                        icon = Icons.Default.Remove,
                        onClick = { viewModel.showCashOutDialog() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Send",
                        icon = Icons.Default.Send,
                        onClick = { navController.navigate(Screen.Transfer.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Daily Limit Progress
            uiState.account?.dailyLimit?.let { limit ->
                if (limit > 0) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Daily Transfer Limit", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        "${CurrencyFormatter.formatBDT(limit - (uiState.account?.usedToday ?: 0.0))} remaining",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { ((uiState.account?.usedToday ?: 0.0) / limit).toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(CircleShape),
                                )
                            }
                        }
                    }
                }
            }

            // Balance History (Chart placeholder)
            item {
                Text(
                    "Balance History (Last 7 days)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(60.dp)
                                        .background(
                                            MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(day, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }

            // Recent Transactions
            item {
                Text(
                    "Recent Transactions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            if (uiState.transfers.isEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No transactions yet",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(uiState.transfers) { transfer ->
                    TransferItem(
                        transfer = transfer,
                        fromAccountName = uiState.account?.name ?: "Unknown",
                        toAccountName = uiState.account?.name ?: "Unknown",
                        accounts = uiState.allAccounts
                    )
                }
            }
        }
    }

    // Add Money Dialog
    if (uiState.showAddMoneyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddMoneyDialog() },
            title = { Text("Add Money") },
            text = {
                Column {
                    Text("Add money to ${uiState.account?.name}")
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.addMoneyAmount,
                        onValueChange = { viewModel.updateAddMoneyAmount(it) },
                        label = { Text("Amount") },
                        leadingIcon = { Text("৳", style = MaterialTheme.typography.titleLarge) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.addMoney() },
                    enabled = uiState.addMoneyAmount.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Add")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideAddMoneyDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Cash Out Dialog
    if (uiState.showCashOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCashOutDialog() },
            title = { Text("Cash Out") },
            text = {
                Column {
                    Text("Withdraw from ${uiState.account?.name}")
                    Text(
                        "Available: ${CurrencyFormatter.formatBDT(uiState.account?.balance ?: 0.0)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = uiState.cashOutAmount,
                        onValueChange = { viewModel.updateCashOutAmount(it) },
                        label = { Text("Amount") },
                        leadingIcon = { Text("৳", style = MaterialTheme.typography.titleLarge) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.cashOut() },
                    enabled = uiState.cashOutAmount.isNotBlank() && !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp))
                    } else {
                        Text("Withdraw")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCashOutDialog() }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Dialog
    if (uiState.operationResult?.success == true) {
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
            title = { Text("Success!") },
            text = {
                Text("New balance: ${CurrencyFormatter.formatBDT(uiState.operationResult?.toAccountNewBalance ?: uiState.operationResult?.fromAccountNewBalance ?: 0.0)}")
            },
            confirmButton = {
                Button(onClick = { viewModel.clearResult() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(title, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
fun TransferItem(
    transfer: Transfer,
    fromAccountName: String,
    toAccountName: String,
    accounts: List<com.rudra.savingbuddy.domain.model.Account>
) {
    val isSent = transfer.toAccountId != transfer.fromAccountId // Simplified
    
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSent) Color(0xFFFFEBEE) else Color(0xFFE8F5E9)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSent) Icons.Default.ArrowForward else Icons.Default.ArrowBack,
                    null,
                    tint = if (isSent) Color(0xFFF44336) else Color(0xFF4CAF50),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isSent) "To account" else "From account",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                transfer.note?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    DateUtils.formatDate(transfer.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Text(
                "${if (isSent) "-" else "+"}${CurrencyFormatter.formatBDT(transfer.amount)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSent) Color(0xFFF44336) else Color(0xFF4CAF50)
            )
        }
    }
}