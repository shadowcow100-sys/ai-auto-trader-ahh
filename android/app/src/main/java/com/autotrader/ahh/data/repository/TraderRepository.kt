package com.autotrader.ahh.data.repository

import android.net.Uri
import com.autotrader.ahh.data.local.ConnectionPreferences
import com.autotrader.ahh.data.model.AccountInfo
import com.autotrader.ahh.data.model.AuthVerifyRequest
import com.autotrader.ahh.data.model.ConnectionTestResult
import com.autotrader.ahh.data.model.DebateSession
import com.autotrader.ahh.data.model.DecisionRecord
import com.autotrader.ahh.data.model.EquitySnapshot
import com.autotrader.ahh.data.model.GlobalSettings
import com.autotrader.ahh.data.model.LogEntry
import com.autotrader.ahh.data.model.PairingPayload
import com.autotrader.ahh.data.model.PauseStatusResponse
import com.autotrader.ahh.data.model.Position
import com.autotrader.ahh.data.model.RecommendPairsRequest
import com.autotrader.ahh.data.model.ServerEvent
import com.autotrader.ahh.data.model.ServerProfile
import com.autotrader.ahh.data.model.SettingsResponse
import com.autotrader.ahh.data.model.Strategy
import com.autotrader.ahh.data.model.Trader
import com.autotrader.ahh.data.model.TraderStatusResponse
import com.autotrader.ahh.data.model.TradesResponse
import com.autotrader.ahh.data.remote.AutoTraderApi
import com.autotrader.ahh.data.remote.DynamicBaseUrlInterceptor
import com.autotrader.ahh.data.remote.NetworkModule
import com.autotrader.ahh.data.remote.SseStreamClient
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

