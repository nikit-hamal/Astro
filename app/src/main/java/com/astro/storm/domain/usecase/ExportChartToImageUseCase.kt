package com.astro.storm.domain.usecase

import androidx.compose.ui.unit.Density
import com.astro.storm.data.model.VedicChart
import com.astro.storm.util.ChartExporter

class ExportChartToImageUseCase(private val chartExporter: ChartExporter) {
    suspend operator fun invoke(
        chart: VedicChart,
        density: Density,
        options: ChartExporter.ImageExportOptions
    ): ChartExporter.ExportResult {
        return chartExporter.exportToImage(chart, options, density)
    }
}
