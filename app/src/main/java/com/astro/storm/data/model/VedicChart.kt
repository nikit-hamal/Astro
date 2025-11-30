package com.astro.storm.data.model

/**
 * Complete Vedic astrology chart
 */
data class VedicChart(
    val birthData: BirthData,
    val julianDay: Double,
    val ayanamsa: Double,
    val ayanamsaName: String,
    val ascendant: Double,
    val midheaven: Double,
    val planetPositions: List<PlanetPosition>,
    val houseCusps: List<Double>,
    val houseSystem: HouseSystem,
    val calculationTime: Long = System.currentTimeMillis()
) {
    val planetsByHouse: Map<Int, List<PlanetPosition>> by lazy {
        planetPositions.groupBy { it.house }
    }
}
