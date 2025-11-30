package com.astro.storm.ui.screen.chartdetail.tabs

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.PlanetPosition
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.DivisionalChartCalculator
import com.astro.storm.ephemeris.DivisionalChartData
import com.astro.storm.ephemeris.DivisionalChartType
import com.astro.storm.ui.chart.ChartRenderer
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import com.astro.storm.ui.screen.chartdetail.ChartDetailUtils
import java.time.format.DateTimeFormatter

/**
 * Chart tab content displaying divisional charts and planetary positions.
 */
@Composable
fun ChartTabContent(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    context: Context,
    onChartClick: (String, DivisionalChartData?) -> Unit,
    onPlanetClick: (PlanetPosition) -> Unit,
    onHouseClick: (Int) -> Unit
) {
    val divisionalCharts = remember(chart) {
        DivisionalChartCalculator.calculateAllDivisionalCharts(chart)
    }

    var selectedChartType by remember { mutableStateOf("D1") }

    val currentChartData = remember(selectedChartType, divisionalCharts) {
        getChartDataForType(selectedChartType, divisionalCharts)
    }

    val chartInfo = remember(selectedChartType) {
        getChartInfo(selectedChartType)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ChartTypeSelector(
                selectedType = selectedChartType,
                onTypeSelected = { selectedChartType = it }
            )
        }

        item {
            MainChartCard(
                chart = chart,
                chartRenderer = chartRenderer,
                chartInfo = chartInfo,
                selectedChartType = selectedChartType,
                currentChartData = currentChartData,
                onChartClick = onChartClick
            )
        }

        item {
            ChartDetailsCard(
                chart = chart,
                currentChartData = currentChartData,
                selectedChartType = selectedChartType,
                onPlanetClick = onPlanetClick
            )
        }

        item {
            HouseCuspsCard(
                chart = chart,
                onHouseClick = onHouseClick
            )
        }

        item {
            BirthInfoCard(chart = chart)
        }

        item {
            AstronomicalDataCard(chart = chart)
        }
    }
}

private fun getChartDataForType(
    type: String,
    divisionalCharts: List<DivisionalChartData>
): DivisionalChartData? {
    return when (type) {
        "D1" -> null
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

private fun getChartInfo(type: String): Triple<String, String, String> {
    return when (type) {
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
        else -> Triple("Chart", "", type)
    }
}

@Composable
private fun ChartTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
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

    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(chartTypes) { (type, name) ->
            FilterChip(
                selected = selectedType == type,
                onClick = { onTypeSelected(type) },
                label = { Text(text = name, fontSize = 12.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ChartDetailColors.AccentGold.copy(alpha = 0.2f),
                    selectedLabelColor = ChartDetailColors.AccentGold,
                    containerColor = ChartDetailColors.CardBackground,
                    labelColor = ChartDetailColors.TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = ChartDetailColors.DividerColor,
                    selectedBorderColor = ChartDetailColors.AccentGold,
                    enabled = true,
                    selected = selectedType == type
                )
            )
        }
    }
}

@Composable
private fun MainChartCard(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    chartInfo: Triple<String, String, String>,
    selectedChartType: String,
    currentChartData: DivisionalChartData?,
    onChartClick: (String, DivisionalChartData?) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onChartClick(chartInfo.first, currentChartData) },
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                        color = ChartDetailColors.AccentGold
                    )
                    if (chartInfo.second.isNotEmpty()) {
                        Text(
                            text = chartInfo.second,
                            fontSize = 12.sp,
                            color = ChartDetailColors.TextMuted
                        )
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Fullscreen,
                        contentDescription = "View fullscreen",
                        tint = ChartDetailColors.TextMuted,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = ChartDetailColors.AccentGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = chartInfo.third,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChartDetailColors.AccentGold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

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
                                chartTitle = chartInfo.third,
                                originalChart = chart
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            ChartLegend()

            Text(
                text = "Tap chart to view fullscreen with download option",
                fontSize = 11.sp,
                color = ChartDetailColors.TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            )
        }
    }
}

@Composable
private fun ChartLegend() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = ChartDetailColors.ChartBackground,
                shape = RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem("*", "Retrograde")
            LegendItem("^", "Combust")
            LegendItem("\u00A4", "Vargottama")
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem("\u2191", "Exalted")
            LegendItem("\u2193", "Debilitated")
        }
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
            color = ChartDetailColors.AccentGold
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun ChartDetailsCard(
    chart: VedicChart,
    currentChartData: DivisionalChartData?,
    selectedChartType: String,
    onPlanetClick: (PlanetPosition) -> Unit
) {
    val planetPositions = if (selectedChartType == "D1") {
        chart.planetPositions
    } else {
        currentChartData?.planetPositions ?: emptyList()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Star,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Planetary Positions",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "Tap for details",
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }

            if (selectedChartType == "D1") {
                AscendantRow(chart = chart)
                HorizontalDivider(
                    color = ChartDetailColors.DividerColor,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            planetPositions.forEach { position ->
                ClickablePlanetPositionRow(
                    position = position,
                    onClick = { onPlanetClick(position) }
                )
            }
        }
    }
}

