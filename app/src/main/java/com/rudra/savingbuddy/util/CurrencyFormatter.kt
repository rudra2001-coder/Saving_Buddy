package com.rudra.savingbuddy.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private var currentCurrency: String = "BDT"
    private var currencySymbol: String = "৳"

    private val currencyConfigs = mapOf(
        "BDT" to CurrencyConfig("BDT", "৳", "Bangladesh"),
        "USD" to CurrencyConfig("USD", "$", "US Dollar"),
        "EUR" to CurrencyConfig("EUR", "€", "Euro"),
        "GBP" to CurrencyConfig("GBP", "£", "British Pound"),
        "INR" to CurrencyConfig("INR", "₹", "Indian Rupee"),
        "PKR" to CurrencyConfig("PKR", "₨", "Pakistani Rupee")
    )

    data class CurrencyConfig(val code: String, val symbol: String, val name: String)

    fun setCurrency(currency: String) {
        currentCurrency = currency
        currencySymbol = currencyConfigs[currency]?.symbol ?: "৳"
    }

    fun getCurrency(): String = currentCurrency

    fun getSymbol(): String = currencySymbol

    fun getCurrencyConfig(): CurrencyConfig? = currencyConfigs[currentCurrency]

    fun format(amount: Double): String = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", amount)}"

    fun formatBDT(amount: Double): String = format(amount)

    fun formatCompact(amount: Double): String = when {
        amount >= 1_000_000 -> "$currencySymbol${String.format(Locale.US, "%.1fM", amount / 1_000_000)}"
        amount >= 1_000 -> "$currencySymbol${String.format(Locale.US, "%.1fK", amount / 1_000)}"
        else -> format(amount)
    }

    fun formatWithCode(amount: Double): String = "$currencySymbol${String.format(Locale.getDefault(), "%,.0f", amount)} ($currentCurrency)"
}

@Composable
fun rememberCurrencySymbol(): String = remember { CurrencyFormatter.getSymbol() }

@Composable
fun rememberCurrency(): String = remember { CurrencyFormatter.getCurrency() }