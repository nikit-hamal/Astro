package com.astro.storm.calculator

import com.astro.storm.data.model.*
import kotlin.math.floor

/**
 * High-precision Divisional Chart (Varga) Calculator
 * Implements D9 (Navamsa), D10 (Dashamsa), D60 (Shashtyamsa) with exact calculations
 */
class DivisionalChartCalculator {

    /**
     * Calculate any divisional chart
     */
    fun calculateDivisionalChart(
        vedicChart: VedicChart,
        division: DivisionalChartType
    ): DivisionalChart {
        val divisionalPositions = vedicChart.planetPositions.map { position ->
            calculateDivisionalPosition(position, division)
        }

        return DivisionalChart(
            baseChart = vedicChart,
            type = division,
            planetPositions = divisionalPositions,
            ascendant = calculateDivisionalLongitude(vedicChart.ascendant, division),
            calculationMethod = "Parashari"
        )
    }

    /**
     * Calculate divisional position for a planet
     */
    private fun calculateDivisionalPosition(
        position: PlanetPosition,
        division: DivisionalChartType
    ): PlanetPosition {
        val divisionalLongitude = calculateDivisionalLongitude(position.longitude, division)
        val divisionalSign = ZodiacSign.fromLongitude(divisionalLongitude)
        val degreeInSign = divisionalLongitude % 30.0

        val (nakshatra, pada) = Nakshatra.fromLongitude(divisionalLongitude)

        return position.copy(
            longitude = divisionalLongitude,
            sign = divisionalSign,
            degree = degreeInSign.toInt().toDouble(),
            minutes = ((degreeInSign - degreeInSign.toInt()) * 60.0).toInt().toDouble(),
            seconds = ((((degreeInSign - degreeInSign.toInt()) * 60.0) -
                ((degreeInSign - degreeInSign.toInt()) * 60.0).toInt()) * 60.0),
            nakshatra = nakshatra,
            nakshatraPada = pada
        )
    }

    /**
     * Calculate divisional longitude based on division type
     * This is the core algorithm for all divisional charts
     */
    private fun calculateDivisionalLongitude(
        longitude: Double,
        division: DivisionalChartType
    ): Double {
        val normalizedLongitude = (longitude % 360.0 + 360.0) % 360.0

        // Get sign (0-11) and position within sign (0-30)
        val signNumber = floor(normalizedLongitude / 30.0).toInt()
        val degreeInSign = normalizedLongitude % 30.0

        return when (division) {
            DivisionalChartType.D9 -> calculateD9(signNumber, degreeInSign)
            DivisionalChartType.D10 -> calculateD10(signNumber, degreeInSign)
            DivisionalChartType.D60 -> calculateD60(signNumber, degreeInSign)
        }
    }

    /**
     * Calculate D9 (Navamsa) position
     * Each sign divided into 9 parts of 3°20' each
     */
    private fun calculateD9(signNumber: Int, degreeInSign: Double): Double {
        // Each navamsa = 3.333333 degrees
        val navamsaNumber = floor(degreeInSign / 3.333333).toInt()

        // Navamsa starts from:
        // - Aries, Leo, Sagittarius (Fire): Aries (sign 0)
        // - Taurus, Virgo, Capricorn (Earth): Capricorn (sign 9)
        // - Gemini, Libra, Aquarius (Air): Libra (sign 6)
        // - Cancer, Scorpio, Pisces (Water): Cancer (sign 3)

        val startSign = when (signNumber % 4) {
            0 -> 0   // Fire (Aries, Leo, Sagittarius)
            1 -> 9   // Earth (Taurus, Virgo, Capricorn)
            2 -> 6   // Air (Gemini, Libra, Aquarius)
            3 -> 3   // Water (Cancer, Scorpio, Pisces)
            else -> 0
        }

        val navamsaSign = (startSign + navamsaNumber) % 12
        val degreeInNavamsa = (degreeInSign % 3.333333) / 3.333333 * 30.0

        return (navamsaSign * 30.0 + degreeInNavamsa) % 360.0
    }

