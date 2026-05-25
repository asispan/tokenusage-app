package com.llmusage.monitor.providers

import com.llmusage.monitor.data.repository.PricingRepository
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.providers.impl.AnthropicProvider
import com.llmusage.monitor.providers.impl.GeminiProvider
import com.llmusage.monitor.providers.impl.ManualProvider
import com.llmusage.monitor.providers.impl.OpenAIProvider
import com.llmusage.monitor.providers.impl.OpenRouterProvider

/**
 * Centralised lookup table for all built-in providers. Add new providers by
 * appending here — the rest of the app discovers them via [byType] / [all].
 */
class ProviderRegistry(pricing: PricingRepository) {

    private val providers: Map<ProviderType, Provider> = listOf(
        OpenAIProvider(pricing),
        AnthropicProvider(pricing),
        OpenRouterProvider(),
        GeminiProvider(),
        ManualProvider()
    ).associateBy { it.type }

    fun byType(type: ProviderType): Provider =
        providers[type] ?: error("No provider registered for $type")

    fun byId(id: String): Provider = byType(ProviderType.fromId(id))

    fun all(): Collection<Provider> = providers.values
}
