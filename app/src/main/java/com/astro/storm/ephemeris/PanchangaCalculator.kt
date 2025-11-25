package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.*
import swisseph.DblObj
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.floor

/**
 * High-precision Panchanga (Five-limb Vedic almanac) Calculator
 *
 * Calculates the five elements of Panchanga:
 * 1. Tithi - Lunar day (based on Moon-Sun angular distance)
 * 2. Nakshatra - Moon's asterism position
 * 3. Yoga - Luni-solar combination
 * 4. Karana - Half of Tithi
 * 5. Vara - Day of the week
 *
 * Additionally calculates:
 * - Sunrise and Sunset times
 * - Moon phase
 * - Paksha (lunar fortnight)
 */
class PanchangaCalculator(context: Context) {

    private val swissEph = SwissEph()
    private val ephemerisPath: String

    companion object {
        private const val AYANAMSA_LAHIRI = SweConst.SE_SIDM_LAHIRI
        private const val SEFLG_SIDEREAL = SweConst.SEFLG_SIDEREAL
        private const val SEFLG_SPEED = SweConst.SEFLG_SPEED

        // Tithi span is 12 degrees (360/30 tithis)
        private const val TITHI_SPAN = 12.0

        // Nakshatra span is 13.333... degrees (360/27 nakshatras)
        private const val NAKSHATRA_SPAN = 13.333333333

        // Yoga span is 13.333... degrees
        private const val YOGA_SPAN = 13.333333333

        // Karana span is 6 degrees (half of tithi)
        private const val KARANA_SPAN = 6.0
    }

    init {
        ephemerisPath = context.filesDir.absolutePath + "/ephe"
        swissEph.swe_set_ephe_path(ephemerisPath)
        swissEph.swe_set_sid_mode(AYANAMSA_LAHIRI, 0.0, 0.0)
    }

