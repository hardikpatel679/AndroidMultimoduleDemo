package com.hdapp.domain.usecase

import com.hdapp.domain.model.ApiResult
import com.hdapp.domain.model.User
import com.hdapp.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import javax.inject.Inject

class LoginUseCase @Inject constructor(private val repository: AuthRepository) {
    operator fun invoke(username: String, password: String): Flow<ApiResult<User>> = flow {
        emit(repository.login(username, password))
    }.onStart { emit(ApiResult.Loading) }
        .flowOn(Dispatchers.IO)
}
