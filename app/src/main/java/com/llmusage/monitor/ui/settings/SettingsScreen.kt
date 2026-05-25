package com.llmusage.monitor.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.data.prefs.ThemeMode
import com.llmusage.monitor.util.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(container: AppContainer) {
    val vm: SettingsViewModel = viewModel(factory = simpleFactory { SettingsViewModel(container) })
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    var confirmReset by remember { mutableStateOf(false) }
    var lastExport by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SettingCard(title = "Currency") {
                OutlinedTextField(
                    value = state.currency,
                    onValueChange = vm::setCurrency,
                    singleLine = true,
                    label = { Text("ISO 4217 code") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Note: figures are not FX-converted in v1 — the symbol changes but values remain in USD.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            SettingCard(title = "Refresh interval") {
                var interval by remember(state.refreshIntervalMinutes) { mutableStateOf(state.refreshIntervalMinutes.toString()) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = interval,
                        onValueChange = { interval = it.filter(Char::isDigit) },
                        label = { Text("Minutes (min 15)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedButton(onClick = {
                        val v = interval.toIntOrNull()?.coerceAtLeast(15) ?: 60
                        vm.setRefreshInterval(v)
                    }) { Text("Apply") }
                }
            }
            SettingCard(title = "Theme") {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ThemeMode.values().forEach { mode ->
                        AssistChip(
                            onClick = { vm.setThemeMode(mode) },
                            label = { Text(mode.name.lowercase().replaceFirstChar { it.titlecase() }) }
                        )
                    }
                }
                Text(
                    "Current: ${state.themeMode.name.lowercase()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            SettingCard(title = "Notifications") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Threshold alerts")
                    Switch(checked = state.notificationsEnabled, onCheckedChange = vm::setNotificationsEnabled)
                }
            }
            SettingCard(title = "Export") {
                OutlinedButton(
                    onClick = { vm.exportCsv(context) { lastExport = it } },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Export usage as CSV") }
                lastExport?.let {
                    Text(
                        "Saved to: $it",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            SettingCard(title = "Danger zone") {
                OutlinedButton(
                    onClick = { confirmReset = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset all data") }
                Text(
                    "Wipes all stored usage, provider keys, and preferences from this device. " +
                        "This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset everything?") },
            text = { Text("All keys, usage history, and settings will be erased from this device.") },
            confirmButton = {
                TextButton(onClick = {
                    vm.resetAllData(); confirmReset = false
                }) { Text("Reset") }
            },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingCard(title: String, content: @Composable () -> Unit) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Column(modifier = Modifier.padding(top = 8.dp)) { content() }
        }
    }
}
