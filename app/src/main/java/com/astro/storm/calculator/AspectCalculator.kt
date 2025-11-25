package com.astro.storm.calculator

import com.astro.storm.data.model.*
import kotlin.math.abs
import kotlin.math.min

/**
 * High-precision Aspect Calculator
 * Calculates angular separations and identifies yogas with configurable orbs
 */
class AspectCalculator {

    /**
     * Default orb configuration per planet class
     */
    data class OrbConfiguration(
        val luminariesOrb: Double = 10.0,      // Sun, Moon
        val beneficsOrb: Double = 7.0,         // Jupiter, Venus, Mercury
        val maleficsOrb: Double = 8.0,         // Mars, Saturn
        val nodesOrb: Double = 5.0,            // Rahu, Ketu
        val conjunctionOrb: Double = 8.0,
        val oppositionOrb: Double = 8.0,
        val trineOrb: Double = 6.0,
        val squareOrb: Double = 6.0,
        val sextileOrb: Double = 5.0
    )

    private val defaultOrbConfig = OrbConfiguration()

    /**
     * Calculate all aspects in a chart
     */
    fun calculateAllAspects(
        planetPositions: List<PlanetPosition>,
        orbConfig: OrbConfiguration = defaultOrbConfig
    ): List<Aspect> {
        val aspects = mutableListOf<Aspect>()

        // Calculate aspects between all planet pairs
        for (i in planetPositions.indices) {
            for (j in i + 1 until planetPositions.size) {
                val planet1 = planetPositions[i]
                val planet2 = planetPositions[j]

                val aspect = calculateAspect(planet1, planet2, orbConfig)
                if (aspect != null) {
                    aspects.add(aspect)
                }
            }
        }

        return aspects.sortedBy { it.orb }
    }

    /**
     * Calculate aspect between two planets
     */
    fun calculateAspect(
        planet1: PlanetPosition,
        planet2: PlanetPosition,
        orbConfig: OrbConfiguration = defaultOrbConfig
    ): Aspect? {
        // Calculate angular separation
        val separation = calculateAngularSeparation(planet1.longitude, planet2.longitude)

        // Get applicable orb for this planet pair
        val maxOrb = getMaxOrb(planet1.planet, planet2.planet, orbConfig)

        // Check each aspect type
        val aspectType = when {
            isWithinOrb(separation, 0.0, orbConfig.conjunctionOrb, maxOrb) ->
                AspectType.CONJUNCTION

            isWithinOrb(separation, 60.0, orbConfig.sextileOrb, maxOrb) ->
                AspectType.SEXTILE

            isWithinOrb(separation, 90.0, orbConfig.squareOrb, maxOrb) ->
                AspectType.SQUARE

            isWithinOrb(separation, 120.0, orbConfig.trineOrb, maxOrb) ->
                AspectType.TRINE

            isWithinOrb(separation, 180.0, orbConfig.oppositionOrb, maxOrb) ->
                AspectType.OPPOSITION

            else -> null
        }

        return if (aspectType != null) {
            val exactAngle = getExactAngle(aspectType)
            val orb = abs(separation - exactAngle).coerceAtMost(abs((360.0 - separation) - exactAngle))

            Aspect(
                planet1 = planet1.planet,
                planet2 = planet2.planet,
                type = aspectType,
                orb = orb,
                separation = separation,
                isApplying = isApplying(planet1, planet2)
            )
        } else null
    }

    /**
     * Calculate precise angular separation between two longitudes
     */
    private fun calculateAngularSeparation(lon1: Double, lon2: Double): Double {
        val diff = abs(lon1 - lon2)
        return min(diff, 360.0 - diff)
    }

    /**
     * Check if separation is within orb
     */
    private fun isWithinOrb(
        separation: Double,
        exactAngle: Double,
        aspectOrb: Double,
        maxOrb: Double
    ): Boolean {
        val effectiveOrb = min(aspectOrb, maxOrb)
        val diff = abs(separation - exactAngle)
        return diff <= effectiveOrb
    }

    /**
     * Get maximum orb for a planet pair
     */
    private fun getMaxOrb(planet1: Planet, planet2: Planet, config: OrbConfiguration): Double {
        val orb1 = getPlanetOrb(planet1, config)
        val orb2 = getPlanetOrb(planet2, config)
        return (orb1 + orb2) / 2.0
    }

