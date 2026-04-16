package com.swapna.foodapp.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.presentation.common.ErrorScreen
import com.swapna.foodapp.presentation.home.components.CategoryChip
import com.swapna.foodapp.presentation.home.components.HomeShimmer
import com.swapna.foodapp.presentation.home.components.HomeTopBar
import com.swapna.foodapp.presentation.home.components.LocationPickerSheet
import com.swapna.foodapp.presentation.home.components.OfferCard
import com.swapna.foodapp.presentation.home.components.OfflineBanner
import com.swapna.foodapp.presentation.home.components.RestaurantCard
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.CATEGORIES_TITLE
import com.swapna.foodapp.utils.AppConstants.DELIVERY
import com.swapna.foodapp.utils.AppConstants.DINING
import com.swapna.foodapp.utils.AppConstants.OFFERS_TITLE
import com.swapna.foodapp.utils.AppConstants.PROFILE
import com.swapna.foodapp.utils.AppConstants.STORE_NEAR
import com.swapna.foodapp.utils.SectionTitle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)



    // Navigation events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeViewModel.HomeEvent.NavigateToRestaurant -> {
                    Log.d("CLICK", "Navigating to: ${event.id}")
                    Log.d(
                        "RESTAURANT_CLICK",
                        "Navigating to id=${event.id}"
                    )
                    navController.navigate(
                        AppRoutes.restaurant(event.id) // "restaurant/101"
                    )
                }

                is HomeViewModel.HomeEvent.NavigateToSearch -> {
                    if (event.query.isNotEmpty()) {
                        navController.navigate(
                            AppRoutes.SEARCH + "?query=${event.query}"
                        )
                    } else {
                        navController.navigate(AppRoutes.SEARCH)
                    }
                }

                HomeViewModel.HomeEvent.NavigateToCart ->
                    navController.navigate(AppRoutes.CART)

                HomeViewModel.HomeEvent.NavigateToProfile ->
                    navController.navigate(AppRoutes.PROFILE)
            }
        }
    }

    // Location picker sheet
    if (state.showLocationPicker) {
        LocationPickerSheet(
            sheetState = bottomSheetState,
            savedAddresses = emptyList(), // populated from Profile on Day 26
            onLocationSelected = viewModel::onLocationSelected,
            onDismiss = viewModel::onLocationDismissed,
        )
    }


    Scaffold(
        topBar = {
            HomeTopBar(
                location = state.userLocation,
                cartItemCount = state.cartItemCount,
                onLocationClick = viewModel::onLocationClicked,
                onCartClick = viewModel::onCartClicked,
                onSearchClick = { viewModel.onSearchClicked() },
                onProfileClick  = { viewModel.onProfileClicked() },
                )
        },
        bottomBar = {
            HomeBottomBar(
                selectedTab = state.selectedTab,
                onTabSelect = viewModel::onTabSelected,
            )
        },
    ) { paddingValues ->

        // Offline banner + content stacked
        Column(modifier = Modifier.fillMaxSize()) {

            // Offline banner slides in from top
            OfflineBanner(isVisible = state.isOffline)

            // Main content area
            when {
                state.isLoading -> HomeShimmer(paddingValues)

                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = viewModel::retry,
                )

                else -> HomeContent(
                    state = state,
                    paddingValues = paddingValues,
                    onRestaurantClick = { restaurantId ->
                        viewModel.onRestaurantClicked(restaurantId)
                    },
                    onCategoryClick = { categoryName ->
                        viewModel.onCategoryClicked(categoryName)
                    },
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeViewModel.HomeUiState,
    paddingValues: PaddingValues,
    onRestaurantClick: (String) -> Unit,
    onCategoryClick: (String) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
    ) {

        // Exciting Offers
        if (state.collections.isNotEmpty()) {
            item(key = "offers_title") {
                SectionTitle(OFFERS_TITLE)
            }
            item(key = "offers_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceL),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                ) {
                    items(state.collections, key = { it.id }) { collection ->
                        OfferCard(collection = collection)
                    }
                }
            }
        }

        // Categories
        if (state.categories.isNotEmpty()) {
            item(key = "categories_title") {
                SectionTitle(CATEGORIES_TITLE)
            }
            item(key = "categories_row") {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceL),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                ) {
                    items(state.categories, key = { it.id }) { category ->
                        CategoryChip(
                            category = category,
                            onClick = { onCategoryClick(category.name) }, //viewModel.onCategoryClicked(category.name)
                        )
                    }
                }
            }
        }

        // Stores Near You
        item(key = "stores_title") {
            SectionTitle(STORE_NEAR)
        }

        items(state.restaurants, key = { it.id }) { restaurant ->
            val restaurantId = restaurant.id
            RestaurantCard(
                restaurant = restaurant,

                onClick = {
                    Log.d("CLICK", "Tapped: ${restaurant.id} - ${restaurant.name}")
                    onRestaurantClick(restaurantId) },


            )
        }
    }
}

// Bottom Nav Bar
@Composable
fun HomeBottomBar(
    selectedTab: HomeViewModel.DeliveryTab,
    onTabSelect: (HomeViewModel.DeliveryTab) -> Unit,
) {
    NavigationBar {
        NavigationBarItem(
            selected = selectedTab == HomeViewModel.DeliveryTab.DELIVERY,
            onClick = { onTabSelect(HomeViewModel.DeliveryTab.DELIVERY) },
            icon = { Icon(Icons.Default.DeliveryDining, DELIVERY) },
            label = { Text(DELIVERY) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = ZomatoRed.copy(alpha = 0.1f),
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
            ),
        )
        NavigationBarItem(
            selected = selectedTab == HomeViewModel.DeliveryTab.DINING,
            onClick = { onTabSelect(HomeViewModel.DeliveryTab.DINING) },
            icon = { Icon(Icons.Default.Restaurant, DINING) },
            label = { Text(DINING) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = ZomatoRed.copy(alpha = 0.1f),
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
            ),
        )
        NavigationBarItem(
            selected = selectedTab ==
                    HomeViewModel.DeliveryTab.PROFILE,
            onClick  = { onTabSelect(HomeViewModel.DeliveryTab.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = PROFILE,) },
            label  = { Text(PROFILE) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor    = ZomatoRed.copy(alpha = 0.1f),
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
            ),
        )
    }
}
