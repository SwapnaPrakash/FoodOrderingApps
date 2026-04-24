package com.swapna.foodapp.presentation.home

import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.presentation.common.fakes.FakeCartRepository
import com.swapna.foodapp.presentation.common.fakes.FakeUserRepository
import com.swapna.foodapp.presentation.common.CurrentLocationResult
import com.swapna.foodapp.presentation.common.LocationManager
import com.swapna.foodapp.utils.AppConstants.DEFAULT_LOCATION
import com.swapna.foodapp.presentation.common.ConnectivityObserver
import com.swapna.foodapp.utils.HomeData
import com.swapna.foodapp.presentation.common.NetworkStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotBeEmpty
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
class HomeViewModelLocationSpec : BehaviorSpec({

    val dispatcher = UnconfinedTestDispatcher()

    // ── MockK ─────────────────────────────────────────────────────────
    val getHomeDataUseCase   = mockk<GetHomeDataUseCase>()
    val connectivityObserver = mockk<ConnectivityObserver>()
    val locationManager      = mockk<LocationManager>()

    // ── Fakes ─────────────────────────────────────────────────────────
    lateinit var fakeCartRepo: FakeCartRepository
    lateinit var fakeUserRepo: FakeUserRepository

    val networkFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    // ── Helpers ───────────────────────────────────────────────────────
    fun emptyHomeData() = HomeData(
        restaurants    = emptyList(),
        collections    = emptyList(),
        categories     = emptyList(),
        filterStatus   = FilterStatus.NO_FILTER,
        requestedArea  = "",
        availableAreas = emptyList(),
    )

    fun gpsSuccess(address: String) = Result.success(
        CurrentLocationResult(
            displayAddress = address,
            latitude       = 12.9352,
            longitude      = 77.6245,
        )
    )

    fun gpsFailure(message: String) =
        Result.failure<CurrentLocationResult>(Exception(message))

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
        fakeCartRepo  = FakeCartRepository()
        fakeUserRepo  = FakeUserRepository()

        networkFlow.value = NetworkStatus.Available
        every { connectivityObserver.networkStatus } returns networkFlow
        every { getHomeDataUseCase(any()) } returns
                flowOf(Result.success(emptyHomeData()))
        coEvery { locationManager.getCurrentLocation() } returns
                gpsSuccess("Koramangala 5th Block, Bengaluru")
    }

    afterEach {
        Dispatchers.resetMain()
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 1 — Location Picker Default State
    // ══════════════════════════════════════════════════════════════════

    given("HomeViewModel is created fresh") {

        `when`("no location selected yet") {
            then("showLocationPicker is false") {
                createViewModel().uiState.value.showLocationPicker shouldBe false
            }

            then("locationFetchState is Idle") {
                createViewModel().uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Idle
            }

            then("locationErrorMsg is empty") {
                createViewModel().uiState.value.locationErrorMsg shouldBe ""
            }

            then("selectedLocation is DEFAULT_LOCATION") {
                createViewModel().uiState.value.selectedLocation shouldBe
                        DEFAULT_LOCATION
            }

            then("savedAddresses is empty") {
                createViewModel().uiState.value.savedAddresses shouldBe emptyList()
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 2 — Open Location Sheet
    // ══════════════════════════════════════════════════════════════════

    given("user taps location bar in HomeTopBar") {

        `when`("onLocationBarClicked is called") {
            then("showLocationPicker becomes true") {
                val vm = createViewModel()
                vm.onLocationBarClicked()

                vm.uiState.value.showLocationPicker shouldBe true
            }

            then("locationFetchState resets to Idle") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.onLocationBarClicked()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Idle
            }

            then("locationErrorMsg is cleared") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.onLocationBarClicked()

                vm.uiState.value.locationErrorMsg shouldBe ""
            }

            then("selectedLocation is unchanged after open") {
                val vm = createViewModel()
                vm.onLocationSelected("Koramangala")
                vm.onLocationBarClicked()

                vm.uiState.value.selectedLocation shouldBe "Koramangala"
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 3 — Dismiss Location Sheet
    // ══════════════════════════════════════════════════════════════════

    given("location picker sheet is open") {

        `when`("onLocationDismissed is called") {
            then("showLocationPicker becomes false") {
                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onLocationDismissed()

                vm.uiState.value.showLocationPicker shouldBe false
            }

            then("locationFetchState resets to Idle") {
                val vm = createViewModel()
                vm.onLocationDismissed()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Idle
            }

            then("locationErrorMsg is cleared on dismiss") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.onLocationDismissed()

                vm.uiState.value.locationErrorMsg shouldBe ""
            }

            then("selectedLocation unchanged after dismiss") {
                val vm = createViewModel()
                vm.onLocationSelected("Indiranagar")
                vm.onLocationBarClicked()
                vm.onLocationDismissed()

                vm.uiState.value.selectedLocation shouldBe "Indiranagar"
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 4 — Manual Location Selection
    // ══════════════════════════════════════════════════════════════════

    given("user picks location from popular list") {

        `when`("Koramangala selected") {
            then("selectedLocation is Koramangala") {
                val vm = createViewModel()
                vm.onLocationSelected("Koramangala")

                vm.uiState.value.selectedLocation shouldBe "Koramangala"
            }
        }

        `when`("HSR Layout selected") {
            then("selectedLocation is HSR Layout") {
                val vm = createViewModel()
                vm.onLocationSelected("HSR Layout")

                vm.uiState.value.selectedLocation shouldBe "HSR Layout"
            }
        }

        `when`("Whitefield selected") {
            then("selectedLocation is Whitefield") {
                val vm = createViewModel()
                vm.onLocationSelected("Whitefield")

                vm.uiState.value.selectedLocation shouldBe "Whitefield"
            }
        }

        `when`("two locations selected in sequence") {
            then("latest location is shown") {
                val vm = createViewModel()
                vm.onLocationSelected("Indiranagar")
                vm.onLocationSelected("HSR Layout")

                vm.uiState.value.selectedLocation shouldBe "HSR Layout"
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
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 5 — Restore Saved Location from Room
    // ══════════════════════════════════════════════════════════════════

    given("user previously saved a location") {

        `when`("Room emits Indiranagar on app start") {
            then("selectedLocation restored to Indiranagar") {
                fakeUserRepo.setUserWithLocation("Indiranagar")
                val vm = createViewModel()

                vm.uiState.value.selectedLocation shouldBe "Indiranagar"
            }
        }

        `when`("Room emits Whitefield on app start") {
            then("selectedLocation restored to Whitefield") {
                fakeUserRepo.setUserWithLocation("Whitefield")
                val vm = createViewModel()

                vm.uiState.value.selectedLocation shouldBe "Whitefield"
            }
        }

        `when`("Room emits saved location but fresh selection already made") {
            then("fresh selection is NOT overwritten") {
                fakeUserRepo.setUserWithLocation("Koramangala")
                val vm = createViewModel()
                vm.onLocationSelected("HSR Layout")

                vm.uiState.value.selectedLocation shouldBe "HSR Layout"
            }
        }

        `when`("saved location is empty string") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUserWithLocation("")
                val vm = createViewModel()

                vm.uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }

        `when`("user is null (not logged in)") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUser(null)
                val vm = createViewModel()

                vm.uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 6 — Saved Addresses
    // ══════════════════════════════════════════════════════════════════

    given("user has saved addresses in profile") {

        `when`("user has Home and Work addresses") {
            then("savedAddresses has 2 items") {
                fakeUserRepo.setUserWithAddresses(
                    FakeUserRepository.fakeHomeAddress(),
                    FakeUserRepository.fakeWorkAddress(),
                )
                val vm = createViewModel()

                vm.uiState.value.savedAddresses.size shouldBe 2
            }

            then("savedAddresses contains Home") {
                fakeUserRepo.setUserWithAddresses(
                    FakeUserRepository.fakeHomeAddress(),
                )
                val vm = createViewModel()

                vm.uiState.value.savedAddresses
                    .any { it.label == "Home" } shouldBe true
            }

            then("savedAddresses contains Work") {
                fakeUserRepo.setUserWithAddresses(
                    FakeUserRepository.fakeWorkAddress(),
                )
                val vm = createViewModel()

                vm.uiState.value.savedAddresses
                    .any { it.label == "Work" } shouldBe true
            }
        }

        `when`("user has no saved addresses") {
            then("savedAddresses is empty") {
                fakeUserRepo.setUser(FakeUserRepository.fakeUser())
                val vm = createViewModel()

                vm.uiState.value.savedAddresses shouldBe emptyList()
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 7 — GPS Success in Service Area
    // ══════════════════════════════════════════════════════════════════

    given("GPS succeeds with Bengaluru address") {

        `when`("GPS returns Koramangala") {
            then("locationFetchState becomes Success") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Koramangala 5th Block, Bengaluru")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Success
            }

            then("selectedLocation updates to GPS address") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Koramangala 5th Block, Bengaluru")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe
                        "Koramangala 5th Block, Bengaluru"
            }

            then("sheet closes automatically") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Koramangala 5th Block, Bengaluru")

                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.showLocationPicker shouldBe false
            }

            then("location saved to UserRepository") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Koramangala 5th Block, Bengaluru")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                fakeUserRepo.saveSelectedLocationCalled shouldBe true
                fakeUserRepo.lastSavedLocation shouldBe
                        "Koramangala 5th Block, Bengaluru"
            }
        }

        `when`("GPS returns Indiranagar") {
            then("selectedLocation updates to Indiranagar address") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("100 Feet Road, Indiranagar, Bengaluru")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe
                        "100 Feet Road, Indiranagar, Bengaluru"
            }
        }

        `when`("GPS returns Whitefield") {
            then("locationFetchState is Success") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("ITPL Main Road, Whitefield, Bengaluru")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Success
            }
        }

        `when`("GPS returns Bangalore (alternate spelling)") {
            then("isInServiceArea returns true — Success") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("MG Road, Bangalore")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Success
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 8 — GPS Outside Service Area
    // ══════════════════════════════════════════════════════════════════

    given("GPS returns address outside Bengaluru") {

        `when`("GPS returns Delhi") {
            then("locationFetchState becomes NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Connaught Place, New Delhi")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.NotInArea
            }

            then("selectedLocation is NOT updated") {
                val locationBefore = createViewModel().uiState.value.selectedLocation

                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Connaught Place, New Delhi")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe locationBefore
            }

            then("sheet stays open for manual pick") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Connaught Place, New Delhi")

                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.showLocationPicker shouldBe true
            }

            then("UserRepository NOT called") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Connaught Place, New Delhi")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                fakeUserRepo.saveSelectedLocationCalled shouldBe false
            }
        }

        `when`("GPS returns Mumbai") {
            then("locationFetchState is NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Bandra, Mumbai")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.NotInArea
            }
        }

        `when`("GPS returns Chennai") {
            then("locationFetchState is NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Anna Nagar, Chennai")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.NotInArea
            }
        }

        `when`("GPS returns Mysore") {
            then("locationFetchState is NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Devaraja Market, Mysore")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.NotInArea
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 9 — GPS Failures
    // ══════════════════════════════════════════════════════════════════

    given("GPS fetch fails") {

        `when`("permission denied error") {
            then("locationFetchState becomes Error") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("permission denied")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg contains permission") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("permission denied")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationErrorMsg shouldContain "permission"
            }

            then("selectedLocation unchanged") {
                val vm            = createViewModel()
                val locationBefore = vm.uiState.value.selectedLocation

                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("permission denied")

                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe locationBefore
            }
        }

        `when`("GPS disabled error") {
            then("locationFetchState becomes Error") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("GPS disabled")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg mentions GPS") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("GPS disabled")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationErrorMsg shouldContain "GPS"
            }
        }

        `when`("unknown error") {
            then("locationFetchState becomes Error") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("unknown crash")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg says pick manually") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("unknown crash")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationErrorMsg shouldContain "manually"
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 10 — Permission Denied from Android Dialog
    // ══════════════════════════════════════════════════════════════════

    given("Android system denies location permission") {

        `when`("onLocationPermissionDenied is called") {
            then("locationFetchState becomes Error") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg is not empty") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()

                vm.uiState.value.locationErrorMsg.shouldNotBeEmpty()
            }

            then("locationErrorMsg contains denied") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()

                vm.uiState.value.locationErrorMsg shouldContain "denied"
            }

            then("selectedLocation unchanged") {
                val vm = createViewModel()
                vm.onLocationSelected("Koramangala")
                vm.onLocationPermissionDenied()

                vm.uiState.value.selectedLocation shouldBe "Koramangala"
            }

            then("showLocationPicker stays open for manual pick") {
                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onLocationPermissionDenied()

                vm.uiState.value.showLocationPicker shouldBe true
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 11 — Service Area Boundary Tests
    // ══════════════════════════════════════════════════════════════════

    given("GPS returns Bengaluru boundary addresses") {

        listOf(
            "BTM Layout, Bengaluru",
            "JP Nagar 3rd Phase, Bengaluru",
            "Marathahalli, Outer Ring Road, Bengaluru",
            "Bellandur, Bengaluru",
            "Sarjapur Road, Bengaluru",
            "Yelahanka, Bengaluru",
            "Hebbal, Bengaluru",
            "Jayanagar, Bengaluru",
            "Malleshwaram, Bengaluru",
            "KR Puram, Bengaluru",
        ).forEach { address ->
            `when`("GPS returns $address") {
                then("isInServiceArea = true → Success") {
                    coEvery { locationManager.getCurrentLocation() } returns
                            gpsSuccess(address)

                    val vm = createViewModel()
                    vm.onUseCurrentLocationTapped()

                    vm.uiState.value.locationFetchState shouldBe
                            HomeViewModel.LocationFetchState.Success
                }
            }
        }

        listOf(
            "Devaraja Market, Mysore",
            "Hampankatta, Mangalore",
            "Connaught Place, New Delhi",
            "Bandra, Mumbai",
            "Anna Nagar, Chennai",
        ).forEach { address ->
            `when`("GPS returns $address") {
                then("isInServiceArea = false → NotInArea") {
                    coEvery { locationManager.getCurrentLocation() } returns
                            gpsSuccess(address)

                    val vm = createViewModel()
                    vm.onUseCurrentLocationTapped()

                    vm.uiState.value.locationFetchState shouldBe
                            HomeViewModel.LocationFetchState.NotInArea
                }
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 12 — State Reset After Error
    // ══════════════════════════════════════════════════════════════════

    given("user encountered an error") {

        `when`("sheet reopened after GPS error") {
            then("error state is cleared") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure("permission denied")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Error

                vm.onLocationBarClicked()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Idle
                vm.uiState.value.locationErrorMsg shouldBe ""
            }
        }

        `when`("sheet dismissed after NotInArea") {
            then("state resets to Idle") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsSuccess("Connaught Place, New Delhi")

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.NotInArea

                vm.onLocationDismissed()

                vm.uiState.value.locationFetchState shouldBe
                        HomeViewModel.LocationFetchState.Idle
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════
    // GROUP 13 — Connectivity
    // ══════════════════════════════════════════════════════════════════

    given("device connectivity changes") {

        `when`("device goes offline") {
            then("isOffline becomes true") {
                val vm = createViewModel()
                networkFlow.value = NetworkStatus.Lost

                vm.uiState.value.isOffline shouldBe true
            }
        }

        `when`("network unavailable") {
            then("isOffline is true") {
                networkFlow.value = NetworkStatus.Unavailable
                val vm = createViewModel()

                vm.uiState.value.isOffline shouldBe true
            }
        }

        `when`("device reconnects") {
            then("isOffline becomes false") {
                val vm = createViewModel()
                networkFlow.value = NetworkStatus.Lost
                networkFlow.value = NetworkStatus.Available

                vm.uiState.value.isOffline shouldBe false
            }
        }
    }
})