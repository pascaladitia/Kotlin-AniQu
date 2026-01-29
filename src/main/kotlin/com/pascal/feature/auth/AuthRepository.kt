package com.pascal.feature.auth

import com.pascal.database.entities.ChangePassword
import com.pascal.database.entities.LoginResponse
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest

interface AuthRepository {
    suspend fun register(request: RegisterRequest): Any
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun otpVerification(userId: String, otp: String): Boolean
    suspend fun changePassword(userId: String, changePassword: ChangePassword): Boolean
}