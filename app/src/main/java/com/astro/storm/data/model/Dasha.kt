package com.astro.storm.data.model

import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * Vimshottari Dasha System
 * 120-year cycle planetary period system
 */

/**
 * Dasha Period Data
 * Represents a specific dasha/antardasha/pratyantardasha period
 */
data class DashaPeriod(
    val planet: Planet,
    val level: DashaLevel,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val durationYears: Double,
    val subPeriods: List<DashaPeriod> = emptyList(),
    val isActive: Boolean = false
) {
    /**
     * Get remaining time in this period
     */
    fun getRemainingTime(currentDate: LocalDateTime = LocalDateTime.now()): Long {
        return ChronoUnit.DAYS.between(currentDate, endDate)
    }

    /**
     * Get elapsed time in this period
     */
    fun getElapsedTime(currentDate: LocalDateTime = LocalDateTime.now()): Long {
        return ChronoUnit.DAYS.between(startDate, currentDate)
    }

    /**
     * Get total duration in days
     */
    fun getTotalDays(): Long {
        return ChronoUnit.DAYS.between(startDate, endDate)
    }

    /**
     * Get completion percentage
     */
    fun getCompletionPercentage(currentDate: LocalDateTime = LocalDateTime.now()): Double {
        val total = getTotalDays().toDouble()
        val elapsed = getElapsedTime(currentDate).toDouble()
        return (elapsed / total * 100.0).coerceIn(0.0, 100.0)
    }

    /**
     * Check if period is currently active
     */
    fun isCurrentlyActive(currentDate: LocalDateTime = LocalDateTime.now()): Boolean {
        return currentDate.isAfter(startDate) && currentDate.isBefore(endDate)
    }

    /**
     * Get formatted period description
     */
    fun getDescription(): String {
        return when (level) {
            DashaLevel.MAHADASHA -> "${planet.displayName} Mahadasha"
            DashaLevel.ANTARDASHA -> "${planet.displayName} Antardasha"
            DashaLevel.PRATYANTARDASHA -> "${planet.displayName} Pratyantardasha"
            DashaLevel.SOOKSHMA -> "${planet.displayName} Sookshma"
            DashaLevel.PRANA -> "${planet.displayName} Prana"
        }
    }

    /**
     * Format as string
     */
    fun toFormattedString(): String {
        val activeMarker = if (isActive) " [ACTIVE]" else ""
        return "${getDescription()}$activeMarker: " +
                "${startDate.toLocalDate()} to ${endDate.toLocalDate()} " +
                "(${String.format("%.2f", durationYears)} years)"
    }
}

/**
 * Dasha Level (Hierarchy)
 */
enum class DashaLevel(val displayName: String, val abbreviation: String) {
    MAHADASHA("Mahadasha", "MD"),
    ANTARDASHA("Antardasha", "AD"),
    PRATYANTARDASHA("Pratyantardasha", "PAD"),
    SOOKSHMA("Sookshma Dasha", "SD"),
    PRANA("Prana Dasha", "PD");
}

/**
 * Vimshottari Dasha Years
 * Total cycle: 120 years
 */
enum class VimshottariYears(val planet: Planet, val years: Int) {
    SUN_YEARS(Planet.SUN, 6),
    MOON_YEARS(Planet.MOON, 10),
    MARS_YEARS(Planet.MARS, 7),
    RAHU_YEARS(Planet.RAHU, 18),
    JUPITER_YEARS(Planet.JUPITER, 16),
    SATURN_YEARS(Planet.SATURN, 19),
    MERCURY_YEARS(Planet.MERCURY, 17),
    KETU_YEARS(Planet.KETU, 7),
    VENUS_YEARS(Planet.VENUS, 20);

    companion object {
        /**
         * Get dasha years for a planet
         */
        fun getYearsForPlanet(planet: Planet): Int {
            return values().find { it.planet == planet }?.years
                ?: throw IllegalArgumentException("Unknown planet for Vimshottari Dasha: $planet")
        }

        /**
         * Get dasha order starting from a given planet
         */
        fun getDashaOrder(startPlanet: Planet): List<Planet> {
            val order = listOf(
                Planet.KETU,
                Planet.VENUS,
                Planet.SUN,
                Planet.MOON,
                Planet.MARS,
                Planet.RAHU,
                Planet.JUPITER,
                Planet.SATURN,
                Planet.MERCURY
            )

            val startIndex = order.indexOf(startPlanet)
            if (startIndex == -1) {
                throw IllegalArgumentException("Invalid start planet: $startPlanet")
            }

            return order.drop(startIndex) + order.take(startIndex)
        }

        /**
         * Total Vimshottari cycle years
         */
        const val TOTAL_YEARS = 120
    }
}

/**
 * Complete Dasha System
 * Contains all mahadashas with their antardashas
 */
