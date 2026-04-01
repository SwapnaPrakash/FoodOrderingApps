package com.swapna.foodapp.data.remote.dto

data class RestaurantDto(
    val name: String,
    val rating: Double,
    val image: String,
    val deliveryTime: String
)