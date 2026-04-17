package com.swapna.foodapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.swapna.foodapp.utils.AppConstants

@Entity(tableName = AppConstants.TABLE_USER)
data class UserEntity(

    @PrimaryKey
    @ColumnInfo(name = AppConstants.COL_ID)
    val id: String,

    @ColumnInfo(name = AppConstants.COL_NAME)
    val name: String,

    @ColumnInfo(name = AppConstants.COL_EMAIL)
    val email: String,

    @ColumnInfo(name = AppConstants.COL_PHONE)
    val phone: String,

    @ColumnInfo(name = AppConstants.COL_PROFILE_IMAGE)
    val profileImage: String = "",

    @ColumnInfo(name = AppConstants.COL_ADDRESSES_JSON)
    val addressesJson: String = "[]",

    @ColumnInfo(name = AppConstants.COL_SELECTED_LOCATION)
    val selectedLocation: String = "",

    @ColumnInfo(name = AppConstants.COL_CACHED_AT)
    val cachedAt: Long = System.currentTimeMillis(),
)