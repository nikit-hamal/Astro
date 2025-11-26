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
                // House 1 in North Indian chart is the top-right inner quadrant
                // Triangle vertices: midTop, center, topRight
                // The "La" marker should be placed in the upper portion of this triangle,
                // positioned to be visually distinct from the house number and planet text
                //
                // Position strategy: Place "La" at a weighted point biased towards the
                // top of the triangle (closer to midTop and the area near top-right)
                val midTop = Offset(centerX, top)
                val topRightCorner = Offset(right, top)
                val chartCenter = Offset(centerX, centerY)

                // Calculate position: between midTop and the centroid, shifted right
                // This places "La" in the upper-center area of house 1's triangle
                val lagnaX = centerX + chartSize * 0.12f  // Shifted right from center
                val lagnaY = top + chartSize * 0.12f  // Near top but inside the house
                val lagnaMarkerPos = Offset(lagnaX, lagnaY)

                drawTextCentered(
                    text = "La",
                    position = lagnaMarkerPos,
                    textSize = size * 0.032f,
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
     *
     * North Indian Chart Geometry (matching AstroSage):
     * =========================================
     * The chart is a square divided by:
     * 1. A central diamond (connecting midpoints of the four sides)
     * 2. Two corner-to-corner diagonals (top-left to bottom-right, top-right to bottom-left)
     *
     * This creates 12 distinct triangular house sections:
     *
     * Visual Layout (numbers indicate house positions):
     *
     *      ┌─────────────────────────────────────────┐
     *      │ ╲              12              ╱        │
     *      │   ╲                         ╱    1     │
     *      │     ╲                     ╱            │
     *      │  11   ╲                 ╱         2   │
     *      │         ╲             ╱                │
     *      ├───────────╲─────────╱──────────────────┤
     *      │             ╲     ╱                    │
     *      │   10         ╲   ╱         3          │
     *      │               ╲ ╱                      │
     *      ├────────────────╳───────────────────────┤
     *      │               ╱ ╲                      │
     *      │    9        ╱     ╲        4          │
     *      │           ╱         ╲                  │
     *      ├─────────╱─────────────╲────────────────┤
     *      │       ╱        8        ╲      5      │
     *      │     ╱                     ╲            │
     *      │   ╱             7           ╲         │
     *      │ ╱                6            ╲       │
     *      └─────────────────────────────────────────┘
     *
     * House boundaries (triangles defined by vertices):
     * - House 1: midTop → center → topRight (top-right quadrant, between diamond and diagonal)
     * - House 2: midTop → topRight → midRight (top-right corner triangle)
     * - House 3: topRight → midRight → center (right side, upper)
     * - House 4: center → midRight → bottomRight (right side, lower)
     * - House 5: midRight → bottomRight → midBottom (bottom-right corner triangle)
     * - House 6: center → midRight → midBottom (not quite - see below)
     * - House 7: midBottom → center → midLeft (bottom center of diamond)
     * - House 8: midBottom → midLeft → center (not quite - see below)
     * - House 9: midBottom → bottomLeft → midLeft (bottom-left corner triangle)
     * - House 10: center → bottomLeft → midLeft (left side, lower)
     * - House 11: topLeft → center → midLeft (left side, upper)
     * - House 12: midLeft → midTop → topLeft (top-left quadrant, between diamond and diagonal)
     *
     * Note: Houses 6, 7, 8 are in the BOTTOM portion of the chart below the center line,
     * bounded by the bottom edge of the diamond (midRight-midBottom-midLeft) and the
     * corner-to-corner diagonals.
     *
     * Planet placement uses the CENTROID of each triangular region for optimal centering.
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

        // Key geometric reference points
        // Diamond vertices (midpoints of the square's sides)
        val midTop = Offset(centerX, top)
        val midRight = Offset(right, centerY)
        val midBottom = Offset(centerX, bottom)
        val midLeft = Offset(left, centerY)

        // Chart center (where all diagonals intersect)
        val center = Offset(centerX, centerY)

        // Square corner points
        val topLeft = Offset(left, top)
        val topRight = Offset(right, top)
        val bottomRight = Offset(right, bottom)
        val bottomLeft = Offset(left, bottom)

        // Calculate planet placement centroids for each triangular house
        return when (houseNum) {
            // House 1: Top-right quadrant inner triangle
            // Bounded by: diamond's top edge (midTop), center, and top-right diagonal
            // Vertices: midTop, center, and point on diagonal ~ towards topRight
            1 -> centroid(midTop, center, topRight)

            // House 2: Top-right corner triangle
            // Bounded by: top edge, right edge (upper), and diamond edge
            // Vertices: midTop, topRight, midRight
            2 -> centroid(midTop, topRight, midRight)

            // House 3: Right side upper triangle
            // Bounded by: top-right diagonal, right edge, and center
            // Vertices: topRight, midRight, center
            3 -> centroid(topRight, midRight, center)

            // House 4: Right side lower triangle
            // Bounded by: center, right edge, bottom-right diagonal
            // Vertices: center, midRight, bottomRight
            4 -> centroid(center, midRight, bottomRight)

            // House 5: Bottom-right corner triangle
            // Bounded by: right edge (lower), bottom edge, and diamond edge
            // Vertices: midRight, bottomRight, midBottom
            5 -> centroid(midRight, bottomRight, midBottom)

            // House 6: Bottom area, right of center
            // Bounded by: bottom-right diagonal, bottom edge, and vertical from center
            // Vertices: center, bottomRight, midBottom (approximate - quadrilateral simplified)
            6 -> centroid(center, bottomRight, midBottom)

            // House 7: Bottom center triangle (opposite to Lagna)
            // This is the apex of the bottom edge pointing into the diamond
            // Vertices: midBottom, center (left portion), center (right portion)
            // Simplified to: midBottom and points on either side near center
            7 -> centroid(midBottom, bottomLeft, bottomRight)

            // House 8: Bottom area, left of center
            // Bounded by: bottom-left diagonal, bottom edge, and vertical from center
            // Vertices: center, midBottom, bottomLeft (approximate)
            8 -> centroid(center, midBottom, bottomLeft)

            // House 9: Bottom-left corner triangle
            // Bounded by: bottom edge, left edge (lower), and diamond edge
            // Vertices: midBottom, bottomLeft, midLeft
            9 -> centroid(midBottom, bottomLeft, midLeft)

            // House 10: Left side lower triangle
            // Bounded by: center, bottom-left diagonal, and left edge
            // Vertices: center, bottomLeft, midLeft
            10 -> centroid(center, bottomLeft, midLeft)

            // House 11: Left side upper triangle
            // Bounded by: top-left diagonal, center, and left edge
            // Vertices: topLeft, center, midLeft
            11 -> centroid(topLeft, center, midLeft)

            // House 12: Top-left quadrant inner triangle
            // Bounded by: diamond's top edge (midTop), center, and top-left diagonal
            // Vertices: midLeft, midTop, topLeft
            12 -> centroid(midLeft, midTop, topLeft)

            else -> center
        }
    }

    /**
     * Calculate the centroid (geometric center) of a triangle
     *
     * The centroid divides each median in the ratio 2:1 from vertex to midpoint.
     * For a triangle with vertices (x1,y1), (x2,y2), (x3,y3):
     * Centroid = ((x1+x2+x3)/3, (y1+y2+y3)/3)
     *
     * This provides the optimal center point for placing text within a triangular region.
     */
    private fun centroid(p1: Offset, p2: Offset, p3: Offset): Offset {
        return Offset(
            (p1.x + p2.x + p3.x) / 3f,
            (p1.y + p2.y + p3.y) / 3f
        )
    }

    /**
     * Get position for house number placement
     *
     * AstroSage-style positioning analysis:
     * - House numbers are placed at the OUTERMOST apex/corner of each triangular house section
     * - Numbers have sufficient padding from grid lines (both outer border and internal diagonals)
     * - The visual effect is that each number marks the "entrance" or "corner" of its house
     *
     * North Indian Chart geometry:
     * - Square with central diamond + two corner-to-corner diagonals
     * - Creates 12 triangular houses
     *
     * Number positioning strategy by house type:
     * 1. Corner houses (2, 5, 9, 12): Number near the square's corner, inset diagonally
     * 2. Edge houses (3, 4, 10, 11): Number along the outer edge, vertically centered within the house section
     * 3. Diamond apex houses (1, 7): Number near the diamond apex pointing to top/bottom edge
     * 4. Inner diamond houses (6, 8): Number between the bottom edge and the diagonal
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

        // Padding values calibrated for AstroSage-style appearance
        // Corner inset: distance from corner for corner house numbers (2, 5, 9, 12)
        val cornerInset = chartSize * 0.065f
        // Edge padding: distance from outer edge for side house numbers (3, 4, 10, 11)
        val edgePadding = chartSize * 0.025f
        // Diamond padding: distance from edge for diamond house numbers (1, 7)
        val diamondPadding = chartSize * 0.055f

        // House sections on right/left are divided by the horizontal center line
        // Position numbers at 75% up/down from center to edge
        val sideVerticalOffset = chartSize * 0.18f

        // Diamond houses 6, 8 are positioned between bottom edge and diagonal
        val bottomDiamondOffset = chartSize * 0.12f

        return when (houseNum) {
            // House 1: Top center diamond apex (Lagna position)
            // Number placed just below the top edge, horizontally centered
            1 -> Offset(centerX, top + diamondPadding)

            // House 2: Top-right corner triangle
            // Number placed diagonally inward from the top-right corner
            2 -> Offset(right - cornerInset, top + cornerInset)

            // House 3: Right edge, upper section (between corner diagonal and center line)
            // Number placed near right edge, in upper portion of right side
            3 -> Offset(right - edgePadding, centerY - sideVerticalOffset)

            // House 4: Right edge, lower section (between center line and corner diagonal)
            // Number placed near right edge, in lower portion of right side
            4 -> Offset(right - edgePadding, centerY + sideVerticalOffset)

            // House 5: Bottom-right corner triangle
            // Number placed diagonally inward from the bottom-right corner
            5 -> Offset(right - cornerInset, bottom - cornerInset)

            // House 6: Bottom diamond, right section
            // Number placed near bottom edge, between center and right diagonal
            6 -> Offset(centerX + bottomDiamondOffset, bottom - diamondPadding)

            // House 7: Bottom center diamond apex (opposite to Lagna)
            // Number placed just above the bottom edge, horizontally centered
            7 -> Offset(centerX, bottom - diamondPadding)

            // House 8: Bottom diamond, left section
            // Number placed near bottom edge, between left diagonal and center
            8 -> Offset(centerX - bottomDiamondOffset, bottom - diamondPadding)

            // House 9: Bottom-left corner triangle
            // Number placed diagonally inward from the bottom-left corner
            9 -> Offset(left + cornerInset, bottom - cornerInset)

            // House 10: Left edge, lower section (between center line and corner diagonal)
            // Number placed near left edge, in lower portion of left side
            10 -> Offset(left + edgePadding, centerY + sideVerticalOffset)

            // House 11: Left edge, upper section (between corner diagonal and center line)
            // Number placed near left edge, in upper portion of left side
            11 -> Offset(left + edgePadding, centerY - sideVerticalOffset)

            // House 12: Top-left corner triangle
            // Number placed diagonally inward from the top-left corner
            12 -> Offset(left + cornerInset, top + cornerInset)

            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Draw planets positioned within a house with degree superscripts and status indicators
     *
     * Layout algorithm (matching AstroSage professional standards):
     * - Single planet: Centered at house centroid
     * - 2-3 planets: Vertical stack with proper line spacing
     * - 4+ planets: Two-column layout with even distribution
     *
     * The positioning avoids overlap with house borders and numbers by:
     * 1. Using the geometric centroid as the anchor point
     * 2. Distributing multiple planets around the centroid
     * 3. Adjusting spacing based on planet count
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
        if (planets.isEmpty()) return

        // Text sizing - slightly smaller for better fit in triangular houses
        val textSize = size * 0.030f
        // Line height for vertical spacing between planets
        val lineHeight = size * 0.038f

        // Determine layout strategy based on planet count
        val layoutConfig = when {
            planets.size == 1 -> LayoutConfig(columns = 1, rows = 1)
            planets.size == 2 -> LayoutConfig(columns = 1, rows = 2)
            planets.size == 3 -> LayoutConfig(columns = 1, rows = 3)
            planets.size == 4 -> LayoutConfig(columns = 2, rows = 2)
            planets.size <= 6 -> LayoutConfig(columns = 2, rows = 3)
            else -> LayoutConfig(columns = 2, rows = (planets.size + 1) / 2)
        }

        // Column spacing for multi-column layouts
        val columnSpacing = size * 0.06f

        planets.forEachIndexed { index, planet ->
            val abbrev = getPlanetAbbreviation(planet.planet)
            val degree = (planet.longitude % 30.0).toInt()
            val degreeSuper = toSuperscript(degree)

            // Build status indicators matching AstroSage style exactly
            // Order: Retrograde*, Exalted↑/Debilitated↓, Combust^, Vargottama¤
            val statusIndicators = buildString {
                // 1. Retrograde indicator (*)
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

            // Calculate position within the layout grid
            val position = calculatePlanetPosition(
                index = index,
                totalPlanets = planets.size,
                layoutConfig = layoutConfig,
                houseCenter = houseCenter,
                lineHeight = lineHeight,
                columnSpacing = columnSpacing
            )

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
     * Layout configuration for planet placement within a house
     */
    private data class LayoutConfig(val columns: Int, val rows: Int)

    /**
     * Calculate the exact position for a planet within the house layout grid
     *
     * @param index The planet's index in the list (0-based)
     * @param totalPlanets Total number of planets in this house
     * @param layoutConfig The layout configuration (columns x rows)
     * @param houseCenter The centroid of the house triangle
     * @param lineHeight Vertical spacing between rows
     * @param columnSpacing Horizontal spacing between columns
     * @return The Offset position where the planet text should be drawn
     */
    private fun calculatePlanetPosition(
        index: Int,
        totalPlanets: Int,
        layoutConfig: LayoutConfig,
        houseCenter: Offset,
        lineHeight: Float,
        columnSpacing: Float
    ): Offset {
        return if (layoutConfig.columns == 1) {
            // Single column layout - vertical stack centered on house centroid
            val totalHeight = (totalPlanets - 1) * lineHeight
            val startY = houseCenter.y - totalHeight / 2f
            Offset(houseCenter.x, startY + index * lineHeight)
        } else {
            // Multi-column layout
            val col = index % layoutConfig.columns
            val row = index / layoutConfig.columns

            // Calculate actual rows used
            val actualRows = (totalPlanets + layoutConfig.columns - 1) / layoutConfig.columns

            // Center the grid on house centroid
            val totalWidth = (layoutConfig.columns - 1) * columnSpacing
            val totalHeight = (actualRows - 1) * lineHeight

            val startX = houseCenter.x - totalWidth / 2f
            val startY = houseCenter.y - totalHeight / 2f

            Offset(
                startX + col * columnSpacing,
                startY + row * lineHeight
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
