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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material.icons.outlined.WbSunny
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.PanchangaCalculator
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors

/**
 * Panchanga tab content displaying the five elements of Vedic time:
 * Tithi, Nakshatra, Yoga, Karana, and Vara.
 */
@Composable
fun PanchangaTabContent(chart: VedicChart) {
    val panchanga = remember(chart) {
        PanchangaCalculator.calculatePanchanga(chart)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            PanchangaSummaryCard(panchanga)
        }

        item {
            TithiCard(panchanga)
        }

        item {
            NakshatraCard(panchanga)
        }

        item {
            YogaCard(panchanga)
        }

        item {
            KaranaCard(panchanga)
        }

        item {
            VaraCard(panchanga)
        }

        item {
            PanchangaInfoCard()
        }
    }
}

@Composable
private fun PanchangaSummaryCard(panchanga: PanchangaCalculator.PanchangaData) {
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
                    Icons.Outlined.WbSunny,
                    contentDescription = null,
                    tint = ChartDetailColors.AccentGold,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Panchanga at Birth",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ChartDetailColors.TextPrimary
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PanchangaElement(
                    label = "Tithi",
                    value = panchanga.tithi.name,
                    color = ChartDetailColors.AccentTeal
                )
                PanchangaElement(
                    label = "Nakshatra",
                    value = panchanga.nakshatra.displayName,
                    color = ChartDetailColors.AccentPurple
                )
                PanchangaElement(
                    label = "Yoga",
                    value = panchanga.yoga.name,
                    color = ChartDetailColors.AccentGold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PanchangaElement(
                    label = "Karana",
                    value = panchanga.karana.name,
                    color = ChartDetailColors.AccentBlue
                )
                PanchangaElement(
                    label = "Vara",
                    value = panchanga.vara.name,
                    color = ChartDetailColors.AccentOrange
                )
            }
        }
    }
}

