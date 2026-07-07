package com.hdapp.data.repository

import com.google.common.truth.Truth.assertThat
import com.hdapp.data.remote.api.AuthApiService
import com.hdapp.data.remote.model.LoginResponse
import com.hdapp.domain.model.ApiResult
import com.hdapp.domain.model.AppError
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.io.IOException

class AuthRepositoryImplTest {

    private lateinit var api: AuthApiService
    private lateinit var repository: AuthRepositoryImpl

    @Before
    fun setUp() {
        api = mockk()
        repository = AuthRepositoryImpl(api)
    }

    @Test
    fun `login success returns user`() = runTest {
        val response = LoginResponse(
            id = 1,
            username = "test",
            email = "test@example.com",
            firstName = "First",
            lastName = "Last",
            gender = "male",
            image = "",
            accessToken = "token",
            refreshToken = "refresh"
        )
        coEvery { api.login(any()) } returns response

        val result = repository.login("username", "password")

        assertThat(result).isInstanceOf(ApiResult.Success::class.java)
        val user = (result as ApiResult.Success).data
        assertThat(user.username).isEqualTo("test")
        assertThat(user.token).isEqualTo("token")
    }

    @Test
    fun `login network error returns NetworkError`() = runTest {
        coEvery { api.login(any()) } throws IOException("No internet")

        val result = repository.login("username", "password")

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        val error = (result as ApiResult.Error).error
        assertThat(error).isInstanceOf(AppError.NetworkError::class.java)
    }

    @Test
    fun `login unexpected error returns UnknownError`() = runTest {
        coEvery { api.login(any()) } throws RuntimeException("Boom")

        val result = repository.login("username", "password")

        assertThat(result).isInstanceOf(ApiResult.Error::class.java)
        val error = (result as ApiResult.Error).error
        assertThat(error).isInstanceOf(AppError.UnknownError::class.java)
    }
}
