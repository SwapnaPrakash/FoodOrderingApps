package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    var sendOtpResult: Result<Unit> =
        Result.success(Unit)

    var verifyOtpResult: Result<User> =
        Result.success(fakeUser())

    override suspend fun sendOtp(phone: String) =
        sendOtpResult

    override suspend fun verifyOtp(otp: String) =
        verifyOtpResult

    override fun isLoggedIn(): Boolean = false

    override suspend fun getUser() =
        Result.success(fakeUser())

    override suspend fun updateUser(user: User) =
        Result.success(Unit)

    override fun getRecentOrders(): Flow<List<Order>> =
        flowOf(emptyList())

    override suspend fun logout() {}

    companion object {
        fun fakeUser() = User(
            id           = "u_001",
            name         = "Test User",
            email        = "test@foodapp.com",
            phone        = "9876543210",
            profileImage = "",
            addresses    = emptyList(),
        )
    }
}