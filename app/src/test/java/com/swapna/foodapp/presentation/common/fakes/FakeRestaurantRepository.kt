package com.swapna.foodapp.presentation.common.fakes

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
import com.swapna.foodapp.utils.TestConstants

class FakeRestaurantRepository : RestaurantRepository {

    // ── Control flags ─────────────────────────────────────────
    var shouldThrowRestaurant = false
    var shouldThrowMenu       = false
    var errorMessage          = "Something went wrong"

    // Override per test
    var menuResult: Map<String, List<MenuItem>> =
        fakeMenuByCategory()

    var restaurantResult: Restaurant = fakeRestaurant()

    // ── Interface ─────────────────────────────────────────────

    override fun getNearbyRestaurants():
            Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(listOf(restaurantResult)))

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
        return flowOf(Result.success(restaurantResult))
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

    // ══════════════════════════════════════════════════════════
    companion object {
        // ══════════════════════════════════════════════════════════

        // ── fakeRestaurant ────────────────────────────────────
        // ✅ SYNCED with TestFunctions.fakeRestaurant()
        // All new Restaurant fields included
        fun fakeRestaurant(
            id:           String       = TestConstants.RESTAURANT_ID,
            name:         String       = TestConstants.RESTAURANT_NAME,
            rating:       Double       = TestConstants.RESTAURANT_RATING,
            deliveryTime: Int          = TestConstants.DELIVERY_TIME,
            isOpen:       Boolean      = true,
            cuisines:     List<String> = listOf(
                "Biryani", "South Indian"
            ),
            locality:     String       = TestConstants.LOCALITY,
        ) = Restaurant(
            id              = id,
            name            = name,
            imageUrl        = TestConstants.IMAGE_URL,
            thumbUrl        = TestConstants.THUMB_URL,
            rating          = rating,
            ratingText      = "Very Good",
            ratingColor     = "5BA829",
            totalVotes      = 1000,
            avgDeliveryTime = deliveryTime,
            deliveryFee     = 30.0,
            minOrder        = 100,
            cuisines        = cuisines,
            address         = TestConstants.RESTAURANT_ADDRESS,
            locality        = locality,
            isOpen          = isOpen,
            hasDelivery     = true,
            offers          = listOf("50% off upto ₹100"),
            avgCostForTwo   = 600,
            phoneNumber     = "",
            openingHours    = "11 AM - 11 PM",
            highlights      = emptyList(),
            knownFor        = "",
            distanceKm      = 0.0,
        )

        // ── fakeMenuItem ──────────────────────────────────────
        // ✅ FIX: isBestseller + isAvailable + customisations added
        // Matches MenuItem domain model exactly
        // ALL params have defaults → call as:
        //   fakeMenuItem()                    → all defaults
        //   fakeMenuItem("m1")                → id only
        //   fakeMenuItem("m1", "Biryani", 249.0) → positional
        //   fakeMenuItem(price = 50.0)        → named
        //   fakeMenuItem(isBestseller = true) → named
        fun fakeMenuItem(
            id:             String              = TestConstants.MENU_ID,
            name:           String              = TestConstants.MENU_NAME,
            price:          Double              = TestConstants.MENU_PRICE,
            category:       String              = TestConstants.CATEGORY,
            isVeg:          Boolean             = false,
            isRecommended:  Boolean             = false,
            // ✅ FIX: was missing from original
            isBestseller:   Boolean             = false,
            // ✅ FIX: was missing from original
            isAvailable:    Boolean             = true,
            // ✅ FIX: was missing from original
            customisations: List<Customisation> = emptyList(),
            restaurantId:   String              = TestConstants.RESTAURANT_ID,
        ) = MenuItem(
            id             = id,
            restaurantId   = restaurantId,
            name           = name,
            description    = "Delicious $name",
            price          = price,
            imageUrl       = TestConstants.FOOD_IMAGE,
            category       = category,
            isVeg          = isVeg,
            isRecommended  = isRecommended,
            isBestseller   = isBestseller,
            isAvailable    = isAvailable,
            customisations = customisations,
        )

        // ── fakeMenuByCategory ────────────────────────────────
        // Default menu: 2 categories, 4 items total
        // m1 + m3 are recommended
        // m1 is bestseller
        fun fakeMenuByCategory() = mapOf(
            "Biryani" to listOf(
                fakeMenuItem(
                    id            = "m1",
                    name          = "Chicken Biryani",
                    price         = 249.0,
                    isRecommended = true,
                    isBestseller  = true,
                ),
                fakeMenuItem(
                    id    = "m2",
                    name  = "Mutton Biryani",
                    price = 349.0,
                ),
            ),
            "Starters" to listOf(
                fakeMenuItem(
                    id            = "m3",
                    name          = "Chicken 65",
                    price         = 199.0,
                    isRecommended = true,
                    category      = "Starters",
                ),
                fakeMenuItem(
                    id       = "m4",
                    name     = "Paneer Tikka",
                    price    = 179.0,
                    isVeg    = true,
                    category = "Starters",
                ),
            ),
        )

        // ── fakeMenuItemWithCustomisations ────────────────────
        // Used in ProductDetailViewModelSpec
        fun fakeMenuItemWithCustomisations() = fakeMenuItem(
            id             = TestConstants.MENU_ID,
            name           = TestConstants.MENU_NAME,
            price          = TestConstants.MENU_PRICE,
            isRecommended  = true,
            isBestseller   = true,
            customisations = listOf(
                Customisation(
                    id      = "size_group",
                    name    = "Size",
                    options = listOf(
                        CustomisationOption("opt1", "Regular", 0.0),
                        CustomisationOption("opt2", "Large",   50.0),
                    ),
                ),
                Customisation(
                    id      = "spice_group",
                    name    = "Spice Level",
                    options = listOf(
                        CustomisationOption("opt3", "Mild",   0.0),
                        CustomisationOption("opt4", "Medium", 0.0),
                        CustomisationOption("opt5", "Hot",    0.0),
                    ),
                ),
            ),
        )

        // ── fakeSimpleMenuItem ────────────────────────────────
        // No customisations — for simple add to cart tests
        fun fakeSimpleMenuItem() = fakeMenuItem(
            id            = "m_simple",
            name          = "Plain Naan",
            price         = 50.0,
            isVeg         = true,
            category      = "Breads",
        )

        // ── fakeMenuByCategoryWithCustomisations ──────────────
        // Used in ProductDetailViewModelSpec
        fun fakeMenuByCategoryWithCustomisations() = mapOf(
            "Biryani" to listOf(
                fakeMenuItemWithCustomisations(),
            ),
            "Breads" to listOf(
                fakeSimpleMenuItem(),
            ),
        )
    }
}