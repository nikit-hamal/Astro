package com.astro.storm.domain.usecase

import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.SwissEphemerisEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CalculateChartUseCase(private val ephemerisEngine: SwissEphemerisEngine) {
    suspend operator fun invoke(birthData: BirthData, houseSystem: HouseSystem): Result<VedicChart> {
        return withContext(Dispatchers.Default) {
            try {
                val chart = ephemerisEngine.calculateVedicChart(birthData, houseSystem)
                Result.success(chart)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}
