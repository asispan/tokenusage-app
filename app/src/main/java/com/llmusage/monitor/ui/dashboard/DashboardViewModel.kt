package com.llmusage.monitor.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.data.db.ProviderConfigEntity
import com.llmusage.monitor.data.repository.DashboardAggregate
import com.llmusage.monitor.data.repository.WindowTotals
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isRefreshing: Boolean = false,
    val aggregate: DashboardAggregate? = null,
    val providers: List<ProviderConfigEntity> = emptyList(),
    val message: String? = null
)

class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<DashboardUiState> =
        combine(
            container.usageRepository.observeDashboard(),
            container.providerRepository.observeAll()
        ) { agg, providers ->
            DashboardUiState(aggregate = agg, providers = providers)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun refresh() {
        viewModelScope.launch {
            container.syncManager.syncAllOnce()
        }
    }
}
