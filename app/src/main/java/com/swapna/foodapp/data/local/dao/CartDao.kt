package com.swapna.foodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swapna.foodapp.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    @Query("SELECT * FROM cart_items ORDER BY added_at ASC")
    fun getAllItems(): Flow<List<CartItemEntity>>

    @Query("SELECT COUNT(*) FROM cart_items")
    fun getItemCount(): Flow<Int>

    @Query("SELECT * FROM cart_items WHERE menu_item_id = :menuItemId LIMIT 1")
    suspend fun getByMenuItemId(menuItemId: String): CartItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity)

    @Query("UPDATE cart_items SET quantity = :qty WHERE id = :id")
    suspend fun updateQuantity(id: String, qty: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearAll()

    @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CartItemEntity?

    @Query("SELECT quantity FROM cart_items WHERE id = :itemId LIMIT 1")
    suspend fun getQuantityById(itemId: String): Int?
}