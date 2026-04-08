package com.swapna.foodapp.domain.usecase.home

import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.utils.HomeData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetHomeDataUseCase @Inject constructor(
    private val repository: RestaurantRepository,
) {
    operator fun invoke(): Flow<Result<HomeData>> =
        combine(
            repository.getCollections(),
            repository.getCategories(),
            repository.getNearbyRestaurants(),
        ) { collectionsResult, categoriesResult, restaurantsResult ->
            Result.success(
                HomeData(
                    collections  = collectionsResult.getOrDefault(emptyList()),
                    categories   = categoriesResult.getOrDefault(emptyList()),
                    restaurants  = restaurantsResult.getOrDefault(emptyList()),
                )
            )
        }
}