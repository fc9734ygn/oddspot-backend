package com.homato.routes

import com.github.michaelbull.result.*
import com.homato.data.model.request.LoginRequest
import com.homato.data.model.request.RegisterRequest
import com.homato.routes.util.COLLECTION_AUTH
import com.homato.routes.util.VERSION_1
import com.homato.routes.util.getUserId
import com.homato.service.authentication.AuthService
import com.homato.service.authentication.LoginError
import com.homato.service.authentication.RegisterError
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.register() {
    val authService: AuthService by inject()

    post("$VERSION_1/$COLLECTION_AUTH/register") {

        val request = call.runCatching { this.receiveNullable<RegisterRequest>() }.getOrElseNotNull {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val result = authService.register(
            email = request.email,
            password = request.password
        )

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

                    RegisterError.Generic -> call.respond(
                        HttpStatusCode.InternalServerError,
                        "Something went wrong"
                    )
                }
            }
        }
    }
}


fun Route.login() {
    val authService: AuthService by inject()

    post("$VERSION_1/$COLLECTION_AUTH/login") {

        val request = call.runCatching { this.receiveNullable<LoginRequest>() }.getOrElseNotNull {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val result = authService.login(request.email, request.password)

        result.fold(
            success = { loginResponse -> call.respond(HttpStatusCode.OK, loginResponse) },
            failure = { error ->
                when (error) {
                    LoginError.UserNotFound -> call.respond(
                        HttpStatusCode.NotFound, "User not found"
                    )

                    LoginError.InvalidCredentials -> call.respond(
                        HttpStatusCode.Unauthorized, "Invalid credentials"
                    )
                }
            }
        )
    }
}

fun Route.authenticate() {
    authenticate {
        get("$VERSION_1/$COLLECTION_AUTH/authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.secretInfo() {
    authenticate {
        get("$VERSION_1/$COLLECTION_AUTH/secret-info") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@get
            }
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}