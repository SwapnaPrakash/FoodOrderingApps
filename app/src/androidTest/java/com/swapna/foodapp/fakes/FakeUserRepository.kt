package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_ERR_NOT_IMPLEMENTED
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_USER_EMAIL_EMPTY
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_USER_ID_U1
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_USER_NAME_TEST
import com.swapna.foodapp.utils.AndroidTestConstants.FAKE_USER_PHONE
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    private val _currentUser = MutableStateFlow<User?>(null)

    var currentUser: User?
        get() = _currentUser.value
        set(value) {
            _currentUser.value = value
        }

    var loggedIn = false
    var sendOtpResult: Result<Unit> = Result.success(Unit)
    var verifyOtpResult: Result<User> = Result.success(
        User(
            id = FAKE_USER_ID_U1,
            name = FAKE_USER_NAME_TEST,
            email = FAKE_USER_EMAIL_EMPTY,
            phone = FAKE_USER_PHONE,
        )
    )

    override fun getCurrentUser(): Flow<User?> = _currentUser

    override suspend fun sendOtp(phone: String): Result<Unit> = sendOtpResult

    override suspend fun verifyOtp(otp: String): Result<User> = verifyOtpResult

    override fun isLoggedIn(): Boolean = loggedIn

    override suspend fun logout() {
        loggedIn = false
    }

    override suspend fun getUser(): Result<User> =
        Result.failure(Exception(FAKE_ERR_NOT_IMPLEMENTED))

    override suspend fun updateUser(user: User): Result<Unit> = Result.success(Unit)

    override suspend fun saveSelectedLocation(location: String) {}

    override suspend fun addAddress(address: Address) {}

    override suspend fun deleteAddress(addressId: String) {}

    override fun getRecentOrders(): Flow<List<Order>> = flowOf(emptyList())
}