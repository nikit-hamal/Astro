package com.astro.storm.ui.screen.chartdetail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.Timeline
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Navigation tabs for the ChartDetail screen.
 * Each tab represents a different aspect of Vedic chart analysis.
 */
enum class ChartTab(
    val title: String,
    val icon: ImageVector,
    val description: String
) {
    /**
     * Main chart view with divisional charts and planetary positions.
     */
    CHART(
        title = "Chart",
        icon = Icons.Outlined.GridView,
        description = "Rashi and divisional charts"
    ),

    /**
     * Detailed planetary positions with nakshatra and conditions.
     */
    PLANETS(
        title = "Planets",
        icon = Icons.Outlined.Star,
        description = "Planetary positions and conditions"
    ),

    /**
     * Yoga analysis showing all detected yogas.
     */
    YOGAS(
        title = "Yogas",
        icon = Icons.Outlined.AutoAwesome,
        description = "Raja, Dhana, and other yogas"
    ),

    /**
     * Ashtakavarga strength analysis.
     */
    ASHTAKAVARGA(
        title = "Ashtakavarga",
        icon = Icons.Outlined.GridOn,
        description = "SAV and BAV analysis"
    ),

    /**
     * Transit analysis with current planetary positions.
     */
    TRANSITS(
        title = "Transits",
        icon = Icons.Outlined.Schedule,
        description = "Current transit effects"
    ),

    /**
     * Dasha timeline and current periods.
     */
    DASHAS(
        title = "Dashas",
        icon = Icons.Outlined.Timeline,
        description = "Vimshottari dasha timeline"
    ),

    /**
     * Panchanga elements for the birth time.
     */
    PANCHANGA(
        title = "Panchanga",
        icon = Icons.Outlined.WbSunny,
        description = "Tithi, Nakshatra, Yoga, Karana, Vara"
    );

    companion object {
        /**
         * Returns the default tab to display.
         */
        val DEFAULT = CHART

        /**
         * Returns all tabs in display order.
         */
        fun getOrderedTabs(): List<ChartTab> = entries.toList()
    }
}
