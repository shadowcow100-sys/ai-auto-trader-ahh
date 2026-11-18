package com.autotrader.ahh

import android.app.Application
import com.autotrader.ahh.data.local.ConnectionPreferences
import com.autotrader.ahh.data.remote.NetworkModule
import com.autotrader.ahh.data.remote.SseStreamClient
import com.autotrader.ahh.data.repository.TraderRepository

class AutoTraderApp : Application() {

    lateinit var repository: TraderRepository
        private set

    override fun onCreate() {
        super.onCreate()
        val gson = NetworkModule.provideGson()
        val interceptor = NetworkModule.provideDynamicInterceptor()
        val restClient = NetworkModule.provideRestOkHttpClient(interceptor)
        val sseOkHttpClient = NetworkModule.provideSseOkHttpClient(interceptor)
        val api = NetworkModule.provideApi(restClient, gson)
        val sseClient = SseStreamClient(sseOkHttpClient, gson, interceptor)
        val prefs = ConnectionPreferences(applicationContext, gson)

        repository = TraderRepository(
            api = api,
            sseClient = sseClient,
            prefs = prefs,
            interceptor = interceptor,
            gson = gson
        )
    }
}
