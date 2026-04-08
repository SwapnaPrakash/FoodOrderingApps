package com.swapna.foodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swapna.foodapp.data.local.entity.*

@Dao
interface UserDao {

    // Only one user is ever stored
    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): UserEntity?

    // OnConflictStrategy.REPLACE updates existing user on re-login
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    // Update specific fields — used by Edit Profile
    @Query("UPDATE user SET name = :name, email = :email WHERE id = :id")
    suspend fun updateNameAndEmail(id: String, name: String, email: String)

    // Called on logout — wipes local user data
    @Query("DELETE FROM user")
    suspend fun clearUser()

    // Used by isLoggedIn() check in UserRepository
    @Query("SELECT COUNT(*) FROM user")
    suspend fun count(): Int
}