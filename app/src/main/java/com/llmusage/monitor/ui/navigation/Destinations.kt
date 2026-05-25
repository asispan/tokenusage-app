package com.llmusage.monitor.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelDestination(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    Dashboard("dashboard", "Dashboard", Icons.Outlined.Dashboard),
    History("history", "History", Icons.Outlined.History),
    Providers("providers", "Providers", Icons.Outlined.BarChart),
    Alerts("alerts", "Alerts", Icons.Outlined.Notifications),
    Settings("settings", "Settings", Icons.Outlined.Settings)
}

object Routes {
    const val OnboardingRoute = "onboarding"
    const val ProviderDetail = "provider/{providerId}"
    fun providerDetail(providerId: String) = "provider/$providerId"
}
