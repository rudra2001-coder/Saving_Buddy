package com.rudra.savingbuddy.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.core.content.FileProvider
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.Income
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

enum class ReportType {
    MONTHLY_SUMMARY, ANNUAL_REPORT, CUSTOM_RANGE, CATEGORY_ANALYSIS, FULL_STATEMENT
}

data class ReportConfig(
    val type: ReportType = ReportType.MONTHLY_SUMMARY,
    val startDate: Long = DateUtils.getStartOfMonth(),
    val endDate: Long = DateUtils.getEndOfMonth(),
    val includeCharts: Boolean = true,
    val includeSummary: Boolean = true,
    val includeDetails: Boolean = true,
    val includeInsights: Boolean = true,
    val title: String = "Financial Report"
)

data class ReportData(
    val config: ReportConfig,
    val totalIncome: Double,
    val totalExpenses: Double,
    val netSavings: Double,
    val savingsRate: Double,
    val incomes: List<Income>,
    val expenses: List<Expense>,
    val categoryBreakdown: List<CategoryBreakdown>,
    val monthlyTrend: List<MonthlyTrend>,
    val topExpenseCategories: List<CategoryBreakdown>,
    val dailyAverage: Double,
    val transactionCount: Int,
    val insights: List<String>
)

data class CategoryBreakdown(
    val category: String,
    val amount: Double,
    val percentage: Double,
    val transactionCount: Int
)

data class MonthlyTrend(
    val month: String,
    val income: Double,
    val expenses: Double,
    val savings: Double
)

