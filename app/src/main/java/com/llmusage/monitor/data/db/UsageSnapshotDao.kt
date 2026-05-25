package com.llmusage.monitor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UsageSnapshotDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(snapshot: UsageSnapshotEntity): Long

    @Query("SELECT * FROM usage_snapshots WHERE dateEpochDay BETWEEN :fromDay AND :toDay ORDER BY dateEpochDay ASC")
    fun observeRange(fromDay: Long, toDay: Long): Flow<List<UsageSnapshotEntity>>

    @Query("SELECT * FROM usage_snapshots WHERE providerId = :providerId ORDER BY dateEpochDay ASC")
    fun observeForProvider(providerId: String): Flow<List<UsageSnapshotEntity>>

    @Query("SELECT * FROM usage_snapshots WHERE dateEpochDay = :day")
    suspend fun snapshotsForDay(day: Long): List<UsageSnapshotEntity>

    @Query("SELECT * FROM usage_snapshots WHERE providerId = :providerId AND dateEpochDay = :day LIMIT 1")
    suspend fun snapshotForProviderDay(providerId: String, day: Long): UsageSnapshotEntity?

    @Query("DELETE FROM usage_snapshots")
    suspend fun deleteAll()

    @Query("DELETE FROM usage_snapshots WHERE providerId = :providerId")
    suspend fun deleteForProvider(providerId: String)

    @Query("SELECT * FROM usage_snapshots ORDER BY dateEpochDay DESC, providerId ASC")
    suspend fun all(): List<UsageSnapshotEntity>
}
