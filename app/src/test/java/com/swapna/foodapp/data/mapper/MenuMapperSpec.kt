package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.CustomisationDto
import com.swapna.foodapp.data.remote.dto.CustomisationOptionDto
import com.swapna.foodapp.data.remote.dto.DailyMenuDto
import com.swapna.foodapp.data.remote.dto.DailyMenuResponse
import com.swapna.foodapp.data.remote.dto.DailyMenuWrapper
import com.swapna.foodapp.data.remote.dto.DishDto
import com.swapna.foodapp.data.remote.dto.DishWrapper
import com.swapna.foodapp.utils.TestConstants.API_IS_NOT_VEG
import com.swapna.foodapp.utils.TestConstants.API_IS_RECOMMENDED
import com.swapna.foodapp.utils.TestConstants.API_IS_VEG
import com.swapna.foodapp.utils.TestConstants.CATEGORY_EMPTY_NAME
import com.swapna.foodapp.utils.TestConstants.CATEGORY_EMPTY_SHORT
import com.swapna.foodapp.utils.TestConstants.CATEGORY_STARTERS
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_GROUP_SIZE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_GROUP_SPICE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_HOT
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_LARGE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_MEDIUM
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_MILD
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_LABEL_REGULAR
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_NAME_SIZE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_NAME_SPICE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_HOT
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_LARGE
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_MEDIUM
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_MILD
import com.swapna.foodapp.utils.TestConstants.CUSTOMISE_OPT_REGULAR
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_LARGE
import com.swapna.foodapp.utils.TestConstants.EXTRA_PRICE_ZERO
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.MENU_DESCRIPTION_BIRYANI
import com.swapna.foodapp.utils.TestConstants.MENU_DTO_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_DTO_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_1
import com.swapna.foodapp.utils.TestConstants.MENU_ID_2
import com.swapna.foodapp.utils.TestConstants.MENU_ID_3
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_65
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_CHICK_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_ITEM_MUTTON_BIR
import com.swapna.foodapp.utils.TestConstants.MENU_PRICE_STR
import com.swapna.foodapp.utils.TestConstants.PRICE_199
import com.swapna.foodapp.utils.TestConstants.PRICE_249
import com.swapna.foodapp.utils.TestConstants.PRICE_349
import com.swapna.foodapp.utils.TestConstants.PRICE_PARSED_99_5
import com.swapna.foodapp.utils.TestConstants.PRICE_PARSED_RS_449
import com.swapna.foodapp.utils.TestConstants.PRICE_STR_199
import com.swapna.foodapp.utils.TestConstants.PRICE_STR_249_RS
import com.swapna.foodapp.utils.TestConstants.PRICE_STR_349
import com.swapna.foodapp.utils.TestConstants.PRICE_STR_349_RUPEE
import com.swapna.foodapp.utils.TestConstants.PRICE_STR_99_50
import com.swapna.foodapp.utils.TestConstants.PRICE_STR_INVALID
import com.swapna.foodapp.utils.TestConstants.PRICE_STR_RS_449
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_2
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_ID_R99
import com.swapna.foodapp.utils.TestConstants.REVIEW_IMG_URL
import com.swapna.foodapp.utils.TestConstants.SINGLE_ITEM_MENU_ID
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class MenuMapperSpec : DescribeSpec({

    val mapper = MenuMapper()

    fun singleItemResponse(
        category: String = HOME_CATEGORY_BIRYANI,
        id: String = MENU_ID_1,
        name: String = MENU_ITEM_CHICK_BIR,
        price: String = MENU_PRICE_STR,
        isVeg: Int = API_IS_NOT_VEG,
        isRecommended: Int = API_IS_RECOMMENDED,
        imageUrl: String? = null,
        customisations: List<CustomisationDto>? = null,
        description: String = "",
    ) = DailyMenuResponse(
        dailyMenus = listOf(
            DailyMenuWrapper(
                menu = DailyMenuDto(
                    id = SINGLE_ITEM_MENU_ID,
                    name = category,
                    dishes = listOf(
                        DishWrapper(
                            dish = DishDto(
                                id = id,
                                name = name,
                                price = price,
                                description = description,
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

    describe("MenuMapper") {

        describe("toDomain") {

            context("response with single category and single item") {

                val response = singleItemResponse(
                    price = PRICE_STR_249_RS,
                    isVeg = API_IS_NOT_VEG,
                    isRecommended = API_IS_RECOMMENDED,
                )

                it("returns map with one category") {
                    mapper.toDomain(response, RESTAURANT_ID_1).size shouldBe 1
                }

                it("category key is Biryani") {
                    mapper.toDomain(response, RESTAURANT_ID_1)
                        .containsKey(HOME_CATEGORY_BIRYANI) shouldBe true
                }

                it("category has one item") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]
                        ?.shouldHaveSize(1)
                }

                it("maps item id correctly") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().id shouldBe MENU_ID_1
                }

                it("maps item name correctly") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().name shouldBe MENU_ITEM_CHICK_BIR
                }

                it("maps restaurantId from parameter") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().restaurantId shouldBe RESTAURANT_ID_1
                }

                it("maps category to item.category") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().category shouldBe HOME_CATEGORY_BIRYANI
                }

                it("maps description correctly") {
                    val r =
                        singleItemResponse(description = MENU_DESCRIPTION_BIRYANI)
                    mapper.toDomain(r, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().description shouldBe MENU_DESCRIPTION_BIRYANI
                }

                it("maps isBestseller to false always — not from API") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().isBestseller shouldBe false
                }

                it("maps isAvailable to true always") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().isAvailable shouldBe true
                }

                it("null customisations maps to empty list") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations.shouldBeEmpty()
                }
            }

            context("restaurantId parameter") {

                it("maps restaurantId r1 correctly") {
                    mapper.toDomain(singleItemResponse(), RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().restaurantId shouldBe RESTAURANT_ID_1
                }

                it("maps restaurantId r99 correctly — different id") {
                    mapper.toDomain(
                        singleItemResponse(),
                        RESTAURANT_ID_R99
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().restaurantId shouldBe RESTAURANT_ID_R99
                }
            }

            // ══════════════════════════════════════════════════
            // isVeg — Int → Boolean mapping
            // ══════════════════════════════════════════════════

            context("isVeg flag mapping — Int to Boolean") {

                it("isVeg 1 maps to true") {
                    mapper.toDomain(
                        singleItemResponse(isVeg = API_IS_VEG),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().isVeg shouldBe true
                }

                it("isVeg 0 maps to false") {
                    mapper.toDomain(
                        singleItemResponse(isVeg = API_IS_NOT_VEG),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().isVeg shouldBe false
                }
            }

            context("isRecommended flag mapping — Int to Boolean") {

                it("isRecommended 1 maps to true") {
                    mapper.toDomain(
                        singleItemResponse(isRecommended = API_IS_RECOMMENDED),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().isRecommended shouldBe true
                }

                it("isRecommended 0 maps to false") {
                    mapper.toDomain(
                        singleItemResponse(isRecommended = API_IS_NOT_VEG),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().isRecommended shouldBe false
                }
            }

            context("price string parsing") {

                it("parses '249 Rs.' to 249.0") {
                    mapper.toDomain(
                        singleItemResponse(price = PRICE_STR_249_RS),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().price shouldBe PRICE_249
                }

                it("parses '₹349' to 349.0") {
                    mapper.toDomain(
                        singleItemResponse(price = PRICE_STR_349_RUPEE),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().price shouldBe PRICE_349
                }

                it("parses '99.50' to 99.5") {
                    mapper.toDomain(
                        singleItemResponse(price = PRICE_STR_99_50),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().price shouldBe PRICE_PARSED_99_5
                }

                it("parses plain number '199' to 199.0") {
                    mapper.toDomain(
                        singleItemResponse(price = PRICE_STR_199),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().price shouldBe PRICE_199
                }

                it("parses 'Rs. 449' to 0.449 — dot from Rs. is kept") {
                    mapper.toDomain(
                        singleItemResponse(price = PRICE_STR_RS_449),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().price shouldBe PRICE_PARSED_RS_449
                }

                it("parses invalid price string 'N/A' to 0.0") {
                    mapper.toDomain(
                        singleItemResponse(price = PRICE_STR_INVALID),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().price shouldBe EXTRA_PRICE_ZERO
                }

                it("parses empty price string to 0.0") {
                    mapper.toDomain(
                        singleItemResponse(price = ""),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().price shouldBe EXTRA_PRICE_ZERO
                }
            }

            context("imageUrl nullable mapping") {

                it("null imageUrl maps to empty string") {
                    mapper.toDomain(
                        singleItemResponse(imageUrl = null),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!
                        .first().imageUrl shouldBe ""
                }

                it("non-null imageUrl maps correctly") {
                    mapper.toDomain(
                        singleItemResponse(imageUrl = REVIEW_IMG_URL),
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]!!.first().imageUrl shouldBe REVIEW_IMG_URL
                }
            }

            context("multiple categories") {

                val response = DailyMenuResponse(
                    dailyMenus = listOf(
                        DailyMenuWrapper(
                            DailyMenuDto(
                                id = MENU_DTO_ID_1,
                                name = HOME_CATEGORY_BIRYANI,
                                dishes = listOf(
                                    DishWrapper(
                                        DishDto(
                                            id = MENU_ID_1,
                                            name = MENU_ITEM_CHICK_BIR,
                                            price = MENU_PRICE_STR,
                                            description = "",
                                            isVeg = API_IS_NOT_VEG,
                                            isRecommended = API_IS_RECOMMENDED,
                                        )
                                    ),
                                    DishWrapper(
                                        DishDto(
                                            id = MENU_ID_2,
                                            name = MENU_ITEM_MUTTON_BIR,
                                            price = PRICE_STR_349,
                                            description = "",
                                            isVeg = API_IS_NOT_VEG,
                                            isRecommended = API_IS_NOT_VEG,
                                        )
                                    ),
                                ),
                            )
                        ),
                        DailyMenuWrapper(
                            DailyMenuDto(
                                id = MENU_DTO_ID_2,
                                name = CATEGORY_STARTERS,
                                dishes = listOf(
                                    DishWrapper(
                                        DishDto(
                                            id = MENU_ID_3,
                                            name = MENU_ITEM_CHICK_65,
                                            price = PRICE_STR_199,
                                            description = "",
                                            isVeg = API_IS_NOT_VEG,
                                            isRecommended = API_IS_RECOMMENDED,
                                        )
                                    ),
                                ),
                            )
                        ),
                    )
                )

                it("returns map with 2 categories") {
                    mapper.toDomain(response, RESTAURANT_ID_1).size shouldBe 2
                }

                it("Biryani category has 2 items") {
                    mapper.toDomain(
                        response,
                        RESTAURANT_ID_1
                    )[HOME_CATEGORY_BIRYANI]?.shouldHaveSize(2)
                }

                it("Starters category has 1 item") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[CATEGORY_STARTERS]?.shouldHaveSize(1)
                }

                it("Biryani first item is Chicken Biryani") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().name shouldBe MENU_ITEM_CHICK_BIR
                }

                it("Starters first item is Chicken 65") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[CATEGORY_STARTERS]!!
                        .first().name shouldBe MENU_ITEM_CHICK_65
                }

                it("all items have correct restaurantId") {
                    val menu = mapper.toDomain(response, RESTAURANT_ID_2)
                    menu.values.flatten()
                        .all { it.restaurantId == RESTAURANT_ID_2 } shouldBe true
                }

                it("Biryani items have category = Biryani") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .all { it.category == HOME_CATEGORY_BIRYANI } shouldBe true
                }

                it("Starters items have category = Starters") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[CATEGORY_STARTERS]!!
                        .all { it.category == CATEGORY_STARTERS } shouldBe true
                }
            }

            context("customisations mapping") {

                val response = singleItemResponse(
                    customisations = listOf(
                        CustomisationDto(
                            id = CUSTOMISE_GROUP_SIZE,
                            name = CUSTOMISE_NAME_SIZE,
                            options = listOf(
                                CustomisationOptionDto(
                                    CUSTOMISE_OPT_REGULAR,
                                    CUSTOMISE_LABEL_REGULAR,
                                    EXTRA_PRICE_ZERO
                                ),
                                CustomisationOptionDto(
                                    CUSTOMISE_OPT_LARGE,
                                    CUSTOMISE_LABEL_LARGE,
                                    EXTRA_PRICE_LARGE
                                ),
                            )
                        )
                    )
                )

                it("maps one customisation group") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations shouldHaveSize 1
                }

                it("maps customisation group id correctly") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations.first().id shouldBe CUSTOMISE_GROUP_SIZE
                }

                it("maps customisation group name correctly") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations.first().name shouldBe CUSTOMISE_NAME_SIZE
                }

                it("maps 2 customisation options") {
                    val options =
                        mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                            .first().customisations.first().options
                    options shouldHaveSize 2
                }

                it("maps first option id correctly") {
                    val options =
                        mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                            .first().customisations.first().options
                    options[0].id shouldBe CUSTOMISE_OPT_REGULAR
                }

                it("maps second option id correctly") {
                    val options =
                        mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                            .first().customisations.first().options
                    options[1].id shouldBe CUSTOMISE_OPT_LARGE
                }

                it("maps option extraPrice correctly") {
                    val options =
                        mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                            .first().customisations.first().options
                    options[1].extraPrice shouldBe EXTRA_PRICE_LARGE
                }

                it("maps option label correctly") {
                    val options =
                        mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                            .first().customisations.first().options
                    options[1].label shouldBe CUSTOMISE_LABEL_LARGE
                }
            }

            context("multiple customisation groups") {

                val response = singleItemResponse(
                    customisations = listOf(
                        CustomisationDto(
                            id = CUSTOMISE_GROUP_SIZE,
                            name = CUSTOMISE_NAME_SIZE,
                            options = listOf(
                                CustomisationOptionDto(
                                    CUSTOMISE_OPT_REGULAR,
                                    CUSTOMISE_LABEL_REGULAR,
                                    EXTRA_PRICE_ZERO
                                ),
                                CustomisationOptionDto(
                                    CUSTOMISE_OPT_LARGE,
                                    CUSTOMISE_LABEL_LARGE,
                                    EXTRA_PRICE_LARGE
                                ),
                            )
                        ),
                        CustomisationDto(
                            id = CUSTOMISE_GROUP_SPICE,
                            name = CUSTOMISE_NAME_SPICE,
                            options = listOf(
                                CustomisationOptionDto(
                                    CUSTOMISE_OPT_MILD,
                                    CUSTOMISE_LABEL_MILD,
                                    EXTRA_PRICE_ZERO
                                ),
                                CustomisationOptionDto(
                                    CUSTOMISE_OPT_MEDIUM,
                                    CUSTOMISE_LABEL_MEDIUM,
                                    EXTRA_PRICE_ZERO
                                ),
                                CustomisationOptionDto(
                                    CUSTOMISE_OPT_HOT,
                                    CUSTOMISE_LABEL_HOT,
                                    EXTRA_PRICE_ZERO
                                ),
                            )
                        ),
                    )
                )

                it("maps 2 customisation groups") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations shouldHaveSize 2
                }

                it("first group is Size") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations[0].name shouldBe CUSTOMISE_NAME_SIZE
                }

                it("second group is Spice Level") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations[1].name shouldBe CUSTOMISE_NAME_SPICE
                }

                it("Spice Level group has 3 options") {
                    mapper.toDomain(response, RESTAURANT_ID_1)[HOME_CATEGORY_BIRYANI]!!
                        .first().customisations[1].options shouldHaveSize 3
                }
            }

            context("empty menu response") {

                it("empty dailyMenus returns empty map") {
                    val response = DailyMenuResponse(dailyMenus = emptyList())
                    mapper.toDomain(response, RESTAURANT_ID_1).isEmpty() shouldBe true
                }

                it("category with empty dishes is excluded from result") {
                    val response = DailyMenuResponse(
                        dailyMenus = listOf(
                            DailyMenuWrapper(
                                DailyMenuDto(
                                    id = MENU_DTO_ID_1,
                                    name = CATEGORY_EMPTY_NAME,
                                    dishes = emptyList(),
                                )
                            )
                        )
                    )
                    mapper.toDomain(response, RESTAURANT_ID_1).isEmpty() shouldBe true
                }

                it("mix of empty and non-empty categories — only non-empty included") {
                    val response = DailyMenuResponse(
                        dailyMenus = listOf(
                            DailyMenuWrapper(
                                DailyMenuDto(
                                    id = MENU_DTO_ID_1,
                                    name = CATEGORY_EMPTY_SHORT,
                                    dishes = emptyList(),
                                )
                            ),
                            DailyMenuWrapper(
                                DailyMenuDto(
                                    id = MENU_DTO_ID_2,
                                    name = HOME_CATEGORY_BIRYANI,
                                    dishes = listOf(
                                        DishWrapper(
                                            DishDto(
                                                id = MENU_ID_1,
                                                name = MENU_ITEM_CHICK_BIR,
                                                price = MENU_PRICE_STR,
                                                description = "",
                                                isVeg = API_IS_NOT_VEG,
                                                isRecommended = API_IS_RECOMMENDED,
                                            )
                                        )
                                    ),
                                )
                            ),
                        )
                    )
                    val result = mapper.toDomain(response, RESTAURANT_ID_1)
                    result.size shouldBe 1
                    result.containsKey(HOME_CATEGORY_BIRYANI) shouldBe true
                    result.containsKey(CATEGORY_EMPTY_SHORT) shouldBe false
                }
            }
        }
    }
})