package com.hdapp.data.mapper

import com.hdapp.domain.model.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toAppError(): AppError {
    return when (this) {
        is IOException -> AppError.NetworkError
        is SocketTimeoutException -> AppError.NetworkError
        is HttpException -> {
            when (code()) {
                401 -> AppError.Unauthorized
                in 500..599 -> AppError.ServerError
                else -> AppError.UnknownError(this)
            }
        }
        is AppError -> this
        else -> AppError.UnknownError(this)
    }
}
