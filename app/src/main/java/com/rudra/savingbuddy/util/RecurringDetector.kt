package com.rudra.savingbuddy.util

import com.rudra.savingbuddy.domain.model.*
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs
import kotlin.math.min

@Singleton
class RecurringDetector @Inject constructor() {

    fun detectPatterns(
        incomeTransactions: List<Income>,
        expenseTransactions: List<Expense>,
        lookbackMonths: Int = 6
    ): List<DetectedTransaction> {
        val allTransactions = mutableListOf<RawTransaction>()

        incomeTransactions
            .filter { !it.isRecurring }
            .forEach { income ->
                allTransactions.add(
                    RawTransaction(
                        description = income.source.ifBlank { income.category.displayName },
                        amount = income.amount,
                        category = income.category.name,
                        date = income.date,
                        type = TransactionType.INCOME
                    )
                )
            }

        expenseTransactions
            .filter { !it.isRecurring }
            .forEach { expense ->
                allTransactions.add(
                    RawTransaction(
                        description = expense.notes ?: expense.category.displayName,
                        amount = expense.amount,
                        category = expense.category.name,
                        date = expense.date,
                        type = TransactionType.EXPENSE
                    )
                )
            }

        val cutoffDate = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(lookbackMonths * 30L)
        val recentTransactions = allTransactions.filter { it.date >= cutoffDate }
        if (recentTransactions.size < 3) return emptyList()

        val groups = groupSimilarTransactions(recentTransactions)
        val patterns = mutableListOf<DetectedTransaction>()

        groups.forEach { group ->
            if (group.size >= 3) {
                val pattern = analyzeGroup(group)
                if (pattern != null && pattern.confidence >= 0.6) {
                    patterns.add(pattern)
                }
            }
        }

        return patterns.sortedByDescending { it.confidence }.take(10)
    }

    private fun groupSimilarTransactions(transactions: List<RawTransaction>): List<List<RawTransaction>> {
        val groups = mutableListOf<MutableList<RawTransaction>>()
        val used = mutableSetOf<Int>()

        for (i in transactions.indices) {
            if (used.contains(i)) continue
            val group = mutableListOf(transactions[i])
            used.add(i)
            for (j in (i + 1) until transactions.size) {
                if (used.contains(j)) continue
                if (areSimilar(transactions[i], transactions[j])) {
                    group.add(transactions[j])
                    used.add(j)
                }
            }
            groups.add(group)
        }

        return groups
    }

    private fun areSimilar(a: RawTransaction, b: RawTransaction): Boolean {
        if (a.type != b.type) return false

        val amountDiff = abs(a.amount - b.amount)
        val avgAmount = (a.amount + b.amount) / 2.0
        if (avgAmount > 0 && amountDiff / avgAmount > 0.05 && amountDiff > 10.0) return false

        if (a.category != b.category) return false

        val descA = normalizeDescription(a.description)
        val descB = normalizeDescription(b.description)

        if (descA == descB) return true
        if (descA.contains(descB) || descB.contains(descA)) return true

        if (descA.length > 3 && descB.length > 3) {
            val distance = levenshteinDistance(descA, descB)
            val maxLen = maxOf(descA.length, descB.length)
            if (distance.toDouble() / maxLen < 0.3) return true
        }

        return false
    }

    private fun analyzeGroup(group: List<RawTransaction>): DetectedTransaction? {
        if (group.size < 3) return null

        val sortedByDate = group.sortedBy { it.date }
        val representative = sortedByDate.first()

        val intervals = mutableListOf<Long>()
        for (i in 1 until sortedByDate.size) {
            intervals.add(sortedByDate[i].date - sortedByDate[i - 1].date)
        }

        val avgInterval = intervals.average()
        val avgIntervalDays = avgInterval / TimeUnit.DAYS.toMillis(1)

        val suggestedInterval = when {
            avgIntervalDays in 0.8..1.2 -> RecurringInterval.DAILY
            avgIntervalDays in 6.5..7.5 -> RecurringInterval.WEEKLY
            avgIntervalDays in 13.0..15.0 -> RecurringInterval.BI_WEEKLY
            avgIntervalDays in 27.0..31.0 -> RecurringInterval.MONTHLY
            avgIntervalDays in 80.0..100.0 -> RecurringInterval.QUARTERLY
            avgIntervalDays in 350.0..380.0 -> RecurringInterval.YEARLY
            else -> return null
        }

        val intervalVariance = intervals.map { abs(it - avgInterval) }.average()
        val normalizedVariance = if (avgInterval > 0) intervalVariance / avgInterval else 1.0
        val consistencyScore = maxOf(0.0, 1.0 - normalizedVariance)
        val occurrenceBonus = minOf(0.2, (group.size - 3) * 0.05)
        val confidence = minOf(1.0, consistencyScore + occurrenceBonus)

        if (confidence < 0.6) return null

        val calendar = Calendar.getInstance()
        val daysOfMonth = sortedByDate.map {
            calendar.timeInMillis = it.date
            calendar.get(Calendar.DAY_OF_MONTH)
        }
        val avgDayOfMonth = daysOfMonth.average().toInt()

        val daysOfWeek = sortedByDate.map {
            calendar.timeInMillis = it.date
            calendar.get(Calendar.DAY_OF_WEEK)
        }
        val avgDayOfWeek = daysOfWeek.average().toInt()

        return DetectedTransaction(
            description = representative.description,
            amount = representative.amount,
            category = representative.category,
            type = representative.type,
            dates = sortedByDate.map { it.date },
            averageDayOfMonth = avgDayOfMonth,
            averageDayOfWeek = avgDayOfWeek,
            confidence = confidence,
            suggestedInterval = suggestedInterval,
            occurrences = group.size
        )
    }

    private fun normalizeDescription(desc: String): String {
        return desc.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun levenshteinDistance(s1: String, s2: String): Int {
        val dp = Array(s1.length + 1) { IntArray(s2.length + 1) }
        for (i in 0..s1.length) dp[i][0] = i
        for (j in 0..s2.length) dp[0][j] = j
        for (i in 1..s1.length) {
            for (j in 1..s2.length) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(dp[i - 1][j] + 1, dp[i][j - 1] + 1, dp[i - 1][j - 1] + cost)
            }
        }
        return dp[s1.length][s2.length]
    }

    private data class RawTransaction(
        val description: String,
        val amount: Double,
        val category: String,
        val date: Long,
        val type: TransactionType
    )
}
