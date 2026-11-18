package com.autotrader.ahh.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.autotrader.ahh.data.model.ServerProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.UUID

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "pc_connection_prefs")

class ConnectionPreferences(
    private val context: Context,
    private val gson: Gson
) {
    private object Keys {
        val ACTIVE_BASE_URL = stringPreferencesKey("active_base_url")
        val ACTIVE_PASSKEY = stringPreferencesKey("active_passkey")
        val ACTIVE_PROFILE_NAME = stringPreferencesKey("active_profile_name")
        val SAVED_PROFILES = stringPreferencesKey("saved_profiles_json")
        val SELECTED_TRADER_ID = stringPreferencesKey("selected_trader_id")
    }

    val activeBaseUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_BASE_URL] ?: ""
    }

    val activePasskey: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_PASSKEY] ?: ""
    }

    val activeProfileName: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.ACTIVE_PROFILE_NAME] ?: "PC Server"
    }

    val selectedTraderId: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.SELECTED_TRADER_ID] ?: ""
    }

    val savedProfiles: Flow<List<ServerProfile>> = context.dataStore.data.map { prefs ->
        parseProfiles(prefs[Keys.SAVED_PROFILES])
    }

    suspend fun saveActiveConnection(name: String, baseUrl: String, passkey: String) {
        val normalized = normalizeUrl(baseUrl)
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_BASE_URL] = normalized
            prefs[Keys.ACTIVE_PASSKEY] = passkey
            prefs[Keys.ACTIVE_PROFILE_NAME] = name.ifBlank { normalized }

            val currentList = parseProfiles(prefs[Keys.SAVED_PROFILES]).toMutableList()
            val existingIdx = currentList.indexOfFirst { it.baseUrl.equals(normalized, ignoreCase = true) }
            val updated = if (existingIdx >= 0) {
                currentList[existingIdx].copy(
                    name = name.ifBlank { currentList[existingIdx].name },
                    baseUrl = normalized,
                    passkey = passkey,
                    lastConnectedAt = System.currentTimeMillis()
                )
            } else {
                ServerProfile(
                    id = UUID.randomUUID().toString(),
                    name = name.ifBlank { "PC Server" },
                    baseUrl = normalized,
                    passkey = passkey,
                    lastConnectedAt = System.currentTimeMillis()
                )
            }
            if (existingIdx >= 0) {
                currentList[existingIdx] = updated
            } else {
                currentList.add(0, updated)
            }
            prefs[Keys.SAVED_PROFILES] = gson.toJson(currentList)
        }
    }

    suspend fun deleteProfile(profileId: String) {
        context.dataStore.edit { prefs ->
            val currentList = parseProfiles(prefs[Keys.SAVED_PROFILES]).filterNot { it.id == profileId }
            prefs[Keys.SAVED_PROFILES] = gson.toJson(currentList)
        }
    }

    suspend fun clearActiveConnection() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.ACTIVE_BASE_URL)
            prefs.remove(Keys.ACTIVE_PASSKEY)
            prefs.remove(Keys.ACTIVE_PROFILE_NAME)
        }
    }

    suspend fun saveSelectedTraderId(traderId: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_TRADER_ID] = traderId
        }
    }

    suspend fun getActiveUrlOnce(): String = activeBaseUrl.first()

    suspend fun getActivePasskeyOnce(): String = activePasskey.first()

    private fun parseProfiles(json: String?): List<ServerProfile> {
        if (json.isNullOrBlank()) return emptyList()
        return try {
            val type = object : TypeToken<List<ServerProfile>>() {}.type
            gson.fromJson<List<ServerProfile>>(json, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    companion object {
        /**
         * Normalizes user-entered PC address into a clean base URL without trailing slash.
         * Examples:
         *  "192.168.1.42:8080" -> "http://192.168.1.42:8080"
         *  "192.168.1.42"      -> "http://192.168.1.42:8080"
         *  "https://my-pc.ts.net/" -> "https://my-pc.ts.net"
         */
        fun normalizeUrl(raw: String): String {
            val trimmed = raw.trim().trimEnd('/')
            if (trimmed.isBlank()) return ""
            val withScheme = if (trimmed.startsWith("http://", ignoreCase = true) ||
                trimmed.startsWith("https://", ignoreCase = true)
            ) {
                trimmed
            } else {
                "http://$trimmed"
            }

            // If user entered a bare IP or hostname without a port and using http, append :8080
            val afterScheme = withScheme.substringAfter("://")
            val hostPort = afterScheme.substringBefore("/")
            return if (!hostPort.contains(":") && withScheme.startsWith("http://", ignoreCase = true)) {
                val pathSuffix = afterScheme.substringAfter("/", "")
                val base = "http://$hostPort:8080"
                if (pathSuffix.isNotEmpty()) "$base/$pathSuffix" else base
            } else {
                withScheme
            }
        }
    }
}
