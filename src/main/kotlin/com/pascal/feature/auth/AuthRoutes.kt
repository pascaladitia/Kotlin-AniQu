package com.pascal.feature.auth

import com.pascal.database.entities.ChangePassword
import com.pascal.model.request.JwtTokenRequest
import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.plugin.RoleManagement
import com.pascal.utils.ApiResponse
import com.pascal.utils.extension.requiredParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import kotlin.coroutines.Continuation

fun Route.authRoutes(authController: AuthService) {
    route("/auth") {
        post("login") {
            val requestBody = call.receive<LoginRequest>()
            call.respond(
                ApiResponse.success(
                    authController.login(requestBody), HttpStatusCode.OK
                )
            )
        }

        post("register") {
            val requestBody = call.receive<RegisterRequest>()
            call.respond(
                ApiResponse.success(authController.register(requestBody), HttpStatusCode.OK)
            )
        }

        get("otp-verification") {
            val (userId, otp) = call.requiredParameters("userId", "otp") ?: return@get
            call.respond(
                ApiResponse.success(
                    authController.otpVerification(userId, otp), HttpStatusCode.OK
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
                authController.changePassword(loginUser!!.userId, ChangePassword(oldPassword, newPassword)).let {
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
    }
}