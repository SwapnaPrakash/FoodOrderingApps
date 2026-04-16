package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>

    fun getCartItemCount(): Flow<Int>

    // CartPriceBreakdown has subtotal + delivery + taxes + total
    fun getCartTotal(): Flow<CartPriceBreakdown>

    suspend fun addItem(item: CartItem)

    suspend fun updateQuantity(itemId: String, quantity: Int)

    suspend fun removeItem(itemId: String)

    suspend fun clearCart()

    suspend fun itemExists(menuItemId: String): Boolean

    //suspend fun getItemQuantity(itemId: String): Int
}