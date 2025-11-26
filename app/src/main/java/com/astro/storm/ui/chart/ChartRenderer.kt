package com.astro.storm.ui.chart

import android.graphics.Bitmap
import android.graphics.Typeface
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import kotlin.math.abs
import kotlin.math.min

/**
 * Professional North Indian Style Vedic Chart Renderer
 *
 * Authentic North Indian chart format matching traditional Vedic astrology software (AstroSage style):
 * - SQUARE outer boundary
 * - Central diamond created by connecting midpoints of sides
 * - Full corner-to-corner diagonals creating proper 12-house divisions
 * - 12 houses in traditional layout
 * - Planet positions with degree superscripts
 * - Status indicators matching AstroSage: * (Retrograde), ^ (Combust), ¤ (Vargottama), ↑ (Exalted), ↓ (Debilitated)
 *
 * Standard North Indian Chart Layout (Houses numbered 1-12):
 * Ascendant is ALWAYS in the top center diamond (House 1)
 * Signs rotate through houses based on rising sign
 *
 * Visual Reference (matching AstroSage):
 *      ┌─────────────────────────────────────────┐
 *      │ ╲                              ╱        │
 *      │   ╲           12           ╱     1     │
 *      │     ╲                   ╱              │
 *      │  11   ╲               ╱          2    │
 *      │         ╲           ╱                  │
 *      │───────────╲       ╱───────────────────│
 *      │             ╲   ╱                      │
 *      │  10          ╲╱          3            │
 *      │              ╱╲                        │
 *      │───────────╱    ╲──────────────────────│
 *      │         ╱        ╲                     │
 *      │   9   ╱            ╲        4         │
 *      │     ╱                ╲                 │
 *      │   ╱         8          ╲     5        │
 *      │ ╱                        ╲             │
 *      │╱              7            ╲          │
 *      └─────────────────────────────────────────┘
 *
 * The chart has:
 * - Outer square border
 * - Central diamond (connecting midpoints of sides)
 * - Two corner-to-corner diagonals
 * This creates 12 distinct triangular houses
 */
class ChartRenderer {

    companion object {
        // Professional color palette matching traditional Vedic astrology software
        private val BACKGROUND_COLOR = Color(0xFFD4C4A8) // Warm parchment background
        private val CHART_BACKGROUND = Color(0xFFD4C4A8)
        private val BORDER_COLOR = Color(0xFFB8860B) // Dark goldenrod for lines
        private val HOUSE_NUMBER_COLOR = Color(0xFF4A4A4A) // Dark gray for house numbers
        private val TITLE_COLOR = Color(0xFF8B4513) // Saddle brown for title

        // Planet-specific colors matching AstroSage style
        private val SUN_COLOR = Color(0xFFD2691E) // Chocolate/orange for Sun
        private val MOON_COLOR = Color(0xFFDC143C) // Crimson for Moon
        private val MARS_COLOR = Color(0xFFDC143C) // Red for Mars
        private val MERCURY_COLOR = Color(0xFF228B22) // Forest green for Mercury
        private val JUPITER_COLOR = Color(0xFFDAA520) // Goldenrod for Jupiter
        private val VENUS_COLOR = Color(0xFF9370DB) // Medium purple for Venus
        private val SATURN_COLOR = Color(0xFF4169E1) // Royal blue for Saturn
        private val RAHU_COLOR = Color(0xFF8B0000) // Dark red for Rahu
        private val KETU_COLOR = Color(0xFF8B0000) // Dark red for Ketu
        private val URANUS_COLOR = Color(0xFF20B2AA) // Light sea green for Uranus
        private val NEPTUNE_COLOR = Color(0xFF4682B4) // Steel blue for Neptune
        private val PLUTO_COLOR = Color(0xFF800080) // Purple for Pluto
        private val LAGNA_COLOR = Color(0xFF8B4513) // Saddle brown for Lagna marker

        // Status indicator symbols (matching AstroSage exactly)
        const val SYMBOL_RETROGRADE = "*"       // Retrograde motion
        const val SYMBOL_COMBUST = "^"          // Combust (too close to Sun)
        const val SYMBOL_VARGOTTAMA = "\u00A4"  // ¤ - Vargottama (same sign in D1 and D9)
        const val SYMBOL_EXALTED = "\u2191"     // ↑ - Exalted
        const val SYMBOL_DEBILITATED = "\u2193" // ↓ - Debilitated
    }

