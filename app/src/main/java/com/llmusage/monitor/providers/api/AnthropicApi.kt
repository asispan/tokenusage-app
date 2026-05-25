package com.llmusage.monitor.providers.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * Anthropic's public REST API. As of authoring (May 2026) Anthropic has rolled
 * out an admin /v1/organizations/{org_id}/usage_report/messages endpoint that
 * is admin-key-only and not available to every account. We therefore include
 * both a lightweight "ping" (1-token /v1/messages call) and the usage endpoint,
 * and the provider degrades to "Unsupported" when usage is unreachable.
 */
interface AnthropicApi {

    @POST("v1/messages")
    suspend fun ping(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01",
        @Body body: PingRequest
    ): PingResponse

    @GET("v1/organizations/me/cost_report")
    suspend fun costReport(
        @Header("x-api-key") apiKey: String,
        @Header("anthropic-version") version: String = "2023-06-01"
    ): CostReportResponse
}

@Serializable
data class PingRequest(
    val model: String = "claude-3-5-haiku-latest",
    @SerialName("max_tokens") val maxTokens: Int = 1,
    val messages: List<PingMessage> = listOf(PingMessage("user", "ping"))
)

@Serializable
data class PingMessage(val role: String, val content: String)

@Serializable
data class PingResponse(
    val id: String? = null,
    val usage: PingUsage? = null
)

@Serializable
data class PingUsage(
    @SerialName("input_tokens") val inputTokens: Long? = 0,
    @SerialName("output_tokens") val outputTokens: Long? = 0
)

@Serializable
data class CostReportResponse(
    val data: List<CostReportRow> = emptyList()
)

@Serializable
data class CostReportRow(
    val model: String? = null,
    @SerialName("input_tokens") val inputTokens: Long? = 0,
    @SerialName("output_tokens") val outputTokens: Long? = 0,
    @SerialName("amount_usd") val amountUsd: Double? = 0.0
)
