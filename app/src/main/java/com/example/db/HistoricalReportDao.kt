package com.example.db

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoricalReportDao {
    @Query("SELECT * FROM historical_reports ORDER BY timestamp DESC")
    fun getAllReports(): Flow<List<HistoricalReport>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: HistoricalReport): Long

    @Query("SELECT * FROM historical_reports WHERE id = :id")
    suspend fun getReportById(id: Int): HistoricalReport?

    @Query("DELETE FROM historical_reports WHERE id = :id")
    suspend fun deleteReportById(id: Int)

    @Query("DELETE FROM historical_reports")
    suspend fun clearAllReports()
}
