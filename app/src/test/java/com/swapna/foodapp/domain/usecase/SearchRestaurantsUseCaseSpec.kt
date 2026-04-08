package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.search.SearchRestaurantsUseCase
import com.swapna.foodapp.utils.fakeRestaurant
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
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
class SearchRestaurantsUseCaseSpec : DescribeSpec({

    val repository = mockk<RestaurantRepository>()

    // ── Test data ─────────────────────────────────────────────
    val allRestaurants = listOf(
        fakeRestaurant("r1", "Meghana Foods",    rating = 4.6, deliveryTime = 30,
            cuisines = listOf("Biryani", "Andhra")),
        fakeRestaurant("r2", "Pizza Hut",        rating = 4.1, deliveryTime = 40,
            cuisines = listOf("Pizza", "Italian")),
        fakeRestaurant("r3", "Burger King",      rating = 3.8, deliveryTime = 25,
            cuisines = listOf("Burger", "Fast Food")),
        fakeRestaurant("r4", "Behrouz Biryani",  rating = 4.3, deliveryTime = 55,
            cuisines = listOf("Biryani", "Mughlai")),
        fakeRestaurant("r5", "Green Bowl",       rating = 4.5, deliveryTime = 20,
            cuisines = listOf("Healthy", "Salads")),
    )

    beforeEach { Dispatchers.setMain(UnconfinedTestDispatcher()) }
    afterEach  { Dispatchers.resetMain() }

    describe("SearchRestaurantsUseCase") {

        // ── Text search ────────────────────────────────────────
        context("text search filtering") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("empty query returns all restaurants unfiltered") {
                val useCase = SearchRestaurantsUseCase(repository)
                val result  = useCase("", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 5
            }

            it("'biryani' query returns restaurants with biryani in name or cuisine") {
                val useCase = SearchRestaurantsUseCase(repository)
                val result  = useCase("biryani", SearchFilters()).first()
                val names   = result.getOrNull()!!.map { it.name }
                names shouldBe listOf("Meghana Foods", "Behrouz Biryani")
            }

            it("'pizza' query matches cuisine AND name") {
                val useCase = SearchRestaurantsUseCase(repository)
                val result  = useCase("pizza", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 1
                result.getOrNull()!!.first().name shouldBe "Pizza Hut"
            }

            it("case-insensitive — 'PIZZA' matches same as 'pizza'") {
                val useCase = SearchRestaurantsUseCase(repository)
                val result  = useCase("PIZZA", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 1
            }

            it("query with no matches returns empty list") {
                val useCase = SearchRestaurantsUseCase(repository)
                val result  = useCase("sushi", SearchFilters()).first()
                result.getOrNull()!!.shouldBeEmpty()
            }

            it("partial match — 'burg' matches 'Burger King'") {
                val useCase = SearchRestaurantsUseCase(repository)
                val result  = useCase("burg", SearchFilters()).first()
                result.getOrNull()!!.first().name shouldBe "Burger King"
            }
        }

        // ── Rating filter ──────────────────────────────────────
        context("minimum rating filter") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("minRating 4.0 filters out restaurants below 4.0") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(minRating = 4.0)
                val result   = useCase("", filters).first()
                val ratings  = result.getOrNull()!!.map { it.rating }
                ratings.all { it >= 4.0 } shouldBe true
            }

            it("minRating 4.5 returns only top-rated restaurants") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(minRating = 4.5)
                val result   = useCase("", filters).first()
                result.getOrNull()!! shouldHaveSize 2  // 4.6 and 4.5
            }

            it("minRating null returns all restaurants") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(minRating = null)
                val result   = useCase("", filters).first()
                result.getOrNull()!! shouldHaveSize 5
            }

            it("minRating 5.0 returns empty list") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(minRating = 5.0)
                val result   = useCase("", filters).first()
                result.getOrNull()!!.shouldBeEmpty()
            }
        }

        // ── Delivery time filter ───────────────────────────────
        context("maximum delivery time filter") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("maxDeliveryTime 30 returns restaurants with <= 30 min delivery") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(maxDeliveryTime = 30)
                val result   = useCase("", filters).first()
                val times    = result.getOrNull()!!.map { it.avgDeliveryTime }
                times.all { it <= 30 } shouldBe true
                result.getOrNull()!! shouldHaveSize 3  // 30, 25, 20
            }

            it("maxDeliveryTime 20 returns only fastest restaurants") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(maxDeliveryTime = 20)
                val result   = useCase("", filters).first()
                result.getOrNull()!! shouldHaveSize 1
                result.getOrNull()!!.first().avgDeliveryTime shouldBe 20
            }

            it("maxDeliveryTime null returns all restaurants") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(maxDeliveryTime = null)
                val result   = useCase("", filters).first()
                result.getOrNull()!! shouldHaveSize 5
            }
        }

        // ── Sort options ───────────────────────────────────────
        context("sort by RATING descending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("restaurants are ordered highest rating first") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(sortBy = SortOption.RATING)
                val result   = useCase("", filters).first()
                val ratings  = result.getOrNull()!!.map { it.rating }
                ratings shouldBe ratings.sortedDescending()
            }

            it("first restaurant has highest rating") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(sortBy = SortOption.RATING)
                val result   = useCase("", filters).first()
                result.getOrNull()!!.first().rating shouldBe 4.6
            }
        }

        context("sort by DELIVERY_TIME ascending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("restaurants are ordered fastest delivery first") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(sortBy = SortOption.DELIVERY_TIME)
                val result   = useCase("", filters).first()
                val times    = result.getOrNull()!!.map { it.avgDeliveryTime }
                times shouldBe times.sorted()
            }

            it("first restaurant has shortest delivery time") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(sortBy = SortOption.DELIVERY_TIME)
                val result   = useCase("", filters).first()
                result.getOrNull()!!.first().avgDeliveryTime shouldBe 20
            }
        }

        context("sort by COST_LOW ascending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("restaurants are ordered cheapest first") {
                val useCase  = SearchRestaurantsUseCase(repository)
                val filters  = SearchFilters(sortBy = SortOption.COST_LOW)
                val result   = useCase("", filters).first()
                val costs    = result.getOrNull()!!.map { it.avgCostForTwo }
                costs shouldBe costs.sorted()
            }
        }

        // ── Combined filters ───────────────────────────────────
        context("combining query + rating filter + sort") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("biryani + rating>=4.0 + sort by rating returns filtered sorted list") {
                val useCase = SearchRestaurantsUseCase(repository)
                val filters = SearchFilters(
                    minRating = 4.0,
                    sortBy    = SortOption.RATING,
                )
                val result  = useCase("biryani", filters).first()
                val names   = result.getOrNull()!!.map { it.name }

                // Both biryani restaurants have rating >= 4.0
                names shouldHaveSize 2
                // Meghana (4.6) should come before Behrouz (4.3)
                names.first() shouldBe "Meghana Foods"
            }

            it("query + maxDeliveryTime 30 filters by both") {
                val useCase = SearchRestaurantsUseCase(repository)
                val filters = SearchFilters(maxDeliveryTime = 30)
                val result  = useCase("biryani", filters).first()

                // Meghana (30 min) ✅, Behrouz (55 min) ❌
                result.getOrNull()!! shouldHaveSize 1
                result.getOrNull()!!.first().name shouldBe "Meghana Foods"
            }
        }

        // ── Repository failure ─────────────────────────────────
        context("repository returns failure") {

            it("propagates failure as Result.failure") {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.failure(Exception("Network error")))

                val useCase = SearchRestaurantsUseCase(repository)
                val result  = useCase("pizza", SearchFilters()).first()

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Network error"
            }
        }
    }
})