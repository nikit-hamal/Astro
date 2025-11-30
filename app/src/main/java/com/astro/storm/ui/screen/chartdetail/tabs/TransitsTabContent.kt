package com.astro.storm.ui.screen.chartdetail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.TransitAnalyzer
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import com.astro.storm.ui.screen.chartdetail.ChartDetailUtils

/**
 * Transits tab content displaying current planetary transits and their effects.
 */
@Composable
fun TransitsTabContent(chart: VedicChart) {
    val context = LocalContext.current
    val transitAnalysis = remember(chart) {
        val analyzer = TransitAnalyzer(context)
        try {
            analyzer.analyzeTransits(chart)
        } finally {
            analyzer.close()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            TransitOverviewCard(transitAnalysis)
        }

        item {
            CurrentTransitsCard(transitAnalysis)
        }

        item {
            GocharaResultsCard(transitAnalysis)
        }

        if (transitAnalysis.transitAspects.isNotEmpty()) {
            item {
                TransitAspectsCard(transitAnalysis)
            }
        }

        if (transitAnalysis.significantPeriods.isNotEmpty()) {
            item {
                SignificantPeriodsCard(transitAnalysis)
            }
        }
    }
}

@Composable
private fun TransitOverviewCard(analysis: TransitAnalyzer.TransitAnalysis) {
    val favorableCount = analysis.gocharaResults.count {
        it.effect == TransitAnalyzer.TransitEffect.EXCELLENT ||
        it.effect == TransitAnalyzer.TransitEffect.GOOD
    }
    val challengingCount = analysis.gocharaResults.count {
        it.effect == TransitAnalyzer.TransitEffect.CHALLENGING ||
        it.effect == TransitAnalyzer.TransitEffect.DIFFICULT
    }

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
                    Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Transit Overview",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewBadge(
                    count = favorableCount,
                    label = "Favorable",
                    color = ChartDetailColors.SuccessColor
                )
                OverviewBadge(
                    count = challengingCount,
                    label = "Challenging",
                    color = ChartDetailColors.WarningColor
                )
                OverviewBadge(
                    count = analysis.transitAspects.size,
                    label = "Aspects",
                    color = ChartDetailColors.AccentBlue
                )
            }

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 12.dp)
            )

            OverallTransitAssessment(analysis)
        }
    }
}

@Composable
private fun OverviewBadge(count: Int, label: String, color: Color) {
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
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun OverallTransitAssessment(analysis: TransitAnalyzer.TransitAnalysis) {
    val overallScore = analysis.overallAssessment.score
    val progress = (overallScore / 100.0).coerceIn(0.0, 1.0).toFloat()
    val scoreColor = when {
        overallScore >= 70 -> ChartDetailColors.SuccessColor
        overallScore >= 50 -> ChartDetailColors.AccentTeal
        overallScore >= 30 -> ChartDetailColors.WarningColor
        else -> ChartDetailColors.ErrorColor
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Overall Transit Score",
                fontSize = 13.sp,
                color = ChartDetailColors.TextSecondary
            )
            Text(
                text = "${String.format("%.1f", overallScore)}%",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = scoreColor
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = scoreColor,
            trackColor = ChartDetailColors.DividerColor
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = analysis.overallAssessment.summary,
            fontSize = 12.sp,
            color = ChartDetailColors.TextMuted,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun CurrentTransitsCard(analysis: TransitAnalyzer.TransitAnalysis) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Planetary Positions",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            analysis.transitPositions.forEach { position ->
                val planetColor = ChartDetailColors.getPlanetColor(position.planet)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .background(planetColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = position.planet.symbol,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = position.planet.displayName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = ChartDetailColors.TextPrimary
                            )
                            Text(
                                text = "${position.sign.displayName} ${ChartDetailUtils.formatDegreeInSign(position.longitude)}",
                                fontSize = 11.sp,
                                color = ChartDetailColors.TextMuted
                            )
                        }
                    }

                    if (position.isRetrograde) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = ChartDetailColors.WarningColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "R",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = ChartDetailColors.WarningColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GocharaResultsCard(analysis: TransitAnalyzer.TransitAnalysis) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                        text = "Gochara Analysis (From Moon)",
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
                    analysis.gocharaResults.forEach { result ->
                        GocharaResultRow(result = result)
                    }
                }
            }
        }
    }
}

