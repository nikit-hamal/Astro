package com.astro.storm.data.model

import java.time.LocalDateTime

/**
 * Tithi - Lunar day (30 tithis in a lunar month)
 */
enum class Tithi(val number: Int, val displayName: String, val deity: String) {
    // Krishna Paksha (Waning Moon) - 1-15
    PRATIPADA_K(1, "Pratipada (Krishna)", "Brahma"),
    DWITIYA_K(2, "Dwitiya (Krishna)", "Vidhata"),
    TRITIYA_K(3, "Tritiya (Krishna)", "Gauri"),
    CHATURTHI_K(4, "Chaturthi (Krishna)", "Yama"),
    PANCHAMI_K(5, "Panchami (Krishna)", "Naga"),
    SHASHTHI_K(6, "Shashthi (Krishna)", "Karttikeya"),
    SAPTAMI_K(7, "Saptami (Krishna)", "Surya"),
    ASHTAMI_K(8, "Ashtami (Krishna)", "Shiva"),
    NAVAMI_K(9, "Navami (Krishna)", "Durga"),
    DASHAMI_K(10, "Dashami (Krishna)", "Yama"),
    EKADASHI_K(11, "Ekadashi (Krishna)", "Vishnu"),
    DWADASHI_K(12, "Dwadashi (Krishna)", "Vishnu"),
    TRAYODASHI_K(13, "Trayodashi (Krishna)", "Kamadeva"),
    CHATURDASHI_K(14, "Chaturdashi (Krishna)", "Shiva"),
    AMAVASYA(15, "Amavasya (New Moon)", "Pitris"),

    // Shukla Paksha (Waxing Moon) - 16-30
    PRATIPADA_S(16, "Pratipada (Shukla)", "Agni"),
    DWITIYA_S(17, "Dwitiya (Shukla)", "Brahma"),
    TRITIYA_S(18, "Tritiya (Shukla)", "Gauri"),
    CHATURTHI_S(19, "Chaturthi (Shukla)", "Ganesha"),
    PANCHAMI_S(20, "Panchami (Shukla)", "Naga"),
    SHASHTHI_S(21, "Shashthi (Shukla)", "Karttikeya"),
    SAPTAMI_S(22, "Saptami (Shukla)", "Surya"),
    ASHTAMI_S(23, "Ashtami (Shukla)", "Shiva"),
    NAVAMI_S(24, "Navami (Shukla)", "Durga"),
    DASHAMI_S(25, "Dashami (Shukla)", "Dharma"),
    EKADASHI_S(26, "Ekadashi (Shukla)", "Vishnu"),
    DWADASHI_S(27, "Dwadashi (Shukla)", "Vishnu"),
    TRAYODASHI_S(28, "Trayodashi (Shukla)", "Kamadeva"),
    CHATURDASHI_S(29, "Chaturdashi (Shukla)", "Shiva"),
    PURNIMA(30, "Purnima (Full Moon)", "Chandra");

    companion object {
        fun fromLunarDayNumber(dayNumber: Int): Tithi {
            return values().find { it.number == dayNumber }
                ?: PRATIPADA_S
        }
    }
}

/**
 * Yoga - Combination of Sun and Moon (27 yogas)
 */
enum class Yoga(val number: Int, val displayName: String, val nature: String) {
    VISHKAMBHA(1, "Vishkambha", "Mixed"),
    PRITI(2, "Priti", "Auspicious"),
    AYUSHMAN(3, "Ayushman", "Auspicious"),
    SAUBHAGYA(4, "Saubhagya", "Auspicious"),
    SHOBHANA(5, "Shobhana", "Auspicious"),
    ATIGANDA(6, "Atiganda", "Inauspicious"),
    SUKARMAN(7, "Sukarman", "Auspicious"),
    DHRITI(8, "Dhriti", "Auspicious"),
    SHULA(9, "Shula", "Inauspicious"),
    GANDA(10, "Ganda", "Inauspicious"),
    VRIDDHI(11, "Vriddhi", "Auspicious"),
    DHRUVA(12, "Dhruva", "Auspicious"),
    VYAGHATA(13, "Vyaghata", "Inauspicious"),
    HARSHANA(14, "Harshana", "Auspicious"),
    VAJRA(15, "Vajra", "Inauspicious"),
    SIDDHI(16, "Siddhi", "Auspicious"),
    VYATIPATA(17, "Vyatipata", "Inauspicious"),
    VARIYAN(18, "Variyan", "Auspicious"),
    PARIGHA(19, "Parigha", "Inauspicious"),
    SHIVA(20, "Shiva", "Auspicious"),
    SIDDHA(21, "Siddha", "Auspicious"),
    SADHYA(22, "Sadhya", "Auspicious"),
    SHUBHA(23, "Shubha", "Auspicious"),
    SHUKLA(24, "Shukla", "Auspicious"),
    BRAHMA(25, "Brahma", "Auspicious"),
    INDRA(26, "Indra", "Auspicious"),
    VAIDHRITI(27, "Vaidhriti", "Inauspicious");

