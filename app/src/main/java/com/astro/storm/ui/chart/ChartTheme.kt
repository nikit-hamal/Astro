package com.astro.storm.ui.chart

import android.graphics.Typeface
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke

data class ChartTheme(
    val backgroundColor: Color = Color(0xFFD4C4A8), // Warm parchment background
    val borderColor: Color = Color(0xFFB8860B), // Dark goldenrod for lines
    val houseNumberColor: Color = Color(0xFF4A4A4A), // Dark gray for house numbers
    val lagnaColor: Color = Color(0xFF8B4513), // Saddle brown for Lagna marker
    val borderStroke: Stroke = Stroke(width = 3f),
    val lineStroke: Stroke = Stroke(width = 2.5f),
    val normalTypeface: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL),
    val boldTypeface: Typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
)
