package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Top-level wrappers matching JSON structure ─────────────────

data class GeocodeResponse(
    @SerializedName("location")
    val location: LocationDto,

    @SerializedName("nearby_restaurants")
    val nearbyRestaurants: List<RestaurantWrapper>,
)

// The API wraps each restaurant in { "restaurant": { ... } }
data class RestaurantWrapper(
    @SerializedName("restaurant")
    val restaurant: RestaurantDto,
)

data class SearchResponse(
    @SerializedName("results_found")
    val totalFound: Int,

    @SerializedName("results_shown")
    val shown: Int,

    @SerializedName("restaurants")
    val restaurants: List<RestaurantWrapper>,
)

// ── Main Restaurant DTO ────────────────────────────────────────

data class RestaurantDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("featured_image")
    val featuredImage: String,

    @SerializedName("thumb")
    val thumb: String,

    @SerializedName("location")
    val location: LocationDto,

    @SerializedName("cuisines")
    val cuisines: String,           // ⚠️ "Biryani, North Indian" — split on ","

    @SerializedName("average_cost_for_two")
    val avgCostForTwo: Int,

    @SerializedName("price_range")
    val priceRange: Int,            // 1=cheap, 4=expensive

    @SerializedName("currency")
    val currency: String,           // "Rs."

    @SerializedName("user_rating")
    val rating: RatingDto,

    @SerializedName("has_online_delivery")
    val hasDelivery: Int,           // ⚠️ Int not Boolean (1 or 0)

    @SerializedName("is_delivering_now")
    val isDeliveringNow: Int,       // ⚠️ Int not Boolean (1 or 0)

    @SerializedName("estimated_delivery_time")
    val deliveryTime: Int? = null,  // nullable — not always present

    @SerializedName("minimum_order")
    val minOrder: Int? = null,

    @SerializedName("offers")
    val offers: List<String>? = null,
)

// ── Rating sub-object ──────────────────────────────────────────

data class RatingDto(
    @SerializedName("aggregate_rating")
    val rating: String,             // ⚠️ "4.3" as String — parse with toDoubleOrNull()

    @SerializedName("rating_text")
    val text: String,               // "Very Good", "Excellent"

    @SerializedName("rating_color")
    val color: String,              // "5BA829" hex, no #

    @SerializedName("votes")
    val votes: String,              // ⚠️ "12,547" as String — strip comma, parse Int
)

// ── Location sub-object ───────────────────────────────────────

data class LocationDto(
    @SerializedName("address")
    val address: String,

    @SerializedName("locality")
    val locality: String,

    @SerializedName("city")
    val city: String,

    @SerializedName("city_id")
    val cityId: Int,

    @SerializedName("latitude")
    val latitude: String,           // ⚠️ "12.9352" as String — parse with toDoubleOrNull()

    @SerializedName("longitude")
    val longitude: String,          // ⚠️ "77.6245" as String — parse with toDoubleOrNull()

    @SerializedName("zipcode")
    val zipcode: String,
)