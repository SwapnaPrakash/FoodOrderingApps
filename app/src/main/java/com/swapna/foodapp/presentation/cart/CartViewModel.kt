package com.swapna.foodapp.presentation.cart

import androidx.lifecycle.ViewModel
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.usecase.home.GetHomeDataUseCase
import com.swapna.foodapp.utils.ConnectivityObserver
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.EVENT_BUFFER_DEFAULT
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// WHY separate CartViewModel from RestaurantViewModel?
// Single Responsibility:
//   RestaurantViewModel → menu browsing + quick add
//   CartViewModel       → cart management + order placement
// CartScreen has its own lifecycle — own ViewModel

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
) : ViewModel() {

    // ══════════════════════════════════════════════════════════
    // UI STATE
    // ══════════════════════════════════════════════════════════
    data class CartUiState(
        val items:       List<CartItem>      = emptyList(),
        val breakdown:   CartPriceBreakdown  = CartPriceBreakdown(
            subtotal    = 0.0,
            deliveryFee = 0.0,
            taxes       = 0.0,
            total       = 0.0,
        ),
        val isLoading:   Boolean = true,
        val isEmpty:     Boolean = false,
        val error:       String? = null,
        // Restaurant name shown at top of cart
        // Derived from first item in cart
        val restaurantName: String = "",
    )

    // ══════════════════════════════════════════════════════════
    // EVENTS — one-time navigation/feedback
    // ══════════════════════════════════════════════════════════
    sealed class CartEvent {
        object NavigateBack              : CartEvent()
        object NavigateToHome            : CartEvent()
        object OrderPlaced               : CartEvent()
        data class ShowError(
            val message: String,
        )                                : CartEvent()
        data class ShowSnackbar(
            val message: String,
        )                                : CartEvent()
    }

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<CartEvent>(
        replay              = 0,
        extraBufferCapacity = EVENT_BUFFER_DEFAULT,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<CartEvent> = _events.asSharedFlow()

    init {
        observeCart()
    }

    // ══════════════════════════════════════════════════════════
    // OBSERVE CART
    // WHY observe instead of fetch-once?
    // User can modify qty from this screen
    // Flow auto-updates UI on every change
    // No manual refresh needed
    // ══════════════════════════════════════════════════════════
    private fun observeCart() = viewModelScope.launch {
        cartRepository.getCartItems().collect { cartItems ->

            // Compute breakdown from cart items
            // CartItem.totalPrice = (price + extras) × qty
            val subtotal = cartItems.sumOf { it.totalPrice }

            val delivery = when {
                cartItems.isEmpty() -> 0.0
                subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE -> 0.0
                else -> AppBusinessRules.DEFAULT_DELIVERY_FEE
            }

            val taxes = subtotal * AppBusinessRules.GST_RATE

            val breakdown = CartPriceBreakdown(
                subtotal    = subtotal,
                deliveryFee = delivery,
                taxes       = taxes,
                total       = subtotal + delivery + taxes,
            )

            // Restaurant name from first item
            val restaurantName = cartItems
                .firstOrNull()
                ?.menuItem
                ?.restaurantId
                ?: ""

            _uiState.update {
                it.copy(
                    items          = cartItems,
                    breakdown      = breakdown,
                    isLoading      = false,
                    isEmpty        = cartItems.isEmpty(),
                    restaurantName = restaurantName,
                )
            }
        }
    }

    // ══════════════════════════════════════════════════════════
    // USER ACTIONS
    // ══════════════════════════════════════════════════════════

    // + tapped on cart item row
    fun onIncrementItem(item: CartItem) = viewModelScope.launch {
        try {
            val newQty = (item.quantity + 1)
                .coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY)
            cartRepository.updateQuantity(
                itemId   = item.id,
                quantity = newQty,
            )
        } catch (e: Exception) {
            _events.emit(CartEvent.ShowError(
                e.message ?: "Failed to update item"
            ))
        }
    }

    // - tapped on cart item row
    // qty 1 → remove item completely
    fun onDecrementItem(item: CartItem) = viewModelScope.launch {
        try {
            if (item.quantity <= 1) {
                cartRepository.removeItem(item.id)
                _events.emit(CartEvent.ShowSnackbar(
                    "${item.menuItem.name} removed"
                ))
            } else {
                cartRepository.updateQuantity(
                    itemId   = item.id,
                    quantity = item.quantity - 1,
                )
            }
        } catch (e: Exception) {
            _events.emit(CartEvent.ShowError(
                e.message ?: "Failed to update item"
            ))
        }
    }

    // Swipe to delete or explicit remove
    fun onRemoveItem(item: CartItem) = viewModelScope.launch {
        try {
            cartRepository.removeItem(item.id)
            _events.emit(CartEvent.ShowSnackbar(
                "${item.menuItem.name} removed from cart"
            ))
        } catch (e: Exception) {
            _events.emit(CartEvent.ShowError(
                e.message ?: "Failed to remove item"
            ))
        }
    }

    // Place Order button tapped
    fun onPlaceOrder() = viewModelScope.launch {
        try {
            val items = _uiState.value.items
            if (items.isEmpty()) {
                _events.emit(CartEvent.ShowError(
                    "Cart is empty"
                ))
                return@launch
            }

            val total = _uiState.value.breakdown.total
            if (total < AppBusinessRules.MIN_ORDER_VALUE) {
                _events.emit(CartEvent.ShowError(
                    "Minimum order is ₹${
                        AppBusinessRules.MIN_ORDER_VALUE.toInt()
                    }"
                ))
                return@launch
            }

            // Clear cart after order placed
            cartRepository.clearCart()
            _events.emit(CartEvent.OrderPlaced)

        } catch (e: Exception) {
            _events.emit(CartEvent.ShowError(
                e.message ?: "Failed to place order"
            ))
        }
    }

    fun onBackPressed() = viewModelScope.launch {
        _events.emit(CartEvent.NavigateBack)
    }
}
