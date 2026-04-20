package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
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
        Collections(1, "Trending", "Hot now", "", 10, "60% OFF"),
    )
    val fakeCategories = listOf(
        FoodCategory(1, "Biryani", ""),
        FoodCategory(2, "Pizza", ""),
    )
    val fakeRestaurants = listOf(
        fakeRestaurant("r1"), fakeRestaurant("r2"),
    )

    beforeEach { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterEach { Dispatchers.resetMain() }

    describe("GetHomeDataUseCase") {

        // ── All three flows succeed
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
                result.getOrNull()!!.collections.size shouldBe 1
                result.getOrNull()!!.categories.size shouldBe 2
                result.getOrNull()!!.restaurants.size shouldBe 2
            }

            it("collection title should match first collection") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.getOrNull()!!.collections.first().title shouldBe "Trending"
            }

            it("first restaurant id should match") {
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                result.getOrNull()!!.restaurants.first().id shouldBe "r1"
            }
        }

        // ── Collections fail — non-critical
        context("collections fail but categories and restaurants succeed") {

            beforeEach {
                every { restaurantRepository.getCollections() } returns
                        flowOf(Result.success(emptyList()))  // non-critical returns empty
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
                result.getOrNull()!!.restaurants.size shouldBe 2
            }
        }

        // ── All data empty
        context("all repositories return empty lists") {

            beforeEach {
                every { restaurantRepository.getCollections() } returns
                        flowOf(Result.success(emptyList()))
                every { restaurantRepository.getCategories() } returns
                        flowOf(Result.success(emptyList()))
                every { restaurantRepository.getNearbyRestaurants() } returns
                        flowOf(Result.success(emptyList()))
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

        // ── Restaurants fail — critical
        context("restaurants fail with network error") {

            beforeEach {
                every { restaurantRepository.getCollections() } returns
                        flowOf(Result.success(fakeCollections))
                every { restaurantRepository.getCategories() } returns
                        flowOf(Result.success(fakeCategories))
                every { restaurantRepository.getNearbyRestaurants() } returns
                        flowOf(Result.failure(Exception("No internet")))
            }

            it("HomeData should still be returned — restaurants will be empty") {
                // combine() waits for all flows to emit
                // even if one fails, Result wraps it
                val useCase = GetHomeDataUseCase(restaurantRepository)
                val result = useCase().first()

                // getOrDefault returns empty on failure
                result.isSuccess shouldBe true
                result.getOrNull()!!.restaurants.shouldBeEmpty()
            }
        }
    }

    // ── Location filter — NO_FILTER cases ────────────────────────

    context("selectedLocation is blank — no filter applied") {
        it("filterStatus is NO_FILTER and all restaurants returned") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(fakeCollections))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(fakeCategories))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        koramRestaurant("r1"),
                        indiranagarRestaurant("r2"),
                    )))

            val useCase = GetHomeDataUseCase(restaurantRepository)
            val result  = useCase("").first().getOrNull()!!

            result.filterStatus     shouldBe FilterStatus.NO_FILTER
            result.restaurants.size shouldBe 2
        }
    }

    context("selectedLocation is 'Select Location' — no filter applied") {
        it("filterStatus is NO_FILTER and all restaurants returned") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(fakeCollections))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(fakeCategories))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        koramRestaurant("r1"),
                        indiranagarRestaurant("r2"),
                    )))

            val useCase = GetHomeDataUseCase(restaurantRepository)
            val result  = useCase("Select Location").first().getOrNull()!!

            result.filterStatus     shouldBe FilterStatus.NO_FILTER
            result.restaurants.size shouldBe 2
        }
    }

    context("selectedLocation is 'Current Location' — GPS fallback") {
        it("filterStatus is NO_FILTER and all restaurants returned") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(fakeCollections))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(fakeCategories))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        koramRestaurant("r1"),
                        indiranagarRestaurant("r2"),
                    )))

            val useCase = GetHomeDataUseCase(restaurantRepository)
            val result  = useCase("Current Location").first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.NO_FILTER
        }
    }

