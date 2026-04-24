package com.swapna.foodapp.data.repository

import com.swapna.foodapp.data.local.dao.CartDao
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.domain.model.AppBusinessRules
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.CartPriceBreakdown
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class CartRepositoryImpl @Inject constructor(
    private val cartDao: CartDao,
    private val entityMapper: EntityMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> = cartDao.getAllItems().map { entities ->
        entities.map { entityMapper.cartItemToDomain(it) }
    }.flowOn(ioDispatcher)

    override fun getCartItemCount(): Flow<Int> = cartDao.getItemCount().flowOn(ioDispatcher)

    override fun getCartTotal(): Flow<CartPriceBreakdown> =
        cartDao.getAllItems()
            .map { entities ->
                val items = entities.map {
                    entityMapper.cartItemToDomain(it)
                }

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

                CartPriceBreakdown(
                    subtotal = subtotal,
                    deliveryFee = delivery,
                    taxes = taxes,
                    total = subtotal + delivery + taxes,
                )
            }
            .flowOn(ioDispatcher)

    override suspend fun addItem(item: CartItem) = withContext(ioDispatcher) {
        cartDao.insert(entityMapper.cartItemToEntity(item))
    }

    override suspend fun updateQuantity(itemId: String, quantity: Int) = withContext(ioDispatcher) {
        cartDao.updateQuantity(itemId, quantity)
    }

    override suspend fun removeItem(itemId: String) = withContext(ioDispatcher) {
        cartDao.deleteById(itemId)
    }

    override suspend fun clearCart() = withContext(ioDispatcher) {
        cartDao.clearAll()
    }

    /*override suspend fun getItemQuantity(itemId: String): Int = withContext(ioDispatcher) {
        cartDao.getByMenuItemId(itemId)?.quantity ?: 0
    }*/

    override suspend fun itemExists(menuItemId: String): Boolean = withContext(ioDispatcher) {
        cartDao.getByMenuItemId(menuItemId) != null
    }
}