package com.example.screenshotmemory.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

import com.example.screenshotmemory.data.repository.DateFilter
import com.example.screenshotmemory.data.repository.SortOption

@Composable
fun FilterChipRow(
    selectedFilter: DateFilter,
    onFilterSelected: (DateFilter) -> Unit,
    selectedSort: SortOption,
    onSortSelected: (SortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DateFilter.entries.forEach { filter ->
                val label = when (filter) {
                    DateFilter.ALL -> "All"
                    DateFilter.TODAY -> "Today"
                    DateFilter.YESTERDAY -> "Yesterday"
                    DateFilter.LAST_7_DAYS -> "Last 7 Days"
                    DateFilter.LAST_30_DAYS -> "Last 30 Days"
                    DateFilter.THIS_YEAR -> "This Year"
                }

                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(label) },
                    modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                        selectedLabelColor = MaterialTheme.colorScheme.primary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }

        Box {
            IconButton(
                onClick = { showSortMenu = true },
                modifier = Modifier.testTag("sort_options_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort Options",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                SortOption.entries.forEach { sort ->
                    val sortName = when (sort) {
                        SortOption.RELEVANCE -> "Most Relevant"
                        SortOption.NEWEST -> "Newest First"
                        SortOption.OLDEST -> "Oldest First"
                    }

                    DropdownMenuItem(
                        text = { Text(sortName) },
                        onClick = {
                            onSortSelected(sort)
                            showSortMenu = false
                        },
                        trailingIcon = {
                            if (selectedSort == sort) {
                                Text("✓", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    )
                }
            }
        }
    }
}
