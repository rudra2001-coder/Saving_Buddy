package com.rudra.savingbuddy.ui.screens.add

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.ShoppingCart
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
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.ui.screens.expense.ExpenseViewModel
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    navController: NavController,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    var amount by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(ExpenseCategory.FOOD) }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }

    val categoryColors = mapOf(
        ExpenseCategory.FOOD to FoodColor,
        ExpenseCategory.TRANSPORT to TransportColor,
        ExpenseCategory.BILLS to BillsColor,
        ExpenseCategory.SHOPPING to ShoppingColor,
        ExpenseCategory.ENTERTAINMENT to Color(0xFFE91E63),
        ExpenseCategory.HEALTH to Color(0xFF4CAF50),
        ExpenseCategory.EDUCATION to Color(0xFF2196F3),
        ExpenseCategory.GIFTS to Color(0xFFFF9800),
        ExpenseCategory.TRAVEL to Color(0xFF9C27B0),
        ExpenseCategory.SUBSCRIPTIONS to Color(0xFF00BCD4),
        ExpenseCategory.RENT to Color(0xFF795548),
        ExpenseCategory.OTHERS to OthersColor
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
        ExpenseCategory.OTHERS to "📦"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add Expense", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Amount Input (prominent)
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = ExpenseRed.copy(alpha = 0.1f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Amount",
                        style = MaterialTheme.typography.titleMedium,
                        color = ExpenseRed
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = amount,
                        onValueChange = { 
                            if (it.isEmpty() || it.matches(Regex("^\\d*\\.?\\d*$"))) {
                                amount = it
                            }
                        },
                        placeholder = { Text("0.00", style = MaterialTheme.typography.headlineMedium) },
                        textStyle = MaterialTheme.typography.headlineLarge.copy(
                            fontWeight = FontWeight.Bold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        ),
                        leadingIcon = {
                            Text(
                                "₹",
                                style = MaterialTheme.typography.headlineMedium,
                                color = ExpenseRed
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent
                        )
                    )
                }
            }

            // Category Selection
            Text(
                text = "Category",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium
            )
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // Row 1 - Food, Transport, Bills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(ExpenseCategory.FOOD, ExpenseCategory.TRANSPORT, ExpenseCategory.BILLS).forEach { category ->
                        CategoryChip(
                            text = category.displayName,
                            emoji = categoryEmojis[category] ?: "📦",
                            selected = selectedCategory == category,
                            color = categoryColors[category] ?: OthersColor,
                            onClick = { selectedCategory = category },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Row 2 - Shopping, Entertainment, Health
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(ExpenseCategory.SHOPPING, ExpenseCategory.ENTERTAINMENT, ExpenseCategory.HEALTH).forEach { category ->
                        CategoryChip(
                            text = category.displayName,
                            emoji = categoryEmojis[category] ?: "📦",
                            selected = selectedCategory == category,
                            color = categoryColors[category] ?: OthersColor,
                            onClick = { selectedCategory = category },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Row 3 - Education, Gifts, Travel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(ExpenseCategory.EDUCATION, ExpenseCategory.GIFTS, ExpenseCategory.TRAVEL).forEach { category ->
                        CategoryChip(
                            text = category.displayName,
                            emoji = categoryEmojis[category] ?: "📦",
                            selected = selectedCategory == category,
                            color = categoryColors[category] ?: OthersColor,
                            onClick = { selectedCategory = category },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                // Row 4 - Subscriptions, Rent, Others
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(ExpenseCategory.SUBSCRIPTIONS, ExpenseCategory.RENT, ExpenseCategory.OTHERS).forEach { category ->
                        CategoryChip(
                            text = category.displayName,
                            emoji = categoryEmojis[category] ?: "📦",
                            selected = selectedCategory == category,
                            color = categoryColors[category] ?: OthersColor,
                            onClick = { selectedCategory = category },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Date Picker
            OutlinedTextField(
                value = DateUtils.formatDate(selectedDate),
                onValueChange = {},
                label = { Text("Date") },
                leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                readOnly = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Notes
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                placeholder = { Text("Add any additional details...") },
                leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 4,
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (amountValue > 0) {
                        viewModel.saveExpense(
                            amount = amountValue,
                            category = selectedCategory,
                            date = selectedDate,
                            notes = notes.ifBlank { null }
                        )
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = amount.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
            ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Add Expense",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

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
}

@Composable
fun CategoryChip(
    text: String,
    emoji: String,
    selected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = if (selected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
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
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = if (selected) color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}