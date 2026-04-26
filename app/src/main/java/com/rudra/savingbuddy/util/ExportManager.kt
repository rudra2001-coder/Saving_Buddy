package com.rudra.savingbuddy.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

enum class ExportFormat {
    CSV,
    TEXT,
    JSON
}

enum class ExportType {
    INCOME,
    EXPENSE,
    ALL
}

object ExportManager {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

    fun exportData(
        context: Context,
        incomes: List<Income>,
        expenses: List<Expense>,
        format: ExportFormat,
        dataType: ExportType
    ): Intent? {
        return when (format) {
            ExportFormat.CSV -> exportToCSV(context, incomes, expenses, dataType)
            ExportFormat.TEXT -> exportToText(context, incomes, expenses, dataType)
            ExportFormat.JSON -> exportToJSON(context, incomes, expenses, dataType)
        }
    }

    fun exportToCSV(
        context: Context,
        incomeList: List<Income>,
        expenses: List<Expense>,
        exportType: ExportType
    ): Intent? {
        return try {
            val fileName = "saving_buddy_${exportType.name.lowercase()}_${fileNameFormat.format(Date())}.csv"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileWriter(file).use { writer ->
                when (exportType) {
                    ExportType.INCOME -> {
                        writer.append("Date,Source,Amount,Category,Notes,Recurring\n")
                        incomeList.forEach { income ->
                            writer.append("${dateFormat.format(Date(income.date))},${escapeCSV(income.source)},${income.amount},${escapeCSV(income.category.displayName)},${escapeCSV(income.notes ?: "")},${income.isRecurring}\n")
                        }
                    }
                    ExportType.EXPENSE -> {
                        writer.append("Date,Amount,Category,Notes\n")
                        expenses.forEach { expense ->
                            writer.append("${dateFormat.format(Date(expense.date))},${expense.amount},${escapeCSV(expense.category.displayName)},${escapeCSV(expense.notes ?: "")}\n")
                        }
                    }
                    ExportType.ALL -> {
                        writer.append("Type,Date,Amount,Category,Description,Notes\n")
                        incomeList.sortedBy { it.date }.forEach { income ->
                            writer.append("Income,${dateFormat.format(Date(income.date))},${income.amount},${escapeCSV(income.category.displayName)},${escapeCSV(income.source)},${escapeCSV(income.notes ?: "")}\n")
                        }
                        expenses.sortedBy { it.date }.forEach { expense ->
                            writer.append("Expense,${dateFormat.format(Date(expense.date))},${expense.amount},${escapeCSV(expense.category.displayName)},${escapeCSV(expense.category.displayName)},${escapeCSV(expense.notes ?: "")}\n")
                        }
                    }
                }
            }

            shareFile(context, file, "text/csv", "Saving Buddy Export - CSV")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportToText(
        context: Context,
        incomeList: List<Income>,
        expenses: List<Expense>,
        exportType: ExportType
    ): Intent? {
        return try {
            val fileName = "saving_buddy_${exportType.name.lowercase()}_${fileNameFormat.format(Date())}.txt"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            FileWriter(file).use { writer ->
                writer.append("=".repeat(50)).append("\n")
                writer.append("SAVING BUDDY - DATA EXPORT\n")
                writer.append("Generated: ${dateFormat.format(Date())}\n")
                writer.append("=".repeat(50)).append("\n\n")

                when (exportType) {
                    ExportType.INCOME -> {
                        writer.append("INCOME TRANSACTIONS\n")
                        writer.append("-".repeat(40)).append("\n")
                        writer.append(String.format("%-12s %-15s %-12s %-15s\n", "Date", "Source", "Amount", "Category"))
                        writer.append("-".repeat(40)).append("\n")
                        incomeList.forEach { income ->
                            writer.append(String.format("%-12s %-15s %-12s %-15s\n",
                                dateFormat.format(Date(income.date)),
                                income.source.take(15),
                                CurrencyFormatter.format(income.amount),
                                income.category.displayName.take(15)
                            ))
                        }
                        writer.append("\nTotal Income: ${CurrencyFormatter.format(incomeList.sumOf { it.amount })}\n")
                    }
                    ExportType.EXPENSE -> {
                        writer.append("EXPENSE TRANSACTIONS\n")
                        writer.append("-".repeat(40)).append("\n")
                        writer.append(String.format("%-12s %-12s %-15s\n", "Date", "Amount", "Category"))
                        writer.append("-".repeat(40)).append("\n")
                        expenses.forEach { expense ->
                            writer.append(String.format("%-12s %-12s %-15s\n",
                                dateFormat.format(Date(expense.date)),
                                CurrencyFormatter.format(expense.amount),
                                expense.category.displayName.take(15)
                            ))
                        }
                        writer.append("\nTotal Expenses: ${CurrencyFormatter.format(expenses.sumOf { it.amount })}\n")
                    }
                    ExportType.ALL -> {
                        writer.append("ALL TRANSACTIONS\n")
                        writer.append("-".repeat(50)).append("\n")
                        writer.append(String.format("%-10s %-12s %-12s %-15s %-15s\n", "Type", "Date", "Amount", "Category", "Description"))
                        writer.append("-".repeat(50)).append("\n")
                        
                        val allTransactions = mutableListOf<Pair<String, Any>>()
                        incomeList.forEach { allTransactions.add("Income" to it) }
                        expenses.forEach { allTransactions.add("Expense" to it) }
                        
                        allTransactions.sortedByDescending { (_, item) ->
                            when (item) {
                                is Income -> item.date
                                is Expense -> item.date
                                else -> 0L
                            }
                        }.forEach { (type, item) ->
                            when (item) {
                                is Income -> {
                                    writer.append(String.format("%-10s %-12s %-12s %-15s %-15s\n",
                                        type,
                                        dateFormat.format(Date(item.date)),
                                        CurrencyFormatter.format(item.amount),
                                        item.category.displayName.take(15),
                                        item.source.take(15)
                                    ))
                                }
                                is Expense -> {
                                    writer.append(String.format("%-10s %-12s %-12s %-15s %-15s\n",
                                        type,
                                        dateFormat.format(Date(item.date)),
                                        CurrencyFormatter.format(item.amount),
                                        item.category.displayName.take(15),
                                        item.category.displayName.take(15)
                                    ))
                                }
                            }
                        }
                        
                        val totalIncome = incomeList.sumOf { it.amount }
                        val totalExpense = expenses.sumOf { it.amount }
                        writer.append("\n")
                        writer.append("=".repeat(50)).append("\n")
                        writer.append("SUMMARY\n")
                        writer.append("=".repeat(50)).append("\n")
                        writer.append("Total Income:    ${CurrencyFormatter.format(totalIncome)}\n")
                        writer.append("Total Expenses: ${CurrencyFormatter.format(totalExpense)}\n")
                        writer.append("Net Savings:    ${CurrencyFormatter.format(totalIncome - totalExpense)}\n")
                        writer.append("Savings Rate:    ${if (totalIncome > 0) ((totalIncome - totalExpense) / totalIncome * 100).toInt() else 0}%\n")
                    }
                }

                writer.append("\n")
                writer.append("=".repeat(50)).append("\n")
                writer.append("Export Info:\n")
                writer.append("Total Records: ${incomeList.size + expenses.size}\n")
                writer.append("Income: ${incomeList.size}\n")
                writer.append("Expenses: ${expenses.size}\n")
            }

            shareFile(context, file, "text/plain", "Saving Buddy Export - Text")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun exportToJSON(
        context: Context,
        incomeList: List<Income>,
        expenses: List<Expense>,
        exportType: ExportType
    ): Intent? {
        return try {
            val fileName = "saving_buddy_${exportType.name.lowercase()}_${fileNameFormat.format(Date())}.json"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            
            val json = JSONObject()
            
            json.put("exportInfo", JSONObject().apply {
                put("generatedAt", Date().toString())
                put("totalRecords", incomeList.size + expenses.size)
                put("incomeCount", incomeList.size)
                put("expenseCount", expenses.size)
            })

            json.put("summary", JSONObject().apply {
                val totalIncome = incomeList.sumOf { it.amount }
                val totalExpense = expenses.sumOf { it.amount }
                put("totalIncome", totalIncome)
                put("totalExpenses", totalExpense)
                put("netSavings", totalIncome - totalExpense)
                put("savingsRate", if (totalIncome > 0) (totalIncome - totalExpense) / totalIncome * 100 else 0.0)
            })

            when (exportType) {
                ExportType.INCOME -> {
                    val incomeArray = JSONArray()
                    incomeList.forEach { income ->
                        incomeArray.put(JSONObject().apply {
                            put("id", income.id)
                            put("date", dateFormat.format(Date(income.date)))
                            put("dateMillis", income.date)
                            put("source", income.source)
                            put("amount", income.amount)
                            put("category", income.category.displayName)
                            put("notes", income.notes ?: "")
                            put("isRecurring", income.isRecurring)
                        })
                    }
                    json.put("incomes", incomeArray)
                }
                ExportType.EXPENSE -> {
                    val expenseArray = JSONArray()
                    expenses.forEach { expense ->
                        expenseArray.put(JSONObject().apply {
                            put("id", expense.id)
                            put("date", dateFormat.format(Date(expense.date)))
                            put("dateMillis", expense.date)
                            put("amount", expense.amount)
                            put("category", expense.category.displayName)
                            put("notes", expense.notes ?: "")
                        })
                    }
                    json.put("expenses", expenseArray)
                }
                ExportType.ALL -> {
                    val incomeArray = JSONArray()
                    incomeList.forEach { income ->
                        incomeArray.put(JSONObject().apply {
                            put("type", "INCOME")
                            put("id", income.id)
                            put("date", dateFormat.format(Date(income.date)))
                            put("dateMillis", income.date)
                            put("amount", income.amount)
                            put("category", income.category.displayName)
                            put("description", income.source)
                            put("notes", income.notes ?: "")
                        })
                    }
                    json.put("incomes", incomeArray)

                    val expenseArray = JSONArray()
                    expenses.forEach { expense ->
                        expenseArray.put(JSONObject().apply {
                            put("type", "EXPENSE")
                            put("id", expense.id)
                            put("date", dateFormat.format(Date(expense.date)))
                            put("dateMillis", expense.date)
                            put("amount", expense.amount)
                            put("category", expense.category.displayName)
                            put("description", expense.category.displayName)
                            put("notes", expense.notes ?: "")
                        })
                    }
                    json.put("expenses", expenseArray)
                }
            }

            FileWriter(file).use { writer ->
                writer.write(json.toString(2))
            }

            shareFile(context, file, "application/json", "Saving Buddy Export - JSON")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    fun getExportSummary(incomeList: List<Income>, expenses: List<Expense>): String {
        val totalIncome = incomeList.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val savings = totalIncome - totalExpenses
        
        return buildString {
            appendLine("=== Saving Buddy Summary ===")
            appendLine("Total Income: ${CurrencyFormatter.format(totalIncome)}")
            appendLine("Total Expenses: ${CurrencyFormatter.format(totalExpenses)}")
            appendLine("Net Savings: ${CurrencyFormatter.format(savings)}")
            appendLine("Savings Rate: ${if (totalIncome > 0) (savings / totalIncome * 100).toInt() else 0}%")
            appendLine("Exports: ${incomeList.size} income, ${expenses.size} expenses")
        }
    }

    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }

    private fun shareFile(context: Context, file: File, mimeType: String, subject: String): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        return Intent(Intent.ACTION_SEND).apply {
            this.type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, subject)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
}