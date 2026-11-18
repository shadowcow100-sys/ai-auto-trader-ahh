package com.autotrader.ahh.ui.screens.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.autotrader.ahh.data.model.Position
import com.autotrader.ahh.ui.components.EquitySparklineChart
import com.autotrader.ahh.ui.components.GlassCard
import com.autotrader.ahh.ui.components.MetricCell
import com.autotrader.ahh.ui.components.StatusBadge
import com.autotrader.ahh.ui.components.formatPercent
import com.autotrader.ahh.ui.components.formatPrice
import com.autotrader.ahh.ui.components.formatSignedUsd
import com.autotrader.ahh.ui.components.formatUsd
import com.autotrader.ahh.ui.theme.AmberSoft
import com.autotrader.ahh.ui.theme.AmberWarn
import com.autotrader.ahh.ui.theme.BgPrimary
import com.autotrader.ahh.ui.theme.BgSurfaceElevated
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
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onOpenConnection: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val pcName by viewModel.activeProfileName.collectAsStateWithLifecycle()
    val pcUrl by viewModel.activeBaseUrl.collectAsStateWithLifecycle()

    val selectedTrader = state.traders.firstOrNull { it.id == state.selectedTraderId }
    val isRunning = state.status?.running ?: selectedTrader?.active ?: false

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header with active PC badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onOpenConnection() }
            ) {
                Text(
                    text = "Dashboard",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Computer,
                        contentDescription = null,
                        tint = EmeraldPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$pcName • ${pcUrl.removePrefix("http://").removePrefix("https://")}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
            IconButton(onClick = { viewModel.refreshAll() }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = TextPrimary
                )
            }
        }

        // Error banner
        state.errorMessage?.let { err ->
            GlassCard(borderColor = CrimsonLoss, containerColor = CrimsonSoft) {
                Text(text = err, color = TextPrimary, fontSize = 13.sp)
            }
        }

        if (state.isLoading && state.traders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
            return@Column
        }

        if (state.traders.isEmpty()) {
            GlassCard {
                Text(
                    text = "No Traders Configured",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Create a trader in the PC web dashboard, then refresh here to start and monitor it from your phone.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(onClick = { viewModel.refreshAll(initial = true) }) {
                    Text("Refresh")
                }
            }
            return@Column
        }

        // Trader selector row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.traders.forEach { trader ->
                val selected = trader.id == state.selectedTraderId
                FilterChip(
                    selected = selected,
                    onClick = { viewModel.selectTrader(trader.id) },
                    label = {
                        Text(
                            text = trader.name.ifBlank { trader.id.take(8) },
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = EmeraldSoft,
                        selectedLabelColor = EmeraldPrimary,
                        containerColor = BgSurfaceElevated,
                        labelColor = TextSecondary
                    )
                )
            }
        }

        // Trader Control Card
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedTrader?.name ?: "Trader",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Strategy: ${state.status?.strategy ?: "Active"} • ${selectedTrader?.exchange?.uppercase() ?: "BINANCE"}",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
                StatusBadge(
                    label = if (isRunning) "RUNNING" else "STOPPED",
                    color = if (isRunning) EmeraldPrimary else CrimsonLoss,
                    backgroundColor = if (isRunning) EmeraldSoft else CrimsonSoft
                )
            }

            // Pause warning
            if (state.pauseStatus?.isPaused == true) {
                Spacer(modifier = Modifier.height(10.dp))
                GlassCard(borderColor = AmberWarn, containerColor = AmberSoft) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.PauseCircle, contentDescription = null, tint = AmberWarn)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Trading paused (daily loss guard)",
                                color = TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        OutlinedButton(
                            onClick = viewModel::resumeSelectedTrader,
                            enabled = !state.isActionPending
                        ) {
                            Text("Resume", fontSize = 12.sp)
                        }
                    }
                }
            }

            // Pairs
            val pairs = state.status?.pairs.orEmpty()
            if (pairs.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    pairs.take(8).forEach { pair ->
                        AssistChip(
                            onClick = {},
                            label = { Text(pair, fontSize = 11.sp, fontFamily = FontFamily.Monospace) },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = BgSurfaceElevated,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = viewModel::startSelectedTrader,
                    enabled = !isRunning && !state.isActionPending,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = EmeraldPrimary,
                        contentColor = BgPrimary
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Start", fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = viewModel::stopSelectedTrader,
                    enabled = isRunning && !state.isActionPending,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CrimsonLoss,
                        contentColor = TextPrimary
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Stop", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Account Summary Card
        val account = state.account
        GlassCard {
            Text(
                text = "Account Overview",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (account != null && account.error.isNullOrBlank()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCell(
                        label = "Total Equity",
                        value = formatUsd(account.totalEquity),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "Unrealized PnL",
                        value = formatSignedUsd(account.unrealizedPnl),
                        valueColor = if (account.unrealizedPnl >= 0) EmeraldPrimary else CrimsonLoss,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCell(
                        label = "Wallet Balance",
                        value = formatUsd(account.walletBalance),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "Available",
                        value = formatUsd(account.available),
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = account?.error ?: "Start trader to stream live Binance Futures balance.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            }
        }

        // Equity Curve Card
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Equity Curve",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${state.equityHistory.size} pts",
                    color = TextMuted,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            EquitySparklineChart(snapshots = state.equityHistory)
        }

        // Open Positions
        GlassCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Open Positions",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                StatusBadge(
                    label = "${state.positions.size} OPEN",
                    color = SkyInfo,
                    backgroundColor = SkySoft,
                    showDot = false
                )
            }
            Spacer(modifier = Modifier.height(12.dp))

            if (state.positions.isEmpty()) {
                Text(
                    text = "No active positions.",
                    color = TextMuted,
                    fontSize = 13.sp
                )
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    state.positions.forEach { pos ->
                        PositionRow(pos)
                    }
                }
            }
        }
    }
}

@Composable
private fun PositionRow(pos: Position) {
    val isLong = pos.side.equals("LONG", ignoreCase = true) || pos.amount > 0
    val isProfit = pos.pnl >= 0
    GlassCard(containerColor = BgSurfaceElevated) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = pos.symbol,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                Spacer(modifier = Modifier.width(8.dp))
                StatusBadge(
                    label = "${if (isLong) "LONG" else "SHORT"} ${pos.leverage}x",
                    color = if (isLong) EmeraldPrimary else CrimsonLoss,
                    backgroundColor = if (isLong) EmeraldSoft else CrimsonSoft,
                    showDot = false
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = formatSignedUsd(pos.pnl),
                    color = if (isProfit) EmeraldPrimary else CrimsonLoss,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = formatPercent(pos.pnlPercent),
                    color = if (isProfit) EmeraldPrimary else CrimsonLoss,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Qty: ${pos.amount}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Entry: ${formatPrice(pos.entryPrice)}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Mark: ${formatPrice(pos.markPrice)}",
                color = TextSecondary,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
