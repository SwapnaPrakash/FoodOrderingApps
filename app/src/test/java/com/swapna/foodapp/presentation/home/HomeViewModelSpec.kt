package com.swapna.foodapp.presentation.home

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.utils.ConnectivityObserver
import com.swapna.foodapp.utils.HomeData
import com.swapna.foodapp.utils.NetworkStatus
import com.swapna.foodapp.utils.fakeRestaurant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelSpec : BehaviorSpec({

    // ── Test doubles
    val getHomeDataUseCase = mockk<GetHomeDataUseCase>()
    val cartRepository = mockk<CartRepository>()
    val connectivityObserver = mockk<ConnectivityObserver>()

    // Shared fake data
    val fakeCollections = listOf(
        Collections(1, "Trending This Week", "Popular now", "", 20, "60% OFF"),
        Collections(2, "Newly Opened", "Just launched", "", 10, "Free Delivery"),
    )
    val fakeCategories = listOf(
        FoodCategory(1, "Biryani", ""),
        FoodCategory(2, "Pizza", ""),
        FoodCategory(3, "Burger", ""),
    )
    val fakeRestaurants = listOf(
        fakeRestaurant("r1", "Meghana Foods", rating = 4.6),
        fakeRestaurant("r2", "Empire Restaurant", rating = 4.4),
        fakeRestaurant("r3", "Pizza Hut", rating = 4.1),
    )
    val fakeHomeData = HomeData(
        collections = fakeCollections,
        categories = fakeCategories,
        restaurants = fakeRestaurants,
    )

    // ── Reusable network flow
    val networkFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    // ── Helper — creates ViewModel with all 3 deps
    fun createViewModel() = HomeViewModel(
        getHomeDataUseCase = getHomeDataUseCase,
        cartRepository = cartRepository,
        connectivityObserver = connectivityObserver,
    )

    // ── Setup / Teardown
    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        // ✅ Always mock ConnectivityObserver — HomeViewModel observes it in init
        every { connectivityObserver.networkStatus } returns networkFlow

        // ✅ Default: cart is empty
        every { cartRepository.getCartItemCount() } returns flowOf(0)
    }

    afterEach {
        networkFlow.value = NetworkStatus.Available
        Dispatchers.resetMain()
    }

    // GIVEN: API returns full home data
    given("API returns full home data") {

        `when`("ViewModel is created") {

            then("isLoading should become false") {
                // ✅ mock setup + ViewModel creation INSIDE then
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.uiState.value.isLoading shouldBe false
            }

            then("collections should have 2 items") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.uiState.value.collections.size shouldBe 2
            }

            then("categories should have 3 items") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.uiState.value.categories.size shouldBe 3
            }

            then("restaurants should have 3 items") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.uiState.value.restaurants.size shouldBe 3
            }

            then("error should be null") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.uiState.value.error shouldBe null
            }
        }
    }

    // GIVEN: API returns empty data
    given("API returns empty HomeData") {

        `when`("ViewModel is created") {

            then("all lists should be empty but no error") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                val vm = createViewModel()
                vm.uiState.value.collections shouldBe emptyList()
                vm.uiState.value.categories shouldBe emptyList()
                vm.uiState.value.restaurants shouldBe emptyList()
                vm.uiState.value.error shouldBe null
                vm.uiState.value.isLoading shouldBe false
            }
        }
    }

    // GIVEN: API throws exception — offline
    given("API throws IOException — device is offline") {
        `when`("ViewModel is created") {

            then("error should not be null") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.failure(Exception("No internet connection")))

                val vm = createViewModel()
                vm.uiState.value.error shouldNotBe null
            }

            then("error message should match exception") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.failure(Exception("No internet connection")))

                val vm = createViewModel()
                vm.uiState.value.error shouldBe "No internet connection"
            }

            then("isLoading should be false after error") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.failure(Exception("No internet connection")))

                val vm = createViewModel()
                vm.uiState.value.isLoading shouldBe false
            }

            then("restaurants list should remain empty") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.failure(Exception("No internet connection")))

                val vm = createViewModel()
                vm.uiState.value.restaurants shouldBe emptyList()
            }
        }
    }

    // GIVEN: Cart has items
    given("cart has 3 items") {

        `when`("ViewModel is created") {

            then("cartItemCount in uiState should be 3") {
                every { cartRepository.getCartItemCount() } returns flowOf(3)
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                val vm = createViewModel()
                vm.uiState.value.cartItemCount shouldBe 3
            }
        }
    }

    given("cart starts with 0 items then grows to 5") {

        `when`("cartCountFlow emits new value") {

            then("cartItemCount in uiState should update reactively") {
                val cartCountFlow = MutableStateFlow(0)
                every { cartRepository.getCartItemCount() } returns cartCountFlow
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                val vm = createViewModel()
                vm.uiState.value.cartItemCount shouldBe 0

                // Simulate adding items from another screen
                cartCountFlow.value = 5
                vm.uiState.value.cartItemCount shouldBe 5
            }
        }
    }

    // GIVEN: Tab selection
    given("default selected tab is DELIVERY") {

        `when`("ViewModel is created") {
            then("selectedTab should be DELIVERY") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                val vm = createViewModel()
                vm.uiState.value.selectedTab shouldBe
                        HomeViewModel.DeliveryTab.DELIVERY
            }
        }

        `when`("user taps DINING tab") {
            then("selectedTab should switch to DINING") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                val vm = createViewModel()
                vm.onTabSelected(HomeViewModel.DeliveryTab.DINING)
                vm.uiState.value.selectedTab shouldBe
                        HomeViewModel.DeliveryTab.DINING
            }
        }

        `when`("user taps DELIVERY again after DINING") {
            then("selectedTab should go back to DELIVERY") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                val vm = createViewModel()
                vm.onTabSelected(HomeViewModel.DeliveryTab.DINING)
                vm.onTabSelected(HomeViewModel.DeliveryTab.DELIVERY)
                vm.uiState.value.selectedTab shouldBe
                        HomeViewModel.DeliveryTab.DELIVERY
            }
        }
    }

    // GIVEN: Navigation events via SharedFlow
    given("home data is loaded") {

        `when`("onRestaurantClicked is called with 'r_001'") {
            then("NavigateToRestaurant event should be emitted") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.events.test {
                    vm.onRestaurantClicked("r_001")
                    val event = awaitItem()
                    event shouldBe
                            HomeViewModel.HomeEvent.NavigateToRestaurant("r_001")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("onCartClicked is called") {
            then("NavigateToCart event should be emitted") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.events.test {
                    vm.onCartClicked()
                    val event = awaitItem()
                    event shouldBe HomeViewModel.HomeEvent.NavigateToCart
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("onSearchClicked is called") {
            then("NavigateToSearch event should be emitted") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(fakeHomeData))

                val vm = createViewModel()
                vm.events.test {
                    vm.onSearchClicked()
                    val event = awaitItem()
                    event shouldBe HomeViewModel.HomeEvent.NavigateToSearch("")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }

    // GIVEN: Retry after error
    given("first load fails, then retry succeeds") {

        `when`("retry is called after error") {
            then("restaurants should load successfully") {
                every { getHomeDataUseCase() } returnsMany listOf(
                    flowOf(Result.failure(Exception("Network error"))),
                    flowOf(Result.success(fakeHomeData)),
                )

                val vm = createViewModel()
                // First load — error
                vm.uiState.value.error shouldNotBe null

                // Retry — success
                vm.retry()
                vm.uiState.value.restaurants.size shouldBe 3
                vm.uiState.value.error shouldBe null
            }
        }
    }

    // GIVEN: Connectivity changes
    given("device is online") {
        `when`("ViewModel is created") {
            then("isOffline should be false") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                networkFlow.value = NetworkStatus.Available
                val vm = createViewModel()

                vm.uiState.value.isOffline shouldBe false
            }
        }
    }

    given("device loses internet connection") {
        `when`("network status changes to Lost") {
            then("isOffline should become true") {
                every { getHomeDataUseCase() } returns
                        flowOf(Result.success(HomeData()))

                networkFlow.value = NetworkStatus.Lost
                val vm = createViewModel()

                vm.uiState.value.isOffline shouldBe true
            }
        }
    }
})