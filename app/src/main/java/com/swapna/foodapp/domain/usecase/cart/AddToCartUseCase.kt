package com.swapna.foodapp.domain.usecase.cart

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.utils.AppConstants
import java.util.UUID
import javax.inject.Inject

class AddToCartUseCase @Inject constructor(
    private val cartRepository: CartRepository,
) {
    suspend operator fun invoke(
        menuItem: MenuItem,
        quantity: Int,
        customisations: List<CustomisationOption> = emptyList(),
    ) {
        require(quantity >= AppConstants.MIN_CART_QUANTITY) {
            "Quantity must be at least ${AppConstants.MIN_CART_QUANTITY}"
        }
        require(quantity <= AppConstants.MAX_CART_QUANTITY) {
            "Quantity cannot exceed ${AppConstants.MAX_CART_QUANTITY}"
        }

        val cartItem = CartItem(
            id                     = UUID.randomUUID().toString(),
            menuItem               = menuItem,
            quantity               = quantity,
            selectedCustomisations = customisations,
        )
        cartRepository.addItem(cartItem)
    }
}