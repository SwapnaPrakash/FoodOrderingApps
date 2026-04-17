package com.swapna.foodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swapna.foodapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

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


    // ── Reactive Flow — ProfileViewModel observes this ────────
    // WHY Flow not suspend?
    // ProfileViewModel needs to auto-update when user changes
    // e.g. after edit name → Flow emits new value → UI updates
    @Query("SELECT * FROM user LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    // ── One-shot — used for updates (addAddress etc.) ─────────
    // WHY suspend not Flow?
    // When we need to READ then WRITE in same operation
    // We need the value once — not observe it
    // e.g. getCurrentUserOnce() → add address → insertUser()
    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getCurrentUserOnce(): UserEntity?


    // ✅ FIX: Update selected delivery location
    // Called every time user picks delivery area
    @Query(
        "UPDATE user SET selected_location = :location " +
                "WHERE id = :id"
    )
    suspend fun updateSelectedLocation(
        id:       String,
        location: String,
    )

    // ✅ FIX: Update addresses JSON
    // Called by addAddress + deleteAddress
    @Query(
        "UPDATE user SET addresses_json = :addressesJson " +
                "WHERE id = :id"
    )
    suspend fun updateAddresses(
        id:           String,
        addressesJson: String,
    )


}