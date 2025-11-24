package com.astro.storm.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.astro.storm.data.model.BirthData
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Screen for inputting birth data
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartInputScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit,
    onChartCalculated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var name by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("1990-01-01") }
    var time by remember { mutableStateOf("12:00") }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var timezone by remember { mutableStateOf(ZoneId.systemDefault().id) }

    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ChartUiState.Success -> {
                val chart = (uiState as ChartUiState.Success).chart
                viewModel.saveChart(chart)
            }
            is ChartUiState.Saved -> {
                onChartCalculated()
            }
            is ChartUiState.Error -> {
                errorMessage = (uiState as ChartUiState.Error).message
                showError = true
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Chart") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Name Input
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Location Input
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            // Date Input
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date (YYYY-MM-DD)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Time Input
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time (HH:MM)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    Icon(Icons.Default.Schedule, contentDescription = null)
                },
                shape = RoundedCornerShape(12.dp)
            )

            // Latitude Input
            OutlinedTextField(
                value = latitude,
                onValueChange = { latitude = it },
                label = { Text("Latitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Example: 40.7128 for New York") },
                shape = RoundedCornerShape(12.dp)
            )

            // Longitude Input
            OutlinedTextField(
                value = longitude,
                onValueChange = { longitude = it },
                label = { Text("Longitude") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                supportingText = { Text("Example: -74.0060 for New York") },
                shape = RoundedCornerShape(12.dp)
            )

            // Timezone Input
            OutlinedTextField(
                value = timezone,
                onValueChange = { timezone = it },
                label = { Text("Timezone") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                supportingText = { Text("Example: America/New_York") },
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Calculate Button
            Button(
                onClick = {
                    try {
                        val dateTime = LocalDateTime.parse("${date}T${time}:00")
                        val birthData = BirthData(
                            name = name.ifBlank { "Unknown" },
                            dateTime = dateTime,
                            latitude = latitude.toDouble(),
                            longitude = longitude.toDouble(),
                            timezone = timezone,
                            location = location.ifBlank { "Unknown" }
                        )
                        viewModel.calculateChart(birthData)
                    } catch (e: Exception) {
                        errorMessage = "Invalid input: ${e.message}"
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState !is ChartUiState.Calculating
            ) {
                if (uiState is ChartUiState.Calculating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Calculate Chart", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text("Error") },
            text = { Text(errorMessage) },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK")
                }
            }
        )
    }
}
