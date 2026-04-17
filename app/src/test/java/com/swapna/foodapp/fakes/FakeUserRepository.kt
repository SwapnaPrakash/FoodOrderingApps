package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    // ── Control flags per test ────────────────────────────────
    var sendOtpResult:    Result<Unit> = Result.success(Unit)
    var verifyOtpResult:  Result<User> = Result.success(fakeUser())

    var getUserResult:    Result<User> = Result.success(fakeUser())
    var updateUserResult: Result<Unit> = Result.success(Unit)

    // ── Tracking ──────────────────────────────────────────────
    var updateUserCalled = false
    var lastUpdatedUser: User? = null
    var logoutCalled     = false

    // ✅ New tracking
    var savedLocation: String? = null
    var addAddressCalled = false
    var deleteAddressCalled = false
    var lastDeletedAddressId: String? = null

    // ── In-memory state (VERY IMPORTANT for realism) ──────────
    private var currentUser: User = fakeUser()

    private val userFlow = MutableStateFlow<User?>(currentUser)

    // ── Auth ──────────────────────────────────────────────────

    override suspend fun sendOtp(phone: String) = sendOtpResult

    override suspend fun verifyOtp(otp: String): Result<User> {
        return verifyOtpResult.onSuccess {
            currentUser = it
            userFlow.value = it
        }
    }

    override fun isLoggedIn(): Boolean = currentUser.id.isNotEmpty()

    override suspend fun logout() {
        logoutCalled = true
        currentUser = fakeUser().copy(id = "")
        userFlow.value = null
    }

    override fun getCurrentUser(): Flow<User?> = userFlow

    // ── Profile ───────────────────────────────────────────────

    override suspend fun getUser(): Result<User> = getUserResult

    override suspend fun updateUser(user: User): Result<Unit> {
        updateUserCalled = true
        lastUpdatedUser = user

        return updateUserResult.onSuccess {
            currentUser = user
            userFlow.value = user
        }
    }

    // ── Location ──────────────────────────────────────────────

    override suspend fun saveSelectedLocation(location: String) {
        savedLocation = location

        // optional: reflect inside user (if your domain has it)
        currentUser = currentUser.copy(
            // if you have selectedLocation in User model, update here
        )
        userFlow.value = currentUser
    }

    // ── Address ───────────────────────────────────────────────

    override suspend fun addAddress(address: Address) {
        addAddressCalled = true

        val updated = currentUser.copy(
            addresses = currentUser.addresses + address
        )
        currentUser = updated
        userFlow.value = updated
    }

    override suspend fun deleteAddress(addressId: String) {
        deleteAddressCalled = true
        lastDeletedAddressId = addressId

        val updated = currentUser.copy(
            addresses = currentUser.addresses.filterNot {
                it.id == addressId
            }
        )
        currentUser = updated
        userFlow.value = updated
    }

    // ── Orders ────────────────────────────────────────────────

    override fun getRecentOrders(): Flow<List<Order>> =
        flowOf(emptyList())

    // ── Helpers ───────────────────────────────────────────────

    fun resetTracking() {
        updateUserCalled = false
        lastUpdatedUser = null
        logoutCalled = false

        savedLocation = null
        addAddressCalled = false
        deleteAddressCalled = false
        lastDeletedAddressId = null
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