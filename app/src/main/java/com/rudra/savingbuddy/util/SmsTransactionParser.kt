package com.rudra.savingbuddy.util

import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.model.Income
import com.rudra.savingbuddy.domain.model.IncomeCategory

object SmsTransactionParser {
    private val expensePatterns = listOf(
        Regex("""debited.*?Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""paid.*?Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""spent.*?Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?).*?debited""", RegexOption.IGNORE_CASE),
        Regex("""debited\s+(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""UPI.*?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""payment.*?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""₹(\d+(?:,\d{3})*)""", RegexOption.IGNORE_CASE)
    )

    private val incomePatterns = listOf(
        Regex("""credited.*?Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""received.*?Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""deposited.*?Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""Rs\.?(\d+(?:,\d{3})*(?:\.\d{2})?).*?credited""", RegexOption.IGNORE_CASE),
        Regex("""salary.*?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""NEFT.*?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE),
        Regex("""IMPS.*?(\d+(?:,\d{3})*(?:\.\d{2})?)""", RegexOption.IGNORE_CASE)
    )

    private val categoryKeywords = mapOf(
        ExpenseCategory.FOOD to listOf("zomato", "swiggy", "food", "restaurant", "diner", "cafe", "pizza", "burger"),
        ExpenseCategory.TRANSPORT to listOf("uber", "ola", "taxi", "metro", "railway", "flight", "airline", "petrol", "fuel"),
        ExpenseCategory.BILLS to listOf("electricity", "water", "gas", "bill", " recharge", "jio", "airtel", "vodafone"),
        ExpenseCategory.SHOPPING to listOf("amazon", "flipkart", "myntra", "shopify", "store", "mall", "market")
    )

    fun parseSms(message: String): ParsedTransaction? {
        val cleanMessage = message.replace(",", "")

        // Try expense patterns first
        for (pattern in expensePatterns) {
            val match = pattern.find(cleanMessage)
            if (match != null) {
                val amountString = match.groupValues[1].replace(",", "")
                val amount = amountString.toDoubleOrNull() ?: return null

                if (amount < 10 || amount > 10000000) return null // Reasonable range

                val category = detectCategory(cleanMessage)
                return ParsedTransaction(
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    category = category,
                    date = System.currentTimeMillis()
                )
            }
        }

        // Try income patterns
        for (pattern in incomePatterns) {
            val match = pattern.find(cleanMessage)
            if (match != null) {
                val amountString = match.groupValues[1].replace(",", "")
                val amount = amountString.toDoubleOrNull() ?: return null

                if (amount < 100) return null // Min income threshold

                return ParsedTransaction(
                    type = TransactionType.INCOME,
                    amount = amount,
                    category = null,
                    date = System.currentTimeMillis()
                )
            }
        }

        return null
    }

    private fun detectCategory(message: String): ExpenseCategory {
        val lowerMessage = message.lowercase()

        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (lowerMessage.contains(keyword)) {
                    return category
                }
            }
        }

        return ExpenseCategory.OTHERS
    }

    fun getSuggestions(messages: List<String>): List<ParsedTransaction> {
        return messages.mapNotNull { parseSms(it) }
    }
}

data class ParsedTransaction(
    val type: TransactionType,
    val amount: Double,
    val category: ExpenseCategory?,
    val date: Long
)

enum class TransactionType {
    INCOME,
    EXPENSE
}