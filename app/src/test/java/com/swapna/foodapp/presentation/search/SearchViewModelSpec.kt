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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelSpec : BehaviorSpec({

    val searchUseCase = mockk<SearchRestaurantsUseCase>()
    val restaurantRepository = mockk<RestaurantRepository>()

    val testDispatcher = StandardTestDispatcher()

    val fakeCuisines = listOf(
        Cuisine(1, "Biryani"),
        Cuisine(2, "Pizza"),
        Cuisine(3, "Burger"),
        Cuisine(4, "Chinese"),
    )
    val fakeResults = listOf(
        fakeRestaurant("r1", "Meghana Foods", rating = 4.6),
        fakeRestaurant("r2", "Pizza Hut", rating = 4.1),
        fakeRestaurant("r3", "Burger King", rating = 3.9),
    )

    fun createViewModel() = SearchViewModel(searchUseCase, restaurantRepository)

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(testDispatcher)
        every { restaurantRepository.getCuisines() } returns
                flowOf(Result.success(fakeCuisines))
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // GIVEN: Initial state
    given("ViewModel is just created") {

        `when`("no query has been entered") {

            then("query should be empty string") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()  // let init coroutines finish
                    vm.uiState.value.query shouldBe ""
                }
            }

            then("results should be empty") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()
                    vm.uiState.value.results.shouldBeEmpty()
                }
            }

            then("isLoading should be false") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()
                    vm.uiState.value.isLoading shouldBe false
                }
            }

            then("hasSearched should be false") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()
                    vm.uiState.value.hasSearched shouldBe false
                }
            }

            then("searchUseCase should NOT be called") {
                runTest {
                    createViewModel()
                    advanceUntilIdle()
                    verify(exactly = 0) { searchUseCase(any(), any()) }
                }
            }
        }

        `when`("cuisines are loaded") {
            then("cuisines list should have 4 items") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()
                    vm.uiState.value.cuisines shouldHaveSize 4
                }
            }

            then("first cuisine should be Biryani") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()
                    vm.uiState.value.cuisines.first().name shouldBe "Biryani"
                }
            }
        }
    }

    // GIVEN: Query too short
    given("user types only 1 character") {

        `when`("onQueryChange called with 'p'") {
            then("no search triggered — below minimum chars") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("p")
                    // Advance past debounce time
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    verify(exactly = 0) { searchUseCase(any(), any()) }
                    vm.uiState.value.results.shouldBeEmpty()
                }
            }
        }
    }

    // GIVEN: Valid query
    given("user types 'pizza' (5 chars — above minimum)") {

        `when`("API returns matching restaurants") {

            then("results should contain matching restaurants") {
                runTest {
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(
                                Result.success(
                                    fakeResults.filter {
                                        it.name.contains("pizza", ignoreCase = true) ||
                                                it.cuisines.any { c ->
                                                    c.contains(
                                                        "pizza",
                                                        ignoreCase = true
                                                    )
                                                }
                                    }
                                ))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("pizza")
                    // ✅ Advance past debounce so search actually fires
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.size shouldBe 1
                    vm.uiState.value.results.first().name shouldBe "Pizza Hut"
                }
            }

            then("hasSearched should become true") {
                runTest {
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(emptyList()))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.hasSearched shouldBe true
                }
            }

            then("isLoading should be false after results arrive") {
                runTest {
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.isLoading shouldBe false
                }
            }
        }

        `when`("API returns error") {

            then("error should be set in uiState") {
                runTest {
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.error shouldNotBe null
                    vm.uiState.value.error shouldBe "Network error"
                }
            }

            then("results should remain empty on error") {
                runTest {
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.shouldBeEmpty()
                }
            }
        }

        `when`("API returns empty list") {

            then("results should be empty and hasSearched true") {
                runTest {
                    every { searchUseCase("pizza", any()) } returns
                            flowOf(Result.success(emptyList()))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("pizza")
                    advanceTimeBy(AppConstants.SEARCH_DEBOUNCE_MS + 100)
                    advanceUntilIdle()

                    vm.uiState.value.results.shouldBeEmpty()
                    vm.uiState.value.hasSearched shouldBe true
                }
            }
        }
    }

    // GIVEN: Query cleared
    given("user had searched and then clears query") {

        `when`("clearSearch is called") {
            then("query, results, hasSearched all reset") {
                runTest {
                    every { searchUseCase(any(), any()) } returns
                            flowOf(Result.success(fakeResults))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onQueryChange("pizza")
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
    }

    // GIVEN: Veg filter toggle — no debounce needed (filter only)
    given("isVegOnly filter is off by default") {

        `when`("onVegToggle is called once") {
            then("isVegOnly should become true") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.uiState.value.filters.isVegOnly shouldBe false
                    vm.onVegToggle()
                    advanceUntilIdle()

                    vm.uiState.value.filters.isVegOnly shouldBe true
                }
            }
        }

        `when`("onVegToggle is called twice") {
            then("isVegOnly should go back to false") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onVegToggle()
                    vm.onVegToggle()
                    advanceUntilIdle()

                    vm.uiState.value.filters.isVegOnly shouldBe false
                }
            }
        }
    }

    // GIVEN: Sort option changes
    given("default sort is RELEVANCE") {

        `when`("ViewModel is created") {
            then("sortBy should be RELEVANCE") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.uiState.value.filters.sortBy shouldBe SortOption.RELEVANCE
                }
            }
        }

        `when`("onSortChange called with RATING") {
            then("filters.sortBy should be RATING") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onSortChange(SortOption.RATING)
                    advanceUntilIdle()

                    vm.uiState.value.filters.sortBy shouldBe SortOption.RATING
                }
            }
        }

        `when`("onSortChange called with DELIVERY_TIME") {
            then("filters.sortBy should be DELIVERY_TIME") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onSortChange(SortOption.DELIVERY_TIME)
                    advanceUntilIdle()

                    vm.uiState.value.filters.sortBy shouldBe SortOption.DELIVERY_TIME
                }
            }
        }
    }

    // GIVEN: Cuisine filter toggle
    given("no cuisine filter selected") {

        `when`("onCuisineSelected called with cuisineId 1") {
            then("filters.cuisineId should be 1") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onCuisineSelected(1)
                    advanceUntilIdle()

                    vm.uiState.value.filters.cuisineId shouldBe 1
                }
            }
        }

        `when`("same cuisine selected twice — deselect") {
            then("filters.cuisineId should go back to null") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onCuisineSelected(1)
                    vm.onCuisineSelected(1)
                    advanceUntilIdle()

                    vm.uiState.value.filters.cuisineId shouldBe null
                }
            }
        }

        `when`("different cuisine selected after first") {
            then("filters.cuisineId should be the new one") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onCuisineSelected(1)
                    vm.onCuisineSelected(2)
                    advanceUntilIdle()

                    vm.uiState.value.filters.cuisineId shouldBe 2
                }
            }
        }
    }

    // GIVEN: Rating filter
    given("no rating filter set") {

        `when`("onMinRatingSelected called with 4.0") {
            then("filters.minRating should be 4.0") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onMinRatingSelected(4.0)
                    advanceUntilIdle()

                    vm.uiState.value.filters.minRating shouldBe 4.0
                }
            }
        }

        `when`("onMinRatingSelected called with null — clear") {
            then("filters.minRating should be null") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onMinRatingSelected(4.0)
                    vm.onMinRatingSelected(null)
                    advanceUntilIdle()

                    vm.uiState.value.filters.minRating shouldBe null
                }
            }
        }
    }

    // GIVEN: Multiple filters applied
    given("user applies multiple filters") {

        `when`("veg + rating + sort all set") {
            then("all three filters should be active simultaneously") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onVegToggle()
                    vm.onMinRatingSelected(4.0)
                    vm.onSortChange(SortOption.RATING)
                    advanceUntilIdle()

                    vm.uiState.value.filters.isVegOnly shouldBe true
                    vm.uiState.value.filters.minRating shouldBe 4.0
                    vm.uiState.value.filters.sortBy shouldBe SortOption.RATING
                }
            }
        }

        `when`("clearFilters is called") {
            then("all filters should reset to defaults") {
                runTest {
                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.onVegToggle()
                    vm.onMinRatingSelected(4.0)
                    vm.onSortChange(SortOption.RATING)
                    vm.clearFilters()
                    advanceUntilIdle()

                    vm.uiState.value.filters shouldBe SearchFilters()
                }
            }
        }
    }

    // GIVEN: Cuisines fail to load
    given("getCuisines API throws exception") {

        `when`("ViewModel is created") {
            then("cuisines should be empty — non-critical failure") {
                runTest {
                    every { restaurantRepository.getCuisines() } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.uiState.value.cuisines.shouldBeEmpty()
                }
            }

            then("main screen should still work — no crash") {
                runTest {
                    every { restaurantRepository.getCuisines() } returns
                            flowOf(Result.failure(Exception("Network error")))

                    val vm = createViewModel()
                    advanceUntilIdle()

                    vm.uiState.value.query shouldBe ""
                    vm.uiState.value.isLoading shouldBe false
                }
            }
        }
    }
})