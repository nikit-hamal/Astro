package com.astro.storm.ui.chart

import android.graphics.Bitmap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.domain.ChartDataProcessor
import com.astro.storm.domain.model.PlanetRenderData
import kotlin.math.min

class ChartRenderer(private val theme: ChartTheme = ChartTheme()) {

    // Geometry ratios
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
    private val chartDataProcessor = ChartDataProcessor()

    private data class ChartFrame(
        val left: Float,
        val top: Float,
        val size: Float,
        val centerX: Float,
        val centerY: Float
    )

    private fun DrawScope.drawNorthIndianFrame(size: Float): ChartFrame {
        val padding = size * 0.02f
        val chartSize = size - (padding * 2)
        val left = padding
        val top = padding
        val right = left + chartSize
        val bottom = top + chartSize
        val centerX = (left + right) / 2
        val centerY = (top + bottom) / 2

        drawRect(color = theme.backgroundColor, size = Size(size, size))

        drawRect(
            color = theme.borderColor,
            topLeft = Offset(left, top),
            size = Size(chartSize, chartSize),
            style = theme.borderStroke
        )

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

        return ChartFrame(left, top, chartSize, centerX, centerY)
    }

    fun drawNorthIndianChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float
    ) {
        with(drawScope) {
            val frame = drawNorthIndianFrame(size)
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
            val renderDataMap = chartDataProcessor.createRenderDataMap(chart)

            drawAllHouseContents(
                frame = frame,
                ascendantSign = ascendantSign,
                renderDataMap = renderDataMap,
                size = size
            )
        }
    }

    fun drawDivisionalChart(
        drawScope: DrawScope,
        chart: VedicChart,
        size: Float,
        chartTitle: String
    ) {
        with(drawScope) {
            val frame = drawNorthIndianFrame(size)
            val ascendantSign = ZodiacSign.fromLongitude(chart.ascendant)
            val renderDataMap = chartDataProcessor.createRenderDataMap(chart)

            drawAllHouseContents(
                frame = frame,
                ascendantSign = ascendantSign,
                renderDataMap = renderDataMap,
                size = size
            )
        }
    }

    private fun signNumberForHouse(houseNum: Int, ascendantSign: ZodiacSign): Int {
        return ((ascendantSign.ordinal + houseNum - 1) % 12) + 1
    }

    private fun DrawScope.drawAllHouseContents(
        frame: ChartFrame,
        ascendantSign: ZodiacSign,
        renderDataMap: Map<Int, List<PlanetRenderData>>,
        size: Float,
        showSignNumbers: Boolean = true
    ) {
        for (houseNum in 1..12) {
            val houseCenter = getHousePlanetCenter(houseNum, frame)
            val numberPos = getHouseNumberPosition(houseNum, frame)

            val numberText = if (showSignNumbers) {
                signNumberForHouse(houseNum, ascendantSign).toString()
            } else {
                houseNum.toString()
            }
            drawTextCentered(
                text = numberText,
                position = numberPos,
                textSize = size * 0.035f,
                color = theme.houseNumberColor,
                isBold = false
            )

            if (houseNum == 1) {
                drawLagnaMarker(houseCenter, size)
            }

            val planets = renderDataMap[houseNum] ?: emptyList()
            if (planets.isNotEmpty()) {
                drawPlanetsInHouse(planets, houseCenter, size, houseNum)
            }
        }
    }

    private fun getHousePlanetCenter(houseNum: Int, frame: ChartFrame): Offset {
        return with(frame) {
            when (houseNum) {
                1 -> Offset(centerX, top + size * DIAMOND_PLANET_VERTICAL_FRACTION)
                2 -> Offset(left + size * CORNER_CENTER_FRACTION, top + size * CORNER_CENTER_FRACTION)
                3 -> Offset(left + size * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY - size * SIDE_VERTICAL_OFFSET_FRACTION)
                4 -> Offset(left + size * DIAMOND_PLANET_VERTICAL_FRACTION, centerY)
                5 -> Offset(left + size * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY + size * SIDE_VERTICAL_OFFSET_FRACTION)
                6 -> Offset(left + size * CORNER_CENTER_FRACTION, frame.top + frame.size - size * CORNER_CENTER_FRACTION)
                7 -> Offset(centerX, frame.top + frame.size - size * DIAMOND_PLANET_VERTICAL_FRACTION)
                8 -> Offset(frame.left + frame.size - size * CORNER_CENTER_FRACTION, frame.top + frame.size - size * CORNER_CENTER_FRACTION)
                9 -> Offset(frame.left + frame.size - size * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY + size * SIDE_VERTICAL_OFFSET_FRACTION)
                10 -> Offset(frame.left + frame.size - size * DIAMOND_PLANET_VERTICAL_FRACTION, centerY)
                11 -> Offset(frame.left + frame.size - size * SIDE_PLANET_HORIZONTAL_OFFSET_FRACTION, centerY - size * SIDE_VERTICAL_OFFSET_FRACTION)
                12 -> Offset(frame.left + frame.size - size * CORNER_CENTER_FRACTION, top + size * CORNER_CENTER_FRACTION)
                else -> Offset(centerX, centerY)
            }
        }
    }

    private fun getHouseNumberPosition(houseNum: Int, frame: ChartFrame): Offset {
        return with(frame) {
            when (houseNum) {
                1 -> Offset(centerX, top + size * DIAMOND_NUMBER_VERTICAL_FRACTION)
                2 -> Offset(centerX - size * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, top + size * CORNER_NUMBER_OFFSET_FRACTION)
                3 -> Offset(left + size * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY - size * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
                4 -> Offset(left + size * DIAMOND_NUMBER_VERTICAL_FRACTION - size * DIAMOND_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + size * DIAMOND_NUMBER_VERTICAL_OFFSET_FRACTION)
                5 -> Offset(left + size * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + size * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
                6 -> Offset(centerX - size * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, frame.top + frame.size - size * CORNER_NUMBER_OFFSET_FRACTION)
                7 -> Offset(centerX, frame.top + frame.size - size * DIAMOND_NUMBER_VERTICAL_FRACTION)
                8 -> Offset(centerX + size * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, frame.top + frame.size - size * CORNER_NUMBER_OFFSET_FRACTION)
                9 -> Offset(frame.left + frame.size - size * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + size * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
                10 -> Offset(frame.left + frame.size - size * DIAMOND_NUMBER_VERTICAL_FRACTION + size * DIAMOND_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY + size * DIAMOND_NUMBER_VERTICAL_OFFSET_FRACTION)
                11 -> Offset(frame.left + frame.size - size * SIDE_NUMBER_HORIZONTAL_OFFSET_FRACTION, centerY - size * SIDE_NUMBER_VERTICAL_OFFSET_FRACTION)
                12 -> Offset(centerX + size * CORNER_NUMBER_HORIZONTAL_OFFSET_FRACTION, top + size * CORNER_NUMBER_OFFSET_FRACTION)
                else -> Offset(centerX, centerY)
            }
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

    private enum class HouseType { DIAMOND, SIDE, CORNER }

    private fun DrawScope.drawPlanetsInHouse(
        planets: List<PlanetRenderData>,
        houseCenter: Offset,
        size: Float,
        houseNum: Int
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

        planets.forEachIndexed { index, planetData ->
            val statusIndicators = planetData.statusIndicators.joinToString("")
            val displayText = "${planetData.symbol}${planetData.degreeText}${statusIndicators}"
            val col = index % columns
            val row = index / columns
            val xOffset = if (columns > 1) (col - 0.5f) * columnSpacing else 0f
            val totalRows = if (columns > 1) itemsPerColumn else planets.size
            val yOffset = (row - (totalRows - 1) / 2f) * lineHeight
            val position = Offset(houseCenter.x + xOffset, houseCenter.y + yOffset)

            drawTextCentered(
                text = displayText,
                position = position,
                textSize = textSize,
                color = planetData.color,
                isBold = true
            )
        }
    }

    private fun DrawScope.drawTextCentered(
        text: String,
        position: Offset,
        textSize: Float,
        color: Color,
        isBold: Boolean = false
    ) {
        val typeface = if (isBold) theme.boldTypeface else theme.normalTypeface
        textPaint.color = color.toArgb()
        textPaint.textSize = textSize
        textPaint.typeface = typeface

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
            drawNorthIndianChart(this, chart, min(width, height).toFloat())
        }

        return bitmap
    }

    fun createDivisionalChartBitmap(
        chart: VedicChart,
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
            drawDivisionalChart(this, chart, min(width, height).toFloat(), chartTitle)
        }
        return bitmap
    }

    private fun DrawScope.drawLagnaMarker(houseCenter: Offset, size: Float) {
        val textSize = size * 0.035f
        drawTextCentered(
            text = "La",
            position = Offset(houseCenter.x, houseCenter.y - size * 0.06f),
            textSize = textSize,
            color = theme.lagnaColor,
            isBold = true
        )
    }
}
