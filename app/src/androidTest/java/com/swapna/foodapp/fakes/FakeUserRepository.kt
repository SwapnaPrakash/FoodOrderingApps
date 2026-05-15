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
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_FULL
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_ID
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LAT
import com.swapna.foodapp.utils.AndroidTestConstants.HOME_ADDRESS_LNG
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUserRepository : UserRepository {

    private val _currentUser = MutableStateFlow<User?>(null)
    private val _orders = MutableStateFlow<List<Order>>(emptyList())

    var currentUser: User?
        get() = _currentUser.value
        set(value) {
            _currentUser.value = value
        }

    var logoutCalled: Boolean = false
    var updateUserCalled: Boolean = false
    var updateUserResult: Result<Unit> = Result.success(Unit)
    var deleteAddressCalled: Boolean = false
    var lastDeletedAddressId: String? = null
    var saveSelectedLocationCalled: Boolean = false
    var lastSavedLocation: String? = null

    // ── Auth state ────────────────────────────────────────────
    var loggedIn: Boolean = false
    var sendOtpResult: Result<Unit> = Result.success(Unit)
    var verifyOtpResult: Result<User> = Result.success(
        User(
            id = FAKE_USER_ID_U1,
            name = FAKE_USER_NAME_TEST,
            email = FAKE_USER_EMAIL_EMPTY,
            phone = FAKE_USER_PHONE,
        )
    )

    // ── Helpers ───────────────────────────────────────────────
    fun setUser(user: User?) {
        _currentUser.value = user
    }

    fun setOrders(orders: List<Order>) {
        _orders.value = orders
    }

    fun setUserWithLocation(location: String) {
        _currentUser.value = (_currentUser.value ?: fakeUser()).copy(
            selectedLocation = location
        )
    }

    fun setUserWithAddresses(vararg addresses: Address) {
        _currentUser.value = (_currentUser.value ?: fakeUser()).copy(
            addresses = addresses.toList()
        )
    }

    // ── UserRepository overrides ──────────────────────────────
    override fun getCurrentUser(): Flow<User?> = _currentUser

    override fun getRecentOrders(): Flow<List<Order>> = _orders

    override suspend fun sendOtp(phone: String): Result<Unit> = sendOtpResult

    override suspend fun verifyOtp(otp: String): Result<User> = verifyOtpResult

    override fun isLoggedIn(): Boolean = loggedIn

    override suspend fun logout() {
        loggedIn = false
        logoutCalled = true
    }

    override suspend fun getUser(): Result<User> =
        Result.failure(Exception(FAKE_ERR_NOT_IMPLEMENTED))

    override suspend fun updateUser(user: User): Result<Unit> {
        updateUserCalled = true
        if (updateUserResult.isSuccess) {
            _currentUser.value = user
        }
        return updateUserResult
    }

    override suspend fun saveSelectedLocation(location: String) {
        saveSelectedLocationCalled = true
        lastSavedLocation = location
    }

    override suspend fun addAddress(address: Address) {
        val current = _currentUser.value ?: return
        _currentUser.value = current.copy(addresses = current.addresses + address)
    }

    override suspend fun deleteAddress(addressId: String) {
        deleteAddressCalled = true
        lastDeletedAddressId = addressId
        _currentUser.value = _currentUser.value?.copy(
            addresses = _currentUser.value?.addresses?.filter { it.id != addressId } ?: emptyList()
        )
    }

    companion object {
        fun fakeUser(
            id: String = FAKE_USER_ID_U1,
            name: String = FAKE_USER_NAME_TEST,
            email: String = FAKE_USER_EMAIL_EMPTY,
            phone: String = FAKE_USER_PHONE,
            addresses: List<Address> = emptyList(),
        ) = User(
            id = id,
            name = name,
            email = email,
            phone = phone,
            addresses = addresses,
        )

        fun fakeHomeAddress() = Address(
            id = HOME_ADDRESS_ID,
            label = HOME_ADDRESS_LABEL_HOME,
            fullAddress = HOME_ADDRESS_FULL,
            landmark = "",
            latitude = HOME_ADDRESS_LAT,
            longitude = HOME_ADDRESS_LNG,
        )

    }
}