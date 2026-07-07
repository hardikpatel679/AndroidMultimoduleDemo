package com.hdapp.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.hdapp.domain.model.ApiResult
import com.hdapp.domain.model.AppError
import com.hdapp.domain.model.User
import com.hdapp.domain.repository.AuthRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
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
    fun `invoke starts with Loading`() = runTest {
        val result = loginUseCase("username", "password").first()
        assertThat(result).isInstanceOf(ApiResult.Loading::class.java)
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
        coEvery { repository.login("username", "password") } returns ApiResult.Success(user)

        val result = loginUseCase("username", "password").drop(1).first()

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        assertThat((result as ApiResult.Success).data).isEqualTo(user)
    }

    @Test
    fun `invoke when repository fails returns failure`() = runTest {
        coEvery { repository.login("username", "password") } returns ApiResult.Error(AppError.Unauthorized)

        val result = loginUseCase("username", "password").drop(1).first()

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        assertThat((result as ApiResult.Error).error).isEqualTo(AppError.Unauthorized)
    }
}
