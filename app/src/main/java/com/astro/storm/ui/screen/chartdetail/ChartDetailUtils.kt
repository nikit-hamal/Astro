package com.astro.storm.ui.screen.chartdetail

import kotlin.math.abs

/**
 * Utility functions for formatting and calculations in ChartDetail screens.
 */
object ChartDetailUtils {

    /**
     * Formats a degree value in DMS (degrees, minutes, seconds) notation.
     *
     * @param degree The degree value to format
     * @return Formatted string like "123° 45' 30"
     */
    fun formatDegree(degree: Double): String {
        val normalizedDegree = (degree % 360.0 + 360.0) % 360.0
        val deg = normalizedDegree.toInt()
        val min = ((normalizedDegree - deg) * 60).toInt()
        val sec = ((((normalizedDegree - deg) * 60) - min) * 60).toInt()
        return "$deg° $min' $sec\""
    }

    /**
     * Formats a longitude value as degree within its zodiac sign (0-30).
     *
     * @param longitude The absolute longitude value
     * @return Formatted string like "15° 30' 45"
     */
    fun formatDegreeInSign(longitude: Double): String {
        val degreeInSign = longitude % 30.0
        val deg = degreeInSign.toInt()
        val min = ((degreeInSign - deg) * 60).toInt()
        val sec = ((((degreeInSign - deg) * 60) - min) * 60).toInt()
        return "$deg° $min' $sec\""
    }

    /**
     * Formats a coordinate (latitude or longitude) with direction indicator.
     *
     * @param value The coordinate value in decimal degrees
     * @param isLatitude True for latitude (N/S), false for longitude (E/W)
     * @return Formatted string like "28° 37' N"
     */
    fun formatCoordinate(value: Double, isLatitude: Boolean): String {
        val absValue = abs(value)
        val degrees = absValue.toInt()
        val minutes = ((absValue - degrees) * 60).toInt()
        val direction = if (isLatitude) {
            if (value >= 0) "N" else "S"
        } else {
            if (value >= 0) "E" else "W"
        }
        return "$degrees° $minutes' $direction"
    }

    /**
     * Formats a coordinate with full precision including seconds.
     *
     * @param value The coordinate value in decimal degrees
     * @param isLatitude True for latitude (N/S), false for longitude (E/W)
     * @return Formatted string like "28° 37' 48" N"
     */
    fun formatCoordinateFull(value: Double, isLatitude: Boolean): String {
        val absValue = abs(value)
        val degrees = absValue.toInt()
        val minutes = ((absValue - degrees) * 60).toInt()
        val seconds = ((((absValue - degrees) * 60) - minutes) * 60).toInt()
        val direction = if (isLatitude) {
            if (value >= 0) "N" else "S"
        } else {
            if (value >= 0) "E" else "W"
        }
        return "$degrees° $minutes' $seconds\" $direction"
    }

    /**
     * Formats a duration in years with appropriate precision.
     *
     * @param years Duration in years
     * @return Formatted string like "6.5 years" or "18 years"
     */
    fun formatYears(years: Double): String {
        return if (years == years.toLong().toDouble()) {
            "${years.toLong()} years"
        } else {
            "${String.format("%.1f", years)} years"
        }
    }

    /**
     * Formats a percentage value.
     *
     * @param value The percentage value
     * @param decimals Number of decimal places (default 1)
     * @return Formatted string like "85.5%"
     */
    fun formatPercentage(value: Double, decimals: Int = 1): String {
        return "${String.format("%.${decimals}f", value)}%"
    }

    /**
     * Formats rupas (Shadbala unit) with appropriate precision.
     *
     * @param rupas The rupas value
     * @return Formatted string like "7.25"
     */
    fun formatRupas(rupas: Double): String = String.format("%.2f", rupas)

    /**
     * Formats virupas (Shadbala sub-unit) with appropriate precision.
     *
     * @param virupas The virupas value
     * @return Formatted string like "120.5"
     */
    fun formatVirupas(virupas: Double): String = String.format("%.1f", virupas)

    /**
     * Calculates the degree within a sign from absolute longitude.
     *
     * @param longitude The absolute longitude value
     * @return Degree value from 0 to 30
     */
    fun getDegreeInSign(longitude: Double): Double = longitude % 30.0

    /**
     * Calculates the sign index (0-11) from absolute longitude.
     *
     * @param longitude The absolute longitude value
     * @return Sign index from 0 (Aries) to 11 (Pisces)
     */
    fun getSignIndex(longitude: Double): Int = ((longitude % 360.0) / 30.0).toInt()

    /**
     * Normalizes a degree value to 0-360 range.
     *
     * @param degree The degree value to normalize
     * @return Normalized value between 0 and 360
     */
    fun normalizeDegree(degree: Double): Double = (degree % 360.0 + 360.0) % 360.0

    /**
     * Calculates the shortest angular distance between two points.
     *
     * @param degree1 First degree value
     * @param degree2 Second degree value
     * @return Shortest angular distance (always positive, 0-180)
     */
    fun angularDistance(degree1: Double, degree2: Double): Double {
        val diff = abs(normalizeDegree(degree1) - normalizeDegree(degree2))
        return if (diff > 180) 360 - diff else diff
    }
}
