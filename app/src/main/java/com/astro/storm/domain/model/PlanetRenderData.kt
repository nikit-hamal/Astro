package com.astro.storm.domain.model

import androidx.compose.ui.graphics.Color
import com.astro.storm.data.model.Planet

data class PlanetRenderData(
    val planet: Planet,
    val symbol: String,
    val degreeText: String,
    val color: Color,
    val statusIndicators: List<String>
)
