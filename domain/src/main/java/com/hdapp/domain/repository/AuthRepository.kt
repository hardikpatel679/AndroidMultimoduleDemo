package com.hdapp.domain.repository

import com.hdapp.domain.model.User

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}
