package com.autotrader.ahh.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkTradingColorScheme = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = BgPrimary,
    secondary = SkyInfo,
    onSecondary = BgPrimary,
    tertiary = VioletAccent,
    background = BgPrimary,
    onBackground = TextPrimary,
    surface = BgSurface,
    onSurface = TextPrimary,
    surfaceVariant = BgSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    error = CrimsonLoss,
    onError = TextPrimary
)

@Composable
fun AutoTraderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkTradingColorScheme,
        typography = AppTypography,
        content = content
    )
}
