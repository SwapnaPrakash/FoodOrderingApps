package com.swapna.foodapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.swapna.foodapp.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getUser(): UserEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("UPDATE user SET name = :name, email = :email WHERE id = :id")
    suspend fun updateNameAndEmail(id: String, name: String, email: String)

    @Query("DELETE FROM user")
    suspend fun clearUser()

    @Query("SELECT COUNT(*) FROM user")
    suspend fun count(): Int

    @Query("SELECT * FROM user LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Query("SELECT * FROM user LIMIT 1")
    suspend fun getCurrentUserOnce(): UserEntity?

    @Query(
        "UPDATE user SET selected_location = :location " +
                "WHERE id = :id"
    )
    suspend fun updateSelectedLocation(
        id: String,
        location: String,
    )

    @Query(
        "UPDATE user SET addresses_json = :addressesJson " +
                "WHERE id = :id"
    )
    suspend fun updateAddresses(
        id: String,
        addressesJson: String,
    )


}