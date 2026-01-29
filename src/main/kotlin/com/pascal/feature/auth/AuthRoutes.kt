package com.pascal.feature.auth

import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.utils.ApiResponse
import com.pascal.utils.extension.requiredParameters
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.coroutines.Continuation

fun Route.authRoutes(authController: AuthService, get: (String, (RoutingContext, Continuation<Unit?>) -> Any?) -> Unit) {
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
    }
}