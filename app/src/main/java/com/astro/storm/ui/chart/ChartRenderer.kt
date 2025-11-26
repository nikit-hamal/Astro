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
import com.astro.storm.data.model.*
import com.astro.storm.ephemeris.ChartCalculator
import kotlin.math.abs
import kotlin.math.min

/**
 * Professional North Indian Style Vedic Chart Renderer.
 *
 * This class is a stateless renderer responsible for drawing a Vedic chart based on the
 * provided `ChartData` model. It accurately renders the North Indian style layout,
 * displaying sign numbers in their correct houses and placing planets accordingly.
 * It is designed to be independent of chart calculation logic.
 */
class ChartRenderer {

    companion object {
        private val BACKGROUND_COLOR = Color(0xFFD4C4A8)
        private val BORDER_COLOR = Color(0xFFB8860B)
        private val HOUSE_NUMBER_COLOR = Color(0xFF4A4A4A)
        private val LAGNA_COLOR = Color(0xFF8B4513)

        private val SUN_COLOR = Color(0xFFD2691E)
        private val MOON_COLOR = Color(0xFFDC143C)
        private val MARS_COLOR = Color(0xFFDC143C)
        private val MERCURY_COLOR = Color(0xFF228B22)
        private val JUPITER_COLOR = Color(0xFFDAA520)
        private val VENUS_COLOR = Color(0xFF9370DB)
        private val SATURN_COLOR = Color(0xFF4169E1)
        private val RAHU_COLOR = Color(0xFF8B0000)
        private val KETU_COLOR = Color(0xFF8B0000)
        private val URANUS_COLOR = Color(0xFF20B2AA)
        private val NEPTUNE_COLOR = Color(0xFF4682B4)
        private val PLUTO_COLOR = Color(0xFF800080)

        const val SYMBOL_RETROGRADE = "*"
        const val SYMBOL_COMBUST = "^"
        const val SYMBOL_VARGOTTAMA = "¤"
        const val SYMBOL_EXALTED = "↑"
        const val SYMBOL_DEBILITATED = "↓"
    }

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

    private fun toSuperscript(degree: Int): String {
        val superscripts = mapOf(
            '0' to '\u2070', '1' to '\u00B9', '2' to '\u00B2', '3' to '\u00B3',
            '4' to '\u2074', '5' to '\u2075', '6' to '\u2076', '7' to '\u2077',
            '8' to '\u2078', '9' to '\u2079'
        )
        return degree.toString().map { superscripts[it] ?: it }.joinToString("")
    }

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

    private fun isVargottama(planet: PlanetPosition, navamsaPlanetPositions: Map<Planet, PlanetPosition>): Boolean {
        val navamsaPosition = navamsaPlanetPositions[planet.planet] ?: return false
        return planet.sign == navamsaPosition.sign
    }

    private fun isCombust(planet: PlanetPosition, sunPosition: PlanetPosition?): Boolean {
        if (planet.planet == Planet.SUN || sunPosition == null) return false
        if (planet.planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) return false

        val angularDistance = abs(planet.longitude - sunPosition.longitude).let { if (it > 180) 360 - it else it }
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
     * Draws a complete chart with an optional legend.
     * This is the primary entry point for rendering a chart.
     */
    fun drawChartWithLegend(
        drawScope: DrawScope,
        chartData: ChartData,
        vedicChart: VedicChart?, // Needed for Vargottama/Combust checks
        size: Float,
        showLegend: Boolean = true
    ) {
        with(drawScope) {
            val legendHeight = if (showLegend) size * 0.08f else 0f
            val chartSize = size - legendHeight

            // Pre-calculate Navamsa chart for performance
            val navamsaPlanetPositions = vedicChart?.let {
                ChartCalculator().calculateNavamsaChart(it).houses.flatMap { it.planets }.associateBy { it.planet }
            } ?: emptyMap()

            drawNorthIndianChart(this, chartData, vedicChart, navamsaPlanetPositions, chartSize)

            if (showLegend) {
                val padding = chartSize * 0.02f
                drawChartLegend(
                    chartBottom = chartSize,
                    chartLeft = padding,
                    chartWidth = chartSize - (padding * 2),
                    textSize = chartSize * 0.028f
                )
            }
        }
    }

    /**
     * Draws the main North Indian style chart structure and contents.
     */
    private fun drawNorthIndianChart(
        drawScope: DrawScope,
        chartData: ChartData,
        vedicChart: VedicChart?,
        navamsaPlanetPositions: Map<Planet, PlanetPosition>,
        size: Float
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

            drawRect(color = BACKGROUND_COLOR, size = Size(size, size))
            drawRect(color = BORDER_COLOR, topLeft = Offset(left, top), size = Size(chartSize, chartSize), style = Stroke(width = 3f))

            val midTop = Offset(centerX, top)
            val midRight = Offset(right, centerY)
            val midBottom = Offset(centerX, bottom)
            val midLeft = Offset(left, centerY)

            drawLine(BORDER_COLOR, midTop, midRight, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midRight, midBottom, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midBottom, midLeft, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, midLeft, midTop, strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, Offset(left, top), Offset(right, bottom), strokeWidth = 2.5f)
            drawLine(BORDER_COLOR, Offset(right, top), Offset(left, bottom), strokeWidth = 2.5f)

            drawAllHouseContents(left, top, chartSize, centerX, centerY, chartData, vedicChart, navamsaPlanetPositions, size)
        }
    }

