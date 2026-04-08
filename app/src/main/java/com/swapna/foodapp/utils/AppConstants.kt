package com.swapna.foodapp.utils

object AppConstants {

    // ══════════════════════════════════════════════════════════
    // NETWORK
    // ══════════════════════════════════════════════════════════

    // Timeout durations
    const val CONNECT_TIMEOUT_SEC  = 30L
    const val READ_TIMEOUT_SEC     = 30L
    const val WRITE_TIMEOUT_SEC    = 30L

    // Cache config
    // Data older than this triggers a background refresh
    const val CACHE_DURATION_MIN   = 15

    // Set in build.gradle.kts as BuildConfig.BASE_URL
    const val BASE_URL_PLACEHOLDER = "https://raw.githubusercontent.com/SwapnaPrakash/zomato-mock-api/main/"

    // ══════════════════════════════════════════════════════════
    // DATABASE
    // ══════════════════════════════════════════════════════════

    const val DB_NAME     = "food_app.db"
    const val DB_VERSION  = 1

    // ══════════════════════════════════════════════════════════
    // CART BUSINESS RULES
    // ══════════════════════════════════════════════════════════

    const val MIN_CART_QUANTITY      = 1
    const val MAX_CART_QUANTITY      = 20
    const val DEFAULT_DELIVERY_FEE   = 30.0      // ₹30 flat delivery
    const val FREE_DELIVERY_THRESHOLD = 199.0    // Free delivery above ₹199
    const val GST_RATE               = 0.05      // 5% GST on food orders
    const val PLATFORM_FEE           = 5.0       // ₹5 platform fee

    // ══════════════════════════════════════════════════════════
    // PRICE FORMATTING
    // ══════════════════════════════════════════════════════════

    const val CURRENCY_SYMBOL        = "₹"
    const val PRICE_DECIMAL_PLACES   = 0        // Show ₹249, not ₹249.00

    // ══════════════════════════════════════════════════════════
    // SEARCH
    // ══════════════════════════════════════════════════════════

    const val SEARCH_DEBOUNCE_MS     = 300L     // ms to wait after last keystroke
    const val SEARCH_MIN_CHARS       = 2        // min chars before triggering search
    const val SEARCH_MAX_RESULTS     = 20       // max results from API per page
    const val CATEGORY_CHIPS_MAX     = 5        // max cuisine chips shown in row

    // ══════════════════════════════════════════════════════════
    // AUTH — OTP
    // ══════════════════════════════════════════════════════════

    const val OTP_LENGTH             = 6
    const val OTP_TIMEOUT_SEC        = 60       // OTP expires after 60 seconds
    const val PHONE_LENGTH           = 10       // Indian mobile numbers
    const val PHONE_COUNTRY_CODE     = "+91"

    // ══════════════════════════════════════════════════════════
    // SPLASH SCREEN
    // ══════════════════════════════════════════════════════════

    const val SPLASH_DELAY_MS        = 1500L    // 1.5 seconds

    // ══════════════════════════════════════════════════════════
    // HOME SCREEN
    // ══════════════════════════════════════════════════════════

    const val DEFAULT_LOCATION       = "Koramangala, Bengaluru"
    const val MAX_OFFER_CARDS        = 5        // max banners shown
    const val MAX_RECOMMENDED_ITEMS  = 5        // max recommended dishes

    // ══════════════════════════════════════════════════════════
    // RESTAURANT / MENU
    // ══════════════════════════════════════════════════════════

    const val DEFAULT_DELIVERY_TIME  = 30       // minutes fallback if API null
    const val MAX_REVIEWS_SHOWN      = 5        // free tier limit from API

    // ══════════════════════════════════════════════════════════
    // RATING THRESHOLDS
    // ══════════════════════════════════════════════════════════

    const val RATING_EXCELLENT       = 4.5
    const val RATING_VERY_GOOD       = 4.0
    const val RATING_GOOD            = 3.5
    const val RATING_AVERAGE         = 3.0

    // ══════════════════════════════════════════════════════════
    // PAGINATION
    // ══════════════════════════════════════════════════════════

    const val PAGE_SIZE              = 20
    const val PAGE_START             = 0

    // ══════════════════════════════════════════════════════════
    // DATASTORE / SHARED PREFS KEYS
    // ══════════════════════════════════════════════════════════

    const val KEY_AUTH_TOKEN         = "auth_token"
    const val KEY_USER_ID            = "user_id"
    const val KEY_IS_LOGGED_IN       = "is_logged_in"
    const val KEY_LAST_LOCATION      = "last_location"
    const val KEY_ONBOARDING_DONE    = "onboarding_done"
}