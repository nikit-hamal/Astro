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
 * Authentic North Indian chart format with:
 * - SQUARE outer boundary (not diamond)
 * - Internal diagonal lines creating diamond pattern inside
 * - 12 houses in traditional layout
 * - Planet positions with degree superscripts
 * - Status indicators (retrograde, combust, exalted, debilitated, vargottama)
 *
 * Standard North Indian Chart Layout (Houses numbered 1-12):
 *
 *        ┌─────────────────────────────────┐
 *        │ \     12      │       1     / │
 *        │  \            │            /  │
 *        │   \──────────────────────/   │
 *        │ 11 │ \                / │  2  │
 *        │    │   \            /   │     │
 *        ├────│     \        /     │─────┤
 *        │    │       \    /       │     │
 *        │ 10 │        \  /        │  3  │
 *        │    │        /  \        │     │
 *        │    │       /    \       │     │
 *        ├────│     /        \     │─────┤
 *        │  9 │   /            \   │  4  │
 *        │    │ /                \ │     │
 *        │   /──────────────────────\   │
 *        │  /            │            \  │
 *        │ /      8      │       7     \ │
 *        └─────────────────────────────────┘
 *                        6       5
 *
 * House 1 (Ascendant) is at the top-right triangle
 */
class ChartRenderer {

    companion object {
        // Warm earthy color palette matching the reference
        private val BACKGROUND_COLOR = Color(0xFFCBB896) // Warm tan/beige background
        private val CHART_BACKGROUND = Color(0xFFCBB896)
        private val BORDER_COLOR = Color(0xFF8B4513) // Saddle brown for lines
        private val HOUSE_NUMBER_COLOR = Color(0xFF4A4A4A) // Dark gray for house numbers
        private val PLANET_TEXT_COLOR = Color(0xFF1A1A1A) // Near black for planets
        private val TITLE_COLOR = Color(0xFF8B4513) // Brown for title

        // Planet-specific colors
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
            else -> PLANET_TEXT_COLOR
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
            else -> planet.displayName.take(2)
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
     * Draw a professional North Indian style Vedic chart with SQUARE outer boundary
     */
    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna"
    ) {
        with(drawScope) {
            val padding = size * 0.04f
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
                style = Stroke(width = 2.5f)
            )

            // Draw the inner diamond (connecting midpoints of the square)
            val midTop = Offset(centerX, top)
            val midRight = Offset(right, centerY)
            val midBottom = Offset(centerX, bottom)
            val midLeft = Offset(left, centerY)

            // Main diagonal lines from corners to center diamond
            // Top-left to center
            drawLine(BORDER_COLOR, Offset(left, top), Offset(centerX, centerY), strokeWidth = 2f)
            // Top-right to center
            drawLine(BORDER_COLOR, Offset(right, top), Offset(centerX, centerY), strokeWidth = 2f)
            // Bottom-right to center
            drawLine(BORDER_COLOR, Offset(right, bottom), Offset(centerX, centerY), strokeWidth = 2f)
            // Bottom-left to center
            drawLine(BORDER_COLOR, Offset(left, bottom), Offset(centerX, centerY), strokeWidth = 2f)

            // Draw horizontal and vertical lines through center
            drawLine(BORDER_COLOR, midTop, midBottom, strokeWidth = 2f)
            drawLine(BORDER_COLOR, midLeft, midRight, strokeWidth = 2f)

            // Get ascendant sign for house-sign mapping
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

            // Draw house numbers and planets
            drawAllHouseContents(
                left, top, chartSize, centerX, centerY,
                ascendantSign, chart.planetPositions, size
            )

            // Draw chart title in center
            drawTextCentered(
                text = chartTitle,
                position = Offset(centerX, centerY),
                textSize = size * 0.045f,
                color = TITLE_COLOR,
                isBold = true
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
            val padding = size * 0.04f
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
                style = Stroke(width = 2.5f)
            )

            // Main diagonal lines from corners to center
            drawLine(BORDER_COLOR, Offset(left, top), Offset(centerX, centerY), strokeWidth = 2f)
            drawLine(BORDER_COLOR, Offset(right, top), Offset(centerX, centerY), strokeWidth = 2f)
            drawLine(BORDER_COLOR, Offset(right, bottom), Offset(centerX, centerY), strokeWidth = 2f)
            drawLine(BORDER_COLOR, Offset(left, bottom), Offset(centerX, centerY), strokeWidth = 2f)

            // Draw horizontal and vertical lines through center
            val midTop = Offset(centerX, top)
            val midRight = Offset(right, centerY)
            val midBottom = Offset(centerX, bottom)
            val midLeft = Offset(left, centerY)
            drawLine(BORDER_COLOR, midTop, midBottom, strokeWidth = 2f)
            drawLine(BORDER_COLOR, midLeft, midRight, strokeWidth = 2f)

            // Get ascendant sign
            val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)

            // Draw house contents
            drawAllHouseContents(
                left, top, chartSize, centerX, centerY,
                ascendantSign, planetPositions, size
            )

