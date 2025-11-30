package com.astro.storm.ephemeris

import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import kotlin.math.abs
import kotlin.math.min

/**
 * Precise Vedic Aspect (Drishti) Calculator
 *
 * In Vedic astrology, aspects work differently than Western astrology:
 * - All planets aspect the 7th house (180°) from their position - FULL ASPECT
 * - Mars additionally aspects 4th (90°) and 8th (210°) houses - SPECIAL ASPECTS
 * - Jupiter additionally aspects 5th (120°) and 9th (240°) houses - SPECIAL ASPECTS
 * - Saturn additionally aspects 3rd (60°) and 10th (270°) houses - SPECIAL ASPECTS
 * - Rahu/Ketu aspect 5th, 7th, and 9th houses (like Jupiter + 7th)
 *
 * This calculator provides:
 * - Precise angular separation calculations
 * - Configurable orbs per planet class
 * - Yoga detection (conjunction, opposition, trine, square, sextile)
 * - Aspect strength calculations based on orb tightness
 */
object AspectCalculator {

    /**
     * Aspect types in Vedic astrology
     */
    enum class AspectType(
        val displayName: String,
        val angle: Double,
        val nature: AspectNature,
        val symbol: String
    ) {
        CONJUNCTION("Conjunction", 0.0, AspectNature.VARIABLE, "☌"),
        OPPOSITION("Opposition", 180.0, AspectNature.CHALLENGING, "☍"),
        TRINE("Trine", 120.0, AspectNature.HARMONIOUS, "△"),
        SQUARE("Square", 90.0, AspectNature.CHALLENGING, "□"),
        SEXTILE("Sextile", 60.0, AspectNature.HARMONIOUS, "⚹"),

        // Vedic special aspects
        MARS_4TH("Mars 4th Aspect", 90.0, AspectNature.CHALLENGING, "♂4"),
        MARS_8TH("Mars 8th Aspect", 210.0, AspectNature.CHALLENGING, "♂8"),
        JUPITER_5TH("Jupiter 5th Aspect", 120.0, AspectNature.HARMONIOUS, "♃5"),
        JUPITER_9TH("Jupiter 9th Aspect", 240.0, AspectNature.HARMONIOUS, "♃9"),
        SATURN_3RD("Saturn 3rd Aspect", 60.0, AspectNature.CHALLENGING, "♄3"),
        SATURN_10TH("Saturn 10th Aspect", 270.0, AspectNature.CHALLENGING, "♄10"),

        // Rahu/Ketu special aspects
        RAHU_KETU_5TH("Rahu/Ketu 5th Aspect", 120.0, AspectNature.SIGNIFICANT, "☊☋5"),
        RAHU_KETU_9TH("Rahu/Ketu 9th Aspect", 240.0, AspectNature.SIGNIFICANT, "☊☋9"),

        // 7th house aspect (all planets)
        FULL_ASPECT("7th House Aspect", 180.0, AspectNature.SIGNIFICANT, "⦻");

        companion object {
            fun fromAngle(angle: Double, orb: Double = 8.0): AspectType? {
                val normalizedAngle = normalizeAngle(angle)
                return entries.firstOrNull { aspect ->
                    val diff = abs(normalizedAngle - aspect.angle)
                    diff <= orb || abs(360.0 - diff) <= orb
                }
            }
        }
    }

    enum class AspectNature(val displayName: String) {
        HARMONIOUS("Harmonious"),
        CHALLENGING("Challenging"),
        VARIABLE("Variable"),
        SIGNIFICANT("Significant")
    }

    /**
     * Planet classification for orb settings
     */
    enum class PlanetClass(val defaultOrb: Double) {
        LUMINARY(10.0),        // Sun, Moon - wider orbs
        PERSONAL(8.0),         // Mercury, Venus, Mars
        SOCIAL(7.0),           // Jupiter, Saturn
        TRANSCENDENTAL(6.0),   // Rahu, Ketu (mean nodes)
        OUTER(5.0)             // Uranus, Neptune, Pluto (if used)
    }

