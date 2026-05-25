package com.llmusage.monitor.ui.providers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.providers.ProviderResult
import com.llmusage.monitor.util.TimeUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProviderDetailUiState(
    val providerId: String,
    val type: ProviderType,
    val hasKey: Boolean = false,
    val testing: Boolean = false,
    val testResult: String? = null,
    val testIsError: Boolean = false,
    val unsupportedHint: String? = null
)

class ProviderDetailViewModel(
    private val container: AppContainer,
    val providerId: String
) : ViewModel() {

    private val _state = MutableStateFlow(
        ProviderDetailUiState(providerId = providerId, type = ProviderType.fromId(providerId))
    )
    val state: StateFlow<ProviderDetailUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val cfg = container.providerRepository.get(providerId)
            _state.update { it.copy(hasKey = cfg?.encryptedApiKey != null) }
        }
    }

    fun saveKey(apiKey: String, extrasJson: String? = null) {
        viewModelScope.launch {
            container.providerRepository.saveApiKey(providerId, apiKey, extrasJson)
            _state.update { it.copy(hasKey = true, testResult = "Saved.", testIsError = false) }
        }
    }

    fun deleteKey() {
        viewModelScope.launch {
            container.providerRepository.deleteApiKey(providerId)
            _state.update { it.copy(hasKey = false, testResult = "Key removed.", testIsError = false) }
        }
    }

    fun testConnection() {
        viewModelScope.launch {
            _state.update { it.copy(testing = true, testResult = null, unsupportedHint = null) }
            val key = container.providerRepository.decryptedKey(providerId)
            val cfg = container.providerRepository.get(providerId)
            val r = container.providers.byId(providerId).testConnection(key, cfg?.extrasJson)
            val (msg, err, unsupportedHint) = when (r) {
                is ProviderResult.Success -> Triple("Connected.", false, null)
                is ProviderResult.Failure -> Triple(r.message, true, null)
                is ProviderResult.Unsupported -> Triple("Key valid, usage not exposed.", false, r.message)
            }
            _state.update {
                it.copy(testing = false, testResult = msg, testIsError = err, unsupportedHint = unsupportedHint)
            }
        }
    }

    fun logManual(inputTokens: Long, outputTokens: Long, estimatedCostUsd: Double) {
        viewModelScope.launch {
            container.usageRepository.upsertSnapshot(
                providerId = providerId,
                day = TimeUtils.todayEpochDay(),
                data = UsageData(
                    providerType = ProviderType.fromId(providerId),
                    inputTokens = inputTokens,
                    outputTokens = outputTokens,
                    totalTokens = inputTokens + outputTokens,
                    estimatedCostUsd = estimatedCostUsd
                )
            )
            _state.update { it.copy(testResult = "Logged.", testIsError = false) }
        }
    }

    fun deleteProviderData() {
        viewModelScope.launch {
            container.usageRepository.deleteForProvider(providerId)
            _state.update { it.copy(testResult = "Cleared.", testIsError = false) }
        }
    }
}
