package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.local.entity.CartItemEntity
import com.swapna.foodapp.data.local.entity.MenuItemEntity
import com.swapna.foodapp.data.local.entity.RestaurantEntity
import com.swapna.foodapp.data.local.entity.UserEntity
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.User
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class EntityMapperSpec : DescribeSpec({

    val mapper = EntityMapper()

    describe("EntityMapper") {

        // ── restaurantToDomain ────────────────────────────────

        describe("restaurantToDomain") {

            context("basic restaurant entity") {

                val entity = RestaurantEntity(
                    id              = "r1",
                    name            = "Meghana Foods",
                    imageUrl        = "https://img.jpg",
                    thumbUrl        = "https://thumb.jpg",
                    rating          = 4.5,
                    ratingText      = "Excellent",
                    ratingColor     = "5BA829",
                    totalVotes      = 1000,
                    avgDeliveryTime = 30,
                    deliveryFee     = 30.0,
                    minOrder        = 100,
                    cuisinesJson    = """["Biryani","South Indian"]""",
                    address         = "Koramangala, Bengaluru",
                    locality        = "Koramangala",
                    isOpen          = true,
                    hasDelivery     = true,
                    offersJson      = """["50% off"]""",
                    avgCostForTwo   = 600,
                    distanceKm      = 1.5,
                    phoneNumber     = "+911234567890",
                    openingHours    = "11 AM - 11 PM",
                    highlightsJson  = """["Dine-in","Takeaway"]""",
                    knownFor        = "Biryani",
                )

                it("maps id correctly") {
                    mapper.restaurantToDomain(entity).id shouldBe "r1"
                }

                it("maps name correctly") {
                    mapper.restaurantToDomain(entity).name shouldBe "Meghana Foods"
                }

                it("maps rating correctly") {
                    mapper.restaurantToDomain(entity).rating shouldBe 4.5
                }

                it("maps avgDeliveryTime correctly") {
                    mapper.restaurantToDomain(entity).avgDeliveryTime shouldBe 30
                }

                it("maps locality correctly") {
                    mapper.restaurantToDomain(entity).locality shouldBe "Koramangala"
                }

                it("maps isOpen correctly") {
                    mapper.restaurantToDomain(entity).isOpen shouldBe true
                }

                it("maps avgCostForTwo correctly") {
                    mapper.restaurantToDomain(entity).avgCostForTwo shouldBe 600
                }

                it("maps distanceKm correctly") {
                    mapper.restaurantToDomain(entity).distanceKm shouldBe 1.5
                }

                it("maps phoneNumber correctly") {
                    mapper.restaurantToDomain(entity).phoneNumber shouldBe "+911234567890"
                }

                it("maps openingHours correctly") {
                    mapper.restaurantToDomain(entity).openingHours shouldBe "11 AM - 11 PM"
                }

                it("maps knownFor correctly") {
                    mapper.restaurantToDomain(entity).knownFor shouldBe "Biryani"
                }

                // WHY test JSON parsing separately?
                // cuisinesJson/offersJson/highlightsJson are stored as JSON strings
                // fromJsonStringList() must parse them back to List<String>
                it("parses cuisinesJson to list correctly") {
                    val restaurant = mapper.restaurantToDomain(entity)
                    restaurant.cuisines shouldHaveSize 2
                    restaurant.cuisines shouldBe listOf("Biryani", "South Indian")
                }

                it("parses offersJson to list correctly") {
                    val restaurant = mapper.restaurantToDomain(entity)
                    restaurant.offers shouldHaveSize 1
                    restaurant.offers.first() shouldBe "50% off"
                }

                it("parses highlightsJson to list correctly") {
                    val restaurant = mapper.restaurantToDomain(entity)
                    restaurant.highlights shouldHaveSize 2
                    restaurant.highlights shouldBe listOf("Dine-in", "Takeaway")
                }
            }

            context("empty JSON lists") {

                val entity = RestaurantEntity(
                    id              = "r1",
                    name            = "Test",
                    imageUrl        = "",
                    thumbUrl        = "",
                    rating          = 4.0,
                    ratingText      = "",
                    ratingColor     = "",
                    totalVotes      = 0,
                    avgDeliveryTime = 30,
                    deliveryFee     = 0.0,
                    minOrder        = 0,
                    cuisinesJson    = "[]",
                    address         = "",
                    locality        = "",
                    isOpen          = true,
                    hasDelivery     = true,
                    offersJson      = "[]",
                    avgCostForTwo   = 0,
                    distanceKm      = 0.0,
                    phoneNumber     = "",
                    openingHours    = "",
                    highlightsJson  = "[]",
                    knownFor        = "",
                )

                // WHY test empty JSON?
                // fromJsonStringList("[]") must return emptyList() not null or crash
                it("empty cuisinesJson parses to empty list") {
                    mapper.restaurantToDomain(entity).cuisines.shouldBeEmpty()
                }

                it("empty offersJson parses to empty list") {
                    mapper.restaurantToDomain(entity).offers.shouldBeEmpty()
                }

                it("empty highlightsJson parses to empty list") {
                    mapper.restaurantToDomain(entity).highlights.shouldBeEmpty()
                }
            }
        }

        // ── restaurantToEntity ────────────────────────────────

        describe("restaurantToEntity") {

            val domain = Restaurant(
                id              = "r1",
                name            = "Meghana Foods",
                imageUrl        = "https://img.jpg",
                thumbUrl        = "https://thumb.jpg",
                rating          = 4.5,
                ratingText      = "Excellent",
                ratingColor     = "5BA829",
                totalVotes      = 1000,
                avgDeliveryTime = 30,
                deliveryFee     = 30.0,
                minOrder        = 100,
                cuisines        = listOf("Biryani", "South Indian"),
                address         = "Koramangala, Bengaluru",
                locality        = "Koramangala",
                isOpen          = true,
                hasDelivery     = true,
                offers          = listOf("50% off"),
                avgCostForTwo   = 600,
                distanceKm      = 1.5,
                phoneNumber     = "+911234567890",
                openingHours    = "11 AM - 11 PM",
                highlights      = listOf("Dine-in"),
                knownFor        = "Biryani",
            )

            it("maps id correctly") {
                mapper.restaurantToEntity(domain).id shouldBe "r1"
            }

            it("maps name correctly") {
                mapper.restaurantToEntity(domain).name shouldBe "Meghana Foods"
            }

            it("maps rating correctly") {
                mapper.restaurantToEntity(domain).rating shouldBe 4.5
            }

            it("serialises cuisines to JSON string") {
                val entity = mapper.restaurantToEntity(domain)
                entity.cuisinesJson shouldBe """["Biryani","South Indian"]"""
            }

            it("serialises offers to JSON string") {
                val entity = mapper.restaurantToEntity(domain)
                entity.offersJson shouldBe """["50% off"]"""
            }

            it("serialises highlights to JSON string") {
                val entity = mapper.restaurantToEntity(domain)
                entity.highlightsJson shouldBe """["Dine-in"]"""
            }

            // WHY test round-trip?
            // toEntity → toDomain must produce identical domain object
            // If any field is lost in serialisation → round-trip fails
            it("round-trip restaurantToEntity then restaurantToDomain is lossless") {
                val entity = mapper.restaurantToEntity(domain)
                val result = mapper.restaurantToDomain(entity)
                result.id       shouldBe domain.id
                result.name     shouldBe domain.name
                result.cuisines shouldBe domain.cuisines
                result.offers   shouldBe domain.offers
                result.rating   shouldBe domain.rating
            }
        }

        // ── cartItemToEntity ──────────────────────────────────

        describe("cartItemToEntity") {

            val menuItem = MenuItem(
                id             = "m1",
                restaurantId   = "r1",
                name           = "Chicken Biryani",
                description    = "Delicious",
                price          = 249.0,
                imageUrl       = "",
                category       = "Biryani",
                isVeg          = false,
                isRecommended  = true,
                isBestseller   = true,
                isAvailable    = true,
                customisations = emptyList(),
            )

            val cartItem = CartItem(
                id                     = "c1",
                menuItem               = menuItem,
                quantity               = 2,
                selectedCustomisations = emptyList(),
            )

            it("maps cart item id correctly") {
                mapper.cartItemToEntity(cartItem).id shouldBe "c1"
            }

            it("maps menuItemId from menuItem.id") {
                mapper.cartItemToEntity(cartItem).menuItemId shouldBe "m1"
            }

            it("maps quantity correctly") {
                mapper.cartItemToEntity(cartItem).quantity shouldBe 2
            }

            it("serialises menuItem to non-empty JSON") {
                val entity = mapper.cartItemToEntity(cartItem)
                entity.menuItemJson shouldNotBe null
                entity.menuItemJson.contains("Chicken Biryani") shouldBe true
            }

            it("serialises empty customisations to empty JSON array") {
                val entity = mapper.cartItemToEntity(cartItem)
                entity.customisationsJson shouldBe "[]"
            }

            it("serialises customisations to JSON when present") {
                val option = CustomisationOption(
                    id         = "opt1",
                    label      = "Large",
                    extraPrice = 50.0,
                )
                val itemWithCustom = cartItem.copy(
                    selectedCustomisations = listOf(option)
                )
                val entity = mapper.cartItemToEntity(itemWithCustom)
                entity.customisationsJson.contains("Large") shouldBe true
                entity.customisationsJson.contains("50.0")  shouldBe true
            }
        }

        // ── cartItemToDomain ──────────────────────────────────

        describe("cartItemToDomain") {

            val menuItemJson = """
                {
                    "id": "m1",
                    "restaurantId": "r1",
                    "name": "Chicken Biryani",
                    "description": "Delicious",
                    "price": 249.0,
                    "imageUrl": "",
                    "category": "Biryani",
                    "isVeg": false,
                    "isRecommended": true,
                    "isBestseller": true,
                    "isAvailable": true,
                    "customisations": []
                }
            """.trimIndent()

            val entity = CartItemEntity(
                id                 = "c1",
                menuItemId         = "m1",
                menuItemJson       = menuItemJson,
                quantity           = 2,
                customisationsJson = "[]",
            )

            it("maps id correctly") {
                mapper.cartItemToDomain(entity).id shouldBe "c1"
            }

            it("maps quantity correctly") {
                mapper.cartItemToDomain(entity).quantity shouldBe 2
            }

            it("parses menuItemJson to MenuItem") {
                val cartItem = mapper.cartItemToDomain(entity)
                cartItem.menuItem.id   shouldBe "m1"
                cartItem.menuItem.name shouldBe "Chicken Biryani"
                cartItem.menuItem.price shouldBe 249.0
            }

            it("parses empty customisationsJson to empty list") {
                mapper.cartItemToDomain(entity)
                    .selectedCustomisations.shouldBeEmpty()
            }

            it("parses customisations JSON when present") {
                val customJson = """
                    [{"id":"opt1","label":"Large","extraPrice":50.0}]
                """.trimIndent()
                val entityWithCustom = entity.copy(customisationsJson = customJson)
                val cartItem = mapper.cartItemToDomain(entityWithCustom)

                cartItem.selectedCustomisations shouldHaveSize 1
                cartItem.selectedCustomisations.first().id         shouldBe "opt1"
                cartItem.selectedCustomisations.first().label      shouldBe "Large"
                cartItem.selectedCustomisations.first().extraPrice shouldBe 50.0
            }

            // WHY test round-trip for cart?
            // cartItemToEntity → cartItemToDomain must preserve all fields
            // Critical: totalPrice depends on menuItem.price being preserved
            it("round-trip cartItemToEntity then cartItemToDomain is lossless") {
                val menuItem = MenuItem(
                    id             = "m1",
                    restaurantId   = "r1",
                    name           = "Chicken Biryani",
                    description    = "Delicious",
                    price          = 249.0,
                    imageUrl       = "",
                    category       = "Biryani",
                    isVeg          = false,
                    isRecommended  = true,
                    isBestseller   = true,
                    isAvailable    = true,
                    customisations = emptyList(),
                )
                val original = CartItem(
                    id                     = "c1",
                    menuItem               = menuItem,
                    quantity               = 2,
                    selectedCustomisations = emptyList(),
                )
                val roundTripped = mapper.cartItemToDomain(
                    mapper.cartItemToEntity(original)
                )

                roundTripped.id              shouldBe original.id
                roundTripped.quantity        shouldBe original.quantity
                roundTripped.menuItem.id     shouldBe original.menuItem.id
                roundTripped.menuItem.price  shouldBe original.menuItem.price
                roundTripped.totalPrice      shouldBe original.totalPrice
            }
        }

        // ── menuItemToEntity ──────────────────────────────────

        describe("menuItemToEntity") {

            val domain = MenuItem(
                id             = "m1",
                restaurantId   = "r1",
                name           = "Chicken Biryani",
                description    = "Delicious",
                price          = 249.0,
                imageUrl       = "https://img.jpg",
                category       = "Biryani",
                isVeg          = false,
                isRecommended  = true,
                isBestseller   = true,
                isAvailable    = true,
                customisations = listOf(
                    Customisation(
                        id      = "size_group",
                        name    = "Size",
                        options = listOf(
                            CustomisationOption("regular", "Regular", 0.0),
                            CustomisationOption("large",   "Large",   50.0),
                        ),
                    )
                ),
            )

            it("maps id correctly") {
                mapper.menuItemToEntity(domain).id shouldBe "m1"
            }

            it("maps restaurantId correctly") {
                mapper.menuItemToEntity(domain).restaurantId shouldBe "r1"
            }

            it("maps name correctly") {
                mapper.menuItemToEntity(domain).name shouldBe "Chicken Biryani"
            }

            it("maps price correctly") {
                mapper.menuItemToEntity(domain).price shouldBe 249.0
            }

            it("maps isVeg correctly") {
                mapper.menuItemToEntity(domain).isVeg shouldBe false
            }

            it("maps isBestseller correctly") {
                mapper.menuItemToEntity(domain).isBestseller shouldBe true
            }

            it("maps isAvailable correctly") {
                mapper.menuItemToEntity(domain).isAvailable shouldBe true
            }

            it("serialises customisations to JSON") {
                val entity = mapper.menuItemToEntity(domain)
                entity.customisationsJson.contains("size_group") shouldBe true
                entity.customisationsJson.contains("Large")      shouldBe true
            }

            it("serialises empty customisations to empty JSON array") {
                val simpleItem = domain.copy(customisations = emptyList())
                mapper.menuItemToEntity(simpleItem).customisationsJson shouldBe "[]"
            }
        }

        // ── menuItemToDomain ──────────────────────────────────

        describe("menuItemToDomain") {

            val entity = MenuItemEntity(
                id                 = "m1",
                restaurantId       = "r1",
                name               = "Chicken Biryani",
                description        = "Delicious",
                price              = 249.0,
                imageUrl           = "",
                category           = "Biryani",
                isVeg              = false,
                isRecommended      = true,
                isBestseller       = true,
                isAvailable        = true,
                customisationsJson = """
                    [{"id":"size_group","name":"Size","options":[
                        {"id":"regular","label":"Regular","extraPrice":0.0},
                        {"id":"large","label":"Large","extraPrice":50.0}
                    ]}]
                """.trimIndent(),
            )

            it("maps id correctly") {
                mapper.menuItemToDomain(entity).id shouldBe "m1"
            }

            it("maps name correctly") {
                mapper.menuItemToDomain(entity).name shouldBe "Chicken Biryani"
            }

            it("maps price correctly") {
                mapper.menuItemToDomain(entity).price shouldBe 249.0
            }

            it("maps isVeg correctly") {
                mapper.menuItemToDomain(entity).isVeg shouldBe false
            }

            it("maps isBestseller correctly") {
                mapper.menuItemToDomain(entity).isBestseller shouldBe true
            }

            it("parses customisationsJson to list") {
                val menuItem = mapper.menuItemToDomain(entity)
                menuItem.customisations shouldHaveSize 1
                menuItem.customisations.first().id   shouldBe "size_group"
                menuItem.customisations.first().name shouldBe "Size"
            }

            it("parses customisation options correctly") {
                val menuItem = mapper.menuItemToDomain(entity)
                val options  = menuItem.customisations.first().options
                options shouldHaveSize 2
                options.first().id         shouldBe "regular"
                options.last().id          shouldBe "large"
                options.last().extraPrice  shouldBe 50.0
            }

            it("parses empty customisationsJson to empty list") {
                val simpleEntity = entity.copy(customisationsJson = "[]")
                mapper.menuItemToDomain(simpleEntity)
                    .customisations.shouldBeEmpty()
            }

            it("round-trip menuItemToEntity then menuItemToDomain is lossless") {
                val domain = MenuItem(
                    id             = "m1",
                    restaurantId   = "r1",
                    name           = "Chicken Biryani",
                    description    = "Delicious",
                    price          = 249.0,
                    imageUrl       = "",
                    category       = "Biryani",
                    isVeg          = false,
                    isRecommended  = true,
                    isBestseller   = true,
                    isAvailable    = true,
                    customisations = emptyList(),
                )
                val result = mapper.menuItemToDomain(mapper.menuItemToEntity(domain))

                result.id          shouldBe domain.id
                result.name        shouldBe domain.name
                result.price       shouldBe domain.price
                result.isVeg       shouldBe domain.isVeg
                result.isBestseller shouldBe domain.isBestseller
            }
        }

        // ── userToEntity ──────────────────────────────────────

        describe("userToEntity") {

            val domain = User(
                id               = "u1",
                name             = "Swapna",
                email            = "swapna@example.com",
                phone            = "+919876543210",
                profileImage     = "https://img.jpg",
                addresses        = listOf(
                    Address(
                        id          = "a1",
                        label       = "Home",
                        fullAddress = "123 Main St",
                        landmark    = "Near Park",
                        latitude    = 12.93,
                        longitude   = 77.62,
                    )
                ),
                selectedLocation = "Koramangala",
            )

            it("maps id correctly") {
                mapper.userToEntity(domain).id shouldBe "u1"
            }

            it("maps name correctly") {
                mapper.userToEntity(domain).name shouldBe "Swapna"
            }

            it("maps email correctly") {
                mapper.userToEntity(domain).email shouldBe "swapna@example.com"
            }

            it("maps phone correctly") {
                mapper.userToEntity(domain).phone shouldBe "+919876543210"
            }

            it("maps selectedLocation correctly") {
                mapper.userToEntity(domain).selectedLocation shouldBe "Koramangala"
            }

            it("serialises addresses to JSON") {
                val entity = mapper.userToEntity(domain)
                entity.addressesJson.contains("Home") shouldBe true
                entity.addressesJson.contains("123 Main St") shouldBe true
            }

            it("serialises empty addresses to empty JSON array") {
                val userNoAddresses = domain.copy(addresses = emptyList())
                mapper.userToEntity(userNoAddresses).addressesJson shouldBe "[]"
            }
        }

        // ── userToDomain ──────────────────────────────────────

        describe("userToDomain") {

            val entity = UserEntity(
                id               = "u1",
                name             = "Swapna",
                email            = "swapna@example.com",
                phone            = "+919876543210",
                profileImage     = "",
                addressesJson    = """
                    [{"id":"a1","label":"Home","fullAddress":"123 Main St",
                      "landmark":"Near Park","latitude":12.93,"longitude":77.62}]
                """.trimIndent(),
                selectedLocation = "Koramangala",
            )

            it("maps id correctly") {
                mapper.userToDomain(entity).id shouldBe "u1"
            }

            it("maps name correctly") {
                mapper.userToDomain(entity).name shouldBe "Swapna"
            }

            it("maps selectedLocation correctly") {
                mapper.userToDomain(entity).selectedLocation shouldBe "Koramangala"
            }

            it("parses addressesJson to list") {
                val user = mapper.userToDomain(entity)
                user.addresses shouldHaveSize 1
                user.addresses.first().id    shouldBe "a1"
                user.addresses.first().label shouldBe "Home"
            }

            it("parses address fields correctly") {
                val address = mapper.userToDomain(entity).addresses.first()
                address.fullAddress shouldBe "123 Main St"
                address.landmark    shouldBe "Near Park"
                address.latitude    shouldBe 12.93
                address.longitude   shouldBe 77.62
            }

            it("parses empty addressesJson to empty list") {
                val emptyEntity = entity.copy(addressesJson = "[]")
                mapper.userToDomain(emptyEntity).addresses.shouldBeEmpty()
            }

            it("round-trip userToEntity then userToDomain is lossless") {
                val domain = User(
                    id               = "u1",
                    name             = "Swapna",
                    email            = "swapna@example.com",
                    phone            = "+919876543210",
                    profileImage     = "",
                    addresses        = listOf(
                        Address("a1", "Home", "123 Main St", "Near Park", 12.93, 77.62)
                    ),
                    selectedLocation = "Koramangala",
                )
                val result = mapper.userToDomain(mapper.userToEntity(domain))

                result.id               shouldBe domain.id
                result.name             shouldBe domain.name
                result.selectedLocation shouldBe domain.selectedLocation
                result.addresses.size   shouldBe domain.addresses.size
                result.addresses.first().label shouldBe "Home"
            }
        }
    }
})