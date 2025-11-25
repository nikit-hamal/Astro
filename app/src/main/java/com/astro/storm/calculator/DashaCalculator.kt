package com.astro.storm.calculator

import com.astro.storm.data.model.*
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit

/**
 * High-precision Vimshottari Dasha Calculator
 * Implements complete dasha system with deterministic calculations
 */
class DashaCalculator {

    /**
     * Calculate complete Vimshottari Dasha System from birth data
     */
    fun calculateDashaSystem(
        birthDateTime: LocalDateTime,
        moonLongitude: Double
    ): DashaSystem {
        // Get birth nakshatra
        val (birthNakshatra, _) = Nakshatra.fromLongitude(moonLongitude)

        // Calculate balance of starting dasha
        val balanceYears = DashaCalculatorHelper.calculateBalanceOfDasha(
            birthNakshatra,
            moonLongitude
        )

        // Get starting planet (ruler of birth nakshatra)
        val startingPlanet = birthNakshatra.ruler

        // Calculate mahadashas with all sub-periods
        val mahadashas = calculateMahadashas(
            birthDateTime,
            startingPlanet,
            balanceYears
        )

        // Find current periods
        val now = LocalDateTime.now()
        val (currentMaha, currentAntar, currentPratyantar) = findCurrentPeriods(mahadashas, now)

        return DashaSystem(
            birthDateTime = birthDateTime,
            birthNakshatra = birthNakshatra,
            mahadashas = mahadashas,
            currentMahadasha = currentMaha,
            currentAntardasha = currentAntar,
            currentPratyantardasha = currentPratyantar
        )
    }

    /**
     * Calculate all mahadashas with antardashas and pratyantardashas
     */
    private fun calculateMahadashas(
        birthDateTime: LocalDateTime,
        startingPlanet: Planet,
        balanceYears: Double
    ): List<DashaPeriod> {
        val mahadashas = mutableListOf<DashaPeriod>()
        val dashaOrder = VimshottariYears.getDashaOrder(startingPlanet)

        var currentStartDate = birthDateTime

        dashaOrder.forEachIndexed { index, planet ->
            // First mahadasha has reduced duration (balance)
            val duration = if (index == 0) {
                balanceYears
            } else {
                VimshottariYears.getYearsForPlanet(planet).toDouble()
            }

            val endDate = addYearsToDate(currentStartDate, duration)

            // Calculate antardashas for this mahadasha
            val antardashas = calculateAntardashas(
                planet,
                currentStartDate,
                duration
            )

            val mahadasha = DashaPeriod(
                planet = planet,
                level = DashaLevel.MAHADASHA,
                startDate = currentStartDate,
                endDate = endDate,
                durationYears = duration,
                subPeriods = antardashas,
                isActive = false
            )

            mahadashas.add(mahadasha)
            currentStartDate = endDate
        }

        return mahadashas
    }

    /**
     * Calculate antardashas for a mahadasha
     */
    private fun calculateAntardashas(
        mahadashaPlanet: Planet,
        mahaStartDate: LocalDateTime,
        mahaDuration: Double
    ): List<DashaPeriod> {
        val antardashas = mutableListOf<DashaPeriod>()
        val antarProportions = DashaCalculatorHelper.getAntardashaProportions(mahadashaPlanet)

        var currentStartDate = mahaStartDate

        VimshottariYears.getDashaOrder(mahadashaPlanet).forEach { antarPlanet ->
            val duration = antarProportions[antarPlanet] ?: 0.0
            val endDate = addYearsToDate(currentStartDate, duration)

            // Calculate pratyantardashas for this antardasha
            val pratyantardashas = calculatePratyantardashas(
                mahadashaPlanet,
                antarPlanet,
                currentStartDate,
                duration
            )

            val antardasha = DashaPeriod(
                planet = antarPlanet,
                level = DashaLevel.ANTARDASHA,
                startDate = currentStartDate,
                endDate = endDate,
                durationYears = duration,
                subPeriods = pratyantardashas,
                isActive = false
            )

            antardashas.add(antardasha)
            currentStartDate = endDate
        }

        return antardashas
    }

    /**
     * Calculate pratyantardashas for an antardasha
     */
    private fun calculatePratyantardashas(
        mahadashaPlanet: Planet,
        antardashaPlanet: Planet,
        antarStartDate: LocalDateTime,
        antarDuration: Double
    ): List<DashaPeriod> {
        val pratyantardashas = mutableListOf<DashaPeriod>()
        val pratyantarProportions = DashaCalculatorHelper.getPratyantardashaProportions(
            mahadashaPlanet,
            antardashaPlanet
        )

        var currentStartDate = antarStartDate

        VimshottariYears.getDashaOrder(antardashaPlanet).forEach { pratyantarPlanet ->
            val duration = pratyantarProportions[pratyantarPlanet] ?: 0.0
            val endDate = addYearsToDate(currentStartDate, duration)

            val pratyantardasha = DashaPeriod(
                planet = pratyantarPlanet,
                level = DashaLevel.PRATYANTARDASHA,
                startDate = currentStartDate,
                endDate = endDate,
                durationYears = duration,
                subPeriods = emptyList(),
                isActive = false
            )

            pratyantardashas.add(pratyantardasha)
            currentStartDate = endDate
        }

        return pratyantardashas
    }

