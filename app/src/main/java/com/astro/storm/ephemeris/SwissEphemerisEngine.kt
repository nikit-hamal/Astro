package com.astro.storm.ephemeris

import android.content.Context
import com.astro.storm.data.model.*
import swisseph.SweConst
import swisseph.SweDate
import swisseph.SwissEph
import java.io.File
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
        private const val CALC_FLAGS = SEFLG_SIDEREAL or SEFLG_SPEED or SEFLG_JPLEPH
    }

    init {
        ephemerisPath = context.filesDir.absolutePath + "/ephe"
        File(ephemerisPath).mkdirs()
        copyEphemerisFiles(context)
        swissEph.swe_set_ephe_path(ephemerisPath)
        swissEph.swe_set_sid_mode(AYANAMSA_LAHIRI, 0.0, 0.0)
    }

    private fun copyEphemerisFiles(context: Context) {
        try {
            val assetManager = context.assets
            val ephemerisFiles = assetManager.list("ephe") ?: emptyArray()
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
        val zonedDateTime = ZonedDateTime.of(
            birthData.dateTime,
            java.time.ZoneId.of(birthData.timezone)
        )
        val utcDateTime = zonedDateTime.withZoneSameInstant(java.time.ZoneId.of("UTC"))
        val julianDay = calculateJulianDay(
            utcDateTime.year,
            utcDateTime.monthValue,
            utcDateTime.dayOfMonth,
            utcDateTime.hour,
            utcDateTime.minute,
            utcDateTime.second
        )

        val ayanamsa = swissEph.swe_get_ayanamsa_ut(julianDay)
        val houseCusps = DoubleArray(13)
        val ascmc = DoubleArray(10)
        swissEph.swe_houses(
            julianDay,
            0,
            birthData.latitude,
            birthData.longitude,
            houseSystem.code.code,
            houseCusps,
            ascmc
        )

        val ascendant = ascmc[0]
        val midheaven = ascmc[1]
        val sunLongitude = getPlanetLongitude(Planet.SUN, julianDay)

        val planetPositions = Planet.MAIN_PLANETS.map { planet ->
            calculatePlanetPosition(planet, julianDay, houseCusps, sunLongitude, ascendant)
        }

        return VedicChart(
            birthData = birthData,
            julianDay = julianDay,
            ayanamsa = ayanamsa,
            ayanamsaName = "Lahiri",
            ascendant = ascendant,
            midheaven = midheaven,
            planetPositions = planetPositions,
            houseCusps = houseCusps.drop(1).toList(),
            houseSystem = houseSystem
        )
    }

    private fun getPlanetLongitude(planet: Planet, julianDay: Double): Double {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        val iflgret = swissEph.swe_calc_ut(julianDay, planet.swissEphId, CALC_FLAGS, xx, serr)
        if (iflgret < 0) throw RuntimeException("Swiss Ephemeris error: $serr")
        return (xx[0] % 360.0 + 360.0) % 360.0
    }

    /**
     * Calculate planet position with full precision
     */
    private fun calculatePlanetPosition(
        planet: Planet,
        julianDay: Double,
        houseCusps: DoubleArray,
        sunLongitude: Double,
        ascendantLongitude: Double
    ): PlanetPosition {
        val xx = DoubleArray(6)
        val serr = StringBuffer()
        val swissEphId = if (planet == Planet.KETU) Planet.RAHU.swissEphId else planet.swissEphId
        val iflgret = swissEph.swe_calc_ut(julianDay, swissEphId, CALC_FLAGS, xx, serr)
        if (iflgret < 0) throw RuntimeException("Swiss Ephemeris calculation error: $serr")

        var longitude = if (planet == Planet.KETU) (xx[0] + 180.0) % 360.0 else xx[0]
        longitude = (longitude % 360.0 + 360.0) % 360.0

        val sign = ZodiacSign.fromLongitude(longitude)
        val degreeInSign = longitude % 30.0
        val isRetrograde = xx[3] < 0.0

        val isCombust = isPlanetCombust(planet, longitude, sunLongitude, isRetrograde)
        val isVargottama = isPlanetVargottama(longitude, ascendantLongitude)

        return PlanetPosition(
            planet = planet,
            longitude = longitude,
            latitude = xx[1],
            distance = xx[2],
            speed = xx[3],
            sign = sign,
            degree = degreeInSign.toInt().toDouble(),
            minutes = ((degreeInSign - degreeInSign.toInt()) * 60.0),
            seconds = (((degreeInSign - degreeInSign.toInt()) * 60.0).let { it - it.toInt() } * 60.0),
            isRetrograde = isRetrograde,
            isCombust = isCombust,
            isVargottama = isVargottama,
            nakshatra = Nakshatra.fromLongitude(longitude).first,
            nakshatraPada = Nakshatra.fromLongitude(longitude).second,
            house = determineHouse(longitude, houseCusps)
        )
    }

    private fun isPlanetCombust(planet: Planet, planetLongitude: Double, sunLongitude: Double, isRetrograde: Boolean): Boolean {
        if (planet == Planet.SUN || planet == Planet.RAHU || planet == Planet.KETU) return false
        val distance = abs((planetLongitude - sunLongitude + 360) % 360)
        val combustionOrb = when (planet) {
            Planet.MOON -> 12.0
            Planet.MARS -> 17.0
            Planet.MERCURY -> if (isRetrograde) 12.0 else 14.0
            Planet.JUPITER -> 11.0
            Planet.VENUS -> if (isRetrograde) 8.0 else 10.0
            Planet.SATURN -> 15.0
            else -> 8.5
        }
        return distance <= combustionOrb || distance >= (360 - combustionOrb)
    }

    private fun isPlanetVargottama(longitude: Double, ascendantLongitude: Double): Boolean {
        val rasiSign = ZodiacSign.fromLongitude(longitude)
        val navamsaSign = getNavamsaSign(longitude)
        return rasiSign == navamsaSign
    }

    private fun getNavamsaSign(longitude: Double): ZodiacSign {
        val navamsaLongitude = (longitude * 9) % 360
        return ZodiacSign.fromLongitude(navamsaLongitude)
    }

    /**
     * Determine which house a planet is in
     */
    private fun determineHouse(longitude: Double, houseCusps: DoubleArray): Int {
        val normalizedLongitude = (longitude % 360.0 + 360.0) % 360.0
        for (i in 1..12) {
            val cuspStart = houseCusps[i]
            val cuspEnd = if (i == 12) houseCusps[1] else houseCusps[i + 1]
            if (cuspEnd > cuspStart) {
                if (normalizedLongitude >= cuspStart && normalizedLongitude < cuspEnd) return i
            } else {
                if (normalizedLongitude >= cuspStart || normalizedLongitude < cuspEnd) return i
            }
        }
        return 1
    }

    /**
     * Calculate Julian Day with full precision
     * Ensures no rounding errors in time conversion
     */
    private fun calculateJulianDay(year: Int, month: Int, day: Int, hour: Int, minute: Int, second: Int): Double {
        val decimalHours = hour + (minute / 60.0) + (second / 3600.0)
        val sweDate = SweDate(year, month, day, decimalHours, SweDate.SE_GREG_CAL)
        return sweDate.julDay
    }

    /**
     * Clean up resources
     */
    fun close() {
        swissEph.swe_close()
    }
}
