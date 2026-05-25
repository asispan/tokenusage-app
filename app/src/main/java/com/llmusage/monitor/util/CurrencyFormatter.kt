package com.llmusage.monitor.util

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

object CurrencyFormatter {

    fun format(amountUsd: Double, currencyCode: String = "USD"): String {
        // We don't include FX conversion in v1 — display the raw USD value
        // formatted with the user's locale, but with the *USD* currency
        // symbol so we don't lie about the unit.
        val fmt = NumberFormat.getCurrencyInstance(Locale.getDefault()).apply {
            runCatching { currency = Currency.getInstance(currencyCode) }
            maximumFractionDigits = if (amountUsd < 10) 4 else 2
        }
        return fmt.format(amountUsd)
    }

    fun formatTokens(n: Long): String = when {
        n >= 1_000_000_000 -> String.format(Locale.US, "%.2fB", n / 1_000_000_000.0)
        n >= 1_000_000 -> String.format(Locale.US, "%.2fM", n / 1_000_000.0)
        n >= 1_000 -> String.format(Locale.US, "%.1fK", n / 1_000.0)
        else -> n.toString()
    }
}
