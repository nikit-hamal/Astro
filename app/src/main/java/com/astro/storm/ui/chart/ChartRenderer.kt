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
import kotlin.math.min

/**
 * Professional North Indian Style Vedic Chart Renderer
 *
 * Authentic North Indian chart format matching traditional Vedic astrology software:
 * - SQUARE outer boundary
 * - Central diamond created by connecting midpoints of sides
 * - Full corner-to-corner diagonals creating proper 12-house divisions
 * - 12 houses in traditional layout
 * - Planet positions with degree superscripts
 * - Status indicators (retrograde, combust, exalted, debilitated, vargottama)
 *
 * Standard North Indian Chart Layout (Houses numbered 1-12):
 * Ascendant is ALWAYS in the top center diamond (House 1)
 * Signs rotate through houses based on rising sign
 *
 *      ┌─────────────────────────────────┐
 *      │ ╲           12          ╱  1  ╱ │
 *      │   ╲                   ╱     ╱   │
 *      │ 11  ╲               ╱     ╱  2  │
 *      │       ╲           ╱     ╱       │
 *      │─────────╲       ╱─────╱─────────│
 *      │           ╲   ╱     ╱           │
 *      │ 10         ╲╱     ╱          3  │
 *      │            ╱╲   ╱               │
 *      │          ╱    ╲╱                │
 *      │        ╱     ╱╲                 │
 *      │      ╱     ╱    ╲               │
 *      │    ╱     ╱        ╲          4  │
 *      │  9     ╱            ╲           │
 *      │      ╱                ╲         │
 *      │─────╱───────────────────╲───────│
 *      │   ╱        7          8   ╲  5  │
 *      │ ╱                           ╲   │
 *      │╱              6               ╲ │
 *      └─────────────────────────────────┘
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
     * Check if planet is combust (too close to Sun)
     * Combustion occurs when planet is within specific degrees from Sun
     */
    private fun isCombust(
        planet: Planet,
        planetLongitude: Double,
        sunLongitude: Double,
        isRetrograde: Boolean
    ): Boolean {
        // Sun, Moon, Rahu, Ketu, and outer planets don't combust
        if (planet in listOf(Planet.SUN, Planet.MOON, Planet.RAHU, Planet.KETU,
                             Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return false
        }

        // Calculate angular distance from Sun
        val diff = kotlin.math.abs(planetLongitude - sunLongitude)
        val angularDistance = if (diff > 180.0) 360.0 - diff else diff

        // Check if within 17 arcminutes (0.283°) - this is Cazimi, not combust
        if (angularDistance <= 0.283) {
            return false // Cazimi - heart of Sun - actually strengthening
        }

        // Combustion orbs for each planet
        val combustionOrb = when (planet) {
            Planet.MERCURY -> if (isRetrograde) 12.0 else 14.0
            Planet.VENUS -> if (isRetrograde) 8.0 else 10.0
            Planet.MARS -> 17.0
            Planet.JUPITER -> 11.0
            Planet.SATURN -> 15.0
            else -> return false
        }

        return angularDistance <= combustionOrb
    }

    /**
     * Check if planet is Vargottama (same sign in both D1 and D9 charts)
     * For now, we'll use a simplified check based on specific degree ranges
     * True Vargottama requires D9 calculation
     */
    private fun isVargottama(planet: Planet, sign: ZodiacSign, degreeInSign: Double): Boolean {
        // Vargottama occurs when planet occupies the same sign in D1 (Rashi) and D9 (Navamsa)
        // This happens when planet is in specific degree ranges:
        // - 0° to 3°20' for moveable signs (Aries, Cancer, Libra, Capricorn)
        // - 10° to 13°20' for fixed signs (Taurus, Leo, Scorpio, Aquarius)
        // - 20° to 23°20' for dual signs (Gemini, Virgo, Sagittarius, Pisces)

        val isMoveable = sign in listOf(ZodiacSign.ARIES, ZodiacSign.CANCER,
                                        ZodiacSign.LIBRA, ZodiacSign.CAPRICORN)
        val isFixed = sign in listOf(ZodiacSign.TAURUS, ZodiacSign.LEO,
                                     ZodiacSign.SCORPIO, ZodiacSign.AQUARIUS)
        val isDual = sign in listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO,
                                   ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES)

        return when {
            isMoveable && degreeInSign >= 0.0 && degreeInSign <= 3.333 -> true
            isFixed && degreeInSign >= 10.0 && degreeInSign <= 13.333 -> true
            isDual && degreeInSign >= 20.0 && degreeInSign <= 23.333 -> true
            else -> false
        }
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

            // Draw house numbers and planets
            drawAllHouseContents(
                left, top, chartSize, centerX, centerY,
                ascendantSign, chart.planetPositions, size
            )

            // Draw symbol legend at the bottom
            drawSymbolLegend(size)
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
        chartTitle: String
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

            // Draw house contents
            drawAllHouseContents(
                left, top, chartSize, centerX, centerY,
                ascendantSign, planetPositions, size
            )

            // Draw symbol legend at the bottom
            drawSymbolLegend(size)
        }
    }

    /**
     * Draw symbol legend showing planetary status indicators (matching AstroSage style)
     */
    private fun DrawScope.drawSymbolLegend(size: Float) {
        val legendTop = size * 0.82f // Position legend near bottom
        val legendLeft = size * 0.05f
        val legendTextSize = size * 0.028f
        val lineHeight = size * 0.035f

        // Background for legend
        drawRect(
            color = Color(0xFFE8D4B8), // Slightly darker parchment for contrast
            topLeft = Offset(legendLeft, legendTop),
            size = Size(size * 0.9f, lineHeight * 3f),
            style = androidx.compose.ui.graphics.drawscope.Fill
        )

        // Border for legend
        drawRect(
            color = BORDER_COLOR,
            topLeft = Offset(legendLeft, legendTop),
            size = Size(size * 0.9f, lineHeight * 3f),
            style = Stroke(width = 1.5f)
        )

        // First row of legend items
        var xOffset = legendLeft + size * 0.02f
        val y1 = legendTop + lineHeight * 0.7f

        // * Retrograde
        drawTextLeftAlign(
            text = "* Retrograde",
            position = Offset(xOffset, y1),
            textSize = legendTextSize,
            color = Color(0xFF2C2C2C),
            isBold = false
        )
        xOffset += size * 0.20f

        // ^ Combust
        drawTextLeftAlign(
            text = "^ Combust",
            position = Offset(xOffset, y1),
            textSize = legendTextSize,
            color = Color(0xFF2C2C2C),
            isBold = false
        )
        xOffset += size * 0.20f

        // ¤ Vargottama
        drawTextLeftAlign(
            text = "\u00A4 Vargottama",
            position = Offset(xOffset, y1),
            textSize = legendTextSize,
            color = Color(0xFF2C2C2C),
            isBold = false
        )

        // Second row of legend items
        xOffset = legendLeft + size * 0.02f
        val y2 = legendTop + lineHeight * 1.7f

        // ↑ Exalted
        drawTextLeftAlign(
            text = "\u2191 Exalted",
            position = Offset(xOffset, y2),
            textSize = legendTextSize,
            color = Color(0xFF2C2C2C),
            isBold = false
        )
        xOffset += size * 0.20f

        // ↓ Debilitated
        drawTextLeftAlign(
            text = "\u2193 Debilitated",
            position = Offset(xOffset, y2),
            textSize = legendTextSize,
            color = Color(0xFF2C2C2C),
            isBold = false
        )
    }

    /**
     * Draw left-aligned text using native canvas
     */
    private fun DrawScope.drawTextLeftAlign(
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
                this.textAlign = android.graphics.Paint.Align.LEFT
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
     * Draw all house contents including house numbers and planets
     *
     * North Indian Chart House Layout:
     * The chart is a square with:
     * - Central diamond (connecting midpoints of sides)
     * - Two corner-to-corner diagonals
     * This creates 12 triangular houses.
     *
     * Standard North Indian layout:
     *
     *      ┌─────────────────────────────────┐
     *      │╲            12             ╱    │
     *      │  ╲                       ╱   2  │
     *      │ 11 ╲                   ╱        │
     *      │      ╲───────────────╱         │
     *      │        ╲     1     ╱           │
     *      │──────────╲       ╱─────────────│
     *      │            ╲   ╱               │
     *      │  10         ╳           3      │
     *      │            ╱ ╲                 │
     *      │──────────╱     ╲───────────────│
     *      │        ╱    7    ╲             │
     *      │      ╱─────────────╲           │
     *      │  9 ╱                 ╲    4    │
     *      │  ╱         8          ╲        │
     *      │╱            6            ╲  5  │
     *      └─────────────────────────────────┘
     *
     * House 1 is at TOP CENTER (Lagna/Ascendant)
     * House 7 is at BOTTOM CENTER (opposite to Lagna)
     */
    private fun DrawScope.drawAllHouseContents(
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float,
        ascendantSign: ZodiacSign,
        planetPositions: List<PlanetPosition>,
        size: Float
    ) {
        val right = left + chartSize
        val bottom = top + chartSize

        // Group planets by house
        val planetsByHouse = planetPositions.groupBy { it.house }

        // Get Sun position for combustion calculations
        val sunPosition = planetPositions.find { it.planet == Planet.SUN }

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

            // Draw planets in this house
            val planets = planetsByHouse[houseNum] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCenter, size, houseNum, sunPosition)
            }
        }
    }

    /**
     * Get the center position for placing planets in each house
     * This determines where planet text appears within each house section
     *
     * North Indian chart layout (matching AstroSage style):
     * - House 1: Top center diamond (Lagna)
     * - House 2: Top right triangle
     * - House 3: Right side upper
     * - House 4: Right side lower
     * - House 5: Bottom right triangle
     * - House 6: Bottom center right
     * - House 7: Bottom center diamond (opposite to Lagna)
     * - House 8: Bottom center left
     * - House 9: Bottom left triangle
     * - House 10: Left side lower
     * - House 11: Left side upper
     * - House 12: Top left triangle
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

        // Divide chart into sixths for precise positioning
        val sixthW = chartSize / 6
        val sixthH = chartSize / 6

        // Calculate geometric centers of each house based on actual diamond structure
        return when (houseNum) {
            // House 1: Top center diamond - center of upper diamond section
            1 -> Offset(centerX, top + sixthH * 1.8f)

            // House 2: Top right corner triangle - balanced center
            2 -> Offset(right - sixthW * 1.0f, top + sixthH * 1.0f)

            // House 3: Right side upper trapezoid - center of upper right section
            3 -> Offset(right - sixthW * 0.8f, centerY - sixthH * 1.0f)

            // House 4: Right side lower trapezoid - center of lower right section
            4 -> Offset(right - sixthW * 0.8f, centerY + sixthH * 1.0f)

            // House 5: Bottom right corner triangle - balanced center
            5 -> Offset(right - sixthW * 1.0f, bottom - sixthH * 1.0f)

            // House 6: Bottom center right - center of lower right section
            6 -> Offset(centerX + sixthW * 1.2f, bottom - sixthH * 1.8f)

            // House 7: Bottom center diamond - center of lower diamond section
            7 -> Offset(centerX, bottom - sixthH * 1.8f)

            // House 8: Bottom center left - center of lower left section
            8 -> Offset(centerX - sixthW * 1.2f, bottom - sixthH * 1.8f)

            // House 9: Bottom left corner triangle - balanced center
            9 -> Offset(left + sixthW * 1.0f, bottom - sixthH * 1.0f)

            // House 10: Left side lower trapezoid - center of lower left section
            10 -> Offset(left + sixthW * 0.8f, centerY + sixthH * 1.0f)

            // House 11: Left side upper trapezoid - center of upper left section
            11 -> Offset(left + sixthW * 0.8f, centerY - sixthH * 1.0f)

            // House 12: Top left corner triangle - balanced center
            12 -> Offset(left + sixthW * 1.0f, top + sixthH * 1.0f)

            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Get position for house number - placed at optimal positions within each house
     * Following AstroSage style: house numbers are positioned clearly within house boundaries
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

        // Calculate key points for diamond structure
        val midTop = Offset(centerX, top)
        val midRight = Offset(right, centerY)
        val midBottom = Offset(centerX, bottom)
        val midLeft = Offset(left, centerY)

        // More aggressive positioning - closer to house centers but away from planet areas
        val sixthW = chartSize / 6
        val sixthH = chartSize / 6

        return when (houseNum) {
            // House 1: Top center diamond - place at very top of diamond
            1 -> Offset(centerX, top + sixthH * 0.7f)

            // House 2: Top right corner - place near top-right corner
            2 -> Offset(right - sixthW * 0.6f, top + sixthH * 0.6f)

            // House 3: Right side upper - place near right edge upper section
            3 -> Offset(right - sixthW * 0.5f, centerY - sixthH * 1.3f)

            // House 4: Right side lower - place near right edge lower section
            4 -> Offset(right - sixthW * 0.5f, centerY + sixthH * 1.3f)

            // House 5: Bottom right corner - place near bottom-right corner
            5 -> Offset(right - sixthW * 0.6f, bottom - sixthH * 0.6f)

            // House 6: Bottom center right - place at bottom-center-right
            6 -> Offset(centerX + sixthW * 1.1f, bottom - sixthH * 0.7f)

            // House 7: Bottom center diamond - place at very bottom of diamond
            7 -> Offset(centerX, bottom - sixthH * 0.7f)

            // House 8: Bottom center left - place at bottom-center-left
            8 -> Offset(centerX - sixthW * 1.1f, bottom - sixthH * 0.7f)

            // House 9: Bottom left corner - place near bottom-left corner
            9 -> Offset(left + sixthW * 0.6f, bottom - sixthH * 0.6f)

            // House 10: Left side lower - place near left edge lower section
            10 -> Offset(left + sixthW * 0.5f, centerY + sixthH * 1.3f)

            // House 11: Left side upper - place near left edge upper section
            11 -> Offset(left + sixthW * 0.5f, centerY - sixthH * 1.3f)

            // House 12: Top left corner - place near top-left corner
            12 -> Offset(left + sixthW * 0.6f, top + sixthH * 0.6f)

            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Draw planets positioned within a house with degree superscripts and status indicators
     * Layout adjusts based on number of planets to prevent overlap
     */
    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetPosition>,
        houseCenter: Offset,
        size: Float,
        houseNum: Int,
        sunPosition: PlanetPosition?
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
            val degreeInSign = planet.longitude % 30.0

            // Build status indicators matching AstroSage style
            val statusIndicators = buildString {
                // Retrograde - use asterisk (*) like AstroSage
                if (planet.isRetrograde) append("*")

                // Combust - use caret (^) like AstroSage
                if (sunPosition != null && isCombust(planet.planet, planet.longitude,
                                                     sunPosition.longitude, planet.isRetrograde)) {
                    append("^")
                }

                // Vargottama - use square/box symbol (¤) like AstroSage
                if (isVargottama(planet.planet, planet.sign, degreeInSign)) {
                    append("\u00A4") // ¤ symbol
                }

                // Exalted - use upward arrow (↑)
                if (isExalted(planet.planet, planet.sign)) {
                    append("\u2191") // ↑ for exalted
                }

                // Debilitated - use downward arrow (↓)
                if (isDebilitated(planet.planet, planet.sign)) {
                    append("\u2193") // ↓ for debilitated
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
}
