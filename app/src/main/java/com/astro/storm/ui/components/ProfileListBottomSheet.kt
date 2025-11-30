package com.astro.storm.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.astro.storm.data.repository.SavedChart

@Composable
fun ProfileListBottomSheet(
    charts: List<SavedChart>,
    onChartSelected: (SavedChart) -> Unit,
    onAddNewChart: () -> Unit,
    onDeleteChart: (Long) -> Unit
) {
    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(
            "Switch Profile",
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn {
            items(charts) { chart ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onChartSelected(chart) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = chart.name,
                        modifier = Modifier.weight(1f)
                    )
                    if (chart.isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected"
                        )
                    }
                    IconButton(onClick = { onDeleteChart(chart.id) }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onAddNewChart() }
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add new chart"
            )
            Text(
                text = "Add new chart",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}