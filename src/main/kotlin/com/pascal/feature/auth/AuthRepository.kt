package com.pascal.feature.auth

import com.pascal.database.entities.LoginResponse
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest

interface AuthRepository {
    suspend fun register(request: RegisterRequest): Any
    suspend fun login(request: LoginRequest): LoginResponse
}