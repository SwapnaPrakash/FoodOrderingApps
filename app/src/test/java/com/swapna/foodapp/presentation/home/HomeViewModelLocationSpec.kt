package com.swapna.foodapp.presentation.home

import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.utils.ConnectivityObserver
import com.swapna.foodapp.utils.HomeData
import com.swapna.foodapp.utils.NetworkStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
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
class HomeViewModelLocationSpec : BehaviorSpec({

    val getHomeDataUseCase = mockk<GetHomeDataUseCase>()
    val cartRepository = mockk<CartRepository>()
    val connectivityObserver = mockk<ConnectivityObserver>()
    val userRepository = mockk<UserRepository>()
    val networkStatusFlow = MutableStateFlow<NetworkStatus>(NetworkStatus.Available)

    fun createViewModel() = HomeViewModel(
        getHomeDataUseCase = getHomeDataUseCase,
        cartRepository = cartRepository,
        connectivityObserver = connectivityObserver,
        userRepository = userRepository,
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())
        every { cartRepository.getCartItemCount() } returns flowOf(0)
        every { connectivityObserver.networkStatus } returns networkStatusFlow
        every { getHomeDataUseCase() } returns flowOf(Result.success(HomeData()))
    }

    afterEach { Dispatchers.resetMain() }

    // GIVEN: Location picker
    given("location picker is closed by default") {
        `when`("ViewModel is created") {
            then("showLocationPicker should be false") {
                createViewModel().uiState.value.showLocationPicker shouldBe false
            }
        }
    }

    given("user taps location in top bar") {
        `when`("onLocationClicked is called") {
            then("showLocationPicker should become true") {
                val vm = createViewModel()
                vm.onLocationClicked()
                vm.uiState.value.showLocationPicker shouldBe true
            }
        }
    }

    given("location picker is open") {
        `when`("onLocationDismissed is called") {
            then("showLocationPicker should become false") {
                val vm = createViewModel()
                vm.onLocationClicked()
                vm.onLocationDismissed()
                vm.uiState.value.showLocationPicker shouldBe false
            }
        }

        `when`("user selects 'Indiranagar, Bengaluru'") {
            then("userLocation should update and picker should close") {
                val vm = createViewModel()
                vm.onLocationClicked()
                vm.onLocationSelected("Indiranagar, Bengaluru")

                vm.uiState.value.userLocation shouldBe "Indiranagar, Bengaluru"
                vm.uiState.value.showLocationPicker shouldBe false
            }
        }
    }

    // GIVEN: Connectivity changes
    given("device is online") {
        `when`("ViewModel is created") {
            then("isOffline should be false") {
                networkStatusFlow.value = NetworkStatus.Available
                createViewModel().uiState.value.isOffline shouldBe false
            }
        }
    }

    given("device loses internet connection") {
        `when`("network status changes to Lost") {
            then("isOffline should become true") {
                val vm = createViewModel()
                networkStatusFlow.value = NetworkStatus.Lost
                vm.uiState.value.isOffline shouldBe true
            }
        }
    }

    given("device is offline") {
        `when`("network status changes to Unavailable") {
            then("isOffline should be true") {
                networkStatusFlow.value = NetworkStatus.Unavailable
                createViewModel().uiState.value.isOffline shouldBe true
            }
        }
    }

    given("device reconnects after being offline") {
        `when`("network status changes from Lost to Available") {
            then("isOffline should become false") {
                networkStatusFlow.value = NetworkStatus.Lost
                val vm = createViewModel()
                vm.uiState.value.isOffline shouldBe true

                networkStatusFlow.value = NetworkStatus.Available
                vm.uiState.value.isOffline shouldBe false
            }
        }
    }

    // GIVEN: Default location
    given("app just launched") {
        `when`("ViewModel is created") {
            then("default location should be Koramangala, Bengaluru") {
                createViewModel().uiState.value.userLocation shouldBe
                        "Koramangala, Bengaluru"
            }
        }
    }

    given("user selects location then selects another") {
        `when`("two locations selected in sequence") {
            then("latest location should be shown") {
                val vm = createViewModel()
                vm.onLocationSelected("Indiranagar, Bengaluru")
                vm.onLocationSelected("HSR Layout, Bengaluru")
                vm.uiState.value.userLocation shouldBe "HSR Layout, Bengaluru"
            }
        }
    }
})