package com.astro.storm.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.ui.unit.Density
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astro.storm.data.local.ChartDatabase
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.ChartRepository
import com.astro.storm.util.ChartExporter
import com.astro.storm.util.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for Chart Detail screen operations.
 *
 * Manages:
 * - Loading individual charts by ID
 * - Deleting charts
 * - Exporting chart data
 */
class ChartDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChartRepository
    private val chartExporter: ChartExporter

    private val _chart = MutableStateFlow<VedicChart?>(null)
    val chart: StateFlow<VedicChart?> = _chart.asStateFlow()

    private val _exportState = MutableStateFlow<ExportState>(ExportState.Idle)
    val exportState: StateFlow<ExportState> = _exportState.asStateFlow()

    init {
        val database = ChartDatabase.getInstance(application)
        repository = ChartRepository(database.chartDao())
        chartExporter = ChartExporter(application)
    }

    /**
     * Load a chart by its ID
     */
    fun loadChart(chartId: Long) {
        viewModelScope.launch {
            try {
                val loadedChart = repository.getChartById(chartId)
                _chart.value = loadedChart
            } catch (e: Exception) {
                _chart.value = null
            }
        }
    }

    /**
     * Delete a chart by its ID
     */
    fun deleteChart(chartId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteChart(chartId)
                _chart.value = null
            } catch (e: Exception) {
                // Handle error silently - chart screen will navigate back
            }
        }
    }

    /**
     * Export chart using the chart exporter
     */
    fun exportChart(chart: VedicChart, context: Context) {
        viewModelScope.launch {
            try {
                _exportState.value = ExportState.Exporting
                val result = withContext(Dispatchers.IO) {
                    chartExporter.exportToText(chart)
                }
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _exportState.value = ExportState.Success("Chart exported successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _exportState.value = ExportState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Export failed")
            }
        }
    }

    /**
     * Export chart as image
     */
    fun exportChartImage(chart: VedicChart, density: Density) {
        viewModelScope.launch {
            try {
                _exportState.value = ExportState.Exporting
                val result = chartExporter.exportToImage(chart, ChartExporter.ImageExportOptions(), density)
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _exportState.value = ExportState.Success("Image saved successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _exportState.value = ExportState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "Image export failed")
            }
        }
    }

    /**
     * Export chart as PDF
     */
    fun exportChartPdf(chart: VedicChart, density: Density) {
        viewModelScope.launch {
            try {
                _exportState.value = ExportState.Exporting
                val result = chartExporter.exportToPdf(chart, ChartExporter.PdfExportOptions(), density)
                when (result) {
                    is ChartExporter.ExportResult.Success -> {
                        _exportState.value = ExportState.Success("PDF saved successfully")
                    }
                    is ChartExporter.ExportResult.Error -> {
                        _exportState.value = ExportState.Error(result.message)
                    }
                }
            } catch (e: Exception) {
                _exportState.value = ExportState.Error(e.message ?: "PDF export failed")
            }
        }
    }

    /**
     * Copy chart data to clipboard
     */
    fun copyToClipboard(chart: VedicChart) {
        try {
            val plaintext = ExportUtils.getChartPlaintext(chart)
            ExportUtils.copyToClipboard(getApplication(), plaintext, "Vedic Chart Data")
            _exportState.value = ExportState.Success("Copied to clipboard")
        } catch (e: Exception) {
            _exportState.value = ExportState.Error("Failed to copy: ${e.message}")
        }
    }

    /**
     * Reset export state
     */
    fun resetExportState() {
        _exportState.value = ExportState.Idle
    }
}

/**
 * Export operation states
 */
sealed class ExportState {
    object Idle : ExportState()
    object Exporting : ExportState()
    data class Success(val message: String) : ExportState()
    data class Error(val message: String) : ExportState()
}
