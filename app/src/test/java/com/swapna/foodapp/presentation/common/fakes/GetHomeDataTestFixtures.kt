package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.RestaurantCollection
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_2
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_PIZZA
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_COUNT
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_DESC
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_DISCOUNT
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_ID
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_TITLE
import com.swapna.foodapp.utils.TestConstants.HOME_RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_RESTAURANT_ID_2

val getHomeDataFakeCollections = listOf(
    RestaurantCollection(
        HOME_COLLECTION_ID,
        HOME_COLLECTION_TITLE,
        HOME_COLLECTION_DESC,
        "",
        HOME_COLLECTION_COUNT,
        HOME_COLLECTION_DISCOUNT,
    ),
)

val getHomeDataFakeCategories = listOf(
    FoodCategory(HOME_CATEGORY_ID_1, HOME_CATEGORY_BIRYANI, ""),
    FoodCategory(HOME_CATEGORY_ID_2, HOME_CATEGORY_PIZZA, ""),
)

val getHomeDataFakeRestaurants = listOf(
    fakeRestaurant(HOME_RESTAURANT_ID_1),
    fakeRestaurant(HOME_RESTAURANT_ID_2),
)



