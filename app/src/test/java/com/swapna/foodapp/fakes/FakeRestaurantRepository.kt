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
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption

// WHY FakeRestaurantRepository?
// Tests need to CONTROL what API returns
// Real API = needs internet + real server
// Fake = deterministic, instant, offline
// Control flags = one flag per failure scenario

class FakeRestaurantRepository : RestaurantRepository {

    // Control flags — set per test
    var shouldThrowRestaurant = false
    var shouldThrowMenu       = false
    var errorMessage          = "Something went wrong"

    // Override menu per test
    var menuResult: Map<String, List<MenuItem>> =
        fakeMenuByCategory()

    override fun getNearbyRestaurants():
            Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCollections():
            Flow<Result<List<Collections>>> =
        flowOf(Result.success(emptyList()))

    override fun getCategories():
            Flow<Result<List<FoodCategory>>> =
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

    override fun getCuisines():
            Flow<Result<List<Cuisine>>> =
        flowOf(Result.success(emptyList()))

    companion object {

        fun fakeRestaurant() = Restaurant(
            id              = "r1",
            name            = "Meghana Foods",
            imageUrl        = "",
            rating          = 4.6,
            cuisines        = listOf("Biryani"),
            isOpen          = true,
            thumbUrl        = "",
            ratingText      = "Excellent",
            ratingColor     = "#3F7E00",
            totalVotes      = 12500,
            avgDeliveryTime = 30,
            deliveryFee     = 0.0,
            minOrder        = 100,
            address         = "Koramangala",
            locality        = "Koramangala",
            distanceKm      = 2.5,
            hasDelivery     = true,
            offers          = emptyList(),
            avgCostForTwo   = 400,
        )

        // ── Customisation options ─────────────────────────────
        // These are the building blocks for customisation tests

        fun fakeSizeOptions() = listOf(
            // extraPrice = 0.0 → "Included" — base price
            CustomisationOption(
                id         = "regular",
                label      = "Regular",
                extraPrice = 0.0,
            ),
            // extraPrice = 50.0 → "+₹50" on top of base
            CustomisationOption(
                id         = "large",
                label      = "Large",
                extraPrice = 50.0,
            ),
            CustomisationOption(
                id         = "extra_large",
                label      = "Extra Large",
                extraPrice = 100.0,
            ),
        )

        fun fakeSpiceOptions() = listOf(
            CustomisationOption(
                id         = "mild",
                label      = "Mild",
                extraPrice = 0.0,
            ),
            CustomisationOption(
                id         = "medium",
                label      = "Medium",
                extraPrice = 0.0,
            ),
            CustomisationOption(
                id         = "hot",
                label      = "Hot",
                extraPrice = 0.0,
            ),
        )

        // ── Customisation groups ──────────────────────────────
        fun fakeCustomisations() = listOf(
            Customisation(
                id      = "size_group",
                name    = "Size",
                options = fakeSizeOptions(),
            ),
            Customisation(
                id      = "spice_group",
                name    = "Spice Level",
                options = fakeSpiceOptions(),
            ),
        )

        // ── MenuItem with customisations ──────────────────────
        fun fakeMenuItemWithCustomisations() = MenuItem(
            id             = "m1",
            restaurantId   = "r1",
            name           = "Chicken Biryani",
            description    = "Aromatic basmati rice",
            price          = 249.0,
            imageUrl       = "",
            category       = "Biryani",
            isVeg          = false,
            isRecommended  = true,
            isBestseller   = true,
            isAvailable    = true,
            // Attach customisations to item
            customisations = fakeCustomisations(),
        )

        // ── MenuItem WITHOUT customisations ───────────────────
        // Some items have no customisation (plain items)
        fun fakeSimpleMenuItem() = MenuItem(
            id             = "m2",
            restaurantId   = "r1",
            name           = "Plain Naan",
            description    = "Tandoor naan",
            price          = 50.0,
            imageUrl       = "",
            category       = "Breads",
            isVeg          = true,
            isRecommended  = false,
            isBestseller   = false,
            isAvailable    = true,
            customisations = emptyList(),  // ← no customisations
        )

        fun fakeMenuByCategory() = mapOf(
            "Biryani" to listOf(
                fakeMenuItemWithCustomisations(),
            ),
            "Breads" to listOf(
                fakeSimpleMenuItem(),
            ),
        )
    }
}