package com.swapna.foodapp.domain.usecase.search

import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class SearchRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(
        query: String,
        filters: SearchFilters = SearchFilters(),
    ): Flow<Result<List<Restaurant>>> =
        repository.searchRestaurants(query, filters)
            .map { result ->
                result.map { list ->
                    list
                        .applyTextFilter(query)
                        .applyRatingFilter(filters.minRating)
                        .applyDeliveryTimeFilter(filters.maxDeliveryTime)
                        .applySort(filters.sortBy)
                }
            }

    // ── Private filter extensions ─────────────────────────────
    // Each filter is a pure function on List<Restaurant>
    // Easy to test individually — no side effects

    private fun List<Restaurant>.applyTextFilter(query: String): List<Restaurant> {
        if (query.isBlank()) return this
        return filter { restaurant ->
            restaurant.name.contains(query, ignoreCase = true) ||
                    restaurant.cuisines.any { cuisine ->
                        cuisine.contains(query, ignoreCase = true)
                    }
        }
    }

    private fun List<Restaurant>.applyRatingFilter(
        minRating: Double?,
    ): List<Restaurant> {
        minRating ?: return this  // null = no filter
        return filter { it.rating >= minRating }
    }

    private fun List<Restaurant>.applyDeliveryTimeFilter(
        maxDeliveryTime: Int?,
    ): List<Restaurant> {
        maxDeliveryTime ?: return this  // null = no filter
        return filter { it.avgDeliveryTime <= maxDeliveryTime }
    }

    private fun List<Restaurant>.applySort(sort: SortOption): List<Restaurant> =
        when (sort) {
            SortOption.RATING        -> sortedByDescending { it.rating }
            SortOption.DELIVERY_TIME -> sortedBy { it.avgDeliveryTime }
            SortOption.COST_LOW      -> sortedBy { it.avgCostForTwo }
            SortOption.COST_HIGH     -> sortedByDescending { it.avgCostForTwo }
            SortOption.RELEVANCE     -> this  // no change — original order
        }
}