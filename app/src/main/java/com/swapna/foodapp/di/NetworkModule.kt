package com.swapna.foodapp.di

import com.swapna.foodapp.BuildConfig
import com.swapna.foodapp.data.remote.api.FoodApi
import com.swapna.foodapp.utils.AppConstants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)   // Lives as long as the app process
object NetworkModule {

    // ── OkHttpClient ──────────────────────────────────────────
    // Handles timeouts and logging
    // Logging only in DEBUG builds — never log in release
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(AppConstants.CONNECT_TIMEOUT_SEC, TimeUnit.SECONDS)
            .readTimeout(AppConstants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .writeTimeout(AppConstants.READ_TIMEOUT_SEC, TimeUnit.SECONDS)
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = if (BuildConfig.DEBUG)
                        HttpLoggingInterceptor.Level.BODY   // Full request+response logs
                    else
                        HttpLoggingInterceptor.Level.NONE   // Silent in production
                }
            )
            .build()

    // ── Retrofit ──────────────────────────────────────────────
    // BASE_URL comes from BuildConfig — set in build.gradle.kts (Day 1)
    // For your GitHub Pages: "https://YOUR_USERNAME.github.io/zomato-mock-api/"
    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

    // ── FoodApi ────────────────────────────────────────────────
    // Retrofit generates the implementation of FoodApi interface at runtime
    @Provides
    @Singleton
    fun provideFoodApi(retrofit: Retrofit): FoodApi =
        retrofit.create(FoodApi::class.java)
}