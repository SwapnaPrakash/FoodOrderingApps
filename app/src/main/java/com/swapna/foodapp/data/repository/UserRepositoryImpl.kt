package com.swapna.foodapp.data.repository

import com.swapna.foodapp.data.auth.ActivityProvider
import com.swapna.foodapp.data.auth.FirebaseAuthManager
import com.swapna.foodapp.data.local.dao.UserDao
import com.swapna.foodapp.data.mapper.EntityMapper
import com.swapna.foodapp.data.mapper.UserMapper
import com.swapna.foodapp.data.remote.api.FoodApi
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.User
import com.swapna.foodapp.domain.repository.UserRepository
import com.swapna.foodapp.utils.AppConstants.APP_BCK
import com.swapna.foodapp.utils.AppConstants.FAILED_OTP_SEND
import com.swapna.foodapp.utils.AppConstants.FAILED_PROFILE
import com.swapna.foodapp.utils.AppConstants.FAILED_UPDATED_PROFILE
import com.swapna.foodapp.utils.AppConstants.FAILED_VERIFICATION
import com.swapna.foodapp.utils.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
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

    override suspend fun sendOtp(phone: String): Result<Unit> =
        withContext(ioDispatcher) {
            val activity = activityProvider.getActivity()
                ?: return@withContext Result.failure(
                    Exception(
                        APP_BCK
                    )
                )
            try {
                firebaseAuthManager.sendOtp(
                    phoneNumber = phone,
                    activity = activity,
                )
            } catch (e: Exception) {
                Result.failure(Exception(e.message ?: FAILED_OTP_SEND))
            }
        }

    override suspend fun verifyOtp(otp: String): Result<User> =
        withContext(ioDispatcher) {
            try {
                val uidResult = firebaseAuthManager.verifyOtp(otp)
                val uid = uidResult.getOrElse {
                    return@withContext Result.failure(it)
                }

                val response = api.getUser()
                val user = userMapper.toDomain(response.user)
                    .copy(id = uid)

                userDao.insertUser(entityMapper.userToEntity(user))
                Result.success(user)

            } catch (e: Exception) {
                Result.failure(
                    Exception(e.message ?: FAILED_VERIFICATION)
                )
            }
        }

    override fun isLoggedIn(): Boolean =
        firebaseAuthManager.isSignedIn()

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
                val user = userMapper.toDomain(response.user)
                userDao.insertUser(entityMapper.userToEntity(user))
                Result.success(user)
            } catch (e: Exception) {
                Result.failure(
                    Exception(e.message ?: FAILED_PROFILE)
                )
            }
        }

    override suspend fun updateUser(user: User): Result<Unit> =
        withContext(ioDispatcher) {
            try {
                userDao.updateNameAndEmail(
                    id = user.id,
                    name = user.name,
                    email = user.email,
                )
                Result.success(Unit)
            } catch (e: Exception) {
                Result.failure(
                    Exception(e.message ?: FAILED_UPDATED_PROFILE)
                )
            }
        }

    override fun getRecentOrders(): Flow<List<Order>> = flow {
        try {
            val response = api.getOrders()
            val orders = response.orders.map { wrapper ->
                userMapper.orderToDomain(wrapper.order)
            }
            emit(orders)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }.flowOn(ioDispatcher)

    override suspend fun logout() =
        withContext(ioDispatcher) {
            firebaseAuthManager.signOut()
            userDao.clearUser()
        }
}