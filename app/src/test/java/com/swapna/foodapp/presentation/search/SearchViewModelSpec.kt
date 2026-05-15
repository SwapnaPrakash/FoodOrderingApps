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

    // GROUP 1 — Initial State
    given("ViewModel is just created") {

        `when`("no query entered") {

            then("query should be empty string") {
                createViewModel().uiState.value.query shouldBe ""
            }

            then("results should be empty") {
                createViewModel().uiState.value.results.shouldBeEmpty()
            }

            then("isLoading should be false") {
                createViewModel().uiState.value.isLoading shouldBe false
            }

            then("hasSearched should be false") {
                createViewModel().uiState.value.hasSearched shouldBe false
            }

            then("error should be null") {
                createViewModel().uiState.value.error shouldBe null
            }

            then("filters should be default SearchFilters") {
                createViewModel().uiState.value.filters shouldBe SearchFilters()
            }

            then("searchUseCase should NOT be called") {
                createViewModel()
                verify(exactly = 0) { searchUseCase(any(), any()) }
            }
        }

        `when`("cuisines are loaded") {

            then("cuisines list should have 4 items") {
                createViewModel().uiState.value.cuisines shouldHaveSize SEARCH_RESULT_COUNT_4
            }

            then("first cuisine should be Biryani") {
                createViewModel().uiState.value.cuisines
                    .first().name shouldBe CUISINE_BIRYANI
            }

            then("all cuisine names load correctly") {
                val cuisines = createViewModel().uiState.value.cuisines
                cuisines.map { it.name } shouldBe
                        listOf(CUISINE_BIRYANI, CUISINE_PIZZA, CUISINE_BURGER, CUISINE_CHINESE)
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Query too short
    // ══════════════════════════════════════════════════════════

    given("user types below minimum characters") {

        `when`("onQueryChange called with single char 'p'") {
            then("no search triggered — below minimum chars") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    val vm = createViewModel()

                    vm.onQueryChange(QUERY_P)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 0) { searchUseCase(any(), any()) }
                    vm.uiState.value.results.shouldBeEmpty()
                }
            }
        }

        `when`("onQueryChange called with empty string") {
            then("no search triggered and results stay empty") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    val vm = createViewModel()

                    vm.onQueryChange("")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 0) { searchUseCase(any(), any()) }
                    vm.uiState.value.results.shouldBeEmpty()
                }
            }
        }

        `when`("query length is exactly SEARCH_MIN_CHARS minus 1") {
            then("search is NOT triggered") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    val belowMin = QUERY_P.repeat(AppConstants.SEARCH_MIN_CHARS - 1)
                    val vm = createViewModel()

                    vm.onQueryChange(belowMin)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

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
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    val minQuery = QUERY_P.repeat(AppConstants.SEARCH_MIN_CHARS)
                    every { searchUseCase(minQuery, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange(minQuery)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase(minQuery, any()) }
                }
            }
        }
    }

    // GROUP 4 — Valid query
    given("user types 'pizza' — valid query above minimum") {

        `when`("API returns matching restaurants") {

            then("results contain only Pizza Hut") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(
                                Result.success(
                                fakeResults.filter {
                                    it.name.contains(QUERY_PIZZA, ignoreCase = true) ||
                                            it.cuisines.any { c ->
                                                c.contains(QUERY_PIZZA, ignoreCase = true)
                                            }
                                }
                            ))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.size shouldBe SEARCH_RESULT_COUNT_1
                    vm.uiState.value.results.first().name shouldBe SEARCH_VM_PIZZA_HUT
                }
            }

            then("hasSearched becomes true") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(emptyList()))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.hasSearched shouldBe true
                }
            }

            then("isLoading is false after results arrive") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.isLoading shouldBe false
                }
            }

            then("query updates uiState immediately without waiting for debounce") {
                val vm = createViewModel()
                vm.onQueryChange(QUERY_PIZZA)
                vm.uiState.value.query shouldBe QUERY_PIZZA
            }

            then("searchUseCase called with exact query string") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase(QUERY_PIZZA, any()) }
                }
            }

            then("error is null after successful search") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldBe null
                }
            }

            then("all 3 results returned when API returns all") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results shouldHaveSize SEARCH_RESULT_COUNT_3
                }
            }

            then("isLoading becomes true while search is in-flight") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())

                    var loadingDuringSearch = false
                    every { searchUseCase(QUERY_PIZZA, any()) } returns flow {
                        loadingDuringSearch = true
                        emit(Result.success(fakeResults))
                    }

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    loadingDuringSearch shouldBe true
                    vm.uiState.value.isLoading shouldBe false
                }
            }
        }

        `when`("API returns error") {

            then("error set in uiState with message") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldNotBe null
                    vm.uiState.value.error shouldBe ERR_NETWORK_MSG
                }
            }

            then("results remain empty on error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.shouldBeEmpty()
                }
            }

            then("hasSearched is true even on error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.hasSearched shouldBe true
                }
            }

            then("isLoading is false after error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.isLoading shouldBe false
                }
            }

            then("error clears when user types a new query after error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())

                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    every { searchUseCase(QUERY_BURGER_FULL, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldNotBe null

                    vm.onQueryChange(QUERY_BURGER_FULL)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldBe null
                }
            }
        }

        `when`("API returns empty list") {

            then("results empty and hasSearched true") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(emptyList()))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe true
                }
            }
        }
    }

    // GROUP 5 — Debounce behavior
    given("user types rapidly within debounce window") {

        `when`("multiple chars typed before debounce expires") {
            then("searchUseCase called only once with final query") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
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
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))
                    every { searchUseCase(QUERY_BURGER_FULL, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.onQueryChange(QUERY_BURGER_FULL)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase(QUERY_PIZZA, any()) }
                    verify(exactly = 1) { searchUseCase(QUERY_BURGER_FULL, any()) }
                }
            }
        }

        `when`("user types then goes below min chars again") {
            then("search cleared and use case not called again") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.onQueryChange(QUERY_P)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase(any(), any()) }
                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe false
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Clear Search
    // ══════════════════════════════════════════════════════════

    given("user had searched and clears query") {

        `when`("clearSearch called after results loaded") {
            then("query results hasSearched error all reset") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.clearSearch()
                    advanceUntilIdle()

                    vm.uiState.value.query shouldBe ""
                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe false
                    vm.uiState.value.error shouldBe null
                }
            }
        }

        `when`("clearSearch called with no prior search — idempotent") {
            then("state stays at defaults and no crash") {
                val vm = createViewModel()
                vm.clearSearch()

                vm.uiState.value.query shouldBe ""
                vm.uiState.value.results.shouldBeEmpty()
                vm.uiState.value.hasSearched shouldBe false
                vm.uiState.value.error shouldBe null
            }
        }

        `when`("clearSearch called after error state") {
            then("error also cleared") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

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
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.clearSearch()
                    advanceUntilIdle()

                    vm.onQueryChange(QUERY_BURGER_FULL)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results shouldHaveSize SEARCH_RESULT_COUNT_3
                    vm.uiState.value.hasSearched shouldBe true
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Veg filter
    // ══════════════════════════════════════════════════════════

    given("isVegOnly filter is off by default") {

        `when`("onVegToggle called once") {
            then("isVegOnly becomes true") {
                val vm = createViewModel()
                vm.uiState.value.filters.isVegOnly shouldBe false
                vm.onVegToggle()
                vm.uiState.value.filters.isVegOnly shouldBe true
            }
        }

        `when`("onVegToggle called twice") {
            then("isVegOnly returns to false") {
                val vm = createViewModel()
                vm.onVegToggle()
                vm.onVegToggle()
                vm.uiState.value.filters.isVegOnly shouldBe false
            }
        }

        `when`("onVegToggle called three times") {
            then("isVegOnly is true — odd toggles = on") {
                val vm = createViewModel()
                vm.onVegToggle()
                vm.onVegToggle()
                vm.onVegToggle()
                vm.uiState.value.filters.isVegOnly shouldBe true
            }
        }

        `when`("veg filter active and search runs") {
            then("searchUseCase called with isVegOnly true") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onVegToggle()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase(QUERY_PIZZA, match { it.isVegOnly == true })
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Sort filter
    // ══════════════════════════════════════════════════════════

    given("default sort is RELEVANCE") {

        `when`("ViewModel is created") {
            then("sortBy defaults to RELEVANCE") {
                createViewModel().uiState.value.filters.sortBy shouldBe SortOption.RELEVANCE
            }
        }

        `when`("onSortChange called with RATING") {
            then("filters.sortBy is RATING") {
                val vm = createViewModel()
                vm.onSortChange(SortOption.RATING)
                vm.uiState.value.filters.sortBy shouldBe SortOption.RATING
            }
        }

        `when`("onSortChange called with DELIVERY_TIME") {
            then("filters.sortBy is DELIVERY_TIME") {
                val vm = createViewModel()
                vm.onSortChange(SortOption.DELIVERY_TIME)
                vm.uiState.value.filters.sortBy shouldBe SortOption.DELIVERY_TIME
            }
        }

        `when`("onSortChange called with COST_LOW") {
            then("filters.sortBy is COST_LOW") {
                val vm = createViewModel()
                vm.onSortChange(SortOption.COST_LOW)
                vm.uiState.value.filters.sortBy shouldBe SortOption.COST_LOW
            }
        }

        `when`("onSortChange called with COST_HIGH") {
            then("filters.sortBy is COST_HIGH") {
                val vm = createViewModel()
                vm.onSortChange(SortOption.COST_HIGH)
                vm.uiState.value.filters.sortBy shouldBe SortOption.COST_HIGH
            }
        }

        `when`("RATING sort active and search runs") {
            then("searchUseCase called with sortBy RATING") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onSortChange(SortOption.RATING)
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase(QUERY_PIZZA, match { it.sortBy == SortOption.RATING })
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Cuisine filter
    // ══════════════════════════════════════════════════════════

    given("no cuisine filter selected") {

        `when`("onCuisineSelected called with cuisineId 1") {
            then("filters.cuisineId is 1") {
                val vm = createViewModel()
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.uiState.value.filters.cuisineId shouldBe CUISINE_ID_1
            }
        }

        `when`("same cuisine selected twice") {
            then("filters.cuisineId goes back to null — deselect") {
                val vm = createViewModel()
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.uiState.value.filters.cuisineId shouldBe null
            }
        }

        `when`("different cuisine selected after first") {
            then("filters.cuisineId is the newer selection") {
                val vm = createViewModel()
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.onCuisineSelected(CUISINE_ID_2)
                vm.uiState.value.filters.cuisineId shouldBe CUISINE_ID_2
            }
        }

        `when`("cuisine 1 selected then cuisine 2 selected") {
            then("cuisine 1 is no longer selected") {
                val vm = createViewModel()
                vm.onCuisineSelected(CUISINE_ID_1)
                vm.onCuisineSelected(CUISINE_ID_2)
                vm.uiState.value.filters.cuisineId shouldNotBe CUISINE_ID_1
            }
        }

        `when`("cuisine filter active and search runs") {
            then("searchUseCase called with cuisineId 1") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onCuisineSelected(CUISINE_ID_1)
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase(QUERY_PIZZA, match { it.cuisineId == CUISINE_ID_1 })
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 10 — Rating filter
    // ══════════════════════════════════════════════════════════

    given("no rating filter set") {

        `when`("onMinRatingSelected called with 4.0") {
            then("filters.minRating is 4.0") {
                val vm = createViewModel()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.uiState.value.filters.minRating shouldBe RATING_FILTER_40
            }
        }

        `when`("onMinRatingSelected called with 3.5") {
            then("filters.minRating is 3.5") {
                val vm = createViewModel()
                vm.onMinRatingSelected(3.5)
                vm.uiState.value.filters.minRating shouldBe 3.5
            }
        }

        `when`("onMinRatingSelected called with null after 4.0") {
            then("filters.minRating is null — cleared") {
                val vm = createViewModel()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onMinRatingSelected(null)
                vm.uiState.value.filters.minRating shouldBe null
            }
        }

        `when`("minRating 4.0 active and search runs") {
            then("searchUseCase called with minRating 4.0") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onMinRatingSelected(RATING_FILTER_40)
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

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

        `when`("onDeliveryTimeSelected called with 30") {
            then("filters.maxDeliveryTime is 30") {
                val vm = createViewModel()
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                vm.uiState.value.filters.maxDeliveryTime shouldBe DELIVERY_FILTER_30
            }
        }

        `when`("onDeliveryTimeSelected called with 45") {
            then("filters.maxDeliveryTime is 45") {
                val vm = createViewModel()
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_45)
                vm.uiState.value.filters.maxDeliveryTime shouldBe DELIVERY_FILTER_45
            }
        }

        `when`("onDeliveryTimeSelected called with null — clear") {
            then("filters.maxDeliveryTime is null") {
                val vm = createViewModel()
                vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                vm.onDeliveryTimeSelected(null)
                vm.uiState.value.filters.maxDeliveryTime shouldBe null
            }
        }

        `when`("delivery time 30 active and search runs") {
            then("searchUseCase called with maxDeliveryTime 30") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase(
                            QUERY_PIZZA,
                            match { it.maxDeliveryTime == DELIVERY_FILTER_30 })
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 12 — Multiple filters
    // ══════════════════════════════════════════════════════════

    given("user applies multiple filters") {

        `when`("veg + rating + sort all set") {
            then("all three filters active simultaneously") {
                val vm = createViewModel()
                vm.onVegToggle()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onSortChange(SortOption.RATING)

                vm.uiState.value.filters.isVegOnly shouldBe true
                vm.uiState.value.filters.minRating shouldBe RATING_FILTER_40
                vm.uiState.value.filters.sortBy shouldBe SortOption.RATING
            }
        }

        `when`("all filters set at once") {
            then("each field reflects its set value") {
                val vm = createViewModel()
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
        }

        `when`("clearFilters called after veg + rating + sort") {
            then("all filters reset to SearchFilters defaults") {
                val vm = createViewModel()
                vm.onVegToggle()
                vm.onMinRatingSelected(RATING_FILTER_40)
                vm.onSortChange(SortOption.RATING)
                vm.clearFilters()

                vm.uiState.value.filters shouldBe SearchFilters()
            }
        }

        `when`("clearFilters called with all 5 filters active") {
            then("all fields return to default values") {
                val vm = createViewModel()
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
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

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
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onVegToggle()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.clearFilters()
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(atLeast = 2) { searchUseCase(QUERY_PIZZA, any()) }
                    verify {
                        searchUseCase(QUERY_PIZZA, match { !it.isVegOnly })
                    }
                }
            }
        }

        `when`("all filters passed together in single search") {
            then("searchUseCase receives all filters correctly") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onVegToggle()
                    vm.onMinRatingSelected(RATING_FILTER_40)
                    vm.onSortChange(SortOption.RATING)
                    vm.onCuisineSelected(CUISINE_ID_2)
                    vm.onDeliveryTimeSelected(DELIVERY_FILTER_30)
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase(
                            QUERY_PIZZA,
                            match {
                                it.isVegOnly == true &&
                                        it.minRating == RATING_FILTER_40 &&
                                        it.sortBy == SortOption.RATING &&
                                        it.cuisineId == CUISINE_ID_2 &&
                                        it.maxDeliveryTime == DELIVERY_FILTER_30
                            },
                        )
                    }
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 13 — Cuisines fail to load
    // ══════════════════════════════════════════════════════════

    given("getCuisines API fails") {

        `when`("ViewModel is created with failing cuisines API") {

            then("cuisines empty — non-critical failure") {
                every { restaurantRepository.getCuisines() } returns
                        flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                createViewModel().uiState.value.cuisines.shouldBeEmpty()
            }

            then("screen still works — query and isLoading correct") {
                every { restaurantRepository.getCuisines() } returns
                        flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                val vm = createViewModel()
                vm.uiState.value.query shouldBe ""
                vm.uiState.value.isLoading shouldBe false
            }

            then("search still works when cuisines fail") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { restaurantRepository.getCuisines() } returns
                            flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))
                    every { searchUseCase(QUERY_PIZZA, any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange(QUERY_PIZZA)
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results shouldHaveSize SEARCH_RESULT_COUNT_3
                    vm.uiState.value.cuisines.shouldBeEmpty()
                }
            }

            then("error in cuisines does not set main error state") {
                every { restaurantRepository.getCuisines() } returns
                        flowOf(Result.failure(Exception(ERR_NETWORK_MSG)))

                createViewModel().uiState.value.error shouldBe null
            }
        }
    }
})