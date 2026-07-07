package com.hdapp.domain.model

sealed class AppError {
    data object NetworkError : AppError()
    data object ServerError : AppError()
    data object Unauthorized : AppError()
    data object ValidationError : AppError()
    data class BusinessError(val code: String, val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}
