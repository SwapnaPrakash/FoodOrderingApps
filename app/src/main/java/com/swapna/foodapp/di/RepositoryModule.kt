package com.swapna.foodapp.di

import com.swapna.foodapp.data.repository.*
import com.swapna.foodapp.domain.repository.*
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