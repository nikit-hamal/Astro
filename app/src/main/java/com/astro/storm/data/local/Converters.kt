package com.astro.storm.data.local

import androidx.room.TypeConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Type converters for Room database
 */
class Converters {
    @TypeConverter
    fun fromString(value: String?): List<String>? {
        return value?.split(",")?.map { it.trim() }
    }

    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }

    @TypeConverter
    fun fromDoubleList(value: String?): List<Double>? {
        return value?.split(",")?.mapNotNull { it.toDoubleOrNull() }
    }

    @TypeConverter
    fun toDoubleList(list: List<Double>?): String? {
        return list?.joinToString(",")
    }
}
