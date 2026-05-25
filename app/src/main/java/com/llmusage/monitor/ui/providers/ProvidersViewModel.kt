package com.llmusage.monitor.ui.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.data.db.ProviderConfigEntity
import com.llmusage.monitor.domain.model.ProviderType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProvidersUiState(
    val items: List<ProviderRow> = emptyList()
)

data class ProviderRow(
    val type: ProviderType,
    val configured: Boolean,
    val enabled: Boolean,
    val lastSyncMillis: Long?,
    val lastError: String?
)

class ProvidersViewModel(private val container: AppContainer) : ViewModel() {

    val state: StateFlow<ProvidersUiState> = container.providerRepository.observeAll()
        .map { rows ->
            val byId = rows.associateBy { it.providerId }
            val items = ProviderType.values().map { type ->
                val cfg: ProviderConfigEntity? = byId[type.id]
                ProviderRow(
                    type = type,
                    configured = type == ProviderType.MANUAL || cfg?.encryptedApiKey != null,
                    enabled = cfg?.enabled == true,
                    lastSyncMillis = cfg?.lastSyncEpochMillis,
                    lastError = cfg?.lastSyncError.takeIf { cfg?.lastSyncSuccess == false }
                )
            }
            ProvidersUiState(items)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProvidersUiState())

    fun setEnabled(providerId: String, enabled: Boolean) {
        viewModelScope.launch { container.providerRepository.setEnabled(providerId, enabled) }
    }
}
