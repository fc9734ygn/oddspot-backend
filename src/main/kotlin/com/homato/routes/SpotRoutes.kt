package com.homato.routes

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.data.model.request.VisitSpotRequest
import com.homato.service.spot.SpotService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
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

fun Route.spots() {
    val spotService: SpotService by inject()
    authenticate {
        get("v1/spot/spots") {
            val result = spotService.getAllSpots()

            result.fold(
                success = {
                    call.respond(HttpStatusCode.OK, it)
                },
                failure = {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to fetch spots")
                }
            )
        }
    }
}

fun Route.visitSpot() {
    val spotService: SpotService by inject()

    authenticate {
        post("v1/spot/visit-spot") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@post
            }

            val multipartData = call.extractMultipartData<VisitSpotRequest>(
                formDataPartName = "data",
                filePartName = "image"
            ).getOrElse {
                call.respond(HttpStatusCode.BadRequest, it)
                return@post
            }

            val result = spotService.visitSpot(
                filePath = multipartData.file.absolutePath,
                userId = userId,
                spotId = multipartData.formData.id,
                fileContentType = multipartData.contentType
            )

            result.fold(
                success = {
                    call.respond(HttpStatusCode.OK, "Spot visit recorded successfully")
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