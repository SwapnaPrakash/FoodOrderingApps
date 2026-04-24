package com.swapna.foodapp.domain.usecase.cart

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.ERROR_MAX_QUANTITY
import com.swapna.foodapp.utils.AppConstants.ERROR_MIN_QUANTITY
import java.util.UUID
import javax.inject.Inject

class AddToCartUseCaseImpl @Inject constructor(
    private val cartRepository: CartRepository,
) : AddToCartUseCase {

    override suspend operator fun invoke(
        menuItem: MenuItem,
        quantity: Int,
        customisations: List<CustomisationOption>,
    ) {
        require(quantity >= AppConstants.MIN_CART_QUANTITY) {
            "$ERROR_MIN_QUANTITY ${AppConstants.MIN_CART_QUANTITY}"
        }
        require(quantity <= AppConstants.MAX_CART_QUANTITY) {
            "$ERROR_MAX_QUANTITY ${AppConstants.MAX_CART_QUANTITY}"
        }

        val cartItem = CartItem(
            id = UUID.randomUUID().toString(),
            menuItem = menuItem,
            quantity = quantity,
            selectedCustomisations = customisations,
        )
        cartRepository.addItem(cartItem)
    }
}
