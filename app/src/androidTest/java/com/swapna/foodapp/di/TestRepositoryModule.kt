package com.swapna.foodapp.di

import com.swapna.foodapp.domain.repository.CartRepository
import com.swapna.foodapp.domain.repository.RestaurantRepository
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.fakes.FakeCartRepository
import com.swapna.foodapp.fakes.FakeRestaurantRepository
import com.swapna.foodapp.fakes.FakeUserRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.components.SingletonComponent
import dagger.hilt.testing.TestInstallIn
import javax.inject.Singleton

@Module
@TestInstallIn(
    components = [SingletonComponent::class],
    replaces = [RepositoryModule::class]
)
object TestRepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository(): UserRepository =
        FakeUserRepository()

    @Provides
    @Singleton
    fun provideCartRepository(): CartRepository =
        FakeCartRepository()

    @Provides
    @Singleton
    fun provideRestaurantRepository(): RestaurantRepository =
        FakeRestaurantRepository()
}