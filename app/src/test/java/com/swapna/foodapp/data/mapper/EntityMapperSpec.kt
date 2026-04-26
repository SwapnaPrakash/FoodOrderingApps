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
import com.swapna.foodapp.utils.TestConstants.ADDRESS_ID_1
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LANDMARK_PARK
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LAT_12_93
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LNG_77_62
import com.swapna.foodapp.utils.TestConstants.ADDRESS_MAIN_ST
import com.swapna.foodapp.utils.TestConstants.CART_ID_1
import com.swapna.foodapp.utils.TestConstants.CART_QTY_2
import com.swapna.foodapp.utils.TestConstants.CUISINES_JSON_BIRYANI_SI
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_GROUP_SIZE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_LARGE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_REGULAR
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_NAME_SIZE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_LARGE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_REGULAR
import com.swapna.foodapp.utils.TestConstants.DELIVERY_TIME
import com.swapna.foodapp.utils.TestConstants.DISTANCE_KM_1_5
import com.swapna.foodapp.utils.TestConstants.EMPTY_JSON_ARRAY
import com.swapna.foodapp.utils.TestConstants.ENTITY_COST_TWO
import com.swapna.foodapp.utils.TestConstants.ENTITY_NAME_TEST
import com.swapna.foodapp.utils.TestConstants.ENTITY_RATING_COLOR
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_LARGE
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_ZERO
import com.swapna.foodapp.utils.TestConstants.FAKE_RESTAURANT_VOTES
import com.swapna.foodapp.utils.TestConstants.HIGHLIGHTS_JSON_ONE
import com.swapna.foodapp.utils.TestConstants.HIGHLIGHTS_JSON_TWO
import com.swapna.foodapp.utils.TestConstants.HIGHLIGHT_DINE_IN
import com.swapna.foodapp.utils.TestConstants.HIGHLIGHT_TAKEAWAY
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.HOME_DELIVERY_FEE
import com.swapna.foodapp.utils.TestConstants.HOME_OFFERS_50_OFF
import com.swapna.foodapp.utils.TestConstants.HOME_RATING_TEXT_EXCELLENT
import com.swapna.foodapp.utils.TestConstants.HOME_SOUTH_INDIAN
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.MENU_DESCRIPTION_SHORT
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.OFFERS_JSON_50_OFF
import com.swapna.foodapp.utils.TestConstants.OPENING_HOURS_DEFAULT
import com.swapna.foodapp.utils.TestConstants.OPT_ID_1
import com.swapna.foodapp.utils.TestConstants.PHONE_11_DIGITS
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ADDRESS_KORA
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MEGHANA
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MIN_ORDER_100
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_RATING_45
import com.swapna.foodapp.utils.TestConstants.REVIEW_IMG_URL
import com.swapna.foodapp.utils.TestConstants.THUMB_URL_HTTPS
import com.swapna.foodapp.utils.TestConstants.USER_EMAIL_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_ID_1
import com.swapna.foodapp.utils.TestConstants.USER_NAME_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_PHONE_VALID
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class EntityMapperSpec : DescribeSpec({

    val mapper = EntityMapper()

    describe("EntityMapper") {

        describe("restaurantToDomain") {

            context("basic restaurant entity") {

                val entity = RestaurantEntity(
                    id = RESTAURANT_ID_1,
                    name = RESTAURANT_MEGHANA,
                    imageUrl = REVIEW_IMG_URL,
                    thumbUrl = THUMB_URL_HTTPS,
                    rating = RESTAURANT_RATING_45,
                    ratingText = HOME_RATING_TEXT_EXCELLENT,
                    ratingColor = ENTITY_RATING_COLOR,
                    totalVotes = FAKE_RESTAURANT_VOTES,
                    avgDeliveryTime = DELIVERY_TIME,
                    deliveryFee = HOME_DELIVERY_FEE,
                    minOrder = RESTAURANT_MIN_ORDER_100,
                    cuisinesJson = CUISINES_JSON_BIRYANI_SI,
                    address = RESTAURANT_ADDRESS_KORA,
                    locality = LOC_KORAMANGALA,
                    isOpen = true,
                    hasDelivery = true,
                    offersJson = OFFERS_JSON_50_OFF,
                    avgCostForTwo = ENTITY_COST_TWO,
                    distanceKm = DISTANCE_KM_1_5,
                    phoneNumber = PHONE_11_DIGITS,
                    openingHours = OPENING_HOURS_DEFAULT,
                    highlightsJson = HIGHLIGHTS_JSON_TWO,
                    knownFor = HOME_CATEGORY_BIRYANI,
                )

                it("maps id correctly") {
                    mapper.restaurantToDomain(entity).id shouldBe RESTAURANT_ID_1
                }

                it("maps name correctly") {
                    mapper.restaurantToDomain(entity).name shouldBe RESTAURANT_MEGHANA
                }

                it("maps rating correctly") {
                    mapper.restaurantToDomain(entity).rating shouldBe RESTAURANT_RATING_45
                }

                it("maps avgDeliveryTime correctly") {
                    mapper.restaurantToDomain(entity).avgDeliveryTime shouldBe DELIVERY_TIME
                }

                it("maps locality correctly") {
                    mapper.restaurantToDomain(entity).locality shouldBe LOC_KORAMANGALA
                }

                it("maps isOpen correctly") {
                    mapper.restaurantToDomain(entity).isOpen shouldBe true
                }

                it("maps avgCostForTwo correctly") {
                    mapper.restaurantToDomain(entity).avgCostForTwo shouldBe ENTITY_COST_TWO
                }

                it("maps distanceKm correctly") {
                    mapper.restaurantToDomain(entity).distanceKm shouldBe DISTANCE_KM_1_5
                }

                it("maps phoneNumber correctly") {
                    mapper.restaurantToDomain(entity).phoneNumber shouldBe PHONE_11_DIGITS
                }

                it("maps openingHours correctly") {
                    mapper.restaurantToDomain(entity).openingHours shouldBe OPENING_HOURS_DEFAULT
                }

                it("maps knownFor correctly") {
                    mapper.restaurantToDomain(entity).knownFor shouldBe HOME_CATEGORY_BIRYANI
                }

                it("parses cuisinesJson to list correctly") {
                    val restaurant = mapper.restaurantToDomain(entity)
                    restaurant.cuisines shouldHaveSize 2
                    restaurant.cuisines shouldBe listOf(
                        HOME_CATEGORY_BIRYANI,
                        HOME_SOUTH_INDIAN
                    )
                }

                it("parses offersJson to list correctly") {
                    val restaurant = mapper.restaurantToDomain(entity)
                    restaurant.offers shouldHaveSize 1
                    restaurant.offers.first() shouldBe HOME_OFFERS_50_OFF
                }

                it("parses highlightsJson to list correctly") {
                    val restaurant = mapper.restaurantToDomain(entity)
                    restaurant.highlights shouldHaveSize 2
                    restaurant.highlights shouldBe listOf(
                        HIGHLIGHT_DINE_IN,
                        HIGHLIGHT_TAKEAWAY
                    )
                }
            }

            context("empty JSON lists") {

                val entity = RestaurantEntity(
                    id = RESTAURANT_ID_1,
                    name = ENTITY_NAME_TEST,
                    imageUrl = "",
                    thumbUrl = "",
                    rating = RESTAURANT_RATING_45,
                    ratingText = "",
                    ratingColor = "",
                    totalVotes = 0,
                    avgDeliveryTime = DELIVERY_TIME,
                    deliveryFee = EXTRA_PRICE_ZERO,
                    minOrder = 0,
                    cuisinesJson = EMPTY_JSON_ARRAY,
                    address = "",
                    locality = "",
                    isOpen = true,
                    hasDelivery = true,
                    offersJson = EMPTY_JSON_ARRAY,
                    avgCostForTwo = 0,
                    distanceKm = EXTRA_PRICE_ZERO,
                    phoneNumber = "",
                    openingHours = "",
                    highlightsJson = EMPTY_JSON_ARRAY,
                    knownFor = "",
                )

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

        describe("restaurantToEntity") {

            val domain = Restaurant(
                id = RESTAURANT_ID_1,
                name = RESTAURANT_MEGHANA,
                imageUrl = REVIEW_IMG_URL,
                thumbUrl = THUMB_URL_HTTPS,
                rating = RESTAURANT_RATING_45,
                ratingText = HOME_RATING_TEXT_EXCELLENT,
                ratingColor = ENTITY_RATING_COLOR,
                totalVotes = FAKE_RESTAURANT_VOTES,
                avgDeliveryTime = DELIVERY_TIME,
                deliveryFee = HOME_DELIVERY_FEE,
                minOrder = RESTAURANT_MIN_ORDER_100,
                cuisines = listOf(HOME_CATEGORY_BIRYANI, HOME_SOUTH_INDIAN),
                address = RESTAURANT_ADDRESS_KORA,
                locality = LOC_KORAMANGALA,
                isOpen = true,
                hasDelivery = true,
                offers = listOf(HOME_OFFERS_50_OFF),
                avgCostForTwo = ENTITY_COST_TWO,
                distanceKm = DISTANCE_KM_1_5,
                phoneNumber = PHONE_11_DIGITS,
                openingHours = OPENING_HOURS_DEFAULT,
                highlights = listOf(HIGHLIGHT_DINE_IN),
                knownFor = HOME_CATEGORY_BIRYANI,
            )

            it("maps id correctly") {
                mapper.restaurantToEntity(domain).id shouldBe RESTAURANT_ID_1
            }

            it("maps name correctly") {
                mapper.restaurantToEntity(domain).name shouldBe RESTAURANT_MEGHANA
            }

            it("maps rating correctly") {
                mapper.restaurantToEntity(domain).rating shouldBe RESTAURANT_RATING_45
            }

            it("serialises cuisines to JSON string") {
                val entity = mapper.restaurantToEntity(domain)
                entity.cuisinesJson shouldBe CUISINES_JSON_BIRYANI_SI
            }

            it("serialises offers to JSON string") {
                val entity = mapper.restaurantToEntity(domain)
                entity.offersJson shouldBe OFFERS_JSON_50_OFF
            }

            it("serialises highlights to JSON string") {
                val entity = mapper.restaurantToEntity(domain)
                entity.highlightsJson shouldBe HIGHLIGHTS_JSON_ONE
            }

            it("round-trip restaurantToEntity then restaurantToDomain is lossless") {
                val entity = mapper.restaurantToEntity(domain)
                val result = mapper.restaurantToDomain(entity)
                result.id shouldBe domain.id
                result.name shouldBe domain.name
                result.cuisines shouldBe domain.cuisines
                result.offers shouldBe domain.offers
                result.rating shouldBe domain.rating
            }
        }

        describe("cartItemToEntity") {

            val menuItem = MenuItem(
                id = MENU_ID_1,
                restaurantId = RESTAURANT_ID_1,
                name = MENU_ITEM_CHICK_BIR,
                description = MENU_DESCRIPTION_SHORT,
                price = PRICE_249,
                imageUrl = "",
                category = HOME_CATEGORY_BIRYANI,
                isVeg = false,
                isRecommended = true,
                isBestseller = true,
                isAvailable = true,
                customisations = emptyList(),
            )

            val cartItem = CartItem(
                id = CART_ID_1,
                menuItem = menuItem,
                quantity = CART_QTY_2,
                selectedCustomisations = emptyList(),
            )

            it("maps cart item id correctly") {
                mapper.cartItemToEntity(cartItem).id shouldBe CART_ID_1
            }

            it("maps menuItemId from menuItem.id") {
                mapper.cartItemToEntity(cartItem).menuItemId shouldBe MENU_ID_1
            }

            it("maps quantity correctly") {
                mapper.cartItemToEntity(cartItem).quantity shouldBe CART_QTY_2
            }

            it("serialises menuItem to non-empty JSON") {
                val entity = mapper.cartItemToEntity(cartItem)
                entity.menuItemJson shouldNotBe null
                entity.menuItemJson.contains(MENU_ITEM_CHICK_BIR) shouldBe true
            }

            it("serialises empty customisations to empty JSON array") {
                val entity = mapper.cartItemToEntity(cartItem)
                entity.customisationsJson shouldBe EMPTY_JSON_ARRAY
            }

            it("serialises customisations to JSON when present") {
                val option = CustomisationOption(
                    id = OPT_ID_1,
                    label = CUSTOMISE_LABEL_LARGE,
                    extraPrice = EXTRA_PRICE_LARGE,
                )
                val itemWithCustom = cartItem.copy(
                    selectedCustomisations = listOf(option)
                )
                val entity = mapper.cartItemToEntity(itemWithCustom)
                entity.customisationsJson.contains(CUSTOMISE_LABEL_LARGE) shouldBe true
                entity.customisationsJson.contains("50.0") shouldBe true
            }
        }

        describe("cartItemToDomain") {

            val menuItemJson = """
                {
                    "id": "${MENU_ID_1}",
                    "restaurantId": "${RESTAURANT_ID_1}",
                    "name": "${MENU_ITEM_CHICK_BIR}",
                    "description": "${MENU_DESCRIPTION_SHORT}",
                    "price": ${PRICE_249},
                    "imageUrl": "",
                    "category": "${HOME_CATEGORY_BIRYANI}",
                    "isVeg": false,
                    "isRecommended": true,
                    "isBestseller": true,
                    "isAvailable": true,
                    "customisations": []
                }
            """.trimIndent()

            val entity = CartItemEntity(
                id = CART_ID_1,
                menuItemId = MENU_ID_1,
                menuItemJson = menuItemJson,
                quantity = CART_QTY_2,
                customisationsJson = EMPTY_JSON_ARRAY,
            )

            it("maps id correctly") {
                mapper.cartItemToDomain(entity).id shouldBe CART_ID_1
            }

            it("maps quantity correctly") {
                mapper.cartItemToDomain(entity).quantity shouldBe CART_QTY_2
            }

            it("parses menuItemJson to MenuItem") {
                val cartItem = mapper.cartItemToDomain(entity)
                cartItem.menuItem.id shouldBe MENU_ID_1
                cartItem.menuItem.name shouldBe MENU_ITEM_CHICK_BIR
                cartItem.menuItem.price shouldBe PRICE_249
            }

            it("parses empty customisationsJson to empty list") {
                mapper.cartItemToDomain(entity)
                    .selectedCustomisations.shouldBeEmpty()
            }

            it("parses customisations JSON when present") {
                val customJson = """
                    [{"id":"${OPT_ID_1}","label":"${CUSTOMISE_LABEL_LARGE}","extraPrice":${EXTRA_PRICE_LARGE}}]
                """.trimIndent()
                val entityWithCustom = entity.copy(customisationsJson = customJson)
                val cartItem = mapper.cartItemToDomain(entityWithCustom)

                cartItem.selectedCustomisations shouldHaveSize 1
                cartItem.selectedCustomisations.first().id shouldBe OPT_ID_1
                cartItem.selectedCustomisations.first().label shouldBe CUSTOMISE_LABEL_LARGE
                cartItem.selectedCustomisations.first().extraPrice shouldBe EXTRA_PRICE_LARGE
            }

            it("round-trip cartItemToEntity then cartItemToDomain is lossless") {
                val menuItem = MenuItem(
                    id = MENU_ID_1,
                    restaurantId = RESTAURANT_ID_1,
                    name = MENU_ITEM_CHICK_BIR,
                    description = MENU_DESCRIPTION_SHORT,
                    price = PRICE_249,
                    imageUrl = "",
                    category = HOME_CATEGORY_BIRYANI,
                    isVeg = false,
                    isRecommended = true,
                    isBestseller = true,
                    isAvailable = true,
                    customisations = emptyList(),
                )
                val original = CartItem(
                    id = CART_ID_1,
                    menuItem = menuItem,
                    quantity = CART_QTY_2,
                    selectedCustomisations = emptyList(),
                )
                val roundTripped = mapper.cartItemToDomain(
                    mapper.cartItemToEntity(original)
                )

                roundTripped.id shouldBe original.id
                roundTripped.quantity shouldBe original.quantity
                roundTripped.menuItem.id shouldBe original.menuItem.id
                roundTripped.menuItem.price shouldBe original.menuItem.price
                roundTripped.totalPrice shouldBe original.totalPrice
            }
        }

        describe("menuItemToEntity") {

            val domain = MenuItem(
                id = MENU_ID_1,
                restaurantId = RESTAURANT_ID_1,
                name = MENU_ITEM_CHICK_BIR,
                description = MENU_DESCRIPTION_SHORT,
                price = PRICE_249,
                imageUrl = REVIEW_IMG_URL,
                category = HOME_CATEGORY_BIRYANI,
                isVeg = false,
                isRecommended = true,
                isBestseller = true,
                isAvailable = true,
                customisations = listOf(
                    Customisation(
                        id = CUSTOMISE_GROUP_SIZE,
                        name = CUSTOMISE_NAME_SIZE,
                        options = listOf(
                            CustomisationOption(
                                CUSTOMISE_OPT_REGULAR,
                                CUSTOMISE_LABEL_REGULAR,
                                EXTRA_PRICE_ZERO
                            ),
                            CustomisationOption(
                                CUSTOMISE_OPT_LARGE,
                                CUSTOMISE_LABEL_LARGE,
                                EXTRA_PRICE_LARGE
                            ),
                        ),
                    )
                ),
            )

            it("maps id correctly") {
                mapper.menuItemToEntity(domain).id shouldBe MENU_ID_1
            }

            it("maps restaurantId correctly") {
                mapper.menuItemToEntity(domain).restaurantId shouldBe RESTAURANT_ID_1
            }

            it("maps name correctly") {
                mapper.menuItemToEntity(domain).name shouldBe MENU_ITEM_CHICK_BIR
            }

            it("maps price correctly") {
                mapper.menuItemToEntity(domain).price shouldBe PRICE_249
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
                entity.customisationsJson.contains(CUSTOMISE_GROUP_SIZE) shouldBe true
                entity.customisationsJson.contains(CUSTOMISE_LABEL_LARGE) shouldBe true
            }

            it("serialises empty customisations to empty JSON array") {
                val simpleItem = domain.copy(customisations = emptyList())
                mapper.menuItemToEntity(simpleItem).customisationsJson shouldBe EMPTY_JSON_ARRAY
            }
        }

        describe("menuItemToDomain") {

            val entity = MenuItemEntity(
                id = MENU_ID_1,
                restaurantId = RESTAURANT_ID_1,
                name = MENU_ITEM_CHICK_BIR,
                description = MENU_DESCRIPTION_SHORT,
                price = PRICE_249,
                imageUrl = "",
                category = HOME_CATEGORY_BIRYANI,
                isVeg = false,
                isRecommended = true,
                isBestseller = true,
                isAvailable = true,
                customisationsJson = """
                    [{"id":"${CUSTOMISE_GROUP_SIZE}","name":"${CUSTOMISE_NAME_SIZE}","options":[
                        {"id":"${CUSTOMISE_OPT_REGULAR}","label":"${CUSTOMISE_LABEL_REGULAR}","extraPrice":${EXTRA_PRICE_ZERO}},
                        {"id":"${CUSTOMISE_OPT_LARGE}","label":"${CUSTOMISE_LABEL_LARGE}","extraPrice":${EXTRA_PRICE_LARGE}}
                    ]}]
                """.trimIndent(),
            )

            it("maps id correctly") {
                mapper.menuItemToDomain(entity).id shouldBe MENU_ID_1
            }

            it("maps name correctly") {
                mapper.menuItemToDomain(entity).name shouldBe MENU_ITEM_CHICK_BIR
            }

            it("maps price correctly") {
                mapper.menuItemToDomain(entity).price shouldBe PRICE_249
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
                menuItem.customisations.first().id shouldBe CUSTOMISE_GROUP_SIZE
                menuItem.customisations.first().name shouldBe CUSTOMISE_NAME_SIZE
            }

            it("parses customisation options correctly") {
                val menuItem = mapper.menuItemToDomain(entity)
                val options = menuItem.customisations.first().options
                options shouldHaveSize 2
                options.first().id shouldBe CUSTOMISE_OPT_REGULAR
                options.last().id shouldBe CUSTOMISE_OPT_LARGE
                options.last().extraPrice shouldBe EXTRA_PRICE_LARGE
            }

            it("parses empty customisationsJson to empty list") {
                val simpleEntity = entity.copy(customisationsJson = EMPTY_JSON_ARRAY)
                mapper.menuItemToDomain(simpleEntity).customisations.shouldBeEmpty()
            }

            it("round-trip menuItemToEntity then menuItemToDomain is lossless") {
                val domain = MenuItem(
                    id = MENU_ID_1,
                    restaurantId = RESTAURANT_ID_1,
                    name = MENU_ITEM_CHICK_BIR,
                    description = MENU_DESCRIPTION_SHORT,
                    price = PRICE_249,
                    imageUrl = "",
                    category = HOME_CATEGORY_BIRYANI,
                    isVeg = false,
                    isRecommended = true,
                    isBestseller = true,
                    isAvailable = true,
                    customisations = emptyList(),
                )
                val result = mapper.menuItemToDomain(mapper.menuItemToEntity(domain))

                result.id shouldBe domain.id
                result.name shouldBe domain.name
                result.price shouldBe domain.price
                result.isVeg shouldBe domain.isVeg
                result.isBestseller shouldBe domain.isBestseller
            }
        }

        describe("userToEntity") {

            val domain = User(
                id = USER_ID_1,
                name = USER_NAME_SWAPNA,
                email = USER_EMAIL_SWAPNA,
                phone = USER_PHONE_VALID,
                profileImage = REVIEW_IMG_URL,
                addresses = listOf(
                    Address(
                        id = ADDRESS_ID_1,
                        label = ADDRESS_LABEL_HOME,
                        fullAddress = ADDRESS_MAIN_ST,
                        landmark = ADDRESS_LANDMARK_PARK,
                        latitude = ADDRESS_LAT_12_93,
                        longitude = ADDRESS_LNG_77_62,
                    )
                ),
                selectedLocation = LOC_KORAMANGALA,
            )

            it("maps id correctly") {
                mapper.userToEntity(domain).id shouldBe USER_ID_1
            }

            it("maps name correctly") {
                mapper.userToEntity(domain).name shouldBe USER_NAME_SWAPNA
            }

            it("maps email correctly") {
                mapper.userToEntity(domain).email shouldBe USER_EMAIL_SWAPNA
            }

            it("maps phone correctly") {
                mapper.userToEntity(domain).phone shouldBe USER_PHONE_VALID
            }

            it("maps selectedLocation correctly") {
                mapper.userToEntity(domain).selectedLocation shouldBe LOC_KORAMANGALA
            }

            it("serialises addresses to JSON") {
                val entity = mapper.userToEntity(domain)
                entity.addressesJson.contains(ADDRESS_LABEL_HOME) shouldBe true
                entity.addressesJson.contains(ADDRESS_MAIN_ST) shouldBe true
            }

            it("serialises empty addresses to empty JSON array") {
                val userNoAddresses = domain.copy(addresses = emptyList())
                mapper.userToEntity(userNoAddresses).addressesJson shouldBe EMPTY_JSON_ARRAY
            }
        }

        describe("userToDomain") {

            val entity = UserEntity(
                id = USER_ID_1,
                name = USER_NAME_SWAPNA,
                email = USER_EMAIL_SWAPNA,
                phone = USER_PHONE_VALID,
                profileImage = "",
                addressesJson = """
                    [{"id":"${ADDRESS_ID_1}","label":"${ADDRESS_LABEL_HOME}","fullAddress":"${ADDRESS_MAIN_ST}",
                      "landmark":"${ADDRESS_LANDMARK_PARK}","latitude":${ADDRESS_LAT_12_93},"longitude":${ADDRESS_LNG_77_62}}]
                """.trimIndent(),
                selectedLocation = LOC_KORAMANGALA,
            )

            it("maps id correctly") {
                mapper.userToDomain(entity).id shouldBe USER_ID_1
            }

            it("maps name correctly") {
                mapper.userToDomain(entity).name shouldBe USER_NAME_SWAPNA
            }

            it("maps selectedLocation correctly") {
                mapper.userToDomain(entity).selectedLocation shouldBe LOC_KORAMANGALA
            }

            it("parses addressesJson to list") {
                val user = mapper.userToDomain(entity)
                user.addresses shouldHaveSize 1
                user.addresses.first().id shouldBe ADDRESS_ID_1
                user.addresses.first().label shouldBe ADDRESS_LABEL_HOME
            }

            it("parses address fields correctly") {
                val address = mapper.userToDomain(entity).addresses.first()
                address.fullAddress shouldBe ADDRESS_MAIN_ST
                address.landmark shouldBe ADDRESS_LANDMARK_PARK
                address.latitude shouldBe ADDRESS_LAT_12_93
                address.longitude shouldBe ADDRESS_LNG_77_62
            }

            it("parses empty addressesJson to empty list") {
                val emptyEntity = entity.copy(addressesJson = EMPTY_JSON_ARRAY)
                mapper.userToDomain(emptyEntity).addresses.shouldBeEmpty()
            }

            it("round-trip userToEntity then userToDomain is lossless") {
                val domain = User(
                    id = USER_ID_1,
                    name = USER_NAME_SWAPNA,
                    email = USER_EMAIL_SWAPNA,
                    phone = USER_PHONE_VALID,
                    profileImage = "",
                    addresses = listOf(
                        Address(
                            ADDRESS_ID_1, ADDRESS_LABEL_HOME, ADDRESS_MAIN_ST,
                            ADDRESS_LANDMARK_PARK, ADDRESS_LAT_12_93, ADDRESS_LNG_77_62
                        )
                    ),
                    selectedLocation = LOC_KORAMANGALA,
                )
                val result = mapper.userToDomain(mapper.userToEntity(domain))

                result.id shouldBe domain.id
                result.name shouldBe domain.name
                result.selectedLocation shouldBe domain.selectedLocation
                result.addresses.size shouldBe domain.addresses.size
                result.addresses.first().label shouldBe ADDRESS_LABEL_HOME
            }
        }
    }
})