package com.astro.storm.domain.usecase

import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.ChartRepository

class LoadChartUseCase(private val chartRepository: ChartRepository) {
    suspend operator fun invoke(chartId: Long): Result<VedicChart?> {
        return try {
            val chart = chartRepository.getChartById(chartId)
            Result.success(chart)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