    /**
     * Get color for a specific planet
     */
    private fun getPlanetColor(planet: Planet): Color {
        return when (planet) {
            Planet.SUN -> SUN_COLOR
            Planet.MOON -> MOON_COLOR
            Planet.MARS -> MARS_COLOR
            Planet.MERCURY -> MERCURY_COLOR
            Planet.JUPITER -> JUPITER_COLOR
            Planet.VENUS -> VENUS_COLOR
            Planet.SATURN -> SATURN_COLOR
            Planet.RAHU -> RAHU_COLOR
            Planet.KETU -> KETU_COLOR
            Planet.URANUS -> URANUS_COLOR
            Planet.NEPTUNE -> NEPTUNE_COLOR
            Planet.PLUTO -> PLUTO_COLOR
        }
    }

    /**
     * Get abbreviated name for planet (2-3 characters)
     */
    private fun getPlanetAbbreviation(planet: Planet): String {
        return when (planet) {
            Planet.SUN -> "Su"
            Planet.MOON -> "Mo"
            Planet.MARS -> "Ma"
            Planet.MERCURY -> "Me"
            Planet.JUPITER -> "Ju"
            Planet.VENUS -> "Ve"
            Planet.SATURN -> "Sa"
            Planet.RAHU -> "Ra"
            Planet.KETU -> "Ke"
            Planet.URANUS -> "Ur"
            Planet.NEPTUNE -> "Ne"
            Planet.PLUTO -> "Pl"
        }
    }

    /**
     * Convert degree to superscript string
     */
    private fun toSuperscript(degree: Int): String {
        val superscripts = mapOf(
            '0' to '\u2070',
            '1' to '\u00B9',
            '2' to '\u00B2',
            '3' to '\u00B3',
            '4' to '\u2074',
            '5' to '\u2075',
            '6' to '\u2076',
            '7' to '\u2077',
            '8' to '\u2078',
            '9' to '\u2079'
        )
        return degree.toString().map { superscripts[it] ?: it }.joinToString("")
    }

