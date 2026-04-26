package com.swapna.foodapp.presentation.home

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import com.swapna.foodapp.presentation.common.NotServiceableSection
import com.swapna.foodapp.presentation.common.SectionTitle
import com.swapna.foodapp.presentation.common.CategoryChip
import com.swapna.foodapp.presentation.common.HomeShimmer
import com.swapna.foodapp.presentation.common.HomeTopBar
import com.swapna.foodapp.presentation.common.LocationPickerSheet
import com.swapna.foodapp.presentation.common.OfferCard
import com.swapna.foodapp.presentation.common.OfflineBanner
import com.swapna.foodapp.presentation.common.RestaurantCard
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.presentation.ui.theme.AppGray
import com.swapna.foodapp.presentation.ui.theme.Dimens
import com.swapna.foodapp.presentation.ui.theme.ZomatoRed
import com.swapna.foodapp.utils.AppConstants.ALPHA_NAV_INDICATOR
import com.swapna.foodapp.utils.AppConstants.CATEGORIES_TITLE
import com.swapna.foodapp.utils.AppConstants.DELIVERY
import com.swapna.foodapp.utils.AppConstants.DINING
import com.swapna.foodapp.utils.AppConstants.KEY_CATEGORIES_ROW
import com.swapna.foodapp.utils.AppConstants.KEY_CATEGORIES_TITLE
import com.swapna.foodapp.utils.AppConstants.KEY_NOT_SERVICEABLE
import com.swapna.foodapp.utils.AppConstants.KEY_NO_RESTAURANTS
import com.swapna.foodapp.utils.AppConstants.KEY_OFFERS_ROW
import com.swapna.foodapp.utils.AppConstants.KEY_OFFERS_TITLE
import com.swapna.foodapp.utils.AppConstants.KEY_STORES_TITLE
import com.swapna.foodapp.utils.AppConstants.NO_RESTAURANTS_FOUND
import com.swapna.foodapp.utils.AppConstants.OFFERS_TITLE
import com.swapna.foodapp.utils.AppConstants.PROFILE
import com.swapna.foodapp.utils.AppConstants.RESTAURANTS_NEAR_YOU

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val bottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) viewModel.onUseCurrentLocationTapped()
        else viewModel.onLocationPermissionDenied()
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeViewModel.HomeEvent.NavigateToRestaurant ->
                    navController.navigate(AppRoutes.restaurant(event.id))

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
            savedAddresses = state.savedAddresses,
            onLocationSelected = viewModel::onLocationSelected,
            onDismiss = viewModel::onLocationDismissed,
            locationFetchState = state.locationFetchState,
            locationErrorMsg = state.locationErrorMsg,
            onUseCurrentLocation = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    )
                )
            },
        )
    }

    Scaffold(
        topBar = {
            HomeTopBar(
                location = state.selectedLocation,
                cartItemCount = state.cartItemCount,
                onLocationClick = viewModel::onLocationBarClicked,
                onCartClick = viewModel::onCartClicked,
                onSearchClick = { viewModel.onSearchClicked() },
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
                state.isLoading -> HomeShimmer(paddingValues)
                state.error != null -> ErrorScreen(
                    message = state.error!!,
                    onRetry = viewModel::retry,
                )

                else -> HomeContent(
                    state = state,
                    paddingValues = paddingValues,
                    onRestaurantClick = viewModel::onRestaurantClicked,
                    onCategoryClick = viewModel::onCategoryClicked,
                    onChangeLocation = viewModel::onLocationBarClicked,
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
    onChangeLocation: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingValues,
    ) {

        if (state.collections.isNotEmpty()) {
            item(key = KEY_OFFERS_TITLE) {
                SectionTitle(OFFERS_TITLE)
            }
            item(key = KEY_OFFERS_ROW) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceL),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceM),
                ) {
                    items(items = state.collections, key = { it.id }) { collection ->
                        OfferCard(collection = collection)
                    }
                }
            }
        }

        if (state.categories.isNotEmpty()) {
            item(key = KEY_CATEGORIES_TITLE) {
                SectionTitle(CATEGORIES_TITLE)
            }
            item(key = KEY_CATEGORIES_ROW) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = Dimens.SpaceL),
                    horizontalArrangement = Arrangement.spacedBy(Dimens.SpaceS),
                ) {
                    items(items = state.categories, key = { it.id }) { category ->
                        CategoryChip(
                            category = category,
                            onClick = { onCategoryClick(category.name) },
                        )
                    }
                }
            }
        }

        when (state.filterStatus) {
            FilterStatus.NOT_SERVICEABLE -> {
                item(key = KEY_NOT_SERVICEABLE) {
                    NotServiceableSection(
                        requestedArea = state.requestedArea,
                        availableAreas = state.availableAreas,
                        onChangeLocation = onChangeLocation,
                    )
                }
            }

            else -> {
                item(key = KEY_STORES_TITLE) {
                    SectionTitle(RESTAURANTS_NEAR_YOU)
                }
                if (state.restaurants.isEmpty()) {
                    item(key = KEY_NO_RESTAURANTS) {
                        EmptyRestaurantsCard()
                    }
                } else {
                    items(items = state.restaurants, key = { it.id }) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onRestaurantClick(restaurant.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyRestaurantsCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Dimens.SpaceXXL),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = NO_RESTAURANTS_FOUND,
            style = MaterialTheme.typography.bodyLarge,
            color = AppGray,
            textAlign = TextAlign.Center,
        )
    }
}

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
                indicatorColor = ZomatoRed.copy(alpha = ALPHA_NAV_INDICATOR),
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
                indicatorColor = ZomatoRed.copy(alpha = ALPHA_NAV_INDICATOR),
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
            ),
        )
        NavigationBarItem(
            selected = selectedTab == HomeViewModel.DeliveryTab.PROFILE,
            onClick = { onTabSelect(HomeViewModel.DeliveryTab.PROFILE) },
            icon = { Icon(Icons.Default.Person, contentDescription = PROFILE) },
            label = { Text(PROFILE) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = ZomatoRed.copy(alpha = ALPHA_NAV_INDICATOR),
                selectedIconColor = ZomatoRed,
                selectedTextColor = ZomatoRed,
            ),
        )
    }
}