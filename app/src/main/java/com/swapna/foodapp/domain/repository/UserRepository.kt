package com.swapna.foodapp.domain.repository

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    // Auth
    suspend fun sendOtp(phone: String): Result<Unit>
    suspend fun verifyOtp(otp: String): Result<User>
    fun isLoggedIn(): Boolean
    suspend fun logout()

    fun getCurrentUser(): Flow<User?>

    // One-shot version — used internally for updates
    suspend fun getUser(): Result<User>

    // Update name + email
    suspend fun updateUser(user: User): Result<Unit>

    // ✅ FIX: Save selected delivery location
    // HomeViewModel calls this when user picks location
    // Persists across app restarts
    suspend fun saveSelectedLocation(location: String)

    // ✅ FIX: Add saved address
    // LocationPickerSheet adds to user's saved addresses
    suspend fun addAddress(address: Address)

    // ✅ FIX: Delete saved address
    // ProfileScreen delete button calls this
    suspend fun deleteAddress(addressId: String)

    // ── Orders ────────────────────────────────────────────────
    fun getRecentOrders(): Flow<List<Order>>

}