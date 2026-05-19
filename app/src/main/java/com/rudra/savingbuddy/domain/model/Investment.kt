package com.rudra.savingbuddy.domain.model

data class Investment(
    val id: Long = 0,
    val name: String,
    val type: InvestmentType,
    val amount: Double,
    val currentValue: Double = amount,
    val purchaseDate: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    val returns: Double get() = currentValue - amount
    val returnPercentage: Double get() = if (amount > 0) (returns / amount) * 100.0 else 0.0
}

enum class InvestmentType(val displayName: String) {
    STOCK("Stocks"),
    MUTUAL_FUND("Mutual Funds"),
    FIXED_DEPOSIT("Fixed Deposit"),
    GOLD("Gold"),
    CRYPTO("Crypto"),
    REAL_ESTATE("Real Estate"),
    SAVINGS("Savings"),
    BONDS("Bonds"),
    ETF("ETF"),
    OTHER("Other")
}
