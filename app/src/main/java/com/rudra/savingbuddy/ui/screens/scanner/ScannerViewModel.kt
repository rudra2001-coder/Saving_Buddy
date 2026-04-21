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
import java.text.SimpleDateFormat
import java.util.*
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
        Pattern.compile("(?i)(?:BDT)[:\\s]*([\\d,]+\\.?\\d*)"),
        Pattern.compile("([\\d,]+\\.?\\d{2})"), // Any decimal number
        Pattern.compile("(\\d+)\\.00") // Whole numbers with .00
    )

    private val datePatterns = listOf(
        Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})"),
        Pattern.compile("(\\d{1,2})\\s+(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z]*\\s+(\\d{2,4})", Pattern.CASE_INSENSITIVE),
        Pattern.compile("(\\d{4})[/-](\\d{1,2})[/-](\\d{1,2})")
    )

    private val storeKeywords = listOf(
        "mart", "store", "shop", "market", "mall", "super", "restaurant",
        "cafe", "hotel", "pharmacy", "medical", "hospital"
    )

    private val categoryKeywords = mapOf(
        ExpenseCategory.FOOD to listOf(
            "restaurant", "food", "pizza", "burger", "cafe", "coffee", "tea",
            "diner", "kitchen", "eatery", "mess", "biriyani", "polao", "kacchi",
            "fast food", "snacks", "bakery", "cake", "confectionery", "buffet"
        ),
        ExpenseCategory.TRANSPORT to listOf(
            "uber", "ola", "taxi", "fuel", "petrol", "diesel", "gas", "station",
            "metro", "railway", "bus", "cng", "rickshaw", "parking", "toll",
            "transport", "travel", "commute", "fare"
        ),
        ExpenseCategory.SHOPPING to listOf(
            "mart", "store", "shop", "market", "mall", "retail", "commerce",
            "flipkart", "amazon", "supermarket", "grocery", "bazar", "clothing",
            "apparel", "fashion", "footwear", "electronics", "gadget"
        ),
        ExpenseCategory.BILLS to listOf(
            "electricity", "water", "bill", "utility", "power", "gas", "phone",
            "internet", "recharge", "disconnection", "prepaid", "postpaid",
            "broadband", "wifi", "mobile", "cellular"
        ),
        ExpenseCategory.ENTERTAINMENT to listOf(
            "movie", "cinema", "theatre", "netflix", "spotify", "game",
            "entertainment", "park", "museum", "zoo", "amusement", "concert",
            "event", "show", "ticket"
        ),
        ExpenseCategory.HEALTH to listOf(
            "pharmacy", "medicine", "hospital", "clinic", "doctor", "health",
            "medical", "diagnostic", "lab", "test", "prescription", "dental",
            "optical", "eyecare", "wellness", "fitness", "gym"
        ),
        ExpenseCategory.EDUCATION to listOf(
            "book", "stationery", "tuition", "course", "school", "college",
            "education", "academy", "coaching", "training", "learning",
            "workshop", "seminar", "webinar"
        ),
        ExpenseCategory.GIFTS to listOf(
            "gift", "present", "flowers", "greeting", "card", "celebration",
            "birthday", "anniversary", "wedding"
        ),
        ExpenseCategory.TRAVEL to listOf(
            "hotel", "travel", "airline", "flight", "booking", "resort",
            "tour", "ticket", "bus", "train", "launch", "vacation", "trip",
            "stay", "accommodation"
        ),
        ExpenseCategory.SUBSCRIPTIONS to listOf(
            "subscription", "membership", "plan", "renewal", "prime", "netflix",
            "youtube", "spotify", "amazon", "monthly", "yearly", "annual"
        ),
        ExpenseCategory.RENT to listOf(
            "rent", "lease", "property", "flat", "apartment", "house", "room",
            "deposit", "maintenance", "society"
        ),
        ExpenseCategory.OTHERS to listOf(
            "receipt", "invoice", "bill", "payment", "transaction", "purchase",
            "order", "checkout"
        )
    )

    fun processRecognizedText(text: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isProcessing = true,
                recognizedText = text
            )

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

        // Try to find amount with currency indicators first
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

        // Fallback: Look for any number that looks like currency
        val numberPattern = Pattern.compile("\\b(\\d+(?:\\.\\d{2})?)\\b")
        val amounts = mutableListOf<Double>()

        lines.forEach { line ->
            val matcher = numberPattern.matcher(line)
            while (matcher.find()) {
                try {
                    val amount = matcher.group(1).toDouble()
                    if (amount in 10.0..100000.0) {
                        amounts.add(amount)
                    }
                } catch (e: NumberFormatException) {
                    // Skip
                }
            }
        }

        // Return the largest reasonable amount (likely the total)
        return amounts.maxOrNull() ?: 0.0
    }

    private fun extractDate(text: String): String {
        for (pattern in datePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                return try {
                    val dateStr = matcher.group(0)
                    // Try to parse and format consistently
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val parsed = parseDate(dateStr)
                    parsed?.let { sdf.format(it) } ?: dateStr
                } catch (e: Exception) {
                    matcher.group(0)
                }
            }
        }
        return ""
    }

    private fun parseDate(dateStr: String): Date? {
        val formats = listOf(
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()),
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        )

        formats.forEach { format ->
            try {
                return format.parse(dateStr)
            } catch (e: Exception) {
                // Try next format
            }
        }
        return null
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

        // First try to find store indicators
        for (line in lines.take(8)) {
            val trimmed = line.trim()
            if (trimmed.length in 3..40 &&
                !trimmed.matches(Regex(".*\\d.*")) &&
                !trimmed.matches(Regex("(?i)(receipt|invoice|total|amount|date|tax|vat|bill|payment|thank|welcome)"))) {

                // Check if it contains store keywords
                if (storeKeywords.any { trimmed.lowercase().contains(it) }) {
                    return formatStoreName(trimmed)
                }
            }
        }

        // Fallback: Take the first reasonable looking line
        for (line in lines.take(5)) {
            val trimmed = line.trim()
            if (trimmed.length in 3..40 &&
                !trimmed.matches(Regex(".*\\d.*")) &&
                trimmed.any { it.isLetter() }) {
                return formatStoreName(trimmed)
            }
        }

        return ""
    }

    private fun formatStoreName(name: String): String {
        return name.split(" ")
            .filter { it.isNotEmpty() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { it.uppercase() }
            }
            .take(50)
    }

    fun updateAmount(amount: Double) {
        _uiState.value = _uiState.value.copy(amount = amount)
    }

    fun updateCategory(category: ExpenseCategory) {
        _uiState.value = _uiState.value.copy(detectedCategory = category)
    }

    fun updateStoreName(storeName: String) {
        _uiState.value = _uiState.value.copy(storeName = storeName)
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
                        if (state.storeName.isNotEmpty()) {
                            append("Store: ${state.storeName}")
                        }
                        if (state.date.isNotEmpty()) {
                            if (isNotEmpty()) append("\n")
                            append("Receipt Date: ${state.date}")
                        }
                        if (state.recognizedText.length <= 200) {
                            if (isNotEmpty()) append("\n\n")
                            append(state.recognizedText)
                        }
                    }.ifEmpty { "Scanned from receipt" }
                )
            )
        }
    }
}