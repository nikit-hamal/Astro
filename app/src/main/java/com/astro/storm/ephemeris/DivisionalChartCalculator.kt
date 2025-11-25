package com.astro.storm.ephemeris

import com.astro.storm.data.model.*

/**
 * High-precision Divisional Chart (Varga) Calculator
 *
 * Implements the following divisional charts with traditional Vedic formulas:
 * - D9 (Navamsa) - Most important divisional chart for marriage and dharma
 * - D10 (Dasamsa) - Career and profession
 * - D60 (Shashtiamsa) - Most precise, for past life karma
 *
 * The formulas used are based on Parashari principles from BPHS (Brihat Parasara Hora Shastra)
 */
object DivisionalChartCalculator {

    /**
     * Calculate D9 (Navamsa) positions for all planets
     *
     * Navamsa divides each sign into 9 equal parts of 3°20' each.
     * The navamsa sign depends on the sign type:
     * - Movable signs (Ar,Ca,Li,Cp): Start from the same sign
     * - Fixed signs (Ta,Le,Sc,Aq): Start from 9th sign
     * - Dual signs (Ge,Vi,Sg,Pi): Start from 5th sign
     *
     * @param chart The original Lagna/Rasi chart
     * @return List of planet positions in Navamsa chart
     */
    fun calculateNavamsa(chart: VedicChart): DivisionalChartData {
        val navamsaPositions = chart.planetPositions.map { position ->
            calculateNavamsaPosition(position)
        }

        val navamsaAscendant = calculateNavamsaLongitude(chart.ascendant)

        return DivisionalChartData(
            chartType = DivisionalChartType.D9_NAVAMSA,
            planetPositions = navamsaPositions,
            ascendantLongitude = navamsaAscendant,
            chartTitle = "Navamsa (D9)"
        )
    }

    private fun calculateNavamsaPosition(position: PlanetPosition): PlanetPosition {
        val navamsaLongitude = calculateNavamsaLongitude(position.longitude)
        val navamsaSign = ZodiacSign.fromLongitude(navamsaLongitude)
        val degreeInSign = navamsaLongitude % 30.0
        val (nakshatra, pada) = Nakshatra.fromLongitude(navamsaLongitude)

        // Calculate house based on navamsa position
        val navamsaHouse = calculateHouseFromLongitude(navamsaLongitude, navamsaSign.number)

        return position.copy(
            longitude = navamsaLongitude,
            sign = navamsaSign,
            degree = degreeInSign.toInt().toDouble(),
            minutes = ((degreeInSign - degreeInSign.toInt()) * 60).toInt().toDouble(),
            seconds = ((((degreeInSign - degreeInSign.toInt()) * 60) - ((degreeInSign - degreeInSign.toInt()) * 60).toInt()) * 60),
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = navamsaHouse
        )
    }

    private fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        // Each navamsa spans 3°20' (3.333...)
        val navamsaPart = (degreeInSign / 3.333333333).toInt() // 0-8

        // Calculate starting sign based on sign type (Movable, Fixed, Dual)
        val startingSignIndex = when (signNumber % 3) {
            0 -> signNumber          // Movable: Ar(0), Ca(3), Li(6), Cp(9) - start from same sign
            1 -> (signNumber + 8) % 12  // Fixed: Ta(1), Le(4), Sc(7), Aq(10) - start from 9th sign
            2 -> (signNumber + 4) % 12  // Dual: Ge(2), Vi(5), Sg(8), Pi(11) - start from 5th sign
            else -> signNumber
        }

        // Calculate final navamsa sign
        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12

        // Calculate degree within navamsa sign
        val positionInNavamsa = degreeInSign % 3.333333333
        val navamsaDegree = (positionInNavamsa / 3.333333333) * 30.0

