package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.AppBusinessRules
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.utils.TestConstants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCartRepository : CartRepository {

    private val items = MutableStateFlow<List<CartItem>>(emptyList())

    var addItemCalled = false
    var updateQtyCalled = false
    var removeItemCalled = false
    var clearCartCalled = false
    var lastUpdatedItemId = ""
    var lastUpdatedQty = 0

    override fun getCartItems(): Flow<List<CartItem>> = items

    override fun getCartItemCount(): Flow<Int> =
        items.map { list -> list.sumOf { it.quantity } }

    override fun getCartTotal(): Flow<CartPriceBreakdown> =
        items.map { list ->
            val subtotal = list.sumOf { it.totalPrice }
            val delivery = when {
                subtotal <= 0.0 -> 0.0
                subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE -> 0.0
                else -> AppBusinessRules.DEFAULT_DELIVERY_FEE
            }
            val taxes = subtotal * AppBusinessRules.GST_RATE
            CartPriceBreakdown(
                subtotal = subtotal,
                deliveryFee = delivery,
                taxes = taxes,
                total = subtotal + delivery + taxes,
            )
        }

    override suspend fun addItem(item: CartItem) {
        addItemCalled = true
        val current = items.value.toMutableList()
        val index = current.indexOfFirst { it.id == item.id }
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
        updateQtyCalled = true
        lastUpdatedItemId = itemId
        lastUpdatedQty = quantity
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
        items.value = emptyList()
    }

    override suspend fun itemExists(menuItemId: String): Boolean =
        items.value.any { it.menuItem.id == menuItemId }

    fun seedCart(vararg cartItems: CartItem) {
        items.value = cartItems.toList()
    }

    fun setItemCount(count: Int) {
        items.value = if (count == 0) {
            emptyList()
        } else {
            (1..count).map { i ->
                CartItem(
                    id = "${TestConstants.FAKE_CART_ID_PREFIX}$i",
                    menuItem = FakeRestaurantRepository.fakeMenuItem(
                        id = "${TestConstants.FAKE_MENU_ID_PREFIX}$i",
                        name = "${TestConstants.FAKE_ITEM_NAME_PREFIX}$i",
                        price = TestConstants.PRICE_100,
                    ),
                    quantity = TestConstants.CART_QTY_1,
                )
            }
        }
    }

    fun getQuantityOf(itemId: String): Int =
        items.value.find { it.id == itemId }?.quantity ?: 0

    fun resetTracking() {
        addItemCalled = false
        updateQtyCalled = false
        removeItemCalled = false
        clearCartCalled = false
        lastUpdatedItemId = ""
        lastUpdatedQty = 0
    }
}