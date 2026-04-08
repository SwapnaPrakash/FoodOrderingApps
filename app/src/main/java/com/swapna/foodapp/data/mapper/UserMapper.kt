package com.swapna.foodapp.data.mapper

import com.swapna.foodapp.data.remote.dto.OrderDto
import com.swapna.foodapp.data.remote.dto.UserDto
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.Order
import com.swapna.foodapp.domain.model.OrderItem
import com.swapna.foodapp.domain.model.User
import javax.inject.Inject

class UserMapper @Inject constructor() {

    fun toDomain(dto: UserDto): User = User(
        id           = dto.id,
        name         = dto.name,
        email        = dto.email,
        phone        = dto.phone,
        profileImage = dto.profileImage ?: "",
        addresses    = dto.addresses?.map { a ->
            Address(
                id          = a.id,
                label       = a.label,
                fullAddress = a.fullAddress,
                landmark    = a.landmark ?: "",
                latitude    = a.latitude,
                longitude   = a.longitude,
            )
        } ?: emptyList(),
    )

    fun orderToDomain(dto: OrderDto): Order = Order(
        id              = dto.id,
        restaurantId    = dto.restaurantId,
        restaurantName  = dto.restaurantName,
        restaurantImage = dto.restaurantImage,
        status          = dto.status,
        timeFriendly    = dto.timeFriendly,
        totalAmount     = dto.totalAmount,
        items           = dto.items.map { i ->
            OrderItem(
                name     = i.name,
                quantity = i.quantity,
                price    = i.price,
            )
        },
        canReorder      = dto.canReorder,
    )
}