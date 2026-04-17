package com.swapna.foodapp.presentation.home

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.presentation.common.ErrorScreen
import com.swapna.foodapp.presentation.common.NotServiceableCard
import com.swapna.foodapp.presentation.common.NotServiceableSection
import com.swapna.foodapp.presentation.home.components.CategoryChip
import com.swapna.foodapp.presentation.home.components.HomeShimmer
import com.swapna.foodapp.presentation.home.components.HomeTopBar
import com.swapna.foodapp.presentation.home.components.LocationPickerSheet
import com.swapna.foodapp.presentation.home.components.OfferCard
import com.swapna.foodapp.presentation.home.components.OfflineBanner
import com.swapna.foodapp.presentation.home.components.RestaurantCard
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.AppGray
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
    viewModel:     HomeViewModel = hiltViewModel(),
) {
    val state        by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeViewModel.HomeEvent.NavigateToRestaurant ->
                    navController.navigate(
                        AppRoutes.restaurant(event.id)
                    )
                is HomeViewModel.HomeEvent.NavigateToSearch -> {
                    if (event.query.isNotEmpty()) {
                        navController.navigate(
                            "${AppRoutes.SEARCH}?query=${event.query}"
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

    if (state.showLocationPicker) {
        LocationPickerSheet(
            savedAddresses          = state.savedAddresses,
            onLocationSelected      = viewModel::onLocationSelected,
            onDismiss               = viewModel::onLocationDismissed,
        )
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                location        = state.selectedLocation,
                cartItemCount   = state.cartItemCount,
                onLocationClick = viewModel::onLocationClicked,
                onCartClick     = viewModel::onCartClicked,
                onSearchClick   = { viewModel.onSearchClicked() },
            )
        },
        bottomBar = {
            HomeBottomBar(
                selectedTab = state.selectedTab,
                onTabSelect = viewModel::onTabSelected,
            )
        },
    ) { paddingValues ->

        Column(modifier = Modifier.fillMaxSize()) {

            OfflineBanner(isVisible = state.isOffline)

            when {
                // ── Loading ───────────────────────────────────
                state.isLoading ->
                    HomeShimmer(paddingValues)

                // ── Network error ─────────────────────────────
                state.error != null ->
                    ErrorScreen(
                        message = state.error!!,
                        onRetry = viewModel::retry,
                    )

                // ✅ Not serviceable location (Jakkur etc.)
                // Shows ABOVE content — not instead of everything
                // Offers + Categories still visible
                // Only restaurant section replaced with message
                else -> HomeContent(
                    state             = state,
                    paddingValues     = paddingValues,
                    onRestaurantClick = viewModel::onRestaurantClicked,
                    onCategoryClick   = viewModel::onCategoryClicked,
                    onChangeLocation  = viewModel::onLocationClicked,
                )
            }
        }
    }
}

@Composable
private fun HomeContent(
    state:             HomeViewModel.HomeUiState,
    paddingValues:     PaddingValues,
    onRestaurantClick: (String) -> Unit,
    onCategoryClick:   (String) -> Unit,
    onChangeLocation:  () -> Unit,
) {
    LazyColumn(
        modifier       = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
    ) {

        // ── Offers — always shown ─────────────────────────────
        if (state.collections.isNotEmpty()) {
            item(key = "offers_title") {
                SectionTitle("Exciting Offers 🔥")
            }
            item(key = "offers_row") {
                LazyRow(
                    contentPadding        = PaddingValues(
                        horizontal = Dimens.SpaceL,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimens.SpaceM,
                    ),
                ) {
                    items(
                        items = state.collections,
                        key   = { it.id },
                    ) { collection ->
                        OfferCard(collection = collection)
                    }
                }
            }
        }

        // ── Categories — always shown ─────────────────────────
        if (state.categories.isNotEmpty()) {
            item(key = "categories_title") {
                SectionTitle("What's on your mind?")
            }
            item(key = "categories_row") {
                LazyRow(
                    contentPadding        = PaddingValues(
                        horizontal = Dimens.SpaceL,
                    ),
                    horizontalArrangement = Arrangement.spacedBy(
                        Dimens.SpaceS,
                    ),
                ) {
                    items(
                        items = state.categories,
                        key   = { it.id },
                    ) { category ->
                        CategoryChip(
                            category = category,
                            onClick  = {
                                onCategoryClick(category.name)
                            },
                        )
                    }
                }
            }
        }

        // ── Restaurants section ───────────────────────────────

        when (state.filterStatus) {

            // ── Not serviceable (Jakkur, Yelahanka etc.) ─────
            FilterStatus.NOT_SERVICEABLE -> {
                item(key = "not_serviceable") {
                    NotServiceableSection(
                        requestedArea    = state.requestedArea,
                        availableAreas   = state.availableAreas,
                        onChangeLocation = onChangeLocation,
                    )
                }
            }

            // ── Found or No filter → show restaurants ────────
            else -> {
                item(key = "stores_title") {
                    SectionTitle("Restaurants Near You")
                }

                if (state.restaurants.isEmpty()) {
                    item(key = "no_restaurants") {
                        EmptyRestaurantsCard()
                    }
                } else {
                    items(
                        items = state.restaurants,
                        key   = { it.id },
                    ) { restaurant ->
                        val restaurantId = restaurant.id
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick    = {
                                onRestaurantClick(restaurantId)
                            },
                        )
                    }
                }
            }
        }
    }
}

// ── Empty restaurants card ────────────────────────────────────
// Shows when filter returns empty but not NOT_SERVICEABLE
@Composable
private fun EmptyRestaurantsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceXXL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "No restaurants found\nfor this area",
            style = MaterialTheme.typography.bodyLarge,
            color = AppGray,
            textAlign = TextAlign.Center,
        )
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
