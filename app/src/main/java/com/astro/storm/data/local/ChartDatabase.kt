package com.astro.storm.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * Room database for chart persistence
 */
@Database(
    entities = [ChartEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ChartDatabase : RoomDatabase() {
    abstract fun chartDao(): ChartDao

    companion object {
        @Volatile
        private var INSTANCE: ChartDatabase? = null

        fun getInstance(context: Context): ChartDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ChartDatabase::class.java,
                    "astrostorm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
