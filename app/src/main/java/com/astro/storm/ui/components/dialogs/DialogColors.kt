package com.astro.storm.ui.components.dialogs

import androidx.compose.ui.graphics.Color
import com.astro.storm.data.model.Planet

/**
 * Centralized color palette for dialog components.
 * Provides consistent styling across all chart-related dialogs.
 */
object DialogColors {
    // Dialog backgrounds
    val DialogBackground = Color(0xFF1A1A1A)
    val DialogSurface = Color(0xFF252525)
    val DialogSurfaceElevated = Color(0xFF2D2D2D)

    // Accent colors
    val AccentGold = Color(0xFFD4AF37)
    val AccentTeal = Color(0xFF4DB6AC)
    val AccentPurple = Color(0xFF9575CD)
    val AccentRose = Color(0xFFE57373)
    val AccentBlue = Color(0xFF64B5F6)
    val AccentGreen = Color(0xFF81C784)
    val AccentOrange = Color(0xFFFFB74D)

    // Text colors
    val TextPrimary = Color(0xFFF5F5F5)
    val TextSecondary = Color(0xFFB0B0B0)
    val TextMuted = Color(0xFF757575)

    // Divider
    val DividerColor = Color(0xFF333333)

    // Planet colors
    val planetColors: Map<Planet, Color> = mapOf(
        Planet.SUN to Color(0xFFD2691E),
        Planet.MOON to Color(0xFFDC143C),
        Planet.MARS to Color(0xFFDC143C),
        Planet.MERCURY to Color(0xFF228B22),
        Planet.JUPITER to Color(0xFFDAA520),
        Planet.VENUS to Color(0xFF9370DB),
        Planet.SATURN to Color(0xFF4169E1),
        Planet.RAHU to Color(0xFF8B0000),
        Planet.KETU to Color(0xFF8B0000),
        Planet.URANUS to Color(0xFF20B2AA),
        Planet.NEPTUNE to Color(0xFF4682B4),
        Planet.PLUTO to Color(0xFF800080)
    )

    /**
     * Gets the color for a specific planet.
     */
    fun getPlanetColor(planet: Planet): Color = planetColors[planet] ?: AccentGold

    /**
     * Gets the color for strength-based indicators.
     */
    fun getStrengthColor(percentage: Double): Color = when {
        percentage >= 100 -> AccentGreen
        percentage >= 85 -> AccentOrange
        else -> AccentRose
    }
}
