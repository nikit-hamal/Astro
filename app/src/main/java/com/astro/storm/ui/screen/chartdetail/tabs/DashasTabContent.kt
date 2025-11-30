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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timeline
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
import com.astro.storm.ephemeris.DashaCalculator
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Dashas tab content displaying Vimshottari dasha timeline and current periods.
 */
@Composable
fun DashasTabContent(chart: VedicChart) {
    val dashaAnalysis = remember(chart) {
        DashaCalculator.calculateVimshottariDasha(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CurrentPeriodCard(dashaAnalysis)
        }

        item {
            DashaTimelineCard(dashaAnalysis)
        }

        items(dashaAnalysis.mahadashas) { mahadasha ->
            MahadashaCard(
                mahadasha = mahadasha,
                isCurrentMahadasha = mahadasha == dashaAnalysis.currentMahadasha
            )
        }

        item {
            DashaInfoCard()
        }
    }
}

@Composable
private fun CurrentPeriodCard(analysis: DashaCalculator.DashaAnalysis) {
    val currentMahadasha = analysis.currentMahadasha
    val currentAntardasha = analysis.currentAntardasha
    val currentPratyantardasha = analysis.currentPratyantardasha

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
                    text = "Current Dasha Period",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            if (currentMahadasha != null) {
                DashaPeriodRow(
                    label = "Mahadasha",
                    planet = currentMahadasha.planet,
                    startDate = currentMahadasha.startDate,
                    endDate = currentMahadasha.endDate,
                    isMain = true
                )

                if (currentAntardasha != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DashaPeriodRow(
                        label = "Antardasha",
                        planet = currentAntardasha.planet,
                        startDate = currentAntardasha.startDate,
                        endDate = currentAntardasha.endDate,
                        isMain = false
                    )
                }

                if (currentPratyantardasha != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DashaPeriodRow(
                        label = "Pratyantardasha",
                        planet = currentPratyantardasha.planet,
                        startDate = currentPratyantardasha.startDate,
                        endDate = currentPratyantardasha.endDate,
                        isMain = false,
                        isSmall = true
                    )
                }

                HorizontalDivider(
                    color = ChartDetailColors.DividerColor,
                    modifier = Modifier.padding(vertical = 12.dp)
                )

                CurrentPeriodSummary(
                    mahadasha = currentMahadasha,
                    antardasha = currentAntardasha
                )
            }
        }
    }
}

@Composable
private fun DashaPeriodRow(
    label: String,
    planet: Planet,
    startDate: LocalDate,
    endDate: LocalDate,
    isMain: Boolean,
    isSmall: Boolean = false
) {
    val planetColor = ChartDetailColors.getPlanetColor(planet)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val today = LocalDate.now()
    val totalDays = ChronoUnit.DAYS.between(startDate, endDate).toFloat()
    val elapsedDays = ChronoUnit.DAYS.between(startDate, today).coerceIn(0, totalDays.toLong()).toFloat()
    val progress = if (totalDays > 0) elapsedDays / totalDays else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(if (isMain) 42.dp else if (isSmall) 28.dp else 36.dp)
                    .background(planetColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = planet.symbol,
                    fontSize = if (isMain) 16.sp else if (isSmall) 10.sp else 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = label,
                        fontSize = if (isSmall) 11.sp else 12.sp,
                        color = ChartDetailColors.TextMuted
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = planet.displayName,
                        fontSize = if (isMain) 16.sp else if (isSmall) 12.sp else 14.sp,
                        fontWeight = if (isMain) FontWeight.Bold else FontWeight.SemiBold,
                        color = planetColor
                    )
                }
                Text(
                    text = "${startDate.format(dateFormatter)} - ${endDate.format(dateFormatter)}",
                    fontSize = if (isSmall) 10.sp else 11.sp,
                    color = ChartDetailColors.TextMuted
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.End,
            modifier = Modifier.width(80.dp)
        ) {
            Text(
                text = "${(progress * 100).toInt()}%",
                fontSize = if (isSmall) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = planetColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSmall) 3.dp else 4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = planetColor,
                trackColor = ChartDetailColors.DividerColor
            )
        }
    }
}

