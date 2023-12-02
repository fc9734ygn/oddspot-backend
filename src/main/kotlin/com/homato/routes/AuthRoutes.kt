package com.homato.routes

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.runCatching
import com.homato.data.request.AuthRequest
import com.homato.data.responses.AuthResponse
import com.homato.service.authentication.AuthService
import com.homato.service.authentication.LoginError
import com.homato.service.authentication.RegisterError
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.register() {
    val authService: AuthService by inject()

    post("v1/register") {

        val request = call.runCatching { this.receiveNullable<AuthRequest>() }.getOrElseNotNull {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val result = authService.register(request.email, request.password)

        when (result) {
            is Ok -> call.respond(HttpStatusCode.Created)
            is Err -> {
                when (result.error) {
                    RegisterError.InvalidEmail -> call.respond(
                        HttpStatusCode.BadRequest, "Invalid email"
                    )

                    RegisterError.InvalidPassword -> call.respond(
                        HttpStatusCode.BadRequest, "Invalid password"
                    )

                    RegisterError.UserAlreadyExists -> call.respond(
                        HttpStatusCode.Conflict, "User already exists"
                    )
                }
            }
        }
    }
}


fun Route.login() {
    val authService: AuthService by inject()

    post("v1/login") {

        val request = call.runCatching { this.receiveNullable<AuthRequest>() }.getOrElseNotNull {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val result = authService.login(request.email, request.password)

        when (result) {
            is Ok -> {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = AuthResponse(
                        token = result.value
                    )
                )
            }

            is Err -> {
                when (result.error) {
                    LoginError.UserNotFound -> call.respond(
                        HttpStatusCode.NotFound, "User not found"
                    )

                    LoginError.InvalidCredentials -> call.respond(
                        HttpStatusCode.Unauthorized, "Invalid credentials"
                    )
                }
            }
        }
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.secretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}