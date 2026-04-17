package com.swapna.foodapp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.domain.usecase.home.FilterStatus
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.utils.AppConstants.DEFAULT_LOCATION
import com.swapna.foodapp.utils.AppConstants.WRONG
import com.swapna.foodapp.utils.ConnectivityObserver
import com.swapna.foodapp.utils.EVENT_BUFFER_DEFAULT
import com.swapna.foodapp.utils.NetworkStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val cartRepository: CartRepository,
    private val userRepository:       UserRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>(
        replay              = 0,
        extraBufferCapacity = EVENT_BUFFER_DEFAULT,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        loadHomeData()
        observeCartCount()
        observeConnectivity()
        observeUserProfile()
        loadSavedLocation()
    }

    private fun loadSavedLocation() = viewModelScope.launch {
        userRepository.getCurrentUser()
            .collect { user ->
                val saved = user?.selectedLocation.orEmpty()
                if (saved.isNotEmpty()) {
                    _uiState.update { it.copy(selectedLocation = saved) }
                }
            }
    }
// Add these methods to HomeViewModel:

    // ✅ Called after GPS returns coordinates
// LocationPickerSheet triggers this via callback
    fun onCurrentLocationDetected(
        locality: String,
        address:  String,
    ) {
        // Show detected locality in TopBar
        // "Koramangala" not full address
        _uiState.update {
            it.copy(
                userLocation      = locality,
                showLocationPicker = false,
            )
        }

        // Save to Room
        viewModelScope.launch {
            userRepository.saveSelectedLocation(locality)
        }

        // Reload restaurants for detected locality
        loadHomeData()
    }

    // ✅ Called when user denies location permission
    fun onLocationPermissionDenied() {
        _uiState.update {
            it.copy(showLocationPicker = false)
        }
    }

    // Load Home Data
    fun loadHomeData() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }
        val currentLocation = _uiState.value.userLocation
        getHomeDataUseCase(
            selectedLocation = currentLocation,
        ).collect { result ->
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            collections = data.collections,
                            categories = data.categories,
                            restaurants = data.restaurants,
                            error = null,
                            filterStatus   = data.filterStatus,
                            requestedArea  = data.requestedArea,
                            availableAreas = data.availableAreas,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = throwable.message ?: WRONG,
                        )
                    }
                },
            )
        }
    }

    // Observe Cart Count
    private fun observeCartCount() = viewModelScope.launch {
        cartRepository.getCartItemCount().collect { count ->
            _uiState.update { it.copy(cartItemCount = count) }
        }
    }

    // Observe Connectivity
    // Shows offline banner when device loses internet
    // Triggers reload when connection is restored
    private fun observeConnectivity() = viewModelScope.launch {
        var wasOffline = false

        connectivityObserver.networkStatus.collect { status ->
            val isOffline = status != NetworkStatus.Available
            _uiState.update { it.copy(isOffline = isOffline) }

            // Auto-reload when coming back online
            if (wasOffline && !isOffline) {
                loadHomeData()
            }
            wasOffline = isOffline
        }
    }

    // Location Actions
    fun onLocationClicked() {
        _uiState.update { it.copy(showLocationPicker = true) }
    }

    fun onLocationDismissed() {
        _uiState.update { it.copy(showLocationPicker = false) }
    }

    fun onLocationSelected(location: String) {
        viewModelScope.launch {
            // 1. Update TopBar immediately (optimistic UI)
            _uiState.update { it.copy(selectedLocation = location) }

            // 2. Save to UserRepository (persists across sessions)
            userRepository.saveSelectedLocation(location)
        }
    }

    private fun observeUserProfile() = viewModelScope.launch {
        userRepository.getCurrentUser().collect { user ->
            if (user != null) {
                val savedLocation = user.selectedLocation

                _uiState.update { state ->
                    state.copy(
                        savedAddresses = user.addresses,
                        userLocation   = savedLocation
                            .ifEmpty { state.userLocation },
                    )
                }

                // Reload restaurants with restored location
                if (savedLocation.isNotEmpty()) {
                    loadHomeData()
                }
            }
        }
    }

    // Navigation Actions
    fun onTabSelected1(tab: DeliveryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }
    // ── Tab selected ──────────────────────────────────────────────
    fun onTabSelected(tab: DeliveryTab) {
        when (tab) {
            DeliveryTab.PROFILE -> {
                viewModelScope.launch {
                    _events.emit(HomeEvent.NavigateToProfile)
                }
            }
            // DELIVERY + DINING → update tab only
            else -> {
                _uiState.update {
                    it.copy(selectedTab = tab)
                }
            }
        }
    }

    fun onProfileClicked() {
        viewModelScope.launch {
            _events.emit(HomeEvent.NavigateToProfile)
        }
    }

    fun onRestaurantClicked(id: String) = viewModelScope.launch {
        _events.emit(HomeEvent.NavigateToRestaurant(id))
    }

    fun onSearchClicked(query: String = "") = viewModelScope.launch {
        _events.emit(HomeEvent.NavigateToSearch(query))
    }

    fun onCartClicked() = viewModelScope.launch {
        _events.emit(HomeEvent.NavigateToCart)
    }

    fun onCategoryClicked(categoryName: String) = viewModelScope.launch {
        _events.emit(HomeEvent.NavigateToSearch(categoryName))
    }

    fun retry() = loadHomeData()

    private fun observeUserAddresses() = viewModelScope.launch {
        userRepository.getCurrentUser().collect { user ->
            _uiState.update {
                it.copy(savedAddresses = user?.addresses ?: emptyList())
            }
        }
    }



    // UI State
    data class HomeUiState(
        val isLoading: Boolean = true,
        val collections: List<Collections> = emptyList(),
        val categories: List<FoodCategory> = emptyList(),
        val restaurants: List<Restaurant> = emptyList(),
        val selectedTab: DeliveryTab = DeliveryTab.DELIVERY,
        val cartItemCount: Int = 0,
        val userLocation: String = DEFAULT_LOCATION,
        val isOffline: Boolean = false,
        val showLocationPicker: Boolean = false,
        val error: String? = null,
        val savedAddresses:    List<Address>     = emptyList(),
        val filterStatus:       FilterStatus       = FilterStatus.NO_FILTER,
        val requestedArea:      String             = "",
        val availableAreas:     List<String>       = emptyList(),
        val selectedLocation: String = "",
    )

    enum class DeliveryTab { DELIVERY, DINING,PROFILE }

    // Events
    sealed class HomeEvent {
        data class NavigateToRestaurant(val id: String) : HomeEvent()
        data class NavigateToSearch(val query: String = "") : HomeEvent()
        object NavigateToCart : HomeEvent()
        object NavigateToProfile : HomeEvent()
    }
}