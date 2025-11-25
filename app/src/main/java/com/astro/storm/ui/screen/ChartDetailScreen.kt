package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DivisionalChartCalculator
import com.astro.storm.ephemeris.DivisionalChartData
import com.astro.storm.ephemeris.PanchangaCalculator
import com.astro.storm.ephemeris.PanchangaData
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.theme.*
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

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
    var snackbarMessage by remember { mutableStateOf<String?>(null) }

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

    // Handle snackbar messages
    LaunchedEffect(snackbarMessage) {
        snackbarMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            snackbarMessage = null
        }
    }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ChartUiState.Exported -> {
                snackbarMessage = (uiState as ChartUiState.Exported).message
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chart Details",
                        style = MaterialTheme.typography.titleLarge
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
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
                ChartContent(
                    chart = chart,
                    chartRenderer = chartRenderer,
                    paddingValues = paddingValues,
                    onCopyClick = {
                        viewModel.copyChartToClipboard(chart)
                        snackbarMessage = "Chart data copied"
                    },
                    onDownloadClick = {
                        if (permissionsState.allPermissionsGranted) {
                            viewModel.exportChartImage(
                                chart,
                                "chart_${chart.birthData.name}_${System.currentTimeMillis()}"
                            )
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }
                )
            }
            is ChartUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
            else -> {}
        }
    }
}

