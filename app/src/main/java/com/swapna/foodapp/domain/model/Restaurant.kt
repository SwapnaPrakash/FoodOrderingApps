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
)