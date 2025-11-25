package com.astro.storm.ui.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ui.theme.*

/**
 * Professional North Indian Style Vedic Chart Renderer
 *
 * North Indian chart layout (Diamond shape):
 * - House 1 (Ascendant) at top center
 * - Houses proceed counter-clockwise
 * - Fixed house positions, signs rotate based on Ascendant
 *
 * Standard North Indian Layout:
 *             ___________
 *            |\ 12 | 1 /|
 *            | \  _|_/  |
 *            |11|/_\|2  |
 *            |__|___|___|
 *            |10| / \ |3|
 *            |  |/9 8\| |
 *            | /_____\ |
 *            |/ 6 | 5 \|
 *             -----------
 *              7 at bottom
 */
class ChartRenderer {

    companion object {
        // Modern professional color scheme from theme
        private val BACKGROUND_COLOR = ChartBackground
        private val BORDER_COLOR = ChartBorder
        private val HOUSE_LINE_COLOR = ChartHouseLine
        private val TEXT_COLOR = Color(0xFFE4E6ED)
        private val PLANET_COLOR = ChartPlanetText
        private val ASCENDANT_COLOR = ChartAscendant
        private val HOUSE_NUMBER_COLOR = ChartHouseNumber
        private val RETROGRADE_COLOR = ChartRetrogradeIndicator
    }

