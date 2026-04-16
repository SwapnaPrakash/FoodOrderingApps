package com.swapna.foodapp.utils


// WHY object not class?
// object = singleton = one instance ever
// All constants live in ONE place
// No magic numbers anywhere in codebase

// WHY not companion object inside a class?
// AppBusinessRules is standalone utility
// No need to tie it to any specific class
// Import directly wherever needed

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

    // Rating display thresholds
    const val RATING_EXCELLENT = 4.5
    const val RATING_VERY_GOOD = 4.0
    const val RATING_GOOD      = 3.5
}