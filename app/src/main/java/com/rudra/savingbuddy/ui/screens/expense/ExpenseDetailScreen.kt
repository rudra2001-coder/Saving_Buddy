package com.rudra.savingbuddy.ui.screens.expense

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.rudra.savingbuddy.ui.theme.ExpenseRed
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseDetailScreen(
    navController: NavController? = null,
    expenseId: Long = 0L,
    viewModel: ExpenseViewModel = hiltViewModel()
) {
    LaunchedEffect(expenseId) {
        if (expenseId > 0) {
            viewModel.loadExpenseById(expenseId)
        }
    }
    
    val uiState by viewModel.uiState.collectAsState()
    val expense = uiState.editingExpense

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expense Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController?.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (expense != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = CurrencyFormatter.formatBDT(expense.amount),
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = ExpenseRed
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = expense.category.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Date", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                DateUtils.formatDate(expense.date),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                        expense.notes?.let { notes ->
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Notes", style = MaterialTheme.typography.bodyMedium)
                                Text(notes, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Expense not found")
            }
        }
    }
}