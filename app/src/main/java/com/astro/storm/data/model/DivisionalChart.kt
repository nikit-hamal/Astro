package com.astro.storm.data.model

enum class DivisionalChartType(val division: Int, val displayName: String) {
    D9(9, "Navamsa"),
    D10(10, "Dasamsa"),
    D60(60, "Shashtyamsa")
}

data class DivisionalChart(
    val type: DivisionalChartType,
    val planetPositions: List<PlanetPosition>
)