data class DashaSystem(
    val birthDateTime: LocalDateTime,
    val birthNakshatra: Nakshatra,
    val mahadashas: List<DashaPeriod>,
    val currentMahadasha: DashaPeriod?,
    val currentAntardasha: DashaPeriod?,
    val currentPratyantardasha: DashaPeriod?
) {
    /**
     * Get dasha at a specific date
     */
    fun getDashaAt(date: LocalDateTime): Triple<DashaPeriod?, DashaPeriod?, DashaPeriod?> {
        val maha = mahadashas.find { it.isCurrentlyActive(date) }
        val antar = maha?.subPeriods?.find { it.isCurrentlyActive(date) }
        val pratyantar = antar?.subPeriods?.find { it.isCurrentlyActive(date) }
        return Triple(maha, antar, pratyantar)
    }

    /**
     * Get all upcoming mahadashas
     */
    fun getUpcomingMahadashas(date: LocalDateTime = LocalDateTime.now()): List<DashaPeriod> {
        return mahadashas.filter { it.startDate.isAfter(date) }
    }

    /**
     * Get past mahadashas
     */
    fun getPastMahadashas(date: LocalDateTime = LocalDateTime.now()): List<DashaPeriod> {
        return mahadashas.filter { it.endDate.isBefore(date) }
    }

    /**
     * Format complete dasha system as string
     */
    fun toFormattedString(): String {
        return buildString {
            appendLine("═══════════════════════════════════════")
            appendLine("      VIMSHOTTARI DASHA SYSTEM")
            appendLine("═══════════════════════════════════════")
            appendLine()
            appendLine("Birth Date: ${birthDateTime.toLocalDate()}")
            appendLine("Birth Nakshatra: ${birthNakshatra.displayName}")
            appendLine("Nakshatra Ruler: ${birthNakshatra.rulerPlanet.displayName}")
            appendLine()

            if (currentMahadasha != null) {
                appendLine("CURRENT PERIODS:")
                appendLine("─────────────────────────────────────")
                appendLine("Mahadasha: ${currentMahadasha.toFormattedString()}")
                appendLine("Progress: ${String.format("%.1f", currentMahadasha.getCompletionPercentage())}%")

                if (currentAntardasha != null) {
                    appendLine()
                    appendLine("Antardasha: ${currentAntardasha.toFormattedString()}")
                    appendLine("Progress: ${String.format("%.1f", currentAntardasha.getCompletionPercentage())}%")
                }

                if (currentPratyantardasha != null) {
                    appendLine()
                    appendLine("Pratyantardasha: ${currentPratyantardasha.toFormattedString()}")
                }
                appendLine()
            }

            appendLine("ALL MAHADASHAS:")
            appendLine("─────────────────────────────────────")
            mahadashas.forEach { maha ->
                appendLine(maha.toFormattedString())
            }
        }
    }

    /**
     * Get timeline summary
     */
    fun getTimelineSummary(): String {
        return buildString {
            mahadashas.forEach { maha ->
                val status = when {
                    maha.isActive -> "[CURRENT]"
                    maha.endDate.isBefore(LocalDateTime.now()) -> "[PAST]"
                    else -> "[FUTURE]"
                }
                appendLine("$status ${maha.planet.symbol} MD: ${maha.startDate.year}-${maha.endDate.year}")
            }
        }
    }
}

/**
 * Dasha Calculator Helper
 */
object DashaCalculatorHelper {
    /**
     * Calculate balance of starting dasha based on birth nakshatra position
     */
    fun calculateBalanceOfDasha(
        nakshatra: Nakshatra,
        moonLongitude: Double
    ): Double {
        // Get nakshatra span (13.333333 degrees)
        val nakshatraSpan = 360.0 / 27.0

        // Calculate position within nakshatra
        val nakshatraStartDegree = nakshatra.startDegree
        val positionInNakshatra = moonLongitude - nakshatraStartDegree

        // Calculate proportion completed
        val proportionCompleted = positionInNakshatra / nakshatraSpan

        // Get ruling planet's dasha years
        val totalYears = VimshottariYears.getYearsForPlanet(nakshatra.rulerPlanet)

        // Calculate balance (years remaining in starting dasha)
        return totalYears * (1.0 - proportionCompleted)
    }

    /**
     * Calculate dasha start date based on balance
     */
    fun calculateDashaStartDate(
        birthDate: LocalDateTime,
        balanceYears: Double
    ): LocalDateTime {
        val days = (balanceYears * 365.25).toLong()
        return birthDate.plusDays(days)
    }

    /**
     * Get antardasha proportions for a mahadasha planet
     */
    fun getAntardashaProportions(mahadashaPlanet: Planet): Map<Planet, Double> {
        val mahaYears = VimshottariYears.getYearsForPlanet(mahadashaPlanet)
        val order = VimshottariYears.getDashaOrder(mahadashaPlanet)

        return order.associateWith { antarPlanet ->
            val antarYears = VimshottariYears.getYearsForPlanet(antarPlanet)
            (mahaYears.toDouble() * antarYears.toDouble()) / 120.0
        }
    }

    /**
     * Get pratyantardasha proportions for an antardasha
     */
    fun getPratyantardashaProportions(
        mahadashaPlanet: Planet,
        antardashaPlanet: Planet
    ): Map<Planet, Double> {
        val antarYears = getAntardashaProportions(mahadashaPlanet)[antardashaPlanet] ?: 0.0
        val order = VimshottariYears.getDashaOrder(antardashaPlanet)

        return order.associateWith { pratyantarPlanet ->
            val pratyantarBasicYears = VimshottariYears.getYearsForPlanet(pratyantarPlanet)
            (antarYears * pratyantarBasicYears.toDouble()) / 120.0
        }
    }
}
