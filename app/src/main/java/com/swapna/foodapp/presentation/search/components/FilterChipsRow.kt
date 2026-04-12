package com.swapna.foodapp.presentation.search.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.VegGreen
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.COST_HIGH
import com.swapna.foodapp.utils.AppConstants.COST_LOW
import com.swapna.foodapp.utils.AppConstants.DELIVERY_TIME
import com.swapna.foodapp.utils.AppConstants.RATING
import com.swapna.foodapp.utils.AppConstants.RELEVANCE
import com.swapna.foodapp.utils.AppConstants.VEG_ONLY

@Composable
fun FilterChipsRow(
    filters: SearchFilters,
    cuisines: List<Cuisine>,
    onVegToggle: () -> Unit,
    onSortChange: (SortOption) -> Unit,
    onCuisineSelected: (Int) -> Unit,
    onMinRatingSelected: (Double?) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = Dimens.SpaceL, vertical = Dimens.SpaceS),
        horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
        verticalAlignment = Alignment.CenterVertically,
    ) {

        // Veg Only Toggle
        FilterChip(
            selected = filters.isVegOnly,
            onClick = onVegToggle,
            label = { Text(VEG_ONLY) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Eco,
                    contentDescription = VEG_ONLY,
                    tint = if (filters.isVegOnly) VegGreen else VegGreen.copy(alpha = 0.5f),
                    modifier = Modifier.size(FilterChipDefaults.IconSize),
                )
            },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = VegGreen.copy(alpha = 0.15f),
                selectedLabelColor = VegGreen,
            ),
        )

        // Rating Filter
        listOf(4.5, 4.0, 3.5).forEach { rating ->
            FilterChip(
                selected = filters.minRating == rating,
                onClick = {
                    // Toggle: select if not selected, clear if already selected
                    onMinRatingSelected(
                        if (filters.minRating == rating) null else rating
                    )
                },
                label = { Text("${rating}+ ⭐") },
                leadingIcon = if (filters.minRating == rating) {
                    { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ZomatoRed.copy(alpha = 0.12f),
                    selectedLabelColor = ZomatoRed,
                ),
            )
        }

        // Sort Options
        SortOption.values().filter { it != SortOption.RELEVANCE }.forEach { sort ->
            FilterChip(
                selected = filters.sortBy == sort,
                onClick = {
                    // Toggle: switch to RELEVANCE if same sort selected
                    onSortChange(
                        if (filters.sortBy == sort) SortOption.RELEVANCE else sort
                    )
                },
                label = { Text(sort.displayLabel) },
                leadingIcon = if (filters.sortBy == sort) {
                    { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ZomatoRed.copy(alpha = 0.12f),
                    selectedLabelColor = ZomatoRed,
                ),
            )
        }

        // Cuisine Filter Chips
        // Show top 5 cuisines only — horizontal scroll handles overflow
        cuisines.take(5).forEach { cuisine ->
            FilterChip(
                selected = filters.cuisineId == cuisine.id,
                onClick = { onCuisineSelected(cuisine.id) },
                label = { Text(cuisine.name) },
                leadingIcon = if (filters.cuisineId == cuisine.id) {
                    { Icon(Icons.Default.Check, null, Modifier.size(FilterChipDefaults.IconSize)) }
                } else null,
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ZomatoRed.copy(alpha = 0.12f),
                    selectedLabelColor = ZomatoRed,
                ),
            )
        }
    }
}

// Extension — human-readable label for each sort option
val SortOption.displayLabel: String
    get() = when (this) {
        SortOption.RELEVANCE -> RELEVANCE
        SortOption.RATING -> RATING
        SortOption.DELIVERY_TIME -> DELIVERY_TIME
        SortOption.COST_LOW -> COST_LOW
        SortOption.COST_HIGH -> COST_HIGH
    }