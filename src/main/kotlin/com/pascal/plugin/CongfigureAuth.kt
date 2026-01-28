package com.pascal.plugin

import com.pascal.contants.UserType
import com.pascal.feature.auth.JwtConfig
import com.pascal.model.request.JwtTokenRequest
import com.pascal.utils.RoleHierarchy
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureAuth() {
    install(Authentication) {
        jwt (RoleManagement.ADMIN.role) {
            provideJwtAuthConfig(this, RoleManagement.ADMIN)
        }
        jwt(RoleManagement.USER.role) {
            provideJwtAuthConfig(this, RoleManagement.USER)
        }
    }
}

fun provideJwtAuthConfig(jwtConfig: JWTAuthenticationProvider.Config, userRole: RoleManagement) {
    jwtConfig.verifier(JwtConfig.verifier)
    jwtConfig.validate { call ->
        val userId = call.payload.getClaim("userId").asString()
        val email = call.payload.getClaim("email").asString()
        val userTypeString = call.payload.getClaim("userType").asString()

        val userType = try {
            UserType.valueOf(userTypeString.uppercase())
        } catch (e: IllegalArgumentException) {
            return@validate null
        }

        if (RoleHierarchy.hasAccess(userType, userRole.userType)) {
            JwtTokenRequest(userId, email, userTypeString)
        } else null
    }
}

enum class RoleManagement(val role: String) {
    ADMIN("admin"),
    USER("user");

    val userType: UserType
        get() = when (this) {
            ADMIN -> UserType.ADMIN
            USER -> UserType.USER
        }
}