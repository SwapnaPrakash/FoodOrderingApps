package com.swapna.foodapp.utils

import com.swapna.foodapp.data.local.entity.CartItemEntity
import com.swapna.foodapp.data.local.entity.UserEntity
import com.swapna.foodapp.data.remote.dto.CustomisationDto
import com.swapna.foodapp.data.remote.dto.DailyMenuDto
import com.swapna.foodapp.data.remote.dto.DailyMenuResponse
import com.swapna.foodapp.data.remote.dto.DailyMenuWrapper
import com.swapna.foodapp.data.remote.dto.DishDto
import com.swapna.foodapp.data.remote.dto.DishWrapper
import com.swapna.foodapp.data.remote.dto.OrderDto
import com.swapna.foodapp.data.remote.dto.OrderWrapper
import com.swapna.foodapp.data.remote.dto.OrdersResponse
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.OrderItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.User

fun fakeUser(
    id: String = TestConstants.USER_ID_1,
    name: String = TestConstants.USER_NAME_SWAPNA,
    email: String = TestConstants.USER_EMAIL_SWAPNA,
    phone: String = TestConstants.USER_PHONE,
) = User(
    id = id,
    name = name,
    email = email,
    phone = phone,
    profileImage = "",
    addresses = listOf(fakeAddress()),
    selectedLocation = "",
)

fun fakeAddress(
    id: String = TestConstants.ADDRESS_ID_1,
    label: String = TestConstants.ADDRESS_LABEL_HOME,
) = Address(
    id = id,
    label = label,
    fullAddress = TestConstants.ADDRESS_MG_ROAD,
    landmark = TestConstants.ADDRESS_NEAR_FORUM,
)

fun fakeRestaurant(
    id: String = TestConstants.RESTAURANT_ID,
    name: String = TestConstants.RESTAURANT_NAME,
    rating: Double = TestConstants.RESTAURANT_RATING,
    deliveryTime: Int = TestConstants.DELIVERY_TIME,
    isOpen: Boolean = true,
    cuisines: List<String> = listOf(
        TestConstants.HOME_CATEGORY_BIRYANI,
        TestConstants.HOME_SOUTH_INDIAN,
    ),
    locality: String = TestConstants.LOCALITY,
    phoneNumber: String = "",
    openingHours: String = TestConstants.OPENING_HOURS_DEFAULT,
    highlights: List<String> = emptyList(),
    knownFor: String = "",
    address: String = "",
) = Restaurant(
    id = id,
    name = name,
    imageUrl = TestConstants.IMAGE_URL,
    thumbUrl = TestConstants.THUMB_URL,
    rating = rating,
    ratingText = TestConstants.ENTITY_RATING_TEXT,
    ratingColor = TestConstants.ENTITY_RATING_COLOR,
    totalVotes = TestConstants.FAKE_RESTAURANT_VOTES,
    avgDeliveryTime = deliveryTime,
    deliveryFee = TestConstants.HOME_DELIVERY_FEE,
    minOrder = TestConstants.RESTAURANT_MIN_ORDER_100,
    cuisines = cuisines,
    address = TestConstants.RESTAURANT_ADDRESS,
    locality = locality,
    isOpen = isOpen,
    hasDelivery = true,
    offers = listOf(TestConstants.FAKE_OFFER_UPTO_100),
    avgCostForTwo = TestConstants.ENTITY_COST_TWO,
    phoneNumber = phoneNumber,
    openingHours = openingHours,
    highlights = highlights,
    knownFor = knownFor,
    distanceKm = 0.0,
)

