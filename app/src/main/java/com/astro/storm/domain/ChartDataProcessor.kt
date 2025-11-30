package com.astro.storm.domain

import androidx.compose.ui.graphics.Color
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.domain.model.PlanetRenderData
import kotlin.math.abs

class ChartDataProcessor {

    companion object {
        // Each sign is 30 degrees, divided into 9 Navamsa parts.
        // Each part is 30 / 9 = 3.333... degrees, which is exactly 10/3 degrees.
        // Using a precise constant avoids floating-point inaccuracies.
        private const val NAVAMSA_PART_DEGREES = 10.0 / 3.0

        // Status indicator symbols (matching AstroSage exactly)
        const val SYMBOL_RETROGRADE = "*"       // Retrograde motion
        const val SYMBOL_COMBUST = "^"          // Combust (too close to Sun)
        const val SYMBOL_VARGOTTAMA = "\u00A4"  // ¤ - Vargottama (same sign in D1 and D9)
        const val SYMBOL_EXALTED = "\u2191"     // ↑ - Exalted
        const val SYMBOL_DEBILITATED = "\u2193" // ↓ - Debilitated

        // Planet-specific colors matching AstroSage style
        private val SUN_COLOR = Color(0xFFD2691E) // Chocolate/orange for Sun
        private val MOON_COLOR = Color(0xFFDC143C) // Crimson for Moon
        private val MARS_COLOR = Color(0xFFDC143C) // Red for Mars
        private val MERCURY_COLOR = Color(0xFF228B22) // Forest green for Mercury
        private val JUPITER_COLOR = Color(0xFFDAA520) // Goldenrod for Jupiter
        private val VENUS_COLOR = Color(0xFF9370DB) // Medium purple for Venus
        private val SATURN_COLOR = Color(0xFF4169E1) // Royal blue for Saturn
        private val RAHU_COLOR = Color(0xFF8B0000) // Dark red for Rahu
        private val KETU_COLOR = Color(0xFF8B0000) // Dark red for Ketu
        private val URANUS_COLOR = Color(0xFF20B2AA) // Light sea green for Uranus
        private val NEPTUNE_COLOR = Color(0xFF4682B4) // Steel blue for Neptune
        private val PLUTO_COLOR = Color(0xFF800080) // Purple for Pluto
    }

    /**
     * Processes all planet positions in a chart to create render-ready data.
     */
    fun createRenderDataMap(chart: VedicChart): Map<Int, List<PlanetRenderData>> {
        val sunPosition = chart.planetPositions.find { it.planet == Planet.SUN }
        return chart.planetPositions.groupBy { it.house }
            .mapValues { entry ->
                entry.value.map { position ->
                    createPlanetRenderData(position, chart, sunPosition)
                }
            }
    }

    /**
     * Converts a single PlanetPosition into display-ready PlanetRenderData.
     */
    private fun createPlanetRenderData(
        planetPosition: PlanetPosition,
        chart: VedicChart,
        sunPosition: PlanetPosition?
    ): PlanetRenderData {
        val degree = (planetPosition.longitude % 30.0).toInt()
        val degreeSuper = toSuperscript(degree)

        val statusIndicators = mutableListOf<String>()
        if (planetPosition.isRetrograde) statusIndicators.add(SYMBOL_RETROGRADE)
        if (isExalted(planetPosition.planet, planetPosition.sign)) {
            statusIndicators.add(SYMBOL_EXALTED)
        } else if (isDebilitated(planetPosition.planet, planetPosition.sign)) {
            statusIndicators.add(SYMBOL_DEBILITATED)
        }
        if (isCombust(planetPosition, sunPosition)) {
            statusIndicators.add(SYMBOL_COMBUST)
        }
        if (isVargottama(planetPosition, chart)) {
            statusIndicators.add(SYMBOL_VARGOTTAMA)
        }

        return PlanetRenderData(
            planet = planetPosition.planet,
            symbol = planetPosition.planet.symbol,
            degreeText = degreeSuper,
            color = getPlanetColor(planetPosition.planet),
            statusIndicators = statusIndicators
        )
    }

