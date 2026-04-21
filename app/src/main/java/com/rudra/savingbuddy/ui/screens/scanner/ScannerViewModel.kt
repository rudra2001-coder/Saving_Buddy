package com.rudra.savingbuddy.ui.screens.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudra.savingbuddy.domain.model.Expense
import com.rudra.savingbuddy.domain.model.ExpenseCategory
import com.rudra.savingbuddy.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.regex.Pattern
import javax.inject.Inject

data class ScannerUiState(
    val recognizedText: String = "",
    val amount: Double = 0.0,
    val storeName: String = "",
    val date: String = "",
    val detectedCategory: ExpenseCategory? = null,
    val isProcessing: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val amountPatterns = listOf(
        Pattern.compile("Rs\\.?\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("₹\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("INR\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("Total[:\\s]*Rs?\\.?\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("Amount[:\\s]*Rs?\\.?\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("Grand Total[:\\s]*Rs?\\.?\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("₹([\\d,]+\\.?\\d*)")
    )

    private val datePatterns = listOf(
        Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})"),
        Pattern.compile("(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{2,4})", Pattern.CASE_INSENSITIVE)
    )

    private val categoryKeywords = mapOf(
        ExpenseCategory.FOOD to listOf("restaurant", "food", "pizza", "burger", "cafe", "coffee", "tea", "diner", "kitchen", "eatery", "mess"),
        ExpenseCategory.TRANSPORT to listOf("uber", "ola", "taxi", "fuel", "petrol", "diesel", "gas", "station", "metro", "railway"),
        ExpenseCategory.SHOPPING to listOf("mart", "store", "shop", "market", "mall", "retail", "commerce", "flipkart", "amazon", "supermarket"),
        ExpenseCategory.BILLS to listOf("electricity", "water", "bill", "utility", "power", "gas", "phone", "internet", "recharge"),
        ExpenseCategory.ENTERTAINMENT to listOf("movie", "cinema", "theatre", "netflix", "spotify", "game", "entertainment", "park", "museum"),
        ExpenseCategory.HEALTH to listOf("pharmacy", "medicine", "hospital", "clinic", "doctor", "health", "medical", "diagnostic"),
        ExpenseCategory.EDUCATION to listOf("book", "stationery", "tuition", "course", "school", "college", "education", "academy"),
        ExpenseCategory.GIFTS to listOf("gift", "present", "flowers", "greeting"),
        ExpenseCategory.TRAVEL to listOf("hotel", "travel", "airline", "flight", "booking", "resort", "tour"),
        ExpenseCategory.SUBSCRIPTIONS to listOf("subscription", "membership", "subscription", "plan"),
        ExpenseCategory.RENT to listOf("rent", "lease", "property", "flat", "apartment")
    )

    fun processRecognizedText(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isProcessing = true, recognizedText = text)

            val amount = extractAmount(text)
            val date = extractDate(text)
            val category = detectCategory(text)
            val storeName = extractStoreName(text)

            _uiState.value = _uiState.value.copy(
                amount = amount,
                date = date,
                detectedCategory = category,
                storeName = storeName,
                isProcessing = false
            )
        }
    }

    private fun extractAmount(text: String): Double {
        val cleanedText = text.replace(",", "").replace(" ", "")
        
        for (pattern in amountPatterns) {
            val matcher = pattern.matcher(cleanedText)
            if (matcher.find()) {
                val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                try {
                    val amount = amountStr.toDouble()
                    if (amount > 0 && amount < 10000000) { // Reasonable max
                        return amount
                    }
                } catch (e: NumberFormatException) {
                    continue
                }
            }
        }
        return 0.0
    }

    private fun extractDate(text: String): String {
        for (pattern in datePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return matcher.group(0)
            }
        }
        return ""
    }

    private fun detectCategory(text: String): ExpenseCategory? {
        val lowerText = text.lowercase()
        
        for ((category, keywords) in categoryKeywords) {
            for (keyword in keywords) {
                if (lowerText.contains(keyword)) {
                    return category
                }
            }
        }
        return ExpenseCategory.OTHERS
    }

    private fun extractStoreName(text: String): String {
        val lines = text.split("\n")
        for (line in lines.take(3)) {
            val trimmed = line.trim()
            if (trimmed.length >= 3 && trimmed.length <= 50 && !trimmed.matches(Regex(".*\\d.*"))) {
                return trimmed
            }
        }
        return ""
    }

    fun reset() {
        _uiState.value = ScannerUiState()
    }

    fun saveAsExpense() {
        val state = _uiState.value
        if (state.amount <= 0) return

        viewModelScope.launch {
            expenseRepository.insertExpense(
                Expense(
                    amount = state.amount,
                    category = state.detectedCategory ?: ExpenseCategory.OTHERS,
                    date = System.currentTimeMillis(),
                    notes = buildString {
                        if (state.storeName.isNotEmpty()) append("Store: ${state.storeName}\n")
                        if (state.date.isNotEmpty()) append("Date: ${state.date}\n")
                        if (state.recognizedText.length <= 200) append(state.recognizedText)
                    }.ifEmpty { null }
                )
            )
        }
    }
}