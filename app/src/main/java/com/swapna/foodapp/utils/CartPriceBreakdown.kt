package com.swapna.foodapp.utils

data class CartPriceBreakdown(
    val subtotal: Double,
    val deliveryFee: Double,
    val taxes: Double,
    val total: Double,
)