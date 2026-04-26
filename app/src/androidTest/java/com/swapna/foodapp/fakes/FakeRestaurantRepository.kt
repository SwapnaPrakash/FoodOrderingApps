package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.Review
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_ERROR_SOMETHING_WRONG
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_CATEGORY_STARTERS
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_CHICK_65
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_CHICK_BIR
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_DESC_PREFIX
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_ID_M1
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_ID_M2
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_ID_M3
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_ID_M4
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_MUTTON_BIR
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PANEER_TIKKA
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_179
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_199
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_249
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_349
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_ADDRESS_KORA
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_COST_FOR_TWO_400
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_CUISINE_BIRYANI
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_CUISINE_SOUTH_IND
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_DELIVERY_FEE_FREE
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_DELIVERY_TIME_30
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_DISTANCE_KM_2_5
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_ID_R1
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_LOCALITY_KORA
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_MEGHANA
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_MIN_ORDER_100
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_OFFER_20_OFF
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_OPENING_HOURS
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_RATING_4_6
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_RATING_COLOR
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_RATING_TEXT_EX
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_RESTAURANT_ID
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_TOTAL_VOTES_12500
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeRestaurantRepository : RestaurantRepository {

    var nearbyRestaurantsResult: Result<List<Restaurant>> =
        Result.success(emptyList())

    var collectionsResult: Result<List<Collections>> =
        Result.success(emptyList())

    var categoriesResult: Result<List<FoodCategory>> =
        Result.success(emptyList())

    var shouldThrowRestaurant = false
    var shouldThrowMenu = false
    var errorMessage = FAKE_ERROR_SOMETHING_WRONG
    var menuResult: Map<String, List<MenuItem>> = fakeMenuByCategory()
    var throwError: Exception? = null

    override fun getNearbyRestaurants(): Flow<Result<List<Restaurant>>> {
        throwError?.let { throw it }
        return flowOf(nearbyRestaurantsResult)
    }

    override fun getCollections(): Flow<Result<List<Collections>>> =
        flowOf(collectionsResult)

    override fun getCategories(): Flow<Result<List<FoodCategory>>> =
        flowOf(categoriesResult)

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

    override fun searchRestaurants(
        query: String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCuisines(): Flow<Result<List<Cuisine>>> =
        flowOf(Result.success(emptyList()))

    companion object {

        fun fakeRestaurant(
            id: String = FAKE_REST_ID_R1,
            name: String = FAKE_REST_MEGHANA,
        ) = Restaurant(
            id = id,
            name = name,
            imageUrl = "",
            rating = FAKE_REST_RATING_4_6,
            cuisines = listOf(
                FAKE_REST_CUISINE_BIRYANI,
                FAKE_REST_CUISINE_SOUTH_IND,
            ),
            isOpen = true,
            thumbUrl = "",
            ratingText = FAKE_REST_RATING_TEXT_EX,
            ratingColor = FAKE_REST_RATING_COLOR,
            totalVotes = FAKE_REST_TOTAL_VOTES_12500,
            avgDeliveryTime = FAKE_REST_DELIVERY_TIME_30,
            deliveryFee = FAKE_REST_DELIVERY_FEE_FREE,
            minOrder = FAKE_REST_MIN_ORDER_100,
            address = FAKE_REST_ADDRESS_KORA,
            locality = FAKE_REST_LOCALITY_KORA,
            distanceKm = FAKE_REST_DISTANCE_KM_2_5,
            hasDelivery = true,
            offers = listOf(FAKE_REST_OFFER_20_OFF),
            avgCostForTwo = FAKE_REST_COST_FOR_TWO_400,
            phoneNumber = "",
            openingHours = FAKE_REST_OPENING_HOURS,
            highlights = emptyList(),
            knownFor = "",
        )

        fun fakeMenuByCategory() = mapOf(
            FAKE_MENU_CATEGORY_BIRYANI to listOf(
                fakeMenuItem(FAKE_MENU_ID_M1, FAKE_MENU_CHICK_BIR, FAKE_MENU_PRICE_249, true),
                fakeMenuItem(
                    FAKE_MENU_ID_M2,
                    FAKE_MENU_MUTTON_BIR,
                    FAKE_MENU_PRICE_349,
                    false
                ),
            ),
            FAKE_MENU_CATEGORY_STARTERS to listOf(

                fakeMenuItem(FAKE_MENU_ID_M3, FAKE_MENU_CHICK_65, FAKE_MENU_PRICE_199, true),
                fakeMenuItem(
                    FAKE_MENU_ID_M4,
                    FAKE_MENU_PANEER_TIKKA,
                    FAKE_MENU_PRICE_179,
                    false
                ),
            ),
        )

        fun fakeMenuItem(
            id: String,
            name: String,
            price: Double,
            isRecommended: Boolean = false,
        ) = MenuItem(
            id = id,
            restaurantId = FAKE_REST_RESTAURANT_ID,
            name = name,
            description = "$FAKE_MENU_DESC_PREFIX$name",
            price = price,
            imageUrl = "",
            category = FAKE_MENU_CATEGORY_BIRYANI,
            isVeg = false,
            isRecommended = isRecommended,
            isBestseller = false,
            isAvailable = true,
            customisations = emptyList(),
        )
    }
}