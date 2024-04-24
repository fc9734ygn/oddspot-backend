package com.homato.routes.spot

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import com.homato.data.model.request.ReportSpotRequest
import com.homato.routes.COLLECTION_SPOT
import com.homato.routes.VERSION_1
import com.homato.routes.getUserId
import com.homato.service.spot.SpotService
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.reportSpot() {
    val spotService: SpotService by inject()
    authenticate {
        post("$VERSION_1/$COLLECTION_SPOT/report") {

            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@post
            }

            val request = call.runCatching { this.receiveNullable<ReportSpotRequest>() }.getOrElseNotNull {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val result = spotService.reportSpot(
                spotId = request.spotId,
                reporterId = userId,
                reason = request.reason
            )

            result.fold(
                success = {
                    call.respond(HttpStatusCode.OK, it)
                },
                failure = {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to report spot")
                }
            )
        }
    }
}