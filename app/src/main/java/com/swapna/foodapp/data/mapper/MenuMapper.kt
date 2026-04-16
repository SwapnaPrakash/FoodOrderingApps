package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.DailyMenuResponse
import com.swapna.foodapp.data.remote.dto.DishDto
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.utils.extensions.parsePriceString
import javax.inject.Inject

class MenuMapper @Inject constructor() {

    // Converts full menu response → Map<CategoryName, List<MenuItem>>
    // e.g. { "Biryani" → [ChickenBiryani, MuttonBiryani], "Starters" → [...] }
    fun toDomain(
        response: DailyMenuResponse,
        restaurantId: String,
    ): Map<String, List<MenuItem>> {
        return response.dailyMenus
            .associate { wrapper ->
                val category = wrapper.menu.name
                val items = wrapper.menu.dishes.map { dishWrapper ->
                    mapDish(dishWrapper.dish, restaurantId, category)
                }
                category to items
            } .filter { (_, items) -> items.isNotEmpty() }
    }

    private fun mapDish(
        dto: DishDto,
        restaurantId: String,
        category: String,
    ): MenuItem = MenuItem(
        id = dto.id,
        restaurantId = restaurantId,
        name = dto.name,
        description = dto.description,

        // "249 Rs." → 249.0
        price = dto.price.parsePriceString(),

        imageUrl = dto.imageUrl ?: "",
        category = category,

        // Int 1/0 → Boolean
        isVeg = dto.isVeg == 1,
        isRecommended = dto.isRecommended == 1,
        isBestseller  = false,
        isAvailable   = true,

        customisations = dto.customisations?.map { c ->
            Customisation(
                id = c.id,
                name = c.name,
                options = c.options.map { o ->
                    CustomisationOption(
                        id = o.id,
                        label = o.label,
                        extraPrice = o.extraPrice,
                    )
                },
            )
        } ?: emptyList(),
    )
}
private fun String.parsePriceString(): Double =
    this.replace("[^0-9.]".toRegex(), "")
        .toDoubleOrNull() ?: 0.0