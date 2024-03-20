package com.homato.routes

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import com.homato.data.model.request.UsernameChangeRequest
import com.homato.routes.util.COLLECTION_PROFILE
import com.homato.routes.util.VERSION_1
import com.homato.routes.util.getUserId
import com.homato.service.profile.ProfileService
import com.homato.service.profile.UsernameChangeError.*
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
                        is InvalidUsername -> call.respond(
                            HttpStatusCode.BadRequest, "Invalid username : ${error.message}"
                        )

                        UsernameAlreadyExists -> call.respond(
                            HttpStatusCode.Conflict, "Username already exists"
                        )

                        UserNotFound -> call.respond(
                            HttpStatusCode.NotFound, "User not found"
                        )

                        Generic -> call.respond(
                            HttpStatusCode.InternalServerError, "Something went wrong"
                        )
                    }
                }
            )
        }
    }
}

fun Route.deleteAccount() {
    val profileService: ProfileService by inject()

    authenticate {
        delete("$VERSION_1/$COLLECTION_PROFILE/delete-account") {

            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@delete
            }

            val result = profileService.deleteAccount(
                userId = userId
            )

            result.fold(
                success = { call.respond(HttpStatusCode.OK) },
                failure = { call.respond(HttpStatusCode.InternalServerError, "Something went wrong") }
            )
        }
    }
}