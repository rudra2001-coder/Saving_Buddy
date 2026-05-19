package com.rudra.savingbuddy.ui.screens.investment

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.Investment
import com.rudra.savingbuddy.domain.model.InvestmentType
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvestmentScreen(
    onNavigateBack: () -> Unit = {},
    viewModel: InvestmentViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Investment Tracker", fontWeight = FontWeight.Bold)
                        Text("Monitor your portfolio", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Investment", fontWeight = FontWeight.SemiBold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                PortfolioSummaryCard(
                    totalInvested = uiState.totalInvested,
                    totalReturns = uiState.totalReturns,
                    totalValue = uiState.totalCurrentValue,
                    returnRate = uiState.overallReturnRate
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Filter by Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            item {
                                FilterChip(
                                    selected = uiState.selectedType == null,
                                    onClick = { viewModel.selectType(null) },
                                    label = { Text("All") }
                                )
                            }
                            items(InvestmentType.entries) { type ->
                                FilterChip(
                                    selected = uiState.selectedType == type,
                                    onClick = { viewModel.selectType(type) },
                                    label = { Text(type.displayName) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.AccountBalance, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Net Worth", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            NetWorthItem("Total Assets", CurrencyFormatter.format(uiState.totalCurrentValue), PrimaryGreen)
                            NetWorthItem("Net Worth", CurrencyFormatter.format(uiState.totalCurrentValue), SavingsBlue)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Outlined.PieChart, null, tint = PrimaryGreen, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Investment Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Spacer(modifier = Modifier.weight(1f))
                            Text("${uiState.filteredInvestments.size} investments", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        if (uiState.filteredInvestments.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxWidth().padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Outlined.AccountBalance, null,
                                        modifier = Modifier.size(48.dp), tint = TextSecondary.copy(alpha = 0.5f))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No investments yet", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                                    Text("Tap + to add your first investment", style = MaterialTheme.typography.bodySmall, color = TextSecondary.copy(alpha = 0.7f))
                                }
                            }
                        } else {
                            uiState.filteredInvestments.forEach { inv ->
                                InvestmentRow(
                                    investment = inv,
                                    onClick = { viewModel.showEditDialog(inv) },
                                    onTrack = { viewModel.showTrackDialog(inv) },
                                    onDelete = { viewModel.showDeleteConfirm(inv) }
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (uiState.showAddDialog) {
        InvestmentDialog(
            investment = uiState.editingInvestment,
            onDismiss = { viewModel.hideAddDialog() },
            onSave = { name, type, amount, currentValue, purchaseDate, notes ->
                viewModel.saveInvestment(name, type, amount, currentValue, purchaseDate, notes)
            }
        )
    }

    uiState.showTrackDialog?.let { investment ->
        TrackValueDialog(
            investment = investment,
            onDismiss = { viewModel.hideTrackDialog() },
            onSave = { newValue -> viewModel.updateCurrentValue(investment, newValue) }
        )
    }

    uiState.showDeleteConfirm?.let { investment ->
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            shape = RoundedCornerShape(24.dp),
            icon = { Icon(Icons.Default.Delete, null, tint = ExpenseRed, modifier = Modifier.size(40.dp)) },
            title = { Text("Delete Investment?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${investment.name}'?", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = { viewModel.deleteInvestment(investment) },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) { Text("Delete", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) {
                    Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}

@Composable
private fun PortfolioSummaryCard(totalInvested: Double, totalReturns: Double, totalValue: Double, returnRate: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            PrimaryGreen.copy(alpha = 0.3f),
                            SavingsBlue.copy(alpha = 0.2f),
                            BackgroundCard
                        )
                    )
                )
                .padding(24.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Text("Portfolio Value", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                Spacer(modifier = Modifier.height(8.dp))
                Text(CurrencyFormatter.format(totalValue), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = if (totalReturns >= 0) PrimaryGreen.copy(alpha = 0.2f) else ExpenseRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            if (totalReturns >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null,
                            tint = if (totalReturns >= 0) PrimaryGreen else ExpenseRed,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            "${if (totalReturns >= 0) "+" else ""}${CurrencyFormatter.format(totalReturns)} (${String.format("%.1f", returnRate)}%)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (totalReturns >= 0) PrimaryGreen else ExpenseRed
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Invested", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(CurrencyFormatter.format(totalInvested), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Returns", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text(CurrencyFormatter.format(totalReturns), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (totalReturns >= 0) PrimaryGreen else ExpenseRed)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ROI", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                        Text("${String.format("%.1f", returnRate)}%", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (returnRate >= 0) PrimaryGreen else ExpenseRed)
                    }
                }
            }
        }
    }
}

@Composable
private fun InvestmentRow(investment: Investment, onClick: () -> Unit, onTrack: () -> Unit, onDelete: () -> Unit) {
    val isPositive = investment.returns >= 0
    val invColor = getInvestmentColor(investment.type)
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BackgroundCard),
        border = BorderStroke(0.5.dp, BorderLight.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp).clip(CircleShape).background(invColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(getInvestmentIcon(investment.type), null, tint = invColor, modifier = Modifier.size(20.dp))
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(investment.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                Text(investment.type.displayName, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(CurrencyFormatter.format(investment.amount), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isPositive) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                        null,
                        tint = if (isPositive) PrimaryGreen else ExpenseRed,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        "${if (isPositive) "+" else ""}${String.format("%.1f", investment.returnPercentage)}%",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isPositive) PrimaryGreen else ExpenseRed
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onTrack, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.TrendingUp, "Track", modifier = Modifier.size(18.dp), tint = SavingsBlue)
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, "Delete", modifier = Modifier.size(18.dp), tint = ExpenseRed.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun NetWorthItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun InvestmentDialog(
    investment: Investment?,
    onDismiss: () -> Unit,
    onSave: (String, InvestmentType, Double, Double, Long, String?) -> Unit
) {
    var name by remember { mutableStateOf(investment?.name ?: "") }
    var selectedType by remember { mutableStateOf(investment?.type ?: InvestmentType.STOCK) }
    var amount by remember { mutableStateOf(investment?.amount?.toString() ?: "") }
    var currentValue by remember { mutableStateOf(investment?.currentValue?.toString() ?: "") }
    var purchaseDate by remember { mutableStateOf(investment?.purchaseDate ?: System.currentTimeMillis()) }
    var notes by remember { mutableStateOf(investment?.notes ?: "") }
    var typeExpanded by remember { mutableStateOf(false) }
    var showDatePicker by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(if (investment != null) Icons.Default.Edit else Icons.Default.TrendingUp, null,
                    tint = PrimaryGreen, modifier = Modifier.size(22.dp))
                Text(if (investment != null) "Edit Investment" else "Add Investment", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Investment Name") },
                    leadingIcon = { Icon(Icons.Outlined.AccountBalance, null, tint = PrimaryGreen) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight.copy(alpha = 0.5f),
                        cursorColor = PrimaryGreen
                    )
                )

                ExposedDropdownMenuBox(expanded = typeExpanded, onExpandedChange = { typeExpanded = it }) {
                    OutlinedTextField(
                        value = selectedType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryGreen,
                            unfocusedBorderColor = BorderLight.copy(alpha = 0.5f)
                        )
                    )
                    ExposedDropdownMenu(expanded = typeExpanded, onDismissRequest = { typeExpanded = false }) {
                        InvestmentType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                onClick = { selectedType = type; typeExpanded = false }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Invested Amount") },
                    leadingIcon = { Text("৳", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = PrimaryGreen) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight.copy(alpha = 0.5f),
                        cursorColor = PrimaryGreen
                    )
                )

                OutlinedTextField(
                    value = currentValue,
                    onValueChange = { currentValue = it },
                    label = { Text("Current Value") },
                    leadingIcon = { Text("৳", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SavingsBlue) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SavingsBlue,
                        unfocusedBorderColor = BorderLight.copy(alpha = 0.5f),
                        cursorColor = SavingsBlue
                    )
                )

                OutlinedTextField(
                    value = dateFormat.format(purchaseDate),
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Purchase Date") },
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, null, tint = PrimaryGreen)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = BorderLight.copy(alpha = 0.5f)
                    )
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = BorderLight.copy(alpha = 0.5f)
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull() ?: 0.0
                    val curr = currentValue.toDoubleOrNull() ?: amt
                    if (name.isNotBlank() && amt > 0) {
                        onSave(name, selectedType, amt, curr, purchaseDate, notes.ifBlank { null })
                    }
                },
                enabled = name.isNotBlank() && amount.toDoubleOrNull() != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) { Text(if (investment != null) "Update" else "Add", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = purchaseDate
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { purchaseDate = it }
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
private fun TrackValueDialog(
    investment: Investment,
    onDismiss: () -> Unit,
    onSave: (Double) -> Unit
) {
    var newValue by remember { mutableStateOf(investment.currentValue.toString()) }
    val gainLoss = (newValue.toDoubleOrNull() ?: investment.currentValue) - investment.amount
    val gainLossPercent = if (investment.amount > 0) (gainLoss / investment.amount) * 100 else 0.0

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(Icons.Default.TrendingUp, null, tint = SavingsBlue, modifier = Modifier.size(22.dp))
                Text("Track ${investment.name}", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass)
                ) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Invested", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(CurrencyFormatter.format(investment.amount), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = TextPrimary)
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Current", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                            Text(CurrencyFormatter.format(investment.currentValue), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (gainLoss >= 0) PrimaryGreen else ExpenseRed)
                        }
                    }
                }

                OutlinedTextField(
                    value = newValue,
                    onValueChange = { newValue = it },
                    label = { Text("Current Value") },
                    leadingIcon = { Text("৳", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = SavingsBlue) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SavingsBlue,
                        unfocusedBorderColor = BorderLight.copy(alpha = 0.5f),
                        cursorColor = SavingsBlue
                    )
                )

                Surface(
                    color = if (gainLoss >= 0) PrimaryGreen.copy(alpha = 0.1f) else ExpenseRed.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            if (gainLoss >= 0) Icons.Default.TrendingUp else Icons.Default.TrendingDown,
                            null,
                            tint = if (gainLoss >= 0) PrimaryGreen else ExpenseRed,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                if (gainLoss >= 0) "Profit: +${CurrencyFormatter.format(gainLoss)}" else "Loss: ${CurrencyFormatter.format(gainLoss)}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (gainLoss >= 0) PrimaryGreen else ExpenseRed
                            )
                            Text(
                                "${if (gainLossPercent >= 0) "+" else ""}${String.format("%.2f", gainLossPercent)}%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (gainLossPercent >= 0) PrimaryGreen else ExpenseRed
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { newValue.toDoubleOrNull()?.let { onSave(it) } },
                enabled = newValue.toDoubleOrNull() != null,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
            ) { Text("Update Value", fontWeight = FontWeight.SemiBold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

private fun getInvestmentColor(type: InvestmentType): Color = when (type) {
    InvestmentType.STOCK -> PrimaryGreen
    InvestmentType.MUTUAL_FUND -> SavingsBlue
    InvestmentType.FIXED_DEPOSIT -> WarningOrange
    InvestmentType.GOLD -> Color(0xFFFFD700)
    InvestmentType.CRYPTO -> AccentPurple
    InvestmentType.REAL_ESTATE -> AccentTeal
    InvestmentType.SAVINGS -> IncomeGreen
    InvestmentType.BONDS -> Color(0xFF795548)
    InvestmentType.ETF -> AccentCyan
    InvestmentType.OTHER -> TextSecondary
}

private fun getInvestmentIcon(type: InvestmentType): ImageVector = when (type) {
    InvestmentType.STOCK -> Icons.Default.TrendingUp
    InvestmentType.MUTUAL_FUND -> Icons.Default.AccountBalance
    InvestmentType.FIXED_DEPOSIT -> Icons.Default.Savings
    InvestmentType.GOLD -> Icons.Default.MonetizationOn
    InvestmentType.CRYPTO -> Icons.Default.CurrencyBitcoin
    InvestmentType.REAL_ESTATE -> Icons.Default.House
    InvestmentType.SAVINGS -> Icons.Default.AccountBalanceWallet
    InvestmentType.BONDS -> Icons.Default.Description
    InvestmentType.ETF -> Icons.Default.ShowChart
    InvestmentType.OTHER -> Icons.Default.Category
}
