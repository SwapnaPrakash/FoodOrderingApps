package com.swapna.foodapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.COL_CACHED_AT
import com.swapna.foodapp.utils.AppConstants.COL_CATEGORY
import com.swapna.foodapp.utils.AppConstants.COL_CUSTOMISATIONS_JSON
import com.swapna.foodapp.utils.AppConstants.COL_DESCRIPTION
import com.swapna.foodapp.utils.AppConstants.COL_IMAGE_URL
import com.swapna.foodapp.utils.AppConstants.COL_IS_RECOMMENDED
import com.swapna.foodapp.utils.AppConstants.COL_IS_VEG
import com.swapna.foodapp.utils.AppConstants.COL_PRICE
import com.swapna.foodapp.utils.AppConstants.TABLE_MENU_ITEMS

@Entity(tableName = TABLE_MENU_ITEMS)
data class MenuItemEntity(

    @PrimaryKey
    @ColumnInfo(name = AppConstants.COL_ID)
    val id: String,

    // Foreign key concept (not enforced) — links menu to restaurant
    @ColumnInfo(name = AppConstants.COL_RESTAURANT_ID)
    val restaurantId: String,

    @ColumnInfo(name = AppConstants.COL_NAME)
    val name: String,

    @ColumnInfo(name = COL_DESCRIPTION)
    val description: String,

    @ColumnInfo(name = COL_PRICE)
    val price: Double,

    @ColumnInfo(name = COL_IMAGE_URL)
    val imageUrl: String,

    // e.g. "Biryani", "Starters" — used for grouping
    @ColumnInfo(COL_CATEGORY)
    val category: String,

    @ColumnInfo(name = COL_IS_VEG)
    val isVeg: Boolean,

    @ColumnInfo(name = COL_IS_RECOMMENDED)
    val isRecommended: Boolean = false,

    @ColumnInfo(name = AppConstants.COL_IS_BESTSELLER)
    val isBestseller: Boolean = false,

    @ColumnInfo(name = AppConstants.COL_IS_AVAILABLE)
    val isAvailable: Boolean = true,

    // Full customisations list as JSON
    @ColumnInfo(name = COL_CUSTOMISATIONS_JSON)
    val customisationsJson: String = "[]",

    @ColumnInfo(name = COL_CACHED_AT)
    val cachedAt: Long = System.currentTimeMillis(),
)