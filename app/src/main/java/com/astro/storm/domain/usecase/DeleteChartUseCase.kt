package com.astro.storm.domain.usecase

import com.astro.storm.data.repository.ChartRepository

class DeleteChartUseCase(private val chartRepository: ChartRepository) {
    suspend operator fun invoke(chartId: Long): Result<Unit> {
        return try {
            chartRepository.deleteChart(chartId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
