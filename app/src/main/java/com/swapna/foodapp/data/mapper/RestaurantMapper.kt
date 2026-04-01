package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.RestaurantDto
import com.swapna.foodapp.domain.model.Restaurant

fun RestaurantDto.toDomain(): Restaurant {
    return Restaurant(
        name = name,
        rating = (3..5).random().toDouble()
    )
}