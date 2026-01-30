package com.pascal.plugin

import com.pascal.feature.auth.AuthService
import com.pascal.feature.auth.authRoutes
import com.pascal.feature.profile.ProfileService
import com.pascal.feature.profile.profileRoutes
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRoute() {
    val authController: AuthService by inject()
    val profileController: ProfileService by inject()
    routing {
        authRoutes(authController)
        profileRoutes(profileController)
    }
}
