package com.llmusage.monitor.providers.impl

import com.llmusage.monitor.data.remote.HttpClient
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.providers.AuthMethod
import com.llmusage.monitor.providers.Provider
import com.llmusage.monitor.providers.ProviderResult
import com.llmusage.monitor.providers.api.GeminiApi

class GeminiProvider : Provider {

    override val type = ProviderType.GEMINI
    override val authMethod = AuthMethod.API_KEY

    private val api: GeminiApi =
        HttpClient.retrofit("https://generativelanguage.googleapis.com/").create(GeminiApi::class.java)

    override suspend fun fetchUsage(apiKey: String?, extrasJson: String?): ProviderResult<UsageData> {
        if (apiKey.isNullOrBlank()) return ProviderResult.Failure("Missing API key")
        return try {
            // Credential check only — Gemini has no programmatic per-key usage
            // endpoint as of authoring; usage lives in Google Cloud Billing.
            api.listModels(apiKey)
            ProviderResult.Unsupported(
                "Gemini has no per-key usage endpoint. Tap 'Log usage' to enter " +
                    "today's totals manually — your key is valid."
            )
        } catch (t: Throwable) {
            ProviderResult.Failure(t.message ?: "Gemini ping failed")
        }
    }
}
