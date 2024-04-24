package com.homato.routes.auth

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.runCatching
import com.homato.data.model.request.LoginRequest
import com.homato.routes.COLLECTION_AUTH
import com.homato.routes.VERSION_1
import com.homato.service.authentication.AuthService
import com.homato.service.authentication.LoginError
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

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