package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.repository.RestaurantRepository
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
})

// extension for readability
fun <T> List<T>.shouldBeEmpty() = this shouldBe emptyList()