        return (navamsaSignIndex * 30.0) + navamsaDegree
    }

    /**
     * Calculate D10 (Dasamsa) positions for all planets
     *
     * Dasamsa divides each sign into 10 equal parts of 3° each.
     * - Odd signs (Ar,Ge,Le,Li,Sg,Aq): Start from the same sign
     * - Even signs (Ta,Ca,Vi,Sc,Cp,Pi): Start from 9th sign
     *
     * @param chart The original Lagna/Rasi chart
     * @return List of planet positions in Dasamsa chart
     */
    fun calculateDasamsa(chart: VedicChart): DivisionalChartData {
        val dasamsaPositions = chart.planetPositions.map { position ->
            calculateDasamsaPosition(position)
        }

        val dasamsaAscendant = calculateDasamsaLongitude(chart.ascendant)

        return DivisionalChartData(
            chartType = DivisionalChartType.D10_DASAMSA,
            planetPositions = dasamsaPositions,
            ascendantLongitude = dasamsaAscendant,
            chartTitle = "Dasamsa (D10)"
        )
    }

    private fun calculateDasamsaPosition(position: PlanetPosition): PlanetPosition {
        val dasamsaLongitude = calculateDasamsaLongitude(position.longitude)
        val dasamsaSign = ZodiacSign.fromLongitude(dasamsaLongitude)
        val degreeInSign = dasamsaLongitude % 30.0
        val (nakshatra, pada) = Nakshatra.fromLongitude(dasamsaLongitude)

        val dasamsaHouse = calculateHouseFromLongitude(dasamsaLongitude, dasamsaSign.number)

        return position.copy(
            longitude = dasamsaLongitude,
            sign = dasamsaSign,
            degree = degreeInSign.toInt().toDouble(),
            minutes = ((degreeInSign - degreeInSign.toInt()) * 60).toInt().toDouble(),
            seconds = ((((degreeInSign - degreeInSign.toInt()) * 60) - ((degreeInSign - degreeInSign.toInt()) * 60).toInt()) * 60),
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = dasamsaHouse
        )
    }

    private fun calculateDasamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        // Each dasamsa spans 3°
        val dasamsaPart = (degreeInSign / 3.0).toInt().coerceIn(0, 9) // 0-9

        // Starting sign based on odd/even
        val startingSignIndex = if (signNumber % 2 == 0) {
            signNumber // Odd signs (Aries=0 is odd in Vedic counting)
        } else {
            (signNumber + 8) % 12 // Even signs start from 9th
        }

        // Calculate final dasamsa sign
        val dasamsaSignIndex = (startingSignIndex + dasamsaPart) % 12

        // Calculate degree within dasamsa sign
        val positionInDasamsa = degreeInSign % 3.0
        val dasamsaDegree = (positionInDasamsa / 3.0) * 30.0

        return (dasamsaSignIndex * 30.0) + dasamsaDegree
    }

    /**
     * Calculate D60 (Shashtiamsa) positions for all planets
     *
     * Shashtiamsa divides each sign into 60 equal parts of 0°30' each.
     * This is the most precise divisional chart and relates to past karma.
     *
     * - Odd signs: Start from the same sign
     * - Even signs: Start from 7th sign (Libra for Taurus, etc.)
     *
     * @param chart The original Lagna/Rasi chart
     * @return List of planet positions in Shashtiamsa chart
     */
    fun calculateShashtiamsa(chart: VedicChart): DivisionalChartData {
        val shashtiamsaPositions = chart.planetPositions.map { position ->
            calculateShashtiamsaPosition(position)
        }

        val shashtiamsaAscendant = calculateShashtiamsaLongitude(chart.ascendant)

        return DivisionalChartData(
            chartType = DivisionalChartType.D60_SHASHTIAMSA,
            planetPositions = shashtiamsaPositions,
            ascendantLongitude = shashtiamsaAscendant,
            chartTitle = "Shashtiamsa (D60)"
        )
    }

    private fun calculateShashtiamsaPosition(position: PlanetPosition): PlanetPosition {
        val shashtiamsaLongitude = calculateShashtiamsaLongitude(position.longitude)
        val shashtiamsaSign = ZodiacSign.fromLongitude(shashtiamsaLongitude)
        val degreeInSign = shashtiamsaLongitude % 30.0
        val (nakshatra, pada) = Nakshatra.fromLongitude(shashtiamsaLongitude)

        val shashtiamsaHouse = calculateHouseFromLongitude(shashtiamsaLongitude, shashtiamsaSign.number)

        return position.copy(
            longitude = shashtiamsaLongitude,
            sign = shashtiamsaSign,
            degree = degreeInSign.toInt().toDouble(),
            minutes = ((degreeInSign - degreeInSign.toInt()) * 60).toInt().toDouble(),
            seconds = ((((degreeInSign - degreeInSign.toInt()) * 60) - ((degreeInSign - degreeInSign.toInt()) * 60).toInt()) * 60),
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = shashtiamsaHouse
        )
    }

    private fun calculateShashtiamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        // Each shashtiamsa spans 0.5° (30 minutes)
        val shashtiamsaPart = (degreeInSign / 0.5).toInt().coerceIn(0, 59) // 0-59

        // Starting sign based on odd/even
        val startingSignIndex = if (signNumber % 2 == 0) {
            signNumber // Odd signs start from same sign
        } else {
            (signNumber + 6) % 12 // Even signs start from 7th sign
        }

        // Calculate final shashtiamsa sign (cycles through 5 complete zodiac rounds)
        val shashtiamsaSignIndex = (startingSignIndex + shashtiamsaPart) % 12

        // Calculate degree within shashtiamsa sign
        val positionInShashtiamsa = degreeInSign % 0.5
        val shashtiamsaDegree = (positionInShashtiamsa / 0.5) * 30.0

        return (shashtiamsaSignIndex * 30.0) + shashtiamsaDegree
    }

    /**
     * Simple house calculation based on sign position
     * In divisional charts, house 1 contains the divisional ascendant sign
     */
    private fun calculateHouseFromLongitude(longitude: Double, ascendantSignNumber: Int): Int {
        val signNumber = ZodiacSign.fromLongitude(longitude).number
        val houseOffset = (signNumber - ascendantSignNumber + 12) % 12
        return if (houseOffset == 0) 1 else houseOffset + 1
    }

    /**
     * Get all divisional charts at once
     */
    fun calculateAllDivisionalCharts(chart: VedicChart): List<DivisionalChartData> {
        return listOf(
            calculateNavamsa(chart),
            calculateDasamsa(chart),
            calculateShashtiamsa(chart)
        )
    }
}

