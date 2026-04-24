package com.swapna.foodapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.swapna.foodapp.utils.AppConstants

@Entity(tableName = AppConstants.TABLE_CART_ITEMS)
data class CartItemEntity(

    @PrimaryKey
    @ColumnInfo(name = AppConstants.COL_ID)
    val id: String,

    @ColumnInfo(name = AppConstants.COL_MENU_ITEM_ID)
    val menuItemId: String,

    @ColumnInfo(name = AppConstants.COL_MENU_ITEM_JSON)
    val menuItemJson: String,

    @ColumnInfo(name = AppConstants.COL_QUANTITY)
    val quantity: Int,

    @ColumnInfo(name = AppConstants.COL_CUSTOMISATIONS_JSON)
    val customisationsJson: String = "[]",

    @ColumnInfo(name = AppConstants.COL_ADDED_AT)
    val addedAt: Long = System.currentTimeMillis(),
)
