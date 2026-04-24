package com.swapna.foodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swapna.foodapp.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {

    @Query(
        """
        SELECT * FROM menu_items 
        WHERE restaurant_id = :restaurantId 
        ORDER BY category ASC, name ASC
    """
    )
    fun getMenuByRestaurant(restaurantId: String): Flow<List<MenuItemEntity>>

    @Query("SELECT * FROM menu_items WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MenuItemEntity?

    @Query(
        """
        SELECT * FROM menu_items 
        WHERE restaurant_id = :restaurantId AND is_recommended = 1 
        LIMIT 5
    """
    )
    suspend fun getRecommended(restaurantId: String): List<MenuItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<MenuItemEntity>)

    // Clear old cache for a restaurant before inserting fresh
    @Query("DELETE FROM menu_items WHERE restaurant_id = :restaurantId")
    suspend fun clearForRestaurant(restaurantId: String)
}