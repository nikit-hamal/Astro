package com.astro.storm.data.model

import kotlin.math.abs

/**
 * Aspect Types in Vedic Astrology
 */
enum class AspectType(
    val angle: Double,
    val orb: Double,
    val displayName: String,
    val strength: AspectStrength,
    val nature: String
) {
    // Major Aspects
    CONJUNCTION(0.0, 8.0, "Conjunction", AspectStrength.VERY_STRONG, "Neutral"),
    OPPOSITION(180.0, 8.0, "Opposition", AspectStrength.VERY_STRONG, "Challenging"),
    TRINE(120.0, 8.0, "Trine", AspectStrength.STRONG, "Harmonious"),
    SQUARE(90.0, 7.0, "Square", AspectStrength.STRONG, "Challenging"),
    SEXTILE(60.0, 6.0, "Sextile", AspectStrength.MODERATE, "Harmonious"),

    // Minor Aspects
    SEMISEXTILE(30.0, 3.0, "Semi-Sextile", AspectStrength.WEAK, "Neutral"),
    QUINCUNX(150.0, 3.0, "Quincunx", AspectStrength.WEAK, "Adjusting"),
    SEMISQUARE(45.0, 2.0, "Semi-Square", AspectStrength.WEAK, "Challenging"),
    SESQUIQUADRATE(135.0, 2.0, "Sesquiquadrate", AspectStrength.WEAK, "Challenging");

    /**
     * Check if a given angle matches this aspect within orb
     */
    fun matches(actualAngle: Double, customOrb: Double? = null): Boolean {
        val orbToUse = customOrb ?: orb
        val diff = abs(actualAngle - angle)
        return diff <= orbToUse || diff >= (360.0 - orbToUse)
    }
}

/**
 * Aspect Strength Classification
 */
enum class AspectStrength(val multiplier: Double, val displayName: String) {
    VERY_STRONG(1.0, "Very Strong"),
    STRONG(0.75, "Strong"),
    MODERATE(0.5, "Moderate"),
    WEAK(0.25, "Weak");
}

/**
 * Vedic Special Aspects (Graha Drishti)
 * Planets have special aspects beyond the 7th house aspect
 */
enum class VedicSpecialAspect(
    val planet: Planet,
    val aspectHouses: List<Int>, // Houses aspect from planet's position
    val strength: Double,
    val description: String
) {
    MARS_ASPECTS(
        Planet.MARS,
        listOf(4, 7, 8),
        1.0,
        "Mars aspects 4th, 7th, and 8th houses from its position"
    ),
    JUPITER_ASPECTS(
        Planet.JUPITER,
        listOf(5, 7, 9),
        1.0,
        "Jupiter aspects 5th, 7th, and 9th houses from its position"
    ),
    SATURN_ASPECTS(
        Planet.SATURN,
        listOf(3, 7, 10),
        1.0,
        "Saturn aspects 3rd, 7th, and 10th houses from its position"
    ),
    RAHU_KETU_ASPECTS(
        Planet.RAHU, // Also applies to Ketu
        listOf(5, 7, 9),
        0.75,
        "Rahu and Ketu aspect 5th, 7th, and 9th houses (some traditions)"
    );

    companion object {
        /**
         * Get special aspects for a planet
         */
        fun getAspectsForPlanet(planet: Planet): VedicSpecialAspect? {
            return when (planet) {
                Planet.MARS -> MARS_ASPECTS
                Planet.JUPITER -> JUPITER_ASPECTS
                Planet.SATURN -> SATURN_ASPECTS
                Planet.RAHU, Planet.KETU -> RAHU_KETU_ASPECTS
                else -> null
            }
        }
    }
}

/**
 * Planetary Aspect Data
 * Represents an aspect between two planets
 */
data class PlanetaryAspect(
    val planet1: Planet,
    val planet2: Planet,
    val aspectType: AspectType,
    val angularSeparation: Double,
    val orbStrength: Double, // 0.0 to 1.0 (1.0 = exact aspect)
    val isApplying: Boolean, // true if aspect is becoming more exact
    val effectiveStrength: Double // Combined strength considering orb and aspect type
) {
    /**
     * Check if this aspect is a yoga (beneficial combination)
     */
    fun isYoga(): Boolean {
        return aspectType in listOf(AspectType.TRINE, AspectType.SEXTILE, AspectType.CONJUNCTION) &&
                orbStrength > 0.7
    }

    /**
     * Check if this aspect is a dosha (challenging combination)
     */
    fun isDosha(): Boolean {
        return aspectType in listOf(AspectType.SQUARE, AspectType.OPPOSITION) &&
                orbStrength > 0.7
    }

    /**
     * Get formatted aspect description
     */
    fun getDescription(): String {
        val strengthDesc = when {
            effectiveStrength > 0.8 -> "Very Strong"
            effectiveStrength > 0.6 -> "Strong"
            effectiveStrength > 0.4 -> "Moderate"
            else -> "Weak"
        }

        val applyingDesc = if (isApplying) "Applying" else "Separating"

        return "${planet1.symbol} ${aspectType.displayName} ${planet2.symbol} " +
                "(${String.format("%.2f", angularSeparation)}° | $strengthDesc | $applyingDesc)"
    }

    /**
     * Format as string
     */
    override fun toString(): String {
        return getDescription()
    }
}

