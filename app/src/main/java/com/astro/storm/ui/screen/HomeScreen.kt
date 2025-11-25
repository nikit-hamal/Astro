package com.astro.storm.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.repository.SavedChart
import com.astro.storm.ui.viewmodel.ChartViewModel
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Dark brown theme colors matching the app
private val ScreenBackground = Color(0xFF1C1410)
private val CardBackground = Color(0xFF2A201A)
private val AccentColor = Color(0xFFB8A99A)
private val TextPrimary = Color(0xFFE8DFD6)
private val TextSecondary = Color(0xFFB8A99A)
private val TextMuted = Color(0xFF8A7A6A)
private val BorderColor = Color(0xFF4A3F38)
private val ButtonBackground = Color(0xFFB8A99A)
private val ButtonText = Color(0xFF1C1410)
private val ErrorColor = Color(0xFFCF6679)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChartViewModel,
    onCreateNewChart: () -> Unit,
    onChartClick: (Long) -> Unit
) {
    val savedCharts by viewModel.savedCharts.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp)
        ) {
            // Header
            Text(
                text = "AstroStorm",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Text(
                text = "Your Vedic Charts",
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
            )

            if (savedCharts.isEmpty()) {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(savedCharts, key = { it.id }) { chart ->
                        ChartListItem(
                            chart = chart,
                            onClick = { onChartClick(chart.id) },
                            onDelete = { viewModel.deleteChart(chart.id) }
                        )
                    }
                }
            }
        }

        // Floating Action Button
        ExtendedFloatingActionButton(
            onClick = onCreateNewChart,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            shape = RoundedCornerShape(28.dp),
            containerColor = ButtonBackground,
            contentColor = ButtonText
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create Chart",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                "New Chart",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun EmptyState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.StarOutline,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = AccentColor.copy(alpha = 0.3f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No Charts Yet",
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextPrimary.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Create your first Vedic chart",
            fontSize = 16.sp,
            color = TextMuted
        )
    }
}

@Composable
private fun ChartListItem(
    chart: SavedChart,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = CardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chart.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDateTime(chart.dateTime),
                    fontSize = 14.sp,
                    color = AccentColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = chart.location,
                    fontSize = 13.sp,
                    color = TextMuted
                )
            }

            IconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    contentColor = ErrorColor.copy(alpha = 0.7f)
                )
            ) {
                Icon(
                    Icons.Outlined.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

private fun formatDateTime(dateTimeStr: String): String {
    return try {
        val dateTime = LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
        dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy - hh:mm a"))
    } catch (e: Exception) {
        dateTimeStr
    }
}
