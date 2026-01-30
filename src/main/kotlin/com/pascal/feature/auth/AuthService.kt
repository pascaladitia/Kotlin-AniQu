package com.pascal.feature.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.pascal.contants.AppConstants
import com.pascal.contants.Message
import com.pascal.contants.UserType
import com.pascal.database.entities.*
import com.pascal.model.request.ForgetPasswordRequest
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.model.request.ResetRequest
import com.pascal.model.response.Registration
import com.pascal.utils.*
import com.pascal.utils.extension.notFoundException
import com.pascal.utils.extension.query
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.core.neq
import java.time.LocalDateTime

class AuthService : AuthRepository {

    override suspend fun register(request: RegisterRequest): Any = query {
        validateRegisterRequest(request)

        val userTypeEnum = try {
            UserType.valueOf(request.userType.uppercase())
        } catch (e: IllegalArgumentException) {
            UserType.USER
        }

        val existingUserSameType =
            UserDao.find { UserTable.email eq request.email and (UserTable.userType eq userTypeEnum) }
                .singleOrNull()

        val existingUserDifferentType =
            UserDao.find { UserTable.email eq request.email and (UserTable.userType neq userTypeEnum) }
                .singleOrNull()

        val otp = generateOTP()
        val now = LocalDateTime.now().plusHours(24)

        if (existingUserSameType != null) {

            if (existingUserSameType.isVerified) {
                throw CommonException(Message.USER_ALREADY_EXIST_WITH_THIS_EMAIL)
            } else {
                if (existingUserSameType.otpExpiry!! < LocalDateTime.now()) {
                    existingUserSameType.otpCode = otp
                    sendEmail(existingUserSameType.email, otp)
                    "${Message.NEW_OTP_SENT_TO} ${existingUserSameType.email}"
                } else {
                    throw CommonException(Message.OTP_ALREADY_SENT_WAIT_UNTIL_EXPIRY)
                }
            }

        } else {

            val inserted = UserDao.new {
                email = request.email
                otpCode = otp
                otpExpiry = now
                password = BCrypt.withDefaults().hashToString(12, request.password.toCharArray())
                userType = userTypeEnum
            }

            UserProfileDAO.new {
                userId = inserted.id
            }

            sendEmail(inserted.email, otp)

            val messageSuffix = if (existingUserDifferentType != null) {
                ". You already have an account as ${existingUserDifferentType.userType}"
            } else {
                ""
            }

            Registration(
                inserted.id.value,
                request.email,
                message = "${Message.OTP_SENT_TO} ${inserted.email}$messageSuffix"
            )
        }
    }

    private fun validateRegisterRequest(request: RegisterRequest) {
        if (!ValidationUtils.validateEmail(request.email))
            throw ValidationException("Invalid email format")
        if (!ValidationUtils.validatePassword(request.password))
            throw ValidationException("Password must be at least 8 characters with at least one letter and one number")
        if (UserType.fromString(request.userType) == null)
            throw ValidationException("Invalid user type. Must be one of: ADMIN, USER")
    }

    override suspend fun login(request: LoginRequest): LoginResponse = query {
        validateLoginRequest(request)

        val userTypeEnum = UserType.fromString(request.userType) ?: run {
            throw request.email.notFoundException()
        }

        val userEntity =
            UserDao.find { UserTable.email eq request.email and (UserTable.userType eq userTypeEnum) }
                .toList().singleOrNull()

        userEntity?.let {
            if (BCrypt.verifyer().verify(
                    request.password.toCharArray(), it.password
                ).verified
            ) {
                if (it.isVerified) {
                    if (it.isActive) {
                        it.loggedInWIthToken()
                    } else {
                        throw CommonException(Message.ACCOUNT_DEACTIVATED)
                    }
                } else {
                    throw CommonException(Message.ACCOUNT_NOT_VERIFIED)
                }
            } else {
                throw PasswordNotMatch()
            }
        } ?: throw request.email.notFoundException()
    }

    override suspend fun otpVerification(userId: String, otp: String): Boolean = query {
        val userEntity = UserDao.find { UserTable.id eq userId }.toList().singleOrNull()
        userEntity?.let {
            if (it.otpCode == otp) {
                it.isVerified = true
                true
            } else {
                false
            }
        } ?: throw UserNotExistException()
    }

