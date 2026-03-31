package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.Restaurant

interface RestaurantRepository {
    suspend fun getRestaurants(): List<Restaurant>
}