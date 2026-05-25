package com.swapna.foodapp.presentation.home

import app.cash.turbine.test
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.presentation.common.ConnectivityObserver
import com.swapna.foodapp.presentation.common.CurrentLocationResult
import com.swapna.foodapp.presentation.common.LocationManager
import com.swapna.foodapp.presentation.common.NetworkStatus
import com.swapna.foodapp.presentation.common.fakes.FakeCartRepository
import com.swapna.foodapp.presentation.common.fakes.FakeUserRepository
import com.swapna.foodapp.presentation.common.fakes.homeData
import com.swapna.foodapp.presentation.common.fakes.restaurantsIndiranagar
import com.swapna.foodapp.presentation.common.fakes.restaurantsKoramangala
import com.swapna.foodapp.utils.AppConstants.DEFAULT_LOCATION
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_WORK
import com.swapna.foodapp.utils.TestConstants.ERR_ERROR_STR
import com.swapna.foodapp.utils.TestConstants.ERR_NO_INTERNET_HOME
import com.swapna.foodapp.utils.TestConstants.GPS_LAT
import com.swapna.foodapp.utils.TestConstants.GPS_LNG
import com.swapna.foodapp.utils.TestConstants.HOME_ADDR_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_AVAILABLE_AREAS_3
import com.swapna.foodapp.utils.TestConstants.HOME_CART_COUNT_3
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.HOME_CAT_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_SIZE_2
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_HSR
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_INDIRANAGAR
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_WHITEFIELD
import com.swapna.foodapp.utils.TestConstants.HOME_REST_COUNT_1
import com.swapna.foodapp.utils.TestConstants.HOME_REST_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_REST_COUNT_4
import com.swapna.foodapp.utils.TestConstants.HOME_REST_PIZZA_HUT
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R1
import com.swapna.foodapp.utils.TestConstants.HOME_SEARCH_QUERY_BURGER
import com.swapna.foodapp.utils.TestConstants.LOC_JAKKUR
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

    val getHomeDataUseCase = mockk<GetHomeDataUseCase>()
    val connectivityObserver = mockk<ConnectivityObserver>()
    val locationManager = mockk<LocationManager>()

    lateinit var fakeCartRepo: FakeCartRepository
    lateinit var fakeUserRepo: FakeUserRepository

    val networkFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    fun createViewModel() = HomeViewModel(
        getHomeDataUseCase = getHomeDataUseCase,
        cartRepository = fakeCartRepo,
        userRepository = fakeUserRepo,
        connectivityObserver = connectivityObserver,
        locationManager = locationManager,
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(dispatcher)
        fakeCartRepo = FakeCartRepository()
        fakeUserRepo = FakeUserRepository()
        networkFlow.value = NetworkStatus.Available

        every { connectivityObserver.networkStatus } returns networkFlow
        every { getHomeDataUseCase(any()) } returns
                flowOf(Result.success(homeData()))
        coEvery { locationManager.getCurrentLocation() } returns
                Result.success(
                    CurrentLocationResult(
                        displayAddress = HOME_LOC_KORAMANGALA,
                        latitude = GPS_LAT,
                        longitude = GPS_LNG,
                    )
                )
    }

    afterEach { Dispatchers.resetMain() }


    // GROUP 1 — Initial State
    given("HomeScreen opens for the first time") {

        `when`("ViewModel is created") {
            lateinit var vm: HomeViewModel

            beforeEach {
                vm = createViewModel()   // ONE VM — all then blocks share
            }

            then("isLoading is false after data loads") {
                vm.uiState.value.isLoading shouldBe false
            }
            then("error is null") {
                vm.uiState.value.error shouldBe null
            }
            then("selectedLocation defaults to DEFAULT_LOCATION") {
                vm.uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
            then("selectedTab is DELIVERY") {
                vm.uiState.value.selectedTab shouldBe
                        HomeViewModel.DeliveryTab.DELIVERY
            }
            then("cartItemCount is 0") {
                vm.uiState.value.cartItemCount shouldBe 0
            }
            then("showLocationPicker is false") {
                vm.uiState.value.showLocationPicker shouldBe false
            }
            then("savedAddresses is empty") {
                vm.uiState.value.savedAddresses shouldBe emptyList()
            }
            then("restaurants list has 4 items") {
                vm.uiState.value.restaurants.size shouldBe HOME_REST_COUNT_4
            }
            then("collections has 2 items") {
                vm.uiState.value.collections.size shouldBe HOME_COLL_SIZE_2
            }
            then("categories has 2 items") {
                vm.uiState.value.categories.size shouldBe HOME_CAT_COUNT_2
            }
        }
    }


    // GROUP 2 — API Error + Retry
    given("API throws error") {

        `when`("ViewModel is created") {
            then("error message is set in state") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.failure(Exception(ERR_NO_INTERNET_HOME)))

                val vm = createViewModel()

                vm.uiState.value.error shouldBe ERR_NO_INTERNET_HOME
                vm.uiState.value.isLoading shouldBe false
            }
        }

        `when`("user taps retry") {
            then("restaurants reload on success") {
                every { getHomeDataUseCase(any()) } returnsMany listOf(
                    flowOf(Result.failure(Exception(ERR_ERROR_STR))),
                    flowOf(Result.success(homeData())),
                )

                val vm = createViewModel()
                vm.uiState.value.error shouldNotBe null

                vm.retry()

                vm.uiState.value.restaurants.size shouldBe HOME_REST_COUNT_4
                vm.uiState.value.error shouldBe null
            }
        }
    }


    // GROUP 3 — Location Selection
    // WHY VM per then:
    // Each then needs different stub (different restaurant list)
    given("user selects a delivery location") {

        `when`("Koramangala is selected") {
            then("selectedLocation updates to Koramangala") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.success(homeData(restaurants = restaurantsKoramangala)))

                val vm = createViewModel()
                vm.onLocationSelected(HOME_LOC_KORAMANGALA)

                vm.uiState.value.selectedLocation shouldBe HOME_LOC_KORAMANGALA
            }

            then("restaurants filter to Koramangala only") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.success(homeData(restaurants = restaurantsKoramangala)))

                val vm = createViewModel()
                vm.onLocationSelected(HOME_LOC_KORAMANGALA)

                vm.uiState.value.restaurants.size shouldBe HOME_REST_COUNT_2
                vm.uiState.value.restaurants
                    .all { it.locality == HOME_LOC_KORAMANGALA } shouldBe true
            }
        }

        `when`("Indiranagar is selected") {
            then("only Indiranagar restaurants shown") {
                every { getHomeDataUseCase(any()) } returns
                        flowOf(Result.success(homeData(restaurants = restaurantsIndiranagar)))

                val vm = createViewModel()
                vm.onLocationSelected(HOME_LOC_INDIRANAGAR)

                vm.uiState.value.restaurants.size shouldBe HOME_REST_COUNT_1
                vm.uiState.value.restaurants.first().name shouldBe HOME_REST_PIZZA_HUT
            }
        }

        `when`("any location is selected") {
            then("saveSelectedLocation called in UserRepository") {
                val vm = createViewModel()
                vm.onLocationSelected(HOME_LOC_WHITEFIELD)

                fakeUserRepo.saveSelectedLocationCalled shouldBe true
                fakeUserRepo.lastSavedLocation shouldBe HOME_LOC_WHITEFIELD
            }

            then("showLocationPicker closes") {
                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onLocationSelected(HOME_LOC_KORAMANGALA)

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

                vm.onLocationSelected(HOME_LOC_HSR)

                callCount shouldNotBe countBefore
            }
        }
    }

    // GROUP 4 — Saved Location Restored
    // WHY VM per then:
    // Each then sets different fakeUserRepo state before VM
    // UserRepository.getCurrentUser() emits on VM init

    given("user had previously saved Indiranagar") {

        `when`("app reopens and Room emits saved location") {
            then("selectedLocation restored to Indiranagar") {
                fakeUserRepo.setUserWithLocation(HOME_LOC_INDIRANAGAR)
                createViewModel().uiState.value.selectedLocation shouldBe HOME_LOC_INDIRANAGAR
            }

            then("fresh selection overrides Room value") {
                fakeUserRepo.setUserWithLocation(HOME_LOC_KORAMANGALA)
                val vm = createViewModel()
                vm.onLocationSelected(HOME_LOC_HSR)
                vm.uiState.value.selectedLocation shouldBe HOME_LOC_HSR
            }
        }

        `when`("saved location is empty") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUserWithLocation("")
                createViewModel().uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }

        `when`("user is null") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUser(null)
                createViewModel().uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Saved Addresses
    // WHY VM per then:
    // Each then sets different fakeUserRepo address state
    // ══════════════════════════════════════════════════════════

    given("user has saved addresses in profile") {

        `when`("ViewModel loads user profile") {
            then("savedAddresses contains both Home and Work") {
                fakeUserRepo.setUserWithAddresses(
                    FakeUserRepository.fakeHomeAddress(),
                    FakeUserRepository.fakeWorkAddress(),
                )
                val vm = createViewModel()

                vm.uiState.value.savedAddresses.size shouldBe HOME_ADDR_COUNT_2
                vm.uiState.value.savedAddresses
                    .any { it.label == ADDRESS_LABEL_HOME } shouldBe true
                vm.uiState.value.savedAddresses
                    .any { it.label == ADDRESS_LABEL_WORK } shouldBe true
            }
        }

        `when`("user has no addresses") {
            then("savedAddresses is empty") {
                fakeUserRepo.setUser(FakeUserRepository.fakeUser())
                createViewModel().uiState.value.savedAddresses shouldBe emptyList()
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Cart Count
    // WHY shared VM for "cart has 3 items":
    // setItemCount BEFORE VM → Flow emits on creation → correct
    // WHY no shared VM for "cart is empty":
    // default is empty — VM already correct without seeding
    // ══════════════════════════════════════════════════════════

    given("cart has items") {

        `when`("cart has 3 items") {
            lateinit var vm: HomeViewModel

            beforeEach {
                fakeCartRepo.setItemCount(HOME_CART_COUNT_3)  // seed first
                vm = createViewModel()                          // then create
            }

            then("cartItemCount shows 3") {
                vm.uiState.value.cartItemCount shouldBe HOME_CART_COUNT_3
            }
        }

        `when`("cart is empty") {
            lateinit var vm: HomeViewModel

            beforeEach {
                vm = createViewModel()   // default cart is empty
            }

            then("cartItemCount is 0") {
                vm.uiState.value.cartItemCount shouldBe 0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Connectivity
    // WHY shared VM for Lost/Available groups:
    // VM created once — networkFlow changed AFTER creation
    // MutableStateFlow emits → connectivity observer reacts
    // ══════════════════════════════════════════════════════════

    given("device loses internet") {

        `when`("network becomes Lost") {
            lateinit var vm: HomeViewModel

            beforeEach {
                vm = createViewModel()
            }

            then("isOffline becomes true") {
                networkFlow.value = NetworkStatus.Lost
                vm.uiState.value.isOffline shouldBe true
            }
        }

        `when`("network becomes Unavailable") {
            lateinit var vm: HomeViewModel

            beforeEach {
                vm = createViewModel()
            }

            then("isOffline is true") {
                networkFlow.value = NetworkStatus.Unavailable
                vm.uiState.value.isOffline shouldBe true
            }
        }
    }

    given("device reconnects") {

        `when`("network changes from Lost to Available") {
            lateinit var vm: HomeViewModel

            beforeEach {
                vm = createViewModel()
            }

            then("isOffline becomes false") {
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

                // Recreate VM with new stub tracking
                val trackedVm = createViewModel()
                val countBefore = callCount

                networkFlow.value = NetworkStatus.Lost
                networkFlow.value = NetworkStatus.Available

                callCount shouldNotBe countBefore
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Filter Status
    // WHY VM per then:
    // Each then needs different stub with different FilterStatus
    // ══════════════════════════════════════════════════════════

    given("API returns filtered results") {

        `when`("filterStatus is FOUND") {
            then("uiState reflects FOUND with requestedArea") {
                every { getHomeDataUseCase(any()) } returns flowOf(
                    Result.success(
                        homeData(
                            restaurants = restaurantsKoramangala,
                            filterStatus = FilterStatus.FOUND,
                            requestedArea = HOME_LOC_KORAMANGALA,
                        )
                    )
                )
                val vm = createViewModel()

                vm.uiState.value.filterStatus shouldBe FilterStatus.FOUND
                vm.uiState.value.requestedArea shouldBe HOME_LOC_KORAMANGALA
            }
        }

        `when`("filterStatus is NOT_SERVICEABLE") {
            then("restaurants empty and availableAreas populated") {
                every { getHomeDataUseCase(any()) } returns flowOf(
                    Result.success(
                        homeData(
                            restaurants = emptyList(),
                            filterStatus = FilterStatus.NOT_SERVICEABLE,
                            requestedArea = LOC_JAKKUR,
                            availableAreas = listOf(
                                HOME_LOC_KORAMANGALA,
                                HOME_LOC_INDIRANAGAR,
                                HOME_LOC_HSR,
                            ),
                        )
                    )
                )
                val vm = createViewModel()

                vm.uiState.value.filterStatus shouldBe FilterStatus.NOT_SERVICEABLE
                vm.uiState.value.restaurants.isEmpty() shouldBe true
                vm.uiState.value.availableAreas.size shouldBe HOME_AVAILABLE_AREAS_3
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 9 — Navigation Events
    // WHY shared VM:
    // All then blocks use DEFAULT stub from spec beforeEach
    // All are navigation events — same initial state
    // ══════════════════════════════════════════════════════════

    given("user is on HomeScreen") {
        lateinit var vm: HomeViewModel

        beforeEach {
            vm = createViewModel()
        }

        `when`("restaurant card tapped") {
            then("NavigateToRestaurant emitted with correct id") {
                vm.events.test {
                    vm.onRestaurantClicked(HOME_REST_R1)
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToRestaurant(HOME_REST_R1)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("search bar tapped") {
            then("NavigateToSearch emitted with empty query") {
                vm.events.test {
                    vm.onSearchClicked()
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToSearch("")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("category chip tapped") {
            then("NavigateToSearch emitted with category name") {
                vm.events.test {
                    vm.onCategoryClicked(HOME_CATEGORY_BIRYANI)
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToSearch(HOME_CATEGORY_BIRYANI)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("cart icon tapped") {
            then("NavigateToCart event emitted") {
                vm.events.test {
                    vm.onCartClicked()
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToCart
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("PROFILE tab tapped") {
            then("NavigateToProfile event emitted") {
                vm.events.test {
                    vm.onTabSelected(HomeViewModel.DeliveryTab.PROFILE)
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToProfile
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("DINING tab tapped") {
            then("selectedTab updates — no event emitted") {
                vm.onTabSelected(HomeViewModel.DeliveryTab.DINING)
                vm.uiState.value.selectedTab shouldBe HomeViewModel.DeliveryTab.DINING
            }
        }

        `when`("profile icon tapped directly") {
            then("NavigateToProfile emitted via onProfileClicked") {
                vm.events.test {
                    vm.onProfileClicked()
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToProfile
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("search tapped with query") {
            then("NavigateToSearch emitted with correct query") {
                vm.events.test {
                    vm.onSearchClicked(HOME_SEARCH_QUERY_BURGER)
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToSearch(HOME_SEARCH_QUERY_BURGER)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})