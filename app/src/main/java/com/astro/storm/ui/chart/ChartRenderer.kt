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
                drawPlanetsInHouse(planets, houseCenter, size, houseNum)
            }
        }
    }

    /**
     * Get the center position for placing planets in each house
     * This determines where planet text appears within each house section
     *
     * North Indian chart layout:
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

        return when (houseNum) {
            // House 1: Top center diamond (Lagna/Ascendant) - upper part of central diamond
            1 -> Offset(centerX, top + sixthH * 1.5f)

            // House 2: Top right corner triangle
            2 -> Offset(right - sixthW * 1.2f, top + sixthH * 1.2f)

            // House 3: Right side upper trapezoid
            3 -> Offset(right - sixthW, centerY - sixthH * 0.8f)

            // House 4: Right side lower trapezoid
            4 -> Offset(right - sixthW, centerY + sixthH * 0.8f)

            // House 5: Bottom right corner triangle
            5 -> Offset(right - sixthW * 1.2f, bottom - sixthH * 1.2f)

            // House 6: Bottom center right - lower right part of bottom
            6 -> Offset(centerX + sixthW * 0.8f, bottom - sixthH * 1.5f)

            // House 7: Bottom center diamond (opposite to Lagna) - lower part of central diamond
            7 -> Offset(centerX, bottom - sixthH * 1.5f)

            // House 8: Bottom center left - lower left part of bottom
            8 -> Offset(centerX - sixthW * 0.8f, bottom - sixthH * 1.5f)

            // House 9: Bottom left corner triangle
            9 -> Offset(left + sixthW * 1.2f, bottom - sixthH * 1.2f)

            // House 10: Left side lower trapezoid
            10 -> Offset(left + sixthW, centerY + sixthH * 0.8f)

            // House 11: Left side upper trapezoid
            11 -> Offset(left + sixthW, centerY - sixthH * 0.8f)

            // House 12: Top left corner triangle
            12 -> Offset(left + sixthW * 1.2f, top + sixthH * 1.2f)

            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Get position for house number (placed near the edge/corner of each house section)
     * Numbers are placed at the outer edges of each house for clarity
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
        val offset = chartSize * 0.05f
        val eighthW = chartSize / 8
        val eighthH = chartSize / 8

        return when (houseNum) {
            // House 1: Top center - place number at top edge
            1 -> Offset(centerX, top + offset * 1.5f)

            // House 2: Top right corner - place at corner area
            2 -> Offset(right - offset * 1.5f, top + offset * 1.5f)

            // House 3: Right side upper - place at right edge
            3 -> Offset(right - offset, centerY - eighthH * 1.5f)

            // House 4: Right side lower - place at right edge
            4 -> Offset(right - offset, centerY + eighthH * 1.5f)

            // House 5: Bottom right corner - place at corner area
            5 -> Offset(right - offset * 1.5f, bottom - offset * 1.5f)

            // House 6: Bottom center right - place at bottom edge
            6 -> Offset(centerX + eighthW * 1.5f, bottom - offset * 1.5f)

            // House 7: Bottom center - place number at bottom edge
            7 -> Offset(centerX, bottom - offset * 1.5f)

            // House 8: Bottom center left - place at bottom edge
            8 -> Offset(centerX - eighthW * 1.5f, bottom - offset * 1.5f)

            // House 9: Bottom left corner - place at corner area
            9 -> Offset(left + offset * 1.5f, bottom - offset * 1.5f)

            // House 10: Left side lower - place at left edge
            10 -> Offset(left + offset, centerY + eighthH * 1.5f)

            // House 11: Left side upper - place at left edge
            11 -> Offset(left + offset, centerY - eighthH * 1.5f)

            // House 12: Top left corner - place at corner area
            12 -> Offset(left + offset * 1.5f, top + offset * 1.5f)

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
        houseNum: Int
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

            // Build status indicators matching AstroSage style
            val statusIndicators = buildString {
                if (planet.isRetrograde) append("*")
                if (isExalted(planet.planet, planet.sign)) append("\u2191") // ↑ for exalted
                if (isDebilitated(planet.planet, planet.sign)) append("\u2193") // ↓ for debilitated
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
