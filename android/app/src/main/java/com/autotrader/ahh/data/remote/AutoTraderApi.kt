package com.autotrader.ahh.data.remote

import com.autotrader.ahh.data.model.AccountInfo
import com.autotrader.ahh.data.model.AuthVerifyRequest
import com.autotrader.ahh.data.model.AuthVerifyResponse
import com.autotrader.ahh.data.model.DebatesResponse
import com.autotrader.ahh.data.model.DecisionsResponse
import com.autotrader.ahh.data.model.EquityHistoryResponse
import com.autotrader.ahh.data.model.GlobalSettings
import com.autotrader.ahh.data.model.HealthResponse
import com.autotrader.ahh.data.model.PauseStatusResponse
import com.autotrader.ahh.data.model.PositionsResponse
import com.autotrader.ahh.data.model.RecommendPairsRequest
import com.autotrader.ahh.data.model.RecommendPairsResponse
import com.autotrader.ahh.data.model.SettingsResponse
import com.autotrader.ahh.data.model.StatusResponse
import com.autotrader.ahh.data.model.StrategiesResponse
import com.autotrader.ahh.data.model.TraderStatusResponse
import com.autotrader.ahh.data.model.TradersResponse
import com.autotrader.ahh.data.model.TradesResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface AutoTraderApi {

    @GET("api/health")
    suspend fun getHealth(): HealthResponse

    @POST("api/auth/verify")
    suspend fun verifyPasskey(@Body body: AuthVerifyRequest): AuthVerifyResponse

    // Traders
    @GET("api/traders")
    suspend fun getTraders(): TradersResponse

    @POST("api/traders/{id}/start")
    suspend fun startTrader(@Path("id") id: String): StatusResponse

    @POST("api/traders/{id}/stop")
    suspend fun stopTrader(@Path("id") id: String): StatusResponse

    @POST("api/traders/{id}/resume")
    suspend fun resumeTrader(@Path("id") id: String): StatusResponse

    @GET("api/traders/{id}/pause-status")
    suspend fun getPauseStatus(@Path("id") id: String): PauseStatusResponse

    // Live Data
    @GET("api/status")
    suspend fun getStatus(@Query("trader_id") traderId: String): TraderStatusResponse

    @GET("api/account")
    suspend fun getAccount(@Query("trader_id") traderId: String): AccountInfo

    @GET("api/positions")
    suspend fun getPositions(@Query("trader_id") traderId: String): PositionsResponse

    @GET("api/decisions")
    suspend fun getDecisions(@Query("trader_id") traderId: String): DecisionsResponse

    @GET("api/trades")
    suspend fun getTrades(@Query("trader_id") traderId: String): TradesResponse

    @GET("api/equity-history")
    suspend fun getEquityHistory(@Query("trader_id") traderId: String): EquityHistoryResponse

    // Strategies
    @GET("api/strategies")
    suspend fun getStrategies(): StrategiesResponse

    @POST("api/strategies/{id}/activate")
    suspend fun activateStrategy(@Path("id") id: String): StatusResponse

    @POST("api/strategies/recommend-pairs")
    suspend fun recommendPairs(@Body body: RecommendPairsRequest): RecommendPairsResponse

    // Debates
    @GET("api/debate/sessions")
    suspend fun getDebateSessions(): DebatesResponse

    @POST("api/debate/sessions/{id}/start")
    suspend fun startDebate(@Path("id") id: String): StatusResponse

    @POST("api/debate/sessions/{id}/stop")
    suspend fun stopDebate(@Path("id") id: String): StatusResponse

    // Settings
    @GET("api/settings")
    suspend fun getSettings(): SettingsResponse

    @PUT("api/settings")
    suspend fun updateSettings(@Body body: GlobalSettings): StatusResponse
}
