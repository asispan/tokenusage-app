package com.llmusage.monitor.data.repository

import com.llmusage.monitor.data.db.ProviderConfigDao
import com.llmusage.monitor.data.db.ProviderConfigEntity
import com.llmusage.monitor.data.security.KeystoreEncryption
import com.llmusage.monitor.domain.model.ProviderType
import kotlinx.coroutines.flow.Flow

/**
 * One-stop repository for everything provider-config-related. The plaintext
 * API key never leaves this class except via [decryptedKey]; callers that
 * just need to display configuration should not touch it.
 */
class ProviderRepository(private val dao: ProviderConfigDao) {

    fun observeAll(): Flow<List<ProviderConfigEntity>> = dao.observeAll()
    fun observe(providerId: String): Flow<ProviderConfigEntity?> = dao.observe(providerId)
    suspend fun get(providerId: String): ProviderConfigEntity? = dao.get(providerId)

    suspend fun saveApiKey(providerId: String, apiKey: String, extrasJson: String? = null) {
        val encrypted = KeystoreEncryption.encrypt(apiKey)
        val existing = dao.get(providerId)
        dao.upsert(
            (existing ?: defaultConfig(providerId)).copy(
                enabled = true,
                encryptedApiKey = encrypted.cipherTextB64,
                apiKeyIv = encrypted.ivB64,
                extrasJson = extrasJson ?: existing?.extrasJson
            )
        )
    }

    suspend fun deleteApiKey(providerId: String) {
        val existing = dao.get(providerId) ?: return
        dao.upsert(existing.copy(encryptedApiKey = null, apiKeyIv = null, enabled = false))
    }

    suspend fun deleteProvider(providerId: String) = dao.delete(providerId)

    suspend fun setEnabled(providerId: String, enabled: Boolean) {
        val existing = dao.get(providerId) ?: defaultConfig(providerId)
        dao.upsert(existing.copy(enabled = enabled))
    }

    suspend fun decryptedKey(providerId: String): String? {
        val config = dao.get(providerId) ?: return null
        val ct = config.encryptedApiKey ?: return null
        val iv = config.apiKeyIv ?: return null
        return runCatching { KeystoreEncryption.decrypt(ct, iv) }.getOrNull()
    }

    suspend fun recordSync(providerId: String, success: Boolean, error: String? = null) {
        dao.updateSyncState(providerId, System.currentTimeMillis(), success, error)
    }

    suspend fun ensureSeeded() {
        val existing = dao.observeAll() // not subscribed; we just need an initial probe
        // Seed default rows so the Providers screen has all built-in types listed.
        ProviderType.values().forEach { type ->
            if (dao.get(type.id) == null) dao.upsert(defaultConfig(type.id))
        }
    }

    private fun defaultConfig(providerId: String) = ProviderConfigEntity(
        providerId = providerId,
        enabled = false,
        encryptedApiKey = null,
        apiKeyIv = null,
        extrasJson = null,
        lastSyncEpochMillis = null,
        lastSyncSuccess = null,
        lastSyncError = null
    )
}
