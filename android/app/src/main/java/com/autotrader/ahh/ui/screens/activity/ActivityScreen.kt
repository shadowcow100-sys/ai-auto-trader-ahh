package com.autotrader.ahh.ui.screens.activity

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.autotrader.ahh.ui.components.GlassCard
import com.autotrader.ahh.ui.components.MetricCell
import com.autotrader.ahh.ui.components.StatusBadge
import com.autotrader.ahh.ui.components.formatPrice
import com.autotrader.ahh.ui.components.formatSignedUsd
import com.autotrader.ahh.ui.components.formatUsd
import com.autotrader.ahh.ui.theme.AmberSoft
import com.autotrader.ahh.ui.theme.AmberWarn
import com.autotrader.ahh.ui.theme.BgPrimary
import com.autotrader.ahh.ui.theme.BgSurface
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
import java.util.Locale

@Composable
fun ActivityScreen(viewModel: ActivityViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val tabs = listOf("Decisions", "Trades", "Debates")

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Activity & AI",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = viewModel::refresh) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
            }
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

        state.errorMessage?.let { err ->
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                GlassCard(borderColor = CrimsonLoss, containerColor = CrimsonSoft) {
                    Text(text = err, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = EmeraldPrimary)
            }
            return@Column
        }

        when (state.selectedTab) {
            0 -> DecisionsTab(state.decisions)
            1 -> TradesTab(state)
            2 -> DebatesTab(state, viewModel::startDebate, viewModel::stopDebate)
        }
    }
}

@Composable
private fun DecisionsTab(decisions: List<DecisionUiItem>) {
    if (decisions.isEmpty()) {
        EmptyState("No AI decisions recorded yet for this trader.")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(decisions, key = { it.record.id }) { dec ->
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = dec.record.timestamp.take(19).replace("T", " "),
                        color = TextMuted,
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    StatusBadge(
                        label = if (dec.record.executed) "EXECUTED" else "EVALUATED",
                        color = if (dec.record.executed) EmeraldPrimary else SkyInfo,
                        backgroundColor = if (dec.record.executed) EmeraldSoft else SkySoft,
                        showDot = false
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (dec.items.isEmpty()) {
                    Text(
                        text = dec.record.aiResponse.take(300).ifBlank { "No decision items" },
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        dec.items.forEach { item ->
                            val actionLower = item.action.lowercase()
                            val badgeColor = when {
                                actionLower.contains("long") || actionLower == "buy" -> EmeraldPrimary to EmeraldSoft
                                actionLower.contains("short") || actionLower == "sell" -> CrimsonLoss to CrimsonSoft
                                else -> AmberWarn to AmberSoft
                            }
                            GlassCard(containerColor = BgSurfaceElevated) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.symbol,
                                            color = TextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Monospace
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        StatusBadge(
                                            label = item.action.uppercase(),
                                            color = badgeColor.first,
                                            backgroundColor = badgeColor.second,
                                            showDot = false
                                        )
                                    }
                                    Text(
                                        text = "${item.confidence}% • ${item.leverage}x",
                                        color = SkyInfo,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                                if (item.reasoning.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = item.reasoning,
                                        color = TextSecondary,
                                        fontSize = 12.sp,
                                        lineHeight = 17.sp
                                    )
                                }
                                if (item.stopLoss > 0 || item.takeProfit > 0) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "SL ${formatPrice(item.stopLoss)}  |  TP ${formatPrice(item.takeProfit)}",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TradesTab(state: ActivityUiState) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            val stats = state.tradeStats
            GlassCard {
                Text(
                    text = "Trade Statistics",
                    color = TextPrimary,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCell(
                        label = "Total PnL",
                        value = formatSignedUsd(stats.totalPnl),
                        valueColor = if (stats.totalPnl >= 0) EmeraldPrimary else CrimsonLoss,
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "Win Rate",
                        value = String.format(Locale.US, "%.1f%%", stats.winRate),
                        valueColor = SkyInfo,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCell(
                        label = "Trades",
                        value = "${stats.totalTrades} (${stats.winningTrades}W/${stats.losingTrades}L)",
                        modifier = Modifier.weight(1f)
                    )
                    MetricCell(
                        label = "Fees",
                        value = formatUsd(stats.totalCommission),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        if (state.trades.isEmpty()) {
            item { EmptyState("No executed trades recorded yet.") }
        } else {
            items(state.trades, key = { it.id }) { trade ->
                val isBuy = trade.side.equals("BUY", ignoreCase = true)
                GlassCard(containerColor = BgSurfaceElevated) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = trade.symbol,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            StatusBadge(
                                label = trade.side.uppercase(),
                                color = if (isBuy) EmeraldPrimary else CrimsonLoss,
                                backgroundColor = if (isBuy) EmeraldSoft else CrimsonSoft,
                                showDot = false
                            )
                        }
                        Text(
                            text = formatSignedUsd(trade.realizedPnl),
                            color = if (trade.realizedPnl >= 0) EmeraldPrimary else CrimsonLoss,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Price ${formatPrice(trade.price)} • Qty ${trade.quantity} • Val ${formatUsd(trade.quoteQty)}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = trade.timestamp.take(19).replace("T", " "),
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun DebatesTab(
    state: ActivityUiState,
    onStart: (String) -> Unit,
    onStop: (String) -> Unit
) {
    if (state.debates.isEmpty()) {
        EmptyState("No Multi-AI debate sessions found.")
        return
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(state.debates, key = { it.id }) { session ->
            val running = session.status == "running" || session.status == "voting"
            GlassCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = session.name.ifBlank { "Debate ${session.id.take(8)}" },
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Round ${session.currentRound}/${session.maxRounds} • ${session.symbols.joinToString(", ")}",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                    StatusBadge(
                        label = session.status.uppercase(),
                        color = if (running) EmeraldPrimary else SkyInfo,
                        backgroundColor = if (running) EmeraldSoft else SkySoft
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { if (running) onStop(session.id) else onStart(session.id) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (running) CrimsonLoss else EmeraldPrimary,
                            contentColor = if (running) TextPrimary else BgPrimary
                        )
                    ) {
                        Text(if (running) "Stop Debate" else "Start Debate", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = TextMuted, fontSize = 14.sp)
    }
}
