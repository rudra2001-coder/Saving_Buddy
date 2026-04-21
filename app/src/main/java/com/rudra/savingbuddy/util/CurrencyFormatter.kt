package com.rudra.savingbuddy.util

import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)

    fun format(amount: Double): String = currencyFormat.format(amount)

    fun formatBDT(amount: Double): String = "৳${String.format(Locale.getDefault(), "%,.0f", amount)}"

    fun formatCompact(amount: Double): String = when {
        amount >= 1_000_000 -> String.format(Locale.US, "$%.1fM", amount / 1_000_000)
        amount >= 1_000 -> String.format(Locale.US, "$%.1fK", amount / 1_000)
        else -> format(amount)
    }
}