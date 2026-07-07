package com.hdapp.domain.model

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class AppErrorTest {

    @Test
    fun `BusinessError preserves properties`() {
        val code = "TEST_CODE"
        val message = "Test Message"
        val error = AppError.BusinessError(code, message)
        
        assertThat(error.code).isEqualTo(code)
        assertThat(error.message).isEqualTo(message)
    }

    @Test
    fun `UnknownError preserves throwable`() {
        val throwable = RuntimeException("Root cause")
        val error = AppError.UnknownError(throwable)
        
        assertThat(error.throwable).isEqualTo(throwable)
    }

    @Test
    fun `Verify singleton objects`() {
        assertThat(AppError.NetworkError).isNotNull()
        assertThat(AppError.ServerError).isNotNull()
        assertThat(AppError.Unauthorized).isNotNull()
    }
}
