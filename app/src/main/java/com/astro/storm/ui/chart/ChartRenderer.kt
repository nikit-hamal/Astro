package com.astro.storm.ui.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign

/**
 * Professional North Indian Style Vedic Chart Renderer
 *
 * Standard North Indian Diamond Chart Layout:
 *
 *                    House 12    House 1 (Asc)
 *                         \    /
 *                   House 11 \/  House 2
 *                           /\
 *           House 10 ------/  \------ House 3
 *                   \  House 9  House 10  /
 *                    \    /\    /
 *                     \  /  \  /
 *           House 9 ---\/----\/--- House 4
 *                      /\    /\
 *                     /  \  /  \
 *                    / H8  \/  H5 \
 *           House 8 /------/\------\ House 5
 *                        H7   H6
 *                     House 7    House 6
 *
 * The chart is a diamond (square rotated 45°) with:
 * - 4 corner triangles (Houses 1, 4, 7, 10 - Kendra houses)
 * - 8 side triangles (remaining houses)
 * - Center diamond for chart name
 */
class ChartRenderer {

    companion object {
        // Dark brown theme colors
        private val BACKGROUND_COLOR = Color(0xFF231A15)
        private val BORDER_COLOR = Color(0xFFB8A99A)
        private val HOUSE_LINE_COLOR = Color(0xFF5A4A40)
        private val TEXT_COLOR = Color(0xFFE8DFD6)
        private val PLANET_COLOR = Color(0xFFE5C46C)
        private val ASCENDANT_COLOR = Color(0xFFFF8A80)
        private val HOUSE_NUMBER_COLOR = Color(0xFF8A7A6A)
        private val RETROGRADE_COLOR = Color(0xFFFFB4AB)
        private val SIGN_COLOR = Color(0xFF6A5A50)
    }