@Composable
private fun AscendantRow(chart: VedicChart) {
    val ascSign = ZodiacSign.fromLongitude(chart.ascendant)
    val ascDegree = chart.ascendant % 30.0

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        color = ChartDetailColors.AccentGold.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ascendant (Lagna)",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = ChartDetailColors.AccentGold
            )
            Row {
                Text(
                    text = ascSign.displayName,
                    fontSize = 13.sp,
                    color = ChartDetailColors.AccentTeal
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${ascDegree.toInt()}°",
                    fontSize = 13.sp,
                    color = ChartDetailColors.TextSecondary
                )
            }
        }
    }
}

@Composable
private fun ClickablePlanetPositionRow(
    position: PlanetPosition,
    onClick: () -> Unit
) {
    val color = ChartDetailColors.getPlanetColor(position.planet)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(6.dp),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = position.planet.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    modifier = Modifier.width(70.dp)
                )
                if (position.isRetrograde) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ChartDetailColors.WarningColor.copy(alpha = 0.2f),
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Text(
                            text = "R",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = ChartDetailColors.WarningColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }
                }
            }

            Text(
                text = position.sign.displayName,
                fontSize = 13.sp,
                color = ChartDetailColors.AccentTeal,
                modifier = Modifier.width(80.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "${(position.longitude % 30.0).toInt()}°",
                fontSize = 13.sp,
                color = ChartDetailColors.TextSecondary,
                modifier = Modifier.width(40.dp),
                textAlign = TextAlign.Center
            )

            Text(
                text = "H${position.house}",
                fontSize = 12.sp,
                color = ChartDetailColors.TextMuted,
                modifier = Modifier.width(30.dp),
                textAlign = TextAlign.End
            )

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = ChartDetailColors.TextMuted,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun HouseCuspsCard(
    chart: VedicChart,
    onHouseClick: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
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
                        Icons.Outlined.Home,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "House Cusps",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Tap house for details",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = ChartDetailColors.TextMuted,
                        modifier = Modifier.rotate(rotation)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    for (row in 0..5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val house1 = row + 1
                            val house2 = row + 7

                            HouseCuspItem(
                                houseNumber = house1,
                                cusp = chart.houseCusps.getOrNull(house1 - 1) ?: 0.0,
                                modifier = Modifier.weight(1f),
                                onClick = { onHouseClick(house1) }
                            )
                            HouseCuspItem(
                                houseNumber = house2,
                                cusp = chart.houseCusps.getOrNull(house2 - 1) ?: 0.0,
                                modifier = Modifier.weight(1f),
                                onClick = { onHouseClick(house2) }
                            )
                        }
                        if (row < 5) Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun HouseCuspItem(
    houseNumber: Int,
    cusp: Double,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val sign = ZodiacSign.fromLongitude(cusp)
    val degreeInSign = cusp % 30.0

    Surface(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "H$houseNumber",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = ChartDetailColors.AccentGold
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = sign.abbreviation,
                    fontSize = 12.sp,
                    color = ChartDetailColors.AccentTeal
                )
                Text(
                    text = "${degreeInSign.toInt()}°",
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun BirthInfoCard(chart: VedicChart) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(
                    Icons.Outlined.Person,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Birth Information",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
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
                "${ChartDetailUtils.formatCoordinate(chart.birthData.latitude, true)}, ${ChartDetailUtils.formatCoordinate(chart.birthData.longitude, false)}"
            )
            InfoRow("Timezone", chart.birthData.timezone)
        }
    }
}

@Composable
private fun AstronomicalDataCard(chart: VedicChart) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
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
                        tint = ChartDetailColors.AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Astronomical Data",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextPrimary
                    )
                }
                Icon(
                    Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = ChartDetailColors.TextMuted,
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
                    InfoRow("Ayanamsa", "${chart.ayanamsaName} (${ChartDetailUtils.formatDegree(chart.ayanamsa)})")
                    InfoRow("Ascendant", ChartDetailUtils.formatDegree(chart.ascendant))
                    InfoRow("Midheaven", ChartDetailUtils.formatDegree(chart.midheaven))
                    InfoRow("House System", chart.houseSystem.displayName)
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
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = ChartDetailColors.TextMuted
        )
        Text(
            text = value,
            fontSize = 13.sp,
            color = ChartDetailColors.TextPrimary
        )
    }
}
