package com.rudra.savingbuddy.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.net.URL
import java.util.concurrent.CopyOnWriteArrayList

data class ExchangeRate(
    val code: String,
    val name: String,
    val symbol: String,
    val flag: String,
    val rateToUsd: Double
)

object ExchangeManager {
    private val json = Json { ignoreUnknownKeys = true }

    private val defaultCurrencies = listOf(
        ExchangeRate("BDT", "Bangladeshi Taka", "\u09F3", "\uD83C\uDDE7\uD83C\uDDE9", 109.50),
        ExchangeRate("USD", "US Dollar", "$", "\uD83C\uDDFA\uD83C\uDDF8", 1.0),
        ExchangeRate("EUR", "Euro", "\u20AC", "\uD83C\uDDEA\uD83C\uDDFA", 0.92),
        ExchangeRate("GBP", "British Pound", "\u00A3", "\uD83C\uDDEC\uD83C\uDDE7", 0.79),
        ExchangeRate("INR", "Indian Rupee", "\u20B9", "\uD83C\uDDEE\uD83C\uDDF3", 83.0),
        ExchangeRate("PKR", "Pakistani Rupee", "\u20A8", "\uD83C\uDDF5\uD83C\uDDF0", 278.0),
        ExchangeRate("JPY", "Japanese Yen", "\u00A5", "\uD83C\uDDEF\uD83C\uDDF5", 149.50),
        ExchangeRate("CNY", "Chinese Yuan", "\u00A5", "\uD83C\uDDE8\uD83C\uDDF3", 7.24),
        ExchangeRate("KRW", "South Korean Won", "\u20A9", "\uD83C\uDDF0\uD83C\uDDF7", 1320.0),
        ExchangeRate("SAR", "Saudi Riyal", "\uFDFC", "\uD83C\uDDF8\uD83C\uDDE6", 3.75),
        ExchangeRate("AED", "UAE Dirham", "\u062F.\u0625", "\uD83C\uDDE6\uD83C\uDDEA", 3.67),
        ExchangeRate("MYR", "Malaysian Ringgit", "RM", "\uD83C\uDDF2\uD83C\uDDFE", 4.68),
        ExchangeRate("SGD", "Singapore Dollar", "S$", "\uD83C\uDDF8\uD83C\uDDEC", 1.34),
        ExchangeRate("AUD", "Australian Dollar", "A$", "\uD83C\uDDE6\uD83C\uDDFA", 1.53)
    )

    private val _listeners = CopyOnWriteArrayList<() -> Unit>()

    private var _liveRates: Map<String, Double>? = null
    var lastUpdated: Long = 0L
        private set

    val currencies: List<ExchangeRate>
        get() = defaultCurrencies.map { currency ->
            val liveRate = _liveRates?.get(currency.code)
            if (liveRate != null) currency.copy(rateToUsd = liveRate) else currency
        }

    fun getRate(code: String): Double {
        return _liveRates?.get(code) ?: defaultCurrencies.find { it.code == code }?.rateToUsd ?: 1.0
    }

    fun convert(amount: Double, from: String, to: String): Double {
        val fromRate = getRate(from)
        val toRate = getRate(to)
        return if (fromRate > 0) (amount / fromRate) * toRate else 0.0
    }

    fun getRateText(from: String, to: String): String {
        val fromRate = getRate(from)
        val toRate = getRate(to)
        return if (fromRate > 0) String.format("%.4f", toRate / fromRate) else "N/A"
    }

    suspend fun refreshRates() {
        withContext(Dispatchers.IO) {
            try {
                val response = URL("https://api.frankfurter.app/latest?from=USD").readText()
                val root = json.parseToJsonElement(response).jsonObject
                val ratesObject = root["rates"]?.jsonObject ?: return@withContext

                val rates = mutableMapOf("USD" to 1.0)
                ratesObject.forEach { (code, element) ->
                    element.jsonPrimitive.doubleOrNull?.let { rates[code] = it }
                }

                _liveRates = rates
                lastUpdated = System.currentTimeMillis()
                _listeners.forEach { it() }
            } catch (e: Exception) {
                _liveRates = null
            }
        }
    }

    fun addListener(listener: () -> Unit) {
        _listeners.add(listener)
    }

    fun removeListener(listener: () -> Unit) {
        _listeners.remove(listener)
    }
}
