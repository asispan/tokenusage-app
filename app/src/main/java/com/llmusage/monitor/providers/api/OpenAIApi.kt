package com.llmusage.monitor.providers.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

/**
 * Subset of the OpenAI REST API we need for usage tracking.
 *
 * NOTE: OpenAI exposes a current-day usage endpoint at GET /v1/usage and
 * subscription / hard-limit info at GET /v1/dashboard/billing/subscription
 * plus GET /v1/dashboard/billing/usage. The dashboard/* endpoints
 * historically required a session cookie rather than an API key; treat
 * them as best-effort and fall back to /v1/usage if they fail.
 */
interface OpenAIApi {

    @GET("v1/usage")
    suspend fun getUsage(
        @Header("Authorization") authorization: String,
        @Header("OpenAI-Organization") organization: String? = null,
        @Query("date") date: String // YYYY-MM-DD
    ): OpenAIUsageResponse

    @GET("v1/dashboard/billing/subscription")
    suspend fun getSubscription(
        @Header("Authorization") authorization: String
    ): OpenAISubscription
}

@Serializable
data class OpenAIUsageResponse(
    val `object`: String? = null,
    val data: List<OpenAIUsageRecord> = emptyList()
)

@Serializable
data class OpenAIUsageRecord(
    @SerialName("aggregation_timestamp") val aggregationTimestamp: Long? = null,
    @SerialName("n_requests") val nRequests: Int? = null,
    val operation: String? = null,
    @SerialName("snapshot_id") val snapshotId: String? = null,
    @SerialName("n_context_tokens_total") val nContextTokens: Long? = null,
    @SerialName("n_generated_tokens_total") val nGeneratedTokens: Long? = null
)

@Serializable
data class OpenAISubscription(
    @SerialName("hard_limit_usd") val hardLimitUsd: Double? = null,
    @SerialName("soft_limit_usd") val softLimitUsd: Double? = null,
    @SerialName("access_until") val accessUntilEpochSeconds: Long? = null
)
