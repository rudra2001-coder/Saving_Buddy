package com.rudra.savingbuddy.ui.screens.add

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.SupportedCurrencies
import com.rudra.savingbuddy.domain.model.RecurringInterval
import com.rudra.savingbuddy.ui.screens.income.IncomeViewModel
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(
    navController: NavController,
    viewModel: IncomeViewModel = hiltViewModel()
) {
    var source by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(IncomeCategory.SALARY) }
    var selectedInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
    var isRecurring by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(SupportedCurrencies.ALL.find { it.code == "BDT" } ?: SupportedCurrencies.DEFAULT) }
    var showAdvanced by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadAccountsForSelection { accountList ->
            accounts = accountList
            selectedAccount = accountList.firstOrNull { it.type.name == "WALLET" } ?: accountList.firstOrNull()
        }
    }

    val quickAmounts = listOf(1000, 2000, 5000, 10000, 20000, 50000)

    val categoryColors = mapOf(
        IncomeCategory.SALARY to Color(0xFF4CAF50),
        IncomeCategory.FREELANCE to Color(0xFF2196F3),
        IncomeCategory.INVESTMENTS to Color(0xFF9C27B0),
        IncomeCategory.BUSINESS to Color(0xFFFF9800),
        IncomeCategory.GIFTS to Color(0xFFE91E63),
        IncomeCategory.OTHERS to Color(0xFF607D8B)
    )

    val categoryEmojis = mapOf(
        IncomeCategory.SALARY to "💰",
        IncomeCategory.FREELANCE to "💻",
        IncomeCategory.INVESTMENTS to "📈",
        IncomeCategory.BUSINESS to "🏢",
        IncomeCategory.GIFTS to "🎁",
        IncomeCategory.OTHERS to "💵"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Add Income", 
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAdvanced = !showAdvanced }) {
                        Icon(
                            if (showAdvanced) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = "Advanced"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Hero Amount Card with Gradient
            Box(
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
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "How much did you receive?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Amount Display
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = selectedCurrency.symbol,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            color = IncomeGreen
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        OutlinedTextField(
                            value = amount,
                            onValueChange = { 
                                if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                    amount = it
                                }
                            },
                            placeholder = { 
                                Text(
                                    "0", 
                                    style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                ) 
                            },
                            textStyle = MaterialTheme.typography.displaySmall.copy(
                                fontWeight = FontWeight.Bold,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            ),
                            modifier = Modifier.widthIn(max = 200.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color.Transparent,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Amount Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.take(3).forEach { quickAmount ->
                            QuickIncomeAmountChip(
                                amount = quickAmount,
                                currency = selectedCurrency,
                                onClick = { amount = quickAmount.toString() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickAmounts.drop(3).forEach { quickAmount ->
                            QuickIncomeAmountChip(
                                amount = quickAmount,
                                currency = selectedCurrency,
                                onClick = { amount = quickAmount.toString() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // Content Section
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                // Source Input
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source / Title") },
                    placeholder = { Text("e.g., Salary, Freelance work") },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null, tint = IncomeGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IncomeGreen,
                        focusedLeadingIconColor = IncomeGreen
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Account Selection
                Text(
                    text = "Add to Account",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                
                IncomeAccountSelector(
                    selectedAccount = selectedAccount,
                    accounts = accounts,
                    onAccountSelect = { selectedAccount = it },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Category Section
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                // Category Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(IncomeCategory.SALARY, IncomeCategory.FREELANCE, IncomeCategory.INVESTMENTS).forEach { category ->
                            ModernIncomeCategoryChip(
                                text = category.displayName,
                                emoji = categoryEmojis[category] ?: "💵",
                                selected = selectedCategory == category,
                                color = categoryColors[category] ?: Color(0xFF607D8B),
                                onClick = { selectedCategory = category },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(IncomeCategory.BUSINESS, IncomeCategory.GIFTS, IncomeCategory.OTHERS).forEach { category ->
                            ModernIncomeCategoryChip(
                                text = category.displayName,
                                emoji = categoryEmojis[category] ?: "💵",
                                selected = selectedCategory == category,
                                color = categoryColors[category] ?: Color(0xFF607D8B),
                                onClick = { selectedCategory = category },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Card
                ModernIncomeDateCard(
                    date = selectedDate,
                    onClick = { showDatePicker = true }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Recurring Toggle Card
                ModernIncomeToggleCard(
                    title = "Recurring Income",
                    subtitle = if (isRecurring) "This income arrives ${selectedInterval.displayName.lowercase()}" else "Track recurring income",
                    icon = Icons.Default.TrendingUp,
                    isEnabled = isRecurring,
                    onToggle = { isRecurring = it },
                    color = IncomeGreen
                )

                if (isRecurring) {
                    Spacer(modifier = Modifier.height(12.dp))
                    // Interval Selection
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(RecurringInterval.WEEKLY, RecurringInterval.MONTHLY, RecurringInterval.YEARLY).forEach { interval ->
                            FilterChip(
                                selected = selectedInterval == interval,
                                onClick = { selectedInterval = interval },
                                label = { Text(interval.displayName) },
                                modifier = Modifier.weight(1f),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                                    selectedLabelColor = IncomeGreen
                                )
                            )
                        }
                    }
                }

                if (showAdvanced) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Currency Selection
                    Text(
                        text = "Currency",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ExposedDropdownMenuBox(
                        expanded = currencyExpanded,
                        onExpandedChange = { currencyExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = "${selectedCurrency.symbol} ${selectedCurrency.code}",
                            onValueChange = {},
                            readOnly = true,
                            leadingIcon = { Icon(Icons.Default.CurrencyExchange, null) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = currencyExpanded,
                            onDismissRequest = { currencyExpanded = false }
                        ) {
                            SupportedCurrencies.ALL.forEach { currency ->
                                DropdownMenuItem(
                                    text = { Text("${currency.symbol} ${currency.code}") },
                                    onClick = { 
                                        selectedCurrency = currency
                                        currencyExpanded = false 
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes") },
                        placeholder = { Text("Add details...") },
                        leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Save Button with Animation
                val buttonScale by animateFloatAsState(
                    targetValue = if (amount.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0 && source.isNotBlank()) 1f else 0.95f,
                    animationSpec = tween(200),
                    label = "button_scale"
                )

                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull() ?: 0.0
                        if (amountValue > 0) {
                            val sourceValue = source.ifBlank { selectedCategory.displayName }
                            viewModel.saveIncome(
                                source = sourceValue,
                                amount = amountValue,
                                category = selectedCategory,
                                date = selectedDate,
                                isRecurring = isRecurring,
                                recurringInterval = if (isRecurring) selectedInterval else null,
                                notes = notes.ifBlank { null },
                                accountId = selectedAccount?.id
                            )
                            navController.popBackStack()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .scale(buttonScale),
                    enabled = amount.isNotBlank() && source.isNotBlank() && (amount.toDoubleOrNull() ?: 0.0) > 0,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
                ) {
                    Icon(Icons.Default.Savings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Add Income",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun QuickIncomeAmountChip(
    amount: Int,
    currency: com.rudra.savingbuddy.domain.model.Currency,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = "${currency.symbol}$amount",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ModernIncomeCategoryChip(
    text: String,
    emoji: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = if (selected) color.copy(alpha = 0.2f) else Color.Transparent,
        animationSpec = tween(200),
        label = "category_color"
    )

    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = tween(200),
        label = "category_scale"
    )

    Surface(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) color else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        color = animatedColor,
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ModernIncomeDateCard(
    date: Long,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = IncomeGreen
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Date",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = DateUtils.formatDate(date),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernIncomeToggleCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) color.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isEnabled) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = if (isEnabled) color else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = color,
                    checkedTrackColor = color.copy(alpha = 0.3f)
                )
            )
        }
    }
}

@Composable
fun IncomeAccountSelector(
    selectedAccount: Account?,
    accounts: List<Account>,
    onAccountSelect: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    
    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
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
                        .background(IncomeGreen.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = selectedAccount?.let { 
                            when {
                                it.type.name == "WALLET" -> "💵"
                                it.type.name == "BANK" -> "🏦"
                                it.type.name == "MOBILE_BANKING" -> "📱"
                                else -> "💳"
                            }
                        } ?: "💵",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedAccount?.name ?: "Select Account",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    selectedAccount?.let {
                        Text(
                            text = "Balance: ${CurrencyFormatter.formatBDT(it.balance)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth(0.9f)
        ) {
            accounts.forEach { account ->
                DropdownMenuItem(
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when {
                                    account.type.name == "WALLET" -> "💵"
                                    account.type.name == "BANK" -> "🏦"
                                    account.type.name == "MOBILE_BANKING" -> "📱"
                                    else -> "💳"
                                },
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(account.name, fontWeight = FontWeight.Medium)
                                Text(
                                    CurrencyFormatter.formatBDT(account.balance),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    onClick = {
                        onAccountSelect(account)
                        expanded = false
                    }
                )
            }
        }
    }
}
