package com.rudra.savingbuddy.ui.screens.accounts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.AccountProvider
import com.rudra.savingbuddy.domain.model.AccountType
import com.rudra.savingbuddy.ui.theme.IncomeGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAccountScreen(
    navController: NavController,
    viewModel: AddAccountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add New Account", 
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
            // Account Type Selection Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "What type of account?",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        val types = listOf(
                            Triple(AccountType.WALLET, "Cash / Wallet", "💵"),
                            Triple(AccountType.BANK, "Bank Account", "🏦"),
                            Triple(AccountType.MOBILE_BANKING, "Mobile Banking", "📱"),
                            Triple(AccountType.DIGITAL_WALLET, "Digital Wallet", "💳")
                        )
                        items(types) { (type, label, emoji) ->
                            val isSelected = uiState.selectedType == type
                            
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    viewModel.selectType(type)
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                },
                                label = {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        Text(emoji, style = MaterialTheme.typography.headlineSmall)
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelMedium,
                                            maxLines = 1
                                        )
                                    }
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = IncomeGreen
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    selectedBorderColor = IncomeGreen,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                modifier = Modifier.width(85.dp).height(100.dp)
                            )
                        }
                    }
                }
            }

            // Provider Selection (when type is selected)
            if (uiState.selectedType != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp)
                    ) {
                        Text(
                            "Select Provider",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            "Choose your service provider",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        val providers = AccountProvider.entries.filter { 
                            it.type == uiState.selectedType 
                        }
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(providers) { provider ->
                                val isSelected = uiState.selectedProvider == provider
                                
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        viewModel.selectProvider(provider)
                                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                    },
                                    label = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                                        ) {
                                            Text(provider.icon)
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                provider.displayName,
                                                maxLines = 1,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                                        selectedLabelColor = IncomeGreen
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                        selectedBorderColor = IncomeGreen,
                                        enabled = true,
                                        selected = isSelected
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Custom Provider Name (if "Other" is selected)
            if (uiState.showCustomProviderName) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            "Enter Provider Name",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedTextField(
                            value = uiState.customProviderName,
                            onValueChange = { viewModel.updateCustomProviderName(it) },
                            label = { Text("Provider Name") },
                            placeholder = { Text("e.g., Bank Asia, SureCash") },
                            leadingIcon = { Icon(Icons.Default.Business, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IncomeGreen
                            )
                        )
                    }
                }
            }

            // Account Details Card
            if (uiState.selectedProvider != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            "Account Details",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        // Account Number / Wallet Name
                        OutlinedTextField(
                            value = uiState.accountNumber,
                            onValueChange = { viewModel.updateAccountNumber(it) },
                            label = { 
                                Text(
                                    if (uiState.selectedType == AccountType.WALLET) "Wallet Name" 
                                    else "Account Number"
                                ) 
                            },
                            placeholder = { 
                                Text(
                                    if (uiState.selectedType == AccountType.WALLET) "e.g., My Main Wallet" 
                                    else "e.g., 017XX-XXXXXX"
                                ) 
                            },
                            leadingIcon = { 
                                Icon(
                                    if (uiState.selectedType == AccountType.WALLET) Icons.Default.AccountBalanceWallet 
                                    else Icons.Default.CreditCard, 
                                    null
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IncomeGreen
                            )
                        )

                        // Nickname (optional)
                        OutlinedTextField(
                            value = uiState.nickname,
                            onValueChange = { viewModel.updateNickname(it) },
                            label = { Text("Nickname (optional)") },
                            placeholder = { Text("e.g., Personal bKash") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IncomeGreen
                            )
                        )

                        // Initial Balance
                        OutlinedTextField(
                            value = uiState.initialBalance,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    viewModel.updateInitialBalance(newValue)
                                }
                            },
                            label = { Text("Initial Balance (optional)") },
                            placeholder = { Text("0") },
                            leadingIcon = { 
                                Text(
                                    "৳",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                ) 
                            },
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Decimal,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = { 
                                    if (uiState.canSave) {
                                        viewModel.saveAccount()
                                    }
                                }
                            ),
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = IncomeGreen,
                                focusedLeadingIconColor = IncomeGreen
                            )
                        )

                        // Daily Limit Info
                        val provider = uiState.selectedProvider
                        if (provider != null && provider.dailyTransferLimit > 0) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        "Daily transfer limit: ৳${provider.dailyTransferLimit.toLong()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error Message
            uiState.error?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            error,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Save Button
            val buttonScale by animateFloatAsState(
                targetValue = if (uiState.canSave) 1f else 0.98f,
                animationSpec = tween(200),
                label = "button_scale"
            )

            Button(
                onClick = { 
                    viewModel.saveAccount()
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .scale(buttonScale),
                enabled = uiState.canSave && !uiState.isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = IncomeGreen,
                    disabledContainerColor = IncomeGreen.copy(alpha = 0.5f)
                ),
                elevation = ButtonDefaults.buttonElevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 8.dp
                )
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        when (uiState.selectedType) {
                            AccountType.WALLET -> Icons.Default.AccountBalanceWallet
                            AccountType.BANK -> Icons.Default.AccountBalance
                            AccountType.MOBILE_BANKING -> Icons.Default.PhoneAndroid
                            AccountType.DIGITAL_WALLET -> Icons.Default.CreditCard
                            else -> Icons.Default.Add
                        },
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add Account",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Success Dialog
    if (uiState.isSaved) {
        AlertDialog(
            onDismissRequest = { navController.popBackStack() },
            icon = { 
                Icon(
                    Icons.Default.CheckCircle, 
                    null, 
                    tint = IncomeGreen, 
                    modifier = Modifier.size(48.dp)
                ) 
            },
            title = { Text("Account Added!") },
            text = { 
                Text(
                    "${uiState.nickname.ifBlank { uiState.selectedProvider?.displayName }} has been linked successfully."
                ) 
            },
            confirmButton = {
                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                    Text("Done")
                }
            }
        )
    }
}
}
