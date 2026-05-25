package com.llmusage.monitor.providers.impl

import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.providers.AuthMethod
import com.llmusage.monitor.providers.Provider
import com.llmusage.monitor.providers.ProviderResult

/**
 * "Manual" provider — for any tool the user wants to track without API
 * integration. fetchUsage is always Unsupported; the UI surfaces a form to
 * record token / cost numbers and writes them directly into the usage table.
 */
class ManualProvider : Provider {
    override val type = ProviderType.MANUAL
    override val authMethod = AuthMethod.NONE

    override fun isConfigured(apiKey: String?, extrasJson: String?): Boolean = true

    override suspend fun fetchUsage(apiKey: String?, extrasJson: String?): ProviderResult<UsageData> =
        ProviderResult.Unsupported(
            "Manual provider — use the 'Log usage' button to record what you used."
        )

    override suspend fun testConnection(apiKey: String?, extrasJson: String?) =
        ProviderResult.Success(Unit)
}
