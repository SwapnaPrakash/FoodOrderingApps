package com.swapna.foodapp.presentation.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.swapna.foodapp.di.IoDispatcher
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.ERR_CART_EMPTY
import com.swapna.foodapp.utils.AppConstants.ERR_FAILED_PLACE_ORDER
import com.swapna.foodapp.utils.AppConstants.ERR_MIN_ORDER
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_NAVIGATION
import com.swapna.foodapp.utils.AppConstants.EVENT_BUFFER_UI
import com.swapna.foodapp.utils.AppConstants.MSG_ITEM_REMOVED
import com.swapna.foodapp.utils.AppConstants.MSG_ITEM_REMOVED_CART
import com.swapna.foodapp.utils.AppConstants.UNKNOWN_ERROR
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    data class CartUiState(
        val items: List<CartItem> = emptyList(),
        val breakdown: CartPriceBreakdown = CartPriceBreakdown(0.0, 0.0, 0.0, 0.0),
        val isLoading: Boolean = true,
        val isEmpty: Boolean = false,
        val error: String? = null,
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
                cartItems.isEmpty() -> AppBusinessRules.FREE_DELIVERY_FEE
                subtotal <= AppBusinessRules.FREE_DELIVERY_FEE -> AppBusinessRules.FREE_DELIVERY_FEE
                subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE -> AppBusinessRules.FREE_DELIVERY_FEE
                else -> AppConstants.DEFAULT_DELIVERY_FEE
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
                )
            }
        }
    }

    private val cartOperationHandler =
        CoroutineExceptionHandler { _, exception ->
            viewModelScope.launch(Dispatchers.Main) {
                _uiEvents.emit(
                    CartEvent.ShowError(exception.message ?: UNKNOWN_ERROR)
                )
            }
        }

    fun onIncrementItem(item: CartItem) =
        viewModelScope.launch(ioDispatcher + cartOperationHandler) {
            cartRepository.updateQuantity(
                itemId = item.id,
                quantity = (item.quantity + 1)
                    .coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY),
            )
        }

    fun onDecrementItem(item: CartItem) =
        viewModelScope.launch(ioDispatcher + cartOperationHandler) {
            if (item.quantity <= 1) {
                cartRepository.removeItem(item.id)
                withContext(Dispatchers.Main) {
                    _uiEvents.emit(
                        CartEvent.ShowSnackbar(
                            "${item.menuItem.name}$MSG_ITEM_REMOVED"
                        )
                    )
                }
            } else {
                cartRepository.updateQuantity(
                    itemId = item.id,
                    quantity = item.quantity - 1,
                )
            }
        }

    fun onRemoveItem(item: CartItem) =
        viewModelScope.launch(ioDispatcher + cartOperationHandler) {
            cartRepository.removeItem(item.id)
            withContext(Dispatchers.Main) {
                _uiEvents.emit(
                    CartEvent.ShowSnackbar(
                        "${item.menuItem.name}$MSG_ITEM_REMOVED_CART"
                    )
                )
            }
        }

    fun onPlaceOrder() = viewModelScope.launch {
        try {
            val state = _uiState.value

            if (state.items.isEmpty()) {
                _uiEvents.emit(CartEvent.ShowError(ERR_CART_EMPTY))
                return@launch
            }

            if (state.breakdown.subtotal < AppBusinessRules.MIN_ORDER_VALUE) {
                _uiEvents.emit(
                    CartEvent.ShowError(
                        "$ERR_MIN_ORDER${AppBusinessRules.MIN_ORDER_VALUE.toInt()}"
                    )
                )
                return@launch
            }

            withContext(ioDispatcher) {
                cartRepository.clearCart()
            }
            _navigationEvents.emit(CartEvent.OrderPlaced)

        } catch (e: Exception) {
            _uiEvents.emit(
                CartEvent.ShowError(e.message ?: ERR_FAILED_PLACE_ORDER)
            )
        }
    }

    fun onBackPressed() = viewModelScope.launch {
        _navigationEvents.emit(CartEvent.NavigateBack)
    }
}