package com.autotrader.ahh.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.autotrader.ahh.data.model.PairingPayload
import com.autotrader.ahh.data.model.ServerEvent
import com.autotrader.ahh.data.repository.TraderRepository
import com.autotrader.ahh.ui.components.LiveEventBanner
import com.autotrader.ahh.ui.screens.activity.ActivityScreen
import com.autotrader.ahh.ui.screens.activity.ActivityViewModel
import com.autotrader.ahh.ui.screens.connect.ConnectScreen
import com.autotrader.ahh.ui.screens.connect.ConnectViewModel
import com.autotrader.ahh.ui.screens.dashboard.DashboardScreen
import com.autotrader.ahh.ui.screens.dashboard.DashboardViewModel
import com.autotrader.ahh.ui.screens.logs.LogsScreen
import com.autotrader.ahh.ui.screens.logs.LogsViewModel
import com.autotrader.ahh.ui.screens.strategies.StrategiesScreen
import com.autotrader.ahh.ui.screens.strategies.StrategiesViewModel
import com.autotrader.ahh.ui.theme.BgPrimary
import com.autotrader.ahh.ui.theme.BgSurface
import com.autotrader.ahh.ui.theme.EmeraldPrimary
import com.autotrader.ahh.ui.theme.EmeraldSoft
import com.autotrader.ahh.ui.theme.TextMuted
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch

sealed class Route(val path: String, val label: String, val icon: ImageVector? = null) {
    data object Connect : Route("connect", "Connect")
    data object Dashboard : Route("dashboard", "Dashboard", Icons.Default.Dashboard)
    data object Activity : Route("activity", "Activity", Icons.Default.Assessment)
    data object Strategies : Route("strategies", "Strategies", Icons.Default.Tune)
    data object Logs : Route("logs", "Console", Icons.Default.Terminal)
}

private val bottomTabs = listOf(
    Route.Dashboard,
    Route.Activity,
    Route.Strategies,
    Route.Logs
)

@Composable
fun AutoTraderNavHost(
    repository: TraderRepository,
    startConnected: Boolean,
    pendingPairing: PairingPayload?,
    onPairingConsumed: () -> Unit
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = currentRoute != Route.Connect.path

    val activeUrl by repository.activeBaseUrl.collectAsStateWithLifecycle(initialValue = "")
    var latestEvent by remember { mutableStateOf<ServerEvent?>(null) }

    // Stream real-time events from PC whenever connected
    LaunchedEffect(activeUrl) {
        if (activeUrl.isNotBlank()) {
            while (true) {
                repository.streamEvents()
                    .catch { delay(5000) }
                    .collect { evt ->
                        if (evt.type != "sys") {
                            latestEvent = evt
                        }
                    }
                delay(4000)
            }
        }
    }

    // Handle deep link pairing URI when app receives autotrader://connect?...
    val connectViewModel: ConnectViewModel = viewModel(factory = ConnectViewModel.Factory(repository))
    LaunchedEffect(pendingPairing) {
        if (pendingPairing != null) {
            connectViewModel.applyPairingPayload(pendingPairing)
            onPairingConsumed()
            navController.navigate(Route.Connect.path) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        containerColor = BgPrimary,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(containerColor = BgSurface) {
                    bottomTabs.forEach { tab ->
                        val selected = currentRoute == tab.path
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(tab.path) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                tab.icon?.let { Icon(it, contentDescription = tab.label) }
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = EmeraldPrimary,
                                selectedTextColor = EmeraldPrimary,
                                indicatorColor = EmeraldSoft,
                                unselectedIconColor = TextMuted,
                                unselectedTextColor = TextMuted
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (showBottomBar) {
                LiveEventBanner(
                    event = latestEvent,
                    onDismiss = { latestEvent = null }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                NavHost(
                    navController = navController,
                    startDestination = if (startConnected) Route.Dashboard.path else Route.Connect.path
                ) {
                    composable(Route.Connect.path) {
                        ConnectScreen(
                            viewModel = connectViewModel,
                            onConnected = {
                                navController.navigate(Route.Dashboard.path) {
                                    popUpTo(Route.Connect.path) { inclusive = true }
                                    launchSingleTop = true
                                }
                            },
                            onBack = if (navController.previousBackStackEntry != null) {
                                { navController.popBackStack() }
                            } else null
                        )
                    }

                    composable(Route.Dashboard.path) {
                        val vm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(repository))
                        DashboardScreen(
                            viewModel = vm,
                            onOpenConnection = {
                                navController.navigate(Route.Connect.path) { launchSingleTop = true }
                            }
                        )
                    }

                    composable(Route.Activity.path) {
                        val vm: ActivityViewModel = viewModel(factory = ActivityViewModel.Factory(repository))
                        ActivityScreen(viewModel = vm)
                    }

                    composable(Route.Strategies.path) {
                        val vm: StrategiesViewModel = viewModel(factory = StrategiesViewModel.Factory(repository))
                        StrategiesScreen(viewModel = vm)
                    }

                    composable(Route.Logs.path) {
                        val vm: LogsViewModel = viewModel(factory = LogsViewModel.Factory(repository))
                        LogsScreen(
                            viewModel = vm,
                            onSwitchPc = {
                                navController.navigate(Route.Connect.path) { launchSingleTop = true }
                            },
                            onDisconnected = {
                                navController.navigate(Route.Connect.path) {
                                    popUpTo(navController.graph.id) { inclusive = true }
                                    launchSingleTop = true
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
