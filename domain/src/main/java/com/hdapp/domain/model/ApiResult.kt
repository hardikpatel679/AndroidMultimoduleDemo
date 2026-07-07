package com.hdapp.domain.model

sealed class ApiResult<out T> {
    data object Loading : ApiResult<Nothing>()
    data class Success<out T>(val data: T) : ApiResult<T>()
    data class Error(val error: AppError) : ApiResult<Nothing>()
}
