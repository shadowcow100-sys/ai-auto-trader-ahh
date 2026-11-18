package com.autotrader.ahh.ui.screens.activity

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.autotrader.ahh.data.model.DebateSession
import com.autotrader.ahh.data.model.DecisionRecord
import com.autotrader.ahh.data.model.ParsedDecisionItem
import com.autotrader.ahh.data.model.Trade
import com.autotrader.ahh.data.model.TradeStats
import com.autotrader.ahh.data.repository.TraderRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DecisionUiItem(
    val record: DecisionRecord,
    val items: List<ParsedDecisionItem>
)

data class ActivityUiState(
    val isLoading: Boolean = false,
    val selectedTab: Int = 0, // 0 = Decisions, 1 = Trades, 2 = Debates
    val decisions: List<DecisionUiItem> = emptyList(),
    val trades: List<Trade> = emptyList(),
    val tradeStats: TradeStats = TradeStats(),
    val debates: List<DebateSession> = emptyList(),
    val errorMessage: String? = null
)

class ActivityViewModel(
    private val repository: TraderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityUiState())
    val uiState: StateFlow<ActivityUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun selectTab(index: Int) {
        _uiState.update { it.copy(selectedTab = index) }
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            val traderId = repository.selectedTraderId.first().ifBlank {
                repository.getTraders().getOrNull()?.firstOrNull()?.id.orEmpty()
            }

            coroutineScope {
                val decisionsDef = if (traderId.isNotBlank()) async { repository.getDecisions(traderId) } else null
                val tradesDef = if (traderId.isNotBlank()) async { repository.getTrades(traderId) } else null
                val debatesDef = async { repository.getDebateSessions() }

                val decisionsRes = decisionsDef?.await()
                val tradesRes = tradesDef?.await()
                val debatesRes = debatesDef.await()

                val parsedDecisions = decisionsRes?.getOrNull().orEmpty().map { rec ->
                    DecisionUiItem(record = rec, items = rec.parsedItems(repository.gson))
                }

                _uiState.update {
                    it.copy(
                        isLoading = false,
                        decisions = parsedDecisions,
                        trades = tradesRes?.getOrNull()?.trades ?: emptyList(),
                        tradeStats = tradesRes?.getOrNull()?.stats ?: TradeStats(),
                        debates = debatesRes.getOrNull() ?: emptyList(),
                        errorMessage = decisionsRes?.exceptionOrNull()?.message
                            ?: tradesRes?.exceptionOrNull()?.message
                            ?: debatesRes.exceptionOrNull()?.message
                    )
                }
            }
        }
    }

    fun startDebate(sessionId: String) {
        viewModelScope.launch {
            repository.startDebate(sessionId)
            refresh()
        }
    }

    fun stopDebate(sessionId: String) {
        viewModelScope.launch {
            repository.stopDebate(sessionId)
            refresh()
        }
    }

    class Factory(private val repository: TraderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ActivityViewModel(repository) as T
        }
    }
}
