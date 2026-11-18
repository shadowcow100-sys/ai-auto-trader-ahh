package com.autotrader.ahh.ui.screens.connect

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autotrader.ahh.data.repository.TraderRepository
import com.autotrader.ahh.ui.components.GlassCard
import com.autotrader.ahh.ui.components.StatusBadge
import com.autotrader.ahh.ui.theme.BgPrimary
import com.autotrader.ahh.ui.theme.BgSurfaceElevated
import com.autotrader.ahh.ui.theme.BorderSubtle
import com.autotrader.ahh.ui.theme.CrimsonLoss
import com.autotrader.ahh.ui.theme.CrimsonSoft
import com.autotrader.ahh.ui.theme.EmeraldPrimary
import com.autotrader.ahh.ui.theme.EmeraldSoft
import com.autotrader.ahh.ui.theme.SkyInfo
import com.autotrader.ahh.ui.theme.SkySoft
import com.autotrader.ahh.ui.theme.TextMuted
import com.autotrader.ahh.ui.theme.TextPrimary
import com.autotrader.ahh.ui.theme.TextSecondary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ConnectScreen(
    viewModel: ConnectViewModel,
    onConnected: () -> Unit,
    onBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val savedProfiles by viewModel.savedProfiles.collectAsStateWithLifecycle()
    val activeUrl by viewModel.activeUrl.collectAsStateWithLifecycle()

    var showPasskey by remember { mutableStateOf(false) }
    var showPairingLinkInput by remember { mutableStateOf(false) }
    var pairingLinkText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("↔", color = TextSecondary, fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.Default.PhoneAndroid,
                        contentDescription = null,
                        tint = SkyInfo,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "PC - Phone Link",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Connect this Android app to your PC AutoTrader Go server (port 8080).",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            }
            if (onBack != null && activeUrl.isNotBlank()) {
                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
            }
        }

        // Connection Form
        GlassCard {
            Text(
                text = "PC Server Connection",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))

            val fieldColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = EmeraldPrimary,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedLabelColor = EmeraldPrimary,
                unfocusedLabelColor = TextSecondary
            )

            OutlinedTextField(
                value = state.profileName,
                onValueChange = viewModel::onNameChanged,
                label = { Text("PC Name") },
                placeholder = { Text("Home PC / Trading Rig") },
                singleLine = true,
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = state.serverUrl,
                onValueChange = viewModel::onUrlChanged,
                label = { Text("PC Address / URL") },
                placeholder = { Text("http://192.168.1.50:8080") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Quick presets
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                AssistChip(
                    onClick = { viewModel.applyPreset("LAN PC", "http://192.168.1.100:8080") },
                    label = { Text("LAN Wi-Fi", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = BgSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
                AssistChip(
                    onClick = { viewModel.applyPreset("Tailscale PC", "http://100.64.0.1:8080") },
                    label = { Text("Tailscale", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = BgSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
                AssistChip(
                    onClick = { viewModel.applyPreset("Android Emulator", "http://10.0.2.2:8080") },
                    label = { Text("Emulator (10.0.2.2)", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = BgSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
                AssistChip(
                    onClick = { showPairingLinkInput = !showPairingLinkInput },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = SkyInfo
                        )
                    },
                    label = { Text("Pairing URI", fontSize = 11.sp) },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = SkySoft,
                        labelColor = SkyInfo
                    )
                )
            }

            if (showPairingLinkInput) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = pairingLinkText,
                    onValueChange = { raw ->
                        pairingLinkText = raw
                        TraderRepository.parsePairingUri(raw)?.let { parsed ->
                            viewModel.applyPairingPayload(parsed)
                            showPairingLinkInput = false
                        }
                    },
                    label = { Text("Paste autotrader://connect?url=...&key=...") },
                    singleLine = true,
                    colors = fieldColors,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = state.passkey,
                onValueChange = viewModel::onPasskeyChanged,
                label = { Text("ACCESS_PASSKEY (optional if unset)") },
                placeholder = { Text("Server .env ACCESS_PASSKEY") },
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Key, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    IconButton(onClick = { showPasskey = !showPasskey }) {
                        Icon(
                            imageVector = if (showPasskey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = "Toggle passkey visibility",
                            tint = TextSecondary
                        )
                    }
                },
                visualTransformation = if (showPasskey) VisualTransformation.None else PasswordVisualTransformation(),
                colors = fieldColors,
                modifier = Modifier.fillMaxWidth()
            )

            // Test result feedback
            state.testResult?.let { res ->
                Spacer(modifier = Modifier.height(12.dp))
                val ok = res.reachable && res.authValid
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (ok) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = if (ok) EmeraldPrimary else CrimsonLoss,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = if (ok) {
                            "PC reachable (${res.latencyMs}ms) • Auth ${if (res.authRequired) "verified" else "open"}"
                        } else {
                            res.errorMessage ?: "Verification failed"
                        },
                        color = if (ok) EmeraldPrimary else CrimsonLoss,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            state.errorMessage?.let { err ->
                if (state.testResult == null || state.testResult?.errorMessage != err) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = err, color = CrimsonLoss, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = viewModel::testConnection,
                    enabled = !state.isTesting && !state.isConnecting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (state.isTesting) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(Icons.Default.NetworkCheck, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test")
                }

                Button(
                    onClick = { viewModel.connectAndSave(onConnected) },
                    enabled = !state.isTesting && !state.isConnecting,
                    modifier = Modifier.weight(1.4f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = BgPrimary
                    )
                ) {
                    if (state.isConnecting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = BgPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(
                        text = "Connect & Save",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Saved PC Profiles
        if (savedProfiles.isNotEmpty()) {
            GlassCard {
                Text(
                    text = "Saved PC Servers",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    savedProfiles.forEach { profile ->
                        val isCurrent = profile.baseUrl.equals(activeUrl, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.applyProfile(profile) }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = profile.name,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    if (isCurrent) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusBadge(
                                            label = "ACTIVE",
                                            color = EmeraldPrimary,
                                            backgroundColor = EmeraldSoft
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = profile.baseUrl,
                                    color = TextSecondary,
                                    fontSize = 12.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            IconButton(onClick = { viewModel.deleteProfile(profile.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete saved PC",
                                    tint = TextMuted
                                )
                            }
                        }
                    }
                }
            }
        }

        // Setup Guide Card
        GlassCard(containerColor = BgSurfaceElevated) {
            Text(
                text = "How to pair PC & Phone",
                color = SkyInfo,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "1. Start the Go backend on your PC (`cd server && go run main.go`). It listens on port 8080.\n" +
                    "2. Same Wi-Fi: find your PC local IP (e.g. 192.168.1.50) and allow TCP port 8080 in your PC firewall.\n" +
                    "3. Remote access: install Tailscale on PC & Android and enter your PC's 100.x.y.z IP or MagicDNS HTTPS URL.\n" +
                    "4. If ACCESS_PASSKEY is set in `server/.env`, enter it above — the phone sends it as X-Access-Key.",
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 18.sp
            )
        }
    }
}
