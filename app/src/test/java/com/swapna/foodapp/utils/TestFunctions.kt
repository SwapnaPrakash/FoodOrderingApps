package com.swapna.foodapp.utils

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.OrderItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.utils.TestConstants

// ── User ──────────────────────────────────────────────────────
fun fakeUser(
    id:    String = TestConstants.USER_ID,
    name:  String = TestConstants.USER_NAME,
    email: String = TestConstants.USER_EMAIL,
    phone: String = TestConstants.USER_PHONE,
) = User(
    id           = id,
    name         = name,
    email        = email,
    phone        = phone,
    profileImage = "",
    addresses    = listOf(fakeAddress()),
)

// ── Address ───────────────────────────────────────────────────
fun fakeAddress(
    id:    String = TestConstants.ADDRESS_ID,
    label: String = TestConstants.ADDRESS_LABEL,
) = Address(
    id          = id,
    label       = label,
    fullAddress = TestConstants.FULL_ADDRESS,
    landmark    = TestConstants.LANDMARK,
)

// ── Restaurant ────────────────────────────────────────────────
// ✅ FIX: Added all new Restaurant fields as parameters
// locality       — needed for location filtering tests
// highlights     — new field added to Restaurant domain
// openingHours   — new field added to Restaurant domain
// knownFor       — new field added to Restaurant domain
// isBestseller already not in Restaurant — skip
fun fakeRestaurant(
    id:           String       = TestConstants.RESTAURANT_ID,
    name:         String       = TestConstants.RESTAURANT_NAME,
    rating:       Double       = TestConstants.RESTAURANT_RATING,
    deliveryTime: Int          = TestConstants.DELIVERY_TIME,
    isOpen:       Boolean      = true,
    cuisines:     List<String> = listOf("Biryani", "South Indian"),
    // ✅ FIX: locality as param — used in filtering tests
    // HomeViewModelSpec: fakeRestaurant("r1", "Meghana", locality="Koramangala")
    locality:     String       = TestConstants.LOCALITY,
    // ✅ FIX: new Restaurant fields with safe defaults
    phoneNumber:  String       = "",
    openingHours: String       = "11 AM - 11 PM",
    highlights:   List<String> = emptyList(),
    knownFor:     String       = "",
) = Restaurant(
    id              = id,
    name            = name,
    imageUrl        = TestConstants.IMAGE_URL,
    thumbUrl        = TestConstants.THUMB_URL,
    rating          = rating,
    ratingText      = "Very Good",
    ratingColor     = "5BA829",
    totalVotes      = 1000,
    avgDeliveryTime = deliveryTime,
    deliveryFee     = 30.0,
    minOrder        = 100,
    cuisines        = cuisines,
    address         = TestConstants.RESTAURANT_ADDRESS,
    // ✅ FIX: use locality param not hardcoded TestConstants
    locality        = locality,
    isOpen          = isOpen,
    hasDelivery     = true,
    offers          = listOf("50% off upto ₹100"),
    avgCostForTwo   = 600,
    // ✅ FIX: map new fields from params
    phoneNumber     = phoneNumber,
    openingHours    = openingHours,
    highlights      = highlights,
    knownFor        = knownFor,
    distanceKm      = 0.0,
)

// ── MenuItem ──────────────────────────────────────────────────
// ✅ FIX: Added isBestseller, isAvailable, customisations
// Before: missing these 3 params → compile error when
//         MenuItem fields not set in constructor
fun fakeMenuItem(
    id:             String              = TestConstants.MENU_ID,
    name:           String              = TestConstants.MENU_NAME,
    price:          Double              = TestConstants.MENU_PRICE,
    category:       String              = TestConstants.CATEGORY,
    isVeg:          Boolean             = false,
    isRecommended:  Boolean             = false,
    // ✅ FIX: isBestseller — missing from original fakeMenuItem
    // MenuItem domain has isBestseller: Boolean = false
    isBestseller:   Boolean             = false,
    // ✅ FIX: isAvailable — missing from original fakeMenuItem
    // MenuItem domain has isAvailable: Boolean = true
    isAvailable:    Boolean             = true,
    // ✅ FIX: customisations — missing from original fakeMenuItem
    // MenuItem domain has customisations: List<Customisation> = emptyList()
    customisations: List<Customisation> = emptyList(),
    // ✅ FIX: restaurantId as param for flexibility in tests
    restaurantId:   String              = TestConstants.RESTAURANT_ID,
) = MenuItem(
    id             = id,
    restaurantId   = restaurantId,
    name           = name,
    description    = TestConstants.DESCRIPTION,
    price          = price,
    imageUrl       = TestConstants.FOOD_IMAGE,
    category       = category,
    isVeg          = isVeg,
    isRecommended  = isRecommended,
    // ✅ FIX: now properly mapped
    isBestseller   = isBestseller,
    isAvailable    = isAvailable,
    customisations = customisations,
)

