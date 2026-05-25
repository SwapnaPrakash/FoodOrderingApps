package com.swapna.foodapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.RestaurantCollection
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.presentation.common.ConnectivityObserver
import com.swapna.foodapp.presentation.common.LocationManager
import com.swapna.foodapp.presentation.common.NetworkStatus
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.DEFAULT_LOCATION
import com.swapna.foodapp.utils.AppConstants.DISABLED
import com.swapna.foodapp.utils.AppConstants.ERR_GPS_DISABLED
import com.swapna.foodapp.utils.AppConstants.ERR_LOCATION_DETECT
import com.swapna.foodapp.utils.AppConstants.ERR_LOCATION_PERMISSION
import com.swapna.foodapp.utils.AppConstants.ERR_LOCATION_PERMISSION_DENIED
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_NAVIGATION
import com.swapna.foodapp.utils.AppConstants.FAILED_TO_SAVE_LOCATION
import com.swapna.foodapp.utils.AppConstants.OBSERVER_FAILED
import com.swapna.foodapp.utils.AppConstants.PERMISSION
import com.swapna.foodapp.utils.AppConstants.WRONG
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import kotlin.coroutines.cancellation.CancellationException

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val cartRepository: CartRepository,
    private val userRepository: UserRepository,
    private val connectivityObserver: ConnectivityObserver,
    private val locationManager: LocationManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    enum class LocationFetchState { Idle, Fetching, Success, Error, NotInArea }
    enum class DeliveryTab { DELIVERY, DINING, PROFILE }

    data class HomeUiState(
        val isLoading: Boolean = true,
        val error: String? = null,
        val collections: List<RestaurantCollection> = emptyList(),
        val categories: List<FoodCategory> = emptyList(),
        val restaurants: List<Restaurant> = emptyList(),
        val cartItemCount: Int = 0,
        val isOffline: Boolean = false,
        val selectedTab: DeliveryTab = DeliveryTab.DELIVERY,
        val filterStatus: FilterStatus = FilterStatus.NO_FILTER,
        val requestedArea: String = "",
        val availableAreas: List<String> = emptyList(),
        val selectedLocation: String = DEFAULT_LOCATION,
        val savedAddresses: List<Address> = emptyList(),
        val showLocationPicker: Boolean = false,
        val locationFetchState: LocationFetchState = LocationFetchState.Idle,
        val locationErrorMsg: String = "",
    )

    sealed class HomeEvent {
        data class NavigateToRestaurant(val id: String) : HomeEvent()
        data class NavigateToSearch(val query: String = "") : HomeEvent()
        object NavigateToCart : HomeEvent()
        object NavigateToProfile : HomeEvent()
    }

    private val observerExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, OBSERVER_FAILED)
        }

    init {
        observeUserProfile()
        loadHomeData()
        observeCartCount()
        observeConnectivity()
    }

    private fun observeUserProfile() = viewModelScope.launch(observerExceptionHandler) {
        userRepository.getCurrentUser().collect { user ->
            _uiState.update { state ->
                state.copy(
                    savedAddresses = user?.addresses ?: emptyList(),
                    selectedLocation = if (
                        state.selectedLocation == DEFAULT_LOCATION &&
                        user?.selectedLocation.orEmpty().isNotEmpty()
                    ) user!!.selectedLocation
                    else state.selectedLocation,
                )
            }
        }
    }

    fun loadHomeData() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        getHomeDataUseCase(selectedLocation = _uiState.value.selectedLocation)
            .collect { result ->
                result.fold(
                    onSuccess = { data ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                collections = data.collections,
                                categories = data.categories,
                                restaurants = data.restaurants,
                                error = null,
                                filterStatus = data.filterStatus,
                                requestedArea = data.requestedArea,
                                availableAreas = data.availableAreas,
                            )
                        }
                    },
                    onFailure = { throwable ->
                        if (throwable is CancellationException)
                            throw throwable
                        _uiState.update {
                            it.copy(isLoading = false, error = throwable.message ?: WRONG)
                        }
                    },
                )
            }
    }

    private fun observeCartCount() = viewModelScope.launch(observerExceptionHandler) {
        cartRepository.getCartItemCount().collect { count ->
            _uiState.update { it.copy(cartItemCount = count) }
        }
    }

    private fun observeConnectivity() = viewModelScope.launch(observerExceptionHandler) {
        var wasOffline = false
        connectivityObserver.networkStatus.collect { status ->
            val isOffline = status != NetworkStatus.Available
            _uiState.update { it.copy(isOffline = isOffline) }
            if (wasOffline && !isOffline) loadHomeData()
            wasOffline = isOffline
        }
    }

    fun onLocationBarClicked() {
        _uiState.update {
            it.copy(
                showLocationPicker = true,
                locationFetchState = LocationFetchState.Idle,
                locationErrorMsg = "",
            )
        }
    }

    fun onLocationDismissed() {
        _uiState.update {
            it.copy(
                showLocationPicker = false,
                locationFetchState = LocationFetchState.Idle,
                locationErrorMsg = "",
            )
        }
    }

    fun onUseCurrentLocationTapped() = viewModelScope.launch {
        _uiState.update { it.copy(locationFetchState = LocationFetchState.Fetching) }
        locationManager.getCurrentLocation()
            .onSuccess { locationResult ->
                if (!isInServiceArea(locationResult.displayAddress)) {
                    _uiState.update { it.copy(locationFetchState = LocationFetchState.NotInArea) }
                    return@launch
                }
                _uiState.update { it.copy(selectedLocation = locationResult.displayAddress) }
                try {
                    userRepository.saveSelectedLocation(locationResult.displayAddress)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Timber.e(e, "$FAILED_TO_SAVE_LOCATION ${locationResult.displayAddress}")
                }
                loadHomeData()
                _uiState.update {
                    it.copy(
                        locationFetchState = LocationFetchState.Success,
                        showLocationPicker = false,
                    )
                }
            }
            .onFailure { error ->
                if (error is CancellationException) throw error
                val msg = when {
                    error.message?.contains(PERMISSION, true) == true -> ERR_LOCATION_PERMISSION
                    error.message?.contains(DISABLED, true) == true -> ERR_GPS_DISABLED
                    else -> ERR_LOCATION_DETECT
                }
                _uiState.update {
                    it.copy(
                        locationFetchState = LocationFetchState.Error,
                        locationErrorMsg = msg,
                    )
                }
            }
    }

    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(
                locationFetchState = LocationFetchState.Error,
                locationErrorMsg = ERR_LOCATION_PERMISSION_DENIED,
            )
        }
    }

    fun onLocationSelected(location: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedLocation = location) }
            try {
                userRepository.saveSelectedLocation(location)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                Timber.e(exception, "$FAILED_TO_SAVE_LOCATION $location")
            }
            loadHomeData()
            _uiState.update { it.copy(showLocationPicker = false) }
        }
    }

    private fun saveAndApplyLocation(location: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedLocation = location) }
            try {
                userRepository.saveSelectedLocation(location)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Timber.e(e, "$FAILED_TO_SAVE_LOCATION $location")
            }
            loadHomeData()
        }
    }

    private fun isInServiceArea(address: String): Boolean {
        val keywords = AppConstants.LOCATION_KEYWORDS
        return keywords.any { address.lowercase().contains(it) }
    }

    fun onTabSelected(tab: DeliveryTab) {
        when (tab) {
            DeliveryTab.PROFILE ->
                viewModelScope.launch { _events.emit(HomeEvent.NavigateToProfile) }

            else ->
                _uiState.update { it.copy(selectedTab = tab) }
        }
    }

    fun onProfileClicked() = viewModelScope.launch {
        _events.emit(HomeEvent.NavigateToProfile)
    }

    fun onRestaurantClicked(id: String) =
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToRestaurant(id))
        }

    fun onSearchClicked(query: String = "") =
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToSearch(query))
        }

    fun onCartClicked() = viewModelScope.launch {
        _events.emit(HomeEvent.NavigateToCart)
    }

    fun onCategoryClicked(name: String) =
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToSearch(name))
        }

    fun retry() = loadHomeData()
}