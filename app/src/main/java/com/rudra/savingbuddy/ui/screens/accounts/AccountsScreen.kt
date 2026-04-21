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
import com.rudra.savingbuddy.ui.theme.IncomeGreen
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountsScreen(
    navController: NavController,
    viewModel: AccountsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Accounts", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.AddAccount.route) }) {
                        Icon(Icons.Default.Add, contentDescription = "Add Account")
                    }
                    IconButton(onClick = { navController.navigate(Screen.Transfer.route) }) {
                        Icon(Icons.Default.SwapHoriz, contentDescription = "Transfer")
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
            // Net Worth Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        IncomeGreen.copy(alpha = 0.15f),
                                        IncomeGreen.copy(alpha = 0.05f)
                                    )
                                )
                            )
                            .padding(24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    "Main Balance",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    CurrencyFormatter.formatBDT(uiState.netWorth),
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = IncomeGreen
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        IncomeGreen.copy(alpha = 0.2f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.AccountBalanceWallet,
                                    null,
                                    tint = IncomeGreen,
                                    modifier = Modifier.size(28.dp)
                                )
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
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionCard(
                        title = "Add Account",
                        icon = Icons.Default.Add,
                        onClick = { navController.navigate(Screen.AddAccount.route) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Wallets Section
            if (uiState.wallets.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "WALLETS",
                        icon = Icons.Default.AccountBalanceWallet
                    )
                }
                items(uiState.wallets) { account ->
                    AccountCard(
                        account = account,
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Bank Accounts Section
            if (uiState.bankAccounts.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "BANK ACCOUNTS",
                        icon = Icons.Default.AccountBalance
                    )
                }
                items(uiState.bankAccounts) { account ->
                    AccountCard(
                        account = account,
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Mobile Banking Section
            if (uiState.mobileBanking.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "MOBILE BANKING",
                        icon = Icons.Default.PhoneAndroid
                    )
                }
                items(uiState.mobileBanking) { account ->
                    AccountCard(
                        account = account,
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Digital Wallets Section
            if (uiState.digitalWallets.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "DIGITAL WALLETS",
                        icon = Icons.Default.CreditCard
                    )
                }
                items(uiState.digitalWallets) { account ->
                    AccountCard(
                        account = account,
                        onClick = { navController.navigate(Screen.AccountDetail.route + "/${account.id}") }
                    )
                }
            }

            // Empty State
            if (uiState.accounts.isEmpty() && !uiState.isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.AccountBalanceWallet,
                                null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No accounts yet")
                            Text(
                                "Add your first account to track your money",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { navController.navigate(Screen.AddAccount.route) }) {
                                Icon(Icons.Default.Add, null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Add Account")
                            }
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val buttonScale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(100),
        label = "button_scale"
    )
    
    Card(
        modifier = modifier
            .scale(buttonScale)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun AccountCard(
    account: Account,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Account Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color(account.iconColor).copy(alpha = 0.2f)),
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
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    account.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    account.accountNumber,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "Updated: ${DateUtils.formatDate(account.lastUpdated)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    CurrencyFormatter.formatBDT(account.balance),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                account.dailyLimit?.let { limit ->
                    if (limit > 0) {
                        val used = account.usedToday
                        val remaining = limit - used
                        Text(
                            "Limit: ${CurrencyFormatter.formatBDT(remaining)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}