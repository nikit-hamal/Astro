package com.astro.storm.ui.screen.chartdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DivisionalChartData
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.components.FullScreenChartDialog
import com.astro.storm.ui.components.HouseDetailDialog
import com.astro.storm.ui.components.NakshatraDetailDialog
import com.astro.storm.ui.components.PlanetDetailDialog
import com.astro.storm.ui.components.ShadbalaDialog
import com.astro.storm.ui.screen.chartdetail.tabs.AshtakavargaTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.ChartTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.DashasTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.PanchangaTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.PlanetsTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.TransitsTabContent
import com.astro.storm.ui.screen.chartdetail.tabs.YogasTabContent
import com.astro.storm.viewmodel.ChartDetailViewModel

/**
 * Main Chart Detail Screen displaying comprehensive Vedic chart analysis.
 *
 * Features:
 * - 7 tabbed sections (Chart, Planets, Yogas, Ashtakavarga, Transits, Dashas, Panchanga)
 * - Interactive charts with zoom/pan and download
 * - Detailed planet, house, and nakshatra dialogs
 * - Export and share functionality
 *
 * Architecture:
 * - This screen serves as the orchestrator, delegating content to tab-specific composables
 * - Dialog state management is centralized here
 * - Uses Hilt ViewModel for data management
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartDetailScreen(
    chartId: Long,
    onNavigateBack: () -> Unit,
    onEditChart: (Long) -> Unit,
    viewModel: ChartDetailViewModel = hiltViewModel()
) {
    val chart by viewModel.chart.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(chartId) {
        viewModel.loadChart(chartId)
    }

    chart?.let { vedicChart ->
        ChartDetailContent(
            chart = vedicChart,
            onNavigateBack = onNavigateBack,
            onEditChart = { onEditChart(chartId) },
            onDeleteChart = {
                viewModel.deleteChart(chartId)
                onNavigateBack()
            },
            onExportChart = { viewModel.exportChart(vedicChart, context) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ChartDetailContent(
    chart: VedicChart,
    onNavigateBack: () -> Unit,
    onEditChart: () -> Unit,
    onDeleteChart: () -> Unit,
    onExportChart: () -> Unit
) {
    val context = LocalContext.current
    val chartRenderer = remember { ChartRenderer() }

    // Tab state
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = ChartTab.getOrderedTabs()

    // Dialog states
    var showMenuDropdown by remember { mutableStateOf(false) }
    var showFullScreenChart by remember { mutableStateOf(false) }
    var fullScreenChartTitle by remember { mutableStateOf("") }
    var fullScreenChartData by remember { mutableStateOf<DivisionalChartData?>(null) }

    var selectedPlanet by remember { mutableStateOf<PlanetPosition?>(null) }
    var selectedHouse by remember { mutableStateOf<Int?>(null) }
    var selectedNakshatra by remember { mutableStateOf<Pair<com.astro.storm.data.model.Nakshatra, Int>?>(null) }
    var showShadbalaDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChartDetailColors.ScreenBackground)
    ) {
        // Top App Bar
        TopAppBar(
            title = {
                Text(
                    text = chart.birthData.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back"
                    )
                }
            },
            actions = {
                IconButton(onClick = { showMenuDropdown = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More options")
                }
                DropdownMenu(
                    expanded = showMenuDropdown,
                    onDismissRequest = { showMenuDropdown = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenuDropdown = false
                            onEditChart()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Export") },
                        onClick = {
                            showMenuDropdown = false
                            onExportChart()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Share, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenuDropdown = false
                            onDeleteChart()
                        },
                        leadingIcon = { Icon(Icons.Outlined.Delete, contentDescription = null) }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = ChartDetailColors.SurfaceColor,
                titleContentColor = ChartDetailColors.TextPrimary,
                navigationIconContentColor = ChartDetailColors.TextPrimary,
                actionIconContentColor = ChartDetailColors.TextPrimary
            )
        )

        // Tab Row
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = ChartDetailColors.SurfaceColor,
            contentColor = ChartDetailColors.TextPrimary,
            edgePadding = 16.dp,
            divider = {}
        ) {
            tabs.forEachIndexed { index, tab ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = tab.title,
                            fontSize = 13.sp,
                            fontWeight = if (selectedTabIndex == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    icon = {
                        Icon(
                            tab.icon,
                            contentDescription = tab.title,
                            tint = if (selectedTabIndex == index) {
                                ChartDetailColors.AccentGold
                            } else {
                                ChartDetailColors.TextMuted
                            }
                        )
                    },
                    selectedContentColor = ChartDetailColors.AccentGold,
                    unselectedContentColor = ChartDetailColors.TextMuted
                )
            }
        }

        // Tab Content with animation
        AnimatedContent(
            targetState = selectedTabIndex,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInHorizontally { it } + fadeIn()) togetherWith
                            (slideOutHorizontally { -it } + fadeOut())
                } else {
                    (slideInHorizontally { -it } + fadeIn()) togetherWith
                            (slideOutHorizontally { it } + fadeOut())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            label = "TabContent"
        ) { tabIndex ->
            when (tabs[tabIndex]) {
                ChartTab.CHART -> ChartTabContent(
                    chart = chart,
                    chartRenderer = chartRenderer,
                    context = context,
                    onChartClick = { title, data ->
                        fullScreenChartTitle = title
                        fullScreenChartData = data
                        showFullScreenChart = true
                    },
                    onPlanetClick = { selectedPlanet = it },
                    onHouseClick = { selectedHouse = it }
                )

                ChartTab.PLANETS -> PlanetsTabContent(
                    chart = chart,
                    onPlanetClick = { selectedPlanet = it },
                    onShadbalaClick = { showShadbalaDialog = true }
                )

                ChartTab.YOGAS -> YogasTabContent(chart = chart)

                ChartTab.ASHTAKAVARGA -> AshtakavargaTabContent(chart = chart)

                ChartTab.TRANSITS -> TransitsTabContent(chart = chart)

                ChartTab.DASHAS -> DashasTabContent(chart = chart)

                ChartTab.PANCHANGA -> PanchangaTabContent(chart = chart)
            }
        }
    }

    // Dialogs
    if (showFullScreenChart) {
        FullScreenChartDialog(
            chart = chart,
            chartRenderer = chartRenderer,
            chartTitle = fullScreenChartTitle,
            divisionalChartData = fullScreenChartData,
            onDismiss = { showFullScreenChart = false }
        )
    }

    selectedPlanet?.let { planet ->
        PlanetDetailDialog(
            planetPosition = planet,
            chart = chart,
            onDismiss = { selectedPlanet = null }
        )
    }

    selectedHouse?.let { house ->
        val planetsInHouse = chart.planetPositions.filter { it.house == house }
        HouseDetailDialog(
            houseNumber = house,
            houseCusp = chart.houseCusps.getOrElse(house - 1) { 0.0 },
            planetsInHouse = planetsInHouse,
            chart = chart,
            onDismiss = { selectedHouse = null }
        )
    }

    selectedNakshatra?.let { (nakshatra, pada) ->
        NakshatraDetailDialog(
            nakshatra = nakshatra,
            pada = pada,
            onDismiss = { selectedNakshatra = null }
        )
    }

    if (showShadbalaDialog) {
        ShadbalaDialog(
            chart = chart,
            onDismiss = { showShadbalaDialog = false }
        )
    }
}
