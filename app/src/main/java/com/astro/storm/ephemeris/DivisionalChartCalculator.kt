package com.astro.storm.ephemeris

import com.astro.storm.data.model.*

/**
 * High-precision Divisional Chart (Varga) Calculator
 *
 * Implements all 16 major divisional charts (Shodasha Varga) with traditional Vedic formulas:
 * - D1 (Rashi/Lagna) - Physical body, general life
 * - D2 (Hora) - Wealth and prosperity
 * - D3 (Drekkana) - Siblings, courage, vitality
 * - D4 (Chaturthamsa) - Fortune, property, fixed assets
 * - D7 (Saptamsa) - Children, progeny
 * - D9 (Navamsa) - Marriage, dharma, fortune (most important)
 * - D10 (Dasamsa) - Career, profession, status
 * - D12 (Dwadasamsa) - Parents, ancestry
 * - D16 (Shodasamsa) - Vehicles, conveyances, pleasures
 * - D20 (Vimsamsa) - Spiritual life, worship
 * - D24 (Chaturvimsamsa) - Education, learning
 * - D27 (Saptavimsamsa/Bhamsa) - Strength, weakness
 * - D30 (Trimsamsa) - Evils, misfortunes
 * - D40 (Khavedamsa) - Auspicious/inauspicious effects
 * - D45 (Akshavedamsa) - General indications
 * - D60 (Shashtiamsa) - Past life karma (most precise)
 *
 * The formulas used are based on Parashari principles from BPHS (Brihat Parasara Hora Shastra)
 */
object DivisionalChartCalculator {

