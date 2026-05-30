package com.rudra.savingbuddy.ui.screens.accounts

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.AccountType
import com.rudra.savingbuddy.ui.navigation.Screen
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

private val Blue600 = Color(0xFF185FA5)
private val Blue50 = Color(0xFFE6F1FB)
private val Green600 = Color(0xFF3B6D11)
private val Green50 = Color(0xFFEAF3DE)
private val Red600 = Color(0xFFD32F2F)
private val Red50 = Color(0xFFFFEBEE)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.showDeleteSuccess) {
        if (uiState.showDeleteSuccess) {
            kotlinx.coroutines.delay(2000)
            viewModel.dismissDeleteSuccess()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Accounts", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineSmall)
                        Text("Manage your accounts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.Transfer.route) }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer",
                            tint = Blue600)
                    }
                    IconButton(onClick = { navController.navigate(Screen.AddAccount.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account",
                            tint = Green600)
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Net Worth Card
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
                            .background(Brush.verticalGradient(listOf(IncomeGreen.copy(alpha = 0.1f), IncomeGreen.copy(alpha = 0.03f))))
                            .padding(20.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Total Balance",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    CurrencyFormatter.formatBDT(uiState.netWorth),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                                Text(
                                    "${uiState.accounts.size} account${if (uiState.accounts.size != 1) "s" else ""}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Box(
                                modifier = Modifier.size(52.dp).clip(RoundedCornerShape(16.dp)).background(IncomeGreen.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, null, tint = IncomeGreen, modifier = Modifier.size(26.dp))
                            }
                        }
                    }
                }
            }

            // Quick Actions
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    QuickActionCard(
                        title = "Transfer",
                        icon = Icons.Default.SwapHoriz,
                        onClick = { navController.navigate(Screen.Transfer.route) },
                        modifier = Modifier.weight(1f),
                        color = Blue600,
                        bgColor = Blue50
                    )
                    QuickActionCard(
                        title = "Add Account",
                        icon = Icons.Default.Add,
                        onClick = { navController.navigate(Screen.AddAccount.route) },
                        modifier = Modifier.weight(1f),
                        color = Green600,
                        bgColor = Green50
                    )
                }
            }

            // Wallets Section
            if (uiState.wallets.isNotEmpty()) {
                item {
                    SectionHeader(title = "WALLETS", icon = Icons.Default.AccountBalanceWallet, count = uiState.wallets.size)
                }
                items(uiState.wallets, key = { it.id }) { account ->
                    SwipeAccountCard(
                        account = account,
                        onEdit = {
                            viewModel.onSwipeEdit(account)
                            navController.navigate(Screen.EditAccount.createRoute(account.id))
                        },
                        onDelete = { viewModel.onSwipeDelete(account) },
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Bank Accounts Section
            if (uiState.bankAccounts.isNotEmpty()) {
                item {
                    SectionHeader(title = "BANK ACCOUNTS", icon = Icons.Default.AccountBalance, count = uiState.bankAccounts.size)
                }
                items(uiState.bankAccounts, key = { it.id }) { account ->
                    SwipeAccountCard(
                        account = account,
                        onEdit = {
                            viewModel.onSwipeEdit(account)
                            navController.navigate(Screen.EditAccount.createRoute(account.id))
                        },
                        onDelete = { viewModel.onSwipeDelete(account) },
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Mobile Banking Section
            if (uiState.mobileBanking.isNotEmpty()) {
                item {
                    SectionHeader(title = "MOBILE BANKING", icon = Icons.Default.PhoneAndroid, count = uiState.mobileBanking.size)
                }
                items(uiState.mobileBanking, key = { it.id }) { account ->
                    SwipeAccountCard(
                        account = account,
                        onEdit = {
                            viewModel.onSwipeEdit(account)
                            navController.navigate(Screen.EditAccount.createRoute(account.id))
                        },
                        onDelete = { viewModel.onSwipeDelete(account) },
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Digital Wallets Section
            if (uiState.digitalWallets.isNotEmpty()) {
                item {
                    SectionHeader(title = "DIGITAL WALLETS", icon = Icons.Default.CreditCard, count = uiState.digitalWallets.size)
                }
                items(uiState.digitalWallets, key = { it.id }) { account ->
                    SwipeAccountCard(
                        account = account,
                        onEdit = {
                            viewModel.onSwipeEdit(account)
                            navController.navigate(Screen.EditAccount.createRoute(account.id))
                        },
                        onDelete = { viewModel.onSwipeDelete(account) },
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Empty State
            if (uiState.accounts.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier.size(72.dp).clip(CircleShape).background(Blue600.copy(alpha = 0.08f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.AccountBalanceWallet, null, modifier = Modifier.size(36.dp), tint = Blue600.copy(alpha = 0.5f))
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            Text("No accounts yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Add your first account to track your money", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = { navController.navigate(Screen.AddAccount.route) },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Blue600)
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Account", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }

    // Delete Confirmation Dialog (for zero-balance accounts)
    if (uiState.showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(Icons.Default.Warning, null, tint = ExpenseRed, modifier = Modifier.size(48.dp))
            },
            title = { Text("Delete Account?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete ${uiState.swipeTargetAccount?.name ?: "this account"}? This action cannot be undone.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteZeroBalance() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    enabled = !uiState.isProcessing
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Delete", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Transfer Dialog (for accounts with balance > 0)
    if (uiState.showTransferDialog) {
        val targetAccount = uiState.swipeTargetAccount
        AlertDialog(
            onDismissRequest = { viewModel.dismissDialogs() },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(Icons.Default.SwapHoriz, null, tint = Blue600, modifier = Modifier.size(48.dp))
            },
            title = { Text("Transfer Balance", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "${targetAccount?.name ?: "This account"} has a balance of ${CurrencyFormatter.formatBDT(targetAccount?.balance ?: 0.0)}. Select an account to transfer the balance before deletion:",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (uiState.otherAccounts.isEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.08f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Info, null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                Text("No other accounts available to transfer balance to.",
                                    color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    } else {
                        uiState.otherAccounts.forEach { account ->
                            val isSelected = uiState.transferTargetAccount?.id == account.id
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectTransferTarget(account) },
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) Blue600.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    if (isSelected) 1.5.dp else 0.5.dp,
                                    if (isSelected) Blue600 else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(Color(account.iconColor).copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            when (account.type) {
                                                AccountType.WALLET -> "💵"
                                                AccountType.BANK -> "🏦"
                                                AccountType.MOBILE_BANKING -> "📱"
                                                AccountType.DIGITAL_WALLET -> "💳"
                                                AccountType.CREDIT_CARD -> "💳"
                                            },
                                            style = MaterialTheme.typography.titleSmall
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(account.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                        Text(CurrencyFormatter.formatBDT(account.balance), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    if (isSelected) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Blue600, modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                    }

                    uiState.error?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.12f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Error, null, tint = ExpenseRed, modifier = Modifier.size(18.dp))
                                Text(error, color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.confirmDeleteWithTransfer() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    enabled = uiState.transferTargetAccount != null && !uiState.isProcessing
                ) {
                    if (uiState.isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Transfer & Delete", fontWeight = FontWeight.SemiBold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDialogs() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Delete Success Snackbar
    if (uiState.showDeleteSuccess) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteSuccess() },
            shape = RoundedCornerShape(24.dp),
            icon = {
                Icon(Icons.Default.CheckCircle, null, tint = IncomeGreen, modifier = Modifier.size(48.dp))
            },
            title = { Text("Account Deleted", fontWeight = FontWeight.Bold) },
            text = {
                Text("The account has been removed successfully.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissDeleteSuccess() },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                    Text("Done", fontWeight = FontWeight.SemiBold)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeAccountCard(
    account: Account,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            when (value) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEdit()
                    false
                }
                SwipeToDismissBoxValue.EndToStart -> {
                    onDelete()
                    false
                }
                SwipeToDismissBoxValue.Settled -> true
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = true,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.75f else 1f,
                animationSpec = tween(200),
                label = "swipe_scale"
            )

            val color by animateColorAsState(
                targetValue = when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> Blue600
                    SwipeToDismissBoxValue.EndToStart -> Red600
                    SwipeToDismissBoxValue.Settled -> Color.Transparent
                },
                animationSpec = tween(200),
                label = "swipe_bg_color"
            )

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(16.dp))
                    .background(color)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = if (direction == SwipeToDismissBoxValue.StartToEnd)
                    Arrangement.Start else Arrangement.End
            ) {
                if (direction == SwipeToDismissBoxValue.StartToEnd) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp).scale(scale)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                } else {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp).scale(scale)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Delete", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        content = {
            AccountCard(
                account = account,
                onClick = onClick
            )
        }
    )
}

@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Blue600,
    bgColor: Color = Blue50
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(RoundedCornerShape(10.dp)).background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int = 0
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier.size(6.dp).clip(CircleShape).background(Blue600)
        )
        Icon(icon, null, modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(title, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
        if (count > 0) {
            Surface(shape = RoundedCornerShape(8.dp), color = Blue50) {
                Text(
                    "$count",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = Blue600,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
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
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp)).background(Color(account.iconColor).copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    when (account.type) {
                        AccountType.WALLET -> "💵"
                        AccountType.BANK -> "🏦"
                        AccountType.MOBILE_BANKING -> "📱"
                        AccountType.DIGITAL_WALLET -> "💳"
                        AccountType.CREDIT_CARD -> "💳"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(account.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(account.accountNumber, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("•", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(account.type.displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text("Updated: ${DateUtils.formatShortDate(account.lastUpdated)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.formatCompact(account.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (account.balance >= 0) IncomeGreen else ExpenseRed
                )
                account.dailyLimit?.let { limit ->
                    if (limit > 0) {
                        val remaining = limit - account.usedToday
                        Text(
                            "${CurrencyFormatter.formatCompact(remaining)} left",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.width(4.dp))
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(18.dp))
        }
    }
}
