package com.rudra.savingbuddy.ui.screens.add

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.domain.model.Account
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.SupportedCurrencies
import com.rudra.savingbuddy.domain.model.RecurringInterval
import com.rudra.savingbuddy.ui.screens.expense.ExpenseViewModel
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var amountError by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var isRecurring by remember { mutableStateOf(false) }
    var selectedInterval by remember { mutableStateOf(RecurringInterval.MONTHLY) }
    var currencyExpanded by remember { mutableStateOf(false) }
    var selectedCurrency by remember { mutableStateOf(SupportedCurrencies.ALL.find { it.code == "BDT" } ?: SupportedCurrencies.DEFAULT) }
    var showAdvanced by remember { mutableStateOf(false) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    val focusManager = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    val quickAmounts = listOf(100, 200, 500, 1000, 2000, 5000)

    val categoryColors = mapOf(
        ExpenseCategory.FOOD to Color(0xFFFF7043),
        ExpenseCategory.TRANSPORT to Color(0xFF42A5F5),
        ExpenseCategory.BILLS to Color(0xFFFFCA28),
        ExpenseCategory.SHOPPING to Color(0xFFAB47BC),
        ExpenseCategory.ENTERTAINMENT to Color(0xFFE91E63),
        ExpenseCategory.HEALTH to Color(0xFF26A69A),
        ExpenseCategory.EDUCATION to Color(0xFF5C6BC0),
        ExpenseCategory.GIFTS to Color(0xFFFF9800),
        ExpenseCategory.TRAVEL to Color(0xFF9C27B0),
        ExpenseCategory.SUBSCRIPTIONS to Color(0xFF00BCD4),
        ExpenseCategory.RENT to Color(0xFF795548),
        ExpenseCategory.UTILITY to Color(0xFFFF9800),
        ExpenseCategory.INSURANCE to Color(0xFF2196F3),
        ExpenseCategory.TAX to Color(0xFFF44336),
        ExpenseCategory.EMI to Color(0xFF9C27B0),
        ExpenseCategory.OTHERS to Color(0xFF78909C)
    )

    val categoryEmojis = mapOf(
        ExpenseCategory.FOOD to "🍔",
        ExpenseCategory.TRANSPORT to "🚌",
        ExpenseCategory.BILLS to "💡",
        ExpenseCategory.SHOPPING to "🛒",
        ExpenseCategory.ENTERTAINMENT to "🎬",
        ExpenseCategory.HEALTH to "💊",
        ExpenseCategory.EDUCATION to "📚",
        ExpenseCategory.GIFTS to "🎁",
        ExpenseCategory.TRAVEL to "✈️",
        ExpenseCategory.SUBSCRIPTIONS to "📱",
        ExpenseCategory.RENT to "🏠",
        ExpenseCategory.UTILITY to "⚡",
        ExpenseCategory.INSURANCE to "🛡️",
        ExpenseCategory.TAX to "📋",
        ExpenseCategory.EMI to "🏦",
        ExpenseCategory.OTHERS to "📦"
    )

    val selectedColor = categoryColors[selectedCategory] ?: ExpenseRed

    LaunchedEffect(Unit) {
        viewModel.loadAccountsForSelection { accountList ->
            accounts = accountList
            selectedAccount = accountList.firstOrNull { it.type.name == "WALLET" } ?: accountList.firstOrNull()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Add Expense",
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
            // Hero Amount Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
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
                                    selectedColor.copy(alpha = 0.15f),
                                    selectedColor.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "How much did you spend?",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Large Amount Input
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amount = newValue
                                amountError = when {
                                    newValue.isEmpty() -> null
                                    newValue.toDoubleOrNull() == null -> "Invalid amount"
                                    newValue.toDouble() <= 0 -> "Amount must be greater than 0"
                                    else -> null
                                }
                            }
                        },
                        placeholder = {
                            Text(
                                "0",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                ),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        },
                        textStyle = MaterialTheme.typography.displaySmall.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            fontSize = 42.sp
                        ),
                        leadingIcon = {
                            Text(
                                selectedCurrency.symbol,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = selectedColor
                            )
                        },
                        trailingIcon = {
                            if (amount.isNotEmpty()) {
                                IconButton(onClick = { amount = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(
                            onNext = { focusManager.moveFocus(androidx.compose.ui.focus.FocusDirection.Down) }
                        ),
                        singleLine = true,
                        isError = amountError != null,
                        supportingText = amountError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = selectedColor,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedLeadingIconColor = selectedColor
                        ),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Quick Amount Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickAmounts) { quickAmount ->
                            FilterChip(
                                selected = amount.toDoubleOrNull() == quickAmount.toDouble(),
                                onClick = {
                                    amount = quickAmount.toString()
                                    amountError = null
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                },
                                label = { Text("${selectedCurrency.symbol}$quickAmount") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = selectedColor.copy(alpha = 0.2f),
                                    selectedLabelColor = selectedColor
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                    selectedBorderColor = selectedColor,
                                    enabled = true,
                                    selected = amount.toDoubleOrNull() == quickAmount.toDouble()
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Content Section
            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Account Selection Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Pay from Account",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        ExpenseAccountSelector(
                            selectedAccount = selectedAccount,
                            accounts = accounts,
                            onAccountSelect = { selectedAccount = it },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Category Selection
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(ExpenseCategory.entries.toList()) { category ->
                        val isSelected = selectedCategory == category
                        val categoryColor = categoryColors[category] ?: ExpenseRed

                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = category
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            },
                            label = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(categoryEmojis[category] ?: "📦")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(category.displayName, maxLines = 1)
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = categoryColor.copy(alpha = 0.2f),
                                selectedLabelColor = categoryColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                selectedBorderColor = categoryColor,
                                enabled = true,
                                selected = isSelected
                            ),
                            modifier = Modifier.height(40.dp)
                        )
                    }
                }

                // Date & Time Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = DateUtils.formatDate(selectedDate),
                        onValueChange = {},
                        label = { Text("Date") },
                        leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showDatePicker = true },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )

                    OutlinedTextField(
                        value = DateUtils.formatTime(selectedDate),
                        onValueChange = {},
                        label = { Text("Time") },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                        trailingIcon = {
                            IconButton(onClick = { showTimePicker = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Select Time")
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showTimePicker = true },
                        readOnly = true,
                        enabled = false,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            disabledTextColor = MaterialTheme.colorScheme.onSurface,
                            disabledBorderColor = MaterialTheme.colorScheme.outline,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }

                // Recurring Toggle
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
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
                                Icons.Default.Repeat,
                                contentDescription = null,
                                tint = if (isRecurring) selectedColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    "Recurring Expense",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    if (isRecurring) selectedInterval.displayName else "Set up recurring",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = {
                                isRecurring = it
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = selectedColor,
                                checkedTrackColor = selectedColor.copy(alpha = 0.3f)
                            )
                        )
                    }
                }

                // Advanced Section
                if (showAdvanced) {
                    AnimatedVisibility(visible = true) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            // Notes
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { notes = it },
                                label = { Text("Notes (optional)") },
                                placeholder = { Text("Add details...") },
                                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                                modifier = Modifier.fillMaxWidth(),
                                maxLines = 3,
                                shape = RoundedCornerShape(16.dp)
                            )

                            // Currency Selection
                            ExposedDropdownMenuBox(
                                expanded = currencyExpanded,
                                onExpandedChange = { currencyExpanded = it }
                            ) {
                                OutlinedTextField(
                                    value = "${selectedCurrency.symbol} ${selectedCurrency.code}",
                                    onValueChange = {},
                                    readOnly = true,
                                    label = { Text("Currency") },
                                    leadingIcon = { Icon(Icons.Default.CurrencyExchange, contentDescription = null) },
                                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = currencyExpanded) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .menuAnchor(),
                                    shape = RoundedCornerShape(16.dp)
                                )
                                ExposedDropdownMenu(
                                    expanded = currencyExpanded,
                                    onDismissRequest = { currencyExpanded = false }
                                ) {
                                    SupportedCurrencies.ALL.forEach { currency ->
                                        DropdownMenuItem(
                                            text = { Text("${currency.symbol} ${currency.code} - ${currency.name}") },
                                            onClick = {
                                                selectedCurrency = currency
                                                currencyExpanded = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Save Button
                val buttonScale by animateFloatAsState(
                    targetValue = if (amount.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true) 1f else 0.98f,
                    animationSpec = tween(200),
                    label = "button_scale"
                )

                Button(
                    onClick = {
                        val amountValue = amount.toDoubleOrNull()
                        if (amountValue == null || amountValue <= 0) {
                            amountError = "Please enter a valid amount"
                            return@Button
                        }

                        isSaving = true
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)

                        viewModel.saveExpense(
                            amount = amountValue,
                            category = selectedCategory,
                            date = selectedDate,
                            notes = notes.ifBlank { null },
                            accountId = selectedAccount?.id
                        )
                        navController.popBackStack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .scale(buttonScale),
                    enabled = !isSaving && amount.isNotBlank() && amount.toDoubleOrNull()?.let { it > 0 } == true,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedColor,
                        disabledContainerColor = selectedColor.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(Icons.Default.ShoppingCart, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Add Expense",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time Picker Dialog
    if (showTimePicker) {
        val calendar = java.util.Calendar.getInstance().apply { timeInMillis = selectedDate }
        val timePickerState = rememberTimePickerState(
            initialHour = calendar.get(java.util.Calendar.HOUR_OF_DAY),
            initialMinute = calendar.get(java.util.Calendar.MINUTE)
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Select Time") },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(onClick = {
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, timePickerState.hour)
                    calendar.set(java.util.Calendar.MINUTE, timePickerState.minute)
                    selectedDate = calendar.timeInMillis
                    showTimePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun ExpenseAccountSelector(
    selectedAccount: Account?,
    accounts: List<Account>,
    onAccountSelect: (Account) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedColor = ExpenseRed

    Box(modifier = modifier) {
        OutlinedCard(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
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
                        .background(selectedColor.copy(alpha = 0.2f)),
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
