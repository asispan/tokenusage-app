package com.llmusage.monitor.ui.providers

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.domain.model.ProviderType
import com.llmusage.monitor.util.simpleFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderDetailScreen(providerId: String, container: AppContainer, onBack: () -> Unit) {
    val vm: ProviderDetailViewModel = viewModel(
        factory = simpleFactory { ProviderDetailViewModel(container, providerId) }
    )
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.type.displayName) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PrivacyCard()

            if (state.type != ProviderType.MANUAL) {
                ApiKeyCard(state = state, vm = vm)
            }

            ManualEntryCard(vm = vm)

            OutlinedButton(onClick = { vm.deleteProviderData() }, modifier = Modifier.fillMaxWidth()) {
                Text("Clear stored usage for this provider")
            }
        }
    }
}

@Composable
private fun PrivacyCard() {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Privacy", style = MaterialTheme.typography.titleMedium)
            Text(
                "Your API key is stored locally using Android Keystore-backed AES/GCM " +
                    "encryption. It is sent only to the provider you authenticate with, " +
                    "directly from this device.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun ApiKeyCard(state: ProviderDetailUiState, vm: ProviderDetailViewModel) {
    var keyInput by rememberSaveable { mutableStateOf("") }
    var extras by rememberSaveable { mutableStateOf("") }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (state.hasKey) "API key on file" else "Add API key", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it },
                label = { Text("API key") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            if (state.type == ProviderType.OPENAI) {
                OutlinedTextField(
                    value = extras,
                    onValueChange = { extras = it },
                    label = { Text("Organization ID (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val extrasJson = if (state.type == ProviderType.OPENAI && extras.isNotBlank())
                            """{"organization":"${extras.trim()}"}""" else null
                        vm.saveKey(keyInput.trim(), extrasJson)
                        keyInput = ""
                    },
                    enabled = keyInput.isNotBlank(),
                    modifier = Modifier.weight(1f)
                ) { Text("Save key") }
                OutlinedButton(
                    onClick = vm::testConnection,
                    enabled = state.hasKey && !state.testing,
                    modifier = Modifier.weight(1f)
                ) { Text(if (state.testing) "Testing…" else "Test connection") }
            }
            if (state.hasKey) {
                TextButton(onClick = vm::deleteKey) { Text("Delete key") }
            }
            state.testResult?.let { msg ->
                Text(
                    msg,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (state.testIsError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            state.unsupportedHint?.let { hint ->
                Text(
                    hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ManualEntryCard(vm: ProviderDetailViewModel) {
    var inputTokens by rememberSaveable { mutableStateOf("") }
    var outputTokens by rememberSaveable { mutableStateOf("") }
    var costStr by rememberSaveable { mutableStateOf("") }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Log usage manually", style = MaterialTheme.typography.titleMedium)
            Text(
                "Records or overwrites today's snapshot for this provider.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputTokens, onValueChange = { inputTokens = it.filter(Char::isDigit) },
                    label = { Text("Input tokens") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = outputTokens, onValueChange = { outputTokens = it.filter(Char::isDigit) },
                    label = { Text("Output tokens") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true, modifier = Modifier.weight(1f)
                )
            }
            OutlinedTextField(
                value = costStr,
                onValueChange = { costStr = it.filter { ch -> ch.isDigit() || ch == '.' } },
                label = { Text("Cost (USD, optional)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            )
            Button(
                onClick = {
                    vm.logManual(
                        inputTokens = inputTokens.toLongOrNull() ?: 0L,
                        outputTokens = outputTokens.toLongOrNull() ?: 0L,
                        estimatedCostUsd = costStr.toDoubleOrNull() ?: 0.0
                    )
                    inputTokens = ""; outputTokens = ""; costStr = ""
                },
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp)
            ) { Text("Log") }
        }
    }
}
