package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.repository.CartRepository
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class FakeCartRepository @Inject constructor() : CartRepository {

    private val cartItems = MutableStateFlow<List<CartItem>>(emptyList())

    override fun getCartItems(): Flow<List<CartItem>> = cartItems

    override fun getCartItemCount(): Flow<Int> =
        cartItems.map { it.size }

    override fun getCartTotal(): Flow<Double> =
        cartItems.map { list ->
            list.sumOf { it.totalPrice }
        }

    override suspend fun addItem(item: CartItem) {
        cartItems.value = cartItems.value + item
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int) {
        cartItems.value = cartItems.value.map {
            if (it.id == itemId) it.copy(quantity = quantity) else it
        }
    }

    override suspend fun removeItem(itemId: String) {
        cartItems.value = cartItems.value.filterNot { it.id == itemId }
    }

    override suspend fun clearCart() {
        cartItems.value = emptyList()
    }

    override suspend fun itemExists(menuItemId: String): Boolean {
        return cartItems.value.any { cartItem ->
            cartItem.menuItem.id == menuItemId
        }
    }
}