package com.homato.routes

import com.homato.data.request.AuthRequest
import com.homato.data.responses.AuthResponse
import com.homato.service.authentication.AuthService
import com.homato.service.authentication.LoginError
import com.homato.service.authentication.RegisterError
import com.homato.service.util.Outcome
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

    post("register") {
        val request = call.runCatching { this.receiveNullable<AuthRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        when (val result = authService.register(request.email, request.password)) {
            is Outcome.Success -> call.respond(HttpStatusCode.Created)
            is Outcome.Failure -> {
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

    post("login") {
        val request = call.runCatching { this.receiveNullable<AuthRequest>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        when (val result = authService.login(request.email, request.password)) {
            is Outcome.Success -> {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = AuthResponse(
                        token = result.data
                    )
                )
            }

            is Outcome.Failure -> {
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

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}