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

    val api                 = mockk<FoodApi>()
    val userDao             = mockk<UserDao>()
    val firebaseAuthManager = mockk<FirebaseAuthManager>()
    val firebaseAuth        = mockk<FirebaseAuth>()
    val activityProvider    = mockk<ActivityProvider>()

    val userMapper   = UserMapper()
    val entityMapper = EntityMapper()

    fun createRepo() = UserRepositoryImpl(
        api                 = api,
        userDao             = userDao,
        firebaseAuthManager = firebaseAuthManager,
        firebaseAuth        = firebaseAuth,
        activityProvider    = activityProvider,
        userMapper          = userMapper,
        entityMapper        = entityMapper,
        ioDispatcher        = UnconfinedTestDispatcher(),
    )

    beforeEach {
        clearAllMocks()
        Dispatchers.setMain(UnconfinedTestDispatcher())

        every { activityProvider.getActivity() }                    returns null
        every { userDao.getCurrentUser() }                          returns flowOf(null)
        every { firebaseAuth.currentUser }                          returns null
        coEvery { userDao.getCurrentUserOnce() }                    returns null
        coEvery { userDao.getUser() }                               returns null
        coEvery { userDao.insertUser(any()) }                       just runs
        coEvery { userDao.clearUser() }                             just runs
        coEvery { userDao.updateNameAndEmail(any(), any(), any()) } just runs
        coEvery { userDao.updateSelectedLocation(any(), any()) }    just runs
        coEvery { userDao.updateAddresses(any(), any()) }           just runs
        coEvery { firebaseAuthManager.signOut() }                   just runs
    }

    afterEach { Dispatchers.resetMain() }

    // ══════════════════════════════════════════════════════════
    // sendOtp
    // ✅ NEW — completely missing
    // WHY? sendOtp has 2 branches:
    //   1. activityProvider returns null → failure immediately
    //   2. activityProvider returns activity → delegates to firebaseAuthManager
    // ══════════════════════════════════════════════════════════

    test("sendOtp: returns failure when activity is null — app in background") {
        // activityProvider.getActivity() returns null in beforeEach
        // WHY test this? App backgrounded during OTP flow
        // Without this test — removing the null check would not be caught
        val result = createRepo().sendOtp("+919876543210")

        result.isFailure shouldBe true
        result.exceptionOrNull()?.message shouldBe
                "App is in background. Please reopen and try again."
    }

    test("sendOtp: delegates to firebaseAuthManager when activity available") {
        val fakeActivity = mockk<android.app.Activity>()
        every { activityProvider.getActivity() } returns fakeActivity
        coEvery {
            firebaseAuthManager.sendOtp(any(), any())
        } returns Result.success(Unit)

        val result = createRepo().sendOtp("+919876543210")

        result.isSuccess shouldBe true
        coVerify { firebaseAuthManager.sendOtp("+919876543210", fakeActivity) }
    }

    test("sendOtp: returns failure when firebaseAuthManager throws") {
        val fakeActivity = mockk<android.app.Activity>()
        every { activityProvider.getActivity() } returns fakeActivity
        coEvery {
            firebaseAuthManager.sendOtp(any(), any())
        } throws Exception("Firebase error")

        val result = createRepo().sendOtp("+919876543210")

        result.isFailure shouldBe true
    }

    test("sendOtp: failure message preserved when firebaseAuthManager throws") {
        val fakeActivity = mockk<android.app.Activity>()
        every { activityProvider.getActivity() } returns fakeActivity
        coEvery {
            firebaseAuthManager.sendOtp(any(), any())
        } throws Exception("Rate limit exceeded")

        val result = createRepo().sendOtp("+919876543210")

        result.exceptionOrNull()?.message shouldBe "Rate limit exceeded"
    }

    // ══════════════════════════════════════════════════════════
    // verifyOtp
    // ✅ NEW — completely missing
    // WHY? verifyOtp has 3 branches:
    //   1. firebaseAuthManager.verifyOtp fails → return failure
    //   2. api.getUser fails → catch → return failure
    //   3. success → save user to Room → return user
    // ══════════════════════════════════════════════════════════

    test("verifyOtp: returns failure when firebaseAuthManager verifyOtp fails") {
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.failure(Exception("Wrong OTP"))

        val result = createRepo().verifyOtp("123456")

        result.isFailure shouldBe true
    }

    test("verifyOtp: returns failure when API throws after OTP verified") {
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.success("uid_123")
        coEvery { api.getUser() } throws Exception("Network error")

        val result = createRepo().verifyOtp("123456")

        result.isFailure shouldBe true
    }

    test("verifyOtp: saves user to Room on success") {
        val fakeUserResponse = fakeUserApiResponse()
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.success("uid_123")
        coEvery { api.getUser() } returns fakeUserResponse

        createRepo().verifyOtp("123456")

        coVerify { userDao.insertUser(any()) }
    }

    test("verifyOtp: returns success with user on valid OTP") {
        val fakeUserResponse = fakeUserApiResponse()
        coEvery {
            firebaseAuthManager.verifyOtp(any())
        } returns Result.success("uid_123")
        coEvery { api.getUser() } returns fakeUserResponse

        val result = createRepo().verifyOtp("123456")

        result.isSuccess shouldBe true
        // WHY check uid? verifyOtp does user.copy(id = uid)
        // The user id must come from Firebase, not from API response
        result.getOrNull()?.id shouldBe "uid_123"
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
        val entity = fakeUserEntity(id = "u1", name = "Swapna")
        every { userDao.getCurrentUser() } returns flowOf(entity)

        createRepo().getCurrentUser().test {
            val result = awaitItem()
            result shouldNotBe null
            result?.id   shouldBe "u1"
            result?.name shouldBe "Swapna"
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: email mapped correctly from entity") {
        val entity = fakeUserEntity(id = "u1", name = "Swapna")
        every { userDao.getCurrentUser() } returns flowOf(entity)

        createRepo().getCurrentUser().test {
            awaitItem()?.email shouldBe "swapna@example.com"
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: falls back to Firebase user when Room empty") {
        val fbUser = mockk<FirebaseUser> {
            every { uid }         returns "fb_uid"
            every { displayName } returns "Firebase User"
            every { email }       returns "fb@example.com"
            every { phoneNumber } returns "+919876543210"
        }
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns fbUser

        createRepo().getCurrentUser().test {
            val result = awaitItem()
            result shouldNotBe null
            result?.id   shouldBe "fb_uid"
            result?.name shouldBe "Firebase User"
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: Firebase fallback phone set correctly") {
        val fbUser = mockk<FirebaseUser> {
            every { uid }         returns "fb_uid"
            every { displayName } returns ""
            every { email }       returns ""
            every { phoneNumber } returns "+919876543210"
        }
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns fbUser

        createRepo().getCurrentUser().test {
            awaitItem()?.phone shouldBe "+919876543210"
            cancelAndIgnoreRemainingEvents()
        }
    }

    test("getCurrentUser: Firebase fallback handles null displayName email phone") {
        val fbUser = mockk<FirebaseUser> {
            every { uid }         returns "fb_uid"
            every { displayName } returns null
            every { email }       returns null
            every { phoneNumber } returns null
        }
        every { userDao.getCurrentUser() } returns flowOf(null)
        every { firebaseAuth.currentUser } returns fbUser

        createRepo().getCurrentUser().test {
            val result = awaitItem()
            result?.name  shouldBe ""
            result?.email shouldBe ""
            result?.phone shouldBe ""
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ══════════════════════════════════════════════════════════
    // getUser — one-shot
    // ✅ NEW — completely missing
    // WHY? getUser has 3 branches:
    //   1. Room has cached user → return it
    //   2. Room empty → fetch from API → save → return
    //   3. Exception → return failure
    // ══════════════════════════════════════════════════════════

    test("getUser: returns cached user from Room without API call") {
        val entity = fakeUserEntity(id = "u1", name = "Swapna")
        coEvery { userDao.getUser() } returns entity

        val result = createRepo().getUser()

        result.isSuccess shouldBe true
        result.getOrNull()?.name shouldBe "Swapna"
        // WHY verify api NOT called?
        // Cache hit = skip API = faster + works offline
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
        coEvery { api.getUser() } throws Exception("Network error")

        val result = createRepo().getUser()

        result.isFailure shouldBe true
    }

    test("getUser: failure message preserved from exception") {
        coEvery { userDao.getUser() } returns null
        coEvery { api.getUser() } throws Exception("Timeout")

        val result = createRepo().getUser()

        result.exceptionOrNull()?.message shouldBe "Timeout"
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
        val user = fakeUser(id = "u1", name = "Swapna", email = "s@example.com")

        createRepo().updateUser(user)

        coVerify {
            userDao.updateNameAndEmail(
                id    = "u1",
                name  = "Swapna",
                email = "s@example.com",
            )
        }
    }

    test("updateUser: returns success when dao succeeds") {
        createRepo().updateUser(fakeUser()).isSuccess shouldBe true
    }

    test("updateUser: returns failure when dao throws") {
        coEvery {
            userDao.updateNameAndEmail(any(), any(), any())
        } throws Exception("DB error")

        createRepo().updateUser(fakeUser()).isFailure shouldBe true
    }

    test("updateUser: failure message preserved from exception") {
        coEvery {
            userDao.updateNameAndEmail(any(), any(), any())
        } throws Exception("DB error")

        createRepo().updateUser(fakeUser())
            .exceptionOrNull()?.message shouldBe "DB error"
    }

    // ══════════════════════════════════════════════════════════
    // saveSelectedLocation
    // ══════════════════════════════════════════════════════════

    test("saveSelectedLocation: updates existing user location in Room") {
        val entity = fakeUserEntity("u1", "Swapna")
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().saveSelectedLocation("Koramangala")

        coVerify {
            userDao.updateSelectedLocation(id = "u1", location = "Koramangala")
        }
    }

    test("saveSelectedLocation: inserts guest user when no user in Room") {
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().saveSelectedLocation("Koramangala")

        coVerify { userDao.insertUser(match { it.id == "guest" }) }
    }

    test("saveSelectedLocation: guest entry has correct location") {
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().saveSelectedLocation("HSR Layout")

        coVerify {
            userDao.insertUser(match { it.selectedLocation == "HSR Layout" })
        }
    }

    // ══════════════════════════════════════════════════════════
    // addAddress
    // ✅ NEW — completely missing
    // WHY? addAddress has 2 branches:
    //   1. No user in Room → early return (nothing called)
    //   2. User exists → parse JSON → add → save back
    // ══════════════════════════════════════════════════════════

    test("addAddress: does nothing when no user in Room") {
        // WHY test early return?
        // getCurrentUserOnce() = null → return@withContext
        // Without this test — removing that guard would not be caught
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().addAddress(fakeAddress())

        coVerify(exactly = 0) { userDao.updateAddresses(any(), any()) }
    }

    test("addAddress: calls updateAddresses when user exists") {
        val entity = fakeUserEntity(id = "u1", addressesJson = "[]")
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().addAddress(fakeAddress(id = "a1", label = "Home"))

        coVerify { userDao.updateAddresses(id = "u1", addressesJson = any()) }
    }

    test("addAddress: new address appears in updated JSON") {
        val entity = fakeUserEntity(id = "u1", addressesJson = "[]")
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().addAddress(fakeAddress(id = "a1", label = "Home"))

        coVerify {
            userDao.updateAddresses(
                id            = "u1",
                // WHY match contains "Home"?
                // Gson serialises Address → JSON
                // "Home" must appear in the updated JSON string
                addressesJson = match { it.contains("Home") },
            )
        }
    }

    test("addAddress: adds to existing addresses — preserves old ones") {
        // User already has one address — adding second must keep first
        val existing = """[{"id":"a1","label":"Home","fullAddress":"123 St","landmark":"","latitude":0.0,"longitude":0.0}]"""
        val entity   = fakeUserEntity(id = "u1", addressesJson = existing)
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().addAddress(fakeAddress(id = "a2", label = "Work"))

        coVerify {
            userDao.updateAddresses(
                id            = "u1",
                // Both addresses must be in the updated JSON
                addressesJson = match {
                    it.contains("Home") && it.contains("Work")
                },
            )
        }
    }

    // ══════════════════════════════════════════════════════════
    // deleteAddress
    // ══════════════════════════════════════════════════════════

    test("deleteAddress: does nothing when no user in Room") {
        coEvery { userDao.getCurrentUserOnce() } returns null

        createRepo().deleteAddress("a1")

        coVerify(exactly = 0) { userDao.updateAddresses(any(), any()) }
    }

    test("deleteAddress: updates addresses JSON after removal") {
        val entity = fakeUserEntity(
            id            = "u1",
            addressesJson = """[{"id":"a1","label":"Home","fullAddress":"123 St","landmark":"","latitude":0.0,"longitude":0.0}]"""
        )
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().deleteAddress("a1")

        coVerify {
            userDao.updateAddresses(id = "u1", addressesJson = "[]")
        }
    }

    // ✅ NEW — delete keeps OTHER addresses intact
    // WHY? addresses.removeAll { it.id == addressId }
    // Must only remove matching id — not clear all addresses
    test("deleteAddress: keeps other addresses when deleting one") {
        val existing = """
            [
              {"id":"a1","label":"Home","fullAddress":"123 St","landmark":"","latitude":0.0,"longitude":0.0},
              {"id":"a2","label":"Work","fullAddress":"456 Office","landmark":"","latitude":0.0,"longitude":0.0}
            ]
        """.trimIndent()
        val entity = fakeUserEntity(id = "u1", addressesJson = existing)
        coEvery { userDao.getCurrentUserOnce() } returns entity

        createRepo().deleteAddress("a1") // delete Home only

        coVerify {
            userDao.updateAddresses(
                id            = "u1",
                // Work must remain — Home must be gone
                addressesJson = match {
                    it.contains("Work") && !it.contains("a1")
                },
            )
        }
    }

    // ✅ NEW — delete non-existent id — no crash, empty result same
    // WHY? removeAll { it.id == "nonexistent" } removes nothing
    // addresses list unchanged — updateAddresses still called
    test("deleteAddress: deleting non-existent id does not crash") {
        val entity = fakeUserEntity(
            id            = "u1",
            addressesJson = """[{"id":"a1","label":"Home","fullAddress":"123 St","landmark":"","latitude":0.0,"longitude":0.0}]"""
        )
        coEvery { userDao.getCurrentUserOnce() } returns entity

        // This must not throw
        createRepo().deleteAddress("nonexistent_id")

        // updateAddresses still called — address list unchanged
        coVerify {
            userDao.updateAddresses(
                id            = "u1",
                addressesJson = match { it.contains("Home") },
            )
        }
    }

    // ══════════════════════════════════════════════════════════
    // getRecentOrders
    // ══════════════════════════════════════════════════════════

    test("getRecentOrders: emits empty list when API throws") {
        coEvery { api.getOrders() } throws Exception("Network error")

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
            orders.first().id shouldBe "o1"
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ✅ NEW — order count correct
    test("getRecentOrders: emits correct number of orders") {
        coEvery { api.getOrders() } returns fakeOrdersResponse()

        createRepo().getRecentOrders().test {
            awaitItem().size shouldBe 1
            cancelAndIgnoreRemainingEvents()
        }
    }
})

// ── Local helpers ─────────────────────────────────────────────

private fun fakeAddress(
    id:          String = "a1",
    label:       String = "Home",
    fullAddress: String = "123 Main Street",
) = Address(
    id          = id,
    label       = label,
    fullAddress = fullAddress,
    landmark    = "",
    latitude    = 0.0,
    longitude   = 0.0,
)

// WHY fakeUserApiResponse here not in TestFunctions?
// Only UserRepositoryImplSpec needs UserResponse + UserDto
// Keeping it local avoids polluting shared test helpers
private fun fakeUserApiResponse() = com.swapna.foodapp.data.remote.dto.UserResponse(
    user = com.swapna.foodapp.data.remote.dto.UserDto(
        id           = "api_user_1",
        name         = "Swapna",
        email        = "swapna@example.com",
        phone        = "+919876543210",
        profileImage = null,
        addresses    = null,
    )
)