package com.homato.routes.profile

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.homato.routes.COLLECTION_PROFILE
import com.homato.routes.VERSION_1
import com.homato.routes.getUserId
import com.homato.service.profile.ProfileService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

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