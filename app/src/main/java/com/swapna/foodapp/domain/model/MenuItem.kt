package com.swapna.foodapp.domain.model

data class MenuItem(
    val id: String,
    val restaurantId: String,
    val name: String,
    val description: String,
    val price: Double,
    val imageUrl: String,
    val category: String,
    val isVeg: Boolean,
    val isRecommended: Boolean = false,
    val customisations: List<Customisation> = emptyList(),
    val isBestseller:   Boolean               = false,
    val isAvailable:    Boolean               = true,
)

data class Customisation(
    val id: String,
    val name: String,
    val options: List<CustomisationOption>,
)

data class CustomisationOption(
    val id: String,
    val label: String,
    val extraPrice: Double,
)

data class MenuCategory(
    val id:    String,
    val name:  String,
    val items: List<MenuItem>,   // ← uses YOUR existing MenuItem
) {
    val itemCount: Int get() = items.size
}
