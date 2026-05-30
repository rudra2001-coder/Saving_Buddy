package com.rudra.savingbuddy.domain.model

data class DetectedTransaction(
    val description: String,
    val amount: Double,
    val category: String,
    val type: TransactionType,
    val dates: List<Long>,
    val averageDayOfMonth: Int?,
    val averageDayOfWeek: Int?,
    val confidence: Double,
    val suggestedInterval: RecurringInterval,
    val occurrences: Int
)

enum class TransactionType { INCOME, EXPENSE }

data class DetectionResult(
    val patterns: List<DetectedTransaction>,
    val lastDetectionDate: Long,
    val totalTransactionsAnalyzed: Int
)

data class DismissedPattern(
    val description: String,
    val amount: Double,
    val category: String,
    val dismissedAt: Long
)
