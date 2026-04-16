package com.swapna.foodapp.di

import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCase
import com.swapna.foodapp.domain.usecase.cart.AddToCartUseCaseImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UseCaseModule {

    @Binds
    abstract fun bindAddToCartUseCase(
        impl: AddToCartUseCaseImpl,
    ): AddToCartUseCase
}