    override suspend fun changePassword(userId: String, changePassword: ChangePassword): Boolean = query {
        val userEntity = UserDao.find { UserTable.id eq userId }.toList().singleOrNull()
        userEntity?.let {
            if (BCrypt.verifyer().verify(changePassword.oldPassword.toCharArray(), it.password).verified) {
                // Check if new password is same as old password
                if (changePassword.oldPassword == changePassword.newPassword) {
                    throw CommonException(Message.NEW_PASSWORD_CANNOT_BE_SAME_AS_OLD_PASSWORD)
                }
                it.password = BCrypt.withDefaults().hashToString(12, changePassword.newPassword.toCharArray())
                true
            } else {
                false
            }
        } ?: throw UserNotExistException()
    }

    override suspend fun forgetPassword(request: ForgetPasswordRequest): String = query {
        val userEntities = UserDao.find { UserTable.email eq request.email }.toList()

        if (userEntities.isEmpty()) {
            throw request.email.notFoundException()
        }

        val userTypeEnum = try {
            UserType.valueOf(request.userType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw request.email.notFoundException()
        }

        val specificUser = userEntities.find { it.userType == userTypeEnum }
        specificUser?.let {
            val otp = generateOTP()
            it.otpCode = otp
            otp
        } ?: throw "${request.email} not found for ${request.userType} role".notFoundException()
    }

    override suspend fun resetPassword(request: ResetRequest): Int = query {
        val userEntities = UserDao.find { UserTable.email eq request.email }.toList()

        if (userEntities.isEmpty()) {
            throw request.email.notFoundException()
        }

        val userTypeEnum = try {
            UserType.valueOf(request.userType.uppercase())
        } catch (e: IllegalArgumentException) {
            throw "${request.email} not found for ${request.userType} role".notFoundException()
        }

        val userEntity = userEntities.find { it.userType == userTypeEnum }
            ?: throw "${request.email} not found for ${request.userType} role".notFoundException()

        if (userEntity.otpCode == request.verificationCode) {
            if (BCrypt.verifyer().verify(request.newPassword.toCharArray(),userEntity.password).verified) {
                throw CommonException(Message.NEW_PASSWORD_CANNOT_BE_SAME_AS_CURRENT_PASSWORD)
            }
            userEntity.password = BCrypt.withDefaults().hashToString(12, request.newPassword.toCharArray())
            AppConstants.DataBaseTransaction.FOUND
        } else {
            AppConstants.DataBaseTransaction.NOT_FOUND
        }
    }

    override suspend fun changeUserType(
        currentUserId: String,
        targetUserId: String,
        newUserType: UserType
    ): Boolean = query {
        if (currentUserId.isBlank()) throw ValidationException("Current user ID cannot be blank")
        if (targetUserId.isBlank()) throw ValidationException("Target user ID cannot be blank")

        val currentUser = UserDao.findById(currentUserId) ?: throw UserNotExistException()
        val targetUser = UserDao.findById(targetUserId) ?: throw UserNotExistException()

        if (!RoleHierarchy.canManageUser(currentUser.userType, targetUser.userType)) {
            throw CommonException("Insufficient permission to change user type to $newUserType")
        }

        targetUser.userType = newUserType

        true
    }

    override suspend fun deactivateUser(currentUserId: String, targetUserId: String): Boolean = query {
        val currentUser = UserDao.find { UserTable.id eq currentUserId }.singleOrNull()
            ?: throw UserNotExistException()

        val targetUser = UserDao.find { UserTable.id eq targetUserId }.singleOrNull()
            ?: throw UserNotExistException()

        if (!RoleHierarchy.canManageUser(currentUser.userType, targetUser.userType)) {
            throw CommonException("Insufficient permissions to deactivate user")
        }

        targetUser.isActive = false
        true
    }

    override suspend fun activateUser(currentUserId: String, targetUserId: String): Boolean = query {
        val currentUser = UserDao.find { UserTable.id eq currentUserId }.singleOrNull()
            ?: throw UserNotExistException()

        val targetUser = UserDao.find { UserTable.id eq targetUserId }.singleOrNull()
            ?: throw UserNotExistException()

        if (!RoleHierarchy.canManageUser(currentUser.userType, targetUser.userType)) {
            throw CommonException("Insufficient permissions to activate user")
        }

        targetUser.isActive = true
        true
    }

    private fun validateLoginRequest(request: LoginRequest) {
        if (!ValidationUtils.validateEmail(request.email))
            throw ValidationException("Invalid email format")
        if (UserType.fromString(request.userType) == null)
            throw ValidationException("Invalid user type. Must be one of: ADMIN, USER")
        if (request.password.isBlank())
            throw ValidationException("Password cannot be empty")
    }
}