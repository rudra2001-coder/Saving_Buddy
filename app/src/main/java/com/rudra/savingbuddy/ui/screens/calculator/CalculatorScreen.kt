package com.rudra.savingbuddy.ui.screens.calculator

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(
    viewModel: CalculatorViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCalculator by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Financial Calculator", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
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
        ) {
            // Tab Row
            ScrollableTabRow(
                selectedTabIndex = selectedCalculator,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary
            ) {
                Tab(
                    selected = selectedCalculator == 0,
                    onClick = { selectedCalculator = 0 },
                    text = { Text("Loan") }
                )
                Tab(
                    selected = selectedCalculator == 1,
                    onClick = { selectedCalculator = 1 },
                    text = { Text("Investment") }
                )
                Tab(
                    selected = selectedCalculator == 2,
                    onClick = { selectedCalculator = 2 },
                    text = { Text("Savings Goal") }
                )
                Tab(
                    selected = selectedCalculator == 3,
                    onClick = { selectedCalculator = 3 },
                    text = { Text("Retirement") }
                )
                Tab(
                    selected = selectedCalculator == 4,
                    onClick = { selectedCalculator = 4 },
                    text = { Text("Budget") }
                )
            }

            when (selectedCalculator) {
                0 -> LoanCalculatorTab(viewModel = viewModel)
                1 -> InvestmentCalculatorTab(viewModel = viewModel)
                2 -> SavingsGoalCalculatorTab(viewModel = viewModel)
                3 -> RetirementCalculatorTab(viewModel = viewModel)
                4 -> BudgetCalculatorTab(viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun LoanCalculatorTab(viewModel: CalculatorViewModel) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Loan Calculator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Calculate monthly payments for loans",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = principal,
            onValueChange = { principal = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Loan Amount") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Annual Interest Rate") },
            leadingIcon = { Icon(Icons.Default.Percent, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            suffix = { Text("%") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = years,
            onValueChange = { years = it.filter { c -> c.isDigit() } },
            label = { Text("Loan Term (Years)") },
            leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.calculateLoan(
                    principal.toDoubleOrNull() ?: 0.0,
                    rate.toDoubleOrNull() ?: 0.0,
                    years.toIntOrNull() ?: 0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
        ) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ResultsCard(viewModel)
    }
}

@Composable
private fun InvestmentCalculatorTab(viewModel: CalculatorViewModel) {
    var principal by remember { mutableStateOf("") }
    var rate by remember { mutableStateOf("") }
    var years by remember { mutableStateOf("") }
    var monthlyContribution by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Investment Calculator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Calculate compound interest growth",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = principal,
            onValueChange = { principal = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Initial Investment") },
            leadingIcon = { Icon(Icons.Default.Savings, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = monthlyContribution,
            onValueChange = { monthlyContribution = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Monthly Contribution") },
            leadingIcon = { Icon(Icons.Default.Add, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = rate,
            onValueChange = { rate = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Expected Annual Return") },
            leadingIcon = { Icon(Icons.Default.TrendingUp, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            suffix = { Text("%") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = years,
            onValueChange = { years = it.filter { c -> c.isDigit() } },
            label = { Text("Investment Period (Years)") },
            leadingIcon = { Icon(Icons.Default.DateRange, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.calculateCompoundInterest(
                    principal.toDoubleOrNull() ?: 0.0,
                    rate.toDoubleOrNull() ?: 0.0,
                    years.toIntOrNull() ?: 0,
                    monthlyContribution.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
        ) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ResultsCard(viewModel)
    }
}

@Composable
private fun SavingsGoalCalculatorTab(viewModel: CalculatorViewModel) {
    var targetAmount by remember { mutableStateOf("") }
    var currentAmount by remember { mutableStateOf("") }
    var months by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Savings Goal Calculator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Calculate monthly savings needed",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = targetAmount,
            onValueChange = { targetAmount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Goal Amount") },
            leadingIcon = { Icon(Icons.Default.Flag, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = currentAmount,
            onValueChange = { currentAmount = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Current Savings") },
            leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = months,
            onValueChange = { months = it.filter { c -> c.isDigit() } },
            label = { Text("Months to Goal") },
            leadingIcon = { Icon(Icons.Default.Timer, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.calculateSavingsGoal(
                    targetAmount.toDoubleOrNull() ?: 0.0,
                    months.toIntOrNull() ?: 0,
                    currentAmount.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = SavingsBlue)
        ) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ResultsCard(viewModel)
    }
}

@Composable
private fun RetirementCalculatorTab(viewModel: CalculatorViewModel) {
    var currentAge by remember { mutableStateOf("") }
    var retirementAge by remember { mutableStateOf("") }
    var currentSavings by remember { mutableStateOf("") }
    var monthlyContribution by remember { mutableStateOf("") }
    var expectedReturn by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Retirement Calculator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Plan your retirement savings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = currentAge,
            onValueChange = { currentAge = it.filter { c -> c.isDigit() } },
            label = { Text("Current Age") },
            leadingIcon = { Icon(Icons.Default.Person, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = retirementAge,
            onValueChange = { retirementAge = it.filter { c -> c.isDigit() } },
            label = { Text("Retirement Age") },
            leadingIcon = { Icon(Icons.Default.BeachAccess, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = currentSavings,
            onValueChange = { currentSavings = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Current Savings") },
            leadingIcon = { Icon(Icons.Default.Savings, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = monthlyContribution,
            onValueChange = { monthlyContribution = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Monthly Contribution") },
            leadingIcon = { Icon(Icons.Default.Add, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = expectedReturn,
            onValueChange = { expectedReturn = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Expected Annual Return") },
            leadingIcon = { Icon(Icons.Default.TrendingUp, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            suffix = { Text("%") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.calculateRetirement(
                    currentAge.toIntOrNull() ?: 0,
                    retirementAge.toIntOrNull() ?: 0,
                    currentSavings.toDoubleOrNull() ?: 0.0,
                    monthlyContribution.toDoubleOrNull() ?: 0.0,
                    expectedReturn.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
        ) {
            Text("Calculate")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ResultsCard(viewModel)
    }
}

@Composable
private fun BudgetCalculatorTab(viewModel: CalculatorViewModel) {
    var income by remember { mutableStateOf("") }
    var savingsGoal by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Budget Calculator",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Based on 50-30-20 budget rule",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = income,
            onValueChange = { income = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Monthly Income") },
            leadingIcon = { Icon(Icons.Default.AccountBalance, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = savingsGoal,
            onValueChange = { savingsGoal = it.filter { c -> c.isDigit() || c == '.' } },
            label = { Text("Savings Goal (optional)") },
            leadingIcon = { Icon(Icons.Default.Flag, null) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            prefix = { Text("৳ ") }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.calculateBudget(
                    income.toDoubleOrNull() ?: 0.0,
                    savingsGoal.toDoubleOrNull() ?: 0.0
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Calculate Budget")
        }

        Spacer(modifier = Modifier.height(16.dp))

        ResultsCard(viewModel)
    }
}

@Composable
private fun ResultsCard(viewModel: CalculatorViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    if (uiState.calculations.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = uiState.resultText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                uiState.calculations.forEach { result ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = result.label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "৳ ${String.format("%,.2f", result.value)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    if (result.label != uiState.calculations.last().label) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}