package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.*
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.format.DateTimeFormatter

// Professional dark theme color palette
private val ScreenBackground = Color(0xFF121212)
private val SurfaceColor = Color(0xFF1E1E1E)
private val CardBackground = Color(0xFF252525)
private val CardBackgroundElevated = Color(0xFF2D2D2D)
private val AccentGold = Color(0xFFD4AF37)
private val AccentTeal = Color(0xFF4DB6AC)
private val AccentPurple = Color(0xFF9575CD)
private val AccentRose = Color(0xFFE57373)
private val AccentBlue = Color(0xFF64B5F6)
private val TextPrimary = Color(0xFFF5F5F5)
private val TextSecondary = Color(0xFFB0B0B0)
private val TextMuted = Color(0xFF757575)
private val DividerColor = Color(0xFF333333)
private val SuccessColor = Color(0xFF81C784)
private val WarningColor = Color(0xFFFFB74D)
private val ErrorColor = Color(0xFFE57373)
private val ChartBackground = Color(0xFF1A1512)

// Navigation tabs
enum class ChartTab(val title: String, val icon: ImageVector) {
    CHART("Chart", Icons.Outlined.GridView),
    PLANETS("Planets", Icons.Outlined.Star),
    DASHAS("Dashas", Icons.Outlined.Timeline),
    ASPECTS("Aspects", Icons.Outlined.Hub),
    PANCHANGA("Panchanga", Icons.Outlined.WbSunny)
}

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
    val context = LocalContext.current

    var currentChart by remember { mutableStateOf<VedicChart?>(null) }
    var selectedTab by remember { mutableStateOf(ChartTab.CHART) }

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

    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is ChartUiState.Success -> currentChart = state.chart
            is ChartUiState.Exported -> {
                snackbarHostState.showSnackbar(
                    message = state.message,
                    duration = SnackbarDuration.Short
                )
                delay(100)
                viewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        containerColor = ScreenBackground,
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.padding(16.dp)
            ) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = CardBackgroundElevated,
                    contentColor = TextPrimary,
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        topBar = {
            ChartTopBar(
                chartName = currentChart?.birthData?.name ?: "Chart Details",
                onNavigateBack = onNavigateBack,
                onExport = {
                    currentChart?.let { chart ->
                        if (permissionsState.allPermissionsGranted) {
                            viewModel.exportChartImage(
                                chart,
                                "chart_${chart.birthData.name.replace(" ", "_")}_${System.currentTimeMillis()}"
                            )
                        } else {
                            permissionsState.launchMultiplePermissionRequest()
                        }
                    }
                },
                onCopy = {
                    currentChart?.let { chart ->
                        viewModel.copyChartToClipboard(chart)
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                message = "Chart data copied to clipboard",
                                duration = SnackbarDuration.Short
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            ChartBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { paddingValues ->
        val displayChart = currentChart ?: (uiState as? ChartUiState.Success)?.chart

        when {
            uiState is ChartUiState.Loading && displayChart == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AccentGold)
                }
            }
            displayChart != null -> {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        fadeIn() + slideInVertically { it / 4 } togetherWith
                                fadeOut() + slideOutVertically { -it / 4 }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    label = "tab_content"
                ) { tab ->
                    when (tab) {
                        ChartTab.CHART -> ChartTabContent(displayChart, chartRenderer, context)
                        ChartTab.PLANETS -> PlanetsTabContent(displayChart)
                        ChartTab.DASHAS -> DashasTabContent(displayChart)
                        ChartTab.ASPECTS -> AspectsTabContent(displayChart)
                        ChartTab.PANCHANGA -> PanchangaTabContent(displayChart, context)
                    }
                }
            }
            uiState is ChartUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (uiState as ChartUiState.Error).message,
                        color = ErrorColor,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartTopBar(
    chartName: String,
    onNavigateBack: () -> Unit,
    onExport: () -> Unit,
    onCopy: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = chartName,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        },
        actions = {
            IconButton(onClick = onCopy) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    tint = TextSecondary
                )
            }
            IconButton(onClick = onExport) {
                Icon(
                    Icons.Default.Download,
                    contentDescription = "Export",
                    tint = TextSecondary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = SurfaceColor
        )
    )
}

