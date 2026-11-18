package com.autotrader.ahh.ui.screens.strategies

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import com.autotrader.ahh.ui.components.StatusBadge
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
fun StrategiesScreen(viewModel: StrategiesViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Strategies",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = viewModel::refresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = TextPrimary)
                }
            }
        }

        state.errorMessage?.let { err ->
            item {
                GlassCard(borderColor = CrimsonLoss, containerColor = CrimsonSoft) {
                    Text(text = err, color = TextPrimary, fontSize = 13.sp)
                }
            }
        }

        // Smart Find Card
        item {
            GlassCard(containerColor = BgSurfaceElevated) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = SkyInfo,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AI Smart Find Pairs",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Turbo", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Switch(
                            checked = state.turboFind,
                            onCheckedChange = viewModel::toggleTurboFind
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Ask the PC server's OpenRouter AI to scan Binance Futures tickers for optimal pairs.",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = viewModel::runSmartFind,
                    enabled = !state.isRecommending,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SkyInfo,
                        contentColor = BgPrimary
                    )
                ) {
                    if (state.isRecommending) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = BgPrimary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text("Recommend 7 Pairs", fontWeight = FontWeight.Bold)
                }

                if (state.recommendedPairs.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        state.recommendedPairs.forEach { pair ->
                            AssistChip(
                                onClick = {},
                                label = {
                                    Text(pair, fontFamily = FontFamily.Monospace, fontSize = 12.sp)
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = SkySoft,
                                    labelColor = SkyInfo
                                )
                            )
                        }
                    }
                }
            }
        }

        if (state.isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = EmeraldPrimary)
                }
            }
        } else {
            items(state.strategies, key = { it.id }) { strategy ->
                val cfg = strategy.config
                val risk = cfg.riskControl
                GlassCard(
                    borderColor = if (strategy.isActive) EmeraldPrimary else BgSurfaceElevated
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = strategy.name,
                                color = TextPrimary,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (strategy.description.isNotBlank()) {
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = strategy.description,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }
                        if (strategy.isActive) {
                            StatusBadge(
                                label = "ACTIVE",
                                color = EmeraldPrimary,
                                backgroundColor = EmeraldSoft
                            )
                        } else {
                            OutlinedButton(onClick = { viewModel.activateStrategy(strategy.id) }) {
                                Text("Activate", fontSize = 12.sp)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StatusBadge(
                            label = "TF ${cfg.indicators.primaryTimeframe} • ${cfg.tradingInterval}m",
                            color = SkyInfo,
                            backgroundColor = SkySoft,
                            showDot = false
                        )
                        StatusBadge(
                            label = "Max Pos ${risk.maxPositions}",
                            color = TextSecondary,
                            backgroundColor = BgSurfaceElevated,
                            showDot = false
                        )
                        StatusBadge(
                            label = "BTC/ETH ${risk.btcEthMaxLeverage}x • Alt ${risk.altcoinMaxLeverage}x",
                            color = TextSecondary,
                            backgroundColor = BgSurfaceElevated,
                            showDot = false
                        )
                        StatusBadge(
                            label = "Min Conf ${risk.minConfidence}% • R:R ${risk.minRiskRewardRatio}",
                            color = TextSecondary,
                            backgroundColor = BgSurfaceElevated,
                            showDot = false
                        )
                        if (cfg.turboMode) {
                            StatusBadge(
                                label = "TURBO",
                                color = AmberWarn,
                                backgroundColor = AmberSoft,
                                showDot = false
                            )
                        }
                    }

                    if (cfg.coinSource.staticCoins.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Coins: ${cfg.coinSource.staticCoins.joinToString(", ")}",
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
