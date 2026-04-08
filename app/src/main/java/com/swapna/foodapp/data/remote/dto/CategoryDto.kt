package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Categories ────────────────────────────────────────────────

data class CategoriesResponse(
    @SerializedName("categories")
    val categories: List<CategoryWrapper>,
)

data class CategoryWrapper(
    @SerializedName("categories")       // ⚠️ Same key name as outer array — API quirk
    val category: CategoryDto,
)

data class CategoryDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("image_url")
    val imageUrl: String? = null,
)

// ── Cuisines ──────────────────────────────────────────────────

data class CuisinesResponse(
    @SerializedName("cuisines")
    val cuisines: List<CuisineWrapper>,
)

data class CuisineWrapper(
    @SerializedName("cuisine")
    val cuisine: CuisineDto,
)

data class CuisineDto(
    @SerializedName("cuisine_id")
    val id: Int,

    @SerializedName("cuisine_name")
    val name: String,
)

// ── Establishments ────────────────────────────────────────────

data class EstablishmentsResponse(
    @SerializedName("establishments")
    val establishments: List<EstablishmentWrapper>,
)

data class EstablishmentWrapper(
    @SerializedName("establishment")
    val establishment: EstablishmentDto,
)

data class EstablishmentDto(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,
)

// ── Locations ─────────────────────────────────────────────────

data class LocationSuggestionsResponse(
    @SerializedName("location_suggestions")
    val suggestions: List<LocationSuggestionDto>,
)

data class LocationSuggestionDto(
    @SerializedName("entity_id")
    val entityId: Int,

    @SerializedName("entity_type")
    val entityType: String,         // "city", "subzone"

    @SerializedName("title")
    val title: String,              // "Koramangala, Bengaluru"

    @SerializedName("latitude")
    val latitude: Double,

    @SerializedName("longitude")
    val longitude: Double,

    @SerializedName("city_id")
    val cityId: Int,

    @SerializedName("city_name")
    val cityName: String,
)