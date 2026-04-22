package com.rudra.savingbuddy.ui.screens.export

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils
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

data class ExportUiState(
    val allIncomes: List<Income> = emptyList(),
    val allExpenses: List<Expense> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val startDate: Long = DateUtils.getStartOfMonth(),
    val endDate: Long = DateUtils.getEndOfMonth(),
    val isLoading: Boolean = false,
    val isExporting: Boolean = false
)

@HiltViewModel
class ExportViewModel @Inject constructor(
    private val incomeRepository: IncomeRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExportUiState())
    val uiState: StateFlow<ExportUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    fun setDateRange(start: Long, end: Long) {
        _uiState.value = _uiState.value.copy(startDate = start, endDate = end)
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val incomes = incomeRepository.getIncomeByDateRange(_uiState.value.startDate, _uiState.value.endDate).first()
            val expenses = expenseRepository.getExpensesByDateRange(_uiState.value.startDate, _uiState.value.endDate).first()
            
            _uiState.value = _uiState.value.copy(
                allIncomes = incomes,
                allExpenses = expenses,
                totalIncome = incomes.sumOf { it.amount },
                totalExpense = expenses.sumOf { it.amount },
                isLoading = false
            )
        }
    }

    fun setExporting(exporting: Boolean) {
        _uiState.value = _uiState.value.copy(isExporting = exporting)
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
    var exportType by remember { mutableStateOf(ExportType.ALL) }

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
            // Summary Card
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
                            text = "Download as CSV for Excel/Google Sheets",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Period Selector
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Data Period",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FilterChip(
                                selected = true,
                                onClick = { viewModel.setDateRange(DateUtils.getStartOfMonth(), DateUtils.getEndOfMonth()) },
                                label = { Text("This Month") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { 
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.MONTH, -1)
                                    viewModel.setDateRange(
                                        DateUtils.getStartOfMonth(cal.timeInMillis),
                                        DateUtils.getEndOfMonth(cal.timeInMillis)
                                    )
                                },
                                label = { Text("Last Month") }
                            )
                            FilterChip(
                                selected = false,
                                onClick = { 
                                    val cal = Calendar.getInstance()
                                    cal.add(Calendar.MONTH, -3)
                                    viewModel.setDateRange(
                                        DateUtils.getStartOfMonth(cal.timeInMillis),
                                        DateUtils.getEndOfMonth()
                                    )
                                },
                                label = { Text("3 Months") }
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${DateUtils.formatDate(uiState.startDate)} - ${DateUtils.formatDate(uiState.endDate)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Data Summary
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Incomes",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${uiState.allIncomes.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = com.rudra.savingbuddy.ui.theme.IncomeGreen
                            )
                            Text(
                                text = CurrencyFormatter.format(uiState.totalIncome),
                                style = MaterialTheme.typography.bodySmall,
                                color = com.rudra.savingbuddy.ui.theme.IncomeGreen
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Expenses",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "${uiState.allExpenses.size}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = com.rudra.savingbuddy.ui.theme.ExpenseRed
                            )
                            Text(
                                text = CurrencyFormatter.format(uiState.totalExpense),
                                style = MaterialTheme.typography.bodySmall,
                                color = com.rudra.savingbuddy.ui.theme.ExpenseRed
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "Net",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = CurrencyFormatter.format(uiState.totalIncome - uiState.totalExpense),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Export Options
            item {
                Text(
                    text = "Export Format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            item {
                ExportOptionCard(
                    title = "All Transactions",
                    description = "Income + Expenses combined",
                    icon = Icons.Default.SwapHoriz,
                    onClick = { 
                        exportType = ExportType.ALL
                        showExportDialog = true
                    }
                )
            }

            item {
                ExportOptionCard(
                    title = "Income Only",
                    description = "${uiState.allIncomes.size} records",
                    icon = Icons.Default.TrendingUp,
                    onClick = { 
                        exportType = ExportType.INCOME
                        showExportDialog = true
                    }
                )
            }

            item {
                ExportOptionCard(
                    title = "Expenses Only",
                    description = "${uiState.allExpenses.size} records",
                    icon = Icons.Default.TrendingDown,
                    onClick = { 
                        exportType = ExportType.EXPENSE
                        showExportDialog = true
                    }
                )
            }
        }
    }

    if (showExportDialog) {
        AlertDialog(
            onDismissRequest = { showExportDialog = false },
            title = { Text("Export as CSV", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Download ${exportType.name.lowercase()} data as CSV file?")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "You can then open it in Excel, Google Sheets, or other apps.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.setExporting(true)
                        val intent = ExportManager.exportToCSV(
                            context = context,
                            incomeList = uiState.allIncomes,
                            expenses = uiState.allExpenses,
                            exportType = exportType
                        )
                        if (intent != null) {
                            context.startActivity(Intent.createChooser(intent, "Export Data"))
                        }
                        viewModel.setExporting(false)
                        showExportDialog = false
                    }
                ) {
                    Text("Export CSV")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExportDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ExportOptionCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}