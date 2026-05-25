package com.swapna.foodapp.domain.usecase.home

import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.utils.AppConstants.CURRENT_LOCATION
import com.swapna.foodapp.utils.AppConstants.SELECT_LOCATION
import com.swapna.foodapp.utils.HomeData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(
        selectedLocation: String = ""
    ): Flow<Result<HomeData>> =
        combine(
            repository.getRestaurantCollection(),
            repository.getCategories(),
            repository.getNearbyRestaurants(),
        ) { collectionsResult, categoriesResult, restaurantsResult ->

            val allRestaurants = restaurantsResult.getOrElse {
                return@combine Result.failure(it)
            }

            val filterResult = buildFilterResult(
                allRestaurants = allRestaurants,
                selectedLocation = selectedLocation,
            )

            Result.success(
                HomeData(
                    collections = collectionsResult
                        .getOrDefault(emptyList()),
                    categories = categoriesResult
                        .getOrDefault(emptyList()),
                    restaurants = filterResult.restaurants,
                    filterStatus = filterResult.status,
                    requestedArea = filterResult.requestedArea,
                    availableAreas = filterResult.availableAreas,
                )
            )
        }

    private fun buildFilterResult(
        allRestaurants: List<Restaurant>,
        selectedLocation: String,
    ): FilterResult {

        if (
            selectedLocation.isBlank() ||
            selectedLocation == SELECT_LOCATION ||
            selectedLocation == CURRENT_LOCATION
        ) {
            return FilterResult(
                restaurants = allRestaurants,
                status = FilterStatus.NO_FILTER,
                requestedArea = selectedLocation,
                availableAreas = emptyList(),
            )
        }

        val locationKey = selectedLocation
            .split(",")
            .first()
            .trim()

        val matched = allRestaurants.filter { restaurant ->
            matchesLocation(restaurant, locationKey)
        }

        return if (matched.isNotEmpty()) {
            FilterResult(
                restaurants = matched,
                status = FilterStatus.FOUND,
                requestedArea = locationKey,
                availableAreas = emptyList(),
            )
        } else {
            FilterResult(
                restaurants = emptyList(),
                status = FilterStatus.NOT_SERVICEABLE,
                requestedArea = locationKey,
                availableAreas = allRestaurants
                    .map { it.locality }
                    .distinct()
                    .sorted(),
            )
        }
    }

    private fun matchesLocation(
        restaurant: Restaurant,
        locationKey: String,
    ): Boolean {
        val locality = restaurant.locality.trim()
        val address = restaurant.address.trim()
        val key = locationKey.trim()

        return when {
            locality.equals(key, ignoreCase = true) ->
                true

            locality.contains(key, ignoreCase = true) ->
                true

            key.contains(locality, ignoreCase = true) ->
                true

            address.contains(key, ignoreCase = true) ->
                true

            else -> false
        }
    }
}

data class FilterResult(
    val restaurants: List<Restaurant>,
    val status: FilterStatus,
    val requestedArea: String,
    val availableAreas: List<String>,
)

enum class FilterStatus {
    NO_FILTER,
    FOUND,
    NOT_SERVICEABLE,
}