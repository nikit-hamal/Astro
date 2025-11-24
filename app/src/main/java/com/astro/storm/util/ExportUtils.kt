package com.astro.storm.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.astro.storm.data.model.VedicChart
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Utilities for exporting charts
 */
object ExportUtils {

    /**
     * Save chart image to device storage
     */
    suspend fun saveChartImage(
        context: Context,
        bitmap: Bitmap,
        fileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val displayName = "$fileName.png"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, displayName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "image/png")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/AstroStorm")
                }

                val uri = context.contentResolver.insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    contentValues
                ) ?: return@withContext Result.failure(Exception("Failed to create MediaStore entry"))

                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                } ?: return@withContext Result.failure(Exception("Failed to open output stream"))

                Result.success(uri.toString())
            } else {
                @Suppress("DEPRECATION")
                val picturesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                val astroStormDir = File(picturesDir, "AstroStorm")
                if (!astroStormDir.exists()) {
                    astroStormDir.mkdirs()
                }

                val file = File(astroStormDir, displayName)
                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                }

                Result.success(file.absolutePath)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get plaintext representation of chart for clipboard/LLM
     */
    fun getChartPlaintext(chart: VedicChart): String {
        return chart.toPlainText()
    }

    /**
     * Copy text to clipboard
     */
    fun copyToClipboard(context: Context, text: String, label: String = "Chart Data") {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }
}