    /**
     * Calculate D10 (Dashamsa) position
     * Each sign divided into 10 parts of 3° each
     * Used for career and profession analysis
     */
    private fun calculateD10(signNumber: Int, degreeInSign: Double): Double {
        // Each dashamsa = 3 degrees
        val dashamsaNumber = floor(degreeInSign / 3.0).toInt()

        // Dashamsa starts from:
        // - Odd signs: Same sign
        // - Even signs: 9th from the sign

        val isOddSign = (signNumber % 2) == 0  // 0-indexed, so Aries (0) is odd

        val startSign = if (isOddSign) {
            signNumber
        } else {
            (signNumber + 8) % 12  // 9th sign (8 signs ahead)
        }

        val dashamsaSign = (startSign + dashamsaNumber) % 12
        val degreeInDashamsa = (degreeInSign % 3.0) / 3.0 * 30.0

        return (dashamsaSign * 30.0 + degreeInDashamsa) % 360.0
    }

    /**
     * Calculate D60 (Shashtyamsa) position
     * Each sign divided into 60 parts of 30' (0.5°) each
     * Most precise divisional chart, shows karmic patterns
     */
    private fun calculateD60(signNumber: Int, degreeInSign: Double): Double {
        // Each shashtyamsa = 0.5 degrees (30 minutes)
        val shashtyamsaNumber = floor(degreeInSign / 0.5).toInt()

        // D60 follows a specific pattern based on sign type
        // Odd signs start from Aries, even signs start from Libra
        val isOddSign = (signNumber % 2) == 0  // 0-indexed

        val startSign = if (isOddSign) 0 else 6  // Aries or Libra

        // The shashtyamsa progresses through all 60 divisions
        // cycling through the zodiac 5 times
        val shashtyamsaSign = (startSign + (shashtyamsaNumber % 12)) % 12
        val cycleAdjustment = (shashtyamsaNumber / 12) * 12

        val finalSign = (shashtyamsaSign + cycleAdjustment) % 12
        val degreeInShashtyamsa = (degreeInSign % 0.5) / 0.5 * 30.0

        return (finalSign * 30.0 + degreeInShashtyamsa) % 360.0
    }

    /**
     * Calculate all common divisional charts
     */
    fun calculateAllDivisionalCharts(vedicChart: VedicChart): List<DivisionalChart> {
        return listOf(
            calculateDivisionalChart(vedicChart, DivisionalChartType.D9),
            calculateDivisionalChart(vedicChart, DivisionalChartType.D10),
            calculateDivisionalChart(vedicChart, DivisionalChartType.D60)
        )
    }

    /**
     * Get planet strength based on divisional chart positions
     * A planet in own sign or exaltation in divisional chart gains strength
     */
    fun calculateDivisionalStrength(
        planet: Planet,
        divisionalPositions: List<DivisionalChart>
    ): Double {
        var strength = 0.0

        divisionalPositions.forEach { chart ->
            val position = chart.planetPositions.find { it.planet == planet }
            if (position != null) {
                // Add strength based on sign placement
                strength += when {
                    isInOwnSign(planet, position.sign) -> 1.0
                    isInExaltation(planet, position.sign) -> 1.5
                    isInMoolatrikona(planet, position.sign) -> 0.75
                    isInFriendSign(planet, position.sign) -> 0.5
                    else -> 0.0
                }

                // Weight by divisional chart importance
                val weight = when (chart.type) {
                    DivisionalChartType.D9 -> 1.5   // Navamsa is most important
                    DivisionalChartType.D10 -> 1.0
                    DivisionalChartType.D60 -> 1.2  // Very precise
                }

                strength *= weight
            }
        }

        return strength
    }

