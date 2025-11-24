package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.*
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.io.File
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.math.abs

/**
 * High-precision Swiss Ephemeris calculation engine for Vedic astrology.
 * Uses JPL ephemeris files for maximum accuracy.
 */
class SwissEphemerisEngine(context: Context) {

    private val swissEph = SwissEph()
    private val ephemerisPath: String

    companion object {
        private const val AYANAMSA_LAHIRI = SweConst.SE_SIDM_LAHIRI
        private const val SEFLG_SIDEREAL = SweConst.SEFLG_SIDEREAL
        private const val SEFLG_SPEED = SweConst.SEFLG_SPEED
        private const val SEFLG_JPLEPH = SweConst.SEFLG_JPLEPH

        // Combined flags for maximum precision
        private const val CALC_FLAGS = SEFLG_SIDEREAL or SEFLG_SPEED or SEFLG_JPLEPH
    }

    init {
        // Set ephemeris path to app's files directory
        ephemerisPath = context.filesDir.absolutePath + "/ephe"
        File(ephemerisPath).mkdirs()

        // Copy ephemeris files from assets if needed
        copyEphemerisFiles(context)

        // Initialize Swiss Ephemeris with JPL mode
        swissEph.swe_set_ephe_path(ephemerisPath)
        swissEph.swe_set_sid_mode(AYANAMSA_LAHIRI, 0.0, 0.0)
    }

    private fun copyEphemerisFiles(context: Context) {
        try {
            val assetManager = context.assets
            val ephemerisFiles = try {
                assetManager.list("ephe") ?: emptyArray()
            } catch (e: Exception) {
                emptyArray()
            }

            ephemerisFiles.forEach { filename ->
                val outFile = File(ephemerisPath, filename)
                if (!outFile.exists()) {
                    assetManager.open("ephe/$filename").use { input ->
                        outFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Ephemeris files are optional; calculations will use less precise methods if unavailable
            android.util.Log.w("SwissEphemeris", "Could not copy ephemeris files: ${e.message}")
        }
    }

    /**
     * Calculate complete Vedic chart with maximum precision
     */
    fun calculateVedicChart(
        birthData: BirthData,
        houseSystem: HouseSystem = HouseSystem.DEFAULT
    ): VedicChart {
        // Convert local time to UTC
        val zonedDateTime = ZonedDateTime.of(
            birthData.dateTime,
            ZoneId.of(birthData.timezone)
        )
        val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneId.of("UTC"))

        // Calculate Julian Day with full precision
        val julianDay = calculateJulianDay(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour,
            utcDateTime.minute,
            utcDateTime.second
        )

        // Get ayanamsa value
        val ayanamsa = swissEph.swe_get_ayanamsa_ut(julianDay)

        // Calculate house cusps and ascendant
        val houseCusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        swissEph.swe_houses(
            julianDay,
            0,
            birthData.latitude.toDouble(),
            birthData.longitude.toDouble(),
            houseSystem.code.code,
            houseCusps,
            ascmc
        )

        val ascendant = ascmc[0]
        val midheaven = ascmc[1]

        // Calculate planetary positions
        val planetPositions = Planet.MAIN_PLANETS.map { planet ->
            calculatePlanetPosition(planet, julianDay, houseCusps, ascendant)
        }

        return VedicChart(
            birthData = birthData,
            julianDay = julianDay,
            ayanamsa = ayanamsa,
            ayanamsaName = "Lahiri",
            ascendant = ascendant,
            midheaven = midheaven,
            planetPositions = planetPositions,
            houseCusps = houseCusps.drop(1).toList(), // Drop first element (unused by Swiss Ephemeris)
            houseSystem = houseSystem
        )
    }

    /**
     * Calculate planet position with full precision
     */
    private fun calculatePlanetPosition(
        planet: Planet,
        julianDay: Double,
        houseCusps: DoubleArray,
        ascendant: Double
    ): PlanetPosition {
        val xx = DoubleArray(6)
        val serr = StringBuffer()

        val iflgret = if (planet == Planet.KETU) {
            // Ketu is 180° opposite to Rahu
            swissEph.swe_calc_ut(
                julianDay,
                Planet.RAHU.swissEphId,
                CALC_FLAGS,
                xx,
                serr
            )
        } else {
            swissEph.swe_calc_ut(
                julianDay,
                planet.swissEphId,
                CALC_FLAGS,
                xx,
                serr
            )
        }

        if (iflgret < 0) {
            throw RuntimeException("Swiss Ephemeris calculation error: $serr")
        }

        var longitude = xx[0]
        val latitude = xx[1]
        val distance = xx[2]
        val speed = xx[3]

        // Adjust Ketu to be 180° from Rahu
        if (planet == Planet.KETU) {
            longitude = (longitude + 180.0) % 360.0
        }

        // Normalize longitude
        longitude = (longitude % 360.0 + 360.0) % 360.0

        // Determine sign and degree within sign
        val sign = ZodiacSign.fromLongitude(longitude)
        val degreeInSign = longitude % 30.0
        val degree = degreeInSign.toInt().toDouble()
        val minutes = ((degreeInSign - degree) * 60.0)
        val seconds = ((minutes - minutes.toInt()) * 60.0)

        // Check if retrograde (negative speed)
        val isRetrograde = speed < 0.0

        // Get nakshatra
        val (nakshatra, pada) = Nakshatra.fromLongitude(longitude)

        // Determine house
        val house = determineHouse(longitude, houseCusps, ascendant)

        return PlanetPosition(
            planet = planet,
            longitude = longitude,
            latitude = latitude,
            distance = distance,
            speed = speed,
            sign = sign,
            degree = degree,
            minutes = minutes.toInt().toDouble(),
            seconds = seconds,
            isRetrograde = isRetrograde,
            nakshatra = nakshatra,
            nakshatraPada = pada,
            house = house
        )
    }

    /**
     * Determine which house a planet is in
     */
    private fun determineHouse(
        longitude: Double,
        houseCusps: DoubleArray,
        ascendant: Double
    ): Int {
        val normalizedLongitude = (longitude % 360.0 + 360.0) % 360.0

        for (i in 1..12) {
            val cuspStart = houseCusps[i]
            val cuspEnd = if (i == 12) houseCusps[1] else houseCusps[i + 1]

            if (cuspEnd > cuspStart) {
                if (normalizedLongitude >= cuspStart && normalizedLongitude < cuspEnd) {
                    return i
                }
            } else {
                // Crosses 0° Aries
                if (normalizedLongitude >= cuspStart || normalizedLongitude < cuspEnd) {
                    return i
                }
            }
        }

        return 1 // Default to first house
    }

    /**
     * Calculate Julian Day with full precision
     * Ensures no rounding errors in time conversion
     */
    private fun calculateJulianDay(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int
    ): Double {
        // Convert time to decimal hours with full precision
        val decimalHours = hour + (minute / 60.0) + (second / 3600.0)

        // Use Swiss Ephemeris date conversion for maximum accuracy
        val sweDate = SweDate(year, month, day, decimalHours, SweDate.SE_GREG_CAL)
        return sweDate.julDay
    }

    /**
     * Get current ayanamsa value
     */
    fun getAyanamsa(julianDay: Double): Double {
        return swissEph.swe_get_ayanamsa_ut(julianDay)
    }

    /**
     * Clean up resources
     */
    fun close() {
        swissEph.swe_close()
    }
}
