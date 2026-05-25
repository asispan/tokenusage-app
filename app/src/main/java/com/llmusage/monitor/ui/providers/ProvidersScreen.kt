package com.llmusage.monitor.ui.providers

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.util.TimeUtils
import com.llmusage.monitor.util.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProvidersScreen(container: AppContainer, onOpen: (String) -> Unit) {
    val vm: ProvidersViewModel = viewModel(factory = simpleFactory { ProvidersViewModel(container) })
    val state by vm.state.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Providers") }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 12.dp)
        ) {
            items(state.items, key = { it.type.id }) { row ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpen(row.type.id) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(row.type.displayName, style = MaterialTheme.typography.titleMedium)
                            Switch(
                                checked = row.enabled,
                                onCheckedChange = { vm.setEnabled(row.type.id, it) },
                                enabled = row.configured
                            )
                        }
                        Text(
                            buildString {
                                append(if (row.configured) "Configured" else "Not configured")
                                append(" · last synced ")
                                append(TimeUtils.localFormat(row.lastSyncMillis))
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        if (row.lastError != null) {
                            Text(
                                row.lastError,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
