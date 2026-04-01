package com.swapna.foodapp.di

import com.squareup.moshi.Moshi
import com.swapna.foodapp.data.remote.api.RestaurantApi
import com.swapna.foodapp.data.repository.RestaurantRepositoryImpl
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton
import com.swapna.foodapp.domain.repository.RestaurantRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.swapna.foodapp.domain.usecase.GetRestaurantsUseCase

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApi(): RestaurantApi {

        val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

        return Retrofit.Builder()
            .baseUrl("https://raw.githubusercontent.com/")
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(RestaurantApi::class.java)
    }

    @Provides
    @Singleton
    fun provideRepository(api: RestaurantApi): RestaurantRepository {
        return RestaurantRepositoryImpl(api)
    }

    @Provides
    fun provideUseCase(repo: RestaurantRepository): GetRestaurantsUseCase {
        return GetRestaurantsUseCase(repo)
    }
}