    companion object {
        fun fromNumber(number: Int): Yoga {
            return values().find { it.number == number }
                ?: VISHKAMBHA
        }

        /**
         * Calculate Yoga from Sun and Moon longitudes
         * Yoga = (Sun longitude + Moon longitude) / 13.333333
         */
        fun calculate(sunLongitude: Double, moonLongitude: Double): Yoga {
            val sum = (sunLongitude + moonLongitude) % 360.0
            val yogaNumber = (sum / 13.333333).toInt() + 1
            return fromNumber(if (yogaNumber > 27) 1 else yogaNumber)
        }
    }
}

/**
 * Karana - Half of a Tithi (60 karanas in a lunar month, 11 types)
 */
enum class Karana(val number: Int, val displayName: String, val nature: String, val isFixed: Boolean = false) {
    // Movable Karanas (Chara) - Repeat 8 times
    BAVA(1, "Bava", "Movable"),
    BALAVA(2, "Balava", "Movable"),
    KAULAVA(3, "Kaulava", "Movable"),
    TAITILA(4, "Taitila", "Movable"),
    GARAJA(5, "Garaja", "Movable"),
    VANIJA(6, "Vanija", "Movable"),
    VISHTI(7, "Vishti (Bhadra)", "Movable"),

    // Fixed Karanas (Sthira) - Occur once
    SHAKUNI(8, "Shakuni", "Fixed", true),
    CHATUSHPADA(9, "Chatushpada", "Fixed", true),
    NAGA(10, "Naga", "Fixed", true),
    KIMSTUGHNA(11, "Kimstughna", "Fixed", true);

    companion object {
        fun fromNumber(number: Int): Karana {
            return values().find { it.number == number }
                ?: BAVA
        }
    }
}

/**
 * Vara - Day of the week with planetary rulers
 */
enum class Vara(
    val dayOfWeek: Int,
    val displayName: String,
    val planetRuler: Planet,
    val color: String,
    val deity: String
) {
    SUNDAY(1, "Sunday (Ravivara)", Planet.SUN, "Red", "Surya"),
    MONDAY(2, "Monday (Somvara)", Planet.MOON, "White", "Chandra"),
    TUESDAY(3, "Tuesday (Mangalavara)", Planet.MARS, "Red", "Mangala"),
    WEDNESDAY(4, "Wednesday (Budhavara)", Planet.MERCURY, "Green", "Budha"),
    THURSDAY(5, "Thursday (Guruvara)", Planet.JUPITER, "Yellow", "Guru"),
    FRIDAY(6, "Friday (Shukravara)", Planet.VENUS, "White", "Shukra"),
    SATURDAY(7, "Saturday (Shanivara)", Planet.SATURN, "Black", "Shani");

    companion object {
        fun fromDayOfWeek(dayOfWeek: Int): Vara {
            return values().find { it.dayOfWeek == dayOfWeek }
                ?: SUNDAY
        }
    }
}

/**
 * Complete Panchanga Data
 * Five limbs of time in Vedic astrology
 */
