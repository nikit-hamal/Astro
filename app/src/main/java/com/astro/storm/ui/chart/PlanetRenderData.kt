
package com.astro.storm.ui.chart

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.ZodiacSign

data class PlanetRenderData(
    val planet: Planet,
    val longitude: Double,
    val house: Int,
    val isRetrograde: Boolean,
    val isExalted: Boolean,
    val isDebilitated: Boolean,
    val isCombust: Boolean,
    val isVargottama: Boolean,
    val sign: ZodiacSign
)
