package com.swapna.foodapp.domain.model

import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.FOR_TWO
import com.swapna.foodapp.utils.AppConstants.MINUTES

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
    val phoneNumber: String = "",
    val openingHours: String = AppConstants.TIME,
    val highlights: List<String> = emptyList(),
    val knownFor: String = "",
) {
    val costForTwoFormatted: String
        get() = "₹$avgCostForTwo $FOR_TWO"

    val deliveryTimeFormatted: String
        get() = "$avgDeliveryTime $MINUTES"

    val ratingFormatted: String
        get() = String.format("%.1f", rating)
}