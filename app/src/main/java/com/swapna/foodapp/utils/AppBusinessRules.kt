package com.swapna.foodapp.utils

object AppBusinessRules {

    // GST = Goods and Services Tax on food
    const val GST_RATE = 0.05  // 5%

    // Free delivery when order above this
    const val FREE_DELIVERY_ABOVE = 500.0   // ₹500

    // Default delivery fee when below threshold
    const val DEFAULT_DELIVERY_FEE = 30.0   // ₹30

    // Max quantity of single item in cart
    const val MAX_ITEM_QUANTITY = 10

    // Minimum order to checkout
    const val MIN_ORDER_VALUE = 100.0       // ₹100
    const val FREE_DELIVERY_FEE = 0.0
}