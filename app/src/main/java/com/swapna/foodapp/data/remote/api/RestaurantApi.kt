package com.swapna.foodapp.data.remote.api

import com.swapna.foodapp.data.remote.dto.RestaurantDto
import retrofit2.http.GET

interface RestaurantApi {

    @GET("SwapnaPrakash/FoodOrderingApps/blob/main/restaurants.json")
    suspend fun getRestaurants(): List<RestaurantDto>
}