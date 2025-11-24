package com.astro.storm.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.PlanetPosition

@Composable
fun SouthIndianChart(
    planetPositions: List<PlanetPosition>,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val colors = MaterialTheme.colorScheme

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
    ) {
        drawSouthIndianChart(
            drawScope = this,
            planetPositions = planetPositions,
            size = size.minDimension,
            textMeasurer = textMeasurer,
            colors = colors
        )
    }
}

private fun drawSouthIndianChart(
    drawScope: DrawScope,
    planetPositions: List<PlanetPosition>,
    size: Float,
    textMeasurer: TextMeasurer,
    colors: androidx.compose.material3.ColorScheme
) {
    with(drawScope) {
        val center = Offset(size / 2f, size / 2f)
        val diamondSize = size * 0.9f

        // Draw background
        drawRect(
            color = colors.background,
            size = Size(size, size)
        )

        // Draw diamond outline
        drawDiamond(center, diamondSize, colors.primary, strokeWidth = 3f)

        // Draw house divisions
        drawHouseDivisions(center, diamondSize, colors.secondary)

        // Draw house numbers
        drawHouseNumbers(center, diamondSize, textMeasurer, colors.onBackground)

        // Draw planets in houses
        drawPlanets(center, diamondSize, planetPositions, textMeasurer, colors.tertiary)

        // Draw ascendant marker
        drawAscendantMarker(center, diamondSize, textMeasurer, colors.secondary)
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

private fun DrawScope.drawHouseDivisions(center: Offset, size: Float, color: Color) {
    val halfSize = size / 2f

    // Horizontal middle line
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y),
        end = Offset(center.x + halfSize, center.y),
        strokeWidth = 2f
    )

    // Vertical middle line
    drawLine(
        color = color,
        start = Offset(center.x, center.y - halfSize),
        end = Offset(center.x, center.y + halfSize),
        strokeWidth = 2f
    )

    // Diagonal lines for inner divisions
    // Top-left to center divisions
    drawLine(
        color = color,
        start = Offset(center.x - halfSize, center.y),
        end = Offset(center.x, center.y - halfSize),
        strokeWidth = 1.5f
    )

    // Top-right to center divisions
    drawLine(
        color = color,
        start = Offset(center.x, center.y - halfSize),
        end = Offset(center.x + halfSize, center.y),
        strokeWidth = 1.5f
    )

    // Bottom-right to center divisions
    drawLine(
        color = color,
        start = Offset(center.x + halfSize, center.y),
        end = Offset(center.x, center.y + halfSize),
        strokeWidth = 1.5f
    )

    // Bottom-left to center divisions
    drawLine(
        color = color,
        start = Offset(center.x, center.y + halfSize),
        end = Offset(center.x - halfSize, center.y),
        strokeWidth = 1.5f
    )
}

private fun DrawScope.drawHouseNumbers(
    center: Offset,
    size: Float,
    textMeasurer: TextMeasurer,
    color: Color
) {
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
        val textLayoutResult = textMeasurer.measure(
            text = houseNumber,
            style = TextStyle(
                color = color,
                fontSize = (size * 0.04f).sp
            )
        )
        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = position.copy(
                x = position.x - textLayoutResult.size.width / 2,
                y = position.y - textLayoutResult.size.height / 2
            )
        )
    }
}

private fun DrawScope.drawPlanets(
    center: Offset,
    size: Float,
    planetPositions: List<PlanetPosition>,
    textMeasurer: TextMeasurer,
    color: Color
) {
    val halfSize = size / 2f

    // Group planets by house
    val planetsByHouse = planetPositions.groupBy { it.house }

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
            val textLayoutResult = textMeasurer.measure(
                text = planetText,
                style = TextStyle(
                    color = color,
                    fontSize = (size * 0.035f).sp
                )
            )

            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = position.copy(
                    x = position.x - textLayoutResult.size.width / 2,
                    y = position.y - textLayoutResult.size.height / 2
                )
            )
        }
    }
}

private fun DrawScope.drawAscendantMarker(
    center: Offset,
    size: Float,
    textMeasurer: TextMeasurer,
    color: Color
) {
    val halfSize = size / 2f
    val markerPosition = Offset(center.x - halfSize * 0.1f, center.y - halfSize * 0.6f)
    val textLayoutResult = textMeasurer.measure(
        text = "As",
        style = TextStyle(
            color = color,
            fontSize = (size * 0.045f).sp
        )
    )
    drawText(
        textLayoutResult = textLayoutResult,
        topLeft = markerPosition.copy(
            x = markerPosition.x - textLayoutResult.size.width / 2,
            y = markerPosition.y - textLayoutResult.size.height / 2
        )
    )
}
