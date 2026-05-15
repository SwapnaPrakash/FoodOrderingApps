package com.swapna.foodapp.presentation.home

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
import com.swapna.foodapp.utils.TestConstants.ERR_GPS_DISABLED_MSG
import com.swapna.foodapp.utils.TestConstants.ERR_PERMISSION_DENIED_GPS
import com.swapna.foodapp.utils.TestConstants.ERR_UNKNOWN_CRASH
import com.swapna.foodapp.utils.TestConstants.GPS_BANGALORE
import com.swapna.foodapp.utils.TestConstants.GPS_BELLANDUR
import com.swapna.foodapp.utils.TestConstants.GPS_BTM
import com.swapna.foodapp.utils.TestConstants.GPS_CHENNAI
import com.swapna.foodapp.utils.TestConstants.GPS_DELHI
import com.swapna.foodapp.utils.TestConstants.GPS_HEBBAL
import com.swapna.foodapp.utils.TestConstants.GPS_INDIRANAGAR
import com.swapna.foodapp.utils.TestConstants.GPS_JAYANAGAR
import com.swapna.foodapp.utils.TestConstants.GPS_JP_NAGAR
import com.swapna.foodapp.utils.TestConstants.GPS_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.GPS_KR_PURAM
import com.swapna.foodapp.utils.TestConstants.GPS_LAT
import com.swapna.foodapp.utils.TestConstants.GPS_LNG
import com.swapna.foodapp.utils.TestConstants.GPS_MALLESHWARAM
import com.swapna.foodapp.utils.TestConstants.GPS_MANGALORE
import com.swapna.foodapp.utils.TestConstants.GPS_MARATHAHALLI
import com.swapna.foodapp.utils.TestConstants.GPS_MUMBAI
import com.swapna.foodapp.utils.TestConstants.GPS_MYSORE
import com.swapna.foodapp.utils.TestConstants.GPS_SARJAPUR
import com.swapna.foodapp.utils.TestConstants.GPS_WHITEFIELD
import com.swapna.foodapp.utils.TestConstants.GPS_YELAHANKA
import com.swapna.foodapp.utils.TestConstants.LOC_INDIRANAGAR
import com.swapna.foodapp.utils.TestConstants.LOC_KEYWORD_DENIED
import com.swapna.foodapp.utils.TestConstants.LOC_KEYWORD_GPS
import com.swapna.foodapp.utils.TestConstants.LOC_KEYWORD_MANUALLY
import com.swapna.foodapp.utils.TestConstants.LOC_KEYWORD_PERMISSION
import com.swapna.foodapp.utils.TestConstants.LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.LOC_SAVE_HSR
import com.swapna.foodapp.utils.TestConstants.LOC_WHITEFIELD_FULL
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

    val getHomeDataUseCase = mockk<GetHomeDataUseCase>()
    val connectivityObserver = mockk<ConnectivityObserver>()
    val locationManager = mockk<LocationManager>()

    lateinit var fakeCartRepo: FakeCartRepository
    lateinit var fakeUserRepo: FakeUserRepository

    val networkFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    fun emptyHomeData() = HomeData(
        restaurants = emptyList(),
        collections = emptyList(),
        categories = emptyList(),
        filterStatus = FilterStatus.NO_FILTER,
        requestedArea = "",
        availableAreas = emptyList(),
    )

    fun gpsSuccess(address: String) = Result.success(
        CurrentLocationResult(
            displayAddress = address,
            latitude = GPS_LAT,
            longitude = GPS_LNG,
        )
    )

    fun gpsFailure(message: String) =
        Result.failure<CurrentLocationResult>(Exception(message))

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
                flowOf(Result.success(emptyHomeData()))
        coEvery { locationManager.getCurrentLocation() } returns
                gpsSuccess(GPS_KORAMANGALA)
    }

    afterEach { Dispatchers.resetMain() }

    // GROUP 1 — Location Picker Default State
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
                createViewModel().uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
            then("savedAddresses is empty") {
                createViewModel().uiState.value.savedAddresses shouldBe emptyList()
            }
        }
    }

    // GROUP 2 — Open Location Sheet
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
                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Idle
            }
            then("locationErrorMsg is cleared") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.onLocationBarClicked()
                vm.uiState.value.locationErrorMsg shouldBe ""
            }
            then("selectedLocation is unchanged after open") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_KORAMANGALA)
                vm.onLocationBarClicked()
                vm.uiState.value.selectedLocation shouldBe LOC_KORAMANGALA
            }
        }
    }

    // GROUP 3 — Dismiss Location Sheet
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
                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Idle
            }
            then("locationErrorMsg is cleared on dismiss") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.onLocationDismissed()
                vm.uiState.value.locationErrorMsg shouldBe ""
            }
            then("selectedLocation unchanged after dismiss") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_INDIRANAGAR)
                vm.onLocationBarClicked()
                vm.onLocationDismissed()
                vm.uiState.value.selectedLocation shouldBe LOC_INDIRANAGAR
            }
        }
    }

    // GROUP 4 — Manual Location Selection
    given("user picks location from popular list") {

        `when`("Koramangala selected") {
            then("selectedLocation is Koramangala") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_KORAMANGALA)
                vm.uiState.value.selectedLocation shouldBe LOC_KORAMANGALA
            }
        }

        `when`("HSR Layout selected") {
            then("selectedLocation is HSR Layout") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_SAVE_HSR)
                vm.uiState.value.selectedLocation shouldBe LOC_SAVE_HSR
            }
        }

        `when`("Whitefield selected") {
            then("selectedLocation is Whitefield") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_WHITEFIELD_FULL)
                vm.uiState.value.selectedLocation shouldBe LOC_WHITEFIELD_FULL
            }
        }

        `when`("two locations selected in sequence") {
            then("latest location is shown") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_INDIRANAGAR)
                vm.onLocationSelected(LOC_SAVE_HSR)
                vm.uiState.value.selectedLocation shouldBe LOC_SAVE_HSR
            }
        }

        `when`("any location is selected") {
            then("saveSelectedLocation called in UserRepository") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_WHITEFIELD_FULL)

                fakeUserRepo.saveSelectedLocationCalled shouldBe true
                fakeUserRepo.lastSavedLocation shouldBe LOC_WHITEFIELD_FULL
            }

            then("showLocationPicker closes") {
                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onLocationSelected(LOC_KORAMANGALA)
                vm.uiState.value.showLocationPicker shouldBe false
            }
        }
    }

    // GROUP 5 — Restore Saved Location from Room
    given("user previously saved a location") {

        `when`("Room emits Indiranagar on app start") {
            then("selectedLocation restored to Indiranagar") {
                fakeUserRepo.setUserWithLocation(LOC_INDIRANAGAR)
                createViewModel().uiState.value.selectedLocation shouldBe LOC_INDIRANAGAR
            }
        }

        `when`("Room emits Whitefield on app start") {
            then("selectedLocation restored to Whitefield") {
                fakeUserRepo.setUserWithLocation(LOC_WHITEFIELD_FULL)
                createViewModel().uiState.value.selectedLocation shouldBe LOC_WHITEFIELD_FULL
            }
        }

        `when`("Room emits saved location but fresh selection already made") {
            then("fresh selection is NOT overwritten") {
                fakeUserRepo.setUserWithLocation(LOC_KORAMANGALA)
                val vm = createViewModel()
                vm.onLocationSelected(LOC_SAVE_HSR)
                vm.uiState.value.selectedLocation shouldBe LOC_SAVE_HSR
            }
        }

        `when`("saved location is empty string") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUserWithLocation("")
                createViewModel().uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }

        `when`("user is null (not logged in)") {
            then("selectedLocation stays DEFAULT_LOCATION") {
                fakeUserRepo.setUser(null)
                createViewModel().uiState.value.selectedLocation shouldBe DEFAULT_LOCATION
            }
        }
    }

    // GROUP 6 — Saved Addresses
    given("user has saved addresses in profile") {

        `when`("user has Home and Work addresses") {
            then("savedAddresses has 2 items") {
                fakeUserRepo.setUserWithAddresses(
                    FakeUserRepository.fakeHomeAddress(),
                    FakeUserRepository.fakeWorkAddress(),
                )
                createViewModel().uiState.value.savedAddresses.size shouldBe 2
            }
            then("savedAddresses contains Home") {
                fakeUserRepo.setUserWithAddresses(FakeUserRepository.fakeHomeAddress())
                createViewModel().uiState.value.savedAddresses
                    .any { it.label == "Home" } shouldBe true
            }
            then("savedAddresses contains Work") {
                fakeUserRepo.setUserWithAddresses(FakeUserRepository.fakeWorkAddress())
                createViewModel().uiState.value.savedAddresses
                    .any { it.label == "Work" } shouldBe true
            }
        }

        `when`("user has no saved addresses") {
            then("savedAddresses is empty") {
                fakeUserRepo.setUser(FakeUserRepository.fakeUser())
                createViewModel().uiState.value.savedAddresses shouldBe emptyList()
            }
        }
    }

    // GROUP 7 — GPS Success in Service Area
    given("GPS succeeds with Bengaluru address") {

        `when`("GPS returns Koramangala") {
            then("locationFetchState becomes Success") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_KORAMANGALA)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Success
            }

            then("selectedLocation updates to GPS address") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_KORAMANGALA)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe GPS_KORAMANGALA
            }

            then("sheet closes automatically") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_KORAMANGALA)

                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.showLocationPicker shouldBe false
            }

            then("location saved to UserRepository") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_KORAMANGALA)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                fakeUserRepo.saveSelectedLocationCalled shouldBe true
                fakeUserRepo.lastSavedLocation shouldBe GPS_KORAMANGALA
            }
        }

        `when`("GPS returns Indiranagar") {
            then("selectedLocation updates to Indiranagar address") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_INDIRANAGAR)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe GPS_INDIRANAGAR
            }
        }

        `when`("GPS returns Whitefield") {
            then("locationFetchState is Success") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_WHITEFIELD)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Success
            }
        }

        `when`("GPS returns Bangalore (alternate spelling)") {
            then("isInServiceArea returns true — Success") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_BANGALORE)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Success
            }
        }
    }

    // GROUP 8 — GPS Outside Service Area
    given("GPS returns address outside Bengaluru") {

        `when`("GPS returns Delhi") {
            then("locationFetchState becomes NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_DELHI)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.NotInArea
            }

            then("selectedLocation is NOT updated") {
                val locationBefore = createViewModel().uiState.value.selectedLocation

                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_DELHI)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe locationBefore
            }

            then("sheet stays open for manual pick") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_DELHI)

                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.showLocationPicker shouldBe true
            }

            then("UserRepository NOT called") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_DELHI)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                fakeUserRepo.saveSelectedLocationCalled shouldBe false
            }
        }

        `when`("GPS returns Mumbai") {
            then("locationFetchState is NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_MUMBAI)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.NotInArea
            }
        }

        `when`("GPS returns Chennai") {
            then("locationFetchState is NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_CHENNAI)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.NotInArea
            }
        }

        `when`("GPS returns Mysore") {
            then("locationFetchState is NotInArea") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_MYSORE)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.NotInArea
            }
        }
    }

    // GROUP 9 — GPS Failures
    given("GPS fetch fails") {

        `when`("permission denied error") {
            then("locationFetchState becomes Error") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_PERMISSION_DENIED_GPS)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg contains permission") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_PERMISSION_DENIED_GPS)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationErrorMsg shouldContain LOC_KEYWORD_PERMISSION
            }

            then("selectedLocation unchanged") {
                val vm = createViewModel()
                val locationBefore = vm.uiState.value.selectedLocation

                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_PERMISSION_DENIED_GPS)

                vm.onUseCurrentLocationTapped()

                vm.uiState.value.selectedLocation shouldBe locationBefore
            }
        }

        `when`("GPS disabled error") {
            then("locationFetchState becomes Error") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_GPS_DISABLED_MSG)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg mentions GPS") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_GPS_DISABLED_MSG)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationErrorMsg shouldContain LOC_KEYWORD_GPS
            }
        }

        `when`("unknown error") {
            then("locationFetchState becomes Error") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_UNKNOWN_CRASH)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg says pick manually") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_UNKNOWN_CRASH)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationErrorMsg shouldContain LOC_KEYWORD_MANUALLY
            }
        }
    }

    // GROUP 10 — Permission Denied from Android Dialog
    given("Android system denies location permission") {

        `when`("onLocationPermissionDenied is called") {
            then("locationFetchState becomes Error") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Error
            }

            then("locationErrorMsg is not empty") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.uiState.value.locationErrorMsg.shouldNotBeEmpty()
            }

            then("locationErrorMsg contains denied") {
                val vm = createViewModel()
                vm.onLocationPermissionDenied()
                vm.uiState.value.locationErrorMsg shouldContain LOC_KEYWORD_DENIED
            }

            then("selectedLocation unchanged") {
                val vm = createViewModel()
                vm.onLocationSelected(LOC_KORAMANGALA)
                vm.onLocationPermissionDenied()
                vm.uiState.value.selectedLocation shouldBe LOC_KORAMANGALA
            }

            then("showLocationPicker stays open for manual pick") {
                val vm = createViewModel()
                vm.onLocationBarClicked()
                vm.onLocationPermissionDenied()
                vm.uiState.value.showLocationPicker shouldBe true
            }
        }
    }

    // GROUP 11 — Service Area Boundary Tests
    given("GPS returns Bengaluru boundary addresses") {

        listOf(
            GPS_BTM,
            GPS_JP_NAGAR,
            GPS_MARATHAHALLI,
            GPS_BELLANDUR,
            GPS_SARJAPUR,
            GPS_YELAHANKA,
            GPS_HEBBAL,
            GPS_JAYANAGAR,
            GPS_MALLESHWARAM,
            GPS_KR_PURAM,
        ).forEach { address ->
            `when`("GPS returns $address") {
                then("isInServiceArea = true → Success") {
                    coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(address)
                    val vm = createViewModel()
                    vm.onUseCurrentLocationTapped()
                    vm.uiState.value.locationFetchState shouldBe
                            HomeViewModel.LocationFetchState.Success
                }
            }
        }

        listOf(
            GPS_MYSORE,
            GPS_MANGALORE,
            GPS_DELHI,
            GPS_MUMBAI,
            GPS_CHENNAI,
        ).forEach { address ->
            `when`("GPS returns $address") {
                then("isInServiceArea = false → NotInArea") {
                    coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(address)
                    val vm = createViewModel()
                    vm.onUseCurrentLocationTapped()
                    vm.uiState.value.locationFetchState shouldBe
                            HomeViewModel.LocationFetchState.NotInArea
                }
            }
        }
    }

    // GROUP 12 — State Reset After Error
    given("user encountered an error") {

        `when`("sheet reopened after GPS error") {
            then("error state is cleared") {
                coEvery { locationManager.getCurrentLocation() } returns
                        gpsFailure(ERR_PERMISSION_DENIED_GPS)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Error

                vm.onLocationBarClicked()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Idle
                vm.uiState.value.locationErrorMsg shouldBe ""
            }
        }

        `when`("sheet dismissed after NotInArea") {
            then("state resets to Idle") {
                coEvery { locationManager.getCurrentLocation() } returns gpsSuccess(GPS_DELHI)

                val vm = createViewModel()
                vm.onUseCurrentLocationTapped()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.NotInArea

                vm.onLocationDismissed()

                vm.uiState.value.locationFetchState shouldBe HomeViewModel.LocationFetchState.Idle
            }
        }
    }

    // GROUP 13 — Connectivity
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
                createViewModel().uiState.value.isOffline shouldBe true
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