    /**
     * Get color for a specific planet.
     */
    private fun getPlanetColor(planet: Planet): Color {
        return when (planet) {
            Planet.SUN -> SUN_COLOR
            Planet.MOON -> MOON_COLOR
            Planet.MARS -> MARS_COLOR
            Planet.MERCURY -> MERCURY_COLOR
            Planet.JUPITER -> JUPITER_COLOR
            Planet.VENUS -> VENUS_COLOR
            Planet.SATURN -> SATURN_COLOR
            Planet.RAHU -> RAHU_COLOR
            Planet.KETU -> KETU_COLOR
            Planet.URANUS -> URANUS_COLOR
            Planet.NEPTUNE -> NEPTUNE_COLOR
            Planet.PLUTO -> PLUTO_COLOR
        }
    }

    /**
     * Convert degree to superscript string.
     */
    private fun toSuperscript(degree: Int): String {
        val superscripts = mapOf(
            '0' to '\u2070', '1' to '\u00B9', '2' to '\u00B2', '3' to '\u00B3',
            '4' to '\u2074', '5' to '\u2075', '6' to '\u2076', '7' to '\u2077',
            '8' to '\u2078', '9' to '\u2079'
        )
        return degree.toString().map { superscripts[it] ?: it }.joinToString("")
    }

    /**
     * Check if planet is exalted in its current sign
     */
    fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            Planet.RAHU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            Planet.KETU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            else -> false
        }
    }

    /**
     * Check if planet is debilitated in its current sign
     */
    fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LIBRA
            Planet.MOON -> sign == ZodiacSign.SCORPIO
            Planet.MARS -> sign == ZodiacSign.CANCER
            Planet.MERCURY -> sign == ZodiacSign.PISCES
            Planet.JUPITER -> sign == ZodiacSign.CAPRICORN
            Planet.VENUS -> sign == ZodiacSign.VIRGO
            Planet.SATURN -> sign == ZodiacSign.ARIES
            Planet.RAHU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            Planet.KETU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            else -> false
        }
    }

    /**
     * Check if a planet is Vargottama (same sign in D1 Rashi and D9 Navamsa charts)
     */
    fun isVargottama(planet: PlanetPosition, chart: VedicChart): Boolean {
        val navamsaLongitude = calculateNavamsaLongitude(planet.longitude)
        val navamsaSign = ZodiacSign.fromLongitude(navamsaLongitude)
        return planet.sign == navamsaSign
    }

    /**
     * Calculate Navamsa longitude for a given longitude
     */
    fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val navamsaPart = (degreeInSign / NAVAMSA_PART_DEGREES).toInt().coerceIn(0, 8) // 0-8

        val startingSignIndex = when (ZodiacSign.values()[signNumber].quality) {
            ZodiacSign.Quality.CARDINAL -> signNumber
            ZodiacSign.Quality.FIXED -> (signNumber + 8) % 12
            ZodiacSign.Quality.MUTABLE -> (signNumber + 4) % 12
        }

        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12

        val positionInNavamsa = degreeInSign % NAVAMSA_PART_DEGREES
        val navamsaDegree = (positionInNavamsa / NAVAMSA_PART_DEGREES) * 30.0

        return (navamsaSignIndex * 30.0) + navamsaDegree
    }

    /**
     * Check if a planet is combust (too close to the Sun)
     */
    fun isCombust(planet: PlanetPosition, sunPosition: PlanetPosition?): Boolean {
        if (planet.planet == Planet.SUN) return false
        if (planet.planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return false
        }
        if (sunPosition == null) return false

        val angularDistance = calculateAngularDistance(planet.longitude, sunPosition.longitude)

        val combustionOrb = when (planet.planet) {
            Planet.MOON -> 12.0
            Planet.MARS -> 17.0
            Planet.MERCURY -> if (planet.isRetrograde) 12.0 else 14.0
            Planet.JUPITER -> 11.0
            Planet.VENUS -> if (planet.isRetrograde) 8.0 else 10.0
            Planet.SATURN -> 15.0
            else -> 0.0
        }

        return angularDistance <= combustionOrb
    }

    /**
     * Calculate the angular distance between two longitudes
     */
    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }
}
