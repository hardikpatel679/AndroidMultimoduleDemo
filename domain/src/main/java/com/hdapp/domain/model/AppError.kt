package com.hdapp.domain.model

sealed class AppError : Throwable() {
    object NetworkError : AppError()
    object ServerError : AppError()
    object Unauthorized : AppError()
    data class BusinessError(val code: String, override val message: String) : AppError()
    data class UnknownError(val throwable: Throwable) : AppError()
}
