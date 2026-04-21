package com.rudra.savingbuddy.domain.model

data class Currency(
    val code: String,
    val name: String,
    val symbol: String,
    val exchangeRate: Double = 1.0
)

object SupportedCurrencies {
    val DEFAULT = Currency("BDT", "Bangladeshi Taka", "৳", 1.0)
    
    val ALL = listOf(
        Currency("BDT", "Bangladeshi Taka", "৳", 1.0),
        Currency("USD", "US Dollar", "$", 1.0),
        Currency("EUR", "Euro", "€", 0.92),
        Currency("GBP", "British Pound", "£", 0.79),
        Currency("INR", "Indian Rupee", "₹", 83.12),
        Currency("JPY", "Japanese Yen", "¥", 149.50),
        Currency("CNY", "Chinese Yuan", "¥", 7.24),
        Currency("AUD", "Australian Dollar", "A$", 1.53),
        Currency("CAD", "Canadian Dollar", "C$", 1.36),
        Currency("SGD", "Singapore Dollar", "S$", 1.34),
        Currency("AED", "UAE Dirham", "د.إ", 3.67),
        Currency("SAR", "Saudi Riyal", "﷼", 3.75),
        Currency("MYR", "Malaysian Ringgit", "RM", 4.72),
        Currency("THB", "Thai Baht", "฿", 35.89),
        Currency("KRW", "South Korean Won", "₩", 1320.0),
        Currency("BRL", "Brazilian Real", "R$", 4.97)
    )
    
    fun getByCode(code: String): Currency {
        return ALL.find { it.code == code } ?: DEFAULT
    }
}

data class MultiCurrencySettings(
    val baseCurrency: String = "USD",
    val showConvertedAmount: Boolean = true,
    val customRates: Map<String, Double> = emptyMap()
)