@Composable
private fun PanchangaElement(
    label: String,
    value: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = ChartDetailColors.TextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = color.copy(alpha = 0.15f)
        ) {
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = color,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun TithiCard(panchanga: PanchangaCalculator.PanchangaData) {
    PanchangaDetailCard(
        title = "Tithi (Lunar Day)",
        icon = Icons.Outlined.Brightness4,
        iconColor = ChartDetailColors.AccentTeal
    ) {
        Column {
            DetailRow("Name", panchanga.tithi.name, ChartDetailColors.AccentTeal)
            DetailRow("Number", "${panchanga.tithi.number} of 30", ChartDetailColors.TextPrimary)
            DetailRow("Paksha", panchanga.tithi.paksha.displayName, ChartDetailColors.TextSecondary)
            DetailRow("Deity", panchanga.tithi.deity, ChartDetailColors.AccentPurple)
            DetailRow("Nature", panchanga.tithi.nature, ChartDetailColors.TextSecondary)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Significance",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = panchanga.tithi.significance,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun NakshatraCard(panchanga: PanchangaCalculator.PanchangaData) {
    PanchangaDetailCard(
        title = "Nakshatra (Lunar Mansion)",
        icon = Icons.Outlined.Star,
        iconColor = ChartDetailColors.AccentPurple
    ) {
        Column {
            DetailRow("Name", panchanga.nakshatra.displayName, ChartDetailColors.AccentPurple)
            DetailRow("Number", "${panchanga.nakshatra.number} of 27", ChartDetailColors.TextPrimary)
            DetailRow("Ruler", panchanga.nakshatra.ruler.displayName, ChartDetailColors.AccentGold)
            DetailRow("Deity", panchanga.nakshatra.deity, ChartDetailColors.TextSecondary)
            DetailRow("Symbol", panchanga.nakshatraSymbol, ChartDetailColors.TextSecondary)
            DetailRow("Pada", panchanga.nakshatraPada.toString(), ChartDetailColors.AccentTeal)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Characteristics",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = panchanga.nakshatraDescription,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun YogaCard(panchanga: PanchangaCalculator.PanchangaData) {
    PanchangaDetailCard(
        title = "Yoga (Luni-Solar Combination)",
        icon = Icons.Outlined.WbSunny,
        iconColor = ChartDetailColors.AccentGold
    ) {
        Column {
            DetailRow("Name", panchanga.yoga.name, ChartDetailColors.AccentGold)
            DetailRow("Number", "${panchanga.yoga.number} of 27", ChartDetailColors.TextPrimary)
            DetailRow("Nature", panchanga.yoga.nature, when(panchanga.yoga.nature) {
                "Auspicious" -> ChartDetailColors.SuccessColor
                "Inauspicious" -> ChartDetailColors.WarningColor
                else -> ChartDetailColors.TextSecondary
            })
            DetailRow("Ruling Planet", panchanga.yoga.ruler.displayName, ChartDetailColors.AccentTeal)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Effects",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = panchanga.yoga.effects,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun KaranaCard(panchanga: PanchangaCalculator.PanchangaData) {
    PanchangaDetailCard(
        title = "Karana (Half Tithi)",
        icon = Icons.Outlined.CalendarMonth,
        iconColor = ChartDetailColors.AccentBlue
    ) {
        Column {
            DetailRow("Name", panchanga.karana.name, ChartDetailColors.AccentBlue)
            DetailRow("Type", panchanga.karana.type, ChartDetailColors.TextPrimary)
            DetailRow("Nature", panchanga.karana.nature, when(panchanga.karana.nature) {
                "Auspicious" -> ChartDetailColors.SuccessColor
                "Inauspicious" -> ChartDetailColors.WarningColor
                else -> ChartDetailColors.TextSecondary
            })
            DetailRow("Lord", panchanga.karana.lord, ChartDetailColors.AccentPurple)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Suitable Activities",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = panchanga.karana.suitableActivities,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun VaraCard(panchanga: PanchangaCalculator.PanchangaData) {
    PanchangaDetailCard(
        title = "Vara (Weekday)",
        icon = Icons.Outlined.CalendarMonth,
        iconColor = ChartDetailColors.AccentOrange
    ) {
        Column {
            DetailRow("Day", panchanga.vara.name, ChartDetailColors.AccentOrange)
            DetailRow("Ruling Planet", panchanga.vara.ruler.displayName, ChartDetailColors.getPlanetColor(panchanga.vara.ruler))

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Significance",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = panchanga.vara.significance,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Favorable Activities",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = panchanga.vara.favorableActivities,
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun PanchangaDetailCard(
    title: String,
    icon: ImageVector,
    iconColor: Color,
    content: @Composable () -> Unit
) {
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
                        icon,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = title,
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
                    content()
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    valueColor: Color
) {
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
            fontWeight = FontWeight.Medium,
            color = valueColor
        )
    }
}

@Composable
private fun PanchangaInfoCard() {
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
                        text = "About Panchanga",
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
                        text = "Panchanga (Sanskrit: पञ्चाङ्ग, meaning \"five limbs\") is the Hindu calendar system that tracks five elements of time:",
                        fontSize = 13.sp,
                        color = ChartDetailColors.TextSecondary,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    val elements = listOf(
                        "Tithi" to "Lunar day based on Moon-Sun angle (30 in a lunar month)",
                        "Nakshatra" to "Lunar mansion based on Moon's position (27 nakshatras)",
                        "Yoga" to "Luni-solar combination of Sun and Moon longitudes (27 yogas)",
                        "Karana" to "Half of a tithi (11 karanas repeat to form 60)",
                        "Vara" to "Weekday ruled by a specific planet"
                    )

                    elements.forEach { (name, description) ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "•",
                                fontSize = 13.sp,
                                color = ChartDetailColors.AccentGold,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                            Column {
                                Text(
                                    text = name,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ChartDetailColors.AccentTeal
                                )
                                Text(
                                    text = description,
                                    fontSize = 12.sp,
                                    color = ChartDetailColors.TextMuted,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "These elements are used for muhurta (electional astrology) to determine auspicious timings for important activities.",
                        fontSize = 13.sp,
                        color = ChartDetailColors.TextSecondary,
                        lineHeight = 20.sp
                    )
                }
            }
        }
    }
}
