package com.swapna.foodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swapna.foodapp.data.local.entity.RestaurantEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RestaurantDao {

    // Flow — UI observes this and auto-refreshes when data changes
    @Query("SELECT * FROM restaurants ORDER BY rating DESC")
    fun getAllRestaurants(): Flow<List<RestaurantEntity>>

    // Suspend — one-shot read, used in Repository to check cache
    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): RestaurantEntity?

    // OnConflictStrategy.REPLACE — if same ID exists, overwrite it
    // This is how fresh API data replaces stale cached data
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(restaurants: List<RestaurantEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(restaurant: RestaurantEntity)

    @Query("DELETE FROM restaurants")
    suspend fun clearAll()

    // Used to decide if cache is empty (force network fetch)
    @Query("SELECT COUNT(*) FROM restaurants")
    suspend fun count(): Int

    // Used to check if cache is stale
    // Returns oldest cachedAt timestamp in the table
    @Query("SELECT MIN(cached_at) FROM restaurants")
    suspend fun oldestCacheTime(): Long?
}

