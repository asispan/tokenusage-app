package com.llmusage.monitor.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.llmusage.monitor.LLMUsageApp
import com.llmusage.monitor.data.repository.DashboardAggregate
import com.llmusage.monitor.util.CurrencyFormatter
import com.llmusage.monitor.util.TimeUtils
import kotlinx.coroutines.flow.first

class UsageWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val container = (context.applicationContext as LLMUsageApp).container
        val agg = container.usageRepository.observeDashboard().first()
        val refreshedAt = System.currentTimeMillis()

        provideContent {
            GlanceTheme {
                WidgetContent(
                    tokensToday = agg.today.total,
                    spendToday = agg.today.spendUsd,
                    quotaPercent = bestQuota(agg),
                    refreshedAt = refreshedAt
                )
            }
        }
    }

    /**
     * Use the *highest* configured-provider quota percent so the widget
     * surfaces the user's most-at-risk provider first.
     */
    private fun bestQuota(agg: DashboardAggregate): Float? =
        agg.perProviderMonth.values.mapNotNull { it.quotaPercent }.maxOrNull()
}

@Composable
private fun WidgetContent(
    tokensToday: Long,
    spendToday: Double,
    quotaPercent: Float?,
    refreshedAt: Long
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(16.dp)
            .cornerRadius(20.dp)
            .background(Color(0xFF0F1424)),
        verticalAlignment = Alignment.Top,
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "LLM Usage",
            style = TextStyle(
                color = ColorProvider(Color(0xFF9AA3B8)),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        )
        Spacer(GlanceModifier.height(6.dp))
        Text(
            text = CurrencyFormatter.format(spendToday),
            style = TextStyle(
                color = ColorProvider(Color(0xFFE6EAF2)),
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = "${CurrencyFormatter.formatTokens(tokensToday)} tokens today",
            style = TextStyle(
                color = ColorProvider(Color(0xFF9AA3B8)),
                fontSize = 12.sp
            )
        )
        Spacer(GlanceModifier.height(8.dp))
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            Text(
                text = quotaPercent?.let { "Quota ${(it * 100).toInt()}%" } ?: "No quota",
                style = TextStyle(
                    color = ColorProvider(Color(0xFF7AB8FF)),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
        Spacer(GlanceModifier.height(4.dp))
        Text(
            text = "Refreshed ${TimeUtils.localFormat(refreshedAt)}",
            style = TextStyle(
                color = ColorProvider(Color(0xFF5B637A)),
                fontSize = 10.sp
            )
        )
    }
}

class UsageWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = UsageWidget()
}
