package com.swapna.foodapp.utils

object AppConstants {
    const val CONNECT_TIMEOUT_SEC = 30L
    const val READ_TIMEOUT_SEC = 30L
    const val CACHE_DURATION_MIN = 15
    const val BASE_URL_PLACEHOLDER =
        "https://raw.githubusercontent.com/SwapnaPrakash/zomato-mock-api/main/"
    const val DB_NAME = "food_app.db"
    const val DB_VERSION = 3
    const val APP_NAME = "FoodApp"
    const val TAG_NAME = "Order food you love"
    const val MIN_CART_QUANTITY = 1
    const val MAX_CART_QUANTITY = 20
    const val DEFAULT_DELIVERY_FEE = 30.0
    const val GST_RATE = 0.05
    const val LOGIN = "Login"
    const val CART = "Cart"
    const val PROFILE = "Profile"
    const val RESTAURANT = "Restaurant"
    const val KEY_CATEGORIES = "categories"
    const val KEY_CATEGORY = "categories"
    const val KEY_CATEGORY_ID = "id"
    const val KEY_CATEGORY_NAME = "name"
    const val KEY_CATEGORY_IMAGE = "image_url"
    const val KEY_CUISINES = "cuisines"
    const val KEY_CUISINE = "cuisine"
    const val KEY_CUISINE_ID = "cuisine_id"
    const val KEY_CUISINE_NAME = "cuisine_name"
    const val KEY_ESTABLISHMENTS = "establishments"
    const val KEY_ESTABLISHMENT = "establishment"
    const val KEY_ESTABLISHMENT_ID = "id"
    const val KEY_ESTABLISHMENT_NAME = "name"
    const val KEY_LOCATION_SUGGESTIONS = "location_suggestions"
    const val KEY_ENTITY_ID = "entity_id"
    const val KEY_ENTITY_TYPE = "entity_type"
    const val KEY_TITLE = "title"
    const val KEY_LATITUDE = "latitude"
    const val KEY_LONGITUDE = "longitude"
    const val KEY_CITY_ID = "city_id"
    const val KEY_CITY_NAME = "city_name"
    const val SEARCH_FAILED = "Search failed"
    const val RESULTS = "results"
    const val FILTER_ACTIVE = "filter(s) active  •  "
    const val RESTAURANT_FOUND = "restaurants found"
    const val RESTAURANT_SEARCH = "Search for restaurants,\ncuisines or dishes"
    const val RESTAURANT_SEARCH_CUISINE = "Search restaurants, cuisines..."
    const val ENTER_MOB_NUM = "Enter your mobile number to continue"
    const val PHONE_NUMBER = "Phone Number"
    const val PHONE = "Phone"
    const val PHONE_DIG = "+91  "
    const val PHONE_OTP = "Enter 6-digit OTP"
    const val RESEND_OTP = "Resend OTP"
    const val SENT_OTP = "OTP sent to +91 "
    const val VERIFY_OTP = "Verify OTP"
    const val SEND_OTP = "Send OTP"
    const val OTP_LENGTH = 6
    const val PHONE_LENGTH = 10
    const val KEY_RATING = "rating"
    const val PHONE_COUNTRY_CODE = "+91"
    const val DELIVERY = "delivery"
    const val DINING = "Dining"
    const val VEG_ONLY = "Veg only"
    const val VEG = "Veg"
    const val NON_VEG = "Non-veg"
    const val RELEVANCE = "Relevance"
    const val RATING = "Rating"
    const val DELIVERY_TIME = "Delivery Time"
    const val COST_LOW = "Cost: Low–High"
    const val COST_HIGH = "Cost: High–Low"
    const val FILTERS_TITLE = "Filters"
    const val ACTIVE = "active"
    const val FILTER_DIETARY = "Dietary"
    const val FILTER_MIN_RATING = "Minimum Rating"
    const val FILTER_DELIVERY_TIME = "Delivery Time"
    const val FILTER_SORT_BY = "Sort By"
    const val FILTER_CUISINES = "Cuisines"
    const val DELIVERY_UNDER_20 = "Under 20 min"
    const val DELIVERY_UNDER_30 = "Under 30 min"
    const val DELIVERY_UNDER_45 = "Under 45 min"
    const val CLEAR_ALL = "Clear All"
    const val APPLY = "Apply"
    const val SELECTED = "Selected"
    const val NO_RESULTS = "No results"
    const val WRONG = "Something went wrong"
    const val TRY_AGAIN = "Try Again"
    const val ERROR = "Error"
    const val DELIVERING_TO = "Delivering to"
    const val CHANGE_LOCATION = "Change location"
    const val DEFAULT_LOCATION = "Koramangala, Bengaluru"
    const val KEY_COLLECTIONS = "collections"
    const val KEY_COLLECTION = "collection"
    const val KEY_COLLECTION_ID = "collection_id"
    const val KEY_COLLECTION_TITLE = "title"
    const val KEY_COLLECTION_DESCRIPTION = "description"
    const val KEY_COLLECTION_IMAGE = "image_url"
    const val KEY_COLLECTION_RES_COUNT = "res_count"
    const val KEY_COLLECTION_DISCOUNT = "discount"
    const val SEARCH_DEBOUNCE_MS = 300L
    const val SEARCH_MIN_CHARS = 2
    const val SPLASH_DELAY_MS = 1500L
    const val KEY_DAILY_MENUS = "daily_menus"
    const val KEY_DAILY_MENU = "daily_menu"
    const val KEY_DAILY_MENU_ID = "daily_menu_id"
    const val KEY_DISHES = "dishes"
    const val KEY_DISH = "dish"
    const val KEY_DISH_ID = "dish_id"
    const val KEY_NAME = "name"
    const val KEY_PRICE = "price"
    const val KEY_DESCRIPTION = "description"
    const val KEY_IMAGE_URL = "image_url"
    const val KEY_IS_VEG = "is_veg"
    const val KEY_IS_RECOMMENDED = "is_recommended"
    const val KEY_CUSTOMISATIONS = "customisations"
    const val KEY_OPTIONS = "options"
    const val KEY_LABEL = "label"
    const val KEY_EXTRA_PRICE = "extra_price"
    const val KEY_USER = "user"
    const val KEY_EMAIL = "email"
    const val KEY_PHONE = "phone"
    const val KEY_PROFILE_IMAGE = "profile_image"
    const val KEY_ADDRESSES = "addresses"
    const val KEY_FULL_ADDRESS = "full_address"
    const val KEY_LANDMARK = "landmark"
    const val KEY_ORDERS = "orders"
    const val KEY_ORDER = "order"
    const val KEY_RESTAURANT_ID = "restaurant_id"
    const val KEY_RESTAURANT_NAME = "restaurant_name"
    const val KEY_RESTAURANT_IMAGE = "restaurant_image"
    const val KEY_STATUS = "status"
    const val KEY_TIME_FRIENDLY = "time_friendly"
    const val KEY_TOTAL_AMOUNT = "total_amount"
    const val KEY_ITEMS = "items"
    const val KEY_QUANTITY = "quantity"
    const val KEY_CAN_REORDER = "can_reorder"
    const val KEY_REVIEWS_COUNT = "reviews_count"
    const val KEY_USER_REVIEWS = "user_reviews"
    const val KEY_REVIEW = "review"
    const val KEY_REVIEW_TEXT = "review_text"
    const val KEY_REVIEW_TIME = "review_time_friendly"
    const val NO_INTERNET = "No internet. Please check your connection."
    const val COULD_NOT_LOAD_MENU = "Could not load menu."
    const val KEY_LOCATION = "location"
    const val KEY_NEARBY_RESTAURANTS = "nearby_restaurants"
    const val KEY_RESTAURANT = "restaurant"
    const val KEY_RESTAURANTS = "restaurants"
    const val KEY_RESULTS_FOUND = "results_found"
    const val KEY_RESULTS_SHOWN = "results_shown"
    const val KEY_ID = "id"
    const val KEY_FEATURED_IMAGE = "featured_image"
    const val KEY_THUMB = "thumb"
    const val KEY_AVG_COST_FOR_TWO = "average_cost_for_two"
    const val KEY_PRICE_RANGE = "price_range"
    const val KEY_CURRENCY = "currency"
    const val KEY_USER_RATING = "user_rating"
    const val KEY_AGGREGATE_RATING = "aggregate_rating"
    const val KEY_RATING_TEXT = "rating_text"
    const val KEY_RATING_COLOR = "rating_color"
    const val KEY_VOTES = "votes"
    const val KEY_HAS_DELIVERY = "has_online_delivery"
    const val KEY_IS_DELIVERING_NOW = "is_delivering_now"
    const val KEY_DELIVERY_TIME = "estimated_delivery_time"
    const val KEY_MIN_ORDER = "minimum_order"
    const val KEY_OFFERS = "offers"
    const val KEY_ADDRESS = "address"
    const val KEY_LOCALITY = "locality"
    const val KEY_CITY = "city"
    const val KEY_ZIPCODE = "zipcode"
    const val NO_INTERNET_LOAD_RESTAURANT =
        "Could not load restaurant. Check your connection."
    const val URI_FALL_BACK =
        "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=200&h=200&fit=crop"
    const val TABLE_CART_ITEMS = "cart_items"
    const val COL_ID = "id"
    const val COL_MENU_ITEM_ID = "menu_item_id"
    const val COL_MENU_ITEM_JSON = "menu_item_json"
    const val COL_QUANTITY = "quantity"
    const val COL_CUSTOMISATIONS_JSON = "customisations_json"
    const val COL_ADDED_AT = "added_at"
    const val OFFERS_TITLE = "Exciting Offers 🔥"
    const val CATEGORIES_TITLE = "What's on your mind?"
    const val CURRENCY_SYMBOL = "₹"
    const val PRICE_DECIMAL_PLACES = 0
    const val ERROR_INVALID_PHONE = "Enter a valid 10-digit phone number"
    const val ERROR_SEND_OTP_FAILED = "Failed to send OTP"
    const val ERROR_INVALID_OTP = "Enter a %d-digit OTP"
    const val ERROR_INCORRECT_OTP = "Incorrect OTP. Try again."
    const val LOCATION_ICON_DESC = "Location"
    const val GO_TO_CART = "Go to cart"
    const val SEARCH_HINT = "Search for restaurants and food"
    const val SEARCH_ICON_DESC = "Search"
    const val CURRENT_LOCATION_DESC = "Current location"
    const val ENABLE_LOCATION_PERMISSION = "Enable location permission"
    const val SAVED_ADDRESSES = "SAVED ADDRESSES"
    const val POPULAR_LOCATIONS = "POPULAR LOCATIONS"
    const val WORK = "work"
    const val LOC_KORAMANGALA = "Koramangala, Bengaluru"
    const val LOC_KORAMANGALA_SUB = "5th Block, Bengaluru - 560095"
    const val LOC_INDIRANAGAR = "Indiranagar, Bengaluru"
    const val LOC_INDIRANAGAR_SUB = "100 Feet Road, Bengaluru - 560038"
    const val LOC_HSR = "HSR Layout, Bengaluru"
    const val LOC_HSR_SUB = "Sector 7, Bengaluru - 560102"
    const val LOC_WHITEFIELD = "Whitefield, Bengaluru"
    const val LOC_WHITEFIELD_SUB = "ITPL Main Road, Bengaluru - 560066"
    const val LOC_ECITY = "Electronic City, Bengaluru"
    const val LOC_ECITY_SUB = "Phase 1, Bengaluru - 560100"
    const val ERROR_MIN_QUANTITY = "Quantity must be at least %d"
    const val ERROR_MAX_QUANTITY = "Quantity cannot exceed %d"
    const val TABLE_USER = "user"
    const val COL_EMAIL = "email"
    const val COL_PHONE = "phone"
    const val COL_PROFILE_IMAGE = "profile_image"
    const val COL_ADDRESSES_JSON = "addresses_json"
    const val TABLE_RESTAURANTS = "restaurants"
    const val COL_THUMB_URL = "thumb_url"
    const val COL_RATING = "rating"
    const val COL_RATING_TEXT = "rating_text"
    const val COL_RATING_COLOR = "rating_color"
    const val COL_TOTAL_VOTES = "total_votes"
    const val COL_AVG_DELIVERY_TIME = "avg_delivery_time"
    const val COL_DELIVERY_FEE = "delivery_fee"
    const val COL_MIN_ORDER = "min_order"
    const val COL_CUISINES_JSON = "cuisines_json"
    const val COL_ADDRESS = "address"
    const val COL_LOCALITY = "locality"
    const val COL_IS_OPEN = "is_open"
    const val COL_HAS_DELIVERY = "has_delivery"
    const val COL_OFFERS_JSON = "offers_json"
    const val COL_AVG_COST_FOR_TWO = "avg_cost_for_two"
    const val COL_NAME = "name"
    const val COL_DESCRIPTION = "description"
    const val COL_PRICE = "price"
    const val COL_IMAGE_URL = "image_url"
    const val COL_IS_VEG = "is_veg"
    const val COL_IS_RECOMMENDED = "is_recommended"
    const val TABLE_MENU_ITEMS = "menu_items"
    const val COL_RESTAURANT_ID = "restaurant_id"
    const val COL_CATEGORY = "category"
    const val COL_CACHED_AT = "cached_at"
    const val COL_SELECTED_LOCATION = "selected_location"
    const val SOMETHING_WRONG = "Something went wrong"
    const val STORE_NEAR = "Stores Near You"
    const val RETRY = "Retry button"
    const val SHIMMER = "shimmer"
    const val SHIMMER_X = "shimmer_x"
    const val BACK = "Back"
    const val CLEAR = "Clear"
    const val RESTAURANT_CHECK =
        "Try a different spelling or check\nfor nearby restaurants"
    const val DIGIT_OTP = " -digit OTP"
    const val OTP_SEND_FAILED = "OTP sending failed"
    const val FIREBASE_ERROR = "Firebase error"
    const val SESSION_EXPIRED = "Session expired. Tap Send OTP again."
    const val LOGIN_FAILED = "Login failed."
    const val WRONG_OTP = "Wrong OTP. Please check and try again."
    const val VERIFY_FAILED = "Verification failed"
    const val OFFLINE = "You're offline — showing cached data"
    const val DEFAULT_DELIVERY_TIME  = 30
    const val MAX_REVIEWS_SHOWN      = 5
    const val MENU_STICKY_OFFSET     = 56
    const val RESTAURANT_HERO_ALPHA  = 0.6f
    const val QUICK_ADD_ANIMATION_MS = 200L
    const val SEARCH_MAX_RESULTS     = 20
    const val CATEGORY_CHIPS_MAX     = 5
    const val MAX_OFFER_CARDS        = 5
    const val MAX_RECOMMENDED_ITEMS  = 5
    const val RATING_EXCELLENT       = 4.5
    const val RATING_VERY_GOOD       = 4.0
    const val RATING_GOOD            = 3.5
    const val RATING_AVERAGE         = 3.0
    const val RECOMMENDED_CARD_MAX    = 5
    const val RECOMMENDED_SECTION_KEY = "recommended_section"
    const val MENU_TAB_KEY            = "menu_tab_row"
    const val CATEGORY_FOOTER_KEY     = "category_footer"
    const val CD_SEARCH_LOCATION    = "Search for a delivery location"
    const val CD_CURRENT_LOCATION   = "Use device current location"
    const val CD_LOCATION_PIN       = "Location pin"
    const val CD_CLOSE_SHEET        = "Close location picker"
    const val COL_IS_BESTSELLER     = "is_bestseller"
    const val COL_IS_AVAILABLE      = "is_available"
    const val COL_DISTANCE_KM       = "distance_km"
    const val COL_PHONE_NUMBER      = "phone_number"
    const val COL_OPENING_HOURS     = "opening_hours"
    const val COL_HIGHLIGHTS_JSON   = "highlights_json"
    const val COL_KNOWN_FOR         = "known_for"
    const val REVIEWS_MAX_SHOWN     = 10
    const val REVIEW_TEXT_MAX_LINES = 3

