package com.astro.storm.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.astro.storm.data.repository.SavedChart

@Composable
fun ProfileSwitcher(
    currentChart: SavedChart?,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onProfileClick,
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(currentChart?.name ?: "No Profile")
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null
            )
        }
    }
}