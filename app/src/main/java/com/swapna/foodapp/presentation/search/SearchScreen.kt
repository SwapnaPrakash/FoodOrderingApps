package com.swapna.foodapp.presentation.search

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.common.EmptySearchResult
import com.swapna.foodapp.presentation.common.FilterBottomSheet
import com.swapna.foodapp.presentation.common.FilterButton
import com.swapna.foodapp.presentation.common.FilterChipsRow
import com.swapna.foodapp.presentation.common.SearchResultItem
import com.swapna.foodapp.presentation.common.SearchTopBar
import com.swapna.foodapp.presentation.common.activeCount
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.COUNT
import com.swapna.foodapp.utils.AppConstants.EMOJI_SEARCH
import com.swapna.foodapp.utils.AppConstants.EMOJI_WARNING
import com.swapna.foodapp.utils.AppConstants.FILTER_ACTIVE
import com.swapna.foodapp.utils.AppConstants.RESTAURANT_FOUND
import com.swapna.foodapp.utils.AppConstants.RESTAURANT_SEARCH
import com.swapna.foodapp.utils.AppConstants.RESULTS

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: SearchViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val filterSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) viewModel.onQueryChange(initialQuery)
    }

    if (showFilterSheet) {
        FilterBottomSheet(
            sheetState = filterSheetState,
            filters = state.filters,
            cuisines = state.cuisines,
            onVegToggle = viewModel::onVegToggle,
            onSortChange = viewModel::onSortChange,
            onCuisineSelected = viewModel::onCuisineSelected,
            onMinRatingSelected = viewModel::onMinRatingSelected,
            onDeliveryTimeSelected = viewModel::onDeliveryTimeSelected,
            onClearAll = viewModel::clearFilters,
            onApply = { },
            onDismiss = { showFilterSheet = false },
        )
    }

    Scaffold(
        topBar = {
            SearchTopBar(
                query = state.query,
                onQueryChange = viewModel::onQueryChange,
                onClear = viewModel::clearSearch,
                onBack = { navController.popBackStack() },
            )
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {

            if (state.isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = ZomatoRed,
                )
            } else {
                HorizontalDivider()
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                FilterChipsRow(
                    filters = state.filters,
                    cuisines = state.cuisines.take(4),
                    onVegToggle = viewModel::onVegToggle,
                    onSortChange = viewModel::onSortChange,
                    onCuisineSelected = viewModel::onCuisineSelected,
                    onMinRatingSelected = viewModel::onMinRatingSelected,
                    modifier = Modifier.weight(1f),
                )
                FilterButton(
                    filters = state.filters,
                    onClick = { showFilterSheet = true },
                    modifier = Modifier.padding(end = Dimens.SpaceS),
                )
            }

            HorizontalDivider()

            if (state.filters.activeCount() > 0 && state.hasSearched) {
                Text(
                    text = "${state.filters.activeCount()} $FILTER_ACTIVE${state.results.size} $RESULTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = AppGray,
                    modifier = Modifier.padding(
                        horizontal = Dimens.SpaceL,
                        vertical = Dimens.SpaceXS,
                    ),
                )
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.error != null && !state.isLoading -> {
                        Text(
                            text = "$EMOJI_WARNING${state.error}",
                            color = AppGray,
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(Dimens.SpaceXXL),
                        )
                    }

                    state.hasSearched && state.results.isEmpty() && !state.isLoading -> {
                        EmptySearchResult(query = state.query)
                    }

                    !state.hasSearched && state.query.isEmpty() -> {
                        IdleSearchHint(modifier = Modifier.align(Alignment.Center))
                    }

                    state.results.isNotEmpty() -> {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item(key = COUNT) {
                                Text(
                                    text = "${state.results.size} $RESTAURANT_FOUND",
                                    color = AppGray,
                                    style = MaterialTheme.typography.labelMedium,
                                    modifier = Modifier.padding(
                                        horizontal = Dimens.SpaceL,
                                        vertical = Dimens.SpaceS,
                                    ),
                                )
                            }
                            items(state.results, key = { it.id }) { restaurant ->
                                SearchResultItem(
                                    restaurant = restaurant,
                                    onClick = {
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

@Composable
private fun IdleSearchHint(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(Dimens.SpaceXXL),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Text(
            text = EMOJI_SEARCH,
            style = MaterialTheme.typography.displayMedium,
        )
        androidx.compose.foundation.layout.Spacer(Modifier.padding(Dimens.SpaceM))
        Text(
            text = RESTAURANT_SEARCH,
            style = MaterialTheme.typography.bodyLarge,
            color = AppGray,
            textAlign = TextAlign.Center,
        )
    }
}