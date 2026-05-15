package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.utils.TestConstants.CUISINE_ANDHRA
import com.swapna.foodapp.utils.TestConstants.CUISINE_BIRYANI
import com.swapna.foodapp.utils.TestConstants.CUISINE_BURGER
import com.swapna.foodapp.utils.TestConstants.CUISINE_CHINESE
import com.swapna.foodapp.utils.TestConstants.CUISINE_FAST_FOOD
import com.swapna.foodapp.utils.TestConstants.CUISINE_HEALTHY
import com.swapna.foodapp.utils.TestConstants.CUISINE_ID_1
import com.swapna.foodapp.utils.TestConstants.CUISINE_ITALIAN
import com.swapna.foodapp.utils.TestConstants.CUISINE_MUGHLAI
import com.swapna.foodapp.utils.TestConstants.CUISINE_PIZZA
import com.swapna.foodapp.utils.TestConstants.CUISINE_SALADS
import com.swapna.foodapp.utils.TestConstants.SEARCH_COST_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_COST_2
import com.swapna.foodapp.utils.TestConstants.SEARCH_COST_3
import com.swapna.foodapp.utils.TestConstants.SEARCH_COST_4
import com.swapna.foodapp.utils.TestConstants.SEARCH_COST_5
import com.swapna.foodapp.utils.TestConstants.SEARCH_DELIVERY_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_DELIVERY_2
import com.swapna.foodapp.utils.TestConstants.SEARCH_DELIVERY_3
import com.swapna.foodapp.utils.TestConstants.SEARCH_DELIVERY_4
import com.swapna.foodapp.utils.TestConstants.SEARCH_DELIVERY_5
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_BEHROUZ
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_BURGER_KING
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_GREEN_BOWL
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_MEGHANA
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_PIZZA_HUT
import com.swapna.foodapp.utils.TestConstants.SEARCH_RATING_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_RATING_2
import com.swapna.foodapp.utils.TestConstants.SEARCH_RATING_3
import com.swapna.foodapp.utils.TestConstants.SEARCH_RATING_4
import com.swapna.foodapp.utils.TestConstants.SEARCH_RATING_5
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_2
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_3
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_4
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_5
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_BURGER_KING
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_MEGHANA
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_PIZZA_HUT
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_R1
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_R2
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_R3
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_RATING_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_RATING_2
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_RATING_3

val allSearchRestaurants: List<Restaurant> = listOf(
    fakeRestaurant(
        SEARCH_RESTAURANT_ID_1, SEARCH_NAME_MEGHANA,
        rating = SEARCH_RATING_1,
        deliveryTime = SEARCH_DELIVERY_1,
        cuisines = listOf(CUISINE_BIRYANI, CUISINE_ANDHRA)
    ).copy(avgCostForTwo = SEARCH_COST_1),

    fakeRestaurant(
        SEARCH_RESTAURANT_ID_2, SEARCH_NAME_PIZZA_HUT,
        rating = SEARCH_RATING_2,
        deliveryTime = SEARCH_DELIVERY_2,
        cuisines = listOf(CUISINE_PIZZA, CUISINE_ITALIAN)
    ).copy(avgCostForTwo = SEARCH_COST_2),

    fakeRestaurant(
        SEARCH_RESTAURANT_ID_3, SEARCH_NAME_BURGER_KING,
        rating = SEARCH_RATING_3,
        deliveryTime = SEARCH_DELIVERY_3,
        cuisines = listOf(CUISINE_BURGER, CUISINE_FAST_FOOD)
    ).copy(avgCostForTwo = SEARCH_COST_3),

    fakeRestaurant(
        SEARCH_RESTAURANT_ID_4, SEARCH_NAME_BEHROUZ,
        rating = SEARCH_RATING_4,
        deliveryTime = SEARCH_DELIVERY_4,
        cuisines = listOf(CUISINE_BIRYANI, CUISINE_MUGHLAI)
    ).copy(avgCostForTwo = SEARCH_COST_4),

    fakeRestaurant(
        SEARCH_RESTAURANT_ID_5, SEARCH_NAME_GREEN_BOWL,
        rating = SEARCH_RATING_5,
        deliveryTime = SEARCH_DELIVERY_5,
        cuisines = listOf(CUISINE_HEALTHY, CUISINE_SALADS)
    ).copy(avgCostForTwo = SEARCH_COST_5),
)

val fakeCuisines = listOf(
    Cuisine(CUISINE_ID_1, CUISINE_BIRYANI),
    Cuisine(2, CUISINE_PIZZA),
    Cuisine(3, CUISINE_BURGER),
    Cuisine(4, CUISINE_CHINESE),
)
val fakeResults = listOf(
    fakeRestaurant(SEARCH_VM_R1, SEARCH_VM_MEGHANA, rating = SEARCH_VM_RATING_1),
    fakeRestaurant(SEARCH_VM_R2, SEARCH_VM_PIZZA_HUT, rating = SEARCH_VM_RATING_2),
    fakeRestaurant(SEARCH_VM_R3, SEARCH_VM_BURGER_KING, rating = SEARCH_VM_RATING_3),
)