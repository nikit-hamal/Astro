package com.astro.storm.ui.screen.chartdetail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.GridOn
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.TrendingUp
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.data.model.ZodiacSign
import com.astro.storm.ephemeris.AshtakavargaCalculator
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors

/**
 * Ashtakavarga tab content displaying SAV and BAV analysis with interpretation.
 */
@Composable
fun AshtakavargaTabContent(chart: VedicChart) {
    val ashtakavarga = remember(chart) {
        AshtakavargaCalculator.calculateAshtakavarga(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            AshtakavargaSummaryCard(ashtakavarga)
        }

        item {
            SarvashtakavargaCard(ashtakavarga)
        }

        item {
            BhinnashtakavargaCard(ashtakavarga)
        }

        item {
            InterpretationGuideCard()
        }
    }
}

@Composable
private fun AshtakavargaSummaryCard(ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    Icons.Outlined.GridOn,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ashtakavarga Summary",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                SummaryItem(
                    label = "Total SAV",
                    value = ashtakavarga.sarvashtakavarga.totalBindus.toString(),
                    color = ChartDetailColors.AccentGold
                )
                SummaryItem(
                    label = "Strongest",
                    value = ashtakavarga.sarvashtakavarga.strongestSign.abbreviation,
                    color = ChartDetailColors.SuccessColor
                )
                SummaryItem(
                    label = "Weakest",
                    value = ashtakavarga.sarvashtakavarga.weakestSign.abbreviation,
                    color = ChartDetailColors.ErrorColor
                )
            }

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            Text(
                text = "Quick Analysis",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            val strongSignCount = ZodiacSign.entries.count {
                ashtakavarga.sarvashtakavarga.getBindusForSign(it) >= 28
            }
            val weakSignCount = ZodiacSign.entries.count {
                ashtakavarga.sarvashtakavarga.getBindusForSign(it) < 25
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Favorable Signs (28+):",
                    fontSize = 13.sp,
                    color = ChartDetailColors.TextMuted
                )
                Text(
                    text = "$strongSignCount signs",
                    fontSize = 13.sp,
                    color = ChartDetailColors.SuccessColor
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Challenging Signs (<25):",
                    fontSize = 13.sp,
                    color = ChartDetailColors.TextMuted
                )
                Text(
                    text = "$weakSignCount signs",
                    fontSize = 13.sp,
                    color = ChartDetailColors.WarningColor
                )
            }
        }
    }
}

@Composable
private fun SummaryItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun SarvashtakavargaCard(ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis) {
    var expanded by remember { mutableStateOf(true) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.TrendingUp,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sarvashtakavarga (SAV)",
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
                    Text(
                        text = "Combined strength of all planets in each sign",
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        ZodiacSign.entries.forEach { sign ->
                            val bindus = ashtakavarga.sarvashtakavarga.getBindusForSign(sign)
                            SAVSignBox(
                                sign = sign,
                                bindus = bindus
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    SAVLegend()
                }
            }
        }
    }
}

@Composable
private fun SAVSignBox(
    sign: ZodiacSign,
    bindus: Int
) {
    val backgroundColor = when {
        bindus >= 30 -> ChartDetailColors.SuccessColor.copy(alpha = 0.2f)
        bindus >= 28 -> ChartDetailColors.AccentTeal.copy(alpha = 0.15f)
        bindus < 25 -> ChartDetailColors.ErrorColor.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        bindus >= 30 -> ChartDetailColors.SuccessColor
        bindus >= 28 -> ChartDetailColors.AccentTeal
        bindus < 25 -> ChartDetailColors.ErrorColor
        else -> ChartDetailColors.TextPrimary
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = sign.abbreviation,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = ChartDetailColors.TextSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = bindus.toString(),
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}

@Composable
private fun SAVLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = ChartDetailColors.SuccessColor,
            label = "30+ (Excellent)"
        )
        LegendItem(
            color = ChartDetailColors.AccentTeal,
            label = "28-29 (Good)"
        )
        LegendItem(
            color = ChartDetailColors.TextMuted,
            label = "25-27 (Average)"
        )
        LegendItem(
            color = ChartDetailColors.ErrorColor,
            label = "<25 (Weak)"
        )
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 9.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun BhinnashtakavargaCard(ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis) {
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
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.GridOn,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentPurple,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Bhinnashtakavarga (BAV)",
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
                    Text(
                        text = "Individual planet strength in each sign (0-8 bindus)",
                        fontSize = 12.sp,
                        color = ChartDetailColors.TextMuted,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        BAVTable(ashtakavarga = ashtakavarga)
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    BAVLegend()
                }
            }
        }
    }
}

