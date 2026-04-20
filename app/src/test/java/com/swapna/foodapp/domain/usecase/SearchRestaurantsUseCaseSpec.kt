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
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
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

    // ✅ NEW — cost added to all restaurants
    // WHY? COST_LOW/COST_HIGH sort tests need distinct avgCostForTwo values
    // Without cost, sorting by cost = arbitrary order = flaky tests
    val allRestaurants = listOf(
        fakeRestaurant("r1", "Meghana Foods",   rating = 4.6, deliveryTime = 30, cuisines = listOf("Biryani", "Andhra"))
            .copy(avgCostForTwo = 600),
        fakeRestaurant("r2", "Pizza Hut",       rating = 4.1, deliveryTime = 40, cuisines = listOf("Pizza", "Italian"))
            .copy(avgCostForTwo = 400),
        fakeRestaurant("r3", "Burger King",     rating = 3.8, deliveryTime = 25, cuisines = listOf("Burger", "Fast Food"))
            .copy(avgCostForTwo = 300),
        fakeRestaurant("r4", "Behrouz Biryani", rating = 4.3, deliveryTime = 55, cuisines = listOf("Biryani", "Mughlai"))
            .copy(avgCostForTwo = 700),
        fakeRestaurant("r5", "Green Bowl",      rating = 4.5, deliveryTime = 20, cuisines = listOf("Healthy", "Salads"))
            .copy(avgCostForTwo = 200),
    )

    beforeEach {
        // ✅ NEW — clearAllMocks missing in original
        // WHY critical?
        // Without clearAllMocks, stubs from one context leak into next
        // e.g. "rating 5.0 returns empty" stub bleeds into "rating null" test
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    afterEach { Dispatchers.resetMain() }

    describe("SearchRestaurantsUseCase") {

        // ══════════════════════════════════════════════════════
        // Text search
        // ══════════════════════════════════════════════════════

        context("text search filtering") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("empty query returns all restaurants unfiltered") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 5
            }

            // ✅ NEW — whitespace-only query
            // WHY? query.isBlank() covers both "" and "   "
            // Without this test, removing isBlank() check breaks whitespace case silently
            it("whitespace-only query returns all restaurants unfiltered") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("   ", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 5
            }

            it("'biryani' query matches name and cuisine") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("biryani", SearchFilters()).first()
                val names = result.getOrNull()!!.map { it.name }
                // Meghana (Biryani cuisine) + Behrouz (Biryani in name AND cuisine)
                names shouldBe listOf("Meghana Foods", "Behrouz Biryani")
            }

            it("'pizza' query matches cuisine AND restaurant name") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("pizza", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 1
                result.getOrNull()!!.first().name shouldBe "Pizza Hut"
            }

            it("case-insensitive — PIZZA matches same as pizza") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("PIZZA", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 1
            }

            it("query with no matches returns empty list") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("sushi", SearchFilters()).first()
                result.getOrNull()!!.shouldBeEmpty()
            }

            it("partial name match — 'burg' matches Burger King") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("burg", SearchFilters()).first()
                result.getOrNull()!!.first().name shouldBe "Burger King"
            }

            // ✅ NEW — cuisine-only match (no name match)
            // WHY? applyTextFilter checks name OR cuisines
            // This test verifies the cuisine branch specifically
            it("cuisine-only match — 'andhra' matches Meghana Foods by cuisine") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("andhra", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize 1
                result.getOrNull()!!.first().name shouldBe "Meghana Foods"
            }

            // ✅ NEW — repository called with correct query
            // WHY? Use case delegates to repository — verify it passes query through
            it("repository.searchRestaurants called with exact query") {
                SearchRestaurantsUseCase(repository).invoke("pizza", SearchFilters()).first()
                verify { repository.searchRestaurants("pizza", any()) }
            }
        }

        // ══════════════════════════════════════════════════════
        // Rating filter
        // ══════════════════════════════════════════════════════

        context("minimum rating filter") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("minRating 4.0 filters out restaurants below 4.0") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = 4.0)).first()
                result.getOrNull()!!.map { it.rating }
                    .all { it >= 4.0 } shouldBe true
            }

            it("minRating 4.5 returns only top-rated restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = 4.5)).first()
                // r1=4.6 ✅ r5=4.5 ✅ others below
                result.getOrNull()!! shouldHaveSize 2
            }

            // ✅ NEW — exactly equal to minRating is INCLUDED
            // WHY? filter uses >= not >
            // "rating == minRating" must pass, not be filtered out
            // This is the boundary condition bug most likely to be missed
            it("restaurant with rating exactly equal to minRating is included") {
                // r2=4.1, r3=3.8, r4=4.3, r1=4.6, r5=4.5
                // minRating=4.1 → r2(4.1) must be included
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = 4.1)).first()
                result.getOrNull()!!.any { it.name == "Pizza Hut" } shouldBe true
            }

            // ✅ NEW — restaurant just below minRating is EXCLUDED
            // WHY? pairs with boundary test above to confirm >= semantics
            it("restaurant with rating just below minRating is excluded") {
                // r3=3.8 → minRating=3.9 → r3 must be excluded
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = 3.9)).first()
                result.getOrNull()!!.none { it.name == "Burger King" } shouldBe true
            }

            it("minRating null returns all restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = null)).first()
                result.getOrNull()!! shouldHaveSize 5
            }

            it("minRating 5.0 returns empty list — no restaurant reaches max") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = 5.0)).first()
                result.getOrNull()!!.shouldBeEmpty()
            }

            // ✅ NEW — minRating lower than all = return all
            // WHY? confirms filter is not overly aggressive
            it("minRating 1.0 returns all restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = 1.0)).first()
                result.getOrNull()!! shouldHaveSize 5
            }
        }

        // ══════════════════════════════════════════════════════
        // Delivery time filter
        // ══════════════════════════════════════════════════════

        context("maximum delivery time filter") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("maxDeliveryTime 30 returns restaurants with <= 30 min delivery") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = 30)).first()
                result.getOrNull()!!.map { it.avgDeliveryTime }
                    .all { it <= 30 } shouldBe true
                // r1=30 ✅ r3=25 ✅ r5=20 ✅
                result.getOrNull()!! shouldHaveSize 3
            }

            it("maxDeliveryTime 20 returns only fastest restaurant") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = 20)).first()
                result.getOrNull()!! shouldHaveSize 1
                result.getOrNull()!!.first().avgDeliveryTime shouldBe 20
            }

            // ✅ NEW — exactly equal to maxDeliveryTime is INCLUDED
            // WHY? filter uses <= not
            // Restaurant with delivery time == maxDeliveryTime must be included
            it("restaurant with delivery time exactly equal to max is included") {
                // r1=30 → maxDeliveryTime=30 → r1 must be included
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = 30)).first()
                result.getOrNull()!!.any { it.name == "Meghana Foods" } shouldBe true
            }

            // ✅ NEW — restaurant just above maxDeliveryTime is EXCLUDED
            // WHY? pairs with boundary test to confirm <= semantics
            it("restaurant with delivery time just above max is excluded") {
                // r2=40 → maxDeliveryTime=39 → r2 must be excluded
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = 39)).first()
                result.getOrNull()!!.none { it.name == "Pizza Hut" } shouldBe true
            }

            it("maxDeliveryTime null returns all restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = null)).first()
                result.getOrNull()!! shouldHaveSize 5
            }

            // ✅ NEW — maxDeliveryTime 0 = nothing passes
            // WHY? lower bound edge case — no restaurant delivers in 0 mins
            it("maxDeliveryTime 0 returns empty list") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = 0)).first()
                result.getOrNull()!!.shouldBeEmpty()
            }
        }

        // ══════════════════════════════════════════════════════
        // Sort options
        // ══════════════════════════════════════════════════════

        context("sort by RATING descending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("restaurants ordered highest rating first") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.RATING)).first()
                val ratings = result.getOrNull()!!.map { it.rating }
                ratings shouldBe ratings.sortedDescending()
            }

            it("first restaurant has highest rating 4.6") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.RATING)).first()
                result.getOrNull()!!.first().rating shouldBe 4.6
            }

            it("last restaurant has lowest rating 3.8") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.RATING)).first()
                result.getOrNull()!!.last().rating shouldBe 3.8
            }
        }

        context("sort by DELIVERY_TIME ascending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("restaurants ordered fastest delivery first") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.DELIVERY_TIME)).first()
                val times = result.getOrNull()!!.map { it.avgDeliveryTime }
                times shouldBe times.sorted()
            }

            it("first restaurant has shortest delivery time 20") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.DELIVERY_TIME)).first()
                result.getOrNull()!!.first().avgDeliveryTime shouldBe 20
            }

            // ✅ NEW — last has longest delivery time
            it("last restaurant has longest delivery time 55") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.DELIVERY_TIME)).first()
                result.getOrNull()!!.last().avgDeliveryTime shouldBe 55
            }
        }

        context("sort by COST_LOW ascending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("restaurants ordered cheapest first") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_LOW)).first()
                val costs = result.getOrNull()!!.map { it.avgCostForTwo }
                costs shouldBe costs.sorted()
            }

            // ✅ NEW — first is cheapest
            it("first restaurant has lowest cost 200") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_LOW)).first()
                result.getOrNull()!!.first().avgCostForTwo shouldBe 200
            }

            // ✅ NEW — last is most expensive
            it("last restaurant has highest cost 700") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_LOW)).first()
                result.getOrNull()!!.last().avgCostForTwo shouldBe 700
            }
        }

        // ✅ NEW — COST_HIGH sort completely missing in original
        // WHY? COST_HIGH is a separate SortOption branch in applySort()
        // Without this test — removing COST_HIGH case = no test failure
        context("sort by COST_HIGH descending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("restaurants ordered most expensive first") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_HIGH)).first()
                val costs = result.getOrNull()!!.map { it.avgCostForTwo }
                costs shouldBe costs.sortedDescending()
            }

            it("first restaurant has highest cost 700") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_HIGH)).first()
                result.getOrNull()!!.first().avgCostForTwo shouldBe 700
            }

            it("last restaurant has lowest cost 200") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_HIGH)).first()
                result.getOrNull()!!.last().avgCostForTwo shouldBe 200
            }
        }

        // ✅ NEW — RELEVANCE sort completely missing in original
        // WHY? RELEVANCE is a branch in applySort() — returns `this` (no change)
        // Without this test — the branch is never exercised
        context("sort by RELEVANCE — original order preserved") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("RELEVANCE keeps original repository order") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.RELEVANCE)).first()
                // Original order: r1, r2, r3, r4, r5
                result.getOrNull()!!.map { it.id } shouldBe
                        listOf("r1", "r2", "r3", "r4", "r5")
            }

            it("RELEVANCE is the default sort when no filter specified") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters()).first()
                // Default SearchFilters() has sortBy = RELEVANCE
                result.getOrNull()!!.map { it.id } shouldBe
                        listOf("r1", "r2", "r3", "r4", "r5")
            }
        }

        // ══════════════════════════════════════════════════════
        // Combined filters
        // ══════════════════════════════════════════════════════

        context("combining query + rating filter + sort") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allRestaurants))
            }

            it("biryani + rating>=4.0 + sort by rating returns filtered sorted list") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    "biryani",
                    SearchFilters(minRating = 4.0, sortBy = SortOption.RATING),
                ).first()
                val names = result.getOrNull()!!.map { it.name }
                names shouldHaveSize 2
                // Meghana(4.6) before Behrouz(4.3) — sorted descending
                names.first() shouldBe "Meghana Foods"
            }

            it("query + maxDeliveryTime 30 filters by both") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    "biryani",
                    SearchFilters(maxDeliveryTime = 30),
                ).first()
                result.getOrNull()!! shouldHaveSize 1
                // Behrouz(55min) excluded, only Meghana(30min) remains
                result.getOrNull()!!.first().name shouldBe "Meghana Foods"
            }

            // ✅ NEW — all three: text + rating + delivery time
            // WHY? filters chain: text → rating → delivery → sort
            // Test verifies chain order is correct
            it("text + rating + delivery time all applied in chain") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    "biryani",
                    SearchFilters(
                        minRating       = 4.0,
                        maxDeliveryTime = 35,
                        sortBy          = SortOption.RATING,
                    ),
                ).first()
                // biryani: Meghana(30min, 4.6) ✅ Behrouz(55min, 4.3) ❌ delivery too slow
                result.getOrNull()!! shouldHaveSize 1
                result.getOrNull()!!.first().name shouldBe "Meghana Foods"
            }

            // ✅ NEW — filter combination produces empty result
            // WHY? when filters are too strict → empty is correct, not an error
            it("overly strict filters return empty list gracefully") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    "biryani",
                    SearchFilters(
                        minRating       = 4.8,  // nothing reaches 4.8
                        maxDeliveryTime = 30,
                    ),
                ).first()
                result.getOrNull()!!.shouldBeEmpty()
            }
        }

        // ✅ NEW — empty list from repository passthrough
        // WHY? original never tested empty repository result
        // If repo returns [], use case must return [] not crash
        context("repository returns empty list") {

            it("empty repository result stays empty after all filters") {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(emptyList()))

                val result = SearchRestaurantsUseCase(repository)
                    .invoke("pizza", SearchFilters()).first()

                result.getOrNull()!!.shouldBeEmpty()
            }
        }

        // ══════════════════════════════════════════════════════
        // Repository failure
        // ══════════════════════════════════════════════════════

        context("repository returns failure") {

            it("propagates failure as Result.failure") {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.failure(Exception("Network error")))

                val result = SearchRestaurantsUseCase(repository)
                    .invoke("pizza", SearchFilters()).first()

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe "Network error"
            }

            // ✅ NEW — success after failure is independent
            // WHY? verify no state pollution between Result types
            it("success result after failure works correctly") {
                every {
                    repository.searchRestaurants(any(), any())
                } returnsMany listOf(
                    flowOf(Result.failure(Exception("error"))),
                    flowOf(Result.success(allRestaurants)),
                )

                val useCase = SearchRestaurantsUseCase(repository)

                val firstResult  = useCase("pizza", SearchFilters()).first()
                val secondResult = useCase("pizza", SearchFilters()).first()

                firstResult.isFailure  shouldBe true
                secondResult.isSuccess shouldBe true
            }
        }
    }
})