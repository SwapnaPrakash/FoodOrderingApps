package com.swapna.foodapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "restaurants")
data class RestaurantEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    // Full image URL for restaurant banner
    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    // Thumbnail URL for list cards
    @ColumnInfo(name = "thumb_url")
    val thumbUrl: String,

    // Already parsed to Double — never store "4.3" as String in Room
    @ColumnInfo(name = "rating")
    val rating: Double,

    @ColumnInfo(name = "rating_text")
    val ratingText: String,

    // Hex color code e.g. "5BA829"
    @ColumnInfo(name = "rating_color")
    val ratingColor: String,

    @ColumnInfo(name = "total_votes")
    val totalVotes: Int,

    // Minutes e.g. 30
    @ColumnInfo(name = "avg_delivery_time")
    val avgDeliveryTime: Int,

    @ColumnInfo(name = "delivery_fee")
    val deliveryFee: Double,

    @ColumnInfo(name = "min_order")
    val minOrder: Int,

    // Stored as JSON string via TypeConverter
    // e.g. "[\"Biryani\",\"North Indian\"]"
    @ColumnInfo(name = "cuisines_json")
    val cuisinesJson: String,

    @ColumnInfo(name = "address")
    val address: String,

    @ColumnInfo(name = "locality")
    val locality: String,

    // Room stores Boolean as INTEGER (0/1) automatically
    @ColumnInfo(name = "is_open")
    val isOpen: Boolean,

    @ColumnInfo(name = "has_delivery")
    val hasDelivery: Boolean,

    // Stored as JSON string e.g. "[\"50% off upto ₹100\"]"
    @ColumnInfo(name = "offers_json")
    val offersJson: String = "[]",

    @ColumnInfo(name = "avg_cost_for_two")
    val avgCostForTwo: Int = 0,

    // Unix timestamp — used to check if cache is stale
    // Stale = older than AppConstants.CACHE_DURATION_MIN minutes
    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
)