    // ══════════════════════════════════════════════════════════
    // SharedFlow Buffer Constants
    // WHY constants not hardcoded numbers?
    // Single place to tune all buffer sizes across all ViewModels
    // Change here → all ViewModels updated automatically
    // ══════════════════════════════════════════════════════════

    // Navigation events — buffer 10, overflow = SUSPEND
    // WHY SUSPEND? Navigation must NEVER be dropped
    //   Dropped NavigateBack = user stuck on screen = worst UX bug
    //   SUSPEND = ViewModel coroutine waits → event never lost
    // WHY 10? Navigation events are rare (1 per user tap)
    //   Buffer 10 = handles 10 rapid taps without blocking
    const val EVENT_BUFFER_NAVIGATION = 10

    // UI feedback events — buffer 3, overflow = DROP_OLDEST
    // WHY DROP_OLDEST? Snackbar/error messages are informational
    //   Latest message is most relevant to user
    //   Older queued snackbars are stale → safe to drop
    // WHY 3? Max 3 queued messages makes UX sense
    const val EVENT_BUFFER_UI = 3

    // General/mixed events — buffer 5, overflow = SUSPEND
    // WHY SUSPEND? Safer default when event criticality is unclear
    //   Better to wait briefly than silently lose an event
    const val EVENT_BUFFER_DEFAULT = 5
    const val ERR_CART_EMPTY          = "Cart is empty"
    const val ERR_FAILED_UPDATE_ITEM  = "Failed to update item"
    const val ERR_FAILED_REMOVE_ITEM  = "Failed to remove item"
    const val ERR_FAILED_PLACE_ORDER  = "Failed to place order"
    const val MSG_ITEM_REMOVED        = " removed"
    const val MSG_ITEM_REMOVED_CART   = " removed from cart"
    const val ERR_MIN_ORDER           = "Minimum order is ₹"
    const val ERR_LOCATION_PERMISSION =
        "Location permission denied. Please enable in Settings."
    const val ERR_GPS_DISABLED        =
        "GPS is turned off. Please enable location."
    const val ERR_LOCATION_DETECT     =
        "Could not detect location. Please pick manually."
    const val ERR_LOCATION_PERMISSION_DENIED =
        "Location permission denied. Pick an area below."
    const val PERMISSION = "permission"
    const val DISABLED = "disabled"
    const val ARG_RESTAURANT_ID_REQUIRED = "restaurantId required"
    const val ARG_MENU_ITEM_ID_REQUIRED  = "menuItemId required"
    const val ERR_ITEM_NOT_FOUND         = "Item not found"
    const val ERR_FAILED_LOAD_ITEM       = "Failed to load item"
    const val ERR_FAILED_ADD_CART        = "Failed to add to cart"
    const val MSG_ADDED_TO_CART          = " added to cart 🛒"
    const val ERR_NO_USER_FOUND          = "No user found. Please login again."
    const val ERR_NAME_EMPTY             = "Name cannot be empty"
    const val ERR_FAILED_UPDATE_PROFILE  = "Failed to update profile"
    const val ERR_FAILED_REMOVE_ADDRESS  = "Failed to remove address"
    const val ERR_LOGOUT_FAILED          = "Logout failed. Please try again."
    const val MSG_PROFILE_UPDATED        = "Profile updated ✅"
    const val MSG_ADDRESS_REMOVED        = "Address removed"
    const val PLACEHOLDER_ADD_NAME       = "Add your name"
    const val PLACEHOLDER_ADD_EMAIL      = "Add email address"
    const val ERR_COULD_NOT_LOAD_PROFILE = "Could not load profile"
    const val ARG_RESTAURANT_ID_MISSING  =
        "restaurantId is required for RestaurantScreen"
    const val ERR_COULD_NOT_LOAD_RESTAURANT = "Could not load restaurant"
    const val ERR_COULD_NOT_ADD_CART     = "Could not add to cart"
    const val ERR_FAILED_ADD_ITEM        = "Failed to add item"
    const val ERR_FAILED_UPDATE_CART     = "Failed to update cart"
    const val TIME = "11 AM - 11 PM"

