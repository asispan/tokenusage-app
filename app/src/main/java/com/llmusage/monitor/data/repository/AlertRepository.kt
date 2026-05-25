package com.llmusage.monitor.data.repository

import com.llmusage.monitor.data.db.AlertDao
import com.llmusage.monitor.data.db.AlertEntity
import kotlinx.coroutines.flow.Flow

class AlertRepository(private val dao: AlertDao) {

    fun observeAll(): Flow<List<AlertEntity>> = dao.observeAll()

    suspend fun upsert(alert: AlertEntity) = dao.upsert(alert)
    suspend fun update(alert: AlertEntity) = dao.update(alert)
    suspend fun delete(id: Long) = dao.delete(id)
    suspend fun activeFor(providerId: String): List<AlertEntity> = dao.activeFor(providerId)
}
