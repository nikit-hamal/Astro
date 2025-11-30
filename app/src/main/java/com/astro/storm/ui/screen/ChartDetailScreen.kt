package com.astro.storm.ui.screen

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.screen.chartdetail.tabs.AshtakavargaTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.DashasTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.PanchangaTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.PlanetsTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.TransitsTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.YogasTabContent
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(
    viewModel: ChartViewModel,
    chartId: Long,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val chartRenderer = remember { ChartRenderer() }
    var currentChart by remember { mutableStateOf<VedicChart?>(null) }
    val context = LocalContext.current

    LaunchedEffect(chartId) {
        viewModel.loadChart(chartId)
    }

    LaunchedEffect(uiState) {
        if (uiState is ChartUiState.Success) {
            currentChart = (uiState as ChartUiState.Success).chart
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentChart?.birthData?.name ?: "Chart Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is ChartUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ChartUiState.Success -> {
                val chart = state.chart
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        ChartCard(chart = chart, chartRenderer = chartRenderer)
                    }
                    item {
                        PlanetsTabContent(chart = chart, onPlanetClick = {}, onShadbalaClick = {})
                    }
                    item {
                        YogasTabContent(chart = chart)
                    }
                    item {
                        DashasTabContent(chart = chart)
                    }
                    item {
                        TransitsTabContent(chart = chart, context = context)
                    }
                    item {
                        AshtakavargaTabContent(chart = chart)
                    }
                    item {
                        PanchangaTabContent(chart = chart, context = context)
                    }
                }
            }
            is ChartUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = state.message)
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ChartCard(chart: VedicChart, chartRenderer: ChartRenderer) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Lagna Chart", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                chartRenderer.drawNorthIndianChart(
                    drawScope = this,
                    chart = chart,
                    size = size.minDimension,
                    chartTitle = "Lagna"
                )
            }
        }
    }
}