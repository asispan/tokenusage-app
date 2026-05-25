package com.llmusage.monitor.providers.impl

import com.llmusage.monitor.data.remote.HttpClient
import com.llmusage.monitor.data.repository.PricingRepository
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.providers.AuthMethod
import com.llmusage.monitor.providers.Provider
import com.llmusage.monitor.providers.ProviderResult
import com.llmusage.monitor.providers.api.OpenAIApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.time.LocalDate
import java.time.ZoneOffset

class OpenAIProvider(private val pricing: PricingRepository) : Provider {

    override val type = ProviderType.OPENAI
    override val authMethod = AuthMethod.API_KEY

    private val api: OpenAIApi = HttpClient.retrofit("https://api.openai.com/").create(OpenAIApi::class.java)

    override suspend fun fetchUsage(apiKey: String?, extrasJson: String?): ProviderResult<UsageData> {
        if (apiKey.isNullOrBlank()) return ProviderResult.Failure("Missing API key")
        val org = parseOrg(extrasJson)
        val today = LocalDate.now(ZoneOffset.UTC).toString()

        // Step 1: pull today's per-request usage records.
        val records = try {
            api.getUsage(authorization = "Bearer $apiKey", organization = org, date = today).data
        } catch (t: Throwable) {
            return ProviderResult.Failure(t.message ?: "OpenAI usage fetch failed")
        }

        var inputTokens = 0L
        var outputTokens = 0L
        var estimatedCost = 0.0
        records.forEach { rec ->
            val input = rec.nContextTokens ?: 0L
            val output = rec.nGeneratedTokens ?: 0L
            inputTokens += input
            outputTokens += output
            val model = rec.snapshotId.orEmpty()
            estimatedCost += pricing.estimateCost(ProviderType.OPENAI.id, model, input, output)
        }

        // Step 2: best-effort hard-limit pull. Many keys can't hit this; if it
        // fails we still return usage without a quota cap.
        val (limitUsd, resetMs) = runCatching {
            val sub = api.getSubscription("Bearer $apiKey")
            sub.hardLimitUsd to sub.accessUntilEpochSeconds?.let { it * 1000 }
        }.getOrDefault(null to null)

        return ProviderResult.Success(
            UsageData(
                providerType = ProviderType.OPENAI,
                inputTokens = inputTokens,
                outputTokens = outputTokens,
                totalTokens = inputTokens + outputTokens,
                estimatedCostUsd = estimatedCost,
                limitUsd = limitUsd,
                limitTokens = null,
                resetEpochMillis = resetMs
            )
        )
    }

    private fun parseOrg(extrasJson: String?): String? {
        if (extrasJson.isNullOrBlank()) return null
        return runCatching {
            (Json.parseToJsonElement(extrasJson) as? JsonObject)
                ?.get("organization")?.jsonPrimitive?.content
        }.getOrNull()
    }
}