/**
 * Aspect Matrix
 * Contains all aspects in a chart
 */
data class AspectMatrix(
    val aspects: List<PlanetaryAspect>,
    val vedicAspects: List<VedicHouseAspect>,
    val configuration: AspectConfiguration = AspectConfiguration()
) {
    /**
     * Get all aspects for a specific planet
     */
    fun getAspectsForPlanet(planet: Planet): List<PlanetaryAspect> {
        return aspects.filter { it.planet1 == planet || it.planet2 == planet }
    }

    /**
     * Get major aspects only
     */
    fun getMajorAspects(): List<PlanetaryAspect> {
        return aspects.filter {
            it.aspectType in listOf(
                AspectType.CONJUNCTION,
                AspectType.OPPOSITION,
                AspectType.TRINE,
                AspectType.SQUARE
            )
        }
    }

    /**
     * Get all yogas (beneficial combinations)
     */
    fun getYogas(): List<PlanetaryAspect> {
        return aspects.filter { it.isYoga() }
    }

    /**
     * Get all doshas (challenging combinations)
     */
    fun getDoshas(): List<PlanetaryAspect> {
        return aspects.filter { it.isDosha() }
    }

    /**
     * Format matrix as string
     */
    fun toFormattedString(): String {
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("         ASPECT MATRIX")
            appendLine("═══════════════════════════════════════")
            appendLine()

            appendLine("MAJOR ASPECTS:")
            appendLine("─────────────────────────────────────")
            getMajorAspects().forEach { aspect ->
                appendLine(aspect.getDescription())
            }

            appendLine()
            appendLine("YOGAS (Beneficial Combinations):")
            appendLine("─────────────────────────────────────")
            val yogas = getYogas()
            if (yogas.isEmpty()) {
                appendLine("No strong yogas found")
            } else {
                yogas.forEach { yoga ->
                    appendLine(yoga.getDescription())
                }
            }

            appendLine()
            appendLine("DOSHAS (Challenging Combinations):")
            appendLine("─────────────────────────────────────")
            val doshas = getDoshas()
            if (doshas.isEmpty()) {
                appendLine("No strong doshas found")
            } else {
                doshas.forEach { dosha ->
                    appendLine(dosha.getDescription())
                }
            }

            appendLine()
            appendLine("VEDIC HOUSE ASPECTS (Graha Drishti):")
            appendLine("─────────────────────────────────────")
            vedicAspects.forEach { aspect ->
                appendLine("${aspect.planet.symbol} in House ${aspect.fromHouse} " +
                        "aspects House ${aspect.toHouse} (${aspect.aspectType})")
            }
        }
    }
}

/**
 * Vedic House Aspect
 * Represents planet aspecting a house
 */
data class VedicHouseAspect(
    val planet: Planet,
    val fromHouse: Int,
    val toHouse: Int,
    val aspectType: String, // "7th aspect", "Special aspect", etc.
    val strength: Double
)

/**
 * Aspect Configuration
 * Settings for aspect calculation
 */
data class AspectConfiguration(
    val orbMultiplier: Double = 1.0, // Adjust orbs (1.0 = default)
    val includeminorAspects: Boolean = false, // Include minor aspects
    val customOrbs: Map<AspectType, Double> = emptyMap() // Custom orbs per aspect
) {
    /**
     * Get orb for an aspect type
     */
    fun getOrb(aspectType: AspectType): Double {
        return customOrbs[aspectType] ?: (aspectType.orb * orbMultiplier)
    }
}

/**
 * Aspect Calculator Helper Functions
 */
object AspectCalculator {
    /**
     * Calculate angular separation between two longitudes
     */
    fun calculateAngularSeparation(longitude1: Double, longitude2: Double): Double {
        var diff = abs(longitude1 - longitude2)
        if (diff > 180.0) {
            diff = 360.0 - diff
        }
        return diff
    }

    /**
     * Calculate orb strength (how close to exact)
     * Returns 1.0 for exact, 0.0 at orb limit
     */
    fun calculateOrbStrength(actualAngle: Double, aspectAngle: Double, orb: Double): Double {
        val diff = abs(actualAngle - aspectAngle)
        val normalizedDiff = if (diff > 180.0) 360.0 - diff else diff
        return (1.0 - (normalizedDiff / orb)).coerceIn(0.0, 1.0)
    }

    /**
     * Calculate effective strength combining aspect type and orb
     */
    fun calculateEffectiveStrength(aspectType: AspectType, orbStrength: Double): Double {
        return aspectType.strength.multiplier * orbStrength
    }
}
