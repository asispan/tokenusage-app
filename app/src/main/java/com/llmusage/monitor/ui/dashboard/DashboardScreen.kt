package com.llmusage.monitor.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.ui.components.EmptyState
import com.llmusage.monitor.ui.components.LoadingState
import com.llmusage.monitor.ui.components.ProviderCard
import com.llmusage.monitor.ui.components.StatCard
import com.llmusage.monitor.util.CurrencyFormatter
import com.llmusage.monitor.util.TimeUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(container: AppContainer, onAddProvider: () -> Unit) {
    val vm: DashboardViewModel = viewModel(factory = simpleFactory { DashboardViewModel(container) })
    val state by vm.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("Dashboard") }) }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = state.isRefreshing,
            onRefresh = vm::refresh,
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            val agg = state.aggregate
            if (agg == null) {
                LoadingState()
                return@PullToRefreshBox
            }

            val configuredProviders = state.providers.filter { it.encryptedApiKey != null || it.providerId == ProviderType.MANUAL.id }
            if (configuredProviders.isEmpty()) {
                EmptyState(
                    title = "No providers configured yet",
                    subtitle = "Add an OpenAI, Anthropic, OpenRouter, or Gemini key to start tracking, or use the manual provider.",
                    ctaLabel = "Add a provider",
                    onCta = onAddProvider
                )
                return@PullToRefreshBox
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatCard(
                            label = "Today",
                            value = CurrencyFormatter.format(agg.today.spendUsd),
                            helper = "${CurrencyFormatter.formatTokens(agg.today.total)} tokens",
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            label = "Week",
                            value = CurrencyFormatter.format(agg.week.spendUsd),
                            helper = "${CurrencyFormatter.formatTokens(agg.week.total)} tokens",
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                item {
                    StatCard(
                        label = "Last 30 days",
                        value = CurrencyFormatter.format(agg.month.spendUsd),
                        helper = "${CurrencyFormatter.formatTokens(agg.month.total)} tokens · estimates",
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                item {
                    Text(
                        "Providers",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                    )
                }
                items(configuredProviders, key = { it.providerId }) { cfg ->
                    val type = ProviderType.fromId(cfg.providerId)
                    val usage = agg.perProviderToday[cfg.providerId]
                    ProviderCard(
                        type = type,
                        usage = usage,
                        lastSyncedLabel = "Last synced ${TimeUtils.localFormat(cfg.lastSyncEpochMillis)}",
                        errorMessage = cfg.lastSyncError.takeIf { cfg.lastSyncSuccess == false },
                        onClick = onAddProvider
                    )
                }
            }
        }
    }
}
