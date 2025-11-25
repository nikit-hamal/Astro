package com.astro.storm.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.astro.storm.data.model.BirthData
import com.astro.storm.ui.viewmodel.ChartUiState
import com.astro.storm.ui.viewmodel.ChartViewModel
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.TimeZone

// Dark brown theme colors matching reference
private val ScreenBackground = Color(0xFF1C1410)
private val CardBackground = Color(0xFF2A201A)
private val AccentColor = Color(0xFFB8A99A)
private val TextPrimary = Color(0xFFE8DFD6)
private val TextSecondary = Color(0xFFB8A99A)
private val BorderColor = Color(0xFF4A3F38)
private val ChipBackground = Color(0xFF3D322B)
private val ButtonBackground = Color(0xFFB8A99A)
private val ButtonText = Color(0xFF1C1410)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChartInputScreen(
    viewModel: ChartViewModel,
    onNavigateBack: () -> Unit,
    onChartCalculated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current

    var name by remember { mutableStateOf("") }
    var locationLabel by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 0)) }
    var latitude by remember { mutableStateOf("") }
    var longitude by remember { mutableStateOf("") }
    var altitude by remember { mutableStateOf("") }
    var selectedTimezone by remember { mutableStateOf(ZoneId.systemDefault().id) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showTimezoneDropdown by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Common timezones list
    val timezones = remember {
        listOf(
            "Asia/Kathmandu",
            "Asia/Kolkata",
            "Asia/Dubai",
            "Asia/Singapore",
            "Asia/Tokyo",
            "Asia/Shanghai",
            "Asia/Hong_Kong",
            "Europe/London",
            "Europe/Paris",
            "Europe/Berlin",
            "America/New_York",
            "America/Los_Angeles",
            "America/Chicago",
            "America/Denver",
            "Australia/Sydney",
            "Pacific/Auckland"
        ).plus(
            TimeZone.getAvailableIDs()
                .filter { it.contains("/") && !it.startsWith("Etc/") }
                .sorted()
        ).distinct()
    }

    // Reset state when entering the screen to prevent auto-navigation
    LaunchedEffect(Unit) {
        viewModel.resetState()
    }

    // Track if we initiated a chart calculation in this session
    var chartCalculationInitiated by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is ChartUiState.Success -> {
                if (chartCalculationInitiated) {
                    val chart = (uiState as ChartUiState.Success).chart
                    viewModel.saveChart(chart)
                }
            }
            is ChartUiState.Saved -> {
                if (chartCalculationInitiated) {
                    chartCalculationInitiated = false
                    onChartCalculated()
                }
            }
            is ChartUiState.Error -> {
                errorMessage = (uiState as ChartUiState.Error).message
                showError = true
                chartCalculationInitiated = false
            }
            else -> {}
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 48.dp, bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "AstroStorm Chart",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    color = TextPrimary,
                    letterSpacing = 0.5.sp
                )
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = "History",
                        tint = TextSecondary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Identity Section
            SectionTitle("Identity")

            Spacer(modifier = Modifier.height(12.dp))

            StyledOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = "Full name",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            StyledOutlinedTextField(
                value = locationLabel,
                onValueChange = { locationLabel = it },
                label = "Location label (optional)",
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Date & Time Section
            SectionTitle("Date & Time")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Date Chip
                DateTimeChip(
                    text = selectedDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                    onClick = { showDatePicker = true },
                    modifier = Modifier.weight(1f)
                )

                // Time Chip
                DateTimeChip(
                    text = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                    onClick = { showTimePicker = true },
                    modifier = Modifier.weight(0.7f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timezone Dropdown
            ExposedDropdownMenuBox(
                expanded = showTimezoneDropdown,
                onExpandedChange = { showTimezoneDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedTimezone,
                    onValueChange = {},
                    readOnly = true,
                    label = {
                        Text(
                            "Timezone",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTimezoneDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = BorderColor,
                        unfocusedBorderColor = BorderColor,
                        focusedLabelColor = TextSecondary,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = AccentColor,
                        focusedTrailingIconColor = TextSecondary,
                        unfocusedTrailingIconColor = TextSecondary
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )

                ExposedDropdownMenu(
                    expanded = showTimezoneDropdown,
                    onDismissRequest = { showTimezoneDropdown = false },
                    modifier = Modifier
                        .background(CardBackground)
                        .heightIn(max = 300.dp)
                ) {
                    timezones.forEach { timezone ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = timezone,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            },
                            onClick = {
                                selectedTimezone = timezone
                                showTimezoneDropdown = false
                            },
                            colors = MenuDefaults.itemColors(
                                textColor = TextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Coordinates Section
            SectionTitle("Coordinates")

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StyledOutlinedTextField(
                    value = latitude,
                    onValueChange = { latitude = it },
                    label = "Latitude",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Right) }
                    )
                )

                StyledOutlinedTextField(
                    value = longitude,
                    onValueChange = { longitude = it },
                    label = "Longitude",
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            StyledOutlinedTextField(
                value = altitude,
                onValueChange = { altitude = it },
                label = "Altitude (m)",
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Generate & Save Button
            Button(
                onClick = {
                    try {
                        val lat = latitude.toDoubleOrNull()
                        val lon = longitude.toDoubleOrNull()

                        if (lat == null || lon == null) {
                            errorMessage = "Please enter valid latitude and longitude"
                            showError = true
                            return@Button
                        }

                        if (lat < -90 || lat > 90) {
                            errorMessage = "Latitude must be between -90 and 90"
                            showError = true
                            return@Button
                        }

                        if (lon < -180 || lon > 180) {
                            errorMessage = "Longitude must be between -180 and 180"
                            showError = true
                            return@Button
                        }

                        val dateTime = LocalDateTime.of(selectedDate, selectedTime)
                        val birthData = BirthData(
                            name = name.ifBlank { "Unknown" },
                            dateTime = dateTime,
                            latitude = lat,
                            longitude = lon,
                            timezone = selectedTimezone,
                            location = locationLabel.ifBlank { "Unknown" }
                        )
                        chartCalculationInitiated = true
                        viewModel.calculateChart(birthData)
                    } catch (e: Exception) {
                        errorMessage = "Please check your input values"
                        showError = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ButtonBackground,
                    contentColor = ButtonText,
                    disabledContainerColor = ButtonBackground.copy(alpha = 0.5f),
                    disabledContentColor = ButtonText.copy(alpha = 0.5f)
                ),
                enabled = uiState !is ChartUiState.Calculating
            ) {
                AnimatedVisibility(
                    visible = uiState is ChartUiState.Calculating,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = ButtonText,
                        strokeWidth = 2.dp
                    )
                }
                AnimatedVisibility(
                    visible = uiState !is ChartUiState.Calculating,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Generate & Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate.atStartOfDay(ZoneId.systemDefault())
                .toInstant().toEpochMilli()
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = java.time.Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = AccentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            colors = DatePickerDefaults.colors(
                containerColor = CardBackground
            )
        ) {
            DatePicker(
                state = datePickerState,
                colors = DatePickerDefaults.colors(
                    containerColor = CardBackground,
                    titleContentColor = TextPrimary,
                    headlineContentColor = TextPrimary,
                    weekdayContentColor = TextSecondary,
                    subheadContentColor = TextSecondary,
                    yearContentColor = TextPrimary,
                    currentYearContentColor = AccentColor,
                    selectedYearContentColor = ButtonText,
                    selectedYearContainerColor = AccentColor,
                    dayContentColor = TextPrimary,
                    selectedDayContentColor = ButtonText,
                    selectedDayContainerColor = AccentColor,
                    todayContentColor = AccentColor,
                    todayDateBorderColor = AccentColor,
                    navigationContentColor = TextSecondary
                )
            )
        }
    }

    // Material 3 Time Picker Dialog
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = selectedTime.hour,
            initialMinute = selectedTime.minute,
            is24Hour = true
        )

        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = {
                selectedTime = LocalTime.of(timePickerState.hour, timePickerState.minute)
                showTimePicker = false
            }
        ) {
            TimePicker(
                state = timePickerState,
                colors = TimePickerDefaults.colors(
                    containerColor = CardBackground,
                    clockDialColor = ChipBackground,
                    clockDialSelectedContentColor = ButtonText,
                    clockDialUnselectedContentColor = TextPrimary,
                    selectorColor = AccentColor,
                    periodSelectorBorderColor = BorderColor,
                    periodSelectorSelectedContainerColor = AccentColor,
                    periodSelectorUnselectedContainerColor = CardBackground,
                    periodSelectorSelectedContentColor = ButtonText,
                    periodSelectorUnselectedContentColor = TextSecondary,
                    timeSelectorSelectedContainerColor = AccentColor,
                    timeSelectorUnselectedContainerColor = ChipBackground,
                    timeSelectorSelectedContentColor = ButtonText,
                    timeSelectorUnselectedContentColor = TextPrimary
                )
            )
        }
    }

    // Error Snackbar
    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = {
                Text(
                    "Input Error",
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
            },
            text = {
                Text(
                    errorMessage,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text("OK", color = AccentColor)
                }
            },
            containerColor = CardBackground,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 16.sp,
        fontWeight = FontWeight.SemiBold,
        color = TextPrimary,
        letterSpacing = 0.5.sp
    )
}

@Composable
private fun StyledOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = {
            Text(
                label,
                color = TextSecondary,
                fontSize = 14.sp
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary,
            focusedBorderColor = BorderColor,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = TextSecondary,
            unfocusedLabelColor = TextSecondary,
            cursorColor = AccentColor
        ),
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
    )
}

@Composable
private fun DateTimeChip(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(26.dp),
        color = ChipBackground,
        border = BorderStroke(1.dp, BorderColor)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardBackground,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Select time",
                    color = TextSecondary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                )

                content()

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = TextSecondary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    TextButton(onClick = onConfirm) {
                        Text("OK", color = AccentColor)
                    }
                }
            }
        }
    }
}
