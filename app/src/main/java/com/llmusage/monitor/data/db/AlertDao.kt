package com.llmusage.monitor.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface AlertDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(alert: AlertEntity): Long

    @Update
    suspend fun update(alert: AlertEntity)

    @Query("SELECT * FROM alerts ORDER BY providerId ASC, thresholdPercent ASC")
    fun observeAll(): Flow<List<AlertEntity>>

    @Query("SELECT * FROM alerts WHERE providerId = :providerId AND enabled = 1")
    suspend fun activeFor(providerId: String): List<AlertEntity>

    @Query("DELETE FROM alerts WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM alerts")
    suspend fun deleteAll()
}
