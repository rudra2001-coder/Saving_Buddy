package com.rudra.savingbuddy.domain.model

data class Account(
    val id: Long = 0,
    val name: String,
    val type: AccountType,
    val provider: String,
    val accountNumber: String,
    val balance: Double = 0.0,
    val initialBalance: Double = 0.0,
    val currency: String = "BDT",
    val iconColor: Long = 0xFF6200EE,
    val isActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val dailyLimit: Double? = null,
    val usedToday: Double = 0.0,
    val linkedGoalId: Long? = null,
    val displayOrder: Int = 0
)

enum class AccountType(val displayName: String, val icon: String) {
    WALLET("Wallet", "💼"),
    BANK("Bank Account", "🏦"),
    MOBILE_BANKING("Mobile Banking", "📱"),
    DIGITAL_WALLET("Digital Wallet", "💳"),
    CREDIT_CARD("Credit Card", "💳")
}

enum class AccountProvider(val type: AccountType, val displayName: String, val icon: String, val dailyTransferLimit: Double, val isCustom: Boolean = false) {
    // Mobile Banking (Bangladesh)
    BKASH(AccountType.MOBILE_BANKING, "bKash", "🟠", 25000.0),
    NAGAD(AccountType.MOBILE_BANKING, "Nagad", "🔴", 25000.0),
    ROCKET(AccountType.MOBILE_BANKING, "Rocket", "🟣", 25000.0),
    UPAY(AccountType.MOBILE_BANKING, "Upay", "🟢", 20000.0),
    OTHER_MOBILE_BANKING(AccountType.MOBILE_BANKING, "Other Mobile Banking", "📱", 25000.0, true),
    
    // Banks
    DBBL(AccountType.BANK, "DBBL Nexus", "🟦", 50000.0),
    CITY_BANK(AccountType.BANK, "City Bank Touch", "🟩", 50000.0),
    BRAC_BANK(AccountType.BANK, "BRAC Bank", "🟧", 40000.0),
    STANDARD_CHARTERED(AccountType.BANK, "Standard Chartered", "🔵", 100000.0),
    HSBC(AccountType.BANK, "HSBC", "⚫", 100000.0),
    OTHER_BANK(AccountType.BANK, "Other Bank", "🏦", 50000.0, true),
    
    // Wallets
    CASH(AccountType.WALLET, "Cash Wallet", "💵", 0.0),
    OTHERS_WALLET(AccountType.WALLET, "Other Wallet", "💼", 0.0, true),
    
    // Digital Wallets
    PAYPAL(AccountType.DIGITAL_WALLET, "PayPal", "🔵", 10000.0),
    PAYONEER(AccountType.DIGITAL_WALLET, "Payoneer", "🟣", 10000.0),
    STRIPE(AccountType.DIGITAL_WALLET, "Stripe", "🟣", 10000.0),
    OTHER_DIGITAL_WALLET(AccountType.DIGITAL_WALLET, "Other Digital Wallet", "💳", 10000.0, true),
    
    // Credit Cards
    OTHER_CREDIT_CARD(AccountType.CREDIT_CARD, "Other Credit Card", "💳", 0.0, true)
}

data class Transfer(
    val id: Long = 0,
    val fromAccountId: Long,
    val toAccountId: Long,
    val amount: Double,
    val fee: Double = 0.0,
    val note: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val status: TransferStatus = TransferStatus.COMPLETED,
    val reference: String? = null
)

enum class TransferStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class TransferResult(
    val success: Boolean,
    val errorMessage: String? = null,
    val fromAccountNewBalance: Double = 0.0,
    val toAccountNewBalance: Double = 0.0,
    val transactionId: String? = null
)

data class DailyTransferSummary(
    val date: Long,
    val totalTransferred: Double,
    val totalReceived: Double,
    val transactionCount: Int
)

data class AccountBalanceHistory(
    val accountId: Long,
    val date: Long,
    val balance: Double
)