@Composable
private fun GocharaResultRow(result: TransitAnalyzer.GocharaResult) {
    val planetColor = ChartDetailColors.getPlanetColor(result.planet)
    val effectColor = when (result.effect) {
        TransitAnalyzer.TransitEffect.EXCELLENT -> ChartDetailColors.SuccessColor
        TransitAnalyzer.TransitEffect.GOOD -> ChartDetailColors.AccentTeal
        TransitAnalyzer.TransitEffect.NEUTRAL -> ChartDetailColors.TextSecondary
        TransitAnalyzer.TransitEffect.CHALLENGING -> ChartDetailColors.WarningColor
        TransitAnalyzer.TransitEffect.DIFFICULT -> ChartDetailColors.ErrorColor
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(planetColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = result.planet.symbol,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = result.planet.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChartDetailColors.TextPrimary
                    )
                    Text(
                        text = " • House ${result.houseFromMoon}",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                }
                if (result.isVedhaAffected) {
                    Text(
                        text = "Vedha from ${result.vedhaSource?.displayName ?: "unknown"}",
                        fontSize = 10.sp,
                        color = ChartDetailColors.WarningColor
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (result.effect == TransitAnalyzer.TransitEffect.EXCELLENT ||
                    result.effect == TransitAnalyzer.TransitEffect.GOOD)
                    Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                contentDescription = null,
                tint = effectColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = result.effect.displayName,
                fontSize = 10.sp,
                color = effectColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransitAspectsCard(analysis: TransitAnalyzer.TransitAnalysis) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = ChartDetailColors.CardBackground
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Outlined.Info,
                        contentDescription = null,
                        tint = ChartDetailColors.AccentTeal,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transit Aspects to Natal",
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
                    analysis.transitAspects.take(10).forEach { aspect ->
                        TransitAspectRow(aspect = aspect)
                    }
                }
            }
        }
    }
}

@Composable
private fun TransitAspectRow(aspect: TransitAnalyzer.TransitAspect) {
    val transitPlanetColor = ChartDetailColors.getPlanetColor(aspect.transitingPlanet)
    val natalPlanetColor = ChartDetailColors.getPlanetColor(aspect.natalPlanet)
    val applyingText = if (aspect.isApplying) "Applying" else "Separating"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(transitPlanetColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = aspect.transitingPlanet.symbol,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Text(
                text = " ${aspect.aspectType} ",
                fontSize = 11.sp,
                color = ChartDetailColors.TextSecondary
            )
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .background(natalPlanetColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = aspect.natalPlanet.symbol,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "Orb: ${String.format("%.1f", aspect.orb)}°",
                fontSize = 10.sp,
                color = ChartDetailColors.TextMuted
            )
            Text(
                text = applyingText,
                fontSize = 10.sp,
                color = if (aspect.isApplying) ChartDetailColors.AccentTeal else ChartDetailColors.TextMuted
            )
        }
    }
}

@Composable
private fun SignificantPeriodsCard(analysis: TransitAnalyzer.TransitAnalysis) {
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
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Significant Periods",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            analysis.significantPeriods.forEach { period ->
                SignificantPeriodRow(period = period)
            }
        }
    }
}

@Composable
private fun SignificantPeriodRow(period: TransitAnalyzer.SignificantPeriod) {
    val intensityColor = when (period.intensity) {
        5 -> ChartDetailColors.ErrorColor
        4 -> ChartDetailColors.WarningColor
        3 -> ChartDetailColors.AccentGold
        else -> ChartDetailColors.AccentTeal
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = period.description,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = ChartDetailColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = intensityColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Intensity ${period.intensity}/5",
                    fontSize = 10.sp,
                    color = intensityColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Row(modifier = Modifier.padding(top = 2.dp)) {
            period.planets.forEach { planet ->
                val planetColor = ChartDetailColors.getPlanetColor(planet)
                Box(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .size(16.dp)
                        .background(planetColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = planet.symbol,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
