package com.astro.storm.data.model

import androidx.compose.ui.graphics.vector.ImageVector

data class InsightsData(
    val currentPlanetaryPeriod: PlanetaryPeriod,
    val upcomingTransits: List<Transit>,
    val otherActions: List<Action>
)

data class PlanetaryPeriod(
    val name: String,
    val yearsRemaining: Int,
    val progress: Float
)

data class Transit(
    val name: String,
    val date: String
)

data class Action(
    val name: String,
    val icon: ImageVector
)