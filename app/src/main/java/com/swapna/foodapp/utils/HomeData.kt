package com.swapna.foodapp.utils

import com.swapna.foodapp.domain.model.*

data class HomeData(
    val collections: List<Collections>   = emptyList(),
    val categories: List<FoodCategory>  = emptyList(),
    val restaurants: List<Restaurant>   = emptyList(),
)