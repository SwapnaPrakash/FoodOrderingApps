package com.swapna.foodapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "menu_items")
data class MenuItemEntity(

    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    // Foreign key concept (not enforced) — links menu to restaurant
    @ColumnInfo(name = "restaurant_id")
    val restaurantId: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String,

    @ColumnInfo(name = "price")
    val price: Double,

    @ColumnInfo(name = "image_url")
    val imageUrl: String,

    // e.g. "Biryani", "Starters" — used for grouping
    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "is_veg")
    val isVeg: Boolean,

    @ColumnInfo(name = "is_recommended")
    val isRecommended: Boolean = false,

    // Full customisations list as JSON
    @ColumnInfo(name = "customisations_json")
    val customisationsJson: String = "[]",

    @ColumnInfo(name = "cached_at")
    val cachedAt: Long = System.currentTimeMillis(),
)