@Composable
private fun ChartBottomNavigation(
    selectedTab: ChartTab,
    onTabSelected: (ChartTab) -> Unit
) {
    NavigationBar(
        containerColor = SurfaceColor,
        contentColor = TextPrimary,
        tonalElevation = 0.dp,
        modifier = Modifier.height(64.dp)
    ) {
        ChartTab.entries.forEach { tab ->
            NavigationBarItem(
                icon = {
                    Icon(
                        tab.icon,
                        contentDescription = tab.title,
                        modifier = Modifier.size(if (selectedTab == tab) 26.dp else 24.dp)
                    )
                },
                label = null, // Icons only - no text labels
                selected = selectedTab == tab,
                onClick = { onTabSelected(tab) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AccentGold,
                    unselectedIconColor = TextMuted,
                    indicatorColor = AccentGold.copy(alpha = 0.15f)
                )
            )
        }
    }
}

// ============ CHART TAB ============

@Composable
private fun ChartTabContent(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    context: android.content.Context
) {
    val divisionalCharts = remember(chart) {
        DivisionalChartCalculator.calculateAllDivisionalCharts(chart)
    }

    var selectedChartType by remember { mutableStateOf("D1") }

    // Get current chart data based on selection
    val currentChartData = remember(selectedChartType, divisionalCharts) {
        when (selectedChartType) {
            "D1" -> null // Lagna chart uses the main chart data
            "D2" -> divisionalCharts.find { it.chartType == DivisionalChartType.D2_HORA }
            "D3" -> divisionalCharts.find { it.chartType == DivisionalChartType.D3_DREKKANA }
            "D4" -> divisionalCharts.find { it.chartType == DivisionalChartType.D4_CHATURTHAMSA }
            "D7" -> divisionalCharts.find { it.chartType == DivisionalChartType.D7_SAPTAMSA }
            "D9" -> divisionalCharts.find { it.chartType == DivisionalChartType.D9_NAVAMSA }
            "D10" -> divisionalCharts.find { it.chartType == DivisionalChartType.D10_DASAMSA }
            "D12" -> divisionalCharts.find { it.chartType == DivisionalChartType.D12_DWADASAMSA }
            "D16" -> divisionalCharts.find { it.chartType == DivisionalChartType.D16_SHODASAMSA }
            "D20" -> divisionalCharts.find { it.chartType == DivisionalChartType.D20_VIMSAMSA }
            "D24" -> divisionalCharts.find { it.chartType == DivisionalChartType.D24_CHATURVIMSAMSA }
            "D27" -> divisionalCharts.find { it.chartType == DivisionalChartType.D27_SAPTAVIMSAMSA }
            "D30" -> divisionalCharts.find { it.chartType == DivisionalChartType.D30_TRIMSAMSA }
            "D60" -> divisionalCharts.find { it.chartType == DivisionalChartType.D60_SHASHTIAMSA }
            else -> null
        }
    }

    // Get chart title and description
    val chartInfo = remember(selectedChartType) {
        when (selectedChartType) {
            "D1" -> Triple("Lagna Chart (Rashi)", "Physical Body, General Life", "D1")
            "D2" -> Triple("Hora Chart", "Wealth, Prosperity", "D2")
            "D3" -> Triple("Drekkana Chart", "Siblings, Courage, Vitality", "D3")
            "D4" -> Triple("Chaturthamsa Chart", "Fortune, Property", "D4")
            "D7" -> Triple("Saptamsa Chart", "Children, Progeny", "D7")
            "D9" -> Triple("Navamsa Chart", "Marriage, Dharma, Fortune", "D9")
            "D10" -> Triple("Dasamsa Chart", "Career, Profession", "D10")
            "D12" -> Triple("Dwadasamsa Chart", "Parents, Ancestry", "D12")
            "D16" -> Triple("Shodasamsa Chart", "Vehicles, Pleasures", "D16")
            "D20" -> Triple("Vimsamsa Chart", "Spiritual Life", "D20")
            "D24" -> Triple("Siddhamsa Chart", "Education, Learning", "D24")
            "D27" -> Triple("Bhamsa Chart", "Strength, Weakness", "D27")
            "D30" -> Triple("Trimsamsa Chart", "Evils, Misfortunes", "D30")
            "D60" -> Triple("Shashtiamsa Chart", "Past Life Karma", "D60")
            else -> Triple("Chart", "", selectedChartType)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Chart type selector
        item {
            ChartTypeSelector(
                selectedType = selectedChartType,
                onTypeSelected = { selectedChartType = it }
            )
        }

        // Main chart display
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = CardBackground
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    // Chart title with description
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = chartInfo.first,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentGold
                            )
                            if (chartInfo.second.isNotEmpty()) {
                                Text(
                                    text = chartInfo.second,
                                    fontSize = 12.sp,
                                    color = TextMuted
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentGold.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = chartInfo.third,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chart canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            if (selectedChartType == "D1") {
                                chartRenderer.drawNorthIndianChart(
                                    drawScope = this,
                                    chart = chart,
                                    size = size.minDimension,
                                    chartTitle = "Lagna"
                                )
                            } else {
                                currentChartData?.let {
                                    chartRenderer.drawDivisionalChart(
                                        drawScope = this,
                                        planetPositions = it.planetPositions,
                                        ascendantLongitude = it.ascendantLongitude,
                                        size = size.minDimension,
                                        chartTitle = chartInfo.third
                                    )
                                }
                            }
                        }
                    }

                    // Legend for status indicators
                    Spacer(modifier = Modifier.height(12.dp))
                    ChartLegend()
                }
            }
        }

        // Chart Details - Planetary positions for selected chart
        item {
            ChartDetailsCard(
                chart = chart,
                currentChartData = currentChartData,
                selectedChartType = selectedChartType
            )
        }

        // Birth information
        item {
            BirthInfoCard(chart)
        }

        // Astronomical data
        item {
            AstronomicalDataCard(chart)
        }
    }
}