    /**
     * Check if planet is exalted in its current sign
     */
    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            Planet.RAHU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            Planet.KETU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            else -> false
        }
    }

    /**
     * Check if planet is debilitated in its current sign
     */
    private fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LIBRA
            Planet.MOON -> sign == ZodiacSign.SCORPIO
            Planet.MARS -> sign == ZodiacSign.CANCER
            Planet.MERCURY -> sign == ZodiacSign.PISCES
            Planet.JUPITER -> sign == ZodiacSign.CAPRICORN
            Planet.VENUS -> sign == ZodiacSign.VIRGO
            Planet.SATURN -> sign == ZodiacSign.ARIES
            Planet.RAHU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            Planet.KETU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            else -> false
        }
    }

    /**
     * Check if planet is in own sign
     */
    private fun isOwnSign(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LEO
            Planet.MOON -> sign == ZodiacSign.CANCER
            Planet.MARS -> sign == ZodiacSign.ARIES || sign == ZodiacSign.SCORPIO
            Planet.MERCURY -> sign == ZodiacSign.GEMINI || sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.SAGITTARIUS || sign == ZodiacSign.PISCES
            Planet.VENUS -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.LIBRA
            Planet.SATURN -> sign == ZodiacSign.CAPRICORN || sign == ZodiacSign.AQUARIUS
            else -> false
        }
    }

    /**
     * Check if a planet is Vargottama (same sign in D1 Rashi and D9 Navamsa charts)
     * This is a highly favorable position in Vedic astrology
     *
     * @param planet The planet position in the Rashi chart
     * @param chart The full Vedic chart for calculating Navamsa
     * @return true if the planet is in the same sign in both D1 and D9
     */
    private fun isVargottama(planet: PlanetPosition, chart: VedicChart): Boolean {
        // Calculate navamsa position for this planet
        val navamsaLongitude = calculateNavamsaLongitude(planet.longitude)
        val navamsaSign = ZodiacSign.fromLongitude(navamsaLongitude)

        // Vargottama = same sign in D1 (Rashi) and D9 (Navamsa)
        return planet.sign == navamsaSign
    }

    /**
     * Calculate Navamsa longitude for a given longitude
     * Navamsa divides each sign into 9 equal parts of 3°20' each
     */
    private fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val navamsaPart = (degreeInSign / 3.333333333).toInt().coerceIn(0, 8) // 0-8

        val startingSignIndex = when (signNumber % 3) {
            0 -> signNumber              // Movable: start from same sign
            1 -> (signNumber + 8) % 12   // Fixed: start from 9th sign
            2 -> (signNumber + 4) % 12   // Dual: start from 5th sign
            else -> signNumber
        }

        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12

        val positionInNavamsa = degreeInSign % 3.333333333
        val navamsaDegree = (positionInNavamsa / 3.333333333) * 30.0

        return (navamsaSignIndex * 30.0) + navamsaDegree
    }

    /**
     * Check if a planet is combust (too close to the Sun)
     * Combustion weakens a planet's significations
     *
     * Combustion orbs (degrees from Sun):
     * - Moon: 12°
     * - Mars: 17°
     * - Mercury: 14° (12° if retrograde)
     * - Jupiter: 11°
     * - Venus: 10° (8° if retrograde)
     * - Saturn: 15°
     *
     * @param planet The planet position to check
     * @param sunPosition The Sun's position (null if Sun itself)
     * @return true if the planet is combust
     */
    private fun isCombust(planet: PlanetPosition, sunPosition: PlanetPosition?): Boolean {
        // Sun itself cannot be combust
        if (planet.planet == Planet.SUN) return false

        // Rahu, Ketu, and outer planets don't combust
        if (planet.planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return false
        }

        // Need Sun's position to check combustion
        if (sunPosition == null) return false

        val angularDistance = calculateAngularDistance(planet.longitude, sunPosition.longitude)

        // Get combustion orb for this planet
        val combustionOrb = when (planet.planet) {
            Planet.MOON -> 12.0
            Planet.MARS -> 17.0
            Planet.MERCURY -> if (planet.isRetrograde) 12.0 else 14.0
            Planet.JUPITER -> 11.0
            Planet.VENUS -> if (planet.isRetrograde) 8.0 else 10.0
            Planet.SATURN -> 15.0
            else -> 0.0
        }

        return angularDistance <= combustionOrb
    }

    /**
     * Calculate the angular distance between two longitudes
     * Handles the wrap-around at 360°
     */
    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }

    /**
     * Draw a professional North Indian style Vedic chart
     * Matches the layout and style of traditional Vedic astrology software
     */
    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna"
    ) {
        with(drawScope) {
            val padding = size * 0.02f
            val chartSize = size - (padding * 2)
            val left = padding
            val top = padding
            val right = left + chartSize
            val bottom = top + chartSize
            val centerX = (left + right) / 2
            val centerY = (top + bottom) / 2

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw outer square border with thicker stroke
            drawRect(
                color = BORDER_COLOR,
                topLeft = Offset(left, top),
                size = Size(chartSize, chartSize),
                style = Stroke(width = 3f)
            )

            // Draw the internal structure - North Indian diamond pattern
            // This creates the classic 12-house layout with a central diamond

            // Key points for the diamond structure
            val midTop = Offset(centerX, top)
            val midRight = Offset(right, centerY)
            val midBottom = Offset(centerX, bottom)
            val midLeft = Offset(left, centerY)

            // Draw the central diamond by connecting midpoints of sides
            // This creates the iconic North Indian chart diamond in the center
            drawLine(BORDER_COLOR, midTop, midRight, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midRight, midBottom, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midBottom, midLeft, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midLeft, midTop, strokeWidth = 2.5f)

            // Draw the corner-to-corner diagonals (full diagonals, not to center)
            // These divide the corner triangles into 2 houses each
            drawLine(BORDER_COLOR, Offset(left, top), Offset(right, bottom), strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, Offset(right, top), Offset(left, bottom), strokeWidth = 2.5f)

            // Get ascendant sign number for mapping houses to signs
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

            // Draw house numbers and planets with full chart context for status checking
            drawAllHouseContents(
                left, top, chartSize, centerX, centerY,
                ascendantSign, chart.planetPositions, size, chart
            )
        }
    }

    /**
     * Draw a divisional chart (D9, D10, D60, etc.) with planet positions
     */
    fun drawDivisionalChart(
        drawScope: DrawScope,
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        size: Float,
        chartTitle: String,
        originalChart: VedicChart? = null
    ) {
        with(drawScope) {
            val padding = size * 0.02f
            val chartSize = size - (padding * 2)
            val left = padding
            val top = padding
            val right = left + chartSize
            val bottom = top + chartSize
            val centerX = (left + right) / 2
            val centerY = (top + bottom) / 2

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw outer square border
            drawRect(
                color = BORDER_COLOR,
                topLeft = Offset(left, top),
                size = Size(chartSize, chartSize),
                style = Stroke(width = 3f)
            )

            // Draw the internal structure - North Indian diamond pattern
            // This creates the classic 12-house layout with a central diamond

            // Key points for the diamond structure
            val midTop = Offset(centerX, top)
            val midRight = Offset(right, centerY)
            val midBottom = Offset(centerX, bottom)
            val midLeft = Offset(left, centerY)

            // Draw the central diamond by connecting midpoints of sides
            // This creates the iconic North Indian chart diamond in the center
            drawLine(BORDER_COLOR, midTop, midRight, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midRight, midBottom, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midBottom, midLeft, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midLeft, midTop, strokeWidth = 2.5f)

            // Draw the corner-to-corner diagonals (full diagonals, not to center)
            // These divide the corner triangles into 2 houses each
            drawLine(BORDER_COLOR, Offset(left, top), Offset(right, bottom), strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, Offset(right, top), Offset(left, bottom), strokeWidth = 2.5f)

            // Get ascendant sign
            val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)

            // Draw house contents (pass original chart for status checking if available)
            drawAllHouseContents(
                left, top, chartSize, centerX, centerY,
                ascendantSign, planetPositions, size, originalChart
            )
        }
    }

    /**
     * Draw all house contents including house numbers and planets
     *
     * North Indian Chart House Layout (matching AstroSage exactly):
     * The chart is a square with:
     * - Central diamond (connecting midpoints of sides)
     * - Two corner-to-corner diagonals
     * This creates 12 triangular houses.
     *
     * Standard North Indian layout (AstroSage reference):
     *
     *      ┌─────────────────────────────────────────┐
     *      │ ╲              12              ╱        │
     *      │   ╲                         ╱    1     │
     *      │     ╲                     ╱            │
     *      │  11   ╲                 ╱         2   │
     *      │         ╲             ╱                │
     *      │───────────╲─────────╱──────────────────│
     *      │             ╲     ╱                    │
     *      │   10         ╲   ╱         3          │
     *      │               ╲ ╱                      │
     *      │───────────────╳────────────────────────│
     *      │               ╱ ╲                      │
     *      │    9        ╱     ╲        4          │
     *      │           ╱         ╲                  │
     *      │─────────╱─────────────╲────────────────│
     *      │       ╱        8        ╲      5      │
     *      │     ╱                     ╲            │
     *      │   ╱             7           ╲         │
     *      │ ╱                6            ╲       │
     *      └─────────────────────────────────────────┘
     *
     * House 1 is at TOP-RIGHT area (Lagna/Ascendant)
     * House 7 is at BOTTOM-CENTER (opposite to Lagna)
     * Houses proceed counter-clockwise from House 1
     */
    private fun DrawScope.drawAllHouseContents(
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float,
        ascendantSign: ZodiacSign,
        planetPositions: List<PlanetPosition>,
        size: Float,
        chart: VedicChart? = null
    ) {
        val right = left + chartSize
        val bottom = top + chartSize

        // Group planets by house
        val planetsByHouse = planetPositions.groupBy { it.house }

        // Get Sun position for combustion checking
        val sunPosition = chart?.planetPositions?.find { it.planet == Planet.SUN }

        // Draw each house (1-12)
        for (houseNum in 1..12) {
            val houseCenter = getHousePlanetCenter(houseNum, left, top, chartSize, centerX, centerY)
            val numberPos = getHouseNumberPosition(houseNum, left, top, chartSize, centerX, centerY)

            // Draw house number
            drawTextCentered(
                text = houseNum.toString(),
                position = numberPos,
                textSize = size * 0.035f,
                color = HOUSE_NUMBER_COLOR,
                isBold = false
            )

            // Draw Lagna marker in house 1 (matching AstroSage style)
            if (houseNum == 1) {
                // Draw "La" marker for Lagna (Ascendant) in the first house
                val lagnaMarkerPos = Offset(centerX, top + chartSize * 0.18f)
                drawTextCentered(
                    text = "La",
                    position = lagnaMarkerPos,
                    textSize = size * 0.035f,
                    color = LAGNA_COLOR,
                    isBold = true
                )
            }

            // Draw planets in this house
            val planets = planetsByHouse[houseNum] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCenter, size, houseNum, chart, sunPosition)
            }
        }
    }

    /**
     * Get the center position for placing planets in each house
     * This determines where planet text appears within each house section
     *
     * North Indian chart layout (matching AstroSage):
     * The houses are arranged in a specific pattern around the square chart.
     * Looking at the AstroSage reference image:
     *
     * - House 1: Top center area (upper diamond triangle) - this is where Lagna marker goes
     * - House 2: Top right corner triangle
     * - House 3: Right side upper trapezoid
     * - House 4: Right side lower trapezoid
     * - House 5: Bottom right corner triangle
     * - House 6: Bottom center right area
     * - House 7: Bottom center area (lower diamond triangle)
     * - House 8: Bottom center left area
     * - House 9: Bottom left corner triangle
     * - House 10: Left side lower trapezoid
     * - House 11: Left side upper trapezoid
     * - House 12: Top left corner triangle
     */
    private fun getHousePlanetCenter(
        houseNum: Int,
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float
    ): Offset {
        val right = left + chartSize
        val bottom = top + chartSize
        // Divide chart into precise sections for accurate positioning
        val quarterW = chartSize / 4
        val quarterH = chartSize / 4
        val eighthW = chartSize / 8
        val eighthH = chartSize / 8

        return when (houseNum) {
            // House 1: Top center diamond - upper central area
            1 -> Offset(centerX, top + quarterH)

            // House 2: Top right corner triangle - positioned in upper right
            2 -> Offset(right - quarterW, top + quarterH)

            // House 3: Right side upper trapezoid - right middle-upper
            3 -> Offset(right - eighthW * 1.5f, centerY - eighthH)

            // House 4: Right side lower trapezoid - right middle-lower
            4 -> Offset(right - eighthW * 1.5f, centerY + eighthH)

            // House 5: Bottom right corner triangle - lower right
            5 -> Offset(right - quarterW, bottom - quarterH)

            // House 6: Bottom center right - between House 7 and House 5
            6 -> Offset(centerX + quarterW * 0.6f, bottom - quarterH)

            // House 7: Bottom center diamond - lower central area
            7 -> Offset(centerX, bottom - quarterH)

            // House 8: Bottom center left - between House 7 and House 9
            8 -> Offset(centerX - quarterW * 0.6f, bottom - quarterH)

            // House 9: Bottom left corner triangle - lower left
            9 -> Offset(left + quarterW, bottom - quarterH)

            // House 10: Left side lower trapezoid - left middle-lower
            10 -> Offset(left + eighthW * 1.5f, centerY + eighthH)

            // House 11: Left side upper trapezoid - left middle-upper
            11 -> Offset(left + eighthW * 1.5f, centerY - eighthH)

            // House 12: Top left corner triangle - upper left
            12 -> Offset(left + quarterW, top + quarterH)

            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Get position for house number (placed at the outer edges of each house section)
     * Numbers are placed strategically to match AstroSage layout
     *
     * In AstroSage reference:
     * - House 1 number appears at top center near the top edge
     * - House 2 number appears at top right corner
     * - House 3 number appears at right edge upper area
     * - House 4 number appears at right edge lower area
     * - House 5 number appears at bottom right corner
     * - House 6 number appears at bottom edge right of center
     * - House 7 number appears at bottom center
     * - House 8 number appears at bottom edge left of center
     * - House 9 number appears at bottom left corner
     * - House 10 number appears at left edge lower area
     * - House 11 number appears at left edge upper area
     * - House 12 number appears at top left corner
     */
    private fun getHouseNumberPosition(
        houseNum: Int,
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float
    ): Offset {
        val right = left + chartSize
        val bottom = top + chartSize
        // Use consistent margin from edges
        val margin = chartSize * 0.04f
        val quarterW = chartSize / 4
        val quarterH = chartSize / 4

        return when (houseNum) {
            // House 1: Top center - placed at top edge, center
            1 -> Offset(centerX, top + margin * 2)

            // House 2: Top right corner - placed near the top-right corner
            2 -> Offset(right - margin * 2, top + margin * 2)

            // House 3: Right side upper - placed at right edge, above center
            3 -> Offset(right - margin * 1.5f, centerY - quarterH * 0.5f)

            // House 4: Right side lower - placed at right edge, below center
            4 -> Offset(right - margin * 1.5f, centerY + quarterH * 0.5f)

            // House 5: Bottom right corner - placed near the bottom-right corner
            5 -> Offset(right - margin * 2, bottom - margin * 2)

            // House 6: Bottom center right - placed at bottom edge, right of center
            6 -> Offset(centerX + quarterW * 0.5f, bottom - margin * 2)

            // House 7: Bottom center - placed at bottom edge, center
            7 -> Offset(centerX, bottom - margin * 2)

            // House 8: Bottom center left - placed at bottom edge, left of center
            8 -> Offset(centerX - quarterW * 0.5f, bottom - margin * 2)

            // House 9: Bottom left corner - placed near the bottom-left corner
            9 -> Offset(left + margin * 2, bottom - margin * 2)

            // House 10: Left side lower - placed at left edge, below center
            10 -> Offset(left + margin * 1.5f, centerY + quarterH * 0.5f)

            // House 11: Left side upper - placed at left edge, above center
            11 -> Offset(left + margin * 1.5f, centerY - quarterH * 0.5f)

            // House 12: Top left corner - placed near the top-left corner
            12 -> Offset(left + margin * 2, top + margin * 2)

            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Draw planets positioned within a house with degree superscripts and status indicators
     * Layout adjusts based on number of planets to prevent overlap
     *
     * Status indicators (matching AstroSage exactly):
     * - * : Retrograde
     * - ^ : Combust (too close to Sun)
     * - ¤ : Vargottama (same sign in D1 and D9)
     * - ↑ : Exalted
     * - ↓ : Debilitated
     */
    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetPosition>,
        houseCenter: Offset,
        size: Float,
        houseNum: Int,
        chart: VedicChart? = null,
        sunPosition: PlanetPosition? = null
    ) {
        val textSize = size * 0.032f
        val lineHeight = size * 0.042f

        // Calculate layout based on number of planets
        val columns = if (planets.size > 3) 2 else 1
        val itemsPerColumn = (planets.size + columns - 1) / columns

        planets.forEachIndexed { index, planet ->
            val abbrev = getPlanetAbbreviation(planet.planet)
            val degree = (planet.longitude % 30.0).toInt()
            val degreeSuper = toSuperscript(degree)

            // Build status indicators matching AstroSage style exactly
            // Order: Retrograde*, Combust^, Vargottama¤, Exalted↑, Debilitated↓
            val statusIndicators = buildString {
                // 1. Retrograde indicator
                if (planet.isRetrograde) append(SYMBOL_RETROGRADE)

                // 2. Exalted indicator (↑) - takes priority over debilitated
                if (isExalted(planet.planet, planet.sign)) {
                    append(SYMBOL_EXALTED)
                }
                // 3. Debilitated indicator (↓)
                else if (isDebilitated(planet.planet, planet.sign)) {
                    append(SYMBOL_DEBILITATED)
                }

                // 4. Combust indicator (^) - only if chart context available
                if (chart != null && isCombust(planet, sunPosition)) {
                    append(SYMBOL_COMBUST)
                }

                // 5. Vargottama indicator (¤) - only if chart context available
                if (chart != null && isVargottama(planet, chart)) {
                    append(SYMBOL_VARGOTTAMA)
                }
            }

            val displayText = "$abbrev$degreeSuper$statusIndicators"

            // Calculate position based on layout
            val col = if (columns > 1) index % columns else 0
            val row = index / columns

            val xOffset = if (columns > 1) {
                (col - 0.5f) * size * 0.08f
            } else {
                0f
            }

            val totalRows = if (columns > 1) itemsPerColumn else planets.size
            val yOffset = (row - (totalRows - 1) / 2f) * lineHeight

            val position = Offset(houseCenter.x + xOffset, houseCenter.y + yOffset)
            val color = getPlanetColor(planet.planet)

            drawTextCentered(
                text = displayText,
                position = position,
                textSize = textSize,
                color = color,
                isBold = true
            )
        }
    }

    /**
     * Draw centered text using native canvas
     */
    private fun DrawScope.drawTextCentered(
        text: String,
        position: Offset,
        textSize: Float,
        color: Color,
        isBold: Boolean = false
    ) {
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                this.color = color.toArgb()
                this.textSize = textSize
                this.textAlign = android.graphics.Paint.Align.CENTER
                this.typeface = Typeface.create(
                    Typeface.SANS_SERIF,
                    if (isBold) Typeface.BOLD else Typeface.NORMAL
                )
                this.isAntiAlias = true
                this.isSubpixelText = true
            }
            val textHeight = paint.descent() - paint.ascent()
            val textOffset = textHeight / 2 - paint.descent()
            drawText(text, position.x, position.y + textOffset, paint)
        }
    }

    /**
     * Create a bitmap from the Lagna chart for export (high resolution)
     */
    fun createChartBitmap(chart: VedicChart, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            Density(1f),
            LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawNorthIndianChart(this, chart, min(width, height).toFloat())
        }

        return bitmap
    }

    /**
     * Create a bitmap from a divisional chart for export (high resolution)
     */
    fun createDivisionalChartBitmap(
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        chartTitle: String,
        width: Int,
        height: Int
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            Density(1f),
            LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawDivisionalChart(this, planetPositions, ascendantLongitude, min(width, height).toFloat(), chartTitle)
        }

        return bitmap
    }

    /**
     * Backward compatible method for legacy code
     */
    fun drawSouthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float
    ) {
        drawNorthIndianChart(drawScope, chart, size, "Lagna")
    }

    /**
     * Draw a legend showing the meaning of each status symbol
     * This matches the AstroSage legend style
     *
     * Legend items:
     * - * Retrograde
     * - ^ Combust
     * - ¤ Vargottama
     * - ↑ Exalted
     * - ↓ Debilitated
     */
    fun DrawScope.drawChartLegend(
        chartBottom: Float,
        chartLeft: Float,
        chartWidth: Float,
        textSize: Float
    ) {
        val legendY = chartBottom + textSize * 1.5f
        val legendItems = listOf(
            Pair("$SYMBOL_RETROGRADE Retrograde", HOUSE_NUMBER_COLOR),
            Pair("$SYMBOL_COMBUST Combust", HOUSE_NUMBER_COLOR),
            Pair("$SYMBOL_VARGOTTAMA Vargottama", HOUSE_NUMBER_COLOR),
            Pair("$SYMBOL_EXALTED Exalted", HOUSE_NUMBER_COLOR),
            Pair("$SYMBOL_DEBILITATED Debilitated", HOUSE_NUMBER_COLOR)
        )

        // Calculate spacing for legend items
        val totalItems = legendItems.size
        val itemWidth = chartWidth / totalItems

        legendItems.forEachIndexed { index, (text, color) ->
            val xPos = chartLeft + (index * itemWidth) + (itemWidth / 2)
            drawTextCentered(
                text = text,
                position = Offset(xPos, legendY),
                textSize = textSize * 0.8f,
                color = color,
                isBold = false
            )
        }
    }

    /**
     * Draw a complete chart with legend included
     * This is the recommended method for displaying charts with symbol explanations
     *
     * @param drawScope The drawing scope
     * @param chart The Vedic chart data
     * @param size The total size available (chart will use most of this, legend takes the rest)
     * @param chartTitle Optional title for the chart
     * @param showLegend Whether to display the symbol legend below the chart
     */
    fun drawChartWithLegend(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna",
        showLegend: Boolean = true
    ) {
        with(drawScope) {
            // Reserve space for legend if needed
            val legendHeight = if (showLegend) size * 0.08f else 0f
            val chartSize = size - legendHeight

            // Draw the main chart
            drawNorthIndianChart(this, chart, chartSize, chartTitle)

            // Draw the legend if requested
            if (showLegend) {
                val padding = chartSize * 0.02f
                val chartBottom = chartSize - padding
                val chartLeft = padding
                val chartWidth = chartSize - (padding * 2)
                val textSize = chartSize * 0.028f

                // Draw legend background
                drawRect(
                    color = BACKGROUND_COLOR,
                    topLeft = Offset(0f, chartSize),
                    size = Size(size, legendHeight)
                )

                // Draw legend items
                drawChartLegend(
                    chartBottom = chartSize,
                    chartLeft = padding,
                    chartWidth = chartWidth,
                    textSize = textSize
                )
            }
        }
    }

    /**
     * Draw a Lagna marker ("La") in house 1 to indicate the ascendant
     * This matches AstroSage style where "La" is shown in the first house
     */
    private fun DrawScope.drawLagnaMarker(
        houseCenter: Offset,
        size: Float
    ) {
        val textSize = size * 0.035f
        drawTextCentered(
            text = "La",
            position = Offset(houseCenter.x, houseCenter.y - size * 0.05f),
            textSize = textSize,
            color = LAGNA_COLOR,
            isBold = true
        )
    }
}
