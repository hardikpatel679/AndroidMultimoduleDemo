package com.hdapp.data.repository

import com.hdapp.data.mapper.toAppError
import com.hdapp.data.remote.api.AuthApi
import com.hdapp.data.remote.model.LoginRequest
import com.hdapp.data.remote.model.toDomain
import com.hdapp.domain.model.User
import com.hdapp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(private val api: AuthApi) : AuthRepository {
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = api.login(LoginRequest(username, password))
            Result.success(response.toDomain())
        } catch (e: Exception) {
            Result.failure(e.toAppError())
        }
    }
}