    /**
     * Check if planet is in own sign
     */
    private fun isInOwnSign(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LEO
            Planet.MOON -> sign == ZodiacSign.CANCER
            Planet.MARS -> sign in listOf(ZodiacSign.ARIES, ZodiacSign.SCORPIO)
            Planet.MERCURY -> sign in listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO)
            Planet.JUPITER -> sign in listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES)
            Planet.VENUS -> sign in listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA)
            Planet.SATURN -> sign in listOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS)
            else -> false
        }
    }

    /**
     * Check if planet is in exaltation
     */
    private fun isInExaltation(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            else -> false
        }
    }

    /**
     * Check if planet is in moolatrikona
     */
    private fun isInMoolatrikona(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LEO
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.ARIES
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.SAGITTARIUS
            Planet.VENUS -> sign == ZodiacSign.LIBRA
            Planet.SATURN -> sign == ZodiacSign.AQUARIUS
            else -> false
        }
    }

    /**
     * Check if planet is in friend's sign (simplified)
     */
    private fun isInFriendSign(planet: Planet, sign: ZodiacSign): Boolean {
        // Simplified friendship rules
        val friendSigns = when (planet) {
            Planet.SUN -> listOf(ZodiacSign.ARIES, ZodiacSign.SAGITTARIUS, ZodiacSign.LEO)
            Planet.MOON -> listOf(ZodiacSign.TAURUS, ZodiacSign.CANCER)
            Planet.MARS -> listOf(ZodiacSign.LEO, ZodiacSign.ARIES, ZodiacSign.SAGITTARIUS)
            Planet.MERCURY -> listOf(ZodiacSign.GEMINI, ZodiacSign.VIRGO)
            Planet.JUPITER -> listOf(ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES, ZodiacSign.CANCER)
            Planet.VENUS -> listOf(ZodiacSign.TAURUS, ZodiacSign.LIBRA, ZodiacSign.PISCES)
            Planet.SATURN -> listOf(ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS, ZodiacSign.LIBRA)
            else -> emptyList()
        }

        return sign in friendSigns
    }

    /**
     * Analyze Navamsa (D9) chart specifically
     * D9 is the most important divisional chart
     */
    fun analyzeNavamsa(d9Chart: DivisionalChart): NavamsaAnalysis {
        val vargottamaPositions = mutableListOf<Planet>()
        val puskaraNavamsaPositions = mutableListOf<Planet>()

        d9Chart.planetPositions.forEach { d9Position ->
            // Find corresponding position in base chart
            val d1Position = d9Chart.baseChart.planetPositions.find {
                it.planet == d9Position.planet
            }

            if (d1Position != null) {
                // Check Vargottama (same sign in D1 and D9)
                if (d1Position.sign == d9Position.sign) {
                    vargottamaPositions.add(d9Position.planet)
                }

                // Check Pushkara Navamsa (specific degrees in Cancer and Capricorn)
                if (isPushkaraNavamsa(d9Position)) {
                    puskaraNavamsaPositions.add(d9Position.planet)
                }
            }
        }

        return NavamsaAnalysis(
            vargottamaPlanets = vargottamaPositions,
            puskaraNavamsaPlanets = puskaraNavamsaPositions,
            d9LagnaLord = getLagnaLord(ZodiacSign.fromLongitude(d9Chart.ascendant))
        )
    }

    /**
     * Check if position is in Pushkara Navamsa
     * Specific navamsas in Cancer and Capricorn that give special strength
     */
    private fun isPushkaraNavamsa(position: PlanetPosition): Boolean {
        val sign = position.sign
        val degreeInSign = position.longitude % 30.0
        val navamsaNumber = floor(degreeInSign / 3.333333).toInt()

        return when (sign) {
            ZodiacSign.CANCER -> navamsaNumber == 7  // 8th navamsa
            ZodiacSign.CAPRICORN -> navamsaNumber == 7  // 8th navamsa
            else -> false
        }
    }

    /**
     * Get lagna lord for a sign
     */
    private fun getLagnaLord(sign: ZodiacSign): Planet {
        return when (sign) {
            ZodiacSign.ARIES, ZodiacSign.SCORPIO -> Planet.MARS
            ZodiacSign.TAURUS, ZodiacSign.LIBRA -> Planet.VENUS
            ZodiacSign.GEMINI, ZodiacSign.VIRGO -> Planet.MERCURY
            ZodiacSign.CANCER -> Planet.MOON
            ZodiacSign.LEO -> Planet.SUN
            ZodiacSign.SAGITTARIUS, ZodiacSign.PISCES -> Planet.JUPITER
            ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS -> Planet.SATURN
        }
    }
}

/**
 * Navamsa-specific analysis
 */
data class NavamsaAnalysis(
    val vargottamaPlanets: List<Planet>,
    val puskaraNavamsaPlanets: List<Planet>,
    val d9LagnaLord: Planet
) {
    fun toFormattedString(): String {
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("        NAVAMSA ANALYSIS (D9)")
            appendLine("═══════════════════════════════════════")
            appendLine()

            if (vargottamaPlanets.isNotEmpty()) {
                appendLine("VARGOTTAMA PLANETS (Same sign in D1 & D9):")
                vargottamaPlanets.forEach { planet ->
                    appendLine("  ${planet.symbol} ${planet.displayName} - Very Strong")
                }
                appendLine()
            }

            if (puskaraNavamsaPlanets.isNotEmpty()) {
                appendLine("PUSHKARA NAVAMSA PLANETS (Special strength):")
                puskaraNavamsaPlanets.forEach { planet ->
                    appendLine("  ${planet.symbol} ${planet.displayName}")
                }
                appendLine()
            }

            appendLine("D9 Lagna Lord: ${d9LagnaLord.symbol} ${d9LagnaLord.displayName}")
        }
    }
}
