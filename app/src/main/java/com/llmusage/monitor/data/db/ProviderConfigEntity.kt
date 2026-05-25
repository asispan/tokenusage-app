package com.llmusage.monitor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Per-provider configuration. Note that [encryptedApiKey] holds the
 * Keystore-encrypted blob — the plaintext key is never written to disk.
 */
@Entity(tableName = "provider_configs")
data class ProviderConfigEntity(
    @PrimaryKey val providerId: String,
    val enabled: Boolean,
    val encryptedApiKey: String?,
    val apiKeyIv: String?,
    /** Provider-specific extras as JSON (e.g. OpenAI org-id). */
    val extrasJson: String?,
    val lastSyncEpochMillis: Long?,
    val lastSyncSuccess: Boolean?,
    val lastSyncError: String?
)
