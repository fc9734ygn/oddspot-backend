package com.homato.routes

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.github.michaelbull.result.runCatching
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.service.spot.SpotService
import com.homato.util.getOrElseNotNull
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.submitSpotRoute() {
    val spotService: SpotService by inject()

    authenticate {
        post("v1/spot/submit-spot") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@post
            }

            val multipartData = call.extractMultipartData<SubmitSpotRequest>(
                formDataPartName = "data",
                filePartName = "image"
            ).getOrElse {
                call.respond(HttpStatusCode.BadRequest, it)
                return@post
            }

            val result = spotService.submitSpot(
                filePath = multipartData.file.absolutePath,
                spotData = multipartData.formData,
                creatorId = userId,
                contentType = multipartData.contentType
            )

            result.fold(
                success = {
                    call.respond(HttpStatusCode.OK, "Spot and image submitted successfully")
                },
                failure = {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to submit spot and image")
                }
            )

            // Cleanup of the temporary file
            multipartData.file.delete()
        }
    }
}

fun Route.unknownSpots() {

    get("v1/spot/unknown-spots") {
        //TODO: Implement

        call.respondText("Hello World!")
    }
}

fun Route.visitedSpots() {

    get("v1/spot/visited-spots") {
        //TODO: Implement

        call.respondText("Hello World!")
    }
}

fun Route.submittedSpots() {
    //TODO: Implement

    get("v1/spot/submitted-spots") {

        call.respondText("Hello World!")
    }
}