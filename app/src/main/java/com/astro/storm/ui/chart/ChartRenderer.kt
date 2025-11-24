package com.astro.storm.ui.chart

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.*
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import kotlin.math.*

/**
 * High-quality chart renderer using Compose Canvas
 * Renders South Indian style diamond chart
 */
class ChartRenderer {

    companion object {
        private val BACKGROUND_COLOR = Color(0xFF1A1B2E)
        private val BORDER_COLOR = Color(0xFF6B7FD7)
        private val HOUSE_LINE_COLOR = Color(0xFF4A5A9D)
        private val TEXT_COLOR = Color.White
        private val PLANET_COLOR = Color(0xFFFFD700)
        private val ASCENDANT_COLOR = Color(0xFFFF6B9D)
        private val HOUSE_NUMBER_COLOR = Color(0xFF9BA8E0)
    }

    /**
     * Draw South Indian style chart
     */
    fun drawSouthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float
    ) {
        with(drawScope) {
            val center = Offset(size / 2f, size / 2f)
            val diamondSize = size * 0.9f

            // Draw background
            drawRect(
                color = BACKGROUND_COLOR,
                size = Size(size, size)
            )

            // Draw diamond outline
            drawDiamond(center, diamondSize, BORDER_COLOR, strokeWidth = 3f)

            // Draw house divisions
            drawHouseDivisions(center, diamondSize)

            // Draw house numbers
            drawHouseNumbers(center, diamondSize)

            // Draw planets in houses
            drawPlanets(center, diamondSize, chart)

            // Draw ascendant marker
            drawAscendantMarker(center, diamondSize, chart.ascendant)
        }
    }

    private fun DrawScope.drawDiamond(
        center: Offset,
        size: Float,
        color: Color,
        strokeWidth: Float = 2f
    ) {
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
            color = color,
            style = Stroke(width = strokeWidth)
        )
    }

    private fun DrawScope.drawHouseDivisions(center: Offset, size: Float) {
        val halfSize = size / 2f

        // Horizontal middle line
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x - halfSize, center.y),
            end = Offset(center.x + halfSize, center.y),
            strokeWidth = 2f
        )

        // Vertical middle line
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x, center.y - halfSize),
            end = Offset(center.x, center.y + halfSize),
            strokeWidth = 2f
        )

        // Diagonal lines for inner divisions
        // Top-left to center divisions
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x - halfSize, center.y),
            end = Offset(center.x, center.y - halfSize),
            strokeWidth = 1.5f
        )

        // Top-right to center divisions
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x, center.y - halfSize),
            end = Offset(center.x + halfSize, center.y),
            strokeWidth = 1.5f
        )

        // Bottom-right to center divisions
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x + halfSize, center.y),
            end = Offset(center.x, center.y + halfSize),
            strokeWidth = 1.5f
        )

        // Bottom-left to center divisions
        drawLine(
            color = HOUSE_LINE_COLOR,
            start = Offset(center.x, center.y + halfSize),
            end = Offset(center.x - halfSize, center.y),
            strokeWidth = 1.5f
        )
    }

    private fun DrawScope.drawHouseNumbers(center: Offset, size: Float) {
        val halfSize = size / 2f

        // South Indian chart house positions (fixed layout)
        val housePositions = listOf(
            Offset(center.x, center.y - halfSize * 0.7f), // House 1 (top)
            Offset(center.x + halfSize * 0.5f, center.y - halfSize * 0.5f), // House 2
            Offset(center.x + halfSize * 0.7f, center.y), // House 3
            Offset(center.x + halfSize * 0.5f, center.y + halfSize * 0.5f), // House 4
            Offset(center.x, center.y + halfSize * 0.7f), // House 5 (bottom)
            Offset(center.x - halfSize * 0.5f, center.y + halfSize * 0.5f), // House 6
            Offset(center.x - halfSize * 0.7f, center.y), // House 7
            Offset(center.x - halfSize * 0.5f, center.y - halfSize * 0.5f), // House 8
            Offset(center.x - halfSize * 0.25f, center.y - halfSize * 0.25f), // House 9
            Offset(center.x + halfSize * 0.25f, center.y - halfSize * 0.25f), // House 10
            Offset(center.x + halfSize * 0.25f, center.y + halfSize * 0.25f), // House 11
            Offset(center.x - halfSize * 0.25f, center.y + halfSize * 0.25f)  // House 12
        )

        housePositions.forEachIndexed { index, position ->
            val houseNumber = (index + 1).toString()
            drawContext.canvas.nativeCanvas.apply {
                val paint = android.graphics.Paint().apply {
                    color = HOUSE_NUMBER_COLOR.toArgb()
                    textSize = size * 0.04f
                    textAlign = android.graphics.Paint.Align.CENTER
                    typeface = android.graphics.Typeface.create(
                        android.graphics.Typeface.DEFAULT,
                        android.graphics.Typeface.BOLD
                    )
                }
                drawText(houseNumber, position.x, position.y, paint)
            }
        }
    }

    private fun DrawScope.drawPlanets(center: Offset, size: Float, chart: VedicChart) {
        val halfSize = size / 2f

        // Group planets by house
        val planetsByHouse = chart.planetPositions.groupBy { it.house }

        // South Indian chart house positions for planets
        val housePositions = listOf(
            Offset(center.x, center.y - halfSize * 0.5f), // House 1
            Offset(center.x + halfSize * 0.5f, center.y - halfSize * 0.35f), // House 2
            Offset(center.x + halfSize * 0.5f, center.y), // House 3
            Offset(center.x + halfSize * 0.5f, center.y + halfSize * 0.35f), // House 4
            Offset(center.x, center.y + halfSize * 0.5f), // House 5
            Offset(center.x - halfSize * 0.5f, center.y + halfSize * 0.35f), // House 6
            Offset(center.x - halfSize * 0.5f, center.y), // House 7
            Offset(center.x - halfSize * 0.5f, center.y - halfSize * 0.35f), // House 8
            Offset(center.x - halfSize * 0.25f, center.y - halfSize * 0.15f), // House 9
            Offset(center.x + halfSize * 0.25f, center.y - halfSize * 0.15f), // House 10
            Offset(center.x + halfSize * 0.25f, center.y + halfSize * 0.15f), // House 11
            Offset(center.x - halfSize * 0.25f, center.y + halfSize * 0.15f)  // House 12
        )

        planetsByHouse.forEach { (house, planets) ->
            if (house in 1..12) {
                val position = housePositions[house - 1]
                val planetText = planets.joinToString(" ") {
                    it.planet.symbol + if (it.isRetrograde) "(R)" else ""
                }

                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        color = PLANET_COLOR.toArgb()
                        textSize = size * 0.035f
                        textAlign = android.graphics.Paint.Align.CENTER
                        typeface = android.graphics.Typeface.create(
                            android.graphics.Typeface.DEFAULT,
                            android.graphics.Typeface.NORMAL
                        )
                    }
                    drawText(planetText, position.x, position.y + size * 0.05f, paint)
                }
            }
        }
    }

    private fun DrawScope.drawAscendantMarker(center: Offset, size: Float, ascendant: Double) {
        val halfSize = size / 2f
        val markerPosition = Offset(center.x - halfSize * 0.1f, center.y - halfSize * 0.6f)

        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = ASCENDANT_COLOR.toArgb()
                textSize = size * 0.045f
                textAlign = android.graphics.Paint.Align.CENTER
                typeface = android.graphics.Typeface.create(
                    android.graphics.Typeface.DEFAULT,
                    android.graphics.Typeface.BOLD
                )
            }
            drawText("As", markerPosition.x, markerPosition.y, paint)
        }
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
            drawSouthIndianChart(this, chart, minOf(width, height).toFloat())
        }

        return bitmap
    }
}