@Composable
private fun ChartLegend() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFF1A1512),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem("*", "Retrograde")
        LegendItem("\u2191", "Exalted")
        LegendItem("\u2193", "Debilitated")
    }
}

@Composable
private fun LegendItem(symbol: String, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = symbol,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = AccentGold
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun ChartDetailsCard(
    chart: VedicChart,
    currentChartData: DivisionalChartData?,
    selectedChartType: String
) {
    val planetPositions = if (selectedChartType == "D1") {
        chart.planetPositions
    } else {
        currentChartData?.planetPositions ?: emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Planetary Positions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            // Ascendant info
            if (selectedChartType == "D1") {
                val ascSign = com.astro.storm.data.model.ZodiacSign.fromLongitude(chart.ascendant)
                val ascDegree = chart.ascendant % 30.0
                PlanetPositionRow(
                    name = "Ascendant",
                    sign = ascSign.displayName,
                    degree = "${ascDegree.toInt()}°",
                    house = "1",
                    isRetrograde = false,
                    color = AccentGold
                )
                HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
            } else {
                currentChartData?.let { data ->
                    val ascSign = com.astro.storm.data.model.ZodiacSign.fromLongitude(data.ascendantLongitude)
                    val ascDegree = data.ascendantLongitude % 30.0
                    PlanetPositionRow(
                        name = "Ascendant",
                        sign = ascSign.displayName,
                        degree = "${ascDegree.toInt()}°",
                        house = "1",
                        isRetrograde = false,
                        color = AccentGold
                    )
                    HorizontalDivider(color = DividerColor, modifier = Modifier.padding(vertical = 8.dp))
                }
            }

            // Planet positions
            planetPositions.forEach { position ->
                PlanetPositionRow(
                    name = position.planet.displayName,
                    sign = position.sign.displayName,
                    degree = "${(position.longitude % 30.0).toInt()}°",
                    house = position.house.toString(),
                    isRetrograde = position.isRetrograde,
                    color = when (position.planet) {
                        com.astro.storm.data.model.Planet.SUN -> Color(0xFFD2691E)
                        com.astro.storm.data.model.Planet.MOON -> Color(0xFFDC143C)
                        com.astro.storm.data.model.Planet.MARS -> Color(0xFFDC143C)
                        com.astro.storm.data.model.Planet.MERCURY -> Color(0xFF228B22)
                        com.astro.storm.data.model.Planet.JUPITER -> Color(0xFFDAA520)
                        com.astro.storm.data.model.Planet.VENUS -> Color(0xFF9370DB)
                        com.astro.storm.data.model.Planet.SATURN -> Color(0xFF4169E1)
                        com.astro.storm.data.model.Planet.RAHU -> Color(0xFF8B0000)
                        com.astro.storm.data.model.Planet.KETU -> Color(0xFF8B0000)
                        else -> TextPrimary
                    }
                )
            }
        }
    }
}

