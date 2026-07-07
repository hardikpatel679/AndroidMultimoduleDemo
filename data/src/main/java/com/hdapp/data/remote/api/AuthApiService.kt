package com.hdapp.data.remote.api

import com.hdapp.data.remote.model.LoginRequest
import com.hdapp.data.remote.model.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {
    @POST(Endpoints.LOGIN)
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
