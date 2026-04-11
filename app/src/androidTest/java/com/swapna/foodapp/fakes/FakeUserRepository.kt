package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

// ── Inline FakeUserRepository ─────────────────────────────────
// No Hilt injection needed — we pass this directly to ViewModel
// 100% controlled — zero Firebase calls
class FakeUserRepositoryForTest : UserRepository {

    var sendOtpResult: Result<Unit>   = Result.success(Unit)
    var verifyOtpResult: Result<User> = Result.success(testUser())
    var loggedIn: Boolean             = false

    override suspend fun sendOtp(phone: String): Result<Unit> =
        sendOtpResult

    override suspend fun verifyOtp(otp: String): Result<User> =
        verifyOtpResult


    override  fun isLoggedIn(): Boolean = loggedIn

    override suspend fun getUser(): Result<User> =
        Result.success(testUser())

    override suspend fun updateUser(user: User): Result<Unit> =
        Result.success(Unit)

    override fun getRecentOrders(): Flow<List<Order>> =
        flowOf(emptyList())

    override suspend fun logout() { loggedIn = false }

    companion object {
        fun testUser() = User(
            id           = "u_001",
            name         = "Test User",
            email        = "test@foodapp.com",
            phone        = "9876543210",
            profileImage = "",
            addresses    = emptyList(),
        )
    }
}