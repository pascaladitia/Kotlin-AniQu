package com.pascal.feature.auth

import com.pascal.contants.AppConstants
import com.pascal.contants.Message
import com.pascal.contants.UserType
import com.pascal.database.entities.ChangePassword
import com.pascal.model.request.ForgetPasswordRequest
import com.pascal.model.request.JwtTokenRequest
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.model.request.ResetRequest
import com.pascal.plugin.RoleManagement
import com.pascal.utils.ApiResponse
import com.pascal.utils.extension.requiredParameters
import com.pascal.utils.sendEmail
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(controller: AuthService) {
    route("/auth") {
        post("login") {
            val requestBody = call.receive<LoginRequest>()
            call.respond(
                ApiResponse.success(
                    controller.login(requestBody), HttpStatusCode.OK
                )
            )
        }

        post("register") {
            val requestBody = call.receive<RegisterRequest>()
            call.respond(
                ApiResponse.success(controller.register(requestBody), HttpStatusCode.OK)
            )
        }

        get("otp-verification") {
            val (userId, otp) = call.requiredParameters("userId", "otp") ?: return@get
            call.respond(
                ApiResponse.success(
                    controller.otpVerification(userId, otp), HttpStatusCode.OK
                )
            )
        }

        authenticate(
            RoleManagement.ADMIN.role,
            RoleManagement.USER.role
        ) {
            put("change-password") {
                val (oldPassword, newPassword) = call.requiredParameters("oldPassword", "newPassword") ?: return@put
                val loginUser = call.principal<JwtTokenRequest>()
                controller.changePassword(loginUser!!.userId, ChangePassword(oldPassword, newPassword)).let {
                    if (it) call.respond(
                        ApiResponse.success(
                            "Password has been changed", HttpStatusCode.OK
                        )
                    ) else call.respond(
                        ApiResponse.failure(
                            "Old password is wrong", HttpStatusCode.OK
                        )
                    )
                }
            }
        }

        put("forget_password") {
            val (email, userType) = call.requiredParameters("email", "userType") ?: return@put
            val requestBody = ForgetPasswordRequest(email, userType)
            controller.forgetPassword(requestBody).let { otp ->
                sendEmail(requestBody.email, otp)
                call.respond(
                    ApiResponse.success(
                        "${Message.VERIFICATION_CODE_SENT_TO} ${requestBody.email}",
                        HttpStatusCode.OK
                    )
                )
            }
        }

        get("reset-password") {
            val (email, otp, newPassword, userType) = call.requiredParameters(
                "email", "otp", "newPassword", "userType"
            ) ?: return@get

            controller.resetPassword(
                ResetRequest(
                    email, otp, newPassword, userType
                )
            ).let {
                when (it) {
                    AppConstants.DataBaseTransaction.FOUND -> {
                        call.respond(
                            ApiResponse.success(
                                Message.PASSWORD_CHANGE_SUCCESS, HttpStatusCode.OK
                            )
                        )
                    }

                    AppConstants.DataBaseTransaction.NOT_FOUND -> {
                        call.respond(
                            ApiResponse.success(
                                Message.VERIFICATION_CODE_IS_NOT_VALID, HttpStatusCode.OK
                            )
                        )
                    }
                }
            }
        }

        authenticate(RoleManagement.ADMIN.role, RoleManagement.USER.role) {
            put("/{userId}/change-user-type") {
                val (userId) = call.requiredParameters("userId") ?: return@put
                val userTypeParam = call.parameters["userType"] ?: run {
                    call.respond(HttpStatusCode.BadRequest, "userType parameter is required")
                    return@put
                }

                val newType = try {
                    UserType.valueOf(userTypeParam.uppercase())
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, "Invalid userType")
                    return@put
                }

                val currentUser = call.principal<JwtTokenRequest>()
                if (currentUser == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                    return@put
                }

                try {
                    val success = controller.changeUserType(
                        currentUser.userId,
                        userId,
                        newType
                    )

                    if (success) {
                        call.respond(
                            ApiResponse.success(
                                "User type changed successfully to $newType", HttpStatusCode.OK
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponse.failure(
                                "Failed to change user type", HttpStatusCode.BadRequest
                            )
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        ApiResponse.failure(
                            e.message ?: "Error changing user type", HttpStatusCode.BadRequest
                        )
                    )
                }
            }

            put("/{userId}/deactivate") {
                val (userId) = call.requiredParameters("userId") ?: return@put
                val currentUser = call.principal<JwtTokenRequest>()

                if (currentUser == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                    return@put
                }

                try {
                    val success = controller.deactivateUser(
                        currentUser.userId,
                        userId
                    )

                    if (success) {
                        call.respond(
                            ApiResponse.success(
                                "User deactivated successfully", HttpStatusCode.OK
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponse.failure(
                                "Failed to deactivate user", HttpStatusCode.BadRequest
                            )
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        ApiResponse.failure(
                            e.message ?: "Error deactivating user", HttpStatusCode.BadRequest
                        )
                    )
                }
            }

            put("/{userId}/activate") {
                val (userId) = call.requiredParameters("userId") ?: return@put
                val currentUser = call.principal<JwtTokenRequest>()

                if (currentUser == null) {
                    call.respond(HttpStatusCode.Unauthorized, "Unauthorized")
                    return@put
                }

                try {
                    val success = controller.activateUser(
                        currentUser.userId,
                        userId
                    )

                    if (success) {
                        call.respond(
                            ApiResponse.success(
                                "User activated successfully", HttpStatusCode.OK
                            )
                        )
                    } else {
                        call.respond(
                            ApiResponse.failure(
                                "Failed to activate user", HttpStatusCode.BadRequest
                            )
                        )
                    }
                } catch (e: Exception) {
                    call.respond(
                        ApiResponse.failure(
                            e.message ?: "Error activating user", HttpStatusCode.BadRequest
                        )
                    )
                }
            }
        }
    }
}