class TraderRepository(
    private val api: AutoTraderApi,
    private val sseClient: SseStreamClient,
    private val prefs: ConnectionPreferences,
    private val interceptor: DynamicBaseUrlInterceptor,
    val gson: Gson
) {
    val activeBaseUrl: Flow<String> = prefs.activeBaseUrl
    val activePasskey: Flow<String> = prefs.activePasskey
    val activeProfileName: Flow<String> = prefs.activeProfileName
    val savedProfiles: Flow<List<ServerProfile>> = prefs.savedProfiles
    val selectedTraderId: Flow<String> = prefs.selectedTraderId

    suspend fun hydrateConnectionFromPrefs(): Boolean {
        val url = prefs.getActiveUrlOnce()
        val key = prefs.getActivePasskeyOnce()
        interceptor.baseUrl = url
        interceptor.accessKey = key
        return url.isNotBlank()
    }

    suspend fun testConnection(rawUrl: String, passkey: String): ConnectionTestResult =
        withContext(Dispatchers.IO) {
            val normalized = ConnectionPreferences.normalizeUrl(rawUrl)
            if (normalized.isBlank()) {
                return@withContext ConnectionTestResult(
                    reachable = false,
                    errorMessage = "Please enter your PC IP address or URL (e.g. 192.168.1.50:8080)."
                )
            }

            // Use an isolated interceptor/API instance so testing a new PC does not
            // disturb an already-active background connection until saved.
            val testInterceptor = DynamicBaseUrlInterceptor().apply {
                baseUrl = normalized
                accessKey = passkey
            }
            val testClient = NetworkModule.provideRestOkHttpClient(testInterceptor)
            val testApi = NetworkModule.provideApi(testClient, gson)

            val startedAt = System.currentTimeMillis()
            return@withContext try {
                val health = testApi.getHealth()
                val latency = (System.currentTimeMillis() - startedAt).coerceAtLeast(1L)
                if (!health.status.equals("ok", ignoreCase = true)) {
                    ConnectionTestResult(
                        reachable = false,
                        latencyMs = latency,
                        errorMessage = "Unexpected health status: ${health.status}"
                    )
                } else {
                    val auth = testApi.verifyPasskey(AuthVerifyRequest(passkey = passkey))
                    ConnectionTestResult(
                        reachable = true,
                        serverTime = health.time,
                        authRequired = auth.required,
                        authValid = auth.valid,
                        latencyMs = latency,
                        errorMessage = if (auth.valid) null else (auth.message.ifBlank { "Invalid passkey" })
                    )
                }
            } catch (e: Exception) {
                ConnectionTestResult(
                    reachable = false,
                    errorMessage = humanizeError(e)
                )
            }
        }

    suspend fun connectAndSave(name: String, rawUrl: String, passkey: String): Result<ConnectionTestResult> {
        val result = testConnection(rawUrl, passkey)
        return if (result.reachable && result.authValid) {
            val normalized = ConnectionPreferences.normalizeUrl(rawUrl)
            interceptor.baseUrl = normalized
            interceptor.accessKey = passkey
            prefs.saveActiveConnection(name, normalized, passkey)
            Result.success(result)
        } else {
            Result.failure(IllegalStateException(result.errorMessage ?: "Unable to connect to PC server"))
        }
    }

    suspend fun disconnect() {
        interceptor.baseUrl = ""
        interceptor.accessKey = ""
        prefs.clearActiveConnection()
    }

    suspend fun deleteSavedProfile(profileId: String) {
        prefs.deleteProfile(profileId)
    }

    suspend fun selectTrader(traderId: String) {
        prefs.saveSelectedTraderId(traderId)
    }

    // ============ TRADER OPERATIONS ============

    suspend fun getTraders(): Result<List<Trader>> = safeCall { api.getTraders().traders }

    suspend fun startTrader(id: String): Result<Unit> = safeCall {
        api.startTrader(id)
        Unit
    }

    suspend fun stopTrader(id: String): Result<Unit> = safeCall {
        api.stopTrader(id)
        Unit
    }

    suspend fun resumeTrader(id: String): Result<Unit> = safeCall {
        api.resumeTrader(id)
        Unit
    }

    suspend fun getPauseStatus(id: String): Result<PauseStatusResponse> = safeCall {
        api.getPauseStatus(id)
    }

    suspend fun getTraderStatus(traderId: String): Result<TraderStatusResponse> = safeCall {
        api.getStatus(traderId)
    }

    suspend fun getAccount(traderId: String): Result<AccountInfo> = safeCall {
        api.getAccount(traderId)
    }

    suspend fun getPositions(traderId: String): Result<List<Position>> = safeCall {
        api.getPositions(traderId).positions
    }

    suspend fun getDecisions(traderId: String): Result<List<DecisionRecord>> = safeCall {
        api.getDecisions(traderId).decisions
    }

    suspend fun getTrades(traderId: String): Result<TradesResponse> = safeCall {
        api.getTrades(traderId)
    }

    suspend fun getEquityHistory(traderId: String): Result<List<EquitySnapshot>> = safeCall {
        api.getEquityHistory(traderId).history
    }

    // ============ STRATEGIES ============

    suspend fun getStrategies(): Result<List<Strategy>> = safeCall {
        api.getStrategies().strategies
    }

    suspend fun activateStrategy(id: String): Result<Unit> = safeCall {
        api.activateStrategy(id)
        Unit
    }

    suspend fun recommendPairs(count: Int = 7, turbo: Boolean = false): Result<List<String>> = safeCall {
        api.recommendPairs(RecommendPairsRequest(count = count, turbo = turbo)).pairs
    }

    // ============ DEBATES ============

    suspend fun getDebateSessions(): Result<List<DebateSession>> = safeCall {
        api.getDebateSessions().sessions
    }

    suspend fun startDebate(id: String): Result<Unit> = safeCall {
        api.startDebate(id)
        Unit
    }

    suspend fun stopDebate(id: String): Result<Unit> = safeCall {
        api.stopDebate(id)
        Unit
    }

    // ============ SETTINGS ============

    suspend fun getSettings(): Result<SettingsResponse> = safeCall {
        api.getSettings()
    }

    suspend fun updateSettings(settings: GlobalSettings): Result<Unit> = safeCall {
        api.updateSettings(settings)
        Unit
    }

    // ============ REAL-TIME SSE ============

    fun streamEvents(): Flow<ServerEvent> = sseClient.streamEvents()

    fun streamLogs(): Flow<LogEntry> = sseClient.streamLogs()

    private suspend fun <T> safeCall(block: suspend () -> T): Result<T> = withContext(Dispatchers.IO) {
        try {
            Result.success(block())
        } catch (e: Exception) {
            Result.failure(IllegalStateException(humanizeError(e), e))
        }
    }

    private fun humanizeError(e: Exception): String {
        return when (e) {
            is HttpException -> when (e.code()) {
                401 -> "Unauthorized (401): Check your ACCESS_PASSKEY."
                404 -> "Endpoint not found (404). Make sure the PC server is up to date."
                429 -> "Rate limited (429). Slowing down requests."
                else -> "Server error (${e.code()}): ${e.message()}"
            }
            is IOException -> e.message ?: "Network unreachable. Check Wi-Fi / Tailscale and PC firewall (port 8080)."
            else -> e.message ?: "Unknown error"
        }
    }

    companion object {
        /**
         * Parses a future PC-to-Phone pairing URI:
         * e.g. autotrader://connect?url=http://192.168.1.42:8080&key=mykey&name=Home%20PC
         */
        fun parsePairingUri(raw: String): PairingPayload? {
            return try {
                val uri = Uri.parse(raw.trim())
                if (uri.scheme == "autotrader" && uri.host == "connect") {
                    val url = uri.getQueryParameter("url").orEmpty()
                    if (url.isBlank()) return null
                    PairingPayload(
                        url = ConnectionPreferences.normalizeUrl(url),
                        passkey = uri.getQueryParameter("key").orEmpty(),
                        name = uri.getQueryParameter("name").orEmpty().ifBlank { "Paired PC" }
                    )
                } else {
                    null
                }
            } catch (_: Exception) {
                null
            }
        }
    }
}
