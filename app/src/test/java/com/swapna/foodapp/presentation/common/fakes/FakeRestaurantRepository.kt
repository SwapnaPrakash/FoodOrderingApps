package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.Review
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.utils.TestConstants
import com.swapna.foodapp.utils.TestConstants.ERROR_MESSAGE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeRestaurantRepository : RestaurantRepository {

    var shouldThrowRestaurant = false
    var shouldThrowMenu = false
    var errorMessage = ERROR_MESSAGE

    var menuResult: Map<String, List<MenuItem>> =
        fakeMenuByCategory()

    var restaurantResult: Restaurant = fakeRestaurant()

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
        query: String,
        filters: SearchFilters,
    ): Flow<Result<List<Restaurant>>> =
        flowOf(Result.success(emptyList()))

    override fun getCuisines():
            Flow<Result<List<Cuisine>>> =
        flowOf(Result.success(emptyList()))


    companion object {
        fun fakeRestaurant(
            id: String = TestConstants.RESTAURANT_ID,
            name: String = TestConstants.RESTAURANT_NAME,
            rating: Double = TestConstants.RESTAURANT_RATING,
            deliveryTime: Int = TestConstants.DELIVERY_TIME,
            isOpen: Boolean = true,
            cuisines: List<String> = listOf(
                TestConstants.HOME_CATEGORY_BIRYANI,
                TestConstants.HOME_SOUTH_INDIAN,
            ),
            locality: String = TestConstants.LOCALITY,
        ) = Restaurant(
            id = id,
            name = name,
            imageUrl = TestConstants.IMAGE_URL,
            thumbUrl = TestConstants.THUMB_URL,
            rating = rating,
            ratingText = TestConstants.ENTITY_RATING_TEXT,
            ratingColor = TestConstants.ENTITY_RATING_COLOR,
            totalVotes = TestConstants.FAKE_RESTAURANT_VOTES,
            avgDeliveryTime = deliveryTime,
            deliveryFee = TestConstants.HOME_DELIVERY_FEE,
            minOrder = TestConstants.RESTAURANT_MIN_ORDER_100,
            cuisines = cuisines,
            address = TestConstants.RESTAURANT_ADDRESS,
            locality = locality,
            isOpen = isOpen,
            hasDelivery = true,
            offers = listOf(TestConstants.FAKE_OFFER_UPTO_100),
            avgCostForTwo = TestConstants.ENTITY_COST_TWO,
            phoneNumber = "",
            openingHours = TestConstants.OPENING_HOURS_DEFAULT,
            highlights = emptyList(),
            knownFor = "",
            distanceKm = 0.0,
        )

        fun fakeMenuItem(
            id: String = TestConstants.MENU_ID,
            name: String = TestConstants.MENU_NAME,
            price: Double = TestConstants.MENU_PRICE,
            category: String = TestConstants.CATEGORY,
            isVeg: Boolean = false,
            isRecommended: Boolean = false,
            isBestseller: Boolean = false,
            isAvailable: Boolean = true,
            customisations: List<Customisation> = emptyList(),
            restaurantId: String = TestConstants.RESTAURANT_ID,
        ) = MenuItem(
            id = id,
            restaurantId = restaurantId,
            name = name,
            description = "Delicious $name",
            price = price,
            imageUrl = TestConstants.FOOD_IMAGE,
            category = category,
            isVeg = isVeg,
            isRecommended = isRecommended,
            isBestseller = isBestseller,
            isAvailable = isAvailable,
            customisations = customisations,
        )

        fun fakeMenuByCategory() = mapOf(
            TestConstants.HOME_CATEGORY_BIRYANI to listOf(
                fakeMenuItem(
                    id = TestConstants.MENU_ID_1,
                    name = TestConstants.MENU_ITEM_CHICK_BIR,
                    price = TestConstants.PRICE_249,
                    isRecommended = true,
                    isBestseller = true,
                ),
                fakeMenuItem(
                    id = TestConstants.MENU_ID_2,
                    name = TestConstants.MENU_ITEM_MUTTON_BIR,
                    price = TestConstants.PRICE_349,
                ),
            ),
            TestConstants.CATEGORY_STARTERS to listOf(
                fakeMenuItem(
                    id = TestConstants.MENU_ID_3,
                    name = TestConstants.MENU_ITEM_CHICK_65,
                    price = TestConstants.PRICE_199,
                    isRecommended = true,
                    category = TestConstants.CATEGORY_STARTERS,
                ),
                fakeMenuItem(
                    id = TestConstants.MENU_ID_4,
                    name = TestConstants.MENU_ITEM_PANEER_TIKKA,
                    price = TestConstants.PRICE_179,
                    isVeg = true,
                    category = TestConstants.CATEGORY_STARTERS,
                ),
            ),
        )

        fun fakeMenuItemWithCustomisations() = fakeMenuItem(
            id = TestConstants.MENU_ID,
            name = TestConstants.MENU_NAME,
            price = TestConstants.MENU_PRICE,
            isRecommended = true,
            isBestseller = true,
            customisations = listOf(
                Customisation(
                    id = TestConstants.CUSTOMISE_GROUP_SIZE,
                    name = TestConstants.CUSTOMISE_NAME_SIZE,
                    options = listOf(
                        CustomisationOption(
                            TestConstants.OPT_ID_1,
                            TestConstants.CUSTOMISE_LABEL_REGULAR,
                            TestConstants.EXTRA_PRICE_ZERO,
                        ),
                        CustomisationOption(
                            TestConstants.OPT_ID_2,
                            TestConstants.CUSTOMISE_LABEL_LARGE,
                            TestConstants.EXTRA_PRICE_LARGE,
                        ),
                    ),
                ),
                Customisation(
                    id = TestConstants.CUSTOMISE_GROUP_SPICE,
                    name = TestConstants.CUSTOMISE_NAME_SPICE,
                    options = listOf(
                        CustomisationOption(
                            TestConstants.OPT_ID_3,
                            TestConstants.CUSTOMISE_LABEL_MILD,
                            TestConstants.EXTRA_PRICE_ZERO,
                        ),
                        CustomisationOption(
                            TestConstants.OPT_ID_4,
                            TestConstants.CUSTOMISE_LABEL_MEDIUM,
                            TestConstants.EXTRA_PRICE_ZERO,
                        ),
                        CustomisationOption(
                            TestConstants.OPT_ID_5,
                            TestConstants.CUSTOMISE_LABEL_HOT,
                            TestConstants.EXTRA_PRICE_ZERO,
                        ),
                    ),
                ),
            ),
        )

        fun fakeSimpleMenuItem() = fakeMenuItem(
            id = TestConstants.MENU_ID_SIMPLE,
            name = TestConstants.MENU_ITEM_PLAIN_NAAN,
            price = TestConstants.PRICE_50,
            isVeg = true,
            category = TestConstants.CATEGORY_BREADS,
        )

        fun fakeMenuByCategoryWithCustomisations() = mapOf(
            TestConstants.HOME_CATEGORY_BIRYANI to listOf(
                fakeMenuItemWithCustomisations(),
            ),
            TestConstants.CATEGORY_BREADS to listOf(
                fakeSimpleMenuItem(),
            ),
        )
    }
}