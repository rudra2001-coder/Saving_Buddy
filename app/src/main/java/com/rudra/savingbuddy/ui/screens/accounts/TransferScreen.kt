package com.rudra.savingbuddy.ui.screens.accounts

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.AccountProvider
import com.rudra.savingbuddy.domain.model.TransferResult
import com.rudra.savingbuddy.ui.navigation.Screen
import com.rudra.savingbuddy.util.CurrencyFormatter
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    navController: NavController,
    viewModel: TransferViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    var showConfirmation by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Transfer Money", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // FROM Account
            Text("FROM", style = MaterialTheme.typography.labelMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFromPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(uiState.fromAccount?.iconColor ?: 0xFF6200EE).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📤", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            uiState.fromAccount?.name ?: "Select account",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        uiState.fromAccount?.let {
                            Text(
                                "Balance: ${CurrencyFormatter.formatBDT(it.balance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            // TO Account
            Text("TO", style = MaterialTheme.typography.labelMedium)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showToPicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(uiState.toAccount?.iconColor ?: 0xFF6200EE).copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📥", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            uiState.toAccount?.name ?: "Select account",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        uiState.toAccount?.let {
                            Text(
                                "Balance: ${CurrencyFormatter.formatBDT(it.balance)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }

            // Amount
            OutlinedTextField(
                value = uiState.amount,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                        viewModel.updateAmount(newValue)
                    }
                },
                label = { Text("Amount") },
                leadingIcon = { Text("৳", style = MaterialTheme.typography.titleLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Fee and Total
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Fee", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            if (uiState.fee == 0.0) "Free" else CurrencyFormatter.formatBDT(uiState.fee),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text(
                            CurrencyFormatter.formatBDT(uiState.totalAmount),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Note
            OutlinedTextField(
                value = uiState.note,
                onValueChange = { viewModel.updateNote(it) },
                label = { Text("Note (optional)") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 2
            )

            // Error message
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Text(
                        error,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Transfer Button
            Button(
                onClick = { showConfirmation = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.amount.isNotBlank() && uiState.fromAccount != null && uiState.toAccount != null && !uiState.isLoading,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Transfer ${CurrencyFormatter.formatBDT(uiState.totalAmount)}", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Account Pickers
    if (showFromPicker) {
        AccountPickerDialog(
            accounts = uiState.availableAccounts,
            title = "Select Source Account",
            onSelect = { account ->
                viewModel.setFromAccount(account)
                showFromPicker = false
            },
            onDismiss = { showFromPicker = false }
        )
    }

    if (showToPicker) {
        AccountPickerDialog(
            accounts = uiState.availableAccounts,
            title = "Select Destination Account",
            onSelect = { account ->
                viewModel.setToAccount(account)
                showToPicker = false
            },
            onDismiss = { showToPicker = false }
        )
    }

    // Confirmation Dialog
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Confirm Transfer") },
            text = {
                Column {
                    Text("Transfer ${CurrencyFormatter.formatBDT(uiState.totalAmount)} from:")
                    Text("${uiState.fromAccount?.name}", fontWeight = FontWeight.Bold)
                    Text("to:")
                    Text("${uiState.toAccount?.name}", fontWeight = FontWeight.Bold)
                    if (uiState.note.isNotBlank()) {
                        Text("Note: ${uiState.note}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.executeTransfer()
                    showConfirmation = false
                }) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Success Dialog
    if (uiState.transferResult?.success == true) {
        AlertDialog(
            onDismissRequest = { 
                viewModel.reset()
                navController.popBackStack()
            },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
            title = { Text("Transfer Successful!") },
            text = {
                Column {
                    Text("${CurrencyFormatter.formatBDT(uiState.totalAmount)} moved")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("New balances:")
                    Text("${uiState.fromAccount?.name}: ${CurrencyFormatter.formatBDT(uiState.transferResult?.fromAccountNewBalance ?: 0.0)}")
                    Text("${uiState.toAccount?.name}: ${CurrencyFormatter.formatBDT(uiState.transferResult?.toAccountNewBalance ?: 0.0)}")
                }
            },
            confirmButton = {
                Button(onClick = { 
                    viewModel.reset()
                    navController.popBackStack()
                }) {
                    Text("Done")
                }
            }
        )
    }

    // Goal Funding Suggestion Dialog
    if (uiState.showGoalSuggestion && uiState.goalSuggestion != null) {
        val suggestion = uiState.goalSuggestion!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissGoalSuggestion() },
            icon = { Icon(Icons.Default.Savings, null, tint = Color(0xFF9C27B0), modifier = Modifier.size(48.dp)) },
            title = { Text("Save to Goal?") },
            text = {
                Column {
                    Text("You just transferred ৳${CurrencyFormatter.formatBDT(uiState.totalAmount)}")
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Would you like to add ৳${CurrencyFormatter.formatBDT(suggestion.suggestedAmount)} to your ${suggestion.goalName} goal?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF9C27B0).copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Goal Progress:", style = MaterialTheme.typography.labelSmall)
                            LinearProgressIndicator(
                                progress = { (suggestion.currentAmount / suggestion.targetAmount).toFloat() },
                                modifier = Modifier.fillMaxWidth(),
                                color = Color(0xFF9C27B0),
                            )
                            Text(
                                "${CurrencyFormatter.formatBDT(suggestion.currentAmount)} / ${CurrencyFormatter.formatBDT(suggestion.targetAmount)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.allocateToGoalFromSuggestion() },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9C27B0))
                ) {
                    Text("Yes, Save!")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGoalSuggestion() }) {
                    Text("Not Now")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountPickerDialog(
    accounts: List<Account>,
    title: String,
    onSelect: (Account) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(accounts) { account ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onSelect(account) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(account.iconColor).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    when (account.type) {
                                        com.rudra.savingbuddy.domain.model.AccountType.WALLET -> "💵"
                                        com.rudra.savingbuddy.domain.model.AccountType.BANK -> "🏦"
                                        com.rudra.savingbuddy.domain.model.AccountType.MOBILE_BANKING -> "📱"
                                        else -> "💳"
                                    },
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(account.name, style = MaterialTheme.typography.bodyMedium)
                                Text(
                                    CurrencyFormatter.formatBDT(account.balance),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}