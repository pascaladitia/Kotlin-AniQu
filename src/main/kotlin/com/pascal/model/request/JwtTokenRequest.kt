package com.pascal.model.request

import com.pascal.contants.UserType
import com.pascal.utils.RoleBasedAuth
import com.pascal.utils.RoleHierarchy
import io.ktor.server.auth.*

data class JwtTokenRequest(val userId: String, val email: String, val userType: String) : Principal {

    fun hasAccessTo(role: UserType): Boolean {
        val currentUserType = try {
            UserType.valueOf(this.userType.uppercase())
        } catch (e: IllegalArgumentException) {
            return false
        }
        return RoleHierarchy.hasAccess(currentUserType, role)
    }


    fun hasRole(role: UserType): Boolean {
        return try {
            UserType.valueOf(this.userType.uppercase()) == role
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun getUserType(): UserType? {
        return try {
            UserType.valueOf(this.userType.uppercase())
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    fun isRegular(): Boolean = RoleBasedAuth.isRegular(this.userType)

    fun isPremium(): Boolean = RoleBasedAuth.isPremium(this.userType)
}