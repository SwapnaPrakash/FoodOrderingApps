package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.RestaurantDto
import com.swapna.foodapp.domain.model.Restaurant
import javax.inject.Inject

class RestaurantMapper @Inject constructor() {

    fun toDomain(dto: RestaurantDto): Restaurant = Restaurant(
        id = dto.id,
        name = dto.name,
        imageUrl = dto.featuredImage.ifEmpty { dto.thumb },
        thumbUrl = dto.thumb,

        // "4.3" String → Double safely
        rating = dto.rating.rating.toDoubleOrNull() ?: 0.0,
        ratingText = dto.rating.text,
        ratingColor = dto.rating.color,

        // "12,547" String → strip comma → Int
        totalVotes = dto.rating.votes
            .replace(",", "")
            .toIntOrNull() ?: 0,

        avgDeliveryTime = dto.deliveryTime ?: 30,
        deliveryFee = 30.0,          // not in API — use default
        minOrder = dto.minOrder ?: 0,

        // "Biryani, North Indian" → ["Biryani", "North Indian"]
        cuisines = dto.cuisines
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() },

        address = dto.location.address,
        locality = dto.location.locality,

        // Int 1/0 → Boolean
        isOpen = dto.isDeliveringNow == 1,
        hasDelivery = dto.hasDelivery == 1,

        offers = dto.offers ?: emptyList(),
        avgCostForTwo = dto.avgCostForTwo,
    )
}