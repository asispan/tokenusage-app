package com.llmusage.monitor.data.repository

import com.llmusage.monitor.data.db.PricingDao
import com.llmusage.monitor.data.db.PricingEntity
import com.llmusage.monitor.domain.model.ProviderType

/**
 * Local pricing registry. Defaults bundled here are *indicative only* — they
 * were correct at the time of writing but providers change prices frequently,
 * so the UI labels every cost computed from these values as an "estimate".
 *
 * Users can override any row from Settings → Pricing.
 */
class PricingRepository(private val dao: PricingDao) {

    suspend fun ensureDefaultsSeeded() {
        val existing = dao.observeAll() // just to keep API obvious; we re-check per provider
        val seed = DefaultPricing.all
        // Only seed rows that don't exist yet so user overrides are preserved.
        val toInsert = mutableListOf<PricingEntity>()
        seed.forEach {
            if (dao.get(it.providerId, it.model) == null) toInsert += it
        }
        if (toInsert.isNotEmpty()) dao.upsertAll(toInsert)
    }

    suspend fun get(providerId: String, model: String): PricingEntity? = dao.get(providerId, model)

    suspend fun forProvider(providerId: String): List<PricingEntity> = dao.forProvider(providerId)

    suspend fun override(entry: PricingEntity) = dao.upsert(entry.copy(userOverride = true))

    suspend fun resetOverrides() {
        dao.deleteOverrides()
        ensureDefaultsSeeded()
    }

    /**
     * Estimate cost in USD given input/output token counts. Returns 0.0 if
     * the model is unknown — callers should treat any positive value as an
     * estimate and any zero as "unknown pricing".
     */
    suspend fun estimateCost(providerId: String, model: String, inputTokens: Long, outputTokens: Long): Double {
        val p = dao.get(providerId, model) ?: return 0.0
        return (inputTokens / 1_000_000.0) * p.inputUsdPer1M +
                (outputTokens / 1_000_000.0) * p.outputUsdPer1M
    }
}

/**
 * Indicative default pricing per 1M tokens, in USD. Source: public provider
 * pricing pages at time of authoring. Treat as estimates; users can override.
 */
private object DefaultPricing {
    val all: List<PricingEntity> = buildList {
        // OpenAI (subset — extend as needed)
        add(PricingEntity(ProviderType.OPENAI.id, "gpt-4o", 2.50, 10.00, false))
        add(PricingEntity(ProviderType.OPENAI.id, "gpt-4o-mini", 0.15, 0.60, false))
        add(PricingEntity(ProviderType.OPENAI.id, "gpt-4-turbo", 10.00, 30.00, false))
        add(PricingEntity(ProviderType.OPENAI.id, "gpt-3.5-turbo", 0.50, 1.50, false))
        add(PricingEntity(ProviderType.OPENAI.id, "o1", 15.00, 60.00, false))
        add(PricingEntity(ProviderType.OPENAI.id, "o1-mini", 3.00, 12.00, false))

        // Anthropic
        add(PricingEntity(ProviderType.ANTHROPIC.id, "claude-3-5-sonnet-latest", 3.00, 15.00, false))
        add(PricingEntity(ProviderType.ANTHROPIC.id, "claude-3-5-haiku-latest", 0.80, 4.00, false))
        add(PricingEntity(ProviderType.ANTHROPIC.id, "claude-3-opus-latest", 15.00, 75.00, false))
        add(PricingEntity(ProviderType.ANTHROPIC.id, "claude-3-haiku-20240307", 0.25, 1.25, false))

        // Google Gemini
        add(PricingEntity(ProviderType.GEMINI.id, "gemini-1.5-pro", 1.25, 5.00, false))
        add(PricingEntity(ProviderType.GEMINI.id, "gemini-1.5-flash", 0.075, 0.30, false))
        add(PricingEntity(ProviderType.GEMINI.id, "gemini-1.5-flash-8b", 0.0375, 0.15, false))

        // OpenRouter — pricing varies per upstream model; provide a generic placeholder
        // entry of 0 so we don't fabricate numbers. Real costs come from OpenRouter
        // /credits + /generation endpoints when available.
        add(PricingEntity(ProviderType.OPENROUTER.id, "unknown", 0.0, 0.0, false))
    }
}
