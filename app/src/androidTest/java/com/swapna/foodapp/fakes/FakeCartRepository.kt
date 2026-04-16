package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.AppBusinessRules
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCartRepository : CartRepository {

    // ── Backing store ─────────────────────────────────────────
    // List<CartItem> — matches your existing FakeCartRepository
    private val items =
        MutableStateFlow<List<CartItem>>(emptyList())

    // ── Tracking flags for test assertions ────────────────────
    var addItemCalled      = false
    var updateQtyCalled    = false
    var removeItemCalled   = false
    var clearCartCalled    = false
    var lastUpdatedItemId  = ""
    var lastUpdatedQty     = 0

    // ── Interface implementations ─────────────────────────────

    override fun getCartItems(): Flow<List<CartItem>> = items

    override fun getCartItemCount(): Flow<Int> =
        items.map { list ->
            list.sumOf { it.quantity }
        }

    override fun getCartTotal(): Flow<CartPriceBreakdown> =
        items.map { map ->
            val subtotal = map.sumOf { it.totalPrice }
            val delivery = if (subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE)
                0.0 else AppBusinessRules.DEFAULT_DELIVERY_FEE
            val taxes = subtotal * AppBusinessRules.GST_RATE
            CartPriceBreakdown(
                subtotal    = subtotal,
                deliveryFee = delivery,
                taxes       = taxes,
                total       = subtotal + delivery + taxes,
            )
        }


    // Simple subtotal only — no delivery/GST
    // ViewModel computes CartPriceBreakdown from getCartItems()
   /* override fun getCartTotal(): Flow<Double> =
        items.map { list ->
            list.sumOf { it.totalPrice }
            // ✅ Uses CartItem.totalPrice which already
            // accounts for customisation extras:
            // (menuItem.price + extras) × quantity
        }*/

    override suspend fun addItem(item: CartItem) {
        addItemCalled = true

        val current = items.value.toMutableList()
        val index   = current.indexOfFirst { it.id == item.id }

        if (index != -1) {
            // Item exists → increment quantity
            val existing = current[index]
            current[index] = existing.copy(
                quantity = existing.quantity + item.quantity
            )
        } else {
            // New item → add to list
            current.add(item)
        }

        items.value = current
    }

    override suspend fun updateQuantity(
        itemId:   String,
        quantity: Int,
    ) {
        updateQtyCalled   = true
        lastUpdatedItemId = itemId
        lastUpdatedQty    = quantity

        items.value = items.value.map {
            if (it.id == itemId) it.copy(quantity = quantity)
            else it
        }
    }

    override suspend fun removeItem(itemId: String) {
        removeItemCalled = true
        items.value = items.value.filterNot { it.id == itemId }
    }

    override suspend fun clearCart() {
        clearCartCalled = true
        items.value     = emptyList()
    }

    override suspend fun itemExists(menuItemId: String): Boolean =
        items.value.any { it.menuItem.id == menuItemId }

    // ── Test helpers ──────────────────────────────────────────

    // Seed cart directly — bypasses addItem tracking flags
    // Use this in test setup to put items in cart
    fun seedCart(vararg cartItems: CartItem) {
        items.value = cartItems.toList()
    }

    // Get current quantity of specific item
    // Useful for asserting qty after increment/decrement
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