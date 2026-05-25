package com.llmusage.monitor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProviderConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(config: ProviderConfigEntity)

    @Query("SELECT * FROM provider_configs")
    fun observeAll(): Flow<List<ProviderConfigEntity>>

    @Query("SELECT * FROM provider_configs WHERE providerId = :providerId LIMIT 1")
    suspend fun get(providerId: String): ProviderConfigEntity?

    @Query("SELECT * FROM provider_configs WHERE providerId = :providerId LIMIT 1")
    fun observe(providerId: String): Flow<ProviderConfigEntity?>

    @Query("DELETE FROM provider_configs WHERE providerId = :providerId")
    suspend fun delete(providerId: String)

    @Query("UPDATE provider_configs SET lastSyncEpochMillis = :ts, lastSyncSuccess = :success, lastSyncError = :error WHERE providerId = :providerId")
    suspend fun updateSyncState(providerId: String, ts: Long, success: Boolean, error: String?)

    @Query("DELETE FROM provider_configs")
    suspend fun deleteAll()
}