    /**
     * Calculate complete Panchanga for a given date, time, and location
     */
    fun calculatePanchanga(
        dateTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): PanchangaData {
        // Convert to UTC
        val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of(timezone))
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))

        // Calculate Julian Day
        val julianDay = calculateJulianDay(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour,
            utcDateTime.minute,
            utcDateTime.second
        )

        // Get Sun and Moon positions
        val sunLongitude = getPlanetLongitude(SweConst.SE_SUN, julianDay)
        val moonLongitude = getPlanetLongitude(SweConst.SE_MOON, julianDay)

        // Calculate Tithi
        val tithi = calculateTithi(sunLongitude, moonLongitude)

        // Calculate Nakshatra (based on Moon position)
        val nakshatra = calculateNakshatra(moonLongitude)

        // Calculate Yoga
        val yoga = calculateYoga(sunLongitude, moonLongitude)

        // Calculate Karana
        val karana = calculateKarana(sunLongitude, moonLongitude)

        // Calculate Vara (day of week)
        val vara = calculateVara(julianDay)

        // Calculate Paksha
        val paksha = calculatePaksha(tithi)

        // Calculate sunrise and sunset
        val (sunrise, sunset) = calculateSunriseSunset(julianDay, latitude, longitude)

        // Calculate moon phase percentage
        val moonPhase = calculateMoonPhase(sunLongitude, moonLongitude)

        return PanchangaData(
            tithi = tithi,
            nakshatra = nakshatra,
            yoga = yoga,
            karana = karana,
            vara = vara,
            paksha = paksha,
            sunrise = sunrise,
            sunset = sunset,
            moonPhase = moonPhase,
            sunLongitude = sunLongitude,
            moonLongitude = moonLongitude
        )
    }

    private fun getPlanetLongitude(planetId: Int, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swissEph.swe_calc_ut(
            julianDay,
            planetId,
            SEFLG_SIDEREAL or SEFLG_SPEED,
            xx,
            serr
        )

        return ((xx[0] % 360.0) + 360.0) % 360.0
    }

    /**
     * Calculate Tithi (lunar day)
     * Tithi is based on the angular distance between Moon and Sun
     * Each tithi spans 12 degrees
     */
    private fun calculateTithi(sunLongitude: Double, moonLongitude: Double): TithiData {
        var diff = moonLongitude - sunLongitude
        if (diff < 0) diff += 360.0

        val tithiNumber = (diff / TITHI_SPAN).toInt() + 1
        val tithiProgress = (diff % TITHI_SPAN) / TITHI_SPAN * 100.0

        val tithi = Tithi.entries[tithiNumber - 1]
        val lord = getTithiLord(tithiNumber)

        return TithiData(
            tithi = tithi,
            number = tithiNumber,
            progress = tithiProgress,
            lord = lord
        )
    }

    private fun getTithiLord(tithiNumber: Int): Planet {
        // Tithi lords follow a specific sequence
        val lords = listOf(
            Planet.SUN,      // 1 - Pratipada
            Planet.MOON,     // 2 - Dwitiya
            Planet.MARS,     // 3 - Tritiya
            Planet.MERCURY,  // 4 - Chaturthi
            Planet.JUPITER,  // 5 - Panchami
            Planet.VENUS,    // 6 - Shashthi
            Planet.SATURN,   // 7 - Saptami
            Planet.RAHU,     // 8 - Ashtami
            Planet.SUN,      // 9 - Navami
            Planet.MOON,     // 10 - Dashami
            Planet.MARS,     // 11 - Ekadashi
            Planet.MERCURY,  // 12 - Dwadashi
            Planet.JUPITER,  // 13 - Trayodashi
            Planet.VENUS,    // 14 - Chaturdashi
            Planet.SATURN    // 15/30 - Purnima/Amavasya
        )
        return lords[(tithiNumber - 1) % 15]
    }

    /**
     * Calculate Nakshatra based on Moon's sidereal position
     */
    private fun calculateNakshatra(moonLongitude: Double): NakshatraData {
        val nakshatraNumber = (moonLongitude / NAKSHATRA_SPAN).toInt() + 1
        val nakshatraProgress = (moonLongitude % NAKSHATRA_SPAN) / NAKSHATRA_SPAN * 100.0
        val pada = ((moonLongitude % NAKSHATRA_SPAN) / (NAKSHATRA_SPAN / 4)).toInt() + 1

        val nakshatra = Nakshatra.entries[nakshatraNumber - 1]

        return NakshatraData(
            nakshatra = nakshatra,
            number = nakshatraNumber,
            pada = pada.coerceIn(1, 4),
            progress = nakshatraProgress,
            lord = nakshatra.ruler
        )
    }

    /**
     * Calculate Yoga (Nithya Yoga)
     * Yoga is calculated by adding Sun and Moon longitudes
     * There are 27 Yogas, each spanning 13°20'
     */
    private fun calculateYoga(sunLongitude: Double, moonLongitude: Double): YogaData {
        var sum = sunLongitude + moonLongitude
        if (sum >= 360.0) sum -= 360.0

        val yogaNumber = (sum / YOGA_SPAN).toInt() + 1
        val yogaProgress = (sum % YOGA_SPAN) / YOGA_SPAN * 100.0

        val yoga = Yoga.entries.getOrElse(yogaNumber - 1) { Yoga.VISHKUMBHA }

        return YogaData(
            yoga = yoga,
            number = yogaNumber,
            progress = yogaProgress
        )
    }

    /**
     * Calculate Karana
     * Karana is half of a Tithi. There are 11 Karanas that repeat in a cycle.
     * 4 fixed karanas occur only once per lunar month.
     */
    private fun calculateKarana(sunLongitude: Double, moonLongitude: Double): KaranaData {
        var diff = moonLongitude - sunLongitude
        if (diff < 0) diff += 360.0

        val karanaNumber = (diff / KARANA_SPAN).toInt() + 1
        val karanaProgress = (diff % KARANA_SPAN) / KARANA_SPAN * 100.0

        val karana = getKarana(karanaNumber)

        return KaranaData(
            karana = karana,
            number = karanaNumber,
            progress = karanaProgress
        )
    }

    private fun getKarana(karanaNumber: Int): Karana {
        // First 4 karanas are fixed (occur only once)
        // Then 7 movable karanas repeat
        return when (karanaNumber) {
            1 -> Karana.KIMSTUGHNA
            in 2..8 -> Karana.entries[(karanaNumber - 2) % 7 + 1]
            in 9..15 -> Karana.entries[(karanaNumber - 9) % 7 + 1]
            in 16..22 -> Karana.entries[(karanaNumber - 16) % 7 + 1]
            in 23..29 -> Karana.entries[(karanaNumber - 23) % 7 + 1]
            in 30..36 -> Karana.entries[(karanaNumber - 30) % 7 + 1]
            in 37..43 -> Karana.entries[(karanaNumber - 37) % 7 + 1]
            in 44..50 -> Karana.entries[(karanaNumber - 44) % 7 + 1]
            in 51..57 -> Karana.entries[(karanaNumber - 51) % 7 + 1]
            58 -> Karana.SHAKUNI
            59 -> Karana.CHATUSHPADA
            60 -> Karana.NAGAVA
            else -> Karana.BAVA
        }
    }

    /**
     * Calculate Vara (day of week)
     */
    private fun calculateVara(julianDay: Double): Vara {
        val dayNumber = ((julianDay + 1.5) % 7).toInt()
        return Vara.entries[dayNumber]
    }

    /**
     * Calculate Paksha (lunar fortnight)
     */
    private fun calculatePaksha(tithiData: TithiData): Paksha {
        return if (tithiData.number <= 15) Paksha.SHUKLA else Paksha.KRISHNA
    }

    /**
     * Calculate Sunrise and Sunset times
     */
    private fun calculateSunriseSunset(
        julianDay: Double,
        latitude: Double,
        longitude: Double
    ): Pair<String, String> {
        val geopos = doubleArrayOf(longitude, latitude, 0.0)
        val tret = DblObj()
        val serr = StringBuffer()

        // Calculate sunrise
        val riseResult = swissEph.swe_rise_trans(
            julianDay,
            SweConst.SE_SUN,
            null,
            SweConst.SEFLG_SWIEPH,
            SweConst.SE_CALC_RISE,
            geopos,
            0.0,
            0.0,
            tret,
            serr
        )

        val sunriseJD = if (riseResult >= 0) tret.`val` else julianDay

        // Calculate sunset
        val setResult = swissEph.swe_rise_trans(
            julianDay,
            SweConst.SE_SUN,
            null,
            SweConst.SEFLG_SWIEPH,
            SweConst.SE_CALC_SET,
            geopos,
            0.0,
            0.0,
            tret,
            serr
        )

        val sunsetJD = if (setResult >= 0) tret.`val` else julianDay

        return Pair(
            formatJulianDayToTime(sunriseJD),
            formatJulianDayToTime(sunsetJD)
        )
    }

    private fun formatJulianDayToTime(julianDay: Double): String {
        val sweDate = SweDate(julianDay)
        val hour = sweDate.hour.toInt()
        val minute = ((sweDate.hour - hour) * 60).toInt()
        val second = ((((sweDate.hour - hour) * 60) - minute) * 60).toInt()

        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }

        return String.format("%d:%02d:%02d %s", displayHour, minute, second, amPm)
    }

    /**
     * Calculate Moon phase percentage (0% = New Moon, 100% = Full Moon)
     */
    private fun calculateMoonPhase(sunLongitude: Double, moonLongitude: Double): Double {
        var diff = moonLongitude - sunLongitude
        if (diff < 0) diff += 360.0

        // Convert to percentage (0-180 = waxing, 180-360 = waning)
        return if (diff <= 180) {
            diff / 180.0 * 100.0
        } else {
            (360.0 - diff) / 180.0 * 100.0
        }
    }

    private fun calculateJulianDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Double {
        val decimalHours = hour + (minute / 60.0) + (second / 3600.0)
        val sweDate = SweDate(year, month, day, decimalHours, SweDate.SE_GREG_CAL)
        return sweDate.julDay
    }

    fun close() {
        swissEph.swe_close()
    }
}