    /**
     * Draw North Indian style Vedic chart
     */
    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna"
    ) {
        with(drawScope) {
            val center = Offset(size / 2f, size / 2f)
            val chartSize = size * 0.88f
            val half = chartSize / 2f

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw outer square (rotated 45° = diamond)
            val outerPath = Path().apply {
                moveTo(center.x, center.y - half)       // Top
                lineTo(center.x + half, center.y)       // Right
                lineTo(center.x, center.y + half)       // Bottom
                lineTo(center.x - half, center.y)       // Left
                close()
            }
            drawPath(outerPath, BORDER_COLOR, style = Stroke(width = 2f))

            // Draw inner square (center diamond for chart title)
            val innerHalf = half * 0.35f
            val innerPath = Path().apply {
                moveTo(center.x, center.y - innerHalf)
                lineTo(center.x + innerHalf, center.y)
                lineTo(center.x, center.y + innerHalf)
                lineTo(center.x - innerHalf, center.y)
                close()
            }
            drawPath(innerPath, HOUSE_LINE_COLOR, style = Stroke(width = 1.5f))

            // Draw diagonal lines from corners to inner diamond
            // Top to inner-top
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x, center.y - innerHalf), strokeWidth = 1.5f)
            // Right to inner-right
            drawLine(HOUSE_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x + innerHalf, center.y), strokeWidth = 1.5f)
            // Bottom to inner-bottom
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x, center.y + innerHalf), strokeWidth = 1.5f)
            // Left to inner-left
            drawLine(HOUSE_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x - innerHalf, center.y), strokeWidth = 1.5f)

            // Draw lines from outer corners to inner corners (creating triangular houses)
            // Top-left corner to inner
            drawLine(HOUSE_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x, center.y - innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x - innerHalf, center.y), strokeWidth = 1f)

            // Top-right corner to inner
            drawLine(HOUSE_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x, center.y - innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x + innerHalf, center.y), strokeWidth = 1f)

            // Bottom-right corner to inner
            drawLine(HOUSE_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x, center.y + innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x + innerHalf, center.y), strokeWidth = 1f)

            // Bottom-left corner to inner
            drawLine(HOUSE_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x, center.y + innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x - innerHalf, center.y), strokeWidth = 1f)

            // Get ascendant sign for placement
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

            // Draw house numbers, zodiac signs, and planets
            drawHouseContents(center, half, innerHalf, ascendantSign, chart.planetPositions)

            // Draw chart title in center
            drawTextCentered(chartTitle, center, size * 0.04f, TEXT_COLOR, isBold = true)

            // Draw Asc marker in house 1
            val ascPos = getHouseCenter(1, center, half, innerHalf)
            drawTextCentered("Asc", Offset(ascPos.x - half * 0.2f, ascPos.y - half * 0.15f), size * 0.028f, ASCENDANT_COLOR, isBold = true)
        }
    }

    private fun DrawScope.drawHouseContents(
        center: Offset,
        half: Float,
        innerHalf: Float,
        ascendantSign: ZodiacSign,
        planetPositions: List<PlanetPosition>
    ) {
        val size = half * 2

        // Group planets by house
        val planetsByHouse = planetPositions.groupBy { it.house }

        for (houseNum in 1..12) {
            val houseCenter = getHouseCenter(houseNum, center, half, innerHalf)

            // Calculate sign for this house
            val signIndex = (ascendantSign.number - 1 + houseNum - 1) % 12
            val sign = ZodiacSign.entries[signIndex]

            // Draw house number (small, in corner)
            val numOffset = getHouseNumberOffset(houseNum, half)
            drawTextCentered(
                houseNum.toString(),
                Offset(houseCenter.x + numOffset.x, houseCenter.y + numOffset.y),
                size * 0.025f,
                HOUSE_NUMBER_COLOR,
                isBold = false,
                alpha = 180
            )

            // Draw sign abbreviation
            val signOffset = getSignOffset(houseNum, half)
            drawTextCentered(
                sign.abbreviation,
                Offset(houseCenter.x + signOffset.x, houseCenter.y + signOffset.y),
                size * 0.022f,
                SIGN_COLOR,
                isBold = false,
                alpha = 150
            )

            // Draw planets in this house
            val planets = planetsByHouse[houseNum] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCenter, size, houseNum)
            }
        }
    }

    private fun getHouseCenter(houseNum: Int, center: Offset, half: Float, innerHalf: Float): Offset {
        // Calculate center position for each house
        // Houses 1, 4, 7, 10 are corner triangles
        // Other houses are side triangles

        val cornerDist = half * 0.55f  // Distance for corner houses
        val sideDist = half * 0.5f     // Distance for side houses
        val diagDist = half * 0.38f    // Diagonal distance for corner-adjacent houses

        return when (houseNum) {
            1 -> Offset(center.x, center.y - cornerDist)                    // Top
            2 -> Offset(center.x + diagDist, center.y - diagDist)           // Top-right inner
            3 -> Offset(center.x + sideDist, center.y - sideDist * 0.15f)   // Right-upper
            4 -> Offset(center.x + cornerDist, center.y)                    // Right
            5 -> Offset(center.x + sideDist, center.y + sideDist * 0.15f)   // Right-lower
            6 -> Offset(center.x + diagDist, center.y + diagDist)           // Bottom-right inner
            7 -> Offset(center.x, center.y + cornerDist)                    // Bottom
            8 -> Offset(center.x - diagDist, center.y + diagDist)           // Bottom-left inner
            9 -> Offset(center.x - sideDist, center.y + sideDist * 0.15f)   // Left-lower
            10 -> Offset(center.x - cornerDist, center.y)                   // Left
            11 -> Offset(center.x - sideDist, center.y - sideDist * 0.15f)  // Left-upper
            12 -> Offset(center.x - diagDist, center.y - diagDist)          // Top-left inner
            else -> center
        }
    }

    private fun getHouseNumberOffset(houseNum: Int, half: Float): Offset {
        val small = half * 0.12f
        return when (houseNum) {
            1 -> Offset(0f, -small * 1.5f)
            2 -> Offset(small, -small)
            3 -> Offset(small * 1.5f, 0f)
            4 -> Offset(small * 1.5f, 0f)
            5 -> Offset(small * 1.5f, 0f)
            6 -> Offset(small, small)
            7 -> Offset(0f, small * 1.5f)
            8 -> Offset(-small, small)
            9 -> Offset(-small * 1.5f, 0f)
            10 -> Offset(-small * 1.5f, 0f)
            11 -> Offset(-small * 1.5f, 0f)
            12 -> Offset(-small, -small)
            else -> Offset(0f, 0f)
        }
    }

    private fun getSignOffset(houseNum: Int, half: Float): Offset {
        val small = half * 0.08f
        return when (houseNum) {
            1 -> Offset(small * 2f, -small)
            2 -> Offset(small, small * 0.5f)
            3 -> Offset(small, small)
            4 -> Offset(0f, small * 2f)
            5 -> Offset(-small, small)
            6 -> Offset(-small, -small * 0.5f)
            7 -> Offset(-small * 2f, small)
            8 -> Offset(-small, -small * 0.5f)
            9 -> Offset(small, -small)
            10 -> Offset(0f, -small * 2f)
            11 -> Offset(small, small)
            12 -> Offset(small, small * 0.5f)
            else -> Offset(0f, 0f)
        }
    }

    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetPosition>,
        houseCenter: Offset,
        size: Float,
        houseNum: Int
    ) {
        val planetTexts = planets.map { planet ->
            val symbol = planet.planet.symbol
            val retro = if (planet.isRetrograde) "(R)" else ""
            Triple(symbol + retro, planet.isRetrograde, planet.planet.displayName)
        }

        val textSize = size * 0.032f
        val lineHeight = size * 0.038f

        // Adjust position based on house type
        val adjustment = when (houseNum) {
            1, 7 -> Offset(0f, 0f)  // Top/Bottom - center
            4, 10 -> Offset(0f, 0f) // Left/Right - center
            else -> Offset(0f, 0f)
        }

        planetTexts.forEachIndexed { index, (text, isRetrograde, _) ->
            val yOffset = when {
                planetTexts.size == 1 -> 0f
                else -> (index - (planetTexts.size - 1) / 2f) * lineHeight
            }

            val color = if (isRetrograde) RETROGRADE_COLOR else PLANET_COLOR
            drawTextCentered(
                text,
                Offset(houseCenter.x + adjustment.x, houseCenter.y + adjustment.y + yOffset),
                textSize,
                color,
                isBold = true
            )
        }
    }

    private fun DrawScope.drawTextCentered(
        text: String,
        position: Offset,
        textSize: Float,
        color: Color,
        isBold: Boolean = false,
        alpha: Int = 255
    ) {
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                this.color = color.toArgb()
                this.textSize = textSize
                this.textAlign = android.graphics.Paint.Align.CENTER
                this.typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    if (isBold) android.graphics.Typeface.BOLD else android.graphics.Typeface.NORMAL
                )
                this.alpha = alpha
                this.isAntiAlias = true
            }
            drawText(text, position.x, position.y + textSize / 3, paint)
        }
    }

    /**
     * Draw a divisional chart (D9, D10, D60, etc.)
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
            val chartSize = size * 0.88f
            val half = chartSize / 2f

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw outer diamond
            val outerPath = Path().apply {
                moveTo(center.x, center.y - half)
                lineTo(center.x + half, center.y)
                lineTo(center.x, center.y + half)
                lineTo(center.x - half, center.y)
                close()
            }
            drawPath(outerPath, BORDER_COLOR, style = Stroke(width = 2f))

            // Draw inner diamond
            val innerHalf = half * 0.35f
            val innerPath = Path().apply {
                moveTo(center.x, center.y - innerHalf)
                lineTo(center.x + innerHalf, center.y)
                lineTo(center.x, center.y + innerHalf)
                lineTo(center.x - innerHalf, center.y)
                close()
            }
            drawPath(innerPath, HOUSE_LINE_COLOR, style = Stroke(width = 1.5f))

            // Draw connecting lines
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x, center.y - innerHalf), strokeWidth = 1.5f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x + innerHalf, center.y), strokeWidth = 1.5f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x, center.y + innerHalf), strokeWidth = 1.5f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x - innerHalf, center.y), strokeWidth = 1.5f)

            // Draw diagonal divisions
            drawLine(HOUSE_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x, center.y - innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x - innerHalf, center.y), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x, center.y - innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x + innerHalf, center.y), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x, center.y + innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x + innerHalf, center.y), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x, center.y + innerHalf), strokeWidth = 1f)
            drawLine(HOUSE_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x - innerHalf, center.y), strokeWidth = 1f)

            // Get ascendant sign
            val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)

            // Draw house contents
            drawHouseContents(center, half, innerHalf, ascendantSign, planetPositions)

            // Draw chart title
            drawTextCentered(chartTitle, center, size * 0.04f, TEXT_COLOR, isBold = true)

            // Draw Asc marker
            val ascPos = getHouseCenter(1, center, half, innerHalf)
            drawTextCentered("Asc", Offset(ascPos.x - half * 0.2f, ascPos.y - half * 0.15f), size * 0.028f, ASCENDANT_COLOR, isBold = true)
        }
    }

    /**
     * Backward compatible method
     */
    fun drawSouthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float
    ) {
        drawNorthIndianChart(drawScope, chart, size, "Lagna")
    }

    /**
     * Create a bitmap from the chart for export
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
            drawDivisionalChart(this, planetPositions, ascendantLongitude, minOf(width, height).toFloat(), chartTitle)
        }

        return bitmap
    }
}