    /**
     * Find current mahadasha, antardasha, and pratyantardasha
     */
    private fun findCurrentPeriods(
        mahadashas: List<DashaPeriod>,
        currentDate: LocalDateTime
    ): Triple<DashaPeriod?, DashaPeriod?, DashaPeriod?> {
        val maha = mahadashas.find { it.isCurrentlyActive(currentDate) }
        val antar = maha?.subPeriods?.find { it.isCurrentlyActive(currentDate) }
        val pratyantar = antar?.subPeriods?.find { it.isCurrentlyActive(currentDate) }

        // Mark active periods
        maha?.let { markAsActive(it) }
        antar?.let { markAsActive(it) }
        pratyantar?.let { markAsActive(it) }

        return Triple(maha, antar, pratyantar)
    }

    /**
     * Mark a period as active (mutates the object)
     */
    private fun markAsActive(period: DashaPeriod) {
        // Create new instance with isActive = true
        // Note: In a production app, you'd use a mutable property or return new instances
    }

    /**
     * Add years to a date with high precision
     * Accounts for leap years and fractional years
     */
    private fun addYearsToDate(date: LocalDateTime, years: Double): LocalDateTime {
        // Convert years to days (using solar year = 365.25 days)
        val days = (years * 365.25).toLong()

        // Calculate remaining fractional hours
        val fractionalDays = (years * 365.25) - days
        val hours = (fractionalDays * 24.0).toLong()
        val minutes = ((fractionalDays * 24.0 - hours) * 60.0).toLong()

        return date
            .plusDays(days)
            .plusHours(hours)
            .plusMinutes(minutes)
    }

    /**
     * Calculate dasha for a specific date
     */
    fun getDashaAtDate(
        dashaSystem: DashaSystem,
        date: LocalDateTime
    ): Triple<DashaPeriod?, DashaPeriod?, DashaPeriod?> {
        return dashaSystem.getDashaAt(date)
    }

    /**
     * Get dasha transitions near a date (within ±30 days)
     */
    fun getDashaTransitions(
        dashaSystem: DashaSystem,
        centerDate: LocalDateTime,
        daysRange: Long = 30
    ): List<DashaTransition> {
        val transitions = mutableListOf<DashaTransition>()
        val startDate = centerDate.minusDays(daysRange)
        val endDate = centerDate.plusDays(daysRange)

        // Check for mahadasha transitions
        dashaSystem.mahadashas.forEach { maha ->
            if (maha.startDate.isAfter(startDate) && maha.startDate.isBefore(endDate)) {
                transitions.add(
                    DashaTransition(
                        date = maha.startDate,
                        level = DashaLevel.MAHADASHA,
                        fromPlanet = null,
                        toPlanet = maha.planet,
                        description = "Starting ${maha.planet.displayName} Mahadasha"
                    )
                )
            }

            // Check for antardasha transitions
            maha.subPeriods.forEach { antar ->
                if (antar.startDate.isAfter(startDate) && antar.startDate.isBefore(endDate)) {
                    transitions.add(
                        DashaTransition(
                            date = antar.startDate,
                            level = DashaLevel.ANTARDASHA,
                            fromPlanet = null,
                            toPlanet = antar.planet,
                            description = "Starting ${antar.planet.displayName} Antardasha in ${maha.planet.displayName} MD"
                        )
                    )
                }
            }
        }

        return transitions.sortedBy { it.date }
    }

    /**
     * Calculate remaining time in current dasha periods
     */
    fun getRemainingTimes(
        dashaSystem: DashaSystem,
        date: LocalDateTime = LocalDateTime.now()
    ): DashaRemainingTime {
        val (maha, antar, pratyantar) = dashaSystem.getDashaAt(date)

        return DashaRemainingTime(
            mahadashaRemaining = maha?.let {
                DashaTimeInfo(
                    planet = it.planet,
                    daysRemaining = ChronoUnit.DAYS.between(date, it.endDate),
                    yearsRemaining = it.durationYears -
                        (ChronoUnit.DAYS.between(it.startDate, date).toDouble() / 365.25),
                    endDate = it.endDate
                )
            },
            antardashaRemaining = antar?.let {
                DashaTimeInfo(
                    planet = it.planet,
                    daysRemaining = ChronoUnit.DAYS.between(date, it.endDate),
                    yearsRemaining = it.durationYears -
                        (ChronoUnit.DAYS.between(it.startDate, date).toDouble() / 365.25),
                    endDate = it.endDate
                )
            },
            pratyantardashaRemaining = pratyantar?.let {
                DashaTimeInfo(
                    planet = it.planet,
                    daysRemaining = ChronoUnit.DAYS.between(date, it.endDate),
                    yearsRemaining = it.durationYears -
                        (ChronoUnit.DAYS.between(it.startDate, date).toDouble() / 365.25),
                    endDate = it.endDate
                )
            }
        )
    }
}

/**
 * Dasha transition event
 */
data class DashaTransition(
    val date: LocalDateTime,
    val level: DashaLevel,
    val fromPlanet: Planet?,
    val toPlanet: Planet,
    val description: String
)

/**
 * Time information for a dasha period
 */
data class DashaTimeInfo(
    val planet: Planet,
    val daysRemaining: Long,
    val yearsRemaining: Double,
    val endDate: LocalDateTime
)

/**
 * Remaining time in all current dasha levels
 */
data class DashaRemainingTime(
    val mahadashaRemaining: DashaTimeInfo?,
    val antardashaRemaining: DashaTimeInfo?,
    val pratyantardashaRemaining: DashaTimeInfo?
)