    /**
     * Configurable orb settings
     */
    data class OrbConfiguration(
        val luminaryOrb: Double = 10.0,
        val personalOrb: Double = 8.0,
        val socialOrb: Double = 7.0,
        val transcendentalOrb: Double = 6.0,
        val outerOrb: Double = 5.0,
        val conjunctionBonus: Double = 2.0,  // Extra orb for conjunctions
        val oppositionBonus: Double = 1.0    // Extra orb for oppositions
    ) {
        fun getOrbForPlanet(planet: Planet): Double {
            return when (planet) {
                Planet.SUN, Planet.MOON -> luminaryOrb
                Planet.MERCURY, Planet.VENUS, Planet.MARS -> personalOrb
                Planet.JUPITER, Planet.SATURN -> socialOrb
                Planet.RAHU, Planet.KETU -> transcendentalOrb
                Planet.URANUS, Planet.NEPTUNE, Planet.PLUTO -> outerOrb
            }
        }

        fun getEffectiveOrb(planet1: Planet, planet2: Planet, aspectType: AspectType): Double {
            val baseOrb = (getOrbForPlanet(planet1) + getOrbForPlanet(planet2)) / 2.0
            return when (aspectType) {
                AspectType.CONJUNCTION -> baseOrb + conjunctionBonus
                AspectType.OPPOSITION, AspectType.FULL_ASPECT -> baseOrb + oppositionBonus
                else -> baseOrb
            }
        }
    }

    /**
     * Result of an aspect calculation
     */
    data class AspectData(
        val planet1: Planet,
        val planet2: Planet,
        val aspectType: AspectType,
        val exactAngle: Double,           // Precise angular separation
        val orb: Double,                  // How far from exact
        val isApplying: Boolean,          // Is the aspect becoming exact?
        val strength: Double,             // 0.0 - 1.0 based on orb tightness
        val isVedicSpecialAspect: Boolean // Is this a Mars/Jupiter/Saturn special aspect?
    ) {
        val strengthDescription: String
            get() = when {
                strength >= 0.9 -> "Exact"
                strength >= 0.7 -> "Very Strong"
                strength >= 0.5 -> "Strong"
                strength >= 0.3 -> "Moderate"
                else -> "Weak"
            }
    }

    /**
     * Complete aspect matrix for a chart
     */
    data class AspectMatrix(
        val aspects: List<AspectData>,
        val conjunctions: List<AspectData>,
        val oppositions: List<AspectData>,
        val trines: List<AspectData>,
        val squares: List<AspectData>,
        val sextiles: List<AspectData>,
        val vedicSpecialAspects: List<AspectData>
    ) {
        val totalAspectCount: Int get() = aspects.size

        fun getAspectsForPlanet(planet: Planet): List<AspectData> {
            return aspects.filter { it.planet1 == planet || it.planet2 == planet }
        }

        fun getAspectBetween(planet1: Planet, planet2: Planet): AspectData? {
            return aspects.find {
                (it.planet1 == planet1 && it.planet2 == planet2) ||
                        (it.planet1 == planet2 && it.planet2 == planet1)
            }
        }
    }

    /**
     * Calculate complete aspect matrix for a Vedic chart
     */
    fun calculateAspectMatrix(
        chart: VedicChart,
        orbConfig: OrbConfiguration = OrbConfiguration()
    ): AspectMatrix {
        val allAspects = mutableListOf<AspectData>()
        val positions = chart.planetPositions

        // Calculate aspects between each pair of planets
        for (i in positions.indices) {
            for (j in i + 1 until positions.size) {
                val planet1 = positions[i]
                val planet2 = positions[j]

                // Calculate all possible aspects between these two planets
                val aspects = calculateAspectsBetween(planet1, planet2, orbConfig)
                allAspects.addAll(aspects)
            }
        }

        // Calculate Vedic special aspects (one-way aspects)
        val vedicSpecialAspects = calculateVedicSpecialAspects(positions, orbConfig)
        allAspects.addAll(vedicSpecialAspects)

        // Sort by strength (strongest first)
        val sortedAspects = allAspects.sortedByDescending { it.strength }

        return AspectMatrix(
            aspects = sortedAspects,
            conjunctions = sortedAspects.filter { it.aspectType == AspectType.CONJUNCTION },
            oppositions = sortedAspects.filter {
                it.aspectType == AspectType.OPPOSITION || it.aspectType == AspectType.FULL_ASPECT
            },
            trines = sortedAspects.filter {
                it.aspectType == AspectType.TRINE ||
                        it.aspectType == AspectType.JUPITER_5TH ||
                        it.aspectType == AspectType.JUPITER_9TH
            },
            squares = sortedAspects.filter {
                it.aspectType == AspectType.SQUARE || it.aspectType == AspectType.MARS_4TH
            },
            sextiles = sortedAspects.filter {
                it.aspectType == AspectType.SEXTILE || it.aspectType == AspectType.SATURN_3RD
            },
            vedicSpecialAspects = sortedAspects.filter { it.isVedicSpecialAspect }
        )
    }

