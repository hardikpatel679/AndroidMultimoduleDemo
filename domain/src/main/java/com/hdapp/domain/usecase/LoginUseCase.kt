package com.hdapp.domain.usecase

import com.hdapp.domain.model.AppError
import com.hdapp.domain.model.User
import com.hdapp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        if (username.isBlank() || password.isBlank()) {
            return Result.failure(AppError.BusinessError("VALIDATION_ERROR", "Username and password cannot be empty"))
        }
        return repository.login(username, password)
    }
}
