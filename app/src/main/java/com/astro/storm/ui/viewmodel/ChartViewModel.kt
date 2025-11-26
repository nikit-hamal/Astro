package com.astro.storm.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astro.storm.data.local.ChartDatabase
import com.astro.storm.data.model.*
import com.astro.storm.data.repository.ChartRepository
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ephemeris.ChartCalculator
import com.astro.storm.ephemeris.SwissEphemerisEngine
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.util.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for chart operations, managing state and interactions between the UI
 * and the data layer.
 */
class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChartRepository
    private val ephemerisEngine: SwissEphemerisEngine
    private val chartCalculator = ChartCalculator()
    private val chartRenderer = ChartRenderer()

    private val _uiState = MutableStateFlow<ChartUiState>(ChartUiState.Initial)
    val uiState: StateFlow<ChartUiState> = _uiState.asStateFlow()

    private val _savedCharts = MutableStateFlow<List<SavedChart>>(emptyList())
    val savedCharts: StateFlow<List<SavedChart>> = _savedCharts.asStateFlow()

    init {
        val database = ChartDatabase.getInstance(application)
        repository = ChartRepository(database.chartDao())
        ephemerisEngine = SwissEphemerisEngine(application)
        loadSavedCharts()
    }

    private fun loadSavedCharts() {
        viewModelScope.launch {
            repository.getAllCharts().collect { charts ->
                _savedCharts.value = charts
            }
        }
    }

    /**
     * Calculates a new Vedic chart and defaults to the Rashi chart view.
     */
    fun calculateChart(birthData: BirthData, houseSystem: HouseSystem = HouseSystem.DEFAULT) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Calculating
            try {
                val vedicChart = withContext(Dispatchers.Default) {
                    ephemerisEngine.calculateVedicChart(birthData, houseSystem)
                }
                val chartData = withContext(Dispatchers.Default) {
                    chartCalculator.calculateRashiChart(vedicChart)
                }
                _uiState.value = ChartUiState.Success(vedicChart, chartData)
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Loads a saved chart from the database.
     */
    fun loadChart(chartId: Long) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading
            try {
                val vedicChart = repository.getChartById(chartId)
                if (vedicChart != null) {
                    val chartData = withContext(Dispatchers.Default) {
                        chartCalculator.calculateRashiChart(vedicChart)
                    }
                    _uiState.value = ChartUiState.Success(vedicChart, chartData)
                } else {
                    _uiState.value = ChartUiState.Error("Chart not found")
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error(e.message ?: "Failed to load chart")
            }
        }
    }

    /**
     * Changes the currently displayed chart type (e.g., Rashi, Navamsa).
     * This is a stateless operation that recalculates the chart view from the original
     * `VedicChart` data, ensuring no state corruption.
     */
    fun changeChartType(chartType: ChartType) {
        viewModelScope.launch {
            val currentState = _uiState.value
            if (currentState is ChartUiState.Success) {
                val newChartData = withContext(Dispatchers.Default) {
                    when (chartType) {
                        ChartType.RASHI -> chartCalculator.calculateRashiChart(currentState.vedicChart)
                        ChartType.BHAVA -> chartCalculator.calculateBhavaChart(currentState.vedicChart)
                        ChartType.NAVAMSA -> chartCalculator.calculateNavamsaChart(currentState.vedicChart)
                        ChartType.DASAMSA -> chartCalculator.calculateDasamsaChart(currentState.vedicChart)
                    }
                }
                _uiState.value = currentState.copy(chartData = newChartData)
            }
        }
    }

    fun saveChart(vedicChart: VedicChart) {
        viewModelScope.launch {
            try {
                repository.saveChart(vedicChart)
                _uiState.value = ChartUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Failed to save chart: ${e.message}")
            }
        }
    }

    fun deleteChart(chartId: Long) {
        viewModelScope.launch {
            repository.deleteChart(chartId)
        }
    }

    /**
     * Exports the currently displayed chart as a high-resolution image.
     */
    fun exportChartImage(chartData: ChartData, vedicChart: VedicChart, fileName: String) {
        viewModelScope.launch {
            try {
                val bitmap = withContext(Dispatchers.Default) {
                    chartRenderer.createChartBitmap(chartData, vedicChart, 2048, 2048)
                }
                val result = ExportUtils.saveChartImage(getApplication(), bitmap, fileName)
                result.onSuccess {
                    _uiState.value = ChartUiState.Exported("Image saved successfully")
                }.onFailure {
                    _uiState.value = ChartUiState.Error("Failed to save image: ${it.message}")
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Export failed: ${e.message}")
            }
        }
    }

    fun copyChartToClipboard(vedicChart: VedicChart) {
        try {
            val plaintext = ExportUtils.getChartPlaintext(vedicChart)
            ExportUtils.copyToClipboard(getApplication(), plaintext, "Vedic Chart Data")
            _uiState.value = ChartUiState.Exported("Chart data copied to clipboard")
        } catch (e: Exception) {
            _uiState.value = ChartUiState.Error("Failed to copy: ${e.message}")
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

/**
 * Represents the various states of the chart screen UI.
 */
sealed class ChartUiState {
    object Initial : ChartUiState()
    object Loading : ChartUiState()
    object Calculating : ChartUiState()
    /**
     * @param vedicChart The raw, original chart calculation data.
     * @param chartData The processed data for the currently displayed chart type.
     */
    data class Success(
        val vedicChart: VedicChart,
        val chartData: ChartData
    ) : ChartUiState()
    data class Error(val message: String) : ChartUiState()
    object Saved : ChartUiState()
    data class Exported(val message: String) : ChartUiState()
}
