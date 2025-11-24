package com.astro.storm.data.model

/**
 * Vedic zodiac signs (Rashis)
 */
enum class ZodiacSign(
    val number: Int,
    val displayName: String,
    val abbreviation: String,
    val startDegree: Double,
    val endDegree: Double,
    val element: String,
    val ruler: Planet
) {
    ARIES(1, "Aries", "Ar", 0.0, 30.0, "Fire", Planet.MARS),
    TAURUS(2, "Taurus", "Ta", 30.0, 60.0, "Earth", Planet.VENUS),
    GEMINI(3, "Gemini", "Ge", 60.0, 90.0, "Air", Planet.MERCURY),
    CANCER(4, "Cancer", "Ca", 90.0, 120.0, "Water", Planet.MOON),
    LEO(5, "Leo", "Le", 120.0, 150.0, "Fire", Planet.SUN),
    VIRGO(6, "Virgo", "Vi", 150.0, 180.0, "Earth", Planet.MERCURY),
    LIBRA(7, "Libra", "Li", 180.0, 210.0, "Air", Planet.VENUS),
    SCORPIO(8, "Scorpio", "Sc", 210.0, 240.0, "Water", Planet.MARS),
    SAGITTARIUS(9, "Sagittarius", "Sg", 240.0, 270.0, "Fire", Planet.JUPITER),
    CAPRICORN(10, "Capricorn", "Cp", 270.0, 300.0, "Earth", Planet.SATURN),
    AQUARIUS(11, "Aquarius", "Aq", 300.0, 330.0, "Air", Planet.SATURN),
    PISCES(12, "Pisces", "Pi", 330.0, 360.0, "Water", Planet.JUPITER);

    companion object {
        fun fromLongitude(longitude: Double): ZodiacSign {
            val normalizedLongitude = (longitude % 360.0 + 360.0) % 360.0
            return values().first {
                normalizedLongitude >= it.startDegree && normalizedLongitude < it.endDegree
            }
        }
    }
}
