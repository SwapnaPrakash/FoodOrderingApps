package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class UserResponse(
    @SerializedName("user")
    val user: UserDto,
)

data class UserDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phone")
    val phone: String,

    @SerializedName("profile_image")
    val profileImage: String? = null,

    @SerializedName("addresses")
    val addresses: List<AddressDto>? = null,
)

data class AddressDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("label")
    val label: String,

    @SerializedName("full_address")
    val fullAddress: String,

    @SerializedName("landmark")
    val landmark: String? = null,

    @SerializedName("latitude")
    val latitude: Double = 0.0,

    @SerializedName("longitude")
    val longitude: Double = 0.0,
)

// ── Orders ────────────────────────────────────────────────────

data class OrdersResponse(
    @SerializedName("orders")
    val orders: List<OrderWrapper>,
)

data class OrderWrapper(
    @SerializedName("order")
    val order: OrderDto,
)

data class OrderDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("restaurant_id")
    val restaurantId: String,

    @SerializedName("restaurant_name")
    val restaurantName: String,

    @SerializedName("restaurant_image")
    val restaurantImage: String,

    @SerializedName("status")
    val status: String,

    @SerializedName("time_friendly")
    val timeFriendly: String,

    @SerializedName("total_amount")
    val totalAmount: Double,

    @SerializedName("items")
    val items: List<OrderItemDto>,

    @SerializedName("can_reorder")
    val canReorder: Boolean = true,
)

data class OrderItemDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("quantity")
    val quantity: Int,

    @SerializedName("price")
    val price: Double,
)

// ── Reviews ───────────────────────────────────────────────────

data class ReviewsResponse(
    @SerializedName("reviews_count")
    val totalCount: Int,

    @SerializedName("user_reviews")
    val reviews: List<ReviewWrapper>,
)

data class ReviewWrapper(
    @SerializedName("review")
    val review: ReviewDto,
)

data class ReviewDto(
    @SerializedName("id")
    val id: Long,

    @SerializedName("rating")
    val rating: Int,

    @SerializedName("review_text")
    val text: String,

    @SerializedName("review_time_friendly")
    val timeAgo: String,

    @SerializedName("user")
    val user: ReviewUserDto,
)

data class ReviewUserDto(
    @SerializedName("name")
    val name: String,

    @SerializedName("profile_image")
    val profileImage: String? = null,
)