package com.astro.storm.ephemeris

import com.astro.storm.data.model.*

/**
 * A stateless, deterministic engine for calculating different types of Vedic charts.
 *
 * This calculator takes raw astronomical data from the `SwissEphemerisEngine` and transforms
 * it into a clean, UI-independent `ChartData` model. It ensures that chart calculations
 * are correct, repeatable, and decoupled from the UI rendering layer.
 */
class ChartCalculator {

    /**
     * Calculates the Lagna Chart (Rashi), the foundational chart in Vedic astrology.
     *
     * In a Rashi chart:
     * 1. The 12 houses are fixed positions on the diagram.
     * 2. The Ascendant (Lagna) sign occupies the first house (top diamond).
     * 3. The signs for the remaining houses (2-12) follow in zodiacal order, counter-clockwise.
     * 4. Planets are placed in houses based on the zodiac sign they fall into.
     *
     * This method correctly implements this logic, ensuring that the numbers displayed
     * in the chart are sign numbers (1 for Aries, 2 for Taurus, etc.), not fixed house
     * numbers.
     *
     * @param vedicChart The raw Vedic chart data containing planetary longitudes and ascendant.
     * @return A `ChartData` object representing the complete Rashi chart.
     */
    fun calculateRashiChart(vedicChart: VedicChart): ChartData {
        val ascendantSign = ZodiacSign.fromLongitude(vedicChart.ascendant)
        val houses = mutableListOf<ChartHouse>()

        // Create a map of sign to the list of planets in that sign
        val planetsBySign = vedicChart.planetPositions.groupBy { it.sign }

        // Determine the sign for each house, starting with the ascendant sign in house 1
        for (houseNumber in 1..12) {
            // The sign in the current house is determined by rotating from the ascendant sign.
            // House 1 has the ascendant sign. House 2 has the next sign, and so on.
            val signIndex = (ascendantSign.ordinal + houseNumber - 1) % 12
            val currentSign = ZodiacSign.values()[signIndex]

            // Get all planets that fall into the current sign.
            val planetsInHouse = planetsBySign[currentSign] ?: emptyList()

            houses.add(
                ChartHouse(
                    houseNumber = houseNumber,
                    sign = currentSign,
                    planets = planetsInHouse
                )
            )
        }

        return ChartData(
            houses = houses,
            ascendantSign = ascendantSign,
            chartType = ChartType.RASHI
        )
    }

    /**
     * Calculates the Bhava (Chalit) Chart, which represents the houses based on their actual cusps.
     *
     * In a Bhava chart:
     * 1. The house cusps (starting points) are calculated based on a specific house system (e.g., Placidus, Koch).
     * 2. The first house begins at the Ascendant degree.
     * 3. Planets are placed in houses based on whether their longitude falls between the house's start and end cusps.
     * 4. This can result in a planet being in a different house than in the Rashi chart.
     *
     * @param vedicChart The raw Vedic chart data containing planetary longitudes and house cusps.
     * @return A `ChartData` object representing the complete Bhava chart.
     */
    fun calculateBhavaChart(vedicChart: VedicChart): ChartData {
        val ascendantSign = ZodiacSign.fromLongitude(vedicChart.ascendant)
        val houses = mutableListOf<ChartHouse>()
        val houseCusps = listOf(vedicChart.ascendant) + vedicChart.houseCusps

        for (houseNumber in 1..12) {
            val cuspStart = houseCusps[houseNumber - 1]
            val cuspEnd = if (houseNumber == 12) (houseCusps[0] + 360 - 0.000001) % 360 else houseCusps[houseNumber]

            val planetsInHouse = vedicChart.planetPositions.filter {
                val longitude = it.longitude
                if (cuspEnd > cuspStart) {
                    longitude >= cuspStart && longitude < cuspEnd
                } else { // Wraps around 0° Aries
                    longitude >= cuspStart || longitude < cuspEnd
                }
            }

            // In a Bhava chart, the sign displayed in the house is typically the same as the Rashi chart.
            val signIndex = (ascendantSign.ordinal + houseNumber - 1) % 12
            val currentSign = ZodiacSign.values()[signIndex]

            houses.add(
                ChartHouse(
                    houseNumber = houseNumber,
                    sign = currentSign,
                    planets = planetsInHouse
                )
            )
        }

        return ChartData(
            houses = houses,
            ascendantSign = ascendantSign,
            chartType = ChartType.BHAVA
        )
    }

