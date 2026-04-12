package com.swapna.foodapp.domain.model

data class Order(
    val id: String,
    val restaurantId: String,
    val restaurantName: String,
    val restaurantImage: String,
    val status: String,
    val timeFriendly: String,
    val totalAmount: Double,
    val items: List<OrderItem>,
    val canReorder: Boolean = true,
)

data class OrderItem(
    val name: String,
    val quantity: Int,
    val price: Double,
)