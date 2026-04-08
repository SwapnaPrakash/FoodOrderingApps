package com.swapna.foodapp.domain.usecase.cart

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.CartPriceBreakdown
import javax.inject.Inject

class CalculateCartTotalUseCase @Inject constructor() {
    operator fun invoke(items: List<CartItem>): CartPriceBreakdown {
        val subtotal    = items.sumOf { it.totalPrice }
        val deliveryFee = if (subtotal > 0) AppConstants.DEFAULT_DELIVERY_FEE else 0.0
        val taxes       = subtotal * AppConstants.GST_RATE
        val total       = subtotal + deliveryFee + taxes
        return CartPriceBreakdown(subtotal, deliveryFee, taxes, total)
    }
}