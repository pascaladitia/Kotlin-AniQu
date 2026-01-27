package com.pascal.contants


enum class UserType {
    SUPER_ADMIN,
    ADMIN,
    SELLER,
    CUSTOMER;

    val isAdminOrHigher get() = this in listOf(SUPER_ADMIN, ADMIN)
    val isSellerOrHigher get() = this in listOf(SUPER_ADMIN, ADMIN, SELLER)
    val isCustomerOrHigher get() = true // All roles can act as customers

    companion object {
        fun fromString(role: String): UserType? =
            values().find { it.name.equals(role, ignoreCase = true) }
    }
}