@Composable
private fun BAVTable(ashtakavarga: AshtakavargaCalculator.AshtakavargaAnalysis) {
    Column {
        Row {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .border(0.5.dp, ChartDetailColors.DividerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "",
                    fontSize = 10.sp,
                    color = ChartDetailColors.TextMuted
                )
            }
            ZodiacSign.entries.forEach { sign ->
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(0.5.dp, ChartDetailColors.DividerColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = sign.abbreviation,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.AccentTeal
                    )
                }
            }
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(36.dp)
                    .border(0.5.dp, ChartDetailColors.DividerColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Total",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChartDetailColors.AccentGold
                )
            }
        }

        ashtakavarga.bhinnashtakavarga.forEach { (planet, bav) ->
            Row {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .border(0.5.dp, ChartDetailColors.DividerColor)
                        .background(ChartDetailColors.getPlanetColor(planet).copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = planet.symbol,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.getPlanetColor(planet)
                    )
                }
                ZodiacSign.entries.forEach { sign ->
                    val bindus = bav.getBindusForSign(sign)
                    BAVCell(bindus = bindus)
                }
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .height(36.dp)
                        .border(0.5.dp, ChartDetailColors.DividerColor)
                        .background(ChartDetailColors.AccentGold.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = bav.totalBindus.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChartDetailColors.AccentGold
                    )
                }
            }
        }
    }
}

@Composable
private fun BAVCell(bindus: Int) {
    val backgroundColor = when {
        bindus >= 5 -> ChartDetailColors.SuccessColor.copy(alpha = 0.15f)
        bindus >= 4 -> ChartDetailColors.AccentTeal.copy(alpha = 0.1f)
        bindus <= 2 -> ChartDetailColors.ErrorColor.copy(alpha = 0.15f)
        else -> Color.Transparent
    }

    val textColor = when {
        bindus >= 5 -> ChartDetailColors.SuccessColor
        bindus >= 4 -> ChartDetailColors.AccentTeal
        bindus <= 2 -> ChartDetailColors.ErrorColor
        else -> ChartDetailColors.TextPrimary
    }

    Box(
        modifier = Modifier
            .size(36.dp)
            .border(0.5.dp, ChartDetailColors.DividerColor)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = bindus.toString(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = textColor
        )
    }
}

@Composable
private fun BAVLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem(
            color = ChartDetailColors.SuccessColor,
            label = "5+ (Strong)"
        )
        LegendItem(
            color = ChartDetailColors.AccentTeal,
            label = "4 (Good)"
        )
        LegendItem(
            color = ChartDetailColors.TextMuted,
            label = "3 (Average)"
        )
        LegendItem(
            color = ChartDetailColors.ErrorColor,
            label = "0-2 (Weak)"
        )
    }
}

@Composable
private fun InterpretationGuideCard() {
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
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
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
                        tint = ChartDetailColors.AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Interpretation Guide",
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
                    GuideSection(
                        title = "Sarvashtakavarga (SAV)",
                        points = listOf(
                            "30+ bindus: Excellent for transits - major positive events",
                            "28-29 bindus: Good for transits - favorable outcomes",
                            "25-27 bindus: Average - mixed results expected",
                            "Below 25: Challenging - caution during transits"
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GuideSection(
                        title = "Bhinnashtakavarga (BAV)",
                        points = listOf(
                            "5+ bindus: Planet transit highly beneficial",
                            "4 bindus: Good results from transit",
                            "3 bindus: Average, neutral results",
                            "0-2 bindus: Difficult transit period"
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GuideSection(
                        title = "Transit Application",
                        points = listOf(
                            "Check SAV of the sign a planet transits",
                            "Check BAV score of that planet in transited sign",
                            "High combined scores = favorable transit",
                            "Use for timing important decisions"
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun GuideSection(
    title: String,
    points: List<String>
) {
    Column {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = ChartDetailColors.AccentTeal,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        points.forEach { point ->
            Row(
                modifier = Modifier.padding(vertical = 2.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = "•",
                    fontSize = 12.sp,
                    color = ChartDetailColors.TextMuted,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = point,
                    fontSize = 12.sp,
                    color = ChartDetailColors.TextSecondary,
                    lineHeight = 18.sp
                )
            }
        }
    }
}