fun fakeMenuItem(
    id: String = TestConstants.MENU_ID,
    name: String = TestConstants.MENU_NAME,
    price: Double = TestConstants.MENU_PRICE,
    category: String = TestConstants.CATEGORY,
    isVeg: Boolean = false,
    isRecommended: Boolean = false,
    isBestseller: Boolean = false,
    isAvailable: Boolean = true,
    customisations: List<Customisation> = emptyList(),
    restaurantId: String = TestConstants.RESTAURANT_ID,
) = MenuItem(
    id = id,
    restaurantId = restaurantId,
    name = name,
    description = TestConstants.DESCRIPTION,
    price = price,
    imageUrl = TestConstants.FOOD_IMAGE,
    category = category,
    isVeg = isVeg,
    isRecommended = isRecommended,
    isBestseller = isBestseller,
    isAvailable = isAvailable,
    customisations = customisations,
)

fun fakeCartItem(
    id: String = TestConstants.CART_ID_1,
    qty: Int = TestConstants.CART_QTY_1,
    price: Double = TestConstants.MENU_PRICE,
    menuItemId: String = TestConstants.MENU_ID,
) = CartItem(
    id = id,
    menuItem = fakeMenuItem(
        id = menuItemId,
        price = price,
    ),
    quantity = qty,
    selectedCustomisations = emptyList(),
)

fun fakeCustomisationOption(
    id: String = TestConstants.OPTION_ID,
    label: String = TestConstants.CUSTOMISE_LABEL_REGULAR,
    extraPrice: Double = TestConstants.EXTRA_PRICE_ZERO,
) = CustomisationOption(
    id = id,
    label = label,
    extraPrice = extraPrice,
)

fun fakeCustomisation(
    id: String = TestConstants.CUSTOM_DEFAULT_ID,
    name: String = TestConstants.CUSTOMISE_NAME_SIZE,
    options: List<CustomisationOption> = listOf(
        fakeCustomisationOption(
            id = TestConstants.OPTION_ID,
            label = TestConstants.CUSTOMISE_LABEL_REGULAR,
            extraPrice = TestConstants.EXTRA_PRICE_ZERO,
        ),
        fakeCustomisationOption(
            id = TestConstants.CUSTOM_OPT_LARGE_ID,
            label = TestConstants.CUSTOMISE_LABEL_LARGE,
            extraPrice = TestConstants.EXTRA_PRICE_LARGE,
        ),
    ),
) = Customisation(
    id = id,
    name = name,
    options = options,
)

fun fakeMenuItemWithCustomisations(
    id: String = TestConstants.MENU_ID,
    name: String = TestConstants.MENU_NAME,
    price: Double = TestConstants.MENU_PRICE,
) = fakeMenuItem(
    id = id,
    name = name,
    price = price,
    isRecommended = true,
    isBestseller = true,
    customisations = listOf(
        fakeCustomisation(
            id = TestConstants.CUSTOMISE_GROUP_SIZE,
            name = TestConstants.CUSTOMISE_NAME_SIZE,
            options = listOf(
                fakeCustomisationOption(
                    TestConstants.OPT_ID_1,
                    TestConstants.CUSTOMISE_LABEL_REGULAR,
                    TestConstants.EXTRA_PRICE_ZERO,
                ),
                fakeCustomisationOption(
                    TestConstants.OPT_ID_2,
                    TestConstants.CUSTOMISE_LABEL_LARGE,
                    TestConstants.EXTRA_PRICE_LARGE,
                ),
            ),
        ),
        fakeCustomisation(
            id = TestConstants.CUSTOMISE_GROUP_SPICE,
            name = TestConstants.CUSTOMISE_NAME_SPICE,
            options = listOf(
                fakeCustomisationOption(
                    TestConstants.OPT_ID_3,
                    TestConstants.CUSTOMISE_LABEL_MILD,
                    TestConstants.EXTRA_PRICE_ZERO,
                ),
                fakeCustomisationOption(
                    TestConstants.OPT_ID_4,
                    TestConstants.CUSTOMISE_LABEL_MEDIUM,
                    TestConstants.EXTRA_PRICE_ZERO,
                ),
                fakeCustomisationOption(
                    TestConstants.OPT_ID_5,
                    TestConstants.CUSTOMISE_LABEL_HOT,
                    TestConstants.EXTRA_PRICE_ZERO,
                ),
            ),
        ),
    ),
)

