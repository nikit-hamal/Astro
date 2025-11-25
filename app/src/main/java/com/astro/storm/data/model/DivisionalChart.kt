package com.astro.storm.data.model

/**
 * Divisional Chart Types (Vargas) for Vedic Astrology
 * Each divisional chart focuses on different life areas
 */
enum class DivisionalChartType(
    val division: Int,
    val abbreviation: String,
    val displayName: String,
    val description: String
) {
    // Main Birth Chart (Rashi/Lagna)
    D1(1, "D-1", "Rashi Chart (D-1)", "Main birth chart showing overall life"),

    // Hora Chart - Wealth
    D2(2, "D-2", "Hora Chart (D-2)", "Wealth and financial matters"),

    // Drekkana Chart - Siblings
    D3(3, "D-3", "Drekkana (D-3)", "Siblings, courage, and efforts"),

    // Chaturthamsa - Property
    D4(4, "D-4", "Chaturthamsa (D-4)", "Property and fixed assets"),

    // Panchamsa - Fame
    D5(5, "D-5", "Panchamsa (D-5)", "Fame and authority"),

    // Shashthamsa - Health
    D6(6, "D-6", "Shashthamsa (D-6)", "Health and disease"),

    // Saptamsa - Children
    D7(7, "D-7", "Saptamsa (D-7)", "Children and creativity"),

    // Ashtamsa - Longevity
    D8(8, "D-8", "Ashtamsa (D-8)", "Longevity and sudden events"),

    // Navamsa - Spouse and Dharma (MOST IMPORTANT)
    D9(9, "D-9", "Navamsa Chart (D-9)", "Spouse, dharma, and inner strength"),

    // Dasamsa - Career and Status (IMPORTANT)
    D10(10, "D-10", "Dasamsa Chart (D-10)", "Career, profession, and status"),

    // Rudramsa - Suffering
    D11(11, "D-11", "Rudramsa (D-11)", "Sufferings and challenges"),

    // Dwadasamsa - Parents
    D12(12, "D-12", "Dwadasamsa (D-12)", "Parents and ancestry"),

    // Shodasamsa - Vehicles
    D16(16, "D-16", "Shodasamsa (D-16)", "Vehicles and comforts"),

    // Vimsamsa - Spiritual Progress
    D20(20, "D-20", "Vimsamsa (D-20)", "Spiritual progress"),

    // Chaturvimsamsa - Education
    D24(24, "D-24", "Chaturvimsamsa (D-24)", "Education and learning"),

    // Bhamsa - Strength and Weakness
    D27(27, "D-27", "Bhamsa (D-27)", "Strengths and weaknesses"),

    // Trimsamsa - Evils
    D30(30, "D-30", "Trimsamsa (D-30)", "Misfortunes and evils"),

    // Khavedamsa - Auspicious/Inauspicious Effects
    D40(40, "D-40", "Khavedamsa (D-40)", "Auspicious and inauspicious effects"),

    // Akshavedamsa - Overall Well-being
    D45(45, "D-45", "Akshavedamsa (D-45)", "Overall well-being"),

    // Shastiamsa - Detailed Analysis (IMPORTANT)
    D60(60, "D-60", "Shastiamsa Chart (D-60)", "Karmic analysis and past life");

    companion object {
        /**
         * Get the most commonly used divisional charts
         */
        fun getCommonCharts(): List<DivisionalChartType> {
            return listOf(D1, D9, D10, D60)
        }

        /**
         * Get all standard divisional charts
         */
        fun getAllCharts(): List<DivisionalChartType> {
            return values().toList()
        }
    }
}

/**
 * Divisional Chart Data
 * Contains planetary positions for a specific divisional chart
 */
data class DivisionalChart(
    val chartType: DivisionalChartType,
    val ascendant: Double,
    val planetPositions: List<DivisionalPlanetPosition>
) {
    /**
     * Get planet position by planet
     */
    fun getPlanetPosition(planet: Planet): DivisionalPlanetPosition? {
        return planetPositions.find { it.planet == planet }
    }

    /**
     * Get all planets in a specific sign
     */
    fun getPlanetsInSign(sign: ZodiacSign): List<DivisionalPlanetPosition> {
        return planetPositions.filter { it.sign == sign }
    }

    /**
     * Get all planets in a specific house
     */
    fun getPlanetsInHouse(houseNumber: Int): List<DivisionalPlanetPosition> {
        return planetPositions.filter { it.house == houseNumber }
    }
}

/**
 * Planet position in a divisional chart
 */
data class DivisionalPlanetPosition(
    val planet: Planet,
    val longitude: Double,
    val sign: ZodiacSign,
    val degree: Int,
    val minutes: Int,
    val seconds: Int,
    val house: Int,
    val nakshatra: Nakshatra,
    val nakshatraPada: Int,
    val isRetrograde: Boolean
) {
    /**
     * Format position as string
     */
    fun toFormattedString(): String {
        val retrogradeMarker = if (isRetrograde) " (R)" else ""
        return "${planet.symbol} ${sign.abbreviation} $degree° $minutes' $seconds\"$retrogradeMarker"
    }
}
