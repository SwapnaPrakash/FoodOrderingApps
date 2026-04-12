package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.swapna.foodapp.utils.AppConstants

data class GeocodeResponse(
    @SerializedName(AppConstants.KEY_LOCATION)
    val location: LocationDto,

    @SerializedName(AppConstants.KEY_NEARBY_RESTAURANTS)
    val nearbyRestaurants: List<RestaurantWrapper>,
)

// The API wraps each restaurant in { "restaurant": { ... } }
data class RestaurantWrapper(
    @SerializedName(AppConstants.KEY_RESTAURANT)
    val restaurant: RestaurantDto,
)

data class SearchResponse(
    @SerializedName(AppConstants.KEY_RESULTS_FOUND)
    val totalFound: Int,

    @SerializedName(AppConstants.KEY_RESULTS_SHOWN)
    val shown: Int,

    @SerializedName(AppConstants.KEY_RESTAURANTS)
    val restaurants: List<RestaurantWrapper>,
)

// Main Restaurant DTO
data class RestaurantDto(
    @SerializedName(AppConstants.KEY_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_NAME)
    val name: String,

    @SerializedName(AppConstants.KEY_FEATURED_IMAGE)
    val featuredImage: String,

    @SerializedName(AppConstants.KEY_THUMB)
    val thumb: String,

    @SerializedName(AppConstants.KEY_LOCATION)
    val location: LocationDto,

    @SerializedName(AppConstants.KEY_CUISINES)
    val cuisines: String,

    @SerializedName(AppConstants.KEY_AVG_COST_FOR_TWO)
    val avgCostForTwo: Int,

    @SerializedName(AppConstants.KEY_PRICE_RANGE)
    val priceRange: Int,

    @SerializedName(AppConstants.KEY_CURRENCY)
    val currency: String,

    @SerializedName(AppConstants.KEY_USER_RATING)
    val rating: RatingDto,

    @SerializedName(AppConstants.KEY_HAS_DELIVERY)
    val hasDelivery: Int,

    @SerializedName(AppConstants.KEY_IS_DELIVERING_NOW)
    val isDeliveringNow: Int,

    @SerializedName(AppConstants.KEY_DELIVERY_TIME)
    val deliveryTime: Int? = null,

    @SerializedName(AppConstants.KEY_MIN_ORDER)
    val minOrder: Int? = null,

    @SerializedName(AppConstants.KEY_OFFERS)
    val offers: List<String>? = null,
)

// Rating sub-object
data class RatingDto(
    @SerializedName(AppConstants.KEY_AGGREGATE_RATING)
    val rating: String,

    @SerializedName(AppConstants.KEY_RATING_TEXT)
    val text: String,

    @SerializedName(AppConstants.KEY_RATING_COLOR)
    val color: String,

    @SerializedName(AppConstants.KEY_VOTES)
    val votes: String,
)

// Location sub-object
data class LocationDto(
    @SerializedName(AppConstants.KEY_ADDRESS)
    val address: String,

    @SerializedName(AppConstants.KEY_LOCALITY)
    val locality: String,

    @SerializedName(AppConstants.KEY_CITY)
    val city: String,

    @SerializedName(AppConstants.KEY_CITY_ID)
    val cityId: Int,

    @SerializedName(AppConstants.KEY_LATITUDE)
    val latitude: String,

    @SerializedName(AppConstants.KEY_LONGITUDE)
    val longitude: String,

    @SerializedName(AppConstants.KEY_ZIPCODE)
    val zipcode: String,
)