package com.swapna.foodapp.data.mapper

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.swapna.foodapp.data.local.entity.CartItemEntity
import com.swapna.foodapp.data.local.entity.MenuItemEntity
import com.swapna.foodapp.data.local.entity.RestaurantEntity
import com.swapna.foodapp.data.local.entity.UserEntity
import com.swapna.foodapp.domain.model.Address
import com.swapna.foodapp.domain.model.CartItem
import com.swapna.foodapp.domain.model.Customisation
import com.swapna.foodapp.domain.model.CustomisationOption
import com.swapna.foodapp.domain.model.MenuItem
import com.swapna.foodapp.domain.model.Restaurant
import com.swapna.foodapp.domain.model.User
import javax.inject.Inject

class EntityMapper @Inject constructor() {

    private val gson = Gson()

    // Restaurant
    fun restaurantToDomain(entity: RestaurantEntity): Restaurant = Restaurant(
        id = entity.id,
        name = entity.name,
        imageUrl = entity.imageUrl,
        thumbUrl = entity.thumbUrl,
        rating = entity.rating,
        ratingText = entity.ratingText,
        ratingColor = entity.ratingColor,
        totalVotes = entity.totalVotes,
        avgDeliveryTime = entity.avgDeliveryTime,
        deliveryFee = entity.deliveryFee,
        minOrder = entity.minOrder,
        cuisines = fromJsonStringList(entity.cuisinesJson),
        address = entity.address,
        locality = entity.locality,
        isOpen = entity.isOpen,
        hasDelivery = entity.hasDelivery,
        offers = fromJsonStringList(entity.offersJson),
        avgCostForTwo = entity.avgCostForTwo,
        distanceKm      = entity.distanceKm,
        phoneNumber     = entity.phoneNumber,
        openingHours    = entity.openingHours,
        highlights      = fromJsonStringList(entity.highlightsJson),
        knownFor        = entity.knownFor,
    )

    fun restaurantToEntity(domain: Restaurant): RestaurantEntity = RestaurantEntity(
        id = domain.id,
        name = domain.name,
        imageUrl = domain.imageUrl,
        thumbUrl = domain.thumbUrl,
        rating = domain.rating,
        ratingText = domain.ratingText,
        ratingColor = domain.ratingColor,
        totalVotes = domain.totalVotes,
        avgDeliveryTime = domain.avgDeliveryTime,
        deliveryFee = domain.deliveryFee,
        minOrder = domain.minOrder,
        cuisinesJson = toJsonStringList(domain.cuisines),
        address = domain.address,
        locality = domain.locality,
        isOpen = domain.isOpen,
        hasDelivery = domain.hasDelivery,
        offersJson = toJsonStringList(domain.offers),
        avgCostForTwo = domain.avgCostForTwo,
        distanceKm      = domain.distanceKm,
        phoneNumber     = domain.phoneNumber,
        openingHours    = domain.openingHours,
        highlightsJson  = toJsonStringList(domain.highlights),
        knownFor        = domain.knownFor,
        )

    // Cart Item
    fun cartItemToEntity(domain: CartItem): CartItemEntity {
        val menuItemJson = gson.toJson(domain.menuItem)
        val customisationsJson = gson.toJson(domain.selectedCustomisations)
        return CartItemEntity(
            id = domain.id,
            menuItemId = domain.menuItem.id,
            menuItemJson = menuItemJson,
            quantity = domain.quantity,
            customisationsJson = customisationsJson,
        )
    }

    fun cartItemToDomain(entity: CartItemEntity): CartItem {
        val menuItem = gson.fromJson(entity.menuItemJson, MenuItem::class.java)
        val customisationType =
            object : TypeToken<List<CustomisationOption>>() {}.type
        val customisations: List<CustomisationOption> =
            gson.fromJson(entity.customisationsJson, customisationType)
                ?: emptyList()
        return CartItem(
            id = entity.id,
            menuItem = menuItem,
            quantity = entity.quantity,
            selectedCustomisations = customisations,
        )
    }

    // Menu Item
    fun menuItemToEntity(domain: MenuItem): MenuItemEntity {
        val customisationsJson = gson.toJson(domain.customisations)
        return MenuItemEntity(
            id = domain.id,
            restaurantId = domain.restaurantId,
            name = domain.name,
            description = domain.description,
            price = domain.price,
            imageUrl = domain.imageUrl,
            category = domain.category,
            isVeg = domain.isVeg,
            isRecommended = domain.isRecommended,
            isBestseller       = domain.isBestseller,
            isAvailable        = domain.isAvailable,
            customisationsJson = customisationsJson,
        )
    }

    fun menuItemToDomain(entity: MenuItemEntity): MenuItem {
        val customisationType =
            object : TypeToken<List<Customisation>>() {}.type
        val customisations: List<Customisation> =
            gson.fromJson(entity.customisationsJson, customisationType)
                ?: emptyList()
        return MenuItem(
            id = entity.id,
            restaurantId = entity.restaurantId,
            name = entity.name,
            description = entity.description,
            price = entity.price,
            imageUrl = entity.imageUrl,
            category = entity.category,
            isVeg = entity.isVeg,
            isRecommended = entity.isRecommended,
            customisations = customisations,
            isBestseller   = entity.isBestseller,
            isAvailable    = entity.isAvailable,
        )
    }

    // User
    fun userToEntity(domain: User): UserEntity = UserEntity(
        id = domain.id,
        name = domain.name,
        email = domain.email,
        phone = domain.phone,
        profileImage = domain.profileImage,
        addressesJson = gson.toJson(domain.addresses),
        selectedLocation = domain.selectedLocation,
    )

    fun userToDomain(entity: UserEntity): User {
        val addressType = object : TypeToken<List<Address>>() {}.type
        val addresses: List<Address> =
            gson.fromJson(entity.addressesJson, addressType) ?: emptyList()
        return User(
            id = entity.id,
            name = entity.name,
            email = entity.email,
            phone = entity.phone,
            profileImage = entity.profileImage,
            addresses = addresses,
            selectedLocation = entity.selectedLocation,
        )
    }

    // Helpers
    private fun toJsonStringList(list: List<String>): String =
        gson.toJson(list)

    private fun fromJsonStringList(json: String): List<String> {
        val type = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }
}