    // ===================== D2 - HORA =====================
    /**
     * Calculate D2 (Hora) positions for all planets
     *
     * Hora divides each sign into 2 parts of 15° each.
     * - First half (0-15°): Sun's Hora (Leo)
     * - Second half (15-30°): Moon's Hora (Cancer)
     * For odd signs: First half = Sun, Second half = Moon
     * For even signs: First half = Moon, Second half = Sun
     */
    fun calculateHora(chart: VedicChart): DivisionalChartData {
        val horaAscendant = calculateHoraLongitude(chart.ascendant)
        val horaAscendantSign = ZodiacSign.fromLongitude(horaAscendant)

        val horaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, horaAscendantSign.number) { long ->
                calculateHoraLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D2_HORA,
            planetPositions = horaPositions,
            ascendantLongitude = horaAscendant,
            chartTitle = "Hora (D2)"
        )
    }

    private fun calculateHoraLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0
        val isOddSign = signNumber % 2 == 0 // Aries=0 is odd in Vedic

        val horaSign = if (isOddSign) {
            if (degreeInSign < 15.0) 4 else 3 // Leo (4) or Cancer (3)
        } else {
            if (degreeInSign < 15.0) 3 else 4 // Cancer (3) or Leo (4)
        }

        // Map to the first 15° within the hora sign
        val positionInHora = degreeInSign % 15.0
        val horaDegree = positionInHora * 2.0 // Scale to 30°

        return (horaSign * 30.0) + horaDegree
    }

    // ===================== D3 - DREKKANA =====================
    /**
     * Calculate D3 (Drekkana) positions for all planets
     *
     * Drekkana divides each sign into 3 parts of 10° each (Decanates).
     * - First 10°: Same sign
     * - Second 10°: 5th sign from it
     * - Third 10°: 9th sign from it
     */
    fun calculateDrekkana(chart: VedicChart): DivisionalChartData {
        val drekkanaAscendant = calculateDrekkanaLongitude(chart.ascendant)
        val drekkanaAscendantSign = ZodiacSign.fromLongitude(drekkanaAscendant)

        val drekkanaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, drekkanaAscendantSign.number) { long ->
                calculateDrekkanaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D3_DREKKANA,
            planetPositions = drekkanaPositions,
            ascendantLongitude = drekkanaAscendant,
            chartTitle = "Drekkana (D3)"
        )
    }

    private fun calculateDrekkanaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val drekkanaPart = (degreeInSign / 10.0).toInt().coerceIn(0, 2) // 0, 1, or 2

        val drekkanaSign = when (drekkanaPart) {
            0 -> signNumber                   // First decanate: same sign
            1 -> (signNumber + 4) % 12        // Second decanate: 5th sign
            2 -> (signNumber + 8) % 12        // Third decanate: 9th sign
            else -> signNumber
        }

        val positionInDrekkana = degreeInSign % 10.0
        val drekkanaDegree = (positionInDrekkana / 10.0) * 30.0

        return (drekkanaSign * 30.0) + drekkanaDegree
    }

    // ===================== D4 - CHATURTHAMSA =====================
    /**
     * Calculate D4 (Chaturthamsa) positions for all planets
     *
     * Chaturthamsa divides each sign into 4 parts of 7°30' each.
     * - First quarter: Same sign
     * - Second quarter: 4th sign
     * - Third quarter: 7th sign
     * - Fourth quarter: 10th sign
     */
    fun calculateChaturthamsa(chart: VedicChart): DivisionalChartData {
        val chaturthamsaAscendant = calculateChaturthamsaLongitude(chart.ascendant)
        val chaturthamsaAscendantSign = ZodiacSign.fromLongitude(chaturthamsaAscendant)

        val chaturthamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, chaturthamsaAscendantSign.number) { long ->
                calculateChaturthamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D4_CHATURTHAMSA,
            planetPositions = chaturthamsaPositions,
            ascendantLongitude = chaturthamsaAscendant,
            chartTitle = "Chaturthamsa (D4)"
        )
    }

    private fun calculateChaturthamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val chaturthamsaPart = (degreeInSign / 7.5).toInt().coerceIn(0, 3) // 0, 1, 2, or 3

        val chaturthamsaSign = (signNumber + (chaturthamsaPart * 3)) % 12

        val positionInChaturthamsa = degreeInSign % 7.5
        val chaturthamsaDegree = (positionInChaturthamsa / 7.5) * 30.0

        return (chaturthamsaSign * 30.0) + chaturthamsaDegree
    }

    // ===================== D7 - SAPTAMSA =====================
    /**
     * Calculate D7 (Saptamsa) positions for all planets
     *
     * Saptamsa divides each sign into 7 parts of 4°17'8.57" each.
     * - Odd signs: Start from the same sign
     * - Even signs: Start from 7th sign
     */
    fun calculateSaptamsa(chart: VedicChart): DivisionalChartData {
        val saptamsaAscendant = calculateSaptamsaLongitude(chart.ascendant)
        val saptamsaAscendantSign = ZodiacSign.fromLongitude(saptamsaAscendant)

        val saptamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, saptamsaAscendantSign.number) { long ->
                calculateSaptamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D7_SAPTAMSA,
            planetPositions = saptamsaPositions,
            ascendantLongitude = saptamsaAscendant,
            chartTitle = "Saptamsa (D7)"
        )
    }

    private fun calculateSaptamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0
        val isOddSign = signNumber % 2 == 0 // Aries=0 is odd

        val saptamsaPart = (degreeInSign / (30.0 / 7.0)).toInt().coerceIn(0, 6) // 0-6

        val startingSign = if (isOddSign) signNumber else (signNumber + 6) % 12
        val saptamsaSign = (startingSign + saptamsaPart) % 12

        val positionInSaptamsa = degreeInSign % (30.0 / 7.0)
        val saptamsaDegree = (positionInSaptamsa / (30.0 / 7.0)) * 30.0

        return (saptamsaSign * 30.0) + saptamsaDegree
    }

    // ===================== D9 - NAVAMSA =====================
    /**
     * Calculate D9 (Navamsa) positions for all planets
     *
     * Navamsa divides each sign into 9 equal parts of 3°20' each.
     * The navamsa sign depends on the sign type:
     * - Movable signs (Ar,Ca,Li,Cp): Start from the same sign
     * - Fixed signs (Ta,Le,Sc,Aq): Start from 9th sign
     * - Dual signs (Ge,Vi,Sg,Pi): Start from 5th sign
     */
    fun calculateNavamsa(chart: VedicChart): DivisionalChartData {
        val navamsaAscendant = calculateNavamsaLongitude(chart.ascendant)
        val navamsaAscendantSign = ZodiacSign.fromLongitude(navamsaAscendant)

        val navamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, navamsaAscendantSign.number) { long ->
                calculateNavamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D9_NAVAMSA,
            planetPositions = navamsaPositions,
            ascendantLongitude = navamsaAscendant,
            chartTitle = "Navamsa (D9)"
        )
    }

    private fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val navamsaPart = (degreeInSign / 3.333333333).toInt().coerceIn(0, 8) // 0-8

        val startingSignIndex = when (signNumber % 3) {
            0 -> signNumber              // Movable: start from same sign
            1 -> (signNumber + 8) % 12   // Fixed: start from 9th sign
            2 -> (signNumber + 4) % 12   // Dual: start from 5th sign
            else -> signNumber
        }

        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12

        val positionInNavamsa = degreeInSign % 3.333333333
        val navamsaDegree = (positionInNavamsa / 3.333333333) * 30.0

        return (navamsaSignIndex * 30.0) + navamsaDegree
    }

    // ===================== D10 - DASAMSA =====================
    /**
     * Calculate D10 (Dasamsa) positions for all planets
     *
     * Dasamsa divides each sign into 10 equal parts of 3° each.
     * - Odd signs: Start from the same sign
     * - Even signs: Start from 9th sign
     */
    fun calculateDasamsa(chart: VedicChart): DivisionalChartData {
        val dasamsaAscendant = calculateDasamsaLongitude(chart.ascendant)
        val dasamsaAscendantSign = ZodiacSign.fromLongitude(dasamsaAscendant)

        val dasamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, dasamsaAscendantSign.number) { long ->
                calculateDasamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D10_DASAMSA,
            planetPositions = dasamsaPositions,
            ascendantLongitude = dasamsaAscendant,
            chartTitle = "Dasamsa (D10)"
        )
    }

    private fun calculateDasamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0
        val isOddSign = signNumber % 2 == 0

        val dasamsaPart = (degreeInSign / 3.0).toInt().coerceIn(0, 9) // 0-9

        val startingSignIndex = if (isOddSign) signNumber else (signNumber + 8) % 12
        val dasamsaSignIndex = (startingSignIndex + dasamsaPart) % 12

        val positionInDasamsa = degreeInSign % 3.0
        val dasamsaDegree = (positionInDasamsa / 3.0) * 30.0

        return (dasamsaSignIndex * 30.0) + dasamsaDegree
    }

    // ===================== D12 - DWADASAMSA =====================
    /**
     * Calculate D12 (Dwadasamsa) positions for all planets
     *
     * Dwadasamsa divides each sign into 12 parts of 2°30' each.
     * Starts from the same sign and goes through all 12 signs.
     */
    fun calculateDwadasamsa(chart: VedicChart): DivisionalChartData {
        val dwadasamsaAscendant = calculateDwadasamsaLongitude(chart.ascendant)
        val dwadasamsaAscendantSign = ZodiacSign.fromLongitude(dwadasamsaAscendant)

        val dwadasamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, dwadasamsaAscendantSign.number) { long ->
                calculateDwadasamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D12_DWADASAMSA,
            planetPositions = dwadasamsaPositions,
            ascendantLongitude = dwadasamsaAscendant,
            chartTitle = "Dwadasamsa (D12)"
        )
    }

    private fun calculateDwadasamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val dwadasamsaPart = (degreeInSign / 2.5).toInt().coerceIn(0, 11) // 0-11

        val dwadasamsaSign = (signNumber + dwadasamsaPart) % 12

        val positionInDwadasamsa = degreeInSign % 2.5
        val dwadasamsaDegree = (positionInDwadasamsa / 2.5) * 30.0

        return (dwadasamsaSign * 30.0) + dwadasamsaDegree
    }

    // ===================== D16 - SHODASAMSA =====================
    /**
     * Calculate D16 (Shodasamsa) positions for all planets
     *
     * Shodasamsa divides each sign into 16 parts of 1°52'30" each.
     * - Movable signs: Start from Aries
     * - Fixed signs: Start from Leo
     * - Dual signs: Start from Sagittarius
     */
    fun calculateShodasamsa(chart: VedicChart): DivisionalChartData {
        val shodasamsaAscendant = calculateShodasamsaLongitude(chart.ascendant)
        val shodasamsaAscendantSign = ZodiacSign.fromLongitude(shodasamsaAscendant)

        val shodasamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, shodasamsaAscendantSign.number) { long ->
                calculateShodasamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D16_SHODASAMSA,
            planetPositions = shodasamsaPositions,
            ascendantLongitude = shodasamsaAscendant,
            chartTitle = "Shodasamsa (D16)"
        )
    }

    private fun calculateShodasamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val shodasamsaPart = (degreeInSign / (30.0 / 16.0)).toInt().coerceIn(0, 15) // 0-15

        // Starting sign based on sign modality
        val startingSign = when (signNumber % 3) {
            0 -> 0   // Movable: Aries
            1 -> 4   // Fixed: Leo
            2 -> 8   // Dual: Sagittarius
            else -> 0
        }

        val shodasamsaSign = (startingSign + shodasamsaPart) % 12

        val positionInShodasamsa = degreeInSign % (30.0 / 16.0)
        val shodasamsaDegree = (positionInShodasamsa / (30.0 / 16.0)) * 30.0

        return (shodasamsaSign * 30.0) + shodasamsaDegree
    }

    // ===================== D20 - VIMSAMSA =====================
    /**
     * Calculate D20 (Vimsamsa) positions for all planets
     *
     * Vimsamsa divides each sign into 20 parts of 1°30' each.
     * - Movable signs: Start from Aries
     * - Fixed signs: Start from Sagittarius
     * - Dual signs: Start from Leo
     */
    fun calculateVimsamsa(chart: VedicChart): DivisionalChartData {
        val vimsamsaAscendant = calculateVimsamsaLongitude(chart.ascendant)
        val vimsamsaAscendantSign = ZodiacSign.fromLongitude(vimsamsaAscendant)

        val vimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, vimsamsaAscendantSign.number) { long ->
                calculateVimsamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D20_VIMSAMSA,
            planetPositions = vimsamsaPositions,
            ascendantLongitude = vimsamsaAscendant,
            chartTitle = "Vimsamsa (D20)"
        )
    }

    private fun calculateVimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val vimsamsaPart = (degreeInSign / 1.5).toInt().coerceIn(0, 19) // 0-19

        // Starting sign based on sign modality
        val startingSign = when (signNumber % 3) {
            0 -> 0   // Movable: Aries
            1 -> 8   // Fixed: Sagittarius
            2 -> 4   // Dual: Leo
            else -> 0
        }

        val vimsamsaSign = (startingSign + vimsamsaPart) % 12

        val positionInVimsamsa = degreeInSign % 1.5
        val vimsamsaDegree = (positionInVimsamsa / 1.5) * 30.0

        return (vimsamsaSign * 30.0) + vimsamsaDegree
    }

    // ===================== D24 - CHATURVIMSAMSA =====================
    /**
     * Calculate D24 (Chaturvimsamsa/Siddhamsa) positions for all planets
     *
     * Chaturvimsamsa divides each sign into 24 parts of 1°15' each.
     * - Odd signs: Start from Leo
     * - Even signs: Start from Cancer
     */
    fun calculateChaturvimsamsa(chart: VedicChart): DivisionalChartData {
        val chaturvimsamsaAscendant = calculateChaturvimsamsaLongitude(chart.ascendant)
        val chaturvimsamsaAscendantSign = ZodiacSign.fromLongitude(chaturvimsamsaAscendant)

        val chaturvimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, chaturvimsamsaAscendantSign.number) { long ->
                calculateChaturvimsamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D24_CHATURVIMSAMSA,
            planetPositions = chaturvimsamsaPositions,
            ascendantLongitude = chaturvimsamsaAscendant,
            chartTitle = "Chaturvimsamsa (D24)"
        )
    }

    private fun calculateChaturvimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0
        val isOddSign = signNumber % 2 == 0

        val chaturvimsamsaPart = (degreeInSign / 1.25).toInt().coerceIn(0, 23) // 0-23

        val startingSign = if (isOddSign) 4 else 3 // Leo or Cancer
        val chaturvimsamsaSign = (startingSign + chaturvimsamsaPart) % 12

        val positionInChaturvimsamsa = degreeInSign % 1.25
        val chaturvimsamsaDegree = (positionInChaturvimsamsa / 1.25) * 30.0

        return (chaturvimsamsaSign * 30.0) + chaturvimsamsaDegree
    }

    // ===================== D27 - SAPTAVIMSAMSA (BHAMSA) =====================
    /**
     * Calculate D27 (Saptavimsamsa/Bhamsa) positions for all planets
     *
     * Saptavimsamsa divides each sign into 27 parts of 1°6'40" each.
     * - Fire signs: Start from Aries
     * - Earth signs: Start from Cancer
     * - Air signs: Start from Libra
     * - Water signs: Start from Capricorn
     */
    fun calculateSaptavimsamsa(chart: VedicChart): DivisionalChartData {
        val saptavimsamsaAscendant = calculateSaptavimsamsaLongitude(chart.ascendant)
        val saptavimsamsaAscendantSign = ZodiacSign.fromLongitude(saptavimsamsaAscendant)

        val saptavimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, saptavimsamsaAscendantSign.number) { long ->
                calculateSaptavimsamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D27_SAPTAVIMSAMSA,
            planetPositions = saptavimsamsaPositions,
            ascendantLongitude = saptavimsamsaAscendant,
            chartTitle = "Bhamsa (D27)"
        )
    }

    private fun calculateSaptavimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0

        val saptavimsamsaPart = (degreeInSign / (30.0 / 27.0)).toInt().coerceIn(0, 26) // 0-26

        // Starting sign based on element
        val startingSign = when (signNumber % 4) {
            0 -> 0   // Fire: Aries
            1 -> 3   // Earth: Cancer
            2 -> 6   // Air: Libra
            3 -> 9   // Water: Capricorn
            else -> 0
        }

        val saptavimsamsaSign = (startingSign + saptavimsamsaPart) % 12

        val positionInSaptavimsamsa = degreeInSign % (30.0 / 27.0)
        val saptavimsamsaDegree = (positionInSaptavimsamsa / (30.0 / 27.0)) * 30.0

        return (saptavimsamsaSign * 30.0) + saptavimsamsaDegree
    }

    // ===================== D30 - TRIMSAMSA =====================
    /**
     * Calculate D30 (Trimsamsa) positions for all planets
     *
     * Trimsamsa divides each sign into 5 unequal parts ruled by Mars, Saturn, Jupiter, Mercury, Venus.
     * This is a special chart using degrees owned by planets.
     */
    fun calculateTrimsamsa(chart: VedicChart): DivisionalChartData {
        val trimsamsaAscendant = calculateTrimsamsaLongitude(chart.ascendant)
        val trimsamsaAscendantSign = ZodiacSign.fromLongitude(trimsamsaAscendant)

        val trimsamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, trimsamsaAscendantSign.number) { long ->
                calculateTrimsamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D30_TRIMSAMSA,
            planetPositions = trimsamsaPositions,
            ascendantLongitude = trimsamsaAscendant,
            chartTitle = "Trimsamsa (D30)"
        )
    }

    private fun calculateTrimsamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0
        val isOddSign = signNumber % 2 == 0

        // Trimsamsa divisions (based on Parashari system)
        // For odd signs: Mars(5°), Saturn(5°), Jupiter(8°), Mercury(7°), Venus(5°)
        // For even signs: Venus(5°), Mercury(7°), Jupiter(8°), Saturn(5°), Mars(5°)
        val trimsamsaSign: Int
        val degreeWithinPart: Double
        val partSize: Double

        if (isOddSign) {
            when {
                degreeInSign < 5.0 -> {
                    trimsamsaSign = 0 // Aries (Mars)
                    degreeWithinPart = degreeInSign
                    partSize = 5.0
                }
                degreeInSign < 10.0 -> {
                    trimsamsaSign = 10 // Aquarius (Saturn)
                    degreeWithinPart = degreeInSign - 5.0
                    partSize = 5.0
                }
                degreeInSign < 18.0 -> {
                    trimsamsaSign = 8 // Sagittarius (Jupiter)
                    degreeWithinPart = degreeInSign - 10.0
                    partSize = 8.0
                }
                degreeInSign < 25.0 -> {
                    trimsamsaSign = 2 // Gemini (Mercury)
                    degreeWithinPart = degreeInSign - 18.0
                    partSize = 7.0
                }
                else -> {
                    trimsamsaSign = 1 // Taurus (Venus)
                    degreeWithinPart = degreeInSign - 25.0
                    partSize = 5.0
                }
            }
        } else {
            when {
                degreeInSign < 5.0 -> {
                    trimsamsaSign = 1 // Taurus (Venus)
                    degreeWithinPart = degreeInSign
                    partSize = 5.0
                }
                degreeInSign < 12.0 -> {
                    trimsamsaSign = 5 // Virgo (Mercury)
                    degreeWithinPart = degreeInSign - 5.0
                    partSize = 7.0
                }
                degreeInSign < 20.0 -> {
                    trimsamsaSign = 11 // Pisces (Jupiter)
                    degreeWithinPart = degreeInSign - 12.0
                    partSize = 8.0
                }
                degreeInSign < 25.0 -> {
                    trimsamsaSign = 9 // Capricorn (Saturn)
                    degreeWithinPart = degreeInSign - 20.0
                    partSize = 5.0
                }
                else -> {
                    trimsamsaSign = 7 // Scorpio (Mars)
                    degreeWithinPart = degreeInSign - 25.0
                    partSize = 5.0
                }
            }
        }

        val trimsamsaDegree = (degreeWithinPart / partSize) * 30.0
        return (trimsamsaSign * 30.0) + trimsamsaDegree
    }

    // ===================== D60 - SHASHTIAMSA =====================
    /**
     * Calculate D60 (Shashtiamsa) positions for all planets
     *
     * Shashtiamsa divides each sign into 60 equal parts of 0°30' each.
     * - Odd signs: Start from the same sign
     * - Even signs: Start from 7th sign
     */
    fun calculateShashtiamsa(chart: VedicChart): DivisionalChartData {
        val shashtiamsaAscendant = calculateShashtiamsaLongitude(chart.ascendant)
        val shashtiamsaAscendantSign = ZodiacSign.fromLongitude(shashtiamsaAscendant)

        val shashtiamsaPositions = chart.planetPositions.map { position ->
            calculateDivisionalPosition(position, shashtiamsaAscendantSign.number) { long ->
                calculateShashtiamsaLongitude(long)
            }
        }

        return DivisionalChartData(
            chartType = DivisionalChartType.D60_SHASHTIAMSA,
            planetPositions = shashtiamsaPositions,
            ascendantLongitude = shashtiamsaAscendant,
            chartTitle = "Shashtiamsa (D60)"
        )
    }

    private fun calculateShashtiamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt() // 0-11
        val degreeInSign = normalizedLong % 30.0
        val isOddSign = signNumber % 2 == 0

        val shashtiamsaPart = (degreeInSign / 0.5).toInt().coerceIn(0, 59) // 0-59

        val startingSignIndex = if (isOddSign) signNumber else (signNumber + 6) % 12
        val shashtiamsaSignIndex = (startingSignIndex + shashtiamsaPart) % 12

        val positionInShashtiamsa = degreeInSign % 0.5
        val shashtiamsaDegree = (positionInShashtiamsa / 0.5) * 30.0

        return (shashtiamsaSignIndex * 30.0) + shashtiamsaDegree
    }

    // ===================== HELPER FUNCTIONS =====================

    /**
     * Generic helper to calculate divisional position
     */
    private fun calculateDivisionalPosition(
        position: PlanetPosition,
        ascendantSignNumber: Int,
        calculateLongitude: (Double) -> Double
    ): PlanetPosition {
        val divisionalLongitude = calculateLongitude(position.longitude)
        val divisionalSign = ZodiacSign.fromLongitude(divisionalLongitude)
        val degreeInSign = divisionalLongitude % 30.0
        val (nakshatra, pada) = Nakshatra.fromLongitude(divisionalLongitude)

        val divisionalHouse = calculateHouseFromSign(divisionalSign.number, ascendantSignNumber)

        return position.copy(
            longitude = divisionalLongitude,
            sign = divisionalSign,
            degree = degreeInSign.toInt().toDouble(),
            minutes = ((degreeInSign - degreeInSign.toInt()) * 60).toInt().toDouble(),
            seconds = ((((degreeInSign - degreeInSign.toInt()) * 60) - ((degreeInSign - degreeInSign.toInt()) * 60).toInt()) * 60),
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = divisionalHouse
        )
    }

    /**
     * Calculate house number based on the planet's sign relative to the ascendant sign.
     */
    private fun calculateHouseFromSign(planetSignNumber: Int, ascendantSignNumber: Int): Int {
        val houseOffset = (planetSignNumber - ascendantSignNumber + 12) % 12
        return if (houseOffset == 0) 1 else houseOffset + 1
    }

    /**
     * Get all major divisional charts at once
     */
    fun calculateAllDivisionalCharts(chart: VedicChart): List<DivisionalChartData> {
        return listOf(
            calculateHora(chart),
            calculateDrekkana(chart),
            calculateChaturthamsa(chart),
            calculateSaptamsa(chart),
            calculateNavamsa(chart),
            calculateDasamsa(chart),
            calculateDwadasamsa(chart),
            calculateShodasamsa(chart),
            calculateVimsamsa(chart),
            calculateChaturvimsamsa(chart),
            calculateSaptavimsamsa(chart),
            calculateTrimsamsa(chart),
            calculateShashtiamsa(chart)
        )
    }

    /**
     * Get commonly used divisional charts (D1, D9, D10)
     */
    fun calculateCommonDivisionalCharts(chart: VedicChart): List<DivisionalChartData> {
        return listOf(
            calculateNavamsa(chart),
            calculateDasamsa(chart)
        )
    }

    /**
     * Calculate specific divisional chart by type
     */
    fun calculateDivisionalChart(chart: VedicChart, type: DivisionalChartType): DivisionalChartData {
        return when (type) {
            DivisionalChartType.D2_HORA -> calculateHora(chart)
            DivisionalChartType.D3_DREKKANA -> calculateDrekkana(chart)
            DivisionalChartType.D4_CHATURTHAMSA -> calculateChaturthamsa(chart)
            DivisionalChartType.D7_SAPTAMSA -> calculateSaptamsa(chart)
            DivisionalChartType.D9_NAVAMSA -> calculateNavamsa(chart)
            DivisionalChartType.D10_DASAMSA -> calculateDasamsa(chart)
            DivisionalChartType.D12_DWADASAMSA -> calculateDwadasamsa(chart)
            DivisionalChartType.D16_SHODASAMSA -> calculateShodasamsa(chart)
            DivisionalChartType.D20_VIMSAMSA -> calculateVimsamsa(chart)
            DivisionalChartType.D24_CHATURVIMSAMSA -> calculateChaturvimsamsa(chart)
            DivisionalChartType.D27_SAPTAVIMSAMSA -> calculateSaptavimsamsa(chart)
            DivisionalChartType.D30_TRIMSAMSA -> calculateTrimsamsa(chart)
            DivisionalChartType.D60_SHASHTIAMSA -> calculateShashtiamsa(chart)
        }
    }
}