// ── Location filter — FOUND cases ────────────────────────────

    context("selectedLocation matches restaurant locality exactly") {
        val restaurants = listOf(
            koramRestaurant("r1"),
            indiranagarRestaurant("r2"),
        )

        beforeEach {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(fakeCollections))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(fakeCategories))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(restaurants))
        }

        it("filterStatus is FOUND") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala").first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.FOUND
        }

        it("only Koramangala restaurant returned — not Indiranagar") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala").first().getOrNull()!!

            result.restaurants.size shouldBe 1
            result.restaurants.first().id shouldBe "r1"
        }

        it("requestedArea is the locality key") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala").first().getOrNull()!!

            result.requestedArea shouldBe "Koramangala"
        }

        it("availableAreas is empty when FOUND") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala").first().getOrNull()!!

            result.availableAreas shouldBe emptyList()
        }
    }

    context("selectedLocation has city suffix — Koramangala, Bengaluru") {
        it("extracts first part before comma and matches correctly") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        koramRestaurant("r1"),
                        indiranagarRestaurant("r2"),
                    )))

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala, Bengaluru").first().getOrNull()!!

            result.filterStatus     shouldBe FilterStatus.FOUND
            result.requestedArea    shouldBe "Koramangala"
            result.restaurants.size shouldBe 1
        }
    }

    context("matchesLocation — locality contains key (partial match)") {
        it("Greater Koramangala locality matches Koramangala search") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        fakeRestaurant("r1").copy(
                            locality = "Greater Koramangala",
                            address  = "Greater Koramangala, Bengaluru",
                        )
                    )))

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala").first().getOrNull()!!

            result.filterStatus     shouldBe FilterStatus.FOUND
            result.restaurants.size shouldBe 1
        }
    }

    context("matchesLocation — key contains locality (reverse partial)") {
        it("Koramangala 5th Block search matches locality Koramangala") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        koramRestaurant("r1"),
                        indiranagarRestaurant("r2"),
                    )))

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala 5th Block").first().getOrNull()!!

            result.filterStatus     shouldBe FilterStatus.FOUND
            result.restaurants.size shouldBe 1
            result.restaurants.first().id shouldBe "r1"
        }
    }

    context("matchesLocation — address contains key fallback") {
        it("restaurant with Koramangala in address but different locality matches") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        fakeRestaurant("r1").copy(
                            locality = "BTM Layout",
                            address  = "5th Block, Koramangala, Bengaluru",
                        )
                    )))

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Koramangala").first().getOrNull()!!

            result.filterStatus     shouldBe FilterStatus.FOUND
            result.restaurants.size shouldBe 1
        }
    }

    context("matchesLocation — case insensitive") {
        it("lowercase koramangala matches KORAMANGALA locality") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        fakeRestaurant("r1").copy(
                            locality = "KORAMANGALA",
                            address  = "KORAMANGALA, Bengaluru",
                        )
                    )))

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("koramangala").first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.FOUND
        }
    }

// ── Location filter — NOT_SERVICEABLE cases ───────────────────

    context("selectedLocation has no matching restaurants — Jakkur") {
        val allRestaurants = listOf(
            koramRestaurant("r1"),
            indiranagarRestaurant("r2"),
            hsrRestaurant("r3"),
        )

        beforeEach {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(fakeCollections))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(fakeCategories))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(allRestaurants))
        }

        it("filterStatus is NOT_SERVICEABLE") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Jakkur").first().getOrNull()!!

            result.filterStatus shouldBe FilterStatus.NOT_SERVICEABLE
        }

        it("restaurants list is empty") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Jakkur").first().getOrNull()!!

            result.restaurants shouldBe emptyList()
        }

        it("requestedArea is Jakkur") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Jakkur").first().getOrNull()!!

            result.requestedArea shouldBe "Jakkur"
        }

        it("availableAreas contains all distinct localities sorted alphabetically") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Jakkur").first().getOrNull()!!

            result.availableAreas shouldBe
                    listOf("HSR Layout", "Indiranagar", "Koramangala")
        }

        it("availableAreas size equals distinct locality count") {
            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Jakkur").first().getOrNull()!!

            result.availableAreas.size shouldBe 3
        }
    }

    context("NOT_SERVICEABLE for out-of-city location — Mumbai") {
        it("returns NOT_SERVICEABLE with all local areas as suggestions") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        koramRestaurant("r1"),
                        indiranagarRestaurant("r2"),
                    )))

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Mumbai").first().getOrNull()!!

            result.filterStatus   shouldBe FilterStatus.NOT_SERVICEABLE
            result.requestedArea  shouldBe "Mumbai"
            result.availableAreas shouldBe listOf("Indiranagar", "Koramangala")
        }
    }

    context("availableAreas deduplication — multiple restaurants same locality") {
        it("availableAreas has no duplicates and is sorted") {
            every { restaurantRepository.getCollections() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getCategories() } returns
                    flowOf(Result.success(emptyList()))
            every { restaurantRepository.getNearbyRestaurants() } returns
                    flowOf(Result.success(listOf(
                        koramRestaurant("r1"),
                        koramRestaurant("r2"), // duplicate locality
                        indiranagarRestaurant("r3"),
                    )))

            val result = GetHomeDataUseCase(restaurantRepository)
                .invoke("Jakkur").first().getOrNull()!!

            // .distinct() removes r2's duplicate Koramangala
            result.availableAreas.size shouldBe 2
            result.availableAreas shouldBe listOf("Indiranagar", "Koramangala")
        }
    }
})

// extension for readability
fun <T> List<T>.shouldBeEmpty() = this shouldBe emptyList()

fun koramRestaurant(id: String) = fakeRestaurant(id).copy(
    locality = "Koramangala",
    address  = "Koramangala, Bengaluru",
)

fun indiranagarRestaurant(id: String) = fakeRestaurant(id).copy(
    locality = "Indiranagar",
    address  = "Indiranagar, Bengaluru",
)

fun hsrRestaurant(id: String) = fakeRestaurant(id).copy(
    locality = "HSR Layout",
    address  = "HSR Layout, Bengaluru",
)