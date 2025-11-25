package com.astro.storm.ui.screen

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

/**
 * Modernized Chart Detail Screen
 * Simplified UI with Copy/Download buttons and Material 3 Snackbars
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ChartDetailScreen(
    viewModel: ChartViewModel,
    chartId: Long,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val chartRenderer = remember { ChartRenderer() }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val permissionsState = rememberMultiplePermissionsState(
        permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            listOf(android.Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            listOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
    )

    LaunchedEffect(chartId) {
        viewModel.loadChart(chartId)
    }

    // Handle export state with snackbar
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ChartUiState.Exported -> {
                scope.launch {
                    snackbarHostState.showSnackbar(
                        message = state.message,
                        duration = SnackbarDuration.Short
                    )
                }
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Chart Details",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { snackbarData ->
                    Snackbar(
                        snackbarData = snackbarData,
                        containerColor = MaterialTheme.colorScheme.inverseSurface,
                        contentColor = MaterialTheme.colorScheme.inverseOnSurface,
                        shape = RoundedCornerShape(12.dp),
                        actionColor = MaterialTheme.colorScheme.inversePrimary
                    )
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
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            is ChartUiState.Success -> {
                val chart = state.chart
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .verticalScroll(rememberScrollState())
                ) {
                    // Chart Visualization with modern card design
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                            .aspectRatio(1f),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {
                        Canvas(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            chartRenderer.drawSouthIndianChart(
                                drawScope = this,
                                chart = chart,
                                size = size.minDimension
                            )
                        }
                    }

                    // Simplified Export Actions - Only "Copy" and "Download"
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Download Button
                        FilledTonalButton(
                            onClick = {
                                if (permissionsState.allPermissionsGranted) {
                                    viewModel.exportChartImage(
                                        chart,
                                        "chart_${chart.birthData.name}_${System.currentTimeMillis()}"
                                    )
                                } else {
                                    permissionsState.launchMultiplePermissionRequest()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Download",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        // Copy Button
                        FilledTonalButton(
                            onClick = {
                                viewModel.copyChartToClipboard(chart)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        message = "Chart data copied to clipboard",
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        ) {
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Copy",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    // Chart Details with improved design
                    ChartDetailsSection(chart = chart)

                    // Bottom spacing
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
            is ChartUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Error Loading Chart",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = state.message,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {}
        }
    }
}

@Composable
fun ChartDetailsSection(chart: VedicChart) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Birth Information
        DetailCard(title = "Birth Information") {
            DetailRow("Name", chart.birthData.name)
            DetailRow(
                "Date & Time",
                chart.birthData.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm:ss a"))
            )
            DetailRow("Location", chart.birthData.location)
            DetailRow(
                "Coordinates",
                "${formatCoordinate(chart.birthData.latitude, true)}, ${formatCoordinate(chart.birthData.longitude, false)}"
            )
            DetailRow("Timezone", chart.birthData.timezone)
        }

        // Astronomical Data
        DetailCard(title = "Astronomical Data") {
            DetailRow("Julian Day", String.format("%.6f", chart.julianDay))
            DetailRow("Ayanamsa", "${chart.ayanamsaName} (${formatDegree(chart.ayanamsa)})")
            DetailRow("Ascendant (Lagna)", "${formatDegree(chart.ascendant)} • House 1")
            DetailRow("Midheaven (MC)", formatDegree(chart.midheaven))
            DetailRow("House System", chart.houseSystem.displayName)
        }

        // Planetary Positions with enhanced design
        DetailCard(title = "Planetary Positions") {
            chart.planetPositions.forEach { position ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = position.planet.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (position.isRetrograde) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Text(
                                    text = "R",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${position.sign.displayName} ${position.degree}° ${position.minutes}' ${position.seconds}\"",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${position.nakshatra.displayName} (Pada ${position.nakshatraPada}) • House ${position.house}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (position != chart.planetPositions.last()) {
                        HorizontalDivider(
                            modifier = Modifier.padding(top = 12.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Column(modifier = Modifier.padding(vertical = 6.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatDegree(degree: Double): String {
    val normalizedDegree = (degree % 360.0 + 360.0) % 360.0
    val deg = normalizedDegree.toInt()
    val min = ((normalizedDegree - deg) * 60).toInt()
    val sec = ((((normalizedDegree - deg) * 60) - min) * 60).toInt()
    return "$deg° $min' $sec\""
}

private fun formatCoordinate(value: Double, isLatitude: Boolean): String {
    val direction = if (isLatitude) {
        if (value >= 0) "N" else "S"
    } else {
        if (value >= 0) "E" else "W"
    }
    val absValue = kotlin.math.abs(value)
    return String.format("%.4f°%s", absValue, direction)
}