    /**
     * Calculates the Navamsa (D9) chart, the most important divisional chart.
     *
     * The Navamsa chart provides deeper insight into a person's life, especially marriage and the second half of life.
     * It is calculated by dividing each zodiac sign into 9 equal parts of 3°20' each.
     *
     * @param vedicChart The raw Rashi chart data.
     * @return A `ChartData` object representing the Navamsa chart.
     */
    fun calculateNavamsaChart(vedicChart: VedicChart): ChartData {
        // Calculate Navamsa longitude for the ascendant
        val navamsaAscendantLongitude = calculateNavamsaLongitude(vedicChart.ascendant)
        val navamsaAscendantSign = ZodiacSign.fromLongitude(navamsaAscendantLongitude)

        // Calculate Navamsa positions for all planets
        val navamsaPlanetPositions = vedicChart.planetPositions.map {
            val navamsaLongitude = calculateNavamsaLongitude(it.longitude)
            it.copy(
                longitude = navamsaLongitude,
                sign = ZodiacSign.fromLongitude(navamsaLongitude)
            )
        }

        val planetsBySign = navamsaPlanetPositions.groupBy { it.sign }
        val houses = mutableListOf<ChartHouse>()

        for (houseNumber in 1..12) {
            val signIndex = (navamsaAscendantSign.ordinal + houseNumber - 1) % 12
            val currentSign = ZodiacSign.values()[signIndex]
            val planetsInHouse = planetsBySign[currentSign] ?: emptyList()

            houses.add(
                ChartHouse(
                    houseNumber = houseNumber,
                    sign = currentSign,
                    planets = planetsInHouse
                )
            )
        }

        return ChartData(
            houses = houses,
            ascendantSign = navamsaAscendantSign,
            chartType = ChartType.NAVAMSA
        )
    }
     /**
     * Calculates the Dasamsa (D10) chart, the divisional chart for career and achievements.
     *
     * The Dasamsa chart provides insight into a person's professional life, reputation, and public standing.
     * It is calculated by dividing each zodiac sign into 10 equal parts of 3° each.
     *
     * @param vedicChart The raw Rashi chart data.
     * @return A `ChartData` object representing the Dasamsa chart.
     */
    fun calculateDasamsaChart(vedicChart: VedicChart): ChartData {
        val dasamsaAscendantLongitude = calculateDasamsaLongitude(vedicChart.ascendant)
        val dasamsaAscendantSign = ZodiacSign.fromLongitude(dasamsaAscendantLongitude)

        val dasamsaPlanetPositions = vedicChart.planetPositions.map {
            val dasamsaLongitude = calculateDasamsaLongitude(it.longitude)
            it.copy(
                longitude = dasamsaLongitude,
                sign = ZodiacSign.fromLongitude(dasamsaLongitude)
            )
        }

        val planetsBySign = dasamsaPlanetPositions.groupBy { it.sign }
        val houses = mutableListOf<ChartHouse>()

        for (houseNumber in 1..12) {
            val signIndex = (dasamsaAscendantSign.ordinal + houseNumber - 1) % 12
            val currentSign = ZodiacSign.values()[signIndex]
            val planetsInHouse = planetsBySign[currentSign] ?: emptyList()

            houses.add(
                ChartHouse(
                    houseNumber = houseNumber,
                    sign = currentSign,
                    planets = planetsInHouse
                )
            )
        }

        return ChartData(
            houses = houses,
            ascendantSign = dasamsaAscendantSign,
            chartType = ChartType.DASAMSA
        )
    }

    private fun calculateNavamsaLongitude(longitude: Double): Double {
        val sign = (longitude / 30).toInt()
        val degInSign = longitude % 30
        val navamsaNum = (degInSign / (30.0 / 9.0)).toInt()

        val signType = sign % 3 // 0 for movable, 1 for fixed, 2 for dual
        val startSign = when (signType) {
            0 -> sign // Movable signs (Aries, Cancer, Libra, Capricorn) - starts from itself
            1 -> (sign + 8) % 12 // Fixed signs (Taurus, Leo, Scorpio, Aquarius) - starts from 9th sign
            else -> (sign + 4) % 12 // Dual signs (Gemini, Virgo, Sagittarius, Pisces) - starts from 5th sign
        }

        val navamsaSign = (startSign + navamsaNum) % 12
        return navamsaSign * 30.0 + (navamsaNum * (30.0/9.0)) // Keep it simple, degree within sign not important for placement
    }

    private fun calculateDasamsaLongitude(longitude: Double): Double {
        val sign = (longitude / 30).toInt()
        val degInSign = longitude % 30
        val dasamsaNum = (degInSign / 3.0).toInt()

        val startSign = if (sign % 2 == 0) { // Odd signs (Aries, Gemini, etc. are 0, 2, ... in 0-indexed)
            sign
        } else { // Even signs
            (sign + 8) % 12
        }

        val dasamsaSign = (startSign + dasamsaNum) % 12
        return dasamsaSign * 30.0 + (dasamsaNum * 3.0)
    }
}
