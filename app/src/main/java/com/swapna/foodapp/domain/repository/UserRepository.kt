package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Auth
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(otp: String): Result<User>
    fun isLoggedIn(): Boolean
    suspend fun logout()

    // Profile
    suspend fun getUser(): Result<User>
    suspend fun updateUser(user: User): Result<Unit>
    fun getRecentOrders(): Flow<List<Order>>
}