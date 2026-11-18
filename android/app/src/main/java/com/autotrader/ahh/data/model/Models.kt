package com.autotrader.ahh.data.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken

// ============ PC-PHONE CONNECTION MODELS ============

data class ServerProfile(
    val id: String,
    val name: String,
    val baseUrl: String,
    val passkey: String = "",
    val lastConnectedAt: Long = 0L
)

data class ConnectionTestResult(
    val reachable: Boolean,
    val serverTime: String? = null,
    val authRequired: Boolean = false,
    val authValid: Boolean = true,
    val latencyMs: Long = 0L,
    val errorMessage: String? = null
)

data class PairingPayload(
    val url: String,
    val passkey: String = "",
    val name: String = "My PC"
)

// ============ HEALTH & AUTH ============

data class HealthResponse(
    val status: String = "",
    val time: String = ""
)

data class AuthVerifyRequest(
    val passkey: String
)

data class AuthVerifyResponse(
    val valid: Boolean = false,
    val message: String = "",
    val required: Boolean = false
)

data class StatusResponse(
    val status: String = ""
)

// ============ TRADER MODELS ============

data class TradersResponse(
    val traders: List<Trader> = emptyList()
)

data class Trader(
    val id: String = "",
    val name: String = "",
    @SerializedName("strategy_id") val strategyId: String = "",
    val exchange: String = "binance",
    val status: String = "stopped",
    @SerializedName("initial_balance") val initialBalance: Double = 0.0,
    val config: TraderConfig = TraderConfig(),
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("is_running") val isRunning: Boolean? = null
) {
    val active: Boolean
        get() = isRunning ?: (status == "running")
}

data class TraderConfig(
    @SerializedName("ai_provider") val aiProvider: String = "openrouter",
    @SerializedName("ai_model") val aiModel: String = "",
    @SerializedName("openrouter_api_key") val openRouterApiKey: String = "",
    @SerializedName("openrouter_model") val openRouterModel: String = "",
    @SerializedName("enable_reasoning") val enableReasoning: Boolean = false,
    @SerializedName("reasoning_model") val reasoningModel: String = "",
    @SerializedName("api_key") val apiKey: String = "",
    @SerializedName("secret_key") val secretKey: String = "",
    val testnet: Boolean = true
)

data class TraderStatusResponse(
    @SerializedName("trader_id") val traderId: String = "",
    @SerializedName("trader_name") val traderName: String = "",
    val running: Boolean = false,
    val strategy: String = "Default",
    val pairs: List<String> = emptyList(),
    val message: String? = null
)

data class PauseStatusResponse(
    @SerializedName("is_paused") val isPaused: Boolean = false,
    @SerializedName("pause_until") val pauseUntil: String? = null,
    @SerializedName("remaining_seconds") val remainingSeconds: Long? = null,
    val error: String? = null
)

// ============ ACCOUNT, POSITIONS & EQUITY ============

data class AccountInfo(
    @SerializedName("total_equity") val totalEquity: Double = 0.0,
    @SerializedName("wallet_balance") val walletBalance: Double = 0.0,
    val available: Double = 0.0,
    @SerializedName("unrealized_pnl") val unrealizedPnl: Double = 0.0,
    val error: String? = null
)

data class PositionsResponse(
    val positions: List<Position> = emptyList()
)

data class Position(
    val symbol: String = "",
    val side: String = "LONG",
    val amount: Double = 0.0,
    @SerializedName("entry_price") val entryPrice: Double = 0.0,
    @SerializedName("mark_price") val markPrice: Double = 0.0,
    val pnl: Double = 0.0,
    @SerializedName("pnl_percent") val pnlPercent: Double = 0.0,
    val leverage: Int = 1
)

data class EquityHistoryResponse(
    val history: List<EquitySnapshot> = emptyList()
)

data class EquitySnapshot(
    val id: Long = 0,
    @SerializedName("trader_id") val traderId: String = "",
    val timestamp: String = "",
    @SerializedName("total_equity") val totalEquity: Double = 0.0,
    val balance: Double = 0.0,
    @SerializedName("unrealized_pnl") val unrealizedPnl: Double = 0.0,
    @SerializedName("position_count") val positionCount: Int = 0,
    @SerializedName("margin_usage_pct") val marginUsagePct: Double = 0.0
)

// ============ DECISIONS & TRADES ============

data class DecisionsResponse(
    val decisions: List<DecisionRecord> = emptyList()
)

