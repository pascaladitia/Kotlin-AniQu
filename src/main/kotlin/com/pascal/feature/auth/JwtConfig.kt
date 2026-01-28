package com.pascal.feature.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.pascal.config.DotEnvConfig
import com.pascal.model.request.JwtTokenRequest
import java.util.*

object JwtConfig {
    private lateinit var secret: String
    private lateinit var issuer: String
    private lateinit var algorithm: Algorithm
    private const val VALIDITY_MS = 24 * 60 * 60 * 1000

    lateinit var verifier: JWTVerifier
        private set

    fun init() {
        secret = DotEnvConfig.jwtSecret
        issuer = DotEnvConfig.jwtIssuer
        algorithm = Algorithm.HMAC512(secret)
        verifier = JWT.require(algorithm).withIssuer(issuer).build()

    }

    fun tokenProvider(jwtTokenBody: JwtTokenRequest): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(issuer)
        .withClaim("email", jwtTokenBody.email)
        .withClaim("userId", jwtTokenBody.userId)
        .withClaim("userType", jwtTokenBody.userType)
        .withExpiresAt(getExpiration())
        .sign(algorithm)

    private fun getExpiration() = Date(System.currentTimeMillis() + VALIDITY_MS)
}