object ReportGenerator {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    private val monthFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun generatePdfReport(
        context: Context,
        reportData: ReportData
    ): Intent? {
        return try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            val titlePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#0B1220")
                textSize = 28f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            val headerPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#22C55E")
                textSize = 16f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            val textPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#333333")
                textSize = 12f
                isAntiAlias = true
            }
            val labelPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#666666")
                textSize = 10f
                isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#111827")
                textSize = 14f
                typeface = Typeface.DEFAULT_BOLD
                isAntiAlias = true
            }
            val linePaint = Paint().apply {
                color = android.graphics.Color.parseColor("#E5E7EB")
                strokeWidth = 1f
            }

            var y = 40f
            val margin = 40f
            val pageWidth = 595f
            val contentWidth = pageWidth - 2 * margin

            canvas.drawColor(android.graphics.Color.WHITE)

            canvas.drawRect(margin, y, margin + 60f, y + 60f, Paint().apply {
                color = android.graphics.Color.parseColor("#22C55E")
            })
            titlePaint.color = android.graphics.Color.parseColor("#0B1220")
            canvas.drawText("SAVING BUDDY", margin + 75f, y + 25f, titlePaint)
            titlePaint.textSize = 14f
            titlePaint.color = android.graphics.Color.parseColor("#64748B")
            canvas.drawText("Financial Report", margin + 75f, y + 45f, titlePaint)
            y += 80f

            paint.color = android.graphics.Color.parseColor("#E5E7EB")
            canvas.drawRect(margin, y, margin + contentWidth, y + 1f, paint)
            y += 20f

            titlePaint.textSize = 22f
            titlePaint.color = android.graphics.Color.parseColor("#0B1220")
            canvas.drawText(reportData.config.title, margin, y, titlePaint)
            y += 25f
            textPaint.color = android.graphics.Color.parseColor("#64748B")
            val dateRangeText = "${dateFormat.format(Date(reportData.config.startDate))} - ${dateFormat.format(Date(reportData.config.endDate))}"
            canvas.drawText(dateRangeText, margin, y, textPaint)
            y += 35f

            canvas.drawRect(margin, y, margin + contentWidth, y + 80f, Paint().apply {
                color = android.graphics.Color.parseColor("#F0FFF4")
            })
            val sectionWidth = contentWidth / 3
            val columnCenters = listOf(
                margin + sectionWidth / 2,
                margin + sectionWidth + sectionWidth / 2,
                margin + 2 * sectionWidth + sectionWidth / 2
            )

            labelPaint.textSize = 10f
            labelPaint.color = android.graphics.Color.parseColor("#64748B")
            labelPaint.textAlign = Paint.Align.CENTER
            valuePaint.textAlign = Paint.Align.CENTER
            valuePaint.color = android.graphics.Color.parseColor("#22C55E")

            labelPaint.color = android.graphics.Color.parseColor("#64748B")
            canvas.drawText("TOTAL INCOME", columnCenters[0], y + 20f, labelPaint)
            valuePaint.color = android.graphics.Color.parseColor("#22C55E")
            canvas.drawText(CurrencyFormatter.format(reportData.totalIncome), columnCenters[0], y + 50f, valuePaint)

            canvas.drawText("TOTAL EXPENSES", columnCenters[1], y + 20f, labelPaint)
            valuePaint.color = android.graphics.Color.parseColor("#EF4444")
            canvas.drawText(CurrencyFormatter.format(reportData.totalExpenses), columnCenters[1], y + 50f, valuePaint)

            canvas.drawText("NET SAVINGS", columnCenters[2], y + 20f, labelPaint)
            valuePaint.color = if (reportData.netSavings >= 0) android.graphics.Color.parseColor("#22C55E") else android.graphics.Color.parseColor("#EF4444")
            canvas.drawText(CurrencyFormatter.format(reportData.netSavings), columnCenters[2], y + 50f, valuePaint)

            y += 100f

            headerPaint.color = android.graphics.Color.parseColor("#22C55E")
            headerPaint.textAlign = Paint.Align.LEFT
            canvas.drawText("SAVINGS RATE", margin, y, headerPaint)
            y += 20f
            valuePaint.textAlign = Paint.Align.LEFT
            valuePaint.textSize = 28f
            valuePaint.color = android.graphics.Color.parseColor("#0B1220")
            canvas.drawText("${reportData.savingsRate.toInt()}%", margin, y, valuePaint)
            y += 30f
            textPaint.color = android.graphics.Color.parseColor("#64748B")
            textPaint.textSize = 11f
            val rateMessage = when {
                reportData.savingsRate >= 50 -> "Excellent! You're saving more than half your income."
                reportData.savingsRate >= 20 -> "Good progress! You're on track with healthy savings."
                reportData.savingsRate >= 0 -> "Room for improvement. Try to save at least 20% of income."
                else -> "Overspending detected. Your expenses exceed your income."
            }
            canvas.drawText(rateMessage, margin, y, textPaint)
            y += 35f

            headerPaint.color = android.graphics.Color.parseColor("#22C55E")
            canvas.drawText("SUMMARY STATISTICS", margin, y, headerPaint)
            y += 20f

            val stats = listOf(
                "Daily Average Spending" to CurrencyFormatter.format(reportData.dailyAverage),
                "Total Transactions" to "${reportData.transactionCount}",
                "Active Categories" to "${reportData.categoryBreakdown.size}",
                "Report Period" to "${30} days"
            )
            stats.forEach { (label, value) ->
                textPaint.color = android.graphics.Color.parseColor("#64748B")
                textPaint.textSize = 11f
                canvas.drawText(label, margin, y, textPaint)
                valuePaint.textSize = 13f
                valuePaint.color = android.graphics.Color.parseColor("#111827")
                canvas.drawText(value, margin + 250f, y, valuePaint)
                y += 18f
            }
            y += 15f

            paint.color = android.graphics.Color.parseColor("#E5E7EB")
            canvas.drawRect(margin, y, margin + contentWidth, y + 1f, paint)
            y += 20f

            headerPaint.color = android.graphics.Color.parseColor("#22C55E")
            canvas.drawText("TOP EXPENSE CATEGORIES", margin, y, headerPaint)
            y += 25f

            reportData.topExpenseCategories.take(5).forEach { category ->
                canvas.drawRect(margin, y, margin + 12f, y + 12f, Paint().apply {
                    color = android.graphics.Color.parseColor("#22C55E")
                })
                textPaint.color = android.graphics.Color.parseColor("#333333")
                textPaint.textSize = 11f
                canvas.drawText(category.category, margin + 20f, y + 10f, textPaint)
                valuePaint.textSize = 11f
                valuePaint.color = android.graphics.Color.parseColor("#111827")
                valuePaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(CurrencyFormatter.format(category.amount), margin + contentWidth - 60f, y + 10f, valuePaint)
                textPaint.textAlign = Paint.Align.RIGHT
                textPaint.color = android.graphics.Color.parseColor("#64748B")
                canvas.drawText("${category.percentage.toInt()}%", margin + contentWidth, y + 10f, textPaint)
                textPaint.textAlign = Paint.Align.LEFT
                y += 22f
            }
            y += 15f

            paint.color = android.graphics.Color.parseColor("#E5E7EB")
            canvas.drawRect(margin, y, margin + contentWidth, y + 1f, paint)
            y += 20f

            headerPaint.color = android.graphics.Color.parseColor("#22C55E")
            canvas.drawText("MONTHLY TREND", margin, y, headerPaint)
            y += 25f

            reportData.monthlyTrend.takeLast(6).forEach { trend ->
                textPaint.color = android.graphics.Color.parseColor("#333333")
                textPaint.textSize = 11f
                canvas.drawText(trend.month.take(7), margin, y + 10f, textPaint)
                valuePaint.textSize = 10f
                valuePaint.color = android.graphics.Color.parseColor("#22C55E")
                valuePaint.textAlign = Paint.Align.RIGHT
                canvas.drawText(CurrencyFormatter.format(trend.income), margin + 180f, y + 10f, valuePaint)
                valuePaint.color = android.graphics.Color.parseColor("#EF4444")
                canvas.drawText(CurrencyFormatter.format(trend.expenses), margin + 300f, y + 10f, valuePaint)
                valuePaint.color = android.graphics.Color.parseColor("#111827")
                canvas.drawText(CurrencyFormatter.format(trend.savings), margin + contentWidth, y + 10f, valuePaint)
                valuePaint.textAlign = Paint.Align.LEFT
                y += 20f
            }
            y += 15f

            paint.color = android.graphics.Color.parseColor("#E5E7EB")
            canvas.drawRect(margin, y, margin + contentWidth, y + 1f, paint)
            y += 20f

            headerPaint.color = android.graphics.Color.parseColor("#22C55E")
            canvas.drawText("INSIGHTS & RECOMMENDATIONS", margin, y, headerPaint)
            y += 25f

            reportData.insights.forEach { insight ->
                textPaint.color = android.graphics.Color.parseColor("#333333")
                textPaint.textSize = 10f
                canvas.drawText("•  $insight", margin, y, textPaint)
                y += 16f
            }
            y += 25f

            paint.color = android.graphics.Color.parseColor("#E5E7EB")
            canvas.drawRect(margin, y, margin + contentWidth, y + 1f, paint)
            y += 25f

            textPaint.color = android.graphics.Color.parseColor("#94A3B8")
            textPaint.textSize = 8f
            canvas.drawText("Generated on ${dateFormat.format(Date())} by Saving Buddy", margin, y, textPaint)
            y += 14f
            canvas.drawText("This is an auto-generated financial report. Data is based on your recorded transactions.", margin, y, textPaint)

            document.finishPage(page)

            val fileName = "saving_buddy_report_${fileNameFormat.format(Date())}.pdf"
            val file = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
            FileOutputStream(file).use { out ->
                document.writeTo(out)
            }
            document.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Saving Buddy Report")
                putExtra(Intent.EXTRA_TEXT, "Here is your financial report from Saving Buddy.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun generateReportData(
        incomes: List<Income>,
        expenses: List<Expense>,
        config: ReportConfig = ReportConfig()
    ): ReportData {
        val totalIncome = incomes.sumOf { it.amount }
        val totalExpenses = expenses.sumOf { it.amount }
        val netSavings = totalIncome - totalExpenses
        val savingsRate = if (totalIncome > 0) (netSavings / totalIncome) * 100 else 0.0

        val categoryMap = expenses.groupBy { it.category.displayName }
        val categoryBreakdown = categoryMap.map { (cat, list) ->
            CategoryBreakdown(
                category = cat,
                amount = list.sumOf { it.amount },
                percentage = if (totalExpenses > 0) (list.sumOf { it.amount } / totalExpenses) * 100 else 0.0,
                transactionCount = list.size
            )
        }.sortedByDescending { it.amount }

        val monthlyTrend = mutableListOf<MonthlyTrend>()
        val cal = Calendar.getInstance()
        for (i in 5 downTo 0) {
            cal.timeInMillis = System.currentTimeMillis()
            cal.add(Calendar.MONTH, -i)
            val monthStart = DateUtils.getStartOfMonth(cal.timeInMillis)
            val monthEnd = DateUtils.getEndOfMonth(cal.timeInMillis)
            val monthIncome = incomes.filter { it.date in monthStart..monthEnd }.sumOf { it.amount }
            val monthExpense = expenses.filter { it.date in monthStart..monthEnd }.sumOf { it.amount }
            monthlyTrend.add(
                MonthlyTrend(
                    month = DateUtils.formatMonthYear(cal.timeInMillis),
                    income = monthIncome,
                    expenses = monthExpense,
                    savings = monthIncome - monthExpense
                )
            )
        }

        val dailyExpenses = expenses.groupBy { DateUtils.getStartOfDay(it.date) }
        val dailyAverage = if (dailyExpenses.isNotEmpty()) totalExpenses / dailyExpenses.size else 0.0

        val insights = mutableListOf<String>()
        when {
            savingsRate >= 50 -> insights.add("Excellent savings rate! You're saving more than 50% of your income.")
            savingsRate >= 20 -> insights.add("Good savings rate of ${savingsRate.toInt()}%. Keep it up!")
            savingsRate >= 10 -> insights.add("Your savings rate is ${savingsRate.toInt()}%. Try to increase it to 20%.")
            savingsRate >= 0 -> insights.add("Your savings rate is low at ${savingsRate.toInt()}%. Consider reducing expenses.")
            else -> insights.add("Overspending detected. Expenses exceed income by ${CurrencyFormatter.format(-netSavings)}.")
        }

        val topCategory = categoryBreakdown.firstOrNull()
        if (topCategory != null) {
            insights.add("Your top spending category is \"${topCategory.category}\" at ${topCategory.percentage.toInt()}% of total expenses.")
        }

        if (totalExpenses > totalIncome * 0.8) {
            insights.add("You're using ${(totalExpenses / totalIncome * 100).toInt()}% of your income. Consider budgeting more strictly.")
        }

        if (totalIncome > 0 && totalExpenses > 0) {
            val ratio = totalIncome / totalExpenses
            when {
                ratio > 2.0 -> insights.add("Your income is double your expenses. Great position for aggressive savings!")
                ratio > 1.5 -> insights.add("Healthy income-to-expense ratio. Consider increasing investments.")
                ratio > 1.0 -> insights.add("Positive cash flow. Look for ways to optimize expenses further.")
                else -> insights.add("Expenses are outpacing income. Review your spending habits.")
            }
        }

        val dailyCount = dailyExpenses.size
        if (dailyCount > 0) {
            insights.add("Average daily spending: ${CurrencyFormatter.format(dailyAverage)} across $dailyCount days.")
        }

        return ReportData(
            config = config,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netSavings = netSavings,
            savingsRate = savingsRate,
            incomes = incomes,
            expenses = expenses,
            categoryBreakdown = categoryBreakdown,
            monthlyTrend = monthlyTrend,
            topExpenseCategories = categoryBreakdown.take(5),
            dailyAverage = dailyAverage,
            transactionCount = incomes.size + expenses.size,
            insights = insights
        )
    }
}
