package com.pascal.feature.profile

import com.pascal.contants.AppConstants
import com.pascal.database.entities.UserProfileDAO
import com.pascal.database.entities.UserProfileTable
import com.pascal.model.request.UserProfileRequest
import com.pascal.model.response.UserProfile
import com.pascal.utils.extension.notFoundException
import com.pascal.utils.extension.query
import org.jetbrains.exposed.v1.core.eq
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.Path

class ProfileService : ProfileRepository {

    init {
        if (!File(AppConstants.ImageFolder.PROFILE_IMAGE_LOCATION).exists()) {
            File(AppConstants.ImageFolder.PROFILE_IMAGE_LOCATION).mkdirs()
        }
    }

    override suspend fun getProfile(userId: String): UserProfile = query {
        val isProfileExist = UserProfileDAO.find { UserProfileTable.userId eq userId }.toList().singleOrNull()
        isProfileExist?.response() ?: throw userId.notFoundException()
    }

    override suspend fun updateProfile(
        userId: String,
        request: UserProfileRequest?
    ): UserProfile = query{
        val userProfileEntity = UserProfileDAO.find { UserProfileTable.userId eq userId }.toList().singleOrNull()

        userProfileEntity?.let {
            it.firstName = request?.firstName ?: it.firstName
            it.lastName = request?.lastName ?: it.lastName
            it.mobile = request?.mobile ?: it.mobile
            it.streetAddress = request?.streetAddress ?: it.streetAddress
            it.city = request?.city ?: it.city
            it.occupation = request?.occupation ?: it.occupation
            it.postCode = request?.postCode ?: it.postCode
            it.gender = request?.gender ?: it.gender
            it.response()
        } ?: throw userId.notFoundException()
    }

    override suspend fun updateProfileImage(
        userId: String,
        imageUrl: String?
    ): UserProfile = query {
        val userProfileEntity = UserProfileDAO.find { UserProfileTable.userId eq userId }.toList().singleOrNull()

        userProfileEntity?.image?.let {
            Files.deleteIfExists(Paths.get("${AppConstants.ImageFolder.PROFILE_IMAGE_LOCATION}$it"))
        }

        userProfileEntity?.let {
            it.image = imageUrl ?: it.image
            it.response()
        } ?: throw userId.notFoundException()
    }
}