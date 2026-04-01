package com.swapna.foodapp.data.local

import android.content.Context
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.swapna.foodapp.data.remote.dto.RestaurantDto
import javax.inject.Inject

class LocalRestaurantDataSource @Inject constructor(
    private val context: Context
) {

    fun getRestaurants(): List<RestaurantDto> {
        val json = context.assets.open("restaurants.json")
            .bufferedReader()
            .use { it.readText() }

        val type = Types.newParameterizedType(List::class.java, RestaurantDto::class.java)
        val adapter = Moshi.Builder().build().adapter<List<RestaurantDto>>(type)

        return adapter.fromJson(json) ?: emptyList()
    }
}