// Enums and Data Classes for Panchanga

enum class Tithi(val number: Int, val displayName: String, val sanskrit: String) {
    PRATIPADA(1, "Pratipada", "प्रतिपदा"),
    DWITIYA(2, "Dwitiya", "द्वितीया"),
    TRITIYA(3, "Tritiya", "तृतीया"),
    CHATURTHI(4, "Chaturthi", "चतुर्थी"),
    PANCHAMI(5, "Panchami", "पंचमी"),
    SHASHTHI(6, "Shashthi", "षष्ठी"),
    SAPTAMI(7, "Saptami", "सप्तमी"),
    ASHTAMI(8, "Ashtami", "अष्टमी"),
    NAVAMI(9, "Navami", "नवमी"),
    DASHAMI(10, "Dashami", "दशमी"),
    EKADASHI(11, "Ekadashi", "एकादशी"),
    DWADASHI(12, "Dwadashi", "द्वादशी"),
    TRAYODASHI(13, "Trayodashi", "त्रयोदशी"),
    CHATURDASHI(14, "Chaturdashi", "चतुर्दशी"),
    PURNIMA(15, "Purnima", "पूर्णिमा"),
    PRATIPADA_K(16, "Pratipada", "प्रतिपदा"),
    DWITIYA_K(17, "Dwitiya", "द्वितीया"),
    TRITIYA_K(18, "Tritiya", "तृतीया"),
    CHATURTHI_K(19, "Chaturthi", "चतुर्थी"),
    PANCHAMI_K(20, "Panchami", "पंचमी"),
    SHASHTHI_K(21, "Shashthi", "षष्ठी"),
    SAPTAMI_K(22, "Saptami", "सप्तमी"),
    ASHTAMI_K(23, "Ashtami", "अष्टमी"),
    NAVAMI_K(24, "Navami", "नवमी"),
    DASHAMI_K(25, "Dashami", "दशमी"),
    EKADASHI_K(26, "Ekadashi", "एकादशी"),
    DWADASHI_K(27, "Dwadashi", "द्वादशी"),
    TRAYODASHI_K(28, "Trayodashi", "त्रयोदशी"),
    CHATURDASHI_K(29, "Chaturdashi", "चतुर्दशी"),
    AMAVASYA(30, "Amavasya", "अमावस्या")
}

