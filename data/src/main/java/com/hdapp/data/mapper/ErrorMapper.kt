package com.hdapp.data.mapper

import com.hdapp.domain.model.AppError
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

fun Throwable.toAppError(): AppError {
    return when (this) {
        is SocketTimeoutException -> AppError.NetworkError
        is IOException -> AppError.NetworkError
        is HttpException -> {
            when (code()) {
                401 -> AppError.Unauthorized
                in 500..599 -> AppError.ServerError
                else -> AppError.UnknownError(this)
            }
        }
        else -> AppError.UnknownError(this)
    }
}
