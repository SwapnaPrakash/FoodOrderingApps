package com.swapna.foodapp.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.swapna.foodapp.utils.AppConstants

data class CollectionsResponse(
    @SerializedName(AppConstants.KEY_COLLECTIONS)
    val collections: List<CollectionWrapper>,
)

data class CollectionWrapper(
    @SerializedName(AppConstants.KEY_COLLECTION)
    val collection: CollectionDto,
)

data class CollectionDto(
    @SerializedName(AppConstants.KEY_COLLECTION_ID)
    val id: Int,

    @SerializedName(AppConstants.KEY_COLLECTION_TITLE)
    val title: String,

    @SerializedName(AppConstants.KEY_COLLECTION_DESCRIPTION)
    val description: String,

    @SerializedName(AppConstants.KEY_COLLECTION_IMAGE)
    val imageUrl: String,

    @SerializedName(AppConstants.KEY_COLLECTION_RES_COUNT)
    val restaurantCount: Int,

    @SerializedName(AppConstants.KEY_COLLECTION_DISCOUNT)
    val discount: String? = null,
)