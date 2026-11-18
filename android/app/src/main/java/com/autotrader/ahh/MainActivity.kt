package com.autotrader.ahh

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.autotrader.ahh.data.model.PairingPayload
import com.autotrader.ahh.data.repository.TraderRepository
import com.autotrader.ahh.ui.navigation.AutoTraderNavHost
import com.autotrader.ahh.ui.theme.AutoTraderTheme
import com.autotrader.ahh.ui.theme.BgPrimary
import com.autotrader.ahh.ui.theme.EmeraldPrimary

class MainActivity : ComponentActivity() {

    private var pendingPairing by mutableStateOf<PairingPayload?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleDeepLink(intent)

        val repository = (application as AutoTraderApp).repository

        setContent {
            var hydrated by mutableStateOf(false)
            var startConnected by mutableStateOf(false)

            LaunchedEffect(Unit) {
                startConnected = repository.hydrateConnectionFromPrefs()
                hydrated = true
            }

            AutoTraderTheme {
                if (!hydrated) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = EmeraldPrimary, trackColor = BgPrimary)
                    }
                } else {
                    AutoTraderNavHost(
                        repository = repository,
                        startConnected = startConnected,
                        pendingPairing = pendingPairing,
                        onPairingConsumed = { pendingPairing = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        val dataString = intent?.dataString ?: return
        val payload = TraderRepository.parsePairingUri(dataString)
        if (payload != null) {
            pendingPairing = payload
        }
    }
}
