package com.example.screenshotmemory.ui.screens.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.screenshotmemory.data.db.AppDatabase
import com.example.screenshotmemory.data.db.ScreenshotEntity
import com.example.screenshotmemory.data.repository.DateFilter
import com.example.screenshotmemory.data.repository.IndexingState
import com.example.screenshotmemory.data.repository.ScreenshotRepository
import com.example.screenshotmemory.data.repository.SortOption
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = ScreenshotRepository(application, db.screenshotDao())

    val searchQuery = MutableStateFlow("")
    val dateFilter = MutableStateFlow(DateFilter.ALL)
    val sortOption = MutableStateFlow(SortOption.RELEVANCE)

    val indexingState: StateFlow<IndexingState> = repository.indexingState
    val totalCount: StateFlow<Int> = repository.totalCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val indexedCount: StateFlow<Int> = repository.indexedCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val pendingCount: StateFlow<Int> = repository.pendingCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val favoriteCount: StateFlow<Int> = repository.favoriteCountFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val searchResults: StateFlow<List<ScreenshotEntity>> = repository.searchScreenshots(
        searchQuery,
        dateFilter,
        sortOption
    ).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteScreens: StateFlow<List<ScreenshotEntity>> = repository.favoriteScreensFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _permissionGranted = MutableStateFlow(false)
    val permissionGranted: StateFlow<Boolean> = _permissionGranted.asStateFlow()

    fun setPermissionGranted(granted: Boolean) {
        _permissionGranted.value = granted
        if (granted) {
            triggerSync()
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.syncAndIndex()
        }
    }

    fun updateSearchQuery(query: String) {
        searchQuery.value = query
    }

    fun updateDateFilter(filter: DateFilter) {
        dateFilter.value = filter
    }

    fun updateSortOption(sort: SortOption) {
        sortOption.value = sort
    }

    fun toggleFavorite(item: ScreenshotEntity) {
        viewModelScope.launch {
            repository.updateScreenshot(item.copy(isFavorite = !item.isFavorite))
        }
    }

    fun clearSearchIndex() {
        viewModelScope.launch {
            repository.clearIndex()
        }
    }
}
