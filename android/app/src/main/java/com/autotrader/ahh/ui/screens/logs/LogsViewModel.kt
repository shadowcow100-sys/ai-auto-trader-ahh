package com.autotrader.ahh.ui.screens.logs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autotrader.ahh.data.model.GlobalSettings
import com.autotrader.ahh.data.model.LogEntry
import com.autotrader.ahh.data.repository.TraderRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class LogsUiState(
    val selectedTab: Int = 0, // 0 = Live Logs, 1 = PC Settings
    val logs: List<LogEntry> = emptyList(),
    val isStreamConnected: Boolean = false,
    val streamError: String? = null,
    val filterQuery: String = "",
    val settings: GlobalSettings = GlobalSettings(),
    val openRouterConfigured: Boolean = false,
    val binanceConfigured: Boolean = false,
    val isSavingSettings: Boolean = false,
    val settingsMessage: String? = null
)

class LogsViewModel(
    private val repository: TraderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogsUiState())
    val uiState: StateFlow<LogsUiState> = _uiState.asStateFlow()

    val activeProfileName: StateFlow<String> = repository.activeProfileName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "PC Server")

    val activeBaseUrl: StateFlow<String> = repository.activeBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    private var streamJob: Job? = null

    init {
        startLogStream()
        loadSettings()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
        if (index == 1) {
            loadSettings()
        }
    }

    fun onFilterChanged(query: String) {
        _uiState.update { it.copy(filterQuery = query) }
    }

    fun clearLogs() {
        _uiState.update { it.copy(logs = emptyList()) }
    }

    fun startLogStream() {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            while (isActive) {
                _uiState.update { it.copy(isStreamConnected = true, streamError = null) }
                repository.streamLogs()
                    .catch { err ->
                        _uiState.update {
                            it.copy(
                                isStreamConnected = false,
                                streamError = err.message ?: "Log stream disconnected"
                            )
                        }
                    }
                    .collect { entry ->
                        _uiState.update { current ->
                            val updated = (current.logs + entry).takeLast(400)
                            current.copy(
                                logs = updated,
                                isStreamConnected = true,
                                streamError = null
                            )
                        }
                    }
                _uiState.update { it.copy(isStreamConnected = false) }
                delay(4000)
            }
        }
    }

    fun loadSettings() {
        viewModelScope.launch {
            repository.getSettings()
                .onSuccess { res ->
                    _uiState.update {
                        it.copy(
                            settings = res.settings,
                            openRouterConfigured = res.configured.openrouter,
                            binanceConfigured = res.configured.binance,
                            settingsMessage = null
                        )
                    }
                }
                .onFailure { err ->
                    _uiState.update { it.copy(settingsMessage = err.message) }
                }
        }
    }

    fun updateModelField(model: String) {
        _uiState.update { it.copy(settings = it.settings.copy(openRouterModel = model)) }
    }

    fun updateTestnet(testnet: Boolean) {
        _uiState.update { it.copy(settings = it.settings.copy(binanceTestnet = testnet)) }
    }

    fun saveSettings() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSavingSettings = true, settingsMessage = null) }
            repository.updateSettings(_uiState.value.settings)
                .onSuccess {
                    _uiState.update {
                        it.copy(
                            isSavingSettings = false,
                            settingsMessage = "Saved to PC server."
                        )
                    }
                    loadSettings()
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isSavingSettings = false,
                            settingsMessage = err.message ?: "Failed to save"
                        )
                    }
                }
        }
    }

    fun disconnectPc(onDisconnected: () -> Unit) {
        viewModelScope.launch {
            streamJob?.cancel()
            repository.disconnect()
            onDisconnected()
        }
    }

    override fun onCleared() {
        streamJob?.cancel()
        super.onCleared()
    }

    class Factory(private val repository: TraderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return LogsViewModel(repository) as T
        }
    }
}
