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
class ChartRenderer {

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


    companion object {
        // Typefaces for text rendering
        private val TYPEFACE_NORMAL = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
        private val TYPEFACE_BOLD = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)

        // Professional color palette matching traditional Vedic astrology software
        private val BACKGROUND_COLOR = Color(0xFFD4C4A8) // Warm parchment background
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
        private val LAGNA_COLOR = Color(0xFF8B4513) // Saddle brown for Lagna marker

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
     * Check if a planet is Vargottama (same sign in D1 Rashi and D9 Navamsa charts)
     */
    private fun isVargottama(planet: PlanetPosition, chart: VedicChart): Boolean {
        val navamsaLongitude = calculateNavamsaLongitude(planet.longitude)
        val navamsaSign = ZodiacSign.fromLongitude(navamsaLongitude)
        return planet.sign == navamsaSign
    }

    /**
     * Calculate Navamsa longitude for a given longitude
     */
    private fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val navamsaPart = (degreeInSign / NAVAMSA_PART_DEGREES).toInt().coerceIn(0, 8) // 0-8

        val startingSignIndex = when (signNumber % 3) {
            0 -> signNumber              // Movable: start from same sign
            1 -> (signNumber + 8) % 12   // Fixed: start from 9th sign
            2 -> (signNumber + 4) % 12   // Dual: start from 5th sign
            else -> signNumber
        }

        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12

        val positionInNavamsa = degreeInSign % NAVAMSA_PART_DEGREES
        val navamsaDegree = (positionInNavamsa / NAVAMSA_PART_DEGREES) * 30.0

