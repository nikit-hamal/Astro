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
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import kotlin.math.min

/**
 * Professional North Indian Style Vedic Chart Renderer
 *
 * This renderer implements the authentic North Indian diamond chart format used
 * in traditional Vedic astrology. The chart consists of:
 *
 * - An outer diamond (square rotated 45°) representing the 12 houses
 * - An inner diamond at the center for the chart title
 * - 4 corner triangular houses (1, 4, 7, 10 - the Kendra houses)
 * - 8 side triangular houses (remaining houses)
 *
 * Standard North Indian Chart Layout (Houses numbered 1-12):
 *
 *                        ┌───────────────────────┐
 *                       /\          12          /\
 *                      /  \                    /  \
 *                     / 11 \                  / 1  \
 *                    /      \                /      \
 *                   /        \──────────────/        \
 *                  /          \            /          \
 *                 / 10         \          /         2  \
 *                /              \        /              \
 *               /                \  ──  /                \
 *              /                  \/  \/                  \
 *              \                  /\  /\                  /
 *               \                /  \/  \                /
 *                \    9        /        \        3     /
 *                 \           /          \            /
 *                  \         /────────────\          /
 *                   \       /              \        /
 *                    \  8  /                \  4   /
 *                     \   /                  \    /
 *                      \ /         7          \  /
 *                       \──────────6───────────\/
 *                        └───────────────────────┘
 *
 * House 1 (Ascendant) is at the top-right, and houses proceed counter-clockwise.
 */
class ChartRenderer {

    companion object {
        // Elegant dark theme colors optimized for astrology charts
        private val BACKGROUND_COLOR = Color(0xFF1A1512)
        private val CHART_BACKGROUND = Color(0xFF231A15)
        private val OUTER_BORDER_COLOR = Color(0xFFB8A99A)
        private val INNER_LINE_COLOR = Color(0xFF5A4A40)
        private val GRID_LINE_COLOR = Color(0xFF3D322B)
        private val TEXT_PRIMARY = Color(0xFFE8DFD6)
        private val PLANET_COLOR = Color(0xFFE5C46C)
        private val ASCENDANT_COLOR = Color(0xFFFF8A80)
        private val HOUSE_NUMBER_COLOR = Color(0xFF7A6A5A)
        private val RETROGRADE_COLOR = Color(0xFFFFB4AB)
        private val SIGN_COLOR = Color(0xFF8A7A6A)
        private val TITLE_COLOR = Color(0xFFD4C4B4)

        // Planet type colors for better visual distinction
        private val BENEFIC_COLOR = Color(0xFF81C784)      // Green for benefics
        private val MALEFIC_COLOR = Color(0xFFE57373)      // Red for malefics
        private val NEUTRAL_COLOR = Color(0xFFE5C46C)      // Gold for neutral
    }

