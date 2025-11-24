package com.astro.storm.data.model

/**
 * 27 Nakshatras in Vedic astrology
 */
enum class Nakshatra(
    val number: Int,
    val displayName: String,
    val startDegree: Double,
    val endDegree: Double,
    val ruler: Planet,
    val deity: String,
    val pada1Sign: ZodiacSign,
    val pada2Sign: ZodiacSign,
    val pada3Sign: ZodiacSign,
    val pada4Sign: ZodiacSign
) {
    ASHWINI(1, "Ashwini", 0.0, 13.333333, Planet.KETU, "Ashwini Kumaras",
        ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES),
    BHARANI(2, "Bharani", 13.333333, 26.666667, Planet.VENUS, "Yama",
        ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES, ZodiacSign.ARIES),
    KRITTIKA(3, "Krittika", 26.666667, 40.0, Planet.SUN, "Agni",
        ZodiacSign.ARIES, ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.TAURUS),
    ROHINI(4, "Rohini", 40.0, 53.333333, Planet.MOON, "Brahma",
        ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.TAURUS),
    MRIGASHIRA(5, "Mrigashira", 53.333333, 66.666667, Planet.MARS, "Soma",
        ZodiacSign.TAURUS, ZodiacSign.TAURUS, ZodiacSign.GEMINI, ZodiacSign.GEMINI),
    ARDRA(6, "Ardra", 66.666667, 80.0, Planet.RAHU, "Rudra",
        ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.GEMINI),
    PUNARVASU(7, "Punarvasu", 80.0, 93.333333, Planet.JUPITER, "Aditi",
        ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.GEMINI, ZodiacSign.CANCER),
    PUSHYA(8, "Pushya", 93.333333, 106.666667, Planet.SATURN, "Brihaspati",
        ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER),
    ASHLESHA(9, "Ashlesha", 106.666667, 120.0, Planet.MERCURY, "Sarpa",
        ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER, ZodiacSign.CANCER),
    MAGHA(10, "Magha", 120.0, 133.333333, Planet.KETU, "Pitris",
        ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO),
    PURVA_PHALGUNI(11, "Purva Phalguni", 133.333333, 146.666667, Planet.VENUS, "Bhaga",
        ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO, ZodiacSign.LEO),
    UTTARA_PHALGUNI(12, "Uttara Phalguni", 146.666667, 160.0, Planet.SUN, "Aryaman",
        ZodiacSign.LEO, ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.VIRGO),
    HASTA(13, "Hasta", 160.0, 173.333333, Planet.MOON, "Savitar",
        ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.VIRGO),
    CHITRA(14, "Chitra", 173.333333, 186.666667, Planet.MARS, "Tvashtar",
        ZodiacSign.VIRGO, ZodiacSign.VIRGO, ZodiacSign.LIBRA, ZodiacSign.LIBRA),
    SWATI(15, "Swati", 186.666667, 200.0, Planet.RAHU, "Vayu",
        ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.LIBRA),
    VISHAKHA(16, "Vishakha", 200.0, 213.333333, Planet.JUPITER, "Indra-Agni",
        ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.LIBRA, ZodiacSign.SCORPIO),
    ANURADHA(17, "Anuradha", 213.333333, 226.666667, Planet.SATURN, "Mitra",
        ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO),
    JYESHTHA(18, "Jyeshtha", 226.666667, 240.0, Planet.MERCURY, "Indra",
        ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO, ZodiacSign.SCORPIO),
    MULA(19, "Mula", 240.0, 253.333333, Planet.KETU, "Nirriti",
        ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS),
    PURVA_ASHADHA(20, "Purva Ashadha", 253.333333, 266.666667, Planet.VENUS, "Apas",
        ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS, ZodiacSign.SAGITTARIUS),
    UTTARA_ASHADHA(21, "Uttara Ashadha", 266.666667, 280.0, Planet.SUN, "Vishwadevas",
        ZodiacSign.SAGITTARIUS, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN),
    SHRAVANA(22, "Shravana", 280.0, 293.333333, Planet.MOON, "Vishnu",
        ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN),
    DHANISHTHA(23, "Dhanishtha", 293.333333, 306.666667, Planet.MARS, "Vasus",
        ZodiacSign.CAPRICORN, ZodiacSign.CAPRICORN, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS),
    SHATABHISHA(24, "Shatabhisha", 306.666667, 320.0, Planet.RAHU, "Varuna",
        ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS),
    PURVA_BHADRAPADA(25, "Purva Bhadrapada", 320.0, 333.333333, Planet.JUPITER, "Aja Ekapada",
        ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.AQUARIUS, ZodiacSign.PISCES),
    UTTARA_BHADRAPADA(26, "Uttara Bhadrapada", 333.333333, 346.666667, Planet.SATURN, "Ahir Budhnya",
        ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES),
    REVATI(27, "Revati", 346.666667, 360.0, Planet.MERCURY, "Pushan",
        ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES, ZodiacSign.PISCES);

    companion object {
        fun fromLongitude(longitude: Double): Pair<Nakshatra, Int> {
            val normalizedLongitude = (longitude % 360.0 + 360.0) % 360.0
            val nakshatra = values().first {
                normalizedLongitude >= it.startDegree && normalizedLongitude < it.endDegree
            }
            // Calculate pada (1-4)
            val nakshatraSpan = 13.333333
            val positionInNakshatra = normalizedLongitude - nakshatra.startDegree
            val pada = ((positionInNakshatra / nakshatraSpan) * 4).toInt() + 1
            return nakshatra to pada.coerceIn(1, 4)
        }
    }
}
