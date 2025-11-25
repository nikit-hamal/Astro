package com.astro.storm.calculator

import com.astro.storm.data.model.*
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs
import kotlin.math.floor

/**
 * High-precision Panchanga Calculator
 * Computes Tithi, Vara, Nakshatra, Yoga, Karana with astronomical accuracy
 */
class PanchangaCalculator(private val swissEph: SwissEph) {

    companion object {
        private const val AYANAMSA_LAHIRI = SweConst.SE_SIDM_LAHIRI
        private const val SEFLG_SIDEREAL = SweConst.SEFLG_SIDEREAL
        private const val SEFLG_SPEED = SweConst.SEFLG_SPEED
        private const val CALC_FLAGS = SEFLG_SIDEREAL or SEFLG_SPEED

        // Astronomical constants
        private const val NAKSHATRA_ARC = 13.333333  // 360 / 27
        private const val TITHI_ARC = 12.0           // 360 / 30
        private const val YOGA_ARC = 13.333333       // 360 / 27
        private const val KARANA_ARC = 6.0           // 360 / 60
    }

    /**
     * Calculate complete Panchanga for a given date, time, and location
     */
    fun calculatePanchanga(
        dateTime: LocalDateTime,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): Panchanga {
        // Convert to UTC
        val zonedDateTime = ZonedDateTime.of(dateTime, ZoneId.of(timezone))
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))

        // Calculate Julian Day
        val julianDay = calculateJulianDay(utcDateTime)

        // Get Sun and Moon positions
        val sunLongitude = getPlanetLongitude(SweConst.SE_SUN, julianDay)
        val moonLongitude = getPlanetLongitude(SweConst.SE_MOON, julianDay)

        // Calculate Panchanga components
        val tithi = calculateTithi(sunLongitude, moonLongitude)
        val tithiEndTime = calculateTithiEndTime(julianDay, sunLongitude, moonLongitude, timezone)

        val vara = calculateVara(dateTime)

        val (nakshatra, _) = Nakshatra.fromLongitude(moonLongitude)
        val nakshatraEndTime = calculateNakshatraEndTime(julianDay, moonLongitude, timezone)

        val yoga = calculateYoga(sunLongitude, moonLongitude)
        val yogaEndTime = calculateYogaEndTime(julianDay, sunLongitude, moonLongitude, timezone)

        val karana = calculateKarana(sunLongitude, moonLongitude)
        val karanaEndTime = calculateKaranaEndTime(julianDay, sunLongitude, moonLongitude, timezone)

        // Calculate sunrise and sunset
        val sunriseTime = calculateSunrise(julianDay, latitude, longitude, timezone)
        val sunsetTime = calculateSunset(julianDay, latitude, longitude, timezone)

        // Calculate moonrise and moonset
        val moonriseTime = calculateMoonrise(julianDay, latitude, longitude, timezone)
        val moonsetTime = calculateMoonset(julianDay, latitude, longitude, timezone)

        // Calculate lunar phase
        val lunarPhase = calculateLunarPhase(sunLongitude, moonLongitude)

        return Panchanga(
            dateTime = dateTime,
            tithi = tithi,
            tithiEndTime = tithiEndTime,
            vara = vara,
            nakshatra = nakshatra,
            nakshatraEndTime = nakshatraEndTime,
            yoga = yoga,
            yogaEndTime = yogaEndTime,
            karana = karana,
            karanaEndTime = karanaEndTime,
            sunriseTime = sunriseTime,
            sunsetTime = sunsetTime,
            moonrise = moonriseTime,
            moonset = moonsetTime,
            sunLongitude = sunLongitude,
            moonLongitude = moonLongitude,
            lunarPhase = lunarPhase
        )
    }

    /**
     * Calculate Tithi from Sun and Moon longitudes
     * Tithi = (Moon longitude - Sun longitude) / 12
     */
    private fun calculateTithi(sunLongitude: Double, moonLongitude: Double): Tithi {
        val elongation = (moonLongitude - sunLongitude + 360.0) % 360.0
        val tithiNumber = floor(elongation / TITHI_ARC).toInt() + 1
        return Tithi.fromLunarDayNumber(tithiNumber.coerceIn(1, 30))
    }

    /**
     * Calculate Yoga from Sun and Moon longitudes
     */
    private fun calculateYoga(sunLongitude: Double, moonLongitude: Double): Yoga {
        return Yoga.calculate(sunLongitude, moonLongitude)
    }

    /**
     * Calculate when current Tithi ends
     */
    private fun calculateTithiEndTime(
        julianDay: Double,
        sunLongitude: Double,
        moonLongitude: Double,
        timezone: String
    ): LocalDateTime? {
        return calculatePanchangaEndTime(
            julianDay,
            sunLongitude,
            moonLongitude,
            TITHI_ARC,
            timezone
        ) { s, m -> (m - s + 360.0) % 360.0 }
    }

    /**
     * Calculate Vara (weekday)
     */
    private fun calculateVara(dateTime: LocalDateTime): Vara {
        val dayOfWeek = dateTime.dayOfWeek.value
        return Vara.fromDayOfWeek(dayOfWeek)
    }

    /**
     * Calculate when current Nakshatra ends
     */
    private fun calculateNakshatraEndTime(
        julianDay: Double,
        moonLongitude: Double,
        timezone: String
    ): LocalDateTime? {
        // Find next nakshatra boundary
        val currentNakshatraIndex = floor(moonLongitude / NAKSHATRA_ARC).toInt()
        val nextBoundary = (currentNakshatraIndex + 1) * NAKSHATRA_ARC

        // Calculate time to reach boundary using Moon's speed
        val moonSpeed = getPlanetSpeed(SweConst.SE_MOON, julianDay)
        if (moonSpeed <= 0) return null

        val degreesToTravel = (nextBoundary - moonLongitude + 360.0) % 360.0
        val daysToTravel = degreesToTravel / moonSpeed

        return julianDayToLocalDateTime(julianDay + daysToTravel, timezone)
    }

    /**
     * Calculate when current Yoga ends
     */
    private fun calculateYogaEndTime(
        julianDay: Double,
        sunLongitude: Double,
        moonLongitude: Double,
        timezone: String
    ): LocalDateTime? {
        return calculatePanchangaEndTime(
            julianDay,
            sunLongitude,
            moonLongitude,
            YOGA_ARC,
            timezone
        ) { s, m -> (s + m) % 360.0 }
    }

    /**
     * Calculate Karana from Sun and Moon longitudes
     * Karana = (Moon longitude - Sun longitude) / 6
     */
    private fun calculateKarana(sunLongitude: Double, moonLongitude: Double): Karana {
        val elongation = (moonLongitude - sunLongitude + 360.0) % 360.0
        val karanaNumber = floor(elongation / KARANA_ARC).toInt()

        // Karana logic: first 7 repeat 8 times, last 4 are fixed
        return when {
            karanaNumber in 0..56 -> {
                val movableIndex = (karanaNumber % 7) + 1
                Karana.values().find { it.number == movableIndex && !it.isFixed } ?: Karana.BAVA
            }
            karanaNumber in 57..59 -> {
                val fixedIndex = karanaNumber - 57 + 8
                Karana.values().find { it.number == fixedIndex && it.isFixed } ?: Karana.SHAKUNI
            }
            else -> Karana.BAVA
        }
    }

    /**
     * Calculate when current Karana ends
     */
    private fun calculateKaranaEndTime(
        julianDay: Double,
        sunLongitude: Double,
        moonLongitude: Double,
        timezone: String
    ): LocalDateTime? {
        return calculatePanchangaEndTime(
            julianDay,
            sunLongitude,
            moonLongitude,
            KARANA_ARC,
            timezone
        ) { s, m -> (m - s + 360.0) % 360.0 }
    }

    /**
     * Generic method to calculate end time of Panchanga elements
     */
    private fun calculatePanchangaEndTime(
        julianDay: Double,
        sunLongitude: Double,
        moonLongitude: Double,
        arc: Double,
        timezone: String,
        elongationFunc: (Double, Double) -> Double
    ): LocalDateTime? {
        val currentElongation = elongationFunc(sunLongitude, moonLongitude)
        val currentIndex = floor(currentElongation / arc).toInt()
        val nextBoundary = (currentIndex + 1) * arc

        // Calculate combined speed (Sun + Moon for yoga, Moon - Sun for tithi/karana)
        val sunSpeed = getPlanetSpeed(SweConst.SE_SUN, julianDay)
        val moonSpeed = getPlanetSpeed(SweConst.SE_MOON, julianDay)

        val combinedSpeed = when {
            arc == YOGA_ARC -> sunSpeed + moonSpeed
            else -> moonSpeed - sunSpeed
        }

        if (combinedSpeed <= 0) return null

        val degreesToTravel = (nextBoundary - currentElongation + 360.0) % 360.0
        val daysToTravel = degreesToTravel / combinedSpeed

        return julianDayToLocalDateTime(julianDay + daysToTravel, timezone)
    }

    /**
     * Calculate sunrise time
     */
    private fun calculateSunrise(
        julianDay: Double,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime {
        val geopos = doubleArrayOf(longitude, latitude, 0.0)
        val tret = doubleArrayOf(0.0, 0.0)
        val serr = StringBuffer()

        swissEph.swe_rise_trans(
            julianDay,
            SweConst.SE_SUN,
            null,
            SweConst.SEFLG_SIDEREAL,
            SweConst.SE_CALC_RISE,
            geopos,
            1013.25,
            10.0,
            tret,
            serr
        )

        return julianDayToLocalDateTime(tret[0], timezone)
    }

    /**
     * Calculate sunset time
     */
    private fun calculateSunset(
        julianDay: Double,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime {
        val geopos = doubleArrayOf(longitude, latitude, 0.0)
        val tret = doubleArrayOf(0.0, 0.0)
        val serr = StringBuffer()

        swissEph.swe_rise_trans(
            julianDay,
            SweConst.SE_SUN,
            null,
            SweConst.SEFLG_SIDEREAL,
            SweConst.SE_CALC_SET,
            geopos,
            1013.25,
            10.0,
            tret,
            serr
        )

        return julianDayToLocalDateTime(tret[0], timezone)
    }

    /**
     * Calculate moonrise time
     */
    private fun calculateMoonrise(
        julianDay: Double,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime? {
        return try {
            val geopos = doubleArrayOf(longitude, latitude, 0.0)
            val tret = doubleArrayOf(0.0, 0.0)
            val serr = StringBuffer()

            val result = swissEph.swe_rise_trans(
                julianDay,
                SweConst.SE_MOON,
                null,
                SweConst.SEFLG_SIDEREAL,
                SweConst.SE_CALC_RISE,
                geopos,
                1013.25,
                10.0,
                tret,
                serr
            )

            if (result >= 0) julianDayToLocalDateTime(tret[0], timezone) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate moonset time
     */
    private fun calculateMoonset(
        julianDay: Double,
        latitude: Double,
        longitude: Double,
        timezone: String
    ): LocalDateTime? {
        return try {
            val geopos = doubleArrayOf(longitude, latitude, 0.0)
            val tret = doubleArrayOf(0.0, 0.0)
            val serr = StringBuffer()

            val result = swissEph.swe_rise_trans(
                julianDay,
                SweConst.SE_MOON,
                null,
                SweConst.SEFLG_SIDEREAL,
                SweConst.SE_CALC_SET,
                geopos,
                1013.25,
                10.0,
                tret,
                serr
            )

            if (result >= 0) julianDayToLocalDateTime(tret[0], timezone) else null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Calculate lunar phase (0 = New Moon, 0.5 = Full Moon, 1 = New Moon)
     */
    private fun calculateLunarPhase(sunLongitude: Double, moonLongitude: Double): Double {
        val elongation = (moonLongitude - sunLongitude + 360.0) % 360.0
        return elongation / 360.0
    }

    /**
     * Get planet longitude at a given Julian Day
     */
    private fun getPlanetLongitude(planetId: Int, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swissEph.swe_calc_ut(
            julianDay,
            planetId,
            CALC_FLAGS,
            xx,
            serr
        )

        return (xx[0] % 360.0 + 360.0) % 360.0
    }

    /**
     * Get planet speed at a given Julian Day
     */
    private fun getPlanetSpeed(planetId: Int, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        swissEph.swe_calc_ut(
            julianDay,
            planetId,
            CALC_FLAGS,
            xx,
            serr
        )

        return xx[3] // Speed is in xx[3]
    }

    /**
     * Calculate Julian Day from LocalDateTime
     */
    private fun calculateJulianDay(utcDateTime: ZonedDateTime): Double {
        val decimalHours = utcDateTime.hour +
            (utcDateTime.minute / 60.0) +
            (utcDateTime.second / 3600.0)

        val sweDate = SweDate(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            decimalHours,
            SweDate.SE_GREG_CAL
        )
        return sweDate.julDay
    }

    /**
     * Convert Julian Day to LocalDateTime in given timezone
     */
    private fun julianDayToLocalDateTime(julianDay: Double, timezone: String): LocalDateTime {
        val sweDate = SweDate(julianDay, SweDate.SE_GREG_CAL)

        val year = sweDate.year
        val month = sweDate.month
        val day = sweDate.day
        val hour = sweDate.hour

        val decimalHour = hour
        val hourInt = decimalHour.toInt()
        val minute = ((decimalHour - hourInt) * 60).toInt()
        val second = ((((decimalHour - hourInt) * 60) - minute) * 60).toInt()

        val utcDateTime = LocalDateTime.of(year, month, day, hourInt, minute, second)
        val zonedUtc = ZonedDateTime.of(utcDateTime, ZoneId.of("UTC"))

        return zonedUtc.withZoneSameInstant(ZoneId.of(timezone)).toLocalDateTime()
    }
}
