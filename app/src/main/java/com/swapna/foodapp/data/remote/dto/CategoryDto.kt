package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.swapna.foodapp.utils.AppConstants
import com.swapna.foodapp.utils.AppConstants.KEY_CATEGORY
import com.swapna.foodapp.utils.AppConstants.KEY_CATEGORY_ID
import com.swapna.foodapp.utils.AppConstants.KEY_CATEGORY_IMAGE
import com.swapna.foodapp.utils.AppConstants.KEY_CATEGORY_NAME
import com.swapna.foodapp.utils.AppConstants.KEY_CITY_ID
import com.swapna.foodapp.utils.AppConstants.KEY_CITY_NAME
import com.swapna.foodapp.utils.AppConstants.KEY_CUISINE
import com.swapna.foodapp.utils.AppConstants.KEY_CUISINES
import com.swapna.foodapp.utils.AppConstants.KEY_CUISINE_ID
import com.swapna.foodapp.utils.AppConstants.KEY_CUISINE_NAME
import com.swapna.foodapp.utils.AppConstants.KEY_ENTITY_ID
import com.swapna.foodapp.utils.AppConstants.KEY_ENTITY_TYPE
import com.swapna.foodapp.utils.AppConstants.KEY_ESTABLISHMENT
import com.swapna.foodapp.utils.AppConstants.KEY_ESTABLISHMENTS
import com.swapna.foodapp.utils.AppConstants.KEY_ESTABLISHMENT_ID
import com.swapna.foodapp.utils.AppConstants.KEY_ESTABLISHMENT_NAME
import com.swapna.foodapp.utils.AppConstants.KEY_LATITUDE
import com.swapna.foodapp.utils.AppConstants.KEY_LOCATION_SUGGESTIONS
import com.swapna.foodapp.utils.AppConstants.KEY_LONGITUDE
import com.swapna.foodapp.utils.AppConstants.KEY_TITLE

data class CategoriesResponse(
    @SerializedName(AppConstants.KEY_CATEGORIES)
    val categories: List<CategoryWrapper>,
)

data class CategoryWrapper(
    @SerializedName(KEY_CATEGORY)
    val category: CategoryDto,
)

data class CategoryDto(
    @SerializedName(KEY_CATEGORY_ID)
    val id: Int,

    @SerializedName(KEY_CATEGORY_NAME)
    val name: String,

    @SerializedName(KEY_CATEGORY_IMAGE)
    val imageUrl: String? = null,
)

data class CuisinesResponse(
    @SerializedName(KEY_CUISINES)
    val cuisines: List<CuisineWrapper>,
)

data class CuisineWrapper(
    @SerializedName(KEY_CUISINE)
    val cuisine: CuisineDto,
)

data class CuisineDto(
    @SerializedName(KEY_CUISINE_ID)
    val id: Int,

    @SerializedName(KEY_CUISINE_NAME)
    val name: String,
)

data class EstablishmentsResponse(
    @SerializedName(KEY_ESTABLISHMENTS)
    val establishments: List<EstablishmentWrapper>,
)

data class EstablishmentWrapper(
    @SerializedName(KEY_ESTABLISHMENT)
    val establishment: EstablishmentDto,
)

data class EstablishmentDto(
    @SerializedName(KEY_ESTABLISHMENT_ID)
    val id: Int,

    @SerializedName(KEY_ESTABLISHMENT_NAME)
    val name: String,
)

data class LocationSuggestionsResponse(
    @SerializedName(KEY_LOCATION_SUGGESTIONS)
    val suggestions: List<LocationSuggestionDto>,
)

data class LocationSuggestionDto(
    @SerializedName(KEY_ENTITY_ID)
    val entityId: Int,

    @SerializedName(KEY_ENTITY_TYPE)
    val entityType: String,

    @SerializedName(KEY_TITLE)
    val title: String,

    @SerializedName(KEY_LATITUDE)
    val latitude: Double,

    @SerializedName(KEY_LONGITUDE)
    val longitude: Double,

    @SerializedName(KEY_CITY_ID)
    val cityId: Int,

    @SerializedName(KEY_CITY_NAME)
    val cityName: String,
)