    const val GET_CATEGORIES = "categories.json"
    const val GET_COLLECTIONS = "collections.json"
    const val GET_GEOCODE = "geocode.json"
    const val SEARCH = "search.json"
    const val GET_CUISINES = "cuisines.json"
    const val GET_DAILY_MENU = "dailymenu.json"
    const val GET_REVIEWS = "reviews.json"
    const val GET_USER = "user.json"
    const val GET_ORDERS = "orders.json"
    const val GET_ESTABLISHMENTS = "establishments.json"
    const val GET_LOCATIONS = "locations.json"
    const val GET_RESTAURANT = "restaurant.json"

    const val APP_BCK              =
        "App is in background. Please reopen and try again."
    const val FAILED_OTP_SEND      =
        "Failed to send OTP. Please try again."
    const val FAILED_VERIFICATION  =
        "Wrong OTP. Please check and try again."
    const val FAILED_PROFILE       =
        "Failed to load profile. Please try again."
    const val FAILED_UPDATED_PROFILE =
        "Failed to update profile. Please try again."

    const val GUEST_USER_ID = "guest"
    const val EMPTY_STRING = ""
    const val EMPTY_JSON_ARRAY = "[]"
    const val RUPEE_SYMBOL = "₹"
    const val FOR_TWO = "for two"
    const val MINUTES = "min"
    const val SELECT_LOCATION = "Select Location"
    const val CURRENT_LOCATION = "Current Location"
    const val EMOJI_BURGER          = "🍔"
    const val EMOJI_SUCCESS_TICK    = "✅"
    const val LOGIN_SCREEN_TAG      = "LoginScreen"
    const val ALPHA_BORDER_UNFOCUSED  = 0.5f
    const val ALPHA_BORDER_DISABLED   = 0.3f
    const val ALPHA_LABEL_DISABLED    = 0.5f
    const val ALPHA_PREFIX_DISABLED   = 0.5f

    val LOCATION_KEYWORDS = listOf(
        "bengaluru",
        "bangalore",
        "koramangala",
        "indiranagar",
        "hsr",
        "whitefield",
        "electronic city",
        "btm",
        "jp nagar",
        "jayanagar",
        "malleshwaram",
        "yelahanka",
        "hebbal",
        "kr puram",
        "marathahalli",
        "bellandur",
        "sarjapur"
    )
}