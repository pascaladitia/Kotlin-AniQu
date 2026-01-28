package com.pascal.contants


enum class UserType {
    ADMIN,
    USER;

    val isAdmin get() = this in listOf(ADMIN, USER)
    val isRegular get() = this in listOf(USER)

    companion object {
        fun fromString(role: String): UserType? =
            values().find { it.name.equals(role, ignoreCase = true) }
    }
}