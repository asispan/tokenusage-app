package com.llmusage.monitor.providers.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Header

/**
 * OpenRouter has a long-standing GET /api/v1/auth/key endpoint that returns
 * current usage and limit info for the calling key — no admin scope needed.
 * This makes it the most informative provider on the consumer side.
 */
interface OpenRouterApi {
    @GET("api/v1/auth/key")
    suspend fun keyInfo(@Header("Authorization") authorization: String): OpenRouterKeyResponse
}

@Serializable
data class OpenRouterKeyResponse(
    val data: OpenRouterKeyData? = null
)

@Serializable
data class OpenRouterKeyData(
    val label: String? = null,
    val usage: Double? = null,
    @SerialName("limit") val limit: Double? = null,
    @SerialName("limit_remaining") val limitRemaining: Double? = null,
    @SerialName("is_free_tier") val isFreeTier: Boolean? = null,
    @SerialName("rate_limit") val rateLimit: RateLimit? = null
)

@Serializable
data class RateLimit(
    val requests: Int? = null,
    val interval: String? = null
)
