package com.swapna.foodapp.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.swapna.foodapp.data.auth.ActivityProvider
import com.swapna.foodapp.data.auth.FirebaseAuthManager
import com.swapna.foodapp.data.local.dao.UserDao
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.data.mapper.UserMapper
import com.swapna.foodapp.data.remote.api.FoodApi
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swapna.foodapp.domain.model.Address
import kotlinx.coroutines.flow.map

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api:                 FoodApi,
    private val userDao:             UserDao,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val firebaseAuth:        FirebaseAuth,
    private val activityProvider:    ActivityProvider,
    private val userMapper:          UserMapper,
    private val entityMapper:        EntityMapper,
    @IoDispatcher
    private val ioDispatcher:        CoroutineDispatcher,
) : UserRepository {

    // Gson instance — reused across address operations
    // WHY single instance?
    // Gson is thread-safe and expensive to create
    // Create once, reuse everywhere in this class
    private val gson = Gson()

    // ── Send OTP ──────────────────────────────────────────────
    override suspend fun sendOtp(
        phone: String,
    ): Result<Unit> = withContext(ioDispatcher) {
        val activity = activityProvider.getActivity()
            ?: return@withContext Result.failure(
                Exception(APP_BCK)
            )
        try {
            firebaseAuthManager.sendOtp(
                phoneNumber = phone,
                activity = activity,
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: FAILED_OTP_SEND)
            )
        }
    }

    // ── Verify OTP ────────────────────────────────────────────
    override suspend fun verifyOtp(
        otp: String,
    ): Result<User> = withContext(ioDispatcher) {
        try {
            val uidResult = firebaseAuthManager.verifyOtp(otp)
            val uid = uidResult.getOrElse {
                return@withContext Result.failure(it)
            }

            val response = api.getUser()
            val user = userMapper
                .toDomain(response.user)
                .copy(id = uid)

            userDao.insertUser(entityMapper.userToEntity(user))
            Result.success(user)

        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: FAILED_VERIFICATION)
            )
        }
    }

    // ── Is logged in ──────────────────────────────────────────
    override fun isLoggedIn(): Boolean =
        firebaseAuthManager.isSignedIn()

    // ── Logout ────────────────────────────────────────────────
    override suspend fun logout() =
        withContext(ioDispatcher) {
            firebaseAuthManager.signOut()
            userDao.clearUser()
        }

    // ── Get current user — REACTIVE Flow ─────────────────────
    // ✅ FIX: ProfileViewModel observes this Flow
    // Before: getUser() returns Result<User> — one-shot
    // After:  getCurrentUser() returns Flow<User?> — reactive
    //
    // WHY Flow?
    // User updates name → Flow emits → ProfileScreen re-renders
    // User adds address → Flow emits → LocationPicker updates
    // No manual refresh needed
    //
    // WHY fallback to Firebase phone?
    // First launch after OTP = Room has user from verifyOtp()
    // But if Room somehow empty → still show phone from Firebase
    override fun getCurrentUser(): Flow<User?> =
        userDao.getCurrentUser().map { entity ->
            when {
                // ── Case 1: User in Room ──────────────────────────
                // Normal case after login + profile load
                entity != null -> {
                    entityMapper.userToDomain(entity)
                }

                // ── Case 2: Room empty, Firebase logged in ────────
                // First launch after OTP, before profile loaded
                // OR if Room was cleared but session still active
                firebaseAuth.currentUser != null -> {
                    // ✅ Use firebaseAuth.currentUser directly
                    // Guaranteed to work — no wrapper needed
                    val fbUser = firebaseAuth.currentUser!!
                    User(
                        id               = fbUser.uid,
                        name             = fbUser.displayName ?: "",
                        email            = fbUser.email ?: "",
                        phone            = fbUser.phoneNumber ?: "",
                        profileImage     = "",
                        addresses        = emptyList(),
                        selectedLocation = "",
                    )
                }
                else -> null
            }
        }


    // ── Get user — one-shot Result ────────────────────────────
    // Kept for backward compatibility
    // Used by screens that don't need reactive updates
    override suspend fun getUser(): Result<User> =
        withContext(ioDispatcher) {
            try {
                val cached = userDao.getUser()
                if (cached != null) {
                    return@withContext Result.success(
                        entityMapper.userToDomain(cached)
                    )
                }
                val response = api.getUser()
                val user     = userMapper.toDomain(response.user)
                userDao.insertUser(entityMapper.userToEntity(user))
                Result.success(user)

            } catch (e: Exception) {
                Result.failure(
                    Exception(e.message ?: FAILED_PROFILE)
                )
            }
        }

    // ── Update user (name + email) ────────────────────────────
    override suspend fun updateUser(
        user: User,
    ): Result<Unit> = withContext(ioDispatcher) {
        try {
            userDao.updateNameAndEmail(
                id    = user.id,
                name  = user.name,
                email = user.email,
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(
                Exception(e.message ?: FAILED_UPDATED_PROFILE)
            )
        }
    }

    // ── Save selected delivery location ───────────────────────
    // ✅ FIX: NEW — was missing
    // Called by HomeViewModel when user picks delivery area
    // Persists to Room so app remembers on next launch
    override suspend fun saveSelectedLocation(
        location: String,
    ) = withContext(ioDispatcher) {
        val current = userDao.getCurrentUserOnce()

        if (current != null) {
            // User exists → update location field
            userDao.updateSelectedLocation(
                id       = current.id,
                location = location,
            )
        } else {
            // No user in Room (edge case: guest browsing)
            // Create minimal user entry to persist location
            userDao.insertUser(
                com.swapna.foodapp.data.local.entity.UserEntity(
                    id               = "guest",
                    name             = "",
                    email            = "",
                    phone            = "",
                    profileImage     = "",
                    addressesJson    = "[]",
                    selectedLocation = location,
                )
            )
        }
    }

    // ── Add address ───────────────────────────────────────────
    // ✅ FIX: NEW — was missing
    // Called when user saves a new delivery address
    // Address stored as JSON array in UserEntity
    override suspend fun addAddress(
        address: Address,
    ) = withContext(ioDispatcher) {
        val current = userDao.getCurrentUserOnce()
            ?: return@withContext

        // Parse existing addresses JSON → MutableList
        val type = object :
            TypeToken<MutableList<Address>>() {}.type
        val addresses: MutableList<Address> =
            gson.fromJson(current.addressesJson, type)
                ?: mutableListOf()

        // Add new address to list
        addresses.add(address)

        // Save back as JSON
        userDao.updateAddresses(
            id            = current.id,
            addressesJson = gson.toJson(addresses),
        )
    }

    // ── Delete address ────────────────────────────────────────
    // ✅ FIX: NEW — was missing
    // Called from ProfileScreen delete button
    override suspend fun deleteAddress(
        addressId: String,
    ) = withContext(ioDispatcher) {
        val current = userDao.getCurrentUserOnce()
            ?: return@withContext

        val type = object :
            TypeToken<MutableList<Address>>() {}.type
        val addresses: MutableList<Address> =
            gson.fromJson(current.addressesJson, type)
                ?: mutableListOf()

        // Remove address with matching id
        addresses.removeAll { it.id == addressId }

        // Save back as JSON
        userDao.updateAddresses(
            id            = current.id,
            addressesJson = gson.toJson(addresses),
        )
    }

    // ── Get recent orders ─────────────────────────────────────
    // Fetches from API — orders not stored in Room for MVP
    // WHY not Room?
    // Orders change frequently (status updates)
    // API is source of truth for order history
    override fun getRecentOrders(): Flow<List<Order>> = flow {
        try {
            val response = api.getOrders()
            val orders   = response.orders.map { wrapper ->
                userMapper.orderToDomain(wrapper.order)
            }
            emit(orders)
        } catch (e: Exception) {
            // Non-critical — ProfileScreen shows empty list
            emit(emptyList())
        }
    }.flowOn(ioDispatcher)

    // ── Error message constants ───────────────────────────────
    private companion object {
        const val APP_BCK              =
            "App is in background. Please reopen and try again."
        const val FAILED_OTP_SEND      =
            "Failed to send OTP. Please try again."
        const val FAILED_VERIFICATION  =
            "Wrong OTP. Please check and try again."
        const val FAILED_PROFILE       =
            "Failed to load profile. Please try again."
        const val FAILED_UPDATED_PROFILE =
            "Failed to update profile. Please try again."
    }
}