data class DecisionRecord(
    val id: Long = 0,
    @SerializedName("trader_id") val traderId: String = "",
    val timestamp: String = "",
    @SerializedName("market_data") val marketData: String = "",
    @SerializedName("ai_response") val aiResponse: String = "",
    val decisions: String = "[]",
    val executed: Boolean = false
) {
    fun parsedItems(gson: Gson): List<ParsedDecisionItem> {
        if (decisions.isBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<ParsedDecisionItem>>() {}.type
            gson.fromJson<List<ParsedDecisionItem>>(decisions, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}

data class ParsedDecisionItem(
    val symbol: String = "",
    val action: String = "hold",
    val leverage: Int = 1,
    @SerializedName("position_size_usd") val positionSizeUsd: Double = 0.0,
    @SerializedName("stop_loss") val stopLoss: Double = 0.0,
    @SerializedName("take_profit") val takeProfit: Double = 0.0,
    val confidence: Int = 0,
    val reasoning: String = ""
)

data class TradesResponse(
    val trades: List<Trade> = emptyList(),
    val stats: TradeStats = TradeStats()
)

data class Trade(
    val id: Long = 0,
    @SerializedName("trader_id") val traderId: String = "",
    val symbol: String = "",
    val side: String = "",
    val price: Double = 0.0,
    val quantity: Double = 0.0,
    @SerializedName("quote_qty") val quoteQty: Double = 0.0,
    @SerializedName("realized_pnl") val realizedPnl: Double = 0.0,
    val commission: Double = 0.0,
    val timestamp: String = "",
    @SerializedName("order_id") val orderId: Long = 0
)

data class TradeStats(
    @SerializedName("total_trades") val totalTrades: Int = 0,
    @SerializedName("total_pnl") val totalPnl: Double = 0.0,
    @SerializedName("winning_trades") val winningTrades: Int = 0,
    @SerializedName("losing_trades") val losingTrades: Int = 0,
    @SerializedName("win_rate") val winRate: Double = 0.0,
    @SerializedName("total_commission") val totalCommission: Double = 0.0
)

// ============ STRATEGIES ============

data class StrategiesResponse(
    val strategies: List<Strategy> = emptyList()
)

data class Strategy(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    @SerializedName("is_active") val isActive: Boolean = false,
    val config: StrategyConfig = StrategyConfig(),
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("updated_at") val updatedAt: String = ""
)

data class StrategyConfig(
    @SerializedName("coin_source") val coinSource: CoinSourceConfig = CoinSourceConfig(),
    val indicators: IndicatorConfig = IndicatorConfig(),
    @SerializedName("risk_control") val riskControl: RiskControlConfig = RiskControlConfig(),
    val ai: AIConfig = AIConfig(),
    @SerializedName("custom_prompt") val customPrompt: String = "",
    @SerializedName("trading_interval") val tradingInterval: Int = 5,
    @SerializedName("turbo_mode") val turboMode: Boolean = false,
    @SerializedName("simple_mode") val simpleMode: Boolean = false,
    @SerializedName("trading_mode") val tradingMode: String = "strategy",
    @SerializedName("enable_market_intel") val enableMarketIntel: Boolean = false
)

data class CoinSourceConfig(
    @SerializedName("source_type") val sourceType: String = "static",
    @SerializedName("static_coins") val staticCoins: List<String> = emptyList()
)

data class IndicatorConfig(
    @SerializedName("primary_timeframe") val primaryTimeframe: String = "5m",
    @SerializedName("kline_count") val klineCount: Int = 50,
    @SerializedName("enable_ema") val enableEma: Boolean = true,
    @SerializedName("enable_macd") val enableMacd: Boolean = true,
    @SerializedName("enable_rsi") val enableRsi: Boolean = true,
    @SerializedName("enable_multi_tf") val enableMultiTf: Boolean = false,
    @SerializedName("confirmation_timeframe") val confirmationTimeframe: String = "15m"
)

data class RiskControlConfig(
    @SerializedName("max_positions") val maxPositions: Int = 2,
    @SerializedName("btc_eth_max_leverage") val btcEthMaxLeverage: Int = 10,
    @SerializedName("altcoin_max_leverage") val altcoinMaxLeverage: Int = 20,
    @SerializedName("min_confidence") val minConfidence: Int = 70,
    @SerializedName("min_risk_reward_ratio") val minRiskRewardRatio: Double = 3.0,
    @SerializedName("max_daily_loss_pct") val maxDailyLossPct: Double = 15.0,
    @SerializedName("enable_trailing_stop") val enableTrailingStop: Boolean = false,
    @SerializedName("enable_emergency_shutdown") val enableEmergencyShutdown: Boolean = false
)

data class AIConfig(
    @SerializedName("enable_reasoning") val enableReasoning: Boolean = false,
    @SerializedName("reasoning_model") val reasoningModel: String = ""
)

data class RecommendPairsRequest(
    val count: Int = 7,
    val turbo: Boolean = false
)

data class RecommendPairsResponse(
    val pairs: List<String> = emptyList()
)

// ============ DEBATES ============

data class DebatesResponse(
    val sessions: List<DebateSession> = emptyList()
)

data class DebateSession(
    val id: String = "",
    val name: String = "",
    val status: String = "pending",
    val symbols: List<String> = emptyList(),
    @SerializedName("max_rounds") val maxRounds: Int = 3,
    @SerializedName("current_round") val currentRound: Int = 0,
    @SerializedName("interval_minutes") val intervalMinutes: Int = 0,
    @SerializedName("auto_execute") val autoExecute: Boolean = false,
    @SerializedName("created_at") val createdAt: String = "",
    val error: String? = null
)

// ============ SETTINGS ============

data class SettingsResponse(
    val settings: GlobalSettings = GlobalSettings(),
    val configured: ConfiguredFlags = ConfiguredFlags()
)

data class GlobalSettings(
    @SerializedName("openrouter_api_key") val openRouterApiKey: String = "",
    @SerializedName("openrouter_model") val openRouterModel: String = "",
    @SerializedName("binance_api_key") val binanceApiKey: String = "",
    @SerializedName("binance_secret_key") val binanceSecretKey: String = "",
    @SerializedName("binance_testnet") val binanceTestnet: Boolean = true
)

data class ConfiguredFlags(
    val openrouter: Boolean = false,
    val binance: Boolean = false
)

// ============ SSE EVENTS & LOGS ============

data class ServerEvent(
    val type: String = "info",
    @SerializedName("trader_id") val traderId: String? = null,
    val symbol: String? = null,
    val message: String = "",
    val timestamp: Long = 0L
)

data class LogEntry(
    val timestamp: String = "",
    val message: String = ""
)
