package com.swapna.foodapp.presentation.common.fakes

import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    // ── Control flags ─────────────────────────────────────────────────
    var sendOtpResult:    Result<Unit> = Result.success(Unit)
    var verifyOtpResult:  Result<User> = Result.success(fakeUser())
    var updateUserResult: Result<Unit> = Result.success(Unit)
    var isLoggedInResult: Boolean      = false

    // ── Tracking flags ────────────────────────────────────────────────
    var updateUserCalled           = false
    var lastUpdatedUser:     User? = null
    var logoutCalled               = false

    // ✅ FIXED: Location tracking was missing
    var saveSelectedLocationCalled = false
    var lastSavedLocation          = ""

    // ✅ FIXED: Address tracking was missing
    var addAddressCalled           = false
    var deleteAddressCalled        = false
    var lastDeletedAddressId       = ""

    // ── In-memory user state ──────────────────────────────────────────
    // WHY MutableStateFlow?
    // getCurrentUser() returns Flow<User?>
    // Tests can push new users → ViewModel reacts reactively
    // Mirrors real Room behavior
    private val _userFlow = MutableStateFlow<User?>(null)

    // ── Auth ──────────────────────────────────────────────────────────
    override suspend fun sendOtp(phone: String) = sendOtpResult

    override suspend fun verifyOtp(otp: String): Result<User> {
        return verifyOtpResult.also { result ->
            result.onSuccess { user ->
                _userFlow.value = user
            }
        }
    }

    // ✅ FIXED: uses isLoggedInResult flag
    override fun isLoggedIn(): Boolean = isLoggedInResult

    override suspend fun logout() {
        logoutCalled   = true
        _userFlow.value = null
    }

    override fun getCurrentUser(): Flow<User?> = _userFlow.asStateFlow()

    // ── Profile ───────────────────────────────────────────────────────
    override suspend fun getUser(): Result<User> {
        val user = _userFlow.value
        return if (user != null) Result.success(user)
        else Result.failure(Exception("User not found"))
    }

    override suspend fun updateUser(user: User): Result<Unit> {
        updateUserCalled = true
        lastUpdatedUser  = user
        return updateUserResult.also { result ->
            result.onSuccess {
                _userFlow.value = _userFlow.value?.copy(
                    name  = user.name,
                    email = user.email,
                ) ?: user
            }
        }
    }

    // ── Location ──────────────────────────────────────────────────────
    // ✅ FIXED: was not tracking + not updating userFlow
    override suspend fun saveSelectedLocation(location: String) {
        saveSelectedLocationCalled = true
        lastSavedLocation          = location

        // Update flow so ViewModel observer sees new location
        _userFlow.value = _userFlow.value?.copy(
            selectedLocation = location,
        )
    }

    // ── Address ───────────────────────────────────────────────────────
    override suspend fun addAddress(address: Address) {
        addAddressCalled = true
        val current = _userFlow.value ?: return
        _userFlow.value = current.copy(
            addresses = current.addresses + address
        )
    }

    override suspend fun deleteAddress(addressId: String) {
        deleteAddressCalled  = true
        lastDeletedAddressId = addressId
        val current = _userFlow.value ?: return
        _userFlow.value = current.copy(
            addresses = current.addresses.filterNot { it.id == addressId }
        )
    }

    // ── Orders ────────────────────────────────────────────────────────
    override fun getRecentOrders(): Flow<List<Order>> =
        flowOf(emptyList())

    // ── Test setup helpers ────────────────────────────────────────────
    // WHY these helpers?
    // Tests call setUser() BEFORE createViewModel()
    // ViewModel's observeUserProfile() collects immediately
    // with UnconfinedTestDispatcher → helpers control what it sees

    // ✅ FIXED: was missing
    fun setUser(user: User?) {
        _userFlow.value = user
    }

    // ✅ FIXED: was missing
    fun setLoggedInUser() {
        _userFlow.value = fakeUser()
    }

    // ✅ FIXED: was missing
    fun setUserWithLocation(location: String) {
        _userFlow.value = fakeUser().copy(selectedLocation = location)
    }

    // ✅ FIXED: was missing
    fun setUserWithAddresses(vararg addresses: Address) {
        _userFlow.value = fakeUser().copy(addresses = addresses.toList())
    }

    // Reset all tracking between tests
    fun resetTracking() {
        updateUserCalled           = false
        lastUpdatedUser            = null
        logoutCalled               = false
        saveSelectedLocationCalled = false
        lastSavedLocation          = ""
        addAddressCalled           = false
        deleteAddressCalled        = false
        lastDeletedAddressId       = ""
    }

    companion object {

        fun fakeUser(
            id:              String = "u_001",
            name:            String = "Swapna Prakash",
            email:           String = "swapna@email.com",
            phone:           String = "9876543210",
            selectedLocation:String = "",
        ) = User(
            id               = id,
            name             = name,
            email            = email,
            phone            = phone,
            profileImage     = "",
            addresses        = emptyList(),
            selectedLocation = selectedLocation,
        )

        fun fakeHomeAddress() = Address(
            id          = "a1",
            label       = "Home",
            fullAddress = "123 MG Road, Koramangala",
            landmark    = "Near Forum Mall",
        )

        fun fakeWorkAddress() = Address(
            id          = "a2",
            label       = "Work",
            fullAddress = "RMZ Ecospace, Bellandur",
            landmark    = "Near Outer Ring Road",
        )
    }
}