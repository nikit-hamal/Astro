package com.astro.storm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.astro.storm.ui.screen.ChartDetailScreen
import com.astro.storm.ui.screen.ChartInputScreen
import com.astro.storm.ui.screen.HomeScreen
import com.astro.storm.ui.viewmodel.ChartViewModel

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object ChartInput : Screen("chart_input")
    object ChartDetail : Screen("chart_detail/{chartId}") {
        fun createRoute(chartId: Long) = "chart_detail/$chartId"
    }
}

/**
 * Main navigation graph
 */
@Composable
fun AstroStormNavigation(
    navController: NavHostController,
    viewModel: ChartViewModel = viewModel()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel = viewModel,
                onCreateNewChart = {
                    navController.navigate(Screen.ChartInput.route)
                },
                onChartClick = { chartId ->
                    navController.navigate(Screen.ChartDetail.createRoute(chartId))
                }
            )
        }

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
