package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeCartRepository : CartRepository {

    private val items = MutableStateFlow<List<CartItem>>(emptyList())

    override fun getCartItems(): Flow<List<CartItem>> = items

    override fun getCartItemCount(): Flow<Int> =
        items.map { list -> list.sumOf { it.quantity } }

    override fun getCartTotal(): Flow<Double> =
        items.map { list ->
            list.sumOf { it.menuItem.price * it.quantity }
        }

    override suspend fun addItem(item: CartItem) {
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
        items.value = items.value.map {
            if (it.id == itemId) it.copy(quantity = quantity) else it
        }
    }

    override suspend fun removeItem(itemId: String) {
        items.value = items.value.filterNot { it.id == itemId }
    }

    override suspend fun clearCart() {
        items.value = emptyList()
    }

    override suspend fun itemExists(menuItemId: String): Boolean {
        return items.value.any { it.menuItem.id == menuItemId }
    }
}