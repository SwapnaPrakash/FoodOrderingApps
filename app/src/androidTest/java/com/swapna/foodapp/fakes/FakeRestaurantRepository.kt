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

    // ✅ Add control flags for testing
    var shouldThrowRestaurant  = false
    var shouldThrowMenu        = false
    var errorMessage           = "Something went wrong"

    // ✅ Override menu result per test
    var menuResult: Map<String, List<MenuItem>> = fakeMenuByCategory()

    override fun getNearbyRestaurants(): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCollections(): Flow<Result<List<Collections>>> =
        flowOf(Result.success(emptyList()))

    override fun getCategories(): Flow<Result<List<FoodCategory>>> =
        flowOf(Result.success(emptyList()))

    override fun getRestaurantDetail(
        id: String,
    ): Flow<Result<Restaurant>> {
        if (shouldThrowRestaurant) {
            return flowOf(
                Result.failure(Exception(errorMessage))
            )
        }
        return flowOf(Result.success(fakeRestaurant()))
    }

    override fun getMenuItems(
        restaurantId: String,
    ): Flow<Result<Map<String, List<MenuItem>>>> {
        if (shouldThrowMenu) {
            return flowOf(
                Result.failure(Exception(errorMessage))
            )
        }
        return flowOf(Result.success(menuResult))
    }

    override fun getReviews(
        restaurantId: String,
    ): Flow<Result<List<Review>>> =
        flowOf(Result.success(emptyList()))

    override fun searchRestaurants(
        query:   String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCuisines(): Flow<Result<List<Cuisine>>> =
        flowOf(Result.success(emptyList()))

    companion object {

        fun fakeRestaurant() = Restaurant(
            id              = "r1",
            name            = "Meghana Foods",
            imageUrl        = "",
            rating          = 4.6,
            cuisines        = listOf("Biryani", "South Indian"),
            isOpen          = true,
            thumbUrl        = "",
            ratingText      = "Excellent",
            ratingColor     = "#3F7E00",
            totalVotes      = 12500,
            avgDeliveryTime = 30,
            deliveryFee     = 0.0,
            minOrder        = 100,
            address         = "Koramangala, Bengaluru",
            locality        = "Koramangala",
            distanceKm      = 2.5,
            hasDelivery     = true,
            offers          = listOf("20% off above ₹499"),
            avgCostForTwo   = 400,
        )

        fun fakeMenuByCategory() = mapOf(
            "Biryani" to listOf(
                fakeMenuItem("m1", "Chicken Biryani", 249.0, true),
                fakeMenuItem("m2", "Mutton Biryani",  349.0, false),
            ),
            "Starters" to listOf(
                fakeMenuItem("m3", "Chicken 65",   199.0, true),
                fakeMenuItem("m4", "Paneer Tikka", 179.0, false),
            ),
        )

        fun fakeMenuItem(
            id:            String,
            name:          String,
            price:         Double,
            isRecommended: Boolean = false,
        ) = MenuItem(
            id            = id,
            restaurantId  = "r1",
            name          = name,
            description   = "Delicious $name",
            price         = price,
            imageUrl      = "",
            category      = "Biryani",
            isVeg         = false,
            isRecommended = isRecommended,
            isBestseller  = false,
            isAvailable   = true,
        )
    }
}