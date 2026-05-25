package com.llmusage.monitor.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Lightweight bar chart. Compose-native to avoid pulling in a chart library.
 * Each bar represents one day; height is normalised to the max value.
 */
@Composable
fun BarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 140.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(vertical = 8.dp)
    ) {
        if (values.isEmpty()) return@Canvas
        val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
        val n = values.size
        val gap = 4.dp.toPx()
        val barWidth = (size.width - gap * (n - 1)) / n

        values.forEachIndexed { i, v ->
            val h = (v / maxValue) * size.height
            val x = i * (barWidth + gap)
            val y = size.height - h
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, h),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
            )
        }
    }
}

/** A simple line chart for cost over time. Smooths via quadratic curves. */
@Composable
fun LineChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    height: Dp = 140.dp
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .padding(vertical = 8.dp)
    ) {
        if (values.size < 2) return@Canvas
        val maxValue = (values.maxOrNull() ?: 0f).coerceAtLeast(1f)
        val stepX = size.width / (values.size - 1)
        val path = Path()
        values.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v / maxValue) * size.height
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path = path, color = lineColor, style = Stroke(width = 4f))
    }
}
