package com.astro.storm.ui.navigation

/**
 * Navigation routes
 */
sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Insights : Screen("insights")
    object Settings : Screen("settings")
    object ChartInput : Screen("chart_input")
    object ChartDetail : Screen("chart_detail/{chartId}") {
        fun createRoute(chartId: Long) = "chart_detail/$chartId"
    }
}