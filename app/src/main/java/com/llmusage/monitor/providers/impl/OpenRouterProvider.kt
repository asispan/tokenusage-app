package com.llmusage.monitor.providers.impl

import com.llmusage.monitor.data.remote.HttpClient
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.providers.AuthMethod
import com.llmusage.monitor.providers.Provider
import com.llmusage.monitor.providers.ProviderResult
import com.llmusage.monitor.providers.api.OpenRouterApi

class OpenRouterProvider : Provider {

    override val type = ProviderType.OPENROUTER
    override val authMethod = AuthMethod.API_KEY

    private val api: OpenRouterApi =
        HttpClient.retrofit("https://openrouter.ai/").create(OpenRouterApi::class.java)

    override suspend fun fetchUsage(apiKey: String?, extrasJson: String?): ProviderResult<UsageData> {
        if (apiKey.isNullOrBlank()) return ProviderResult.Failure("Missing API key")

        return try {
            val resp = api.keyInfo("Bearer $apiKey").data
                ?: return ProviderResult.Failure("Empty response from OpenRouter")
            ProviderResult.Success(
                UsageData(
                    providerType = ProviderType.OPENROUTER,
                    inputTokens = 0,
                    outputTokens = 0,
                    totalTokens = 0,
                    estimatedCostUsd = resp.usage ?: 0.0,
                    limitUsd = resp.limit
                )
            )
        } catch (t: Throwable) {
            ProviderResult.Failure(t.message ?: "OpenRouter key info failed")
        }
    }
}
