package com.swapna.foodapp.presentation.home

import app.cash.turbine.test
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.presentation.common.ConnectivityObserver
import com.swapna.foodapp.presentation.common.CurrentLocationResult
import com.swapna.foodapp.presentation.common.LocationManager
import com.swapna.foodapp.presentation.common.NetworkStatus
import com.swapna.foodapp.presentation.common.fakes.FakeCartRepository
import com.swapna.foodapp.presentation.common.fakes.FakeUserRepository
import com.swapna.foodapp.utils.AppConstants.DEFAULT_LOCATION
import com.swapna.foodapp.utils.HomeData
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_WORK
import com.swapna.foodapp.utils.TestConstants.DELIVERY_TIME
import com.swapna.foodapp.utils.TestConstants.ERR_ERROR_STR
import com.swapna.foodapp.utils.TestConstants.ERR_NO_INTERNET_HOME
import com.swapna.foodapp.utils.TestConstants.GPS_LAT
import com.swapna.foodapp.utils.TestConstants.GPS_LNG
import com.swapna.foodapp.utils.TestConstants.HOME_ADDR_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_AVAILABLE_AREAS_3
import com.swapna.foodapp.utils.TestConstants.HOME_CART_COUNT_3
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_BIRYANI
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_ID_2
import com.swapna.foodapp.utils.TestConstants.HOME_CATEGORY_PIZZA
import com.swapna.foodapp.utils.TestConstants.HOME_CAT_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_60_OFF
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_COUNT_10
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_COUNT_20
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_FREE_DEL
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_ID_1
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_ID_2
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_JUST_LAUNCHED
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_NEWLY_OPENED
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_POPULAR
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_SIZE_2
import com.swapna.foodapp.utils.TestConstants.HOME_COLL_TRENDING
import com.swapna.foodapp.utils.TestConstants.HOME_DELIVERY_FEE
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_HSR
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_INDIRANAGAR
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.HOME_LOC_WHITEFIELD
import com.swapna.foodapp.utils.TestConstants.HOME_OFFERS_50_OFF
import com.swapna.foodapp.utils.TestConstants.HOME_RATING_COLOR
import com.swapna.foodapp.utils.TestConstants.HOME_RATING_TEXT_EXCELLENT
import com.swapna.foodapp.utils.TestConstants.HOME_REST_BURGER_KING
import com.swapna.foodapp.utils.TestConstants.HOME_REST_COUNT_1
import com.swapna.foodapp.utils.TestConstants.HOME_REST_COUNT_2
import com.swapna.foodapp.utils.TestConstants.HOME_REST_COUNT_4
import com.swapna.foodapp.utils.TestConstants.HOME_REST_EMPIRE
import com.swapna.foodapp.utils.TestConstants.HOME_REST_MEGHANA
import com.swapna.foodapp.utils.TestConstants.HOME_REST_PIZZA_HUT
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R1
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R2
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R3
import com.swapna.foodapp.utils.TestConstants.HOME_REST_R4
import com.swapna.foodapp.utils.TestConstants.HOME_SEARCH_QUERY_BURGER
import com.swapna.foodapp.utils.TestConstants.HOME_SOUTH_INDIAN
import com.swapna.foodapp.utils.TestConstants.HOME_TEST_REST_NAME
import com.swapna.foodapp.utils.TestConstants.LOC_JAKKUR
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_COST_500
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_MIN_ORDER_100
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_RATING_45
import com.swapna.foodapp.utils.TestConstants.RESTAURANT_VOTES_5000
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

    val fakeCollections = listOf(
        Collections(
            HOME_COLL_ID_1,
            HOME_COLL_TRENDING,
            HOME_COLL_POPULAR,
            "",
            HOME_COLL_COUNT_20,
            HOME_COLL_60_OFF
        ),
        Collections(
            HOME_COLL_ID_2,
            HOME_COLL_NEWLY_OPENED,
            HOME_COLL_JUST_LAUNCHED,
            "",
            HOME_COLL_COUNT_10,
            HOME_COLL_FREE_DEL
        ),
    )

    val fakeCategories = listOf(
        FoodCategory(HOME_CATEGORY_ID_1, HOME_CATEGORY_BIRYANI, ""),
        FoodCategory(HOME_CATEGORY_ID_2, HOME_CATEGORY_PIZZA, ""),
    )

    val restaurantsKoramangala = listOf(
        fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA, locality = HOME_LOC_KORAMANGALA),
        fakeRestaurant(HOME_REST_R4, HOME_REST_EMPIRE, locality = HOME_LOC_KORAMANGALA),
    )

    val restaurantsIndiranagar = listOf(
        fakeRestaurant(HOME_REST_R2, HOME_REST_PIZZA_HUT, locality = HOME_LOC_INDIRANAGAR),
    )

    val restaurantsAll = listOf(
        fakeRestaurant(HOME_REST_R1, HOME_REST_MEGHANA, locality = HOME_LOC_KORAMANGALA),
        fakeRestaurant(HOME_REST_R2, HOME_REST_PIZZA_HUT, locality = HOME_LOC_INDIRANAGAR),
        fakeRestaurant(HOME_REST_R3, HOME_REST_BURGER_KING, locality = HOME_LOC_HSR),
        fakeRestaurant(HOME_REST_R4, HOME_REST_EMPIRE, locality = HOME_LOC_KORAMANGALA),
    )

    fun emptyHomeData() = HomeData(
        restaurants = emptyList(),
        collections = emptyList(),
        categories = emptyList(),
        filterStatus = FilterStatus.NO_FILTER,
        requestedArea = "",
        availableAreas = emptyList(),
    )

    fun homeData(
        restaurants: List<Restaurant> = restaurantsAll,
        collections: List<Collections> = fakeCollections,
        categories: List<FoodCategory> = fakeCategories,
        filterStatus: FilterStatus = FilterStatus.NO_FILTER,
        requestedArea: String = "",
        availableAreas: List<String> = emptyList(),
    ) = HomeData(
        restaurants = restaurants,
        collections = collections,
        categories = categories,
        filterStatus = filterStatus,
        requestedArea = requestedArea,
        availableAreas = availableAreas,
    )

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

    // ══════════════════════════════════════════════════════════
    // GROUP 1 — Initial State
    // ══════════════════════════════════════════════════════════

    given("HomeScreen opens for the first time") {

        `when`("ViewModel is created") {
            then("isLoading is false after data loads") {
                createViewModel().uiState.value.isLoading shouldBe false
            }
            then("error is null") {
                createViewModel().uiState.value.error shouldBe null
            }
            then("selectedLocation defaults to DEFAULT_LOCATION") {
                createViewModel().uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
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
                createViewModel().uiState.value.restaurants.size shouldBe HOME_REST_COUNT_4
            }
            then("collections has 2 items") {
                createViewModel().uiState.value.collections.size shouldBe HOME_COLL_SIZE_2
            }
            then("categories has 2 items") {
                createViewModel().uiState.value.categories.size shouldBe HOME_CAT_COUNT_2
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 2 — API Error + Retry
    // ══════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════
    // GROUP 3 — Location Selection
    // ══════════════════════════════════════════════════════════

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

    // ══════════════════════════════════════════════════════════
    // GROUP 4 — Saved Location Restored
    // ══════════════════════════════════════════════════════════

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
                vm.uiState.value.savedAddresses.any { it.label == ADDRESS_LABEL_HOME } shouldBe true
                vm.uiState.value.savedAddresses.any { it.label == ADDRESS_LABEL_WORK } shouldBe true
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
    // ══════════════════════════════════════════════════════════

    given("cart has items") {

        `when`("cart has 3 items") {
            then("cartItemCount shows 3") {
                fakeCartRepo.setItemCount(HOME_CART_COUNT_3)
                createViewModel().uiState.value.cartItemCount shouldBe HOME_CART_COUNT_3
            }
        }

        `when`("cart is empty") {
            then("cartItemCount is 0") {
                createViewModel().uiState.value.cartItemCount shouldBe 0
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // GROUP 7 — Connectivity
    // ══════════════════════════════════════════════════════════

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
                createViewModel().uiState.value.isOffline shouldBe true
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

    // ══════════════════════════════════════════════════════════
    // GROUP 8 — Filter Status
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
    // ══════════════════════════════════════════════════════════

    given("user is on HomeScreen") {

        `when`("restaurant card tapped") {
            then("NavigateToRestaurant emitted with correct id") {
                val vm = createViewModel()
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
                val vm = createViewModel()
                vm.events.test {
                    vm.onSearchClicked()
                    awaitItem() shouldBe HomeViewModel.HomeEvent.NavigateToSearch("")
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        `when`("category chip tapped") {
            then("NavigateToSearch emitted with category name") {
                val vm = createViewModel()
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
                vm.uiState.value.selectedTab shouldBe HomeViewModel.DeliveryTab.DINING
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
                    vm.onSearchClicked(HOME_SEARCH_QUERY_BURGER)
                    awaitItem() shouldBe
                            HomeViewModel.HomeEvent.NavigateToSearch(HOME_SEARCH_QUERY_BURGER)
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }
    }
})

fun fakeRestaurant(
    id: String = HOME_REST_R1,
    name: String = HOME_TEST_REST_NAME,
    locality: String = HOME_LOC_KORAMANGALA,
    rating: Double = RESTAURANT_RATING_45,
) = Restaurant(
    id = id,
    name = name,
    imageUrl = "https://picsum.photos/seed/$id/600/300",
    thumbUrl = "https://picsum.photos/seed/$id/200/200",
    rating = rating,
    ratingText = HOME_RATING_TEXT_EXCELLENT,
    ratingColor = HOME_RATING_COLOR,
    totalVotes = RESTAURANT_VOTES_5000,
    avgDeliveryTime = DELIVERY_TIME,
    deliveryFee = HOME_DELIVERY_FEE,
    avgCostForTwo = RESTAURANT_COST_500,
    minOrder = RESTAURANT_MIN_ORDER_100,
    cuisines = listOf(
        HOME_CATEGORY_BIRYANI,
        HOME_SOUTH_INDIAN
    ),
    address = "$locality, Bengaluru",
    locality = locality,
    distanceKm = 0.0,
    hasDelivery = true,
    isOpen = true,
    offers = listOf(HOME_OFFERS_50_OFF),
)