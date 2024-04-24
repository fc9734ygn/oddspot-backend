package com.homato.routes.profile

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import com.homato.data.model.request.UsernameChangeRequest
import com.homato.routes.COLLECTION_PROFILE
import com.homato.routes.VERSION_1
import com.homato.routes.getUserId
import com.homato.service.profile.ProfileService
import com.homato.service.profile.UsernameChangeError
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.changeUsername() {
    val profileService: ProfileService by inject()

    authenticate {
        patch("$VERSION_1/$COLLECTION_PROFILE/change-username") {

            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@patch
            }

            val request = call.runCatching { receiveNullable<UsernameChangeRequest>() }.getOrElseNotNull {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }

            val result = profileService.changeUsername(
                username = request.username,
                id = userId
            )

            result.fold(
                success = { call.respond(HttpStatusCode.OK) },
                failure = { error ->
                    when (error) {
                        is UsernameChangeError.InvalidUsername -> call.respond(
                            HttpStatusCode.BadRequest, "Invalid username : ${error.message}"
                        )

                        UsernameChangeError.UsernameAlreadyExists -> call.respond(
                            HttpStatusCode.Conflict, "Username already exists"
                        )

                        UsernameChangeError.UserNotFound -> call.respond(
                            HttpStatusCode.NotFound, "User not found"
                        )

                        UsernameChangeError.Generic -> call.respond(
                            HttpStatusCode.InternalServerError, "Something went wrong"
                        )
                    }
                }
            )
        }
    }
}