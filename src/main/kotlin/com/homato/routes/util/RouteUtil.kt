package com.homato.routes.util

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.runCatching
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import kotlinx.serialization.json.Json
import java.io.File

fun getUserId(call: ApplicationCall): Result<String, Throwable> {
    return runCatching {
        val principal = call.principal<JWTPrincipal>()
        principal!!.getClaim(USER_ID_CLAIM, String::class)!!
    }
}

suspend inline fun <reified T : Any> ApplicationCall.extractMultipartData(
    formDataPartName: String,
    filePartName: String,
    tempDirectory: String = TEMP_DIRECTORY,
    onlyImagesAllowed: Boolean = true
): Result<MultipartData<T>, String> {
    var formData: T? = null
    var tempFile: File? = null
    var contentType: ContentType? = null

    val directory = File(tempDirectory)
    if (!directory.exists()) directory.mkdirs()

    receiveMultipart().forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                if (part.name == formDataPartName) {
                    formData = Json.decodeFromString<T>(part.value)
                }
            }

            is PartData.FileItem -> {
                if (part.name == filePartName) {
                    contentType = part.contentType
                    val fileName = part.originalFileName ?: DEFAULT_FILE_NAME
                    val fileBytes = part.streamProvider().readBytes()
                    tempFile = File.createTempFile(DEFAULT_FILE_PREFIX, fileName, directory).apply {
                        writeBytes(fileBytes)
                    }
                }
            }

            is PartData.BinaryChannelItem, is PartData.BinaryItem -> {
                // Not supported
            }
        }
        part.dispose()
    }

    val immutableFormData = formData ?: return Err("Missing form data")
    val immutableFile = tempFile ?: return Err("Missing file data")
    val immutableContentType = contentType ?: return Err("Missing content type")
    if (contentType?.match(ContentType.Image.Any) == false && onlyImagesAllowed) {
        return Err("Only images are allowed")
    }

    return Ok(MultipartData(immutableFormData, immutableFile, immutableContentType))
}

data class MultipartData<T>(
    val formData: T,
    val file: File,
    val contentType: ContentType
)