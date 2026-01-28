package com.pascal.feature.auth

import com.pascal.contants.Message
import com.pascal.contants.UserType
import com.pascal.database.entities.LoginResponse
import com.pascal.database.entities.UserDao
import com.pascal.database.entities.UserProfileDAO
import com.pascal.database.entities.UserTable
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.model.response.Registration
import com.pascal.utils.CommonException
import com.pascal.utils.ValidationException
import com.pascal.utils.ValidationUtils
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

    override suspend fun login(request: LoginRequest): LoginResponse {
        TODO("Not yet implemented")
    }
}