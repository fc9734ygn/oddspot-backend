package com.homato.routes.spot

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.homato.routes.COLLECTION_SPOT
import com.homato.routes.VERSION_1
import com.homato.routes.getUserId
import com.homato.service.spot.SpotService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.submittedSpots() {
    val spotService: SpotService by inject()

    authenticate {
        get("$VERSION_1/$COLLECTION_SPOT/submitted-spots") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@get
            }

            val result = spotService.getSubmittedSpots(userId)
            result.fold(
                success = {
                    call.respond(HttpStatusCode.OK, it)
                },
                failure = {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to fetch submitted spots")
                }
            )
        }
    }
}