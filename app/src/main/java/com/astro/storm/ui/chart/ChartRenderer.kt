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
        private val BACKGROUND_COLOR = Color(0xFFFFFFFF) // Pure white background
        private val BORDER_COLOR = Color(0xFFB8860B) // Dark goldenrod for lines
        private val HOUSE_NUMBER_COLOR = Color(0xFF4A4A4A) // Dark gray for house numbers

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

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            drawChartStructure(left, top, chartSize)

            drawAllHouseContents(
                left, top, chartSize,
                chart.ascendant, chart.planetPositions, size
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

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            drawChartStructure(left, top, chartSize)

            drawAllHouseContents(
                left, top, chartSize,
                ascendantLongitude, planetPositions, size
            )
        }
    }

    private fun DrawScope.drawChartStructure(left: Float, top: Float, chartSize: Float) {
        val right = left + chartSize
        val bottom = top + chartSize
        val centerX = left + chartSize / 2
        val centerY = top + chartSize / 2

        // Draw outer square border
        drawRect(
            color = BORDER_COLOR,
            topLeft = Offset(left, top),
            size = Size(chartSize, chartSize),
            style = Stroke(width = 3f)
        )

        // Draw corner-to-corner diagonals
        drawLine(BORDER_COLOR, Offset(left, top), Offset(right, bottom), strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, Offset(right, top), Offset(left, bottom), strokeWidth = 2.5f)

        // Draw the central diamond
        val midTop = Offset(centerX, top)
        val midRight = Offset(right, centerY)
        val midBottom = Offset(centerX, bottom)
        val midLeft = Offset(left, centerY)
        drawLine(BORDER_COLOR, midTop, midRight, strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, midRight, midBottom, strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, midBottom, midLeft, strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, midLeft, midTop, strokeWidth = 2.5f)
    }

    /**
     * Draw all house contents including zodiac sign numbers and planets
     */
    private fun DrawScope.drawAllHouseContents(
        left: Float,
        top: Float,
        chartSize: Float,
        ascendantLongitude: Double,
        planetPositions: List<PlanetPosition>,
        size: Float
    ) {
        val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)

        // Group planets by their zodiac sign
        val planetsBySign = planetPositions.groupBy { it.sign }

        for (houseNum in 1..12) {
            val signIndex = (ascendantSign.number - 1 + (houseNum - 1)) % 12
            val currentSign = ZodiacSign.values()[signIndex]

            val houseCenter = getHouseCenter(houseNum, chartSize, left, top)
            val numberPos = getSignNumberPosition(houseNum, chartSize, left, top)

            // Draw zodiac sign number
            drawTextCentered(
                text = currentSign.number.toString(),
                position = numberPos,
                textSize = size * 0.04f,
                color = HOUSE_NUMBER_COLOR,
                isBold = false
            )

            // Draw planets in this sign
            val planets = planetsBySign[currentSign] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCenter, size)
            }
        }

        // Draw "La" for Lagna/Ascendant in House 1
        val lagnaCenter = getHouseCenter(1, chartSize, left, top)
        drawTextCentered(
            text = "La" + toSuperscript((ascendantLongitude % 30).toInt()),
            position = Offset(lagnaCenter.x, lagnaCenter.y - size * 0.05f),
            textSize = size * 0.035f,
            color = Color.Black,
            isBold = true
        )
    }

    private fun getHouseCenter(houseNum: Int, chartSize: Float, left: Float, top: Float): Offset {
        val centerX = left + chartSize / 2
        val centerY = top + chartSize / 2
        val quarter = chartSize / 4

        return when (houseNum) {
            1 -> Offset(centerX, top + quarter)
            2 -> Offset(left + quarter * 3, top + quarter)
            3 -> Offset(left + quarter * 3, top + quarter * 2)
            4 -> Offset(left + quarter * 3, top + quarter * 3)
            5 -> Offset(centerX, top + quarter * 3)
            6 -> Offset(left + quarter, top + quarter * 3)
            7 -> Offset(left + quarter, top + quarter * 2)
            8 -> Offset(left + quarter, top + quarter)
            9 -> Offset(centerX, top + quarter * 0.8f)
            10 -> Offset(left + quarter * 3.2f, centerY)
            11 -> Offset(centerX, top + quarter * 3.2f)
            12 -> Offset(left + quarter * 0.8f, centerY)
            else -> Offset(centerX, centerY)
        }
    }


    private fun getSignNumberPosition(houseNum: Int, chartSize: Float, left: Float, top: Float): Offset {
        val centerX = left + chartSize / 2
        val centerY = top + chartSize / 2
        val oneEighth = chartSize / 8

        return when (houseNum) {
            1 -> Offset(centerX, top + oneEighth)
            2 -> Offset(left + oneEighth * 6, top + oneEighth * 2)
            3 -> Offset(left + oneEighth * 7, centerY)
            4 -> Offset(left + oneEighth * 6, top + oneEighth * 6)
            5 -> Offset(centerX, top + oneEighth * 7)
            6 -> Offset(left + oneEighth * 2, top + oneEighth * 6)
            7 -> Offset(left + oneEighth, centerY)
            8 -> Offset(left + oneEighth * 2, top + oneEighth * 2)
            9 -> Offset(left + oneEighth * 3, top + oneEighth * 3)
            10 -> Offset(left + oneEighth * 5, top + oneEighth * 3)
            11 -> Offset(left + oneEighth * 5, top + oneEighth * 5)
            12 -> Offset(left + oneEighth * 3, top + oneEighth * 5)
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
        size: Float
    ) {
        val textSize = size * 0.032f
        val lineHeight = size * 0.042f
        val totalPlanets = planets.size
        val columns = if (totalPlanets > 2) 2 else 1
        val rows = (totalPlanets + columns - 1) / columns

        planets.forEachIndexed { index, planet ->
            val abbrev = getPlanetAbbreviation(planet.planet)
            val degree = (planet.longitude % 30.0).toInt()
            val degreeSuper = toSuperscript(degree)

            // Build status indicators matching AstroSage style
            val statusIndicators = buildString {
                if (planet.isRetrograde) append("*")
                if (planet.isCombust) append("^")
                if (isExalted(planet.planet, planet.sign)) append("\u2191") // ↑ for exalted
                if (isDebilitated(planet.planet, planet.sign)) append("\u2193") // ↓ for debilitated
                if (isOwnSign(planet.planet, planet.sign)) append("S")
                if (planet.isVargottama) append("\u25A1") // □ for Vargottama
            }

            val displayText = "$abbrev$degreeSuper$statusIndicators"

            val rowIndex = index % rows
            val colIndex = index / rows
            val xOffset = (colIndex - (columns - 1) / 2f) * (size * 0.1f)
            val yOffset = (rowIndex - (rows - 1) / 2f) * lineHeight

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
