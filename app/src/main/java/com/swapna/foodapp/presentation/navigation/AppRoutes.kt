package com.swapna.foodapp.presentation.navigation

object AppRoutes {
    const val SPLASH = "splash"
    const val LOGIN = "login"
    const val HOME = "home"
    const val SEARCH = "search"
    const val CART = "cart"
    const val PROFILE = "profile"
    const val RESTAURANT = "restaurant/{restaurantId}"
    const val SEARCH_WITH_QUERY = "search?query={query}"
    const val ARG_RESTAURANT_ID = "restaurantId"
    const val ARG_MENU_ITEM_ID = "menuItemId"
    const val ARG_SEARCH_QUERY = "query"
    const val PRODUCT =
        "product/{restaurantId}/{menuItemId}"

    fun restaurant(id: String) = "restaurant/$id"
    fun product(id: String) = "product/$id"
    fun product(
        restaurantId: String,
        menuItemId: String,
    ) = "product/$restaurantId/$menuItemId"

}