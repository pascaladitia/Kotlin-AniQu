package com.pascal.utils

import com.pascal.contants.UserType
import com.pascal.model.request.JwtTokenRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

object RoleHierarchy {
    val roleHierarchy = mapOf(
        UserType.PREMIUM to setOf(UserType.PREMIUM, UserType.REGULAR),
        UserType.REGULAR to setOf(UserType.REGULAR)
    )

    fun hasAccess(userRole: UserType, resourceRole: UserType): Boolean {
        return roleHierarchy[userRole]?.contains(resourceRole) ?: false
    }
}


suspend fun ApplicationCall.requireRole(role: UserType) {
    val jwtPrincipal = this.principal<JwtTokenRequest>()
    if (jwtPrincipal == null || !jwtPrincipal.hasAccessTo(role)) {
        this.respond(HttpStatusCode.Forbidden, "Access denied")
        return
    }
}
suspend fun ApplicationCall.requireSpecificRole(role: UserType) {
    val jwtPrincipal = this.principal<JwtTokenRequest>()
    if (jwtPrincipal == null || !jwtPrincipal.hasRole(role)) {
        this.respond(HttpStatusCode.Forbidden, "Access denied")
        return
    }
}

object RoleBasedAuth {

    fun isRegular(userType: String): Boolean {
        return UserType.fromString(userType) == UserType.REGULAR
    }

    fun isPremium(userType: String): Boolean {
        return UserType.fromString(userType) == UserType.PREMIUM
    }
}