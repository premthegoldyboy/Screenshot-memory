package com.example.screenshotmemory.ui.screens.viewer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.screenshotmemory.data.db.AppDatabase
import com.example.screenshotmemory.data.db.ScreenshotEntity
import com.example.screenshotmemory.data.repository.ScreenshotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ScreenshotViewerViewModel(
    application: Application,
    private val screenshotId: Long
) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = ScreenshotRepository(application, db.screenshotDao())

    private val _screenshot = MutableStateFlow<ScreenshotEntity?>(null)
    val screenshot: StateFlow<ScreenshotEntity?> = _screenshot.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getScreenshotByIdFlow(screenshotId).collect { item ->
                _screenshot.value = item
            }
        }
    }

    fun updateScreenshot(updatedScreenshot: ScreenshotEntity) {
        viewModelScope.launch {
            repository.updateScreenshot(updatedScreenshot)
        }
    }

    fun deleteScreenshot(onDeleted: () -> Unit) {
        viewModelScope.launch {
            repository.deleteScreenshot(screenshotId)
            onDeleted()
        }
    }
}
