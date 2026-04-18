package com.rudra.savingbuddy.ui.screens.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rudra.savingbuddy.domain.model.Goal
import com.rudra.savingbuddy.domain.model.GoalCategory
import com.rudra.savingbuddy.ui.theme.Green500
import com.rudra.savingbuddy.util.CurrencyFormatter
import com.rudra.savingbuddy.util.DateUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalsScreen(
    viewModel: GoalsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showContributionDialog by remember { mutableStateOf<Goal?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Goal?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            TabRow(selectedTabIndex = uiState.selectedTab) {
                Tab(
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.selectTab(0) },
                    text = { Text("Active (${uiState.activeGoals.size})") }
                )
                Tab(
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.selectTab(1) },
                    text = { Text("Completed (${uiState.completedGoals.size})") }
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Financial Goals",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                val goals = if (uiState.selectedTab == 0) uiState.activeGoals else uiState.completedGoals

                if (goals.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Flag,
                                    contentDescription = null,
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (uiState.selectedTab == 0) "No active goals" else "No completed goals",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                items(goals, key = { it.id }) { goal ->
                    GoalCard(
                        goal = goal,
                        onEdit = { viewModel.showEditDialog(goal) },
                        onDelete = { showDeleteDialog = goal },
                        onContribute = { showContributionDialog = goal },
                        onMarkComplete = { viewModel.markComplete(goal.id) }
                    )
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        GoalDialog(
            goal = uiState.editingGoal,
            onDismiss = { viewModel.hideDialog() },
            onSave = { name, amount, category, deadline, autoAllocate, percentage ->
                viewModel.saveGoal(name, amount, category, deadline, autoAllocate, percentage)
            }
        )
    }

    showContributionDialog?.let { goal ->
        ContributionDialog(
            goalName = goal.name,
            targetAmount = goal.targetAmount,
            currentAmount = goal.currentAmount,
            onDismiss = { showContributionDialog = null },
            onContribute = { amount ->
                viewModel.addToGoal(goal.id, amount)
                showContributionDialog = null
            }
        )
    }

    showDeleteDialog?.let { goal ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Goal") },
            text = { Text("Are you sure you want to delete '${goal.name}'?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGoal(goal)
                    showDeleteDialog = null
                }) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun GoalCard(
    goal: Goal,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onContribute: () -> Unit,
    onMarkComplete: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = { expanded = !expanded }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = goal.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = goal.category.displayName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AssistChip(
                    onClick = {},
                    label = { Text("${goal.daysRemaining} days left") }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier.fillMaxWidth(),
                color = if (goal.isCompleted) Green500 else MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${CurrencyFormatter.format(goal.currentAmount)} / ${CurrencyFormatter.format(goal.targetAmount)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "${(goal.progress * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Green500
                )
            }

            Text(
                text = "${goal.remainingAmount.toLong()} remaining",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(onClick = onContribute) {
                        Text("Add Funds")
                    }
                    if (!goal.isCompleted) {
                        Button(onClick = onMarkComplete) {
                            Text("Mark Complete")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDialog(
    goal: Goal?,
    onDismiss: () -> Unit,
    onSave: (String, Double, GoalCategory, Long, Boolean, Double) -> Unit
) {
    var name by remember { mutableStateOf(goal?.name ?: "") }
    var targetAmount by remember { mutableStateOf(goal?.targetAmount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(goal?.category ?: GoalCategory.OTHERS) }
    var deadline by remember { mutableStateOf(goal?.deadline ?: System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000) }
    var autoAllocate by remember { mutableStateOf(goal?.autoAllocate ?: false) }
    var allocationPercentage by remember { mutableStateOf("10") }
    var categoryExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (goal != null) "Edit Goal" else "Create Goal") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Goal Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = targetAmount,
                    onValueChange = { targetAmount = it },
                    label = { Text("Target Amount") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = categoryExpanded,
                    onExpandedChange = { categoryExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedCategory.displayName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        GoalCategory.entries.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category.displayName) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }

                Text(
                    text = "Deadline: ${DateUtils.formatDate(deadline)}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = autoAllocate,
                        onCheckedChange = { autoAllocate = it }
                    )
                    Text(
                        text = "Auto-allocate from savings",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (autoAllocate) {
                    OutlinedTextField(
                        value = allocationPercentage,
                        onValueChange = { allocationPercentage = it },
                        label = { Text("Allocation %") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amount = targetAmount.toDoubleOrNull() ?: 0.0
                    val percent = allocationPercentage.toDoubleOrNull() ?: 0.0
                    if (name.isNotBlank() && amount > 0) {
                        onSave(name, amount, selectedCategory, deadline, autoAllocate, percent)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ContributionDialog(
    goalName: String,
    targetAmount: Double,
    currentAmount: Double,
    onDismiss: () -> Unit,
    onContribute: (Double) -> Unit
) {
    var amount by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to $goalName") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Target: ${CurrencyFormatter.format(targetAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Current: ${CurrencyFormatter.format(currentAmount)}",
                    style = MaterialTheme.typography.bodySmall
                )
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount to Add") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                amount.toDoubleOrNull()?.let { onContribute(it) }
            }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}