data class Panchanga(
    val dateTime: LocalDateTime,
    val tithi: Tithi,
    val tithiEndTime: LocalDateTime?,
    val vara: Vara,
    val nakshatra: Nakshatra,
    val nakshatraEndTime: LocalDateTime?,
    val yoga: Yoga,
    val yogaEndTime: LocalDateTime?,
    val karana: Karana,
    val karanaEndTime: LocalDateTime?,
    val sunriseTime: LocalDateTime,
    val sunsetTime: LocalDateTime,
    val moonrise: LocalDateTime?,
    val moonset: LocalDateTime?,
    val sunLongitude: Double,
    val moonLongitude: Double,
    val lunarPhase: Double // 0-1 (0 = New Moon, 0.5 = Full Moon)
) {
    /**
     * Get formatted Panchanga as string
     */
    fun toFormattedString(): String {
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("            PANCHANGA")
            appendLine("═══════════════════════════════════════")
            appendLine()
            appendLine("Date & Time: ${dateTime.toLocalDate()}")
            appendLine()
            appendLine("1. TITHI (Lunar Day):")
            appendLine("   ${tithi.displayName}")
            appendLine("   Deity: ${tithi.deity}")
            if (tithiEndTime != null) {
                appendLine("   Ends: ${tithiEndTime.toLocalTime()}")
            }
            appendLine()
            appendLine("2. VARA (Weekday):")
            appendLine("   ${vara.displayName}")
            appendLine("   Ruler: ${vara.planetRuler.displayName}")
            appendLine("   Deity: ${vara.deity}")
            appendLine()
            appendLine("3. NAKSHATRA (Lunar Mansion):")
            appendLine("   ${nakshatra.displayName}")
            appendLine("   Ruler: ${nakshatra.ruler.displayName}")
            appendLine("   Deity: ${nakshatra.deity}")
            if (nakshatraEndTime != null) {
                appendLine("   Ends: ${nakshatraEndTime.toLocalTime()}")
            }
            appendLine()
            appendLine("4. YOGA (Sun-Moon Combination):")
            appendLine("   ${yoga.displayName}")
            appendLine("   Nature: ${yoga.nature}")
            if (yogaEndTime != null) {
                appendLine("   Ends: ${yogaEndTime.toLocalTime()}")
            }
            appendLine()
            appendLine("5. KARANA (Half Tithi):")
            appendLine("   ${karana.displayName}")
            appendLine("   Nature: ${karana.nature}")
            if (karanaEndTime != null) {
                appendLine("   Ends: ${karanaEndTime.toLocalTime()}")
            }
            appendLine()
            appendLine("CELESTIAL TIMES:")
            appendLine("─────────────────────────────────────")
            appendLine("Sunrise: ${sunriseTime.toLocalTime()}")
            appendLine("Sunset:  ${sunsetTime.toLocalTime()}")
            if (moonrise != null) {
                appendLine("Moonrise: ${moonrise.toLocalTime()}")
            }
            if (moonset != null) {
                appendLine("Moonset:  ${moonset.toLocalTime()}")
            }
            appendLine()
            appendLine("PLANETARY POSITIONS:")
            appendLine("Sun:  ${formatDegree(sunLongitude)}")
            appendLine("Moon: ${formatDegree(moonLongitude)}")
            appendLine("Lunar Phase: ${(lunarPhase * 100).toInt()}% illuminated")
        }
    }

    private fun formatDegree(degree: Double): String {
        val normalizedDegree = (degree % 360.0 + 360.0) % 360.0
        val deg = normalizedDegree.toInt()
        val min = ((normalizedDegree - deg) * 60).toInt()
        return "$deg° $min'"
    }

    /**
     * Check if the current moment is auspicious
     */
    fun isAuspicious(): Boolean {
        return yoga.nature == "Auspicious" &&
                tithi !in listOf(Tithi.AMAVASYA, Tithi.ASHTAMI_K, Tithi.CHATURDASHI_K) &&
                karana != Karana.VISHTI
    }

    /**
     * Get auspiciousness score (0-100)
     */
    fun getAuspiciousnessScore(): Int {
        var score = 50 // Base score

        // Yoga contribution (30%)
        score += when (yoga.nature) {
            "Auspicious" -> 30
            "Inauspicious" -> -30
            else -> 0
        }

        // Tithi contribution (20%)
        score += when {
            tithi == Tithi.PURNIMA -> 20
            tithi == Tithi.AMAVASYA -> -20
            tithi in listOf(
                Tithi.PANCHAMI_S,
                Tithi.ASHTAMI_S,
                Tithi.EKADASHI_S,
                Tithi.TRAYODASHI_S
            ) -> 15
            else -> 0
        }

        // Karana contribution (10%)
        score += if (karana == Karana.VISHTI) -10 else 5

        // Vara contribution (10%)
        score += when (vara) {
            Vara.THURSDAY, Vara.FRIDAY -> 10
            Vara.TUESDAY, Vara.SATURDAY -> -10
            else -> 5
        }

        return score.coerceIn(0, 100)
    }
}
