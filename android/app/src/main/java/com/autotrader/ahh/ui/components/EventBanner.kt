package com.autotrader.ahh.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.autotrader.ahh.data.model.ServerEvent
import com.autotrader.ahh.ui.theme.AmberSoft
import com.autotrader.ahh.ui.theme.AmberWarn
import com.autotrader.ahh.ui.theme.CrimsonLoss
import com.autotrader.ahh.ui.theme.CrimsonSoft
import com.autotrader.ahh.ui.theme.EmeraldPrimary
import com.autotrader.ahh.ui.theme.EmeraldSoft
import com.autotrader.ahh.ui.theme.SkyInfo
import com.autotrader.ahh.ui.theme.SkySoft
import com.autotrader.ahh.ui.theme.TextPrimary

@Composable
fun LiveEventBanner(
    event: ServerEvent?,
    onDismiss: () -> Unit
) {
    AnimatedVisibility(visible = event != null && event.type != "sys") {
        event?.let { evt ->
            val (bg, accent) = when (evt.type) {
                "trade" -> EmeraldSoft to EmeraldPrimary
                "error" -> CrimsonSoft to CrimsonLoss
                "decision" -> SkySoft to SkyInfo
                else -> AmberSoft to AmberWarn
            }
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                color = bg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = buildString {
                            if (!evt.symbol.isNullOrBlank()) append("[${evt.symbol}] ")
                            append(evt.message)
                        },
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = TextPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
