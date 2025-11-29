
package com.astro.storm.ui.chart

import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

data class ChartTheme(
    val backgroundColor: Color = Color(0xFFD4C4A8),
    val borderColor: Color = Color(0xFFB8860B),
    val houseNumberColor: Color = Color(0xFF4A4A4A),
    val borderStroke: Stroke = Stroke(width = 3f),
    val lineStroke: Stroke = Stroke(width = 2.5f),
    val normalTypeface: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
    val boldTypeface: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD),
    val sunColor: Color = Color(0xFFD2691E),
    val moonColor: Color = Color(0xFFDC143C),
    val marsColor: Color = Color(0xFFDC143C),
    val mercuryColor: Color = Color(0xFF228B22),
    val jupiterColor: Color = Color(0xFFDAA520),
    val venusColor: Color = Color(0xFF9370DB),
    val saturnColor: Color = Color(0xFF4169E1),
    val rahuColor: Color = Color(0xFF8B0000),
    val ketuColor: Color = Color(0xFF8B0000),
    val uranusColor: Color = Color(0xFF20B2AA),
    val neptuneColor: Color = Color(0xFF4682B4),
    val plutoColor: Color = Color(0xFF800080),
    val lagnaColor: Color = Color(0xFF8B4513)
) {
    fun getPlanetColor(planet: com.astro.storm.data.model.Planet): Color {
        return when (planet) {
            com.astro.storm.data.model.Planet.SUN -> sunColor
            com.astro.storm.data.model.Planet.MOON -> moonColor
            com.astro.storm.data.model.Planet.MARS -> marsColor
            com.astro.storm.data.model.Planet.MERCURY -> mercuryColor
            com.astro.storm.data.model.Planet.JUPITER -> jupiterColor
            com.astro.storm.data.model.Planet.VENUS -> venusColor
            com.astro.storm.data.model.Planet.SATURN -> saturnColor
            com.astro.storm.data.model.Planet.RAHU -> rahuColor
            com.astro.storm.data.model.Planet.KETU -> ketuColor
            com.astro.storm.data.model.Planet.URANUS -> uranusColor
            com.astro.storm.data.model.Planet.NEPTUNE -> neptuneColor
            com.astro.storm.data.model.Planet.PLUTO -> plutoColor
        }
    }
}
