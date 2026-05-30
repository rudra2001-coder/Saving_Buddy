package com.rudra.savingbuddy.ui.screens.currency

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.rudra.savingbuddy.ui.theme.*
import com.rudra.savingbuddy.util.ExchangeManager
import com.rudra.savingbuddy.util.ExchangeRate
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CurrencyConverterScreen(
    onNavigateBack: () -> Unit = {}
) {
    var amount by remember { mutableStateOf("100") }
    var fromCurrency by remember { mutableStateOf("USD") }
    var toCurrency by remember { mutableStateOf("BDT") }
    var showFromPicker by remember { mutableStateOf(false) }
    var showToPicker by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isRefreshing by remember { mutableStateOf(false) }

    val currencies = remember { ExchangeManager.currencies }

    LaunchedEffect(Unit) {
        isRefreshing = true
        ExchangeManager.refreshRates()
        isRefreshing = false
    }

    val fromRate = currencies.find { it.code == fromCurrency }?.rateToUsd ?: 1.0
    val toRate = currencies.find { it.code == toCurrency }?.rateToUsd ?: 1.0
    val fromSymbol = currencies.find { it.code == fromCurrency }?.symbol ?: "$"
    val toSymbol = currencies.find { it.code == toCurrency }?.symbol ?: "$"
    val fromFlag = currencies.find { it.code == fromCurrency }?.flag ?: ""
    val toFlag = currencies.find { it.code == toCurrency }?.flag ?: ""

    val inputAmount = amount.toDoubleOrNull() ?: 0.0
    val convertedAmount = if (fromRate > 0) (inputAmount / fromRate) * toRate else 0.0
    val rate = if (fromRate > 0) toRate / fromRate else 0.0

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Currency Converter", fontWeight = FontWeight.Bold)
                        Text("Real-time exchange rates", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PrimaryGreen,
                            strokeWidth = 2.dp
                        )
                    } else {
                        IconButton(onClick = {
                            isRefreshing = true
                            scope.launch {
                                ExchangeManager.refreshRates()
                                isRefreshing = false
                            }
                        }) {
                            Icon(Icons.Default.Refresh, "Refresh", tint = PrimaryGreen)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
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
                                Brush.linearGradient(colors = listOf(AccentCyan.copy(alpha = 0.3f), PrimaryGreen.copy(alpha = 0.2f), BackgroundCard))
                            )
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Enter Amount", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = amount,
                                onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) amount = it },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                textStyle = MaterialTheme.typography.headlineLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PrimaryGreen,
                                    unfocusedBorderColor = BorderLight,
                                    focusedContainerColor = BackgroundCardGlass,
                                    unfocusedContainerColor = BackgroundCardGlass,
                                    cursorColor = PrimaryGreen
                                ),
                                singleLine = true
                            )
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
                        Text("From", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BackgroundCard).clickable { showFromPicker = true }.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(fromFlag, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(fromCurrency, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(currencies.find { it.code == fromCurrency }?.name ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Text(fromSymbol, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = PrimaryGreen)
                        }
                    }
                }
            }

            item {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    FilledIconButton(
                        onClick = {
                            val temp = fromCurrency
                            fromCurrency = toCurrency
                            toCurrency = temp
                        },
                        modifier = Modifier.size(48.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = PrimaryGreen)
                    ) {
                        Icon(Icons.Default.SwapVert, "Swap", tint = Color.White)
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
                        Text("To", style = MaterialTheme.typography.labelMedium, color = TextSecondary)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(BackgroundCard).clickable { showToPicker = true }.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(toFlag, fontSize = 28.sp)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(toCurrency, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                    Text(currencies.find { it.code == toCurrency }?.name ?: "", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                }
                            }
                            Text(toSymbol, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = AccentCyan)
                        }
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, BorderLight.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier.fillMaxWidth()
                            .background(Brush.linearGradient(colors = listOf(PrimaryGreen.copy(alpha = 0.2f), AccentCyan.copy(alpha = 0.15f), BackgroundCard)))
                            .padding(24.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            Text("Converted Amount", style = MaterialTheme.typography.titleMedium, color = TextSecondary)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "$toSymbol ${String.format("%,.2f", convertedAmount)}",
                                style = MaterialTheme.typography.displaySmall,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryGreen
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = BackgroundCardGlass, shape = RoundedCornerShape(12.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                                    Text(
                                        "1 $fromCurrency = ${String.format("%.4f", rate)} $toCurrency",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary
                                    )
                                    if (ExchangeManager.lastUpdated > 0) {
                                        Text(
                                            "Updated: ${SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(ExchangeManager.lastUpdated))}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = TextSecondary.copy(alpha = 0.6f)
                                        )
                                    }
                                }
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
                            Icon(Icons.Outlined.Star, null, tint = WarningOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Quick Convert", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("10", "50", "100", "500", "1000", "5000").forEach { amt ->
                                AssistChip(
                                    onClick = { amount = amt },
                                    label = { Text("$fromSymbol$amt") },
                                    leadingIcon = { Icon(Icons.Default.AttachMoney, null, modifier = Modifier.size(16.dp)) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(20.dp), tint = PrimaryGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("All Currencies", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
            }

            items(currencies) { currency ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = BackgroundCardGlass),
                    border = BorderStroke(1.dp, if (currency.code == fromCurrency || currency.code == toCurrency) PrimaryGreen.copy(alpha = 0.5f) else BorderLight.copy(alpha = 0.3f))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currency.flag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("${currency.code} - ${currency.name}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, color = TextPrimary)
                            Text("1 USD = ${currency.symbol}${String.format("%,.2f", currency.rateToUsd)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        if (currency.code == fromCurrency) {
                            Surface(color = PrimaryGreen.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text("From", style = MaterialTheme.typography.labelSmall, color = PrimaryGreen, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                        if (currency.code == toCurrency) {
                            Surface(color = AccentCyan.copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                                Text("To", style = MaterialTheme.typography.labelSmall, color = AccentCyan, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            }
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showFromPicker) {
        CurrencyPickerDialog(
            title = "Select Currency",
            currencies = currencies,
            selectedCode = fromCurrency,
            onDismiss = { showFromPicker = false },
            onSelect = { fromCurrency = it; showFromPicker = false }
        )
    }

    if (showToPicker) {
        CurrencyPickerDialog(
            title = "Select Currency",
            currencies = currencies,
            selectedCode = toCurrency,
            onDismiss = { showToPicker = false },
            onSelect = { toCurrency = it; showToPicker = false }
        )
    }
}

@Composable
private fun CurrencyPickerDialog(
    title: String,
    currencies: List<ExchangeRate>,
    selectedCode: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn {
                items(currencies) { currency ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onSelect(currency.code) }
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(currency.flag, fontSize = 24.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(currency.code, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(currency.name, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        if (currency.code == selectedCode) {
                            Icon(Icons.Default.CheckCircle, "Selected", tint = PrimaryGreen)
                        }
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
