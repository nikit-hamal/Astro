package com.astro.storm.data.model

/**
 * Vedic zodiac signs (Rashis)
 */
enum class ZodiacSign(
    val number: Int,
    val displayName: String,
    val abbreviation: String,
    val element: String,
    val ruler: Planet,
    val quality: Quality
) {
    ARIES(1, "Aries", "Ar", "Fire", Planet.MARS, Quality.CARDINAL),
    TAURUS(2, "Taurus", "Ta", "Earth", Planet.VENUS, Quality.FIXED),
    GEMINI(3, "Gemini", "Ge", "Air", Planet.MERCURY, Quality.MUTABLE),
    CANCER(4, "Cancer", "Ca", "Water", Planet.MOON, Quality.CARDINAL),
    LEO(5, "Leo", "Le", "Fire", Planet.SUN, Quality.FIXED),
    VIRGO(6, "Virgo", "Vi", "Earth", Planet.MERCURY, Quality.MUTABLE),
    LIBRA(7, "Libra", "Li", "Air", Planet.VENUS, Quality.CARDINAL),
    SCORPIO(8, "Scorpio", "Sc", "Water", Planet.MARS, Quality.FIXED),
    SAGITTARIUS(9, "Sagittarius", "Sg", "Fire", Planet.JUPITER, Quality.MUTABLE),
    CAPRICORN(10, "Capricorn", "Cp", "Earth", Planet.SATURN, Quality.CARDINAL),
    AQUARIUS(11, "Aquarius", "Aq", "Air", Planet.SATURN, Quality.FIXED),
    PISCES(12, "Pisces", "Pi", "Water", Planet.JUPITER, Quality.MUTABLE);

    enum class Quality {
        CARDINAL, FIXED, MUTABLE
    }

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
