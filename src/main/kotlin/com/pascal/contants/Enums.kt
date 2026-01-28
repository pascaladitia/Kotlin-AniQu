package com.pascal.contants


enum class UserType {
    PREMIUM,
    REGULAR;

    val isUserPremium get() = this in listOf(PREMIUM, REGULAR)
    val isUserRegular get() = this in listOf(REGULAR)
    val isUserOrHigher get() = true

    companion object {
        fun fromString(role: String): UserType? =
            values().find { it.name.equals(role, ignoreCase = true) }
    }
}