    /**
     * Draw North Indian style Vedic chart (Lagna/Rasi chart)
     * This is the traditional diamond-shaped chart with house 1 at top center
     */
    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna"
    ) {
        with(drawScope) {
            val center = Offset(size / 2f, size / 2f)
            val chartSize = size * 0.92f

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw outer diamond border
            drawOuterDiamond(center, chartSize)

            // Draw inner house divisions (creates 12 houses)
            drawInnerDiamond(center, chartSize)

            // Draw middle cross lines
            drawCrossLines(center, chartSize)

            // Draw house numbers (1-12)
            drawHouseNumbers(center, chartSize)

            // Draw zodiac signs in each house based on ascendant
            drawZodiacSigns(center, chartSize, chart)

            // Draw planets in their respective houses
            drawPlanets(center, chartSize, chart)

            // Draw chart title in center
            drawChartTitle(center, chartSize, chartTitle)

            // Draw ascendant marker
            drawAscendantMarker(center, chartSize)
        }
    }

    /**
     * Backward compatible method - calls the North Indian chart
     */
    fun drawSouthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float
    ) {
        drawNorthIndianChart(drawScope, chart, size, "Lagna")
    }

    private fun DrawScope.drawOuterDiamond(center: Offset, size: Float) {
        val halfSize = size / 2f
        val path = Path().apply {
            moveTo(center.x, center.y - halfSize) // Top
            lineTo(center.x + halfSize, center.y) // Right
            lineTo(center.x, center.y + halfSize) // Bottom
            lineTo(center.x - halfSize, center.y) // Left
            close()
        }
        drawPath(
            path = path,
            color = BORDER_COLOR,
            style = Stroke(width = 2.5f)
        )
    }

    private fun DrawScope.drawInnerDiamond(center: Offset, size: Float) {
        val innerSize = size / 2f
        val innerHalf = innerSize / 2f
        val path = Path().apply {
            moveTo(center.x, center.y - innerHalf) // Top
            lineTo(center.x + innerHalf, center.y) // Right
            lineTo(center.x, center.y + innerHalf) // Bottom
            lineTo(center.x - innerHalf, center.y) // Left
            close()
        }
        drawPath(
            path = path,
            color = HOUSE_LINE_COLOR,
            style = Stroke(width = 1.5f)
        )
    }

    private fun DrawScope.drawCrossLines(center: Offset, size: Float) {
        val halfSize = size / 2f

        // Horizontal line through center (left to right)
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x - halfSize, center.y),
            end = Offset(center.x + halfSize, center.y),
            strokeWidth = 1.5f
        )

        // Vertical line through center (top to bottom)
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x, center.y - halfSize),
            end = Offset(center.x, center.y + halfSize),
            strokeWidth = 1.5f
        )
    }

    private fun DrawScope.drawHouseNumbers(center: Offset, size: Float) {
        // North Indian chart house number positions
        // Houses are numbered 1-12 counter-clockwise starting from top center
        val positions = mapOf(
            1 to Offset(center.x, center.y - size * 0.42f),        // Top center
            2 to Offset(center.x + size * 0.28f, center.y - size * 0.28f),  // Top right
            3 to Offset(center.x + size * 0.42f, center.y),        // Right center
            4 to Offset(center.x + size * 0.28f, center.y + size * 0.28f),  // Bottom right
            5 to Offset(center.x, center.y + size * 0.42f),        // Bottom center
            6 to Offset(center.x - size * 0.28f, center.y + size * 0.28f),  // Bottom left
            7 to Offset(center.x - size * 0.42f, center.y),        // Left center
            8 to Offset(center.x - size * 0.28f, center.y - size * 0.28f),  // Top left
            9 to Offset(center.x - size * 0.14f, center.y - size * 0.14f),  // Inner top left
            10 to Offset(center.x + size * 0.14f, center.y - size * 0.14f), // Inner top right
            11 to Offset(center.x + size * 0.14f, center.y + size * 0.14f), // Inner bottom right
            12 to Offset(center.x - size * 0.14f, center.y + size * 0.14f)  // Inner bottom left
        )

        positions.forEach { (houseNum, position) ->
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = HOUSE_NUMBER_COLOR.toArgb()
                    textSize = size * 0.03f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.NORMAL
                    )
                    alpha = 160
                }
                drawText(houseNum.toString(), position.x, position.y + size * 0.01f, paint)
            }
        }
    }

    private fun DrawScope.drawZodiacSigns(center: Offset, size: Float, chart: VedicChart) {
        // Get the ascendant sign to determine sign placement in each house
        val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

        // Sign label positions - slightly offset from house numbers
        val signPositions = mapOf(
            1 to Offset(center.x + size * 0.06f, center.y - size * 0.36f),
            2 to Offset(center.x + size * 0.24f, center.y - size * 0.24f),
            3 to Offset(center.x + size * 0.36f, center.y + size * 0.03f),
            4 to Offset(center.x + size * 0.24f, center.y + size * 0.24f),
            5 to Offset(center.x + size * 0.06f, center.y + size * 0.38f),
            6 to Offset(center.x - size * 0.24f, center.y + size * 0.24f),
            7 to Offset(center.x - size * 0.36f, center.y + size * 0.03f),
            8 to Offset(center.x - size * 0.24f, center.y - size * 0.24f),
            9 to Offset(center.x - size * 0.10f, center.y - size * 0.08f),
            10 to Offset(center.x + size * 0.10f, center.y - size * 0.08f),
            11 to Offset(center.x + size * 0.10f, center.y + size * 0.10f),
            12 to Offset(center.x - size * 0.10f, center.y + size * 0.10f)
        )

        signPositions.forEach { (houseNum, position) ->
            // Calculate which sign is in this house
            // House 1 has the ascendant sign, subsequent houses have subsequent signs
            val signIndex = (ascendantSign.number - 1 + houseNum - 1) % 12
            val sign = ZodiacSign.entries[signIndex]

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = HOUSE_NUMBER_COLOR.toArgb()
                    textSize = size * 0.025f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.NORMAL
                    )
                    alpha = 130
                }
                drawText(sign.abbreviation, position.x, position.y, paint)
            }
        }
    }

    private fun DrawScope.drawPlanets(center: Offset, size: Float, chart: VedicChart) {
        // Group planets by house
        val planetsByHouse = chart.planetPositions.groupBy { it.house }

        // Planet rendering positions for each house
        val housePlanetAreas = mapOf(
            1 to Offset(center.x, center.y - size * 0.32f),
            2 to Offset(center.x + size * 0.20f, center.y - size * 0.20f),
            3 to Offset(center.x + size * 0.32f, center.y),
            4 to Offset(center.x + size * 0.20f, center.y + size * 0.20f),
            5 to Offset(center.x, center.y + size * 0.32f),
            6 to Offset(center.x - size * 0.20f, center.y + size * 0.20f),
            7 to Offset(center.x - size * 0.32f, center.y),
            8 to Offset(center.x - size * 0.20f, center.y - size * 0.20f),
            9 to Offset(center.x - size * 0.05f, center.y - size * 0.03f),
            10 to Offset(center.x + size * 0.05f, center.y - size * 0.03f),
            11 to Offset(center.x + size * 0.05f, center.y + size * 0.05f),
            12 to Offset(center.x - size * 0.05f, center.y + size * 0.05f)
        )

        planetsByHouse.forEach { (house, planets) ->
            if (house in 1..12) {
                val basePosition = housePlanetAreas[house] ?: return@forEach

                // Create planet text with retrograde indicator
                val planetTexts = planets.map { planet ->
                    val retrograde = if (planet.isRetrograde) "(R)" else ""
                    Pair(planet.planet.symbol + retrograde, planet.isRetrograde)
                }

                // Render planets - stack vertically if multiple in same house
                planetTexts.forEachIndexed { index, (text, isRetrograde) ->
                    val yOffset = when {
                        planetTexts.size == 1 -> 0f
                        planetTexts.size == 2 -> (index - 0.5f) * size * 0.04f
                        else -> (index - (planetTexts.size - 1) / 2f) * size * 0.035f
                    }

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = if (isRetrograde)
                                RETROGRADE_COLOR.toArgb()
                            else
                                PLANET_COLOR.toArgb()
                            textSize = size * 0.04f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.create(
                                android.graphics.Typeface.DEFAULT,
                                android.graphics.Typeface.BOLD
                            )
                        }
                        drawText(text, basePosition.x, basePosition.y + yOffset, paint)
                    }
                }
            }
        }
    }

    private fun DrawScope.drawChartTitle(center: Offset, size: Float, title: String) {
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = TEXT_COLOR.toArgb()
                textSize = size * 0.038f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD
                )
            }
            drawText(title, center.x, center.y + size * 0.015f, paint)
        }
    }

    private fun DrawScope.drawAscendantMarker(center: Offset, size: Float) {
        // Draw "Asc" indicator in house 1 area
        val position = Offset(center.x - size * 0.08f, center.y - size * 0.38f)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = ASCENDANT_COLOR.toArgb()
                textSize = size * 0.03f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD
                )
            }
            drawText("Asc", position.x, position.y, paint)
        }
    }

    /**
     * Draw a divisional chart (D9, D10, D60, etc.)
     * Used for Navamsa, Dasamsa, and other Vargas
     */
    fun drawDivisionalChart(
        drawScope: DrawScope,
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        size: Float,
        chartTitle: String
    ) {
        with(drawScope) {
            val center = Offset(size / 2f, size / 2f)
            val chartSize = size * 0.92f

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw chart structure
            drawOuterDiamond(center, chartSize)
            drawInnerDiamond(center, chartSize)
            drawCrossLines(center, chartSize)

            // Draw house numbers
            drawHouseNumbers(center, chartSize)

            // Draw chart title
            drawChartTitle(center, chartSize, chartTitle)

            // Draw ascendant marker
            drawAscendantMarker(center, chartSize)

            // Draw zodiac signs based on divisional ascendant
            val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)
            drawDivisionalSigns(center, chartSize, ascendantSign)

            // Draw planets in divisional positions
            drawDivisionalPlanets(center, chartSize, planetPositions)
        }
    }

    private fun DrawScope.drawDivisionalSigns(center: Offset, size: Float, ascendantSign: ZodiacSign) {
        val signPositions = mapOf(
            1 to Offset(center.x + size * 0.06f, center.y - size * 0.36f),
            2 to Offset(center.x + size * 0.24f, center.y - size * 0.24f),
            3 to Offset(center.x + size * 0.36f, center.y + size * 0.03f),
            4 to Offset(center.x + size * 0.24f, center.y + size * 0.24f),
            5 to Offset(center.x + size * 0.06f, center.y + size * 0.38f),
            6 to Offset(center.x - size * 0.24f, center.y + size * 0.24f),
            7 to Offset(center.x - size * 0.36f, center.y + size * 0.03f),
            8 to Offset(center.x - size * 0.24f, center.y - size * 0.24f),
            9 to Offset(center.x - size * 0.10f, center.y - size * 0.08f),
            10 to Offset(center.x + size * 0.10f, center.y - size * 0.08f),
            11 to Offset(center.x + size * 0.10f, center.y + size * 0.10f),
            12 to Offset(center.x - size * 0.10f, center.y + size * 0.10f)
        )

        signPositions.forEach { (houseNum, position) ->
            val signIndex = (ascendantSign.number - 1 + houseNum - 1) % 12
            val sign = ZodiacSign.entries[signIndex]

            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = HOUSE_NUMBER_COLOR.toArgb()
                    textSize = size * 0.025f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.NORMAL
                    )
                    alpha = 130
                }
                drawText(sign.abbreviation, position.x, position.y, paint)
            }
        }
    }

    private fun DrawScope.drawDivisionalPlanets(
        center: Offset,
        size: Float,
        planetPositions: List<PlanetPosition>
    ) {
        val planetsByHouse = planetPositions.groupBy { it.house }

        val housePlanetAreas = mapOf(
            1 to Offset(center.x, center.y - size * 0.32f),
            2 to Offset(center.x + size * 0.20f, center.y - size * 0.20f),
            3 to Offset(center.x + size * 0.32f, center.y),
            4 to Offset(center.x + size * 0.20f, center.y + size * 0.20f),
            5 to Offset(center.x, center.y + size * 0.32f),
            6 to Offset(center.x - size * 0.20f, center.y + size * 0.20f),
            7 to Offset(center.x - size * 0.32f, center.y),
            8 to Offset(center.x - size * 0.20f, center.y - size * 0.20f),
            9 to Offset(center.x - size * 0.05f, center.y - size * 0.03f),
            10 to Offset(center.x + size * 0.05f, center.y - size * 0.03f),
            11 to Offset(center.x + size * 0.05f, center.y + size * 0.05f),
            12 to Offset(center.x - size * 0.05f, center.y + size * 0.05f)
        )

        planetsByHouse.forEach { (house, planets) ->
            if (house in 1..12) {
                val basePosition = housePlanetAreas[house] ?: return@forEach

                val planetTexts = planets.map { planet ->
                    Pair(
                        planet.planet.symbol + if (planet.isRetrograde) "(R)" else "",
                        planet.isRetrograde
                    )
                }

                planetTexts.forEachIndexed { index, (text, isRetrograde) ->
                    val yOffset = when {
                        planetTexts.size == 1 -> 0f
                        planetTexts.size == 2 -> (index - 0.5f) * size * 0.04f
                        else -> (index - (planetTexts.size - 1) / 2f) * size * 0.035f
                    }

                    drawContext.canvas.nativeCanvas.apply {
                        val paint = android.graphics.Paint().apply {
                            color = if (isRetrograde)
                                RETROGRADE_COLOR.toArgb()
                            else
                                PLANET_COLOR.toArgb()
                            textSize = size * 0.04f
                            textAlign = android.graphics.Paint.Align.CENTER
                            typeface = android.graphics.Typeface.create(
                                android.graphics.Typeface.DEFAULT,
                                android.graphics.Typeface.BOLD
                            )
                        }
                        drawText(text, basePosition.x, basePosition.y + yOffset, paint)
                    }
                }
            }
        }
    }

    /**
     * Create a bitmap from the Lagna chart for export
     */
    fun createChartBitmap(chart: VedicChart, width: Int, height: Int): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = androidx.compose.ui.graphics.drawscope.CanvasDrawScope()

        drawScope.draw(
            androidx.compose.ui.unit.Density(1f),
            androidx.compose.ui.unit.LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawNorthIndianChart(this, chart, minOf(width, height).toFloat())
        }

        return bitmap
    }

    /**
     * Create bitmap for divisional chart export
     */
    fun createDivisionalChartBitmap(
        planetPositions: List<PlanetPosition>,
        ascendantLongitude: Double,
        chartTitle: String,
        width: Int,
        height: Int
    ): android.graphics.Bitmap {
        val bitmap = android.graphics.Bitmap.createBitmap(width, height, android.graphics.Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)
        val drawScope = androidx.compose.ui.graphics.drawscope.CanvasDrawScope()

        drawScope.draw(
            androidx.compose.ui.unit.Density(1f),
            androidx.compose.ui.unit.LayoutDirection.Ltr,
            Canvas(canvas),
            Size(width.toFloat(), height.toFloat())
        ) {
            drawDivisionalChart(
                this,
                planetPositions,
                ascendantLongitude,
                minOf(width, height).toFloat(),
                chartTitle
            )
        }

        return bitmap
    }
}
