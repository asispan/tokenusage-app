package com.llmusage.monitor.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.data.db.UsageSnapshotEntity
import com.llmusage.monitor.util.TimeUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class HistoryUiState(
    val dailyTokens: List<Pair<Long, Long>> = emptyList(),
    val dailyCostUsd: List<Pair<Long, Double>> = emptyList(),
    val perProviderTotals: Map<String, Long> = emptyMap()
)

class HistoryViewModel(container: AppContainer) : ViewModel() {

    val state: StateFlow<HistoryUiState> = container.usageRepository
        .observeDailyTotals(TimeUtils.todayEpochDay() - 29, TimeUtils.todayEpochDay())
        .map(::aggregate)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), HistoryUiState())

    private fun aggregate(rows: List<UsageSnapshotEntity>): HistoryUiState {
        val byDay = (TimeUtils.todayEpochDay() - 29..TimeUtils.todayEpochDay()).associateWith { 0L }.toMutableMap()
        val costByDay = (TimeUtils.todayEpochDay() - 29..TimeUtils.todayEpochDay()).associateWith { 0.0 }.toMutableMap()
        val byProvider = mutableMapOf<String, Long>()
        rows.forEach {
            byDay[it.dateEpochDay] = (byDay[it.dateEpochDay] ?: 0L) + it.totalTokens
            costByDay[it.dateEpochDay] = (costByDay[it.dateEpochDay] ?: 0.0) + it.estimatedCostUsd
            byProvider[it.providerId] = (byProvider[it.providerId] ?: 0L) + it.totalTokens
        }
        return HistoryUiState(
            dailyTokens = byDay.toSortedMap().map { (k, v) -> k to v },
            dailyCostUsd = costByDay.toSortedMap().map { (k, v) -> k to v },
            perProviderTotals = byProvider
        )
    }
}
