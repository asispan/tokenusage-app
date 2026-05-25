package com.llmusage.monitor.ui.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.ui.components.BarChart
import com.llmusage.monitor.ui.components.EmptyState
import com.llmusage.monitor.ui.components.LineChart
import com.llmusage.monitor.util.CurrencyFormatter
import com.llmusage.monitor.util.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(container: AppContainer) {
    val vm: HistoryViewModel = viewModel(factory = simpleFactory { HistoryViewModel(container) })
    val state by vm.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        if (state.dailyTokens.all { it.second == 0L }) {
            EmptyState(
                title = "No usage recorded yet",
                subtitle = "Once your providers sync (or you log usage manually), the last 30 days will appear here."
            )
            return@Scaffold
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ChartCard(title = "Daily tokens (last 30d)") {
                BarChart(values = state.dailyTokens.map { it.second.toFloat() })
            }
            ChartCard(title = "Daily estimated cost (USD)") {
                LineChart(values = state.dailyCostUsd.map { it.second.toFloat() })
            }
            ChartCard(title = "Provider breakdown (tokens)") {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    state.perProviderTotals.forEach { (id, tokens) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(ProviderType.fromId(id).displayName)
                            Text(CurrencyFormatter.formatTokens(tokens))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            content()
        }
    }
}
