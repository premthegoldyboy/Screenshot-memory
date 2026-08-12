package com.example.screenshotmemory.data.repository

import android.content.Context
import android.util.Log
import com.example.screenshotmemory.data.db.ScreenshotDao
import com.example.screenshotmemory.data.db.ScreenshotEntity
import com.example.screenshotmemory.data.ocr.OcrManager
import com.example.screenshotmemory.data.scanner.MediaStoreScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.util.Calendar

enum class DateFilter {
    ALL, TODAY, YESTERDAY, LAST_7_DAYS, LAST_30_DAYS, THIS_YEAR
}

enum class SortOption {
    RELEVANCE, NEWEST, OLDEST
}

data class IndexingState(
    val isScanning: Boolean = false,
    val isOcrRunning: Boolean = false,
    val totalPending: Int = 0,
    val processedCount: Int = 0,
    val lastScanTime: Long = 0L,
    val statusMessage: String = ""
)

class ScreenshotRepository(
    private val context: Context,
    private val dao: ScreenshotDao
) {
    private val scanner = MediaStoreScanner(context)
    private val ocrManager = OcrManager(context)

    private val _indexingState = MutableStateFlow(IndexingState())
    val indexingState: StateFlow<IndexingState> = _indexingState.asStateFlow()

    val allScreenshotsFlow: Flow<List<ScreenshotEntity>> = dao.getAllScreenshotsFlow()
    val favoriteScreensFlow: Flow<List<ScreenshotEntity>> = dao.getFavoriteScreenshotsFlow()
    val totalCountFlow: Flow<Int> = dao.getTotalCountFlow()
    val indexedCountFlow: Flow<Int> = dao.getIndexedCountFlow()
    val pendingCountFlow: Flow<Int> = dao.getPendingCountFlow()
    val favoriteCountFlow: Flow<Int> = dao.getFavoriteCountFlow()

    fun getScreenshotByIdFlow(id: Long): Flow<ScreenshotEntity?> = dao.getByIdFlow(id)

    suspend fun getScreenshotById(id: Long): ScreenshotEntity? = dao.getById(id)

    suspend fun syncAndIndex() = withContext(Dispatchers.IO) {
        if (_indexingState.value.isScanning || _indexingState.value.isOcrRunning) return@withContext

        _indexingState.value = _indexingState.value.copy(
            isScanning = true,
            statusMessage = "Scanning for screenshots..."
        )

        try {
            // Step 1: Scan MediaStore
            val foundScreenshots = scanner.scanScreenshots()
            val existingUris = dao.getAllUris().toSet()

            val newScreenshots = foundScreenshots.filter { it.uri !in existingUris }
            if (newScreenshots.isNotEmpty()) {
                dao.insertAll(newScreenshots)
            }

            // Step 2: Verify deleted screenshots
            val allDbUris = dao.getAllUris()
            for (uri in allDbUris) {
                if (!scanner.verifyUriExists(uri)) {
                    dao.deleteByUri(uri)
                }
            }

            _indexingState.value = _indexingState.value.copy(
                isScanning = false,
                lastScanTime = System.currentTimeMillis(),
                statusMessage = "Scan complete. Starting OCR..."
            )

            // Step 3: Run OCR on pending items
            runPendingOcr()

        } catch (e: Exception) {
            Log.e("ScreenshotRepository", "Error syncing screenshots", e)
            _indexingState.value = _indexingState.value.copy(
                isScanning = false,
                isOcrRunning = false,
                statusMessage = "Scan failed: ${e.message}"
            )
        }
    }

    suspend fun runPendingOcr() = withContext(Dispatchers.IO) {
        val pendingItems = dao.getPendingOcrItems()
        if (pendingItems.isEmpty()) {
            _indexingState.value = _indexingState.value.copy(
                isOcrRunning = false,
                totalPending = 0,
                processedCount = 0,
                statusMessage = "All screenshots indexed"
            )
            return@withContext
        }

        _indexingState.value = _indexingState.value.copy(
            isOcrRunning = true,
            totalPending = pendingItems.size,
            processedCount = 0,
            statusMessage = "Recognizing text (0/${pendingItems.size})..."
        )

        var processed = 0
        for (item in pendingItems) {
            if (!scanner.verifyUriExists(item.uri)) {
                dao.deleteById(item.id)
            } else {
                val ocrResult = ocrManager.extractTextFromUri(item.uri)
                val text = ocrResult.getOrDefault("")
                val newStatus = if (ocrResult.isSuccess) {
                    ScreenshotEntity.INDEX_STATUS_COMPLETED
                } else {
                    ScreenshotEntity.INDEX_STATUS_FAILED
                }

                val updatedItem = item.copy(
                    ocrText = text,
                    indexingStatus = newStatus
                )
                dao.update(updatedItem)
            }

            processed++
            _indexingState.value = _indexingState.value.copy(
                processedCount = processed,
                statusMessage = "Recognizing text ($processed/${pendingItems.size})..."
            )
        }

        _indexingState.value = _indexingState.value.copy(
            isOcrRunning = false,
            statusMessage = "Indexing complete ($processed screenshots processed)"
        )
    }

    fun searchScreenshots(
        queryFlow: StateFlow<String>,
        filterFlow: StateFlow<DateFilter>,
        sortFlow: StateFlow<SortOption>
    ): Flow<List<ScreenshotEntity>> {
        return combine(allScreenshotsFlow, queryFlow, filterFlow, sortFlow) { list, query, filter, sort ->
            val now = System.currentTimeMillis()
            val filteredByDate = list.filter { item ->
                matchDateFilter(item.dateTaken, filter, now)
            }

            val queryClean = query.trim().lowercase()
            if (queryClean.isEmpty()) {
                return@combine when (sort) {
                    SortOption.RELEVANCE, SortOption.NEWEST -> filteredByDate.sortedByDescending { it.dateTaken }
                    SortOption.OLDEST -> filteredByDate.sortedBy { it.dateTaken }
                }
            }

            val tokens = queryClean.split(Regex("\\s+")).filter { it.isNotBlank() }

            val scoredItems = filteredByDate.mapNotNull { item ->
                val ocrLower = item.ocrText.lowercase()
                val filenameLower = item.filename.lowercase()
                var score = 0

                if (ocrLower.contains(queryClean)) {
                    score += 50
                }
                if (filenameLower.contains(queryClean)) {
                    score += 30
                }

                for (token in tokens) {
                    if (ocrLower.contains(token)) {
                        score += 15
                    }
                    if (filenameLower.contains(token)) {
                        score += 10
                    }
                }

                if (score > 0) {
                    Pair(item, score)
                } else {
                    null
                }
            }

            val sortedList = when (sort) {
                SortOption.RELEVANCE -> scoredItems.sortedWith(
                    compareByDescending<Pair<ScreenshotEntity, Int>> { it.second }
                        .thenByDescending { it.first.dateTaken }
                ).map { it.first }
                SortOption.NEWEST -> scoredItems.sortedByDescending { it.first.dateTaken }.map { it.first }
                SortOption.OLDEST -> scoredItems.sortedBy { it.first.dateTaken }.map { it.first }
            }

            sortedList
        }
    }

    private fun matchDateFilter(dateTaken: Long, filter: DateFilter, nowMillis: Long): Boolean {
        if (filter == DateFilter.ALL) return true

        val cal = Calendar.getInstance().apply { timeInMillis = nowMillis }
        val currentYear = cal.get(Calendar.YEAR)

        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val todayStart = cal.timeInMillis

        return when (filter) {
            DateFilter.ALL -> true
            DateFilter.TODAY -> dateTaken >= todayStart
            DateFilter.YESTERDAY -> {
                val yesterdayStart = todayStart - 86400000L
                dateTaken in yesterdayStart..<todayStart
            }
            DateFilter.LAST_7_DAYS -> dateTaken >= (nowMillis - 7 * 86400000L)
            DateFilter.LAST_30_DAYS -> dateTaken >= (nowMillis - 30 * 86400000L)
            DateFilter.THIS_YEAR -> {
                val yearCal = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.DAY_OF_YEAR, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                dateTaken >= yearCal.timeInMillis
            }
        }
    }

    suspend fun updateScreenshot(item: ScreenshotEntity) {
        dao.update(item)
    }

    suspend fun deleteScreenshot(id: Long) {
        dao.deleteById(id)
    }

    suspend fun clearIndex() {
        dao.deleteAll()
    }
}