@Composable
private fun ChartContent(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    paddingValues: PaddingValues,
    onCopyClick: () -> Unit,
    onDownloadClick: () -> Unit
) {
    val context = LocalContext.current

    // Calculate divisional charts
    val divisionalCharts = remember(chart) {
        DivisionalChartCalculator.calculateAllDivisionalCharts(chart)
    }

    // Calculate Panchanga
    var panchangaData by remember { mutableStateOf<PanchangaData?>(null) }

    LaunchedEffect(chart) {
        withContext(Dispatchers.Default) {
            try {
                val calculator = PanchangaCalculator(context)
                panchangaData = calculator.calculatePanchanga(
                    dateTime = chart.birthData.dateTime,
                    latitude = chart.birthData.latitude,
                    longitude = chart.birthData.longitude,
                    timezone = chart.birthData.timezone
                )
                calculator.close()
            } catch (e: Exception) {
                // Panchanga calculation failed silently
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(rememberScrollState())
    ) {
        // Lagna Chart Visualization
        ChartCard(
            title = "Lagna Chart (D1)",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
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

        // Action Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FilledTonalButton(
                onClick = onDownloadClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Download")
            }

            FilledTonalButton(
                onClick = onCopyClick,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Copy")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Birth Information Section
        ExpandableSection(
            title = "Birth Information",
            icon = Icons.Outlined.Person,
            defaultExpanded = true
        ) {
            InfoRow("Name", chart.birthData.name)
            InfoRow(
                "Date & Time",
                chart.birthData.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm:ss a"))
            )
            InfoRow("Location", chart.birthData.location)
            InfoRow(
                "Coordinates",
                "${formatCoordinate(chart.birthData.latitude, true)}, ${formatCoordinate(chart.birthData.longitude, false)}"
            )
            InfoRow("Timezone", chart.birthData.timezone)
        }

        // Panchanga Section
        panchangaData?.let { panchanga ->
            ExpandableSection(
                title = "Panchanga",
                icon = Icons.Outlined.WbSunny,
                defaultExpanded = true
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanchangaItem(
                        label = "Tithi",
                        value = panchanga.tithi.tithi.displayName,
                        subValue = panchanga.paksha.displayName,
                        modifier = Modifier.weight(1f)
                    )
                    PanchangaItem(
                        label = "Nakshatra",
                        value = panchanga.nakshatra.nakshatra.displayName,
                        subValue = "Pada ${panchanga.nakshatra.pada}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanchangaItem(
                        label = "Yoga",
                        value = panchanga.yoga.yoga.displayName,
                        subValue = panchanga.yoga.yoga.nature,
                        modifier = Modifier.weight(1f)
                    )
                    PanchangaItem(
                        label = "Karana",
                        value = panchanga.karana.karana.displayName,
                        subValue = panchanga.karana.karana.nature,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    PanchangaItem(
                        label = "Vara",
                        value = panchanga.vara.displayName,
                        subValue = "Lord: ${panchanga.vara.lord.displayName}",
                        modifier = Modifier.weight(1f)
                    )
                    PanchangaItem(
                        label = "Moon Phase",
                        value = "${String.format("%.1f", panchanga.moonPhase)}%",
                        subValue = if (panchanga.moonPhase > 50) "Illuminated" else "Dark",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            "Sunrise",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            panchanga.sunrise,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "Sunset",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            panchanga.sunset,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                }
            }
        }

        // Astronomical Data Section
        ExpandableSection(
            title = "Astronomical Data",
            icon = Icons.Outlined.Info,
            defaultExpanded = false
        ) {
            InfoRow("Julian Day", String.format("%.6f", chart.julianDay))
            InfoRow("Ayanamsa", "${chart.ayanamsaName} (${formatDegree(chart.ayanamsa)})")
            InfoRow("Ascendant", formatDegree(chart.ascendant))
            InfoRow("Midheaven", formatDegree(chart.midheaven))
            InfoRow("House System", chart.houseSystem.displayName)
        }

        // Planetary Positions Section
        ExpandableSection(
            title = "Planetary Positions",
            icon = Icons.Outlined.Star,
            defaultExpanded = true
        ) {
            chart.planetPositions.forEach { position ->
                PlanetRow(
                    planetName = position.planet.displayName,
                    sign = position.sign.displayName,
                    degree = formatDegreeInSign(position.longitude),
                    nakshatra = "${position.nakshatra.displayName} (Pada ${position.nakshatraPada})",
                    house = position.house,
                    isRetrograde = position.isRetrograde
                )
                if (position != chart.planetPositions.last()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                    )
                }
            }
        }

        // Divisional Charts Section
        DivisionalChartsSection(
            divisionalCharts = divisionalCharts,
            chartRenderer = chartRenderer
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun DivisionalChartsSection(
    divisionalCharts: List<DivisionalChartData>,
    chartRenderer: ChartRenderer
) {
    divisionalCharts.forEach { divisionalChart ->
        ExpandableSection(
            title = divisionalChart.chartTitle,
            icon = Icons.Outlined.Star,
            defaultExpanded = false,
            subtitle = divisionalChart.chartType.description
        ) {
            // Chart visualization
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChartBackground)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    chartRenderer.drawDivisionalChart(
                        drawScope = this,
                        planetPositions = divisionalChart.planetPositions,
                        ascendantLongitude = divisionalChart.ascendantLongitude,
                        size = size.minDimension,
                        chartTitle = divisionalChart.chartTitle.split(" ").first()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Planet positions in this chart
            Text(
                "Planet Positions",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            divisionalChart.planetPositions.forEach { position ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = position.planet.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (position.isRetrograde)
                            MaterialTheme.colorScheme.secondary
                        else
                            MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${position.sign.abbreviation} ${formatDegreeInSign(position.longitude)}${if (position.isRetrograde) " (R)" else ""} | H${position.house}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            content()
        }
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    icon: ImageVector,
    defaultExpanded: Boolean,
    subtitle: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by remember { mutableStateOf(defaultExpanded) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        subtitle?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                Icon(
                    imageVector = if (expanded) Icons.Outlined.ExpandLess else Icons.Outlined.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 16.dp
                    )
                ) {
                    content()
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun PlanetRow(
    planetName: String,
    sign: String,
    degree: String,
    nakshatra: String,
    house: Int,
    isRetrograde: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = planetName,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (isRetrograde)
                        MaterialTheme.colorScheme.secondary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (isRetrograde) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = "R",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Text(
                text = "$sign $degree",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = nakshatra,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "House $house",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PanchangaItem(
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subValue,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
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

private fun formatDegreeInSign(longitude: Double): String {
    val degreeInSign = longitude % 30.0
    val deg = degreeInSign.toInt()
    val min = ((degreeInSign - deg) * 60).toInt()
    val sec = ((((degreeInSign - deg) * 60) - min) * 60).toInt()
    return "$deg° $min' $sec\""
}

private fun formatCoordinate(value: Double, isLatitude: Boolean): String {
    val abs = kotlin.math.abs(value)
    val degrees = abs.toInt()
    val minutes = ((abs - degrees) * 60).toInt()
    val direction = if (isLatitude) {
        if (value >= 0) "N" else "S"
    } else {
        if (value >= 0) "E" else "W"
    }
    return "$degrees° $minutes' $direction"
}
