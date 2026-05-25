package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.RestaurantCollection
import com.swapna.foodapp.domain.model.Review
import com.swapna.foodapp.domain.model.SearchFilters
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {

    fun getNearbyRestaurants(): Flow<Result<List<Restaurant>>>
    fun getRestaurantCollection(): Flow<Result<List<RestaurantCollection>>>
    fun getCategories(): Flow<Result<List<FoodCategory>>>
    fun getRestaurantDetail(id: String): Flow<Result<Restaurant>>
    fun getMenuItems(restaurantId: String): Flow<Result<Map<String, List<MenuItem>>>>
    fun getReviews(restaurantId: String): Flow<Result<List<Review>>>
    fun searchRestaurants(
        query: String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>>
    fun getCuisines(): Flow<Result<List<Cuisine>>>

}