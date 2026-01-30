package com.pascal.feature.auth

import com.pascal.contants.UserType
import com.pascal.database.entities.ChangePassword
import com.pascal.database.entities.LoginResponse
import com.pascal.model.request.ForgetPasswordRequest
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.model.request.ResetRequest

interface AuthRepository {
    suspend fun register(request: RegisterRequest): Any
    suspend fun login(request: LoginRequest): LoginResponse
    suspend fun otpVerification(userId: String, otp: String): Boolean
    suspend fun changePassword(userId: String, changePassword: ChangePassword): Boolean
    suspend fun forgetPassword(request: ForgetPasswordRequest): String
    suspend fun resetPassword(request: ResetRequest): Int
    suspend fun changeUserType(currentUserId: String, targetUserId: String, newUserType: UserType): Boolean
    suspend fun deactivateUser(currentUserId: String, targetUserId: String): Boolean
    suspend fun activateUser(currentUserId: String, targetUserId: String): Boolean
}