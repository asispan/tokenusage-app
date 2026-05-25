package com.llmusage.monitor.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.llmusage.monitor.AppContainer
import com.llmusage.monitor.ui.alerts.AlertsScreen
import com.llmusage.monitor.ui.dashboard.DashboardScreen
import com.llmusage.monitor.ui.history.HistoryScreen
import com.llmusage.monitor.ui.navigation.Routes
import com.llmusage.monitor.ui.navigation.TopLevelDestination
import com.llmusage.monitor.ui.onboarding.OnboardingScreen
import com.llmusage.monitor.ui.providers.ProviderDetailScreen
import com.llmusage.monitor.ui.providers.ProvidersScreen
import com.llmusage.monitor.ui.settings.SettingsScreen

@Composable
fun AppRoot(container: AppContainer) {
    val nav = rememberNavController()
    val backStack by nav.currentBackStackEntryAsState()
    val currentRoute = backStack?.destination?.route

    val onboarded by container.settings.onboardingComplete.collectAsState(initial = true)
    val startDestination = if (onboarded) TopLevelDestination.Dashboard.route else Routes.OnboardingRoute

    Scaffold(
        bottomBar = {
            if (currentRoute in TopLevelDestination.values().map { it.route }) {
                NavigationBar {
                    TopLevelDestination.values().forEach { dest ->
                        NavigationBarItem(
                            selected = currentRoute == dest.route,
                            onClick = {
                                nav.navigate(dest.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(dest.icon, contentDescription = dest.label) },
                            label = { Text(dest.label) }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nav,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Routes.OnboardingRoute) {
                OnboardingScreen(
                    onDone = {
                        nav.navigate(TopLevelDestination.Dashboard.route) {
                            popUpTo(Routes.OnboardingRoute) { inclusive = true }
                        }
                    },
                    container = container
                )
            }
            composable(TopLevelDestination.Dashboard.route) {
                DashboardScreen(container = container, onAddProvider = {
                    nav.navigate(TopLevelDestination.Providers.route)
                })
            }
            composable(TopLevelDestination.History.route) {
                HistoryScreen(container = container)
            }
            composable(TopLevelDestination.Providers.route) {
                ProvidersScreen(container = container, onOpen = { id ->
                    nav.navigate(Routes.providerDetail(id))
                })
            }
            composable(TopLevelDestination.Alerts.route) {
                AlertsScreen(container = container)
            }
            composable(TopLevelDestination.Settings.route) {
                SettingsScreen(container = container)
            }
            composable(Routes.ProviderDetail) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("providerId").orEmpty()
                ProviderDetailScreen(providerId = id, container = container, onBack = { nav.popBackStack() })
            }
        }
    }
}
