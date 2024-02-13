package com.homato.routes

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.data.model.request.VisitSpotRequest
import com.homato.service.spot.SpotService
import com.homato.service.spot.VisitSpotError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.submitSpotRoute() {
    val spotService: SpotService by inject()

    authenticate {
        post("$VERSION_1/$COLLECTION_SPOT/submit-spot") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@post
            }

            val multipartData = call.extractMultipartData<SubmitSpotRequest>(
                formDataPartName = MULTIPART_DATA_KEY,
                filePartName = MULTIPART_IMAGE_KEY
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
        get("$VERSION_1/$COLLECTION_SPOT/spots") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@get
            }

            val result = spotService.getSpotsFeed(userId)

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
        post("$VERSION_1/$COLLECTION_SPOT/visit-spot") {
            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@post
            }

            val multipartData = call.extractMultipartData<VisitSpotRequest>(
                formDataPartName = MULTIPART_DATA_KEY,
                filePartName = MULTIPART_IMAGE_KEY
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
                    val errorResponse = when(it){
                        VisitSpotError.Generic -> HttpStatusCode.InternalServerError to "Failed"
                        VisitSpotError.ImageUpload -> HttpStatusCode.InternalServerError to "Failed to upload image"
                        VisitSpotError.SpotInactive -> HttpStatusCode.BadRequest to "Spot is inactive"
                        VisitSpotError.SpotNotFound -> HttpStatusCode.NotFound to "Spot not found"
                        VisitSpotError.SpotRecentlyVisited -> HttpStatusCode.BadRequest to "Spot recently visited"
                    }
                    call.respond(errorResponse.first, errorResponse.second)
                }
            )

            // Cleanup of the temporary file
            multipartData.file.delete()
        }
    }
}

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