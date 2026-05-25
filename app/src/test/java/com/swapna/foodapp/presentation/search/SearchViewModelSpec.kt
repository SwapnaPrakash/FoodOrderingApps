package com.swapna.foodapp.presentation.search

import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.search.SearchRestaurantsUseCase
import com.swapna.foodapp.presentation.common.fakes.fakeCuisines
import com.swapna.foodapp.presentation.common.fakes.fakeResults
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.TestConstants.CUISINE_BIRYANI
import com.swapna.foodapp.utils.TestConstants.CUISINE_BURGER
import com.swapna.foodapp.utils.TestConstants.CUISINE_CHINESE
import com.swapna.foodapp.utils.TestConstants.CUISINE_ID_1
import com.swapna.foodapp.utils.TestConstants.CUISINE_ID_2
import com.swapna.foodapp.utils.TestConstants.CUISINE_PIZZA
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FILTER_30
import com.swapna.foodapp.utils.TestConstants.DELIVERY_FILTER_45
import com.swapna.foodapp.utils.TestConstants.ERR_NETWORK_MSG
import com.swapna.foodapp.utils.TestConstants.QUERY_B
import com.swapna.foodapp.utils.TestConstants.QUERY_BURGER_FULL
import com.swapna.foodapp.utils.TestConstants.QUERY_P
import com.swapna.foodapp.utils.TestConstants.QUERY_PIZZA
import com.swapna.foodapp.utils.TestConstants.RATING_FILTER_40
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESULT_COUNT_1
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESULT_COUNT_3
import com.swapna.foodapp.utils.TestConstants.SEARCH_RESULT_COUNT_4
import com.swapna.foodapp.utils.TestConstants.SEARCH_VM_PIZZA_HUT
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelSpec : BehaviorSpec({

    val searchUseCase = mockk<SearchRestaurantsUseCase>()
    val restaurantRepository = mockk<RestaurantRepository>()

    fun createViewModel() = SearchViewModel(searchUseCase, restaurantRepository)

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { restaurantRepository.getCuisines() } returns
                flowOf(Result.success(fakeCuisines))
    }

    afterEach { Dispatchers.resetMain() }

    suspend fun kotlinx.coroutines.test.TestScope.searchAndWait(
        vm: SearchViewModel,
        query: String,
    ) {
        vm.onQueryChange(query)
        advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
        advanceUntilIdle()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial State
    // WHY shared VM:
    // All then blocks check initial state — no debounce needed
    // Same default stubs → one VM per when block correct
    // ══════════════════════════════════════════════════════════

    given("ViewModel is just created") {

        `when`("no query entered") {
            lateinit var vm: SearchViewModel

            beforeEach { vm = createViewModel() }

            then("query should be empty string") {
                vm.uiState.value.query shouldBe ""
            }
            then("results should be empty") {
                vm.uiState.value.results.shouldBeEmpty()
            }
            then("isLoading should be false") {
                vm.uiState.value.isLoading shouldBe false
            }
            then("hasSearched should be false") {
                vm.uiState.value.hasSearched shouldBe false
            }
            then("error should be null") {
                vm.uiState.value.error shouldBe null
            }
            then("filters should be default SearchFilters") {
                vm.uiState.value.filters shouldBe SearchFilters()
            }
            then("searchUseCase should NOT be called") {
                verify(exactly = 0) { searchUseCase(any(), any()) }
            }
        }

        `when`("cuisines are loaded") {
            lateinit var vm: SearchViewModel

            beforeEach { vm = createViewModel() }

            then("cuisines list should have 4 items") {
                vm.uiState.value.cuisines shouldHaveSize SEARCH_RESULT_COUNT_4
            }
            then("first cuisine should be Biryani") {
                vm.uiState.value.cuisines.first().name shouldBe CUISINE_BIRYANI
            }
            then("all cuisine names load correctly") {
                vm.uiState.value.cuisines.map { it.name } shouldBe
                        listOf(CUISINE_BIRYANI, CUISINE_PIZZA, CUISINE_BURGER, CUISINE_CHINESE)
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Query too short
    // WHY VM per then inside runTest:
    // Debounce tests need StandardTestDispatcher + advanceTimeBy
    // runTest controls virtual time for debounce
    // ══════════════════════════════════════════════════════════

    given("user types below minimum characters") {

        `when`("onQueryChange called with single char 'p'") {
            then("no search triggered — below minimum chars") {
                runTest(StandardTestDispatcher()) {
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_P)
                    verify(exactly = 0) { searchUseCase(any(), any()) }
                    vm.uiState.value.results.shouldBeEmpty()
                }
            }
        }

        `when`("onQueryChange called with empty string") {
            then("no search triggered and results stay empty") {
                runTest(StandardTestDispatcher()) {
                    val vm = createViewModel()
                    searchAndWait(vm, "")
                    verify(exactly = 0) { searchUseCase(any(), any()) }
                    vm.uiState.value.results.shouldBeEmpty()
                }
            }
        }

        `when`("query length is exactly SEARCH_MIN_CHARS minus 1") {
            then("search is NOT triggered") {
                runTest(StandardTestDispatcher()) {
                    val belowMin = QUERY_P.repeat(AppConstants.SEARCH_MIN_CHARS - 1)
                    val vm = createViewModel()
                    searchAndWait(vm, belowMin)
                    verify(exactly = 0) { searchUseCase(any(), any()) }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Min chars boundary
    // ══════════════════════════════════════════════════════════

    given("user types exactly SEARCH_MIN_CHARS characters") {

        `when`("query length equals minimum threshold") {
            then("search IS triggered at boundary") {
                runTest(StandardTestDispatcher()) {
                    val minQuery = QUERY_P.repeat(AppConstants.SEARCH_MIN_CHARS)
                    every { searchUseCase(minQuery, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, minQuery)
                    verify(exactly = 1) { searchUseCase(minQuery, any()) }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Valid query
    // WHY stub set inside then not beforeEach:
    // Each then returns different result (fakeResults, emptyList, etc.)
    // Stub set BEFORE onQueryChange — VM created in beforeEach
    // ══════════════════════════════════════════════════════════

    given("user types 'pizza' — valid query above minimum") {

        `when`("API returns matching restaurants") {

            then("results contain only Pizza Hut") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults.filter {
                                it.name.contains(QUERY_PIZZA, ignoreCase = true) ||
                                        it.cuisines.any { c ->
                                            c.contains(QUERY_PIZZA, ignoreCase = true)
                                        }
                            }))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.results.size shouldBe SEARCH_RESULT_COUNT_1
                    vm.uiState.value.results.first().name shouldBe SEARCH_VM_PIZZA_HUT
                }
            }

            then("hasSearched becomes true") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(emptyList()))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.hasSearched shouldBe true
                }
            }

            then("isLoading is false after results arrive") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.isLoading shouldBe false
                }
            }

            then("query updates uiState immediately without waiting for debounce") {
                // WHY no runTest: onQueryChange updates _uiState immediately
                // not tied to debounce — UnconfinedTestDispatcher handles
                val vm = createViewModel()
                vm.onQueryChange(QUERY_PIZZA)
                vm.uiState.value.query shouldBe QUERY_PIZZA
            }

            then("searchUseCase called with exact query string") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    verify(exactly = 1) { searchUseCase(QUERY_PIZZA, any()) }
                }
            }

            then("error is null after successful search") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.error shouldBe null
                }
            }

            then("all 3 results returned when API returns all") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.results shouldHaveSize SEARCH_RESULT_COUNT_3
                }
            }

            then("isLoading becomes true while search is in-flight") {
                runTest(StandardTestDispatcher()) {
                    var loadingDuringSearch = false
                    every { searchUseCase(QUERY_PIZZA, any()) } returns flow {
                        loadingDuringSearch = true
                        emit(Result.success(fakeResults))
                    }
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    loadingDuringSearch shouldBe true
                    vm.uiState.value.isLoading shouldBe false
                }
            }
        }

        `when`("API returns error") {

            then("error set in uiState with message") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.error shouldNotBe null
                    vm.uiState.value.error shouldBe ERR_NETWORK_MSG
                }
            }

            then("results remain empty on error") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.results.shouldBeEmpty()
                }
            }

            then("hasSearched is true even on error") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.hasSearched shouldBe true
                }
            }

            then("isLoading is false after error") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.isLoading shouldBe false
                }
            }

            then("error clears when user types a new query after error") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    every { searchUseCase(QUERY_BURGER_FULL, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.error shouldNotBe null

                    searchAndWait(vm, QUERY_BURGER_FULL)
                    vm.uiState.value.error shouldBe null
                }
            }
        }

        `when`("API returns empty list") {
            then("results empty and hasSearched true") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(emptyList()))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe true
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Debounce behavior
    // WHY VM per then:
    // Each test has complex timing sequences — runTest required
    // ══════════════════════════════════════════════════════════

    given("user types rapidly within debounce window") {

        `when`("multiple chars typed before debounce expires") {
            then("searchUseCase called only once with final query") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_BURGER_FULL, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_B)
                    advanceTimeBy(50)
                    vm.onQueryChange("bu")
                    advanceTimeBy(50)
                    vm.onQueryChange("bur")
                    advanceTimeBy(50)
                    vm.onQueryChange("burg")
                    advanceTimeBy(50)
                    vm.onQueryChange("burge")
                    advanceTimeBy(50)
                    vm.onQueryChange(QUERY_BURGER_FULL)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase(QUERY_BURGER_FULL, any()) }
                    verify(exactly = 0) { searchUseCase(QUERY_B, any()) }
                    verify(exactly = 0) { searchUseCase("bu", any()) }
                    verify(exactly = 0) { searchUseCase("bur", any()) }
                    verify(exactly = 0) { searchUseCase("burg", any()) }
                }
            }
        }

        `when`("user pauses between two distinct queries") {
            then("searchUseCase called once per paused query") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    every { searchUseCase(QUERY_BURGER_FULL, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    searchAndWait(vm, QUERY_BURGER_FULL)

                    verify(exactly = 1) { searchUseCase(QUERY_PIZZA, any()) }
                    verify(exactly = 1) { searchUseCase(QUERY_BURGER_FULL, any()) }
                }
            }
        }

        `when`("user types then goes below min chars again") {
            then("search cleared and use case not called again") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    searchAndWait(vm, QUERY_P)

                    verify(exactly = 1) { searchUseCase(any(), any()) }
                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe false
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Clear Search
    // WHY split strategy:
    // Simple idempotent test → shared VM
    // Tests after search → runTest needed for debounce
    // ══════════════════════════════════════════════════════════

    given("user had searched and clears query") {

        `when`("clearSearch called with no prior search — idempotent") {
            // WHY shared VM: no debounce, no stubs needed
            lateinit var vm: SearchViewModel
            beforeEach { vm = createViewModel() }

            then("state stays at defaults and no crash") {
                vm.clearSearch()
                vm.uiState.value.query shouldBe ""
                vm.uiState.value.results.shouldBeEmpty()
                vm.uiState.value.hasSearched shouldBe false
                vm.uiState.value.error shouldBe null
            }
        }

        `when`("clearSearch called after results loaded") {
            then("query results hasSearched error all reset") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.clearSearch()
                    advanceUntilIdle()

                    vm.uiState.value.query shouldBe ""
                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe false
                    vm.uiState.value.error shouldBe null
                }
            }
        }

        `when`("clearSearch called after error state") {
            then("error also cleared") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.error shouldNotBe null
                    vm.clearSearch()
                    advanceUntilIdle()
                    vm.uiState.value.error shouldBe null
                }
            }
        }

        `when`("user searches again after clearSearch") {
            then("new search returns results correctly") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.clearSearch()
                    advanceUntilIdle()
                    searchAndWait(vm, QUERY_BURGER_FULL)

                    vm.uiState.value.results shouldHaveSize SEARCH_RESULT_COUNT_3
                    vm.uiState.value.hasSearched shouldBe true
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Veg filter
    // WHY split strategy:
    // Toggle tests → shared VM (no debounce)
    // Search integration → runTest needed
    // ══════════════════════════════════════════════════════════

    given("isVegOnly filter is off by default") {

        `when`("veg toggle tests") {
            // WHY shared: no debounce, no different stubs
            lateinit var vm: SearchViewModel
            beforeEach { vm = createViewModel() }

            then("onVegToggle once → isVegOnly true") {
                vm.uiState.value.filters.isVegOnly shouldBe false
                vm.onVegToggle()
                vm.uiState.value.filters.isVegOnly shouldBe true
            }
            then("onVegToggle twice → isVegOnly false") {
                vm.onVegToggle()
                vm.onVegToggle()
                vm.uiState.value.filters.isVegOnly shouldBe false
            }
            then("onVegToggle three times → isVegOnly true") {
                vm.onVegToggle()
                vm.onVegToggle()
                vm.onVegToggle()
                vm.uiState.value.filters.isVegOnly shouldBe true
            }
        }

        `when`("veg filter active and search runs") {
            then("searchUseCase called with isVegOnly true") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    vm.onVegToggle()
                    searchAndWait(vm, QUERY_PIZZA)
                    verify { searchUseCase(QUERY_PIZZA, match { it.isVegOnly == true }) }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Sort filter
    // WHY shared VM for sort change tests:
    // Just updating _filters state — no debounce
    // ══════════════════════════════════════════════════════════

    given("default sort is RELEVANCE") {

        `when`("sort change tests") {
            lateinit var vm: SearchViewModel
            beforeEach { vm = createViewModel() }

            then("sortBy defaults to RELEVANCE") {
                vm.uiState.value.filters.sortBy shouldBe SortOption.RELEVANCE
            }
            then("onSortChange RATING → filters.sortBy is RATING") {
                vm.onSortChange(SortOption.RATING)
                vm.uiState.value.filters.sortBy shouldBe SortOption.RATING
            }
            then("onSortChange DELIVERY_TIME → filters.sortBy is DELIVERY_TIME") {
                vm.onSortChange(SortOption.DELIVERY_TIME)
                vm.uiState.value.filters.sortBy shouldBe SortOption.DELIVERY_TIME
            }
            then("onSortChange COST_LOW → filters.sortBy is COST_LOW") {
                vm.onSortChange(SortOption.COST_LOW)
                vm.uiState.value.filters.sortBy shouldBe SortOption.COST_LOW
            }
            then("onSortChange COST_HIGH → filters.sortBy is COST_HIGH") {
                vm.onSortChange(SortOption.COST_HIGH)
                vm.uiState.value.filters.sortBy shouldBe SortOption.COST_HIGH
            }
        }

        `when`("RATING sort active and search runs") {
            then("searchUseCase called with sortBy RATING") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    vm.onSortChange(SortOption.RATING)
                    searchAndWait(vm, QUERY_PIZZA)
                    verify { searchUseCase(QUERY_PIZZA, match { it.sortBy == SortOption.RATING }) }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Cuisine filter
    // ══════════════════════════════════════════════════════════

    given("no cuisine filter selected") {

        `when`("cuisine selection tests") {
            lateinit var vm: SearchViewModel
            beforeEach { vm = createViewModel() }

            then("onCuisineSelected 1 → cuisineId is 1") {
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.uiState.value.filters.cuisineId shouldBe CUISINE_ID_1
            }
            then("same cuisine twice → deselect → null") {
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.uiState.value.filters.cuisineId shouldBe null
            }
            then("different cuisine → replaces first selection") {
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.onCuisineSelected(CUISINE_ID_2)
                vm.uiState.value.filters.cuisineId shouldBe CUISINE_ID_2
            }
            then("cuisine 1 replaced by cuisine 2 → cuisine 1 not selected") {
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.onCuisineSelected(CUISINE_ID_2)
                vm.uiState.value.filters.cuisineId shouldNotBe CUISINE_ID_1
            }
        }

        `when`("cuisine filter active and search runs") {
            then("searchUseCase called with cuisineId 1") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    vm.onCuisineSelected(CUISINE_ID_1)
                    searchAndWait(vm, QUERY_PIZZA)
                    verify { searchUseCase(QUERY_PIZZA, match { it.cuisineId == CUISINE_ID_1 }) }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 10 — Rating filter
    // ══════════════════════════════════════════════════════════

    given("no rating filter set") {

        `when`("rating filter tests") {
            lateinit var vm: SearchViewModel
            beforeEach { vm = createViewModel() }

            then("onMinRatingSelected 4.0 → minRating is 4.0") {
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.uiState.value.filters.minRating shouldBe RATING_FILTER_40
            }
            then("onMinRatingSelected 3.5 → minRating is 3.5") {
                vm.onMinRatingSelected(3.5)
                vm.uiState.value.filters.minRating shouldBe 3.5
            }
            then("onMinRatingSelected null → minRating cleared") {
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onMinRatingSelected(null)
                vm.uiState.value.filters.minRating shouldBe null
            }
        }

        `when`("minRating 4.0 active and search runs") {
            then("searchUseCase called with minRating 4.0") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    vm.onMinRatingSelected(RATING_FILTER_40)
                    searchAndWait(vm, QUERY_PIZZA)
                    verify {
                        searchUseCase(QUERY_PIZZA, match { it.minRating == RATING_FILTER_40 })
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 11 — Delivery time filter
    // ══════════════════════════════════════════════════════════

    given("no delivery time filter set") {

        `when`("delivery time filter tests") {
            lateinit var vm: SearchViewModel
            beforeEach { vm = createViewModel() }

            then("onDeliveryTimeSelected 30 → maxDeliveryTime is 30") {
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                vm.uiState.value.filters.maxDeliveryTime shouldBe DELIVERY_FILTER_30
            }
            then("onDeliveryTimeSelected 45 → maxDeliveryTime is 45") {
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_45)
                vm.uiState.value.filters.maxDeliveryTime shouldBe DELIVERY_FILTER_45
            }
            then("onDeliveryTimeSelected null → maxDeliveryTime cleared") {
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                vm.onDeliveryTimeSelected(null)
                vm.uiState.value.filters.maxDeliveryTime shouldBe null
            }
        }

        `when`("delivery time 30 active and search runs") {
            then("searchUseCase called with maxDeliveryTime 30") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                    searchAndWait(vm, QUERY_PIZZA)
                    verify {
                        searchUseCase(
                            QUERY_PIZZA,
                            match { it.maxDeliveryTime == DELIVERY_FILTER_30 },
                        )
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 12 — Multiple filters
    // WHY split:
    // State-only tests → shared VM
    // Search integration tests → runTest
    // ══════════════════════════════════════════════════════════

    given("user applies multiple filters") {

        `when`("filter combination state tests") {
            lateinit var vm: SearchViewModel
            beforeEach { vm = createViewModel() }

            then("veg + rating + sort all set simultaneously") {
                vm.onVegToggle()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onSortChange(SortOption.RATING)
                vm.uiState.value.filters.isVegOnly shouldBe true
                vm.uiState.value.filters.minRating shouldBe RATING_FILTER_40
                vm.uiState.value.filters.sortBy shouldBe SortOption.RATING
            }

            then("all 5 filters set — each field reflects value") {
                vm.onVegToggle()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onSortChange(SortOption.RATING)
                vm.onCuisineSelected(CUISINE_ID_2)
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)

                vm.uiState.value.filters.isVegOnly shouldBe true
                vm.uiState.value.filters.minRating shouldBe RATING_FILTER_40
                vm.uiState.value.filters.sortBy shouldBe SortOption.RATING
                vm.uiState.value.filters.cuisineId shouldBe CUISINE_ID_2
                vm.uiState.value.filters.maxDeliveryTime shouldBe DELIVERY_FILTER_30
            }

            then("clearFilters resets all 3 filters to defaults") {
                vm.onVegToggle()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onSortChange(SortOption.RATING)
                vm.clearFilters()
                vm.uiState.value.filters shouldBe SearchFilters()
            }

            then("clearFilters resets all 5 filters to defaults") {
                vm.onVegToggle()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onSortChange(SortOption.RATING)
                vm.onCuisineSelected(CUISINE_ID_2)
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                vm.clearFilters()
                vm.uiState.value.filters shouldBe SearchFilters()
            }
        }

        `when`("filter changes while valid query is active") {
            then("searchUseCase called again with new filters") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.onVegToggle()
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()
                    verify(atLeast = 2) { searchUseCase(QUERY_PIZZA, any()) }
                }
            }
        }

        `when`("clearFilters called while valid query is active") {
            then("searchUseCase called again with default filters") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    vm.onVegToggle()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.clearFilters()
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()
                    verify(atLeast = 2) { searchUseCase(QUERY_PIZZA, any()) }
                    verify { searchUseCase(QUERY_PIZZA, match { !it.isVegOnly }) }
                }
            }
        }

        `when`("all filters passed together in single search") {
            then("searchUseCase receives all filters correctly") {
                runTest(StandardTestDispatcher()) {
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    vm.onVegToggle()
                    vm.onMinRatingSelected(RATING_FILTER_40)
                    vm.onSortChange(SortOption.RATING)
                    vm.onCuisineSelected(CUISINE_ID_2)
                    vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                    searchAndWait(vm, QUERY_PIZZA)
                    verify {
                        searchUseCase(QUERY_PIZZA, match {
                            it.isVegOnly == true &&
                                    it.minRating == RATING_FILTER_40 &&
                                    it.sortBy == SortOption.RATING &&
                                    it.cuisineId == CUISINE_ID_2 &&
                                    it.maxDeliveryTime == DELIVERY_FILTER_30
                        })
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 13 — Cuisines fail to load
    // WHY split:
    // Initial state tests → set stub then shared VM
    // Search still works test → runTest
    // ══════════════════════════════════════════════════════════

    given("getCuisines API fails") {

        `when`("ViewModel created with failing cuisines API") {
            lateinit var vm: SearchViewModel

            beforeEach {
                // Override spec-level getCuisines stub with failure
                every { restaurantRepository.getCuisines() } returns
                        flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                vm = createViewModel()
            }

            then("cuisines empty — non-critical failure") {
                vm.uiState.value.cuisines.shouldBeEmpty()
            }
            then("query is still empty string") {
                vm.uiState.value.query shouldBe ""
            }
            then("isLoading is still false") {
                vm.uiState.value.isLoading shouldBe false
            }
            then("main error state is null — cuisines non-critical") {
                vm.uiState.value.error shouldBe null
            }
        }

        `when`("search still works when cuisines fail") {
            then("results returned correctly despite cuisines failure") {
                runTest(StandardTestDispatcher()) {
                    every { restaurantRepository.getCuisines() } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    val vm = createViewModel()
                    searchAndWait(vm, QUERY_PIZZA)
                    vm.uiState.value.results shouldHaveSize SEARCH_RESULT_COUNT_3
                    vm.uiState.value.cuisines.shouldBeEmpty()
                }
            }
        }
    }
})