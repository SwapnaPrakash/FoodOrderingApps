package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {

    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(otp: String): Result<User>
    fun isLoggedIn(): Boolean
    suspend fun logout()

    fun getCurrentUser(): Flow<User?>

    suspend fun getUser(): Result<User>

    suspend fun updateUser(user: User): Result<Unit>

    suspend fun saveSelectedLocation(location: String)

    suspend fun addAddress(address: Address)

    suspend fun deleteAddress(addressId: String)

    fun getRecentOrders(): Flow<List<Order>>

}