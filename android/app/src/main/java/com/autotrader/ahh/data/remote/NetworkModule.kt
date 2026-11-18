package com.autotrader.ahh.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object NetworkModule {

    fun provideGson(): Gson = GsonBuilder()
        .setLenient()
        .create()

    fun provideDynamicInterceptor(): DynamicBaseUrlInterceptor = DynamicBaseUrlInterceptor()

    fun provideRestOkHttpClient(interceptor: DynamicBaseUrlInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .addInterceptor(logging)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(45, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun provideSseOkHttpClient(interceptor: DynamicBaseUrlInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(0, TimeUnit.SECONDS) // Long-lived SSE stream
            .retryOnConnectionFailure(true)
            .build()
    }

    fun provideApi(client: OkHttpClient, gson: Gson): AutoTraderApi {
        return Retrofit.Builder()
            .baseUrl("http://placeholder.local/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
            .create(AutoTraderApi::class.java)
    }
}
