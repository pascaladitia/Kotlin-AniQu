package com.pascal.plugin

import com.pascal.model.request.LoginRequest
import com.pascal.model.request.RegisterRequest
import io.ktor.server.application.*
import io.ktor.server.plugins.requestvalidation.*

fun Application.configureRequestValidation() {
    install(RequestValidation) {
        validate<LoginRequest> { login ->
            login.validation()
            ValidationResult.Valid
        }
        validate<RegisterRequest> { register ->
            register.validation()
            ValidationResult.Valid
        }
    }
}