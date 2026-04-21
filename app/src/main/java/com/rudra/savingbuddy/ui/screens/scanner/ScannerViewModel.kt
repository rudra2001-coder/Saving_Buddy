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
        Pattern.compile("(?i)(?:Total|Amount|Grand Total|Net Amount|Payable| payable)[:\\s]*[Tk৳]\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("(?i)(?:Rs\\.?|INR)[:\\s]*([\\d,]+\\.?\\d*)"),
        Pattern.compile("[Tk৳]\\s*([\\d,]+\\.?\\d*)"),
        Pattern.compile("([\\d,]+\\.?\\d*)\\s*(?:Tk|৳)"),
        Pattern.compile("(?i)(?:BDT)[:\\s]*([\\d,]+\\.?\\d*)")
    )

    private val datePatterns = listOf(
        Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})"),
        Pattern.compile("(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{2,4})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})")
    )

    private val categoryKeywords = mapOf(
        ExpenseCategory.FOOD to listOf("restaurant", "food", "pizza", "burger", "cafe", "coffee", "tea", "diner", "kitchen", "eatery", "mess", "biriyani", "polao", "kacchi", "fast food", "snacks", "bakery", "cake", " confectionery"),
        ExpenseCategory.TRANSPORT to listOf("uber", "ola", "taxi", "fuel", "petrol", "diesel", "gas", "station", "metro", "railway", "bus", "cng", "rickshaw", "parking", "toll"),
        ExpenseCategory.SHOPPING to listOf("mart", "store", "shop", "market", "mall", "retail", "commerce", "flipkart", "amazon", "supermarket", "grocery", "bazar", "department store"),
        ExpenseCategory.BILLS to listOf("electricity", "water", "bill", "utility", "power", "gas", "phone", "internet", "recharge", "disconnection", " prepaid", "postpaid"),
        ExpenseCategory.ENTERTAINMENT to listOf("movie", "cinema", "theatre", "netflix", "spotify", "game", "entertainment", "park", "museum", "zoo", "amusement"),
        ExpenseCategory.HEALTH to listOf("pharmacy", "medicine", "hospital", "clinic", "doctor", "health", "medical", "diagnostic", "lab", "test", "prescription"),
        ExpenseCategory.EDUCATION to listOf("book", "stationery", "tuition", "course", "school", "college", "education", "academy", "coaching", "training"),
        ExpenseCategory.GIFTS to listOf("gift", "present", "flowers", "greeting", "card", "celebration"),
        ExpenseCategory.TRAVEL to listOf("hotel", "travel", "airline", "flight", "booking", "resort", "tour", "ticket", "bus", "train", "launch"),
        ExpenseCategory.SUBSCRIPTIONS to listOf("subscription", "membership", "subscription", "plan", "renewal", "prime", "netflix", "youtube"),
        ExpenseCategory.RENT to listOf("rent", "lease", "property", "flat", "apartment", "house", "room", "deposit"),
        ExpenseCategory.OTHERS to listOf("receipt", "invoice", "bill", "payment", "transaction")
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
        val lines = text.split("\n").reversed()

        for (line in lines) {
            val lineCleaned = line.replace(",", "").replace(" ", "")
            for (pattern in amountPatterns) {
                val matcher = pattern.matcher(lineCleaned)
                if (matcher.find()) {
                    val amountStr = matcher.group(1)?.replace(",", "") ?: continue
                    try {
                        val amount = amountStr.toDouble()
                        if (amount > 0 && amount < 1000000) {
                            return amount
                        }
                    } catch (e: NumberFormatException) {
                        continue
                    }
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
        var bestMatch: ExpenseCategory? = null
        var maxMatches = 0

        for ((category, keywords) in categoryKeywords) {
            var matches = 0
            for (keyword in keywords) {
                if (lowerText.contains(keyword)) {
                    matches++
                }
            }
            if (matches > maxMatches) {
                maxMatches = matches
                bestMatch = category
            }
        }
        return bestMatch ?: ExpenseCategory.OTHERS
    }

    private fun extractStoreName(text: String): String {
        val lines = text.split("\n")
        for (line in lines.take(5)) {
            val trimmed = line.trim()
            if (trimmed.length >= 3 && trimmed.length <= 50 && 
                !trimmed.matches(Regex(".*\\d.*")) && 
                !trimmed.matches(Regex("(?i)(receipt|invoice|total|amount|date)"))) {
                return trimmed.replaceFirstChar { it.uppercase() }
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