package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CollectionsResponse(
    @SerializedName("collections")
    val collections: List<CollectionWrapper>,
)

data class CollectionWrapper(
    @SerializedName("collection")
    val collection: CollectionDto,
)

data class CollectionDto(
    @SerializedName("collection_id")
    val id: Int,

    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("image_url")
    val imageUrl: String,

    @SerializedName("res_count")
    val restaurantCount: Int,

    @SerializedName("discount")
    val discount: String? = null,
)