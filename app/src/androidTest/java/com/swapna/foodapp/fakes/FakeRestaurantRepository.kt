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

class FakeRestaurantRepository : RestaurantRepository {

    override fun getNearbyRestaurants(): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCollections(): Flow<Result<List<Collections>>> =
        flowOf(Result.success(emptyList()))

    override fun getCategories(): Flow<Result<List<FoodCategory>>> =
        flowOf(Result.success(emptyList()))

    override fun getRestaurantDetail(id: String): Flow<Result<Restaurant>> =
        flowOf(Result.success(fakeRestaurant()))

    override fun getMenuItems(
        restaurantId: String
    ): Flow<Result<Map<String, List<MenuItem>>>> =
        flowOf(Result.success(emptyMap()))

    override fun getReviews(restaurantId: String): Flow<Result<List<Review>>> =
        flowOf(Result.success(emptyList()))

    override fun searchRestaurants(
        query: String,
        filters: SearchFilters
    ): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCuisines(): Flow<Result<List<Cuisine>>> =
        flowOf(Result.success(emptyList()))

    // ✅ Fake data
    private fun fakeRestaurant() = Restaurant(
        id = "r1",
        name = "Test Restaurant",
        imageUrl = "",
        rating = 4.5,
        cuisines = listOf("Indian"),
        isOpen = true,
        thumbUrl = "",
        ratingText = "",
        ratingColor = "",
        totalVotes = 9,
        avgDeliveryTime = 9,
        deliveryFee = 0.0,
        minOrder = 5,
        address = "",
        locality = "",
        distanceKm = 0.0,
        hasDelivery = true,
        offers = emptyList(),
        avgCostForTwo = 9
    )
}