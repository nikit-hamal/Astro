package com.astro.storm.data.model

/**
 * Represents planets used in Vedic astrology
 */
enum class Planet(val swissEphId: Int, val displayName: String, val symbol: String) {
    SUN(0, "Sun", "Su"),
    MOON(1, "Moon", "Mo"),
    MERCURY(2, "Mercury", "Me"),
    VENUS(3, "Venus", "Ve"),
    MARS(4, "Mars", "Ma"),
    JUPITER(5, "Jupiter", "Ju"),
    SATURN(6, "Saturn", "Sa"),
    RAHU(11, "Rahu", "Ra"),  // Mean node
    KETU(11, "Ketu", "Ke"),  // 180° from Rahu
    URANUS(7, "Uranus", "Ur"),
    NEPTUNE(8, "Neptune", "Ne"),
    PLUTO(9, "Pluto", "Pl");

    companion object {
        val MAIN_PLANETS = listOf(SUN, MOON, MARS, MERCURY, JUPITER, VENUS, SATURN, RAHU, KETU)
    }
}
