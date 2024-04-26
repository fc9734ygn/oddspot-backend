package com.homato.routes.profile

import com.github.michaelbull.result.fold
import com.github.michaelbull.result.getOrElse
import com.homato.routes.*
import com.homato.service.profile.ProfileService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Route.changeAvatar() {
    val profileService: ProfileService by inject()

    authenticate {
        patch("$VERSION_1/$COLLECTION_PROFILE/change-avatar") {

            val userId = getUserId(call).getOrElse {
                call.respond(HttpStatusCode.Unauthorized, "User not authorized")
                return@patch
            }

            val multipartData = call.extractMultipartData<Unit>(
                filePartName = MULTIPART_IMAGE_KEY
            ).getOrElse {
                call.respond(HttpStatusCode.BadRequest, it)
                return@patch
            }

            val result = profileService.changeAvatar(
                userId = userId,
                filePath = multipartData.file.absolutePath,
                fileContentType = multipartData.contentType
            )

            result.fold(
                success = {
                    call.respond(HttpStatusCode.OK, it)
                },
                failure = {
                    call.respond(HttpStatusCode.InternalServerError, "Failed to change the avatar")
                }
            )

            // Cleanup of the temporary file
            multipartData.file.delete()
        }
    }
}