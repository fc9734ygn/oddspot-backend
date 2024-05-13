package com.homato.routes.spot

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.homato.data.model.request.SubmitSpotRequest
import com.homato.routes.*
import com.homato.service.spot.SpotService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.submitSpot() {
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
                filePath = multipartData.file!!.absolutePath,
                spotData = multipartData.formData!!,
                creatorId = userId,
                contentType = multipartData.contentType!!
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