enum class Yoga(val number: Int, val displayName: String, val nature: String) {
    VISHKUMBHA(1, "Vishkumbha", "Inauspicious"),
    PRITI(2, "Priti", "Auspicious"),
    AYUSHMAN(3, "Ayushman", "Auspicious"),
    SAUBHAGYA(4, "Saubhagya", "Auspicious"),
    SHOBHANA(5, "Shobhana", "Auspicious"),
    ATIGANDA(6, "Atiganda", "Inauspicious"),
    SUKARMA(7, "Sukarma", "Auspicious"),
    DHRITI(8, "Dhriti", "Auspicious"),
    SHOOLA(9, "Shoola", "Inauspicious"),
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
    VAIDHRITI(27, "Vaidhriti", "Inauspicious")
}

enum class Karana(val number: Int, val displayName: String, val nature: String) {
    KIMSTUGHNA(1, "Kimstughna", "Fixed"),
    BAVA(2, "Bava", "Movable"),
    BALAVA(3, "Balava", "Movable"),
    KAULAVA(4, "Kaulava", "Movable"),
    TAITILA(5, "Taitila", "Movable"),
    GARIJA(6, "Garija", "Movable"),
    VANIJA(7, "Vanija", "Movable"),
    VISHTI(8, "Vishti", "Movable"),
    SHAKUNI(9, "Shakuni", "Fixed"),
    CHATUSHPADA(10, "Chatushpada", "Fixed"),
    NAGAVA(11, "Nagava", "Fixed")
}

enum class Vara(val number: Int, val displayName: String, val lord: Planet) {
    SUNDAY(0, "Sunday", Planet.SUN),
    MONDAY(1, "Monday", Planet.MOON),
    TUESDAY(2, "Tuesday", Planet.MARS),
    WEDNESDAY(3, "Wednesday", Planet.MERCURY),
    THURSDAY(4, "Thursday", Planet.JUPITER),
    FRIDAY(5, "Friday", Planet.VENUS),
    SATURDAY(6, "Saturday", Planet.SATURN)
}

enum class Paksha(val displayName: String, val description: String) {
    SHUKLA("Shukla Paksha", "Bright/Waxing Fortnight"),
    KRISHNA("Krishna Paksha", "Dark/Waning Fortnight")
}

data class TithiData(
    val tithi: Tithi,
    val number: Int,
    val progress: Double,
    val lord: Planet
)

data class NakshatraData(
    val nakshatra: Nakshatra,
    val number: Int,
    val pada: Int,
    val progress: Double,
    val lord: Planet
)

data class YogaData(
    val yoga: Yoga,
    val number: Int,
    val progress: Double
)

data class KaranaData(
    val karana: Karana,
    val number: Int,
    val progress: Double
)

data class PanchangaData(
    val tithi: TithiData,
    val nakshatra: NakshatraData,
    val yoga: YogaData,
    val karana: KaranaData,
    val vara: Vara,
    val paksha: Paksha,
    val sunrise: String,
    val sunset: String,
    val moonPhase: Double,
    val sunLongitude: Double,
    val moonLongitude: Double
) {
    fun toPlainText(): String {
        return buildString {
            appendLine("═══════════════════════════════════════════════════")
            appendLine("                    PANCHANGA")
            appendLine("═══════════════════════════════════════════════════")
            appendLine()
            appendLine("TITHI")
            appendLine("  ${tithi.tithi.displayName} (${tithi.tithi.sanskrit})")
            appendLine("  Progress: ${String.format("%.1f", tithi.progress)}%")
            appendLine("  Lord: ${tithi.lord.displayName}")
            appendLine()
            appendLine("NAKSHATRA")
            appendLine("  ${nakshatra.nakshatra.displayName} - Pada ${nakshatra.pada}")
            appendLine("  Progress: ${String.format("%.1f", nakshatra.progress)}%")
            appendLine("  Lord: ${nakshatra.lord.displayName}")
            appendLine()
            appendLine("YOGA")
            appendLine("  ${yoga.yoga.displayName} (${yoga.yoga.nature})")
            appendLine("  Progress: ${String.format("%.1f", yoga.progress)}%")
            appendLine()
            appendLine("KARANA")
            appendLine("  ${karana.karana.displayName} (${karana.karana.nature})")
            appendLine("  Progress: ${String.format("%.1f", karana.progress)}%")
            appendLine()
            appendLine("VARA: ${vara.displayName}")
            appendLine("PAKSHA: ${paksha.displayName}")
            appendLine()
            appendLine("SUNRISE: $sunrise")
            appendLine("SUNSET: $sunset")
            appendLine("MOON ILLUMINATION: ${String.format("%.1f", moonPhase)}%")
            appendLine()
        }
    }
}
