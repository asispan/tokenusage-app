package com.llmusage.monitor.ui.alerts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.util.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsScreen(container: AppContainer) {
    val vm: AlertsViewModel = viewModel(factory = simpleFactory { AlertsViewModel(container) })
    val state by vm.state.collectAsState()
    var menuOpen by remember { mutableStateOf(false) }
    var selectedProvider by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Alerts") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text(
                "Get notified when a provider crosses a quota threshold. " +
                    "Each threshold fires at most once per day.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Add a threshold", style = MaterialTheme.typography.titleMedium)
                    Row(modifier = Modifier.padding(top = 8.dp)) {
                        TextButton(onClick = { menuOpen = true }) {
                            Text(selectedProvider?.let { ProviderType.fromId(it).displayName } ?: "Pick provider")
                        }
                        DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                            state.providerIds.forEach { id ->
                                DropdownMenuItem(
                                    text = { Text(ProviderType.fromId(id).displayName) },
                                    onClick = { selectedProvider = id; menuOpen = false }
                                )
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(50, 75, 90, 100).forEach { pct ->
                            AssistChip(
                                onClick = { selectedProvider?.let { vm.addAlert(it, pct) } },
                                label = { Text("$pct%") }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(top = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.alerts, key = { it.id }) { alert ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "${ProviderType.fromId(alert.providerId).displayName} @ ${alert.thresholdPercent}%",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    if (alert.enabled) "Active" else "Disabled",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Switch(checked = alert.enabled, onCheckedChange = { vm.toggle(alert, it) })
                                IconButton(onClick = { vm.delete(alert.id) }) {
                                    Icon(Icons.Outlined.Delete, contentDescription = "Delete")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