@Composable
private fun CurrentPeriodSummary(
    mahadasha: DashaCalculator.MahaDasha,
    antardasha: DashaCalculator.AntarDasha?
) {
    val interpretation = getDashaPeriodInterpretation(
        mahadasha.planet,
        antardasha?.planet
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = ChartDetailColors.CardBackgroundElevated
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Period Interpretation",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.AccentGold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = interpretation,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun DashaTimelineCard(analysis: DashaCalculator.DashaAnalysis) {
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
                    Icons.Outlined.Timeline,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentTeal,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Dasha Timeline Overview",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy")

            analysis.mahadashas.forEach { dasha ->
                val isPast = dasha.endDate.isBefore(today)
                val isCurrent = !dasha.startDate.isAfter(today) && !dasha.endDate.isBefore(today)
                val planetColor = ChartDetailColors.getPlanetColor(dasha.planet)

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .background(
                                if (isCurrent) planetColor else planetColor.copy(alpha = if (isPast) 0.3f else 0.6f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dasha.planet.symbol,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = dasha.planet.displayName,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) planetColor else ChartDetailColors.TextSecondary,
                        modifier = Modifier.width(60.dp)
                    )
                    Text(
                        text = "${dasha.startDate.format(dateFormatter)} - ${dasha.endDate.format(dateFormatter)}",
                        fontSize = 11.sp,
                        color = if (isCurrent) ChartDetailColors.TextPrimary else ChartDetailColors.TextMuted
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${dasha.years.toInt()} yrs",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                }
            }
        }
    }
}

