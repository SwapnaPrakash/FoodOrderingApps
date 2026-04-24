package com.swapna.foodapp.domain.model

data class CartItem(
    val id: String,
    val menuItem: MenuItem,
    val quantity: Int,
    val selectedCustomisations: List<CustomisationOption> = emptyList(),
) {
    val totalPrice: Double
        get() {
            val extras = selectedCustomisations.sumOf { it.extraPrice }
            return (menuItem.price + extras) * quantity
        }

    fun withQuantity(qty: Int) = copy(quantity = qty)
}

data class CartPriceBreakdown(
    val subtotal: Double,
    val deliveryFee: Double,
    val taxes: Double,
    val total: Double,
)