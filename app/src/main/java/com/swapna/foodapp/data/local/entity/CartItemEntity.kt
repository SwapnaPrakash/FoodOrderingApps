package com.swapna.foodapp.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(

    // UUID generated in AddToCartUseCase
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    // Needed to check if same item already in cart
    @ColumnInfo(name = "menu_item_id")
    val menuItemId: String,

    // Full MenuItem serialized as JSON string
    // Reason: MenuItem has nested objects (customisations)
    // Storing as JSON is simpler than separate tables
    @ColumnInfo(name = "menu_item_json")
    val menuItemJson: String,

    @ColumnInfo(name = "quantity")
    val quantity: Int,

    // Selected customisations serialized as JSON
    // e.g. [{"id":"o2","label":"Large","extraPrice":80.0}]
    @ColumnInfo(name = "customisations_json")
    val customisationsJson: String = "[]",

    // Used to maintain order items were added
    @ColumnInfo(name = "added_at")
    val addedAt: Long = System.currentTimeMillis(),
)