    /**
     * Calculate standard Western-style aspects between two planets
     */
    private fun calculateAspectsBetween(
        pos1: PlanetPosition,
        pos2: PlanetPosition,
        orbConfig: OrbConfiguration
    ): List<AspectData> {
        val aspects = mutableListOf<AspectData>()
        val angularSeparation = calculateAngularSeparation(pos1.longitude, pos2.longitude)

        // Check each standard aspect type
        val standardAspects = listOf(
            AspectType.CONJUNCTION,
            AspectType.OPPOSITION,
            AspectType.TRINE,
            AspectType.SQUARE,
            AspectType.SEXTILE
        )

        for (aspectType in standardAspects) {
            val effectiveOrb = orbConfig.getEffectiveOrb(pos1.planet, pos2.planet, aspectType)
            val orb = calculateOrb(angularSeparation, aspectType.angle)

            if (orb <= effectiveOrb) {
                val strength = calculateStrength(orb, effectiveOrb)
                val isApplying = isAspectApplying(pos1, pos2, aspectType.angle)

                aspects.add(
                    AspectData(
                        planet1 = pos1.planet,
                        planet2 = pos2.planet,
                        aspectType = aspectType,
                        exactAngle = angularSeparation,
                        orb = orb,
                        isApplying = isApplying,
                        strength = strength,
                        isVedicSpecialAspect = false
                    )
                )
            }
        }

        return aspects
    }

    /**
     * Calculate Vedic special aspects (Drishti)
     *
     * In Vedic astrology:
     * - Mars aspects 4th and 8th houses from its position
     * - Jupiter aspects 5th and 9th houses
     * - Saturn aspects 3rd and 10th houses
     * - Rahu/Ketu aspect 5th, 7th, 9th houses
     */
    private fun calculateVedicSpecialAspects(
        positions: List<PlanetPosition>,
        orbConfig: OrbConfiguration
    ): List<AspectData> {
        val aspects = mutableListOf<AspectData>()

        for (aspectingPlanet in positions) {
            val specialAspectAngles = getSpecialAspectAngles(aspectingPlanet.planet)

            for ((angle, aspectType) in specialAspectAngles) {
                // Find planets that receive this special aspect
                for (receivingPlanet in positions) {
                    if (receivingPlanet.planet == aspectingPlanet.planet) continue

                    val angularSeparation = calculateAngularSeparation(
                        aspectingPlanet.longitude,
                        receivingPlanet.longitude
                    )

                    val effectiveOrb = orbConfig.getEffectiveOrb(
                        aspectingPlanet.planet,
                        receivingPlanet.planet,
                        aspectType
                    )
                    val orb = calculateOrb(angularSeparation, angle)

                    if (orb <= effectiveOrb) {
                        val strength = calculateStrength(orb, effectiveOrb)
                        val isApplying = isAspectApplying(aspectingPlanet, receivingPlanet, angle)

                        aspects.add(
                            AspectData(
                                planet1 = aspectingPlanet.planet,
                                planet2 = receivingPlanet.planet,
                                aspectType = aspectType,
                                exactAngle = angularSeparation,
                                orb = orb,
                                isApplying = isApplying,
                                strength = strength,
                                isVedicSpecialAspect = true
                            )
                        )
                    }
                }
            }
        }

        return aspects
    }

