package com.llmusage.monitor.providers

import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData

/**
 * The provider contract. Add a new LLM provider by implementing this and
 * registering it in [ProviderRegistry].
 *
 * Implementations MUST NOT throw — return [ProviderResult.Failure] for any
 * error path. The app treats a thrown exception from a provider as a bug.
 */
interface Provider {

    val type: ProviderType
    val authMethod: AuthMethod

    /** True if the user has supplied enough configuration to attempt a fetch. */
    fun isConfigured(apiKey: String?, extrasJson: String?): Boolean = !apiKey.isNullOrBlank()

    /**
     * Pull current usage for the active billing window. If the provider does
     * not publish a usage API, return a [ProviderResult.Unsupported] result
     * with a human-readable hint — the UI surfaces that hint and switches to
     * manual entry mode.
     */
    suspend fun fetchUsage(apiKey: String?, extrasJson: String?): ProviderResult<UsageData>

    /**
     * Lightweight credential check. Default implementation calls [fetchUsage]
     * and inspects the result, but providers with a cheaper "ping" endpoint
     * should override.
     */
    suspend fun testConnection(apiKey: String?, extrasJson: String?): ProviderResult<Unit> {
        val r = fetchUsage(apiKey, extrasJson)
        return when (r) {
            is ProviderResult.Success -> ProviderResult.Success(Unit)
            is ProviderResult.Failure -> ProviderResult.Failure(r.message)
            is ProviderResult.Unsupported -> ProviderResult.Unsupported(r.message)
        }
    }
}

enum class AuthMethod { API_KEY, NONE }