// ── CartItem ──────────────────────────────────────────────────
fun fakeCartItem(
    id:         String = TestConstants.CART_ID,
    qty:        Int    = TestConstants.QUANTITY,
    price:      Double = TestConstants.MENU_PRICE,
    menuItemId: String = TestConstants.MENU_ID,
) = CartItem(
    id                     = id,
    menuItem               = fakeMenuItem(
        id    = menuItemId,
        price = price,
    ),
    quantity               = qty,
    selectedCustomisations = emptyList(),
)

// ── CustomisationOption ───────────────────────────────────────
fun fakeCustomisationOption(
    id:         String = TestConstants.OPTION_ID,
    label:      String = TestConstants.OPTION_LABEL,
    extraPrice: Double = TestConstants.EXTRA_PRICE,
) = CustomisationOption(
    id         = id,
    label      = label,
    extraPrice = extraPrice,
)

// ── Customisation Group ───────────────────────────────────────
// ✅ NEW: helper for building full Customisation with options
// Used in ProductDetailViewModelSpec and CartViewModelSpec
fun fakeCustomisation(
    id:      String                  = "custom_1",
    name:    String                  = "Size",
    options: List<CustomisationOption> = listOf(
        fakeCustomisationOption(
            id         = "opt_regular",
            label      = "Regular",
            extraPrice = 0.0,
        ),
        fakeCustomisationOption(
            id         = "opt_large",
            label      = "Large",
            extraPrice = 50.0,
        ),
    ),
) = Customisation(
    id      = id,
    name    = name,
    options = options,
)

// ── MenuItem WITH customisations ──────────────────────────────
// ✅ NEW: convenience builder for ProductDetail tests
fun fakeMenuItemWithCustomisations(
    id:    String = TestConstants.MENU_ID,
    name:  String = TestConstants.MENU_NAME,
    price: Double = TestConstants.MENU_PRICE,
) = fakeMenuItem(
    id             = id,
    name           = name,
    price          = price,
    isRecommended  = true,
    isBestseller   = true,
    customisations = listOf(
        fakeCustomisation(
            id      = "size_group",
            name    = "Size",
            options = listOf(
                fakeCustomisationOption("opt1", "Regular", 0.0),
                fakeCustomisationOption("opt2", "Large",   50.0),
            ),
        ),
        fakeCustomisation(
            id      = "spice_group",
            name    = "Spice Level",
            options = listOf(
                fakeCustomisationOption("opt3", "Mild",   0.0),
                fakeCustomisationOption("opt4", "Medium", 0.0),
                fakeCustomisationOption("opt5", "Hot",    0.0),
            ),
        ),
    ),
)

// ── Order ─────────────────────────────────────────────────────
fun fakeOrder(
    id:             String = TestConstants.ORDER_ID,
    restaurantName: String = TestConstants.RESTAURANT_NAME,
    total:          Double = TestConstants.TOTAL_AMOUNT,
) = Order(
    id              = id,
    restaurantId    = TestConstants.RESTAURANT_ID,
    restaurantName  = restaurantName,
    restaurantImage = TestConstants.IMAGE_URL,
    status          = "Delivered",
    timeFriendly    = "Yesterday, 1:45 PM",
    totalAmount     = total,
    items           = listOf(
        OrderItem(
            name     = TestConstants.MENU_NAME,
            quantity = 2,
            price    = TestConstants.MENU_PRICE,
        )
    ),
    canReorder      = true,
)