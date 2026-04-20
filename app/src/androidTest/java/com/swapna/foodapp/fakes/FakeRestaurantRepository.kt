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

    // ── Per-test control properties ───────────────────────────
    // WHY separate result per method?
    // HomeScreen tests need to control restaurants/categories/collections
    // independently — e.g. show categories but no restaurants

    var nearbyRestaurantsResult: Result<List<Restaurant>> =
        Result.success(emptyList())

    var collectionsResult: Result<List<Collections>> =
        Result.success(emptyList())

    var categoriesResult: Result<List<FoodCategory>> =
        Result.success(emptyList())

    // Keep existing flags for RestaurantScreen tests
    var shouldThrowRestaurant = false
    var shouldThrowMenu       = false
    var errorMessage          = "Something went wrong"
    var menuResult: Map<String, List<MenuItem>> = fakeMenuByCategory()

    // Add this property
    var throwError: Exception? = null

    override fun getNearbyRestaurants(): Flow<Result<List<Restaurant>>> {
        // WHY throw not emit failure?
        // GetHomeDataUseCase uses combine() which calls getOrDefault(emptyList())
        // Result.failure emitted → use case sees empty list → NO error state in UI
        // Throwing from flow → combine() propagates exception → HomeViewModel
        //   catches it → sets error state → ErrorScreen shown
        throwError?.let { throw it }
        return flowOf(nearbyRestaurantsResult)
    }

    // ── Home screen methods ───────────────────────────────────

    override fun getCollections(): Flow<Result<List<Collections>>> =
        flowOf(collectionsResult)

    override fun getCategories(): Flow<Result<List<FoodCategory>>> =
        flowOf(categoriesResult)

    // ── Restaurant screen methods ─────────────────────────────

    override fun getRestaurantDetail(id: String): Flow<Result<Restaurant>> {
        if (shouldThrowRestaurant) {
            return flowOf(Result.failure(Exception(errorMessage)))
        }
        return flowOf(Result.success(fakeRestaurant()))
    }

    override fun getMenuItems(
        restaurantId: String,
    ): Flow<Result<Map<String, List<MenuItem>>>> {
        if (shouldThrowMenu) {
            return flowOf(Result.failure(Exception(errorMessage)))
        }
        return flowOf(Result.success(menuResult))
    }

    override fun getReviews(
        restaurantId: String,
    ): Flow<Result<List<Review>>> =
        flowOf(Result.success(emptyList()))

    // ── Search screen methods ─────────────────────────────────

    override fun searchRestaurants(
        query:   String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCuisines(): Flow<Result<List<Cuisine>>> =
        flowOf(Result.success(emptyList()))

    companion object {

        fun fakeRestaurant(
            id:   String = "r1",
            name: String = "Meghana Foods",
        ) = Restaurant(
            id              = id,
            name            = name,
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
            phoneNumber     = "",
            openingHours    = "11 AM - 11 PM",
            highlights      = emptyList(),
            knownFor        = "",
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
            id             = id,
            restaurantId   = "r1",
            name           = name,
            description    = "Delicious $name",
            price          = price,
            imageUrl       = "",
            category       = "Biryani",
            isVeg          = false,
            isRecommended  = isRecommended,
            isBestseller   = false,
            isAvailable    = true,
            customisations = emptyList(),
        )
    }
}