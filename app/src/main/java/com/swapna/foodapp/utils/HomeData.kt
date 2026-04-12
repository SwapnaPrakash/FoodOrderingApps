package com.swapna.foodapp.utils

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant

data class HomeData(
    val collections: List<Collections> = emptyList(),
    val categories: List<FoodCategory> = emptyList(),
    val restaurants: List<Restaurant> = emptyList(),
)