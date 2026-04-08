package com.swapna.foodapp.utils

import com.swapna.foodapp.domain.model.*
import com.swapna.foodapp.utils.testdata.TestConstants

fun fakeUser(
    id: String    = TestConstants.USER_ID,
    name: String  = TestConstants.USER_NAME,
    email: String = TestConstants.USER_EMAIL,
    phone: String = TestConstants.USER_PHONE,
) = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    profileImage = "",
    addresses = listOf(fakeAddress()),
)

fun fakeAddress(
    id: String = TestConstants.ADDRESS_ID,
    label: String = TestConstants.ADDRESS_LABEL,
) = Address(
    id = id,
    label = label,
    fullAddress = TestConstants.FULL_ADDRESS,
    landmark = TestConstants.LANDMARK,
)

fun fakeRestaurant(
    id: String = TestConstants.RESTAURANT_ID,
    name: String = TestConstants.RESTAURANT_NAME,
    rating: Double = TestConstants.RESTAURANT_RATING,
    deliveryTime: Int = TestConstants.DELIVERY_TIME,
    isOpen: Boolean = true,
    cuisines: List<String> = listOf("Biryani", "South Indian"),
) = Restaurant(
    id = id,
    name = name,
    imageUrl = TestConstants.IMAGE_URL,
    thumbUrl = TestConstants.THUMB_URL,
    rating = rating,
    ratingText = "Very Good",
    ratingColor = "5BA829",
    totalVotes = 1000,
    avgDeliveryTime = deliveryTime,
    deliveryFee = 30.0,
    minOrder = 100,
    cuisines = cuisines,
    address = TestConstants.RESTAURANT_ADDRESS,
    locality = TestConstants.LOCALITY,
    isOpen = isOpen,
    hasDelivery = true,
    offers = listOf("50% off upto ₹100"),
    avgCostForTwo = 600,
)

fun fakeMenuItem(
    id: String = TestConstants.MENU_ID,
    name: String = TestConstants.MENU_NAME,
    price: Double = TestConstants.MENU_PRICE,
    category: String = TestConstants.CATEGORY,
    isVeg: Boolean = false,
    isRecommended: Boolean = false,
) = MenuItem(
    id = id,
    restaurantId = TestConstants.RESTAURANT_ID,
    name = name,
    description = TestConstants.DESCRIPTION,
    price = price,
    imageUrl = TestConstants.FOOD_IMAGE,
    category = category,
    isVeg = isVeg,
    isRecommended = isRecommended,
)

fun fakeCartItem(
    id: String = TestConstants.CART_ID,
    qty: Int = TestConstants.QUANTITY,
    price: Double = TestConstants.MENU_PRICE,
    menuItemId: String = TestConstants.MENU_ID,
) = CartItem(
    id = id,
    menuItem = fakeMenuItem(id = menuItemId, price = price),
    quantity = qty,
    selectedCustomisations = emptyList(),
)

fun fakeCustomisationOption(
    id: String = TestConstants.OPTION_ID,
    label: String = TestConstants.OPTION_LABEL,
    extraPrice: Double = TestConstants.EXTRA_PRICE,
) = CustomisationOption(
    id = id,
    label = label,
    extraPrice = extraPrice,
)

fun fakeOrder(
    id: String = TestConstants.ORDER_ID,
    restaurantName: String = TestConstants.RESTAURANT_NAME,
    total: Double = TestConstants.TOTAL_AMOUNT,
) = Order(
    id = id,
    restaurantId = TestConstants.RESTAURANT_ID,
    restaurantName = restaurantName,
    restaurantImage = TestConstants.IMAGE_URL,
    status = "Delivered",
    timeFriendly = "Yesterday, 1:45 PM",
    totalAmount = total,
    items = listOf(
        OrderItem(TestConstants.MENU_NAME, 2, TestConstants.MENU_PRICE)
    ),
    canReorder = true,
)