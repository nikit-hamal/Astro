
package com.astro.storm.ui.chart

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import kotlin.math.abs

class ChartDataProcessor {

    companion object {
        private const val NAVAMSA_PART_DEGREES = 10.0 / 3.0
    }

    fun processPlanetPositions(
        planetPositions: List<PlanetPosition>,
        d1Chart: VedicChart
    ): List<PlanetRenderData> {
        val sunPosition = planetPositions.find { it.planet == Planet.SUN }
        val d1SunPosition = d1Chart.planetPositions.find { it.planet == Planet.SUN }

        return planetPositions.map { planetPosition ->
            val isExalted = isExalted(planetPosition.planet, planetPosition.sign)
            val isDebilitated = isDebilitated(planetPosition.planet, planetPosition.sign)
            val isCombust = isCombust(planetPosition, sunPosition)
            val isVargottama = isVargottama(planetPosition, d1Chart)

            PlanetRenderData(
                planet = planetPosition.planet,
                longitude = planetPosition.longitude,
                house = planetPosition.house,
                isRetrograde = planetPosition.isRetrograde,
                isExalted = isExalted,
                isDebilitated = isDebilitated,
                isCombust = isCombust,
                isVargottama = isVargottama,
                sign = planetPosition.sign
            )
        }
    }

    private fun isExalted(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.ARIES
            Planet.MOON -> sign == ZodiacSign.TAURUS
            Planet.MARS -> sign == ZodiacSign.CAPRICORN
            Planet.MERCURY -> sign == ZodiacSign.VIRGO
            Planet.JUPITER -> sign == ZodiacSign.CANCER
            Planet.VENUS -> sign == ZodiacSign.PISCES
            Planet.SATURN -> sign == ZodiacSign.LIBRA
            Planet.RAHU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            Planet.KETU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            else -> false
        }
    }

    private fun isDebilitated(planet: Planet, sign: ZodiacSign): Boolean {
        return when (planet) {
            Planet.SUN -> sign == ZodiacSign.LIBRA
            Planet.MOON -> sign == ZodiacSign.SCORPIO
            Planet.MARS -> sign == ZodiacSign.CANCER
            Planet.MERCURY -> sign == ZodiacSign.PISCES
            Planet.JUPITER -> sign == ZodiacSign.CAPRICORN
            Planet.VENUS -> sign == ZodiacSign.VIRGO
            Planet.SATURN -> sign == ZodiacSign.ARIES
            Planet.RAHU -> sign == ZodiacSign.SCORPIO || sign == ZodiacSign.SAGITTARIUS
            Planet.KETU -> sign == ZodiacSign.TAURUS || sign == ZodiacSign.GEMINI
            else -> false
        }
    }

    private fun isCombust(planet: PlanetPosition, sunPosition: PlanetPosition?): Boolean {
        if (planet.planet == Planet.SUN) return false
        if (planet.planet in listOf(Planet.RAHU, Planet.KETU, Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO)) {
            return false
        }
        if (sunPosition == null) return false

        val angularDistance = calculateAngularDistance(planet.longitude, sunPosition.longitude)

        val combustionOrb = when (planet.planet) {
            Planet.MOON -> 12.0
            Planet.MARS -> 17.0
            Planet.MERCURY -> if (planet.isRetrograde) 12.0 else 14.0
            Planet.JUPITER -> 11.0
            Planet.VENUS -> if (planet.isRetrograde) 8.0 else 10.0
            Planet.SATURN -> 15.0
            else -> 0.0
        }

        return angularDistance <= combustionOrb
    }

    private fun isVargottama(planet: PlanetPosition, d1Chart: VedicChart): Boolean {
        val d1PlanetPosition = d1Chart.planetPositions.find { it.planet == planet.planet }
        if (d1PlanetPosition == null) return false

        val navamsaLongitude = calculateNavamsaLongitude(d1PlanetPosition.longitude)
        val navamsaSign = ZodiacSign.fromLongitude(navamsaLongitude)

        return d1PlanetPosition.sign == navamsaSign
    }

    private fun calculateNavamsaLongitude(longitude: Double): Double {
        val normalizedLong = ((longitude % 360.0) + 360.0) % 360.0
        val signNumber = (normalizedLong / 30.0).toInt()
        val degreeInSign = normalizedLong % 30.0

        val navamsaPart = (degreeInSign / NAVAMSA_PART_DEGREES).toInt().coerceIn(0, 8)

        val startingSignIndex = when (ZodiacSign.values()[signNumber].quality) {
            ZodiacSign.Quality.CARDINAL -> signNumber
            ZodiacSign.Quality.FIXED -> (signNumber + 8) % 12
            ZodiacSign.Quality.MUTABLE -> (signNumber + 4) % 12
        }

        val navamsaSignIndex = (startingSignIndex + navamsaPart) % 12

        val positionInNavamsa = degreeInSign % NAVAMSA_PART_DEGREES
        val navamsaDegree = (positionInNavamsa / NAVAMSA_PART_DEGREES) * 30.0

        return (navamsaSignIndex * 30.0) + navamsaDegree
    }

    private fun calculateAngularDistance(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }
}
