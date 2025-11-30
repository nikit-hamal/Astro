package com.astro.storm.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * Room entity for persisting chart data
 */
@Entity(tableName = "charts")
@TypeConverters(Converters::class)
data class ChartEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val dateTime: String,
    val latitude: Double,
    val longitude: Double,
    val timezone: String,
    val location: String,
    val julianDay: Double,
    val ayanamsa: Double,
    val ayanamsaName: String,
    val ascendant: Double,
    val midheaven: Double,
    val planetPositionsJson: String,
    val houseCuspsJson: String,
    val houseSystem: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSelected: Boolean = false
)
