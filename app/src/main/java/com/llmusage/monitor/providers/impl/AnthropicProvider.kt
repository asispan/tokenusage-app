package com.llmusage.monitor.providers.impl

import com.llmusage.monitor.data.remote.HttpClient
import com.llmusage.monitor.data.repository.PricingRepository
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.providers.AuthMethod
import com.llmusage.monitor.providers.Provider
import com.llmusage.monitor.providers.ProviderResult
import com.llmusage.monitor.providers.api.AnthropicApi
import com.llmusage.monitor.providers.api.PingRequest

/**
 * Anthropic provider. We attempt the `/v1/organizations/me/cost_report`
 * endpoint first; if that returns 401/403/404 (the common case for
 * non-admin keys) we fall back to a 1-token /v1/messages ping which
 * proves the key works but yields no aggregate usage. In that case the
 * provider returns Unsupported, the UI prompts for manual entry, and the
 * worker stores whatever the user types.
 */
class AnthropicProvider(private val pricing: PricingRepository) : Provider {

    override val type = ProviderType.ANTHROPIC
    override val authMethod = AuthMethod.API_KEY

    private val api: AnthropicApi =
        HttpClient.retrofit("https://api.anthropic.com/").create(AnthropicApi::class.java)

    override suspend fun fetchUsage(apiKey: String?, extrasJson: String?): ProviderResult<UsageData> {
        if (apiKey.isNullOrBlank()) return ProviderResult.Failure("Missing API key")

        val cost = runCatching { api.costReport(apiKey) }.getOrNull()
        if (cost != null && cost.data.isNotEmpty()) {
            var input = 0L
            var output = 0L
            var spend = 0.0
            cost.data.forEach { row ->
                input += row.inputTokens ?: 0L
                output += row.outputTokens ?: 0L
                spend += row.amountUsd ?: 0.0
            }
            return ProviderResult.Success(
                UsageData(
                    providerType = ProviderType.ANTHROPIC,
                    inputTokens = input,
                    outputTokens = output,
                    totalTokens = input + output,
                    estimatedCostUsd = spend
                )
            )
        }

        // No cost report — verify the key is at least valid with a ping.
        return try {
            api.ping(apiKey = apiKey, body = PingRequest())
            ProviderResult.Unsupported(
                "Anthropic doesn't expose programmatic usage for this key. " +
                    "Tap 'Log usage' to enter today's totals manually."
            )
        } catch (t: Throwable) {
            ProviderResult.Failure(t.message ?: "Anthropic ping failed")
        }
    }
}
