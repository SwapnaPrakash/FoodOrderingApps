package com.swapna.foodapp.presentation.restaurant

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.domain.model.MenuCategory
import com.swapna.foodapp.presentation.common.ErrorScreen
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.common.CartBottomBar
import com.swapna.foodapp.presentation.common.CategorySectionHeader
import com.swapna.foodapp.presentation.common.MenuItemRow
import com.swapna.foodapp.presentation.common.MenuTabRow
import com.swapna.foodapp.presentation.common.RecommendedSection
import com.swapna.foodapp.presentation.common.RestaurantInfoHeader
import com.swapna.foodapp.presentation.common.RestaurantShimmer
import com.swapna.foodapp.utils.AppConstants.KEY_RECOMMENDED
import com.swapna.foodapp.utils.AppConstants.KEY_RESTAURANT_HEADER
import com.swapna.foodapp.utils.AppConstants.MENU_TAB_KEY
import com.swapna.foodapp.utils.AppConstants.MSG_ITEM_ADDED_CART

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RestaurantScreen(
    navController: NavController,
    viewModel: RestaurantViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val quantities by viewModel.quantities.collectAsStateWithLifecycle()
    val breakdown by viewModel.cartBreakdown.collectAsStateWithLifecycle()

    val restaurantId = viewModel.restaurantId
    val listState = rememberLazyListState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RestaurantViewModel.RestaurantEvent.NavigateToProduct ->
                    navController.navigate(
                        AppRoutes.product(
                            restaurantId = restaurantId,
                            menuItemId = event.itemId,
                        )
                    )

                is RestaurantViewModel.RestaurantEvent.ItemAdded ->
                    snackbarHost.showSnackbar("${event.itemName}$MSG_ITEM_ADDED_CART")

                is RestaurantViewModel.RestaurantEvent.ShowError ->
                    snackbarHost.showSnackbar(event.message)

                RestaurantViewModel.RestaurantEvent.NavigateToCart ->
                    navController.navigate(AppRoutes.CART)

                RestaurantViewModel.RestaurantEvent.NavigateBack ->
                    navController.popBackStack()
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.scrollToCategory.collect { category ->
            val categories = viewModel.getCategoryNames()
            val idx = categories.indexOf(category)
            if (idx >= 0) listState.animateScrollToItem(idx + 3)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },
        bottomBar = {
            CartBottomBar(
                itemCount = state.cartItemCount,
                breakdown = breakdown,
                onViewCart = viewModel::onCartBarTapped,
            )
        },
    ) { paddingValues ->

        when {
            state.isLoading -> RestaurantShimmer()

            state.error != null && state.restaurant == null -> {
                ErrorScreen(
                    message = state.error!!,
                    onRetry = viewModel::retry,
                )
            }

            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {
                    LazyColumn(state = listState) {

                        state.restaurant?.let { restaurant ->
                            item(key = KEY_RESTAURANT_HEADER) {
                                RestaurantInfoHeader(
                                    restaurant = restaurant,
                                    cartItemCount = state.cartItemCount,
                                    onBack = viewModel::onBackPressed,
                                    onShare = { },
                                    onSearch = { },
                                    onCart = viewModel::onCartBarTapped,
                                )
                            }
                        }

                        if (state.recommended.isNotEmpty()) {
                            item(key = KEY_RECOMMENDED) {
                                RecommendedSection(
                                    items = state.recommended,
                                    onItemTap = { itemId -> viewModel.onMenuItemTapped(itemId) },
                                    onAddTap = { item -> viewModel.quickAddToCart(item) },
                                )
                            }
                        }

                        val menuCategories = state.menuByCategory
                            .entries
                            .mapIndexed { index, (name, items) ->
                                MenuCategory(id = index.toString(), name = name, items = items)
                            }

                        if (menuCategories.isNotEmpty()) {
                            stickyHeader(key = MENU_TAB_KEY) {
                                MenuTabRow(
                                    categories = menuCategories,
                                    selectedIndex = state.menuByCategory.keys.toList()
                                        .indexOf(state.menuByCategory.keys.firstOrNull())
                                        .coerceAtLeast(0),
                                    onTabSelected = { index ->
                                        val tabName = state.menuByCategory.keys.toList()
                                            .getOrNull(index) ?: return@MenuTabRow
                                        viewModel.onCategoryFooterTapped(tabName)
                                    },
                                    listState = listState,
                                )
                            }
                        }

                        state.menuByCategory.entries.forEachIndexed { catIndex, (category, items) ->
                            stickyHeader(key = "cat_header_$catIndex") {
                                CategorySectionHeader(
                                    categoryName = category,
                                    itemCount = items.size,
                                )
                            }

                            itemsIndexed(
                                items = items,
                                key = { _, item -> item.id },
                            ) { _, item ->
                                MenuItemRow(
                                    item = item,
                                    quantity = quantities[item.id] ?: 0,
                                    onIncrement = { viewModel.onIncrementItem(item) },
                                    onDecrement = { viewModel.onDecrementItem(item.id) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}