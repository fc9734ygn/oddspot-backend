package com.homato.di

import com.homato.JWT_AUDIENCE
import com.homato.JWT_ISSUER
import com.homato.JWT_SECRET_ENV
import com.homato.service.authentication.token.TokenConfig
import io.ktor.server.application.*

private const val TOKEN_EXPIRATION_DURATION = 365L * 1000L * 60L * 60L * 24L // 1 year

fun Application.createTokenConfig(): TokenConfig = TokenConfig(
    issuer = System.getenv(JWT_ISSUER),
    audience = System.getenv(JWT_AUDIENCE),
    expiresIn = TOKEN_EXPIRATION_DURATION,
    secret = System.getenv(JWT_SECRET_ENV)
)
