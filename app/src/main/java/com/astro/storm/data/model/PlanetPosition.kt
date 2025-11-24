package com.astro.storm.data.model

/**
 * Position of a planet in the chart
 */
data class PlanetPosition(
    val planet: Planet,
    val longitude: Double,
    val latitude: Double,
    val distance: Double,
    val speed: Double,
    val sign: ZodiacSign,
    val degree: Double,
    val minutes: Double,
    val seconds: Double,
    val isRetrograde: Boolean,
    val nakshatra: Nakshatra,
    val nakshatraPada: Int,
    val house: Int
) {
    fun toFormattedString(): String {
        val degreeInSign = longitude % 30.0
        val deg = degreeInSign.toInt()
        val min = ((degreeInSign - deg) * 60).toInt()
        val sec = ((((degreeInSign - deg) * 60) - min) * 60).toInt()
        val retrograde = if (isRetrograde) " (R)" else ""
        return "${planet.displayName}: ${sign.abbreviation} ${deg}° ${min}' ${sec}\"$retrograde"
    }

    fun toLLMString(): String {
        val degreeInSign = longitude % 30.0
        val deg = degreeInSign.toInt()
        val min = ((degreeInSign - deg) * 60).toInt()
        val sec = ((((degreeInSign - deg) * 60) - min) * 60).toInt()
        val retrograde = if (isRetrograde) " [Retrograde]" else ""
        return "${planet.displayName.padEnd(10)}: ${sign.displayName.padEnd(12)} ${deg}° ${min}' ${sec}\" | House ${house} | ${nakshatra.displayName} (Pada ${nakshatraPada})$retrograde"
    }
}
