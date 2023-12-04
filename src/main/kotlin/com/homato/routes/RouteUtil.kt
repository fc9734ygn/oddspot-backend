package com.homato.routes

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun getUserId(call: ApplicationCall): String? {
    val principal = call.principal<JWTPrincipal>()
    return principal?.getClaim("userId", String::class)
}