    /**
     * Get special aspect angles for each planet
     */
    private fun getSpecialAspectAngles(planet: Planet): List<Pair<Double, AspectType>> {
        return when (planet) {
            Planet.MARS -> listOf(
                90.0 to AspectType.MARS_4TH,   // 4th house
                210.0 to AspectType.MARS_8TH   // 8th house
            )

            Planet.JUPITER -> listOf(
                120.0 to AspectType.JUPITER_5TH,  // 5th house
                240.0 to AspectType.JUPITER_9TH   // 9th house
            )

            Planet.SATURN -> listOf(
                60.0 to AspectType.SATURN_3RD,    // 3rd house
                270.0 to AspectType.SATURN_10TH   // 10th house
            )

            Planet.RAHU, Planet.KETU -> listOf(
                120.0 to AspectType.RAHU_KETU_5TH,
                240.0 to AspectType.RAHU_KETU_9TH
            )

            else -> emptyList()
        }
    }

    /**
     * Calculate angular separation between two longitudes
     */
    private fun calculateAngularSeparation(long1: Double, long2: Double): Double {
        val diff = abs(long1 - long2)
        return if (diff > 180.0) 360.0 - diff else diff
    }

    /**
     * Calculate orb (distance from exact aspect)
     */
    private fun calculateOrb(actualAngle: Double, aspectAngle: Double): Double {
        val diff = abs(actualAngle - aspectAngle)
        return min(diff, 360.0 - diff)
    }

    /**
     * Calculate aspect strength based on orb tightness
     * Returns value from 0.0 (at edge of orb) to 1.0 (exact)
     */
    private fun calculateStrength(orb: Double, maxOrb: Double): Double {
        if (orb >= maxOrb) return 0.0
        return 1.0 - (orb / maxOrb)
    }

    /**
     * Determine if aspect is applying (getting tighter) or separating
     */
    private fun isAspectApplying(
        pos1: PlanetPosition,
        pos2: PlanetPosition,
        aspectAngle: Double
    ): Boolean {
        // Use planetary speeds to determine if aspect is forming or separating
        // Positive speed = direct motion, Negative = retrograde

        val speed1 = pos1.speed
        val speed2 = pos2.speed

        // Calculate relative speed (how fast planets are moving toward/away from each other)
        val relativeSpeed = speed1 - speed2

        // Current angular distance
        val currentDistance = calculateAngularSeparation(pos1.longitude, pos2.longitude)

        // Predicted distance in 1 day
        val futureLong1 = normalizeAngle(pos1.longitude + speed1)
        val futureLong2 = normalizeAngle(pos2.longitude + speed2)
        val futureDistance = calculateAngularSeparation(futureLong1, futureLong2)

        // If future distance is closer to aspect angle, it's applying
        val currentOrb = calculateOrb(currentDistance, aspectAngle)
        val futureOrb = calculateOrb(futureDistance, aspectAngle)

        return futureOrb < currentOrb
    }

    /**
     * Normalize angle to 0-360 range
     */
    private fun normalizeAngle(angle: Double): Double {
        return ((angle % 360.0) + 360.0) % 360.0
    }

