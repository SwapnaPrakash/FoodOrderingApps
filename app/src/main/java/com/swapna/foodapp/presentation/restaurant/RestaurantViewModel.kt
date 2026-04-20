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
import com.swapna.foodapp.utils.AppConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.swapna.foodapp.utils.AppBusinessRules
import com.swapna.foodapp.utils.EVENT_BUFFER_DEFAULT
import kotlinx.coroutines.channels.BufferOverflow

@HiltViewModel
class RestaurantViewModel @Inject constructor(
    savedStateHandle:                 SavedStateHandle,
    private val restaurantRepository: RestaurantRepository,
    private val cartRepository:       CartRepository,
    private val addToCartUseCase:     AddToCartUseCase,
) : ViewModel() {

    // Get restaurantId from navigation argument
    val restaurantId: String =
        checkNotNull(savedStateHandle[AppRoutes.ARG_RESTAURANT_ID]) {
            "restaurantId is required for RestaurantScreen"
        }

    // ══════════════════════════════════════════════════════════
    // UI STATE — your existing structure kept intact
    // ══════════════════════════════════════════════════════════
    data class RestaurantUiState(
        val restaurant:     Restaurant?                     = null,
        val menuByCategory: Map<String, List<MenuItem>>     = emptyMap(),
        val recommended:    List<MenuItem>                  = emptyList(),
        val reviews:        List<Review>                    = emptyList(),
        val cartItemCount:  Int                             = 0,
        val cartTotal:      Double                          = 0.0,
        val isLoading:      Boolean                         = true,
        val isMenuLoading:  Boolean                         = true,
        val error:          String?                         = null,
        val selectedTab:    MenuTab                         = MenuTab.MENU,
    )

    // Two tabs on restaurant screen
    enum class MenuTab { MENU, REVIEWS }

    // ── Events ────────────────────────────────────────────────
    sealed class RestaurantEvent {
        data class NavigateToProduct(val itemId: String) : RestaurantEvent()
        data class ItemAdded(val itemName: String)       : RestaurantEvent()
        data class ShowError(val message: String)        : RestaurantEvent()
        object NavigateToCart                            : RestaurantEvent()
        object NavigateBack                              : RestaurantEvent()
    }

    // ── StateFlows ────────────────────────────────────────────
    private val _uiState = MutableStateFlow(RestaurantUiState())
    val uiState: StateFlow<RestaurantUiState> = _uiState.asStateFlow()

    // ✅ Day 19: Per-item quantities for ADD/[- qty +] switching
    // Map<itemId, quantity> — MenuItemRow reads this
    private val _quantities =
        MutableStateFlow<Map<String, Int>>(emptyMap())
    val quantities: StateFlow<Map<String, Int>> =
        _quantities.asStateFlow()

    // ✅ Day 19: Full breakdown for CartBottomBar
    private val _cartBreakdown = MutableStateFlow(
        CartPriceBreakdown(0.0, 0.0, 0.0, 0.0)
    )
    val cartBreakdown: StateFlow<CartPriceBreakdown> =
        _cartBreakdown.asStateFlow()

    // ── SharedFlows ───────────────────────────────────────────
    private val _events = MutableSharedFlow<RestaurantEvent>(
        replay              = 0,
        extraBufferCapacity = EVENT_BUFFER_DEFAULT,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )
    val events: SharedFlow<RestaurantEvent> = _events.asSharedFlow()

    // Your existing scroll flow — unchanged
    private val _scrollToCategory = MutableSharedFlow<String>(
        replay              = 0,
        extraBufferCapacity = EVENT_BUFFER_DEFAULT,
        onBufferOverflow    = BufferOverflow.DROP_OLDEST,
    )
    val scrollToCategory: SharedFlow<String> =
        _scrollToCategory.asSharedFlow()

    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    // ── Init ──────────────────────────────────────────────────
    init {
        loadRestaurantData()
        observeCart()
        observeQuantities()    // ✅ Day 19
        observeBreakdown()     // ✅ Day 19
    }

    // ══════════════════════════════════════════════════════════
    // LOAD DATA — your existing combine() kept intact
    // ══════════════════════════════════════════════════════════
    private fun loadRestaurantData() = viewModelScope.launch {
        combine(
            restaurantRepository.getRestaurantDetail(restaurantId),
            restaurantRepository.getMenuItems(restaurantId),
            restaurantRepository.getReviews(restaurantId),
        ) { restaurantResult, menuResult, reviewsResult ->

            val restaurant = restaurantResult.getOrNull()
            val menuMap    = menuResult.getOrDefault(emptyMap())
            val reviews    = reviewsResult.getOrDefault(emptyList())

            val recommended = menuMap.values
                .flatten()
                .filter { it.isRecommended }
                .take(AppConstants.MAX_RECOMMENDED_ITEMS)

            RestaurantUiState(
                restaurant     = restaurant,
                menuByCategory = menuMap,
                recommended    = recommended,
                reviews        = reviews,
                isLoading      = false,
                isMenuLoading  = false,
                error          = if (restaurant == null)
                    "Could not load restaurant" else null,
            )
        }.collect { newState ->
            // Preserve cart state when merging
            _uiState.update { current ->
                newState.copy(
                    cartItemCount = current.cartItemCount,
                    cartTotal     = current.cartTotal,
                )
            }
        }
    }

    // ── Observe Cart count + total ────────────────────────────
    // Your existing observeCart — unchanged
    private fun observeCart() = viewModelScope.launch {
        combine(
            cartRepository.getCartItemCount(),
            cartRepository.getCartTotal(),
        ) { count, breakdown -> count to breakdown }
            .collect { (count, breakdown) ->
                _uiState.update {
                    it.copy(
                        cartItemCount = count,
                        // ✅ Extract Double from CartPriceBreakdown
                        cartTotal     = breakdown.total,
                    )
                }
                // ✅ Also update cartBreakdown StateFlow for CartBottomBar
                _cartBreakdown.value = breakdown
            }
    }

    // ✅ Day 19: Observe per-item quantities
    // WHY separate from observeCart?
    // observeCart = total count + total price (for badge + bar)
    // observeQuantities = per-item qty (for [- 2 +] selector)
    // Different concerns → separate flows
    private fun observeQuantities() = viewModelScope.launch {
        cartRepository.getCartItems().collect { items ->
            _cartItems.value = items
            _quantities.value = items.associate {
                it.menuItem.id to it.quantity
            }
        }
    }

    // ✅ Day 19: Compute CartPriceBreakdown for CartBottomBar
    // Your CartRepository returns Flow<Double> for total
    // We compute delivery + GST here in ViewModel
    private fun observeBreakdown() = viewModelScope.launch {
        cartRepository.getCartItems().collect { items ->
            val subtotal = items.sumOf { it.totalPrice }

            val delivery = when {
                subtotal <= 0.0 ->
                    0.0
                subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE ->
                    0.0
                else ->
                    AppBusinessRules.DEFAULT_DELIVERY_FEE
            }

            val taxes = subtotal * AppBusinessRules.GST_RATE

            _cartBreakdown.value = CartPriceBreakdown(
                subtotal    = subtotal,
                deliveryFee = delivery,
                taxes       = taxes,
                total       = subtotal + delivery + taxes,
            )
        }
    }

    // ══════════════════════════════════════════════════════════
    // USER ACTIONS
    // ══════════════════════════════════════════════════════════

    // Your existing method — navigate to Product Detail
    fun onMenuItemTapped(itemId: String) = viewModelScope.launch {
        _events.emit(RestaurantEvent.NavigateToProduct(itemId))
    }

    // Your existing quickAddToCart — uses AddToCartUseCase
    fun quickAddToCart(item: MenuItem) = viewModelScope.launch {
        try {
            addToCartUseCase(
                menuItem       = item,
                quantity       = AppConstants.MIN_CART_QUANTITY,
                customisations = emptyList(),
            )
            _events.emit(RestaurantEvent.ItemAdded(item.name))
        } catch (e: Exception) {
            _events.emit(
                RestaurantEvent.ShowError(
                    e.message ?: "Could not add to cart"
                )
            )
        }
    }

    // ✅ Day 19: + button tapped on MenuItemRow
    // Uses itemExists to decide: insert new OR increment existing
    fun onIncrementItem(item: MenuItem) = viewModelScope.launch {
        try {
            val currentQty = _quantities.value[item.id] ?: 0

            if (currentQty == 0) {
                // Not in cart — use AddToCartUseCase (your existing)
                addToCartUseCase(
                    menuItem       = item,
                    quantity       = AppConstants.MIN_CART_QUANTITY,
                    customisations = emptyList(),
                )
                _events.emit(RestaurantEvent.ItemAdded(item.name))
            } else {
                val cartItem = _cartItems.value
                    .find { it.menuItem.id == item.id }
                    ?: return@launch

                cartRepository.updateQuantity(
                    itemId   = cartItem.id,
                    quantity = (currentQty + 1)
                        .coerceAtMost(AppBusinessRules.MAX_ITEM_QUANTITY),
                )
            }
        } catch (e: Exception) {
            _events.emit(
                RestaurantEvent.ShowError(e.message ?: "Failed to add item")
            )
        }
    }

    // ✅ Day 19: - button tapped on MenuItemRow
    // Reduce qty by 1 — remove from Room if reaches 0
    fun onDecrementItem(itemId: String) = viewModelScope.launch {
        try {
            val cartItem = _cartItems.value
                .find { it.menuItem.id == itemId }
                ?: return@launch                        // not in cart — guard

            val currentQty = cartItem.quantity

            when {
                currentQty <= 1 -> {
                    // Remove completely using CartItem UUID ✅
                    cartRepository.removeItem(cartItem.id)
                }
                currentQty > 1  -> {
                    // Decrement using CartItem UUID ✅
                    cartRepository.updateQuantity(
                        itemId   = cartItem.id,
                        quantity = currentQty - 1,
                    )
                }
            }
        } catch (e: Exception) {
            _events.emit(
                RestaurantEvent.ShowError(
                    e.message ?: "Failed to update cart"
                )
            )
        }
    }

    // Your existing methods — all unchanged
    fun onCategoryFooterTapped(category: String) =
        viewModelScope.launch {
            _scrollToCategory.emit(category)
        }

    fun onTabSelected(tab: MenuTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun onCartBarTapped() = viewModelScope.launch {
        _events.emit(RestaurantEvent.NavigateToCart)
    }

    fun onBackPressed() = viewModelScope.launch {
        _events.emit(RestaurantEvent.NavigateBack)
    }

    fun retry() = loadRestaurantData()

    fun getCategoryNames(): List<String> =
        _uiState.value.menuByCategory.keys.toList()
}