    private fun DrawScope.drawAllHouseContents(
        left: Float, top: Float, chartSize: Float, centerX: Float, centerY: Float,
        chartData: ChartData, vedicChart: VedicChart?, navamsaPlanetPositions: Map<Planet, PlanetPosition>, size: Float
    ) {
        val sunPosition = vedicChart?.planetPositions?.find { it.planet == Planet.SUN }

        chartData.houses.forEach { chartHouse ->
            val houseCenter = getHousePlanetCenter(chartHouse.houseNumber, left, top, chartSize, centerX, centerY)
            val numberPos = getHouseNumberPosition(chartHouse.houseNumber, left, top, chartSize, centerX, centerY)

            // Draw the SIGN number (e.g., 1 for Aries, 12 for Pisces)
            drawTextCentered(
                text = chartHouse.sign.number.toString(),
                position = numberPos,
                textSize = size * 0.035f,
                color = HOUSE_NUMBER_COLOR,
                isBold = false
            )

            if (chartHouse.houseNumber == 1) {
                val lagnaMarkerPos = Offset(centerX, top + chartSize * 0.15f)
                drawTextCentered(
                    text = "La",
                    position = lagnaMarkerPos,
                    textSize = size * 0.035f,
                    color = LAGNA_COLOR,
                    isBold = true
                )
            }

            if (chartHouse.planets.isNotEmpty()) {
                drawPlanetsInHouse(chartHouse.planets, houseCenter, size, chartHouse.houseNumber, sunPosition, navamsaPlanetPositions)
            }
        }
    }

    private fun getHousePlanetCenter(houseNum: Int, left: Float, top: Float, chartSize: Float, centerX: Float, centerY: Float): Offset {
        val right = left + chartSize; val bottom = top + chartSize
        val cornerOffset = chartSize * 0.18f
        val sideOffset = chartSize * 0.15f
        val diamondOffset = chartSize * 0.25f
        return when (houseNum) {
            1 -> Offset(centerX, top + diamondOffset)
            2 -> Offset(left + cornerOffset, top + cornerOffset)
            3 -> Offset(left + sideOffset, centerY - chartSize * 0.18f)
            4 -> Offset(left + diamondOffset, centerY)
            5 -> Offset(left + sideOffset, centerY + chartSize * 0.18f)
            6 -> Offset(left + cornerOffset, bottom - cornerOffset)
            7 -> Offset(centerX, bottom - diamondOffset)
            8 -> Offset(right - cornerOffset, bottom - cornerOffset)
            9 -> Offset(right - sideOffset, centerY + chartSize * 0.18f)
            10 -> Offset(right - diamondOffset, centerY)
            11 -> Offset(right - sideOffset, centerY - chartSize * 0.18f)
            12 -> Offset(right - cornerOffset, top + cornerOffset)
            else -> Offset(centerX, centerY)
        }
    }

    private fun getHouseNumberPosition(houseNum: Int, left: Float, top: Float, chartSize: Float, centerX: Float, centerY: Float): Offset {
        val cornerNumberOffset = chartSize * 0.12f
        val sideNumberOffset = chartSize * 0.08f
        val diamondNumberOffset = chartSize * 0.38f
        return when (houseNum) {
            1 -> Offset(centerX + chartSize * 0.12f, top + diamondNumberOffset)
            2 -> Offset(centerX - chartSize * 0.18f, top + cornerNumberOffset)
            3 -> Offset(left + sideNumberOffset, centerY - chartSize * 0.28f)
            4 -> Offset(left + diamondNumberOffset - chartSize * 0.12f, centerY + chartSize * 0.08f)
            5 -> Offset(left + sideNumberOffset, centerY + chartSize * 0.28f)
            6 -> Offset(centerX - chartSize * 0.18f, bottom - cornerNumberOffset)
            7 -> Offset(centerX, bottom - diamondNumberOffset)
            8 -> Offset(centerX + chartSize * 0.18f, bottom - cornerNumberOffset)
            9 -> Offset(right - sideNumberOffset, centerY + chartSize * 0.28f)
            10 -> Offset(right - diamondNumberOffset + chartSize * 0.12f, centerY + chartSize * 0.08f)
            11 -> Offset(right - sideNumberOffset, centerY - chartSize * 0.28f)
            12 -> Offset(centerX + chartSize * 0.18f, top + cornerNumberOffset)
            else -> Offset(centerX, centerY)
        }
    }

