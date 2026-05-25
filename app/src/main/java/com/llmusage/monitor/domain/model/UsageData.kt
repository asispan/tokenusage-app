package com.llmusage.monitor.domain.model

/**
 * Aggregated usage figures for a provider over a window.
 *
 * Any nullable field means "the provider does not expose this datum" — the UI
 * must render it as "n/a" rather than zero.
 */
data class UsageData(
    val providerType: ProviderType,
    val inputTokens: Long,
    val outputTokens: Long,
    val totalTokens: Long,
    val estimatedCostUsd: Double,
    /** Hard usage cap from the provider, if exposed. */
    val limitUsd: Double? = null,
    val limitTokens: Long? = null,
    /** Epoch millis at which the current quota window resets, if known. */
    val resetEpochMillis: Long? = null,
    val perModel: Map<String, ModelUsage> = emptyMap()
) {
    val totalSpend: Double get() = estimatedCostUsd
    val quotaPercent: Float?
        get() = limitUsd?.takeIf { it > 0 }?.let { (estimatedCostUsd / it).toFloat().coerceIn(0f, 1f) }
            ?: limitTokens?.takeIf { it > 0 }?.let { (totalTokens.toFloat() / it).coerceIn(0f, 1f) }
}

data class ModelUsage(
    val model: String,
    val inputTokens: Long,
    val outputTokens: Long,
    val costUsd: Double
)
