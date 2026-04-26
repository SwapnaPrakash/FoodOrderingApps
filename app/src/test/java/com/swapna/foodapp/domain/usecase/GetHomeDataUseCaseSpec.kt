package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.TestConstants.ADDR_BTM_KORA
import com.swapna.foodapp.utils.TestConstants.ADDR_GREATER_KORA
import com.swapna.foodapp.utils.TestConstants.ADDR_HSR
import com.swapna.foodapp.utils.TestConstants.ADDR_INDIRANAGAR
import com.swapna.foodapp.utils.TestConstants.ADDR_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.ADDR_UPPER_KORA
import com.swapna.foodapp.utils.TestConstants.AVAILABLE_AREAS_COUNT_2
import com.swapna.foodapp.utils.TestConstants.AVAILABLE_AREAS_COUNT_3
import com.swapna.foodapp.utils.TestConstants.COLLECTION_TITLE_FIRST
import com.swapna.foodapp.utils.TestConstants.ERR_NO_INTERNET_MSG
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_2
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_PIZZA
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_COUNT
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_COUNT_1
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_DESC
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_DISCOUNT
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_ID
import com.swapna.foodapp.utils.TestConstants.HOME_COLLECTION_TITLE
import com.swapna.foodapp.utils.TestConstants.HOME_RESTAURANT_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_RESTAURANT_ID_2
import com.swapna.foodapp.utils.TestConstants.HOME_RESTAURANT_ID_3
import com.swapna.foodapp.utils.TestConstants.LOC_BTM
import com.swapna.foodapp.utils.TestConstants.LOC_GREATER_KORA
import com.swapna.foodapp.utils.TestConstants.LOC_HSR
import com.swapna.foodapp.utils.TestConstants.LOC_INDIRANAGAR
import com.swapna.foodapp.utils.TestConstants.LOC_JAKKUR
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA_FULL
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA_LOWER
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA_PARTIAL
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA_UPPER
import com.swapna.foodapp.utils.TestConstants.LOC_MUMBAI
import com.swapna.foodapp.utils.fakeRestaurant
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class GetHomeDataUseCaseSpec : DescribeSpec({

    val restaurantRepository = mockk<RestaurantRepository>()

    val fakeCollections = listOf(
        Collections(
            HOME_COLLECTION_ID,        // ✅ was 1
            HOME_COLLECTION_TITLE,     // ✅ was "Trending"
            HOME_COLLECTION_DESC,      // ✅ was "Hot now"
            "",
            HOME_COLLECTION_COUNT,     // ✅ was 10
            HOME_COLLECTION_DISCOUNT,  // ✅ was "60% OFF"
        ),
    )
    val fakeCategories = listOf(
        FoodCategory(HOME_CATEGORY_ID_1, HOME_CATEGORY_BIRYANI, ""), // ✅ was 1, "Biryani"
        FoodCategory(HOME_CATEGORY_ID_2, HOME_CATEGORY_PIZZA, ""), // ✅ was 2, "Pizza"
    )
    val fakeRestaurants = listOf(
        fakeRestaurant(HOME_RESTAURANT_ID_1),                        // ✅ was "r1"
        fakeRestaurant(HOME_RESTAURANT_ID_2),                        // ✅ was "r2"
    )

    beforeEach { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterEach { Dispatchers.resetMain() }

    describe("GetHomeDataUseCase") {

        // ── All three flows succeed ───────────────────────────

        context("all repositories return data successfully") {

            beforeEach {
                every { restaurantRepository.getCollections() } returns
                        flowOf(Result.success(fakeCollections))
                every { restaurantRepository.getCategories() } returns
                        flowOf(Result.success(fakeCategories))
                every { restaurantRepository.getNearbyRestaurants() } returns
                        flowOf(Result.success(fakeRestaurants))
            }

            it("should return HomeData with all three lists populated") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.isSuccess shouldBe true
                result.getOrNull()!!.collections.size shouldBe HOME_COLLECTION_COUNT_1 // ✅ was 1
                result.getOrNull()!!.categories.size shouldBe HOME_CATEGORY_COUNT_2   // ✅ was 2
                result.getOrNull()!!.restaurants.size shouldBe HOME_RESTAURANT_COUNT_2 // ✅ was 2
            }

            it("collection title should match first collection") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.getOrNull()!!.collections.first().title shouldBe COLLECTION_TITLE_FIRST // ✅ was "Trending"
            }

            it("first restaurant id should match") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.getOrNull()!!.restaurants.first().id shouldBe HOME_RESTAURANT_ID_1 // ✅ was "r1"
            }
        }

        // ── Collections fail — non-critical ──────────────────

        context("collections fail but categories and restaurants succeed") {

            beforeEach {
                every { restaurantRepository.getCollections() } returns
                        flowOf(Result.success(emptyList()))
                every { restaurantRepository.getCategories() } returns
                        flowOf(Result.success(fakeCategories))
                every { restaurantRepository.getNearbyRestaurants() } returns
                        flowOf(Result.success(fakeRestaurants))
            }

            it("should still return HomeData with empty collections") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.isSuccess shouldBe true
                result.getOrNull()!!.collections.shouldBeEmpty()
                result.getOrNull()!!.restaurants.size shouldBe HOME_RESTAURANT_COUNT_2
            }
        }

        // ── All data empty ────────────────────────────────────

        context("all repositories return empty lists") {

            beforeEach {
                every { restaurantRepository.getCollections() } returns flowOf(
                    Result.success(
                        emptyList()
                    )
                )
                every { restaurantRepository.getCategories() } returns flowOf(
                    Result.success(
                        emptyList()
                    )
                )
                every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                    Result.success(
                        emptyList()
                    )
                )
            }

            it("should return HomeData with all empty lists — not an error") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.isSuccess shouldBe true
                result.getOrNull()!!.collections.shouldBeEmpty()
                result.getOrNull()!!.categories.shouldBeEmpty()
                result.getOrNull()!!.restaurants.shouldBeEmpty()
            }
        }

        // ── Restaurants fail — critical ───────────────────────

        context("restaurants fail with network error") {

            beforeEach {
                every { restaurantRepository.getCollections() } returns
                        flowOf(Result.success(fakeCollections))
                every { restaurantRepository.getCategories() } returns
                        flowOf(Result.success(fakeCategories))
                every { restaurantRepository.getNearbyRestaurants() } returns
                        flowOf(Result.failure(Exception(ERR_NO_INTERNET_MSG))) // ✅ was "No internet"
            }

            it("HomeData should still be returned — restaurants will be empty") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.isSuccess shouldBe true
                result.getOrNull()!!.restaurants.shouldBeEmpty()
            }
        }
    }

    // ── Location filter — NO_FILTER ───────────────────────────

    context("selectedLocation is blank — no filter applied") {
        it("filterStatus is NO_FILTER and all restaurants returned") {
            every { restaurantRepository.getCollections() } returns flowOf(
                Result.success(
                    fakeCollections
                )
            )
            every { restaurantRepository.getCategories() } returns flowOf(
                Result.success(
                    fakeCategories
                )
            )
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        koramRestaurant(HOME_RESTAURANT_ID_1),
                        indiranagarRestaurant(HOME_RESTAURANT_ID_2),
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository).invoke("").first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.NO_FILTER
            result.restaurants.size shouldBe HOME_RESTAURANT_COUNT_2
        }
    }

    context("selectedLocation is 'Select Location' — no filter applied") {
        it("filterStatus is NO_FILTER and all restaurants returned") {
            every { restaurantRepository.getCollections() } returns flowOf(
                Result.success(
                    fakeCollections
                )
            )
            every { restaurantRepository.getCategories() } returns flowOf(
                Result.success(
                    fakeCategories
                )
            )
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        koramRestaurant(HOME_RESTAURANT_ID_1),
                        indiranagarRestaurant(HOME_RESTAURANT_ID_2),
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(AppConstants.SELECT_LOCATION).first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.NO_FILTER
            result.restaurants.size shouldBe HOME_RESTAURANT_COUNT_2
        }
    }

    context("selectedLocation is 'Current Location' — GPS fallback") {
        it("filterStatus is NO_FILTER and all restaurants returned") {
            every { restaurantRepository.getCollections() } returns flowOf(
                Result.success(
                    fakeCollections
                )
            )
            every { restaurantRepository.getCategories() } returns flowOf(
                Result.success(
                    fakeCategories
                )
            )
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        koramRestaurant(HOME_RESTAURANT_ID_1),
                        indiranagarRestaurant(HOME_RESTAURANT_ID_2),
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(AppConstants.CURRENT_LOCATION).first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.NO_FILTER
        }
    }

    // ── Location filter — FOUND ───────────────────────────────

    context("selectedLocation matches restaurant locality exactly") {
        val restaurants = listOf(
            koramRestaurant(HOME_RESTAURANT_ID_1),
            indiranagarRestaurant(HOME_RESTAURANT_ID_2),
        )

        beforeEach {
            every { restaurantRepository.getCollections() } returns flowOf(
                Result.success(
                    fakeCollections
                )
            )
            every { restaurantRepository.getCategories() } returns flowOf(
                Result.success(
                    fakeCategories
                )
            )
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    restaurants
                )
            )
        }

        it("filterStatus is FOUND") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA).first().getOrNull()!!     // ✅ was "Koramangala"

            result.filterStatus shouldBe FilterStatus.FOUND
        }

        it("only Koramangala restaurant returned — not Indiranagar") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA).first().getOrNull()!!

            result.restaurants.size shouldBe 1
            result.restaurants.first().id shouldBe HOME_RESTAURANT_ID_1
        }

        it("requestedArea is the locality key") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA).first().getOrNull()!!

            result.requestedArea shouldBe LOC_KORAMANGALA
        }

        it("availableAreas is empty when FOUND") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA).first().getOrNull()!!

            result.availableAreas shouldBe emptyList()
        }
    }

    context("selectedLocation has city suffix — Koramangala, Bengaluru") {
        it("extracts first part before comma and matches correctly") {
            every { restaurantRepository.getCollections() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        koramRestaurant(HOME_RESTAURANT_ID_1),
                        indiranagarRestaurant(HOME_RESTAURANT_ID_2),
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA_FULL).first()
                .getOrNull()!! // ✅ was "Koramangala, Bengaluru"

            result.filterStatus shouldBe FilterStatus.FOUND
            result.requestedArea shouldBe LOC_KORAMANGALA
            result.restaurants.size shouldBe 1
        }
    }

    context("matchesLocation — locality contains key (partial match)") {
        it("Greater Koramangala locality matches Koramangala search") {
            every { restaurantRepository.getCollections() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        fakeRestaurant(HOME_RESTAURANT_ID_1).copy(
                            locality = LOC_GREATER_KORA,   // ✅ was "Greater Koramangala"
                            address = ADDR_GREATER_KORA,  // ✅ was "Greater Koramangala, Bengaluru"
                        )
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA).first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.FOUND
            result.restaurants.size shouldBe 1
        }
    }

    context("matchesLocation — key contains locality (reverse partial)") {
        it("Koramangala 5th Block search matches locality Koramangala") {
            every { restaurantRepository.getCollections() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        koramRestaurant(HOME_RESTAURANT_ID_1),
                        indiranagarRestaurant(HOME_RESTAURANT_ID_2),
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA_PARTIAL).first()
                .getOrNull()!! // ✅ was "Koramangala 5th Block"

            result.filterStatus shouldBe FilterStatus.FOUND
            result.restaurants.size shouldBe 1
            result.restaurants.first().id shouldBe HOME_RESTAURANT_ID_1
        }
    }

    context("matchesLocation — address contains key fallback") {
        it("restaurant with Koramangala in address but different locality matches") {
            every { restaurantRepository.getCollections() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        fakeRestaurant(HOME_RESTAURANT_ID_1).copy(
                            locality = LOC_BTM,         // ✅ was "BTM Layout"
                            address = ADDR_BTM_KORA,   // ✅ was "5th Block, Koramangala, Bengaluru"
                        )
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA).first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.FOUND
            result.restaurants.size shouldBe 1
        }
    }

    context("matchesLocation — case insensitive") {
        it("lowercase koramangala matches KORAMANGALA locality") {
            every { restaurantRepository.getCollections() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        fakeRestaurant(HOME_RESTAURANT_ID_1).copy(
                            locality = LOC_KORAMANGALA_UPPER, // ✅ was "KORAMANGALA"
                            address = ADDR_UPPER_KORA,       // ✅ was "KORAMANGALA, Bengaluru"
                        )
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_KORAMANGALA_LOWER).first().getOrNull()!! // ✅ was "koramangala"

            result.filterStatus shouldBe FilterStatus.FOUND
        }
    }

    // ── Location filter — NOT_SERVICEABLE ─────────────────────

    context("selectedLocation has no matching restaurants — Jakkur") {
        val allRestaurants = listOf(
            koramRestaurant(HOME_RESTAURANT_ID_1),
            indiranagarRestaurant(HOME_RESTAURANT_ID_2),
            hsrRestaurant(HOME_RESTAURANT_ID_3),
        )

        beforeEach {
            every { restaurantRepository.getCollections() } returns flowOf(
                Result.success(
                    fakeCollections
                )
            )
            every { restaurantRepository.getCategories() } returns flowOf(
                Result.success(
                    fakeCategories
                )
            )
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    allRestaurants
                )
            )
        }

        it("filterStatus is NOT_SERVICEABLE") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_JAKKUR).first().getOrNull()!!           // ✅ was "Jakkur"

            result.filterStatus shouldBe FilterStatus.NOT_SERVICEABLE
        }

        it("restaurants list is empty") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_JAKKUR).first().getOrNull()!!

            result.restaurants shouldBe emptyList()
        }

        it("requestedArea is Jakkur") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_JAKKUR).first().getOrNull()!!

            result.requestedArea shouldBe LOC_JAKKUR
        }

        it("availableAreas contains all distinct localities sorted alphabetically") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_JAKKUR).first().getOrNull()!!

            result.availableAreas shouldBe
                    listOf(LOC_HSR, LOC_INDIRANAGAR, LOC_KORAMANGALA) // ✅ was hardcoded strings
        }

        it("availableAreas size equals distinct locality count") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_JAKKUR).first().getOrNull()!!

            result.availableAreas.size shouldBe AVAILABLE_AREAS_COUNT_3 // ✅ was 3
        }
    }

    context("NOT_SERVICEABLE for out-of-city location — Mumbai") {
        it("returns NOT_SERVICEABLE with all local areas as suggestions") {
            every { restaurantRepository.getCollections() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        koramRestaurant(HOME_RESTAURANT_ID_1),
                        indiranagarRestaurant(HOME_RESTAURANT_ID_2),
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_MUMBAI).first().getOrNull()!!            // ✅ was "Mumbai"

            result.filterStatus shouldBe FilterStatus.NOT_SERVICEABLE
            result.requestedArea shouldBe LOC_MUMBAI
            result.availableAreas shouldBe listOf(LOC_INDIRANAGAR, LOC_KORAMANGALA) // ✅
        }
    }

    context("availableAreas deduplication — multiple restaurants same locality") {
        it("availableAreas has no duplicates and is sorted") {
            every { restaurantRepository.getCollections() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns flowOf(
                Result.success(
                    listOf(
                        koramRestaurant(HOME_RESTAURANT_ID_1),
                        koramRestaurant(HOME_RESTAURANT_ID_2),              // duplicate locality
                        indiranagarRestaurant(HOME_RESTAURANT_ID_3),
                    )
                )
            )

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke(LOC_JAKKUR).first().getOrNull()!!

            // .distinct() removes r2's duplicate Koramangala
            result.availableAreas.size shouldBe AVAILABLE_AREAS_COUNT_2 // ✅ was 2
            result.availableAreas shouldBe listOf(LOC_INDIRANAGAR, LOC_KORAMANGALA)
        }
    }
})

// ── extension for readability ─────────────────────────────────
fun <T> List<T>.shouldBeEmpty() = this shouldBe emptyList()

// ── Location helpers ──────────────────────────────────────────
fun koramRestaurant(id: String) = fakeRestaurant(id).copy(
    locality = LOC_KORAMANGALA,   // ✅ was "Koramangala"
    address = ADDR_KORAMANGALA,  // ✅ was "Koramangala, Bengaluru"
)

fun indiranagarRestaurant(id: String) = fakeRestaurant(id).copy(
    locality = LOC_INDIRANAGAR,   // ✅ was "Indiranagar"
    address = ADDR_INDIRANAGAR,  // ✅ was "Indiranagar, Bengaluru"
)

fun hsrRestaurant(id: String) = fakeRestaurant(id).copy(
    locality = LOC_HSR,           // ✅ was "HSR Layout"
    address = ADDR_HSR,          // ✅ was "HSR Layout, Bengaluru"
)