@Composable
private fun MahadashaCard(
    mahadasha: DashaCalculator.MahaDasha,
    isCurrentMahadasha: Boolean
) {
    var expanded by remember { mutableStateOf(isCurrentMahadasha) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        label = "rotation"
    )

    val planetColor = ChartDetailColors.getPlanetColor(mahadasha.planet)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(16.dp),
        color = if (isCurrentMahadasha) {
            planetColor.copy(alpha = 0.1f)
        } else {
            ChartDetailColors.CardBackground
        }
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
                            text = mahadasha.planet.symbol,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${mahadasha.planet.displayName} Mahadasha",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ChartDetailColors.TextPrimary
                            )
                            if (isCurrentMahadasha) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = planetColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "Current",
                                        fontSize = 10.sp,
                                        color = planetColor,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = "${mahadasha.years.toInt()} years • ${mahadasha.startDate.format(dateFormatter)} - ${mahadasha.endDate.format(dateFormatter)}",
                            fontSize = 11.sp,
                            color = ChartDetailColors.TextMuted
                        )
                    }
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
                    HorizontalDivider(color = ChartDetailColors.DividerColor)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Antardashas in ${mahadasha.planet.displayName} Period",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    mahadasha.antardashas.forEach { antardasha ->
                        AntardashaRow(
                            antardasha = antardasha,
                            mahadashaPlanet = mahadasha.planet
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AntardashaRow(
    antardasha: DashaCalculator.AntarDasha,
    mahadashaPlanet: Planet
) {
    val planetColor = ChartDetailColors.getPlanetColor(antardasha.planet)
    val dateFormatter = DateTimeFormatter.ofPattern("MMM yyyy")
    val today = LocalDate.now()
    val isCurrent = !antardasha.startDate.isAfter(today) && !antardasha.endDate.isBefore(today)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                if (isCurrent) planetColor.copy(alpha = 0.1f) else Color.Transparent,
                RoundedCornerShape(6.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
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
                    text = antardasha.planet.symbol,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "${mahadashaPlanet.symbol}-${antardasha.planet.displayName}",
                fontSize = 12.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                color = if (isCurrent) planetColor else ChartDetailColors.TextPrimary
            )
        }

        Text(
            text = "${antardasha.startDate.format(dateFormatter)} - ${antardasha.endDate.format(dateFormatter)}",
            fontSize = 11.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

@Composable
private fun DashaInfoCard() {
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
                        text = "About Vimshottari Dasha",
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
                        text = "The Vimshottari Dasha system is the most widely used planetary period system in Vedic astrology. It is based on the Moon's nakshatra at birth and spans 120 years.",
                        fontSize = 13.sp,
                        color = ChartDetailColors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Dasha Period Durations:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.TextSecondary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val periods = listOf(
                        Planet.SUN to "6 years",
                        Planet.MOON to "10 years",
                        Planet.MARS to "7 years",
                        Planet.RAHU to "18 years",
                        Planet.JUPITER to "16 years",
                        Planet.SATURN to "19 years",
                        Planet.MERCURY to "17 years",
                        Planet.KETU to "7 years",
                        Planet.VENUS to "20 years"
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            periods.take(5).forEach { (planet, duration) ->
                                DashaDurationRow(planet = planet, duration = duration)
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            periods.drop(5).forEach { (planet, duration) ->
                                DashaDurationRow(planet = planet, duration = duration)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashaDurationRow(planet: Planet, duration: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .background(ChartDetailColors.getPlanetColor(planet), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = planet.symbol,
                fontSize = 8.sp,
                color = Color.White
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = "${planet.displayName}: $duration",
            fontSize = 11.sp,
            color = ChartDetailColors.TextMuted
        )
    }
}

private fun getDashaPeriodInterpretation(
    mahadashaPlanet: Planet,
    antardashaplanet: Planet?
): String {
    val mahaInterpretation = when (mahadashaPlanet) {
        Planet.SUN -> "Period of authority, recognition, and self-development. Focus on career, leadership, and father-related matters."
        Planet.MOON -> "Emotional and intuitive period. Focus on mother, public dealings, mental peace, and domestic matters."
        Planet.MARS -> "Period of action, courage, and energy. Focus on property, siblings, technical pursuits, and competition."
        Planet.MERCURY -> "Period of learning, communication, and business. Focus on education, writing, trade, and intellectual pursuits."
        Planet.JUPITER -> "Period of wisdom, expansion, and fortune. Focus on spirituality, teaching, children, and higher learning."
        Planet.VENUS -> "Period of luxury, relationships, and creativity. Focus on marriage, arts, vehicles, and material comforts."
        Planet.SATURN -> "Period of discipline, karma, and hard work. Focus on service, perseverance, delays leading to success."
        Planet.RAHU -> "Period of ambition and worldly desires. Focus on foreign matters, technology, unconventional paths."
        Planet.KETU -> "Period of spirituality and detachment. Focus on liberation, research, healing, and past life karma."
        else -> "Period of transformation and growth."
    }

    return if (antardashaplanet != null && antardashaplanet != mahadashaPlanet) {
        val antarInterpretation = when (antardashaplanet) {
            Planet.SUN -> "Sub-period brings focus on authority and recognition."
            Planet.MOON -> "Sub-period emphasizes emotions and public matters."
            Planet.MARS -> "Sub-period brings action and energy."
            Planet.MERCURY -> "Sub-period emphasizes communication and learning."
            Planet.JUPITER -> "Sub-period brings wisdom and expansion."
            Planet.VENUS -> "Sub-period emphasizes relationships and creativity."
            Planet.SATURN -> "Sub-period brings discipline and hard work."
            Planet.RAHU -> "Sub-period emphasizes worldly ambitions."
            Planet.KETU -> "Sub-period brings spiritual insights."
            else -> "Sub-period brings mixed influences."
        }
        "$mahaInterpretation\n\n$antarInterpretation"
    } else {
        mahaInterpretation
    }
}