fun fakeOrder(
    id: String = TestConstants.ORDER_ID_1,
    restaurantName: String = TestConstants.RESTAURANT_NAME,
    total: Double = TestConstants.TOTAL_AMOUNT,
) = Order(
    id = id,
    restaurantId = TestConstants.RESTAURANT_ID,
    restaurantName = restaurantName,
    restaurantImage = TestConstants.IMAGE_URL,
    status = TestConstants.ORDER_STATUS_DELIVERED_CAP,
    timeFriendly = TestConstants.ORDER_TIME_YESTERDAY,
    totalAmount = total,
    items = listOf(
        OrderItem(
            name = TestConstants.MENU_NAME,
            quantity = TestConstants.ORDER_ITEM_QTY_2,
            price = TestConstants.MENU_PRICE,
        )
    ),
    canReorder = true,
)

fun fakeUserEntity(
    id: String = TestConstants.USER_ID_1,
    name: String = TestConstants.USER_NAME_SWAPNA,
    addressesJson: String = TestConstants.EMPTY_JSON_ARRAY,
) = UserEntity(
    id = id,
    name = name,
    email = TestConstants.USER_EMAIL_SWAPNA,
    phone = TestConstants.USER_PHONE_VALID,
    profileImage = "",
    addressesJson = addressesJson,
    selectedLocation = "",
    cachedAt = System.currentTimeMillis(),
)

fun fakeOrdersResponse() = OrdersResponse(
    orders = listOf(
        OrderWrapper(
            order = OrderDto(
                id = TestConstants.ORDER_ID_1,
                restaurantId = TestConstants.RESTAURANT_ID_1,
                restaurantName = TestConstants.RESTAURANT_MEGHANA,
                restaurantImage = "",
                status = TestConstants.ORDER_STATUS_DELIVERED_CAP,
                timeFriendly = TestConstants.ORDER_TIME_FRIENDLY,
                totalAmount = TestConstants.PRICE_249,
                items = emptyList(),
                canReorder = true,
            )
        )
    )
)

fun fakeCartItemEntity(
    id: String = TestConstants.CART_ID_1,
    menuItemId: String = TestConstants.MENU_ID_1,
    qty: Int = TestConstants.CART_QTY_1,
) = CartItemEntity(
    id = id,
    menuItemId = menuItemId,
    menuItemJson = """{"id":"$menuItemId",
        |"name":"${TestConstants.MENU_ITEM_CHICK_BIR}",
        |"price":${TestConstants.PRICE_249}}""".trimMargin(),
    quantity = qty,
    customisationsJson = TestConstants.EMPTY_JSON_ARRAY,
    addedAt = System.currentTimeMillis(),
)

fun singleItemResponse(
    category: String = TestConstants.HOME_CATEGORY_BIRYANI,
    id: String = TestConstants.MENU_ID_1,
    name: String = TestConstants.MENU_ITEM_CHICK_BIR,
    price: String = TestConstants.MENU_PRICE_STR,
    isVeg: Int = TestConstants.API_IS_NOT_VEG,
    isRecommended: Int = TestConstants.API_IS_RECOMMENDED,
    imageUrl: String? = null,
    customisations: List<CustomisationDto>? = null,
) = DailyMenuResponse(
    dailyMenus = listOf(
        DailyMenuWrapper(
            menu = DailyMenuDto(
                id = TestConstants.SINGLE_ITEM_MENU_ID,
                name = category,
                dishes = listOf(
                    DishWrapper(
                        dish = DishDto(
                            id = id,
                            name = name,
                            price = price,
                            description = "",
                            imageUrl = imageUrl,
                            isVeg = isVeg,
                            isRecommended = isRecommended,
                            customisations = customisations,
                        )
                    )
                )
            )
        )
    )
)