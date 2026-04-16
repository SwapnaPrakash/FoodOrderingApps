package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.AppBusinessRules
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

// WHY List-based not Map-based?
// Matches YOUR existing FakeCartRepository
// List = simpler to reason about in tests
// MutableStateFlow = reactive = ViewModel collects updates

class FakeCartRepository : CartRepository {

    // ── Backing store ─────────────────────────────────────────
    // List<CartItem> not Map — simpler for tests
    // MutableStateFlow emits new value on every change
    // All Flow methods derive from this single source
    private val items =
        MutableStateFlow<List<CartItem>>(emptyList())

    // ── Tracking flags ────────────────────────────────────────
    // WHY tracking flags?
    // Tests verify WHAT was called + WHAT args were passed
    // Without flags → can only check final state
    // With flags → verify repository interaction itself
    var addItemCalled      = false
    var updateQtyCalled    = false
    var removeItemCalled   = false
    var clearCartCalled    = false
    var lastUpdatedItemId  = ""
    var lastUpdatedQty     = 0

    // ── getCartItems ──────────────────────────────────────────
    // Returns raw list — ViewModel maps to quantities Map
    override fun getCartItems(): Flow<List<CartItem>> = items

    // ── getCartItemCount ──────────────────────────────────────
    // Sum of all quantities across all items
    // qty 2 + qty 1 = 3 total
    override fun getCartItemCount(): Flow<Int> =
        items.map { list ->
            list.sumOf { it.quantity }
        }

    // ── getCartTotal ──────────────────────────────────────────
    // ✅ FIX: Returns Flow<CartPriceBreakdown>
    // Matches actual CartRepository interface exactly
    //
    // WHY compute breakdown in FakeCartRepository?
    // Tests that rely on getCartTotal() need realistic values
    // e.g. CartViewModelSpec tests delivery = ₹30 below ₹500
    //      CartViewModelSpec tests delivery = FREE above ₹500
    // Fake must mirror real implementation logic
    // so ViewModel tests behave identically to production
    override fun getCartTotal(): Flow<CartPriceBreakdown> =
        items.map { list ->

            // CartItem.totalPrice = (price + customisation extras) × qty
            // Already handles customisations correctly
            val subtotal = list.sumOf { it.totalPrice }

            // Same business rules as CartRepositoryImpl
            // WHY same rules not hardcoded?
            // AppBusinessRules is the single source of truth
            // If GST_RATE changes 5% → 8% → fake auto-updates
            val delivery = when {
                subtotal <= 0.0 ->
                    0.0
                subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE ->
                    0.0  // FREE delivery above ₹500
                else ->
                    AppBusinessRules.DEFAULT_DELIVERY_FEE  // ₹30
            }

            val taxes = subtotal * AppBusinessRules.GST_RATE  // 5%

            CartPriceBreakdown(
                subtotal    = subtotal,
                deliveryFee = delivery,
                taxes       = taxes,
                total       = subtotal + delivery + taxes,
            )
        }

    // ── addItem ───────────────────────────────────────────────
    // If same id exists → merge quantity (upsert)
    // If new item → add to list
    override suspend fun addItem(item: CartItem) {
        addItemCalled = true

        val current = items.value.toMutableList()
        val index   = current.indexOfFirst { it.id == item.id }

        if (index != -1) {
            // Item already in cart → increment quantity
            val existing = current[index]
            current[index] = existing.copy(
                quantity = existing.quantity + item.quantity
            )
        } else {
            // New item → append to list
            current.add(item)
        }

        items.value = current
    }

    // ── updateQuantity ────────────────────────────────────────
    // Replaces quantity for specific itemId
    // Called from RestaurantViewModel.onIncrementItem
    // when item already in cart
    override suspend fun updateQuantity(
        itemId:   String,
        quantity: Int,
    ) {
        updateQtyCalled   = true
        lastUpdatedItemId = itemId
        lastUpdatedQty    = quantity

        items.value = items.value.map { cartItem ->
            if (cartItem.id == itemId)
                cartItem.copy(quantity = quantity)
            else
                cartItem
        }
    }

    // ── removeItem ────────────────────────────────────────────
    // Completely removes item from cart list
    // Called when qty decremented to 0
    override suspend fun removeItem(itemId: String) {
        removeItemCalled = true
        items.value = items.value.filterNot {
            it.id == itemId
        }
    }

    // ── clearCart ─────────────────────────────────────────────
    // Empties entire cart
    // Called after order placed
    override suspend fun clearCart() {
        clearCartCalled = true
        items.value     = emptyList()
    }

    // ── itemExists ────────────────────────────────────────────
    // Checks by menuItem.id not CartItem.id
    // WHY menuItem.id?
    // CartItem.id = UUID (unique per add action)
    // menuItem.id = stable product ID ("m1", "m2")
    // Use case needs: "is this product already in cart?"
    override suspend fun itemExists(menuItemId: String): Boolean =
        items.value.any { it.menuItem.id == menuItemId }

    // ── Test helpers ──────────────────────────────────────────

    // Seed cart with items for test setup
    // WHY bypass addItem()?
    // addItem() sets addItemCalled = true
    // seedCart() sets initial state WITHOUT triggering flags
    // Tests that check addItemCalled = true are not polluted
    fun seedCart(vararg cartItems: CartItem) {
        items.value = cartItems.toList()
    }

    // Get current quantity of specific item
    // Used in assertions: fakeCartRepo.getQuantityOf("c1") shouldBe 2
    fun getQuantityOf(itemId: String): Int =
        items.value.find { it.id == itemId }?.quantity ?: 0

    // Reset all tracking flags between tests
    // Call in afterEach if reusing same instance
    fun resetTracking() {
        addItemCalled     = false
        updateQtyCalled   = false
        removeItemCalled  = false
        clearCartCalled   = false
        lastUpdatedItemId = ""
        lastUpdatedQty    = 0
    }
}