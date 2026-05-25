package com.llmusage.monitor.domain.model

/**
 * Pricing per 1M tokens, in USD. Values here are *defaults* that ship with
 * the app — the user can override any entry via Settings. Always treat values
 * computed from these defaults as estimates.
 */
data class PricingEntry(
    val providerId: String,
    val model: String,
    val inputUsdPer1M: Double,
    val outputUsdPer1M: Double,
    val userOverride: Boolean = false
)
