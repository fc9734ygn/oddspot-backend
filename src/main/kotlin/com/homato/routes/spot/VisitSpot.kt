package com.homato.routes.spot

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.homato.data.model.request.VisitSpotRequest
import com.homato.routes.*
import com.homato.service.spot.SpotService
import com.homato.service.spot.VisitSpotError
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

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
                spotId = multipartData.formData!!.id,
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
                        VisitSpotError.SpotVisited -> HttpStatusCode.BadRequest to "Spot visited"
                    }
                    call.respond(errorResponse.first, errorResponse.second)
                }
            )

            // Cleanup of the temporary file
            multipartData.file.delete()
        }
    }
}