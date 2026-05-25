package com.llmusage.monitor.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One row per provider per UTC day. The pair (providerId, dateEpochDay) is
 * unique so a re-sync overwrites instead of appending.
 */
@Entity(
    tableName = "usage_snapshots",
    indices = [
        Index(value = ["providerId", "dateEpochDay"], unique = true),
        Index(value = ["dateEpochDay"])
    ]
)
data class UsageSnapshotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: String,
    val dateEpochDay: Long,
    val inputTokens: Long,
    val outputTokens: Long,
    val totalTokens: Long,
    val estimatedCostUsd: Double,
    val limitUsd: Double?,
    val limitTokens: Long?,
    val resetEpochMillis: Long?,
    val capturedAtEpochMillis: Long
)
