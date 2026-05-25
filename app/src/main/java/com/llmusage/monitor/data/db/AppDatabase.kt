package com.llmusage.monitor.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        UsageSnapshotEntity::class,
        ProviderConfigEntity::class,
        PricingEntity::class,
        AlertEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun usageSnapshotDao(): UsageSnapshotDao
    abstract fun providerConfigDao(): ProviderConfigDao
    abstract fun pricingDao(): PricingDao
    abstract fun alertDao(): AlertDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "llm_usage.db"
                )
                    .fallbackToDestructiveMigration() // v1 — no migrations yet
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
