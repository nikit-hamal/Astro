package com.astro.storm.data.model

/**
 * Vedic zodiac signs (Rashis)
 */
enum class ZodiacSign(
    val number: Int,
    val displayName: String,
    val abbreviation: String,
    val element: String,
    val ruler: Planet
) {
    ARIES(1, "Aries", "Ar", "Fire", Planet.MARS),
    TAURUS(2, "Taurus", "Ta", "Earth", Planet.VENUS),
    GEMINI(3, "Gemini", "Ge", "Air", Planet.MERCURY),
    CANCER(4, "Cancer", "Ca", "Water", Planet.MOON),
    LEO(5, "Leo", "Le", "Fire", Planet.SUN),
    VIRGO(6, "Virgo", "Vi", "Earth", Planet.MERCURY),
    LIBRA(7, "Libra", "Li", "Air", Planet.VENUS),
    SCORPIO(8, "Scorpio", "Sc", "Water", Planet.MARS),
    SAGITTARIUS(9, "Sagittarius", "Sg", "Fire", Planet.JUPITER),
    CAPRICORN(10, "Capricorn", "Cp", "Earth", Planet.SATURN),
    AQUARIUS(11, "Aquarius", "Aq", "Air", Planet.SATURN),
    PISCES(12, "Pisces", "Pi", "Water", Planet.JUPITER);

    val startDegree: Double get() = (number - 1) * ZODIAC_SIGN_SPAN
    val endDegree: Double get() = number * ZODIAC_SIGN_SPAN

    companion object {
        private const val ZODIAC_SIGN_SPAN = 30.0

        fun fromLongitude(longitude: Double): ZodiacSign {
            val normalizedLongitude = (longitude % 360.0 + 360.0) % 360.0
            val signIndex = (normalizedLongitude / ZODIAC_SIGN_SPAN).toInt()
            return values()[signIndex.coerceIn(0, 11)]
        }
    }
}
