package com.swapna.foodapp.presentation.common

import com.swapna.foodapp.utils.AppConstants.UNKNOWN_ERROR

sealed class Resource<out T> {
    data class Success<T>(val data: T) : Resource<T>()
    data class Error(
        val message: String,
        val throwable: Throwable? = null
    ) : Resource<Nothing>()

    object Loading : Resource<Nothing>()

    val isSuccess get() = this is Success
    val isError get() = this is Error
    val isLoading get() = this is Loading

    fun getOrNull(): T? = if (this is Success) data else null
}

fun <T> Result<T>.toResource(): Resource<T> = fold(
    onSuccess = { Resource.Success(it) },
    onFailure = {
        Resource.Error(
            it.message ?: UNKNOWN_ERROR,
            throwable = it
        )
    }
)