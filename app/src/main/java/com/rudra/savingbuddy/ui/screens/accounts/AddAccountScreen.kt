package com.rudra.savingbuddy.ui.screens.accounts

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import com.rudra.savingbuddy.domain.model.AccountType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    navController: NavController,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Link New Account", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
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
            // Account Type Selection
            Text("Select type", style = MaterialTheme.typography.titleMedium)
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val types = listOf(
                    Triple(AccountType.BANK, "Bank Account", "🏦"),
                    Triple(AccountType.MOBILE_BANKING, "Mobile Banking", "📱"),
                    Triple(AccountType.WALLET, "Wallet", "💼")
                )
                items(types) { (type, label, emoji) ->
                    Card(
                        modifier = Modifier
                            .width(100.dp)
                            .clickable { viewModel.selectType(type) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (uiState.selectedType == type)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(emoji, style = MaterialTheme.typography.headlineMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            // Provider Selection
            if (uiState.selectedType != null) {
                Text("Provider", style = MaterialTheme.typography.titleMedium)
                
                val providers = AccountProvider.entries.filter { 
                    it.type == uiState.selectedType 
                }
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(providers) { provider ->
                        Card(
                            modifier = Modifier
                                .clickable { viewModel.selectProvider(provider) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (uiState.selectedProvider == provider)
                                    MaterialTheme.colorScheme.primaryContainer
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(provider.icon, style = MaterialTheme.typography.titleMedium)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(provider.displayName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }

            // Custom Provider Name
            if (uiState.showCustomProviderName) {
                OutlinedTextField(
                    value = uiState.customProviderName,
                    onValueChange = { viewModel.updateCustomProviderName(it) },
                    label = { Text("Provider Name") },
                    placeholder = { Text("e.g., My Bank Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }

            // Account Number
            OutlinedTextField(
                value = uiState.accountNumber,
                onValueChange = { viewModel.updateAccountNumber(it) },
                label = { Text(if (uiState.selectedType == AccountType.WALLET) "Wallet Name" else "Account Number") },
                placeholder = { Text(if (uiState.selectedType == AccountType.WALLET) "e.g., My Cash" else "e.g., 017XX-XXXXXX") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Nickname
            OutlinedTextField(
                value = uiState.nickname,
                onValueChange = { viewModel.updateNickname(it) },
                label = { Text("Nickname (optional)") },
                placeholder = { Text("e.g., Personal bKash") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Initial Balance
            OutlinedTextField(
                value = uiState.initialBalance,
                onValueChange = { viewModel.updateInitialBalance(it) },
                label = { Text("Initial Balance") },
                leadingIcon = { Text("৳", style = MaterialTheme.typography.titleLarge) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Daily Limit info
            uiState.selectedProvider?.let { provider ->
                if (provider.dailyTransferLimit > 0) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Daily transfer limit: ৳${provider.dailyTransferLimit.toLong()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Error
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // Save Button
            Button(
                onClick = { viewModel.saveAccount() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = uiState.canSave && !uiState.isLoading
            ) {
                Icon(Icons.Default.Link, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Link Account", fontWeight = FontWeight.Bold)
            }
        }
    }

    // Success Dialog
    if (uiState.isSaved) {
        AlertDialog(
            onDismissRequest = { navController.popBackStack() },
            icon = { Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp)) },
            title = { Text("Account Linked!") },
            text = { Text("${uiState.nickname.ifBlank { uiState.selectedProvider?.displayName }} has been added successfully.") },
            confirmButton = {
                Button(onClick = { navController.popBackStack() }) {
                    Text("Done")
                }
            }
        )
    }
}