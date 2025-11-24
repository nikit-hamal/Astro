package com.astro.storm.data.model

import java.time.LocalDateTime

/**
 * Birth data for chart calculation
 */
data class BirthData(
    val name: String,
    val dateTime: LocalDateTime,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val location: String
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90 degrees" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180 degrees" }
    }
}
