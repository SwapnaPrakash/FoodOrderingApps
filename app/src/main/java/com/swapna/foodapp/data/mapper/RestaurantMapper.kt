package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.RestaurantDto
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.utils.AppConstants.DEFAULT_DELIVERY_FEE
import com.swapna.foodapp.utils.AppConstants.DEFAULT_DELIVERY_TIME
import com.swapna.foodapp.utils.AppConstants.TIME
import javax.inject.Inject

class RestaurantMapper @Inject constructor() {

    fun toDomain(dto: RestaurantDto): Restaurant = Restaurant(
        id = dto.id,
        name = dto.name,
        imageUrl = dto.featuredImage.ifEmpty { dto.thumb },
        thumbUrl = dto.thumb,
        rating = dto.rating.rating.toDoubleOrNull() ?: 0.0,
        ratingText = dto.rating.text,
        ratingColor = dto.rating.color,
        totalVotes = dto.rating.votes
            .replace(",", "")
            .toIntOrNull() ?: 0,
        avgDeliveryTime = dto.deliveryTime ?: DEFAULT_DELIVERY_TIME,
        deliveryFee = DEFAULT_DELIVERY_FEE,
        minOrder = dto.minOrder ?: 0,
        cuisines = dto.cuisines
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() },

        address = dto.location.address,
        locality = dto.location.locality,
        isOpen = dto.isDeliveringNow == 1,
        hasDelivery = dto.hasDelivery == 1,
        offers = dto.offers ?: emptyList(),
        avgCostForTwo = dto.avgCostForTwo,
        distanceKm = 0.0,
        phoneNumber = "",
        openingHours = TIME,
        highlights = emptyList(),
        knownFor = "",
    )
}