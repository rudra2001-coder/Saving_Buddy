package com.rudra.savingbuddy.ui.screens.export

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.ui.theme.IncomeGreen
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
import com.rudra.savingbuddy.util.ExportFormat
import com.rudra.savingbuddy.util.ExportManager
import com.rudra.savingbuddy.util.ExportType
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import com.rudra.savingbuddy.domain.repository.IncomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import java.text.SimpleDateFormat
import java.util.*

enum class ExportDataType {
    ALL,
    INCOME,
    EXPENSE
}

enum class ExportPeriod {
    THIS_MONTH,
    LAST_MONTH,
    LAST_3_MONTHS,
    LAST_6_MONTHS,
    THIS_YEAR,
    ALL_TIME,
    CUSTOM
}

data class ExportUiState(
    val allIncomes: List<Income> = emptyList(),
    val allExpenses: List<Expense> = emptyList(),
    val filteredIncomes: List<Income> = emptyList(),
    val filteredExpenses: List<Expense> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val filteredIncome: Double = 0.0,
    val filteredExpense: Double = 0.0,
    val startDate: Long = DateUtils.getStartOfMonth(),
    val endDate: Long = DateUtils.getEndOfMonth(),
    val period: ExportPeriod = ExportPeriod.THIS_MONTH,
    val dataType: ExportDataType = ExportDataType.ALL,
    val format: ExportFormat = ExportFormat.CSV,
    val selectedCategories: Set<String> = emptySet(),
    val availableCategories: List<String> = emptyList(),
    val isLoading: Boolean = false,
    val isExporting: Boolean = false,
    val exportSuccess: Boolean = false,
    val showCustomDatePicker: Boolean = false,
    val showCategoryFilter: Boolean = false
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadAllData()
    }

    fun setPeriod(period: ExportPeriod) {
        val (start, end) = when (period) {
            ExportPeriod.THIS_MONTH -> DateUtils.getStartOfMonth() to DateUtils.getEndOfMonth()
            ExportPeriod.LAST_MONTH -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -1)
                DateUtils.getStartOfMonth(cal.timeInMillis) to DateUtils.getEndOfMonth(cal.timeInMillis)
            }
            ExportPeriod.LAST_3_MONTHS -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -3)
                DateUtils.getStartOfMonth(cal.timeInMillis) to DateUtils.getEndOfMonth()
            }
            ExportPeriod.LAST_6_MONTHS -> {
                val cal = Calendar.getInstance()
                cal.add(Calendar.MONTH, -6)
                DateUtils.getStartOfMonth(cal.timeInMillis) to DateUtils.getEndOfMonth()
            }
            ExportPeriod.THIS_YEAR -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_YEAR, 1)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                cal.set(Calendar.MILLISECOND, 0)
                cal.timeInMillis to System.currentTimeMillis()
            }
            ExportPeriod.ALL_TIME -> 0L to System.currentTimeMillis()
            ExportPeriod.CUSTOM -> _uiState.value.startDate to _uiState.value.endDate
        }
        _uiState.value = _uiState.value.copy(period = period, startDate = start, endDate = end)
        filterData()
    }

    fun setCustomDateRange(start: Long, end: Long) {
        _uiState.value = _uiState.value.copy(
            startDate = start,
            endDate = end,
            period = ExportPeriod.CUSTOM
        )
        filterData()
    }

    fun setExportFormat(format: ExportFormat) {
        _uiState.value = _uiState.value.copy(format = format)
    }

    fun setDataType(dataType: ExportDataType) {
        _uiState.value = _uiState.value.copy(dataType = dataType)
    }

    fun toggleCategory(category: String) {
        val current = _uiState.value.selectedCategories
        val updated = if (category in current) current - category else current + category
        _uiState.value = _uiState.value.copy(selectedCategories = updated)
        applyFilters()
    }

    fun clearCategories() {
        _uiState.value = _uiState.value.copy(selectedCategories = emptySet())
        applyFilters()
    }

    fun showCustomDatePicker(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCustomDatePicker = show)
    }

    fun showCategoryFilter(show: Boolean) {
        _uiState.value = _uiState.value.copy(showCategoryFilter = show)
    }

    private fun loadAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val incomes = incomeRepository.getAllIncome().first()
            val expenses = expenseRepository.getAllExpenses().first()
            
            val categories = (incomes.map { it.category.displayName } + expenses.map { it.category.displayName }).distinct()
            
            _uiState.value = _uiState.value.copy(
                allIncomes = incomes,
                allExpenses = expenses,
                filteredIncomes = incomes,
                filteredExpenses = expenses,
                totalIncome = incomes.sumOf { it.amount },
                totalExpense = expenses.sumOf { it.amount },
                filteredIncome = incomes.sumOf { it.amount },
                filteredExpense = expenses.sumOf { it.amount },
                availableCategories = categories,
                isLoading = false
            )
        }
    }

    private fun filterData() {
        val start = _uiState.value.startDate
        val end = _uiState.value.endDate
        
        val filteredIncomes = if (start == 0L) {
            _uiState.value.allIncomes
        } else {
            _uiState.value.allIncomes.filter { it.date in start..end }
        }
        val filteredExpenses = if (start == 0L) {
            _uiState.value.allExpenses
        } else {
            _uiState.value.allExpenses.filter { it.date in start..end }
        }
        
        _uiState.value = _uiState.value.copy(
            filteredIncomes = filteredIncomes,
            filteredExpenses = filteredExpenses,
            filteredIncome = filteredIncomes.sumOf { it.amount },
            filteredExpense = filteredExpenses.sumOf { it.amount }
        )
        applyFilters()
    }

    private fun applyFilters() {
        val categories = _uiState.value.selectedCategories
        if (categories.isEmpty()) {
            filterData()
            return
        }
        
        val filteredIncomes = _uiState.value.allIncomes.filter { 
            it.date in _uiState.value.startDate.._uiState.value.endDate || _uiState.value.startDate == 0L
        }.filter { it.category.displayName in categories }
        
        val filteredExpenses = _uiState.value.allExpenses.filter { 
            it.date in _uiState.value.startDate.._uiState.value.endDate || _uiState.value.startDate == 0L
        }.filter { it.category.displayName in categories }
        
        _uiState.value = _uiState.value.copy(
            filteredIncomes = filteredIncomes,
            filteredExpenses = filteredExpenses,
            filteredIncome = filteredIncomes.sumOf { it.amount },
            filteredExpense = filteredExpenses.sumOf { it.amount }
        )
    }

    fun setExportSuccess(success: Boolean) {
        _uiState.value = _uiState.value.copy(exportSuccess = success)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportScreen(
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ExportViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    var showExportDialog by remember { mutableStateOf(false) }
    var showFormatDialog by remember { mutableStateOf(false) }
    var showPeriodSheet by remember { mutableStateOf(false) }

    val periodLabel = when (uiState.period) {
        ExportPeriod.THIS_MONTH -> "This Month"
        ExportPeriod.LAST_MONTH -> "Last Month"
        ExportPeriod.LAST_3_MONTHS -> "Last 3 Months"
        ExportPeriod.LAST_6_MONTHS -> "Last 6 Months"
        ExportPeriod.THIS_YEAR -> "This Year"
        ExportPeriod.ALL_TIME -> "All Time"
        ExportPeriod.CUSTOM -> "Custom"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Export Data", fontWeight = FontWeight.Bold) },
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.FileDownload,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Export Your Data",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Download in CSV, Text, or JSON format",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Data Period",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            AssistChip(
                                onClick = { showPeriodSheet = true },
                                label = { Text(periodLabel) },
                                leadingIcon = { Icon(Icons.Default.DateRange, null, modifier = Modifier.size(16.dp)) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.period == ExportPeriod.THIS_MONTH,
                                onClick = { viewModel.setPeriod(ExportPeriod.THIS_MONTH) },
                                label = { Text("This Month") }
                            )
                            FilterChip(
                                selected = uiState.period == ExportPeriod.LAST_MONTH,
                                onClick = { viewModel.setPeriod(ExportPeriod.LAST_MONTH) },
                                label = { Text("Last Month") }
                            )
                            FilterChip(
                                selected = uiState.period == ExportPeriod.LAST_3_MONTHS,
                                onClick = { viewModel.setPeriod(ExportPeriod.LAST_3_MONTHS) },
                                label = { Text("3 Months") }
                            )
                            FilterChip(
                                selected = uiState.period == ExportPeriod.THIS_YEAR,
                                onClick = { viewModel.setPeriod(ExportPeriod.THIS_YEAR) },
                                label = { Text("Year") }
                            )
                            FilterChip(
                                selected = uiState.period == ExportPeriod.ALL_TIME,
                                onClick = { viewModel.setPeriod(ExportPeriod.ALL_TIME) },
                                label = { Text("All Time") }
                            )
                        }

                        if (uiState.period == ExportPeriod.CUSTOM) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${DateUtils.formatDate(uiState.startDate)} - ${DateUtils.formatDate(uiState.endDate)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (uiState.selectedCategories.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Categories: ${uiState.selectedCategories.size} selected",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                TextButton(onClick = { viewModel.clearCategories() }) {
                                    Text("Clear")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Income",
                                style = MaterialTheme.typography.labelMedium,
                                color = IncomeGreen
                            )
                            Text(
                                text = "${uiState.filteredIncomes.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = IncomeGreen
                            )
                            Text(
                                text = CurrencyFormatter.formatCompact(uiState.filteredIncome),
                                style = MaterialTheme.typography.bodySmall,
                                color = IncomeGreen
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f))
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Expenses",
                                style = MaterialTheme.typography.labelMedium,
                                color = ExpenseRed
                            )
                            Text(
                                text = "${uiState.filteredExpenses.size}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = ExpenseRed
                            )
                            Text(
                                text = CurrencyFormatter.formatCompact(uiState.filteredExpense),
                                style = MaterialTheme.typography.bodySmall,
                                color = ExpenseRed
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Net",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = CurrencyFormatter.formatCompact(uiState.filteredIncome - uiState.filteredExpense),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Export Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select Data Type",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.dataType == ExportDataType.ALL,
                                onClick = { viewModel.setDataType(ExportDataType.ALL) },
                                label = { Text("All") }
                            )
                            FilterChip(
                                selected = uiState.dataType == ExportDataType.INCOME,
                                onClick = { viewModel.setDataType(ExportDataType.INCOME) },
                                label = { Text("Income") }
                            )
                            FilterChip(
                                selected = uiState.dataType == ExportDataType.EXPENSE,
                                onClick = { viewModel.setDataType(ExportDataType.EXPENSE) },
                                label = { Text("Expenses") }
                            )
                        }
                    }
                }
            }

            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Export Format",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = uiState.format == ExportFormat.CSV,
                                onClick = { viewModel.setExportFormat(ExportFormat.CSV) },
                                label = { Text("CSV") },
                                leadingIcon = if (uiState.format == ExportFormat.CSV) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = uiState.format == ExportFormat.TEXT,
                                onClick = { viewModel.setExportFormat(ExportFormat.TEXT) },
                                label = { Text("Text") },
                                leadingIcon = if (uiState.format == ExportFormat.TEXT) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                            FilterChip(
                                selected = uiState.format == ExportFormat.JSON,
                                onClick = { viewModel.setExportFormat(ExportFormat.JSON) },
                                label = { Text("JSON") },
                                leadingIcon = if (uiState.format == ExportFormat.JSON) {
                                    { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                } else null
                            )
                        }
                    }
                }
            }

            item {
                OutlinedCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { showExportDialog = true }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "Export Now",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${uiState.filteredIncomes.size + uiState.filteredExpenses.size} records",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

            item {
                AssistButton(
                    onClick = { viewModel.showCategoryFilter(true) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Filter by Category")
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }
    }

    if (showPeriodSheet) {
        ModalBottomSheet(onDismissRequest = { showPeriodSheet = false }) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(
                    text = "Select Time Period",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                PeriodOption("This Month", ExportPeriod.THIS_MONTH, uiState.period) { viewModel.setPeriod(it) }
                PeriodOption("Last Month", ExportPeriod.LAST_MONTH, uiState.period) { viewModel.setPeriod(it) }
                PeriodOption("Last 3 Months", ExportPeriod.LAST_3_MONTHS, uiState.period) { viewModel.setPeriod(it) }
                PeriodOption("Last 6 Months", ExportPeriod.LAST_6_MONTHS, uiState.period) { viewModel.setPeriod(it) }
                PeriodOption("This Year", ExportPeriod.THIS_YEAR, uiState.period) { viewModel.setPeriod(it) }
                PeriodOption("All Time", ExportPeriod.ALL_TIME, uiState.period) { viewModel.setPeriod(it) }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Confirm Export", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Export ${uiState.format.name} format with ${uiState.filteredIncomes.size + uiState.filteredExpenses.size} records?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Period: ${DateUtils.formatDate(uiState.startDate)} - ${DateUtils.formatDate(uiState.endDate)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Format: ${uiState.format.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = ExportManager.exportData(
                            context = context,
                            incomes = uiState.filteredIncomes,
                            expenses = uiState.filteredExpenses,
                            format = uiState.format,
                            dataType = when (uiState.dataType) {
                                ExportDataType.ALL -> com.rudra.savingbuddy.util.ExportType.ALL
                                ExportDataType.INCOME -> com.rudra.savingbuddy.util.ExportType.INCOME
                                ExportDataType.EXPENSE -> com.rudra.savingbuddy.util.ExportType.EXPENSE
                            }
                        )
                        if (intent != null) {
                            context.startActivity(Intent.createChooser(intent, "Export Data"))
                        }
                        viewModel.setExportSuccess(true)
                        showExportDialog = false
                    }
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Export ${uiState.format.name}")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (uiState.showCategoryFilter) {
        CategoryFilterSheet(
            categories = uiState.availableCategories,
            selectedCategories = uiState.selectedCategories,
            onCategoryToggle = { viewModel.toggleCategory(it) },
            onClear = { viewModel.clearCategories() },
            onDismiss = { viewModel.showCategoryFilter(false) }
        )
    }

    if (uiState.exportSuccess) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2000)
            viewModel.setExportSuccess(false)
        }
    }
}

@Composable
private fun PeriodOption(
    label: String,
    period: ExportPeriod,
    currentPeriod: ExportPeriod,
    onSelect: (ExportPeriod) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge
        )
        RadioButton(
            selected = period == currentPeriod,
            onClick = { onSelect(period) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CategoryFilterSheet(
    categories: List<String>,
    selectedCategories: Set<String>,
    onCategoryToggle: (String) -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Filter by Category",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onClear) {
                    Text("Clear All")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))

            if (categories.isEmpty()) {
                Text(
                    text = "No categories available",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                categories.forEach { category ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = category in selectedCategories,
                            onCheckedChange = { onCategoryToggle(category) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = category,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AssistButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        content = content
    )
}