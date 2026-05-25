package com.swapna.foodapp.presentation.restaurant

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.Review
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase
import com.swapna.foodapp.presentation.navigation.AppRoutes
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.AppBusinessRules.FREE_DELIVERY_FEE
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.ARG_RESTAURANT_ID_MISSING
import com.swapna.foodapp.utils.AppConstants.ERR_COULD_NOT_ADD_CART
import com.swapna.foodapp.utils.AppConstants.ERR_COULD_NOT_LOAD_RESTAURANT
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_ADD_ITEM
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_UPDATE_CART
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_NAVIGATION
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_UI
import com.swapna.foodapp.utils.AppConstants.OBSERVER_FAILED
import com.swapna.foodapp.utils.AppConstants.RESTAURANT_MERGE_FAILED
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val restaurantRepository: RestaurantRepository,
    private val cartRepository: CartRepository,
    private val addToCartUseCase: AddToCartUseCase,
) : ViewModel() {

    val restaurantId: String =
        checkNotNull(savedStateHandle[AppRoutes.ARG_RESTAURANT_ID]) {
            ARG_RESTAURANT_ID_MISSING
        }

    data class RestaurantUiState(
        val restaurant: Restaurant? = null,
        val menuByCategory: Map<String, List<MenuItem>> = emptyMap(),
        val recommended: List<MenuItem> = emptyList(),
        val reviews: List<Review> = emptyList(),
        val cartItemCount: Int = 0,
        val cartTotal: Double = 0.0,
        val isLoading: Boolean = true,
        val isMenuLoading: Boolean = true,
        val error: String? = null,
        val selectedTab: MenuTab = MenuTab.MENU,
    )

    enum class MenuTab { MENU, REVIEWS }

    sealed class RestaurantEvent {
        data class NavigateToProduct(val itemId: String) : RestaurantEvent()
        object NavigateToCart : RestaurantEvent()
        object NavigateBack : RestaurantEvent()
        data class ItemAdded(val itemName: String) : RestaurantEvent()
        data class ShowError(val message: String) : RestaurantEvent()
    }

    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    private val _quantities = MutableStateFlow<Map<String, Int>>(emptyMap())
    val quantities: StateFlow<Map<String, Int>> = _quantities.asStateFlow()

    private val _cartBreakdown = MutableStateFlow(
        CartPriceBreakdown(
            subtotal = FREE_DELIVERY_FEE,
            deliveryFee = FREE_DELIVERY_FEE,
            taxes = FREE_DELIVERY_FEE,
            total = FREE_DELIVERY_FEE,
        )
    )
    val cartBreakdown: StateFlow<CartPriceBreakdown> = _cartBreakdown.asStateFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    private val _navigationEvents = MutableSharedFlow<RestaurantEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    private val _uiEvents = MutableSharedFlow<RestaurantEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_UI,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val _mergedEvents = MutableSharedFlow<RestaurantEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val events: SharedFlow<RestaurantEvent> = _mergedEvents.asSharedFlow()

    private val _scrollToCategory = MutableSharedFlow<String>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val scrollToCategory: SharedFlow<String> = _scrollToCategory.asSharedFlow()

    private val loadRestaurantDataHandler =
        CoroutineExceptionHandler { _, exception ->
            viewModelScope.launch(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isMenuLoading = false,
                        error = exception.message ?: ERR_COULD_NOT_LOAD_RESTAURANT,
                    )
                }
            }
        }

    private val observerExceptionHandler =
        CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, OBSERVER_FAILED)
        }

    private val mergeObserverHandler =
        CoroutineExceptionHandler { _, exception ->
            Timber.e(exception, RESTAURANT_MERGE_FAILED)
        }

    init {
        loadRestaurantData()
        observeCart()
        observeQuantities()
        observeBreakdown()
        viewModelScope.launch(mergeObserverHandler) {
            _navigationEvents.collect { _mergedEvents.emit(it) }
        }
        viewModelScope.launch(mergeObserverHandler) {
            _uiEvents.collect { _mergedEvents.emit(it) }
        }
    }

    private fun loadRestaurantData() =
        viewModelScope.launch(loadRestaurantDataHandler) {
            combine(
                restaurantRepository.getRestaurantDetail(restaurantId),
                restaurantRepository.getMenuItems(restaurantId),
                restaurantRepository.getReviews(restaurantId),
            ) { restaurantResult, menuResult, reviewsResult ->
                val restaurant = restaurantResult.getOrNull()
                val menuMap = menuResult.getOrDefault(emptyMap())
                val reviews = reviewsResult.getOrDefault(emptyList())
                val recommended = menuMap.values.flatten()
                    .filter { it.isRecommended }
                    .take(AppConstants.MAX_RECOMMENDED_ITEMS)

                RestaurantUiState(
                    restaurant = restaurant,
                    menuByCategory = menuMap,
                    recommended = recommended,
                    reviews = reviews,
                    isLoading = false,
                    isMenuLoading = false,
                    error = if (restaurant == null) ERR_COULD_NOT_LOAD_RESTAURANT else null,
                )
            }.collect { newState ->
                _uiState.update { current ->
                    newState.copy(
                        cartItemCount = current.cartItemCount,
                        cartTotal = current.cartTotal,
                    )
                }
            }
        }

    private fun observeCart() =
        viewModelScope.launch(observerExceptionHandler) {
            combine(
                cartRepository.getCartItemCount(),
                cartRepository.getCartTotal(),
            ) { count, breakdown -> count to breakdown }
                .collect { (count, breakdown) ->
                    _uiState.update {
                        it.copy(cartItemCount = count, cartTotal = breakdown.total)
                    }
                    _cartBreakdown.value = breakdown
                }
        }

    private fun observeQuantities() =
        viewModelScope.launch(observerExceptionHandler) {
            cartRepository.getCartItems().collect { items ->
                _cartItems.value = items
                _quantities.value = items.associate { it.menuItem.id to it.quantity }
            }
        }

    private fun observeBreakdown() =
        viewModelScope.launch(observerExceptionHandler) {
            cartRepository.getCartItems().collect { items ->
                val subtotal = items.sumOf { it.totalPrice }
                val delivery = when {
                    subtotal <= 0.0 -> 0.0
                    subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE -> 0.0
                    else -> AppBusinessRules.DEFAULT_DELIVERY_FEE
                }
                val taxes = subtotal * AppBusinessRules.GST_RATE
                _cartBreakdown.value = CartPriceBreakdown(
                    subtotal = subtotal,
                    deliveryFee = delivery,
                    taxes = taxes,
                    total = subtotal + delivery + taxes,
                )
            }
        }

    fun quickAddToCart(item: MenuItem) = viewModelScope.launch {
        try {
            addToCartUseCase(
                menuItem = item,
                quantity = AppConstants.MIN_CART_QUANTITY,
                customisations = emptyList(),
            )
            _uiEvents.emit(RestaurantEvent.ItemAdded(item.name))
        } catch (e: CancellationException) {
            throw e   // Fix 1 — rethrow structured concurrency
        } catch (e: Exception) {
            _uiEvents.emit(RestaurantEvent.ShowError(e.message ?: ERR_COULD_NOT_ADD_CART))
        }
    }

    fun onIncrementItem(item: MenuItem) = viewModelScope.launch {
        try {
            val currentQty = _quantities.value[item.id] ?: 0
            if (currentQty == 0) {
                addToCartUseCase(
                    menuItem = item,
                    quantity = AppConstants.MIN_CART_QUANTITY,
                    customisations = emptyList(),
                )
                _uiEvents.emit(RestaurantEvent.ItemAdded(item.name))
            } else {
                val cartItem = _cartItems.value.find { it.menuItem.id == item.id }
                    ?: return@launch
                cartRepository.updateQuantity(
                    itemId = cartItem.id,
                    quantity = (currentQty + 1)
                        .coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY),
                )
            }
        } catch (e: CancellationException) {
            throw e   // Fix 2 — rethrow
        } catch (e: Exception) {
            _uiEvents.emit(RestaurantEvent.ShowError(e.message ?: ERR_FAILED_ADD_ITEM))
        }
    }

    fun onDecrementItem(itemId: String) = viewModelScope.launch {
        try {
            val cartItem = _cartItems.value.find { it.menuItem.id == itemId }
                ?: return@launch
            when {
                cartItem.quantity <= 1 ->
                    cartRepository.removeItem(cartItem.id)

                cartItem.quantity > 1 ->
                    cartRepository.updateQuantity(
                        itemId = cartItem.id,
                        quantity = cartItem.quantity - 1,
                    )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            _uiEvents.emit(RestaurantEvent.ShowError(e.message ?: ERR_FAILED_UPDATE_CART))
        }
    }

    fun onMenuItemTapped(itemId: String) =
        viewModelScope.launch {
            _navigationEvents.emit(RestaurantEvent.NavigateToProduct(itemId))
        }

    fun onCartBarTapped() =
        viewModelScope.launch {
            _navigationEvents.emit(RestaurantEvent.NavigateToCart)
        }

    fun onBackPressed() =
        viewModelScope.launch {
            _navigationEvents.emit(RestaurantEvent.NavigateBack)
        }

    fun onCategoryFooterTapped(category: String) =
        viewModelScope.launch {
            _scrollToCategory.emit(category)
        }

    fun onTabSelected(tab: MenuTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun retry() = loadRestaurantData()

    fun getCategoryNames() = _uiState.value.menuByCategory.keys.toList()
}