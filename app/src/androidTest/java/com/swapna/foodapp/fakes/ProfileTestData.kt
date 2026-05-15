package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.OrderItem
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_REST_ID_R1
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_USER_ID_U1
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_FULL
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_ID
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_USER_EMAIL
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_USER_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_USER_PHONE
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_ID
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_ITEM_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_REST_NAME
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_STATUS_DELIVERED
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_TIME
import com.swapna.foodapp.utils.AndroidTestConstants.PROFILE_ORDER_TOTAL

fun fakeProfileUser(
    name: String = HOME_USER_NAME,
    email: String = HOME_USER_EMAIL,
    phone: String = HOME_USER_PHONE,
    addresses: List<Address> = emptyList(),
) = User(
    id = FAKE_USER_ID_U1,
    name = name,
    email = email,
    phone = phone,
    addresses = addresses,
)

fun fakeProfileAddress(
    id: String = HOME_ADDRESS_ID,
    label: String = HOME_ADDRESS_LABEL_HOME,
    fullAddress: String = HOME_ADDRESS_FULL,
) = Address(
    id = id,
    label = label,
    fullAddress = fullAddress,
    landmark = "",
    latitude = 0.0,
    longitude = 0.0,
)

fun fakeProfileOrder() = Order(
    id = PROFILE_ORDER_ID,
    restaurantId = FAKE_REST_ID_R1,
    restaurantName = PROFILE_ORDER_REST_NAME,
    restaurantImage = "",
    status = PROFILE_ORDER_STATUS_DELIVERED,
    timeFriendly = PROFILE_ORDER_TIME,
    totalAmount = PROFILE_ORDER_TOTAL,
    items = listOf(
        OrderItem(
            name = PROFILE_ORDER_ITEM_NAME,
            quantity = 1,
            price = PROFILE_ORDER_TOTAL,
        )
    ),
    canReorder = true,
)