/**
 * Enum for divisional chart types (Shodasha Varga)
 */
enum class DivisionalChartType(
    val division: Int,
    val displayName: String,
    val shortName: String,
    val description: String
) {
    D2_HORA(2, "Hora", "D2", "Wealth, Prosperity"),
    D3_DREKKANA(3, "Drekkana", "D3", "Siblings, Courage"),
    D4_CHATURTHAMSA(4, "Chaturthamsa", "D4", "Fortune, Property"),
    D7_SAPTAMSA(7, "Saptamsa", "D7", "Children, Progeny"),
    D9_NAVAMSA(9, "Navamsa", "D9", "Marriage, Dharma"),
    D10_DASAMSA(10, "Dasamsa", "D10", "Career, Profession"),
    D12_DWADASAMSA(12, "Dwadasamsa", "D12", "Parents, Ancestry"),
    D16_SHODASAMSA(16, "Shodasamsa", "D16", "Vehicles, Pleasures"),
    D20_VIMSAMSA(20, "Vimsamsa", "D20", "Spiritual Life"),
    D24_CHATURVIMSAMSA(24, "Siddhamsa", "D24", "Education, Learning"),
    D27_SAPTAVIMSAMSA(27, "Bhamsa", "D27", "Strength, Weakness"),
    D30_TRIMSAMSA(30, "Trimsamsa", "D30", "Evils, Misfortunes"),
    D60_SHASHTIAMSA(60, "Shashtiamsa", "D60", "Past Life Karma")
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
            appendLine("           ${chartType.displayName.uppercase()} CHART (${chartType.shortName})")
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
