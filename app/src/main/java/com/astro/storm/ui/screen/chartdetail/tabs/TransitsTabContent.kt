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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.Planet
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.TransitAnalyzer
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import com.astro.storm.ui.screen.chartdetail.ChartDetailUtils
import java.time.format.DateTimeFormatter

/**
 * Transits tab content displaying current planetary transits and their effects.
 */
@Composable
fun TransitsTabContent(chart: VedicChart) {
    val transitAnalysis = remember(chart) {
        TransitAnalyzer.analyzeCurrentTransits(chart)
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

        items(transitAnalysis.planetTransits) { transit ->
            TransitDetailCard(transit = transit)
        }

        if (transitAnalysis.upcomingSignChanges.isNotEmpty()) {
            item {
                UpcomingTransitsCard(transitAnalysis.upcomingSignChanges)
            }
        }
    }
}

@Composable
private fun TransitOverviewCard(analysis: TransitAnalyzer.TransitAnalysis) {
    val favorableCount = analysis.planetTransits.count { it.overallEffect.isFavorable }
    val unfavorableCount = analysis.planetTransits.count { !it.overallEffect.isFavorable }

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
                    count = unfavorableCount,
                    label = "Challenging",
                    color = ChartDetailColors.WarningColor
                )
                OverviewBadge(
                    count = analysis.upcomingSignChanges.size,
                    label = "Upcoming",
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
    val overallScore = analysis.overallTransitScore
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
            text = when {
                overallScore >= 70 -> "Highly favorable period for new initiatives"
                overallScore >= 50 -> "Generally positive transit period"
                overallScore >= 30 -> "Mixed results expected, exercise caution"
                else -> "Challenging period, focus on consolidation"
            },
            fontSize = 12.sp,
            color = ChartDetailColors.TextMuted
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

            analysis.planetTransits.forEach { transit ->
                CurrentTransitRow(transit = transit)
            }
        }
    }
}

@Composable
private fun CurrentTransitRow(transit: TransitAnalyzer.PlanetTransit) {
    val planetColor = ChartDetailColors.getPlanetColor(transit.planet)
    val effectColor = if (transit.overallEffect.isFavorable) {
        ChartDetailColors.SuccessColor
    } else {
        ChartDetailColors.WarningColor
    }

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
                    text = transit.planet.symbol,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = transit.planet.displayName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChartDetailColors.TextPrimary
                )
                Text(
                    text = "${transit.transitSign.displayName} (H${transit.natalHouse})",
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (transit.overallEffect.isFavorable) Icons.Outlined.CheckCircle else Icons.Outlined.Warning,
                contentDescription = null,
                tint = effectColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (transit.overallEffect.isFavorable) "Favorable" else "Challenging",
                fontSize = 11.sp,
                color = effectColor
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TransitDetailCard(transit: TransitAnalyzer.PlanetTransit) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    val planetColor = ChartDetailColors.getPlanetColor(transit.planet)
    val effectColor = if (transit.overallEffect.isFavorable) {
        ChartDetailColors.SuccessColor
    } else {
        ChartDetailColors.WarningColor
    }

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
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(planetColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = transit.planet.symbol,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "${transit.planet.displayName} Transit",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartDetailColors.TextPrimary
                        )
                        Text(
                            text = "Through ${transit.transitSign.displayName} • House ${transit.natalHouse}",
                            fontSize = 12.sp,
                            color = ChartDetailColors.TextSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = effectColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = transit.overallEffect.strength.displayName,
                            fontSize = 11.sp,
                            color = effectColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
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
                    HorizontalDivider(color = ChartDetailColors.DividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Current Position",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextSecondary
                    )
                    Text(
                        text = ChartDetailUtils.formatDegree(transit.transitLongitude),
                        fontSize = 13.sp,
                        color = ChartDetailColors.AccentTeal,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    if (transit.isRetrograde) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = ChartDetailColors.WarningColor.copy(alpha = 0.15f),
                            modifier = Modifier.padding(bottom = 8.dp)
                        ) {
                            Text(
                                text = "Currently Retrograde",
                                fontSize = 11.sp,
                                color = ChartDetailColors.WarningColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = "Transit Effects",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = transit.overallEffect.description,
                        fontSize = 13.sp,
                        color = ChartDetailColors.TextPrimary,
                        lineHeight = 20.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    if (transit.aspectsToNatal.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Aspects to Natal Planets",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ChartDetailColors.TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            transit.aspectsToNatal.forEach { aspect ->
                                AspectChip(
                                    aspect = aspect.aspectType.displayName,
                                    planet = aspect.natalPlanet.symbol,
                                    isFavorable = aspect.isFavorable
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        color = ChartDetailColors.CardBackgroundElevated
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "SAV & BAV Analysis",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChartDetailColors.AccentGold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "SAV Score:",
                                    fontSize = 12.sp,
                                    color = ChartDetailColors.TextMuted
                                )
                                Text(
                                    text = "${transit.savScore} bindus",
                                    fontSize = 12.sp,
                                    color = ChartDetailColors.getSavFavorableColor(transit.savScore >= 28)
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "BAV Score:",
                                    fontSize = 12.sp,
                                    color = ChartDetailColors.TextMuted
                                )
                                Text(
                                    text = "${transit.bavScore} bindus",
                                    fontSize = 12.sp,
                                    color = ChartDetailColors.getBinduColor(transit.bavScore)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AspectChip(
    aspect: String,
    planet: String,
    isFavorable: Boolean
) {
    val color = if (isFavorable) ChartDetailColors.AccentTeal else ChartDetailColors.WarningColor

    Surface(
        shape = RoundedCornerShape(6.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = aspect,
                fontSize = 10.sp,
                color = color
            )
            Text(
                text = " $planet",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun UpcomingTransitsCard(upcomingChanges: List<TransitAnalyzer.UpcomingSignChange>) {
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
                    text = "Upcoming Sign Changes",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            upcomingChanges.take(5).forEach { change ->
                UpcomingChangeRow(change = change)
            }
        }
    }
}

@Composable
private fun UpcomingChangeRow(change: TransitAnalyzer.UpcomingSignChange) {
    val planetColor = ChartDetailColors.getPlanetColor(change.planet)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

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
                    .size(24.dp)
                    .background(planetColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = change.planet.symbol,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = change.planet.displayName,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = ChartDetailColors.TextPrimary
                )
                Text(
                    text = "${change.fromSign.abbreviation} → ${change.toSign.abbreviation}",
                    fontSize = 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }
        }

        Text(
            text = change.date.format(dateFormatter),
            fontSize = 11.sp,
            color = ChartDetailColors.AccentTeal
        )
    }
}
