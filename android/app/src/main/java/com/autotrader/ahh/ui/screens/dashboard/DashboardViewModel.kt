package com.autotrader.ahh.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autotrader.ahh.data.model.AccountInfo
import com.autotrader.ahh.data.model.EquitySnapshot
import com.autotrader.ahh.data.model.PauseStatusResponse
import com.autotrader.ahh.data.model.Position
import com.autotrader.ahh.data.model.Trader
import com.autotrader.ahh.data.model.TraderStatusResponse
import com.autotrader.ahh.data.repository.TraderRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    val isActionPending: Boolean = false,
    val traders: List<Trader> = emptyList(),
    val selectedTraderId: String? = null,
    val status: TraderStatusResponse? = null,
    val account: AccountInfo? = null,
    val positions: List<Position> = emptyList(),
    val pauseStatus: PauseStatusResponse? = null,
    val equityHistory: List<EquitySnapshot> = emptyList(),
    val errorMessage: String? = null
)

class DashboardViewModel(
    private val repository: TraderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    val activeProfileName: StateFlow<String> = repository.activeProfileName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "PC Server")

    val activeBaseUrl: StateFlow<String> = repository.activeBaseUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    init {
        refreshAll(initial = true)
        startPollingLoop()
    }

    private fun startPollingLoop() {
        viewModelScope.launch {
            while (isActive) {
                delay(3000)
                val traderId = _uiState.value.selectedTraderId
                if (!traderId.isNullOrBlank()) {
                    loadTraderDetails(traderId)
                }
            }
        }
    }

    fun refreshAll(initial: Boolean = false) {
        viewModelScope.launch {
            if (initial) {
                _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            }
            val tradersResult = repository.getTraders()
            tradersResult.onSuccess { list ->
                val savedId = repository.selectedTraderId.first()
                val currentId = _uiState.value.selectedTraderId
                val chosen = when {
                    !currentId.isNullOrBlank() && list.any { it.id == currentId } -> currentId
                    savedId.isNotBlank() && list.any { it.id == savedId } -> savedId
                    list.isNotEmpty() -> list.first().id
                    else -> null
                }
                if (chosen != null) {
                    repository.selectTrader(chosen)
                }
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        traders = list,
                        selectedTraderId = chosen,
                        errorMessage = null
                    )
                }
                if (chosen != null) {
                    loadTraderDetails(chosen)
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = err.message ?: "Failed to load traders from PC"
                    )
                }
            }
        }
    }

    fun selectTrader(traderId: String) {
        viewModelScope.launch {
            repository.selectTrader(traderId)
            _uiState.update { it.copy(selectedTraderId = traderId, errorMessage = null) }
            loadTraderDetails(traderId)
        }
    }

    private suspend fun loadTraderDetails(traderId: String) = coroutineScope {
        val statusDeferred = async { repository.getTraderStatus(traderId) }
        val accountDeferred = async { repository.getAccount(traderId) }
        val positionsDeferred = async { repository.getPositions(traderId) }
        val pauseDeferred = async { repository.getPauseStatus(traderId) }
        val equityDeferred = async { repository.getEquityHistory(traderId) }

        val statusRes = statusDeferred.await()
        val accountRes = accountDeferred.await()
        val positionsRes = positionsDeferred.await()
        val pauseRes = pauseDeferred.await()
        val equityRes = equityDeferred.await()

        _uiState.update { current ->
            if (current.selectedTraderId != traderId) return@update current
            current.copy(
                status = statusRes.getOrNull() ?: current.status,
                account = accountRes.getOrNull() ?: current.account,
                positions = positionsRes.getOrNull() ?: current.positions,
                pauseStatus = pauseRes.getOrNull() ?: current.pauseStatus,
                equityHistory = equityRes.getOrNull() ?: current.equityHistory,
                errorMessage = statusRes.exceptionOrNull()?.message
            )
        }
    }

    fun startSelectedTrader() {
        val id = _uiState.value.selectedTraderId ?: return
        performTraderAction { repository.startTrader(id) }
    }

    fun stopSelectedTrader() {
        val id = _uiState.value.selectedTraderId ?: return
        performTraderAction { repository.stopTrader(id) }
    }

    fun resumeSelectedTrader() {
        val id = _uiState.value.selectedTraderId ?: return
        performTraderAction { repository.resumeTrader(id) }
    }

    private fun performTraderAction(action: suspend () -> Result<Unit>) {
        viewModelScope.launch {
            _uiState.update { it.copy(isActionPending = true, errorMessage = null) }
            val result = action()
            _uiState.update {
                it.copy(
                    isActionPending = false,
                    errorMessage = result.exceptionOrNull()?.message
                )
            }
            refreshAll()
        }
    }

    class Factory(private val repository: TraderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(repository) as T
        }
    }
}
