package com.swapna.foodapp.domain.model

// Domain-level business rule constants
// Pure Kotlin — no Android imports
// Used by UseCases and domain models

object AppBusinessRules {

    const val MIN_CART_QUANTITY      = 1
    const val MAX_CART_QUANTITY      = 20
    const val DEFAULT_DELIVERY_FEE   = 30.0      // ₹30 flat
    const val FREE_DELIVERY_ABOVE    = 199.0     // free delivery if cart > ₹199
    const val GST_RATE               = 0.05      // 5% GST
    const val PLATFORM_FEE           = 5.0       // ₹5 platform fee

    const val RATING_EXCELLENT       = 4.5
    const val RATING_VERY_GOOD       = 4.0
    const val RATING_GOOD            = 3.5
    const val RATING_AVERAGE         = 3.0

    const val DELIVERY_FAST_MAX_MIN  = 20        // "Under 20 min" fast bucket
    const val DELIVERY_NORMAL_MAX_MIN = 30       // "Under 30 min" normal bucket
    const val DELIVERY_SLOW_MAX_MIN  = 45        // "Under 45 min" slow bucket
    const val DELIVERY_DEFAULT_MIN   = 30        // fallback when API returns null

    const val SEARCH_MIN_CHARS       = 2
    const val SEARCH_DEBOUNCE_MS     = 300L
    const val MAX_RESULTS_PER_PAGE   = 20

    const val OTP_LENGTH             = 6
    const val OTP_TIMEOUT_SEC        = 60
    const val PHONE_LENGTH           = 10
}