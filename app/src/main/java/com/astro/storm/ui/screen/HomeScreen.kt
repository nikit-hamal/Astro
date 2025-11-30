package com.astro.storm.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.astro.storm.ui.components.ProfileListBottomSheet
import com.astro.storm.ui.components.ProfileSwitcher
import com.astro.storm.ui.viewmodel.ChartViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChartViewModel,
    onNavigateToChartInput: () -> Unit,
    onNavigateToChartDetail: (Long) -> Unit
) {
    val savedCharts by viewModel.savedCharts.collectAsState()
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }
    val currentChart = savedCharts.find { it.isSelected }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chart") },
                actions = {
                    ProfileSwitcher(
                        currentChart = currentChart,
                        onProfileClick = {
                            showBottomSheet = true
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (currentChart != null) {
                Text("Displaying chart for ${currentChart.name}")
                Button(onClick = { onNavigateToChartDetail(currentChart.id) }) {
                    Text("View Details")
                }
            } else {
                Text("No chart selected")
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = {
                    showBottomSheet = false
                },
                sheetState = sheetState
            ) {
                ProfileListBottomSheet(
                    charts = savedCharts,
                    onChartSelected = { chart ->
                        viewModel.setSelectedChart(chart.id)
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                            }
                        }
                    },
                    onAddNewChart = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                showBottomSheet = false
                                onNavigateToChartInput()
                            }
                        }
                    },
                    onDeleteChart = { chartId ->
                        viewModel.deleteChart(chartId)
                    }
                )
            }
        }
    }
}