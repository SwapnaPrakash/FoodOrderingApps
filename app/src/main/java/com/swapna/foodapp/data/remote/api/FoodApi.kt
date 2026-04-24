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
import com.swapna.foodapp.utils.AppConstants.GET_CATEGORIES
import com.swapna.foodapp.utils.AppConstants.GET_COLLECTIONS
import com.swapna.foodapp.utils.AppConstants.GET_CUISINES
import com.swapna.foodapp.utils.AppConstants.GET_DAILY_MENU
import com.swapna.foodapp.utils.AppConstants.GET_ESTABLISHMENTS
import com.swapna.foodapp.utils.AppConstants.GET_GEOCODE
import com.swapna.foodapp.utils.AppConstants.GET_LOCATIONS
import com.swapna.foodapp.utils.AppConstants.GET_ORDERS
import com.swapna.foodapp.utils.AppConstants.GET_RESTAURANT
import com.swapna.foodapp.utils.AppConstants.GET_REVIEWS
import com.swapna.foodapp.utils.AppConstants.GET_USER
import com.swapna.foodapp.utils.AppConstants.SEARCH
import retrofit2.http.GET

interface FoodApi {

    @GET(GET_CATEGORIES)
    suspend fun getCategories(): CategoriesResponse

    @GET(GET_COLLECTIONS)
    suspend fun getCollections(): CollectionsResponse

    @GET(GET_GEOCODE)
    suspend fun getNearbyRestaurants(): GeocodeResponse

    @GET(SEARCH)
    suspend fun searchRestaurants(): SearchResponse

    @GET(GET_CUISINES)
    suspend fun getCuisines(): CuisinesResponse

    @GET(GET_DAILY_MENU)
    suspend fun getDailyMenu(): DailyMenuResponse

    @GET(GET_REVIEWS)
    suspend fun getReviews(): ReviewsResponse

    @GET(GET_USER)
    suspend fun getUser(): UserResponse

    @GET(GET_ORDERS)
    suspend fun getOrders(): OrdersResponse

    @GET(GET_ESTABLISHMENTS)
    suspend fun getEstablishments(): EstablishmentsResponse

    @GET(GET_LOCATIONS)
    suspend fun getLocations(): LocationSuggestionsResponse

    @GET(GET_RESTAURANT)
    suspend fun getRestaurantDetail(): RestaurantDto
}