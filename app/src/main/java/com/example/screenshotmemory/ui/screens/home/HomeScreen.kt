package com.example.screenshotmemory.ui.screens.home

import android.Manifest
import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.screenshotmemory.ui.components.FilterChipRow
import com.example.screenshotmemory.ui.components.LargeSearchBar
import com.example.screenshotmemory.ui.components.PermissionRequestCard
import com.example.screenshotmemory.ui.components.ScreenshotGridItem
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

enum class HomeTab(val label: String) {
    RECENT("Recent"),
    FAVORITES("Favorites"),
    COLLECTIONS("Collections")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToViewer: (Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }

    val permissionState = rememberPermissionState(permission)

    LaunchedEffect(permissionState.status.isGranted) {
        viewModel.setPermissionGranted(permissionState.status.isGranted)
    }

    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val dateFilter by viewModel.dateFilter.collectAsStateWithLifecycle()
    val sortOption by viewModel.sortOption.collectAsStateWithLifecycle()
    val searchResults by viewModel.searchResults.collectAsStateWithLifecycle()
    val favoriteScreens by viewModel.favoriteScreens.collectAsStateWithLifecycle()
    val indexingState by viewModel.indexingState.collectAsStateWithLifecycle()
    val totalCount by viewModel.totalCount.collectAsStateWithLifecycle()
    val indexedCount by viewModel.indexedCount.collectAsStateWithLifecycle()
    val favoriteCount by viewModel.favoriteCount.collectAsStateWithLifecycle()

    var selectedTab by remember { mutableStateOf(HomeTab.RECENT) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ImageSearch,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Screenshot Memory",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Black
                                )
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Search your screenshots with a premium, private experience",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.triggerSync() },
                        modifier = Modifier.testTag("rescan_appbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Rescan Screenshots"
                        )
                    }
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("settings_appbar_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                ),
                modifier = Modifier.background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surface.copy(alpha = 0.98f)
                        )
                    )
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.triggerSync() },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 10.dp),
                modifier = Modifier.testTag("refresh_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Rescan Screenshots"
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 10.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == HomeTab.RECENT,
                    onClick = { selectedTab = HomeTab.RECENT },
                    icon = { Icon(Icons.Default.ImageSearch, contentDescription = "Recent") },
                    label = { Text("Recent") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.FAVORITES,
                    onClick = { selectedTab = HomeTab.FAVORITES },
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
                    label = { Text("Favorites") }
                )
                NavigationBarItem(
                    selected = selectedTab == HomeTab.COLLECTIONS,
                    onClick = { selectedTab = HomeTab.COLLECTIONS },
                    icon = { Icon(Icons.Default.List, contentDescription = "Collections") },
                    label = { Text("Collections") }
                )
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        )
                    )
                )
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (!permissionState.status.isGranted) {
                    PermissionRequestCard(
                        onRequestPermission = { permissionState.launchPermissionRequest() }
                    )
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = "Discover your screenshot memories",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.SemiBold
                                )
                            )
                            Text(
                                text = "Quickly search, filter, and revisit screenshots with a clean, modern interface.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LargeSearchBar(
                                query = searchQuery,
                                onQueryChange = { viewModel.updateSearchQuery(it) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (indexingState.isScanning || indexingState.isOcrRunning || indexingState.totalPending > 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.35f)
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = indexingState.statusMessage.ifBlank { "Preparing your screenshot library…" },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Text(
                                        text = "$indexedCount / $totalCount Indexed",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                                if (indexingState.isScanning || indexingState.isOcrRunning) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    val progress = if (indexingState.totalPending > 0) {
                                        indexingState.processedCount.toFloat() / indexingState.totalPending.toFloat()
                                    } else 0f

                                    LinearProgressIndicator(
                                        progress = progress,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }

                    FilterChipRow(
                        selectedFilter = dateFilter,
                        onFilterSelected = { viewModel.updateDateFilter(it) },
                        selectedSort = sortOption,
                        onSortSelected = { viewModel.updateSortOption(it) },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )

                    HomeTabContent(
                        selectedTab = selectedTab,
                        searchQuery = searchQuery,
                        searchResults = searchResults,
                        favoriteScreens = favoriteScreens,
                        totalCount = totalCount,
                        favoriteCount = favoriteCount,
                        onNavigateToViewer = onNavigateToViewer,
                        onToggleFavorite = { viewModel.toggleFavorite(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeTabContent(
    selectedTab: HomeTab,
    searchQuery: String,
    searchResults: List<com.example.screenshotmemory.data.db.ScreenshotEntity>,
    favoriteScreens: List<com.example.screenshotmemory.data.db.ScreenshotEntity>,
    totalCount: Int,
    favoriteCount: Int,
    onNavigateToViewer: (Long) -> Unit,
    onToggleFavorite: (com.example.screenshotmemory.data.db.ScreenshotEntity) -> Unit
) {
    when (selectedTab) {
        HomeTab.FAVORITES -> {
            if (favoriteScreens.isEmpty()) {
                EmptyStatePlaceholder(
                    title = "No favorites yet",
                    subtitle = "Tap the heart icon on a screenshot to keep it in your premium favorites."
                )
            } else {
                ScreenshotSectionGrid(
                    items = favoriteScreens,
                    onNavigateToViewer = onNavigateToViewer,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
        HomeTab.COLLECTIONS -> {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp),
                modifier = Modifier
                    .fillMaxSize()
            ) {
                items(
                    listOf(
                        CollectionCardModel("Receipts & bills", "Review all payment captures and invoices."),
                        CollectionCardModel("Travel memories", "Trip plans, tickets and adventure snaps."),
                        CollectionCardModel("Quick notes", "Screenshots with text, ideas and reminders."),
                        CollectionCardModel("Saved visuals", "Important visuals kept for later inspiration.")
                    )
                ) { card ->
                    CollectionCard(card)
                }
            }
        }
        HomeTab.RECENT -> {
            if (searchResults.isEmpty()) {
                EmptyStatePlaceholder(
                    title = if (searchQuery.isNotBlank()) "No results found" else "No screenshots found",
                    subtitle = if (searchQuery.isNotBlank()) "Try a different search term or scan again." else "Please grant permission and scan your library to load screenshots."
                )
            } else {
                ScreenshotSectionGrid(
                    items = searchResults,
                    onNavigateToViewer = onNavigateToViewer,
                    onToggleFavorite = onToggleFavorite
                )
            }
        }
    }
}

@Composable
private fun ScreenshotSectionGrid(
    items: List<com.example.screenshotmemory.data.db.ScreenshotEntity>,
    onNavigateToViewer: (Long) -> Unit,
    onToggleFavorite: (com.example.screenshotmemory.data.db.ScreenshotEntity) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 24.dp),
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(
            items = items,
            key = { it.id }
        ) { screenshot ->
            ScreenshotGridItem(
                item = screenshot,
                onClick = { onNavigateToViewer(screenshot.id) },
                onToggleFavorite = { onToggleFavorite(screenshot) }
            )
        }
    }
}

private data class CollectionCardModel(
    val title: String,
    val description: String
)

@Composable
private fun CollectionCard(card: CollectionCardModel) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = card.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "View collection",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun EmptyStatePlaceholder(
    title: String,
    subtitle: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ImageSearch,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(64.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
