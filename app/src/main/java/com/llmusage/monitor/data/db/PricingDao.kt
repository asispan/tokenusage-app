package com.llmusage.monitor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PricingDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<PricingEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entry: PricingEntity)

    @Query("SELECT * FROM pricing")
    fun observeAll(): Flow<List<PricingEntity>>

    @Query("SELECT * FROM pricing WHERE providerId = :providerId")
    suspend fun forProvider(providerId: String): List<PricingEntity>

    @Query("SELECT * FROM pricing WHERE providerId = :providerId AND model = :model LIMIT 1")
    suspend fun get(providerId: String, model: String): PricingEntity?

    @Query("DELETE FROM pricing WHERE userOverride = 1")
    suspend fun deleteOverrides()
}
