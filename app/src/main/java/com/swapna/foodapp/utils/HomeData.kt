package com.swapna.foodapp.utils

import com.swapna.foodapp.domain.model.RestaurantCollection
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.usecase.home.FilterStatus

data class HomeData(
    val collections: List<RestaurantCollection> = emptyList(),
    val categories: List<FoodCategory> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
    val filterStatus: FilterStatus = FilterStatus.NO_FILTER,
    val requestedArea: String = "",
    val availableAreas: List<String> = emptyList()
)