package com.hdapp.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.AppError
import com.hdapp.domain.model.User
import com.hdapp.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LoginUseCaseTest {

    private lateinit var repository: AuthRepository
    private lateinit var loginUseCase: LoginUseCase

    @Before
    fun setUp() {
        repository = mockk()
        loginUseCase = LoginUseCase(repository)
    }

    @Test
    fun `invoke with empty username returns validation error`() = runTest {
        val result = loginUseCase("", "password")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isInstanceOf(AppError.BusinessError::class.java)
        val businessError = exception as AppError.BusinessError
        assertThat(businessError.code).isEqualTo("VALIDATION_ERROR")
    }

    @Test
    fun `invoke with empty password returns validation error`() = runTest {
        val result = loginUseCase("username", "")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isInstanceOf(AppError.BusinessError::class.java)
        val businessError = exception as AppError.BusinessError
        assertThat(businessError.code).isEqualTo("VALIDATION_ERROR")
    }

    @Test
    fun `invoke with blank username returns validation error`() = runTest {
        val result = loginUseCase("   ", "password")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isInstanceOf(AppError.BusinessError::class.java)
    }

    @Test
    fun `invoke with blank password returns validation error`() = runTest {
        val result = loginUseCase("username", "   ")

        assertThat(result.isFailure).isTrue()
        val exception = result.exceptionOrNull()
        assertThat(exception).isInstanceOf(AppError.BusinessError::class.java)
    }

    @Test
    fun `invoke with valid credentials returns success`() = runTest {
        val user = User(
            id = 1,
            username = "test",
            email = "test@example.com",
            firstName = "First",
            lastName = "Last",
            gender = "male",
            image = "",
            token = "token",
            refreshToken = "refresh"
        )
        coEvery { repository.login("username", "password") } returns Result.success(user)

        val result = loginUseCase("username", "password")

        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()).isEqualTo(user)
    }

    @Test
    fun `invoke when repository fails returns failure`() = runTest {
        val error = AppError.Unauthorized
        coEvery { repository.login("username", "password") } returns Result.failure(error)

        val result = loginUseCase("username", "password")

        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()).isEqualTo(error)
    }
}
