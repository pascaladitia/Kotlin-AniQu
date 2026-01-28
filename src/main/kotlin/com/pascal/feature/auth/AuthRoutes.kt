package com.pascal.feature.auth

import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import com.pascal.utils.ApiResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

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
    }
}