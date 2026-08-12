package com.example.screenshotmemory.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "screenshots",
    indices = [
        Index(value = ["uri"], unique = true),
        Index(value = ["dateTaken"])
    ]
)
data class ScreenshotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val uri: String,
    val filename: String,
    val relativePath: String?,
    val dateTaken: Long,
    val dateModified: Long,
    val size: Long,
    val width: Int,
    val height: Int,
    val ocrText: String = "",
    val notes: String = "",
    val tags: String = "",
    val isFavorite: Boolean = false,
    val indexingStatus: String = INDEX_STATUS_PENDING,
    val isScreenshot: Boolean = true
) {
    companion object {
        const val INDEX_STATUS_PENDING = "PENDING"
        const val INDEX_STATUS_COMPLETED = "COMPLETED"
        const val INDEX_STATUS_FAILED = "FAILED"
    }
}
