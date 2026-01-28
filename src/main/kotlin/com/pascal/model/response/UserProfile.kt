package com.pascal.model.response


data class UserProfile(
    var userId: String,
    val image: String?,
    val firstName: String?,
    val lastName: String?,
    val mobile: String?,
    val streetAddress: String?,
    val city: String?,
    val occupation: String?,
    val postCode: String?,
    val gender: String?
)
