package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    // Observe — returns Flow because cart changes in real time
    fun getCartItems(): Flow<List<CartItem>>
    fun getCartItemCount(): Flow<Int>
    fun getCartTotal(): Flow<Double>

    // Mutate — suspend because they're one-shot DB writes
    suspend fun addItem(item: CartItem)
    suspend fun updateQuantity(itemId: String, quantity: Int)
    suspend fun removeItem(itemId: String)
    suspend fun clearCart()
    suspend fun itemExists(menuItemId: String): Boolean
}