    /**
     * Draw a professional North Indian style Vedic chart
     *
     * @param drawScope The draw scope for rendering
     * @param chart The VedicChart data to render
     * @param size The size of the chart canvas
     * @param chartTitle Title to display in center (e.g., "Lagna", "D9")
     */
    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String = "Lagna"
    ) {
        with(drawScope) {
            val center = Offset(size / 2f, size / 2f)
            val padding = size * 0.06f
            val chartSize = size - (padding * 2)
            val half = chartSize / 2f

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw chart area background
            val chartPath = Path().apply {
                moveTo(center.x, center.y - half)
                lineTo(center.x + half, center.y)
                lineTo(center.x, center.y + half)
                lineTo(center.x - half, center.y)
                close()
            }
            drawPath(chartPath, CHART_BACKGROUND)

            // Draw outer diamond border
            drawPath(chartPath, OUTER_BORDER_COLOR, style = Stroke(width = 2.5f))

            // Inner diamond ratio (center area for title)
            val innerRatio = 0.32f
            val innerHalf = half * innerRatio

            // Draw inner diamond
            val innerPath = Path().apply {
                moveTo(center.x, center.y - innerHalf)
                lineTo(center.x + innerHalf, center.y)
                lineTo(center.x, center.y + innerHalf)
                lineTo(center.x - innerHalf, center.y)
                close()
            }
            drawPath(innerPath, INNER_LINE_COLOR, style = Stroke(width = 1.8f))

            // Draw the four main dividing lines (from outer corners to inner diamond corners)
            // These create the 4 corner Kendra houses (1, 4, 7, 10)

            // Vertical line: Top to inner-top
            drawLine(
                color = INNER_LINE_COLOR,
                start = Offset(center.x, center.y - half),
                end = Offset(center.x, center.y - innerHalf),
                strokeWidth = 1.8f
            )

            // Horizontal line: Right to inner-right
            drawLine(
                color = INNER_LINE_COLOR,
                start = Offset(center.x + half, center.y),
                end = Offset(center.x + innerHalf, center.y),
                strokeWidth = 1.8f
            )

            // Vertical line: Bottom to inner-bottom
            drawLine(
                color = INNER_LINE_COLOR,
                start = Offset(center.x, center.y + half),
                end = Offset(center.x, center.y + innerHalf),
                strokeWidth = 1.8f
            )

            // Horizontal line: Left to inner-left
            drawLine(
                color = INNER_LINE_COLOR,
                start = Offset(center.x - half, center.y),
                end = Offset(center.x - innerHalf, center.y),
                strokeWidth = 1.8f
            )

            // Draw diagonal lines to create the 8 side houses
            // Top-left quadrant: Creates houses 11 and 12
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x - half, center.y),
                end = Offset(center.x, center.y - innerHalf),
                strokeWidth = 1.2f
            )
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x, center.y - half),
                end = Offset(center.x - innerHalf, center.y),
                strokeWidth = 1.2f
            )

            // Top-right quadrant: Creates houses 1 and 2
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x + half, center.y),
                end = Offset(center.x, center.y - innerHalf),
                strokeWidth = 1.2f
            )
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x, center.y - half),
                end = Offset(center.x + innerHalf, center.y),
                strokeWidth = 1.2f
            )

            // Bottom-right quadrant: Creates houses 3 and 4
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x + half, center.y),
                end = Offset(center.x, center.y + innerHalf),
                strokeWidth = 1.2f
            )
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x, center.y + half),
                end = Offset(center.x + innerHalf, center.y),
                strokeWidth = 1.2f
            )

            // Bottom-left quadrant: Creates houses 5 and 6
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x - half, center.y),
                end = Offset(center.x, center.y + innerHalf),
                strokeWidth = 1.2f
            )
            drawLine(
                color = GRID_LINE_COLOR,
                start = Offset(center.x, center.y + half),
                end = Offset(center.x - innerHalf, center.y),
                strokeWidth = 1.2f
            )

            // Get ascendant sign for house-sign mapping
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)

            // Draw house contents (signs, planets, house numbers)
            drawAllHouseContents(center, half, innerHalf, size, ascendantSign, chart.planetPositions)

            // Draw chart title in center
            drawTextCentered(
                text = chartTitle,
                position = center,
                textSize = size * 0.042f,
                color = TITLE_COLOR,
                isBold = true
            )

            // Draw "Asc" marker in house 1
            val house1Center = getHouseCentroid(1, center, half, innerHalf)
            drawAscendantMarker(house1Center, size)
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
            val center = Offset(size / 2f, size / 2f)
            val padding = size * 0.06f
            val chartSize = size - (padding * 2)
            val half = chartSize / 2f

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw chart area background
            val chartPath = Path().apply {
                moveTo(center.x, center.y - half)
                lineTo(center.x + half, center.y)
                lineTo(center.x, center.y + half)
                lineTo(center.x - half, center.y)
                close()
            }
            drawPath(chartPath, CHART_BACKGROUND)
            drawPath(chartPath, OUTER_BORDER_COLOR, style = Stroke(width = 2.5f))

            val innerRatio = 0.32f
            val innerHalf = half * innerRatio

            // Draw inner diamond
            val innerPath = Path().apply {
                moveTo(center.x, center.y - innerHalf)
                lineTo(center.x + innerHalf, center.y)
                lineTo(center.x, center.y + innerHalf)
                lineTo(center.x - innerHalf, center.y)
                close()
            }
            drawPath(innerPath, INNER_LINE_COLOR, style = Stroke(width = 1.8f))

            // Draw main dividing lines
            drawLine(INNER_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x, center.y - innerHalf), strokeWidth = 1.8f)
            drawLine(INNER_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x + innerHalf, center.y), strokeWidth = 1.8f)
            drawLine(INNER_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x, center.y + innerHalf), strokeWidth = 1.8f)
            drawLine(INNER_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x - innerHalf, center.y), strokeWidth = 1.8f)

            // Draw diagonal divisions
            drawLine(GRID_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x, center.y - innerHalf), strokeWidth = 1.2f)
            drawLine(GRID_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x - innerHalf, center.y), strokeWidth = 1.2f)
            drawLine(GRID_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x, center.y - innerHalf), strokeWidth = 1.2f)
            drawLine(GRID_LINE_COLOR, Offset(center.x, center.y - half), Offset(center.x + innerHalf, center.y), strokeWidth = 1.2f)
            drawLine(GRID_LINE_COLOR, Offset(center.x + half, center.y), Offset(center.x, center.y + innerHalf), strokeWidth = 1.2f)
            drawLine(GRID_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x + innerHalf, center.y), strokeWidth = 1.2f)
            drawLine(GRID_LINE_COLOR, Offset(center.x - half, center.y), Offset(center.x, center.y + innerHalf), strokeWidth = 1.2f)
            drawLine(GRID_LINE_COLOR, Offset(center.x, center.y + half), Offset(center.x - innerHalf, center.y), strokeWidth = 1.2f)

            // Get ascendant sign
            val ascendantSign = ZodiacSign.fromLongitude(ascendantLongitude)

            // Draw house contents
            drawAllHouseContents(center, half, innerHalf, size, ascendantSign, planetPositions)

            // Draw chart title
            drawTextCentered(
                text = chartTitle,
                position = center,
                textSize = size * 0.038f,
                color = TITLE_COLOR,
                isBold = true
            )

            // Draw Asc marker
            val house1Center = getHouseCentroid(1, center, half, innerHalf)
            drawAscendantMarker(house1Center, size)
        }
    }

    /**
     * Draw all house contents including signs, house numbers, and planets
     */
    private fun DrawScope.drawAllHouseContents(
        center: Offset,
        half: Float,
        innerHalf: Float,
        size: Float,
        ascendantSign: ZodiacSign,
        planetPositions: List<PlanetPosition>
    ) {
        // Group planets by house
        val planetsByHouse = planetPositions.groupBy { it.house }

        for (houseNum in 1..12) {
            val houseCentroid = getHouseCentroid(houseNum, center, half, innerHalf)

            // Calculate sign for this house (house 1 = ascendant sign)
            val signIndex = (ascendantSign.ordinal + houseNum - 1) % 12
            val sign = ZodiacSign.entries[signIndex]

            // Draw house number (small, positioned at edge)
            val houseNumPos = getHouseNumberPosition(houseNum, center, half, innerHalf)
            drawTextCentered(
                text = houseNum.toString(),
                position = houseNumPos,
                textSize = size * 0.024f,
                color = HOUSE_NUMBER_COLOR,
                isBold = false
            )

            // Draw sign abbreviation
            val signPos = getSignPosition(houseNum, center, half, innerHalf)
            drawTextCentered(
                text = sign.abbreviation,
                position = signPos,
                textSize = size * 0.022f,
                color = SIGN_COLOR,
                isBold = false
            )

            // Draw planets in this house
            val planets = planetsByHouse[houseNum] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCentroid, size, houseNum, half)
            }
        }
    }

    /**
     * Get the centroid (center point) of each house for planet placement
     * This uses precise geometric calculations for the North Indian diamond layout
     */
    private fun getHouseCentroid(houseNum: Int, center: Offset, half: Float, innerHalf: Float): Offset {
        // Calculate centroids based on the triangular/quadrilateral shape of each house
        // Corner houses (1, 4, 7, 10) are larger triangular areas at the corners
        // Side houses are smaller triangular areas along the edges

        val cornerOffset = half * 0.58f    // Distance from center for corner houses
        val sideOffset = half * 0.52f      // Distance for side houses on main axis
        val diagOffset = half * 0.42f      // Diagonal offset for in-between houses

        return when (houseNum) {
            // House 1 - Top right corner (Ascendant house)
            1 -> Offset(center.x + diagOffset * 0.5f, center.y - cornerOffset)

            // House 2 - Upper right side
            2 -> Offset(center.x + diagOffset * 0.9f, center.y - diagOffset * 0.55f)

            // House 3 - Right upper area
            3 -> Offset(center.x + sideOffset * 0.95f, center.y - sideOffset * 0.2f)

            // House 4 - Right corner
            4 -> Offset(center.x + cornerOffset, center.y + diagOffset * 0.05f)

            // House 5 - Right lower area
            5 -> Offset(center.x + sideOffset * 0.95f, center.y + sideOffset * 0.25f)

            // House 6 - Lower right side
            6 -> Offset(center.x + diagOffset * 0.85f, center.y + diagOffset * 0.6f)

            // House 7 - Bottom corner
            7 -> Offset(center.x + diagOffset * 0.05f, center.y + cornerOffset)

            // House 8 - Lower left side
            8 -> Offset(center.x - diagOffset * 0.85f, center.y + diagOffset * 0.6f)

            // House 9 - Left lower area
            9 -> Offset(center.x - sideOffset * 0.95f, center.y + sideOffset * 0.25f)

            // House 10 - Left corner
            10 -> Offset(center.x - cornerOffset, center.y + diagOffset * 0.05f)

            // House 11 - Left upper area
            11 -> Offset(center.x - sideOffset * 0.95f, center.y - sideOffset * 0.2f)

            // House 12 - Upper left side
            12 -> Offset(center.x - diagOffset * 0.85f, center.y - diagOffset * 0.55f)

            else -> center
        }
    }

    /**
     * Get position for house number display
     */
    private fun getHouseNumberPosition(houseNum: Int, center: Offset, half: Float, innerHalf: Float): Offset {
        val offset = half * 0.82f
        val diagOffset = half * 0.65f
        val smallDiag = half * 0.55f

        return when (houseNum) {
            1 -> Offset(center.x + smallDiag * 0.3f, center.y - offset * 0.92f)
            2 -> Offset(center.x + diagOffset * 0.75f, center.y - diagOffset * 0.4f)
            3 -> Offset(center.x + offset * 0.92f, center.y - smallDiag * 0.15f)
            4 -> Offset(center.x + offset * 0.92f, center.y + smallDiag * 0.2f)
            5 -> Offset(center.x + diagOffset * 0.75f, center.y + diagOffset * 0.42f)
            6 -> Offset(center.x + smallDiag * 0.35f, center.y + offset * 0.92f)
            7 -> Offset(center.x - smallDiag * 0.25f, center.y + offset * 0.92f)
            8 -> Offset(center.x - diagOffset * 0.7f, center.y + diagOffset * 0.42f)
            9 -> Offset(center.x - offset * 0.92f, center.y + smallDiag * 0.2f)
            10 -> Offset(center.x - offset * 0.92f, center.y - smallDiag * 0.15f)
            11 -> Offset(center.x - diagOffset * 0.7f, center.y - diagOffset * 0.4f)
            12 -> Offset(center.x - smallDiag * 0.25f, center.y - offset * 0.92f)
            else -> center
        }
    }

    /**
     * Get position for zodiac sign abbreviation
     */
    private fun getSignPosition(houseNum: Int, center: Offset, half: Float, innerHalf: Float): Offset {
        val offset = half * 0.72f
        val diagOffset = half * 0.58f
        val smallDiag = half * 0.48f

        return when (houseNum) {
            1 -> Offset(center.x + smallDiag * 0.55f, center.y - offset * 0.78f)
            2 -> Offset(center.x + diagOffset * 0.88f, center.y - diagOffset * 0.28f)
            3 -> Offset(center.x + offset * 0.85f, center.y - smallDiag * 0.02f)
            4 -> Offset(center.x + offset * 0.85f, center.y + smallDiag * 0.08f)
            5 -> Offset(center.x + diagOffset * 0.88f, center.y + diagOffset * 0.3f)
            6 -> Offset(center.x + smallDiag * 0.55f, center.y + offset * 0.78f)
            7 -> Offset(center.x - smallDiag * 0.45f, center.y + offset * 0.78f)
            8 -> Offset(center.x - diagOffset * 0.82f, center.y + diagOffset * 0.3f)
            9 -> Offset(center.x - offset * 0.85f, center.y + smallDiag * 0.08f)
            10 -> Offset(center.x - offset * 0.85f, center.y - smallDiag * 0.02f)
            11 -> Offset(center.x - diagOffset * 0.82f, center.y - diagOffset * 0.28f)
            12 -> Offset(center.x - smallDiag * 0.45f, center.y - offset * 0.78f)
            else -> center
        }
    }

    /**
     * Draw planets positioned within a house
     */
    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetPosition>,
        houseCenter: Offset,
        size: Float,
        houseNum: Int,
        half: Float
    ) {
        val textSize = size * 0.030f
        val lineHeight = size * 0.036f

        // Calculate vertical offset for centering multiple planets
        val totalHeight = (planets.size - 1) * lineHeight
        val startY = houseCenter.y - totalHeight / 2f

        planets.forEachIndexed { index, planet ->
            val symbol = planet.planet.symbol
            val retroIndicator = if (planet.isRetrograde) "ᴿ" else ""
            val displayText = "$symbol$retroIndicator"

            val yOffset = startY + (index * lineHeight)
            val color = if (planet.isRetrograde) RETROGRADE_COLOR else PLANET_COLOR

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
     * Draw Ascendant marker
     */
    private fun DrawScope.drawAscendantMarker(position: Offset, size: Float) {
        val markerSize = size * 0.022f
        val markerPos = Offset(position.x - size * 0.06f, position.y - size * 0.05f)

        drawTextCentered(
            text = "As",
            position = markerPos,
            textSize = markerSize,
            color = ASCENDANT_COLOR,
            isBold = true
        )
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