        return (navamsaSignIndex * 30.0) + navamsaDegree
    }

    /**
     * Check if a planet is combust (too close to the Sun)
     */
    private fun isCombust(planet: PlanetPosition, sunPosition: PlanetPosition?): Boolean {
        if (planet.planet == Planet.SUN) return false
        if (planet.planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return false
        }
        if (sunPosition == null) return false

        val angularDistance = calculateAngularDistance(planet.longitude, sunPosition.longitude)

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
     */
    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }

    private data class ChartFrame(
        val left: Float,
        val top: Float,
        val size: Float,
        val centerX: Float,
        val centerY: Float
    )

    private fun DrawScope.drawNorthIndianFrame(
        size: Float
    ): ChartFrame {
        val padding = size * 0.02f
        val chartSize = size - (padding * 2)
        val left = padding
        val top = padding
        val right = left + chartSize
        val bottom = top + chartSize
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2

        // Background
        drawRect(
            color = BACKGROUND_COLOR,
            size = Size(size, size)
        )

        // Outer square
        drawRect(
            color = BORDER_COLOR,
            topLeft = Offset(left, top),
            size = Size(chartSize, chartSize),
            style = Stroke(width = 3f)
        )

        // Midpoints (diamond)
        val midTop = Offset(centerX, top)
        val midRight = Offset(right, centerY)
        val midBottom = Offset(centerX, bottom)
        val midLeft = Offset(left, centerY)

        // Central diamond
        drawLine(BORDER_COLOR, midTop, midRight, strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, midRight, midBottom, strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, midBottom, midLeft, strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, midLeft, midTop, strokeWidth = 2.5f)

        // Corner diagonals
        drawLine(BORDER_COLOR, Offset(left, top), Offset(right, bottom), strokeWidth = 2.5f)
        drawLine(BORDER_COLOR, Offset(right, top), Offset(left, bottom), strokeWidth = 2.5f)

        return ChartFrame(left, top, chartSize, centerX, centerY)
    }

    /**
     * Draw a professional North Indian style Vedic chart
     */
    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna"
    ) {
        with(drawScope) {
            val frame = drawNorthIndianFrame(size)
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

            drawAllHouseContents(
                left = frame.left,
                top = frame.top,
                chartSize = frame.size,
                centerX = frame.centerX,
                centerY = frame.centerY,
                ascendantSign = ascendantSign,
                planetPositions = chart.planetPositions,
                size = size,
                chart = chart
            )
        }
    }

    /**
     * Draw a divisional chart (D9, D10, etc.)
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
            val frame = drawNorthIndianFrame(size)
            val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)

            drawAllHouseContents(
                left = frame.left,
                top = frame.top,
                chartSize = frame.size,
                centerX = frame.centerX,
                centerY = frame.centerY,
                ascendantSign = ascendantSign,
                planetPositions = planetPositions,
                size = size,
                chart = originalChart
            )
        }
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
        planetPositions: List<PlanetPosition>,
        size: Float,
        chart: VedicChart? = null,
        showSignNumbers: Boolean = true
    ) {
        val right = left + chartSize
        val bottom = top + chartSize

        val planetsByHouse = planetPositions.groupBy { it.house }
        val sunPosition = chart?.planetPositions?.find { it.planet == Planet.SUN }

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
                textSize = size * 0.035f,
                color = HOUSE_NUMBER_COLOR,
                isBold = false
            )

            // Lagna marker in House 1: position it slightly above the planet center
            if (houseNum == 1) {
                drawLagnaMarker(houseCenter, size)
            }

            // Planets
            val planets = planetsByHouse[houseNum] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCenter, size, houseNum, chart, sunPosition)
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
            // House 1 now centered horizontally in the diamond
            1 -> Offset(centerX, top + chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION)
            2 -> Offset(centerX - chartSize * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, top + chartSize * CORNER_NUMBER_OFFSET_FRACTION)
            3 -> Offset(left + chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY - chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            4 -> Offset(left + chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION - chartSize * DIAMOND_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * DIAMOND_NUMBER_VERTICAL_OFFSET_FRACTION)
            5 -> Offset(left + chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            6 -> Offset(centerX - chartSize * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, bottom - chartSize * CORNER_NUMBER_OFFSET_FRACTION)
            7 -> Offset(centerX, bottom - chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION)
            8 -> Offset(centerX + chartSize * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, bottom - chartSize * CORNER_NUMBER_OFFSET_FRACTION)
            9 -> Offset(right - chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            10 -> Offset(right - chartSize * DIAMOND_NUMBER_VERTICAL_FRACTION + chartSize * DIAMOND_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + chartSize * DIAMOND_NUMBER_VERTICAL_OFFSET_FRACTION)
            11 -> Offset(right - chartSize * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY - chartSize * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
            12 -> Offset(centerX + chartSize * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, top + chartSize * CORNER_NUMBER_OFFSET_FRACTION)
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
        planets: List<PlanetPosition>,
        houseCenter: Offset,
        size: Float,
        houseNum: Int,
        chart: VedicChart? = null,
        sunPosition: PlanetPosition? = null
    ) {
        val houseType = getHouseType(houseNum)
        val baseTextSize = size * 0.032f
        val textSize = when {
            planets.size > 4 && houseType == HouseType.CORNER -> baseTextSize * 0.85f
            planets.size > 3 && houseType == HouseType.CORNER -> baseTextSize * 0.9f
            planets.size > 5 -> baseTextSize * 0.9f
            else -> baseTextSize
        }

        val baseLineHeight = size * 0.042f
        val lineHeight = when (houseType) {
            HouseType.CORNER -> baseLineHeight * 0.85f
            HouseType.SIDE -> baseLineHeight * 0.9f
            HouseType.DIAMOND -> baseLineHeight
        }

        val columns = when {
            houseType == HouseType.CORNER && planets.size >= 3 -> 2
            houseType == HouseType.SIDE && planets.size >= 4 -> 2
            houseType == HouseType.DIAMOND && planets.size >= 5 -> 2
            else -> 1
        }

        val itemsPerColumn = (planets.size + columns - 1) / columns

        val columnSpacing = when (houseType) {
            HouseType.CORNER -> size * 0.055f
            HouseType.SIDE -> size * 0.065f
            HouseType.DIAMOND -> size * 0.08f
        }

        planets.forEachIndexed { index, planet ->
            val abbrev = planet.planet.symbol
            val degree = (planet.longitude % 30.0).toInt()
            val degreeSuper = toSuperscript(degree)

            val statusIndicators = buildString {
                if (planet.isRetrograde) append(SYMBOL_RETROGRADE)
                if (isExalted(planet.planet, planet.sign)) {
                    append(SYMBOL_EXALTED)
                } else if (isDebilitated(planet.planet, planet.sign)) {
                    append(SYMBOL_DEBILITATED)
                }
                if (chart != null && isCombust(planet, sunPosition)) {
                    append(SYMBOL_COMBUST)
                }
                if (chart != null && isVargottama(planet, chart)) {
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
     * Draw centered text
     */
    private fun DrawScope.drawTextCentered(
        text: String,
        position: Offset,
        textSize: Float,
        color: Color,
        isBold: Boolean = false
    ) {
        drawContext.canvas.nativeCanvas.apply {
            textPaint.color = color.toArgb()
            textPaint.textSize = textSize
            textPaint.typeface = if (isBold) TYPEFACE_BOLD else TYPEFACE_NORMAL

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
            drawNorthIndianChart(this, chart, min(width, height).toFloat())
        }

        return bitmap
    }

    fun createDivisionalChartBitmap(
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        chartTitle: String,
        width: Int,
        height: Int,
        density: Density
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
            drawDivisionalChart(this, planetPositions, ascendantLongitude, min(width, height).toFloat(), chartTitle)
        }

        return bitmap
    }

    /**
     * Draws a South Indian style chart. Currently, this is a backward-compatibility alias
     * to the North Indian renderer, as a true South Indian layout is not yet implemented.
     */
    fun drawSouthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float
    ) {
        drawNorthIndianChart(drawScope, chart, size, "Lagna")
    }

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

    fun drawChartWithLegend(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna",
        showLegend: Boolean = true
    ) {
        with(drawScope) {
            val legendHeight = if (showLegend) size * 0.08f else 0f
            val chartSize = size - legendHeight

            drawNorthIndianChart(this, chart, chartSize, chartTitle)

            if (showLegend) {
                val padding = chartSize * 0.02f
                val chartBottom = chartSize - padding
                val chartLeft = padding
                val chartWidth = chartSize - (padding * 2)
                val textSize = chartSize * 0.028f

                drawRect(
                    color = BACKGROUND_COLOR,
                    topLeft = Offset(0f, chartSize),
                    size = Size(size, legendHeight)
                )

                drawChartLegend(
                    chartBottom = chartSize,
                    chartLeft = padding,
                    chartWidth = chartWidth,
                    textSize = textSize
                )
            }
        }
    }

    private fun DrawScope.drawLagnaMarker(
        houseCenter: Offset,
        size: Float
    ) {
        val textSize = size * 0.035f
        drawTextCentered(
            text = "La",
            position = Offset(houseCenter.x, houseCenter.y - size * 0.06f),
            textSize = textSize,
            color = LAGNA_COLOR,
            isBold = true
        )
    }
}