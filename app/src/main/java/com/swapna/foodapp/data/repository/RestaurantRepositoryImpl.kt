package com.swapna.foodapp.data.repository

import com.swapna.foodapp.data.mapper.toDomain
import com.swapna.foodapp.data.remote.api.RestaurantApi
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.repository.RestaurantRepository
import javax.inject.Inject

class RestaurantRepositoryImpl @Inject constructor(
    private val api: RestaurantApi
) : RestaurantRepository {

    override suspend fun getRestaurants(): List<Restaurant> {
        return api.getRestaurants().map { it.toDomain() }
    }
}