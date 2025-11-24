package com.astro.storm.data.model

/**
 * House systems used in Vedic astrology
 */
enum class HouseSystem(val code: Char, val displayName: String) {
    PLACIDUS('P', "Placidus"),
    KOCH('K', "Koch"),
    PORPHYRIUS('O', "Porphyrius"),
    REGIOMONTANUS('R', "Regiomontanus"),
    CAMPANUS('C', "Campanus"),
    EQUAL('E', "Equal"),
    WHOLE_SIGN('W', "Whole Sign"),
    VEHLOW('V', "Vehlow"),
    MERIDIAN('X', "Meridian"),
    MORINUS('M', "Morinus"),
    ALCABITUS('B', "Alcabitus");

    companion object {
        val DEFAULT = PLACIDUS
    }
}
