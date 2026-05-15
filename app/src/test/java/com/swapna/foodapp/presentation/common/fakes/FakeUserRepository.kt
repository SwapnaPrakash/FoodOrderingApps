package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.TestConstants
import com.swapna.foodapp.utils.TestConstants.ERR_USER_NOT_FOUND
import com.swapna.foodapp.utils.TestConstants.FAKE_USER_EMAIL
import com.swapna.foodapp.utils.TestConstants.FAKE_USER_ID
import com.swapna.foodapp.utils.TestConstants.FAKE_USER_NAME
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    var sendOtpResult: Result<Unit> = Result.success(Unit)
    var verifyOtpResult: Result<User> = Result.success(fakeUser())
    var updateUserResult: Result<Unit> = Result.success(Unit)
    var isLoggedInResult: Boolean = false

    var updateUserCalled = false
    var lastUpdatedUser: User? = null
    var logoutCalled = false

    var saveSelectedLocationCalled = false
    var lastSavedLocation = ""

    var addAddressCalled = false
    var deleteAddressCalled = false
    var lastDeletedAddressId = ""

    private val _userFlow = MutableStateFlow<User?>(null)

    override suspend fun sendOtp(phone: String) = sendOtpResult

    override suspend fun verifyOtp(otp: String): Result<User> {
        return verifyOtpResult.also { result ->
            result.onSuccess { user ->
                _userFlow.value = user
            }
        }
    }

    override fun isLoggedIn(): Boolean = isLoggedInResult

    override suspend fun logout() {
        logoutCalled = true
        _userFlow.value = null
    }

    override fun getCurrentUser(): Flow<User?> = _userFlow.asStateFlow()

    override suspend fun getUser(): Result<User> {
        val user = _userFlow.value
        return if (user != null) Result.success(user)
        else Result.failure(Exception(ERR_USER_NOT_FOUND))
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        updateUserCalled = true
        lastUpdatedUser = user
        return updateUserResult.also { result ->
            result.onSuccess {
                _userFlow.value = _userFlow.value?.copy(
                    name = user.name,
                    email = user.email,
                ) ?: user
            }
        }
    }

    override suspend fun saveSelectedLocation(location: String) {
        saveSelectedLocationCalled = true
        lastSavedLocation = location

        // Update flow so ViewModel observer sees new location
        _userFlow.value = _userFlow.value?.copy(
            selectedLocation = location,
        )
    }

    override suspend fun addAddress(address: Address) {
        addAddressCalled = true
        val current = _userFlow.value ?: return
        _userFlow.value = current.copy(
            addresses = current.addresses + address
        )
    }

    override suspend fun deleteAddress(addressId: String) {
        deleteAddressCalled = true
        lastDeletedAddressId = addressId
        val current = _userFlow.value ?: return
        _userFlow.value = current.copy(
            addresses = current.addresses.filterNot { it.id == addressId }
        )
    }

    override fun getRecentOrders(): Flow<List<Order>> =
        flowOf(emptyList())

    fun setUser(user: User?) {
        _userFlow.value = user
    }

    fun setLoggedInUser() {
        _userFlow.value = fakeUser()
    }

    fun setUserWithLocation(location: String) {
        _userFlow.value = fakeUser().copy(selectedLocation = location)
    }

    fun setUserWithAddresses(vararg addresses: Address) {
        _userFlow.value = fakeUser().copy(addresses = addresses.toList())
    }

    // Reset all tracking between tests
    fun resetTracking() {
        updateUserCalled = false
        lastUpdatedUser = null
        logoutCalled = false
        saveSelectedLocationCalled = false
        lastSavedLocation = ""
        addAddressCalled = false
        deleteAddressCalled = false
        lastDeletedAddressId = ""
    }

    companion object {

        fun fakeUser(
            id: String = FAKE_USER_ID,
            name: String = FAKE_USER_NAME,
            email: String = FAKE_USER_EMAIL,
            phone: String = TestConstants.VALID_PHONE,
            selectedLocation: String = "",
        ) = User(
            id = id,
            name = name,
            email = email,
            phone = phone,
            profileImage = "",
            addresses = emptyList(),
            selectedLocation = selectedLocation,
        )

        fun fakeHomeAddress() = Address(
            id = TestConstants.ADDRESS_ID_1,
            label = TestConstants.ADDRESS_LABEL_HOME,
            fullAddress = TestConstants.ADDRESS_MG_ROAD,
            landmark = TestConstants.ADDRESS_NEAR_FORUM,
        )

        fun fakeWorkAddress() = Address(
            id = TestConstants.ADDRESS_ID_2,
            label = TestConstants.ADDRESS_LABEL_WORK,
            fullAddress = TestConstants.ADDRESS_RMZ,
            landmark = TestConstants.ADDRESS_NEAR_ORR,
        )
    }


    private val _orders = MutableStateFlow<List<Order>>(emptyList())

    // New helper
    fun setOrders(orders: List<Order>) {
        _orders.value = orders
    }

}