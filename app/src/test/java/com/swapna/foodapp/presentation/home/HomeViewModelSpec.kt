package com.swapna.foodapp.presentation.home

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.presentation.common.fakes.FakeCartRepository
import com.swapna.foodapp.presentation.common.fakes.FakeUserRepository
import com.swapna.foodapp.presentation.common.CurrentLocationResult
import com.swapna.foodapp.presentation.common.LocationManager
import com.swapna.foodapp.utils.AppConstants.DEFAULT_LOCATION
import com.swapna.foodapp.utils.ConnectivityObserver
import com.swapna.foodapp.utils.HomeData
import com.swapna.foodapp.utils.NetworkStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
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

    val dispatcher = UnconfinedTestDispatcher()

    // ── MockK for Android-dependent classes ───────────────────────────
    val getHomeDataUseCase   = mockk<GetHomeDataUseCase>()
    val connectivityObserver = mockk<ConnectivityObserver>()
    val locationManager      = mockk<LocationManager>()

    // ── Fakes for pure Kotlin classes ─────────────────────────────────
    lateinit var fakeCartRepo: FakeCartRepository
    lateinit var fakeUserRepo: FakeUserRepository

    // ── Network flow — controlled per test ────────────────────────────
    val networkFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    // ── Test data ─────────────────────────────────────────────────────
    val fakeCollections = listOf(
        Collections(1, "Trending",     "Popular",       "", 20, "60% OFF"),
        Collections(2, "Newly Opened", "Just launched", "", 10, "Free Delivery"),
    )

    val fakeCategories = listOf(
        FoodCategory(1, "Biryani", ""),
        FoodCategory(2, "Pizza",   ""),
    )

    val restaurantsKoramangala = listOf(
        fakeRestaurant("r1", "Meghana Foods", locality = "Koramangala"),
        fakeRestaurant("r4", "Empire",         locality = "Koramangala"),
    )

    val restaurantsIndiranagar = listOf(
        fakeRestaurant("r2", "Pizza Hut", locality = "Indiranagar"),
    )

    val restaurantsAll = listOf(
        fakeRestaurant("r1", "Meghana Foods", locality = "Koramangala"),
        fakeRestaurant("r2", "Pizza Hut",     locality = "Indiranagar"),
        fakeRestaurant("r3", "Burger King",   locality = "HSR Layout"),
        fakeRestaurant("r4", "Empire",         locality = "Koramangala"),
    )

    fun emptyHomeData() = HomeData(
        restaurants    = emptyList(),
        collections    = emptyList(),
        categories     = emptyList(),
        filterStatus   = FilterStatus.NO_FILTER,
        requestedArea  = "",
        availableAreas = emptyList(),
    )

    fun homeData(
        restaurants:    List<Restaurant>   = restaurantsAll,
        collections:    List<Collections>  = fakeCollections,
        categories:     List<FoodCategory> = fakeCategories,
        filterStatus:   FilterStatus       = FilterStatus.NO_FILTER,
        requestedArea:  String             = "",
        availableAreas: List<String>       = emptyList(),
    ) = HomeData(
        restaurants    = restaurants,
        collections    = collections,
        categories     = categories,
        filterStatus   = filterStatus,
        requestedArea  = requestedArea,
        availableAreas = availableAreas,
    )

    fun createViewModel() = HomeViewModel(
        getHomeDataUseCase   = getHomeDataUseCase,
        cartRepository       = fakeCartRepo,
        userRepository       = fakeUserRepo,
        connectivityObserver = connectivityObserver,
        locationManager      = locationManager,
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(dispatcher)
        fakeCartRepo = FakeCartRepository()
        fakeUserRepo = FakeUserRepository()

        // Reset network to online
        networkFlow.value = NetworkStatus.Available

        // Default stubs
        every { connectivityObserver.networkStatus } returns networkFlow
        every { getHomeDataUseCase(any()) } returns
                flowOf(Result.success(homeData()))
        coEvery { locationManager.getCurrentLocation() } returns
                Result.success(
                    CurrentLocationResult(
                        displayAddress = "Koramangala, Bengaluru",
                        latitude       = 12.9352,
                        longitude      = 77.6245,
                    )
                )
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 1 — Initial State
    // ══════════════════════════════════════════════════════════════════

    given("HomeScreen opens for the first time") {

        `when`("ViewModel is created") {
            then("isLoading is false after data loads") {
                createViewModel().uiState.value.isLoading shouldBe false
            }

            then("error is null") {
                createViewModel().uiState.value.error shouldBe null
            }

            then("selectedLocation defaults to DEFAULT_LOCATION") {
                createViewModel().uiState.value.selectedLocation shouldBe
                        DEFAULT_LOCATION
            }

            then("selectedTab is DELIVERY") {
                createViewModel().uiState.value.selectedTab shouldBe
                        HomeViewModel.DeliveryTab.DELIVERY
            }

            then("cartItemCount is 0") {
                createViewModel().uiState.value.cartItemCount shouldBe 0
            }

            then("showLocationPicker is false") {
                createViewModel().uiState.value.showLocationPicker shouldBe false
            }

            then("savedAddresses is empty") {
                createViewModel().uiState.value.savedAddresses shouldBe emptyList()
            }

            then("restaurants list has 4 items") {
                createViewModel().uiState.value.restaurants.size shouldBe 4
            }

            then("collections has 2 items") {
                createViewModel().uiState.value.collections.size shouldBe 2
            }

            then("categories has 2 items") {
                createViewModel().uiState.value.categories.size shouldBe 2
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 2 — API Error + Retry
    // ══════════════════════════════════════════════════════════════════

    given("API throws error") {

        `when`("ViewModel is created") {
            then("error message is set in state") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.failure(Exception("No internet")))

                val vm = createViewModel()

                vm.uiState.value.error     shouldBe "No internet"
                vm.uiState.value.isLoading shouldBe false
            }
        }

        `when`("user taps retry") {
            then("restaurants reload on success") {
                every { getHomeDataUseCase(any()) } returnsMany listOf(
                    flowOf(Result.failure(Exception("error"))),
                    flowOf(Result.success(homeData())),
                )

                val vm = createViewModel()
                vm.uiState.value.error shouldNotBe null

                vm.retry()

                vm.uiState.value.restaurants.size shouldBe 4
                vm.uiState.value.error            shouldBe null
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 3 — Location Selection
    // ══════════════════════════════════════════════════════════════════

    given("user selects a delivery location") {

        `when`("Koramangala is selected") {
            then("selectedLocation updates to Koramangala") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.success(homeData(restaurants = restaurantsKoramangala)))

                val vm = createViewModel()
                vm.onLocationSelected("Koramangala")

                vm.uiState.value.selectedLocation shouldBe "Koramangala"
            }

            then("restaurants filter to Koramangala only") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.success(homeData(restaurants = restaurantsKoramangala)))

                val vm = createViewModel()
                vm.onLocationSelected("Koramangala")

                vm.uiState.value.restaurants.size shouldBe 2
                vm.uiState.value.restaurants
                    .all { it.locality == "Koramangala" } shouldBe true
            }
        }

        `when`("Indiranagar is selected") {
            then("only Indiranagar restaurants shown") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.success(homeData(restaurants = restaurantsIndiranagar)))

                val vm = createViewModel()
                vm.onLocationSelected("Indiranagar")

                vm.uiState.value.restaurants.size         shouldBe 1
                vm.uiState.value.restaurants.first().name shouldBe "Pizza Hut"
            }
        }

        `when`("any location is selected") {
            then("saveSelectedLocation called in UserRepository") {
                val vm = createViewModel()
                vm.onLocationSelected("Whitefield")

                fakeUserRepo.saveSelectedLocationCalled shouldBe true
                fakeUserRepo.lastSavedLocation          shouldBe "Whitefield"
            }

            then("showLocationPicker closes") {
                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onLocationSelected("Koramangala")

                vm.uiState.value.showLocationPicker shouldBe false
            }

            then("loadHomeData triggered again") {
                var callCount = 0
                every { getHomeDataUseCase(any()) } answers {
                    callCount++
                    flowOf(Result.success(homeData()))
                }

                val vm = createViewModel()
                val countBefore = callCount

                vm.onLocationSelected("HSR Layout")

                callCount shouldNotBe countBefore
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 4 — Saved Location Restored
    // ══════════════════════════════════════════════════════════════════

    given("user had previously saved Indiranagar") {

        `when`("app reopens and Room emits saved location") {
            then("selectedLocation restored to Indiranagar") {
                fakeUserRepo.setUserWithLocation("Indiranagar")
                val vm = createViewModel()

                vm.uiState.value.selectedLocation shouldBe "Indiranagar"
            }

            then("fresh selection overrides Room value") {
                fakeUserRepo.setUserWithLocation("Koramangala")
                val vm = createViewModel()

                vm.onLocationSelected("HSR Layout")

                vm.uiState.value.selectedLocation shouldBe "HSR Layout"
            }
        }

        `when`("saved location is empty") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUserWithLocation("")
                val vm = createViewModel()

                vm.uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }

        `when`("user is null") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUser(null)
                val vm = createViewModel()

                vm.uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 5 — Saved Addresses
    // ══════════════════════════════════════════════════════════════════

    given("user has saved addresses in profile") {

        `when`("ViewModel loads user profile") {
            then("savedAddresses contains both Home and Work") {
                fakeUserRepo.setUserWithAddresses(
                    FakeUserRepository.fakeHomeAddress(),
                    FakeUserRepository.fakeWorkAddress(),
                )
                val vm = createViewModel()

                vm.uiState.value.savedAddresses.size shouldBe 2
                vm.uiState.value.savedAddresses
                    .any { it.label == "Home" } shouldBe true
                vm.uiState.value.savedAddresses
                    .any { it.label == "Work" } shouldBe true
            }
        }

        `when`("user has no addresses") {
            then("savedAddresses is empty") {
                fakeUserRepo.setUser(FakeUserRepository.fakeUser())
                val vm = createViewModel()

                vm.uiState.value.savedAddresses shouldBe emptyList()
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 6 — Cart Count
    // ══════════════════════════════════════════════════════════════════

    given("cart has items") {

        `when`("cart has 3 items") {
            then("cartItemCount shows 3") {
                fakeCartRepo.setItemCount(3)
                val vm = createViewModel()

                vm.uiState.value.cartItemCount shouldBe 3
            }
        }

        `when`("cart is empty") {
            then("cartItemCount is 0") {
                createViewModel().uiState.value.cartItemCount shouldBe 0
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 7 — Connectivity
    // ══════════════════════════════════════════════════════════════════

    given("device loses internet") {

        `when`("network becomes Lost") {
            then("isOffline becomes true") {
                val vm = createViewModel()
                networkFlow.value = NetworkStatus.Lost

                vm.uiState.value.isOffline shouldBe true
            }
        }

        `when`("network becomes Unavailable") {
            then("isOffline is true") {
                networkFlow.value = NetworkStatus.Unavailable
                val vm = createViewModel()

                vm.uiState.value.isOffline shouldBe true
            }
        }
    }

    given("device reconnects") {

        `when`("network changes from Lost to Available") {
            then("isOffline becomes false") {
                val vm = createViewModel()
                networkFlow.value = NetworkStatus.Lost
                networkFlow.value = NetworkStatus.Available

                vm.uiState.value.isOffline shouldBe false
            }

            then("loadHomeData called again after reconnect") {
                var callCount = 0
                every { getHomeDataUseCase(any()) } answers {
                    callCount++
                    flowOf(Result.success(homeData()))
                }

                val vm = createViewModel()
                val countBefore = callCount

                networkFlow.value = NetworkStatus.Lost
                networkFlow.value = NetworkStatus.Available

                callCount shouldNotBe countBefore
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 8 — Filter Status
    // ══════════════════════════════════════════════════════════════════

    given("API returns filtered results") {

        `when`("filterStatus is FOUND") {
            then("uiState reflects FOUND with requestedArea") {
                every { getHomeDataUseCase(any()) } returns flowOf(
                    Result.success(
                        homeData(
                            restaurants   = restaurantsKoramangala,
                            filterStatus  = FilterStatus.FOUND,
                            requestedArea = "Koramangala",
                        )
                    )
                )
                val vm = createViewModel()

                vm.uiState.value.filterStatus  shouldBe FilterStatus.FOUND
                vm.uiState.value.requestedArea shouldBe "Koramangala"
            }
        }

        `when`("filterStatus is NOT_SERVICEABLE") {
            then("restaurants empty and availableAreas populated") {
                every { getHomeDataUseCase(any()) } returns flowOf(
                    Result.success(
                        homeData(
                            restaurants    = emptyList(),
                            filterStatus   = FilterStatus.NOT_SERVICEABLE,
                            requestedArea  = "Jakkur",
                            availableAreas = listOf(
                                "Koramangala", "Indiranagar", "HSR Layout"
                            ),
                        )
                    )
                )
                val vm = createViewModel()

                vm.uiState.value.filterStatus          shouldBe
                        FilterStatus.NOT_SERVICEABLE
                vm.uiState.value.restaurants.isEmpty() shouldBe true
                vm.uiState.value.availableAreas.size   shouldBe 3
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 9 — Navigation Events
    // ══════════════════════════════════════════════════════════════════

    given("user is on HomeScreen") {

        `when`("restaurant card tapped") {
            then("NavigateToRestaurant emitted with correct id") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onRestaurantClicked("r1")
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToRestaurant("r1")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("search bar tapped") {
            then("NavigateToSearch emitted with empty query") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onSearchClicked()
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToSearch("")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("category chip tapped") {
            then("NavigateToSearch emitted with category name") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onCategoryClicked("Biryani")
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToSearch("Biryani")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("cart icon tapped") {
            then("NavigateToCart event emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onCartClicked()
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToCart
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("PROFILE tab tapped") {
            then("NavigateToProfile event emitted") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onTabSelected(HomeViewModel.DeliveryTab.PROFILE)
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToProfile
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("DINING tab tapped") {
            then("selectedTab updates — no event emitted") {
                val vm = createViewModel()
                vm.onTabSelected(HomeViewModel.DeliveryTab.DINING)

                vm.uiState.value.selectedTab shouldBe
                        HomeViewModel.DeliveryTab.DINING
            }
        }

        `when`("profile icon tapped directly") {
            then("NavigateToProfile emitted via onProfileClicked") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onProfileClicked()
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToProfile
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("search tapped with query") {
            then("NavigateToSearch emitted with correct query") {
                val vm = createViewModel()
                vm.events.test {
                    vm.onSearchClicked("Burger")
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToSearch("Burger")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})

// ── Shared helper — used by both Spec files ───────────────────────────────
fun fakeRestaurant(
    id:       String = "r1",
    name:     String = "Test Restaurant",
    locality: String = "Koramangala",
) = Restaurant(
    id              = id,
    name            = name,
    imageUrl        = "https://picsum.photos/seed/$id/600/300",
    thumbUrl        = "https://picsum.photos/seed/$id/200/200",
    rating          = 4.5,
    ratingText      = "Excellent",
    ratingColor     = "#3F7E00",
    totalVotes      = 5000,
    avgDeliveryTime = 30,
    deliveryFee     = 30.0,
    avgCostForTwo   = 500,
    minOrder        = 100,
    cuisines        = listOf("Biryani", "South Indian"),
    address         = "$locality, Bengaluru",
    locality        = locality,
    distanceKm      = 0.0,
    hasDelivery     = true,
    isOpen          = true,
    offers          = listOf("50% off"),
)