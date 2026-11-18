package com.autotrader.ahh.data.remote

import com.autotrader.ahh.data.model.LogEntry
import com.autotrader.ahh.data.model.ServerEvent
import com.google.gson.Gson
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources

class SseStreamClient(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val interceptor: DynamicBaseUrlInterceptor
) {
    fun streamEvents(): Flow<ServerEvent> = callbackFlow {
        val baseUrl = interceptor.baseUrl.trim().trimEnd('/')
        if (baseUrl.isBlank()) {
            close(IllegalStateException("PC server URL is not configured"))
            return@callbackFlow
        }

        val request = Request.Builder()
            .url("http://placeholder.local/api/events")
            .header("Accept", "text/event-stream")
            .build()

        val factory = EventSources.createFactory(okHttpClient)
        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val event = gson.fromJson(data, ServerEvent::class.java)
                    if (event != null) {
                        trySend(event)
                    }
                } catch (_: Exception) {
                    // Ignore malformed SSE payload
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                close(t ?: IllegalStateException("Event stream closed (${response?.code})"))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val eventSource = factory.newEventSource(request, listener)
        awaitClose { eventSource.cancel() }
    }

    fun streamLogs(): Flow<LogEntry> = callbackFlow {
        val baseUrl = interceptor.baseUrl.trim().trimEnd('/')
        if (baseUrl.isBlank()) {
            close(IllegalStateException("PC server URL is not configured"))
            return@callbackFlow
        }

        val request = Request.Builder()
            .url("http://placeholder.local/api/logs/stream")
            .header("Accept", "text/event-stream")
            .build()

        val factory = EventSources.createFactory(okHttpClient)
        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                try {
                    val entry = gson.fromJson(data, LogEntry::class.java)
                    if (entry != null && entry.message.isNotBlank()) {
                        trySend(entry)
                    }
                } catch (_: Exception) {
                    // Ignore malformed log line
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                close(t ?: IllegalStateException("Log stream closed (${response?.code})"))
            }

            override fun onClosed(eventSource: EventSource) {
                close()
            }
        }

        val eventSource = factory.newEventSource(request, listener)
        awaitClose { eventSource.cancel() }
    }
}
