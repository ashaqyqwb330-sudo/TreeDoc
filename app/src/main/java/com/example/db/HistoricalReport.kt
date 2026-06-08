package com.example.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "historical_reports")
data class HistoricalReport(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val rootPath: String,
    val outputName: String,
    val timestamp: Long = System.currentTimeMillis(),
    val foldersCount: Int,
    val filesCount: Int,
    val sizeText: String,
    val jsonContent: String, // Contains the full generated JSON tree for offline search and structure comparison
    val format: String
)
