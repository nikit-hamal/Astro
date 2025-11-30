package com.astro.storm.domain.usecase

import androidx.compose.ui.unit.Density
import com.astro.storm.data.model.VedicChart
import com.astro.storm.util.ChartExporter

class ExportChartToPdfUseCase(private val chartExporter: ChartExporter) {
    suspend operator fun invoke(
        chart: VedicChart,
        density: Density,
        options: ChartExporter.PdfExportOptions
    ): ChartExporter.ExportResult {
        return chartExporter.exportToPdf(chart, options, density)
    }
}
