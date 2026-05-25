package com.llmusage.monitor.ui.alerts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.data.db.AlertEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AlertsUiState(
    val alerts: List<AlertEntity> = emptyList(),
    val providerIds: List<String> = emptyList()
)

class AlertsViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<AlertsUiState> = combine(
        container.alertRepository.observeAll(),
        container.providerRepository.observeAll()
    ) { alerts, providers ->
        AlertsUiState(alerts = alerts, providerIds = providers.map { it.providerId })
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AlertsUiState())

    fun addAlert(providerId: String, percent: Int) {
        viewModelScope.launch {
            container.alertRepository.upsert(
                AlertEntity(providerId = providerId, thresholdPercent = percent, enabled = true, lastFiredEpochDay = null)
            )
        }
    }

    fun toggle(alert: AlertEntity, enabled: Boolean) {
        viewModelScope.launch { container.alertRepository.update(alert.copy(enabled = enabled)) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { container.alertRepository.delete(id) }
    }
}
