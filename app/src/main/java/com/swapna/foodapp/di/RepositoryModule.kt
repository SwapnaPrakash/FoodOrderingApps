package com.swapna.foodapp.di

import com.swapna.foodapp.data.repository.CartRepositoryImpl
import com.swapna.foodapp.data.repository.RestaurantRepositoryImpl
import com.swapna.foodapp.data.repository.UserRepositoryImpl
import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.repository.UserRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// @Binds = tells Hilt "when someone asks for RestaurantRepository,
// give them RestaurantRepositoryImpl"
// Must be abstract class — Hilt generates the implementation

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindRestaurantRepository(
        impl: RestaurantRepositoryImpl,
    ): RestaurantRepository

    @Binds
    @Singleton
    abstract fun bindCartRepository(
        impl: CartRepositoryImpl,
    ): CartRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryImpl,
    ): UserRepository
}