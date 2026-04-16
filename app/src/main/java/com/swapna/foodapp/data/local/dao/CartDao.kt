package com.swapna.foodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swapna.foodapp.data.local.entity.CartItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CartDao {

    // Flow — CartViewModel observes this
    // Every insert/update/delete auto-notifies all observers
    @Query("SELECT * FROM cart_items ORDER BY added_at ASC")
    fun getAllItems(): Flow<List<CartItemEntity>>

    // Flow — HomeScreen uses this for badge count
    @Query("SELECT COUNT(*) FROM cart_items")
    fun getItemCount(): Flow<Int>

    // Used to check if item already in cart before adding
    @Query("SELECT * FROM cart_items WHERE menu_item_id = :menuItemId LIMIT 1")
    suspend fun getByMenuItemId(menuItemId: String): CartItemEntity?

    // OnConflictStrategy.REPLACE handles both insert and upsert
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: CartItemEntity)

    // Direct SQL update — faster than read-modify-write
    @Query("UPDATE cart_items SET quantity = :qty WHERE id = :id")
    suspend fun updateQuantity(id: String, qty: Int)

    @Query("DELETE FROM cart_items WHERE id = :id")
    suspend fun deleteById(id: String)

    // Called when order is placed
    @Query("DELETE FROM cart_items")
    suspend fun clearAll()

    // Used by CalculateCartTotalUseCase via Repository
    @Query("SELECT * FROM cart_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): CartItemEntity?

    @Query("SELECT quantity FROM cart_items WHERE id = :itemId LIMIT 1")
    suspend fun getQuantityById(itemId: String): Int?
}