@Composable
private fun PlanetPositionRow(
    name: String,
    sign: String,
    degree: String,
    house: String,
    isRetrograde: Boolean,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = name,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = color,
                modifier = Modifier.width(70.dp)
            )
            if (isRetrograde) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = WarningColor.copy(alpha = 0.2f),
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Text(
                        text = "R",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = WarningColor,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Text(
            text = sign,
            fontSize = 13.sp,
            color = AccentTeal,
            modifier = Modifier.width(80.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = degree,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center
        )

        Text(
            text = "H$house",
            fontSize = 12.sp,
            color = TextMuted,
            modifier = Modifier.width(30.dp),
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun ChartTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    // All available divisional charts
    val chartTypes = listOf(
        "D1" to "Lagna",
        "D2" to "Hora",
        "D3" to "Drekkana",
        "D4" to "D4",
        "D7" to "Saptamsa",
        "D9" to "Navamsa",
        "D10" to "Dasamsa",
        "D12" to "D12",
        "D16" to "D16",
        "D20" to "D20",
        "D24" to "D24",
        "D27" to "Bhamsa",
        "D30" to "D30",
        "D60" to "D60"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(chartTypes) { (type, name) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = {
                    Text(
                        text = name,
                        fontSize = 12.sp
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = AccentGold.copy(alpha = 0.2f),
                    selectedLabelColor = AccentGold,
                    containerColor = CardBackground,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = DividerColor,
                    selectedBorderColor = AccentGold,
                    enabled = true,
                    selected = selectedType == type
                )
            )
        }
    }
}

@Composable
private fun BirthInfoCard(chart: VedicChart) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Birth Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            InfoRow("Name", chart.birthData.name)
            InfoRow(
                "Date & Time",
                chart.birthData.dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy • hh:mm:ss a"))
            )
            InfoRow("Location", chart.birthData.location)
            InfoRow(
                "Coordinates",
                "${formatCoordinate(chart.birthData.latitude, true)}, ${formatCoordinate(chart.birthData.longitude, false)}"
            )
            InfoRow("Timezone", chart.birthData.timezone)
        }
    }
}

@Composable
private fun AstronomicalDataCard(chart: VedicChart) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Astronomical Data",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                val rotation by animateFloatAsState(
                    targetValue = if (expanded) 180f else 0f,
                    label = "rotation"
                )
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextMuted,
                    modifier = Modifier.rotate(rotation)
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    InfoRow("Julian Day", String.format("%.6f", chart.julianDay))
                    InfoRow("Ayanamsa", "${chart.ayanamsaName} (${formatDegree(chart.ayanamsa)})")
                    InfoRow("Ascendant", formatDegree(chart.ascendant))
                    InfoRow("Midheaven", formatDegree(chart.midheaven))
                    InfoRow("House System", chart.houseSystem.displayName)
                }
            }
        }
    }
}

// ============ PLANETS TAB ============

