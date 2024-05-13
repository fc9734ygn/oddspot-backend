package com.homato.routes

import com.github.michaelbull.result.*
import com.homato.routes.MultipartDataError.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import java.io.File

fun getUserId(call: ApplicationCall): Result<String, Throwable> {
    return runCatching {
        val principal = call.principal<JWTPrincipal>()
        principal!!.getClaim(USER_ID_CLAIM, String::class)!!
    }
}

// Make sure to clean up the temporary file after using the multipart data
suspend inline fun <reified T : Any> ApplicationCall.extractMultipartData(
    formDataPartName: String? = null, // If formDataPartName is not null, returned formData will not be null
    filePartName: String,
    tempDirectory: String = TEMP_DIRECTORY,
    onlyImagesAllowed: Boolean = true,
    allowNullFile: Boolean = false // If allowNullFile set to false, the file returned will not be null
): Result<MultipartData<T?>, MultipartDataError> {

    val directory = File(tempDirectory)
    if (!directory.exists()) directory.mkdirs()

    var formData: T? = null
    var tempFile: File? = null
    var contentType: ContentType? = null
    var error: MultipartDataError? = null

    receiveMultipart().forEachPart { part ->
        when (part) {
            is PartData.FormItem -> {
                if (formDataPartName != null && part.name == formDataPartName) {
                    formData = extractFormData<T>(part)
                        .getOrElse {
                            error = it
                            return@forEachPart
                        }
                }
            }

            is PartData.FileItem -> {
                if (part.name == filePartName) {
                    val fileData = extractFileData(part, directory, onlyImagesAllowed, allowNullFile)
                        .getOrElse {
                            error = it
                            return@forEachPart
                        }
                    tempFile = fileData.first
                    contentType = fileData.second
                }
            }

            is PartData.BinaryChannelItem, is PartData.BinaryItem -> {
                error = BinaryDataNotAllowed
                return@forEachPart
            }

        }
        part.dispose()
    }

    if (error != null) return Err(error!!)

    val immutableFile = if (!allowNullFile) {
        tempFile ?: return Err(MissingFileData)
    } else {
        tempFile
    }

    val immutableContentType = if (!allowNullFile) {
        contentType ?: return Err(MissingContentType)
    } else {
        contentType
    }

    return Ok(MultipartData(formData, immutableFile, immutableContentType))
}

inline fun <reified T : Any> extractFormData(
    part: PartData.FormItem,
): Result<T, MultipartDataError> = runCatching {
    Json.decodeFromString<T>(part.value)
}.mapError {
    Generic
}

suspend fun extractFileData(
    part: PartData.FileItem,
    directory: File,
    onlyImagesAllowed: Boolean,
    allowNullFile: Boolean = false
): Result<Pair<File?, ContentType?>, MultipartDataError> = withContext(Dispatchers.IO) {
    runCatching {
        if (onlyImagesAllowed && part.contentType?.match(ContentType.Image.Any) == false) {
            return@withContext Err(OnlyImagesAllowed) // Throw an exception to be caught by runCatching
        }
        val fileName = part.originalFileName ?: DEFAULT_FILE_NAME
        val fileBytes = part.streamProvider().readBytes()
        val file = File.createTempFile(DEFAULT_FILE_PREFIX, fileName, directory).apply {
            writeBytes(fileBytes)
        }
        Pair(file, part.contentType)
    }.mapError {
        Generic
    }
}

data class MultipartData<T>(
    val formData: T,
    val file: File?,
    val contentType: ContentType?
)

sealed class MultipartDataError {
    object MissingFileData : MultipartDataError()
    object MissingContentType : MultipartDataError()
    object OnlyImagesAllowed : MultipartDataError()
    object BinaryDataNotAllowed : MultipartDataError()
    object Generic : MultipartDataError()
}