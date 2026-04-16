package com.swapna.foodapp.domain.model

object AppBusinessRules {

    const val MIN_CART_QUANTITY      = 1
    const val MAX_CART_QUANTITY      = 20
    const val DEFAULT_DELIVERY_FEE   = 30.0
    const val FREE_DELIVERY_ABOVE    = 199.0
    const val GST_RATE               = 0.05
    const val PLATFORM_FEE           = 5.0

    const val RATING_EXCELLENT       = 4.5
    const val RATING_VERY_GOOD       = 4.0
    const val RATING_GOOD            = 3.5
    const val RATING_AVERAGE         = 3.0

    const val DELIVERY_FAST_MAX_MIN  = 20
    const val DELIVERY_NORMAL_MAX_MIN = 30
    const val DELIVERY_SLOW_MAX_MIN  = 45
    const val DELIVERY_DEFAULT_MIN   = 30

    const val SEARCH_MIN_CHARS       = 2
    const val SEARCH_DEBOUNCE_MS     = 300L
    const val MAX_RESULTS_PER_PAGE   = 20

    const val OTP_LENGTH             = 6
    const val OTP_TIMEOUT_SEC        = 60
    const val PHONE_LENGTH           = 10

    const val MAX_RECOMMENDED_SHOWN   = 5
    const val MIN_VOTES_FOR_RATING    = 10   // show rating only if votes > this
    const val COST_FOR_TWO_CHEAP      = 300  // ₹ = "₹" price range
    const val COST_FOR_TWO_MODERATE   = 600  // ₹₹
    const val COST_FOR_TWO_EXPENSIVE  = 1000 // ₹₹₹

    const val MENU_ITEM_DESC_MAX_LINES = 2
    const val MENU_CATEGORY_MIN_ITEMS  = 1
}