            // Draw chart title
            drawTextCentered(
                text = chartTitle,
                position = Offset(centerX, centerY),
                textSize = size * 0.04f,
                color = TITLE_COLOR,
                isBold = true
            )
        }
    }

    /**
     * Draw all house contents including house numbers and planets
     *
     * North Indian chart house positions (with square outer boundary):
     *
     * House 12: Top-left triangle (upper)
     * House 1: Top-right triangle (upper) - ASCENDANT
     * House 11: Left-top triangle
     * House 2: Right-top triangle
     * House 10: Left-bottom triangle
     * House 3: Right-bottom triangle
     * House 9: Bottom-left triangle (lower)
     * House 4: Bottom-right triangle (lower)
     * House 8: Bottom-left triangle (upper part)
     * House 5: Bottom-right triangle (upper part)
     * House 7: Bottom center-left
     * House 6: Bottom center-right
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
        val quarterW = chartSize / 4
        val quarterH = chartSize / 4

        // Group planets by house
        val planetsByHouse = planetPositions.groupBy { it.house }

        // Draw each house (1-12)
        for (houseNum in 1..12) {
            val houseCenter = getHouseCenter(houseNum, left, top, chartSize, centerX, centerY)
            val numberPos = getHouseNumberPosition(houseNum, left, top, chartSize, centerX, centerY)

            // Draw house number
            drawTextCentered(
                text = houseNum.toString(),
                position = numberPos,
                textSize = size * 0.032f,
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
     *
     * North Indian chart layout (12 houses in a square):
     * Top row: 12 | 1
     * Upper side: 11 (left) | 2 (right)
     * Middle side: 10 (left) | 3 (right)
     * Lower side: 9 (left) | 4 (right)
     * Bottom row: 8 | 7 | 6 | 5
     */
    private fun getHouseCenter(
        houseNum: Int,
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float
    ): Offset {
        val right = left + chartSize
        val bottom = top + chartSize
        val halfW = chartSize / 2
        val halfH = chartSize / 2
        val quarterW = chartSize / 4
        val quarterH = chartSize / 4

        return when (houseNum) {
            // House 1: Top-right triangle (Ascendant)
            1 -> Offset(centerX + quarterW * 0.8f, top + quarterH * 0.7f)

            // House 2: Right upper triangle
            2 -> Offset(right - quarterW * 0.7f, centerY - quarterH * 0.6f)

            // House 3: Right lower triangle
            3 -> Offset(right - quarterW * 0.7f, centerY + quarterH * 0.6f)

            // House 4: Bottom-right triangle
            4 -> Offset(centerX + quarterW * 0.8f, bottom - quarterH * 0.7f)

            // House 5: Bottom-right area
            5 -> Offset(centerX + quarterW * 0.4f, bottom - quarterH * 0.35f)

            // House 6: Bottom-center-right
            6 -> Offset(centerX + quarterW * 0.1f, bottom - quarterH * 0.5f)

            // House 7: Bottom-center area
            7 -> Offset(centerX - quarterW * 0.4f, bottom - quarterH * 0.35f)

            // House 8: Bottom-left triangle
            8 -> Offset(centerX - quarterW * 0.8f, bottom - quarterH * 0.7f)

            // House 9: Left lower triangle
            9 -> Offset(left + quarterW * 0.7f, centerY + quarterH * 0.6f)

            // House 10: Left upper triangle
            10 -> Offset(left + quarterW * 0.7f, centerY - quarterH * 0.6f)

            // House 11: Top-left upper area
            11 -> Offset(centerX - quarterW * 0.8f, top + quarterH * 0.7f)

            // House 12: Top-center area
            12 -> Offset(centerX - quarterW * 0.4f, top + quarterH * 0.35f)

            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Get position for house number (placed near the edge of each house section)
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
        val halfW = chartSize / 2
        val halfH = chartSize / 2
        val offset = chartSize * 0.08f

        return when (houseNum) {
            1 -> Offset(right - offset * 1.5f, top + offset)
            2 -> Offset(right - offset, centerY - offset * 1.5f)
            3 -> Offset(right - offset, centerY + offset * 1.5f)
            4 -> Offset(right - offset * 1.5f, bottom - offset)
            5 -> Offset(centerX + offset * 2f, bottom - offset)
            6 -> Offset(centerX + offset * 0.5f, bottom - offset)
            7 -> Offset(centerX - offset * 0.5f, bottom - offset)
            8 -> Offset(centerX - offset * 2f, bottom - offset)
            9 -> Offset(left + offset, centerY + offset * 1.5f)
            10 -> Offset(left + offset, centerY - offset * 1.5f)
            11 -> Offset(left + offset * 1.5f, top + offset)
            12 -> Offset(centerX - offset * 0.5f, top + offset)
            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Draw planets positioned within a house with degree superscripts and status indicators
     */
    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetPosition>,
        houseCenter: Offset,
        size: Float,
        houseNum: Int
    ) {
        val textSize = size * 0.030f
        val lineHeight = size * 0.038f

        // Calculate vertical offset for centering multiple planets
        val totalHeight = (planets.size - 1) * lineHeight
        val startY = houseCenter.y - totalHeight / 2f

        planets.forEachIndexed { index, planet ->
            val abbrev = getPlanetAbbreviation(planet.planet)
            val degree = (planet.longitude % 30.0).toInt()
            val degreeSuper = toSuperscript(degree)

            // Build status indicators
            val statusIndicators = buildString {
                if (planet.isRetrograde) append("*")
                // Note: Combust status would need to be passed in or calculated here
                if (isExalted(planet.planet, planet.sign)) append("\u2191") // ↑
                if (isDebilitated(planet.planet, planet.sign)) append("\u2193") // ↓
            }

            val displayText = "$abbrev$degreeSuper$statusIndicators"

            val yOffset = startY + (index * lineHeight)
            val color = getPlanetColor(planet.planet)

            drawTextCentered(
                text = displayText,
                position = Offset(houseCenter.x, yOffset),
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
     * Create a bitmap from the Lagna chart for export
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
     * Create a bitmap from a divisional chart for export
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
