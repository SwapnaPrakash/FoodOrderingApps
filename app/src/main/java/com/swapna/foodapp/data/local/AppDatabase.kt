package com.swapna.foodapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.swapna.foodapp.data.local.converter.Converters
import com.swapna.foodapp.data.local.dao.*
import com.swapna.foodapp.data.local.entity.*
import com.swapna.foodapp.utils.AppConstants

@Database(
    entities = [
        RestaurantEntity::class,
        CartItemEntity::class,
        MenuItemEntity::class,
        UserEntity::class,
    ],
    version  = AppConstants.DB_VERSION,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun restaurantDao(): RestaurantDao
    abstract fun cartDao(): CartDao
    abstract fun menuItemDao(): MenuItemDao
    abstract fun userDao(): UserDao
}
