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
 * Houses flow COUNTER-CLOCKWISE from House 1
 *
 * Visual Reference (matching AstroSage exactly):
 *
 *       ┌────────────────────────────────────────┐
 *       │\              2               /        │
 *       │  \                          /    1    │
 *       │ 3  \                      /           │
 *       │      \                  /         12  │
 *       │        \              /               │
 *       │─────────\────────────/────────────────│
 *       │           \        /                  │
 *       │    4       \      /       11          │
 *       │             \    /                    │
 *       │──────────────\  /─────────────────────│
 *       │               \/                      │
 *       │               /\                      │
 *       │    5        /    \        10          │
 *       │           /        \                  │
 *       │─────────/────────────\────────────────│
 *       │       /       7        \       9      │
 *       │  6  /                    \            │
 *       │   /                        \     8    │
 *       │ /                            \        │
 *       └────────────────────────────────────────┘
 *
 * House positions (counter-clockwise from House 1):
 * - House 1:  Top center diamond (Lagna/Ascendant)
 * - House 2:  Top-left corner triangle
 * - House 3:  Left side upper triangle
 * - House 4:  Left center diamond
 * - House 5:  Left side lower triangle
 * - House 6:  Bottom-left corner triangle
 * - House 7:  Bottom center diamond
 * - House 8:  Bottom-right corner triangle
 * - House 9:  Right side lower triangle
 * - House 10: Right center diamond
 * - House 11: Right side upper triangle
 * - House 12: Top-right corner triangle
 *
 * The chart has:
 * - Outer square border
 * - Central diamond (connecting midpoints of sides)
 * - Two corner-to-corner diagonals
 * This creates 12 distinct triangular/diamond houses
 */
