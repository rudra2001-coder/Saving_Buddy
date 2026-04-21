package com.rudra.savingbuddy.ui.screens.accounts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.AccountType
import com.rudra.savingbuddy.domain.model.Transfer
import com.rudra.savingbuddy.domain.model.TransferStatus
import com.rudra.savingbuddy.ui.navigation.Screen
import com.rudra.savingbuddy.ui.theme.*
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
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        uiState.account?.let { Color(it.iconColor) } ?: MaterialTheme.colorScheme.primary,
                                        uiState.account?.let { Color(it.iconColor) }?.copy(alpha = 0.7f) ?: MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f)
                                    )
                                )
                            )
                            .padding(28.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    imageVector = getAccountIcon(uiState.account?.type),
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.9f),
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Current Balance",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White.copy(alpha = 0.9f)
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                CurrencyFormatter.formatBDT(uiState.account?.balance ?: 0.0),
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Surface(
                                color = Color.White.copy(alpha = 0.2f),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    "Last updated: ${uiState.account?.let { DateUtils.formatDate(it.lastUpdated) } ?: "N/A"}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionButton(
                        title = "Add Money",
                        icon = Icons.Outlined.AddCircle,
                        color = IncomeGreen,
                        onClick = { viewModel.showAddMoneyDialog() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Cash Out",
                        icon = Icons.Outlined.RemoveCircle,
                        color = ExpenseRed,
                        onClick = { viewModel.showCashOutDialog() },
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "Send",
                        icon = Icons.Outlined.Send,
                        color = SavingsBlue,
                        onClick = { navController.navigate(Screen.Transfer.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            uiState.account?.dailyLimit?.let { limit ->
                if (limit > 0) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Outlined.Speed,
                                            contentDescription = null,
                                            tint = Color(0xFFFF9800),
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            "Daily Transfer Limit",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                val progress = ((uiState.account?.usedToday ?: 0.0) / limit).toFloat().coerceIn(0f, 1f)
                                val animatedProgress by animateFloatAsState(
                                    targetValue = progress,
                                    animationSpec = tween(500),
                                    label = "limit_progress"
                                )
                                LinearProgressIndicator(
                                    progress = { animatedProgress },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp)),
                                    color = when {
                                        progress >= 0.9f -> ExpenseRed
                                        progress >= 0.7f -> Color(0xFFFF9800)
                                        else -> IncomeGreen
                                    },
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "${CurrencyFormatter.formatBDT(limit - (uiState.account?.usedToday ?: 0.0))} remaining of ${CurrencyFormatter.formatBDT(limit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    "Balance History (Last 7 days)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
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
                                            RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp)
                                        )
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    day,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Transactions",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (uiState.transfers.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                Icons.Outlined.SwapHoriz,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = SavingsBlue.copy(alpha = 0.5f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
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

            item { Spacer(modifier = Modifier.height(24.dp)) }
        }
    }

    if (uiState.showAddMoneyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideAddMoneyDialog() },
            icon = {
                Icon(
                    Icons.Default.AddCircle,
                    contentDescription = null,
                    tint = IncomeGreen,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Add Money", fontWeight = FontWeight.Bold) },
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
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
                    enabled = uiState.addMoneyAmount.isNotBlank() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen),
                    shape = RoundedCornerShape(12.dp)
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

    if (uiState.showCashOutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCashOutDialog() },
            icon = {
                Icon(
                    Icons.Default.RemoveCircle,
                    contentDescription = null,
                    tint = ExpenseRed,
                    modifier = Modifier.size(40.dp)
                )
            },
            title = { Text("Cash Out", fontWeight = FontWeight.Bold) },
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
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
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
                    enabled = uiState.cashOutAmount.isNotBlank() && !uiState.isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(12.dp)
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

    if (uiState.operationResult?.success == true) {
        AlertDialog(
            onDismissRequest = { viewModel.clearResult() },
            icon = {
                Icon(Icons.Default.CheckCircle, null, tint = IncomeGreen, modifier = Modifier.size(48.dp))
            },
            title = { Text("Success!", fontWeight = FontWeight.Bold) },
            text = {
                Text("New balance: ${CurrencyFormatter.formatBDT(uiState.operationResult?.toAccountNewBalance ?: uiState.operationResult?.fromAccountNewBalance ?: 0.0)}")
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.clearResult() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
private fun getAccountIcon(type: AccountType?): androidx.compose.ui.graphics.vector.ImageVector {
    return when (type) {
        AccountType.WALLET -> Icons.Default.AccountBalanceWallet
        AccountType.BANK -> Icons.Default.AccountBalance
        AccountType.MOBILE_BANKING -> Icons.Default.PhoneAndroid
        AccountType.DIGITAL_WALLET -> Icons.Default.CreditCard
        AccountType.CREDIT_CARD -> Icons.Default.CreditCard
        null -> Icons.Default.AccountBalanceWallet
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

@Composable
fun TransferItem(
    transfer: Transfer,
    fromAccountName: String,
    toAccountName: String,
    accounts: List<com.rudra.savingbuddy.domain.model.Account>
) {
    val isSent = transfer.toAccountId != transfer.fromAccountId

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSent) ExpenseRed.copy(alpha = 0.15f)
                        else IncomeGreen.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSent) Icons.Outlined.Output else Icons.Outlined.Input,
                    null,
                    tint = if (isSent) ExpenseRed else IncomeGreen,
                    modifier = Modifier.size(22.dp)
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
                color = if (isSent) ExpenseRed else IncomeGreen
            )
        }
    }
}