    /**
     * Get orb for a specific planet
     */
    private fun getPlanetOrb(planet: Planet, config: OrbConfiguration): Double {
        return when (planet) {
            Planet.SUN, Planet.MOON -> config.luminariesOrb
            Planet.JUPITER, Planet.VENUS -> config.beneficsOrb
            Planet.MERCURY -> config.beneficsOrb
            Planet.MARS, Planet.SATURN -> config.maleficsOrb
            Planet.RAHU, Planet.KETU -> config.nodesOrb
        }
    }

    /**
     * Get exact angle for aspect type
     */
    private fun getExactAngle(aspectType: AspectType): Double {
        return when (aspectType) {
            AspectType.CONJUNCTION -> 0.0
            AspectType.SEXTILE -> 60.0
            AspectType.SQUARE -> 90.0
            AspectType.TRINE -> 120.0
            AspectType.OPPOSITION -> 180.0
        }
    }

    /**
     * Determine if aspect is applying (planets moving toward exact aspect)
     */
    private fun isApplying(planet1: PlanetPosition, planet2: PlanetPosition): Boolean {
        // If either planet is retrograde, calculation is more complex
        if (planet1.isRetrograde || planet2.isRetrograde) {
            // With retrograde motion, we check if relative speed brings them closer
            val relativeSpeed = planet1.speed - planet2.speed
            return when {
                planet1.longitude < planet2.longitude -> relativeSpeed > 0
                else -> relativeSpeed < 0
            }
        }

        // For direct motion, faster planet approaching slower one
        return if (planet1.speed > planet2.speed) {
            planet1.longitude < planet2.longitude
        } else {
            planet2.longitude < planet1.longitude
        }
    }

    /**
     * Calculate aspect matrix for all planets
     */
    fun calculateAspectMatrix(
        planetPositions: List<PlanetPosition>,
        orbConfig: OrbConfiguration = defaultOrbConfig
    ): AspectMatrix {
        val matrix = mutableMapOf<Pair<Planet, Planet>, Aspect?>()

        for (i in planetPositions.indices) {
            for (j in i + 1 until planetPositions.size) {
                val planet1 = planetPositions[i]
                val planet2 = planetPositions[j]

                val aspect = calculateAspect(planet1, planet2, orbConfig)
                matrix[planet1.planet to planet2.planet] = aspect
            }
        }

        return AspectMatrix(matrix)
    }

    /**
     * Identify yogas (special planetary combinations)
     */
    fun identifyYogas(
        planetPositions: List<PlanetPosition>,
        aspects: List<Aspect>
    ): List<Yoga> {
        val yogas = mutableListOf<Yoga>()

        // Raja Yoga: Jupiter and Venus in conjunction or mutual aspect
        val jupiterVenusAspect = aspects.find {
            (it.planet1 == Planet.JUPITER && it.planet2 == Planet.VENUS) ||
            (it.planet1 == Planet.VENUS && it.planet2 == Planet.JUPITER)
        }
        if (jupiterVenusAspect != null) {
            yogas.add(
                Yoga(
                    name = "Raja Yoga",
                    description = "Jupiter and Venus in ${jupiterVenusAspect.type.displayName}",
                    type = YogaType.BENEFICIAL,
                    strength = calculateYogaStrength(jupiterVenusAspect),
                    planets = listOf(Planet.JUPITER, Planet.VENUS)
                )
            )
        }

        // Dhana Yoga: Lords of wealth houses in favorable aspects
        // Check Sun-Moon aspects
        val sunMoonAspect = aspects.find {
            (it.planet1 == Planet.SUN && it.planet2 == Planet.MOON) ||
            (it.planet1 == Planet.MOON && it.planet2 == Planet.SUN)
        }
        if (sunMoonAspect != null) {
            yogas.add(
                Yoga(
                    name = "Luminaries Yoga",
                    description = "Sun and Moon in ${sunMoonAspect.type.displayName}",
                    type = if (sunMoonAspect.type in listOf(AspectType.TRINE, AspectType.SEXTILE))
                        YogaType.BENEFICIAL else YogaType.MIXED,
                    strength = calculateYogaStrength(sunMoonAspect),
                    planets = listOf(Planet.SUN, Planet.MOON)
                )
            )
        }

        // Mars-Saturn aspects (often challenging)
        val marsSaturnAspect = aspects.find {
            (it.planet1 == Planet.MARS && it.planet2 == Planet.SATURN) ||
            (it.planet1 == Planet.SATURN && it.planet2 == Planet.MARS)
        }
        if (marsSaturnAspect != null) {
            yogas.add(
                Yoga(
                    name = "Mars-Saturn Yoga",
                    description = "Mars and Saturn in ${marsSaturnAspect.type.displayName}",
                    type = if (marsSaturnAspect.type == AspectType.TRINE)
                        YogaType.MIXED else YogaType.CHALLENGING,
                    strength = calculateYogaStrength(marsSaturnAspect),
                    planets = listOf(Planet.MARS, Planet.SATURN)
                )
            )
        }

        // Budha-Aditya Yoga: Sun and Mercury conjunction
        val sunMercuryAspect = aspects.find {
            ((it.planet1 == Planet.SUN && it.planet2 == Planet.MERCURY) ||
            (it.planet1 == Planet.MERCURY && it.planet2 == Planet.SUN)) &&
            it.type == AspectType.CONJUNCTION
        }
        if (sunMercuryAspect != null) {
            yogas.add(
                Yoga(
                    name = "Budha-Aditya Yoga",
                    description = "Sun and Mercury in conjunction (enhances intellect)",
                    type = YogaType.BENEFICIAL,
                    strength = calculateYogaStrength(sunMercuryAspect),
                    planets = listOf(Planet.SUN, Planet.MERCURY)
                )
            )
        }

        // Chandra-Mangala Yoga: Moon and Mars conjunction
        val moonMarsAspect = aspects.find {
            ((it.planet1 == Planet.MOON && it.planet2 == Planet.MARS) ||
            (it.planet1 == Planet.MARS && it.planet2 == Planet.MOON)) &&
            it.type == AspectType.CONJUNCTION
        }
        if (moonMarsAspect != null) {
            yogas.add(
                Yoga(
                    name = "Chandra-Mangala Yoga",
                    description = "Moon and Mars in conjunction (wealth and property)",
                    type = YogaType.BENEFICIAL,
                    strength = calculateYogaStrength(moonMarsAspect),
                    planets = listOf(Planet.MOON, Planet.MARS)
                )
            )
        }

        return yogas
    }

