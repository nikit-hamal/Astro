package com.astro.storm.data.model

/**
 * Represents a single house in a Vedic chart.
 *
 * A house is a fixed geometric position on the chart diagram. It contains a zodiac sign
 * and a list of planets. The sign that occupies a house depends on the ascendant and the
 * chart type.
 *
 * @property houseNumber The number of the house (1-12).
 * @property sign The zodiac sign occupying this house.
 * @property planets The list of planets located within this house's boundaries.
 */
data class ChartHouse(
    val houseNumber: Int,
    val sign: ZodiacSign,
    val planets: List<PlanetPosition>
)

/**
 * Represents the complete data for a rendered Vedic chart.
 *
 * This data model is UI-independent and provides a clean representation of any chart type,
 * such as Rashi (Lagna), Bhava (Chalit), Navamsa (D9), etc. It is the result of a
 * deterministic calculation from raw planetary longitudes and serves as the single source
 * of truth for the rendering layer.
 *
 * @property houses A list of 12 `ChartHouse` objects, representing the entire chart.
 * @property ascendantSign The zodiac sign of the ascendant (Lagna).
 * @property chartType The type of the chart (e.g., Rashi, Navamsa).
 */
data class ChartData(
    val houses: List<ChartHouse>,
    val ascendantSign: ZodiacSign,
    val chartType: ChartType
)

/**
 * Enum representing the different types of Vedic charts supported by the application.
 *
 * Each chart type has a unique calculation logic and display requirements.
 *
 * @property displayName The user-facing name of the chart.
 */
enum class ChartType(val displayName: String) {
    RASHI("Lagna Chart (Rashi)"),
    BHAVA("Bhava (Chalit) Chart"),
    NAVAMSA("Navamsa (D9) Chart"),
    DASAMSA("Dasamsa (D10) Chart"),
//    SUN("Sun Chart (Surya Kundli)"),
//    MOON("Moon Chart (Chandra Kundli)"),
}
