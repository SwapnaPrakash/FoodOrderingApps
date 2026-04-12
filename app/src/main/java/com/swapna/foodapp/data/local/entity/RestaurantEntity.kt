package com.swapna.foodapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.swapna.foodapp.utils.AppConstants

@Entity(tableName = AppConstants.TABLE_RESTAURANTS)
data class RestaurantEntity(

    @PrimaryKey
    @ColumnInfo(name = AppConstants.COL_ID)
    val id: String,

    @ColumnInfo(name = AppConstants.COL_NAME)
    val name: String,

    @ColumnInfo(name = AppConstants.COL_IMAGE_URL)
    val imageUrl: String,

    @ColumnInfo(name = AppConstants.COL_THUMB_URL)
    val thumbUrl: String,

    @ColumnInfo(name = AppConstants.COL_RATING)
    val rating: Double,

    @ColumnInfo(name = AppConstants.COL_RATING_TEXT)
    val ratingText: String,

    @ColumnInfo(name = AppConstants.COL_RATING_COLOR)
    val ratingColor: String,

    @ColumnInfo(name = AppConstants.COL_TOTAL_VOTES)
    val totalVotes: Int,

    @ColumnInfo(name = AppConstants.COL_AVG_DELIVERY_TIME)
    val avgDeliveryTime: Int,

    @ColumnInfo(name = AppConstants.COL_DELIVERY_FEE)
    val deliveryFee: Double,

    @ColumnInfo(name = AppConstants.COL_MIN_ORDER)
    val minOrder: Int,

    @ColumnInfo(name = AppConstants.COL_CUISINES_JSON)
    val cuisinesJson: String,

    @ColumnInfo(name = AppConstants.COL_ADDRESS)
    val address: String,

    @ColumnInfo(name = AppConstants.COL_LOCALITY)
    val locality: String,

    @ColumnInfo(name = AppConstants.COL_IS_OPEN)
    val isOpen: Boolean,

    @ColumnInfo(name = AppConstants.COL_HAS_DELIVERY)
    val hasDelivery: Boolean,

    @ColumnInfo(name = AppConstants.COL_OFFERS_JSON)
    val offersJson: String = "[]",

    @ColumnInfo(name = AppConstants.COL_AVG_COST_FOR_TWO)
    val avgCostForTwo: Int = 0,

    @ColumnInfo(name = AppConstants.COL_CACHED_AT)
    val cachedAt: Long = System.currentTimeMillis(),
)