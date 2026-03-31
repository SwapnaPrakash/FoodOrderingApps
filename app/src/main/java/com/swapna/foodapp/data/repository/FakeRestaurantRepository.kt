package com.swapna.foodapp.data.repository

import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.repository.RestaurantRepository

class FakeRestaurantRepository : RestaurantRepository {

    override suspend fun getRestaurants(): List<Restaurant> {
        return listOf(
            Restaurant("1", "Pizza Hut", "4.2"),
            Restaurant("2", "Dominos", "4.0")
        )
    }
}