package com.llmusage.monitor.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.llmusage.monitor.ui.theme.DangerRed
import com.llmusage.monitor.ui.theme.SuccessGreen
import com.llmusage.monitor.ui.theme.WarnAmber

@Composable
fun QuotaBar(
    label: String,
    valueLabel: String,
    /** Percent in 0..1, null means "no quota data available". */
    progress: Float?,
    modifier: Modifier = Modifier
) {
    val safeProgress = progress?.coerceIn(0f, 1f)
    val color = when {
        safeProgress == null -> MaterialTheme.colorScheme.primary
        safeProgress < 0.5f -> SuccessGreen
        safeProgress < 0.9f -> WarnAmber
        else -> DangerRed
    }

    Box(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(valueLabel, style = MaterialTheme.typography.labelMedium)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(50))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (safeProgress != null && safeProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(safeProgress)
                        .fillMaxSize()
                        .clip(RoundedCornerShape(50))
                        .background(color)
                )
            }
        }
    }
}
