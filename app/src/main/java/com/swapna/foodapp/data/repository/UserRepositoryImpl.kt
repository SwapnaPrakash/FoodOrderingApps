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
    private val firebaseAuthManager: FirebaseAuthManager,
    private val activityProvider: ActivityProvider,
    private val api: FoodApi,
    private val userDao: UserDao,
    private val userMapper: UserMapper,
    private val entityMapper: EntityMapper,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : UserRepository {

    private var pendingPhone: String? = null

    // ── Send OTP ──────────────────────────────────────────────
    override suspend fun sendOtp(phone: String): Result<Unit> =
        withContext(ioDispatcher) {
            val activity = activityProvider.getActivity()
                ?: return@withContext Result.failure(
                    Exception("App is in background. Please reopen.")
                )
            pendingPhone = phone
            firebaseAuthManager.sendOtp(phone, activity)
        }

    // ── Verify OTP ────────────────────────────────────────────
    override suspend fun verifyOtp(otp: String): Result<User> =
        withContext(ioDispatcher) {
            if (otp.length != AppConstants.OTP_LENGTH) {
                return@withContext Result.failure(
                    Exception("Enter the 6-digit OTP")
                )
            }

            val result = firebaseAuthManager.verifyOtp(otp)

            result.fold(
                onSuccess = { uid ->
                    val user = try {
                        val response = api.getUser()
                        userMapper.toDomain(response.user)
                            .copy(
                                id    = uid,
                                phone = pendingPhone ?: "",
                            )
                    } catch (e: Exception) {
                        User(
                            id           = uid,
                            name         = "User",
                            email        = "",
                            phone        = pendingPhone ?: "",
                            profileImage = "",
                            addresses    = emptyList(),
                        )
                    }
                    userDao.insertUser(entityMapper.userToEntity(user))
                    pendingPhone = null
                    Result.success(user)
                },
                onFailure = { Result.failure(it) }
            )
        }

    // ── Is Logged In ──────────────────────────────────────────
    override suspend fun isLoggedIn(): Boolean =
        withContext(ioDispatcher) {
            try {
                firebaseAuthManager.isSignedIn() &&
                        userDao.getUser() != null
            } catch (e: Exception) { false }
        }

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
                Result.failure(Exception(e.message ?: "Failed"))
            }
        }

    // ── Update User ───────────────────────────────────────────
    override suspend fun updateUser(user: User): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                userDao.updateNameAndEmail(
                    user.id, user.name, user.email
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: "Update failed"))
            }
        }

    // ── Recent Orders ─────────────────────────────────────────
    override fun getRecentOrders(): Flow<List<Order>> = flow {
        try {
            val response = api.getOrders()
            emit(response.orders.map {
                userMapper.orderToDomain(it.order)
            })
        } catch (e: Exception) { emit(emptyList()) }
    }
        .catch { emit(emptyList()) }
        .flowOn(ioDispatcher)

    // ── Logout ────────────────────────────────────────────────
    override suspend fun logout() =
        withContext(ioDispatcher) {
            pendingPhone = null
            firebaseAuthManager.signOut()
            userDao.clearUser()
        }
}