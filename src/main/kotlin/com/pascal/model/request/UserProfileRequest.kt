package com.pascal.model.request

data class UserProfileRequest(
    val firstName: String?,
    val lastName: String?,
    val mobile: String?,
    val streetAddress: String?,
    val city: String?,
    val occupation: String?,
    val postCode: String?,
    val gender: String?
)