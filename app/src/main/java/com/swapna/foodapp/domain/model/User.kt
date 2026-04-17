package com.swapna.foodapp.domain.model

data class User(
    val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val profileImage: String = "",
    val addresses: List<Address> = emptyList(),
    val selectedLocation : String = "",
)

data class Address(
    val id: String,
    val label: String,         // "Home", "Work"
    val fullAddress: String,
    val landmark: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
)