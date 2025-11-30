package com.astro.storm.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astro.storm.data.model.Action
import com.astro.storm.data.model.InsightsData
import com.astro.storm.data.model.PlanetaryPeriod
import com.astro.storm.data.model.Transit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen() {
    val mockData = InsightsData(
        currentPlanetaryPeriod = PlanetaryPeriod(
            name = "Venus Dasa",
            yearsRemaining = 1,
            progress = 0.8f
        ),
        upcomingTransits = listOf(
            Transit(
                name = "Saturn Transit in Aquarius",
                date = "20th December 2025"
            )
        ),
        otherActions = listOf(
            Action(
                name = "Matchmaking",
                icon = Icons.Default.Favorite
            ),
            Action(
                name = "Horoscope",
                icon = Icons.Default.DateRange
            ),
            Action(
                name = "Full Chart",
                icon = Icons.Default.List
            )
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights") }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.padding(paddingValues),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedby(16.dp)
        ) {
            item {
                CurrentPlanetaryPeriod(mockData.currentPlanetaryPeriod)
            }
            item {
                UpcomingTransits(mockData.upcomingTransits)
            }
            item {
                OtherActions(mockData.otherActions)
            }
        }
    }
}

@Composable
fun CurrentPlanetaryPeriod(period: PlanetaryPeriod) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Current Planetary Period",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(period.name, style = MaterialTheme.typography.bodyLarge)
                Text(
                    "${period.yearsRemaining} years remaining",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = period.progress,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun UpcomingTransits(transits: List<Transit>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Upcoming Transits",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                transits.forEach { transit ->
                    Text(transit.name, style = MaterialTheme.typography.bodyLarge)
                    Text(transit.date, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Composable
fun OtherActions(actions: List<Action>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                "Other Actions",
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column {
                actions.forEach { action ->
                    ActionItem(
                        icon = action.icon,
                        text = action.name,
                        onClick = {}
                    )
                }
            }
        }
    }
}

@Composable
fun ActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )
        Icon(
            imageVector = Icons.Default.ArrowForward,
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
    }
}