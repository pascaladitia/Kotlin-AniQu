package com.pascal.feature.profile

import com.pascal.model.request.UserProfileRequest
import com.pascal.model.response.UserProfile

interface ProfileRepository {
    suspend fun getProfile(userId: String): UserProfile
    suspend fun updateProfile(userId: String, request: UserProfileRequest?): UserProfile
    suspend fun updateProfileImage(userId: String, imageUrl: String?): UserProfile
}