package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName

// ── Top-level wrapper ──────────────────────────────────────────

data class DailyMenuResponse(
    @SerializedName("daily_menus")
    val dailyMenus: List<DailyMenuWrapper>,
)

data class DailyMenuWrapper(
    @SerializedName("daily_menu")
    val menu: DailyMenuDto,
)

// ── Menu section (one per category e.g. "Biryani", "Starters") ─

data class DailyMenuDto(
    @SerializedName("daily_menu_id")
    val id: String,

    @SerializedName("name")
    val name: String,           // This becomes the category name e.g. "Biryani"

    @SerializedName("dishes")
    val dishes: List<DishWrapper>,
)

data class DishWrapper(
    @SerializedName("dish")
    val dish: DishDto,
)

// ── Individual dish ────────────────────────────────────────────

data class DishDto(
    @SerializedName("dish_id")
    val id: String,

    @SerializedName("name")
    val name: String,

    @SerializedName("price")
    val price: String,              // ⚠️ "249 Rs." — parse with parsePriceString()

    @SerializedName("description")
    val description: String,

    @SerializedName("image_url")
    val imageUrl: String? = null,   // nullable — not always present

    @SerializedName("is_veg")
    val isVeg: Int = 0,             // 1 = veg, 0 = non-veg

    @SerializedName("is_recommended")
    val isRecommended: Int = 0,

    @SerializedName("customisations")
    val customisations: List<CustomisationDto>? = null,
)

// ── Customisation group (e.g. "Size") ─────────────────────────

data class CustomisationDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("name")
    val name: String,               // "Size", "Spice Level"

    @SerializedName("options")
    val options: List<CustomisationOptionDto>,
)

// ── One option inside a customisation (e.g. "Large + ₹80") ───

data class CustomisationOptionDto(
    @SerializedName("id")
    val id: String,

    @SerializedName("label")
    val label: String,              // "Large", "Full Plate"

    @SerializedName("extra_price")
    val extraPrice: Double,         // 0.0 if no extra charge
)