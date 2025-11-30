package com.astro.storm.ui.components.dialogs

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.astro.storm.data.model.VedicChart
import com.astro.storm.ephemeris.DivisionalChartData
import com.astro.storm.ui.chart.ChartRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/**
 * Full-screen chart dialog with zoom, pan, and download functionality.
 *
 * Features:
 * - Pinch-to-zoom and pan gestures
 * - High-resolution chart export to gallery
 * - Supports both main chart and divisional charts
 * - Zoom indicator with reset capability
 */
@Composable
fun FullScreenChartDialog(
    chart: VedicChart,
    chartRenderer: ChartRenderer,
    chartTitle: String,
    divisionalChartData: DivisionalChartData? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    val scope = rememberCoroutineScope()

    var isDownloading by remember { mutableStateOf(false) }
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DialogColors.DialogBackground)
        ) {
            // Chart canvas with zoom/pan
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .transformable(
                        state = rememberTransformableState { zoomChange, panChange, _ ->
                            scale = (scale * zoomChange).coerceIn(0.5f, 3f)
                            offsetX += panChange.x
                            offsetY += panChange.y
                        }
                    )
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(16.dp)
                ) {
                    if (divisionalChartData != null) {
                        chartRenderer.drawDivisionalChart(
                            drawScope = this,
                            planetPositions = divisionalChartData.planetPositions,
                            ascendantLongitude = divisionalChartData.ascendantLongitude,
                            size = size.minDimension,
                            chartTitle = chartTitle,
                            originalChart = chart
                        )
                    } else {
                        chartRenderer.drawNorthIndianChart(
                            drawScope = this,
                            chart = chart,
                            size = size.minDimension,
                            chartTitle = chartTitle
                        )
                    }
                }
            }

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DialogColors.DialogBackground.copy(alpha = 0.9f))
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chartTitle,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = DialogColors.AccentGold
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = DialogColors.TextPrimary
                    )
                }
            }

            // Bottom action bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DialogColors.DialogBackground.copy(alpha = 0.9f))
                    .padding(16.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ActionButton(
                    icon = Icons.Default.CenterFocusStrong,
                    label = "Reset",
                    onClick = {
                        scale = 1f
                        offsetX = 0f
                        offsetY = 0f
                    }
                )

                ActionButton(
                    icon = Icons.Default.ZoomIn,
                    label = "Zoom In",
                    onClick = { scale = (scale * 1.2f).coerceAtMost(3f) }
                )

                ActionButton(
                    icon = Icons.Default.ZoomOut,
                    label = "Zoom Out",
                    onClick = { scale = (scale / 1.2f).coerceAtLeast(0.5f) }
                )

                ActionButton(
                    icon = if (isDownloading) Icons.Default.HourglassEmpty else Icons.Default.Download,
                    label = if (isDownloading) "Saving..." else "Download",
                    onClick = {
                        if (!isDownloading) {
                            isDownloading = true
                            scope.launch {
                                val success = saveChartToGallery(
                                    context = context,
                                    chartRenderer = chartRenderer,
                                    chart = chart,
                                    divisionalChartData = divisionalChartData,
                                    chartTitle = chartTitle,
                                    density = density
                                )

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        if (success) "Chart saved to gallery!" else "Failed to save chart",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                                isDownloading = false
                            }
                        }
                    },
                    enabled = !isDownloading
                )
            }

            // Zoom indicator
            AnimatedVisibility(
                visible = scale != 1f,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 72.dp, end = 16.dp),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = DialogColors.DialogSurface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = "${(scale * 100).toInt()}%",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        fontSize = 14.sp,
                        color = DialogColors.TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(8.dp)
    ) {
        Icon(
            icon,
            contentDescription = label,
            tint = if (enabled) DialogColors.AccentGold else DialogColors.TextMuted,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = if (enabled) DialogColors.TextSecondary else DialogColors.TextMuted
        )
    }
}

/**
 * Saves the chart as a high-resolution PNG to the device gallery.
 */
private suspend fun saveChartToGallery(
    context: Context,
    chartRenderer: ChartRenderer,
    chart: VedicChart,
    divisionalChartData: DivisionalChartData?,
    chartTitle: String,
    density: androidx.compose.ui.unit.Density
): Boolean = withContext(Dispatchers.IO) {
    try {
        val size = 2048
        val bitmap = if (divisionalChartData != null) {
            chartRenderer.createDivisionalChartBitmap(
                planetPositions = divisionalChartData.planetPositions,
                ascendantLongitude = divisionalChartData.ascendantLongitude,
                chartTitle = chartTitle,
                width = size,
                height = size,
                density = density
            )
        } else {
            chartRenderer.createChartBitmap(chart, size, size, density)
        }

        val filename = "AstroStorm_${chartTitle.replace(" ", "_")}_${System.currentTimeMillis()}.png"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val contentValues = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
                put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AstroStorm")
                put(MediaStore.Images.Media.IS_PENDING, 1)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            ) ?: return@withContext false

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            contentValues.clear()
            contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
            context.contentResolver.update(uri, contentValues, null, null)
        } else {
            @Suppress("DEPRECATION")
            val directory = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES + "/AstroStorm"
            )
            if (!directory.exists()) directory.mkdirs()

            val file = java.io.File(directory, filename)
            java.io.FileOutputStream(file).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            }

            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DATA, file.absolutePath)
                put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            }
            context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        }

        bitmap.recycle()
        true
    } catch (e: IOException) {
        e.printStackTrace()
        false
    }
}
