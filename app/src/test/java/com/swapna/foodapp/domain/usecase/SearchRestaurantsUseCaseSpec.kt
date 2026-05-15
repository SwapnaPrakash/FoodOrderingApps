package com.swapna.foodapp.domain.usecase

import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.search.SearchRestaurantsUseCase
import com.swapna.foodapp.presentation.common.fakes.allSearchRestaurants
import com.swapna.foodapp.utils.TestConstants.COST_HIGHEST
import com.swapna.foodapp.utils.TestConstants.COST_LOWEST
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FASTEST
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FILTER_0
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FILTER_20
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FILTER_30
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FILTER_35
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FILTER_39
import com.swapna.foodapp.utils.TestConstants.DELIVERY_SLOWEST
import com.swapna.foodapp.utils.TestConstants.ERR_NETWORK_MSG
import com.swapna.foodapp.utils.TestConstants.QUERY_ANDHRA
import com.swapna.foodapp.utils.TestConstants.QUERY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.QUERY_BURGER
import com.swapna.foodapp.utils.TestConstants.QUERY_PIZZA
import com.swapna.foodapp.utils.TestConstants.QUERY_PIZZA_UPPER
import com.swapna.foodapp.utils.TestConstants.QUERY_SUSHI
import com.swapna.foodapp.utils.TestConstants.QUERY_WHITESPACE
import com.swapna.foodapp.utils.TestConstants.RATING_FILTER_10
import com.swapna.foodapp.utils.TestConstants.RATING_FILTER_39
import com.swapna.foodapp.utils.TestConstants.RATING_FILTER_40
import com.swapna.foodapp.utils.TestConstants.RATING_FILTER_41
import com.swapna.foodapp.utils.TestConstants.RATING_FILTER_45
import com.swapna.foodapp.utils.TestConstants.RATING_FILTER_50
import com.swapna.foodapp.utils.TestConstants.RATING_HIGHEST
import com.swapna.foodapp.utils.TestConstants.RATING_LOWEST
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_BEHROUZ
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_BURGER_KING
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_MEGHANA
import com.swapna.foodapp.utils.TestConstants.SEARCH_NAME_PIZZA_HUT
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_2
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_3
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_4
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESTAURANT_ID_5
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESULT_COUNT_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESULT_COUNT_2
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESULT_COUNT_ALL
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

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    afterEach { Dispatchers.resetMain() }

    describe("SearchRestaurantsUseCase") {

        // Text search
        context("text search filtering") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
            }

            it("empty query returns all restaurants unfiltered") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_ALL
            }

            it("whitespace-only query returns all restaurants unfiltered") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_WHITESPACE, SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_ALL
            }

            it("'biryani' query matches name and cuisine") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_BIRYANI, SearchFilters()).first()
                val names = result.getOrNull()!!.map { it.name }
                // Meghana (Biryani cuisine) + Behrouz (Biryani in name AND cuisine)
                names shouldBe listOf(SEARCH_NAME_MEGHANA, SEARCH_NAME_BEHROUZ)
            }

            it("'pizza' query matches cuisine AND restaurant name") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_PIZZA, SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_1
                result.getOrNull()!!.first().name shouldBe SEARCH_NAME_PIZZA_HUT
            }

            it("case-insensitive — PIZZA matches same as pizza") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_PIZZA_UPPER, SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_1
            }

            it("query with no matches returns empty list") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_SUSHI, SearchFilters()).first()
                result.getOrNull()!!.shouldBeEmpty()
            }

            it("partial name match — 'burg' matches Burger King") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_BURGER, SearchFilters()).first()
                result.getOrNull()!!.first().name shouldBe SEARCH_NAME_BURGER_KING
            }

            it("cuisine-only match — 'andhra' matches Meghana Foods by cuisine") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_ANDHRA, SearchFilters()).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_1
                result.getOrNull()!!.first().name shouldBe SEARCH_NAME_MEGHANA
            }

            it("repository.searchRestaurants called with exact query") {
                SearchRestaurantsUseCase(repository).invoke(QUERY_PIZZA, SearchFilters()).first()
                verify { repository.searchRestaurants(QUERY_PIZZA, any()) }
            }
        }

        // Rating filter
        context("minimum rating filter") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
            }

            it("minRating 4.0 filters out restaurants below 4.0") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = RATING_FILTER_40)).first()
                result.getOrNull()!!.map { it.rating }
                    .all { it >= RATING_FILTER_40 } shouldBe true
            }

            it("minRating 4.5 returns only top-rated restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = RATING_FILTER_45)).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_2
            }

            it("restaurant with rating exactly equal to minRating is included") {
                // r2=4.1 → minRating=4.1 → r2 must be included (>= not >)
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = RATING_FILTER_41)).first()
                result.getOrNull()!!.any { it.name == SEARCH_NAME_PIZZA_HUT } shouldBe true
            }

            it("restaurant with rating just below minRating is excluded") {
                // r3=3.8 → minRating=3.9 → r3 must be excluded
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = RATING_FILTER_39)).first()
                result.getOrNull()!!.none { it.name == SEARCH_NAME_BURGER_KING } shouldBe true
            }

            it("minRating null returns all restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = null)).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_ALL
            }

            it("minRating 5.0 returns empty list — no restaurant reaches max") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = RATING_FILTER_50)).first()
                result.getOrNull()!!.shouldBeEmpty()
            }

            it("minRating 1.0 returns all restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(minRating = RATING_FILTER_10)).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_ALL
            }
        }

        // Delivery time filter
        context("maximum delivery time filter") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
            }

            it("maxDeliveryTime 30 returns restaurants with <= 30 min delivery") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = DELIVERY_FILTER_30))
                    .first()
                result.getOrNull()!!.map { it.avgDeliveryTime }
                    .all { it <= DELIVERY_FILTER_30 } shouldBe true
                result.getOrNull()!! shouldHaveSize 3
            }

            it("maxDeliveryTime 20 returns only fastest restaurant") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = DELIVERY_FILTER_20))
                    .first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_1
                result.getOrNull()!!.first().avgDeliveryTime shouldBe DELIVERY_FASTEST
            }

            it("restaurant with delivery time exactly equal to max is included") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = DELIVERY_FILTER_30)).first()
                result.getOrNull()!!.any { it.name == SEARCH_NAME_MEGHANA } shouldBe true
            }

            it("restaurant with delivery time just above max is excluded") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = DELIVERY_FILTER_39))
                    .first()
                result.getOrNull()!!.none { it.name == SEARCH_NAME_PIZZA_HUT } shouldBe true
            }

            it("maxDeliveryTime null returns all restaurants") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = null)).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_ALL
            }

            it("maxDeliveryTime 0 returns empty list") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(maxDeliveryTime = DELIVERY_FILTER_0))
                    .first()
                result.getOrNull()!!.shouldBeEmpty()
            }
        }

        // Sort options
        context("sort by RATING descending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
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
                result.getOrNull()!!.first().rating shouldBe RATING_HIGHEST
            }

            it("last restaurant has lowest rating 3.8") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.RATING)).first()
                result.getOrNull()!!.last().rating shouldBe RATING_LOWEST
            }
        }

        context("sort by DELIVERY_TIME ascending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
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
                result.getOrNull()!!.first().avgDeliveryTime shouldBe DELIVERY_FASTEST
            }

            it("last restaurant has longest delivery time 55") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.DELIVERY_TIME)).first()
                result.getOrNull()!!.last().avgDeliveryTime shouldBe DELIVERY_SLOWEST
            }
        }

        context("sort by COST_LOW ascending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
            }

            it("restaurants ordered cheapest first") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_LOW)).first()
                val costs = result.getOrNull()!!.map { it.avgCostForTwo }
                costs shouldBe costs.sorted()
            }

            it("first restaurant has lowest cost 200") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_LOW)).first()
                result.getOrNull()!!.first().avgCostForTwo shouldBe COST_LOWEST
            }

            it("last restaurant has highest cost 700") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_LOW)).first()
                result.getOrNull()!!.last().avgCostForTwo shouldBe COST_HIGHEST
            }
        }

        context("sort by COST_HIGH descending") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
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
                result.getOrNull()!!.first().avgCostForTwo shouldBe COST_HIGHEST
            }

            it("last restaurant has lowest cost 200") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.COST_HIGH)).first()
                result.getOrNull()!!.last().avgCostForTwo shouldBe COST_LOWEST
            }
        }

        context("sort by RELEVANCE — original order preserved") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
            }

            it("RELEVANCE keeps original repository order") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters(sortBy = SortOption.RELEVANCE)).first()
                result.getOrNull()!!.map { it.id } shouldBe listOf(
                    SEARCH_RESTAURANT_ID_1,
                    SEARCH_RESTAURANT_ID_2,
                    SEARCH_RESTAURANT_ID_3,
                    SEARCH_RESTAURANT_ID_4,
                    SEARCH_RESTAURANT_ID_5,
                )
            }

            it("RELEVANCE is the default sort when no filter specified") {
                val result = SearchRestaurantsUseCase(repository)
                    .invoke("", SearchFilters()).first()
                result.getOrNull()!!.map { it.id } shouldBe listOf(
                    SEARCH_RESTAURANT_ID_1,
                    SEARCH_RESTAURANT_ID_2,
                    SEARCH_RESTAURANT_ID_3,
                    SEARCH_RESTAURANT_ID_4,
                    SEARCH_RESTAURANT_ID_5,
                )
            }
        }

        // Combined filters
        context("combining query + rating filter + sort") {

            beforeEach {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(allSearchRestaurants))
            }

            it("biryani + rating>=4.0 + sort by rating returns filtered sorted list") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    QUERY_BIRYANI,
                    SearchFilters(minRating = RATING_FILTER_40, sortBy = SortOption.RATING),
                ).first()
                val names = result.getOrNull()!!.map { it.name }
                names shouldHaveSize SEARCH_RESULT_COUNT_2
                names.first() shouldBe SEARCH_NAME_MEGHANA
            }

            it("query + maxDeliveryTime 30 filters by both") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    QUERY_BIRYANI,
                    SearchFilters(maxDeliveryTime = DELIVERY_FILTER_30),
                ).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_1
                // Behrouz(55min) excluded, only Meghana(30min) remains
                result.getOrNull()!!.first().name shouldBe SEARCH_NAME_MEGHANA
            }

            it("text + rating + delivery time all applied in chain") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    QUERY_BIRYANI,
                    SearchFilters(
                        minRating = RATING_FILTER_40,
                        maxDeliveryTime = DELIVERY_FILTER_35,
                        sortBy = SortOption.RATING,
                    ),
                ).first()
                result.getOrNull()!! shouldHaveSize SEARCH_RESULT_COUNT_1
                result.getOrNull()!!.first().name shouldBe SEARCH_NAME_MEGHANA
            }

            it("overly strict filters return empty list gracefully") {
                val result = SearchRestaurantsUseCase(repository).invoke(
                    QUERY_BIRYANI,
                    SearchFilters(
                        minRating = RATING_FILTER_50,
                        maxDeliveryTime = DELIVERY_FILTER_30,
                    ),
                ).first()
                result.getOrNull()!!.shouldBeEmpty()
            }
        }

        // Empty repository result
        context("repository returns empty list") {

            it("empty repository result stays empty after all filters") {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.success(emptyList()))

                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_PIZZA, SearchFilters()).first()

                result.getOrNull()!!.shouldBeEmpty()
            }
        }

        // Repository failure
        context("repository returns failure") {

            it("propagates failure as Result.failure") {
                every {
                    repository.searchRestaurants(any(), any())
                } returns flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                val result = SearchRestaurantsUseCase(repository)
                    .invoke(QUERY_PIZZA, SearchFilters()).first()

                result.isFailure shouldBe true
                result.exceptionOrNull()?.message shouldBe ERR_NETWORK_MSG
            }

            it("success result after failure works correctly") {
                every {
                    repository.searchRestaurants(any(), any())
                } returnsMany listOf(
                    flowOf(Result.failure(Exception(ERR_NETWORK_MSG))),
                    flowOf(Result.success(allSearchRestaurants)),
                )

                val useCase = SearchRestaurantsUseCase(repository)

                val firstResult = useCase(QUERY_PIZZA, SearchFilters()).first()
                val secondResult = useCase(QUERY_PIZZA, SearchFilters()).first()

                firstResult.isFailure shouldBe true
                secondResult.isSuccess shouldBe true
            }
        }
    }
})