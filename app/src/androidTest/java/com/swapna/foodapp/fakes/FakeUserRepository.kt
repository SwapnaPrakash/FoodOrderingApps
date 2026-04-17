package com.swapna.foodapp.fakes

import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.OrderItem
import com.swapna.foodapp.utils.testdata.TestConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf

class FakeUserRepository : UserRepository {

    // ── Control flags — set in test to simulate outcomes ──────
    var sendOtpResult:   Result<Unit> = Result.success(Unit)
    var verifyOtpResult: Result<User> = Result.success(fakeUser())
    var isLoggedInResult: Boolean     = false

    // ── Tracking flags — verify what was called ───────────────
    // WHY tracking flags?
    // Tests verify WHAT was called + WHAT args were passed
    // Same pattern as FakeCartRepository
    var updateUserCalled           = false
    var logoutCalled               = false
    var saveSelectedLocationCalled = false
    var addAddressCalled           = false
    var deleteAddressCalled        = false
    var lastSavedLocation          = ""
    var lastAddedAddress: Address? = null
    var lastDeletedAddressId       = ""

    // ── Internal user state ───────────────────────────────────
    // WHY MutableStateFlow?
    // getCurrentUser() returns Flow<User?>
    // Tests can update _userFlow → Flow emits → ViewModel reacts
    // Simulates real Room behavior correctly
    private val _userFlow = MutableStateFlow<User?>(null)

    // ── Seed helpers ──────────────────────────────────────────
    // Set user state before creating ViewModel in tests
    // Same pattern as FakeCartRepository.seedCart()
    fun setUser(user: User?) {
        _userFlow.value = user
    }

    fun setLoggedInUser() {
        _userFlow.value = fakeUser()
    }

    fun setUserWithAddresses(vararg addresses: Address) {
        _userFlow.value = fakeUser().copy(
            addresses = addresses.toList()
        )
    }

    fun setUserWithLocation(location: String) {
        _userFlow.value = fakeUser().copy(
            selectedLocation = location
        )
    }

    // ── Reset tracking flags between tests ────────────────────
    fun resetTracking() {
        updateUserCalled           = false
        logoutCalled               = false
        saveSelectedLocationCalled = false
        addAddressCalled           = false
        deleteAddressCalled        = false
        lastSavedLocation          = ""
        lastAddedAddress           = null
        lastDeletedAddressId       = ""
    }

    // ── UserRepository interface implementations ───────────────

    // ── Auth ──────────────────────────────────────────────────
    override suspend fun sendOtp(
        phone: String,
    ): Result<Unit> = sendOtpResult

    override suspend fun verifyOtp(
        otp: String,
    ): Result<User> = verifyOtpResult

    override fun isLoggedIn(): Boolean = isLoggedInResult

    override suspend fun logout() {
        logoutCalled   = true
        _userFlow.value = null  // clear user on logout
    }

    // ── User Profile ──────────────────────────────────────────

    // ✅ FIX: getCurrentUser() returns Flow<User?> not Result
    // WHY MutableStateFlow?
    // ProfileViewModel.loadUserProfile() collects this Flow
    // Tests can push new values → ViewModel reacts reactively
    // Same as how real UserRepositoryImpl returns Room Flow
    override fun getCurrentUser(): Flow<User?> =
        _userFlow.asStateFlow()

    // One-shot version — kept for backward compat
    override suspend fun getUser(): Result<User> {
        val user = _userFlow.value
        return if (user != null) {
            Result.success(user)
        } else {
            Result.failure(Exception("User not found"))
        }
    }

    // Update name + email
    override suspend fun updateUser(
        user: User,
    ): Result<Unit> {
        updateUserCalled = true
        // ✅ Actually update the flow so ProfileViewModel
        // sees the change reactively
        _userFlow.value = _userFlow.value?.copy(
            name  = user.name,
            email = user.email,
        ) ?: user
        return Result.success(Unit)
    }

    // ✅ FIX: saveSelectedLocation — was missing
    // Called by HomeViewModel when user picks delivery area
    override suspend fun saveSelectedLocation(
        location: String,
    ) {
        saveSelectedLocationCalled = true
        lastSavedLocation          = location
        // Update flow so observers see new location
        _userFlow.value = _userFlow.value?.copy(
            selectedLocation = location,
        )
    }

    // ✅ FIX: addAddress — was missing
    // Called when user saves new delivery address
    override suspend fun addAddress(address: Address) {
        addAddressCalled = true
        lastAddedAddress = address
        // Add to current user's address list
        val current = _userFlow.value ?: return
        _userFlow.value = current.copy(
            addresses = current.addresses + address
        )
    }

    // ✅ FIX: deleteAddress — was missing
    // Called from ProfileScreen delete button
    override suspend fun deleteAddress(addressId: String) {
        deleteAddressCalled  = true
        lastDeletedAddressId = addressId
        // Remove from current user's address list
        val current = _userFlow.value ?: return
        _userFlow.value = current.copy(
            addresses = current.addresses.filterNot {
                it.id == addressId
            }
        )
    }

    // ── Orders ────────────────────────────────────────────────
    // ✅ FIX: getRecentOrders() not getOrders()
    override fun getRecentOrders(): Flow<List<Order>> =
        flowOf(emptyList())

    // ══════════════════════════════════════════════════════════
    companion object {
        // ══════════════════════════════════════════════════════════

        // ── Default fake user ─────────────────────────────────
        // WHY TestConstants not hardcoded strings?
        // Consistent with other fakes + easy to update
        fun fakeUser(
            id:    String = TestConstants.USER_ID,
            name:  String = TestConstants.USER_NAME,
            email: String = TestConstants.USER_EMAIL,
            phone: String = TestConstants.USER_PHONE,
        ) = User(
            id               = id,
            name             = name,
            email            = email,
            phone            = phone,
            profileImage     = "",
            addresses        = emptyList(),
            selectedLocation = "",
        )

        // ── Fake user with addresses ──────────────────────────
        // Used in ProfileViewModelSpec address tests
        fun fakeUserWithAddresses() = fakeUser().copy(
            addresses = listOf(
                fakeHomeAddress(),
                fakeWorkAddress(),
            )
        )

        // ── Fake address helpers ──────────────────────────────
        fun fakeHomeAddress() = Address(
            id          = TestConstants.ADDRESS_ID,
            label       = "Home",
            fullAddress = TestConstants.FULL_ADDRESS,
            landmark    = TestConstants.LANDMARK,
        )

        fun fakeWorkAddress() = Address(
            id          = "a2",
            label       = "Work",
            fullAddress = "Tech Park, Whitefield, Bengaluru",
            landmark    = "Near ITPL",
        )

        // ── Fake orders ───────────────────────────────────────
        fun fakeOrders() = listOf(
            Order(
                id              = TestConstants.ORDER_ID,
                restaurantId    = TestConstants.RESTAURANT_ID,
                restaurantName  = TestConstants.RESTAURANT_NAME,
                restaurantImage = TestConstants.IMAGE_URL,
                status          = "Delivered",
                timeFriendly    = "Yesterday, 1:45 PM",
                totalAmount     = TestConstants.TOTAL_AMOUNT,
                items           = listOf(
                    OrderItem(
                        name     = TestConstants.MENU_NAME,
                        quantity = 2,
                        price    = TestConstants.MENU_PRICE,
                    )
                ),
                canReorder = true,
            )
        )
    }
}