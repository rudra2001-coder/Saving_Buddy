package com.rudra.savingbuddy.ui.screens.accounts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.AccountType
import com.rudra.savingbuddy.domain.model.TransferResult
import com.rudra.savingbuddy.ui.navigation.Screen
import com.rudra.savingbuddy.ui.theme.*
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

    val animatedAmount by animateFloatAsState(
        targetValue = uiState.totalAmount.toFloat(),
        animationSpec = tween(500),
        label = "amount_animation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Transfer", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Text("Move money between accounts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item { Spacer(modifier = Modifier.height(4.dp)) }

            // Hero Amount Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.verticalGradient(listOf(IncomeGreen.copy(alpha = 0.12f), IncomeGreen.copy(alpha = 0.03f))))
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Total Amount", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(IncomeGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("৳", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = IncomeGreen)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                CurrencyFormatter.formatBDT(animatedAmount.toDouble()),
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                        }
                        if (uiState.fee > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Fee: ${CurrencyFormatter.formatBDT(uiState.fee)}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // From Account
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(IncomeGreen))
                        Icon(Icons.Outlined.Output, null, modifier = Modifier.size(16.dp), tint = IncomeGreen)
                        Text("Source", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = IncomeGreen)
                    }
                    AccountSelectorCard(
                        account = uiState.fromAccount,
                        icon = Icons.Outlined.Output,
                        label = "Select source account",
                        onClick = { showFromPicker = true }
                    )
                }
            }

            // Swap Button
            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    IconButton(
                        onClick = {
                            val from = uiState.fromAccount
                            val to = uiState.toAccount
                            viewModel.setFromAccount(to ?: return@IconButton)
                            viewModel.setToAccount(from ?: return@IconButton)
                        },
                        enabled = uiState.fromAccount != null && uiState.toAccount != null
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).clip(CircleShape).background(IncomeGreen.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.SwapVert, null, tint = IncomeGreen, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // To Account
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SavingsBlue))
                        Icon(Icons.Outlined.Input, null, modifier = Modifier.size(16.dp), tint = SavingsBlue)
                        Text("Destination", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold, color = SavingsBlue)
                    }
                    AccountSelectorCard(
                        account = uiState.toAccount,
                        icon = Icons.Outlined.Input,
                        label = "Select destination account",
                        onClick = { showToPicker = true }
                    )
                }
            }

            // Amount Field
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.TrendingUp, null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                            Text("Amount", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.amount,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    viewModel.updateAmount(newValue)
                                }
                            },
                            label = { Text("Amount") },
                            placeholder = { Text("0") },
                            leadingIcon = { Text("৳", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = IncomeGreen) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IncomeGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                cursorColor = IncomeGreen
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Amount Chips
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("100", "500", "1000", "5000").forEach { preset ->
                                Surface(
                                    modifier = Modifier.clickable { viewModel.updateAmount(preset) },
                                    shape = RoundedCornerShape(10.dp),
                                    color = IncomeGreen.copy(alpha = 0.1f)
                                ) {
                                    Text("৳$preset", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = IncomeGreen)
                                }
                            }
                        }
                    }
                }
            }

            // Fee Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(IncomeGreen.copy(alpha = 0.06f), IncomeGreen.copy(alpha = 0.01f))))
                        .padding(16.dp))
                    {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Receipt, null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                            Text("Summary", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Fee", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(if (uiState.fee == 0.0) "Free" else CurrencyFormatter.formatBDT(uiState.fee),
                                style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface)
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text(CurrencyFormatter.formatBDT(uiState.totalAmount),
                                style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                        }
                    }
                }
            }

            // Note
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Notes, null, tint = IncomeGreen, modifier = Modifier.size(18.dp))
                            Text("Note", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.note,
                            onValueChange = { viewModel.updateNote(it) },
                            label = { Text("Note (optional)") },
                            placeholder = { Text("e.g., Monthly savings transfer") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 2,
                            singleLine = false,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IncomeGreen,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                cursorColor = IncomeGreen
                            )
                        )
                    }
                }
            }

            // Error
            uiState.error?.let { error ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f)),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, ExpenseRed.copy(alpha = 0.3f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Icon(Icons.Default.Error, null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                            Text(error, style = MaterialTheme.typography.bodySmall, color = ExpenseRed)
                        }
                    }
                }
            }

            // Transfer Button
            item {
                Button(
                    onClick = { showConfirmation = true },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    enabled = uiState.canTransfer && !uiState.isLoading,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IncomeGreen,
                        disabledContainerColor = IncomeGreen.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 8.dp)
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(22.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.SwapHoriz, null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Transfer ${CurrencyFormatter.formatBDT(uiState.totalAmount)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
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
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Default.SwapHoriz, null, tint = IncomeGreen, modifier = Modifier.size(40.dp)) },
            title = { Text("Confirm Transfer", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.08f))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Output, null, tint = IncomeGreen, modifier = Modifier.size(16.dp))
                                Text(uiState.fromAccount?.name ?: "", fontWeight = FontWeight.Medium)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.ArrowDownward, null, tint = IncomeGreen, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Outlined.Input, null, tint = SavingsBlue, modifier = Modifier.size(16.dp))
                                Text(uiState.toAccount?.name ?: "", fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                    Text("Amount: ${CurrencyFormatter.formatBDT(uiState.totalAmount)}", fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium, color = IncomeGreen)
                    if (uiState.note.isNotBlank()) {
                        Text("Note: ${uiState.note}", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeTransfer()
                        showConfirmation = false
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) { Text("Confirm Transfer", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) { Text("Cancel") }
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
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Default.CheckCircle, null, tint = IncomeGreen, modifier = Modifier.size(48.dp)) },
            title = { Text("Transfer Successful!", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("${CurrencyFormatter.formatBDT(uiState.totalAmount)} moved successfully",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(uiState.fromAccount?.name ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyFormatter.formatBDT(uiState.transferResult?.fromAccountNewBalance ?: 0.0),
                                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            }
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(uiState.toAccount?.name ?: "", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(CurrencyFormatter.formatBDT(uiState.transferResult?.toAccountNewBalance ?: 0.0),
                                    style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, color = IncomeGreen)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reset()
                        navController.popBackStack()
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) { Text("Done", fontWeight = FontWeight.SemiBold) }
            }
        )
    }

    // Goal Funding Suggestion Dialog
    if (uiState.showGoalSuggestion && uiState.goalSuggestion != null) {
        val suggestion = uiState.goalSuggestion!!
        AlertDialog(
            onDismissRequest = { viewModel.dismissGoalSuggestion() },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Default.Savings, null, tint = SavingsBlue, modifier = Modifier.size(44.dp)) },
            title = { Text("Save to Goal?", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("You just transferred ${CurrencyFormatter.formatBDT(uiState.totalAmount)}")
                    Text("Would you like to add ${CurrencyFormatter.formatBDT(suggestion.suggestedAmount)} to your ${suggestion.goalName} goal?",
                        style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = SavingsBlue.copy(alpha = 0.1f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Goal Progress:", style = MaterialTheme.typography.labelSmall)
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { (suggestion.currentAmount / suggestion.targetAmount).toFloat() },
                                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                                color = SavingsBlue,
                                trackColor = SavingsBlue.copy(alpha = 0.15f)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("${CurrencyFormatter.formatBDT(suggestion.currentAmount)} / ${CurrencyFormatter.formatBDT(suggestion.targetAmount)}",
                                style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.allocateToGoalFromSuggestion() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
                ) { Text("Yes, Save!", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissGoalSuggestion() }) { Text("Not Now") }
            }
        )
    }
}

@Composable
private fun AccountSelectorCard(
    account: Account?,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                    .background(if (account != null) Color(account.iconColor).copy(alpha = 0.12f) else IncomeGreen.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = account?.let { getAccountIcon(it.type) } ?: icon,
                    contentDescription = null,
                    tint = if (account != null) Color(account.iconColor) else IncomeGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(account?.name ?: label, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium,
                    color = if (account != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                account?.let {
                    Text("Balance: ${CurrencyFormatter.formatBDT(it.balance)}",
                        style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Icon(Icons.Default.KeyboardArrowDown, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun getAccountIcon(type: AccountType): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        AccountType.WALLET -> Icons.Default.AccountBalanceWallet
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.MOBILE_BANKING -> Icons.Default.PhoneAndroid
        AccountType.DIGITAL_WALLET -> Icons.Default.CreditCard
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
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
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.AccountBalanceWallet, null, tint = IncomeGreen, modifier = Modifier.size(20.dp))
                Text(title, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { account ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSelect(account) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier.size(40.dp).clip(RoundedCornerShape(12.dp)).background(Color(account.iconColor).copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(getAccountIcon(account.type), null, tint = Color(account.iconColor), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(account.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                Text(CurrencyFormatter.formatBDT(account.balance), style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier.size(18.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}
