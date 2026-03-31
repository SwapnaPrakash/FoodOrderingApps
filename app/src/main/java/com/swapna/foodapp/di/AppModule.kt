package com.swapna.foodapp.di

import com.swapna.foodapp.data.repository.FakeRestaurantRepository
import com.swapna.foodapp.domain.repository.RestaurantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideRepository(): RestaurantRepository {
        return FakeRestaurantRepository()
    }
}