package com.autotrader.ahh.data.remote

import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.Response
import java.io.IOException

/**
 * Rewrites every request to target the currently selected PC server URL and
 * attaches X-Access-Key when configured.
 */
class DynamicBaseUrlInterceptor : Interceptor {

    @Volatile
    var baseUrl: String = ""

    @Volatile
    var accessKey: String = ""

    override fun intercept(chain: Interceptor.Chain): Response {
        val original = chain.request()
        val currentBase = baseUrl.trim().trimEnd('/')

        if (currentBase.isBlank()) {
            throw IOException("PC server URL is not configured. Connect to your PC first.")
        }

        val parsedBase = currentBase.toHttpUrlOrNull()
            ?: throw IOException("Invalid PC server URL: $currentBase")

        val baseSegments = parsedBase.pathSegments.filter { it.isNotEmpty() }
        val requestSegments = original.url.pathSegments.filter { it.isNotEmpty() }

        val newUrlBuilder = original.url.newBuilder()
            .scheme(parsedBase.scheme)
            .host(parsedBase.host)
            .port(parsedBase.port)

        // Replace path segments: [baseSegments..., requestSegments...]
        for (i in original.url.pathSize - 1 downTo 0) {
            newUrlBuilder.removePathSegment(i)
        }
        baseSegments.forEach { newUrlBuilder.addPathSegment(it) }
        requestSegments.forEach { newUrlBuilder.addPathSegment(it) }

        val requestBuilder = original.newBuilder()
            .url(newUrlBuilder.build())

        val key = accessKey.trim()
        if (key.isNotEmpty()) {
            requestBuilder.header("X-Access-Key", key)
        }

        return chain.proceed(requestBuilder.build())
    }
}
