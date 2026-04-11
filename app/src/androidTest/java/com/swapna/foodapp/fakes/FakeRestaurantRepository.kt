package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.Review
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.repository.RestaurantRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class FakeRestaurantRepository @Inject constructor() : RestaurantRepository {

    override fun getNearbyRestaurants(): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCollections(): Flow<Result<List<Collections>>> =
        flowOf(Result.success(emptyList()))

    override fun getCategories(): Flow<Result<List<FoodCategory>>> =
        flowOf(Result.success(emptyList()))

    override fun getRestaurantDetail(id: String): Flow<Result<Restaurant>> =
        flowOf(Result.failure(Exception("Fake: No data")))

    override fun getMenuItems(
        restaurantId: String,
    ): Flow<Result<Map<String, List<MenuItem>>>> =
        flowOf(Result.success(emptyMap()))

    override fun getReviews(restaurantId: String): Flow<Result<List<Review>>> =
        flowOf(Result.success(emptyList()))

    override fun searchRestaurants(
        query: String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCuisines(): Flow<Result<List<Cuisine>>> =
        flowOf(Result.success(emptyList()))
}