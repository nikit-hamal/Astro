package com.astro.storm.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for chart operations
 */
@Dao
interface ChartDao {
    @Query("SELECT * FROM charts ORDER BY createdAt DESC")
    fun getAllCharts(): Flow<List<ChartEntity>>

    @Query("SELECT * FROM charts WHERE id = :id")
    suspend fun getChartById(id: Long): ChartEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChart(chart: ChartEntity): Long

    @Update
    suspend fun updateChart(chart: ChartEntity)

    @Delete
    suspend fun deleteChart(chart: ChartEntity)

    @Query("DELETE FROM charts WHERE id = :id")
    suspend fun deleteChartById(id: Long)

    @Query("SELECT COUNT(*) FROM charts")
    suspend fun getChartCount(): Int

    @Query("SELECT * FROM charts WHERE name LIKE '%' || :query || '%' OR location LIKE '%' || :query || '%'")
    fun searchCharts(query: String): Flow<List<ChartEntity>>

    @Transaction
    suspend fun setSelectedChart(chartId: Long) {
        clearAllSelections()
        setChartSelected(chartId)
    }

    @Query("UPDATE charts SET isSelected = 0")
    suspend fun clearAllSelections()

    @Query("UPDATE charts SET isSelected = 1 WHERE id = :chartId")
    suspend fun setChartSelected(chartId: Long)
}
