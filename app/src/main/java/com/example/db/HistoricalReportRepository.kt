package com.example.db

import kotlinx.coroutines.flow.Flow

class HistoricalReportRepository(private val dao: HistoricalReportDao) {
    val allReports: Flow<List<HistoricalReport>> = dao.getAllReports()

    suspend fun insert(report: HistoricalReport): Long {
        return dao.insertReport(report)
    }

    suspend fun getReportById(id: Int): HistoricalReport? {
        return dao.getReportById(id)
    }

    suspend fun deleteById(id: Int) {
        dao.deleteReportById(id)
    }

    suspend fun clearAll() {
        dao.clearAllReports()
    }
}
