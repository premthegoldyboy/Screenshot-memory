package com.example.screenshotmemory.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.screenshotmemory.data.db.AppDatabase
import com.example.screenshotmemory.data.repository.ScreenshotRepository

class IndexWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val db = AppDatabase.getDatabase(applicationContext)
            val repository = ScreenshotRepository(applicationContext, db.screenshotDao())
            repository.syncAndIndex()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}
