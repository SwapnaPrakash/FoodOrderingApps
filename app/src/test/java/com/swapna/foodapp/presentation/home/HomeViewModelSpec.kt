package com.swapna.foodapp.presentation.home

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.utils.ConnectivityObserver
import com.swapna.foodapp.utils.HomeData
import com.swapna.foodapp.utils.NetworkStatus
import com.swapna.foodapp.utils.fakeRestaurant
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelSpec : BehaviorSpec({

    val getHomeDataUseCase   = mockk<GetHomeDataUseCase>()
    val cartRepository       = mockk<CartRepository>()
    val connectivityObserver = mockk<ConnectivityObserver>()
    val userRepository       = mockk<UserRepository>()

    val fakeCollections = listOf(
        Collections(1, "Trending", "Popular", "", 20, "60% OFF"),
        Collections(2, "Newly Opened", "Just launched", "", 10, "Free Delivery"),
    )
    val fakeCategories = listOf(
        FoodCategory(1, "Biryani", ""),
        FoodCategory(2, "Pizza", ""),
    )

    // ✅ Restaurants from different localities
    val fakeRestaurantsKoramangala = listOf(
        fakeRestaurant("r1", "Meghana Foods",  locality = "Koramangala"),
        fakeRestaurant("r4", "Empire",          locality = "Koramangala"),
    )
    val fakeRestaurantsIndiranagar = listOf(
        fakeRestaurant("r2", "Pizza Hut",      locality = "Indiranagar"),
    )
    val fakeRestaurantsAll = listOf(
        fakeRestaurant("r1", "Meghana Foods",  locality = "Koramangala"),
        fakeRestaurant("r2", "Pizza Hut",      locality = "Indiranagar"),
        fakeRestaurant("r3", "Burger King",    locality = "HSR Layout"),
        fakeRestaurant("r4", "Empire",          locality = "Koramangala"),
    )

    val fakeHomeDataAll = HomeData(
        collections = fakeCollections,
        categories  = fakeCategories,
        restaurants = fakeRestaurantsAll,
    )
    val fakeHomeDataKoramangala = HomeData(
        collections = fakeCollections,
        categories  = fakeCategories,
        restaurants = fakeRestaurantsKoramangala,
    )
    val fakeHomeDataIndiranagar = HomeData(
        collections = fakeCollections,
        categories  = fakeCategories,
        restaurants = fakeRestaurantsIndiranagar,
    )

    val networkFlow = MutableStateFlow<NetworkStatus>(
        NetworkStatus.Available
    )

    // ✅ FIX: createViewModel now includes userRepository
    fun createViewModel() = HomeViewModel(
        getHomeDataUseCase   = getHomeDataUseCase,
        cartRepository       = cartRepository,
        userRepository       = userRepository,
        connectivityObserver = connectivityObserver,
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { connectivityObserver.networkStatus } returns
                networkFlow

        every { cartRepository.getCartItemCount() } returns
                flowOf(0)

        // ✅ FIX: default user mock = no saved location
        every { userRepository.getCurrentUser() } returns
                flowOf(null)

        coEvery {
            userRepository.saveSelectedLocation(any())
        } just Runs
    }

    afterEach {
        networkFlow.value = NetworkStatus.Available
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial Load (no location selected)
    // ══════════════════════════════════════════════════════════

    given("API returns full home data — no location filter") {

        `when`("ViewModel created with no saved location") {
            then("all restaurants shown — no filter applied") {
                // ✅ invoke with empty string = no filter
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()

                vm.uiState.value.restaurants.size shouldBe 4
                vm.uiState.value.isLoading        shouldBe false
                vm.uiState.value.error            shouldBe null
            }
        }

        `when`("ViewModel created") {
            then("collections have 2 items") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()
                vm.uiState.value.collections.size shouldBe 2
            }
        }

        `when`("ViewModel created") {
            then("categories have 2 items") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()
                vm.uiState.value.categories.size shouldBe 2
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — Location Filter
    // ✅ NEW: Tests for location-based filtering
    // ══════════════════════════════════════════════════════════

    given("user selects a delivery location") {

        `when`("Koramangala is selected") {
            then("only Koramangala restaurants shown") {
                every {
                    getHomeDataUseCase("")
                } returns flowOf(Result.success(fakeHomeDataAll))

                every {
                    getHomeDataUseCase("Koramangala, Bengaluru")
                } returns flowOf(
                    Result.success(fakeHomeDataKoramangala)
                )

                val vm = createViewModel()
                vm.onLocationSelected("Koramangala, Bengaluru")

                vm.uiState.value.restaurants.size shouldBe 2
                vm.uiState.value.restaurants
                    .all { it.locality == "Koramangala" } shouldBe true
            }
        }

        `when`("Indiranagar is selected") {
            then("only Indiranagar restaurants shown") {
                every {
                    getHomeDataUseCase("")
                } returns flowOf(Result.success(fakeHomeDataAll))

                every {
                    getHomeDataUseCase("Indiranagar, Bengaluru")
                } returns flowOf(
                    Result.success(fakeHomeDataIndiranagar)
                )

                val vm = createViewModel()
                vm.onLocationSelected("Indiranagar, Bengaluru")

                vm.uiState.value.restaurants.size shouldBe 1
                vm.uiState.value.restaurants
                    .first().name shouldBe "Pizza Hut"
            }
        }

        `when`("location is selected") {
            then("userLocation in state updates to selected value") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()
                vm.onLocationSelected("HSR Layout, Bengaluru")

                vm.uiState.value.userLocation shouldBe
                        "HSR Layout, Bengaluru"
            }
        }

        `when`("location is selected") {
            then("saveSelectedLocation called in repository") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()
                vm.onLocationSelected("Whitefield, Bengaluru")

                coVerify {
                    userRepository.saveSelectedLocation(
                        "Whitefield, Bengaluru"
                    )
                }
            }
        }

        `when`("location is selected") {
            then("showLocationPicker becomes false") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()
                vm.onLocationClicked()
                vm.uiState.value.showLocationPicker shouldBe true

                vm.onLocationSelected("Koramangala, Bengaluru")
                vm.uiState.value.showLocationPicker shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Saved Location Restored on Startup
    // ✅ NEW: Tests for location persistence
    // ══════════════════════════════════════════════════════════

    given("user had previously selected Indiranagar") {

        `when`("app reopens — saved location restored from Room") {
            then("userLocation shows saved location not default") {
                // User saved "Indiranagar" in previous session
                every {
                    userRepository.getCurrentUser()
                } returns flowOf(
                    User(
                        id               = "u1",
                        name             = "Swapna",
                        email            = "",
                        phone            = "+91 9876543210",
                        selectedLocation = "Indiranagar, Bengaluru",
                        addresses        = emptyList(),
                    )
                )

                every {
                    getHomeDataUseCase(any())
                } returns flowOf(
                    Result.success(fakeHomeDataIndiranagar)
                )

                val vm = createViewModel()

                vm.uiState.value.userLocation shouldBe
                        "Indiranagar, Bengaluru"
            }
        }

        `when`("app reopens — saved location restored") {
            then("restaurants filtered for saved location") {
                every {
                    userRepository.getCurrentUser()
                } returns flowOf(
                    User(
                        id               = "u1",
                        name             = "Swapna",
                        email            = "",
                        phone            = "+91 9876543210",
                        selectedLocation = "Indiranagar, Bengaluru",
                        addresses        = emptyList(),
                    )
                )

                every {
                    getHomeDataUseCase("Indiranagar, Bengaluru")
                } returns flowOf(
                    Result.success(fakeHomeDataIndiranagar)
                )

                every {
                    getHomeDataUseCase("")
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()

                vm.uiState.value.restaurants
                    .all { it.locality == "Indiranagar" } shouldBe true
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Saved Addresses from Room
    // ✅ NEW: Tests for real addresses in LocationPickerSheet
    // ══════════════════════════════════════════════════════════

    given("user has saved addresses in profile") {

        `when`("ViewModel loads user profile") {
            then("savedAddresses populated from Room") {
                val fakeAddresses = listOf(
                    Address(
                        id          = "a1",
                        label       = "Home",
                        fullAddress = "123 MG Road, Koramangala",
                    ),
                    Address(
                        id          = "a2",
                        label       = "Work",
                        fullAddress = "Tech Park, Whitefield",
                    ),
                )

                every {
                    userRepository.getCurrentUser()
                } returns flowOf(
                    User(
                        id               = "u1",
                        name             = "Swapna",
                        email            = "",
                        phone            = "+91 9876543210",
                        selectedLocation = "",
                        addresses        = fakeAddresses,
                    )
                )

                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()

                vm.uiState.value.savedAddresses.size shouldBe 2
                vm.uiState.value.savedAddresses
                    .first().label shouldBe "Home"
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 5 — Use Current Location
    // ✅ NEW: Tests for GPS location
    // ══════════════════════════════════════════════════════════

    given("user taps Use Current Location") {

        `when`("current location detected as Koramangala") {
            then("restaurants filtered for detected locality") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(
                    Result.success(fakeHomeDataKoramangala)
                )

                val vm = createViewModel()

                // Simulate GPS returning Koramangala
                vm.onCurrentLocationDetected(
                    locality = "Koramangala",
                    address  = "5th Block, Koramangala, Bengaluru",
                )

                vm.uiState.value.userLocation shouldBe
                        "Koramangala"
                vm.uiState.value.restaurants
                    .all { it.locality == "Koramangala" } shouldBe true
            }
        }

        `when`("GPS permission denied") {
            then("location stays as previous selection") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()
                val prevLocation = vm.uiState.value.userLocation

                vm.onLocationPermissionDenied()

                // location unchanged
                vm.uiState.value.userLocation shouldBe prevLocation
                vm.uiState.value.showLocationPicker shouldBe false
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 6 — Cart + Error + Connectivity (unchanged)
    // ══════════════════════════════════════════════════════════

    given("cart has 3 items") {
        `when`("ViewModel created") {
            then("cartItemCount should be 3") {
                every { cartRepository.getCartItemCount() } returns
                        flowOf(3)
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(HomeData()))

                val vm = createViewModel()
                vm.uiState.value.cartItemCount shouldBe 3
            }
        }
    }

    given("API throws IOException") {
        `when`("ViewModel created") {
            then("error message shown") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(
                    Result.failure(Exception("No internet"))
                )

                val vm = createViewModel()
                vm.uiState.value.error shouldBe "No internet"
                vm.uiState.value.isLoading shouldBe false
            }
        }
    }

    given("device loses internet") {
        `when`("network status changes to Lost") {
            then("isOffline becomes true") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(HomeData()))

                networkFlow.value = NetworkStatus.Lost
                val vm = createViewModel()

                vm.uiState.value.isOffline shouldBe true
            }
        }
    }

    given("retry after error") {
        `when`("retry called") {
            then("restaurants reload successfully") {
                every {
                    getHomeDataUseCase(any())
                } returnsMany listOf(
                    flowOf(Result.failure(Exception("Network error"))),
                    flowOf(Result.success(fakeHomeDataAll)),
                )

                val vm = createViewModel()
                vm.uiState.value.error shouldNotBe null

                vm.retry()
                vm.uiState.value.restaurants.size shouldBe 4
                vm.uiState.value.error shouldBe null
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Navigation Events (unchanged + profile)
    // ══════════════════════════════════════════════════════════

    given("home data loaded") {

        `when`("restaurant tapped") {
            then("NavigateToRestaurant event emitted") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()

                vm.events.test {
                    vm.onRestaurantClicked("r1")
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent
                                .NavigateToRestaurant("r1")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("cart tapped") {
            then("NavigateToCart event emitted") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()

                vm.events.test {
                    vm.onCartClicked()
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToCart
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("profile tab tapped") {
            then("NavigateToProfile event emitted") {
                every {
                    getHomeDataUseCase(any())
                } returns flowOf(Result.success(fakeHomeDataAll))

                val vm = createViewModel()

                vm.events.test {
                    vm.onTabSelected(
                        HomeViewModel.DeliveryTab.PROFILE
                    )
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToProfile
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})