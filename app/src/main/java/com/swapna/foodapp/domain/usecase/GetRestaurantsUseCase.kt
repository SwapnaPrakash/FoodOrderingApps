package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.repository.RestaurantRepository
import javax.inject.Inject

class GetRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantRepository
) {
    suspend operator fun invoke(): List<Restaurant> {
        return repository.getRestaurants()
    }
}