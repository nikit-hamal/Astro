package com.astro.storm.ui.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.astro.storm.data.local.ChartDatabase
import com.astro.storm.data.model.*
import com.astro.storm.data.repository.ChartRepository
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ephemeris.SwissEphemerisEngine
import com.astro.storm.util.ExportUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

/**
 * ViewModel for chart operations
 */
class ChartViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ChartRepository
    private val ephemerisEngine: SwissEphemerisEngine

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
     * Calculate a new Vedic chart
     */
    fun calculateChart(
        birthData: BirthData,
        houseSystem: HouseSystem = HouseSystem.DEFAULT
    ) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Calculating

            try {
                val chart = withContext(Dispatchers.Default) {
                    ephemerisEngine.calculateVedicChart(birthData, houseSystem)
                }
                val divisionalCharts = calculateDivisionalCharts(chart)
                _uiState.value = ChartUiState.Success(chart, divisionalCharts)
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }

    /**
     * Load a saved chart
     */
    fun loadChart(chartId: Long) {
        viewModelScope.launch {
            _uiState.value = ChartUiState.Loading

            try {
                val chart = repository.getChartById(chartId)
                if (chart != null) {
                    val divisionalCharts = calculateDivisionalCharts(chart)
                    _uiState.value = ChartUiState.Success(chart, divisionalCharts)
                } else {
                    _uiState.value = ChartUiState.Error("Chart not found")
                }
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error(e.message ?: "Failed to load chart")
            }
        }
    }

    /**
     * Save current chart
     */
    fun saveChart(chart: VedicChart) {
        viewModelScope.launch {
            try {
                repository.saveChart(chart)
                _uiState.value = ChartUiState.Saved
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Failed to save chart: ${e.message}")
            }
        }
    }

    /**
     * Delete a saved chart
     */
    fun deleteChart(chartId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteChart(chartId)
            } catch (e: Exception) {
                _uiState.value = ChartUiState.Error("Failed to delete chart: ${e.message}")
            }
        }
    }

    /**
     * Export chart as image
     */
    /**
     * Copy chart plaintext to clipboard
     */
    fun copyChartToClipboard(chart: VedicChart) {
        try {
            val plaintext = ExportUtils.getChartPlaintext(chart)
            ExportUtils.copyToClipboard(getApplication(), plaintext, "Vedic Chart Data")
            _uiState.value = ChartUiState.Exported("Chart data copied to clipboard")
        } catch (e: Exception) {
            _uiState.value = ChartUiState.Error("Failed to copy: ${e.message}")
        }
    }

    /**
     * Reset UI state
     */
    fun resetState() {
        _uiState.value = ChartUiState.Initial
    }

    override fun onCleared() {
        super.onCleared()
        ephemerisEngine.close()
    }

    private fun calculateDivisionalCharts(chart: VedicChart): List<DivisionalChart> {
        return listOf(
            ephemerisEngine.calculateDivisionalChart(chart, DivisionalChartType.D9),
            ephemerisEngine.calculateDivisionalChart(chart, DivisionalChartType.D10),
            ephemerisEngine.calculateDivisionalChart(chart, DivisionalChartType.D60)
        )
    }
}

sealed class ChartUiState {
    object Initial : ChartUiState()
    object Loading : ChartUiState()
    object Calculating : ChartUiState()
    data class Success(
        val chart: VedicChart,
        val divisionalCharts: List<DivisionalChart>
    ) : ChartUiState()
    data class Error(val message: String) : ChartUiState()
    object Saved : ChartUiState()
    data class Exported(val message: String) : ChartUiState()
}
