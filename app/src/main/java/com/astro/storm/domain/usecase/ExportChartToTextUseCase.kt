package com.astro.storm.domain.usecase

import com.astro.storm.data.model.VedicChart
import com.astro.storm.util.ChartExporter

class ExportChartToTextUseCase(private val chartExporter: ChartExporter) {
    suspend operator fun invoke(chart: VedicChart): ChartExporter.ExportResult {
        return chartExporter.exportToText(chart)
    }
}
