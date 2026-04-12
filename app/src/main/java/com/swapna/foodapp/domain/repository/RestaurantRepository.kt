package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.Review
import com.swapna.foodapp.domain.model.SearchFilters
import kotlinx.coroutines.flow.Flow

interface RestaurantRepository {

    // Home screen
    fun getNearbyRestaurants(): Flow<Result<List<Restaurant>>>
    fun getCollections(): Flow<Result<List<Collections>>>
    fun getCategories(): Flow<Result<List<FoodCategory>>>

    // Restaurant screen
    fun getRestaurantDetail(id: String): Flow<Result<Restaurant>>
    fun getMenuItems(restaurantId: String): Flow<Result<Map<String, List<MenuItem>>>>
    fun getReviews(restaurantId: String): Flow<Result<List<Review>>>

    // Search screen
    fun searchRestaurants(
        query: String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>>

    fun getCuisines(): Flow<Result<List<Cuisine>>>
}