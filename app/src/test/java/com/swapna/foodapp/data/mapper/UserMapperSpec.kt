package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.AddressDto
import com.swapna.foodapp.data.remote.dto.OrderDto
import com.swapna.foodapp.data.remote.dto.OrderItemDto
import com.swapna.foodapp.data.remote.dto.UserDto
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class UserMapperSpec : DescribeSpec({

    val mapper = UserMapper()

    describe("UserMapper") {

        // ── toDomain (UserDto → User) ─────────────────────────

        describe("toDomain") {

            context("user with all fields populated") {

                val dto = UserDto(
                    id           = "u1",
                    name         = "Swapna",
                    email        = "swapna@example.com",
                    phone        = "+919876543210",
                    profileImage = "https://img.jpg",
                    addresses    = listOf(
                        AddressDto(
                            id          = "a1",
                            label       = "Home",
                            fullAddress = "123 Main St",
                            landmark    = "Near Park",
                            latitude    = 12.93,
                            longitude   = 77.62,
                        )
                    ),
                )

                it("maps id correctly") {
                    mapper.toDomain(dto).id shouldBe "u1"
                }

                it("maps name correctly") {
                    mapper.toDomain(dto).name shouldBe "Swapna"
                }

                it("maps email correctly") {
                    mapper.toDomain(dto).email shouldBe "swapna@example.com"
                }

                it("maps phone correctly") {
                    mapper.toDomain(dto).phone shouldBe "+919876543210"
                }

                it("maps profileImage correctly") {
                    mapper.toDomain(dto).profileImage shouldBe "https://img.jpg"
                }

                it("maps addresses list correctly") {
                    mapper.toDomain(dto).addresses shouldHaveSize 1
                }

                it("maps address id correctly") {
                    mapper.toDomain(dto).addresses.first().id shouldBe "a1"
                }

                it("maps address label correctly") {
                    mapper.toDomain(dto).addresses.first().label shouldBe "Home"
                }

                it("maps address fullAddress correctly") {
                    mapper.toDomain(dto).addresses.first().fullAddress shouldBe "123 Main St"
                }

                it("maps address landmark correctly") {
                    mapper.toDomain(dto).addresses.first().landmark shouldBe "Near Park"
                }

                it("maps address latitude correctly") {
                    mapper.toDomain(dto).addresses.first().latitude shouldBe 12.93
                }

                it("maps address longitude correctly") {
                    mapper.toDomain(dto).addresses.first().longitude shouldBe 77.62
                }
            }

            context("user with null optional fields") {

                val dto = UserDto(
                    id           = "u1",
                    name         = "Swapna",
                    email        = "swapna@example.com",
                    phone        = "+919876543210",
                    profileImage = null,   // ← null
                    addresses    = null,   // ← null
                )

                // WHY test null fields?
                // UserDto.profileImage and addresses are nullable
                // mapper uses ?: to handle nulls
                // Without this test, removing ?: "" would not be caught
                it("null profileImage maps to empty string") {
                    mapper.toDomain(dto).profileImage shouldBe ""
                }

                it("null addresses maps to empty list") {
                    mapper.toDomain(dto).addresses.shouldBeEmpty()
                }
            }

            context("user with null landmark in address") {

                val dto = UserDto(
                    id    = "u1",
                    name  = "Swapna",
                    email = "s@example.com",
                    phone = "+91",
                    addresses = listOf(
                        AddressDto(
                            id          = "a1",
                            label       = "Home",
                            fullAddress = "123 St",
                            landmark    = null,  // ← null
                        )
                    ),
                )

                // WHY? landmark = a.landmark ?: ""
                it("null landmark in address maps to empty string") {
                    mapper.toDomain(dto).addresses.first().landmark shouldBe ""
                }
            }

            context("user with multiple addresses") {

                val dto = UserDto(
                    id    = "u1",
                    name  = "Swapna",
                    email = "s@example.com",
                    phone = "+91",
                    addresses = listOf(
                        AddressDto("a1", "Home", "123 St"),
                        AddressDto("a2", "Work", "456 Office Rd"),
                    ),
                )

                it("maps all addresses correctly") {
                    val addresses = mapper.toDomain(dto).addresses
                    addresses shouldHaveSize 2
                    addresses[0].label shouldBe "Home"
                    addresses[1].label shouldBe "Work"
                }
            }
        }

        // ── orderToDomain (OrderDto → Order) ─────────────────

        describe("orderToDomain") {

            context("order with all fields") {

                val dto = OrderDto(
                    id              = "o1",
                    restaurantId    = "r1",
                    restaurantName  = "Meghana Foods",
                    restaurantImage = "https://img.jpg",
                    status          = "Delivered",
                    timeFriendly    = "Today, 12:00 PM",
                    totalAmount     = 249.0,
                    items           = listOf(
                        OrderItemDto(
                            name     = "Chicken Biryani",
                            quantity = 2,
                            price    = 249.0,
                        )
                    ),
                    canReorder = true,
                )

                it("maps order id correctly") {
                    mapper.orderToDomain(dto).id shouldBe "o1"
                }

                it("maps restaurantId correctly") {
                    mapper.orderToDomain(dto).restaurantId shouldBe "r1"
                }

                it("maps restaurantName correctly") {
                    mapper.orderToDomain(dto).restaurantName shouldBe "Meghana Foods"
                }

                it("maps restaurantImage correctly") {
                    mapper.orderToDomain(dto).restaurantImage shouldBe "https://img.jpg"
                }

                it("maps status correctly") {
                    mapper.orderToDomain(dto).status shouldBe "Delivered"
                }

                it("maps timeFriendly correctly") {
                    mapper.orderToDomain(dto).timeFriendly shouldBe "Today, 12:00 PM"
                }

                it("maps totalAmount correctly") {
                    mapper.orderToDomain(dto).totalAmount shouldBe 249.0
                }

                it("maps canReorder correctly") {
                    mapper.orderToDomain(dto).canReorder shouldBe true
                }

                it("maps order items list correctly") {
                    mapper.orderToDomain(dto).items shouldHaveSize 1
                }

                it("maps order item name correctly") {
                    mapper.orderToDomain(dto).items.first().name shouldBe "Chicken Biryani"
                }

                it("maps order item quantity correctly") {
                    mapper.orderToDomain(dto).items.first().quantity shouldBe 2
                }

                it("maps order item price correctly") {
                    mapper.orderToDomain(dto).items.first().price shouldBe 249.0
                }
            }

            context("order with empty items list") {

                val dto = OrderDto(
                    id              = "o1",
                    restaurantId    = "r1",
                    restaurantName  = "Meghana Foods",
                    restaurantImage = "",
                    status          = "Delivered",
                    timeFriendly    = "Today",
                    totalAmount     = 0.0,
                    items           = emptyList(),
                    canReorder      = false,
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
                    id              = "o1",
                    restaurantId    = "r1",
                    restaurantName  = "Meghana Foods",
                    restaurantImage = "",
                    status          = "Pending",
                    timeFriendly    = "Tomorrow",
                    totalAmount     = 500.0,
                    items           = listOf(
                        OrderItemDto("Chicken Biryani", 1, 249.0),
                        OrderItemDto("Mutton Biryani",  1, 349.0),
                    ),
                    canReorder = true,
                )

                it("maps all order items") {
                    mapper.orderToDomain(dto).items shouldHaveSize 2
                }

                it("first item name correct") {
                    mapper.orderToDomain(dto).items[0].name shouldBe "Chicken Biryani"
                }

                it("second item name correct") {
                    mapper.orderToDomain(dto).items[1].name shouldBe "Mutton Biryani"
                }
            }
        }
    }
})