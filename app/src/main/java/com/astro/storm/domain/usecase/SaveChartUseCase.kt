package com.astro.storm.domain.usecase

import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.ChartRepository

class SaveChartUseCase(private val chartRepository: ChartRepository) {
    suspend operator fun invoke(chart: VedicChart): Result<Unit> {
        return try {
            chartRepository.saveChart(chart)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
