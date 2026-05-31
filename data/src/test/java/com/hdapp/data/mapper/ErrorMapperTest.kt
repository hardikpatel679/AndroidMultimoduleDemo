package com.hdapp.data.mapper

import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.AppError
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

class ErrorMapperTest {

    @Test
    fun `IOException maps to NetworkError`() {
        val exception = IOException()
        val result = exception.toAppError()
        assertThat(result).isEqualTo(AppError.NetworkError)
    }

    @Test
    fun `SocketTimeoutException maps to NetworkError`() {
        val exception = SocketTimeoutException()
        val result = exception.toAppError()
        assertThat(result).isEqualTo(AppError.NetworkError)
    }

    @Test
    fun `HttpException 401 maps to Unauthorized`() {
        val response = Response.error<Any>(401, "".toResponseBody(null))
        val exception = HttpException(response)
        val result = exception.toAppError()
        assertThat(result).isEqualTo(AppError.Unauthorized)
    }

    @Test
    fun `HttpException 500 maps to ServerError`() {
        val response = Response.error<Any>(500, "".toResponseBody(null))
        val exception = HttpException(response)
        val result = exception.toAppError()
        assertThat(result).isEqualTo(AppError.ServerError)
    }

    @Test
    fun `AppError returns itself`() {
        val error = AppError.NetworkError
        val result = error.toAppError()
        assertThat(result).isEqualTo(error)
    }

    @Test
    fun `Other exceptions map to UnknownError`() {
        val exception = RuntimeException("Boom")
        val result = exception.toAppError()
        assertThat(result).isInstanceOf(AppError.UnknownError::class.java)
        assertThat((result as AppError.UnknownError).throwable).isEqualTo(exception)
    }
}
