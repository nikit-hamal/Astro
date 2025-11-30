package com.astro.storm.ui.viewmodel

import android.app.Application
import androidx.compose.ui.unit.Density
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astro.storm.data.local.ChartDatabase
import com.astro.storm.data.model.BirthData
import com.astro.storm.data.model.HouseSystem
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.repository.ChartRepository
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.domain.usecase.*
import com.astro.storm.ephemeris.SwissEphemerisEngine
import com.astro.storm.util.ChartExporter
import com.astro.storm.util.ExportUtils
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChartRepository
    private val ephemerisEngine: SwissEphemerisEngine
    private val chartExporter: ChartExporter

    // Use Cases
    private val calculateChartUseCase: CalculateChartUseCase
    private val saveChartUseCase: SaveChartUseCase
    private val loadChartUseCase: LoadChartUseCase
    private val deleteChartUseCase: DeleteChartUseCase
    private val exportChartToPdfUseCase: ExportChartToPdfUseCase
    private val exportChartToJsonUseCase: ExportChartToJsonUseCase
    private val exportChartToCsvUseCase: ExportChartToCsvUseCase
    private val exportChartToImageUseCase: ExportChartToImageUseCase
    private val exportChartToTextUseCase: ExportChartToTextUseCase

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Initial)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private val _savedCharts = MutableStateFlow<List<SavedChart>>(emptyList())
    val savedCharts: StateFlow<List<SavedChart>> = _savedCharts.asStateFlow()

    init {
        val database = ChartDatabase.getInstance(application)
        repository = ChartRepository(database.chartDao())
        ephemerisEngine = SwissEphemerisEngine(application)
        chartExporter = ChartExporter(application)

        calculateChartUseCase = CalculateChartUseCase(ephemerisEngine)
        saveChartUseCase = SaveChartUseCase(repository)
        loadChartUseCase = LoadChartUseCase(repository)
        deleteChartUseCase = DeleteChartUseCase(repository)
        exportChartToPdfUseCase = ExportChartToPdfUseCase(chartExporter)
        exportChartToJsonUseCase = ExportChartToJsonUseCase(chartExporter)
        exportChartToCsvUseCase = ExportChartToCsvUseCase(chartExporter)
        exportChartToImageUseCase = ExportChartToImageUseCase(chartExporter)
        exportChartToTextUseCase = ExportChartToTextUseCase(chartExporter)


        loadSavedCharts()
    }

    private fun loadSavedCharts() {
        viewModelScope.launch {
            repository.getAllCharts().collect { charts ->
                _savedCharts.value = charts
            }
        }
    }

    fun calculateChart(birthData: BirthData, houseSystem: HouseSystem = HouseSystem.DEFAULT) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Calculating
            calculateChartUseCase(birthData, houseSystem)
                .onSuccess { _uiState.value = ChartUiState.Success(it) }
                .onFailure { _uiState.value = ChartUiState.Error(it.message ?: "Unknown error") }
        }
    }

    fun loadChart(chartId: Long) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading
            loadChartUseCase(chartId)
                .onSuccess { chart ->
                    if (chart != null) {
                        _uiState.value = ChartUiState.Success(chart)
                    } else {
                        _uiState.value = ChartUiState.Error("Chart not found")
                    }
                }
                .onFailure { _uiState.value = ChartUiState.Error(it.message ?: "Failed to load chart") }
        }
    }

    fun saveChart(chart: VedicChart) {
        viewModelScope.launch {
            saveChartUseCase(chart)
                .onSuccess { _uiState.value = ChartUiState.Saved }
                .onFailure { _uiState.value = ChartUiState.Error("Failed to save chart: ${it.message}") }
        }
    }

    fun deleteChart(chartId: Long) {
        viewModelScope.launch {
            deleteChartUseCase(chartId)
                .onFailure { _uiState.value = ChartUiState.Error("Failed to delete chart: ${it.message}") }
        }
    }

    fun copyChartToClipboard(chart: VedicChart) {
        try {
            val plaintext = ExportUtils.getChartPlaintext(chart)
            ExportUtils.copyToClipboard(getApplication(), plaintext, "Vedic Chart Data")
            _uiState.value = ChartUiState.Exported("Chart data copied to clipboard")
        } catch (e: Exception) {
            _uiState.value = ChartUiState.Error("Failed to copy: ${e.message}")
        }
    }

    fun exportChartToPdf(
        chart: VedicChart,
        density: Density,
        options: ChartExporter.PdfExportOptions = ChartExporter.PdfExportOptions()
    ) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Exporting("Generating PDF report...")
            when (val result = exportChartToPdfUseCase(chart, density, options)) {
                is ChartExporter.ExportResult.Success -> _uiState.value = ChartUiState.Exported("PDF saved successfully")
                is ChartExporter.ExportResult.Error -> _uiState.value = ChartUiState.Error(result.message)
            }
        }
    }

    fun exportChartToJson(chart: VedicChart) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Exporting("Generating JSON...")
            when (val result = exportChartToJsonUseCase(chart)) {
                is ChartExporter.ExportResult.Success -> _uiState.value = ChartUiState.Exported("JSON saved successfully")
                is ChartExporter.ExportResult.Error -> _uiState.value = ChartUiState.Error(result.message)
            }
        }
    }

    fun exportChartToCsv(chart: VedicChart) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Exporting("Generating CSV...")
            when (val result = exportChartToCsvUseCase(chart)) {
                is ChartExporter.ExportResult.Success -> _uiState.value = ChartUiState.Exported("CSV saved successfully")
                is ChartExporter.ExportResult.Error -> _uiState.value = ChartUiState.Error(result.message)
            }
        }
    }

    fun exportChartToImage(
        chart: VedicChart,
        density: Density,
        options: ChartExporter.ImageExportOptions = ChartExporter.ImageExportOptions()
    ) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Exporting("Generating image...")
            when (val result = exportChartToImageUseCase(chart, density, options)) {
                is ChartExporter.ExportResult.Success -> _uiState.value = ChartUiState.Exported("Image saved successfully")
                is ChartExporter.ExportResult.Error -> _uiState.value = ChartUiState.Error(result.message)
            }
        }
    }

    fun exportChartToText(chart: VedicChart) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Exporting("Generating text report...")
            when (val result = exportChartToTextUseCase(chart)) {
                is ChartExporter.ExportResult.Success -> _uiState.value = ChartUiState.Exported("Text report saved successfully")
                is ChartExporter.ExportResult.Error -> _uiState.value = ChartUiState.Error(result.message)
            }
        }
    }

    fun resetState() {
        _uiState.value = ChartUiState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        ephemerisEngine.close()
    }
}

sealed class ChartUiState {
    object Initial : ChartUiState()
    object Loading : ChartUiState()
    object Calculating : ChartUiState()
    data class Success(val chart: VedicChart) : ChartUiState()
    data class Error(val message: String) : ChartUiState()
    object Saved : ChartUiState()
    data class Exporting(val message: String) : ChartUiState()
    data class Exported(val message: String) : ChartUiState()
}
