package com.llmusage.monitor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.llmusage.monitor.data.prefs.ThemeMode
import com.llmusage.monitor.ui.AppRoot
import com.llmusage.monitor.ui.theme.LLMUsageMonitorTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val container = (application as LLMUsageApp).container

        setContent {
            val mode by container.settings.themeMode.collectAsState(initial = ThemeMode.SYSTEM)
            LLMUsageMonitorTheme(themeMode = mode) {
                AppRoot(container = container)
            }
        }
    }
}
