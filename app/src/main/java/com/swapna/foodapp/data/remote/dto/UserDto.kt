package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.swapna.foodapp.utils.AppConstants

data class UserResponse(
    @SerializedName(AppConstants.KEY_USER)
    val user: UserDto,
)

data class UserDto(
    @SerializedName(AppConstants.KEY_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_NAME)
    val name: String,

    @SerializedName(AppConstants.KEY_EMAIL)
    val email: String,

    @SerializedName(AppConstants.KEY_PHONE)
    val phone: String,

    @SerializedName(AppConstants.KEY_PROFILE_IMAGE)
    val profileImage: String? = null,

    @SerializedName(AppConstants.KEY_ADDRESSES)
    val addresses: List<AddressDto>? = null,
)

data class AddressDto(
    @SerializedName(AppConstants.KEY_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_LABEL)
    val label: String,

    @SerializedName(AppConstants.KEY_FULL_ADDRESS)
    val fullAddress: String,

    @SerializedName(AppConstants.KEY_LANDMARK)
    val landmark: String? = null,

    @SerializedName(AppConstants.KEY_LATITUDE)
    val latitude: Double = 0.0,

    @SerializedName(AppConstants.KEY_LONGITUDE)
    val longitude: Double = 0.0,
)

data class OrdersResponse(
    @SerializedName(AppConstants.KEY_ORDERS)
    val orders: List<OrderWrapper>,
)

data class OrderWrapper(
    @SerializedName(AppConstants.KEY_ORDER)
    val order: OrderDto,
)

data class OrderDto(
    @SerializedName(AppConstants.KEY_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_RESTAURANT_ID)
    val restaurantId: String,

    @SerializedName(AppConstants.KEY_RESTAURANT_NAME)
    val restaurantName: String,

    @SerializedName(AppConstants.KEY_RESTAURANT_IMAGE)
    val restaurantImage: String,

    @SerializedName(AppConstants.KEY_STATUS)
    val status: String,

    @SerializedName(AppConstants.KEY_TIME_FRIENDLY)
    val timeFriendly: String,

    @SerializedName(AppConstants.KEY_TOTAL_AMOUNT)
    val totalAmount: Double,

    @SerializedName(AppConstants.KEY_ITEMS)
    val items: List<OrderItemDto>,

    @SerializedName(AppConstants.KEY_CAN_REORDER)
    val canReorder: Boolean = true,
)

data class OrderItemDto(
    @SerializedName(AppConstants.KEY_NAME)
    val name: String,

    @SerializedName(AppConstants.KEY_QUANTITY)
    val quantity: Int,

    @SerializedName(AppConstants.KEY_PRICE)
    val price: Double,
)

data class ReviewsResponse(
    @SerializedName(AppConstants.KEY_REVIEWS_COUNT)
    val totalCount: Int,

    @SerializedName(AppConstants.KEY_USER_REVIEWS)
    val reviews: List<ReviewWrapper>,
)

data class ReviewWrapper(
    @SerializedName(AppConstants.KEY_REVIEW)
    val review: ReviewDto,
)

data class ReviewDto(
    @SerializedName(AppConstants.KEY_ID)
    val id: Long,

    @SerializedName(AppConstants.KEY_RATING)
    val rating: Int,

    @SerializedName(AppConstants.KEY_REVIEW_TEXT)
    val text: String,

    @SerializedName(AppConstants.KEY_REVIEW_TIME)
    val timeAgo: String,

    @SerializedName(AppConstants.KEY_USER)
    val user: ReviewUserDto,
)

data class ReviewUserDto(
    @SerializedName(AppConstants.KEY_NAME)
    val name: String,

    @SerializedName(AppConstants.KEY_PROFILE_IMAGE)
    val profileImage: String? = null,
)