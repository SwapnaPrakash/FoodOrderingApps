package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.AppBusinessRules
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_ID_PREFIX
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_ITEM_NAME_PREFIX
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_MENU_ID_PREFIX
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_CART_RESTAURANT_ID
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_CATEGORY_TEST
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_MENU_PRICE_100
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCartRepository : CartRepository {

    private val items = MutableStateFlow<List<CartItem>>(emptyList())

    var seedCartCalled = false
    var addItemCalled = false
    var updateQtyCalled = false
    var removeItemCalled = false
    var clearCartCalled = false
    var lastUpdatedItemId = ""
    var lastUpdatedQty = 0

    override fun getCartItems(): Flow<List<CartItem>> = _items

    override fun getCartItemCount(): Flow<Int> =
        _items.map { it.sumOf { item -> item.quantity } }

    override fun getCartTotal(): Flow<CartPriceBreakdown> =
        items.map { list ->
            val subtotal = list.sumOf { it.totalPrice }
            val delivery = if (subtotal >= AppBusinessRules.FREE_DELIVERY_ABOVE)
                0.0 else AppBusinessRules.DEFAULT_DELIVERY_FEE
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

    override suspend fun itemExists(menuItemId: String): Boolean =
        items.value.any { it.menuItem.id == menuItemId }

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

    private val _items = MutableStateFlow<List<CartItem>>(emptyList())

    fun setItemCount(count: Int) {
        _items.value = (1..count).map { i ->
            CartItem(
                id = "$FAKE_CART_ID_PREFIX$i",
                menuItem = fakeMenuItem(
                    "$FAKE_CART_MENU_ID_PREFIX$i",
                    "$FAKE_CART_ITEM_NAME_PREFIX$i",
                ),
                quantity = 1,
            )
        }
    }

    private fun fakeMenuItem(id: String, name: String) = MenuItem(
        id = id,
        restaurantId = FAKE_CART_RESTAURANT_ID,
        name = name,
        description = "",
        price = FAKE_MENU_PRICE_100,
        imageUrl = "",
        category = FAKE_MENU_CATEGORY_TEST,
        isVeg = false,
        isRecommended = false,
        isBestseller = false,
        isAvailable = true,
        customisations = emptyList(),
    )


    // Add seedCart helper:
    fun seedCart(vararg items: CartItem) {
        _items.value = items.toList()
        seedCartCalled = true
    }

    // Update override functions to track calls:
    override suspend fun updateQuantity(itemId: String, quantity: Int) {
        updateQtyCalled = true
        lastUpdatedItemId = itemId
        lastUpdatedQty = quantity
        _items.value = _items.value.map {
            if (it.id == itemId) it.copy(quantity = quantity) else it
        }
    }

    override suspend fun removeItem(itemId: String) {
        removeItemCalled = true
        _items.value = _items.value.filter { it.id != itemId }
    }

    override suspend fun clearCart() {
        clearCartCalled = true
        _items.value = emptyList()
    }
}