package com.swapna.foodapp.domain.model

data class Restaurant(
    val id: String,
    val name: String,
    val imageUrl: String,
    val thumbUrl: String,
    val rating: Double,
    val ratingText: String,
    val ratingColor: String,
    val totalVotes: Int,
    val avgDeliveryTime: Int,
    val deliveryFee: Double,
    val minOrder: Int,
    val cuisines: List<String>,
    val address: String,
    val locality: String,
    val distanceKm: Double = 0.0,
    val isOpen: Boolean,
    val hasDelivery: Boolean,
    val offers: List<String> = emptyList(),
    val avgCostForTwo: Int = 0,
    val phoneNumber: String     = "",
    val openingHours: String    = "11 AM - 11 PM",
    val highlights: List<String> = emptyList(),
    val knownFor: String        = "",
) {

    // Computed — used on restaurant screen info card
    val costForTwoFormatted: String
        get() = "₹$avgCostForTwo for two"

    val priceRange: String
        get() = when {
            avgCostForTwo <= AppBusinessRules.COST_FOR_TWO_CHEAP    -> "₹"
            avgCostForTwo <= AppBusinessRules.COST_FOR_TWO_MODERATE -> "₹₹"
            else                                                     -> "₹₹₹"
        }

    val deliveryTimeFormatted: String
        get() = "$avgDeliveryTime min"

    val ratingFormatted: String
        get() = String.format("%.1f", rating)
}