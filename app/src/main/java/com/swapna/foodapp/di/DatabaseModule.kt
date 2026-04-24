package com.swapna.foodapp.di

import android.content.Context
import androidx.room.Room
import com.swapna.foodapp.data.local.AppDatabase
import com.swapna.foodapp.data.local.dao.CartDao
import com.swapna.foodapp.data.local.dao.MenuItemDao
import com.swapna.foodapp.data.local.dao.RestaurantDao
import com.swapna.foodapp.data.local.dao.UserDao
import com.swapna.foodapp.utils.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppConstants.DB_NAME,
        )
            .fallbackToDestructiveMigration(true)
            .addMigrations(
                AppDatabase.MIGRATION_1_2,
                AppDatabase.MIGRATION_2_3,
            )
            .build()

    @Provides
    fun provideRestaurantDao(db: AppDatabase): RestaurantDao =
        db.restaurantDao()

    @Provides
    fun provideCartDao(db: AppDatabase): CartDao =
        db.cartDao()

    @Provides
    fun provideMenuItemDao(db: AppDatabase): MenuItemDao =
        db.menuItemDao()

    @Provides
    fun provideUserDao(db: AppDatabase): UserDao =
        db.userDao()

}