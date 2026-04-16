package com.swapna.foodapp.presentation.restaurant

import android.util.Log
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
import com.swapna.foodapp.presentation.restaurant.components.CartBottomBar
import com.swapna.foodapp.presentation.restaurant.components.CategorySectionHeader
import com.swapna.foodapp.presentation.restaurant.components.MenuItemRow
import com.swapna.foodapp.presentation.restaurant.components.MenuTabRow
import com.swapna.foodapp.presentation.restaurant.components.RecommendedSection
import com.swapna.foodapp.presentation.restaurant.components.RestaurantInfoHeader
import com.swapna.foodapp.presentation.restaurant.components.RestaurantShimmer

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun RestaurantScreen(
    navController: NavController,
    viewModel: RestaurantViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val quantities by viewModel.quantities.collectAsStateWithLifecycle()
    val breakdown by viewModel.cartBreakdown.collectAsStateWithLifecycle()

    Log.d("CLICK", "Test "+ {state.restaurant}.toString())
    val restaurantId = viewModel.restaurantId

    val listState = rememberLazyListState()
    val snackbarHost = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // ── One-time events ───────────────────────────────────────
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is RestaurantViewModel.RestaurantEvent.NavigateToProduct ->

                    navController.navigate(
                        AppRoutes.product(
                            restaurantId =  restaurantId, // from savedStateHandle
                            menuItemId   = event.itemId,
                        )
                    )

                is RestaurantViewModel.RestaurantEvent.ItemAdded ->
                    snackbarHost.showSnackbar(
                        "${event.itemName} added to cart 🛒"
                    )

                is RestaurantViewModel.RestaurantEvent.ShowError ->
                    snackbarHost.showSnackbar(event.message)

                RestaurantViewModel.RestaurantEvent.NavigateToCart ->
                    navController.navigate(AppRoutes.CART)

                RestaurantViewModel.RestaurantEvent.NavigateBack ->
                    navController.popBackStack()
            }
        }
    }

    // ── Scroll to category from footer tap ────────────────────
    LaunchedEffect(Unit) {
        viewModel.scrollToCategory.collect { category ->
            val categories = viewModel.getCategoryNames()
            val idx = categories.indexOf(category)
            if (idx >= 0) {
                listState.animateScrollToItem(idx + 3)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHost) },

        // ✅ CartBottomBar slides up when cart has items
        bottomBar = {
            CartBottomBar(
                itemCount = state.cartItemCount,
                breakdown = breakdown,
                onViewCart = viewModel::onCartBarTapped,
            )
        },
    ) { paddingValues ->

        when {
            // ── Loading ───────────────────────────────────────
            state.isLoading -> {
                RestaurantShimmer()
            }

            // ── Error ─────────────────────────────────────────
            state.error != null && state.restaurant == null -> {
                ErrorScreen(
                    message = state.error!!,
                    onRetry = viewModel::retry,
                )
            }

            // ── Content ───────────────────────────────────────
            else -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                ) {

                    LazyColumn(state = listState) {

                        // ── 1. Restaurant Info Header ─────────
                        state.restaurant?.let { restaurant ->
                            item(key = "restaurant_header") {
                                Log.d("CLICK", "Test "+ {restaurant}.toString())
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

                        // ── 2. Recommended Section ────────────
                        // ✅ FIX: RecommendedSection uses onItemTap + onAddTap
                        // NOT quantities/onIncrement/onDecrement
                        if (state.recommended.isNotEmpty()) {
                            item(key = "recommended") {
                                RecommendedSection(
                                    items = state.recommended,
                                    onItemTap = { itemId ->
                                        viewModel.onMenuItemTapped(itemId)
                                    },
                                    onAddTap = { item ->
                                        viewModel.quickAddToCart(item)
                                    },
                                )
                            }
                        }

                        // ── 3. Menu Tab Row (sticky) ──────────
                        // ✅ FIX: MenuTabRow takes List<MenuCategory>
                        // Build MenuCategory list from menuByCategory map
                        val menuCategories = state.menuByCategory
                            .entries
                            .mapIndexed { index, (name, items) ->
                                MenuCategory(
                                    id = index.toString(),
                                    name = name,
                                    items = items,
                                )
                            }

                        if (menuCategories.isNotEmpty()) {
                            stickyHeader(key = "tab_row") {
                                // ✅ FIX: MenuTabRow takes:
                                //   categories: List<MenuCategory>
                                //   selectedIndex: Int  (from selectedTab)
                                //   onTabSelected: (Int) -> Unit
                                //   listState: LazyListState
                                MenuTabRow(
                                    categories = menuCategories,
                                    selectedIndex = state.menuByCategory
                                        .keys.toList()
                                        .indexOf(
                                            state.menuByCategory
                                                .keys.firstOrNull()
                                        ).coerceAtLeast(0),
                                    onTabSelected = { index ->
                                        val tabName = state.menuByCategory
                                            .keys.toList()
                                            .getOrNull(index) ?: return@MenuTabRow
                                        viewModel.onCategoryFooterTapped(tabName)
                                    },
                                    listState = listState,
                                )
                            }
                        }

                        // ── 4. Menu with Sticky Headers ───────
                        // ✅ FIX: state.menuByCategory is Map<String, List<MenuItem>>
                        state.menuByCategory
                            .entries
                            .forEachIndexed { catIndex, (category, items) ->

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
                                    // ✅ FIX: MenuItemRow has NO onItemTapped param
                                    // Use existing signature:
                                    //   item, quantity, onIncrement, onDecrement
                                    MenuItemRow(
                                        item = item,
                                        quantity = quantities[item.id] ?: 0,
                                        onIncrement = {
                                            viewModel.onIncrementItem(item)
                                        },
                                        onDecrement = {
                                            viewModel.onDecrementItem(item.id)
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