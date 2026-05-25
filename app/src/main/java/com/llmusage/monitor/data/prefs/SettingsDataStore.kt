package com.llmusage.monitor.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "settings")

enum class ThemeMode { SYSTEM, LIGHT, DARK }

/**
 * Lightweight user-settings store. Anything sensitive (API keys) lives in
 * Room + Keystore — DataStore is for plain preferences only.
 */
class SettingsDataStore(private val context: Context) {

    private object Keys {
        val Currency = stringPreferencesKey("currency")
        val RefreshIntervalMin = intPreferencesKey("refresh_interval_min")
        val ThemeMode = stringPreferencesKey("theme_mode")
        val OnboardingComplete = booleanPreferencesKey("onboarding_complete")
        val NotificationsEnabled = booleanPreferencesKey("notifications_enabled")
    }

    val currency: Flow<String> = context.dataStore.data.map { it[Keys.Currency] ?: "USD" }

    val refreshIntervalMinutes: Flow<Int> =
        context.dataStore.data.map { it[Keys.RefreshIntervalMin] ?: 60 }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map {
        runCatching { ThemeMode.valueOf(it[Keys.ThemeMode] ?: ThemeMode.SYSTEM.name) }
            .getOrDefault(ThemeMode.SYSTEM)
    }

    val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.OnboardingComplete] ?: false }

    val notificationsEnabled: Flow<Boolean> =
        context.dataStore.data.map { it[Keys.NotificationsEnabled] ?: true }

    suspend fun setCurrency(value: String) { context.dataStore.edit { it[Keys.Currency] = value } }
    suspend fun setRefreshInterval(minutes: Int) { context.dataStore.edit { it[Keys.RefreshIntervalMin] = minutes } }
    suspend fun setThemeMode(mode: ThemeMode) { context.dataStore.edit { it[Keys.ThemeMode] = mode.name } }
    suspend fun setOnboardingComplete(complete: Boolean) { context.dataStore.edit { it[Keys.OnboardingComplete] = complete } }
    suspend fun setNotificationsEnabled(enabled: Boolean) { context.dataStore.edit { it[Keys.NotificationsEnabled] = enabled } }

    suspend fun clearAll() { context.dataStore.edit { it.clear() } }
}
