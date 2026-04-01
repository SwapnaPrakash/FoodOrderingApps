package com.swapna.foodapp.data.remote.dto

data class RestaurantDto(
    val id: Int,
    val name: String,
    val rating: Double,
    val deliveryTime: String
)