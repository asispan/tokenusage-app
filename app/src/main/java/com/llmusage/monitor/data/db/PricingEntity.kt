package com.llmusage.monitor.data.db

import androidx.room.Entity

@Entity(tableName = "pricing", primaryKeys = ["providerId", "model"])
data class PricingEntity(
    val providerId: String,
    val model: String,
    val inputUsdPer1M: Double,
    val outputUsdPer1M: Double,
    val userOverride: Boolean
)
