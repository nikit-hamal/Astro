package com.astro.storm.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.astro.storm.ui.navigation.Screen
import com.astro.storm.ui.viewmodel.ChartViewModel

@Composable
fun AppScreen(
    viewModel: ChartViewModel = viewModel()
) {
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                val items = listOf(
                    Screen.Home,
                    Screen.Insights,
                    Screen.Settings
                )
                items.forEach { screen ->
                    NavigationBarItem(
                        icon = {
                            when (screen) {
                                Screen.Home -> Icon(Icons.Default.Home, contentDescription = null)
                                Screen.Insights -> Icon(Icons.Default.Info, contentDescription = null)
                                Screen.Settings -> Icon(Icons.Default.Settings, contentDescription = null)
                            }
                        },
                        label = { Text(screen.route.replaceFirstChar { it.uppercase() }) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController,
            startDestination = Screen.Home.route,
            Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToChartInput = {
                        navController.navigate(Screen.ChartInput.route)
                    },
                    onNavigateToChartDetail = { chartId ->
                        navController.navigate(Screen.ChartDetail.createRoute(chartId))
                    }
                )
            }
            composable(Screen.Insights.route) { InsightsScreen() }
            composable(Screen.Settings.route) { SettingsScreen() }
            composable(Screen.ChartInput.route) {
                ChartInputScreen(
                    viewModel = viewModel,
                    onNavigateBack = {
                        navController.popBackStack()
                    },
                    onChartCalculated = {
                        navController.popBackStack()
                    }
                )
            }
            composable(
                route = Screen.ChartDetail.route,
                arguments = listOf(navArgument("chartId") { type = NavType.LongType })
            ) { backStackEntry ->
                val chartId = backStackEntry.arguments?.getLong("chartId") ?: return@composable
                ChartDetailScreen(
                    viewModel = viewModel,
                    chartId = chartId,
                    onNavigateBack = {
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}