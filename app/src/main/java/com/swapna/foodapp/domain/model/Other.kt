package com.swapna.foodapp.domain.model

data class FoodCategory(
    val id: Int,
    val name: String,
    val imageUrl: String,
)

data class Collections(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val restaurantCount: Int,
    val discount: String = "",
)

data class Cuisine(
    val id: Int,
    val name: String,
)

data class Review(
    val id: Long,
    val rating: Int,
    val text: String,
    val timeAgo: String,
    val userName: String,
    val userImage: String,
)

data class SearchFilters(
    val cuisineId: Int? = null,
    val minRating: Double? = null,
    val maxDeliveryTime: Int? = null,
    val isVegOnly: Boolean = false,
    val sortBy: SortOption = SortOption.RELEVANCE,
)

enum class SortOption {
    RELEVANCE, RATING, DELIVERY_TIME, COST_LOW, COST_HIGH
}