package com.autotrader.ahh.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autotrader.ahh.data.model.EquitySnapshot
import com.autotrader.ahh.ui.theme.BgSurface
import com.autotrader.ahh.ui.theme.BorderSubtle
import com.autotrader.ahh.ui.theme.CrimsonLoss
import com.autotrader.ahh.ui.theme.CrimsonSoft
import com.autotrader.ahh.ui.theme.EmeraldPrimary
import com.autotrader.ahh.ui.theme.EmeraldSoft
import com.autotrader.ahh.ui.theme.TextMuted
import com.autotrader.ahh.ui.theme.TextPrimary
import com.autotrader.ahh.ui.theme.TextSecondary
import java.util.Locale
import kotlin.math.abs

@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderSubtle,
    containerColor: Color = BgSurface,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun StatusBadge(
    label: String,
    color: Color,
    backgroundColor: Color,
    showDot: Boolean = true
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(backgroundColor)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showDot) {
            Box(
                modifier = Modifier
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun MetricCell(
    label: String,
    value: String,
    valueColor: Color = TextPrimary,
    subValue: String? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(text = label, color = TextSecondary, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            color = valueColor,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
        if (!subValue.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subValue, color = TextMuted, fontSize = 11.sp)
        }
    }
}

@Composable
fun EquitySparklineChart(
    snapshots: List<EquitySnapshot>,
    modifier: Modifier = Modifier
) {
    if (snapshots.size < 2) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (snapshots.isEmpty()) "No equity snapshots yet" else "Waiting for more snapshots...",
                color = TextMuted,
                fontSize = 13.sp
            )
        }
        return
    }

    val values = snapshots.map { it.totalEquity }
    val minVal = values.minOrNull() ?: 0.0
    val maxVal = values.maxOrNull() ?: 0.0
    val firstVal = values.first()
    val lastVal = values.last()
    val isPositive = lastVal >= firstVal
    val lineColor = if (isPositive) EmeraldPrimary else CrimsonLoss
    val fillStart = if (isPositive) EmeraldSoft else CrimsonSoft

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Min ${formatUsd(minVal)}",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = "Max ${formatUsd(maxVal)}",
                color = TextMuted,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val range = (maxVal - minVal).takeIf { it > 0.0001 } ?: 1.0
            val w = size.width
            val h = size.height
            val padY = 8f

            val points = values.mapIndexed { idx, v ->
                val x = (idx.toFloat() / (values.size - 1).coerceAtLeast(1)) * w
                val normY = ((v - minVal) / range).toFloat()
                val y = (h - padY) - normY * (h - padY * 2f)
                Offset(x, y)
            }

            val linePath = Path().apply {
                points.forEachIndexed { i, p ->
                    if (i == 0) moveTo(p.x, p.y) else lineTo(p.x, p.y)
                }
            }

            val fillPath = Path().apply {
                addPath(linePath)
                lineTo(points.last().x, h)
                lineTo(points.first().x, h)
                close()
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(fillStart, Color.Transparent),
                    startY = 0f,
                    endY = h
                )
            )
            drawPath(
                path = linePath,
                color = lineColor,
                style = Stroke(width = 4f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
        }
    }
}

fun formatUsd(value: Double): String {
    val sign = if (value < 0) "-" else ""
    return String.format(Locale.US, "%s$%.2f", sign, abs(value))
}

fun formatSignedUsd(value: Double): String {
    val sign = if (value >= 0) "+" else "-"
    return String.format(Locale.US, "%s$%.2f", sign, abs(value))
}

fun formatPrice(price: Double): String {
    if (price == 0.0) return "$0.00"
    val absPrice = abs(price)
    return when {
        absPrice >= 1000.0 -> String.format(Locale.US, "$%.2f", price)
        absPrice >= 1.0 -> String.format(Locale.US, "$%.4f", price)
        absPrice >= 0.01 -> String.format(Locale.US, "$%.6f", price)
        else -> String.format(Locale.US, "$%.8f", price)
    }
}

fun formatPercent(pct: Double): String {
    val sign = if (pct >= 0) "+" else "-"
    return String.format(Locale.US, "%s%.2f%%", sign, abs(pct))
}
