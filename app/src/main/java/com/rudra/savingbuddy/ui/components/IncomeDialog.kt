package com.rudra.savingbuddy.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.IncomeCategory
import com.rudra.savingbuddy.domain.model.RecurringInterval

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeDialog(
    income: Income? = null,
    onDismiss: () -> Unit,
    onSave: (String, Double, IncomeCategory, Long, Boolean, RecurringInterval?, String?) -> Unit
) {
    var source by remember { mutableStateOf(income?.source ?: "") }
    var amount by remember { mutableStateOf(income?.amount?.toString() ?: "") }
    var selectedCategory by remember { mutableStateOf(income?.category ?: IncomeCategory.SALARY) }
    var selectedInterval by remember { mutableStateOf(income?.recurringInterval ?: RecurringInterval.MONTHLY) }
    var isRecurring by remember { mutableStateOf(income?.isRecurring ?: false) }
    var notes by remember { mutableStateOf(income?.notes ?: "") }
    var categoryExpanded by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (income != null) "Edit Income" else "Add Income") },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = source,
                    onValueChange = { source = it },
                    label = { Text("Source") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Amount") },
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = { categoryExpanded = false }
                    ) {
                        IncomeCategory.entries.forEach { category ->
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

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Checkbox(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it }
                    )
                    Text(
                        text = "Recurring",
                        modifier = Modifier.padding(top = 12.dp)
                    )
                }

                if (isRecurring) {
                    ExposedDropdownMenuBox(
                        expanded = intervalExpanded,
                        onExpandedChange = { intervalExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedInterval.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Interval") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = intervalExpanded) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = intervalExpanded,
                            onDismissRequest = { intervalExpanded = false }
                        ) {
                            RecurringInterval.entries.forEach { interval ->
                                DropdownMenuItem(
                                    text = { Text(interval.displayName) },
                                    onClick = {
                                        selectedInterval = interval
                                        intervalExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val amountValue = amount.toDoubleOrNull() ?: 0.0
                    if (source.isNotBlank() && amountValue > 0) {
                        onSave(
                            source,
                            amountValue,
                            selectedCategory,
                            income?.date ?: System.currentTimeMillis(),
                            isRecurring,
                            if (isRecurring) selectedInterval else null,
                            notes.ifBlank { null }
                        )
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