@Composable
private fun PlanetsTabContent(chart: VedicChart) {
    val conditionAnalysis = remember(chart) {
        RetrogradeCombustionCalculator.analyzePlanetaryConditions(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Status summary
        item {
            PlanetaryStatusSummary(conditionAnalysis)
        }

        // Individual planet cards
        items(chart.planetPositions) { position ->
            val condition = conditionAnalysis.planetConditions.find { it.planet == position.planet }
            PlanetDetailCard(position, condition)
        }
    }
}

@Composable
private fun PlanetaryStatusSummary(analysis: RetrogradeCombustionCalculator.PlanetaryConditionAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatusBadge(
                count = analysis.currentRetrogrades.size,
                label = "Retrograde",
                color = WarningColor
            )
            StatusBadge(
                count = analysis.currentCombustions.size,
                label = "Combust",
                color = ErrorColor
            )
            StatusBadge(
                count = analysis.planetaryWars.size,
                label = "In War",
                color = AccentPurple
            )
        }
    }
}

@Composable
private fun StatusBadge(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = count.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun PlanetDetailCard(
    position: com.astro.storm.data.model.PlanetPosition,
    condition: RetrogradeCombustionCalculator.PlanetCondition?
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Planet header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = position.planet.symbol,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (position.isRetrograde) WarningColor else AccentGold
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = position.planet.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (position.isRetrograde) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = WarningColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "R",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = WarningColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "House ${position.house}",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = position.sign.displayName,
                        fontSize = 14.sp,
                        color = AccentTeal
                    )
                    Text(
                        text = formatDegreeInSign(position.longitude),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = DividerColor)
            Spacer(modifier = Modifier.height(12.dp))

            // Nakshatra info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Nakshatra", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = "${position.nakshatra.displayName} • Pada ${position.nakshatraPada}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Lord", fontSize = 11.sp, color = TextMuted)
                    Text(
                        text = position.nakshatra.ruler.displayName,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                }
            }

            // Condition info
            condition?.let { cond ->
                if (cond.combustionStatus != RetrogradeCombustionCalculator.CombustionStatus.NOT_COMBUST ||
                    cond.isInPlanetaryWar) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (cond.combustionStatus != RetrogradeCombustionCalculator.CombustionStatus.NOT_COMBUST) {
                            ConditionChip(
                                label = cond.combustionStatus.displayName,
                                color = ErrorColor
                            )
                        }
                        if (cond.isInPlanetaryWar) {
                            ConditionChip(
                                label = "War with ${cond.warOpponent?.displayName}",
                                color = AccentPurple
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ConditionChip(label: String, color: Color) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

// ============ DASHAS TAB ============

@Composable
private fun DashasTabContent(chart: VedicChart) {
    val dashaTimeline = remember(chart) {
        DashaCalculator.calculateDashaTimeline(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Current period
        item {
            CurrentDashaPeriodCard(dashaTimeline)
        }

        // Birth nakshatra info
        item {
            DashaBirthInfoCard(dashaTimeline)
        }

        // Mahadasha timeline
        item {
            Text(
                text = "Mahadasha Timeline",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(dashaTimeline.mahadashas.take(12)) { mahadasha ->
            MahadashaCard(mahadasha, dashaTimeline.currentMahadasha == mahadasha)
        }
    }
}

@Composable
private fun CurrentDashaPeriodCard(timeline: DashaCalculator.DashaTimeline) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Period",
                fontSize = 14.sp,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            timeline.currentMahadasha?.let { md ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "${md.planet.displayName} Mahadasha",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGold
                        )
                        timeline.currentAntardasha?.let { ad ->
                            Text(
                                text = "${ad.planet.displayName} Bhukti",
                                fontSize = 14.sp,
                                color = AccentTeal
                            )
                        }
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Until",
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                        Text(
                            text = md.endDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashaBirthInfoCard(timeline: DashaCalculator.DashaTimeline) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackgroundElevated
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Birth Nakshatra", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = timeline.birthNakshatra.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Nakshatra Lord", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = timeline.birthNakshatraLord.displayName,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = AccentGold
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Balance at Birth", fontSize = 11.sp, color = TextMuted)
                Text(
                    text = "${String.format("%.2f", timeline.balanceOfFirstDasha)} yrs",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun MahadashaCard(mahadasha: DashaCalculator.Mahadasha, isActive: Boolean) {
    var expanded by remember { mutableStateOf(isActive) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isActive) AccentGold.copy(alpha = 0.1f) else CardBackground,
        border = if (isActive) androidx.compose.foundation.BorderStroke(1.dp, AccentGold) else null
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mahadasha.planet.symbol,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) AccentGold else TextPrimary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = mahadasha.planet.displayName,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = SuccessColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "ACTIVE",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SuccessColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${String.format("%.1f", mahadasha.durationYears)} years",
                            fontSize = 12.sp,
                            color = TextMuted
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = mahadasha.startDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Text(
                        text = mahadasha.endDate.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                }
            }

            // Antardashas
            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    HorizontalDivider(color = DividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Bhuktis (Sub-periods)",
                        fontSize = 12.sp,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    mahadasha.antardashas.forEach { antardasha ->
                        val isActiveAntardasha = antardasha.isActive
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    if (isActiveAntardasha) AccentTeal.copy(alpha = 0.1f)
                                    else Color.Transparent,
                                    RoundedCornerShape(6.dp)
                                )
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = antardasha.planet.displayName,
                                fontSize = 13.sp,
                                color = if (isActiveAntardasha) AccentTeal else TextSecondary
                            )
                            Text(
                                text = "${antardasha.startDate.format(DateTimeFormatter.ofPattern("MM/yy"))} - ${antardasha.endDate.format(DateTimeFormatter.ofPattern("MM/yy"))}",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============ ASPECTS TAB ============

@Composable
private fun AspectsTabContent(chart: VedicChart) {
    val aspectMatrix = remember(chart) {
        AspectCalculator.calculateAspectMatrix(chart)
    }
    val yogas = remember(chart) {
        AspectCalculator.detectYogas(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Yogas section
        if (yogas.isNotEmpty()) {
            item {
                YogasSection(yogas)
            }
        }

        // Aspect summary
        item {
            AspectSummaryCard(aspectMatrix)
        }

        // Major aspects
        item {
            Text(
                text = "Planetary Aspects",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }

        items(aspectMatrix.aspects.take(15)) { aspect ->
            AspectCard(aspect)
        }
    }
}

@Composable
private fun YogasSection(yogas: List<AspectCalculator.Yoga>) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.AutoAwesome,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Yogas Detected",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }

            yogas.forEach { yoga ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = if (yoga.isAuspicious) SuccessColor.copy(alpha = 0.1f)
                    else WarningColor.copy(alpha = 0.1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = yoga.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (yoga.isAuspicious) SuccessColor else WarningColor
                            )
                            Text(
                                text = "${(yoga.strength * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = TextMuted
                            )
                        }
                        Text(
                            text = yoga.description,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectSummaryCard(matrix: AspectCalculator.AspectMatrix) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AspectCountBadge("Conjunctions", matrix.conjunctions.size, AccentGold)
            AspectCountBadge("Trines", matrix.trines.size, SuccessColor)
            AspectCountBadge("Squares", matrix.squares.size, ErrorColor)
            AspectCountBadge("Special", matrix.vedicSpecialAspects.size, AccentPurple)
        }
    }
}

@Composable
private fun AspectCountBadge(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count.toString(),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun AspectCard(aspect: AspectCalculator.AspectData) {
    val natureColor = when (aspect.aspectType.nature) {
        AspectCalculator.AspectNature.HARMONIOUS -> SuccessColor
        AspectCalculator.AspectNature.CHALLENGING -> ErrorColor
        AspectCalculator.AspectNature.VARIABLE -> WarningColor
        AspectCalculator.AspectNature.SIGNIFICANT -> AccentPurple
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${aspect.planet1.symbol} ${aspect.aspectType.symbol} ${aspect.planet2.symbol}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = natureColor
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "${aspect.planet1.displayName} - ${aspect.planet2.displayName}",
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = aspect.aspectType.displayName,
                        fontSize = 11.sp,
                        color = TextMuted
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = aspect.strengthDescription,
                    fontSize = 12.sp,
                    color = natureColor
                )
                Text(
                    text = "Orb: ${String.format("%.1f", aspect.orb)}°",
                    fontSize = 11.sp,
                    color = TextMuted
                )
            }
        }
    }
}

// ============ PANCHANGA TAB ============

@Composable
private fun PanchangaTabContent(chart: VedicChart, context: android.content.Context) {
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
                // Handle error
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        panchangaData?.let { panchanga ->
            // Main Panchanga elements
            item {
                PanchangaMainCard(panchanga)
            }

            // Tithi
            item {
                PanchangaElementCard(
                    title = "Tithi",
                    icon = Icons.Outlined.Brightness2,
                    mainValue = panchanga.tithi.tithi.displayName,
                    subValue = panchanga.paksha.displayName,
                    detail = "Lord: ${panchanga.tithi.lord.displayName}",
                    progress = panchanga.tithi.progress.toFloat() / 100f,
                    color = AccentGold
                )
            }

            // Nakshatra
            item {
                PanchangaElementCard(
                    title = "Nakshatra",
                    icon = Icons.Outlined.Star,
                    mainValue = panchanga.nakshatra.nakshatra.displayName,
                    subValue = "Pada ${panchanga.nakshatra.pada}",
                    detail = "Lord: ${panchanga.nakshatra.lord.displayName}",
                    progress = panchanga.nakshatra.progress.toFloat() / 100f,
                    color = AccentTeal
                )
            }

            // Yoga
            item {
                PanchangaElementCard(
                    title = "Yoga",
                    icon = Icons.Outlined.AllInclusive,
                    mainValue = panchanga.yoga.yoga.displayName,
                    subValue = panchanga.yoga.yoga.nature,
                    detail = null,
                    progress = panchanga.yoga.progress.toFloat() / 100f,
                    color = AccentPurple
                )
            }

            // Karana
            item {
                PanchangaElementCard(
                    title = "Karana",
                    icon = Icons.Outlined.HourglassEmpty,
                    mainValue = panchanga.karana.karana.displayName,
                    subValue = panchanga.karana.karana.nature,
                    detail = null,
                    progress = panchanga.karana.progress.toFloat() / 100f,
                    color = AccentBlue
                )
            }

            // Vara
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardBackground
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Vara (Day)", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = panchanga.vara.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("Day Lord", fontSize = 12.sp, color = TextMuted)
                            Text(
                                text = panchanga.vara.lord.displayName,
                                fontSize = 14.sp,
                                color = AccentGold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PanchangaMainCard(panchanga: PanchangaData) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Sunrise", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = panchanga.sunrise,
                        fontSize = 16.sp,
                        color = WarningColor
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Moon Phase", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = "${String.format("%.0f", panchanga.moonPhase)}%",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Sunset", fontSize = 12.sp, color = TextMuted)
                    Text(
                        text = panchanga.sunset,
                        fontSize = 16.sp,
                        color = AccentPurple
                    )
                }
            }
        }
    }
}

@Composable
private fun PanchangaElementCard(
    title: String,
    icon: ImageVector,
    mainValue: String,
    subValue: String,
    detail: String?,
    progress: Float,
    color: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(title, fontSize = 12.sp, color = TextMuted)
                        Text(
                            text = mainValue,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = subValue,
                        fontSize = 13.sp,
                        color = color
                    )
                    detail?.let {
                        Text(
                            text = it,
                            fontSize = 11.sp,
                            color = TextMuted
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = color,
                trackColor = color.copy(alpha = 0.2f)
            )

            Text(
                text = "${(progress * 100).toInt()}% complete",
                fontSize = 10.sp,
                color = TextMuted,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ============ UTILITY FUNCTIONS ============

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = TextMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = TextPrimary
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
