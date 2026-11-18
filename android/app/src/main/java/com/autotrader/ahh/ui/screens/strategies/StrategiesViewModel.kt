package com.autotrader.ahh.ui.screens.strategies

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autotrader.ahh.data.model.Strategy
import com.autotrader.ahh.data.repository.TraderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StrategiesUiState(
    val isLoading: Boolean = true,
    val strategies: List<Strategy> = emptyList(),
    val isRecommending: Boolean = false,
    val turboFind: Boolean = false,
    val recommendedPairs: List<String> = emptyList(),
    val errorMessage: String? = null
)

class StrategiesViewModel(
    private val repository: TraderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StrategiesUiState())
    val uiState: StateFlow<StrategiesUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.getStrategies()
                .onSuccess { list ->
                    _uiState.update { it.copy(isLoading = false, strategies = list) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = err.message ?: "Failed to load strategies")
                    }
                }
        }
    }

    fun activateStrategy(id: String) {
        viewModelScope.launch {
            repository.activateStrategy(id)
                .onSuccess { refresh() }
                .onFailure { err ->
                    _uiState.update { it.copy(errorMessage = err.message) }
                }
        }
    }

    fun toggleTurboFind(enabled: Boolean) {
        _uiState.update { it.copy(turboFind = enabled) }
    }

    fun runSmartFind() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRecommending = true, errorMessage = null) }
            repository.recommendPairs(count = 7, turbo = _uiState.value.turboFind)
                .onSuccess { pairs ->
                    _uiState.update { it.copy(isRecommending = false, recommendedPairs = pairs) }
                }
                .onFailure { err ->
                    _uiState.update {
                        it.copy(isRecommending = false, errorMessage = err.message ?: "Smart Find failed")
                    }
                }
        }
    }

    class Factory(private val repository: TraderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StrategiesViewModel(repository) as T
        }
    }
}
