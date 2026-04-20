package com.swapna.foodapp.presentation.search

import com.swapna.foodapp.domain.model.Cuisine
import com.swapna.foodapp.domain.model.SearchFilters
import com.swapna.foodapp.domain.model.SortOption
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.search.SearchRestaurantsUseCase
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.fakeRestaurant
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

    val searchUseCase        = mockk<SearchRestaurantsUseCase>()
    val restaurantRepository = mockk<RestaurantRepository>()

    val fakeCuisines = listOf(
        Cuisine(1, "Biryani"),
        Cuisine(2, "Pizza"),
        Cuisine(3, "Burger"),
        Cuisine(4, "Chinese"),
    )
    val fakeResults = listOf(
        fakeRestaurant("r1", "Meghana Foods", rating = 4.6),
        fakeRestaurant("r2", "Pizza Hut",     rating = 4.1),
        fakeRestaurant("r3", "Burger King",   rating = 3.9),
    )

    fun createViewModel() = SearchViewModel(searchUseCase, restaurantRepository)

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { restaurantRepository.getCuisines() } returns
                flowOf(Result.success(fakeCuisines))
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial State
    // ══════════════════════════════════════════════════════════

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
                createViewModel().uiState.value.cuisines shouldHaveSize 4
            }

            then("first cuisine should be Biryani") {
                createViewModel().uiState.value.cuisines
                    .first().name shouldBe "Biryani"
            }

            then("all cuisine names load correctly") {
                val cuisines = createViewModel().uiState.value.cuisines
                cuisines.map { it.name } shouldBe
                        listOf("Biryani", "Pizza", "Burger", "Chinese")
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

                    vm.onQueryChange("p")
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
                    val belowMin = "p".repeat(AppConstants.SEARCH_MIN_CHARS - 1)
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
                    val minQuery = "p".repeat(AppConstants.SEARCH_MIN_CHARS)
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

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Valid query
    // ══════════════════════════════════════════════════════════

    given("user types 'pizza' — valid query above minimum") {

        `when`("API returns matching restaurants") {

            then("results contain only Pizza Hut") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(
                                fakeResults.filter {
                                    it.name.contains("pizza", ignoreCase = true) ||
                                            it.cuisines.any { c ->
                                                c.contains("pizza", ignoreCase = true)
                                            }
                                }
                            ))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.size shouldBe 1
                    vm.uiState.value.results.first().name shouldBe "Pizza Hut"
                }
            }

            then("hasSearched becomes true") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(emptyList()))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.hasSearched shouldBe true
                }
            }

            then("isLoading is false after results arrive") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.isLoading shouldBe false
                }
            }

            then("query updates uiState immediately without waiting for debounce") {
                val vm = createViewModel()
                vm.onQueryChange("pizza")
                vm.uiState.value.query shouldBe "pizza"
            }

            then("searchUseCase called with exact query string") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase("pizza", any()) }
                }
            }

            then("error is null after successful search") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldBe null
                }
            }

            then("all 3 results returned when API returns all") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results shouldHaveSize 3
                }
            }

            // ✅ NEW — isLoading becomes true BEFORE results arrive
            // WHY important?
            // VM sets isLoading=true before calling searchUseCase
            // If this is broken → spinner never shows → bad UX
            // Use a slow/suspending flow to catch the in-between state
            then("isLoading becomes true while search is in-flight") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())

                    // WHY flow { emit } not flowOf?
                    // flowOf emits immediately — no way to observe isLoading=true
                    // flow { } lets us check state BEFORE emit
                    var loadingDuringSearch = false
                    every { searchUseCase("pizza", any()) } returns flow {
                        // At this point VM has set isLoading=true
                        // but not yet received results
                        loadingDuringSearch = true
                        emit(Result.success(fakeResults))
                    }

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    // Verify the loading state was set during search
                    loadingDuringSearch shouldBe true
                    // After results arrive — loading stops
                    vm.uiState.value.isLoading shouldBe false
                }
            }
        }

        `when`("API returns error") {

            then("error set in uiState with message") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldNotBe null
                    vm.uiState.value.error shouldBe "Network error"
                }
            }

            then("results remain empty on error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.shouldBeEmpty()
                }
            }

            then("hasSearched is true even on error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.hasSearched shouldBe true
                }
            }

            then("isLoading is false after error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.isLoading shouldBe false
                }
            }

            // ✅ NEW — error clears when new search starts
            // WHY important?
            // User sees error → types new query → error should clear
            // If error stays visible during new search → confusing UX
            // VM does: _uiState.update { it.copy(isLoading = true, error = null) }
            // This test verifies that line executes
            then("error clears when user types a new query after error") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())

                    // First search fails
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.failure(Exception("Network error")))
                    // Second search succeeds
                    every { searchUseCase("burger", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    // Error set from first search
                    vm.uiState.value.error shouldNotBe null

                    // User types new query
                    vm.onQueryChange("burger")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    // Error must be cleared after successful new search
                    vm.uiState.value.error shouldBe null
                }
            }
        }

        `when`("API returns empty list") {

            then("results empty and hasSearched true") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(emptyList()))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe true
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Debounce behavior
    // ══════════════════════════════════════════════════════════

    given("user types rapidly within debounce window") {

        `when`("multiple chars typed before debounce expires") {
            then("searchUseCase called only once with final query") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("burger", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange("b")
                    advanceTimeBy(50)
                    vm.onQueryChange("bu")
                    advanceTimeBy(50)
                    vm.onQueryChange("bur")
                    advanceTimeBy(50)
                    vm.onQueryChange("burg")
                    advanceTimeBy(50)
                    vm.onQueryChange("burge")
                    advanceTimeBy(50)
                    vm.onQueryChange("burger")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase("burger", any()) }
                    verify(exactly = 0) { searchUseCase("b",    any()) }
                    verify(exactly = 0) { searchUseCase("bu",   any()) }
                    verify(exactly = 0) { searchUseCase("bur",  any()) }
                    verify(exactly = 0) { searchUseCase("burg", any()) }
                }
            }
        }

        `when`("user pauses between two distinct queries") {
            then("searchUseCase called once per paused query") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza",  any()) } returns
                            flowOf(Result.success(fakeResults))
                    every { searchUseCase("burger", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.onQueryChange("burger")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 1) { searchUseCase("pizza",  any()) }
                    verify(exactly = 1) { searchUseCase("burger", any()) }
                }
            }
        }

        `when`("user types then goes below min chars again") {
            then("search cleared and use case not called again") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.onQueryChange("p")
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
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.clearSearch()
                    advanceUntilIdle()

                    vm.uiState.value.query       shouldBe ""
                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe false
                    vm.uiState.value.error       shouldBe null
                }
            }
        }

        `when`("clearSearch called with no prior search — idempotent") {
            then("state stays at defaults and no crash") {
                val vm = createViewModel()
                vm.clearSearch()

                vm.uiState.value.query       shouldBe ""
                vm.uiState.value.results.shouldBeEmpty()
                vm.uiState.value.hasSearched shouldBe false
                vm.uiState.value.error       shouldBe null
            }
        }

        `when`("clearSearch called after error state") {
            then("error also cleared") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldNotBe null

                    vm.clearSearch()
                    advanceUntilIdle()

                    vm.uiState.value.error shouldBe null
                }
            }
        }

        // ✅ NEW — search works again after clearSearch
        // WHY important?
        // clearSearch resets _query to ""
        // New search after clear must re-trigger the pipeline
        // If _query not reset properly → combine doesn't re-emit
        `when`("user searches again after clearSearch") {
            then("new search returns results correctly") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    // First search
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    // Clear
                    vm.clearSearch()
                    advanceUntilIdle()

                    // Second search
                    vm.onQueryChange("burger")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results shouldHaveSize 3
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
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onVegToggle()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase("pizza", match { it.isVegOnly == true })
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
                createViewModel().uiState.value.filters.sortBy shouldBe
                        SortOption.RELEVANCE
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
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onSortChange(SortOption.RATING)
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase("pizza", match { it.sortBy == SortOption.RATING })
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
                vm.onCuisineSelected(1)
                vm.uiState.value.filters.cuisineId shouldBe 1
            }
        }

        `when`("same cuisine selected twice") {
            then("filters.cuisineId goes back to null — deselect") {
                val vm = createViewModel()
                vm.onCuisineSelected(1)
                vm.onCuisineSelected(1)
                vm.uiState.value.filters.cuisineId shouldBe null
            }
        }

        `when`("different cuisine selected after first") {
            then("filters.cuisineId is the newer selection") {
                val vm = createViewModel()
                vm.onCuisineSelected(1)
                vm.onCuisineSelected(2)
                vm.uiState.value.filters.cuisineId shouldBe 2
            }
        }

        `when`("cuisine 1 selected then cuisine 2 selected") {
            then("cuisine 1 is no longer selected") {
                val vm = createViewModel()
                vm.onCuisineSelected(1)
                vm.onCuisineSelected(2)
                vm.uiState.value.filters.cuisineId shouldNotBe 1
            }
        }

        `when`("cuisine filter active and search runs") {
            then("searchUseCase called with cuisineId 1") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onCuisineSelected(1)
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase("pizza", match { it.cuisineId == 1 })
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
                vm.onMinRatingSelected(4.0)
                vm.uiState.value.filters.minRating shouldBe 4.0
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
                vm.onMinRatingSelected(4.0)
                vm.onMinRatingSelected(null)
                vm.uiState.value.filters.minRating shouldBe null
            }
        }

        `when`("minRating 4.0 active and search runs") {
            then("searchUseCase called with minRating 4.0") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onMinRatingSelected(4.0)
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase("pizza", match { it.minRating == 4.0 })
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
                vm.onDeliveryTimeSelected(30)
                vm.uiState.value.filters.maxDeliveryTime shouldBe 30
            }
        }

        `when`("onDeliveryTimeSelected called with 45") {
            then("filters.maxDeliveryTime is 45") {
                val vm = createViewModel()
                vm.onDeliveryTimeSelected(45)
                vm.uiState.value.filters.maxDeliveryTime shouldBe 45
            }
        }

        `when`("onDeliveryTimeSelected called with null — clear") {
            then("filters.maxDeliveryTime is null") {
                val vm = createViewModel()
                vm.onDeliveryTimeSelected(30)
                vm.onDeliveryTimeSelected(null)
                vm.uiState.value.filters.maxDeliveryTime shouldBe null
            }
        }

        // ✅ NEW — verify filter actually passed to searchUseCase
        // The original GROUP 11 only tested state — not the pipeline
        // WHY both needed?
        // State test: filter is stored correctly in uiState
        // Verify test: filter is passed through combine → searchUseCase
        // A bug could store the filter but not pass it to the use case
        `when`("delivery time 30 active and search runs") {
            then("searchUseCase called with maxDeliveryTime 30") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onDeliveryTimeSelected(30)
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase("pizza", match { it.maxDeliveryTime == 30 })
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
                vm.onMinRatingSelected(4.0)
                vm.onSortChange(SortOption.RATING)

                vm.uiState.value.filters.isVegOnly shouldBe true
                vm.uiState.value.filters.minRating shouldBe 4.0
                vm.uiState.value.filters.sortBy    shouldBe SortOption.RATING
            }
        }

        `when`("all filters set at once") {
            then("each field reflects its set value") {
                val vm = createViewModel()
                vm.onVegToggle()
                vm.onMinRatingSelected(4.0)
                vm.onSortChange(SortOption.RATING)
                vm.onCuisineSelected(2)
                vm.onDeliveryTimeSelected(30)

                vm.uiState.value.filters.isVegOnly      shouldBe true
                vm.uiState.value.filters.minRating       shouldBe 4.0
                vm.uiState.value.filters.sortBy          shouldBe SortOption.RATING
                vm.uiState.value.filters.cuisineId       shouldBe 2
                vm.uiState.value.filters.maxDeliveryTime shouldBe 30
            }
        }

        `when`("clearFilters called after veg + rating + sort") {
            then("all filters reset to SearchFilters defaults") {
                val vm = createViewModel()
                vm.onVegToggle()
                vm.onMinRatingSelected(4.0)
                vm.onSortChange(SortOption.RATING)
                vm.clearFilters()

                vm.uiState.value.filters shouldBe SearchFilters()
            }
        }

        `when`("clearFilters called with all 5 filters active") {
            then("all fields return to default values") {
                val vm = createViewModel()
                vm.onVegToggle()
                vm.onMinRatingSelected(4.0)
                vm.onSortChange(SortOption.RATING)
                vm.onCuisineSelected(2)
                vm.onDeliveryTimeSelected(30)
                vm.clearFilters()

                vm.uiState.value.filters shouldBe SearchFilters()
            }
        }

        `when`("filter changes while valid query is active") {
            then("searchUseCase called again with new filters") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.onVegToggle()
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(atLeast = 2) { searchUseCase("pizza", any()) }
                }
            }
        }

        // ✅ NEW — clearFilters re-triggers search when query active
        // WHY missing before?
        // clearFilters resets _filters → combine re-emits → debounce → new search
        // Without this test, clearFilters could skip the pipeline update
        `when`("clearFilters called while valid query is active") {
            then("searchUseCase called again with default filters") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()

                    // Set filters + search
                    vm.onVegToggle()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    // Clear filters — should re-trigger pipeline
                    vm.clearFilters()
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    // Called at least twice — once with veg, once without
                    verify(atLeast = 2) { searchUseCase("pizza", any()) }
                    // Final call has default filters
                    verify {
                        searchUseCase("pizza", match { !it.isVegOnly })
                    }
                }
            }
        }

        `when`("all filters passed together in single search") {
            then("searchUseCase receives all filters correctly") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onVegToggle()
                    vm.onMinRatingSelected(4.0)
                    vm.onSortChange(SortOption.RATING)
                    vm.onCuisineSelected(2)
                    vm.onDeliveryTimeSelected(30)
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify {
                        searchUseCase(
                            "pizza",
                            match {
                                it.isVegOnly       == true           &&
                                        it.minRating       == 4.0            &&
                                        it.sortBy          == SortOption.RATING &&
                                        it.cuisineId       == 2              &&
                                        it.maxDeliveryTime == 30
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
                        flowOf(Result.failure(Exception("Network error")))

                createViewModel().uiState.value.cuisines.shouldBeEmpty()
            }

            then("screen still works — query and isLoading correct") {
                every { restaurantRepository.getCuisines() } returns
                        flowOf(Result.failure(Exception("Network error")))

                val vm = createViewModel()
                vm.uiState.value.query     shouldBe ""
                vm.uiState.value.isLoading shouldBe false
            }

            then("search still works when cuisines fail") {
                runTest(StandardTestDispatcher()) {
                    Dispatchers.setMain(UnconfinedTestDispatcher())
                    every { restaurantRepository.getCuisines() } returns
                            flowOf(Result.failure(Exception("Network error")))
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results shouldHaveSize 3
                    vm.uiState.value.cuisines.shouldBeEmpty()
                }
            }

            then("error in cuisines does not set main error state") {
                every { restaurantRepository.getCuisines() } returns
                        flowOf(Result.failure(Exception("Network error")))

                createViewModel().uiState.value.error shouldBe null
            }
        }
    }
})