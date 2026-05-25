package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.RestaurantCollection
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.utils.HomeData
import com.swapna.foodapp.utils.TestConstants.CITY_BENGALURU
import com.swapna.foodapp.utils.TestConstants.DELIVERY_TIME
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_2
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_PIZZA
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_60_OFF
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_COUNT_10
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_COUNT_20
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_FREE_DEL
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_ID_2
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_JUST_LAUNCHED
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_NEWLY_OPENED
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_POPULAR
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_TRENDING
import com.swapna.foodapp.utils.TestConstants.HOME_DELIVERY_FEE
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_HSR
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_INDIRANAGAR
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.HOME_OFFERS_50_OFF
import com.swapna.foodapp.utils.TestConstants.HOME_RATING_COLOR
import com.swapna.foodapp.utils.TestConstants.HOME_RATING_TEXT_EXCELLENT
import com.swapna.foodapp.utils.TestConstants.HOME_REST_BURGER_KING
import com.swapna.foodapp.utils.TestConstants.HOME_REST_EMPIRE
import com.swapna.foodapp.utils.TestConstants.HOME_REST_MEGHANA
import com.swapna.foodapp.utils.TestConstants.HOME_REST_PIZZA_HUT
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R1
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R2
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R3
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R4
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
) = Restaurant(
    id = id,
    name = name,
    imageUrl = "https://picsum.photos/seed/$id/600/300",
    thumbUrl = "https://picsum.photos/seed/$id/200/200",
    rating = rating,
    ratingText = HOME_RATING_TEXT_EXCELLENT,
    ratingColor = HOME_RATING_COLOR,
    totalVotes = RESTAURANT_VOTES_5000,
    avgDeliveryTime = DELIVERY_TIME,
    deliveryFee = HOME_DELIVERY_FEE,
    avgCostForTwo = RESTAURANT_COST_500,
    minOrder = RESTAURANT_MIN_ORDER_100,
    cuisines = listOf(HOME_CATEGORY_BIRYANI, HOME_SOUTH_INDIAN),
    address = "$locality, $CITY_BENGALURU",
    locality = locality,
    distanceKm = 0.0,
    hasDelivery = true,
    isOpen = true,
    offers = listOf(HOME_OFFERS_50_OFF),
)

val fakeCollections = listOf(
    RestaurantCollection(
        HOME_COLL_ID_1,
        HOME_COLL_TRENDING,
        HOME_COLL_POPULAR,
        "",
        HOME_COLL_COUNT_20,
        HOME_COLL_60_OFF
    ),
    RestaurantCollection(
        HOME_COLL_ID_2,
        HOME_COLL_NEWLY_OPENED,
        HOME_COLL_JUST_LAUNCHED,
        "",
        HOME_COLL_COUNT_10,
        HOME_COLL_FREE_DEL
    ),
)

val fakeCategories = listOf(
    FoodCategory(HOME_CATEGORY_ID_1, HOME_CATEGORY_BIRYANI, ""),
    FoodCategory(HOME_CATEGORY_ID_2, HOME_CATEGORY_PIZZA, ""),
)

val restaurantsAll = listOf(
    fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA, locality = HOME_LOC_KORAMANGALA),
    fakeRestaurant(HOME_REST_R2, HOME_REST_PIZZA_HUT, locality = HOME_LOC_INDIRANAGAR),
    fakeRestaurant(HOME_REST_R3, HOME_REST_BURGER_KING, locality = HOME_LOC_HSR),
    fakeRestaurant(HOME_REST_R4, HOME_REST_EMPIRE, locality = HOME_LOC_KORAMANGALA),
)

val restaurantsKoramangala = listOf(
    fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA, locality = HOME_LOC_KORAMANGALA),
    fakeRestaurant(HOME_REST_R4, HOME_REST_EMPIRE, locality = HOME_LOC_KORAMANGALA),
)

val restaurantsIndiranagar = listOf(
    fakeRestaurant(HOME_REST_R2, HOME_REST_PIZZA_HUT, locality = HOME_LOC_INDIRANAGAR),
)

fun emptyHomeData() = HomeData(
    restaurants = emptyList(),
    collections = emptyList(),
    categories = emptyList(),
    filterStatus = FilterStatus.NO_FILTER,
    requestedArea = "",
    availableAreas = emptyList(),
)

fun homeData(
    restaurants: List<Restaurant> = restaurantsAll,
    collections: List<RestaurantCollection> = fakeCollections,
    categories: List<FoodCategory> = fakeCategories,
    filterStatus: FilterStatus = FilterStatus.NO_FILTER,
    requestedArea: String = "",
    availableAreas: List<String> = emptyList(),
) = HomeData(
    restaurants = restaurants,
    collections = collections,
    categories = categories,
    filterStatus = filterStatus,
    requestedArea = requestedArea,
    availableAreas = availableAreas,
)
