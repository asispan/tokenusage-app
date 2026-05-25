package com.llmusage.monitor.data.repository

import com.llmusage.monitor.data.db.UsageSnapshotDao
import com.llmusage.monitor.data.db.UsageSnapshotEntity
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.util.TimeUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class UsageRepository(private val dao: UsageSnapshotDao) {

    fun observeDailyTotals(fromDay: Long, toDay: Long): Flow<List<UsageSnapshotEntity>> =
        dao.observeRange(fromDay, toDay)

    fun observeForProvider(providerId: String): Flow<List<UsageSnapshotEntity>> =
        dao.observeForProvider(providerId)

    /**
     * Aggregates today, this-week, and this-month per-provider into a single
     * structure usable by the dashboard. "Week" is rolling 7 days, "Month" is
     * rolling 30 days — calendar boundaries differ across locales, so we
     * deliberately use windows the user can reason about.
     */
    fun observeDashboard(): Flow<DashboardAggregate> {
        val today = TimeUtils.todayEpochDay()
        val from = today - 29
        return dao.observeRange(from, today).map { rows ->
            val byDay = rows.groupBy { it.dateEpochDay }
            val todayRows = byDay[today].orEmpty()
            val weekRows = rows.filter { it.dateEpochDay >= today - 6 }
            val monthRows = rows

            DashboardAggregate(
                today = sum(todayRows),
                week = sum(weekRows),
                month = sum(monthRows),
                perProviderToday = todayRows.associate { it.providerId to toUsageData(it) },
                perProviderMonth = monthRows.groupBy { it.providerId }.mapValues { (_, list) ->
                    toUsageData(sumEntity(list))
                }
            )
        }
    }

    suspend fun upsertSnapshot(providerId: String, day: Long, data: UsageData) {
        dao.upsert(
            UsageSnapshotEntity(
                providerId = providerId,
                dateEpochDay = day,
                inputTokens = data.inputTokens,
                outputTokens = data.outputTokens,
                totalTokens = data.totalTokens,
                estimatedCostUsd = data.estimatedCostUsd,
                limitUsd = data.limitUsd,
                limitTokens = data.limitTokens,
                resetEpochMillis = data.resetEpochMillis,
                capturedAtEpochMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteAll() = dao.deleteAll()
    suspend fun deleteForProvider(providerId: String) = dao.deleteForProvider(providerId)
    suspend fun exportAll(): List<UsageSnapshotEntity> = dao.all()

    private fun sum(rows: List<UsageSnapshotEntity>): WindowTotals {
        var input = 0L; var output = 0L; var total = 0L; var spend = 0.0
        rows.forEach {
            input += it.inputTokens; output += it.outputTokens
            total += it.totalTokens; spend += it.estimatedCostUsd
        }
        return WindowTotals(input, output, total, spend)
    }

    private fun sumEntity(rows: List<UsageSnapshotEntity>): UsageSnapshotEntity {
        val first = rows.first()
        val t = sum(rows)
        return first.copy(
            inputTokens = t.input,
            outputTokens = t.output,
            totalTokens = t.total,
            estimatedCostUsd = t.spendUsd
        )
    }

    private fun toUsageData(e: UsageSnapshotEntity) = UsageData(
        providerType = ProviderType.fromId(e.providerId),
        inputTokens = e.inputTokens,
        outputTokens = e.outputTokens,
        totalTokens = e.totalTokens,
        estimatedCostUsd = e.estimatedCostUsd,
        limitUsd = e.limitUsd,
        limitTokens = e.limitTokens,
        resetEpochMillis = e.resetEpochMillis
    )
}

data class WindowTotals(
    val input: Long,
    val output: Long,
    val total: Long,
    val spendUsd: Double
) {
    companion object { val Empty = WindowTotals(0, 0, 0, 0.0) }
}

data class DashboardAggregate(
    val today: WindowTotals,
    val week: WindowTotals,
    val month: WindowTotals,
    val perProviderToday: Map<String, UsageData>,
    val perProviderMonth: Map<String, UsageData>
)
