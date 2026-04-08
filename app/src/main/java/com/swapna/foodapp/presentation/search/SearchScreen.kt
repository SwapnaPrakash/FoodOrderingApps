package com.swapna.foodapp.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.search.components.EmptySearchResult
import com.swapna.foodapp.presentation.search.components.FilterChipsRow
import com.swapna.foodapp.presentation.search.components.SearchResultItem
import com.swapna.foodapp.presentation.search.components.SearchTopBar
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.swapna.foodapp.presentation.search.components.FilterBottomSheet
import com.swapna.foodapp.presentation.search.components.FilterButton
import com.swapna.foodapp.presentation.search.components.activeCount

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state           by viewModel.uiState.collectAsStateWithLifecycle()
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet  by remember { mutableStateOf(false) }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            viewModel.onQueryChange(initialQuery)
        }
    }

    // ── Filter bottom sheet ───────────────────────────────────
    if (showFilterSheet) {
        FilterBottomSheet(
            sheetState             = filterSheetState,
            filters                = state.filters,
            cuisines               = state.cuisines,
            onVegToggle            = viewModel::onVegToggle,
            onSortChange           = viewModel::onSortChange,
            onCuisineSelected      = viewModel::onCuisineSelected,
            onMinRatingSelected    = viewModel::onMinRatingSelected,
            onDeliveryTimeSelected = viewModel::onDeliveryTimeSelected,
            onClearAll             = viewModel::clearFilters,
            onApply                = { /* filters already reactive */ },
            onDismiss              = { showFilterSheet = false },
        )
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                query         = state.query,
                onQueryChange = viewModel::onQueryChange,
                onClear       = viewModel::clearSearch,
                onBack        = { navController.popBackStack() },
            )
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {

            // ── Loading indicator ─────────────────────────────
            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color    = ZomatoRed,
                )
            } else {
                HorizontalDivider()
            }

            // ── Filter row: chips + filter button ─────────────
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                // Quick filter chips — scrollable horizontally
                FilterChipsRow(
                    filters             = state.filters,
                    cuisines            = state.cuisines.take(4),
                    onVegToggle         = viewModel::onVegToggle,
                    onSortChange        = viewModel::onSortChange,
                    onCuisineSelected   = viewModel::onCuisineSelected,
                    onMinRatingSelected = viewModel::onMinRatingSelected,
                    modifier            = Modifier.weight(1f),
                )

                // Filter button with active count badge
                FilterButton(
                    filters  = state.filters,
                    onClick  = { showFilterSheet = true },
                    modifier = Modifier.padding(end = Dimens.SpaceS),
                )
            }

            HorizontalDivider()

            // ── Active filter summary ─────────────────────────
            if (state.filters.activeCount() > 0 && state.hasSearched) {
                Text(
                    text     = "${state.filters.activeCount()} filter(s) active  •  " +
                            "${state.results.size} results",
                    style    = MaterialTheme.typography.labelSmall,
                    color    = AppGray,
                    modifier = Modifier.padding(
                        horizontal = Dimens.SpaceL,
                        vertical   = Dimens.SpaceXS,
                    ),
                )
            }

            // ── Results / States ──────────────────────────────
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    // Error state
                    state.error != null && !state.isLoading -> {
                        Text(
                            text     = "⚠️  ${state.error}",
                            color    = AppGray,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(Dimens.SpaceXXL),
                        )
                    }

                    // No results after search
                    state.hasSearched
                            && state.results.isEmpty()
                            && !state.isLoading -> {
                        EmptySearchResult(query = state.query)
                    }

                    // Idle — no query entered yet
                    !state.hasSearched && state.query.isEmpty() -> {
                        IdleSearchHint(
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }

                    // Results list
                    state.results.isNotEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {

                            // Result count
                            item(key = "count") {
                                Text(
                                    text     = "${state.results.size} restaurants found",
                                    color    = AppGray,
                                    style    = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(
                                        horizontal = Dimens.SpaceL,
                                        vertical   = Dimens.SpaceS,
                                    ),
                                )
                            }

                            items(state.results, key = { it.id }) { restaurant ->
                                SearchResultItem(
                                    restaurant = restaurant,
                                    onClick    = {
                                        navController.navigate(
                                            AppRoutes.restaurant(restaurant.id)
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Idle hint ──────────────────────────────────────────────────
@Composable
private fun IdleSearchHint(modifier: Modifier = Modifier) {
    Column(
        modifier            = modifier.padding(Dimens.SpaceXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text("🔍", style = MaterialTheme.typography.displayMedium)
        androidx.compose.foundation.layout.Spacer(
            Modifier.padding(Dimens.SpaceM)
        )
        Text(
            text      = "Search for restaurants,\ncuisines or dishes",
            style     = MaterialTheme.typography.bodyLarge,
            color     = AppGray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
    }
}