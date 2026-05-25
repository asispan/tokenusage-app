package com.llmusage.monitor.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One configured threshold notification. We keep [lastFiredEpochDay] so the
 * same threshold doesn't re-notify on every refresh — it can fire at most once
 * per day per provider.
 */
@Entity(tableName = "alerts")
data class AlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: String,
    /** 0..100 — e.g. 75 for "75 percent". */
    val thresholdPercent: Int,
    val enabled: Boolean,
    val lastFiredEpochDay: Long?
)
