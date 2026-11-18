package com.autotrader.ahh.ui.screens.logs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autotrader.ahh.ui.components.GlassCard
import com.autotrader.ahh.ui.components.StatusBadge
import com.autotrader.ahh.ui.theme.BgPrimary
import com.autotrader.ahh.ui.theme.BgSurface
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

@Composable
fun LogsScreen(
    viewModel: LogsViewModel,
    onSwitchPc: () -> Unit,
    onDisconnected: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pcName by viewModel.activeProfileName.collectAsStateWithLifecycle()
    val pcUrl by viewModel.activeBaseUrl.collectAsStateWithLifecycle()
    val tabs = listOf("Live Logs", "PC & Settings")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Console & Settings",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            StatusBadge(
                label = if (state.isStreamConnected) "SSE LIVE" else "OFFLINE",
                color = if (state.isStreamConnected) EmeraldPrimary else CrimsonLoss,
                backgroundColor = if (state.isStreamConnected) EmeraldSoft else CrimsonSoft
            )
        }

        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = BgSurface,
            contentColor = EmeraldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                    color = EmeraldPrimary
                )
            }
        ) {
            tabs.forEachIndexed { idx, title ->
                Tab(
                    selected = state.selectedTab == idx,
                    onClick = { viewModel.selectTab(idx) },
                    text = {
                        Text(
                            text = title,
                            color = if (state.selectedTab == idx) EmeraldPrimary else TextSecondary,
                            fontWeight = if (state.selectedTab == idx) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        if (state.selectedTab == 0) {
            LiveLogsTab(state = state, viewModel = viewModel)
        } else {
            SettingsTab(
                state = state,
                pcName = pcName,
                pcUrl = pcUrl,
                viewModel = viewModel,
                onSwitchPc = onSwitchPc,
                onDisconnected = onDisconnected
            )
        }
    }
}

@Composable
private fun LiveLogsTab(state: LogsUiState, viewModel: LogsViewModel) {
    val filteredLogs = if (state.filterQuery.isBlank()) {
        state.logs
    } else {
        state.logs.filter { it.message.contains(state.filterQuery, ignoreCase = true) }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(filteredLogs.size) {
        if (filteredLogs.isNotEmpty()) {
            listState.animateScrollToItem(filteredLogs.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = state.filterQuery,
                onValueChange = viewModel::onFilterChanged,
                placeholder = { Text("Filter logs...", fontSize = 12.sp) },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = viewModel::startLogStream) {
                Icon(Icons.Default.Refresh, contentDescription = "Reconnect SSE", tint = TextPrimary)
            }
            IconButton(onClick = viewModel::clearLogs) {
                Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = TextSecondary)
            }
        }

        state.streamError?.let { err ->
            Text(text = err, color = CrimsonLoss, fontSize = 12.sp)
        }

        GlassCard(modifier = Modifier.weight(1f)) {
            if (filteredLogs.isEmpty()) {
                Text(
                    text = "Waiting for log entries from PC server (/api/logs/stream)...",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            } else {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredLogs) { entry ->
                        Text(
                            text = "${entry.timestamp.take(19).replace("T", " ")}  ${entry.message.trimEnd()}",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    state: LogsUiState,
    pcName: String,
    pcUrl: String,
    viewModel: LogsViewModel,
    onSwitchPc: () -> Unit,
    onDisconnected: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Connected PC Card
        GlassCard {
            Text(
                text = "Connected PC Host",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = pcName, color = EmeraldPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(
                text = pcUrl,
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onSwitchPc,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Switch PC")
                }
                Button(
                    onClick = { viewModel.disconnectPc(onDisconnected) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonLoss,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.LinkOff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Disconnect")
                }
            }
        }

        // Global PC Settings
        GlassCard {
            Text(
                text = "PC Server Global Settings",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(
                    label = if (state.openRouterConfigured) "OpenRouter OK" else "OpenRouter Missing",
                    color = if (state.openRouterConfigured) EmeraldPrimary else CrimsonLoss,
                    backgroundColor = if (state.openRouterConfigured) EmeraldSoft else CrimsonSoft
                )
                StatusBadge(
                    label = if (state.binanceConfigured) "Binance OK" else "Binance Missing",
                    color = if (state.binanceConfigured) EmeraldPrimary else CrimsonLoss,
                    backgroundColor = if (state.binanceConfigured) EmeraldSoft else CrimsonSoft
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = state.settings.openRouterModel,
                onValueChange = viewModel::updateModelField,
                label = { Text("OpenRouter Model") },
                placeholder = { Text("deepseek/deepseek-chat") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = EmeraldPrimary,
                    unfocusedBorderColor = BorderSubtle,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Binance Testnet Mode", color = TextPrimary, fontSize = 14.sp)
                    Text("Keep enabled for paper trading", color = TextMuted, fontSize = 12.sp)
                }
                Switch(
                    checked = state.settings.binanceTestnet,
                    onCheckedChange = viewModel::updateTestnet
                )
            }

            state.settingsMessage?.let { msg ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = msg, color = SkyInfo, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = viewModel::saveSettings,
                enabled = !state.isSavingSettings,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = EmeraldPrimary,
                    contentColor = BgPrimary
                )
            ) {
                if (state.isSavingSettings) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = BgPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text("Save Settings to PC", fontWeight = FontWeight.Bold)
            }
        }
    }
}