    /**
     * Detect specific Yogas (planetary combinations)
     */
    fun detectYogas(chart: VedicChart): List<Yoga> {
        val yogas = mutableListOf<Yoga>()
        val aspectMatrix = calculateAspectMatrix(chart)

        // Budha-Aditya Yoga: Sun-Mercury conjunction
        val sunMercuryConjunction = aspectMatrix.getAspectBetween(Planet.SUN, Planet.MERCURY)
        if (sunMercuryConjunction != null && sunMercuryConjunction.aspectType == AspectType.CONJUNCTION) {
            yogas.add(
                Yoga(
                    name = "Budha-Aditya Yoga",
                    planets = listOf(Planet.SUN, Planet.MERCURY),
                    description = "Intelligence, communication skills, sharp intellect",
                    strength = sunMercuryConjunction.strength,
                    isAuspicious = true
                )
            )
        }

        // Gaja-Kesari Yoga: Jupiter in Kendra from Moon
        val moonPos = chart.planetPositions.find { it.planet == Planet.MOON }
        val jupiterPos = chart.planetPositions.find { it.planet == Planet.JUPITER }
        if (moonPos != null && jupiterPos != null) {
            val angularDist = calculateAngularSeparation(moonPos.longitude, jupiterPos.longitude)
            // Check if Jupiter is in 1st, 4th, 7th, or 10th from Moon (Kendra positions)
            val isKendra = listOf(0.0, 90.0, 180.0, 270.0).any { kendraAngle ->
                calculateOrb(angularDist, kendraAngle) <= 15.0
            }
            if (isKendra) {
                yogas.add(
                    Yoga(
                        name = "Gaja-Kesari Yoga",
                        planets = listOf(Planet.MOON, Planet.JUPITER),
                        description = "Fame, wisdom, wealth, and noble character",
                        strength = 0.8,
                        isAuspicious = true
                    )
                )
            }
        }

        // Chandra-Mangala Yoga: Moon-Mars conjunction or mutual aspect
        val moonMarsAspect = aspectMatrix.getAspectBetween(Planet.MOON, Planet.MARS)
        if (moonMarsAspect != null && moonMarsAspect.aspectType == AspectType.CONJUNCTION) {
            yogas.add(
                Yoga(
                    name = "Chandra-Mangala Yoga",
                    planets = listOf(Planet.MOON, Planet.MARS),
                    description = "Wealth through enterprise, business acumen",
                    strength = moonMarsAspect.strength,
                    isAuspicious = true
                )
            )
        }

        // Guru-Chandal Yoga: Jupiter-Rahu conjunction
        val jupiterRahuAspect = aspectMatrix.getAspectBetween(Planet.JUPITER, Planet.RAHU)
        if (jupiterRahuAspect != null && jupiterRahuAspect.aspectType == AspectType.CONJUNCTION) {
            yogas.add(
                Yoga(
                    name = "Guru-Chandal Yoga",
                    planets = listOf(Planet.JUPITER, Planet.RAHU),
                    description = "Challenges to traditional wisdom, unconventional beliefs",
                    strength = jupiterRahuAspect.strength,
                    isAuspicious = false
                )
            )
        }

        // Shani-Rahu Conjunction (Shrapit Yoga indicators)
        val saturnRahuAspect = aspectMatrix.getAspectBetween(Planet.SATURN, Planet.RAHU)
        if (saturnRahuAspect != null && saturnRahuAspect.aspectType == AspectType.CONJUNCTION) {
            yogas.add(
                Yoga(
                    name = "Shani-Rahu Yoga",
                    planets = listOf(Planet.SATURN, Planet.RAHU),
                    description = "Karmic challenges, need for patience and discipline",
                    strength = saturnRahuAspect.strength,
                    isAuspicious = false
                )
            )
        }

        // Neecha Bhanga Raja Yoga detection (simplified)
        // When a debilitated planet gets cancellation

        // Venus-Jupiter conjunction (Lakshmi Yoga elements)
        val venusJupiterAspect = aspectMatrix.getAspectBetween(Planet.VENUS, Planet.JUPITER)
        if (venusJupiterAspect != null && venusJupiterAspect.aspectType == AspectType.CONJUNCTION) {
            yogas.add(
                Yoga(
                    name = "Venus-Jupiter Conjunction",
                    planets = listOf(Planet.VENUS, Planet.JUPITER),
                    description = "Prosperity, luxury, spiritual inclinations, marital happiness",
                    strength = venusJupiterAspect.strength,
                    isAuspicious = true
                )
            )
        }

        return yogas.sortedByDescending { it.strength }
    }

    /**
     * Yoga data class
     */
    data class Yoga(
        val name: String,
        val planets: List<Planet>,
        val description: String,
        val strength: Double,
        val isAuspicious: Boolean
    )
}
