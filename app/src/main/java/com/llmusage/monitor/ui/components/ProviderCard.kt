package com.llmusage.monitor.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.domain.model.UsageData
import com.llmusage.monitor.util.CurrencyFormatter

@Composable
fun ProviderCard(
    type: ProviderType,
    usage: UsageData?,
    lastSyncedLabel: String,
    errorMessage: String?,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(type.displayName, style = MaterialTheme.typography.titleMedium)
                Text(
                    if (usage != null) CurrencyFormatter.format(usage.estimatedCostUsd)
                    else "—",
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (errorMessage != null) {
                Text(
                    errorMessage,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            Text(
                buildString {
                    if (usage != null) {
                        append(CurrencyFormatter.formatTokens(usage.totalTokens))
                        append(" tokens · ")
                    }
                    append(lastSyncedLabel)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )

            if (usage?.quotaPercent != null) {
                QuotaBar(
                    label = "Quota",
                    valueLabel = "${(usage.quotaPercent!! * 100).toInt()}%",
                    progress = usage.quotaPercent,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}
