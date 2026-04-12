package com.swapna.foodapp.data.remote.api

import com.swapna.foodapp.data.remote.dto.CategoriesResponse
import com.swapna.foodapp.data.remote.dto.CollectionsResponse
import com.swapna.foodapp.data.remote.dto.CuisinesResponse
import com.swapna.foodapp.data.remote.dto.DailyMenuResponse
import com.swapna.foodapp.data.remote.dto.EstablishmentsResponse
import com.swapna.foodapp.data.remote.dto.GeocodeResponse
import com.swapna.foodapp.data.remote.dto.LocationSuggestionsResponse
import com.swapna.foodapp.data.remote.dto.OrdersResponse
import com.swapna.foodapp.data.remote.dto.RestaurantDto
import com.swapna.foodapp.data.remote.dto.ReviewsResponse
import com.swapna.foodapp.data.remote.dto.SearchResponse
import com.swapna.foodapp.data.remote.dto.UserResponse
import retrofit2.http.GET

interface FoodApi {

    // ── Home Screen
    @GET("categories.json")
    suspend fun getCategories(): CategoriesResponse

    @GET("collections.json")
    suspend fun getCollections(): CollectionsResponse

    @GET("geocode.json")
    suspend fun getNearbyRestaurants(): GeocodeResponse

    // ── Search Screen
    @GET("search.json")
    suspend fun searchRestaurants(): SearchResponse

    @GET("cuisines.json")
    suspend fun getCuisines(): CuisinesResponse

    @GET("establishments.json")
    suspend fun getEstablishments(): EstablishmentsResponse

    @GET("locations.json")
    suspend fun getLocations(): LocationSuggestionsResponse

    // ── Restaurant Screen
    @GET("restaurant.json")
    suspend fun getRestaurantDetail(): RestaurantDto

    @GET("dailymenu.json")
    suspend fun getDailyMenu(): DailyMenuResponse

    @GET("reviews.json")
    suspend fun getReviews(): ReviewsResponse

    // ── Profile Screen
    @GET("user.json")
    suspend fun getUser(): UserResponse

    @GET("orders.json")
    suspend fun getOrders(): OrdersResponse
}