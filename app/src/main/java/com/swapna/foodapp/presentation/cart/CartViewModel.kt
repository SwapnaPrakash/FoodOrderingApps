package com.swapna.foodapp.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.AppConstants.ERR_CART_EMPTY
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_PLACE_ORDER
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_REMOVE_ITEM
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_UPDATE_ITEM
import com.swapna.foodapp.utils.AppConstants.ERR_MIN_ORDER
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_NAVIGATION
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_UI
import com.swapna.foodapp.utils.AppConstants.MSG_ITEM_REMOVED
import com.swapna.foodapp.utils.AppConstants.MSG_ITEM_REMOVED_CART
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
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    data class CartUiState(
        val items: List<CartItem> = emptyList(),
        val breakdown: CartPriceBreakdown = CartPriceBreakdown(0.0, 0.0, 0.0, 0.0),
        val isLoading: Boolean = true,
        val isEmpty: Boolean = false,
        val error: String? = null,
        val restaurantName: String = "",
    )

    sealed class CartEvent {
        object NavigateBack : CartEvent()
        object NavigateToHome : CartEvent()
        object OrderPlaced : CartEvent()
        data class ShowError(val message: String) : CartEvent()
        data class ShowSnackbar(val message: String) : CartEvent()
    }

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _navigationEvents = MutableSharedFlow<CartEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )

    private val _uiEvents = MutableSharedFlow<CartEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_UI,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private val _mergedEvents = MutableSharedFlow<CartEvent>(
        replay = 0,
        extraBufferCapacity = EVENT_BUFFER_NAVIGATION,
        onBufferOverflow = BufferOverflow.SUSPEND,
    )
    val events: SharedFlow<CartEvent> = _mergedEvents.asSharedFlow()

    init {
        observeCart()
        viewModelScope.launch {
            _navigationEvents.collect { _mergedEvents.emit(it) }
        }
        viewModelScope.launch {
            _uiEvents.collect { _mergedEvents.emit(it) }
        }
    }

    private fun observeCart() = viewModelScope.launch {
        cartRepository.getCartItems().collect { cartItems ->
            val subtotal = cartItems.sumOf { it.totalPrice }
            val delivery = when {
                cartItems.isEmpty() -> 0.0
                subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE -> 0.0
                else -> AppBusinessRules.DEFAULT_DELIVERY_FEE
            }
            val taxes = subtotal * AppBusinessRules.GST_RATE
            _uiState.update {
                it.copy(
                    items = cartItems,
                    breakdown = CartPriceBreakdown(
                        subtotal = subtotal,
                        deliveryFee = delivery,
                        taxes = taxes,
                        total = subtotal + delivery + taxes,
                    ),
                    isLoading = false,
                    isEmpty = cartItems.isEmpty(),
                    restaurantName = cartItems.firstOrNull()?.menuItem?.restaurantId ?: "",
                )
            }
        }
    }

    fun onIncrementItem(item: CartItem) = viewModelScope.launch {
        try {
            cartRepository.updateQuantity(
                itemId = item.id,
                quantity = (item.quantity + 1).coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY),
            )
        } catch (e: Exception) {
            _uiEvents.emit(CartEvent.ShowError(e.message ?: ERR_FAILED_UPDATE_ITEM))
        }
    }

    fun onDecrementItem(item: CartItem) = viewModelScope.launch {
        try {
            if (item.quantity <= 1) {
                cartRepository.removeItem(item.id)
                _uiEvents.emit(CartEvent.ShowSnackbar("${item.menuItem.name}$MSG_ITEM_REMOVED"))
            } else {
                cartRepository.updateQuantity(itemId = item.id, quantity = item.quantity - 1)
            }
        } catch (e: Exception) {
            _uiEvents.emit(CartEvent.ShowError(e.message ?: ERR_FAILED_UPDATE_ITEM))
        }
    }

    fun onRemoveItem(item: CartItem) = viewModelScope.launch {
        try {
            cartRepository.removeItem(item.id)
            _uiEvents.emit(CartEvent.ShowSnackbar("${item.menuItem.name}$MSG_ITEM_REMOVED_CART"))
        } catch (e: Exception) {
            _uiEvents.emit(CartEvent.ShowError(e.message ?: ERR_FAILED_REMOVE_ITEM))
        }
    }

    fun onPlaceOrder() = viewModelScope.launch {
        try {
            val items = _uiState.value.items
            if (items.isEmpty()) {
                _uiEvents.emit(CartEvent.ShowError(ERR_CART_EMPTY))
                return@launch
            }
            if (_uiState.value.breakdown.total < AppBusinessRules.MIN_ORDER_VALUE) {
                _uiEvents.emit(
                    CartEvent.ShowError(
                        "$ERR_MIN_ORDER${AppBusinessRules.MIN_ORDER_VALUE.toInt()}"
                    )
                )
                return@launch
            }
            cartRepository.clearCart()
            _navigationEvents.emit(CartEvent.OrderPlaced)
        } catch (e: Exception) {
            _uiEvents.emit(CartEvent.ShowError(e.message ?: ERR_FAILED_PLACE_ORDER))
        }
    }

    fun onBackPressed() = viewModelScope.launch {
        _navigationEvents.emit(CartEvent.NavigateBack)
    }
}