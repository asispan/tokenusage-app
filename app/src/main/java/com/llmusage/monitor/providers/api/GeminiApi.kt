package com.llmusage.monitor.providers.api

import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Google's Generative Language API. There is no public per-key usage
 * endpoint — usage lives in Google Cloud billing reports. We use the
 * /v1beta/models list as a credential-validity ping; the provider returns
 * Unsupported for actual usage and the user can log totals manually.
 */
interface GeminiApi {
    @GET("v1beta/models")
    suspend fun listModels(@Query("key") apiKey: String): GeminiModelsResponse
}

@Serializable
data class GeminiModelsResponse(
    val models: List<GeminiModelInfo> = emptyList()
)

@Serializable
data class GeminiModelInfo(val name: String? = null)
