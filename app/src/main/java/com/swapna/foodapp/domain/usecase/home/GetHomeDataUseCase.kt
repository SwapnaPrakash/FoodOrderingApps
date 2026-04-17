package com.swapna.foodapp.domain.usecase.home

import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.repository.RestaurantRepository
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
            repository.getCollections(),
            repository.getCategories(),
            repository.getNearbyRestaurants(),
        ) { collectionsResult, categoriesResult, restaurantsResult ->

            val allRestaurants = restaurantsResult
                .getOrDefault(emptyList())

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

    // ── Filter logic — works for ANY location ─────────────────
    private fun buildFilterResult(
        allRestaurants:   List<Restaurant>,
        selectedLocation: String,
    ): FilterResult {

        // Case 1 + 2: No location or generic GPS fallback
        // → show everything
        if (
            selectedLocation.isBlank()            ||
            selectedLocation == "Select Location" ||
            selectedLocation == "Current Location"
        ) {
            return FilterResult(
                restaurants    = allRestaurants,
                status         = FilterStatus.NO_FILTER,
                requestedArea  = selectedLocation,
                availableAreas = emptyList(),
            )
        }

        // Extract main locality keyword
        // "Koramangala, Bengaluru" → "Koramangala"
        // "HSR Layout"             → "HSR Layout"
        val locationKey = selectedLocation
            .split(",")
            .first()
            .trim()

        // Case 3: Match found — location IS in geocode.json
        // Case 4: No match — Jakkur, Yelahanka, Mumbai etc.
        val matched = allRestaurants.filter { restaurant ->
            matchesLocation(restaurant, locationKey)
        }

        return if (matched.isNotEmpty()) {
            // Case 3 — known serviceable location
            FilterResult(
                restaurants    = matched,
                status         = FilterStatus.FOUND,
                requestedArea  = locationKey,
                availableAreas = emptyList(),
            )
        } else {
            // Case 4 — ANY unknown/unserviceable location
            // Jakkur, Yelahanka, Hebbal, Mumbai, Delhi...
            // All get same NOT_SERVICEABLE treatment
            FilterResult(
                restaurants    = emptyList(),
                status         = FilterStatus.NOT_SERVICEABLE,
                requestedArea  = locationKey,
                // Show all available areas so user can pick one
                availableAreas = allRestaurants
                    .map { it.locality }
                    .distinct()
                    .sorted(),
            )
        }
    }

    // ── Location matching — handles GPS sub-locality issues ───
    private fun matchesLocation(
        restaurant:  Restaurant,
        locationKey: String,
    ): Boolean {
        val locality = restaurant.locality.trim()
        val address  = restaurant.address.trim()
        val key      = locationKey.trim()

        return when {
            // Exact match
            locality.equals(key, ignoreCase = true) ->
                true

            // Locality contains key
            // "Greater Koramangala".contains("Koramangala")
            locality.contains(key, ignoreCase = true) ->
                true

            // Key contains locality
            // "Koramangala 5th Block".contains("Koramangala")
            key.contains(locality, ignoreCase = true) ->
                true

            // Address contains key
            // "5th Block, Koramangala".contains("Koramangala")
            address.contains(key, ignoreCase = true) ->
                true

            // No match
            else -> false
        }
    }
}



// ── FilterResult ──────────────────────────────────────────────
data class FilterResult(
    val restaurants:    List<Restaurant>,
    val status:         FilterStatus,
    val requestedArea:  String,
    val availableAreas: List<String>,
)

enum class FilterStatus {
    NO_FILTER,          // No location selected → show all
    FOUND,              // Restaurants found for this location
    NOT_SERVICEABLE,    // Jakkur case — no delivery here yet
}