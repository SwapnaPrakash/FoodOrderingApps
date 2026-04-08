package com.swapna.foodapp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.Restaurant
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import com.swapna.foodapp.domain.model.Collections
import com.swapna.foodapp.domain.model.FoodCategory
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.utils.ConnectivityObserver
import com.swapna.foodapp.utils.NetworkStatus
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getHomeDataUseCase: GetHomeDataUseCase,
    private val cartRepository: CartRepository,
    private val connectivityObserver: ConnectivityObserver,
) : ViewModel() {

    // ── UI State ──────────────────────────────────────────────
    data class HomeUiState(
        val isLoading: Boolean              = true,
        val collections: List<Collections>   = emptyList(),
        val categories: List<FoodCategory>  = emptyList(),
        val restaurants: List<Restaurant>   = emptyList(),
        val selectedTab: DeliveryTab        = DeliveryTab.DELIVERY,
        val cartItemCount: Int              = 0,
        val userLocation: String            = "Koramangala, Bengaluru",
        val isOffline: Boolean              = false,
        val showLocationPicker: Boolean     = false,
        val error: String?                  = null,
    )

    enum class DeliveryTab { DELIVERY, DINING }

    // ── Events ─────────────────────────────────────────────────
    sealed class HomeEvent {
        data class NavigateToRestaurant(val id: String) : HomeEvent()
        data class NavigateToSearch(val query: String = "") : HomeEvent()
        object NavigateToCart : HomeEvent()
    }

    // ── StateFlow ─────────────────────────────────────────────
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // ── SharedFlow ────────────────────────────────────────────
    private val _events = MutableSharedFlow<HomeEvent>(
        replay              = 0,
        extraBufferCapacity = 1,
    )
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    // ── Init ──────────────────────────────────────────────────
    init {
        loadHomeData()
        observeCartCount()
        observeConnectivity()
    }

    // ── Load Home Data ────────────────────────────────────────
    fun loadHomeData() = viewModelScope.launch {
        _uiState.update { it.copy(isLoading = true, error = null) }

        getHomeDataUseCase().collect { result ->
            result.fold(
                onSuccess = { data ->
                    _uiState.update {
                        it.copy(
                            isLoading   = false,
                            collections = data.collections,
                            categories  = data.categories,
                            restaurants = data.restaurants,
                            error       = null,
                        )
                    }
                },
                onFailure = { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error     = throwable.message ?: "Something went wrong",
                        )
                    }
                },
            )
        }
    }

    // ── Observe Cart Count ────────────────────────────────────
    private fun observeCartCount() = viewModelScope.launch {
        cartRepository.getCartItemCount().collect { count ->
            _uiState.update { it.copy(cartItemCount = count) }
        }
    }

    // ── Observe Connectivity ──────────────────────────────────
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

    // ── Location Actions ──────────────────────────────────────
    fun onLocationClicked() {
        _uiState.update { it.copy(showLocationPicker = true) }
    }

    fun onLocationDismissed() {
        _uiState.update { it.copy(showLocationPicker = false) }
    }

    fun onLocationSelected(location: String) {
        _uiState.update {
            it.copy(
                userLocation      = location,
                showLocationPicker = false,
            )
        }
        // Reload with new location
        loadHomeData()
    }

    // ── Navigation Actions ────────────────────────────────────
    fun onTabSelected(tab: DeliveryTab) {
        _uiState.update { it.copy(selectedTab = tab) }
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
}