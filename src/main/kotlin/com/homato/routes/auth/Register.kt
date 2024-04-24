package com.homato.routes.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.runCatching
import com.homato.data.model.request.RegisterRequest
import com.homato.routes.COLLECTION_AUTH
import com.homato.routes.VERSION_1
import com.homato.service.authentication.AuthService
import com.homato.service.authentication.RegisterError
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
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