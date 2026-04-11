package com.swapna.foodapp.data.repository

import com.swapna.foodapp.data.auth.ActivityProvider
import com.swapna.foodapp.data.auth.FirebaseAuthManager
import com.swapna.foodapp.data.local.dao.UserDao
import com.swapna.foodapp.data.mapper.*
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
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.data.mapper.UserMapper
import com.swapna.foodapp.utils.AppConstants
import kotlinx.coroutines.flow.catch
import javax.inject.Singleton

@Singleton
class UserRepositoryImpl @Inject constructor(
    private val api: FoodApi,
    private val userDao: UserDao,
    private val firebaseAuthManager: FirebaseAuthManager,
    private val activityProvider: ActivityProvider,
    private val userMapper: UserMapper,
    private val entityMapper: EntityMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    // ── Send OTP ──────────────────────────────────────────────
    override suspend fun sendOtp(phone: String): Result<Unit> =
        withContext(ioDispatcher) {
            val activity = activityProvider.getActivity()
                ?: return@withContext Result.failure(
                    Exception(
                        "App is in background. Please reopen and try again."
                    )
                )
            try {
                firebaseAuthManager.sendOtp(
                    phoneNumber = phone,
                    activity    = activity,
                )
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "Failed to send OTP"))
            }
        }

    // ── Verify OTP ────────────────────────────────────────────
    override suspend fun verifyOtp(otp: String): Result<User> =
        withContext(ioDispatcher) {
            try {
                val uidResult = firebaseAuthManager.verifyOtp(otp)
                val uid       = uidResult.getOrElse {
                    return@withContext Result.failure(it)
                }

                val response = api.getUser()
                val user     = userMapper.toDomain(response.user)
                    .copy(id = uid)

                userDao.insertUser(entityMapper.userToEntity(user))
                Result.success(user)

            } catch (e: Exception) {
                Result.failure(
                    Exception(e.message ?: "Verification failed")
                )
            }
        }

    // ── Login state ───────────────────────────────────────────
    // ✅ NOT suspend — same as original working code
    override fun isLoggedIn(): Boolean =
        firebaseAuthManager.isSignedIn()

    // ── Get User ──────────────────────────────────────────────
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
                    Exception(e.message ?: "Failed to load profile")
                )
            }
        }

    // ── Update User ───────────────────────────────────────────
    override suspend fun updateUser(user: User): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                userDao.updateNameAndEmail(
                    id    = user.id,
                    name  = user.name,
                    email = user.email,
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(
                    Exception(e.message ?: "Failed to update profile")
                )
            }
        }

    // ── Recent Orders ─────────────────────────────────────────
    override fun getRecentOrders(): Flow<List<Order>> = flow {
        try {
            val response = api.getOrders()
            val orders   = response.orders.map { wrapper ->
                userMapper.orderToDomain(wrapper.order)
            }
            emit(orders)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(ioDispatcher)

    // ── Logout ────────────────────────────────────────────────
    override suspend fun logout() =
        withContext(ioDispatcher) {
            firebaseAuthManager.signOut()
            userDao.clearUser()
        }
}