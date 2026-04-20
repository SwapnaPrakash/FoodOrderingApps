package com.swapna.foodapp.presentation.common.fakes

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

    private val items = MutableStateFlow<List<CartItem>>(emptyList())

    var addItemCalled     = false
    var updateQtyCalled   = false
    var removeItemCalled  = false
    var clearCartCalled   = false
    var lastUpdatedItemId = ""
    var lastUpdatedQty    = 0

    override fun getCartItems(): Flow<List<CartItem>> = items

    override fun getCartItemCount(): Flow<Int> =
        items.map { list -> list.sumOf { it.quantity } }

    override fun getCartTotal(): Flow<CartPriceBreakdown> =
        items.map { list ->
            val subtotal = list.sumOf { it.totalPrice }
            val delivery = when {
                subtotal <= 0.0                              -> 0.0
                subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE -> 0.0
                else -> AppBusinessRules.DEFAULT_DELIVERY_FEE
            }
            val taxes = subtotal * AppBusinessRules.GST_RATE
            CartPriceBreakdown(
                subtotal    = subtotal,
                deliveryFee = delivery,
                taxes       = taxes,
                total       = subtotal + delivery + taxes,
            )
        }

    override suspend fun addItem(item: CartItem) {
        addItemCalled = true
        val current = items.value.toMutableList()
        val index   = current.indexOfFirst { it.id == item.id }
        if (index != -1) {
            val existing = current[index]
            current[index] = existing.copy(
                quantity = existing.quantity + item.quantity
            )
        } else {
            current.add(item)
        }
        items.value = current
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int) {
        updateQtyCalled   = true
        lastUpdatedItemId = itemId
        lastUpdatedQty    = quantity
        items.value = items.value.map { cartItem ->
            if (cartItem.id == itemId) cartItem.copy(quantity = quantity)
            else cartItem
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

    // ── Test helpers ──────────────────────────────────────────────────

    fun seedCart(vararg cartItems: CartItem) {
        items.value = cartItems.toList()
    }

    // ✅ ADDED: set item count without constructing CartItems manually
    // setItemCount(3) → creates 3 items each with quantity 1 → count = 3
    // setItemCount(0) → clears cart → count = 0
    fun setItemCount(count: Int) {
        items.value = if (count == 0) {
            emptyList()
        } else {
            (1..count).map { i ->
                CartItem(
                    id       = "test_item_$i",
                    menuItem = FakeRestaurantRepository.fakeMenuItem(
                        id    = "menu_$i",
                        name  = "Test Item $i",
                        price = 100.0,
                    ),
                    quantity = 1,
                )
            }
        }
    }

    fun getQuantityOf(itemId: String): Int =
        items.value.find { it.id == itemId }?.quantity ?: 0

    fun resetTracking() {
        addItemCalled     = false
        updateQtyCalled   = false
        removeItemCalled  = false
        clearCartCalled   = false
        lastUpdatedItemId = ""
        lastUpdatedQty    = 0
    }
}