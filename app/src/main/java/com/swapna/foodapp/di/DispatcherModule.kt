package com.swapna.foodapp.di

import com.swapna.foodapp.utils.DefaultDispatcher
import com.swapna.foodapp.utils.IoDispatcher
import com.swapna.foodapp.utils.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {

    // ── IO Dispatcher ──────────────────────────────────────────
    // Use for: network calls, Room reads/writes, file I/O
    // All Repository implementations inject this
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    // ── Main Dispatcher ────────────────────────────────────────
    // Use for: UI updates that must run on main thread
    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    // ── Default Dispatcher ─────────────────────────────────────
    // Use for: CPU-intensive work (sorting, filtering large lists)
    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default
}