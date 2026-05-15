package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.utils.TestConstants.CITY_BENGALURU
import com.swapna.foodapp.utils.TestConstants.DELIVERY_TIME
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.HOME_DELIVERY_FEE
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.HOME_OFFERS_50_OFF
import com.swapna.foodapp.utils.TestConstants.HOME_RATING_COLOR
import com.swapna.foodapp.utils.TestConstants.HOME_RATING_TEXT_EXCELLENT
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R1
import com.swapna.foodapp.utils.TestConstants.HOME_SOUTH_INDIAN
import com.swapna.foodapp.utils.TestConstants.HOME_TEST_REST_NAME
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_COST_500
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MIN_ORDER_100
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_RATING_45
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_VOTES_5000

fun fakeRestaurant(
    id: String = HOME_REST_R1,
    name: String = HOME_TEST_REST_NAME,
    locality: String = HOME_LOC_KORAMANGALA,
    rating: Double = RESTAURANT_RATING_45,
    deliveryTime: Int = DELIVERY_TIME,
    cuisines: List<String> = listOf(HOME_CATEGORY_BIRYANI, HOME_SOUTH_INDIAN),
) = Restaurant(
    id = id,
    name = name,
    imageUrl = "https://picsum.photos/seed/$id/600/300",
    thumbUrl = "https://picsum.photos/seed/$id/200/200",
    rating = rating,
    ratingText = HOME_RATING_TEXT_EXCELLENT,
    ratingColor = HOME_RATING_COLOR,
    totalVotes = RESTAURANT_VOTES_5000,
    avgDeliveryTime = deliveryTime,
    deliveryFee = HOME_DELIVERY_FEE,
    avgCostForTwo = RESTAURANT_COST_500,
    minOrder = RESTAURANT_MIN_ORDER_100,
    cuisines = cuisines,
    address = "$locality, $CITY_BENGALURU",
    locality = locality,
    distanceKm = 0.0,
    hasDelivery = true,
    isOpen = true,
    offers = listOf(HOME_OFFERS_50_OFF),
)

