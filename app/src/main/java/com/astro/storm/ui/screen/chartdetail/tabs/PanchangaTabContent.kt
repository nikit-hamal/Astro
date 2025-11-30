package com.astro.storm.ui.screen.chartdetail.tabs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.PanchangaCalculator
import com.astro.storm.ephemeris.PanchangaData
import com.astro.storm.ui.screen.chartdetail.ChartDetailColors

/**
 * Panchanga tab content displaying the five elements of Vedic time:
 * Tithi, Nakshatra, Yoga, Karana, and Vara.
 */
@Composable
fun PanchangaTabContent(chart: VedicChart) {
    val context = LocalContext.current
    val panchanga = remember(chart) {
        val calculator = PanchangaCalculator(context)
        try {
            calculator.calculatePanchanga(
                dateTime = chart.birthData.dateTime,
                latitude = chart.birthData.latitude,
                longitude = chart.birthData.longitude,
                timezone = chart.birthData.timezone
            )
        } finally {
            calculator.close()
        }
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
private fun PanchangaSummaryCard(panchanga: PanchangaData) {
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
                    value = panchanga.tithi.tithi.displayName,
                    color = ChartDetailColors.AccentTeal
                )
                PanchangaElement(
                    label = "Nakshatra",
                    value = panchanga.nakshatra.nakshatra.displayName,
                    color = ChartDetailColors.AccentPurple
                )
                PanchangaElement(
                    label = "Yoga",
                    value = panchanga.yoga.yoga.displayName,
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
                    value = panchanga.karana.karana.displayName,
                    color = ChartDetailColors.AccentBlue
                )
                PanchangaElement(
                    label = "Vara",
                    value = panchanga.vara.displayName,
                    color = ChartDetailColors.AccentOrange
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sunrise/Sunset row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sunrise",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                    Text(
                        text = panchanga.sunrise,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.AccentGold
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Sunset",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                    Text(
                        text = panchanga.sunset,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.AccentOrange
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Moon Phase",
                        fontSize = 11.sp,
                        color = ChartDetailColors.TextMuted
                    )
                    Text(
                        text = "${String.format("%.1f", panchanga.moonPhase)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = ChartDetailColors.AccentPurple
                    )
                }
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
private fun TithiCard(panchanga: PanchangaData) {
    PanchangaDetailCard(
        title = "Tithi (Lunar Day)",
        icon = Icons.Outlined.Brightness4,
        iconColor = ChartDetailColors.AccentTeal
    ) {
        Column {
            DetailRow("Name", panchanga.tithi.tithi.displayName, ChartDetailColors.AccentTeal)
            DetailRow("Sanskrit", panchanga.tithi.tithi.sanskrit, ChartDetailColors.TextSecondary)
            DetailRow("Number", "${panchanga.tithi.number} of 30", ChartDetailColors.TextPrimary)
            DetailRow("Paksha", panchanga.paksha.displayName, ChartDetailColors.TextSecondary)
            DetailRow("Lord", panchanga.tithi.lord.displayName, ChartDetailColors.AccentPurple)
            DetailRow("Progress", "${String.format("%.1f", panchanga.tithi.progress)}%", ChartDetailColors.AccentGold)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "About Tithi",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getTithiDescription(panchanga.tithi.tithi.number),
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun NakshatraCard(panchanga: PanchangaData) {
    PanchangaDetailCard(
        title = "Nakshatra (Lunar Mansion)",
        icon = Icons.Outlined.Star,
        iconColor = ChartDetailColors.AccentPurple
    ) {
        Column {
            DetailRow("Name", panchanga.nakshatra.nakshatra.displayName, ChartDetailColors.AccentPurple)
            DetailRow("Number", "${panchanga.nakshatra.number} of 27", ChartDetailColors.TextPrimary)
            DetailRow("Ruler", panchanga.nakshatra.lord.displayName, ChartDetailColors.AccentGold)
            DetailRow("Pada", "${panchanga.nakshatra.pada} of 4", ChartDetailColors.AccentTeal)
            DetailRow("Progress", "${String.format("%.1f", panchanga.nakshatra.progress)}%", ChartDetailColors.AccentGold)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Nakshatra Characteristics",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getNakshatraDescription(panchanga.nakshatra.nakshatra),
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun YogaCard(panchanga: PanchangaData) {
    PanchangaDetailCard(
        title = "Yoga (Luni-Solar Combination)",
        icon = Icons.Outlined.WbSunny,
        iconColor = ChartDetailColors.AccentGold
    ) {
        Column {
            DetailRow("Name", panchanga.yoga.yoga.displayName, ChartDetailColors.AccentGold)
            DetailRow("Number", "${panchanga.yoga.number} of 27", ChartDetailColors.TextPrimary)
            DetailRow("Nature", panchanga.yoga.yoga.nature, when(panchanga.yoga.yoga.nature) {
                "Auspicious" -> ChartDetailColors.SuccessColor
                "Inauspicious" -> ChartDetailColors.WarningColor
                else -> ChartDetailColors.TextSecondary
            })
            DetailRow("Progress", "${String.format("%.1f", panchanga.yoga.progress)}%", ChartDetailColors.AccentTeal)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "Yoga Effects",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getYogaDescription(panchanga.yoga.yoga),
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun KaranaCard(panchanga: PanchangaData) {
    PanchangaDetailCard(
        title = "Karana (Half Tithi)",
        icon = Icons.Outlined.CalendarMonth,
        iconColor = ChartDetailColors.AccentBlue
    ) {
        Column {
            DetailRow("Name", panchanga.karana.karana.displayName, ChartDetailColors.AccentBlue)
            DetailRow("Number", "${panchanga.karana.number} of 60", ChartDetailColors.TextPrimary)
            DetailRow("Type", panchanga.karana.karana.nature, ChartDetailColors.TextSecondary)
            DetailRow("Progress", "${String.format("%.1f", panchanga.karana.progress)}%", ChartDetailColors.AccentGold)

            HorizontalDivider(
                color = ChartDetailColors.DividerColor,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Text(
                text = "About Karana",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = ChartDetailColors.TextSecondary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = getKaranaDescription(panchanga.karana.karana),
                fontSize = 13.sp,
                color = ChartDetailColors.TextPrimary,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
private fun VaraCard(panchanga: PanchangaData) {
    PanchangaDetailCard(
        title = "Vara (Weekday)",
        icon = Icons.Outlined.CalendarMonth,
        iconColor = ChartDetailColors.AccentOrange
    ) {
        Column {
            DetailRow("Day", panchanga.vara.displayName, ChartDetailColors.AccentOrange)
            DetailRow("Ruling Planet", panchanga.vara.lord.displayName, ChartDetailColors.getPlanetColor(panchanga.vara.lord))

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
                text = getVaraDescription(panchanga.vara),
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

// Helper functions for descriptions
private fun getTithiDescription(tithiNumber: Int): String {
    return when (tithiNumber) {
        1, 16 -> "Pratipada - New beginnings, starting new projects."
        2, 17 -> "Dwitiya - Good for social activities and travel."
        3, 18 -> "Tritiya - Favorable for celebrations and auspicious events."
        4, 19 -> "Chaturthi - Mixed results, worship of Ganesha recommended."
        5, 20 -> "Panchami - Excellent for learning and education."
        6, 21 -> "Shashthi - Good for medical treatments and healing."
        7, 22 -> "Saptami - Favorable for journeys and pilgrimages."
        8, 23 -> "Ashtami - Mixed, good for spiritual practices."
        9, 24 -> "Navami - Aggressive activities, worship of Durga."
        10, 25 -> "Dashami - Victory and success, good for important tasks."
        11, 26 -> "Ekadashi - Highly spiritual, fasting recommended."
        12, 27 -> "Dwadashi - Good for religious ceremonies."
        13, 28 -> "Trayodashi - Favorable for worship of Shiva."
        14, 29 -> "Chaturdashi - Mixed, good for tantric practices."
        15 -> "Purnima - Full Moon, highly auspicious for all activities."
        30 -> "Amavasya - New Moon, good for ancestral rites and spiritual practices."
        else -> "Varies based on planetary influences."
    }
}

private fun getNakshatraDescription(nakshatra: com.astro.storm.data.model.Nakshatra): String {
    return "${nakshatra.displayName} is ruled by ${nakshatra.ruler.displayName}. " +
            "Each nakshatra has unique characteristics that influence personality, " +
            "life events, and compatibility. The pada (quarter) further refines these influences."
}

private fun getYogaDescription(yoga: com.astro.storm.ephemeris.Yoga): String {
    val nature = if (yoga.nature == "Auspicious") "auspicious" else "challenging"
    return "${yoga.displayName} is considered $nature in Vedic astrology. " +
            "Yoga is calculated from the sum of Sun and Moon longitudes and " +
            "influences the overall quality of time for activities."
}

private fun getKaranaDescription(karana: com.astro.storm.ephemeris.Karana): String {
    return "${karana.displayName} is a ${karana.nature.lowercase()} karana. " +
            "Karanas are half-tithis and provide more refined timing for muhurta. " +
            "There are 11 karanas that cycle through the lunar month."
}

private fun getVaraDescription(vara: com.astro.storm.ephemeris.Vara): String {
    return when (vara) {
        com.astro.storm.ephemeris.Vara.SUNDAY -> "Sunday is ruled by the Sun. Favorable for government matters, authority figures, health initiatives, and spiritual practices."
        com.astro.storm.ephemeris.Vara.MONDAY -> "Monday is ruled by the Moon. Good for travel, public dealings, emotional matters, and starting new ventures."
        com.astro.storm.ephemeris.Vara.TUESDAY -> "Tuesday is ruled by Mars. Suitable for property matters, surgery, competitive activities, and physical endeavors."
        com.astro.storm.ephemeris.Vara.WEDNESDAY -> "Wednesday is ruled by Mercury. Excellent for education, communication, business deals, and intellectual pursuits."
        com.astro.storm.ephemeris.Vara.THURSDAY -> "Thursday is ruled by Jupiter. Most auspicious for religious ceremonies, marriages, education, and financial matters."
        com.astro.storm.ephemeris.Vara.FRIDAY -> "Friday is ruled by Venus. Ideal for romantic matters, artistic activities, luxury purchases, and entertainment."
        com.astro.storm.ephemeris.Vara.SATURDAY -> "Saturday is ruled by Saturn. Good for property, agriculture, labor-related work, and spiritual discipline."
    }
}
