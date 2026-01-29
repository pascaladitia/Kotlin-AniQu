package com.pascal.feature.auth

import at.favre.lib.crypto.bcrypt.BCrypt
import com.pascal.contants.Message
import com.pascal.contants.UserType
import com.pascal.database.entities.ChangePassword
import com.pascal.database.entities.LoginResponse
import com.pascal.database.entities.UserDao
import com.pascal.database.entities.UserProfileDAO
import com.pascal.database.entities.UserTable
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.model.response.Registration
import com.pascal.utils.CommonException
import com.pascal.utils.PasswordNotMatch
import com.pascal.utils.UserNotExistException
import com.pascal.utils.ValidationException
import com.pascal.utils.ValidationUtils
import com.pascal.utils.extension.notFoundException
import com.pascal.utils.extension.query
import com.pascal.utils.generateOTP
import com.pascal.utils.sendEmail
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

    private fun validateLoginRequest(request: LoginRequest) {
        if (!ValidationUtils.validateEmail(request.email))
            throw ValidationException("Invalid email format")
        if (UserType.fromString(request.userType) == null)
            throw ValidationException("Invalid user type. Must be one of: ADMIN, USER")
        if (request.password.isBlank())
            throw ValidationException("Password cannot be empty")
    }
}