package com.pascal.plugin

import com.pascal.feature.auth.AuthService
import com.pascal.feature.auth.authRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRoute() {
    val authController: AuthService by inject()
    routing {
        authRoutes(authController)
    }
}
