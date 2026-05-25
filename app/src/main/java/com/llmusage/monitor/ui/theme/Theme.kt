package com.llmusage.monitor.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.llmusage.monitor.data.prefs.ThemeMode

private val DarkScheme = darkColorScheme(
    primary = SeedBlue,
    onPrimary = BgDark,
    primaryContainer = SeedBlueDeep,
    onPrimaryContainer = OnDark,
    secondary = SeedBlue,
    background = BgDark,
    onBackground = OnDark,
    surface = SurfaceDark,
    onSurface = OnDark,
    surfaceVariant = SurfaceDarkElev,
    onSurfaceVariant = OnDarkMuted,
    outline = DividerDark,
    error = DangerRed
)

private val LightScheme = lightColorScheme(
    primary = SeedBlueDeep,
    onPrimary = SurfaceLight,
    primaryContainer = SeedBlue,
    onPrimaryContainer = OnLight,
    secondary = SeedBlueDeep,
    background = BgLight,
    onBackground = OnLight,
    surface = SurfaceLight,
    onSurface = OnLight,
    surfaceVariant = SurfaceLightElev,
    onSurfaceVariant = OnLightMuted,
    outline = DividerLight,
    error = DangerRed
)

@Composable
fun LLMUsageMonitorTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    dynamicColor: Boolean = false, // off by default — we want a consistent brand
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }
    val context = LocalContext.current
    val colors = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> DarkScheme
        else -> LightScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !useDark
        }
    }

    MaterialTheme(
        colorScheme = colors,
        typography = AppTypography,
        content = content
    )
}
