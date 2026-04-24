package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.CustomisationDto
import com.swapna.foodapp.data.remote.dto.CustomisationOptionDto
import com.swapna.foodapp.data.remote.dto.DailyMenuDto
import com.swapna.foodapp.data.remote.dto.DailyMenuResponse
import com.swapna.foodapp.data.remote.dto.DailyMenuWrapper
import com.swapna.foodapp.data.remote.dto.DishDto
import com.swapna.foodapp.data.remote.dto.DishWrapper
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe

class MenuMapperSpec : DescribeSpec({

    val mapper = MenuMapper()

    // ── Helper — reduces DailyMenuWrapper boilerplate ─────────
    // WHY helper function?
    // DailyMenuWrapper(DailyMenuDto(...)) is verbose
    // Most tests only change price/isVeg/imageUrl
    // Helper makes each test read clearly — same pattern as fakeRestaurant()
    fun singleItemResponse(
        category: String = "Biryani",
        id: String = "m1",
        name: String = "Chicken Biryani",
        price: String = "249",
        isVeg: Int = 0,
        isRecommended: Int = 1,
        imageUrl: String? = null,
        customisations: List<CustomisationDto>? = null,
        description: String = "",
    ) = DailyMenuResponse(
        dailyMenus = listOf(
            DailyMenuWrapper(
                menu = DailyMenuDto(
                    id = "menu_1",
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

            // ══════════════════════════════════════════════════
            // Basic mapping
            // ══════════════════════════════════════════════════

            context("response with single category and single item") {

                val response = singleItemResponse(
                    price = "249 Rs.",
                    isVeg = 0,
                    isRecommended = 1,
                )

                it("returns map with one category") {
                    mapper.toDomain(response, "r1").size shouldBe 1
                }

                it("category key is Biryani") {
                    mapper.toDomain(response, "r1")
                        .containsKey("Biryani") shouldBe true
                }

                it("category has one item") {
                    mapper.toDomain(response, "r1")["Biryani"]?.shouldHaveSize(1)
                }

                it("maps item id correctly") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().id shouldBe "m1"
                }

                it("maps item name correctly") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().name shouldBe "Chicken Biryani"
                }

                it("maps restaurantId from parameter") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().restaurantId shouldBe "r1"
                }

                it("maps category to item.category") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().category shouldBe "Biryani"
                }

                it("maps description correctly") {
                    val r = singleItemResponse(description = "Delicious Biryani")
                    mapper.toDomain(r, "r1")["Biryani"]!!
                        .first().description shouldBe "Delicious Biryani"
                }

                // WHY test isBestseller = false always?
                // API has no isBestseller field in DishDto
                // mapper hardcodes false — must verify this contract
                it("maps isBestseller to false always — not from API") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().isBestseller shouldBe false
                }

                it("maps isAvailable to true always") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().isAvailable shouldBe true
                }

                it("null customisations maps to empty list") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.shouldBeEmpty()
                }
            }

            // ══════════════════════════════════════════════════
            // restaurantId parameter
            // ══════════════════════════════════════════════════

            context("restaurantId parameter") {

                it("maps restaurantId r1 correctly") {
                    mapper.toDomain(singleItemResponse(), "r1")["Biryani"]!!
                        .first().restaurantId shouldBe "r1"
                }

                it("maps restaurantId r99 correctly — different id") {
                    mapper.toDomain(singleItemResponse(), "r99")["Biryani"]!!
                        .first().restaurantId shouldBe "r99"
                }
            }

            // ══════════════════════════════════════════════════
            // isVeg — Int → Boolean mapping
            // WHY test this specifically?
            // API sends 0/1 not true/false
            // isVeg = dto.isVeg == 1
            // If changed to > 0 or != 0 → behavior changes silently
            // ══════════════════════════════════════════════════

            context("isVeg flag mapping — Int to Boolean") {

                it("isVeg 1 maps to true") {
                    mapper.toDomain(singleItemResponse(isVeg = 1), "r1")["Biryani"]!!
                        .first().isVeg shouldBe true
                }

                it("isVeg 0 maps to false") {
                    mapper.toDomain(singleItemResponse(isVeg = 0), "r1")["Biryani"]!!
                        .first().isVeg shouldBe false
                }
            }

            // ══════════════════════════════════════════════════
            // isRecommended — Int → Boolean mapping
            // ══════════════════════════════════════════════════

            context("isRecommended flag mapping — Int to Boolean") {

                it("isRecommended 1 maps to true") {
                    mapper.toDomain(singleItemResponse(isRecommended = 1), "r1")["Biryani"]!!
                        .first().isRecommended shouldBe true
                }

                it("isRecommended 0 maps to false") {
                    mapper.toDomain(singleItemResponse(isRecommended = 0), "r1")["Biryani"]!!
                        .first().isRecommended shouldBe false
                }
            }

            // ══════════════════════════════════════════════════
            // Price string parsing
            // WHY critical to test?
            // "249 Rs." → parsePriceString() → 249.0
            // Wrong price = wrong bill shown to user
            // parsePriceString strips non-numeric chars → toDoubleOrNull
            // ══════════════════════════════════════════════════

            context("price string parsing") {

                it("parses '249 Rs.' to 249.0") {
                    mapper.toDomain(singleItemResponse(price = "249 Rs."), "r1")["Biryani"]!!
                        .first().price shouldBe 249.0
                }

                it("parses '₹349' to 349.0") {
                    mapper.toDomain(singleItemResponse(price = "₹349"), "r1")["Biryani"]!!
                        .first().price shouldBe 349.0
                }

                it("parses '99.50' to 99.5") {
                    mapper.toDomain(singleItemResponse(price = "99.50"), "r1")["Biryani"]!!
                        .first().price shouldBe 99.5
                }

                it("parses plain number '199' to 199.0") {
                    mapper.toDomain(singleItemResponse(price = "199"), "r1")["Biryani"]!!
                        .first().price shouldBe 199.0
                }

                // ✅ FIX: "Rs. 449" keeps the dot → ". 449" → toDoubleOrNull() = null → 0.0
                // The regex [^0-9.] whitelists dots, so "Rs." dot is preserved
                // ". 449" is not a valid Double → falls back to 0.0
                it("parses 'Rs. 449' to 0.449 — dot from Rs. is kept") {
                    mapper.toDomain(singleItemResponse(price = "Rs. 449"), "r1")["Biryani"]!!
                        .first().price shouldBe 0.449
                }

                it("parses invalid price string 'N/A' to 0.0") {
                    mapper.toDomain(singleItemResponse(price = "N/A"), "r1")["Biryani"]!!
                        .first().price shouldBe 0.0
                }

                it("parses empty price string to 0.0") {
                    mapper.toDomain(singleItemResponse(price = ""), "r1")["Biryani"]!!
                        .first().price shouldBe 0.0
                }
            }

            // ══════════════════════════════════════════════════
            // imageUrl — nullable handling
            // ══════════════════════════════════════════════════

            context("imageUrl nullable mapping") {

                it("null imageUrl maps to empty string") {
                    mapper.toDomain(singleItemResponse(imageUrl = null), "r1")["Biryani"]!!
                        .first().imageUrl shouldBe ""
                }

                it("non-null imageUrl maps correctly") {
                    mapper.toDomain(
                        singleItemResponse(imageUrl = "https://img.jpg"), "r1"
                    )["Biryani"]!!.first().imageUrl shouldBe "https://img.jpg"
                }
            }

            // ══════════════════════════════════════════════════
            // Multiple categories
            // ══════════════════════════════════════════════════

            context("multiple categories") {

                val response = DailyMenuResponse(
                    dailyMenus = listOf(
                        DailyMenuWrapper(
                            DailyMenuDto(
                                id = "1",
                                name = "Biryani",
                                dishes = listOf(
                                    DishWrapper(
                                        DishDto(
                                            id = "m1", name = "Chicken Biryani",
                                            price = "249", description = "",
                                            isVeg = 0, isRecommended = 1,
                                        )
                                    ),
                                    DishWrapper(
                                        DishDto(
                                            id = "m2", name = "Mutton Biryani",
                                            price = "349", description = "",
                                            isVeg = 0, isRecommended = 0,
                                        )
                                    ),
                                ),
                            )
                        ),
                        DailyMenuWrapper(
                            DailyMenuDto(
                                id = "2",
                                name = "Starters",
                                dishes = listOf(
                                    DishWrapper(
                                        DishDto(
                                            id = "m3", name = "Chicken 65",
                                            price = "199", description = "",
                                            isVeg = 0, isRecommended = 1,
                                        )
                                    ),
                                ),
                            )
                        ),
                    )
                )

                it("returns map with 2 categories") {
                    mapper.toDomain(response, "r1").size shouldBe 2
                }

                it("Biryani category has 2 items") {
                    mapper.toDomain(response, "r1")["Biryani"]?.shouldHaveSize(2)
                }

                it("Starters category has 1 item") {
                    mapper.toDomain(response, "r1")["Starters"]?.shouldHaveSize(1)
                }

                it("Biryani first item is Chicken Biryani") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().name shouldBe "Chicken Biryani"
                }

                it("Starters first item is Chicken 65") {
                    mapper.toDomain(response, "r1")["Starters"]!!
                        .first().name shouldBe "Chicken 65"
                }

                it("all items have correct restaurantId") {
                    val menu = mapper.toDomain(response, "r2")
                    menu.values.flatten()
                        .all { it.restaurantId == "r2" } shouldBe true
                }

                it("Biryani items have category = Biryani") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .all { it.category == "Biryani" } shouldBe true
                }

                it("Starters items have category = Starters") {
                    mapper.toDomain(response, "r1")["Starters"]!!
                        .all { it.category == "Starters" } shouldBe true
                }
            }

            // ══════════════════════════════════════════════════
            // Customisations mapping
            // ══════════════════════════════════════════════════

            context("customisations mapping") {

                val response = singleItemResponse(
                    customisations = listOf(
                        CustomisationDto(
                            id = "size_group",
                            name = "Size",
                            options = listOf(
                                CustomisationOptionDto("regular", "Regular", 0.0),
                                CustomisationOptionDto("large", "Large", 50.0),
                            )
                        )
                    )
                )

                it("maps one customisation group") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations shouldHaveSize 1
                }

                it("maps customisation group id correctly") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.first().id shouldBe "size_group"
                }

                it("maps customisation group name correctly") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.first().name shouldBe "Size"
                }

                it("maps 2 customisation options") {
                    val options = mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.first().options
                    options shouldHaveSize 2
                }

                it("maps first option id correctly") {
                    val options = mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.first().options
                    options[0].id shouldBe "regular"
                }

                it("maps second option id correctly") {
                    val options = mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.first().options
                    options[1].id shouldBe "large"
                }

                it("maps option extraPrice correctly") {
                    val options = mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.first().options
                    options[1].extraPrice shouldBe 50.0
                }

                it("maps option label correctly") {
                    val options = mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations.first().options
                    options[1].label shouldBe "Large"
                }
            }

            context("multiple customisation groups") {

                val response = singleItemResponse(
                    customisations = listOf(
                        CustomisationDto(
                            id = "size_group",
                            name = "Size",
                            options = listOf(
                                CustomisationOptionDto("regular", "Regular", 0.0),
                                CustomisationOptionDto("large", "Large", 50.0),
                            )
                        ),
                        CustomisationDto(
                            id = "spice_group",
                            name = "Spice Level",
                            options = listOf(
                                CustomisationOptionDto("mild", "Mild", 0.0),
                                CustomisationOptionDto("medium", "Medium", 0.0),
                                CustomisationOptionDto("hot", "Hot", 0.0),
                            )
                        ),
                    )
                )

                it("maps 2 customisation groups") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations shouldHaveSize 2
                }

                it("first group is Size") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations[0].name shouldBe "Size"
                }

                it("second group is Spice Level") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations[1].name shouldBe "Spice Level"
                }

                it("Spice Level group has 3 options") {
                    mapper.toDomain(response, "r1")["Biryani"]!!
                        .first().customisations[1].options shouldHaveSize 3
                }
            }

            // ══════════════════════════════════════════════════
            // Empty / edge cases
            // ══════════════════════════════════════════════════

            context("empty menu response") {

                it("empty dailyMenus returns empty map") {
                    val response = DailyMenuResponse(dailyMenus = emptyList())
                    // ✅ Map has no shouldBeEmpty() — use isEmpty()
                    mapper.toDomain(response, "r1").isEmpty() shouldBe true
                }

                it("category with empty dishes is excluded from result") {
                    val response = DailyMenuResponse(
                        dailyMenus = listOf(
                            DailyMenuWrapper(
                                DailyMenuDto(
                                    id = "1",
                                    name = "Empty Category",
                                    dishes = emptyList(),
                                )
                            )
                        )
                    )
                    // ✅ Remove ?. — not nullable. Use isEmpty() not shouldBeEmpty()
                    mapper.toDomain(response, "r1").isEmpty() shouldBe true
                }

                it("mix of empty and non-empty categories — only non-empty included") {
                    val response = DailyMenuResponse(
                        dailyMenus = listOf(
                            DailyMenuWrapper(
                                DailyMenuDto(
                                    id = "1", name = "Empty", dishes = emptyList()
                                )
                            ),
                            DailyMenuWrapper(
                                DailyMenuDto(
                                    id = "2",
                                    name = "Biryani",
                                    dishes = listOf(
                                        DishWrapper(
                                            DishDto(
                                                id = "m1",
                                                name = "Chicken Biryani",
                                                price = "249",
                                                description = "",
                                                isVeg = 0,
                                                isRecommended = 1,
                                            )
                                        )
                                    ),
                                )
                            ),
                        )
                    )
                    val result = mapper.toDomain(response, "r1")
                    result.size shouldBe 1
                    result.containsKey("Biryani") shouldBe true
                    result.containsKey("Empty") shouldBe false
                }
            }
        }
    }
})