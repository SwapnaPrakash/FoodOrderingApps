package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.AddressDto
import com.swapna.foodapp.data.remote.dto.OrderDto
import com.swapna.foodapp.data.remote.dto.OrderItemDto
import com.swapna.foodapp.data.remote.dto.UserDto
import com.swapna.foodapp.utils.TestConstants.ADDRESS_FULL_123_ST
import com.swapna.foodapp.utils.TestConstants.ADDRESS_ID_1
import com.swapna.foodapp.utils.TestConstants.ADDRESS_ID_2
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_WORK
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LANDMARK_PARK
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LAT_12_93
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LNG_77_62
import com.swapna.foodapp.utils.TestConstants.ADDRESS_MAIN_ST
import com.swapna.foodapp.utils.TestConstants.ADDRESS_OFFICE_RD
import com.swapna.foodapp.utils.TestConstants.CART_QTY_1
import com.swapna.foodapp.utils.TestConstants.EMAIL_SHORT
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_ZERO
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_MUTTON_BIR
import com.swapna.foodapp.utils.TestConstants.ORDER_ID_1
import com.swapna.foodapp.utils.TestConstants.ORDER_ITEM_QTY_2
import com.swapna.foodapp.utils.TestConstants.ORDER_STATUS_DELIVERED_CAP
import com.swapna.foodapp.utils.TestConstants.ORDER_STATUS_PENDING
import com.swapna.foodapp.utils.TestConstants.ORDER_TIME_FRIENDLY
import com.swapna.foodapp.utils.TestConstants.ORDER_TIME_TODAY_SHORT
import com.swapna.foodapp.utils.TestConstants.ORDER_TIME_TOMORROW
import com.swapna.foodapp.utils.TestConstants.PHONE_SHORT
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_349
import com.swapna.foodapp.utils.TestConstants.PRICE_500
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MEGHANA
import com.swapna.foodapp.utils.TestConstants.REVIEW_IMG_URL
import com.swapna.foodapp.utils.TestConstants.USER_EMAIL_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_ID_1
import com.swapna.foodapp.utils.TestConstants.USER_NAME_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_PHONE_VALID
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class UserMapperSpec : DescribeSpec({

    val mapper = UserMapper()

    describe("UserMapper") {

        describe("toDomain") {

            context("user with all fields populated") {

                val dto = UserDto(
                    id = USER_ID_1,
                    name = USER_NAME_SWAPNA,
                    email = USER_EMAIL_SWAPNA,
                    phone = USER_PHONE_VALID,
                    profileImage = REVIEW_IMG_URL,
                    addresses = listOf(
                        AddressDto(
                            id = ADDRESS_ID_1,
                            label = ADDRESS_LABEL_HOME,
                            fullAddress = ADDRESS_MAIN_ST,
                            landmark = ADDRESS_LANDMARK_PARK,
                            latitude = ADDRESS_LAT_12_93,
                            longitude = ADDRESS_LNG_77_62,
                        )
                    ),
                )

                it("maps id correctly") {
                    mapper.toDomain(dto).id shouldBe USER_ID_1
                }

                it("maps name correctly") {
                    mapper.toDomain(dto).name shouldBe USER_NAME_SWAPNA
                }

                it("maps email correctly") {
                    mapper.toDomain(dto).email shouldBe USER_EMAIL_SWAPNA
                }

                it("maps phone correctly") {
                    mapper.toDomain(dto).phone shouldBe USER_PHONE_VALID
                }

                it("maps profileImage correctly") {
                    mapper.toDomain(dto).profileImage shouldBe REVIEW_IMG_URL
                }

                it("maps addresses list correctly") {
                    mapper.toDomain(dto).addresses shouldHaveSize 1
                }

                it("maps address id correctly") {
                    mapper.toDomain(dto).addresses.first().id shouldBe ADDRESS_ID_1
                }

                it("maps address label correctly") {
                    mapper.toDomain(dto).addresses.first().label shouldBe ADDRESS_LABEL_HOME
                }

                it("maps address fullAddress correctly") {
                    mapper.toDomain(dto).addresses.first().fullAddress shouldBe ADDRESS_MAIN_ST
                }

                it("maps address landmark correctly") {
                    mapper.toDomain(dto).addresses.first().landmark shouldBe ADDRESS_LANDMARK_PARK
                }

                it("maps address latitude correctly") {
                    mapper.toDomain(dto).addresses.first().latitude shouldBe ADDRESS_LAT_12_93
                }

                it("maps address longitude correctly") {
                    mapper.toDomain(dto).addresses.first().longitude shouldBe ADDRESS_LNG_77_62
                }
            }

            context("user with null optional fields") {

                val dto = UserDto(
                    id = USER_ID_1,
                    name = USER_NAME_SWAPNA,
                    email = USER_EMAIL_SWAPNA,
                    phone = USER_PHONE_VALID,
                    profileImage = null,
                    addresses = null,
                )

                it("null profileImage maps to empty string") {
                    mapper.toDomain(dto).profileImage shouldBe ""
                }

                it("null addresses maps to empty list") {
                    mapper.toDomain(dto).addresses.shouldBeEmpty()
                }
            }

            context("user with null landmark in address") {

                val dto = UserDto(
                    id = USER_ID_1,
                    name = USER_NAME_SWAPNA,
                    email = EMAIL_SHORT,
                    phone = PHONE_SHORT,
                    addresses = listOf(
                        AddressDto(
                            id = ADDRESS_ID_1,
                            label = ADDRESS_LABEL_HOME,
                            fullAddress = ADDRESS_FULL_123_ST,
                            landmark = null,
                        )
                    ),
                )

                it("null landmark in address maps to empty string") {
                    mapper.toDomain(dto).addresses.first().landmark shouldBe ""
                }
            }

            context("user with multiple addresses") {

                val dto = UserDto(
                    id = USER_ID_1,
                    name = USER_NAME_SWAPNA,
                    email = EMAIL_SHORT,
                    phone = PHONE_SHORT,
                    addresses = listOf(
                        AddressDto(ADDRESS_ID_1, ADDRESS_LABEL_HOME, ADDRESS_FULL_123_ST),
                        AddressDto(
                            ADDRESS_ID_2,
                            ADDRESS_LABEL_WORK,
                            ADDRESS_OFFICE_RD
                        ),
                    ),
                )

                it("maps all addresses correctly") {
                    val addresses = mapper.toDomain(dto).addresses
                    addresses shouldHaveSize 2
                    addresses[0].label shouldBe ADDRESS_LABEL_HOME
                    addresses[1].label shouldBe ADDRESS_LABEL_WORK
                }
            }
        }

        // ── orderToDomain (OrderDto → Order) ─────────────────

        describe("orderToDomain") {

            context("order with all fields") {

                val dto = OrderDto(
                    id = ORDER_ID_1,
                    restaurantId = RESTAURANT_ID_1,
                    restaurantName = RESTAURANT_MEGHANA,
                    restaurantImage = REVIEW_IMG_URL,
                    status = ORDER_STATUS_DELIVERED_CAP,
                    timeFriendly = ORDER_TIME_FRIENDLY,
                    totalAmount = PRICE_249,
                    items = listOf(
                        OrderItemDto(
                            name = MENU_ITEM_CHICK_BIR,
                            quantity = ORDER_ITEM_QTY_2,
                            price = PRICE_249,
                        )
                    ),
                    canReorder = true,
                )

                it("maps order id correctly") {
                    mapper.orderToDomain(dto).id shouldBe ORDER_ID_1
                }

                it("maps restaurantId correctly") {
                    mapper.orderToDomain(dto).restaurantId shouldBe RESTAURANT_ID_1
                }

                it("maps restaurantName correctly") {
                    mapper.orderToDomain(dto).restaurantName shouldBe RESTAURANT_MEGHANA
                }

                it("maps restaurantImage correctly") {
                    mapper.orderToDomain(dto).restaurantImage shouldBe REVIEW_IMG_URL
                }

                it("maps status correctly") {
                    mapper.orderToDomain(dto).status shouldBe ORDER_STATUS_DELIVERED_CAP
                }

                it("maps timeFriendly correctly") {
                    mapper.orderToDomain(dto).timeFriendly shouldBe ORDER_TIME_FRIENDLY
                }

                it("maps totalAmount correctly") {
                    mapper.orderToDomain(dto).totalAmount shouldBe PRICE_249
                }

                it("maps canReorder correctly") {
                    mapper.orderToDomain(dto).canReorder shouldBe true
                }

                it("maps order items list correctly") {
                    mapper.orderToDomain(dto).items shouldHaveSize 1
                }

                it("maps order item name correctly") {
                    mapper.orderToDomain(dto).items.first().name shouldBe MENU_ITEM_CHICK_BIR
                }

                it("maps order item quantity correctly") {
                    mapper.orderToDomain(dto).items.first().quantity shouldBe ORDER_ITEM_QTY_2
                }

                it("maps order item price correctly") {
                    mapper.orderToDomain(dto).items.first().price shouldBe PRICE_249
                }
            }

            context("order with empty items list") {

                val dto = OrderDto(
                    id = ORDER_ID_1,
                    restaurantId = RESTAURANT_ID_1,
                    restaurantName = RESTAURANT_MEGHANA,
                    restaurantImage = "",
                    status = ORDER_STATUS_DELIVERED_CAP,
                    timeFriendly = ORDER_TIME_TODAY_SHORT,
                    totalAmount = EXTRA_PRICE_ZERO,
                    items = emptyList(),
                    canReorder = false,
                )

                it("empty items list maps to empty list") {
                    mapper.orderToDomain(dto).items.shouldBeEmpty()
                }

                it("canReorder false maps correctly") {
                    mapper.orderToDomain(dto).canReorder shouldBe false
                }
            }

            context("order with multiple items") {

                val dto = OrderDto(
                    id = ORDER_ID_1,
                    restaurantId = RESTAURANT_ID_1,
                    restaurantName = RESTAURANT_MEGHANA,
                    restaurantImage = "",
                    status = ORDER_STATUS_PENDING,
                    timeFriendly = ORDER_TIME_TOMORROW,
                    totalAmount = PRICE_500,
                    items = listOf(
                        OrderItemDto(MENU_ITEM_CHICK_BIR, CART_QTY_1, PRICE_249),
                        OrderItemDto(
                            MENU_ITEM_MUTTON_BIR,
                            CART_QTY_1,
                            PRICE_349
                        ),
                    ),
                    canReorder = true,
                )

                it("maps all order items") {
                    mapper.orderToDomain(dto).items shouldHaveSize 2
                }

                it("first item name correct") {
                    mapper.orderToDomain(dto).items[0].name shouldBe MENU_ITEM_CHICK_BIR
                }

                it("second item name correct") {
                    mapper.orderToDomain(dto).items[1].name shouldBe MENU_ITEM_MUTTON_BIR
                }
            }
        }
    }
})