/**
 * Enum for divisional chart types
 */
enum class DivisionalChartType(val division: Int, val displayName: String, val description: String) {
    D9_NAVAMSA(9, "Navamsa", "Marriage, Dharma, Fortune"),
    D10_DASAMSA(10, "Dasamsa", "Career, Profession, Status"),
    D60_SHASHTIAMSA(60, "Shashtiamsa", "Past Life Karma, General Fortune")
}

/**
 * Data class for divisional chart results
 */
data class DivisionalChartData(
    val chartType: DivisionalChartType,
    val planetPositions: List<PlanetPosition>,
    val ascendantLongitude: Double,
    val chartTitle: String
) {
    fun toPlainText(): String {
        return buildString {
            appendLine("═══════════════════════════════════════════════════")
            appendLine("           ${chartType.displayName.uppercase()} CHART (D${chartType.division})")
            appendLine("           ${chartType.description}")
            appendLine("═══════════════════════════════════════════════════")
            appendLine()
            appendLine("Ascendant: ${formatDegree(ascendantLongitude)} (${ZodiacSign.fromLongitude(ascendantLongitude).displayName})")
            appendLine()
            appendLine("PLANETARY POSITIONS")
            appendLine("───────────────────────────────────────────────────")
            planetPositions.forEach { position ->
                val retrograde = if (position.isRetrograde) " [R]" else ""
                appendLine("${position.planet.displayName.padEnd(10)}: ${position.sign.displayName.padEnd(12)} ${formatDegreeInSign(position.longitude)}$retrograde | House ${position.house}")
            }
            appendLine()
        }
    }

    private fun formatDegree(degree: Double): String {
        val normalizedDegree = (degree % 360.0 + 360.0) % 360.0
        val deg = normalizedDegree.toInt()
        val min = ((normalizedDegree - deg) * 60).toInt()
        val sec = ((((normalizedDegree - deg) * 60) - min) * 60).toInt()
        return "$deg° $min' $sec\""
    }

    private fun formatDegreeInSign(longitude: Double): String {
        val degreeInSign = longitude % 30.0
        val deg = degreeInSign.toInt()
        val min = ((degreeInSign - deg) * 60).toInt()
        val sec = ((((degreeInSign - deg) * 60) - min) * 60).toInt()
        return "$deg° $min' $sec\""
    }
}
