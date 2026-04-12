package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.swapna.foodapp.utils.AppConstants

// Top-level wrapper
data class DailyMenuResponse(
    @SerializedName(AppConstants.KEY_DAILY_MENUS)
    val dailyMenus: List<DailyMenuWrapper>,
)

data class DailyMenuWrapper(
    @SerializedName(AppConstants.KEY_DAILY_MENU)
    val menu: DailyMenuDto,
)

// Menu section
data class DailyMenuDto(
    @SerializedName(AppConstants.KEY_DAILY_MENU_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_NAME)
    val name: String,

    @SerializedName(AppConstants.KEY_DISHES)
    val dishes: List<DishWrapper>,
)

data class DishWrapper(
    @SerializedName(AppConstants.KEY_DISH)
    val dish: DishDto,
)

// Individual dish
data class DishDto(
    @SerializedName(AppConstants.KEY_DISH_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_NAME)
    val name: String,

    @SerializedName(AppConstants.KEY_PRICE)
    val price: String,

    @SerializedName(AppConstants.KEY_DESCRIPTION)
    val description: String,

    @SerializedName(AppConstants.KEY_IMAGE_URL)
    val imageUrl: String? = null,

    @SerializedName(AppConstants.KEY_IS_VEG)
    val isVeg: Int = 0,

    @SerializedName(AppConstants.KEY_IS_RECOMMENDED)
    val isRecommended: Int = 0,

    @SerializedName(AppConstants.KEY_CUSTOMISATIONS)
    val customisations: List<CustomisationDto>? = null,
)

// Customisation group
data class CustomisationDto(
    @SerializedName(AppConstants.KEY_DISH_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_NAME)
    val name: String,

    @SerializedName(AppConstants.KEY_OPTIONS)
    val options: List<CustomisationOptionDto>,
)

data class CustomisationOptionDto(
    @SerializedName(AppConstants.KEY_DISH_ID)
    val id: String,

    @SerializedName(AppConstants.KEY_LABEL)
    val label: String,

    @SerializedName(AppConstants.KEY_EXTRA_PRICE)
    val extraPrice: Double,
)