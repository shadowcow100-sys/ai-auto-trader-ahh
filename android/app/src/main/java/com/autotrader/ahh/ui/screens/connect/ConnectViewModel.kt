package com.autotrader.ahh.ui.screens.connect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autotrader.ahh.data.model.ConnectionTestResult
import com.autotrader.ahh.data.model.PairingPayload
import com.autotrader.ahh.data.model.ServerProfile
import com.autotrader.ahh.data.repository.TraderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ConnectUiState(
    val profileName: String = "My PC",
    val serverUrl: String = "",
    val passkey: String = "",
    val isTesting: Boolean = false,
    val isConnecting: Boolean = false,
    val testResult: ConnectionTestResult? = null,
    val errorMessage: String? = null
)

class ConnectViewModel(
    private val repository: TraderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConnectUiState())
    val uiState: StateFlow<ConnectUiState> = _uiState.asStateFlow()

    val savedProfiles: StateFlow<List<ServerProfile>> = repository.savedProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeUrl: StateFlow<String> = repository.activeBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        viewModelScope.launch {
            repository.activeBaseUrl.collect { url ->
                if (url.isNotBlank() && _uiState.value.serverUrl.isBlank()) {
                    _uiState.update { it.copy(serverUrl = url) }
                }
            }
        }
        viewModelScope.launch {
            repository.activePasskey.collect { key ->
                if (key.isNotBlank() && _uiState.value.passkey.isBlank()) {
                    _uiState.update { it.copy(passkey = key) }
                }
            }
        }
    }

    fun onNameChanged(value: String) {
        _uiState.update { it.copy(profileName = value, errorMessage = null) }
    }

    fun onUrlChanged(value: String) {
        _uiState.update { it.copy(serverUrl = value, testResult = null, errorMessage = null) }
    }

    fun onPasskeyChanged(value: String) {
        _uiState.update { it.copy(passkey = value, testResult = null, errorMessage = null) }
    }

    fun applyPreset(name: String, url: String) {
        _uiState.update {
            it.copy(
                profileName = name,
                serverUrl = url,
                testResult = null,
                errorMessage = null
            )
        }
    }

    fun applyProfile(profile: ServerProfile) {
        _uiState.update {
            it.copy(
                profileName = profile.name,
                serverUrl = profile.baseUrl,
                passkey = profile.passkey,
                testResult = null,
                errorMessage = null
            )
        }
    }

    fun applyPairingPayload(payload: PairingPayload) {
        _uiState.update {
            it.copy(
                profileName = payload.name,
                serverUrl = payload.url,
                passkey = payload.passkey,
                testResult = null,
                errorMessage = null
            )
        }
    }

    fun testConnection() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isTesting = true, errorMessage = null, testResult = null) }
            val result = repository.testConnection(state.serverUrl, state.passkey)
            _uiState.update {
                it.copy(
                    isTesting = false,
                    testResult = result,
                    errorMessage = if (!result.reachable || !result.authValid) result.errorMessage else null
                )
            }
        }
    }

    fun connectAndSave(onConnected: () -> Unit) {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isConnecting = true, errorMessage = null) }
            val res = repository.connectAndSave(state.profileName, state.serverUrl, state.passkey)
            res.onSuccess { testRes ->
                _uiState.update { it.copy(isConnecting = false, testResult = testRes) }
                onConnected()
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isConnecting = false,
                        errorMessage = err.message ?: "Connection failed"
                    )
                }
            }
        }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch {
            repository.deleteSavedProfile(profileId)
        }
    }

    class Factory(private val repository: TraderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ConnectViewModel(repository) as T
        }
    }
}