    private enum class HouseType { DIAMOND, SIDE, CORNER }
    private fun getHouseType(houseNum: Int): HouseType {
        return when (houseNum) {
            1, 4, 7, 10 -> HouseType.DIAMOND
            3, 5, 9, 11 -> HouseType.SIDE
            2, 6, 8, 12 -> HouseType.CORNER
            else -> HouseType.DIAMOND
        }
    }

    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetPosition>, houseCenter: Offset, size: Float, houseNum: Int,
        sunPosition: PlanetPosition?, navamsaPlanetPositions: Map<Planet, PlanetPosition>
    ) {
        val houseType = getHouseType(houseNum)
        val baseTextSize = size * 0.032f
        val textSize = when {
            planets.size > 4 && houseType == HouseType.CORNER -> baseTextSize * 0.85f
            planets.size > 3 && houseType == HouseType.CORNER -> baseTextSize * 0.9f
            planets.size > 5 -> baseTextSize * 0.9f
            else -> baseTextSize
        }
        val lineHeight = size * 0.042f * when (houseType) {
            HouseType.CORNER -> 0.85f; HouseType.SIDE -> 0.9f; else -> 1f
        }
        val columns = when {
            houseType == HouseType.CORNER && planets.size >= 3 -> 2
            houseType == HouseType.SIDE && planets.size >= 4 -> 2
            houseType == HouseType.DIAMOND && planets.size >= 5 -> 2
            else -> 1
        }
        val columnSpacing = size * when (houseType) {
            HouseType.CORNER -> 0.055f; HouseType.SIDE -> 0.065f; else -> 0.08f
        }

        planets.forEachIndexed { index, planet ->
            val status = buildString {
                if (planet.isRetrograde) append(SYMBOL_RETROGRADE)
                if (isExalted(planet.planet, planet.sign)) append(SYMBOL_EXALTED)
                else if (isDebilitated(planet.planet, planet.sign)) append(SYMBOL_DEBILITATED)
                if (isCombust(planet, sunPosition)) append(SYMBOL_COMBUST)
                if (isVargottama(planet, navamsaPlanetPositions)) append(SYMBOL_VARGOTTAMA)
            }
            val degree = (planet.longitude % 30.0).toInt()
            val displayText = "${getPlanetAbbreviation(planet.planet)}${toSuperscript(degree)}$status"

            val col = index % columns
            val row = index / columns
            val xOffset = if (columns > 1) (col - 0.5f) * columnSpacing else 0f
            val totalRows = (planets.size + columns - 1) / columns
            val yOffset = (row - (totalRows - 1) / 2f) * lineHeight

            drawTextCentered(displayText, Offset(houseCenter.x + xOffset, houseCenter.y + yOffset), textSize, getPlanetColor(planet.planet), true)
        }
    }

    private fun DrawScope.drawTextCentered(text: String, position: Offset, textSize: Float, color: Color, isBold: Boolean = false) {
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                this.color = color.toArgb()
                this.textSize = textSize
                this.textAlign = android.graphics.Paint.Align.CENTER
                this.typeface = Typeface.create(Typeface.SANS_SERIF, if (isBold) Typeface.BOLD else Typeface.NORMAL)
                this.isAntiAlias = true
            }
            val textOffset = (paint.descent() - paint.ascent()) / 2 - paint.descent()
            drawText(text, position.x, position.y + textOffset, paint)
        }
    }

    /**
     * Creates a bitmap from the chart data for export.
     */
    fun createChartBitmap(chartData: ChartData, vedicChart: VedicChart?, width: Int, height: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        CanvasDrawScope().draw(Density(1f), LayoutDirection.Ltr, Canvas(canvas), Size(width.toFloat(), height.toFloat())) {
            drawChartWithLegend(this, chartData, vedicChart, min(width, height).toFloat())
        }
        return bitmap
    }

    private fun DrawScope.drawChartLegend(chartBottom: Float, chartLeft: Float, chartWidth: Float, textSize: Float) {
        val legendY = chartBottom + textSize * 1.5f
        val legendItems = listOf(
            "$SYMBOL_RETROGRADE Retrograde", "$SYMBOL_COMBUST Combust", "$SYMBOL_VARGOTTAMA Vargottama",
            "$SYMBOL_EXALTED Exalted", "$SYMBOL_DEBILITATED Debilitated"
        )
        val itemWidth = chartWidth / legendItems.size
        legendItems.forEachIndexed { index, text ->
            val xPos = chartLeft + (index * itemWidth) + (itemWidth / 2)
            drawTextCentered(text, Offset(xPos, legendY), textSize * 0.8f, HOUSE_NUMBER_COLOR, false)
        }
    }
}
