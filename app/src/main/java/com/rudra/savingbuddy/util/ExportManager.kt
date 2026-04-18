package com.rudra.savingbuddy.util

import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.core.content.FileProvider
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ExportManager {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())

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
                            writer.append("${dateFormat.format(Date(income.date))},${income.source},${income.amount},${income.category.displayName},${income.notes ?: ""},${income.isRecurring}\n")
                        }
                    }
                    ExportType.EXPENSE -> {
                        writer.append("Date,Amount,Category,Notes\n")
                        expenses.forEach { expense ->
                            writer.append("${dateFormat.format(Date(expense.date))},${expense.amount},${expense.category.displayName},${expense.notes ?: ""}\n")
                        }
                    }
                    ExportType.ALL -> {
                        writer.append("Type,Date,Amount,Category,Description,Notes\n")
                        incomeList.forEach { income ->
                            writer.append("Income,${dateFormat.format(Date(income.date))},${income.amount},${income.category.displayName},${income.source},${income.notes ?: ""}\n")
                        }
                        expenses.forEach { expense ->
                            writer.append("Expense,${dateFormat.format(Date(expense.date))},${expense.amount},${expense.category.displayName},${expense.category.displayName},${expense.notes ?: ""}\n")
                        }
                    }
                }
            }

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Saving Buddy Export - ${exportType.name}")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }.let { return it }
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
}

enum class ExportType {
    INCOME,
    EXPENSE,
    ALL
}