    /**
     * Calculate yoga strength based on aspect orb
     */
    private fun calculateYogaStrength(aspect: Aspect): Double {
        // Tighter orb = stronger yoga
        // Maximum strength (100%) at 0° orb, decreasing to 0% at max orb
        val maxOrb = 10.0
        return ((maxOrb - aspect.orb) / maxOrb * 100.0).coerceIn(0.0, 100.0)
    }

    /**
     * Get all aspects for a specific planet
     */
    fun getAspectsForPlanet(
        planet: Planet,
        aspects: List<Aspect>
    ): List<Aspect> {
        return aspects.filter { it.planet1 == planet || it.planet2 == planet }
    }

    /**
     * Get strongest aspects in chart
     */
    fun getStrongestAspects(
        aspects: List<Aspect>,
        count: Int = 5
    ): List<Aspect> {
        return aspects.sortedBy { it.orb }.take(count)
    }
}

/**
 * Aspect Matrix - stores aspects between all planet pairs
 */
data class AspectMatrix(
    private val matrix: Map<Pair<Planet, Planet>, Aspect?>
) {
    /**
     * Get aspect between two planets
     */
    fun getAspect(planet1: Planet, planet2: Planet): Aspect? {
        return matrix[planet1 to planet2] ?: matrix[planet2 to planet1]
    }

    /**
     * Get all aspects
     */
    fun getAllAspects(): List<Aspect> {
        return matrix.values.filterNotNull()
    }

    /**
     * Get aspects by type
     */
    fun getAspectsByType(type: AspectType): List<Aspect> {
        return matrix.values.filterNotNull().filter { it.type == type }
    }

    /**
     * Format as string table
     */
    fun toFormattedString(): String {
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("          ASPECT MATRIX")
            appendLine("═══════════════════════════════════════")
            appendLine()

            val planets = Planet.MAIN_PLANETS
            planets.forEachIndexed { i, planet1 ->
                planets.drop(i + 1).forEach { planet2 ->
                    val aspect = getAspect(planet1, planet2)
                    if (aspect != null) {
                        appendLine(
                            "${planet1.symbol} ${aspect.type.symbol} ${planet2.symbol} " +
                            "(orb: ${String.format("%.2f", aspect.orb)}°) " +
                            if (aspect.isApplying) "[Applying]" else "[Separating]"
                        )
                    }
                }
            }
        }
    }
}

/**
 * Yoga (special planetary combination)
 */
data class Yoga(
    val name: String,
    val description: String,
    val type: YogaType,
    val strength: Double,
    val planets: List<Planet>
)

/**
 * Yoga Type
 */
enum class YogaType {
    BENEFICIAL,
    CHALLENGING,
    MIXED
}
