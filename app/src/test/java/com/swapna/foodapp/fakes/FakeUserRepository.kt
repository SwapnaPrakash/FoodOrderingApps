package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    // ── Control flags per test ────────────────────────────────
    var sendOtpResult:    Result<Unit> = Result.success(Unit)
    var verifyOtpResult:  Result<User> = Result.success(fakeUser())

    // ✅ New: control getUser + updateUser results
    var getUserResult:    Result<User> = Result.success(fakeUser())
    var updateUserResult: Result<Unit> = Result.success(Unit)

    // ✅ Track calls for verification
    var updateUserCalled = false
    var lastUpdatedUser: User? = null
    var logoutCalled     = false

    // ── Auth methods ──────────────────────────────────────────

    override suspend fun sendOtp(phone: String) =
        sendOtpResult

    override suspend fun verifyOtp(otp: String) =
        verifyOtpResult

    override fun isLoggedIn(): Boolean = false

    // ── Profile methods ───────────────────────────────────────

    override suspend fun getUser(): Result<User> =
        getUserResult

    override suspend fun updateUser(
        user: User,
    ): Result<Unit> {
        updateUserCalled = true
        lastUpdatedUser  = user
        return updateUserResult
    }

    override fun getRecentOrders(): Flow<List<Order>> =
        flowOf(emptyList())

    override suspend fun logout() {
        logoutCalled = true
    }

    // ── Helpers ───────────────────────────────────────────────

    fun resetTracking() {
        updateUserCalled = false
        lastUpdatedUser  = null
        logoutCalled     = false
    }

    companion object {
        fun fakeUser() = User(
            id           = "u_001",
            name         = "Swapna Prakash",
            email        = "swapna@email.com",
            phone        = "9876543210",
            profileImage = "",
            addresses    = emptyList(),
        )

        fun fakeUserWithEmail(email: String) =
            fakeUser().copy(email = email)

        fun fakeUserWithName(name: String) =
            fakeUser().copy(name = name)
    }
}