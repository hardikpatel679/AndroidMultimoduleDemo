package com.hdapp.data.repository

import com.hdapp.data.mapper.toAppError
import com.hdapp.data.remote.api.AuthApiService
import com.hdapp.data.remote.model.LoginRequest
import com.hdapp.data.remote.model.toDomain
import com.hdapp.domain.model.ApiResult
import com.hdapp.domain.model.User
import com.hdapp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val api: AuthApiService) : AuthRepository {
    override suspend fun login(username: String, password: String): ApiResult<User> {
        return try {
            val response = api.login(LoginRequest(username, password))
            ApiResult.Success(response.toDomain())
        } catch (e: Exception) {
            ApiResult.Error(e.toAppError())
        }
    }
}
