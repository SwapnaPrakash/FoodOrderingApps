package com.swapna.foodapp.data.repository

import app.cash.turbine.test
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.swapna.foodapp.data.auth.ActivityProvider
import com.swapna.foodapp.data.auth.FirebaseAuthManager
import com.swapna.foodapp.data.local.dao.UserDao
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.data.mapper.UserMapper
import com.swapna.foodapp.data.remote.api.FoodApi
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.utils.TestConstants.ADDRESS_FULL_1
import com.swapna.foodapp.utils.TestConstants.ADDRESS_FULL_2
import com.swapna.foodapp.utils.TestConstants.ADDRESS_FULL_3
import com.swapna.foodapp.utils.TestConstants.ADDRESS_ID_1
import com.swapna.foodapp.utils.TestConstants.ADDRESS_ID_2
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_HOME
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LABEL_WORK
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LOC_HSR
import com.swapna.foodapp.utils.TestConstants.ADDRESS_LOC_KORAMANGALA
import com.swapna.foodapp.utils.TestConstants.ERR_BACKGROUND_MSG
import com.swapna.foodapp.utils.TestConstants.ERR_DB
import com.swapna.foodapp.utils.TestConstants.ERR_FIREBASE
import com.swapna.foodapp.utils.TestConstants.ERR_NETWORK
import com.swapna.foodapp.utils.TestConstants.ERR_RATE_LIMIT
import com.swapna.foodapp.utils.TestConstants.ERR_TIMEOUT
import com.swapna.foodapp.utils.TestConstants.ERR_WRONG_OTP_MSG
import com.swapna.foodapp.utils.TestConstants.ORDER_COUNT_1
import com.swapna.foodapp.utils.TestConstants.ORDER_ID_1
import com.swapna.foodapp.utils.TestConstants.USER_EMAIL_FIREBASE
import com.swapna.foodapp.utils.TestConstants.USER_EMAIL_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_ID_1
import com.swapna.foodapp.utils.TestConstants.USER_ID_API
import com.swapna.foodapp.utils.TestConstants.USER_ID_FIREBASE
import com.swapna.foodapp.utils.TestConstants.USER_ID_GUEST
import com.swapna.foodapp.utils.TestConstants.USER_ID_UID
import com.swapna.foodapp.utils.TestConstants.USER_NAME_FIREBASE
import com.swapna.foodapp.utils.TestConstants.USER_NAME_SWAPNA
import com.swapna.foodapp.utils.TestConstants.USER_PHONE_VALID
import com.swapna.foodapp.utils.TestConstants.VALID_OTP
import com.swapna.foodapp.utils.fakeOrdersResponse
import com.swapna.foodapp.utils.fakeUser
import com.swapna.foodapp.utils.fakeUserEntity
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class UserRepositoryImplSpec : FunSpec({

    val api = mockk<FoodApi>()
    val userDao = mockk<UserDao>()
    val firebaseAuthManager = mockk<FirebaseAuthManager>()
    val firebaseAuth = mockk<FirebaseAuth>()
    val activityProvider = mockk<ActivityProvider>()
    val userMapper = UserMapper()
    val entityMapper = EntityMapper()

    fun createRepo() = UserRepositoryImpl(
        api = api,
        userDao = userDao,
        firebaseAuthManager = firebaseAuthManager,
        firebaseAuth = firebaseAuth,
        activityProvider = activityProvider,
        userMapper = userMapper,
        entityMapper = entityMapper,
        ioDispatcher = UnconfinedTestDispatcher(),
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { activityProvider.getActivity() } returns null
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns null
        coEvery { userDao.getCurrentUserOnce() } returns null
        coEvery { userDao.getUser() } returns null
        coEvery { userDao.insertUser(any()) } just runs
        coEvery { userDao.clearUser() } just runs
        coEvery { userDao.updateNameAndEmail(any(), any(), any()) } just runs
        coEvery { userDao.updateSelectedLocation(any(), any()) } just runs
        coEvery { userDao.updateAddresses(any(), any()) } just runs
        coEvery { firebaseAuthManager.signOut() } just runs
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // sendOtp
    // ══════════════════════════════════════════════════════════

    test("sendOtp: returns failure when activity is null — app in background") {
        val result = createRepo().sendOtp(USER_PHONE_VALID)

        result.isFailure shouldBe true
        result.exceptionOrNull()?.message shouldBe ERR_BACKGROUND_MSG
    }

    test("sendOtp: delegates to firebaseAuthManager when activity available") {
        val fakeActivity = mockk<android.app.Activity>()
        every { activityProvider.getActivity() } returns fakeActivity
        coEvery {
            firebaseAuthManager.sendOtp(any(), any())
        } returns Result.success(Unit)

        val result = createRepo().sendOtp(USER_PHONE_VALID)

        result.isSuccess shouldBe true
        coVerify { firebaseAuthManager.sendOtp(USER_PHONE_VALID, fakeActivity) }
    }

    test("sendOtp: returns failure when firebaseAuthManager throws") {
        val fakeActivity = mockk<android.app.Activity>()
        every { activityProvider.getActivity() } returns fakeActivity
        coEvery {
            firebaseAuthManager.sendOtp(any(), any())
        } throws Exception(ERR_FIREBASE)

        val result = createRepo().sendOtp(USER_PHONE_VALID)

        result.isFailure shouldBe true
    }

    test("sendOtp: failure message preserved when firebaseAuthManager throws") {
        val fakeActivity = mockk<android.app.Activity>()
        every { activityProvider.getActivity() } returns fakeActivity
        coEvery {
            firebaseAuthManager.sendOtp(any(), any())
        } throws Exception(ERR_RATE_LIMIT)

        val result = createRepo().sendOtp(USER_PHONE_VALID)

        result.exceptionOrNull()?.message shouldBe ERR_RATE_LIMIT
    }

    // ══════════════════════════════════════════════════════════
    // verifyOtp
    // ══════════════════════════════════════════════════════════

    test("verifyOtp: returns failure when firebaseAuthManager verifyOtp fails") {
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.failure(Exception(ERR_WRONG_OTP_MSG))

        val result = createRepo().verifyOtp(VALID_OTP)

        result.isFailure shouldBe true
    }

    test("verifyOtp: returns failure when API throws after OTP verified") {
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.success(USER_ID_UID)
        coEvery { api.getUser() } throws Exception(ERR_NETWORK)

        val result = createRepo().verifyOtp(VALID_OTP)

        result.isFailure shouldBe true
    }

    test("verifyOtp: saves user to Room on success") {
        val fakeUserResponse = fakeUserApiResponse()
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.success(USER_ID_UID)
        coEvery { api.getUser() } returns fakeUserResponse

        createRepo().verifyOtp(VALID_OTP)

        coVerify { userDao.insertUser(any()) }
    }

    test("verifyOtp: returns success with user on valid OTP") {
        val fakeUserResponse = fakeUserApiResponse()
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.success(USER_ID_UID)
        coEvery { api.getUser() } returns fakeUserResponse

        val result = createRepo().verifyOtp(VALID_OTP)

        result.isSuccess shouldBe true
        result.getOrNull()?.id shouldBe USER_ID_UID
    }

    // ══════════════════════════════════════════════════════════
    // isLoggedIn
    // ══════════════════════════════════════════════════════════

    test("isLoggedIn: returns true when Firebase signed in") {
        every { firebaseAuthManager.isSignedIn() } returns true
        createRepo().isLoggedIn() shouldBe true
    }

    test("isLoggedIn: returns false when not signed in") {
        every { firebaseAuthManager.isSignedIn() } returns false
        createRepo().isLoggedIn() shouldBe false
    }

    // ══════════════════════════════════════════════════════════
    // getCurrentUser
    // ══════════════════════════════════════════════════════════

    test("getCurrentUser: emits null when Room empty and no Firebase user") {
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns null

        createRepo().getCurrentUser().test {
            awaitItem() shouldBe null
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: emits mapped User when Room has entity") {
        val entity = fakeUserEntity(id = USER_ID_1, name = USER_NAME_SWAPNA)

        every { userDao.getCurrentUser() } returns flowOf(entity)

        createRepo().getCurrentUser().test {
            val result = awaitItem()
            result shouldNotBe null
            result?.id shouldBe USER_ID_1
            result?.name shouldBe USER_NAME_SWAPNA
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: email mapped correctly from entity") {
        val entity = fakeUserEntity(id = USER_ID_1, name = USER_NAME_SWAPNA)
        every { userDao.getCurrentUser() } returns flowOf(entity)

        createRepo().getCurrentUser().test {
            awaitItem()?.email shouldBe USER_EMAIL_SWAPNA
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: falls back to Firebase user when Room empty") {
        val fbUser = mockk<FirebaseUser> {
            every { uid } returns USER_ID_FIREBASE
            every { displayName } returns USER_NAME_FIREBASE
            every { email } returns USER_EMAIL_FIREBASE
            every { phoneNumber } returns USER_PHONE_VALID
        }
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns fbUser

        createRepo().getCurrentUser().test {
            val result = awaitItem()
            result shouldNotBe null
            result?.id shouldBe USER_ID_FIREBASE
            result?.name shouldBe USER_NAME_FIREBASE
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: Firebase fallback phone set correctly") {
        val fbUser = mockk<FirebaseUser> {
            every { uid } returns USER_ID_FIREBASE
            every { displayName } returns ""
            every { email } returns ""
            every { phoneNumber } returns USER_PHONE_VALID
        }
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns fbUser

        createRepo().getCurrentUser().test {
            awaitItem()?.phone shouldBe USER_PHONE_VALID
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: Firebase fallback handles null displayName email phone") {
        val fbUser = mockk<FirebaseUser> {
            every { uid } returns USER_ID_FIREBASE
            every { displayName } returns null
            every { email } returns null
            every { phoneNumber } returns null
        }
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns fbUser

        createRepo().getCurrentUser().test {
            val result = awaitItem()
            result?.name shouldBe ""
            result?.email shouldBe ""
            result?.phone shouldBe ""
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ══════════════════════════════════════════════════════════
    // getUser
    // ══════════════════════════════════════════════════════════

    test("getUser: returns cached user from Room without API call") {
        val entity = fakeUserEntity(id = USER_ID_1, name = USER_NAME_SWAPNA)
        coEvery { userDao.getUser() } returns entity

        val result = createRepo().getUser()

        result.isSuccess shouldBe true
        result.getOrNull()?.name shouldBe USER_NAME_SWAPNA
        coVerify(exactly = 0) { api.getUser() }
    }

    test("getUser: fetches from API when Room is empty") {
        coEvery { userDao.getUser() } returns null
        coEvery { api.getUser() } returns fakeUserApiResponse()

        val result = createRepo().getUser()

        result.isSuccess shouldBe true
        coVerify { api.getUser() }
    }

    test("getUser: saves API response to Room when cache empty") {
        coEvery { userDao.getUser() } returns null
        coEvery { api.getUser() } returns fakeUserApiResponse()

        createRepo().getUser()

        coVerify { userDao.insertUser(any()) }
    }

    test("getUser: returns failure when API throws and no cache") {
        coEvery { userDao.getUser() } returns null
        coEvery { api.getUser() } throws Exception(ERR_NETWORK)

        val result = createRepo().getUser()

        result.isFailure shouldBe true
    }

    test("getUser: failure message preserved from exception") {
        coEvery { userDao.getUser() } returns null
        coEvery { api.getUser() } throws Exception(ERR_TIMEOUT)

        val result = createRepo().getUser()

        result.exceptionOrNull()?.message shouldBe ERR_TIMEOUT
    }

    // ══════════════════════════════════════════════════════════
    // logout
    // ══════════════════════════════════════════════════════════

    test("logout: calls firebaseAuthManager.signOut") {
        createRepo().logout()
        coVerify { firebaseAuthManager.signOut() }
    }

    test("logout: clears user from Room") {
        createRepo().logout()
        coVerify { userDao.clearUser() }
    }

    // ══════════════════════════════════════════════════════════
    // updateUser
    // ══════════════════════════════════════════════════════════

    test("updateUser: calls userDao.updateNameAndEmail with correct params") {
        val user = fakeUser(id = USER_ID_1, name = USER_NAME_SWAPNA, email = USER_EMAIL_SWAPNA)

        createRepo().updateUser(user)

        coVerify {
            userDao.updateNameAndEmail(
                id = USER_ID_1,
                name = USER_NAME_SWAPNA,
                email = USER_EMAIL_SWAPNA,
            )
        }
    }

    test("updateUser: returns success when dao succeeds") {
        createRepo().updateUser(fakeUser()).isSuccess shouldBe true
    }

    test("updateUser: returns failure when dao throws") {
        coEvery {
            userDao.updateNameAndEmail(any(), any(), any())
        } throws Exception(ERR_DB)

        createRepo().updateUser(fakeUser()).isFailure shouldBe true
    }

    test("updateUser: failure message preserved from exception") {
        coEvery {
            userDao.updateNameAndEmail(any(), any(), any())
        } throws Exception(ERR_DB)

        createRepo().updateUser(fakeUser())
            .exceptionOrNull()?.message shouldBe ERR_DB
    }

    // ══════════════════════════════════════════════════════════
    // saveSelectedLocation
    // ══════════════════════════════════════════════════════════

    test("saveSelectedLocation: updates existing user location in Room") {
        val entity = fakeUserEntity(USER_ID_1, USER_NAME_SWAPNA)
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().saveSelectedLocation(ADDRESS_LOC_KORAMANGALA)

        coVerify {
            userDao.updateSelectedLocation(
                id = USER_ID_1,
                location = ADDRESS_LOC_KORAMANGALA,
            )
        }
    }

    test("saveSelectedLocation: inserts guest user when no user in Room") {
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().saveSelectedLocation(ADDRESS_LOC_KORAMANGALA)

        coVerify { userDao.insertUser(match { it.id == USER_ID_GUEST }) }
    }

    test("saveSelectedLocation: guest entry has correct location") {
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().saveSelectedLocation(ADDRESS_LOC_HSR)

        coVerify {
            userDao.insertUser(match { it.selectedLocation == ADDRESS_LOC_HSR })
        }
    }

    // ══════════════════════════════════════════════════════════
    // addAddress
    // ══════════════════════════════════════════════════════════

    test("addAddress: does nothing when no user in Room") {
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().addAddress(fakeAddress())

        coVerify(exactly = 0) { userDao.updateAddresses(any(), any()) }
    }

    test("addAddress: calls updateAddresses when user exists") {
        val entity = fakeUserEntity(id = USER_ID_1, addressesJson = "[]")
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().addAddress(fakeAddress(id = ADDRESS_ID_1, label = ADDRESS_LABEL_HOME))

        coVerify { userDao.updateAddresses(id = USER_ID_1, addressesJson = any()) }
    }

    test("addAddress: new address appears in updated JSON") {
        val entity = fakeUserEntity(id = USER_ID_1, addressesJson = "[]")
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().addAddress(fakeAddress(id = ADDRESS_ID_1, label = ADDRESS_LABEL_HOME))

        coVerify {
            userDao.updateAddresses(
                id = USER_ID_1,
                addressesJson = match { it.contains(ADDRESS_LABEL_HOME) },
            )
        }
    }

    test("addAddress: adds to existing addresses — preserves old ones") {
        val existing = """[{"id":"$ADDRESS_ID_1",
            |"label":"$ADDRESS_LABEL_HOME",
            |"fullAddress":"$ADDRESS_FULL_2",
            |"landmark":"","latitude":0.0,"longitude":0.0}]""".trimMargin()
        val entity = fakeUserEntity(id = USER_ID_1, addressesJson = existing)
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().addAddress(fakeAddress(id = ADDRESS_ID_2, label = ADDRESS_LABEL_WORK))

        coVerify {
            userDao.updateAddresses(
                id = USER_ID_1,
                addressesJson = match {
                    it.contains(ADDRESS_LABEL_HOME) && it.contains(ADDRESS_LABEL_WORK)
                },
            )
        }
    }

    // ══════════════════════════════════════════════════════════
    // deleteAddress
    // ══════════════════════════════════════════════════════════

    test("deleteAddress: does nothing when no user in Room") {
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().deleteAddress(ADDRESS_ID_1)

        coVerify(exactly = 0) { userDao.updateAddresses(any(), any()) }
    }

    test("deleteAddress: updates addresses JSON after removal") {
        val entity = fakeUserEntity(
            id = USER_ID_1,
            addressesJson = """[{"id":"$ADDRESS_ID_1",
                |"label":"$ADDRESS_LABEL_HOME","fullAddress":"$ADDRESS_FULL_2",
                |"landmark":"","latitude":0.0,"longitude":0.0}]""".trimMargin()
        )
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().deleteAddress(ADDRESS_ID_1)

        coVerify {
            userDao.updateAddresses(id = USER_ID_1, addressesJson = "[]")
        }
    }

    test("deleteAddress: keeps other addresses when deleting one") {
        val existing = """
            [
              {"id":"$ADDRESS_ID_1","label":"$ADDRESS_LABEL_HOME","fullAddress":"$ADDRESS_FULL_2","landmark":"","latitude":0.0,"longitude":0.0},
              {"id":"$ADDRESS_ID_2","label":"$ADDRESS_LABEL_WORK","fullAddress":"$ADDRESS_FULL_3","landmark":"","latitude":0.0,"longitude":0.0}
            ]
        """.trimIndent()
        val entity = fakeUserEntity(id = USER_ID_1, addressesJson = existing)
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().deleteAddress(ADDRESS_ID_1)

        coVerify {
            userDao.updateAddresses(
                id = USER_ID_1,
                addressesJson = match {
                    it.contains(ADDRESS_LABEL_WORK) && !it.contains(ADDRESS_ID_1)
                },
            )
        }
    }

    test("deleteAddress: deleting non-existent id does not crash") {
        val entity = fakeUserEntity(
            id = USER_ID_1,
            addressesJson = """[{"id":"$ADDRESS_ID_1","label":"$ADDRESS_LABEL_HOME","fullAddress":"$ADDRESS_FULL_2","landmark":"","latitude":0.0,"longitude":0.0}]"""
        )
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().deleteAddress("nonexistent_id")

        coVerify {
            userDao.updateAddresses(
                id = USER_ID_1,
                addressesJson = match { it.contains(ADDRESS_LABEL_HOME) },
            )
        }
    }

    // ══════════════════════════════════════════════════════════
    // getRecentOrders
    // ══════════════════════════════════════════════════════════

    test("getRecentOrders: emits empty list when API throws") {
        coEvery { api.getOrders() } throws Exception(ERR_NETWORK)

        createRepo().getRecentOrders().test {
            awaitItem().isEmpty() shouldBe true
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getRecentOrders: emits mapped orders from API") {
        coEvery { api.getOrders() } returns fakeOrdersResponse()

        createRepo().getRecentOrders().test {
            val orders = awaitItem()
            orders.isNotEmpty() shouldBe true
            orders.first().id shouldBe ORDER_ID_1
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getRecentOrders: emits correct number of orders") {
        coEvery { api.getOrders() } returns fakeOrdersResponse()

        createRepo().getRecentOrders().test {
            awaitItem().size shouldBe ORDER_COUNT_1
            cancelAndIgnoreRemainingEvents()
        }
    }
})

// ── Local helpers ─────────────────────────────────────────────

private fun fakeAddress(
    id: String = ADDRESS_ID_1,
    label: String = ADDRESS_LABEL_HOME,
    fullAddress: String = ADDRESS_FULL_1,
) = Address(
    id = id,
    label = label,
    fullAddress = fullAddress,
    landmark = "",
    latitude = 0.0,
    longitude = 0.0,
)

private fun fakeUserApiResponse() = com.swapna.foodapp.data.remote.dto.UserResponse(
    user = com.swapna.foodapp.data.remote.dto.UserDto(
        id = USER_ID_API,
        name = USER_NAME_SWAPNA,
        email = USER_EMAIL_SWAPNA,
        phone = USER_PHONE_VALID,
        profileImage = null,
        addresses = null,
    )
)