class ChartRenderer(
    private val chartDataProcessor: ChartDataProcessor = ChartDataProcessor(),
    private val theme: ChartTheme = ChartTheme()
) {

    // Named constants for geometry ratios
    private val CORNER_CENTER_FRACTION = 0.18f
    private val SIDE_VERTICAL_OFFSET_FRACTION = 0.18f
    private val DIAMOND_PLANET_VERTICAL_FRACTION = 0.25f
    private val DIAMOND_NUMBER_VERTICAL_FRACTION = 0.38f
    private val SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION = 0.15f
    private val CORNER_NUMBER_OFFSET_FRACTION = 0.12f
    private val SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION = 0.08f
    private val SIDE_NUMBER_VERTICAL_OFFSET_FRACTION = 0.28f
    private val DIAMOND_NUMBER_HORIZONTAL_OFFSET_FRACTION = 0.12f
    private val DIAMOND_NUMBER_VERTICAL_OFFSET_FRACTION = 0.08f
    private val CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION = 0.18f

    private val textPaint = android.graphics.Paint().apply {
        textAlign = android.graphics.Paint.Align.CENTER
        isAntiAlias = true
        isSubpixelText = true
    }

    private val frameLinesPath = Path()


    companion object {
        // Status indicator symbols (matching AstroSage exactly)
        const val SYMBOL_RETROGRADE = "*"       // Retrograde motion
        const val SYMBOL_COMBUST = "^"          // Combust (too close to Sun)
        const val SYMBOL_VARGOTTAMA = "\u00A4"  // ¤ - Vargottama (same sign in D1 and D9)
        const val SYMBOL_EXALTED = "\u2191"     // ↑ - Exalted
        const val SYMBOL_DEBILITATED = "\u2193" // ↓ - Debilitated

        // Each sign is 30 degrees, divided into 9 Navamsa parts.
        // Each part is 30 / 9 = 3.333... degrees, which is exactly 10/3 degrees.
        // Using a precise constant avoids floating-point inaccuracies.
        private const val NAVAMSA_PART_DEGREES = 10.0 / 3.0

        private val SUPERSCRIPT_MAP = mapOf(
            '0' to '\u2070', '1' to '\u00B9', '2' to '\u00B2', '3' to '\u00B3',
            '4' to '\u2074', '5' to '\u2075', '6' to '\u2076', '7' to '\u2077',
            '8' to '\u2078', '9' to '\u2079'
        )
    }
    /**
     * Convert degree to superscript string
     */
    private fun toSuperscript(degree: Int): String {
        return degree.toString().map { SUPERSCRIPT_MAP[it] ?: it }.joinToString("")
    }

    private data class ChartFrame(
        val left: Float,
        val top: Float,
        val size: Float,
        val centerX: Float,
        val centerY: Float
    )

    private fun DrawScope.drawNorthIndianFrame(): ChartFrame {
        val chartSize = min(size.width, size.height)
        val padding = chartSize * 0.02f
        val effectiveChartSize = chartSize - (padding * 2)
        val left = (size.width - effectiveChartSize) / 2
        val top = (size.height - effectiveChartSize) / 2
        val right = left + effectiveChartSize
        val bottom = top + effectiveChartSize
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2

        // Background
        drawRect(
            color = theme.backgroundColor,
            size = size
        )

        // Outer square
        drawRect(
            color = theme.borderColor,
            topLeft = Offset(left, top),
            size = Size(effectiveChartSize, effectiveChartSize),
            style = theme.borderStroke
        )

        // Use cached path to draw all internal lines in a single operation
        frameLinesPath.reset()
        frameLinesPath.moveTo(centerX, top)
        frameLinesPath.lineTo(right, centerY)
        frameLinesPath.lineTo(centerX, bottom)
        frameLinesPath.lineTo(left, centerY)
        frameLinesPath.close()
        frameLinesPath.moveTo(left, top)
        frameLinesPath.lineTo(right, bottom)
        frameLinesPath.moveTo(right, top)
        frameLinesPath.lineTo(left, bottom)

        drawPath(frameLinesPath, theme.borderColor, style = theme.lineStroke)

        return ChartFrame(left, top, effectiveChartSize, centerX, centerY)
    }

    /**
     * Draw a professional North Indian style Vedic chart
     */
    fun DrawScope.drawNorthIndianChart(
        chart: VedicChart
    ) {
        val frame = drawNorthIndianFrame()
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
        val renderData = chartDataProcessor.processPlanetPositions(chart.planetPositions, chart)

        drawAllHouseContents(
            left = frame.left,
            top = frame.top,
            chartSize = frame.size,
            centerX = frame.centerX,
            centerY = frame.centerY,
            ascendantSign = ascendantSign,
            planetPositions = renderData,
        )
    }

    /**
     * Draw a divisional chart (D9, D10, etc.)
     */
    fun DrawScope.drawDivisionalChart(
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        originalChart: VedicChart
    ) {
        val frame = drawNorthIndianFrame()
        val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)
        val renderData = chartDataProcessor.processPlanetPositions(planetPositions, originalChart)

        drawAllHouseContents(
            left = frame.left,
            top = frame.top,
            chartSize = frame.size,
            centerX = frame.centerX,
            centerY = frame.centerY,
            ascendantSign = ascendantSign,
            planetPositions = renderData,
        )
    }

    /**
     * Converts a house index into a sign number, given the ascendant sign.
     */
    private fun signNumberForHouse(
        houseNum: Int,
        ascendantSign: ZodiacSign
    ): Int {
        // ZodiacSign.ordinal is assumed 0..11 = Aries..Pisces
        return ((ascendantSign.ordinal + houseNum - 1) % 12) + 1
    }

    /**
     * Draw all house contents including house numbers and planets
     */
    private fun DrawScope.drawAllHouseContents(
        left: Float,
        top: Float,
        chartSize: Float,
        centerX: Float,
        centerY: Float,
        ascendantSign: ZodiacSign,
        planetPositions: List<PlanetRenderData>,
        showSignNumbers: Boolean = true
    ) {
        val right = left + chartSize
        val bottom = top + chartSize

        val planetsByHouse = planetPositions.groupBy { it.house }

        for (houseNum in 1..12) {
            val houseCenter = getHousePlanetCenter(houseNum, left, top, chartSize, centerX, centerY)
            val numberPos = getHouseNumberPosition(houseNum, left, top, chartSize, centerX, centerY)

            // House number
            val numberText = if (showSignNumbers) {
                signNumberForHouse(houseNum, ascendantSign).toString()
            } else {
                houseNum.toString()
            }
            drawTextCentered(
                text = numberText,
                position = numberPos,
                textSize = chartSize * 0.035f,
                color = theme.houseNumberColor,
                isBold = false
            )

            // Lagna marker in House 1: position it slightly above the planet center
            if (houseNum == 1) {
                drawLagnaMarker(houseCenter, chartSize)
            }

            // Planets
            val planets = planetsByHouse[houseNum] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCenter, chartSize, houseNum)
            }
        }
    }

    /**
     * Center for planets in each house
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

        return when (houseNum) {
            1 -> Offset(centerX, top + chartSize * DIAMOND_PLANET_VERTICAL_FRACTION)
            2 -> Offset(left + chartSize * CORNER_CENTER_FRACTION, top + chartSize * CORNER_CENTER_FRACTION)
            3 -> Offset(left + chartSize * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY - chartSize * SIDE_VERTICAL_OFFSET_FRACTION)
            4 -> Offset(left + chartSize * DIAMOND_PLANET_VERTICAL_FRACTION, centerY)
            5 -> Offset(left + chartSize * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * SIDE_VERTICAL_OFFSET_FRACTION)
            6 -> Offset(left + chartSize * CORNER_CENTER_FRACTION, bottom - chartSize * CORNER_CENTER_FRACTION)
            7 -> Offset(centerX, bottom - chartSize * DIAMOND_PLANET_VERTICAL_FRACTION)
            8 -> Offset(right - chartSize * CORNER_CENTER_FRACTION, bottom - chartSize * CORNER_CENTER_FRACTION)
            9 -> Offset(right - chartSize * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * SIDE_VERTICAL_OFFSET_FRACTION)
            10 -> Offset(right - chartSize * DIAMOND_PLANET_VERTICAL_FRACTION, centerY)
            11 -> Offset(right - chartSize * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY - chartSize * SIDE_VERTICAL_OFFSET_FRACTION)
            12 -> Offset(right - chartSize * CORNER_CENTER_FRACTION, top + chartSize * CORNER_CENTER_FRACTION)
            else -> Offset(centerX, centerY)
        }
    }

    /**
     * Position for house number (INSIDE each house)
     *
     * FIX: House 1 number is now centered in the top diamond instead of being
     * pushed toward the diagonal between houses 1 and 10.
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

        return when (houseNum) {
            1 -> Offset(centerX, top + chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION)
            2 -> Offset(left + chartSize * CORNER_NUMBER_OFFSET_FRACTION, top + chartSize * CORNER_NUMBER_OFFSET_FRACTION)
            3 -> Offset(left + chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY - chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            4 -> Offset(left + chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION, centerY)
            5 -> Offset(left + chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            6 -> Offset(left + chartSize * CORNER_NUMBER_OFFSET_FRACTION, bottom - chartSize * CORNER_NUMBER_OFFSET_FRACTION)
            7 -> Offset(centerX, bottom - chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION)
            8 -> Offset(right - chartSize * CORNER_NUMBER_OFFSET_FRACTION, bottom - chartSize * CORNER_NUMBER_OFFSET_FRACTION)
            9 -> Offset(right - chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            10 -> Offset(right - chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION, centerY)
            11 -> Offset(right - chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY - chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            12 -> Offset(right - chartSize * CORNER_NUMBER_OFFSET_FRACTION, top + chartSize * CORNER_NUMBER_OFFSET_FRACTION)
            else -> Offset(centerX, centerY)
        }
    }

    private fun getHouseType(houseNum: Int): HouseType {
        return when (houseNum) {
            1, 4, 7, 10 -> HouseType.DIAMOND
            3, 5, 9, 11 -> HouseType.SIDE
            2, 6, 8, 12 -> HouseType.CORNER
            else -> HouseType.DIAMOND
        }
    }

    private enum class HouseType {
        DIAMOND,
        SIDE,
        CORNER
    }

    /**
     * Draw planets in a house
     */
    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetRenderData>,
        houseCenter: Offset,
        chartSize: Float,
        houseNum: Int
    ) {
        val houseType = getHouseType(houseNum)
        val planetCount = planets.size

        val (columns, rows) = when (houseType) {
            HouseType.DIAMOND -> when {
                planetCount >= 5 -> Pair(2, (planetCount + 1) / 2)
                else -> Pair(1, planetCount)
            }
            HouseType.SIDE -> when {
                planetCount >= 4 -> Pair(2, (planetCount + 1) / 2)
                else -> Pair(1, planetCount)
            }
            HouseType.CORNER -> when {
                planetCount >= 3 -> Pair(2, (planetCount + 1) / 2)
                else -> Pair(1, planetCount)
            }
        }

        val baseTextSize = chartSize * 0.032f
        val textSize = when {
            planetCount > 4 && houseType == HouseType.CORNER -> baseTextSize * 0.85f
            planetCount > 3 && houseType == HouseType.CORNER -> baseTextSize * 0.9f
            planetCount > 5 -> baseTextSize * 0.9f
            else -> baseTextSize
        }

        val baseLineHeight = chartSize * 0.042f
        val lineHeight = when {
            rows > 2 -> baseLineHeight * 0.85f
            else -> baseLineHeight
        }

        val columnSpacing = when (houseType) {
            HouseType.DIAMOND -> chartSize * 0.08f
            HouseType.SIDE -> chartSize * 0.065f
            HouseType.CORNER -> chartSize * 0.055f
        }

        planets.forEachIndexed { index, planet ->
            val abbrev = planet.planet.symbol
            val degree = (planet.longitude % 30.0).toInt()
            val degreeSuper = toSuperscript(degree)

            val statusIndicators = buildString {
                if (planet.isRetrograde) append(SYMBOL_RETROGRADE)
                if (planet.isExalted) {
                    append(SYMBOL_EXALTED)
                } else if (planet.isDebilitated) {
                    append(SYMBOL_DEBILITATED)
                }
                if (planet.isCombust) {
                    append(SYMBOL_COMBUST)
                }
                if (planet.isVargottama) {
                    append(SYMBOL_VARGOTTAMA)
                }
            }

            val displayText = "$abbrev$degreeSuper$statusIndicators"

            val col = if (columns > 1) index % columns else 0
            val row = index / columns

            val xOffset = if (columns > 1) {
                (col - 0.5f) * columnSpacing
            } else {
                0f
            }

            val totalRows = if (columns > 1) itemsPerColumn else planets.size
            val yOffset = (row - (totalRows - 1) / 2f) * lineHeight

            val position = Offset(houseCenter.x + xOffset, houseCenter.y + yOffset)
            val color = theme.getPlanetColor(planet.planet)

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
     * Draw centered text
     */
    private fun DrawScope.drawTextCentered(
        text: String,
        position: Offset,
        textSize: Float,
        color: Color,
        isBold: Boolean = false
    ) {
        val typeface = if (isBold) theme.boldTypeface else theme.normalTypeface
        if (textPaint.color != color.toArgb()) {
            textPaint.color = color.toArgb()
        }
        if (textPaint.textSize != textSize) {
            textPaint.textSize = textSize
        }
        if (textPaint.typeface != typeface) {
            textPaint.typeface = typeface
        }

        drawContext.canvas.nativeCanvas.apply {
            val textHeight = textPaint.descent() - textPaint.ascent()
            val textOffset = textHeight / 2 - textPaint.descent()
            drawText(text, position.x, position.y + textOffset, textPaint)
        }
    }

    fun createChartBitmap(chart: VedicChart, width: Int, height: Int, density: Density): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            density,
            LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawNorthIndianChart(chart)
        }

        return bitmap
    }

    fun createDivisionalChartBitmap(
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        chartTitle: String,
        width: Int,
        height: Int,
        density: Density,
        originalChart: VedicChart
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = CanvasDrawScope()

        drawScope.draw(
            density,
            LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawDivisionalChart(planetPositions, ascendantLongitude, originalChart)
        }

        return bitmap
    }

    /**
     * Draws a South Indian style chart. Currently, this is a backward-compatibility alias
     * to the North Indian renderer, as a true South Indian layout is not yet implemented.
     */
    fun DrawScope.drawSouthIndianChart(
        chart: VedicChart
    ) {
        drawNorthIndianChart(chart)
    }

    fun DrawScope.drawChartLegend(
        chartBottom: Float,
        chartLeft: Float,
        chartWidth: Float,
        textSize: Float
    ) {
        val legendY = chartBottom + textSize * 1.5f
        val legendItems = listOf(
            Pair("$SYMBOL_RETROGRADE Retrograde", theme.houseNumberColor),
            Pair("$SYMBOL_COMBUST Combust", theme.houseNumberColor),
            Pair("$SYMBOL_VARGOTTAMA Vargottama", theme.houseNumberColor),
            Pair("$SYMBOL_EXALTED Exalted", theme.houseNumberColor),
            Pair("$SYMBOL_DEBILITATED Debilitated", theme.houseNumberColor)
        )

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

    fun DrawScope.drawChartWithLegend(
        chart: VedicChart,
        showLegend: Boolean = true
    ) {
        val legendHeight = if (showLegend) size.height * 0.08f else 0f
        val chartHeight = size.height - legendHeight

        // A custom DrawScope to draw the chart in the upper part of the canvas
        val chartDrawScope = object : DrawScope by this {
            override val size: Size get() = Size(this@drawChartWithLegend.size.width, chartHeight)
        }
        chartDrawScope.drawNorthIndianChart(chart)

        if (showLegend) {
            val chartSize = min(size.width, chartHeight)
            val padding = chartSize * 0.02f
            val chartWidth = chartSize - (padding * 2)
            val textSize = chartSize * 0.028f
            val legendTop = chartHeight

            drawRect(
                color = theme.backgroundColor,
                topLeft = Offset(0f, legendTop),
                size = Size(size.width, legendHeight)
            )

            drawChartLegend(
                chartBottom = legendTop,
                chartLeft = padding,
                chartWidth = chartWidth,
                textSize = textSize
            )
        }
    }

    private fun DrawScope.drawLagnaMarker(
        houseCenter: Offset,
        chartSize: Float
    ) {
        val textSize = chartSize * 0.035f
        drawTextCentered(
            text = "La",
            position = Offset(houseCenter.x, houseCenter.y - chartSize * 0.06f),
            textSize = textSize,
            color = theme.lagnaColor,
            isBold = true
        )
    }
}